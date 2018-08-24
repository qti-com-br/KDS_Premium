package com.bematechus.kdsstatistic;

//import android.app.Fragment;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.ConditionOneTime;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.MySpinnerArrayAdapter;

/**

 */
public class STFragmentGeneral extends Fragment implements KDSUIDialogBase.KDSDialogBaseListener {
    static final int MAX_STATIONS_COUNT = 256;
    Date m_dtDailyFrom = new Date();
    Date m_dtDailyTo = new Date();
    Date m_dtTimeFrom = new Date();
    Date m_dtTimeTo = new Date();

    Spinner m_spStationFrom = null;
    Spinner m_spStationTo = null;
    Spinner m_spOrderReportContent = null;
    Spinner m_spReportType = null;
    Spinner m_spReportArrangment = null;
    Spinner m_spTimeSlot = null;
    Spinner m_spDayOfWeek = null;
    Spinner m_spItemDataMode = null;


    Button m_btnDateFrom = null;
    Button m_btnDateTo = null;
    Button m_btnTimeFrom = null;
    Button m_btnTimeTo = null;

    CheckBox m_chkDayOfWeek = null;

    TextView m_txtError = null;

    EditText m_txtItemDescription = null;
   // CheckBox m_chkTimeSlot = null;

    ArrayList<View> m_arOneTimeViews = new ArrayList<>(); //for hide/show views
    ArrayList<View> m_arItemViews = new ArrayList<>(); //for item report options

    ConditionBase.ReportMode m_reportMode = ConditionBase.ReportMode.Order;


    private OnGeneralFragmentInteraction mListener;

    public void setReportMode(ConditionBase.ReportMode mode)
    {
        m_reportMode = mode;
        if (mode == ConditionBase.ReportMode.Item)
            enableItemOptions(true);
        else
            enableItemOptions(false);
    }

    public void setListener(OnGeneralFragmentInteraction l)
    {
        mListener = l;
    }
    public STFragmentGeneral() {

    }



    public static STFragmentGeneral newInstance(String param1, String param2) {
        STFragmentGeneral fragment = new STFragmentGeneral();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.st_fragment_general, container, false);

        m_btnDateFrom = (Button) v.findViewById(R.id.btnDailyDateFrom);
        m_btnDateFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDailyDateStartClicked(v);
            }
        });
        m_btnDateFrom.setText(KDSUtil.convertDateToShortString(m_dtDailyFrom));

        m_btnDateTo = (Button) v.findViewById(R.id.btnDailyDateTo);
        m_btnDateTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDailyDateToClicked(v);
            }
        });
        m_btnDateTo.setText(KDSUtil.convertDateToShortString(m_dtDailyTo));

        Calendar c = Calendar.getInstance();
        c.set(1999, 1, 1, 0, 0, 0);
        m_dtTimeFrom = c.getTime();
        m_btnTimeFrom = (Button) v.findViewById(R.id.btnTimeFrom);
        m_btnTimeFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeStartClicked(v);
            }
        });
        m_btnTimeFrom.setText(KDSUtil.convertTimeToShortString(m_dtTimeFrom));

        c.set(1999, 1, 1, 23, 59, 59);
        m_dtTimeTo = c.getTime();
        m_btnTimeTo = (Button) v.findViewById(R.id.btnTimeTo);
        m_btnTimeTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimeToClicked(v);
            }
        });
        m_btnTimeTo.setText(KDSUtil.convertTimeToShortString(m_dtTimeTo));

        m_spStationFrom = (Spinner)v.findViewById(R.id.spFromStation);
        m_spStationTo = (Spinner)v.findViewById(R.id.spToStation);
        m_spOrderReportContent= (Spinner)v.findViewById(R.id.spOrderReportConent);
        m_spReportType =  (Spinner)v.findViewById(R.id.spReportType);
        m_spReportArrangment  = (Spinner)v.findViewById(R.id.spArrangeReport);
        m_spTimeSlot = (Spinner)v.findViewById(R.id.spTimeSlot);
        m_spDayOfWeek = (Spinner)v.findViewById(R.id.spDayOfWeek);

        m_chkDayOfWeek = (CheckBox) v.findViewById(R.id.chkDayOfWeek);


        m_spItemDataMode= (Spinner)v.findViewById(R.id.spItemDataMode);

        m_arOneTimeViews.add(v.findViewById(R.id.txtDateFrom));
        m_arOneTimeViews.add(m_btnDateFrom);
        m_arOneTimeViews.add(v.findViewById(R.id.txtDateTo));
        m_arOneTimeViews.add(m_btnDateTo);
        m_arOneTimeViews.add(m_chkDayOfWeek);
        m_arOneTimeViews.add(m_spDayOfWeek);
        m_arOneTimeViews.add(v.findViewById(R.id.txtArrangeReport));
        m_arOneTimeViews.add(m_spReportArrangment);
        m_arItemViews.add(v.findViewById(R.id.trItemSelect));

        m_txtError = (TextView) v.findViewById(R.id.txtError);


        init_stations(m_spStationFrom);
        init_stations(m_spStationTo);
        m_spStationTo.setSelection(5); //station 5

        init_order_report_content(m_spOrderReportContent);
        init_report_type(m_spReportType);
        init_time_slot(m_spTimeSlot);
        m_spTimeSlot.setSelection(2);

        init_day_of_week(m_spDayOfWeek);
        init_report_arrangement(m_spReportArrangment);
        init_item_data_mode(m_spItemDataMode);

        m_spReportType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onReportTypeChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btnRefresh = (Button) v.findViewById(R.id.btnDisplayReport);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonRefreshClicked(v);
            }
        });

        m_txtItemDescription = (EditText)  v.findViewById(R.id.txtItemDescription);
        m_txtItemDescription.clearFocus();
        View view =  v.findViewById(R.id.loCondition);
        view.requestFocus();

        return v;
    }

    public void onButtonRefreshClicked(View v)
    {
        ConditionStatistic condition = getCondition();
        if (!checkConditionValidation(condition))
            return;
        if (mListener != null) {
            mListener.onGeneralFragmentInteraction(STFragmentOrderReport.ButtonCommand.Refresh);
        }
    }
    private void enableOneTimeOptions(boolean bEnable)
    {
        for (int i=0; i< m_arOneTimeViews.size(); i++)
        {
            m_arOneTimeViews.get(i).setEnabled(bEnable);
        }
    }

    private void enableItemOptions(boolean bEnable)
    {
        int n = View.VISIBLE;
        if (!bEnable )
            n = View.GONE;
        for (int i=0; i< m_arItemViews.size(); i++)
        {
            m_arItemViews.get(i).setVisibility(n);
        }
    }
    public void onReportTypeChanged()
    {
        ConditionStatistic.ReportType rt = ConditionStatistic.ReportType.values()[ m_spReportType.getSelectedItemPosition()];
        if (rt == ConditionStatistic.ReportType.OneTime)
        {
            enableOneTimeOptions(true);
        }
        else
        {
            enableOneTimeOptions(false);
        }
    }

    private void initSpinner(Spinner spinner, List<String> list)
    {

        MySpinnerArrayAdapter adapter;
        adapter = new MySpinnerArrayAdapter(this.getActivity().getApplicationContext(), list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void init_stations(Spinner sp)
    {
        List<String> list = new ArrayList<String>();
        for (int i=0; i<MAX_STATIONS_COUNT; i++)
        {
            list.add(KDSUtil.convertIntToString(i));
        }
        initSpinner(sp, list);

    }

    private void init_order_report_content(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.order_report_content_counter));
        list.add(this.getActivity().getApplicationContext().getString(R.string.order_report_content_prep_time));
        initSpinner(sp, list);

    }

    private void init_report_type(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.daily_report));
        list.add(this.getActivity().getApplicationContext().getString(R.string.weekly_report));
        list.add(this.getActivity().getApplicationContext().getString(R.string.monthly_report));
        list.add(this.getActivity().getApplicationContext().getString(R.string.one_time_report));
        initSpinner(sp, list);

    }

    private void init_report_arrangement(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.report_arrangement_full_date_range));
        list.add(this.getActivity().getApplicationContext().getString(R.string.report_arrangement_per_month));
        list.add(this.getActivity().getApplicationContext().getString(R.string.report_arrangement_per_week));

        initSpinner(sp, list);

    }

    private void init_item_data_mode(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.item_data_mode_average_time));
        list.add(this.getActivity().getApplicationContext().getString(R.string.item_data_mode_counter));


        initSpinner(sp, list);

    }

    private void init_time_slot(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.mins_15));
        list.add(this.getActivity().getApplicationContext().getString(R.string.mins_30));
        list.add(this.getActivity().getApplicationContext().getString(R.string.hour_1));
        list.add(this.getActivity().getApplicationContext().getString(R.string.hours_8));
        list.add(this.getActivity().getApplicationContext().getString(R.string.hours_12));

        initSpinner(sp, list);

    }

    private void init_day_of_week(Spinner sp)
    {
        List<String> list = new ArrayList<String>();

        list.add(this.getActivity().getApplicationContext().getString(R.string.sunday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.monday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.tuesday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.wednesday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.thursday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.friday));
        list.add(this.getActivity().getApplicationContext().getString(R.string.saturday));


        initSpinner(sp, list);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralFragmentInteraction) {
            mListener = (OnGeneralFragmentInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onDailyDateStartClicked(View v)
    {
        STDialogDatePicker dlg = new STDialogDatePicker(this.getActivity(),m_dtDailyFrom, this );
        dlg.setTag(v);
        dlg.show();

    }
    public void onDailyDateToClicked(View v)
    {
        STDialogDatePicker dlg = new STDialogDatePicker(this.getActivity(),m_dtDailyTo, this );
        dlg.setTag(v);
        dlg.show();

    }


    public void onTimeStartClicked(View v)
    {
        STDialogTimePicker dlg = new STDialogTimePicker(this.getActivity(),m_dtTimeFrom, this );
        dlg.setTag(v);
        dlg.show();

    }
    public void onTimeToClicked(View v)
    {
        STDialogTimePicker dlg = new STDialogTimePicker(this.getActivity(),m_dtTimeTo, this );
        dlg.setTag(v);
        dlg.show();

    }
    public void showErrorMessage(String strMessage)
    {
        m_txtError.setText(strMessage);
    }
    public boolean checkConditionValidation(ConditionStatistic condition)
    {
        int nStationFrom = KDSUtil.convertStringToInt(condition.getStationFrom(), 0);
        int nStationTo = KDSUtil.convertStringToInt(condition.getStationTo(), 0);
        if (nStationFrom > nStationTo) {
            showErrorMessage(getString(R.string.to_station_to_less_than_from_station));//"[To station] is less than [From station].");
            return false;
        }
        switch (condition.getReportType())
        {

            case Daily:
            {
                Date tmFrom = condition.getDailyReportCondition().getTimeFrom();
                Date tmTo = condition.getDailyReportCondition().getTimeTo();
                if (tmTo.getTime() - tmFrom.getTime()<=0 ) {
                    showErrorMessage(getString(R.string.time_to_less_than_time_from));//"[Time to] is less than [Time from]");
                    return false;
                }
            }
                break;

            case Weekly:
            {
                Date tmFrom = condition.getWeeklyCondition().getTimeFrom();
                Date tmTo = condition.getWeeklyCondition().getTimeTo();
                if (tmTo.getTime() - tmFrom.getTime()<=0 ) {
                    showErrorMessage(getString(R.string.time_to_less_than_time_from));//"[Time to] is less than [Time from]");
                    return false;
                }
            }
                break;

            case Monthly:
            {
                Date tmFrom = condition.getMonthlyCondition().getTimeFrom();
                Date tmTo = condition.getMonthlyCondition().getTimeTo();
                if (tmTo.getTime() - tmFrom.getTime()<=0 ) {
                    showErrorMessage(getString(R.string.time_to_less_than_time_from));//"[Time to] is less than [Time from]");
                    return false;
                }
            }
                break;
            case OneTime:
            {
                Date tmFrom = condition.getOneTimeCondition().getTimeFrom();
                Date tmTo = condition.getOneTimeCondition().getTimeTo();
                if (tmTo.getTime() - tmFrom.getTime()<=0 ) {
                    showErrorMessage(getString(R.string.time_to_less_than_time_from));//"[Time to] is less than [Time from]");
                    return false;
                }

                Date dtFrom = condition.getOneTimeCondition().getDateFrom();
                Date dtTo = condition.getOneTimeCondition().getDateTo();

                if (dtTo.getTime() - dtFrom.getTime() <=0)
                {
                    showErrorMessage(getString(R.string.date_to_less_than_date_from));//"[Date to] is less than [Date from]");
                    return false;
                }

            }
                break;
        }
        showErrorMessage("");
        return true;

    }

    public ConditionStatistic getCondition()
    {

        ConditionStatistic condition = new ConditionStatistic();

        condition.setReportMode(m_reportMode);

        ConditionStatistic.ReportType rtype = ConditionStatistic.ReportType.values()[m_spReportType.getSelectedItemPosition()];
        condition.setReportType(rtype);

        String s = m_txtItemDescription.getText().toString();
        condition.setItemDescription(s);


        int nfrom =  m_spStationFrom.getSelectedItemPosition();
        condition.setStationFrom(KDSUtil.convertIntToString(nfrom));

        int nto =  m_spStationTo.getSelectedItemPosition();
        condition.setStationTo(KDSUtil.convertIntToString(nto));

        ConditionStatistic.OrderReportContent m = ConditionStatistic.OrderReportContent.values()[ m_spOrderReportContent.getSelectedItemPosition()];
        condition.setOrderReportContent(m);

        switch (rtype)
        {

            case Daily: {
                ConditionOneTime.TimeSlot ts = ConditionOneTime.TimeSlot.values()[ m_spTimeSlot.getSelectedItemPosition()];
                condition.getDailyReportCondition().setTimeSlot(ts);
                condition.getDailyReportCondition().setDate(new Date());
                condition.getDailyReportCondition().setTimeFromString(m_btnTimeFrom.getText().toString());
                condition.getDailyReportCondition().setTimeToString(m_btnTimeTo.getText().toString());
            }
            break;
            case Weekly:
                condition.getWeeklyCondition().setTimeFromString(m_btnTimeFrom.getText().toString());
                condition.getWeeklyCondition().setTimeToString(m_btnTimeTo.getText().toString());
                break;
            case Monthly:
                condition.getMonthlyCondition().setTimeFromString(m_btnTimeFrom.getText().toString());
                condition.getMonthlyCondition().setTimeToString(m_btnTimeTo.getText().toString());
                break;
            case OneTime: {
                String dateFrom = m_btnDateFrom.getText().toString();
                String dateTo = m_btnDateTo.getText().toString();
                condition.getOneTimeCondition().setDateFromString(dateFrom);
                condition.getOneTimeCondition().setDateToString(dateTo);
                condition.getOneTimeCondition().setTimeFromString(m_btnTimeFrom.getText().toString());
                condition.getOneTimeCondition().setTimeToString(m_btnTimeTo.getText().toString());

                condition.getOneTimeCondition().setEnableDayOfWeek(m_chkDayOfWeek.isChecked());
                condition.getOneTimeCondition().setDayOfWeek( ConditionOneTime.WeekDay.values()[ m_spDayOfWeek.getSelectedItemPosition()]);

                condition.getOneTimeCondition().setReportArrangement( ConditionOneTime.ReportArrangement.values()[ m_spReportArrangment.getSelectedItemPosition()]);

                //condition.getOneTimeCondition().setEnableTimeSlot(m_chkTimeSlot.isChecked());
                condition.getOneTimeCondition().setTimeSlot( ConditionOneTime.TimeSlot.values()[ m_spTimeSlot.getSelectedItemPosition()]);

            }
            break;
        }
        return condition;

    }

    public interface OnGeneralFragmentInteraction {

        void onGeneralFragmentInteraction(STFragmentOrderReport.ButtonCommand command);
    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof STDialogDatePicker)
        {
            STDialogDatePicker dlg = (STDialogDatePicker)dialog;
            if (dlg.getTag() == m_btnDateFrom)
            {
                Date dt =(Date) dlg.getResult();
                if (dt != null)
                    m_btnDateFrom.setText(KDSUtil.convertDateToShortString(dt));

            }
            else if (dlg.getTag() == m_btnDateTo)
            {
                Date dt =(Date) dlg.getResult();
                if (dt != null)
                    m_btnDateTo.setText(KDSUtil.convertDateToShortString(dt));
            }
        }
        else if (dialog instanceof STDialogTimePicker)
        {
            STDialogTimePicker dlg = (STDialogTimePicker)dialog;
            if (dlg.getTag() == m_btnTimeFrom)
            {
                Date dt =(Date) dlg.getResult();
                if (dt != null)
                    m_btnTimeFrom.setText(KDSUtil.convertTimeToShortString(dt));
            }
            else if (dlg.getTag() == m_btnTimeTo)
            {
                Date dt =(Date) dlg.getResult();
                if (dt != null)
                    m_btnTimeTo.setText(KDSUtil.convertTimeToShortString(dt));
            }
        }
    }


    public void restoreCondition(ConditionStatistic condition)
    {
        m_spStationFrom.setSelection(KDSUtil.convertStringToInt( condition.getStationFrom(), 0));
        m_spStationTo.setSelection(KDSUtil.convertStringToInt( condition.getStationTo(), 0));
        m_spReportType.setSelection(condition.getReportType().ordinal());
        switch (condition.getReportType())
        {

            case Daily:
                m_spTimeSlot.setSelection(condition.getDailyReportCondition().getTimeSlot().ordinal());
                m_btnTimeFrom.setText( KDSUtil.convertTimeToShortString( condition.getDailyReportCondition().getTimeFrom()));
                m_btnTimeTo.setText( KDSUtil.convertTimeToShortString( condition.getDailyReportCondition().getTimeTo()));

                break;
            case Weekly:

                m_btnTimeFrom.setText( KDSUtil.convertTimeToShortString( condition.getWeeklyCondition().getTimeFrom()));
                m_btnTimeTo.setText( KDSUtil.convertTimeToShortString( condition.getWeeklyCondition().getTimeTo()));
                break;
            case Monthly:


                m_btnTimeFrom.setText( KDSUtil.convertTimeToShortString( condition.getMonthlyCondition().getTimeFrom()));
                m_btnTimeTo.setText( KDSUtil.convertTimeToShortString( condition.getMonthlyCondition().getTimeTo()));
                break;
            case OneTime:
                m_spTimeSlot.setSelection(condition.getOneTimeCondition().getTimeSlot().ordinal());
                m_btnDateFrom.setText( KDSUtil.convertDateToShortString( condition.getOneTimeCondition().getDateFrom()));
                m_btnDateTo.setText( KDSUtil.convertDateToShortString( condition.getOneTimeCondition().getDateTo()));
                m_btnTimeFrom.setText( KDSUtil.convertTimeToShortString( condition.getOneTimeCondition().getTimeFrom()));
                m_btnTimeTo.setText( KDSUtil.convertTimeToShortString( condition.getOneTimeCondition().getTimeTo()));
                break;
        }
    }


}
