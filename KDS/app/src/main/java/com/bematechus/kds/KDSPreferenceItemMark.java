package com.bematechus.kds;

import android.content.ClipData;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * Created by Administrator on 2017/11/24.
 */
public class KDSPreferenceItemMark extends DialogPreference implements KDSUIDialogBase.KDSDialogBaseListener {
    private PreferenceActivity parent;

    private String m_strMarkSetting = ""; //format,value
    private  String m_strKey = "";

    private TextView m_txtColor = null;
    private  TextView m_txtString = null;
    private ImageView m_imgIcon = null;

    private String m_strDefaultValue = "";
    public KDSPreferenceItemMark(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    public KDSPreferenceItemMark(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }
    private void init(Context context, AttributeSet attrs)
    {
        setWidgetLayoutResource(R.layout.preference_item_mark);
        m_strKey = this.getKey();
        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_strDefaultValue = defaultVal;
        m_strMarkSetting = defaultVal;
        init_summary(context);

    }
    private void init_summary(Context context)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());

        m_strMarkSetting = pref.getString(m_strKey, "");

        ItemMark mark = ItemMark.parseString(m_strMarkSetting);
        this.setSummary(mark.getDescription());


    }

    void setActivity(PreferenceActivity parent) {
        this.parent = parent;
    }

    @Override
    public boolean isPersistent() {

        return true;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        init_summary(this.getContext());
        m_txtColor = (TextView)view.findViewById(R.id.txtColor);
        m_txtString = (TextView)view.findViewById(R.id.txtString);
        m_imgIcon = (ImageView)view.findViewById(R.id.imgIcon);
        updateUI();

    }

    @Override
    protected void showDialog (Bundle state)
    {


        ItemMark itemMark =  ItemMark.parseString(m_strMarkSetting);
        itemMark.setMarkType(ItemMark.getMarkType(m_strKey));

        KDSUIDlgItemMark dlg = new KDSUIDlgItemMark(this.getContext(),itemMark, this);
        dlg.show();


    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        //String str = (String)obj;
        ItemMark m = (ItemMark)obj;//  ItemMark.parseString(str);
        String str = m.toString();
        this.setSummary(m.getDescription());
        SharedPreferences.Editor editor = this.getSharedPreferences().edit();
        editor.putString(m_strKey, str);
        editor.apply();
        editor.commit();
        m_strMarkSetting = str;
        updateUI();
    }

    @Override protected Object onGetDefaultValue(TypedArray a, int index) {
        // This preference type's value type is Integer, so we read the default value from the attributes as an Integer.
        //KDSViewFontFace ff = new KDSViewFontFace();
        //      return m_valueFF;
        String s = a.getString(index);

        return s;
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
        this.m_strMarkSetting = myState.value;
        notifyChanged();
    }

    @Override protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) { // Restore state
            String s = getPersistedString(m_strMarkSetting);
            m_strMarkSetting = s;

        } else { // Set state
            String val = (String) defaultValue;
            this.m_strMarkSetting = val;
            persistString(m_strMarkSetting);
        }
    }

    public void updateUI()
    {
        ItemMark itemMark = ItemMark.parseString(m_strMarkSetting);
        switch (itemMark.getFormat())
        {

            case Icon:
                m_imgIcon.setVisibility(View.VISIBLE);
                m_txtColor.setVisibility(View.GONE);
                m_txtString.setVisibility(View.GONE);
                m_imgIcon.setImageResource(ItemMark.getIconResID( itemMark.getInternalIcon()));
                break;
            case Char:
                m_imgIcon.setVisibility(View.GONE);
                m_txtColor.setVisibility(View.GONE);
                m_txtString.setVisibility(View.VISIBLE);
                m_txtString.setText(itemMark.getMarkString());
                break;
            case Color:
                m_imgIcon.setVisibility(View.GONE);
                m_txtColor.setVisibility(View.VISIBLE);
                m_txtString.setVisibility(View.GONE);
                m_txtColor.setTextColor(itemMark.getMarkColor().getFG());
                m_txtColor.setBackgroundColor(itemMark.getMarkColor().getBG());
                break;
        }
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
