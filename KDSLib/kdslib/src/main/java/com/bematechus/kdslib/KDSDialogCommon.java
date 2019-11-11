package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by David.Wong on 2019/9/3.
 * common function in KDSUIDialogBase(KDS/KDSRouter).
 *
 * Rev:
 */
public class KDSDialogCommon {
    public interface KDSDialogBaseListener {
        public void onKDSDialogCancel(KDSDialogCommon dialog);
        public void onKDSDialogOK(KDSDialogCommon dialog, Object obj);

    }
    public enum DialogEvent
    {
        Unknown,
        OK,
        Neutral,
        Cancel,

    }

    static KDSDialogCommon m_singleInstance = null;

    protected AlertDialog dialog = null;

    protected KDSDialogBaseListener listener= null;
    protected View m_view = null;
    protected Object m_tag = null;

    public void setTag(Object tag)
    {
        m_tag = tag;
    }
    public Object getTag()
    {
        return m_tag;
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

    public String makeCancelButtonText(Context context)
    {
        String s = context.getString( R.string.cancel);
        String bumpbar = getBumpbarCancelKeyText(context);
        return s + bumpbar;
    }

    /**
     * override by child
     * @param context
     * @return
     */
    public String getBumpbarCancelKeyText(Context context)
    {
        return "";
    }

    public String makeCancelButtonText(Context context, String text)
    {
        String s = text;
        String bumpbar = getBumpbarCancelKeyText(context);
        return s + bumpbar;
    }

    public String makeOKButtonText(Context context)
    {
        String s = context.getString( R.string.ok);
        String bumpbar = getBumpbarOKKeyText(context);
        return s + bumpbar;
    }

    /**
     * Override by child
     * @param context
     * @return
     */
    public String getBumpbarOKKeyText(Context context)
    {
        return "";
    }

    public String makeOKButtonText(Context context, String text)
    {
        String s = text;
        String bumpbar = getBumpbarOKKeyText(context);
        return s + bumpbar;
    }
    ////////////////////////////////////////////////////////////////////////////////////////
    /**
     * set the keyboard keys
     */
    public void updateButtonsText()
    {
        Button btnOK =  dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (btnOK == null) return;
        String s = getBumpbarOKKeyText(KDSApplication.getContext());//
        s = btnOK.getText().toString() + s;
        btnOK.setText(s);

        Button btnCancel =  dialog.getButton(Dialog.BUTTON_NEGATIVE);
        if (btnCancel == null) return;
        s = getBumpbarCancelKeyText(KDSApplication.getContext());// KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKeyString(KDSSettings.ID.Bumpbar_Cancel);
        s = btnCancel.getText().toString() + s;
        btnCancel.setText(s);
    }

    public void enableOKButton(boolean bEnable)
    {
        Button btn =dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btn == null) return;
        btn.setEnabled(bEnable);


    }


    public AlertDialog createOkButtonsDialog(Context context)
    {
        String strOK = makeOKButtonText(context);// makeButtonText(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                        }
                    }
                })


                .create();
        return d;
    }


    public AlertDialog createCancelButtonsDialog(Context context)
    {

        String strCancel = makeCancelButtonText(context);//makeButtonText(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)

                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
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
    public AlertDialog createInformationDialog( final Context context, String strTitle, String strInfo, boolean noCancel)
    {


        String text = makeOKButtonText(context);// makeButtonText(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);

        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(text, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
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

    /**
     *
     * @param ev
     * @return
     * true: handle this key
     */
    public boolean onKeyPressed(KeyEvent ev)
    {
        return false;
    }

    /**
     * override by child
     * @param event
     * @return
     */
    public DialogEvent checkDialogKeyboardEvent(KeyEvent event)
    {
        return DialogEvent.Unknown;
    }
    protected void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                if (event.getRepeatCount()>0) return false;
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                if (onKeyPressed(event)) return false; //for others to override
                DialogEvent ev = DialogEvent.Unknown;
                //KDSSettings.ID evID = KDSSettings.ID.NULL;
                ev = checkDialogKeyboardEvent(event);

                if (ev == DialogEvent.OK)
                {
                    dialog.dismiss();
                    if (KDSDialogCommon.this.listener != null)
                        KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                    return true;
                }
                else if (ev == DialogEvent.Cancel)
                {
                    dialog.cancel();
                    if (KDSDialogCommon.this.listener != null)
                        KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP)
                {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }
    public  void createOkCancelDialog( final Context context,Object objTag,String okText, String cancelText, String strTitle, String strInfo, boolean noCancel,KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeOKButtonText(context, okText);// makeButtonText2(context,okText, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeCancelButtonText(context, cancelText);// makeButtonText2(context, cancelText, KDSSettings.ID.Bumpbar_Cancel);
        dialog = new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (KDSDialogCommon.this.listener != null)
                                    KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSDialogCommon.this.listener != null)
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
                        }
                    }
                })
                .create();
        if (noCancel) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        init_dialog_events(dialog);


    }

    public  void createOkCancelDialog( final Context context,Object objTag, String strTitle, String strInfo, boolean noCancel,KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeOKButtonText(context);// makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeCancelButtonText(context);// makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        dialog = new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (KDSDialogCommon.this.listener != null)
                                    KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSDialogCommon.this.listener != null)
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
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

        String strOK = makeOKButtonText(context);//makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel =makeCancelButtonText(context);// makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                        }
                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        //   if (KDSUIDlgInputSMBFolder.this.listener != null) {
                        //       KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                        //   }
                    }
                })
                .create();
        return d;
    }

//    public  void create3ButtonsDialog( final Context context,Object objTag, String strTitle, String strInfo, String btnText, KDSDialogBaseListener listener)
//    {
//
//        this.listener = listener;
//        setTag(objTag);
//
//        String strOK = makeOKButtonText(context);//makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
//        String strCancel = makeCancelButtonText(context);//makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
//        dialog = new AlertDialog.Builder(context)
//                .setTitle(strTitle)
//                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
//                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                onOkClicked();
//                                if (KDSDialogCommon.this.listener != null)
//                                    KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
//                            }
//                        }
//                )
//                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        if (KDSDialogCommon.this.listener != null)
//                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
//                    }
//                })
//                .setNeutralButton(btnText, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                })
//                .setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    // if back button is used, call back our listener.
//                    @Override
//                    public void onCancel(DialogInterface paramDialogInterface) {
////                        if (KDSUIDialogBase.this.listener != null) {
////                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
////                        }
//                    }
//                })
//                .create();
//        boolean noCancel = true;
//        if (noCancel) {
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.setCancelable(false);
//        }
//        init_dialog_events(dialog);
//
//
//    }



    public AlertDialog create3ButtonsDialog(Context context, String neutralButtonText)
    {
        String strOK = makeOKButtonText(context);//makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel =makeCancelButtonText(context);// makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onOkClicked();
                        //saveToSmb();
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogOK(KDSDialogCommon.this, getResult());
                        }
                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSDialogCommon.this.listener != null) {
                            KDSDialogCommon.this.listener.onKDSDialogCancel(KDSDialogCommon.this);
                        }
                    }
                })
                .setNeutralButton(neutralButtonText, null)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        //   if (KDSUIDlgInputSMBFolder.this.listener != null) {
                        //       KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                        //   }
                    }
                })
                .create();
        return d;
    }

//    public void setNeutralButtonText(String strText)
//    {
//        Button btn =dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        if (btn != null)
//            btn.setText(strText);
//
//    }

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

    /**
     * Button text with the bump key description
     * @param text
     * @param keyDescription
     * @return
     */
    static public String makeDialogButtonText( String text, String keyDescription)
    {
        String s = text;
        if (!keyDescription.isEmpty())
            s = s  +  keyDescription ;

        return s;
    }




    public void show() {

        dialog.show();
        //init_navigation_bar();

    }



    private long m_nAutoCloseTimeoutMs = 0;
    public void setAutoCloseTimeout(long nTimeoutMs)
    {
        m_nAutoCloseTimeoutMs = nTimeoutMs;
        if (m_nAutoCloseTimeoutMs >0)
            startTimerForAutoClose();
    }

    private void startTimerForAutoClose()
    {
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {

                dialog.dismiss();
                t.cancel();
            }
        }, m_nAutoCloseTimeoutMs);
    }

    /**
     *
     * @return
     *  true: ok
     *  false: failed.
     */
    protected boolean checkDataValidation()
    {
        return true;
    }

    static public KDSDialogCommon singleInstance()
    {

        if (m_singleInstance != null) {
            if (m_singleInstance.getDialog() != null) {
                if (m_singleInstance.getDialog().isShowing())
                    m_singleInstance.getDialog().cancel();
            }
            m_singleInstance = null;
        }

        m_singleInstance = new KDSDialogCommon();
        return m_singleInstance;
    }

}
