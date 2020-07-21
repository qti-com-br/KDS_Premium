package com.bematechus.kds;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.OpenFileDialog;

/**
 * Created by Administrator on 2017/11/22.
 */
public class KDSPreferenceIconPicker extends Preference {
    //private final boolean supportsAlpha;
    String value;

    public KDSPreferenceIconPicker(Context context, AttributeSet attrs) {
        super(context, attrs);


        setWidgetLayoutResource(R.layout.kdsui_icon_picker_pref_widget);
    }

    @Override protected void onBindView(View view) {
        super.onBindView(view);

        // Set our custom views inside the layout
        final ImageView box = (ImageView) view.findViewById(R.id.imgIcon);
        if (box != null) {
            String fileName = value;
            Bitmap bm = BitmapFactory.decodeFile(fileName);
            box.setImageBitmap(bm);

        }
    }
    OpenFileDialog m_openFileDlg = null;
    @Override protected void onClick() {
        if (m_openFileDlg != null ) return;
        m_openFileDlg = new OpenFileDialog(getContext(),".png;.jpg;.bmp;", new KDSUIDialogBase.KDSDialogBaseListener() {
             public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {
                 String s = (String)obj;
                if (!callChangeListener(s)) return; // They don't want the value to be set
                value = s;
                persistString(value);
                notifyChanged();
                 m_openFileDlg = null;
            }

            public void onKDSDialogCancel(KDSUIDialogBase dialog) {
                // nothing to do
                m_openFileDlg = null;
            }
        }, OpenFileDialog.Mode.Choose_File);
        m_openFileDlg.show();
    }

    public void forceSetValue(String val) {
        this.value = val;
        persistString(val);
        notifyChanged();
    }

    @Override protected Object onGetDefaultValue(TypedArray a, int index) {
        // This preference type's value type is Integer, so we read the default value from the attributes as an Integer.
        return a.getString(index);

    }

    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) { // Restore state
            value = getPersistedString(value);
        } else { // Set state
            String val = (String) defaultValue;
            this.value = val;
            persistString(value);
        }
    }

    /*
     * Suppose a client uses this preference type without persisting. We
     * must save the instance state so it is able to, for example, survive
     * orientation changes.
     */
    @Override protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) return superState; // No need to save instance state since it's persistent

        final SavedState myState = new SavedState(superState);
        myState.value = value;
        return myState;
    }

    @Override protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        // Restore the instance state
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        this.value = myState.value;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        String value;

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        @Override public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused") public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}