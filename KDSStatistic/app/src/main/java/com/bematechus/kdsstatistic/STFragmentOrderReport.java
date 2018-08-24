package com.bematechus.kdsstatistic;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.ConditionBase;
import com.bematechus.kdslib.ConditionStatistic;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.MySpinnerArrayAdapter;
import com.bematechus.kdslib.TimeSlotOrderReport;

public class STFragmentOrderReport extends Fragment implements KDSUIDialogBase.KDSDialogBaseListener, STReportCreatorEvents{

    public enum ButtonCommand
    {
        Refresh,
        Next,
        Prev,
        Save_Profile,
        Load_Profile
    }

    private Spinner m_spReportContent = null;

    private STReportGrid m_grid = new STReportGrid();
    private OnOrderReportFragmentInteractionListener mListener;
    private ConditionBase.ReportMode m_reportMode = ConditionBase.ReportMode.Order;

    STReportCreator m_reportCreator = new STReportCreator();


    public void setReportMode(ConditionBase.ReportMode mode)
    {
        m_reportMode = mode;
        init_order_report_content(m_spReportContent);
    }
    public ConditionBase.ReportMode getReportMode()
    {
        return m_reportMode;
    }

    public void setListener(OnOrderReportFragmentInteractionListener l)
    {
        mListener = l;
    }

    public STFragmentOrderReport() {

    }


    public static STFragmentOrderReport newInstance(String param1, String param2) {
        STFragmentOrderReport fragment = new STFragmentOrderReport();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        m_reportCreator.setEventsReceiver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.st_fragment_order_report, container, false);
        m_grid.init(this.getActivity().getApplicationContext(), v);
        Button btnRefresh = (Button) v.findViewById(R.id.btnCSV);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonCSVClicked(v);
            }
        });

        TextView btnPrev = (TextView) v.findViewById(R.id.btnPrev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonPrevClicked(v);
            }
        });

        TextView btnNext = (TextView) v.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonNextClicked(v);
            }
        });
        m_grid.setCols(1);

        m_spReportContent = (Spinner)v.findViewById(R.id.spReportContent);
        init_order_report_content(m_spReportContent);

        m_spReportContent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onReportContentOptionChanged(ConditionStatistic.OrderReportContent.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button btn =(Button) v.findViewById(R.id.btnSave);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSaveClicked(v);
            }
        });

        btn =(Button) v.findViewById(R.id.btnSaveProfile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSaveProfileClicked(v);
            }
        });

        btn =(Button) v.findViewById(R.id.btnLoadProfile);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnLoadProfileClicked(v);
            }
        });

        TextView t = (TextView)v.findViewById(R.id.txtInfo0);
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDateFromClicked(v);
            }
        });
        return v;
    }

    ConditionStatistic getShowingCondition()
    {

        return m_reportCreator.getCondition();
    }
    public void onDateFromClicked(View v)
    {
        Date dt = new Date();
        ConditionStatistic c = getShowingCondition();
        switch (c.getReportType())
        {

            case Daily:
                dt = c.getDailyReportCondition().getDate();
                break;
            case Weekly:
                dt = c.getWeeklyCondition().getWeekFirstDayDate();
                break;
            case Monthly:
                dt = c.getMonthlyCondition().getMonthFirstDay();
                break;
            case OneTime:
               return;

        }

        STDialogDatePicker dlg = new STDialogDatePicker(this.getActivity(),dt, this );
        dlg.setTag(v);
        dlg.show();
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {
        if (dialog instanceof STDialogProgress)
        {
            cancel();
        }

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof STDialogInputProfileName)
        {
            String fileName =(String) ((STDialogInputProfileName)dialog).getResult();
            STReportCreator.saveConditionProfile(fileName, m_reportCreator.getCondition());

        }
        else if (dialog instanceof  STDialogProfileList)
        {
            String fileName =(String) ((STDialogProfileList)dialog).getResult();
            if (fileName.isEmpty()) return;
            restoreConditionFile(fileName);
        }
        else if (dialog instanceof STDialogDatePicker )
        {
            Date dt =(Date) ((STDialogDatePicker)dialog).getResult();
            onSelectNewFromDate(dt);
        }
        else if (dialog instanceof STDialogProgress)
        {
            cancel();
        }
    }

    private void onSelectNewFromDate(Date dt)
    {
        ConditionStatistic condition = m_reportCreator.getCondition();

        switch (condition.getReportType())
        {

            case Daily:
                condition.getDailyReportCondition().setDate(dt);
                break;
            case Weekly:
                Calendar weekFirstDay = Calendar.getInstance();
                weekFirstDay.setTime(dt);
                int nFirstDayOfWeek = weekFirstDay.getFirstDayOfWeek();
                //int nDayOfWeek = currentWeekFirstDay.get(Calendar.DAY_OF_WEEK);
                weekFirstDay.set(Calendar.DAY_OF_WEEK,nFirstDayOfWeek);

                condition.getWeeklyCondition().setWeekFirstDay(weekFirstDay.getTime());
                break;
            case Monthly:
                Calendar monthFirstDay = Calendar.getInstance();
                monthFirstDay.setTime(dt);

                monthFirstDay.set(Calendar.DAY_OF_MONTH,1);

                condition.getMonthlyCondition().setMonthFirstDay(monthFirstDay.getTime());
                break;
            case OneTime:

                break;
        }

        refreshReport(condition);
        m_grid.showOrderReport(null);

    }

    /**
     *
     * @param fileName
     */
    public void restoreConditionFile(String fileName)
    {
        ConditionStatistic condition =  STReportCreator.loadConditionProfile(fileName);

        if (mListener != null) {
            mListener.onOrderReportFragmentInteraction(ButtonCommand.Load_Profile, condition);
        }

        refreshReport(condition);
    }


    public void onBtnSaveProfileClicked(View v)
    {
        if (m_reportCreator.getReport() == null) {
            KDSToast t = new KDSToast();
            t.showMessage(this.getActivity(), this.getActivity().getString(R.string.display_report_first));
            return;
        }
        STDialogInputProfileName dlg = new STDialogInputProfileName(this.getActivity(), m_grid.getReport().getCondition(), this);
        dlg.show();
    }


    public void onBtnLoadProfileClicked(View v)
    {
        STDialogProfileList dlg = new STDialogProfileList(this.getActivity(),ConditionStatistic.getProfileFolder(), this);
        dlg.show();
    }

    private void onBtnSaveClicked(View v)
    {

        if (m_reportCreator.getReport() == null) {
            KDSToast t = new KDSToast();
            t.showMessage(this.getActivity(), this.getActivity().getString(R.string.display_report_first));
            return;
        }
        TimeSlotOrderReport.exportToFile(m_grid.getReport());
        KDSToast.showMessage(KDSApplication.getContext(),KDSApplication.getContext().getString(R.string.done));
    }
    private void onReportContentOptionChanged(ConditionStatistic.OrderReportContent content)
    {
        if (this.m_grid.getReport() == null) return;
        m_grid.setReportContent(content);
    }

    private void init_order_report_content(Spinner sp)
    {
        List<String> list = new ArrayList<String>();
        if (m_reportMode == ConditionBase.ReportMode.Order) {
            list.add(this.getActivity().getApplicationContext().getString(R.string.order_report_content_counter));
            list.add(this.getActivity().getApplicationContext().getString(R.string.order_report_content_prep_time));
        }
        else
        {
            list.add(this.getActivity().getApplicationContext().getString(R.string.item_report_content_counter));
            list.add(this.getActivity().getApplicationContext().getString(R.string.item_report_content_prep_time));
        }
        initSpinner(sp, list);

    }

    private void initSpinner(Spinner spinner, List<String> list)
    {

        MySpinnerArrayAdapter adapter;
        adapter = new MySpinnerArrayAdapter(this.getActivity().getApplicationContext(), list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void onButtonPrevClicked(View v)
    {
        if (m_reportCreator.getReport() == null) return;
        m_reportCreator.getReport().prev();

        refreshReport(m_reportCreator.getReport().getCondition());

    }

    public void onButtonNextClicked(View v)
    {
        if (m_reportCreator.getReport() == null) return;
        m_reportCreator.getReport().next();

        refreshReport(m_reportCreator.getReport().getCondition());


    }

    public void onButtonCSVClicked(View v)
    {
        if (m_reportCreator.getReport() == null) {
            KDSToast t = new KDSToast();
            t.showMessage(this.getActivity(), this.getActivity().getString(R.string.display_report_first));
            return;
        }
        TimeSlotOrderReport.exportToCSV(m_grid.getReport());
        KDSToast.showMessage(KDSApplication.getContext(),KDSApplication.getContext().getString(R.string.done));
    }


    public void refreshReport(ConditionStatistic condition)
    {
        if (m_nProgressMax !=0) return;

        ArrayList<String> ar= m_reportCreator.refreshReport(condition);
        int ncount = ar.size();
        m_grid.clear();
        if (ncount >0)
            showProgressDialog(KDSApplication.getContext().getString(R.string.waiting_collecting), KDSApplication.getContext().getString(R.string.waiting_for_statistic) , ncount, ar);
        else {
            KDSToast t = new KDSToast();
            t.showMessage(this.getActivity(),this.getActivity().getString(R.string.no_active_stations));
        }

    }

    int m_nProgress = 0;
    int m_nProgressMax = 0;
    STDialogProgress m_progressDlg = null;
    public void showProgressDialog(String title, String message, int maxValue, ArrayList<String> stationsID)
    {

        m_progressDlg = new STDialogProgress(this.getActivity(), this);
        m_nProgress = 0;
        m_nProgressMax = maxValue;

        m_progressDlg.setTitle(title );
        m_progressDlg.setMessage(message + " 0/" + KDSUtil.convertIntToString(maxValue));
        m_progressDlg.showActiveStations(stationsID);
        m_progressDlg.show();
    }
    public void cancel()
    {
        m_nProgress = 0;
        m_nProgressMax = 0;

        if (m_progressDlg!= null)
            m_progressDlg.hide();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOrderReportFragmentInteractionListener) {
            mListener = (OnOrderReportFragmentInteractionListener) context;
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


    public interface OnOrderReportFragmentInteractionListener {

        void onOrderReportFragmentInteraction(ButtonCommand command, Object objParam);
    }


    public void onReportCreatorStartCreateReport(STReportCreator creator, ConditionStatistic condition)
    {

    }
    public void onReportCreatorReceiveStationReport(STReportCreator creator, String stationID,ConditionStatistic condition, TimeSlotOrderReport report )
    {
        if (!m_reportCreator.isCreating()) return;
        if (m_progressDlg != null)
        {
            m_nProgress ++;
            m_progressDlg.setStationDone(stationID);
            m_progressDlg.setMessage(KDSApplication.getContext().getString(R.string.waiting_for_statistic)  + " " + KDSUtil.convertIntToString(m_nProgress) + "/" + KDSUtil.convertIntToString( m_nProgressMax));

            if (m_nProgress == m_nProgressMax) {
                m_nProgress =0;
                m_nProgressMax = 0;
                cancel();
            }
        }
        m_grid.showOrderReport(m_reportCreator.getReport());
    }
    public void onReportCreatorReportCreated(STReportCreator creator, ConditionStatistic condition)
    {
        cancel();
        //m_reportCreator.reset();
    }
}
