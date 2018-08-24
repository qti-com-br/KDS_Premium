package com.bematechus.kdsstatistic;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import com.bematechus.kdslib.ConditionStatistic;

import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.TimeSlotEntry;
import com.bematechus.kdslib.TimeSlotEntryDetail;
import com.bematechus.kdslib.TimeSlotOrderReport;

/**
 * Created by Administrator on 2016/8/15.
 */
public class STReportGrid {

    ArrayList<TextView> m_arTitleCellViews = new ArrayList<>();
    ArrayList<View> m_arTitleLineViews = new ArrayList<>();
    ListView m_listItems = null;
    LinearLayout m_linearTitles = null;
    TextView m_txtTitle = null;
    TextView m_txtInfo0 = null;
    TextView m_txtInfo1 = null;
    TextView m_txtInfo2 = null;
    TextView m_txtInfo3 = null;
    TextView m_txtInfo4 = null;
    TextView m_txtInfo5 = null;

    LinearLayout m_infoRow0 = null;
    LinearLayout m_infoRow1 = null;
    LinearLayout m_infoRow2 = null;

    TimeSlotOrderReport m_report = null;

    Context m_context = null;

    ConditionStatistic.OrderReportContent m_reportContent = ConditionStatistic.OrderReportContent.Counter;
    /**
     *
     * @param reportView
     *  The view that create report GUI.
     *   See st_fragment_order_report.xml
     */
    public  void init(Context context, View reportView)
    {
        m_context = context;
        m_listItems = (ListView) reportView.findViewById(R.id.lstData);
        int ids[] = new int[]{
                R.id.colFixed, R.id.col0,  R.id.col1,  R.id.col2,   R.id.col3,
                R.id.col4,      R.id.col5,  R.id.col6,  R.id.col7,  R.id.col8,
                R.id.col9,      R.id.col10, R.id.col11, R.id.col12, R.id.col13,
                R.id.col14,     R.id.col15, R.id.col16,
//                R.id.col17, R.id.col18,
//                R.id.col19,     R.id.col20, R.id.col21, R.id.col22, R.id.col23,
//                R.id.col24,     R.id.col25, R.id.col26, R.id.col27, R.id.col28,
//                R.id.col29,     R.id.col30,R.id.col31,  R.id.col32, R.id.col33
        };

        for (int i=0; i< ids.length; i++)
        {
            m_arTitleCellViews.add((TextView) reportView.findViewById(ids[i]));

        }

        int lines[] = new int[]{
                R.id.line0,  R.id.line1,  R.id.line2,   R.id.line3,
                R.id.line4,      R.id.line5,  R.id.line6,  R.id.line7,  R.id.line8,
                R.id.line9,      R.id.line10, R.id.line11, R.id.line12, R.id.line13,
                R.id.line14,     R.id.line15, R.id.line16,
//                R.id.line17, R.id.line18,
//                R.id.line19,     R.id.line20, R.id.line21, R.id.line22, R.id.line23,
//                R.id.line24,     R.id.line25, R.id.line26, R.id.line27, R.id.line28,
//                R.id.line29,     R.id.line30,R.id.line31,  R.id.line32, R.id.line33
        };

        for (int i=0; i< lines.length; i++)
        {
            m_arTitleLineViews.add(reportView.findViewById(lines[i]));

        }


        m_listItems.setAdapter(new MyAdapter(context, new ArrayList<TimeSlotEntry>()));
        m_listItems.focusableViewAvailable(m_listItems);

        m_linearTitles = (LinearLayout)reportView.findViewById(R.id.linearTitle);

        m_txtTitle = (TextView) reportView.findViewById(R.id.txtTitle);
        m_txtInfo0 = (TextView) reportView.findViewById(R.id.txtInfo0);
        m_txtInfo1 = (TextView) reportView.findViewById(R.id.txtInfo1);
        m_txtInfo2 = (TextView) reportView.findViewById(R.id.txtInfo2);
        m_txtInfo3 = (TextView) reportView.findViewById(R.id.txtinfo3);
        m_txtInfo4 = (TextView) reportView.findViewById(R.id.txtInfo4);
        m_txtInfo5 = (TextView) reportView.findViewById(R.id.txtinfo5);
        m_infoRow0 = (LinearLayout) reportView.findViewById(R.id.linearInfoRow0);
        m_infoRow1 = (LinearLayout) reportView.findViewById(R.id.linearInfoRow0);
        m_infoRow2 = (LinearLayout) reportView.findViewById(R.id.linearInfoRow0);


    }
    public void refresh()
    {
        this.showOrderReport(m_report);
    }
    public TimeSlotOrderReport getReport()
    {
        return m_report;
    }
    public void showOrderReport(TimeSlotOrderReport report)
    {

        m_report = report;
        if (report == null)
        {
            this.setCols(0);
            return;
        }
        m_report.getCondition().setOrderReportContent(m_reportContent);
        //report.revertRowCol();
        int ncols = report.getNeedCols();
        this.setCols(ncols);
        showTitles(report);
        ((MyAdapter)m_listItems.getAdapter()).setReportContnet( report.getCondition().getOrderReportContent());
        ((MyAdapter)m_listItems.getAdapter()).setListData(report.getData());
        ((MyAdapter) m_listItems.getAdapter()).notifyDataSetChanged();
    }

    /**
     *
     * @param report
     */
    public void combineReport(TimeSlotOrderReport report)
    {

        if (m_report == null ||
           (! m_report.getCondition().isSameCondition(report.getCondition())) ||
                m_report.getData().size() <=0) {
            showOrderReport(report);
            return;
        }
        if (report.getData().size() <=0) return;
        TimeSlotOrderReport.removeTotal(m_report);
        if (report.getCondition().getReportMode() == ConditionBase.ReportMode.Order)
            combineOrderReport(report);
        else
        {
            combineItemReport(report);
        }


        TimeSlotOrderReport.addTotal(m_report);



        m_report.getCondition().setOrderReportContent(m_reportContent);
        //report.revertRowCol();
        int ncols = m_report.getNeedCols();
        this.setCols(ncols);
        showTitles(m_report);
        ((MyAdapter)m_listItems.getAdapter()).setReportContnet( m_report.getCondition().getOrderReportContent());
        ((MyAdapter)m_listItems.getAdapter()).setListData(m_report.getData());
        ((MyAdapter) m_listItems.getAdapter()).notifyDataSetChanged();
    }

    private void combineOrderReport(TimeSlotOrderReport report)
    {
        int existedStationIndex = -1;

        String dataStationID =  report.getData().get(0).getData().get(0).getStationID();//.getCondition().getStationFrom();
        for (int i=0; i< m_report.getData().get(0).getData().size(); i++)
        {
            if (m_report.getData().get(0).getData().get(i).getStationID().equals(dataStationID))
                existedStationIndex = i;
        }
        if (existedStationIndex ==-1) {
            for (int i = 0; i < m_report.getData().size(); i++) {
                m_report.getData().get(i).getData().add(report.getData().get(i).getData().get(0));
            }
        }
        else
        {
            for (int i = 0; i < m_report.getData().size(); i++) {
                m_report.getData().get(i).getData().set(existedStationIndex, report.getData().get(i).getData().get(0));
            }
        }
    }

    private void combineItemReport(TimeSlotOrderReport report)
    {
        int existedStationIndex = -1;

        String dataStationID =  report.getData().get(0).getData().get(0).getStationID();//.getCondition().getStationFrom();
        for (int i=0; i< m_report.getData().get(0).getData().size(); i++)
        {
            if (m_report.getData().get(0).getData().get(i).getStationID().equals(dataStationID))
                existedStationIndex = i;
        }

        int stationsCount = m_report.getData().get(0).getData().size();
        if (existedStationIndex ==-1) {//this is a new station
            stationsCount ++;
            for (int i = 0; i < report.getData().size(); i++) {
                if ( report.getData().get(i).getData().get(0).getStationID().equals("-1")) continue;
                String dataText = report.getData().get(i).getFixedText();
                int nExistItemIndex = findItemInReport(m_report, dataText);

                appendReportCell(m_report,nExistItemIndex, report.getData().get(i).getData().get(0) ,stationsCount,dataText);

            }
            checkEmptyCellAfterAddNewStation(m_report, stationsCount, dataStationID);
        }
        else
        {
            //
            for (int i = 0; i < report.getData().size(); i++) {
                if ( report.getData().get(i).getData().get(0).getStationID().equals("-1")) continue;
                String dataText = report.getData().get(i).getFixedText();
                if (dataText.isEmpty()) continue;
                int nExistItemIndex = findItemInReport(m_report, dataText);
                //
                addOrReplaceCell(m_report,existedStationIndex,nExistItemIndex, report.getData().get(i).getData().get(0),stationsCount,dataText );
            }
        }
        removeEmptyRow(m_report);
    }

    /**
     * in item report, in order to show null report, I use "" as empty data row.
     * While combine report, we have to remove this null row
     * @param report
     */
    private void removeEmptyRow(TimeSlotOrderReport  report)
    {
        for (int i=report.getData().size()-1; i>=0 ; i--)
        {
            if (report.getData().get(i).getFixedText().isEmpty())
            {
                report.getData().remove(i);
            }
        }
    }
    private void checkEmptyCellAfterAddNewStation(TimeSlotOrderReport  report, int nStationsCount, String newStationID)
    {
        for (int i=0; i< report.getData().size(); i++)
        {
            if (report.getData().get(i).getData().size()<nStationsCount)
            {
                report.getData().get(i).getData().add(new TimeSlotEntryDetail(newStationID, 0, 0));
            }
        }
    }
    private void addOrReplaceCell(TimeSlotOrderReport report, int nCol, int nRow, TimeSlotEntryDetail detailCell, int stationCount, String dataText )
    {
        if (nCol < 0) return;
        if (nRow >=0)
        { //this new data is existed in old table.
            report.getData().get(nRow).getData().set(nCol, detailCell);
        }
        else
        {//add new data row
            TimeSlotEntry t = new TimeSlotEntry();
            appendRowCell(report, t, stationCount);
            t.getData().set(nCol, detailCell);
            report.getData().add(t);
        }

    }

    /**
     *
     * @param report
     * @param nRow
     * @param detailCell
     * @param stationCount
     * @param dataText
     */
    private void appendReportCell(TimeSlotOrderReport report, int nRow,TimeSlotEntryDetail detailCell, int stationCount ,String dataText)
    {
        if (nRow >=0)
            report.getData().get(nRow).getData().add(detailCell);
        else {
            ///
            TimeSlotEntry t = new TimeSlotEntry();
            appendRowCell(report, t, stationCount);
            t.getData().set(stationCount-1,detailCell);
            t.setFixedText(dataText);
            report.getData().add(t);
        }
    }

    private void appendRowCell(TimeSlotOrderReport report,TimeSlotEntry entry, int stationCount)
    {
        //for (int i=0; i<report.getData().get(0).getData().size(); i++)
        for (int i=0; i< stationCount; i++)
        {
            TimeSlotEntryDetail t = null;
            if (i>= report.getData().get(0).getData().size())
                entry.add("255", 0, 0);
            else
            {
                t = report.getData().get(0).getData().get(i);

                entry.add(t.getStationID(), 0, 0);
            }
        }

    }
    private int findItemInReport(TimeSlotOrderReport report, String itemDescription)
    {
        for (int i=0; i< report.getData().size(); i++)
        {
            if (report.getData().get(i).getFixedText().equals(itemDescription))
                return i;
        }
        return -1;
    }

    public void showTitles(TimeSlotOrderReport report)
    {

        m_txtTitle.setText( report.getTitleString());
        showInfomation(report);
        if (report.getData().size()<=0) return;


        for (int i = 0; i< report.getData().get(0).getData().size()-1; i++)
        {
            TextView v =m_arTitleCellViews.get(i+1);
            String stationID =  report.getData().get(0).getData().get(i).getStationID();
            String s = "#" + stationID;
            v.setText(s);
        }
        setTotalTitleText(report,report.getCondition().getOrderReportContent() );


        m_arTitleCellViews.get(0).setText(report.getFixedColString());


    }

    private void setTotalTitleText(TimeSlotOrderReport report, ConditionStatistic.OrderReportContent content)
    {

        int n = report.getData().get(0).getData().size();

        TextView v =m_arTitleCellViews.get(n);//total col
        if (content == ConditionStatistic.OrderReportContent.Counter)
            v.setText(m_context.getString(R.string.total));
        else if (content == ConditionStatistic.OrderReportContent.PrepTime)
            v.setText(m_context.getString(R.string.total_mins));
    }
    /**
     * show some summary basicly information
     * @param report
     */
    public void showInfomation(TimeSlotOrderReport report)
    {
        setInfo0Color(report);
        switch (report.getCondition().getReportType())
        {

            case Daily:
                showDailyReportCaptions(report);
                break;
            case Weekly:
                showWeeklyReportCaptions(report);
                break;
            case Monthly:
                showMonthlyReportCaptions(report);
                break;
            case OneTime:
                showOnetimeReportCaptions(report);
                break;
        }
    }

    /**
     * Date: D0				From Time:		To Time:
     Total Order Count				Total Order Prep Time
     Average order count per time slot				Average Order Prep Time

     * @param rp
     */
    private void showDailyReportCaptions(TimeSlotOrderReport rp)
    {
        m_infoRow0.setVisibility(View.VISIBLE);
        m_infoRow1.setVisibility(View.VISIBLE);
        m_infoRow2.setVisibility(View.VISIBLE);
        //
        String s = KDSUtil.convertDateToShortString(rp.getCondition().getDailyReportCondition().getDate());
        m_txtInfo0.setText( m_context.getString(R.string.information_date) + " " +s);
        s =  m_context.getString(R.string.information_time_from)+ " " + KDSUtil.convertTimeToShortString( rp.getCondition().getDailyReportCondition().getTimeFrom());
        s += " " + m_context.getString(R.string.information_time_to)+ " "  + KDSUtil.convertTimeToShortString( rp.getCondition().getDailyReportCondition().getTimeTo());
        m_txtInfo1.setText(s);

        //
        if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Order) {
            s = m_context.getString(R.string.total_orders_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
            m_txtInfo2.setText(s);
            s = m_context.getString(R.string.total_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo3.setText(s);

            s = m_context.getString(R.string.average_orders_per_timeslot) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderCountPerTimeslot());
            m_txtInfo4.setText(s);
            s = m_context.getString(R.string.average_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo5.setText(s);
        }
        else if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Item)
        {
                s = m_context.getString(R.string.total_items_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
                m_txtInfo2.setText(s);
                s = m_context.getString(R.string.total_items_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo3.setText(s);

                s = m_context.getString(R.string.average_item_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo4.setText(s);

        }

    }

    /**
     * 	From Date: D0				To Date: Dn
     Total Order Count				Total Order Prep Time
     Average Order count per day				Average Order Prep Time

     * @param rp
     */
    private void showWeeklyReportCaptions(TimeSlotOrderReport rp)
    {
        m_infoRow0.setVisibility(View.VISIBLE);
        m_infoRow1.setVisibility(View.VISIBLE);
        m_infoRow2.setVisibility(View.VISIBLE);
        //
        String s = KDSUtil.convertDateToShortString(rp.getCondition().getWeeklyCondition().getWeekFirstDayDate());
        m_txtInfo0.setText( m_context.getString(R.string.information_from_date)+ " "+s);
        s = KDSUtil.convertDateToShortString(rp.getCondition().getWeeklyCondition().getWeekLastDayDate());
        m_txtInfo1.setText( m_context.getString(R.string.information_to_date)+ " "+s);
        if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Order) {
            //
            s = m_context.getString(R.string.total_orders_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
            m_txtInfo2.setText(s);
            s = m_context.getString(R.string.total_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo3.setText(s);

            s = m_context.getString(R.string.average_orders_per_timeslot) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderCountPerTimeslot());
            m_txtInfo4.setText(s);
            s = m_context.getString(R.string.average_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo5.setText(s);
        }
        else if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Item)
        {

                s = m_context.getString(R.string.total_items_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
                m_txtInfo2.setText(s);
                s = m_context.getString(R.string.total_items_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo3.setText(s);

                s = m_context.getString(R.string.average_item_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo4.setText(s);

        }
    }

    /**
     * From Date: D0				To Date: Dn
     Total Order Count				Total Order Prep Time
     Average Order count per day				Average Order Prep Time

     * @param rp
     */
    private void showMonthlyReportCaptions(TimeSlotOrderReport rp)
    {
        m_infoRow0.setVisibility(View.VISIBLE);
        m_infoRow1.setVisibility(View.VISIBLE);
        m_infoRow2.setVisibility(View.VISIBLE);
        //
        String s = KDSUtil.convertDateToShortString(rp.getCondition().getMonthlyCondition().getMonthFirstDay());
        m_txtInfo0.setText( m_context.getString(R.string.information_from_date)+ " "+s);
        s = KDSUtil.convertDateToShortString(rp.getCondition().getMonthlyCondition().getMonthLastDay());
        m_txtInfo1.setText(m_context.getString(R.string.information_to_date)+ " "+s);

        //
        if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Order) {
            s = m_context.getString(R.string.total_orders_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
            m_txtInfo2.setText(s);
            s = m_context.getString(R.string.total_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo3.setText(s);

            s = m_context.getString(R.string.average_orders_per_timeslot) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderCountPerTimeslot());
            m_txtInfo4.setText(s);
            s = m_context.getString(R.string.average_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo5.setText(s);
        }
        else if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Item)
        {

                s = m_context.getString(R.string.total_items_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
                m_txtInfo2.setText(s);
                s = m_context.getString(R.string.total_items_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo3.setText(s);

                s = m_context.getString(R.string.average_item_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo4.setText(s);

        }
    }

    private void setInfo0Color(TimeSlotOrderReport rp)
    {
        switch (rp.getCondition().getReportType())
        {

            case Daily:

            case Weekly:

            case Monthly:
                m_txtInfo0.setTextColor(Color.BLUE);
                break;
            case OneTime:
                m_txtInfo0.setTextColor(m_txtInfo1.getCurrentTextColor());
                break;
        }
    }
    /**
     * From Date:	D0	To Date:	Dn		From Time:	T0	To Time:	T1
     Total Order Count					Total Order Prep Time
     Average Order count per day					Average Order Prep Time

     * @param rp
     */
    private void showOnetimeReportCaptions(TimeSlotOrderReport rp)
    {
        m_infoRow0.setVisibility(View.VISIBLE);
        m_infoRow1.setVisibility(View.VISIBLE);
        m_infoRow2.setVisibility(View.VISIBLE);
        //
        String text = "";
        String s = KDSUtil.convertDateToShortString(rp.getCondition().getOneTimeCondition().getDateFrom());
        text = m_context.getString(R.string.information_date_from)+ " " + s;
        s = KDSUtil.convertDateToShortString(rp.getCondition().getOneTimeCondition().getDateTo());
        text += " " + m_context.getString(R.string.information_date_to)+ " "+s;
        m_txtInfo0.setText(text);

        s =m_context.getString(R.string.information_time_from) + " " + KDSUtil.convertTimeToShortString( rp.getCondition().getOneTimeCondition().getTimeFrom());

        s +=" " + m_context.getString(R.string.information_time_to)+ " " + KDSUtil.convertTimeToShortString( rp.getCondition().getOneTimeCondition().getTimeTo());
        m_txtInfo1.setText(s);
        if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Order) {
            //
            s = m_context.getString(R.string.total_orders_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
            m_txtInfo2.setText(s);
            s = m_context.getString(R.string.total_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo3.setText(s);

            s = m_context.getString(R.string.average_orders_per_timeslot) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderCountPerTimeslot());
            m_txtInfo4.setText(s);
            s = m_context.getString(R.string.average_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
            m_txtInfo5.setText(s);
        }
        else if (rp.getCondition().getReportMode() == ConditionBase.ReportMode.Item)
        {

                s = m_context.getString(R.string.total_items_count) + " " + KDSUtil.convertIntToString(rp.getTotalOrderCount());
                m_txtInfo2.setText(s);
                s = m_context.getString(R.string.total_items_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getTotalBumpTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo3.setText(s);
                s = m_context.getString(R.string.average_item_prep_time) + " " + KDSUtil.convertFloatToShortString(rp.getAverageOrderPrepTime()) + " " + m_context.getString(R.string.mins);
                m_txtInfo4.setText(s);

        }

    }

    public void setCols(int nCols)
    {
        for (int i=0; i< nCols; i++)
            m_arTitleCellViews.get(i).setVisibility(View.VISIBLE);
        for (int i = nCols; i< m_arTitleCellViews.size(); i++)
            m_arTitleCellViews.get(i).setVisibility(View.GONE);

        for (int i=0; i< nCols-1; i++)
            m_arTitleLineViews.get(i).setVisibility(View.VISIBLE);
        if (nCols >0) {
            for (int i = nCols - 1; i < m_arTitleLineViews.size(); i++)
                m_arTitleLineViews.get(i).setVisibility(View.GONE);
        }

        ((MyAdapter)m_listItems.getAdapter()).setCols(nCols);
        m_linearTitles.setWeightSum(nCols +1);

    }

    public void setReportContent(ConditionStatistic.OrderReportContent content)
    {
        m_reportContent = content;
        if (this.getReport() == null) return;
        setTotalTitleText(this.getReport(),content );
        ((MyAdapter)m_listItems.getAdapter()).setReportContnet(content);
        ((MyAdapter)m_listItems.getAdapter()).notifyDataSetChanged();
    }

    private class RowCells
    {
        public ArrayList<TextView> m_arCellViews = new ArrayList<>();
        public ArrayList<View> m_arCellLines = new ArrayList<>();
        public TimeSlotEntry m_rowEntry = new TimeSlotEntry();
    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public List<TimeSlotEntry> m_listData; //KDSStationsRelation class array


        ConditionStatistic.OrderReportContent m_reportContent = ConditionStatistic.OrderReportContent.Counter;


        private LinearLayout m_linearRow = null;
        int m_nCols = 3;

        public MyAdapter(Context context, List<TimeSlotEntry> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public void setReportContnet(ConditionStatistic.OrderReportContent content)
        {
            m_reportContent = content;
        }

        public List<TimeSlotEntry> getListData()
        {
            return m_listData;
        }
        public void setListData(List<TimeSlotEntry> lst)
        {
            m_listData = lst;
        }
        public int getCount() {

            return m_listData.size();
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        public void setCols(int nCols)
        {
            m_nCols = nCols;
            if (nCols == 0)
                m_listData.clear();
        }
        public int getCols()
        {
            return m_nCols;
        }

        public void init_views(View convertView, RowCells row)
        {
            row.m_arCellViews.clear();
            int ids[] = new int[]{
                    R.id.colFixed, R.id.col0, R.id.col1, R.id.col2, R.id.col3,
                    R.id.col4, R.id.col5, R.id.col6, R.id.col7, R.id.col8,
                    R.id.col9, R.id.col10, R.id.col11, R.id.col12, R.id.col13,
                    R.id.col14, R.id.col15, R.id.col16,
//                                                        R.id.col17, R.id.col18,
//                    R.id.col19, R.id.col20, R.id.col21, R.id.col22, R.id.col23,
//                    R.id.col24, R.id.col25, R.id.col26, R.id.col27, R.id.col28,
//                    R.id.col29, R.id.col30, R.id.col31, R.id.col32, R.id.col33
            };

            for (int i = 0; i < ids.length; i++) {
                row.m_arCellViews.add((TextView) convertView.findViewById(ids[i]));

            }
            row.m_arCellLines.clear();
            int lines[] = new int[]{
                    R.id.line0,  R.id.line1,  R.id.line2,   R.id.line3,
                    R.id.line4,      R.id.line5,  R.id.line6,  R.id.line7,  R.id.line8,
                    R.id.line9,      R.id.line10, R.id.line11, R.id.line12, R.id.line13,
                    R.id.line14,     R.id.line15, R.id.line16,
//                                                                R.id.line17, R.id.line18,
//                    R.id.line19,     R.id.line20, R.id.line21, R.id.line22, R.id.line23,
//                    R.id.line24,     R.id.line25, R.id.line26, R.id.line27, R.id.line28,
//                    R.id.line29,     R.id.line30,R.id.line31,  R.id.line32, R.id.line33
            };

            for (int i=0; i< lines.length; i++)
            {
                row.m_arCellLines.add(convertView.findViewById(lines[i]));

            }

        }


        public void resetColsView(RowCells row)
        {
            for (int i=0; i< m_nCols; i++)
            {
                row.m_arCellViews.get(i).setVisibility(View.VISIBLE);
            }
            for (int i=0; i< m_nCols-1; i++)
                row.m_arCellLines.get(i).setVisibility(View.VISIBLE);

            for (int i=m_nCols; i< row.m_arCellViews.size(); i++)
            {
                row.m_arCellViews.get(i).setVisibility(View.GONE);
            }
            for (int i=m_nCols-1; i< row.m_arCellLines.size(); i++)
                row.m_arCellLines.get(i).setVisibility(View.GONE);
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            RowCells row = null;
            TimeSlotEntry r =  m_listData.get(position);

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.st_listitem_report, null);
                row = new RowCells();
                row.m_rowEntry = r;
                init_views(convertView, row);
                convertView.setTag(row);
                // init_views(convertView);
            }
            else
            {
                row = (RowCells) convertView.getTag();
                row.m_rowEntry = r;


            }

            resetColsView(row);

            m_linearRow =(LinearLayout) convertView.findViewById(R.id.linearRow);
            m_linearRow.setWeightSum(m_nCols+1);


            //init_views(convertView);

            row.m_arCellViews.get(0).setText(r.getFixedText());
            if (m_reportContent == ConditionStatistic.OrderReportContent.Counter) {
                for (int i = 1; i < m_nCols; i++) {

                    row.m_arCellViews.get(i).setText(r.getCounterText(i - 1));
                }
            }
            else if (m_reportContent == ConditionStatistic.OrderReportContent.PrepTime)
            {
                for (int i = 1; i < m_nCols; i++) {

                    row.m_arCellViews.get(i).setText(r.getBumpTimeMinsText(i - 1));

                }
            }



            return convertView;
        }


    }

    public void clear()
    {
        TextView ar[] = new TextView[]{ m_txtInfo0,m_txtInfo1,m_txtInfo2,m_txtInfo3,m_txtInfo4,m_txtInfo5};
        for (int i=0; i< ar.length; i++)
        {
            ar[i].setText("");
        }
        m_txtTitle.setText("");
        setCols(0);
        ((MyAdapter)m_listItems.getAdapter()).notifyDataSetChanged();
        m_report = null;

    }
}
