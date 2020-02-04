package com.bematechus.kdslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/3/14 0014.
 */
public class KDSPreferenceBGFGPicker extends Preference {

    //private final boolean supportsAlpha;
    View m_viewBind = null;


    KDSBGFG m_valueBGFG = new KDSBGFG();

    public KDSPreferenceBGFGPicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_valueBGFG = KDSBGFG.parseString(defaultVal);// KDSViewFontFace.parseString(defaultVal);

        setWidgetLayoutResource(R.layout.kdsui_bgfg_picker_pref_widget);
    }

    @Override protected void onBindView(View view) {
        super.onBindView(view);
        m_viewBind = view;
        // Set our custom views inside the layout
        final TextView box =(TextView) view.findViewById(R.id.txtDemo);
        if (box != null) {



            box.setBackgroundColor(m_valueBGFG.getBG());
            //box.setBackgroundColor(Color.RED);
            box.setTextColor(m_valueBGFG.getFG());

            box.invalidate();
        }
        view.invalidate();
    }
    public void refreshDemo()
    {

    }
    @Override protected void onClick() {
        if (KDSUIBGFGPickerDialog.g_instance != null) return;
        new KDSUIBGFGPickerDialog(getContext(), m_valueBGFG, new KDSUIBGFGPickerDialog.OnBGFGPickerDlgListener() {
            @Override
            public void onCancel(KDSUIBGFGPickerDialog dialog) {

            }

            @Override
            public void onOk(KDSUIBGFGPickerDialog dialog, KDSBGFG ff) {

                if (!callChangeListener(ff)) return; // They don't want the value to be set
                m_valueBGFG.copyFrom(ff);

                persistString(m_valueBGFG.toString());
                notifyChanged();
                KDSPreferenceBGFGPicker.this.refreshDemo();
            }
        }).show();
    }

    public void forceSetValue(KDSBGFG value) {
        this.m_valueBGFG = value;
        persistString(value.toString());
        notifyChanged();
    }

    @Override protected Object onGetDefaultValue(TypedArray a, int index) {
        // This preference type's value type is Integer, so we read the default value from the attributes as an Integer.
        //KDSViewFontFace ff = new KDSViewFontFace();
        //      return m_valueFF;
        String s = a.getString(index);
        KDSBGFG ff = KDSBGFG.parseString(s);
        return ff;
    }

    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) { // Restore state
            String s = getPersistedString(m_valueBGFG.toString());
            m_valueBGFG = KDSBGFG.parseString(s);
        } else { // Set state
            KDSBGFG val = (KDSBGFG) defaultValue;
            this.m_valueBGFG = val;
            persistString(m_valueBGFG.toString());
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
        myState.value = m_valueBGFG;
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
        this.m_valueBGFG = myState.value;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        KDSBGFG value = new KDSBGFG();

        public SavedState(Parcel source) {
            super(source);
            value = KDSBGFG.parseString( source.readString());
        }

        @Override public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value.toString());
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
