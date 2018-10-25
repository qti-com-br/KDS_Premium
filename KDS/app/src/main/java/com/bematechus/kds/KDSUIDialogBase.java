package com.bematechus.kds;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2015/11/6 0006.
 */
public class KDSUIDialogBase {

    public interface KDSDialogBaseListener {
        public void onKDSDialogCancel(KDSUIDialogBase dialog);
        public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj);

    }

    protected AlertDialog dialog = null;

    protected KDSDialogBaseListener listener= null;
    protected View m_view = null;
    protected KDSUser m_kdsUser = null;

    protected Object m_tag = null;

    public void setTag(Object tag)
    {
        m_tag = tag;
    }
    public Object getTag()
    {
        return m_tag;
    }

    public KDSUser getKdsUser()
    {
        return m_kdsUser;
    }
    public void setKDSUser(KDSUser kdsuser)
    {
        m_kdsUser = kdsuser;

    }

    protected void init_navigation_bar()
    {
        if (KDSGlobalVariables.getKDS() == null) return;
        boolean bhide = KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar);
        hideNavigationBar(bhide);
    }

    private void hideNavigationBar( boolean bHide)
    {
        if (bHide) {
            View view = this.getDialog().getWindow().getDecorView();
            if (view != null) {
                KDSUtil.enableSystemVirtualBar(view, false);
                view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        KDSUtil.enableSystemVirtualBar(KDSUIDialogBase.this.getView(), false);
                    }
                });
            }
        }
        else
        {
            View view =  this.getDialog().getWindow().getDecorView();
            if (view != null) {
                KDSUtil.enableSystemVirtualBar(view, true);
                view.setOnSystemUiVisibilityChangeListener(null);
            }
        }
    }

    static public String makeButtonText(Context context, int nResID, KDSSettings.ID funcKey )
    {
        String s = context.getString(nResID);
        return makeButtonText(context, s, funcKey);

    }

    static public String makeButtonText(Context context, String strText, KDSSettings.ID funcKey )
    {
        String s =strText;// context.getString(nResID);

        String strFunc = "";
        if (KDSGlobalVariables.getKDS()!=null &&
                KDSGlobalVariables.getKDS().getBumpbarKeysFunc() != null &&
                KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getCount() >0)
            strFunc = KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKeyString(funcKey);
        else {
            if (funcKey == KDSSettings.ID.Bumpbar_OK)
                strFunc = KDSSettings.getOkKeyString(context);
            else if (funcKey == KDSSettings.ID.Bumpbar_Cancel)
                strFunc = KDSSettings.getCancelKeyString(context);

        }
        if (!strFunc.isEmpty())
            s = s  +  strFunc ;
        //s = s  + "[" + strFunc +"]";
        return s;
    }
    /**
     * set the keyboard keys
     */
     public void updateButtonsText()
    {
        Button btnOK =  dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (btnOK == null) return;
        String s = KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKeyString(KDSSettings.ID.Bumpbar_OK);
        s = btnOK.getText().toString() + s;
        btnOK.setText(s);

        Button btnCancel =  dialog.getButton(Dialog.BUTTON_NEGATIVE);
        if (btnCancel == null) return;
        s = KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKeyString(KDSSettings.ID.Bumpbar_Cancel);
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

    public AlertDialog create1ButtonsDialog(Context context)
    {

        String strCancel = makeButtonText(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        AlertDialog d = new AlertDialog.Builder(context)

                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                        }
                    }
                })
                .create();
        return d;
    }

    public AlertDialog createOkButtonsDialog(Context context)
    {
        String strOK = makeButtonText(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
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

    /**
     * One button, with information.
     * @param context
     * @param strInfo
     * @return
     */
    public AlertDialog createInformationDialog( final Context context, String strTitle, String strInfo, boolean noCancel)
    {


        String text = makeButtonText(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);

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


    protected void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
               // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                if (event.getRepeatCount()>0) return false;
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                if (onKeyPressed(event)) return false; //for others to override
                KDSSettings.ID evID = KDSSettings.ID.NULL;
                if (KDSGlobalVariables.getKDS()!= null)
                    evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                else
                {
                    KDSBumpBarFunctions funcs = new KDSBumpBarFunctions();
                    KDSSettings settings = new KDSSettings(dlg.getContext().getApplicationContext());
                    funcs.updateSettings(settings);
                    evID = funcs.getKDSDlgEvent(event, null);
                }
                if (evID == KDSSettings.ID.Bumpbar_OK)
                {
                    dialog.dismiss();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                    return true;
                }
                else if (evID == KDSSettings.ID.Bumpbar_Cancel)
                {
                    dialog.cancel();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
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

    /**
     * make this function can been override,
     * it was override in kdsuidlginputpassword.java
     * @param context
     * @param nResID
     * @param funcKey
     * @return
     */
    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey)
    {
        return makeButtonText(context, nResID, funcKey);
    }

    protected String makeButtonText2(Context context, String strText, KDSSettings.ID funcKey)
    {
        return makeButtonText(context, strText, funcKey);
    }
    public  void createOkCancelDialog( final Context context,Object objTag,String okText, String cancelText, String strTitle, String strInfo, boolean noCancel,KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeButtonText2(context,okText, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText2(context, cancelText, KDSSettings.ID.Bumpbar_Cancel);
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

    public  void createOkCancelDialog( final Context context,Object objTag, String strTitle, String strInfo, boolean noCancel,KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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

    public void enableOKButton(boolean bEnable)
    {
        Button btn =dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btn == null) return;
        btn.setEnabled(bEnable);


    }

    public  void create3ButtonsDialog( final Context context,Object objTag, String strTitle, String strInfo, String btnText, KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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
                .setNeutralButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
        boolean noCancel = true;
        if (noCancel) {
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
        }
        init_dialog_events(dialog);


    }


    public AlertDialog create2ButtonsDialog(Context context)
    {
        String strOK = makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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
                        //   if (KDSUIDlgInputSMBFolder.this.listener != null) {
                        //       KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                        //   }
                    }
                })
                .create();
        return d;
    }

    public AlertDialog create3ButtonsDialog(Context context, String neutralButtonText)
    {
        String strOK = makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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
                        //   if (KDSUIDlgInputSMBFolder.this.listener != null) {
                        //       KDSUIDlgInputSMBFolder.this.listener.onSMBCancel(KDSUIDlgInputSMBFolder.this);
                        //   }
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


    static public String makeCtrlEnterButtonText(Context context, String text, KDSSettings.ID funcKey )
    {
        String s = text;//context.getString(nResID);

        String strFunc = "";

        if (funcKey == KDSSettings.ID.Bumpbar_OK) {
            KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
            func.setKeyCode(KeyEvent.KEYCODE_ENTER);
            strFunc = func.getSummaryString(KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKbdType());
            //strFunc = KDSSettings.getOkKeyString(context);
        }
        else if (funcKey == KDSSettings.ID.Bumpbar_Cancel){
            KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
            func.setCtrl(true);
            strFunc = func.getSummaryString( KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKbdType());


        }
        if (!strFunc.isEmpty())
            s = s  +  strFunc ;
        //s = s  + "[" + strFunc +"]";
        return s;
    }

    static public String makeCtrlEnterButtonText(Context context,int nResID, KDSSettings.ID funcKey )
    {
        String s = context.getString(nResID);
        return makeCtrlEnterButtonText(context, s, funcKey);

    }

    static public KDSSettings.ID checkCtrlEnterEvent(int keyCode,KeyEvent event )
    {
        KDSSettings.ID evID = KDSSettings.ID.NULL;
        if (event.getRepeatCount() > 0) return evID;
        if (event.getAction() != KeyEvent.ACTION_UP) return evID;

        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
            evID =  KDSSettings.ID.Bumpbar_Cancel;
        else if (keyCode == KeyEvent.KEYCODE_ENTER)
            evID =  KDSSettings.ID.Bumpbar_OK;
        return evID;
    }

    public void show() {

        dialog.show();
        init_navigation_bar();

    }

    protected void init_dialog_ctrl_enter_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                if (event.getRepeatCount() > 0) return false;
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                KDSSettings.ID evID = KDSSettings.ID.NULL;
                if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                        keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
                    evID =  KDSSettings.ID.Bumpbar_Cancel;
                else if (keyCode == KeyEvent.KEYCODE_ENTER)
                    evID =  KDSSettings.ID.Bumpbar_OK;

                if (evID == KDSSettings.ID.Bumpbar_OK) {
                    onOkClicked();
                    dialog.dismiss();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                    return true;
                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
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


}
