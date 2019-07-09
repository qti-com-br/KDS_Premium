package com.bematechus.kdsrouter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import com.bematechus.kdslib.KDSUIColorPickerDialog;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/17 0017.
 */
public class KDSUIDlgCategory  extends  KDSUIDialogBase  implements KDSUIColorPickerDialog.OnColorPickerDlgListener{



    EditText m_txtDescription = null;
    EditText m_txtStation = null;
    EditText m_txtScreen = null;
    EditText m_txtDelay = null;
    EditText m_txtSeconds = null;
    CheckBox m_chkPrintable = null;


    Button m_btnBG = null;
    Button m_btnFG = null;

    KDSRouterDataCategory m_category = new KDSRouterDataCategory();
    int m_nBG = 0;
    int m_nFG =0;

    boolean m_bAddNew = false;
    public boolean isAddNew()
    {
        return m_bAddNew;
    }
    @Override
    public void onOkClicked()
    {//save data here
        //show data
        m_category.setDescription(m_txtDescription.getText().toString());

        m_category.setBG(m_nBG);
        m_category.setFG(m_nFG);

        m_category.setToStation(m_txtStation.getText().toString());
        m_category.setToScreen(m_txtScreen.getText().toString());
        String mins = m_txtDelay.getText().toString();
        String secs = m_txtSeconds.getText().toString();
        int nmins = KDSUtil.convertStringToInt(mins, 0);
        int nsecs = KDSUtil.convertStringToInt(secs, 0);
        float flt = nsecs;
        flt /=60;
        flt += nmins;

        m_category.setDelay( flt);//KDSUtil.convertStringToFloat(m_txtDelay.getText().toString(), 0));
        m_category.setPrintable(m_chkPrintable.isChecked());
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_category;


    }


    @Override
    public void show() {
        super.show();
        this.getView().findViewById(R.id.rlBackground).requestFocus();

    }



    public KDSUIDlgCategory(final Context context, KDSDialogBaseListener listener, KDSRouterDataCategory category) {
        this.int_dialog(context, listener, R.layout.dlg_category, "");
        m_category = category;
        if (m_category == null) {
            this.setTitle(context.getString(R.string.new_category));
            m_category = new KDSRouterDataCategory();
            category = m_category;
            m_bAddNew = true;
        }
        else
            this.setTitle(m_category.getDescription());


        m_txtDescription = (EditText)getView().findViewById(R.id.txtDescription);

        m_txtStation =  (EditText)getView().findViewById(R.id.txtStation);
        m_txtScreen =  (EditText)getView().findViewById(R.id.txtScreen);
        m_txtDelay =  (EditText)getView().findViewById(R.id.txtDelay);
        m_txtSeconds =  (EditText)getView().findViewById(R.id.txtSecs);

        m_chkPrintable =  (CheckBox)getView().findViewById(R.id.chkPrintable);

        m_btnBG = (Button)getView().findViewById(R.id.btnBG);
        m_btnBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBGButtonClicked(v);
            }
        });
        m_btnFG = (Button)getView().findViewById(R.id.btnFG);
        m_btnFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFGButtonClicked(v);
            }
        });

        //show data
        m_txtDescription.setText(category.getDescription());
        if (category.isAssignedColor())
        {
            m_txtDescription.setBackgroundColor(category.getBG());
            m_txtDescription.setTextColor(category.getFG());
            m_nBG = category.getBG();
            m_nFG = category.getFG();

        }
        m_txtStation.setText(category.getToStation());
        m_txtScreen.setText(category.getToScreen());
        m_txtDelay.setText(KDSUtil.convertIntToString(category.getDelayMins()));
        m_txtSeconds.setText(KDSUtil.convertIntToString(category.getDelaySecs()));
        m_chkPrintable.setChecked(category.getPrintable());



    }

    static private final int TAG_BG = 0;
    static private final int TAG_FG = 1;
    /**
     *
     * @param v
     */
    private void onBGButtonClicked(View v)
    {

        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getDialog().getContext(), m_nBG, this);
        dlg.setTag(TAG_BG);
        dlg.show();
    }

    private void onFGButtonClicked(View v)
    {
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getDialog().getContext(), m_nFG, this);
        dlg.setTag(TAG_FG);
        dlg.show();
    }

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
}
