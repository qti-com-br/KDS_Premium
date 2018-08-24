package com.bematechus.kdsstatistic;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;

import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUIBGFGPickerDialog;
import com.bematechus.kdslib.KDSUtil;

/**
 * Created by David.Wong on 2018/5/16.
 * Rev:
 */
public class SOSDialogPropertiesBase extends KDSUIDialogBase implements KDSUIBGFGPickerDialog.OnBGFGPickerDlgListener,KDSUIDialogBase.KDSDialogBaseListener{

    public void onCancel(KDSUIBGFGPickerDialog dialog)
    {

    }

    public void onOk(KDSUIBGFGPickerDialog dialog, KDSBGFG ff)
    {
        showButtonColor((Button) dialog.getTag(), ff.getBG(), ff.getFG());
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof SOSDialogTimePicker)
        {
            SOSDialogTimePicker dlg = (SOSDialogTimePicker) dialog;
            Button btn = (Button) dlg.getTag();
            int n = 0;
            if (obj!= null)
                n = (int)obj;
           showButtonTime(btn, n);




        }
    }

    protected void showButtonTime(Button btn, int nSeconds)
    {
        btn.setText(KDSUtil.seconds2HMS(nSeconds));
        btn.setTag(nSeconds);
    }

    static public int getButtonTime(Button btn)
    {
        Object obj = btn.getTag();
        int n = 0;
        if (obj!= null)
            n = (int)obj;
        return n;
    }
    protected void showButtonColor(Button btn, int nBG, int nFG)
    {
        btn.setBackgroundColor(nBG);
        btn.setTextColor(nFG);
    }

    static public int getButtonBG(View btn, int nDefault)
    {
        Drawable background=btn.getBackground();
        if (background instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) background;
            int color = colorDrawable.getColor();
            return color;
        }
        else
            return nDefault;
    }

    protected int getButtonFG(Button btn, int nDefault)
    {
        return btn.getCurrentTextColor();


    }
    protected void chooseColor(View v)
    {
        Button btn = (Button)v;
        int bg = getButtonBG(btn, 0);
        int fg = getButtonFG(btn, 0);
        KDSBGFG bgfg = new KDSBGFG(bg, fg);
        KDSUIBGFGPickerDialog dlg = new KDSUIBGFGPickerDialog(this.getView().getContext(),bgfg, this );

        dlg.setTag(v);
        dlg.show();

    }

    protected void chooseDuration(View v)
    {
        Button btn = (Button)v;
        Object obj =  btn.getTag();


        int n = 0;
        if (obj != null)
            n = (int)obj;


        SOSDialogTimePicker dlg = new SOSDialogTimePicker(this.getView().getContext(), n, this);
        dlg.setTag(v);
        dlg.show();
    }
}
