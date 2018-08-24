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
 * Created by Administrator on 2015/8/27 0027.
 */

public class KDSPreferenceFontPicker extends Preference {
    //private final boolean supportsAlpha;
    View m_viewBind = null;
    int value;
    KDSViewFontFace m_valueFF = new KDSViewFontFace();
    boolean m_bSupportColorSelection = false;
    public KDSPreferenceFontPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
//        int ncount = attrs.getAttributeCount();
//        String name, value;
//        for (int i=0; i< ncount; i++)
//        {
//            name = attrs.getAttributeName(i);
//            value = attrs.getAttributeValue(i);
//        }
        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_valueFF = KDSViewFontFace.parseString(defaultVal);
        //final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.AmbilWarnaPreference);
        //supportsAlpha = ta.getBoolean(R.styleable.AmbilWarnaPreference_supportsAlpha, false);
        final TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.KDSPreferenceFontPicker);//.AmbilWarnaPreference);
        m_bSupportColorSelection = ta.getBoolean(R.styleable.KDSPreferenceFontPicker_supportsColor, false);

        setWidgetLayoutResource(R.layout.kdsui_font_picker_pref_widget);
    }

    @Override protected void onBindView(View view) {
        super.onBindView(view);
        m_viewBind = view;
        // Set our custom views inside the layout
        final TextView box =(TextView) view.findViewById(R.id.txtDemo);
        if (box != null) {


            box.setTypeface(m_valueFF.getTypeFace());
            box.setTextSize(m_valueFF.getFontSize());

            box.setBackgroundColor(m_valueFF.getBG());
            box.setTextColor(m_valueFF.getFG());
            //box.setBackgroundColor(Color.RED);
            box.invalidate();
        }
        view.invalidate();
    }
    public void refreshDemo()
    {

    }
    @Override protected void onClick() {

        if (KDSUIFontPickerDialog.g_instance != null)
            return;

        new KDSUIFontPickerDialog(getContext(), m_valueFF, m_bSupportColorSelection/*supportsAlpha*/, new KDSUIFontPickerDialog.OnFontPickerDlgListener() {
            @Override public void onOk(KDSUIFontPickerDialog dialog, KDSViewFontFace ff) {
                if (!callChangeListener(ff)) return; // They don't want the value to be set
                m_valueFF.copyFrom( ff);
                m_valueFF.resetTyptFace();
                persistString(m_valueFF.toString());
                notifyChanged();
                KDSPreferenceFontPicker.this.refreshDemo();

            }

            @Override public void onCancel(KDSUIFontPickerDialog dialog) {
                // nothing to do
            }
        }).show();
    }

    public void forceSetValue(KDSViewFontFace value) {
        this.m_valueFF = value;
        persistString(value.toString());
        notifyChanged();
    }

    @Override protected Object onGetDefaultValue(TypedArray a, int index) {
        // This preference type's value type is Integer, so we read the default value from the attributes as an Integer.
        //KDSViewFontFace ff = new KDSViewFontFace();
  //      return m_valueFF;
        String s = a.getString(index);
        KDSViewFontFace ff = KDSViewFontFace.parseString(s);
        return ff;
    }

    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) { // Restore state
            String s = getPersistedString(m_valueFF.toString());
            m_valueFF = KDSViewFontFace.parseString(s);
        } else { // Set state
            KDSViewFontFace val = (KDSViewFontFace) defaultValue;
            this.m_valueFF = val;
            persistString(m_valueFF.toString());
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
        myState.value = m_valueFF;
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
        this.m_valueFF = myState.value;
        notifyChanged();
    }

    /**
     * SavedState, a subclass of {@link BaseSavedState}, will store the state
     * of MyPreference, a subclass of Preference.
     * <p>
     * It is important to always call through to super methods.
     */
    private static class SavedState extends BaseSavedState {
        KDSViewFontFace value = new KDSViewFontFace();

        public SavedState(Parcel source) {
            super(source);
            value = KDSViewFontFace.parseString( source.readString());
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

