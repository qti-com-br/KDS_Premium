//package com.bematechus.kdsrouter;
///**
// * ******************************** IMPORTANT **********************
// *  This file is different with the file in android KDS project.
// */
//import android.content.Context;
//import android.content.DialogInterface;
//import android.os.AsyncTask;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
//import android.widget.Button;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.bematechus.kdslib.KDSLog;
//import com.bematechus.kdslib.KDSStationIP;
//import com.bematechus.kdslib.KDSUIDialogBase;
//import com.bematechus.kdslib.KDSUtil;
//import com.bematechus.kdslib.StationAnnounceEvents;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Created by Administrator on 2015/9/22 0022.
// */
//public class KDSUIIPSearchDialog extends KDSUIDialogBase implements StationAnnounceEvents {
//    final static String TAG = "KDSUIIPSearchDialog";
//    public enum IPSelectionMode{
//        Zero,
//        Single,
//        Multiple,
//
//    }
//
//    ListView m_lstStations = null;
//    //ArrayList<String> m_lstIPs = new ArrayList<String>();
//    TextView m_txtTitle = null;
//
//    int m_nJustListStationWithThisIpPort = 0;
//    boolean m_bShowMySelf = true;
//
//    Object m_tag = null;
//
//    public void setTag(Object obj)
//    {
//        m_tag = obj;
//    }
//    public Object getTag()
//    {
//        return m_tag;
//    }
//
//    public void setSelf(boolean bShowSelf)
//    {
//        m_bShowMySelf = bShowSelf;
//    }
//    //2015-12-31
//    public void setFilterIpPort(int nPort)
//    {
//        m_nJustListStationWithThisIpPort = nPort;
//    }
//
//
//    /**
//     * it will been overrided by child
//     * @return
//     */
//    @Override
//    public Object getResult()
//    {
//        return  getSelectedStations();
//
//    }
//
//    public KDSUIIPSearchDialog(final Context context, IPSelectionMode selectionMode, KDSDialogBaseListener listener , String strTitle) {
//
//        this.int_dialog(context, listener, R.layout.kdsui_dlg_ip_search,context.getString( R.string.str_refresh));
//        this.setTitle(strTitle);
//
//        m_lstStations = (ListView)this.getView().findViewById(R.id.lstStations);
//
//        m_txtTitle  = (TextView)this.getView().findViewById(R.id.txtTitle);
//
//        m_txtTitle.setText( context.getString(R.string.searching_active_stations));//.no_active_stations));
//
//        m_lstStations.setAdapter(new MyAdapter(context, new ArrayList<KDSStationIP>()));
//        m_lstStations.focusableViewAvailable(m_lstStations);
//        switch (selectionMode)
//        {
//            case Zero:
//                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_NONE);
//                break;
//            case Single:
//                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
//                break;
//            case Multiple:
//                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//                break;
//        }
//
//        AsyncTask tast = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                try {
//                    Thread.sleep(5000);
//                }
//                catch (Exception e)
//                {
//                    KDSLog.e(TAG,KDSLog._FUNCLINE_(),e);// + e.toString());
//                    //KDSLog.e(TAG, KDSUtil.error( e));
//                }
//                return null;
//            }
//            @Override
//            protected void onPostExecute(Object o) {
//                super.onPostExecute(o);
//                if (m_lstStations.getCount()==0)
//                    m_txtTitle.setText(R.string.no_active_stations);
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//
//    }
//
//    public void refresh()
//    {
//        ((MyAdapter) m_lstStations.getAdapter()).getListData().clear();
//
//        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(KDSUIIPSearchDialog.this);
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                KDSGlobalVariables.getKDS().broadcastRequireStationsUDP();
//                return null;
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//
//    }
//
//    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
//    {
//        if (!m_bShowMySelf)
//        {
//            String localID =  KDSGlobalVariables.getKDS().getStationID();
//            if (localID.equals(stationReceived.getID()))
//                return;
//        }
//
//        if (findStation(stationReceived.getIP(),stationReceived.getPort()))
//            return;
//        //Just for router
//        if (m_nJustListStationWithThisIpPort >0) {
//            int nport = KDSUtil.convertStringToInt(stationReceived.getPort(), 0);
//            if (nport != m_nJustListStationWithThisIpPort)
//                return;
//        }
//        // m_lstIPs.add(stationID + ":" + ip + ":" + port);
//        KDSStationIP station = new KDSStationIP();
//        station.setID(stationReceived.getID());
//        station.setIP(stationReceived.getIP());
//        station.setPort(stationReceived.getPort());
//        ((MyAdapter) m_lstStations.getAdapter()).getListData().add(station);
//        KDSStationIP.sortStations(((MyAdapter) m_lstStations.getAdapter()).getListData());
//        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//
//        String str = this.getDialog().getContext().getString(R.string.active_stations_list);
//        m_txtTitle.setText(str);
//    }
//
//    /**
//     *
//     * @return
//     */
//    private ArrayList<String> getSelectedStations()
//    {
//        ArrayList<String> ar = new ArrayList<>();
//
//        int ncount = m_lstStations.getCount();
//        for (int i=0; i< ncount; i++) {
//            if ( m_lstStations.isItemChecked(i))
//            {
//                String s = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).toString();
//                ar.add(s);
//
//            }
//        }
//        return ar;
//    }
//
//    /**
//     * different with KDS
//     * @param ip
//     * @param port
//     * @return
//     */
//    private boolean findStation(String ip, String port)
//    {
//        int ncount = m_lstStations.getCount();
//        for (int i=0; i< ncount; i++) {
//
//            String strIP = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getIP();
//            String strPort = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getPort();
//            if (strIP.equals(ip) &&
//                    strPort.equals(port))
//                return true;
//
//
//        }
//        return false;
//    }
//
//    public void show() {
//        super.show();
//        Button btn =dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
//        btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                KDSUIIPSearchDialog.this.refresh();
//
//            }
//        });
//        refresh();
//    }
//    private class MyAdapter extends BaseAdapter {
//        private LayoutInflater mInflater;
//
//        public List<KDSStationIP> m_listData; //KDSStationsRelation class array
//
//
//        public MyAdapter(Context context, List<KDSStationIP> data) {
//            this.mInflater = LayoutInflater.from(context);
//            m_listData = data;
//        }
//        public List<KDSStationIP> getListData()
//        {
//            return m_listData;
//        }
//        public void setListData(List<KDSStationIP> lst)
//        {
//            m_listData = lst;
//        }
//        public int getCount() {
//
//            return m_listData.size();
//        }
//        public Object getItem(int arg0) {
//
//            return m_listData.get(arg0);
//        }
//        public long getItemId(int arg0) {
//
//            return arg0;
//        }
//        public View getView(int position, View convertView, ViewGroup parent) {
//
//            KDSStationIP r =  m_listData.get(position);
//            if (convertView == null) {
//
//                convertView = mInflater.inflate(R.layout.kdsui_listitem_ip, null);
//            }
//            else
//            {
//
//            }
//            convertView.setTag(r);
//
//            ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());
//
//            ((TextView) convertView.findViewById(R.id.txtIP)).setText(r.getIP());
//            ((TextView) convertView.findViewById(R.id.txtPort)).setText(r.getPort());
//            return convertView;
//        }
//
//    }
//
//
//
//}
