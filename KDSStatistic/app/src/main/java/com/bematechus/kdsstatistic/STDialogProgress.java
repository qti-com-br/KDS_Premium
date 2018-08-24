package com.bematechus.kdsstatistic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class STDialogProgress extends KDSUIDialogBase {



    TextView m_txtInfo = null;
    ProgressBar m_progressBar = null;
    ListView m_lstStations = null;

    ArrayList<STStationStatisticInfo> m_arData = new ArrayList<>();

    public STDialogProgress(final Context context, KDSDialogBaseListener listener) {

        this.int_one_button_dialog(context, R.layout.st_dialog_progress, true);
        this.listener = listener;
        this.setTitle(context.getString(R.string.waiting_collecting));
        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtMessage);
        //m_txtInfo.setText(strVersion);

        m_progressBar = (ProgressBar)this.getView().findViewById(R.id.progressBar);

        m_lstStations = (ListView)this.getView().findViewById(R.id.lstStations);
        m_lstStations.setAdapter(new AdapterStations(this.getDialog().getContext(),m_arData  ));
    }

    public void showActiveStations( ArrayList<String> arStationsID)
    {
        ArrayList<KDSStationIP> ar = STGlobalVariables.getKDS().getAllActiveConnections().getAllActiveStations();


        int ncounter = 0;
        for (int i=0; i< ar.size(); i++)
        {
            KDSStationIP station = ar.get(i);
            if (!KDSUtil.isExistedInArray(arStationsID, station.getID()))
                continue;
            STStationStatisticInfo stationInfo = new STStationStatisticInfo();
            stationInfo.copyFrom(station);

            m_arData.add(stationInfo);


        }


    }
    public void setMessage(String str)
    {
        m_txtInfo.setText(str);
    }

    public void setTitle(String str)
    {
        this.dialog.setTitle(str);
    }
    public void hide()
    {
        dialog.cancel();
    }
    public void setStationDone(String stationID)
    {
        for (int i=0; i< m_arData.size(); i++)
        {
            if (m_arData.get(i).getID().equals(stationID))
                m_arData.get(i).setStatus(STStationStatisticInfo.StationStatisticStatus.Updated);
        }
        ((AdapterStations)m_lstStations.getAdapter()).notifyDataSetChanged();
    }

    public void show()
    {
        super.show();
        this.dialog.setCanceledOnTouchOutside(false);
        this.dialog.setCancelable(false);
    }

    class AdapterStations  extends BaseAdapter {
        private LayoutInflater mInflater;

        public List<STStationStatisticInfo> m_listData;


        public AdapterStations(Context context, List<STStationStatisticInfo> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<STStationStatisticInfo> getListData()
        {
            return m_listData;
        }
        public void setListData(List<STStationStatisticInfo> lst)
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
        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            STStationStatisticInfo r =  m_listData.get(position);
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.st_listitem_report_station, null);

            }
            else
            {


            }
            convertView.setTag(r);

            ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());

            ((TextView) convertView.findViewById(R.id.txtIP)).setText(r.getIP());
            if (r.getStatus() == STStationStatisticInfo.StationStatisticStatus.Updated) {
                ((ImageView) convertView.findViewById(R.id.imgOK)).setVisibility(View.VISIBLE);
                convertView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            }
            else
            {
                ((ImageView) convertView.findViewById(R.id.imgOK)).setVisibility(View.GONE);
                convertView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }

}
