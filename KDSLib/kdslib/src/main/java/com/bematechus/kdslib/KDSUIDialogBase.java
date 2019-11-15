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
 * I plan to use this to replace okd KDSUIDialogBase class.
 * usage:
 *  1. If need bumpbar support, call init_kbd_keys function first.
 *  2. In KDS app, call init_navigation_bar_settings function first too.
 *  3. If [0]/[1] can not fit dialog input requirement, call "setUserCtrlEnter" function before init_dialog.
 * Rev:
 */
public class KDSUIDialogBase {
    public interface KDSDialogBaseListener {
        public void onKDSDialogCancel(KDSUIDialogBase dialog);
        public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj);

    }
    public enum DialogEvent
    {
        Unknown,
        OK,
        Neutral,
        Cancel,

    }

    /**
     * update keyboard keys description when settings changed.
     */
    static public KDSBumpBarKeyFunc.KeyboardType m_kbdType = KDSBumpBarKeyFunc.KeyboardType.Standard;
    static public KDSBumpBarKeyFunc m_funcOK = null;
    static public KDSBumpBarKeyFunc m_funcCancel = null;

    static public boolean m_bHideNavigationBar = false;

    static KDSUIDialogBase m_singleInstance = null;

    protected AlertDialog dialog = null;

    protected KDSDialogBaseListener listener= null;
    protected View m_view = null;
    protected Object m_tag = null;
    protected Object m_userTag = null;

    protected boolean m_bUseCtrlEnterForOkCancel = false; //if use [ctrl]/[enter] for dialog Cancel/OK button keyboard.

    /**
     * Please call this function if dialog needs bumpbar support.
     * @param kbdType
     * @param keyOK
     * @param keyCancel
     */
    static public void init_kbd_keys(KDSBumpBarKeyFunc.KeyboardType kbdType, KDSBumpBarKeyFunc keyOK, KDSBumpBarKeyFunc keyCancel)
    {
        m_kbdType = kbdType;
        m_funcOK = keyOK;
        m_funcCancel = keyCancel;
    }

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
    public Object getKdsUser()
    {
        return m_userTag;
    }
    public void setKDSUser(Object kdsuser)
    {
        m_userTag = kdsuser;

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
        if (m_bUseCtrlEnterForOkCancel)
            return makeCtrlEnterButtonText(context, DialogEvent.Cancel);
        else
            return makeCancelButtonText2(context);
//
//        String s = context.getString( R.string.cancel);
//        String bumpbar = getBumpbarCancelKeyText(context);
//        return s + bumpbar;
    }
    static public String makeCancelButtonText2(Context context)
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
    static public String getBumpbarCancelKeyText(Context context)
    {
        if (m_funcCancel == null) return "";
        return m_funcCancel.getSummaryString(m_kbdType);
    }

    public String makeCancelButtonText(Context context, String text)
    {
        String s = text;
        String bumpbar = getBumpbarCancelKeyText(context);
        return s + bumpbar;
    }

    public String makeOKButtonText(Context context)
    {
        if (m_bUseCtrlEnterForOkCancel)
            return makeCtrlEnterButtonText(context, DialogEvent.OK);
        else
            return makeOKButtonText2(context);
//        String s = context.getString( R.string.ok);
//        String bumpbar = getBumpbarOKKeyText(context);
//        return s + bumpbar;
    }

    static public String makeOKButtonText2(Context context)
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
    static public String getBumpbarOKKeyText(Context context)
    {
        if (m_funcOK == null) return "";
        return m_funcOK.getSummaryString(m_kbdType);
    }

    static public String makeOKButtonText(Context context, String text)
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

        String strCancel = makeCancelButtonText(context);//makeButtonText(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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
        if (m_funcOK == null ||
            m_funcCancel == null) return DialogEvent.Unknown;
        if (m_funcOK.isFitWithMyEvent(event, null))
            return DialogEvent.OK;
        if (m_funcCancel.isFitWithMyEvent(event, null))
            return DialogEvent.Cancel;
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
                if (m_bUseCtrlEnterForOkCancel)
                    ev = checkCtrlEnterEvent(event.getKeyCode(), event);
                else
                    ev = checkDialogKeyboardEvent(event);

                if (ev == DialogEvent.OK)
                {
                    if (!checkDataValidation())
                        return true;
                    onOkClicked();
                    dialog.dismiss();
                    if (KDSUIDialogBase.this.listener != null)
                        KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
                    return true;
                }
                else if (ev == DialogEvent.Cancel)
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
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
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

        String strOK = makeOKButtonText(context);//makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel =makeCancelButtonText(context);// makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
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
                        if (KDSUIDialogBase.this.listener != null) {
                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
                        }
                    }
                })
                .create();
        return d;
    }

    public  void create3ButtonsDialog( final Context context,Object objTag, String strTitle, String strInfo, String btnText, KDSDialogBaseListener listener)
    {

        this.listener = listener;
        setTag(objTag);

        String strOK = makeOKButtonText(context);//makeButtonText2(context, R.string.ok, KDSSettings.ID.Bumpbar_OK);
        String strCancel = makeCancelButtonText(context);//makeButtonText2(context, R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
        dialog = new AlertDialog.Builder(context)
                .setTitle(strTitle)
                .setMessage(strInfo)//this.getString(R.string.confirm_import_db))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                onOkClicked();
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
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
//                        if (KDSUIDialogBase.this.listener != null) {
//                            KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
//                        }
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


    public void show() {

        dialog.show();
        init_navigation_bar();

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

    static public KDSUIDialogBase singleInstance()
    {

        if (m_singleInstance != null) {
            if (m_singleInstance.getDialog() != null) {
                if (m_singleInstance.getDialog().isShowing())
                    m_singleInstance.getDialog().cancel();
            }
            m_singleInstance = null;
        }

        m_singleInstance = new KDSUIDialogBase();
        return m_singleInstance;
    }

    protected void hideNavigationBar( boolean bHide)
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


    static public void init_navigation_bar_settings(boolean bHide)
    {
        m_bHideNavigationBar = bHide;
    }

    protected void init_navigation_bar()
    {

        hideNavigationBar(m_bHideNavigationBar);
    }

    /**
     * For using Ctrl/Enter for "OK/Cancel" buttons.
     * In default, we use 0/1 for ok/cancel, this cause user can not input 0,1 in text box.
     * So, create this Ctrl/Enter button function.
     * @param context
     * @param text
     * @param funcKey
     * @return
     */
    static public String makeCtrlEnterButtonText(Context context, String text, DialogEvent funcKey)
    {
        String s = text;//context.getString(nResID);

        String strFunc = "";
        if (m_funcOK != null) { //use m_funcOK check if support bumpbar
            if (funcKey == DialogEvent.OK) {
                KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
                func.setKeyCode(KeyEvent.KEYCODE_ENTER);
                strFunc = func.getSummaryString(m_kbdType);
                //strFunc = KDSSettings.getOkKeyString(context);
            } else if (funcKey == DialogEvent.Cancel) {
                KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
                func.setCtrl(true);
                strFunc = func.getSummaryString(m_kbdType);


            }
        }
        if (!strFunc.isEmpty())
            s = s  +  strFunc ;
        //s = s  + "[" + strFunc +"]";
        return s;
    }

    static public String makeCtrlEnterButtonText(Context context,int nResID, DialogEvent funcKey )
    {
        String s = context.getString(nResID);
        return makeCtrlEnterButtonText(context, s, funcKey);

    }

    static public String makeCtrlEnterButtonText(Context context, DialogEvent funcKey )
    {
        String s = "";
        if (funcKey == DialogEvent.OK)
            s = context.getString(R.string.ok);
        else
            s = context.getString(R.string.cancel);

        return makeCtrlEnterButtonText(context, s, funcKey);

    }

    static public DialogEvent checkCtrlEnterEvent(int keyCode,KeyEvent event )
    {
        DialogEvent evID = DialogEvent.Unknown;
        if (event.getRepeatCount() > 0) return evID;
        if (event.getAction() != KeyEvent.ACTION_UP) return evID;

        if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
            evID = DialogEvent.Cancel;
        else if (keyCode == KeyEvent.KEYCODE_ENTER)
            evID = DialogEvent.OK;
        return evID;
    }



//    protected void init_dialog_ctrl_enter_events(final AlertDialog dlg)
//    {
//        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//                if (event.getRepeatCount() > 0) return false;
//                if (event.getAction() != KeyEvent.ACTION_UP) return false;
//                DialogEvent evID = checkCtrlEnterEvent(event.getKeyCode(), event);
//
//                if (evID == DialogEvent.OK) {
//                    if (!checkDataValidation())
//                        return true;
//                    onOkClicked();
//                    dialog.dismiss();
//                    if (KDSUIDialogBase.this.listener != null)
//                        KDSUIDialogBase.this.listener.onKDSDialogOK(KDSUIDialogBase.this, getResult());
//                    return true;
//                } else if (evID == DialogEvent.Cancel) {
//                    dialog.cancel();
//                    if (KDSUIDialogBase.this.listener != null)
//                        KDSUIDialogBase.this.listener.onKDSDialogCancel(KDSUIDialogBase.this);
//                    return true;
//                }
//                if (event.getAction() == KeyEvent.ACTION_UP) {
//                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
//                }
//                return false;
//            }
//        });
//    }

    public void setCancelByClickOutside(boolean bEnable)
    {

        dialog.setCanceledOnTouchOutside(bEnable);
        dialog.setCancelable(bEnable);

    }

    /**
     * Force dialog use ctrl,enter keyboard key
     * call this before init_dialog function.
     * @param bCtrlEnter
     */
    public void setUseCtrlEnterKey(boolean bCtrlEnter)
    {
        m_bUseCtrlEnterForOkCancel = bCtrlEnter;
    }
}
