package com.bematechus.kdslib;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/1/25 0025.
 */
public class KDSUIAboutDlg extends KDSUIDialogBase implements UpdateManager.UpdateEvents {
    public interface AboutDlgEvent
    {
        public void aboutDlgCallActivation();
    }
    AboutDlgEvent m_eventReceiver = null;

    private String APP_NAME = "";

    TextView m_txtInfo = null;
    Button m_btnUpdate = null;
    UpdateManager m_updateManager = null;
    TextView m_txtVersionInfo = null;
    TextView m_txtActivation = null;
    //MainActivity m_mainActivity = null;
    ImageView m_imageIcon = null;

//    public void setMainActivity(MainActivity a)
//    {
//        m_mainActivity = a;
//    }

    public KDSUIAboutDlg(final Context context, String strVersion, String appName, Drawable appIcon, AboutDlgEvent receiver) {
        this.int_information_dialog(context, R.layout.kdsui_about);
        this.setTitle(context.getString(R.string.about));
        APP_NAME = appName;
        m_eventReceiver = receiver;

        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtVersion);
        m_txtInfo.setText(strVersion);

        m_txtVersionInfo = (TextView)this.getView().findViewById(R.id.txtVersionInfo);
        m_imageIcon = (ImageView)this.getView().findViewById(R.id.imageView);
        m_imageIcon.setImageDrawable(appIcon);

        m_updateManager = new UpdateManager(this.getView().getContext());
        m_updateManager.setEventsReceiver(this);


        m_btnUpdate =  (Button)this.getView().findViewById(R.id.btnUpdate);
        m_btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnUpdateClicked();
            }
        });

        m_txtActivation = (TextView)this.getView().findViewById(R.id.txtActivation);
        if (KDSConst.ENABLE_FEATURE_ACTIVATION) {
            String activiationText = "";
            if (Activation.isActivationPassed()) {
                activiationText = "Active";

                if (!Activation.getStoreName().isEmpty())
                    activiationText += " (" + Activation.getStoreName() + ")";

            }
            else
                activiationText = "Inactive";
            //add serial number
//            ArrayList<String> ar = KDSSocketManager.getLocalAllMac();
//            if (ar.size() >0)
//                activiationText += "," + ar.get(0);
            activiationText += "," + Activation.getMySerialNumber();

            m_txtActivation.setText(activiationText);


            m_txtActivation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onActivationClicked();
                }
            });
        }
    }
   // static void showAbout(MainActivity context, KDSUser user,  String strVersion)
   static public void showAbout(Context context, String strVersion, String appName, Drawable appIcon, AboutDlgEvent receiver)
    {
        KDSUIAboutDlg dlg = new KDSUIAboutDlg(context, strVersion, appName, appIcon,receiver);
//        dlg.setKDSUser(user);
        //dlg.setMainActivity(context);
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

    public void onActivationClicked()
    {
        if (m_eventReceiver != null)
            m_eventReceiver.aboutDlgCallActivation();
        //m_mainActivity.doActivation(false, true, "");
        this.getDialog().hide();
    }


}
