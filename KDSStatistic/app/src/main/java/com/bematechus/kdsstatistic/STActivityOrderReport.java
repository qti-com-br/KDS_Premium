package com.bematechus.kdsstatistic;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
//import android.support.v4.app.Fragment;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.TextView;
import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.ConditionStatistic;

public class STActivityOrderReport extends Activity implements STFragmentGeneral.OnGeneralFragmentInteraction, STFragmentOrderReport.OnOrderReportFragmentInteractionListener {

    STFragmentGeneral m_fragmentGeneral = null;
    STFragmentOrderReport m_fragmentReport = null;
    PowerManager.WakeLock m_wakeLock = null;
    TextView m_txtTitle = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lockAndroidWakeMode(true);
        //
        setContentView(R.layout.st_activity_order_report);

        m_txtTitle =(TextView) this.findViewById(R.id.txtReportTitle);

        Fragment f = this.getFragmentManager().findFragmentById(R.id.fragmentGeneral);

        m_fragmentGeneral = (STFragmentGeneral)f;//
        m_fragmentGeneral.setListener(this);
        Fragment frag = this.getFragmentManager().findFragmentById(R.id.fragmentReport);
        m_fragmentReport =  (STFragmentOrderReport) frag;
        m_fragmentReport.setListener(this);
        setReportMode();
        updateTitle();
    }
    public void updateTitle()
    {
        m_txtTitle.setText(this.getString(R.string.order_report));
    }
    protected  void setReportMode()
    {
        m_fragmentReport.setReportMode(ConditionBase.ReportMode.Order);
        m_fragmentGeneral.setReportMode(ConditionBase.ReportMode.Order);
    }
    public void lockAndroidWakeMode(boolean bLock)
    {
        if (m_wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RptWakeLock");
        }
        if (bLock) {
            m_wakeLock.acquire();
            this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {

            m_wakeLock.release();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void onGeneralFragmentInteraction(STFragmentOrderReport.ButtonCommand command)
    {


        onOrderReportFragmentInteraction(command, null);

    }

    public void onOrderReportFragmentInteraction(STFragmentOrderReport.ButtonCommand command, Object objParam)
    {
        if (command == STFragmentOrderReport.ButtonCommand.Load_Profile)
        {
            m_fragmentGeneral.restoreCondition((ConditionStatistic) objParam);
        }
        else if (command == STFragmentOrderReport.ButtonCommand.Refresh) {

            ConditionStatistic condition = m_fragmentGeneral.getCondition();
            //m_fragmentReport.setOriginalCondition(condition);
            m_fragmentReport.refreshReport(condition);
        }
    }
}
