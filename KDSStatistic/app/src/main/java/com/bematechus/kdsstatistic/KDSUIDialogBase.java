package com.bematechus.kdsstatistic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.bematechus.kdslib.KDSKbdRecorder;

/**
 * SAME AS KDSROUTER file
 */

/**
 * Created by Administrator on 2015/11/6 0006.
 * **************IMPORTANT**********************
 * >>>>>>>>>>>>>>This is different with KDS file<<<<<<<<<<<<<<<
 * It don't support bumpbar operation
 */
public class KDSUIDialogBase {

    public interface KDSDialogBaseListener {
        public void onKDSDialogCancel(KDSUIDialogBase dialog);
        public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj);

    }

    protected AlertDialog dialog = null;

    protected KDSDialogBaseListener listener= null;
    protected View m_view = null;
    //protected KDSUser m_kdsUser = null;

    protected Object m_tag = null;

    public void setTag(Object tag)
    {
        m_tag = tag;
    }
    public Object getTag()
    {
        return m_tag;
    }



    static public String getFuncKeyName(STSettings.ID funcKey)
    {
        String strFunc = "";


        return strFunc;
    }
    static public String makeButtonText(Context context, int nResID, STSettings.ID funcKey )
    {
        String s = context.getString(nResID);

        String strFunc = getFuncKeyName(funcKey);
        if (!strFunc.isEmpty())
            s = s  +  strFunc ;

        return s;
    }
    /**
     * set the keyboard keys
     */
    public void updateButtonsText()
    {
        Button btnOK =  dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (btnOK == null) return;
        String s = getFuncKeyName(STSettings.ID.Bumpbar_OK);
        s = btnOK.getText().toString() + s;
        btnOK.setText(s);

        Button btnCancel =  dialog.getButton(Dialog.BUTTON_NEGATIVE);
        if (btnCancel == null) return;
        s = getFuncKeyName(STSettings.ID.Bumpbar_Cancel);
        s = btnCancel.getText().toString() + s;
        btnCancel.setText(s);
    }

    public View getView()
    {
        return m_view;
    }
    public AlertDialog getDialog()
    {
        return dialog;
    }

    public void setTitle(String strTitle)
    {
        if (dialog != null)
            dialog.setTitle(strTitle);
    }

    /**
     * it will been overrided by child
     */
    public void onOkClicked()
    {

    }

    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return null;
    }



    public AlertDialog createOkButtonsDialog(Context context)
    {
        String strOK = makeButtonText(context, R.string.ok, STSettings.ID.Bumpbar_OK);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                        }
                    }
                })


                .create();
        return d;
    }
    public AlertDialog createCancelButtonsDialog(Context context)
    {
        String strCancel = makeButtonText(context, R.string.cancel, STSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                        }
                    }
                })


                .create();
        return d;
    }
    /**
     * One button, with information.
     * @param context
     * @param strInfo
     * @return
     */
    public AlertDialog createInformationDialog(final Context context, String strTitle, String strInfo, boolean noCancel)
    {


        String text = makeButtonText(context, R.string.ok, STSettings.ID.Bumpbar_OK);

        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                        }
                    }
                })
                .setTitle(strTitle)
                .setMessage(strInfo)
                .create();
        if (noCancel) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }

        updateButtonsText();
        init_dialog_events(dialog);

        return dialog;
    }

    static public STSettings.ID checkKdbEvent(KeyEvent event)
    {
        return STSettings.ID.NULL;
    }

    private void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (event.getRepeatCount()>0) return false;
                if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                STSettings.ID evID = STSettings.ID.NULL;
                evID = checkKdbEvent(event);

                if (evID == STSettings.ID.Bumpbar_OK)
                {
                    dialog.dismiss();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                    return true;
                }
                else if (evID == STSettings.ID.Bumpbar_Cancel)
                {
                    dialog.cancel();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }

    public  void createOkCancelDialog(final Context context, Object objTag, String strTitle, String strInfo, boolean noCancel, KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeButtonText(context, R.string.ok, STSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText(context, R.string.cancel, STSettings.ID.Bumpbar_Cancel);
        dialog = new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (KDSUIDialogBase.this.listener != null)
                                    KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIDialogBase.this.listener != null)
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                    }
                })
                .create();
        if (noCancel) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        init_dialog_events(dialog);


    }

    public AlertDialog create2ButtonsDialog(Context context)
    {
        String strOK = makeButtonText(context, R.string.ok, STSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText(context, R.string.cancel, STSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                        }
                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {

                    }
                })
                .create();
        return d;
    }

    public AlertDialog create3ButtonsDialog(Context context, String neutralButtonText)
    {
        String strOK = makeButtonText(context, R.string.ok, STSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText(context, R.string.cancel, STSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                        }
                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                        }
                    }
                })
                .setNeutralButton(neutralButtonText, null)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {

                    }
                })
                .create();
        return d;
    }

    public void setNeutralButtonText(String strText)
    {
        Button btn =dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        if (btn != null)
            btn.setText(strText);

    }

    public void enableOKButton(boolean bEnable)
    {
        Button btn =dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btn == null) return;
        btn.setEnabled(bEnable);


    }

    public void int_dialog(Context context, KDSDialogBaseListener listener, int resDlgID, String neutralButtonText) {
        this.listener = listener;
        m_view = LayoutInflater.from(context).inflate(resDlgID, null);
        if (neutralButtonText.isEmpty())
            dialog = create2ButtonsDialog(context);
        else
            dialog = create3ButtonsDialog(context, neutralButtonText);

        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);
        init_dialog_events(dialog);

    }

    /**
     * this is for about dialog. Use self layout
     * @param context
     * @param resDlgID
     */
    public void int_information_dialog(Context context, int resDlgID) {
        this.listener = null;
        m_view = LayoutInflater.from(context).inflate(resDlgID, null);

        dialog = createOkButtonsDialog(context);


        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);
        init_dialog_events(dialog);

    }

    static public String makeCtrlEnterButtonText(Context context, String text, STKDSSettings.ID funcKey )
    {
        String s = text;//

        String strFunc = "";


        if (!strFunc.isEmpty())
            s = s  +  strFunc ;

        return s;
    }

    /**
     * this is for about dialog. Use self layout
     * @param context
     * @param resDlgID
     */
    public void int_one_button_dialog(Context context, int resDlgID, boolean bCancelButton) {
        this.listener = null;
        m_view = LayoutInflater.from(context).inflate(resDlgID, null);

        if (bCancelButton)
            dialog = createCancelButtonsDialog(context);
        else
            dialog = createOkButtonsDialog(context);


        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);
        init_dialog_events(dialog);


    }

    static public String makeCtrlEnterButtonText(Context context,int nResID, STKDSSettings.ID funcKey )
    {
        String s = context.getString(nResID);
        return makeCtrlEnterButtonText(context, s, funcKey);

    }

    static public STKDSSettings.ID checkCtrlEnterEvent(int keyCode,KeyEvent event )
    {
        STKDSSettings.ID evID = STKDSSettings.ID.NULL;

        return evID;
    }


    public void show() {
        dialog.show();
    }


}
