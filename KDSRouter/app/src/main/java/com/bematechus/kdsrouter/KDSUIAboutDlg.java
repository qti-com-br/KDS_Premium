package com.bematechus.kdsrouter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bematechus.kdslib.UpdateManager;
/**
 *
 * Most of it is same as KDS file
 */

/**
 * Created by Administrator on 2016/1/25 0025.
 */
public class KDSUIAboutDlg extends KDSUIDialogBase implements UpdateManager.UpdateEvents {

    final static String APP_NAME = "kdsrouter";

    TextView m_txtInfo = null;
    Button m_btnUpdate = null;
    UpdateManager m_updateManager = null;
    TextView m_txtVersionInfo = null;

    public KDSUIAboutDlg(final Context context, String strVersion) {
        this.int_information_dialog(context, R.layout.kdsui_about);
        this.setTitle(context.getString(R.string.about));
        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtVersion);
        m_txtInfo.setText(strVersion);

        m_txtVersionInfo = (TextView)this.getView().findViewById(R.id.txtVersionInfo);

        m_updateManager = new UpdateManager(this.getView().getContext());
        m_updateManager.setEventsReceiver(this);

        m_btnUpdate =  (Button)this.getView().findViewById(R.id.btnUpdate);
        m_btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnUpdateClicked();
            }
        });

    }
    static void showAbout(Context context, String strVersion)
    {
        KDSUIDialogBase dlg = new KDSUIAboutDlg(context, strVersion);

        dlg.show();
    }

    public  void onBtnUpdateClicked()
    {
        m_btnUpdate.setText(getView().getContext().getString(R.string.checking));

        m_updateManager.checkUpdateInfo(APP_NAME);
    }

    public void onNoNewVersionApk()
    {
        m_txtVersionInfo.setText(this.getView().getContext().getString(R.string.it_is_latest_version));
        m_btnUpdate.setText(getView().getContext().getString(R.string.check_new_version));
    }
    public void onUpdateCanceled()
    {
        m_txtVersionInfo.setText(this.getView().getContext().getString(R.string.update_canceled));
        m_btnUpdate.setText(getView().getContext().getString(R.string.check_new_version));
    }
    public void onUpdateError(String strError)
    {
        m_txtVersionInfo.setText(strError);
        m_btnUpdate.setText(getView().getContext().getString(R.string.check_new_version));
    }
    public void onNewVersionValid(String newVersion)
    {
        m_txtVersionInfo.setText(this.getView().getContext().getString(R.string.found_new_version)+":"+newVersion);
        m_btnUpdate.setText(getView().getContext().getString(R.string.check_new_version));
    }


}
