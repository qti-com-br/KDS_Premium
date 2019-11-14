package com.bematechus.kdsrouter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.bematechus.kdslib.KDSDataSumName;
import com.bematechus.kdslib.KDSUIColorPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;

/**
 *
 */
public class KDSUIDlgSumName  extends  KDSUIDialogBase  implements KDSUIColorPickerDialog.OnColorPickerDlgListener{

    EditText m_txtDescription = null;
    EditText m_txtQty = null;

    KDSDataSumName m_sumName = null;

    boolean m_bAddNew = false;
    int m_nBG = 0;
    int m_nFG = 0;


    public KDSUIDlgSumName(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, KDSDataSumName sumName) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_sumname, "");
        m_sumName = sumName;
        if (m_sumName == null) {
            this.setTitle(context.getString(R.string.new_item));
            m_sumName = new KDSDataSumName();
            sumName = m_sumName;
            m_bAddNew = true;
        }
        else
            this.setTitle(sumName.getDescription());


        m_txtDescription = (EditText)getView().findViewById(R.id.txtDescription);
        m_txtDescription.setText(m_sumName.getDescription());

        m_txtQty = (EditText)getView().findViewById(R.id.txtQty);
        m_txtQty.setText(KDSUtil.convertFloatToString( m_sumName.getSumQty()));

    }

    public void onOkClicked()
    {
        m_sumName.setDescription(m_txtDescription.getText().toString());
        m_sumName.setSumQty( KDSUtil.convertStringToFloat( m_txtQty.getText().toString(), 0));
    }

    public Object getResult()
    {
        return m_sumName;
    }

    public boolean isAddNew()
    {
        return m_bAddNew;
    }

    static private final int TAG_BG = 0;
    static private final int TAG_FG = 1;


    public void onCancel(KDSUIColorPickerDialog dialog)
    {

    }

    public void onOk(KDSUIColorPickerDialog dialog, int color)
    {
        int ntag = (int) dialog.getTag();
        if (ntag == TAG_BG)
            m_nBG = color;
        else
            m_nFG = color;
        m_txtDescription.setBackgroundColor(m_nBG);
        m_txtDescription.setTextColor(m_nFG);
    }
    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
}
