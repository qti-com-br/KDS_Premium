package com.bematechus.kds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bematechus.kdslib.KDSApplication;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/9/19.
 */
public class KDSUIDlgColsSize extends KDSUIDialogBase {


    ColSizeView m_sizeView = null;

    String m_strColsSize = "";
    @Override
    public void onOkClicked()
    {//save data here
        m_strColsSize = m_sizeView.getColsPercentString();
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_strColsSize;


    }

    public KDSUIDlgColsSize(final Context context, String colsSize, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_cols_size, "");
        m_strColsSize = colsSize;
        m_sizeView =(ColSizeView) this.getView().findViewById(R.id.colsizeView);
        m_sizeView.setColsPercentString(colsSize);
        m_sizeView.setBkColor(KDSApplication.getContext().getResources().getColor(android.R.color.background_light));
    }



}
