package com.bematechus.kdslib;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/22 0022.
 */
public class KDSUIIPSearchDialog extends KDSUIDialogBase implements StationAnnounceEvents {
    final static String TAG = "KDSUIIPSearchDialog";
    public enum IPSelectionMode{
        Zero,
        Single,
        Multiple,

    }

    ListView m_lstStations = null;

    TextView m_txtTitle = null;
    //  Button m_btnRefresh = null;
    int m_nJustListStationWithThisIpPort = 0;
    boolean m_bShowMySelf = true;

    boolean m_bShowMultipleUser = false; //if multiple users, show two users.
    Object m_tag = null;

    String m_strDefaultStationID = "";
    KDSCallback m_kdsCallback = null;
    /////////////////////////////////////////

    public void setKDSCallback(KDSCallback kdsCallback)
    {
        m_kdsCallback = kdsCallback;
    }

    public void setDefaultStationID(String stationID)
    {
        m_strDefaultStationID = stationID;
    }

    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }

    public void setSelf(boolean bShowSelf)
    {
        m_bShowMySelf = bShowSelf;
    }
    public void setShowMultipleUsers(boolean bShowMultiple)
    {
        m_bShowMultipleUser = bShowMultiple;
    }
    //2015-12-31
    public void setFilterIpPort(int nPort)
    {
        m_nJustListStationWithThisIpPort = nPort;
    }



    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return  getSelectedStations();

    }

    public KDSUIIPSearchDialog(final Context context, IPSelectionMode selectionMode, KDSDialogBaseListener listener, String strTitle) {

        this.int_dialog(context, listener, R.layout.kdsui_dlg_ip_search,context.getString( R.string.str_refresh));
        this.setTitle(strTitle);

        m_lstStations = (ListView)this.getView().findViewById(R.id.lstStations);

        m_txtTitle  = (TextView)this.getView().findViewById(R.id.txtTitle);

        m_txtTitle.setText( context.getString(R.string.searching_active_stations));//.no_active_stations));

        m_lstStations.setAdapter(new MyAdapter(context, new ArrayList<KDSStationIP>()));
        m_lstStations.focusableViewAvailable(m_lstStations);
        switch (selectionMode)
        {
            case Zero:
                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_NONE);
                break;
            case Single:
                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                break;
            case Multiple:
                m_lstStations.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                break;
        }

        AsyncTask tast = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                try {
                    Thread.sleep(5000);
                }
                catch (Exception e)
                {
                    KDSLog.e(TAG, KDSLog._FUNCLINE_() , e);
                    //KDSLog.e(TAG, KDSUtil.error( e));
                }
                return null;
            }
            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if (m_lstStations.getCount()==0)
                    m_txtTitle.setText(R.string.no_active_stations);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        ((HandleDataListView)m_lstStations).setDataChangedListener(new HandleDataListView.DataChangedListener() {
            @Override
            public void onSuccess() {
                if (m_lstStations.getChoiceMode() !=ListView.CHOICE_MODE_NONE ) {
                    KDSUIIPSearchDialog.this.getView().requestFocus(View.FOCUS_UP);
                    m_lstStations.requestFocus();
                    m_lstStations.requestFocusFromTouch();
                    highlightDefault();
                }
            }
        });
        m_lstStations.setFocusable(true);
//        m_lstStations.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//
//                String s = v.toString();
//                Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
//
//            }
//        });
        m_lstStations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                m_lstStations.setItemChecked(position, true);
//                String s = view.toString();
//                Toast.makeText(KDSApplication.getContext(), s, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        m_lstStations.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN ||
//                     keyCode == KeyEvent.KEYCODE_DPAD_UP)
//                {
//                    if (event.getAction() == KeyEvent.ACTION_UP)
//                        KDSUtil.sendKeyCode(KeyEvent.KEYCODE_ENTER);
//
//                }
//                return false;
//            }
//        });

        m_lstStations.setFocusableInTouchMode(true);
        m_lstStations.requestFocus();
        m_lstStations.requestFocusFromTouch();
        this.getView().requestFocus(View.FOCUS_UP);

//        ((HandleDataListView)m_lstStations).setDataChangedListener(new  (new HandleDataListView.DataChangedListener() {
//            @Override
//            public void onSuccess() {
//               highlightDefault();
//
//            };

    }

    public void refresh()
    {
        ((MyAdapter) m_lstStations.getAdapter()).getListData().clear();

        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(KDSUIIPSearchDialog.this);
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastRequireStationsUDP();
//                return null;
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (m_kdsCallback != null)
        {
            m_kdsCallback.call_setStationAnnounceEventsReceiver(this);

            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    m_kdsCallback.call_broadcastRequireStationsUDP();
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }


    }

    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
    {
        if (!m_bShowMySelf)
        {
            String localID = m_kdsCallback.call_getStationID();// KDSGlobalVariables.getKDS().getStationID();
            if (localID.equals(stationReceived.getID()))
                return;
        }

        if (findStation(stationReceived.getIP()))
            return;
        //Just for router
        if (m_nJustListStationWithThisIpPort >0) {
            int nport = KDSUtil.convertStringToInt(stationReceived.getPort(), 0);
            if (nport != m_nJustListStationWithThisIpPort)
                return;
        }

        KDSStationIP station = new KDSStationIP();
        station.setID(stationReceived.getID());
        station.setIP(stationReceived.getIP());
        station.setPort(stationReceived.getPort());
        station.setUserMode(stationReceived.getUserMode());
        station.setScreen(0);//KDSUser.USER.USER_A.ordinal());

        ((MyAdapter) m_lstStations.getAdapter()).getListData().add(station);
        if (m_bShowMultipleUser)
        {
            if (stationReceived.getUserMode() == SettingsBase.KDSUserMode.Multiple.ordinal())
            {
                KDSStationIP station1 = new KDSStationIP();
                station1.setID(stationReceived.getID());
                station1.setIP(stationReceived.getIP());
                station1.setPort(stationReceived.getPort());
                station1.setUserMode(stationReceived.getUserMode());
                station1.setScreen(1);//KDSUser.USER.USER_B.ordinal());
                ((MyAdapter) m_lstStations.getAdapter()).getListData().add(station1);
            }
        }
        KDSStationIP.sortStations ( ((MyAdapter) m_lstStations.getAdapter()).getListData() );

       // highlightDefault();

        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
       // highlightDefault();
       // ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
        String str = this.getDialog().getContext().getString(R.string.active_stations_list);
        m_txtTitle.setText(str);
    }

    /**
     *
     * @return
     */
    private ArrayList<String> getSelectedStations()
    {

        ArrayList<String> ar = new ArrayList<>();
        if (!m_lstStations.isFocused())
            return ar;
        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstStations.isItemChecked(i))
            {
                String s = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).toString();
                ar.add(s);

            }
        }
        return ar;
    }

    public KDSStationIP getSelectedStation()
    {

//        if (!m_lstStations.isFocused())
//            return null;
        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstStations.isItemChecked(i))
            {//2.1.15.4 add isFocused to it.
               return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);

            }
        }
//        View v =  m_lstStations.getSelectedView();//.getFocusedChild();//.findFocus();
//        if (v != null)
//        {
//            Toast.makeText(KDSApplication.getContext(), "focused", Toast.LENGTH_LONG).show();
//        }
        return null;
    }

    private boolean findStation(String ip)
    {


        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {

                String strIP = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getIP();
                if (strIP.equals(ip))
                    return true;


        }
        return false;

    }

    /**
     * different with KDS
     * @param ip
     * @param port
     * @return
     */
    private boolean findStation(String ip, String port)
    {
        int ncount = m_lstStations.getCount();
        for (int i=0; i< ncount; i++) {

            String strIP = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getIP();
            String strPort = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getPort();
            if (strIP.equals(ip) &&
                    strPort.equals(port))
                return true;


        }
        return false;
    }
    public void show() {
        super.show();
        Button btn =dialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KDSUIIPSearchDialog.this.refresh();

            }
        });
        refresh();
//        this.getView().requestFocus();
//        m_lstStations.requestFocus();

    }

    private void highlightDefault()
    {
        if (m_strDefaultStationID.isEmpty())
            return;
        if (getSelectedStations().size()>0)
            return;
        MyAdapter adapter = ((MyAdapter) m_lstStations.getAdapter());

        for (int i=0; i< adapter.getListData().size(); i++)
        {

            if (adapter.getListData().get(i).getID().equals(m_strDefaultStationID)) {
//                //m_lstStations.setAdapter(m_lstStations.getAdapter());
                m_lstStations.setSelection(i);
                m_lstStations.setItemChecked(i, true);
            }
            else
            {
                m_lstStations.setItemChecked(i, false);
            }
//
        }


    }
    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public List<KDSStationIP> m_listData; //KDSStationsRelation class array


        public MyAdapter(Context context, List<KDSStationIP> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<KDSStationIP> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSStationIP> lst)
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

            KDSStationIP r =  m_listData.get(position);
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.kdsui_listitem_ip, null);

            }
            else
            {

            }
            convertView.setTag(r);

            if (!m_bShowMultipleUser)
                ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());
            else
            {
                if (r.getUserMode() == SettingsBase.KDSUserMode.Multiple.ordinal())
                {
                    ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID() + "(" +KDSUtil.convertIntToString( r.getScreen()) + ")");
                }
                else
                    ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());
            }

            ((TextView) convertView.findViewById(R.id.txtIP)).setText(r.getIP());
            ((TextView) convertView.findViewById(R.id.txtPort)).setText(r.getPort());

            return convertView;
        }

    }


}

