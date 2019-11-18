package com.bematechus.kds;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSStationConnection;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSStationsRelation;
import com.bematechus.kdslib.KDSXMLParserCommand;
import com.bematechus.kdslib.StationAnnounceEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class KDSUIRetriveConfig extends Activity implements StationAnnounceEvents, KDS.KDSEvents{


    TextView m_txtInfo = null;
    ListView m_lstStations = null;
    ArrayList<Map<String,Object>> m_arData= new ArrayList<Map<String,Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_settings);

        m_lstStations = (ListView) this.findViewById(R.id.lstStations);
        m_txtInfo  =(TextView) this.findViewById(R.id.txtInfo);
        KDS kds =  KDSGlobalVariables.getKDS();
        if (kds == null) return;
        kds.setStationAnnounceEventsReceiver(this);

        ArrayList<KDSStationsRelation> data = new ArrayList<>();
        MyAdapter adapter = new MyAdapter(this.getApplicationContext(), data);

        Button btn = (Button)this.findViewById(R.id.btnGet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KDSUIRetriveConfig.this.onBtnGetClicked(v);
            }
        });
        m_lstStations.setAdapter(adapter);
        m_lstStations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MyAdapter)m_lstStations.getAdapter()).setSelected(position);
                ((MyAdapter)m_lstStations.getAdapter()).notifyDataSetChanged();
            }
        });
        m_lstStations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((MyAdapter)m_lstStations.getAdapter()).setSelected(position);
                ((MyAdapter)m_lstStations.getAdapter()).notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        KDS kds = KDSGlobalVariables.getKDS();
        if (kds != null)
            kds.removeEventReceiver(this);
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        KDSKbdRecorder.convertKeyEvent(keyCode,event);
        return super.onKeyUp(keyCode, event);
    }

    private String getTargetStationID()
    {
        int nindex = ((MyAdapter)m_lstStations.getAdapter()).getSelected();
        if (nindex <0) return "";
        String stationID = ((MyAdapter)m_lstStations.getAdapter()).getListData().get(nindex).getID();
        return stationID;
    }

    public void getConfig()
    {
        KDS kds =  KDSGlobalVariables.getKDS();
        kds.setEventReceiver(this);
        String id = getTargetStationID();
        if (!id.isEmpty())
            kds.retrieveConfigFromStation(this.getTargetStationID(), m_txtInfo);
        // kds.removeEventReceiver(this);
    }
    public void onBtnGetClicked(View v)
    {
        String id = getTargetStationID();

        if (id.isEmpty()) {
            m_txtInfo.setText(getString(R.string.select_station));
            return;
        }
        String s = getString(R.string.confirm_retrieve_config);
        s = s.replace("#", "#"+id);


        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.confirm))
                .setMessage(s)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                KDSUIRetriveConfig.this.getConfig();

                            }
                        }
                )
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        d.show();



    }

    public void onStationConnected(String ip, KDSStationConnection conn)
    {

        m_txtInfo.setText(this.getString(R.string.retrieve_config_waiting_data));
        String s = KDSXMLParserCommand.createRequireConfiguration(""); //don't need the fromip
        conn.getSock().writeXmlTextCommand(s);


        setPrefFlag();


    }

    private void setPrefFlag()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isDirtyPrefs", true);
        editor.apply();
        editor.commit();
    }

    /**
     * Others send out the latest setting to me.
     * update the table.
     */
    public void onReceiveNewRelations()
    {

    }
    public void onReceiveRelationsDifferent(){}

    public void onXmlCommandBumpOrder(String orderGuid)
    {

    }
    public void onTTBumpOrder(String orderGuid)
    {

    }
    public void onStationDisconnected(String ip)
    {

    }
    public void onAskOrderState(Object objSource, String orderName)
    {

    }
    public void onSetFocusToOrder(String orderGuid) //set focus to this order
    {

    }
    public void onAcceptIP(String ip)
    {

    }
    public void onRefreshView(KDSUser.USER userID, KDSDataOrders orders, KDS.RefreshViewParam nParam)
    {

    }

//    public void onItemQtyChanged(KDSDataOrder order, KDSDataItem item)
//    {
//
//    }
//    public void onOrderStatusChanged(KDSDataOrder order, int nOldStatus)
//    {
//
//    }
    public void onRetrieveNewConfigFromOtherStation()
    {

        m_txtInfo.setText(this.getString(R.string.retrieve_config_done));
    }
    public void onShowMessage(KDS.MessageType msgType,String ip)
    {

    }

    public void onRefreshSummary(KDSUser.USER userID)
    {

    }
    private KDSStationsRelation  findStationByMac(String mac)
    {
        List<KDSStationsRelation> ar =  ((MyAdapter)m_lstStations.getAdapter()).getListData();
        int ncount = ar.size();
        for (int i=0; i< ncount ;i++)
        {
            if (ar.get(i).getMac().equals(mac))
                return ar.get(i);
        }

        return null;
    }
    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
    {


        if (stationReceived.getMac().equals(KDSGlobalVariables.getKDS().getLocalMacAddress()))
            return;

        KDSStationsRelation r = findStationByMac(stationReceived.getMac());


        //update data
        if ( r == null ) {
            r = new KDSStationsRelation();
            r.setID(stationReceived.getID());
            r.setIP(stationReceived.getIP());
            r.setPort(stationReceived.getPort());
            r.setMac(stationReceived.getMac());
            ((MyAdapter) m_lstStations.getAdapter()).getListData().add(r);
            ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
        }
        else
        {
            boolean bchanged = false;
            if (!r.getIP().equals(stationReceived.getIP())) {
                bchanged = true;
                r.setIP(stationReceived.getIP());
            }
            if (!r.getPort().equals(stationReceived.getPort())) {
                bchanged = true;
                r.setPort(stationReceived.getPort());
            }
            if (!r.getID().equals(stationReceived.getID())) {
                bchanged = true;
                r.setID(stationReceived.getID());
            }
            if (bchanged)
                ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
        }


    }

    private class MyAdapter extends BaseAdapter {
        public List<KDSStationsRelation> m_listData; //KDSStationsRelation class array
        private LayoutInflater minflater = null;
        private int m_nSelected = -1;
        public MyAdapter(Context context,  List<KDSStationsRelation> data) {
            this.minflater = LayoutInflater.from(context);
            m_listData = data;

        }
        public List<KDSStationsRelation> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSStationsRelation> lst)
        {
            m_listData = lst;

        }
        @Override
        public int getCount() {
            return m_listData.size();
        }
        @Override
        public Object getItem(int position) {
            return m_listData.get(position);
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
        public void setSelected(int nSelected)
        {
            m_nSelected = nSelected;
        }
        public int getSelected()
        {
            return m_nSelected;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.e("TEST", "refresh once");
            if (convertView == null)
                convertView = minflater.inflate(R.layout.simple_list_item_2, null, false);

            TextView txtID = (TextView) convertView.findViewById(android.R.id.text1);
            TextView txtIP = (TextView) convertView.findViewById(android.R.id.text2);
            txtID.setText( m_listData.get(position).getID());
            txtIP.setText(m_listData.get(position).getIP());

            if (position == m_nSelected) {//
                convertView.setBackgroundColor(minflater.getContext().getResources().getColor(R.color.listview_focus_bg));//

            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);//
            }
            return convertView;
        }
    }

}
