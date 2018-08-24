package com.bematechus.kdsstatistic;

import android.util.Log;
import android.view.View;

import com.bematechus.kdslib.SOSReportCondition;
import com.bematechus.kdslib.SOSReportOneStation;
import com.bematechus.kdslib.SOSReportTimeSlotData;
import com.bematechus.kdslib.SOSStationConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.PointValue;

/**
 * Created by Administrator on 2018/3/19.
 */
public class SOSReportShowing {
    static final String TAG = "ReportShowing";

    SOSLinearLayout m_layoutParent = null;

    //ArrayList<View> m_stationsView = new ArrayList<>();

    //ArrayList<SOSStationConfig> m_arValidStations = new ArrayList<>();


    public SOSSettings getSettings()
    {
        return SOSKDSGlobalVariables.getKDSSOS().getSettings();
    }

    public void setParentLayout(SOSLinearLayout layout)
    {
        m_layoutParent = layout;
    }
    public SOSLinearLayout getParentLayout()
    {
        return m_layoutParent;
    }

//    public void addStation(String stationID)
//    {
//        if (findView(stationID)!= null) return;
//        View v = View.inflate(KDSApplication.getContext(), R.layout.listitem_sos_show, null);
//        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, Gravity.LEFT);
//        lp.weight = 1.0f;
//        v.setLayoutParams(lp);
//        v.setTag(stationID);
//
//        getParentLayout().addView(v);
//        m_stationsView.add(v);
//        initView(v);
//        showReport(stationID, null);
//
//    }

    public void initViews()
    {

        for (int i=0; i< m_layoutParent.getChildCount(); i++)
        {
            View v = m_layoutParent.getChildAt(i);

            if (v instanceof SOSRealTimeView)
            {
              initRealView((SOSRealTimeView)v);
            }
            else if (v instanceof SOSGraphView)
            {
                initGraphView (((SOSGraphView)v));
            }
        }



    }

    public void initRealView(SOSRealTimeView v)
    {
        v.init_for_running();
    }

    public  void initGraphView(SOSGraphView v)
    {
        v.init_for_running();
    }



//    public void clear()
//    {
//        getParentLayout().removeAllViews();
//        m_stationsView.clear();
//    }

    public ArrayList<View> findViews(String stationID)
    {

        ArrayList<View> ar = new ArrayList<>();
        for (int i=0; i< m_layoutParent.getChildCount(); i++)
        {
            View v = m_layoutParent.getChildAt(i);

            if (v instanceof SOSRealTimeView)
            {
                if (((SOSRealTimeView)v).isForStation(stationID))
                    ar.add(v);
            }
            else if (v instanceof SOSGraphView)
            {
                if (((SOSGraphView)v).isForStation(stationID))
                    ar.add(v);
            }
        }
        return ar;
    }

    public boolean refreshReportView(String stationID, SOSReportCreators creators)
    {
        Date dtStart = SOSReportCondition.getGraphEndTime();//. new Date(); //1.1.4

        SOSReportOneStation report = creators.getLatestStationReport(stationID, dtStart);
        //Log.d("KDSSOS",report.export2Xml());

        showReport(stationID, report);

//        SOSReportOneStation reportOverall = creators.getOverallStationReport(m_arValidStations,dtStart );
//        reportOverall.setConditionForShowing(report.getConditionForShowing());
//
//        showReport(KDSSOSSettings.OVERALL_STATION_ID, reportOverall);
        return true;

    }


    /**
     * show the report to view
     * @param stationID
     * @param report
     * @return
     */
    private boolean showReport(String stationID, SOSReportOneStation report)
    {
        ArrayList<View>  arViews = findViews(stationID);
        if (arViews.size()<=0)
            return false;

        if (report != null) {
            showReportForRealTime(stationID, arViews, report);
            return showChart(stationID, arViews,report);
        }
        return true;
    }

    private void showReportForRealTime(String stationID, ArrayList<View> arStationViews, SOSReportOneStation report )
    {
        for (int i=0; i< arStationViews.size(); i++)
        {
            if (arStationViews.get(i) instanceof SOSRealTimeView)
                showReportForRealTime(stationID, (SOSRealTimeView) arStationViews.get(i), report);
        }
    }

    private void showReportForRealTime(String stationID, SOSRealTimeView v, SOSReportOneStation report)
    {
       // TextView txtRealTime = (TextView) v.findViewById(R.id.txtRealTime);
        v.setRealTimeSeconds( Math.round(report.getRealTime().getAverageBumpTime()*60 ));

        v.setOverTargetCount( Math.round( report.getRealTime().getOverTargetCount()) );

        float flt = 0;
        if (report.getRealTime().getCount() >0)
            flt =1f- report.getRealTime().getOverTargetCount()/ report.getRealTime().getCount();
        v.setPercent(flt);



//        //txtRealTime.setText(report.getRealTimePrepTimeString());
//        SOSStationConfig config = getStationConfig(stationID);
//
//        if (config != null)
//        {
//            v.setTartTimeSeconds((int)(config.getTargetPrepTime() * 60) );
////            if (report.getRealTime().getAverageBumpTime() > config.getTargetPrepTime())
////            {
////                KDSViewFontFace ff = getSettings().getKDSViewFontFace(KDSSOSSettings.ID.Real_time_alert_font);
//////                txtRealTime.setTextColor(ff.getFG());
//////                txtRealTime.setBackgroundColor(ff.getBG());
//////                txtRealTime.setTextSize(ff.getFontSize());
////            }
//        }
        v.invalidate();
    }

    private boolean showChart(String stationID, ArrayList<View> arViews, SOSReportOneStation report)
    {
        List<AxisValue> xLabels = getAxisXLabels(report);
        List<PointValue> ptValues = getAxisPoints(report);

        SOSGraphView.reverseAxisX(xLabels);
        SOSGraphView.reversePoints(ptValues);


        for (int i=0; i< arViews.size(); i++)
        {
            if (arViews.get(i) instanceof SOSGraphView) {
                SOSGraphView graph = (SOSGraphView) arViews.get(i);
                SOSGraphView.showLineChart(graph, xLabels, ptValues);
            }
        }

        return true;
    }


    /**
     * set x axis labels
     */
    private List<AxisValue> getAxisXLabels(SOSReportOneStation report){
        ArrayList<AxisValue> axisXValues = new ArrayList<>();

        for ( int i = 0; i < report.getTimeslots().size(); i++)  {

            String s = report.getTimeslotLabelForGraph(i);
            AxisValue v = new AxisValue(i);
            v.setLabel(s);
            Log.d(TAG, s);
            axisXValues.add(v);//(new AxisValue(i).setLabel(s));
        }
        return axisXValues;
    }

    final float MIN_CHART_VALUE = 1.0f;
    final float MAX_CHART_VALUE = 60.0f;
    /**
     * get points values
     */
    private List<PointValue> getAxisPoints(SOSReportOneStation report) {
        ArrayList<PointValue> ar = new ArrayList<>();

        for (int i = 0; i < report.getTimeslots().size(); i++) {
            SOSReportTimeSlotData t = report.getTimeslots().get(i);
            float val = t.getAverageBumpTime();
            if (val >0 && val < MIN_CHART_VALUE)  //1.1.6
                val = MIN_CHART_VALUE;
            if (val > MAX_CHART_VALUE)
                val = MAX_CHART_VALUE;
            ar.add(new PointValue(i, val));

        }
        return ar;
    }

    public void updateSettings(SOSSettings settings)
    {
        //String s= settings.getString(KDSSOSSettings.ID.SOS_Stations);
       // m_arValidStations = KDSSOSSettings.parseStations(s);

    }

    private SOSStationConfig getStationConfig(String stationID)
    {
        ArrayList<SOSStationConfig> ar =  getAllEnabledStations();

        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).getStationID().equals(stationID))
                return ar.get(i);
        }
        return null;
    }

    public void updateStationStatus(String stationID, SOSKDSStationSOSInfo.StationSOSStatus status)
    {
        ArrayList<View> ar = findViews(stationID);
        if (ar.size() <=0) return;

        String str = SOSKDSStationSOSInfo.getStatusInfo(status);

        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i) instanceof SOSRealTimeView)
            {
                ((SOSRealTimeView)ar.get(i)).setBottomText(str);
            }
        }
    }

    public boolean isStationEnabled(String stationID)
    {
        return (findViews(stationID).size()>0);
    }

    private boolean existedStation(ArrayList<SOSStationConfig> ar, String stationID)
    {
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).getStationID().equals(stationID))
                return true;
        }
        return false;
    }
    public ArrayList<SOSStationConfig> getAllEnabledStations()
    {
        ArrayList<SOSStationConfig> ar = new ArrayList<>();


        int ncount = m_layoutParent.getChildCount();

        for (int i=0; i< ncount; i++)
        {
            View v = m_layoutParent.getChildAt(i);
            if (v instanceof SOSRealTimeView)
            {
                SOSStationConfig c = new SOSStationConfig(((SOSRealTimeView) v).m_properties.m_stationID);
                c.setTargetPrepTime(((SOSRealTimeView) v).m_properties.getTargetMinutes());
                if (!existedStation(ar, c.getStationID()))
                    ar.add(c);


            }
            else if (v instanceof SOSGraphView)
            {
                SOSStationConfig c = new SOSStationConfig(((SOSGraphView) v).m_properties.m_stationID);
                c.setTargetPrepTime(((SOSGraphView) v).m_properties.getTargetMinutes());
                if (!existedStation(ar, c.getStationID()))
                    ar.add(c);
            }
        }
        return ar;

    }

}




































