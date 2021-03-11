package com.bematechus.kdslib;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/9/21.
 */
public class KDSUIDlgInternetFile extends KDSUIDialogBase {
    TextView m_txtPathName = null;
    String m_strFileName = "";
    String m_strExtension = "";

    public Object getResult()
    {
        return m_strFileName;
    }
    private String getSuffix(String filename) {
        int dix = filename.lastIndexOf('.');
        if (dix < 0) {
            return "";
        } else {
            return filename.substring(dix + 1);
        }
    }

    /**
     *
     * @param context
     * @param strExtension
     * @param listener
     */
    public KDSUIDlgInternetFile(final Context context,String strExtension, KDSDialogBaseListener listener) {
        this.setUseCtrlEnterKey(true);
        this.int_dialog(context, listener, R.layout.kdsui_dlg_internet_file, "");
        this.setTitle(context.getString(R.string.input_internet_file));
        m_strExtension = strExtension;
        m_txtPathName = (TextView)this.getView().findViewById(R.id.txtFileName);
        m_txtPathName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String extension = getSuffix(m_txtPathName.getText().toString()).toLowerCase();
                if (m_strExtension == null || m_strExtension.length() == 0 || (extension.length() > 0 && m_strExtension.indexOf("." + extension + ";") >= 0))
                    KDSUIDlgInternetFile.this.enableOKButton(true);
                else
                    KDSUIDlgInternetFile.this.enableOKButton(false);
            }
        });

    }

    @Override
    public void onOkClicked() {//save data here

        m_strFileName = m_txtPathName.getText().toString();
    }


    public void show() {
        dialog.show();
        enableOKButton(false);
    }

}
