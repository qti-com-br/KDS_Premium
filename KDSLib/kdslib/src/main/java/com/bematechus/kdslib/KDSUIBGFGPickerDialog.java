package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 *
 */
public class KDSUIBGFGPickerDialog {

    static Object g_instance = null;

    public interface OnBGFGPickerDlgListener {
        public void onCancel(KDSUIBGFGPickerDialog dialog);

        public void onOk(KDSUIBGFGPickerDialog dialog, KDSBGFG ff);
    }

    AlertDialog dialog = null;

    OnBGFGPickerDlgListener listener= null;

    TextView m_txtDemo = null;
    Button m_btnBG = null;
    Button m_btnFG = null;
    KDSBGFG m_bgfgOriginal =  new KDSBGFG();

    Object m_tag = null;
    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }
    /*****************************************************************************************/
    private void createDefaultBGFG()
    {

        m_bgfgOriginal = new KDSBGFG();

    }
    public KDSUIBGFGPickerDialog(final Context context, KDSBGFG bgfg, OnBGFGPickerDlgListener listener) {
        g_instance = this;
        this.listener = listener;
        m_bgfgOriginal = bgfg;
        if (m_bgfgOriginal == null)
            createDefaultBGFG();;

        final View view = LayoutInflater.from(context).inflate(R.layout.kdsui_dlg_bgfg_picker, null);

        m_txtDemo = (TextView)view.findViewById(R.id.txtDemo);


        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIBGFGPickerDialog.this.listener != null) {
                            KDSUIBGFGPickerDialog.this.listener.onOk(KDSUIBGFGPickerDialog.this, getBgFg());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIBGFGPickerDialog.this.listener != null) {
                            KDSUIBGFGPickerDialog.this.listener.onCancel(KDSUIBGFGPickerDialog.this);
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if (KDSUIBGFGPickerDialog.this.listener != null) {
                            KDSUIBGFGPickerDialog.this.listener.onCancel(KDSUIBGFGPickerDialog.this);
                        }

                    }
                })
                .create();


        // kill all padding from the dialog window
        dialog.setView(view, 0, 0, 0, 0);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                g_instance = null;
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });

        m_btnBG = (Button)view.findViewById(R.id.btnBG);

        m_btnBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KDSUIColorPickerDialog d = new KDSUIColorPickerDialog(dialog.getContext(),m_bgfgOriginal.getBG(), new KDSUIColorPickerDialog.OnColorPickerDlgListener() {
                    @Override
                    public void onCancel(KDSUIColorPickerDialog dialog) {

                    }

                    @Override
                    public void onOk(KDSUIColorPickerDialog dialog, int color) {
                        m_txtDemo.setBackgroundColor(color);
                        m_bgfgOriginal.setBG(color);
                    }
                });
                d.show();
            }
        });

        m_btnFG = (Button)view.findViewById(R.id.btnFG);

        m_btnFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KDSUIColorPickerDialog d = new KDSUIColorPickerDialog(dialog.getContext(),m_bgfgOriginal.getFG(), new KDSUIColorPickerDialog.OnColorPickerDlgListener() {
                    @Override
                    public void onCancel(KDSUIColorPickerDialog dialog) {

                    }

                    @Override
                    public void onOk(KDSUIColorPickerDialog dialog, int color) {
                        m_txtDemo.setTextColor(color);
                        m_bgfgOriginal.setFG(color);
                    }
                });
                d.show();
            }
        });

        m_txtDemo.setBackgroundColor(m_bgfgOriginal.getBG());
        m_txtDemo.setTextColor(m_bgfgOriginal.getFG());
    }


    KDSBGFG getBgFg()
    {
        return m_bgfgOriginal;
    }


    public void show() {
        dialog.show();
    }

    public AlertDialog getDialog() {
        return dialog;
    }


}
