package com.bematechus.kds;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Administrator on 2016/3/14 0014.
 */
public class KDSPreferenceColsSize extends Preference implements ColSizeView.ColsSizeViewEvents, KDSUIDialogBase.KDSDialogBaseListener {


    View m_viewBind = null;

    ColSizeView m_colSizeView = null;
    String m_strValues = "";
    String m_strDefaultValues = "";


    public KDSPreferenceColsSize(Context context, AttributeSet attrs) {
        super(context, attrs);

        String defaultVal = attrs.getAttributeValue("http://schemas.android.com/apk/res/android", "defaultValue");
        m_strDefaultValues = defaultVal;
        m_strValues = defaultVal;// KDSViewFontFace.parseString(defaultVal);
        String s =  this.getPersistedString(defaultVal);
        m_strValues = s;
        setWidgetLayoutResource(R.layout.pref_cols_size);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        m_viewBind = view;
        // Set our custom views inside the layout
        m_colSizeView =(ColSizeView) view.findViewById(R.id.colsizeView);
        if (m_colSizeView != null) {
            m_colSizeView.setEventsReceiver(this);
            m_colSizeView.setSizeDrawMode(ColSizeView.SizeDrawMode.Demo);
            String s =  this.getPersistedString(m_strDefaultValues);
            m_strValues = s;
            m_colSizeView.setColsPercentString(m_strValues);

            m_colSizeView.setBkColor(getContext().getResources().getColor(R.color.settings_page_bg));
            m_colSizeView.invalidate();
        }

        view.invalidate();

    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {

        String s = a.getString(index);

        return s;
    }

    public boolean isPersistent() {
        return true;
    }

    KDSUIDlgColsSize m_dlg =null;
    protected void onClick() {
        if (m_dlg != null) return;
        m_dlg = new KDSUIDlgColsSize(this.getContext(),m_strValues, this );
        m_dlg.show();
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {
        m_dlg = null;


    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        m_dlg = null;
        String s = (String) ((KDSUIDlgColsSize)dialog).getResult();
        m_colSizeView.setColsPercentString(s);
        onColsSizeViewDataChanged();
    }


    public void onColsSizeViewDataChanged()
    {

        String s = m_colSizeView.getColsPercentString();
        persistString(s);
        notifyChanged();
    }

    public void onColsSizeViewTouchDown()
    {
    }
    public void onColsSizeViewTouchUp()
    {
    }

}
