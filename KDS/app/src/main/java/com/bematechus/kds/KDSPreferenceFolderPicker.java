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
 * Created by Administrator on 2017/11/23.
 */
public class KDSPreferenceFolderPicker extends Preference {
    //private final boolean supportsAlpha;
    String value;
    String m_strSummary = "";
    public KDSPreferenceFolderPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        m_strSummary = this.getSummary().toString();
        //m_strSummary = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "summary");
        init_summary();
    }


    @Override protected void onBindView(View view) {
        super.onBindView(view);

        init_summary();
    }

    public void init_summary()
    {
        if (m_strSummary.isEmpty())
            this.setSummary(value);
        else
        {
            String s = m_strSummary;
            s += "\n";
            s += value;
            this.setSummary(s);
        }
    }

    OpenFileDialog m_openFileDlg = null;
    @Override protected void onClick() {
        if (m_openFileDlg != null ) return;
        m_openFileDlg = new OpenFileDialog(getContext(),".png;", new KDSUIDialogBase.KDSDialogBaseListener() {
            public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj) {
                String s = (String)obj;
                if (!callChangeListener(s)) return; // They don't want the value to be set
                value = s;
                persistString(value);
                notifyChanged();
                m_openFileDlg = null;
                init_summary();
            }

            public void onKDSDialogCancel(KDSUIDialogBase dialog) {
                // nothing to do
                m_openFileDlg = null;
            }
        }, OpenFileDialog.Mode.Choose_Folder);
        m_openFileDlg.show();
    }

    public void forceSetValue(String val) {
        this.value = val;
        persistString(val);
        notifyChanged();
        init_summary();
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
        init_summary();
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
        init_summary();
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
        init_summary();
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