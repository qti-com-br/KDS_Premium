package com.bematechus.kdslib;

/**
 * Created by Administrator on 2017/2/13.
 */

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 * history:
 *  2.0.11, change its name from StationsPreferenceFragment to PreferenceFragmentStations
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PreferenceFragmentStations
        extends KDSPreferenceFragment
        implements StationAnnounceEvents,
                    KDSBase.KDSEvents,
                    KDSUIDialogBase.KDSDialogBaseListener,
                    KDSTimer.KDSTimerInterface
{

    private static final String TAG = "StationsPref";
    static public PreferenceFragmentStations m_stationsRelations = null;
    //////////////////////
    ListView m_lstStations = null;
    TextView m_txtError = null;
    CheckBox m_chkNoCheckRelation = null;
    //please set it before call ths class
    static KDSCallback m_kdsCallback = null;
    static public void setKDSCallback(KDSCallback kdsCallback)
    {
        m_kdsCallback = kdsCallback;
    }

    public void onStationConnected(String ip, KDSStationConnection conn){}
    public void onStationDisconnected(String ip){}
    public void onAcceptIP(String ip){}

    public void onRetrieveNewConfigFromOtherStation(){ reloadRelations();}
    public void onShowMessage(KDSBase.MessageType msgType,String message){}
    // public void onShowToastMessage(String message){}
    //public void onRefreshSummary(KDSUser.USER userID){}
    public void onAskOrderState(Object objSource, String orderName){}
    public void onSetFocusToOrder(String orderGuid){}
    public void onXmlCommandBumpOrder(String orderGuid){}
    public void onTTBumpOrder(String orderGuid){}
    public void onReceiveRelationsDifferent(){}
    //    public void onItemQtyChanged(KDSDataOrder order, KDSDataItem item)
//    {
//
//    }
//    public void onOrderStatusChanged(KDSDataOrder order, int nOldStatus)
//    {
//
//    }
    public void onReceiveNewRelations(){

        ArrayList<KDSStationsRelation> ar =  KDSStationsRelation.loadStationsRelation(KDSApplication.getContext(), false);
        if (!((MyAdapter)(m_lstStations.getAdapter() )).isDifferent(ar))
            return;


        reloadRelations();
        if (this.isVisible())
        {
            KDSUIDialogBase d = KDSUIDialogBase.singleInstance();//new KDSUIDialogBase(); //KPP1-164
            d.createInformationDialog(this.getActivity(),this.getString(R.string.str_message), this.getString(R.string.receive_new_relations), false );
            d.show();
        }
    }

    /**********************************************************************************************
     * From here, the code is same in KDSROUTER and KDS app.
     *
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_kdsCallback.call_setEventReceiver(this);
        //KDSGlobalVariables.getKDS().setEventReceiver(this);

    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View view =  inflater.inflate(R.layout.activity_kdsuistations_config, container, false);
        view.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
        init_variables(view);
        m_kdsCallback.call_setStationAnnounceEventsReceiver(this);
        //KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
        return view;
    }

    public void onDestroy() {
        super.onDestroy();
        m_kdsCallback.call_removeEventReceiver(this);
        //KDSGlobalVariables.getKDS().removeEventReceiver(this);
    }
    public void onResume()
    {
        super.onResume();
        m_stationsRelations = this;
        m_kdsCallback.call_setStationAnnounceEventsReceiver(this);
        //KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
        //m_timer.setReceiver(this);
        try {
            m_timer = new KDSTimer();
            m_timer.start(this.getActivity(), this, 1000);
        }catch (Exception e)
        {
            KDSLog.e(TAG, KDSLog._FUNCLINE_(),e);// + e.toString());
            //KDSLog.e(TAG, KDSUtil.error( e));
        }
    }

    /**
     *
     * @param keyCode
     * @param event
     * @return
     *    true: handle it
     *    false; pass
     */
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            if (((MyAdapter)m_lstStations.getAdapter()).isChanged()) {
                showChangedAlertDialog();
                return true;
            }
        return false;
    }


    public void showChangedAlertDialog()
    {
        m_contextApp = this.getActivity().getApplicationContext();
        String strOK = KDSUIDialogBase.makeOKButtonText2(KDSApplication.getContext());// .makeButtonText(KDSApplication.getContext(),R.string.ok, KDSSettings.ID.Bumpbar_OK );
        String strCancel = KDSUIDialogBase.makeCancelButtonText2(KDSApplication.getContext());// .makeButtonText(KDSApplication.getContext(),R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );

        AlertDialog d = new AlertDialog.Builder(this.getActivity())
                .setTitle(this.getString(R.string.question))
                .setMessage(this.getString(R.string.confirm_broadcast_relations_changes))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceFragmentStations.this.broadcastUpdateAfterPause();

                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceFragmentStations.this. reloadRelations();
                    }
                })
                .create();
        d.setCancelable(false);//.setFinishOnTouchOutside(false);
        d.setCanceledOnTouchOutside(false);
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                KDSUIDialogBase.DialogEvent evID = KDSUIDialogBase.checkDialogKeyboardEvent(event);
                if (evID == KDSUIDialogBase.DialogEvent.OK){// KDSSettings.ID.Bumpbar_OK) {
                    dialog.dismiss();
                    PreferenceFragmentStations.this.broadcastUpdateAfterPause();
                    return true;
                } else if (evID == KDSUIDialogBase.DialogEvent.Cancel){// KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        d.show();
    }

    Context m_contextApp = null;
    public void onPause()
    {
        super.onPause();
        m_stationsRelations = null;
        m_contextApp = this.getActivity().getApplicationContext();
        m_timer.stop();

        if (((MyAdapter)m_lstStations.getAdapter()).isError()) { //if error show pop
            KDSUIDialogBase d = new KDSUIDialogBase();
            d.createInformationDialog(this.getActivity(),this.getString(R.string.error), this.getString(R.string.confirm_fix_errors), false );
            d.show();
            return;
        }
        if (((MyAdapter)m_lstStations.getAdapter()).isChanged()) {
            showChangedAlertDialog();
        }
        m_kdsCallback.call_setStationAnnounceEventsReceiver(null);
        //KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(null);
    }

    public void onListItemClicked(View viewRow)
    {
        if (m_lstStations.getTag() != null) {
            View v = (View) m_lstStations.getTag();
            v.setBackgroundColor(Color.TRANSPARENT);
            v.invalidate();
        }
        View viewFocused = (((MyAdapter) m_lstStations.getAdapter())).getEditingView();
        if (viewFocused instanceof EditText)
        {
            if (viewFocused.getTag() != viewRow)
            {
                ((EditText)viewFocused).clearFocus();
            }
        }



        ((MyAdapter) m_lstStations.getAdapter()).setSelectItem((int) (viewRow.getTag()));
        m_lstStations.setTag(viewRow);
        viewRow.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        //((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
    }

    public int getSelectedItem()
    {
        return ((MyAdapter) m_lstStations.getAdapter()).getSelectedItem();
    }
    public void onNew(View v)
    {
        ((MyAdapter) m_lstStations.getAdapter()).getListData().add(new KDSStationsRelation());
        if (m_lstStations.getTag() != null) {
            View vRow = (View) m_lstStations.getTag();
            vRow.setBackgroundColor(Color.TRANSPARENT);

        }
        m_lstStations.setTag(null);
        ((MyAdapter) m_lstStations.getAdapter()).setSelectItem(-1);
        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();

    }
    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof KDSUIIPSearchDialog)
        {
            //KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
            m_kdsCallback.call_setStationAnnounceEventsReceiver(this);
            ArrayList<String> ar =(ArrayList<String>) ((KDSUIIPSearchDialog)dialog).getResult();
            if (ar.size() <=0) return;
            this.retrieveRelationFrom(ar.get(0));
        }
        else if (dialog instanceof KDSUIDialogConfirm)
        { //confirm remmove
            KDSStationsRelation r = (KDSStationsRelation) obj;
            ((MyAdapter) m_lstStations.getAdapter()).getListData().remove(r);
            PreferenceFragmentStations.this.save();
            ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
        }
    }
    public void onDel(View v)
    {
        int nindex = ((MyAdapter) m_lstStations.getAdapter()).getSelectedItem();
        if (nindex <0) return;
        String s = this.getString(R.string.confirm_remove_relation_station);
        KDSStationsRelation r = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(nindex);
        s += r.getID();
        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(this.getActivity(), s, this);
        dlg.setTag(r);
        dlg.show();
    }

    public void onShowStationID(View v)
    {

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                //KDSGlobalVariables.getKDS().getBroadcaster().broadcastShowStationID();
                m_kdsCallback.call_broadcastShowStationID();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    public void onRetrieveRelations(View v)
    {
        KDSUIIPSearchDialog dlg = new KDSUIIPSearchDialog(this.getActivity(), KDSUIIPSearchDialog.IPSelectionMode.Single, this, "");
        //dlg.setKDSCallback(KDSGlobalVariables.getKDS());
        dlg.setKDSCallback(m_kdsCallback);
        dlg.setTitle(this.getString(R.string.select_retrieve_station));
        dlg.setSelf(false);
        dlg.show();
    }
    public void onResetRelations(View v)
    {
        ((MyAdapter) m_lstStations.getAdapter()).reset();
    }

    /**
     *
     * @param stationInfo
     * stationID:ip:port
     */
    public void retrieveRelationFrom( String stationInfo)
    {
        KDSStationIP station = KDSStationActived.parseString(stationInfo);
        String id = station.getID();
        Object[] ar = new Object[]{id};
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String id =(String) params[0];
                //KDSGlobalVariables.getKDS().udpAskRelations(id);
                m_kdsCallback.call_udpAskRelations(id);
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
    }

    public void broadcastUpdate()
    {

        this.save();
        //KDS.broadcastStationsRelations();
        m_kdsCallback.call_broadcastStationsRelations();

//        String s = KDSSettings.loadStationsRelationString(this.getActivity().getApplicationContext(), true);
//        Object[] ar = new Object[]{s};
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                String strData =(String) params[0];
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastRelations(strData);
//                return null;
//            }
//
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
    }

    public void broadcastUpdateAfterPause()
    {

        KDSStationsRelation.save(m_contextApp, (ArrayList) ((MyAdapter) (m_lstStations.getAdapter())).getListData(), m_chkNoCheckRelation.isChecked());

        //String s = KDSSettings.loadStationsRelationString(m_contextApp, true);
        String s = m_kdsCallback.call_loadStationsRelationString(true);
        Object[] ar = new Object[]{s};

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                String strData =(String) params[0];
                //KDSGlobalVariables.getKDS().getBroadcaster().broadcastRelations(strData);
                m_kdsCallback.call_broadcastRelations(strData);
                return null;
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
        this.reloadRelations();
    }

    public void onBroadcastUpdate(View v)
    {

        if (((MyAdapter)m_lstStations.getAdapter()).isError())
        {
            KDSUIDialogBase d = new KDSUIDialogBase();
            d.createInformationDialog(this.getActivity(),this.getString(R.string.confirm), this.getString(R.string.confirm_fix_errors), false );
            d.show();
            return ;
        }

        String strOK = KDSUIDialogBase.makeOKButtonText2(KDSApplication.getContext());//.makeButtonText(KDSApplication.getContext(),R.string.ok, KDSSettings.ID.Bumpbar_OK );
        String strCancel = KDSUIDialogBase.makeCancelButtonText2(KDSApplication.getContext());// .makeButtonText(KDSApplication.getContext(),R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );

        AlertDialog d = new AlertDialog.Builder(this.getActivity())
                .setTitle(this.getString(R.string.confirm))
                .setMessage(this.getString(R.string.confirm_broadcast_relations))
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PreferenceFragmentStations.this.broadcastUpdate();
                            }
                        }
                )
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                KDSUIDialogBase.DialogEvent evID = KDSUIDialogBase.checkDialogKeyboardEvent(event);

                if (evID == KDSUIDialogBase.DialogEvent.OK)// KDSSettings.ID.Bumpbar_OK)
                {
                    dialog.dismiss();
                    PreferenceFragmentStations.this.broadcastUpdate();
                    return true;
                }
                else if (evID == KDSUIDialogBase.DialogEvent.Cancel)// KDSSettings.ID.Bumpbar_Cancel)
                {
                    dialog.cancel();
                    return true;
                }
                return false;
            }
        });
        d.show();
    }
    protected KDSStationsRelation findStationByID(String stationID)
    {
        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
        for (int i=0; i< ncount; i++) {
            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getID().equals(stationID))
            {
                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);

            }
        }
        return null;
    }

    protected KDSStationsRelation findStationByMac(String mac)
    {
        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
        for (int i=0; i< ncount; i++) {
            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getMac().equals(mac))
            {
                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);

            }
        }
        return null;
    }

    protected KDSStationsRelation findStationByIP(String stationIP)
    {
        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
        for (int i=0; i< ncount; i++) {
            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getIP().equals(stationIP))
            {
                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);

            }
        }
        return null;
    }

    KDSTimer m_timer = new KDSTimer();
    public void onTime()
    {
        refreshNetworkStatusIcon();
    }
    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
    {


        KDSStationsRelation r = findStationByMac(stationReceived.getMac());
        //update data
        if ( r == null ) {
            //check if we have a manually added station.
            KDSStationsRelation stationManual = findStationByID(stationReceived.getID());
            if (stationManual == null) {
                r = new KDSStationsRelation();
                r.setID(stationReceived.getID());
                r.setIP(stationReceived.getIP());
                r.setPort(stationReceived.getPort());
                r.setMac(stationReceived.getMac());
                ((MyAdapter) m_lstStations.getAdapter()).getListData().add(r);
                KDSStationsRelation.sortStationsRelation(((MyAdapter) m_lstStations.getAdapter()).getListData());
                ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
            }
            else
            {
                if (stationManual.getMac().isEmpty())
                {
                    stationManual.setMac(stationReceived.getMac());
                    stationManual.setIP(stationReceived.getIP());
                    stationManual.setPort(stationReceived.getPort());
                }

            }
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

    /**
     * refresh the network state icon according the active stations state.
     * Call it in a timer
     */
    public void refreshNetworkStatusIcon()
    {
        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
        for (int i=0; i< ncount; i++) {


            // MyAdapter.ViewHolder holder =( MyAdapter.ViewHolder) (r.getTag());
            View view = m_lstStations.getChildAt(i);
            if (view == null) return;
            if (view.getTag() == null) continue;
            int nPosition = (int)view.getTag();
            KDSStationsRelation r =(KDSStationsRelation) ((MyAdapter) m_lstStations.getAdapter()).getListData().get(nPosition);
            refreshStationNetworkStatusIcon(view, r);

//            if (view == null) continue;
//            ImageView img = (ImageView) view.findViewById(R.id.imgNetwork);
//            //if (holder == null) continue;
//            boolean bonline = true;
//            if (r.getMac().equals(KDSGlobalVariables.getKDS().getLocalMacAddress()))
//            {
//                bonline = KDSSocketManager.isNetworkActived(KDSApplication.getContext());
//            }
//            else
//            {
//                bonline = (KDSGlobalVariables.getKDS().getStationsConnections().findActivedStationByID(r.getID()) != null);
//
//            }
//            if (bonline)
//            {
//                if (img != null) {
//                    img.setImageResource(R.drawable.online);
//                    img.invalidate();
//                }
//            }
//            else
//            {
//                if (img != null) {
//                    img.setImageResource(R.drawable.offline);
//                    img.invalidate();
//                }
//            }
        }

    }

    /**
     *
     * @param viewRow
     *  Its tag contains the position value.
     * @param r
     */
    public void refreshStationNetworkStatusIcon(View viewRow,  KDSStationsRelation r)
    {


        if (r.getTag() == null) return;

        ImageView img = (ImageView) viewRow.findViewById(R.id.imgNetwork);
        if (img == null) return;

        //if (holder == null) continue;
        boolean bonline = true;
        if (r.getMac().equals(m_kdsCallback.call_getLocalMacAddress()))// KDSGlobalVariables.getKDS().getLocalMacAddress()))
        {
            bonline = KDSSocketManager.isNetworkActived(KDSApplication.getContext());
        }
        else
        {
            //bonline = (KDSGlobalVariables.getKDS().getStationsConnections().findActivedStationByID(r.getID()) != null);
            bonline = (m_kdsCallback.call_findActivedStationByID(r.getID()) != null);

        }
        if (bonline)
        {
            if (img != null) {
                img.setImageResource(com.bematechus.kdslib.R.drawable.online);
                img.invalidate();
            }
        }
        else
        {
            if (img != null) {
                img.setImageResource(R.drawable.offline);
                img.invalidate();
            }
        }


    }

    KDSStationsRelation findStation(List<KDSStationsRelation> lst, String stationID)
    {
        int ncount = lst.size();
        for (int i=0; i< ncount; i++)
        {
            if (lst.get(i).getID().equals(stationID))
                return lst.get(i);
        }
        return null;
    }



    protected void init_variables(View view) {

        m_txtError = (TextView)view.findViewById(R.id.txtError);
        m_lstStations = (ListView)view.findViewById(R.id.lstStations);
        m_chkNoCheckRelation = (CheckBox) view.findViewById(R.id.chkNoRelationsCheck);

        List  lst =  new ArrayList<KDSStationsRelation>();
        MyAdapter adapter = new MyAdapter(KDSApplication.getContext(), lst);
        m_lstStations.setAdapter(adapter);
        m_lstStations.setEnabled(true);

        Button btnNew =  (Button)view.findViewById(R.id.btnNew);
        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onNew(v);
            }
        });

        Button btnDel =  (Button)view.findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onDel(v);
            }
        });

        Button btnUpdate =  (Button)view.findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onBroadcastUpdate(v);
            }
        });


        Button btnShowID =(Button)view.findViewById(R.id.btnShowID);
        btnShowID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onShowStationID(v);
            }
        });

        Button btnRetrieve =(Button)view.findViewById(R.id.btnRetrieve);
        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onRetrieveRelations(v);
            }
        });

        Button btnReset =(Button)view.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceFragmentStations.this.onResetRelations(v);
            }
        });

        m_chkNoCheckRelation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean bNoCheck =  ((CheckBox)v).isChecked();
                onNoCheckRelationChanged(bNoCheck);
            }
        });


        load();
        adapter.notifyDataSetChanged();

    }

    private void onNoCheckRelationChanged(boolean bNoCheck)
    {
        ArrayList<KDSStationsRelation> ar =SettingsBase.loadStationsRelation(KDSApplication.getContext(), true);
        KDSStationsRelation r =  SettingsBase.findRelationNoCheckOptionStation(ar);

        if (r == null)
            SettingsBase.addRelationNoCheckOptionStation(ar, bNoCheck);
        else
        {
            r.setID( ( bNoCheck?SettingsBase.NO_CHECK_TRUE:SettingsBase.NO_CHECK_FALSE) );

        }
        SettingsBase.saveStationsRelation(KDSApplication.getContext(), ar);
    }

    public void save()
    {

        // if (!saveScreenDataToBuffer()) return;
        KDSStationsRelation.save(KDSApplication.getContext(), (ArrayList) ((MyAdapter) (m_lstStations.getAdapter())).getListData(), m_chkNoCheckRelation.isChecked());
        List<KDSStationsRelation> ar =((MyAdapter)(m_lstStations.getAdapter() )).getListData();
        ((MyAdapter)(m_lstStations.getAdapter() )).cloneToOriginalArray();
        ((MyAdapter)(m_lstStations.getAdapter() )).notifyDataSetChanged();

    }
    public void load()
    {
        if (this == null) return;
        if (this.getActivity() == null) return;
        if (this.getActivity().getApplicationContext() == null) return;
        ArrayList<KDSStationsRelation> ar =  KDSStationsRelation.loadStationsRelation(KDSApplication.getContext(), true);
        //find no check option. I save it as station.
        KDSStationsRelation noCheckRelation = SettingsBase.removeRelationNoCheckOptionStation(ar);


        sortStations(ar);
        ((MyAdapter)(m_lstStations.getAdapter() )).setListData(ar);
        if (noCheckRelation != null)
            m_chkNoCheckRelation.setChecked(noCheckRelation.getID().equals(SettingsBase.NO_CHECK_TRUE));
        else
            m_chkNoCheckRelation.setChecked(false);

    }

    private void sortStations(ArrayList<KDSStationsRelation> arStationsRelation)
    {
        if (arStationsRelation.size() <=1 )
            return;
        Collections.sort(arStationsRelation, new Comparator() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        KDSStationsRelation c1 = (KDSStationsRelation) o1;
                        KDSStationsRelation c2 = (KDSStationsRelation) o2;
                        String name1 = c1.getID();//.makeDurationString();
                        String pre = "000000000000000000000";
                        name1 = pre + name1;
                        name1 = name1.substring(name1.length()-20);
                        String name2 = c2.getID();
                        name2 = pre + name2;
                        name2 = name2.substring(name2.length()-20);
                        return name1.compareTo(name2);
                    }
                }
        );
    }



    public void reloadRelations()
    {
        load();
        ( (MyAdapter)(m_lstStations.getAdapter())).notifyDataSetChanged();
    }

    /**
     * listview client [edit] popup dialog
     */

    private class MyAdapter extends BaseAdapter {

        public class ViewHolder{
            EditText m_txtStationID = null;
            // EditText m_txtIP = null;
            //EditText m_txtPort = null;
            Spinner m_spinnerFunc = null;
            EditText m_txtExp = null;
            TextView m_txtSlave = null;
            Spinner m_spinnerSlaveFunc = null;
            Spinner m_spinnerStatus = null;
            ImageView m_viewImg = null;
            ImageView m_viewNetwork = null;

        }


        private class CustomTextWatcher implements TextWatcher {
            private EditText mEditText;

            public CustomTextWatcher(EditText e) {
                mEditText = e;
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
            }
            public EditText getEditText()
            {
                return mEditText;
            }
        }

        /**
         * while the spinner clicked in relation table, this class will work!
         */
        private class CustomSpinnerItemSelectedListener implements  AdapterView.OnItemSelectedListener
        {
            private Spinner m_spinner = null;

            public CustomSpinnerItemSelectedListener(Spinner e) {
                m_spinner = e;
            }

            public Spinner getSpinner()
            {
                return m_spinner;
            }

            KDSStationsRelation m_relationBeforeShowSlaveDialog = null;
            View m_viewBeforeShowSlaveDialog = null;
            int m_npositionBeforeShowSlaveDialog = 0;
            SlaveFunction m_slaveFunctionBeforeShowSlaveDialog = null;
            AlertDialog m_dlgShowSlave = null;

            List<KDSStationsRelation> m_lstBeforeShowDialog = null;

            private void confirmRemoveExpo2(KDSStationsRelation relation)
            {
                String expoID = relation.getID();

                String strComfirm = PreferenceFragmentStations.this.getString(R.string.remove_relations_expo);
                strComfirm = strComfirm.replace("#", "#" + expoID);

                MyAdapter.this.removeExpeditorFromAll(expoID);
                MyAdapter.this.notifyDataSetChanged();

                int duration = Toast.LENGTH_LONG;
                Toast t = Toast.makeText(KDSApplication.getContext(), strComfirm, duration);
                t.show();

            }

            /**
             *
             * @param primaryStationID
             *  set which station
             * @param slaveStationID
             *  primaryStationID's slave station id.
             * @param slaveStationFunc
             *  The slave station use what function.
             */
            private void changeStationSlaveFunctionAccordingItsSlaveStatioin(String primaryStationID, String slaveStationID, SettingsBase.StationFunc slaveStationFunc)
            {
                KDSStationsRelation primaryRelation = getStationRelation(primaryStationID);
                if (primaryRelation == null) return;
                SettingsBase.SlaveFunc slaveFunc = SettingsBase.SlaveFunc.Unknown;
                switch (slaveStationFunc)
                {
                    case Prep:
                        slaveFunc =  SettingsBase.SlaveFunc.Unknown;
                        break;
                    case Expeditor:
                    case Queue_Expo:
                        slaveFunc =  SettingsBase.SlaveFunc.Unknown;
                        break;
                    case Queue:
                        slaveFunc =  SettingsBase.SlaveFunc.Order_Queue_Display;
                        //2.0.11, allow prep as primary of queue
                        if (primaryRelation.getFunction() != SettingsBase.StationFunc.Expeditor &&
                                primaryRelation.getFunction() != SettingsBase.StationFunc.Prep)
                            return;
                        break;
                    case Mirror:
                        slaveFunc =  SettingsBase.SlaveFunc.Mirror;
                        if ( (primaryRelation.getFunction() != SettingsBase.StationFunc.Prep) &&
                                (primaryRelation.getFunction() != SettingsBase.StationFunc.Expeditor)) //kpp1-286, expo allow mirror
                            return;
                        break;
                    case Backup:
                        slaveFunc =  SettingsBase.SlaveFunc.Backup;
                        if (primaryRelation.getFunction() != SettingsBase.StationFunc.Prep
                                &&primaryRelation.getFunction() != SettingsBase.StationFunc.Expeditor
                                &&primaryRelation.getFunction() != SettingsBase.StationFunc.Backup &&
                                primaryRelation.getFunction() != SettingsBase.StationFunc.Queue_Expo)

                            return;
                        break;
                    case Workload:
                        slaveFunc =  SettingsBase.SlaveFunc.Automatic_work_loan_distribution;
                        if (primaryRelation.getFunction() != SettingsBase.StationFunc.Prep )
                            return;
                        break;
                    case Duplicate:
                        slaveFunc =  SettingsBase.SlaveFunc.Duplicate_station;
                        if (primaryRelation.getFunction() != SettingsBase.StationFunc.Prep &&
                           (primaryRelation.getFunction() != SettingsBase.StationFunc.Expeditor)) //kpp1-286, expo allow mirror
                            return;
                        break;
                }
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(primaryStationID))
                    {
                        MyAdapter.this.getListData().get(i).setSlaveFunc(slaveFunc);
                        MyAdapter.this.getListData().get(i).setSlaveStations(slaveStationID);
                    }
                }
            }

            private String getMyPrimaryStation(String slaveStationID)
            {
                return MyAdapter.this.getMyPrimaryStation(slaveStationID);

            }

            private KDSStationsRelation getStationRelation(String stationID)
            {
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
                    {

                        return  MyAdapter.this.getListData().get(i);

                    }
                }
                return null;
            }

            private void onInputPrimaryIDDlgOK()
            {

                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                String primaryID = editText.getText().toString();
                //m_relationBeforeShowSlaveDialog.setSlaveStations(slaveID);
                changeStationSlaveFunctionAccordingItsSlaveStatioin(primaryID,m_relationBeforeShowSlaveDialog.getID(), m_relationBeforeShowSlaveDialog.getFunction() );

                SettingsBase.StationFunc sfunc = MyAdapter.this.getStationChainFunction(m_relationBeforeShowSlaveDialog.getID(), "");
                if ( sfunc == SettingsBase.StationFunc.Expeditor ||
                        sfunc == SettingsBase.StationFunc.Queue_Expo)
                    MyAdapter.this.addExpeditorToAll(m_relationBeforeShowSlaveDialog.getID());
                else
                    MyAdapter.this.removeExpeditorFromAll(m_relationBeforeShowSlaveDialog.getID());

                MyAdapter.this.notifyDataSetChanged();
            }
            Spinner m_spinnerBeforeCallInputPrimaryID = null;//for redraw this spinner
            private void inputPrimaryStationID(Spinner stationFuncSpinner, int position, KDSStationsRelation relation)
            {
                m_relationBeforeShowSlaveDialog = relation;
                String strComfirm = PreferenceFragmentStations.this.getString(R.string.please_input_primary_station);
                strComfirm = strComfirm.replace("#", relation.getID());


                String strOK = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(),KDSApplication.getContext().getString( R.string.ok), KDSUIDialogBase.DialogEvent.OK);// KDSSettings.ID.Bumpbar_OK);

                String strCancel = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.cancel), KDSUIDialogBase.DialogEvent.Cancel);// KDSSettings.ID.Bumpbar_Cancel);
                m_viewBeforeShowSlaveDialog = LayoutInflater.from(KDSApplication.getContext()).inflate(R.layout.kdsui_dlg_input_slave, null);
                m_spinnerBeforeCallInputPrimaryID = stationFuncSpinner;
                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
                        .setTitle(PreferenceFragmentStations.this.getString(R.string.input))
                        .setMessage(strComfirm)
                        .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onInputPrimaryIDDlgOK();
                                        if (m_spinnerBeforeCallInputPrimaryID != null) {
                                            ((ArrayAdapter) m_spinnerBeforeCallInputPrimaryID.getAdapter()).notifyDataSetChanged();
                                            m_spinnerBeforeCallInputPrimaryID = null;
                                        }
                                    }
                                }
                        )
                        .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MyAdapter.this.notifyDataSetChanged();
                            }
                        })
                        .create();
                d.setView(m_viewBeforeShowSlaveDialog);
                m_dlgShowSlave = d;
                m_viewBeforeShowSlaveDialog.setFocusable(true);

                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                editText.setText(getMyPrimaryStation(relation.getID() ));


                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
                        String primaryID = s.toString();
                        if (primaryID.isEmpty()) {
                            m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
                            if (btn != null) btn.setEnabled(false);
                        }
                        else
                        {
                            String err = validateInputPrimary(m_relationBeforeShowSlaveDialog.getID(), m_relationBeforeShowSlaveDialog.getFunction(), primaryID);
                            if (err.isEmpty()) {
                                m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
                                if (btn != null) btn.setEnabled(true);
                            }
                            else {

                                m_dlgShowSlave.setTitle(err);
                                if (btn != null) btn.setEnabled(false);
                            }
                        }
                    }
                });


                d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        //KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                        //KDSSettings.ID evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
                        KDSUIDialogBase.DialogEvent evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
                        if (evID == KDSUIDialogBase.DialogEvent.OK) {
                            m_relationBeforeShowSlaveDialog.setSlaveFunc(SettingsBase.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
                            EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                            m_relationBeforeShowSlaveDialog.setSlaveStations(editText.getText().toString());

                            dialog.dismiss();
                            return true;
                        } else if (evID == KDSUIDialogBase.DialogEvent.Cancel) {
                            dialog.cancel();
                            return true;
                        }
                        return false;
                    }
                });
                //d.getButton(AlertDialog.BUTTON_POSITIVE).setTag(relation.getTag());

                d.show();
                Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
                if (relation.getSlaveStations().isEmpty()) {
                    if (btn != null)
                        btn.setEnabled(false);
                }
            }
            private String validateInputPrimary(String stationID, SettingsBase.StationFunc stationFunc, String primaryID)
            {
                String strErr = "";
                if (stationID.equals(primaryID))
                    return  PreferenceFragmentStations.this.getString(R.string.error_primary_myself);
                switch (stationFunc)
                {

                    case Prep:
                    case Expeditor:
                    case TableTracker:
                        break;
                    case Queue:
                        //2.0.11, comment it, allow prep station has queue slave.
                        if ( (!isExpoStation(primaryID)) && (!isPrepStation(primaryID)))
                            strErr =  PreferenceFragmentStations.this.getString(R.string.error_not_expo_or_prep);//R.string.error_not_expo);
                        break;
                    case Mirror:
                    case Duplicate:
                        //kpp1-286, just expo and prep allow mirror and duplicate.
                        if ( (!isExpoStation(primaryID)) && (!isPrepStation(primaryID)))
                            strErr =  PreferenceFragmentStations.this.getString(R.string.error_not_expo_or_prep);//R.string.error_not_expo);
                        break; //kpp1-286, Duplicate and Mirror Expeditor
                    case Workload:
                        if (!isPrepStation(primaryID))
                            strErr = PreferenceFragmentStations.this.getString(R.string.error_not_prep);
                        break;

                    case Backup:
                        if (!isExpoStation(primaryID) &&
                                !isPrepStation(primaryID) &&
                                !isStationFunc(primaryID, SettingsBase.StationFunc.Backup))
                            strErr = PreferenceFragmentStations.this.getString(R.string.error_not_expo_prep_backup);
                        break;



                }
                return strErr;
            }
            private SettingsBase.SlaveFunc getSlaveFuncFromStationFunc(SettingsBase.StationFunc stationFunc)
            {
                SettingsBase.SlaveFunc slaveFunc = SettingsBase.SlaveFunc.Unknown;
                switch (stationFunc)
                {

                    case Prep:
                        slaveFunc =  SettingsBase.SlaveFunc.Unknown;
                        break;
                    case Expeditor:
                        slaveFunc =  SettingsBase.SlaveFunc.Unknown;
                        break;
                    case TableTracker:
                        slaveFunc =  SettingsBase.SlaveFunc.Unknown;
                        break;
                    case Queue:
                        slaveFunc =  SettingsBase.SlaveFunc.Order_Queue_Display;

                        break;
                    case Mirror:
                        slaveFunc =  SettingsBase.SlaveFunc.Mirror;

                        break;
                    case Backup:
                        slaveFunc =  SettingsBase.SlaveFunc.Backup;

                        break;
                    case Workload:
                        slaveFunc =  SettingsBase.SlaveFunc.Automatic_work_loan_distribution;

                        break;
                    case Duplicate:
                        slaveFunc =  SettingsBase.SlaveFunc.Duplicate_station;

                        break;
                }
                return slaveFunc;
            }

            /**
             *
             * @param slaveStationID
             *  The slave station
             * @param slaveStationFunc
             *  This slave station's station function
             */
            private void removeSlaveStation(String slaveStationID, SettingsBase.StationFunc slaveStationFunc)
            {
                boolean bChanged = false;
                SettingsBase.SlaveFunc slaveFunc =  getSlaveFuncFromStationFunc(slaveStationFunc);
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getSlaveStations().equals(slaveStationID))
                    {
                        if (MyAdapter.this.getListData().get(i).getSlaveFunc() == slaveFunc) {
                            MyAdapter.this.getListData().get(i).setSlaveFunc(SettingsBase.SlaveFunc.Unknown);
                            MyAdapter.this.getListData().get(i).setSlaveStations("");
                            bChanged = true;
                        }

                    }
                }
                if (bChanged)
                    MyAdapter.this.notifyDataSetChanged();
            }


            private void showOnlyOneTrackerAllowed() {
                Toast t = Toast.makeText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.tracker_only_one_allowed),  Toast.LENGTH_LONG);
                t.show();
            }

            private void showPressTTButtonMessage()
            {
                Toast t = Toast.makeText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.tt_authen_press_button),  Toast.LENGTH_LONG);
                t.show();
            }

            public void onStationFunctionItemSelected2(AdapterView<?> parent, View view, int position, long id) {
                //TextView v1 = (TextView) view;
                //v1.setTextColor(Color.BLACK);
                Spinner t = this.getSpinner();
                View viewRow = (View) t.getTag();
                int nposition = (int) viewRow.getTag();
                if (nposition >= MyAdapter.this.getListData().size())
                    return;
                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);

                //KDSSettings.StationFunc newStationFunc =  KDSSettings.StationFunc.values()[position];
                SettingsBase.StationFunc newStationFunc = ((MyStationFuncSpinnerAdapter) t.getAdapter()).getItem(position).getFunction();

                if (newStationFunc == SettingsBase.StationFunc.TableTracker)
                { //only one tracker allowed
                    if (isMoreThanOneTrackerStation(relation))
                    {
                        MyAdapter.this.notifyDataSetChanged();
                        showOnlyOneTrackerAllowed();
                        return;
                    }
                    else
                    {
                        if (relation.getFunction() != newStationFunc) {
                            //if (KDSSettings.loadTrackerAuthen().isEmpty())
                            showPressTTButtonMessage();
                        }
                    }
                }

                Spinner slaveFuncSpinner = (Spinner) viewRow.findViewById(R.id.spinnerSlaveFunc);
                boolean bRemoveExpo = false;
                switch (newStationFunc)
                {

                    case Prep:

                        if (relation.getFunction() == SettingsBase.StationFunc.Expeditor ||
                                relation.getFunction() == SettingsBase.StationFunc.Queue_Expo)
                            bRemoveExpo = true;
                        SettingsBase.StationFunc sfunc = MyAdapter.this.getStationChainFunction(relation.getID(), "");
                        if (sfunc == SettingsBase.StationFunc.Expeditor ||
                                sfunc == SettingsBase.StationFunc.Queue_Expo)
                            bRemoveExpo = true;
                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  SettingsBase.StationFunc.Prep);

                        //relation.setFunction(SettingsBase.StationFunc.Normal);
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());

                        relation.setFunction(newStationFunc);
                        if (bRemoveExpo)
                            confirmRemoveExpo2(relation);
                        break;
                    case Expeditor:

                        //Spinner slaveFuncSpinner = (Spinner) viewRow.findViewById(R.id.spinnerSlaveFunc);
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());


                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  SettingsBase.StationFunc.Expeditor);
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());
                        if (relation.getFunction() != SettingsBase.StationFunc.Expeditor) {
                            relation.setFunction(SettingsBase.StationFunc.Expeditor);
                            relation.setExpStations(""); //clear expo'expo
                            MyAdapter.this.addExpeditorToAll(relation.getID());
                            MyAdapter.this.notifyDataSetChanged();
                        }
                        break;
                    case Queue_Expo:
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());


                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  SettingsBase.StationFunc.Queue_Expo);
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());
                        if (relation.getFunction() != SettingsBase.StationFunc.Queue_Expo) {
                            relation.setFunction(SettingsBase.StationFunc.Queue_Expo);
                            relation.setExpStations(""); //clear expo'expo
                            //Comment it:
                            //There is another minor bug that can be fixed. When you set a station to queue-expo all of the prep stations get pointed to that queue expo.
                            // When testing on 24 stations you have to remove most of them and takes a lot of time.
                            //Can you make it so it does not do that. When you set to queue-expo you have to manually put the expo stations in for each prep.
                            //MyAdapter.this.addExpeditorToAll(relation.getID());
                            MyAdapter.this.notifyDataSetChanged();
                        }
                        break;
                    case TableTracker:
                    {
                        SettingsBase.StationFunc oldFunc = relation.getFunction();
                        if (relation.getFunction() == SettingsBase.StationFunc.Expeditor ||
                                relation.getFunction() == SettingsBase.StationFunc.Queue_Expo)
                            bRemoveExpo = true;
                        SettingsBase.StationFunc stationfunc = MyAdapter.this.getStationChainFunction(relation.getID(), "");
                        if ( stationfunc == SettingsBase.StationFunc.Expeditor ||
                                stationfunc == SettingsBase.StationFunc.Queue_Expo)
                            bRemoveExpo = true;
                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  SettingsBase.StationFunc.TableTracker);
                        relation.setExpStations("");
                        //relation.setFunction(SettingsBase.StationFunc.Normal);
                        if (relation.getFunction() != newStationFunc)
                            removeSlaveStation(relation.getID(), relation.getFunction());

                        relation.setFunction(newStationFunc);
                        relation.setSlaveStations("");
                        if (oldFunc != relation.getFunction())
                            MyAdapter.this.notifyDataSetChanged();
                        if (bRemoveExpo)
                            confirmRemoveExpo2(relation);

                        break;
                    }

                    case Queue:
                    case Mirror:
                    case Backup:
                    case Workload:
                    case Duplicate:
                        boolean bIsQueue = (relation.getFunction() == SettingsBase.StationFunc.Queue);

                        if (relation.getFunction() != newStationFunc) {
                            removeSlaveStation(relation.getID(), relation.getFunction());
                            if (newStationFunc == SettingsBase.StationFunc.Queue )
                                relation.setExpStations("");
                        }
                        if (relation.getFunction() == SettingsBase.StationFunc.Expeditor ||
                                relation.getFunction() == SettingsBase.StationFunc.Queue_Expo)
                            bRemoveExpo = true;
                        SettingsBase.StationFunc sfunction = MyAdapter.this.getStationChainFunction(relation.getID(), "");
                        if ( (sfunction == SettingsBase.StationFunc.Expeditor|| sfunction == SettingsBase.StationFunc.Queue_Expo )
                                && newStationFunc != SettingsBase.StationFunc.Backup) {
                            if (!bIsQueue) //if this is queue, don't remove it as expo
                                bRemoveExpo = true;
                        }
                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  newStationFunc);
                        if (relation.getFunction() != newStationFunc) {
                            relation.setFunction(newStationFunc);
                            MyAdapter.this.notifyDataSetChanged();

                            inputPrimaryStationID(t, position, relation);
                        }
                        if (bRemoveExpo && !bIsQueue)
                            confirmRemoveExpo2(relation);

                        break;

                }


            }


            /**
             * after input slave ID, change this station function to select function.
             * @param stationID
             *  The old slave station ID, this station worked as slaveFunc. Change station function.
             * @param
             */
            private void changeStationToGiveFunctionAccordingSlaveFunction(String stationID, SettingsBase.SlaveFunc slaveFunc, boolean bCheckPrimaryLoop)
            {
                SettingsBase.StationFunc stationFunc = SettingsBase.StationFunc.Prep;
                String primaryStation = getMyPrimaryStation(stationID);
                if (bCheckPrimaryLoop)
                    if (!primaryStation.isEmpty()) return; //this station work as others station's slave (the backup allow loop), return it.
                switch (slaveFunc)
                {

                    case Unknown:
                        stationFunc = SettingsBase.StationFunc.Prep;
                        break;
                    case Backup:
                        stationFunc = SettingsBase.StationFunc.Backup;
                        break;
                    case Mirror:
                        stationFunc = SettingsBase.StationFunc.Mirror;
                        break;
                    case Automatic_work_loan_distribution:
                        stationFunc = SettingsBase.StationFunc.Workload;
                        break;
                    case Duplicate_station:
                        stationFunc = SettingsBase.StationFunc.Duplicate;
                        break;
                    case Order_Queue_Display:
                        stationFunc = SettingsBase.StationFunc.Queue;
                        break;
                }
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
                    {
                        MyAdapter.this.getListData().get(i).setFunction(stationFunc);
                    }
                }
            }

            private void onInputSlaveStationIDDlgOK()
            {
                //m_relationBeforeShowSlaveDialog.setSlaveFunc(SettingsBase.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
                m_relationBeforeShowSlaveDialog.setSlaveFunc(m_slaveFunctionBeforeShowSlaveDialog.getFunction());
                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                String slaveID = editText.getText().toString();
                m_relationBeforeShowSlaveDialog.setSlaveStations(slaveID);
                changeStationToGiveFunctionAccordingSlaveFunction(slaveID,m_slaveFunctionBeforeShowSlaveDialog.getFunction(), false );
                SettingsBase.StationFunc sfunc = MyAdapter.this.getStationChainFunction(slaveID, "");

                if (( sfunc == SettingsBase.StationFunc.Expeditor  || sfunc == SettingsBase.StationFunc.Queue_Expo )&&
                        m_relationBeforeShowSlaveDialog.getSlaveFunc() == SettingsBase.SlaveFunc.Backup)
                    MyAdapter.this.addExpeditorToAll(slaveID);
                else
                    MyAdapter.this.removeExpeditorFromAll(slaveID);
                MyAdapter.this.notifyDataSetChanged();
            }
            private void inputSlaveStationID(Spinner slaveFuncSpinner, int position, KDSStationsRelation relation)
            {
                m_relationBeforeShowSlaveDialog = relation;
                m_lstBeforeShowDialog = MyAdapter.this.cloneListData();

                String strComfirm = PreferenceFragmentStations.this.getString(R.string.please_input_slave_station);
                strComfirm = strComfirm.replace("#", relation.getID());
                strComfirm = strComfirm.replace("$", ((MySlaveSpinnerArrayAdapter)slaveFuncSpinner.getAdapter()).getSlaveFunction(position).toString());
                //String strOK = KDSUIDialogBase.makeButtonText(view.getContext().getApplicationContext(), android.R.string.ok, SettingsBase.ID.Bumpbar_OK);
                String strOK = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.ok), KDSUIDialogBase.DialogEvent.OK);// SettingsBase.ID.Bumpbar_OK);
                //String strCancel = KDSUIDialogBase.makeButtonText(view.getContext().getApplicationContext(), android.R.string.cancel, SettingsBase.ID.Bumpbar_Cancel);
                String strCancel = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.cancel),KDSUIDialogBase.DialogEvent.Cancel);// SettingsBase.ID.Bumpbar_Cancel);
                m_viewBeforeShowSlaveDialog = LayoutInflater.from(KDSApplication.getContext()).inflate(R.layout.kdsui_dlg_input_slave, null);

                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
                        .setTitle(PreferenceFragmentStations.this.getString(R.string.input))
                        .setMessage(strComfirm)
                        .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        onInputSlaveStationIDDlgOK();
                                    }
                                }
                        )
                        .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                m_relationBeforeShowSlaveDialog.setSlaveFunc(SettingsBase.SlaveFunc.Unknown);
                                MyAdapter.this.notifyDataSetChanged();
                            }
                        })
                        .create();

                d.setView(m_viewBeforeShowSlaveDialog);
                d.setCancelable(false);
                d.setCanceledOnTouchOutside(false);

                m_dlgShowSlave = d;
                m_viewBeforeShowSlaveDialog.setFocusable(true);

                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                editText.setText(relation.getSlaveStations());


                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                    @Override
                    public void afterTextChanged(Editable s) {
                        Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
                        if (s.toString().isEmpty()) {
                            m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
                            if (btn != null) btn.setEnabled(false);
                        }
                        else
                        {
                            String slaveID = s.toString();
                            String strErr = validateInputSlave(m_relationBeforeShowSlaveDialog.getID(),m_slaveFunctionBeforeShowSlaveDialog.getFunction(), slaveID );
                            if (strErr.isEmpty()) {
                                m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
                                if (btn != null) btn.setEnabled(true);
                            }
                            else {
                                if (btn != null) btn.setEnabled(false);
                                m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.error_slave));
                            }
                        }
                    }
                });


                d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        //SettingsBase.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                        //SettingsBase.ID evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
                        KDSUIDialogBase.DialogEvent evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
                        //if (evID == SettingsBase.ID.Bumpbar_OK) {
                        if (evID == KDSUIDialogBase.DialogEvent.OK) {
                            m_relationBeforeShowSlaveDialog.setSlaveFunc(SettingsBase.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
                            EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                            m_relationBeforeShowSlaveDialog.setSlaveStations(editText.getText().toString());

                            dialog.dismiss();
                            return true;
                        } else if (evID == KDSUIDialogBase.DialogEvent.Cancel) {
                            dialog.cancel();
                            return true;
                        }
                        return false;
                    }
                });
                //d.getButton(AlertDialog.BUTTON_POSITIVE).setTag(relation.getTag());

                d.show();
                Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
                if (relation.getSlaveStations().isEmpty()) {
                    if (btn != null)
                        btn.setEnabled(false);
                }
                if (btn != null)
                {
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (onSlaveIDDlgOKClicked()) {
                                m_dlgShowSlave.hide();
                                onInputSlaveStationIDDlgOK();
                            }

                        }
                    });
                }
            }

            private String validateInputSlave(String stationID, SettingsBase.SlaveFunc slaveFunc, String slaveID)
            {
                String strErr = "";
                if (stationID.equals(slaveID))
                    return  PreferenceFragmentStations.this.getString(R.string.error_slave_myself);
                strErr = KDSStationsRelation.checkSlaveConflict(m_lstBeforeShowDialog,  stationID,slaveID,  slaveFunc);
                return strErr;
            }

            private boolean onSlaveIDDlgOKClicked()
            {
                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
                String slaveID = editText.getText().toString();
                if (!isExpoStation(slaveID)) return true;
                //expo allow expo as backup
                if (m_relationBeforeShowSlaveDialog.getFunction() == SettingsBase.StationFunc.Expeditor ||
                        m_relationBeforeShowSlaveDialog.getFunction() == SettingsBase.StationFunc.Queue_Expo )
                    return true;
                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
                        .setTitle(PreferenceFragmentStations.this.getString(R.string.error))
                        .setMessage(PreferenceFragmentStations.this.getString(R.string.relation_error_expo_as_slave))
                        .setPositiveButton(PreferenceFragmentStations.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }
                        )
                        .create();
                d.show();
                return false;
            }
            private boolean isExpoStation(String stationID)
            {
                if (stationID.isEmpty()) return false;
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
                    {
                        if (MyAdapter.this.getListData().get(i).getFunction() == SettingsBase.StationFunc.Expeditor ||
                                MyAdapter.this.getListData().get(i).getFunction() == SettingsBase.StationFunc.Queue_Expo)
                            return true;
                    }
                }
                return false;

            }

            private boolean isPrepStation(String stationID)
            {
                if (stationID.isEmpty()) return false;
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
                    {
                        if (MyAdapter.this.getListData().get(i).getFunction() == SettingsBase.StationFunc.Prep)
                            return true;
                    }
                }
                return false;

            }

            private boolean isStationFunc(String stationID, SettingsBase.StationFunc func)
            {
                if (stationID.isEmpty()) return false;
                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
                {
                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
                    {

                        if (MyAdapter.this.getListData().get(i).getFunction() == func)
                            return true;

                    }
                }
                return false;

            }
            public void onSlaveFunctionItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ///////////////////////////////////////////////////////////////////////////
                if (view == null) return;
                TextView v1 = (TextView) view;
                //v1.setTextColor(Color.BLACK);
                Spinner t = this.getSpinner();
                View viewRow = (View) t.getTag();
                int nposition = (int) viewRow.getTag();
                if (nposition >= MyAdapter.this.getListData().size())
                    return;
                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);

                SlaveFunction slaveFunctionValue = ((MySlaveSpinnerArrayAdapter) t.getAdapter()).getSlaveFunction(position);
                if (relation.getSlaveFunc() == slaveFunctionValue.getFunction()) return;
                String oldSlaveID = relation.getSlaveStations();
                relation.setSlaveFunc(slaveFunctionValue.getFunction()); //20170224

                m_npositionBeforeShowSlaveDialog = position;
                m_slaveFunctionBeforeShowSlaveDialog = slaveFunctionValue;

                if (slaveFunctionValue.getFunction() != SettingsBase.SlaveFunc.Unknown) {
                    inputSlaveStationID(t, position, relation);

                }
                else
                {

                    relation.setSlaveFunc(SettingsBase.SlaveFunc.Unknown);

                    relation.setSlaveStations("");
                    if (!oldSlaveID.isEmpty())
                    {
                        changeStationToGiveFunctionAccordingSlaveFunction(oldSlaveID,SettingsBase.SlaveFunc.Unknown, true );
                        if (relation.getFunction() == SettingsBase.StationFunc.Expeditor || relation.getFunction() == SettingsBase.StationFunc.Queue_Expo)
                            MyAdapter.this.removeExpeditorBackupFromExpoList(oldSlaveID);

                    }
                    MyAdapter.this.notifyDataSetChanged();

                }

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView v1 = (TextView) view;

                Spinner t = this.getSpinner();
                View viewRow = (View) t.getTag();
                int nposition = (int)viewRow.getTag();
                if (nposition >= MyAdapter.this.getListData().size())
                    return;
                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);

                if (t.getId() == R.id.spinnerFunc)
                {
                    onStationFunctionItemSelected2(parent, view, position, id);

                }
                else if (t.getId() == R.id.spinnerStatus)
                {
                    if (position == 0)
                        relation.setStatus(SettingsBase.StationStatus.Enabled);
                    else
                        relation.setStatus(SettingsBase.StationStatus.Disabled);
                }
                else if (t.getId() == R.id.spinnerSlaveFunc)
                {//slave function
                    onSlaveFunctionItemSelected(parent, view, position, id);
                }
                setImageEditingIcon((ImageView)viewRow.findViewById(R.id.imgEdit), relation);
                //PreferenceFragmentStations.this.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        }


        View m_viewEditing = null;
        private LayoutInflater mInflater;

        public List<KDSStationsRelation> m_listData; //KDSStationsRelation class array
        public List<KDSStationsRelation> m_listOriginal; //check changed.

        public View getEditingView()
        {
            return m_viewEditing;
        }
        public MyAdapter(Context context, List<KDSStationsRelation> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<KDSStationsRelation> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSStationsRelation> lst)
        {
            m_listData = lst;
            cloneToOriginalArray(); //backup it.
        }

        public List<KDSStationsRelation> cloneListData()
        {
            ArrayList<KDSStationsRelation> lst = new ArrayList<>();

            for (int i=0; i< m_listData.size(); i++)
            {
                KDSStationsRelation r = m_listData.get(i);
                KDSStationsRelation relation = new KDSStationsRelation();
                relation.copyFrom(r);
                lst.add(relation);
            }
            return lst;
        }

        private boolean isExpoAllowed(SettingsBase.StationFunc func)
        {
            if (func == SettingsBase.StationFunc.Expeditor ||
                    func == SettingsBase.StationFunc.Queue   ||
                    func == SettingsBase.StationFunc.TableTracker ||
                    func == SettingsBase.StationFunc.Queue_Expo)
                return false;
            return true;
        }
        /**
         * add this expo to all stations
         * @param expoID
         */
        public void addExpeditorToAll(String expoID)
        {

            boolean bExistedInExpo = false;
            for (int i=0; i< m_listData.size(); i++)
            {
                KDSStationsRelation r = m_listData.get(i);
                if ( !isExpoAllowed(r.getFunction()))
                    continue;

                String s = r.getExpStations();
                if (KDSStationsRelation.existedStation(s, expoID))
                    bExistedInExpo = true;
            }
            if (bExistedInExpo) //if this existed in any station, don't auto add it.
                return;

            for (int i=0; i< m_listData.size(); i++)
            {
                KDSStationsRelation r = m_listData.get(i);
                if ( !isExpoAllowed(r.getFunction()))
                    continue;
                SettingsBase.StationFunc sfunc =getStationChainFunction(r.getID(), "");
                if (sfunc == SettingsBase.StationFunc.Expeditor ||
                        sfunc == SettingsBase.StationFunc.Queue_Expo) {
                    r.setExpStations("");
                    continue;
                }
                r.addExpoStation(expoID);
            }
        }

        public void removeExpeditorFromAll(String expoID)
        {
            removeExpeditorChainFromAll(expoID);

        }

        public void removeExpeditorFromList(String expoID)
        {

            for (int i=0; i< m_listData.size(); i++)
            {
                KDSStationsRelation r = m_listData.get(i);
                if ( !isExpoAllowed(r.getFunction()))
                    continue;

                r.removeExpoStation(expoID);
            }
        }

        public void removeExpeditorBackupFromExpoList(String expoSlaveID)
        {
            // removeExpeditorChainFromAll(expoID);
            KDSStationsRelation r = getRelation(expoSlaveID);
            if (r == null) return;
            removeExpeditorFromList(expoSlaveID);
            for (int i=0; i< 1000; i++)
            {
                if (r == null) return;
                if (r.getSlaveStations().isEmpty()) return;
                removeExpeditorFromList(r.getSlaveStations());
                r =  getRelation(r.getSlaveStations());

            }

        }
        public void removeExpeditorChainFromAll(String expoID)
        {

            KDSStationsRelation expo =  getRelation(expoID);

            for (int i=0; i< m_listData.size(); i++)
            {
                KDSStationsRelation r = m_listData.get(i);
                KDSStationsRelation topRelation =  getStationChainPrimaryToppest(r.getID(), "");
                if (topRelation == null) continue;
                if (topRelation.getID() == expoID)
                    removeExpeditorFromList(r.getID());


            }
            removeExpeditorFromList(expoID);
        }
        /**
         * check if editing something.
         * @return
         */
        public boolean isChanged()
        {
            return isDifferent(m_listData);

        }

        /**
         * compare this new relation with showing
         * @param ar
         * @return
         */
        public boolean isDifferent(List<KDSStationsRelation> ar)
        {
            if (ar.size() != m_listOriginal.size())
                return true;
            for (int i=0; i< ar.size(); i++) {
                KDSStationsRelation r = ar.get(i);

                KDSStationsRelation relationOriginal = findStation(m_listOriginal, r.getID());
                if (relationOriginal == null)
                    return true;
                if  (!relationOriginal.isRelationEqual(r))
                    return true;

            }
            return false;
        }
        private void initSpinner(Context context, Spinner spinner, List<String> list)
        {



            MySpinnerArrayAdapter adapter = new MySpinnerArrayAdapter(context, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }


        ArrayList<StationFunction> m_stationFuncSpinnerListHaveExpo = new ArrayList<>();
        ArrayList<StationFunction> m_stationFuncSpinnerListNoExpo = new ArrayList<>();
        private void initStationfunctionSpinner2(Context context, Spinner spinner,KDSStationsRelation relation)
        {

            SpinnerAdapter oldAdapter =  spinner.getAdapter();
            if (oldAdapter instanceof MyStationFuncSpinnerAdapter)
            {
                if (isExpoExisted())
                {
                    if (oldAdapter.getCount() == SettingsBase.StationFunc.MAX_COUNT.ordinal()) return;
                }
                else
                {
                    //2.0.11, remove MAX_COUNT.ordinal()-1
                    if (oldAdapter.getCount() == SettingsBase.StationFunc.MAX_COUNT.ordinal()) return;
                }

            }
            MyStationFuncSpinnerAdapter adapter;
            ArrayList<StationFunction> list = new ArrayList<>();

            List<String> arFuncStrings = Arrays.asList(context.getResources().getStringArray(R.array.station_function));

            if (isExpoExisted()) {
                if (m_stationFuncSpinnerListHaveExpo.size()<=0) {
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Prep.ordinal()), SettingsBase.StationFunc.Prep));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Expeditor.ordinal()), SettingsBase.StationFunc.Expeditor));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Queue.ordinal()), SettingsBase.StationFunc.Queue));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Mirror.ordinal()), SettingsBase.StationFunc.Mirror));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Backup.ordinal()), SettingsBase.StationFunc.Backup));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Workload.ordinal()), SettingsBase.StationFunc.Workload));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Duplicate.ordinal()), SettingsBase.StationFunc.Duplicate));


                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.TableTracker.ordinal()), SettingsBase.StationFunc.TableTracker));
                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Queue_Expo.ordinal()), SettingsBase.StationFunc.Queue_Expo));
                }
                list = m_stationFuncSpinnerListHaveExpo;

            }
            else {
                if (m_stationFuncSpinnerListNoExpo.size() <=0) {
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Prep.ordinal()), SettingsBase.StationFunc.Prep));
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Expeditor.ordinal()), SettingsBase.StationFunc.Expeditor));
                    //2.0.11, allow queue in prep station
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Queue.ordinal()), SettingsBase.StationFunc.Queue));
                    //
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Mirror.ordinal()), SettingsBase.StationFunc.Mirror));
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Backup.ordinal()), SettingsBase.StationFunc.Backup));
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Workload.ordinal()), SettingsBase.StationFunc.Workload));
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Duplicate.ordinal()), SettingsBase.StationFunc.Duplicate));

                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.TableTracker.ordinal()), SettingsBase.StationFunc.TableTracker));
                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(SettingsBase.StationFunc.Queue_Expo.ordinal()), SettingsBase.StationFunc.Queue_Expo));
                }
                list = m_stationFuncSpinnerListNoExpo;
            }


            adapter = new MyStationFuncSpinnerAdapter(context, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setEnabled(list.size()>1);



        }

        public String getMyPrimaryStation(String slaveStationID)
        {
            for (int i=0; i<  this.getListData().size(); i++)
            {
                if (this.getListData().get(i).getSlaveStations().equals(slaveStationID))
                {

                    return  this.getListData().get(i).getID();

                }
            }
            return "";
        }

        public KDSStationsRelation getMyPrimaryRelation(String slaveStationID)
        {
            for (int i=0; i<  this.getListData().size(); i++)
            {
                if (this.getListData().get(i).getSlaveStations().equals(slaveStationID))
                {

                    return  this.getListData().get(i);

                }
            }
            return null;
        }

        public KDSStationsRelation getRelation(String stationID)
        {
            for (int i=0; i<  this.getListData().size(); i++)
            {
                if (this.getListData().get(i).getID().equals(stationID))
                {

                    return  this.getListData().get(i);

                }
            }
            return null;
        }
        /**
         * check expo, queue and prep
         * @param stationID
         * @return
         */
        public SettingsBase.StationFunc getStationChainFunction(String stationID, String whoUseMeAsSlave)
        {

            KDSStationsRelation r = getRelation(stationID);
            if (r == null) return  SettingsBase.StationFunc.Queue; //show nothing
            if (r.getFunction() != SettingsBase.StationFunc.Expeditor &&
                    r.getFunction() != SettingsBase.StationFunc.Prep &&
                    r.getFunction() != SettingsBase.StationFunc.Queue_Expo &&
                    r.getFunction() != SettingsBase.StationFunc.Queue) //add this line, in old version, it add queue as expo
            {
                String primary = getMyPrimaryStation(stationID);
                if (primary.isEmpty()) return SettingsBase.StationFunc.Queue;
                if (primary.equals(whoUseMeAsSlave)) return r.getFunction();
                return getStationChainFunction(primary, stationID);
            }
            else
                return r.getFunction();

        }

        public KDSStationsRelation getStationChainPrimaryToppest(String stationID, String whoUseMeAsPrimary)
        {

            KDSStationsRelation r = getRelation(stationID);
            if (r == null) return  null; //show nothing
            if (r.getFunction() != SettingsBase.StationFunc.Expeditor &&
                    r.getFunction() != SettingsBase.StationFunc.Prep &&
                    r.getFunction()!= SettingsBase.StationFunc.TableTracker &&
                    r.getFunction() != SettingsBase.StationFunc.Queue_Expo) {
                String primary = getMyPrimaryStation(stationID);
                if (primary.isEmpty()) return null;
                if (primary.equals(whoUseMeAsPrimary)) return r;
                return getStationChainPrimaryToppest(primary, stationID);
            }
            else
                return r;

        }

        public boolean isExpoExisted()
        {
            for (int i=0; i<  this.getListData().size(); i++)
            {
                if (this.getListData().get(i).getFunction() == SettingsBase.StationFunc.Expeditor ||
                        this.getListData().get(i).getFunction() == SettingsBase.StationFunc.Queue_Expo    )
                {

                    return true;

                }
            }
            return false;
        }

        public boolean isMoreThanOneTrackerStation(KDSStationsRelation trackerRelation)
        {
            for (int i=0; i<  this.getListData().size(); i++)
            {
                if (this.getListData().get(i).getFunction() == SettingsBase.StationFunc.TableTracker)
                {
                    if (this.getListData().get(i) != trackerRelation)
                        return true;

                }
            }
            return false;
        }
        //2.0.11, change it from 5 o 6, allow queue for prep station.
        final static int PREP_SLAVE_OPTIONS = 6;
        final static int EXPO_SLAVE_OPTIONS = 5; //kpp1-286, Duplicate and Mirror Expeditor. Change it from 3 to 5.

        final static int NO_SLAVE_OPTIONS = 1;
        final static int MIRROR_SLAVE_OPTIONS = 1;
        final static int WORKLOAD_SLAVE_OPTIONS = 1;
        final static int DUPLICATE_SLAVE_OPTIONS = 1;
        final static int BACKUP_SLAVE_OPTIONS = 2;
        final static int Queue_Expo_SLAVE_OPTIONS = 2;

        ArrayList<SlaveFunction> m_slaveFuncNormalSpinnerList = new ArrayList<>();
        ArrayList<SlaveFunction> m_slaveFuncExpoSpinnerList = new ArrayList<>();
        ArrayList<SlaveFunction> m_slaveFuncQueueExpoSpinnerList = new ArrayList<>();
        ArrayList<SlaveFunction> m_slaveFuncBackupSpinnerList = new ArrayList<>();
        ArrayList<SlaveFunction> m_slaveFuncOtherSpinnerList = new ArrayList<>();

        /**
         * according to the function to decide what slave supported.
         * @param context
         * @param spinner
         * @param stationFunction
         */
        private void initSlaveFunctionSpinner(Context context, Spinner spinner, SettingsBase.StationFunc stationFunction)
        {
            SpinnerAdapter oldAdapter =  spinner.getAdapter();
            if (oldAdapter instanceof MySlaveSpinnerArrayAdapter)
            {
                switch (stationFunction)
                {

                    case Prep:
                        if (oldAdapter.getCount() == PREP_SLAVE_OPTIONS) return;
                        break;
                    case Expeditor:

                        if (oldAdapter.getCount() == EXPO_SLAVE_OPTIONS) return;//no slave, backup,  queue
                        break;
                    case Queue:
                    case Mirror:
                    case Workload:
                    case Duplicate:
                    case TableTracker:
                        if (oldAdapter.getCount() == NO_SLAVE_OPTIONS) return;//no slave
                        break;
                    case Backup:
                        if (oldAdapter.getCount() == BACKUP_SLAVE_OPTIONS) return;//no slave, backup
                        break;
                    case Queue_Expo:
                        if (oldAdapter.getCount() == Queue_Expo_SLAVE_OPTIONS) return;//no slave, backup,  queue
                        break;
                }
            }
            MySlaveSpinnerArrayAdapter adapter;
            ArrayList<SlaveFunction> list = new ArrayList<>();

            List<String> arFuncStrings = Arrays.asList(context.getResources().getStringArray(R.array.slave_function));

            switch (stationFunction)
            {
                case Prep:
                    if (m_slaveFuncNormalSpinnerList.size()<=0) {
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Unknown.ordinal()), SettingsBase.SlaveFunc.Unknown));
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Backup.ordinal()), SettingsBase.SlaveFunc.Backup));
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Mirror.ordinal()), SettingsBase.SlaveFunc.Mirror));
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Automatic_work_loan_distribution.ordinal()), SettingsBase.SlaveFunc.Automatic_work_loan_distribution));
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Duplicate_station.ordinal()), SettingsBase.SlaveFunc.Duplicate_station));
                        //2.0.11, allow queue in prep station
                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Order_Queue_Display.ordinal()), SettingsBase.SlaveFunc.Order_Queue_Display));
                    }
                    list = m_slaveFuncNormalSpinnerList;
                    break;
                case Expeditor:
                    if (m_slaveFuncExpoSpinnerList.size() <=0) {
                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Unknown.ordinal()), SettingsBase.SlaveFunc.Unknown));
                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Backup.ordinal()), SettingsBase.SlaveFunc.Backup));
                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Order_Queue_Display.ordinal()), SettingsBase.SlaveFunc.Order_Queue_Display));
                        //kpp1-285, Duplicate and Mirror Expeditor.
                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Mirror.ordinal()), SettingsBase.SlaveFunc.Mirror));
                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Duplicate_station.ordinal()), SettingsBase.SlaveFunc.Duplicate_station));
                    }
                    list = m_slaveFuncExpoSpinnerList;
                    break;
                case Queue_Expo:
                    if (m_slaveFuncQueueExpoSpinnerList.size() <=0) {
                        m_slaveFuncQueueExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Unknown.ordinal()), SettingsBase.SlaveFunc.Unknown));
                        m_slaveFuncQueueExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Backup.ordinal()), SettingsBase.SlaveFunc.Backup));
                    }
                    list = m_slaveFuncQueueExpoSpinnerList;
                    break;
                case Queue:
                case Mirror:
                case Workload:
                case Duplicate:
                case TableTracker:
                    if (m_slaveFuncOtherSpinnerList.size() <=0) {
                        m_slaveFuncOtherSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Unknown.ordinal()), SettingsBase.SlaveFunc.Unknown));
                    }
                    list = m_slaveFuncOtherSpinnerList;
                    break;
                case Backup:
                    if (m_slaveFuncBackupSpinnerList.size() <=0) {
                        m_slaveFuncBackupSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Unknown.ordinal()), SettingsBase.SlaveFunc.Unknown));
                        m_slaveFuncBackupSpinnerList.add(new SlaveFunction(arFuncStrings.get(SettingsBase.SlaveFunc.Backup.ordinal()), SettingsBase.SlaveFunc.Backup));
                    }
                    list = m_slaveFuncBackupSpinnerList;
                    break;
            }
            adapter = new MySlaveSpinnerArrayAdapter(context, list);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            spinner.setEnabled(list.size()>1);
        }


        /**
         * check if has error in it.
         *
         *   prevent t
         * @return
         */
        public boolean isError()
        {

            for (int i=0; i< m_listData.size(); i++) {
                KDSStationsRelation r = m_listData.get(i);
                KDSStationsRelation.EditingState state = checkEditingState(r, false);
                if (state != KDSStationsRelation.EditingState.OK &&
                        state != KDSStationsRelation.EditingState.Changed)
                    return true;


            }
            return false;
        }

        public void reset()
        {
            for (int i=0; i< m_listData.size(); i++) {
                KDSStationsRelation r = m_listData.get(i);
                r.reset();


            }
            this.notifyDataSetChanged();
        }
        public void cloneToOriginalArray()
        {
            List<KDSStationsRelation> lst = m_listData;
            if (m_listOriginal == null)
                m_listOriginal = new ArrayList<>();
            m_listOriginal.clear();
            for (int i=0; i< lst.size(); i++)
            {
                KDSStationsRelation r = new KDSStationsRelation();
                r.copyFrom(lst.get(i));
                m_listOriginal.add(r);

            }
        }

        /**
         *
         * @param r
         * @param bShowErrorsMessage
         *  prevent the "isError" dead loop.
         * @return
         */
        private KDSStationsRelation.EditingState checkEditingState(KDSStationsRelation r, boolean bShowErrorsMessage)
        {
            if (r.getID().isEmpty()) return KDSStationsRelation.EditingState.Error_ID;
            KDSStationsRelation relationOriginal = findStation(m_listOriginal, r.getID());
            ArrayList<String> errors = new ArrayList<>();
            KDSStationsRelation.EditingState err = checkValidation(r, errors);
            if (errors.size()>0) {
                PreferenceFragmentStations.this.m_txtError.setText(errors.get(0));
            }
            else
            {
                if (bShowErrorsMessage)
                    if (!isError()) PreferenceFragmentStations.this.m_txtError.setText("");

            }
            if (err != KDSStationsRelation.EditingState.OK)
                return err;

            if (relationOriginal == null)
                return KDSStationsRelation.EditingState.Changed;

            if (relationOriginal.isRelationEqual(r)) {
                return KDSStationsRelation.EditingState.OK;
            } else {

                return KDSStationsRelation.EditingState.Changed;
            }

        }

        private KDSStationsRelation.EditingState checkValidation(KDSStationsRelation r, ArrayList<String> errorsMsg)
        {
            String myStationID = r.getID();

            //if (KDSGlobalVariables.getKDS().getStationsConnections().findActivedStationCountByID(myStationID)>1)
            if (m_kdsCallback.call_findActivedStationCountByID(myStationID)>1)
                return KDSStationsRelation.EditingState.Error_IP;

            String strErr = KDSStationsRelation.checkSlaveConflict(m_listData, myStationID,r.getSlaveStations(), r.getSlaveFunc());
            if (!strErr.isEmpty())
            {
                String s = PreferenceFragmentStations.this.getActivity().getApplicationContext().getString(R.string.error_relations_repeat_backup_mirror);// String.format("Stations: %s works as backup and mirror.", strErr);
                s = s.replace("#", strErr);
                if (errorsMsg != null)
                    errorsMsg.add(s);


                return KDSStationsRelation.EditingState.Error_Repeat_BackupMirror;
            }
            return KDSStationsRelation.EditingState.OK;
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


        public  void setSelectItem(int selectItem) {
            this.m_selectItem = selectItem;
        }
        public int getSelectedItem()
        {
            return m_selectItem;
        }
        private int  m_selectItem=-1;

        private void init_view_focus_event(View v, Object objTag)
        {
            v.setTag(objTag);
            v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {

                        PreferenceFragmentStations.this.onListItemClicked((View) (v.getTag()));
                        MyAdapter.this.m_viewEditing = v;

                    }
                }
            });



        }
        private void init_edittext_changed_event(EditText v)
        {

            v.addTextChangedListener(new CustomTextWatcher(v) {
                public void afterTextChanged(Editable s) {
                    //PreferenceFragmentStations.this.save();
                    EditText t = this.getEditText();
                    View viewRow = (View) t.getTag();
                    int nposition = (int) viewRow.getTag();
                    KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);
                    ViewHolder holder = (ViewHolder) relation.getTag();

                    //relation.setEditingState(KDSStationsRelation.EditingState.Changed);
                    if (t.getId() == R.id.txtStationID) {
                        relation.setID(s.toString());
                    }
                    else if (t.getId() == R.id.txtExpStations) {
                        relation.setExpStations(s.toString());
                    } else if (t.getId() == R.id.txtSlaveStations) {
                        relation.setSlaveStations(s.toString());
                    }

                    ImageView img = (ImageView) viewRow.findViewById(R.id.imgEdit);
                    setImageEditingIcon(img, relation);
                }
            });
        }

        public void setImageEditingIcon(ImageView img, KDSStationsRelation relation)
        {
            KDSStationsRelation.EditingState state = checkEditingState(relation, true);
            ViewHolder holder = (ViewHolder) relation.getTag();
            if (state == KDSStationsRelation.EditingState.Changed) {
                if (img != null)
                    img.setImageResource(R.drawable.edit_24px_16);

            } else if (state != KDSStationsRelation.EditingState.OK) {
                if (img != null)
                    img.setImageResource(R.drawable.delete_24px_32);
            } else {
                if (img != null)
                    img.setImageResource(0);
            }
        }

        private void init_view_click_event(View v, Object objTag)
        {
            v.setTag(objTag);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PreferenceFragmentStations.this.onListItemClicked((View) (v.getTag()));
                    MyAdapter.this.m_viewEditing = v;

                }
            });
        }

        private void buildSlaveFunctionSpinner(Spinner spinner, View convertView,KDSStationsRelation relation )
        {

            initSlaveFunctionSpinner(getActivity().getApplicationContext(), spinner,relation.getFunction());


            if (spinner.getOnItemSelectedListener() == null) {
                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));

            }

            boolean bChanged = false;
            SettingsBase.SlaveFunc oldSlaveFunc = relation.getSlaveFunc();
            int nIndex = ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).getIndex(relation.getSlaveFunc());
            spinner.setSelection(nIndex);
            //maybe the slave function was changed. While station function changed, the slave options is different, more or less than before.
            relation.setSlaveFunc(((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).getSlaveFunction(nIndex).getFunction());


            init_view_focus_event(spinner, convertView);
            bChanged = (oldSlaveFunc != relation.getSlaveFunc());


            if (bChanged) {
                spinner.setSelection(0); //no slave
                relation.setSlaveFunc(SettingsBase.SlaveFunc.Unknown);
                relation.setSlaveStations("");
                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
                return;
            }

            if (relation.getSlaveFunc() == SettingsBase.SlaveFunc.Unknown)
            {
                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).setTextColor(Color.GRAY);

            }
            else {
                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).resetTextColor();

            }

        }

        private void buildStationFunctionSpinner2(Spinner spinner, View convertView,KDSStationsRelation relation )
        {

            initStationfunctionSpinner2(getActivity().getApplicationContext(), spinner,relation);

            if (spinner.getOnItemSelectedListener() == null) {
                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));

            }

            boolean bChanged = false;
            SettingsBase.StationFunc oldStationFunc = relation.getFunction();
            int nIndex = ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).getIndex(relation.getFunction());
            spinner.setSelection(nIndex);
            //maybe the slave function was changed. While station function changed, the slave options is different, more or less than before.
            relation.setFunction( ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).getStationFunction(nIndex).getFunction());

            init_view_focus_event(spinner, convertView);
            bChanged = (oldStationFunc != relation.getFunction());

            if (bChanged) {
                spinner.setSelection(0); //no slave
                relation.setFunction(SettingsBase.StationFunc.Prep);
                relation.setSlaveFunc(SettingsBase.SlaveFunc.Unknown);
                relation.setSlaveStations("");

                ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).notifyDataSetChanged();
                return;
            }



        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            KDSStationsRelation r = m_listData.get(position);
            //boolean bLoadingData = false;
            ViewHolder viewHolder = null;
            // viewHolder =(ViewHolder) r.getTag();
            if ( r.getTag() == null) {
                viewHolder = new ViewHolder();
                r.setTag(viewHolder);
            }
            else
            {
                viewHolder =(ViewHolder) r.getTag();
            }

            boolean bNewView = false;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.kdsui_listitem_stations_setting, null);
                bNewView = true;
                //bLoadingData = true;
            }

            convertView.setTag(position);
            EditText txtStationID = ((EditText) convertView.findViewById(R.id.txtStationID));//
            viewHolder.m_txtStationID = txtStationID;
            txtStationID.setText(r.getID());
            init_view_focus_event(txtStationID, convertView);
            if (bNewView)
                init_edittext_changed_event(txtStationID);

            EditText txtExp = ((EditText) convertView.findViewById(R.id.txtExpStations));
            viewHolder.m_txtExp = txtExp;
            txtExp.setText(r.getExpStations());
            init_view_focus_event(txtExp, convertView);
            if (bNewView)
                init_edittext_changed_event(txtExp);

            //EditText txtSlave = ((EditText) convertView.findViewById(R.id.txtSlaveStations));
            TextView txtSlave = ((TextView) convertView.findViewById(R.id.txtSlaveStations));
            viewHolder.m_txtSlave = txtSlave;
            txtSlave.setText(r.getSlaveStations());
            init_view_focus_event(txtSlave, convertView);
            init_view_click_event(txtSlave, convertView);
            //init_edittext_changed_event(txtSlave);

            Spinner spinnerSlaveFunc = ((Spinner) convertView.findViewById(R.id.spinnerSlaveFunc));
            viewHolder.m_spinnerSlaveFunc = spinnerSlaveFunc;
            spinnerSlaveFunc.setTag(convertView);

            buildSlaveFunctionSpinner(spinnerSlaveFunc, convertView, r);


            ImageView imgEdit = ((ImageView) convertView.findViewById(R.id.imgEdit));
            viewHolder.m_viewImg = imgEdit;
            init_view_click_event(imgEdit, convertView);

            ImageView imgNetwork = ((ImageView) convertView.findViewById(R.id.imgNetwork));
            viewHolder.m_viewNetwork = imgNetwork;
            init_view_click_event(imgNetwork, convertView);
            refreshStationNetworkStatusIcon(convertView, r); //2.1.15.4


            Spinner spinner = ((Spinner) convertView.findViewById(R.id.spinnerFunc));//..setText(r.getFunctionString());
            viewHolder.m_spinnerFunc = spinner;
            spinner.setTag(convertView);

            buildStationFunctionSpinner2(spinner, convertView, r);
            //((MyStationFuncSpinnerAdapter)spinner.getAdapter()).notifyDataSetChanged();

            //
            spinner = ((Spinner) convertView.findViewById(R.id.spinnerStatus));//..setText(r.getFunctionString());
            viewHolder.m_spinnerStatus = spinner;
            spinner.setTag(convertView);

            if (bNewView)
                initSpinner(getActivity().getApplicationContext(), viewHolder.m_spinnerStatus, Arrays.asList( getActivity().getApplicationContext().getResources().getStringArray(R.array.station_status)));


            if (spinner.getOnItemSelectedListener() == null) {
                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));

            }

            if (r.getStatus() == SettingsBase.StationStatus.Enabled) {
                spinner.setSelection(0);
            } else {
                spinner.setSelection(1);
            }

            init_view_focus_event(spinner, convertView);
            setImageEditingIcon(imgEdit, r);


            //boolean bUnderLineLocalStation = (r.getMac().equals(KDSGlobalVariables.getKDS().getLocalMacAddress()));
            boolean bUnderLineLocalStation = (r.getMac().equals(m_kdsCallback.call_getLocalMacAddress()));
            //{

            TextPaint tp = viewHolder.m_txtStationID.getPaint();
            tp.setUnderlineText(bUnderLineLocalStation);
            tp.setFakeBoldText(bUnderLineLocalStation);

            //}
//            else
//            {
//                TextPaint tp = viewHolder.m_txtStationID.getPaint();
//                tp.setFakeBoldText(false);
//                tp.setUnderlineText(false);
//
//            }
            return convertView;

        }
    }

    private class SlaveFunction
    {
        String m_strDescription = "";
        SettingsBase.SlaveFunc m_slaveFunction = SettingsBase.SlaveFunc.Unknown;

        public SlaveFunction(String strDescription, SettingsBase.SlaveFunc func)
        {
            setDescription(strDescription);
            setFunction(func);
        }
        public void setDescription(String strDescription)
        {
            m_strDescription = strDescription;
        }
        public  String getDescription()
        {
            return m_strDescription;
        }
        public  void setFunction(SettingsBase.SlaveFunc func)
        {
            m_slaveFunction = func;
        }
        public SettingsBase.SlaveFunc getFunction()
        {
            return m_slaveFunction;
        }

        @Override
        public String toString()
        {
            return m_strDescription;
        }
    }

    public class MySlaveSpinnerArrayAdapter extends ArrayAdapter<SlaveFunction> {
        private Context mContext;
        private List<SlaveFunction> mStringArray;
        private int m_textColor = Color.BLACK;

        public MySlaveSpinnerArrayAdapter(Context context, List<SlaveFunction> stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray=stringArray;
        }

        public void setTextColor(int nColor)
        {
            m_textColor = nColor;
        }
        public void resetTextColor()
        {
            m_textColor = Color.BLACK;
        }

        public SlaveFunction getSlaveFunction(int nPosition)
        {
            if (nPosition>=mStringArray.size())
                return mStringArray.get(0);
            return mStringArray.get(nPosition);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
                convertView.setBackgroundColor(Color.WHITE);
            }


            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray.get(position).toString());
            tv.setTextColor(Color.BLACK);

            return convertView;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            //此处text1是Spinner默认的用来显示文字的TextView
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray.get(position).toString());

            if (mStringArray.get(position).getFunction() == SettingsBase.SlaveFunc.Unknown)
                tv.setTextColor(Color.GRAY);
            else
                tv.setTextColor(Color.BLACK);

            tv.setGravity(Gravity.CENTER);
            return convertView;
        }
        public int getIndex( SettingsBase.SlaveFunc func)
        {
            for (int i=0; i< mStringArray.size(); i++)
            {
                if (mStringArray.get(i).getFunction() == func)
                    return i;
            }
            return 0;
        }

    }
    private class StationFunction
    {
        String m_strDescription = "";
        SettingsBase.StationFunc m_stationFunc = SettingsBase.StationFunc.Prep;

        public StationFunction(String strDescription, SettingsBase.StationFunc func)
        {
            setDescription(strDescription);
            setFunction(func);
        }
        public void setDescription(String strDescription)
        {
            m_strDescription = strDescription;
        }
        public  String getDescription()
        {
            return m_strDescription;
        }
        public  void setFunction(SettingsBase.StationFunc func)
        {
            m_stationFunc = func;
        }
        public SettingsBase.StationFunc getFunction()
        {
            return m_stationFunc;
        }

        @Override
        public String toString()
        {
            return m_strDescription;
        }
    }


    public class MyStationFuncSpinnerAdapter extends ArrayAdapter<StationFunction> {
        private Context mContext;
        private List<StationFunction> mStringArray;
        private int m_textColor = Color.BLACK;

        public MyStationFuncSpinnerAdapter(Context context, List<StationFunction> stringArray) {
            super(context, android.R.layout.simple_spinner_item, stringArray);
            mContext = context;
            mStringArray=stringArray;
        }

        public void setTextColor(int nColor)
        {
            m_textColor = nColor;
        }
        public void resetTextColor()
        {
            m_textColor = Color.BLACK;
        }

        public StationFunction getStationFunction(int nPosition)
        {
            if (nPosition>=mStringArray.size())
                return mStringArray.get(0);
            return mStringArray.get(nPosition);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //修改Spinner展开后的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
                convertView.setBackgroundColor(Color.WHITE);
            }

            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText(mStringArray.get(position).toString());

            tv.setTextColor(Color.BLACK);

            return convertView;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // 修改Spinner选择后结果的字体颜色
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }


            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);

            SettingsBase.StationFunc func = mStringArray.get(position).getFunction();

            int relationPosition = -1;
            if (((View)parent.getParent()).getTag()!= null) {
                relationPosition = (int)((View)parent.getParent()).getTag();

            }
            String text = mStringArray.get(position).getDescription();//.toString();
            switch (func)
            {

                case Prep:
                case Expeditor:
                case TableTracker:
                    break;

                case Queue:
                case Mirror:
                case Backup:
                case Workload:
                case Duplicate:
                    if (relationPosition >=0 )
                    {
                        KDSStationsRelation relation = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getListData().get(relationPosition);
                        KDSStationsRelation relationPrimary =((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getMyPrimaryRelation(relation.getID());
                        //String primary = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getMyPrimaryStation(relation.getID());
                        if (text.indexOf(">")<0)
                        {
                            // if (!primary.isEmpty()) {
                            if (relationPrimary != null){
                                //text = "#" +primary + "'s " + text;
                                //text += " [" + primary + "]";
                                if (func == SettingsBase.StationFunc.Backup) {
                                    SettingsBase.StationFunc primaryFunc = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getStationChainFunction(relationPrimary.getID(), "");

                                    if (primaryFunc == SettingsBase.StationFunc.Expeditor ||
                                            primaryFunc == SettingsBase.StationFunc.Queue_Expo)
                                        text = this.getContext().getString(R.string.expo_backup);//"Expo/Backup";
                                    else if (primaryFunc == SettingsBase.StationFunc.Prep)
                                        text = this.getContext().getString(R.string.prep_backup);// "Prep/Backup";
                                }
                                text += " -> " + relationPrimary.getID() ;

                            }
                        }
                    }
                    break;


            }

            tv.setText(text);
            tv.setTextColor(m_textColor);

            tv.setGravity(Gravity.CENTER);

            return convertView;
        }
        public int getIndex( SettingsBase.StationFunc func)
        {
            for (int i=0; i< mStringArray.size(); i++)
            {
                if (mStringArray.get(i).getFunction() == func)
                    return i;
            }
            return 0;
        }

    }

    public void onRefreshSummary(int userID){}//KDSUser.USER userID);
    public void onRefreshView(int userID, KDSDataOrders orders, KDSBase.RefreshViewParam nParam){}
    public void onShowStationStateMessage(String stationID, int nState){}
    public void onShowMessage(String message){}
    public Object onKDSEvent(KDSBase.KDSEventType evt, ArrayList<Object> arParams){return null;}
}

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        KDSGlobalVariables.getKDS().setEventReceiver(this);
//
//    }
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState)
//    {
//        super.onCreateView(inflater, container, savedInstanceState);
//        View view =  inflater.inflate(R.layout.activity_kdsuistations_config, container, false);
//        view.setBackgroundColor(this.getResources().getColor(R.color.settings_page_bg));
//        init_variables(view);
//        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
//        return view;
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        KDSGlobalVariables.getKDS().removeEventReceiver(this);
//    }
//    public void onResume()
//    {
//        super.onResume();
//        m_stationsRelations = this;
//        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
//        //m_timer.setReceiver(this);
//        try {
//            m_timer = new KDSTimer();
//            m_timer.start(this.getActivity(), this, 1000);
//        }catch (Exception e)
//        {
//
//            KDSLog.e(TAG, KDSLog._FUNCLINE_() , e);
//            //KDSLog.e(TAG, KDSUtil.error( e));
//        }
//    }
//
//    /**
//     *
//     * @param keyCode
//     * @param event
//     * @return
//     *    true: handle it
//     *    false; pass
//     */
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK)
//            if (((MyAdapter)m_lstStations.getAdapter()).isChanged()) {
//                showChangedAlertDialog();
//                return true;
//            }
//        return false;
//    }
//
//
//    public void showChangedAlertDialog()
//    {
//        m_contextApp = this.getActivity().getApplicationContext();
//        String strOK = KDSUIDialogBase.makeButtonText(this.getActivity().getApplicationContext(),R.string.ok, SettingsBase.ID.Bumpbar_OK );
//        String strCancel = KDSUIDialogBase.makeButtonText(this.getActivity().getApplicationContext(),R.string.cancel, SettingsBase.ID.Bumpbar_Cancel );
//
//        AlertDialog d = new AlertDialog.Builder(this.getActivity())
//                .setTitle(this.getString(R.string.question))
//                .setMessage(this.getString(R.string.confirm_broadcast_relations_changes))
//                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                PreferenceFragmentStations.this.broadcastUpdateAfterPause();
//
//                            }
//                        }
//                )
//                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        PreferenceFragmentStations.this. reloadRelations();
//                    }
//                })
//                .create();
//        d.setCancelable(false);//.setFinishOnTouchOutside(false);
//        d.setCanceledOnTouchOutside(false);
//        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//
//                if (evID == KDSSettings.ID.Bumpbar_OK) {
//                    dialog.dismiss();
//                    PreferenceFragmentStations.this.broadcastUpdateAfterPause();
//                    return true;
//                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
//                    dialog.cancel();
//                    return true;
//                }
//                return false;
//            }
//        });
//        d.show();
//    }
//
//    Context m_contextApp = null;
//    public void onPause()
//    {
//        super.onPause();
//        m_stationsRelations = null;
//        m_contextApp = this.getActivity().getApplicationContext();
//        m_timer.stop();
//
//        if (((MyAdapter)m_lstStations.getAdapter()).isError()) { //if error show pop
//            KDSUIDialogBase d = new KDSUIDialogBase();
//            d.createInformationDialog(this.getActivity(),this.getString(R.string.error), this.getString(R.string.confirm_fix_errors), false );
//            d.show();
//            return;
//        }
//        if (((MyAdapter)m_lstStations.getAdapter()).isChanged()) {
//            showChangedAlertDialog();
//        }
//
//        KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(null);
//    }
//
//    public void onListItemClicked(View viewRow)
//    {
//        if (m_lstStations.getTag() != null) {
//            View v = (View) m_lstStations.getTag();
//            v.setBackgroundColor(Color.TRANSPARENT);
//            v.invalidate();
//        }
//        View viewFocused = (((MyAdapter) m_lstStations.getAdapter())).getEditingView();
//        if (viewFocused instanceof EditText)
//        {
//            if (viewFocused.getTag() != viewRow)
//            {
//                ((EditText)viewFocused).clearFocus();
//            }
//        }
//
//
//
//        ((MyAdapter) m_lstStations.getAdapter()).setSelectItem((int) (viewRow.getTag()));
//        m_lstStations.setTag(viewRow);
//        viewRow.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
//        //((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//    }
//
//    public int getSelectedItem()
//    {
//        return ((MyAdapter) m_lstStations.getAdapter()).getSelectedItem();
//    }
//    public void onNew(View v)
//    {
//        ((MyAdapter) m_lstStations.getAdapter()).getListData().add(new KDSStationsRelation());
//        if (m_lstStations.getTag() != null) {
//            View vRow = (View) m_lstStations.getTag();
//            vRow.setBackgroundColor(Color.TRANSPARENT);
//
//        }
//        m_lstStations.setTag(null);
//        ((MyAdapter) m_lstStations.getAdapter()).setSelectItem(-1);
//        ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//
//    }
//    public void onKDSDialogCancel(KDSUIDialogBase dialog)
//    {
//
//    }
//    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
//    {
//        if (dialog instanceof KDSUIIPSearchDialog)
//        {
//            KDSGlobalVariables.getKDS().setStationAnnounceEventsReceiver(this);
//            ArrayList<String> ar =(ArrayList<String>) ((KDSUIIPSearchDialog)dialog).getResult();
//            if (ar.size() <=0) return;
//            this.retrieveRelationFrom(ar.get(0));
//        }
//        else if (dialog instanceof KDSUIDialogConfirm)
//        { //confirm remmove
//            KDSStationsRelation r = (KDSStationsRelation) obj;
//            ((MyAdapter) m_lstStations.getAdapter()).getListData().remove(r);
//            PreferenceFragmentStations.this.save();
//            ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//        }
//    }
//    public void onDel(View v)
//    {
//        int nindex = ((MyAdapter) m_lstStations.getAdapter()).getSelectedItem();
//        if (nindex <0) return;
//        String s = this.getString(R.string.confirm_remove_relation_station);
//        KDSStationsRelation r = ((MyAdapter) m_lstStations.getAdapter()).getListData().get(nindex);
//        s += r.getID();
//        KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(this.getActivity(), s, this);
//        dlg.setTag(r);
//        dlg.show();
//    }
//
//    public void onShowStationID(View v)
//    {
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastShowStationID();
//                return null;
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
//    }
//
//    public void onRetrieveRelations(View v)
//    {
//        KDSUIIPSearchDialog dlg = new KDSUIIPSearchDialog(this.getActivity(), KDSUIIPSearchDialog.IPSelectionMode.Single, this, "");
//        dlg.setTitle(this.getString(R.string.select_retrieve_station));
//        dlg.setSelf(false);
//        dlg.show();
//    }
//    public void onResetRelations(View v)
//    {
//        ((MyAdapter) m_lstStations.getAdapter()).reset();
//    }
//
//    /**
//     *
//     * @param stationInfo
//     * stationID:ip:port
//     */
//    public void retrieveRelationFrom( String stationInfo)
//    {
//        KDSStationIP station = KDSStationActived.parseString(stationInfo);
//        String id = station.getID();
//        Object[] ar = new Object[]{id};
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                String id =(String) params[0];
//                KDSGlobalVariables.getKDS().udpAskRelations(id);
//                return null;
//            }
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
//    }
//
//    public void broadcastUpdate()
//    {
//
//        this.save();
//        String s = KDSSettings.loadStationsRelationString(this.getActivity().getApplicationContext());
//        Object[] ar = new Object[]{s};
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                String strData =(String) params[0];
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastRelations(strData);
//                return null;
//            }
//
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
//    }
//
//    public void broadcastUpdateAfterPause()
//    {
//
//        KDSStationsRelation.save(m_contextApp, (ArrayList) ((MyAdapter) (m_lstStations.getAdapter())).getListData());
//
//        String s = KDSSettings.loadStationsRelationString(m_contextApp);
//        Object[] ar = new Object[]{s};
//
//        AsyncTask task = new AsyncTask() {
//            @Override
//            protected Object doInBackground(Object[] params) {
//                String strData =(String) params[0];
//                KDSGlobalVariables.getKDS().getBroadcaster().broadcastRelations(strData);
//                return null;
//            }
//
//        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ar);
//        this.reloadRelations();
//    }
//
//    public void onBroadcastUpdate(View v)
//    {
//
//        if (((MyAdapter)m_lstStations.getAdapter()).isError())
//        {
//            KDSUIDialogBase d = new KDSUIDialogBase();
//            d.createInformationDialog(this.getActivity(),this.getString(R.string.confirm), this.getString(R.string.confirm_fix_errors), false );
//            d.show();
//            return ;
//        }
//
//        String strOK = KDSUIDialogBase.makeButtonText(this.getActivity().getApplicationContext(),R.string.ok, KDSSettings.ID.Bumpbar_OK );
//        String strCancel = KDSUIDialogBase.makeButtonText(this.getActivity().getApplicationContext(),R.string.cancel, KDSSettings.ID.Bumpbar_Cancel );
//
//        AlertDialog d = new AlertDialog.Builder(this.getActivity())
//                .setTitle(this.getString(R.string.confirm))
//                .setMessage(this.getString(R.string.confirm_broadcast_relations))
//                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                PreferenceFragmentStations.this.broadcastUpdate();
//                            }
//                        }
//                )
//                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                    }
//                })
//                .create();
//        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
//            @Override
//            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//
//                if (evID == KDSSettings.ID.Bumpbar_OK)
//                {
//                    dialog.dismiss();
//                    PreferenceFragmentStations.this.broadcastUpdate();
//                    return true;
//                }
//                else if (evID == KDSSettings.ID.Bumpbar_Cancel)
//                {
//                    dialog.cancel();
//                    return true;
//                }
//                return false;
//            }
//        });
//        d.show();
//    }
//    protected KDSStationsRelation findStationByID(String stationID)
//    {
//        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
//        for (int i=0; i< ncount; i++) {
//            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getID().equals(stationID))
//            {
//                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);
//
//            }
//        }
//        return null;
//    }
//
//    protected KDSStationsRelation findStationByMac(String mac)
//    {
//        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
//        for (int i=0; i< ncount; i++) {
//            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getMac().equals(mac))
//            {
//                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);
//
//            }
//        }
//        return null;
//    }
//
//    protected KDSStationsRelation findStationByIP(String stationIP)
//    {
//        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
//        for (int i=0; i< ncount; i++) {
//            if ( ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i).getIP().equals(stationIP))
//            {
//                return ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);
//
//            }
//        }
//        return null;
//    }
//
//    KDSTimer m_timer = new KDSTimer();
//    public void onTime()
//    {
//        refreshNetworkStatusIcon();
//    }
//    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
//    {
//
//
//        KDSStationsRelation r = findStationByMac(stationReceived.getMac());
//        //update data
//        if ( r == null ) {
//            //check if we have a manually added station.
//            KDSStationsRelation stationManual = findStationByID(stationReceived.getID());
//            if (stationManual == null) {
//                r = new KDSStationsRelation();
//                r.setID(stationReceived.getID());
//                r.setIP(stationReceived.getIP());
//                r.setPort(stationReceived.getPort());
//                r.setMac(stationReceived.getMac());
//                ((MyAdapter) m_lstStations.getAdapter()).getListData().add(r);
//                ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//            }
//            else
//            {
//                if (stationManual.getMac().isEmpty())
//                {
//                    stationManual.setMac(stationReceived.getMac());
//                    stationManual.setIP(stationReceived.getIP());
//                    stationManual.setPort(stationReceived.getPort());
//                }
//
//            }
//        }
//        else
//        {
//            boolean bchanged = false;
//            if (!r.getIP().equals(stationReceived.getIP())) {
//                bchanged = true;
//                r.setIP(stationReceived.getIP());
//            }
//            if (!r.getPort().equals(stationReceived.getPort())) {
//                bchanged = true;
//                r.setPort(stationReceived.getPort());
//            }
//            if (!r.getID().equals(stationReceived.getID())) {
//                bchanged = true;
//                r.setID(stationReceived.getID());
//            }
//            if (bchanged)
//                ((MyAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();
//        }
//
//    }
//
//    public void refreshNetworkStatusIcon()
//    {
//        int ncount = ((MyAdapter) m_lstStations.getAdapter()).getListData().size();
//        for (int i=0; i< ncount; i++) {
//            KDSStationsRelation r =(KDSStationsRelation) ((MyAdapter) m_lstStations.getAdapter()).getListData().get(i);
//
//            // MyAdapter.ViewHolder holder =( MyAdapter.ViewHolder) (r.getTag());
//            View view = m_lstStations.getChildAt(i);
//            if (view == null) continue;
//            ImageView img = (ImageView) view.findViewById(R.id.imgNetwork);
//            //if (holder == null) continue;
//            boolean bonline = true;
//            if (r.getMac().equals(KDSGlobalVariables.getKDS().getLocalMacAddress()))
//            {
//                bonline = KDSSocketManager.isNetworkActived(this.getActivity().getApplicationContext());
//            }
//            else
//            {
//                bonline = (KDSGlobalVariables.getKDS().getStationsConnections().findActivedStationByID(r.getID()) != null);
//
//            }
//            if (bonline)
//            {
//                if (img != null) {
//                    img.setImageResource(R.drawable.online);
//                    img.invalidate();
//                }
//            }
//            else
//            {
//                if (img != null) {
//                    img.setImageResource(R.drawable.offline);
//                    img.invalidate();
//                }
//            }
//        }
//
//    }
//
//    KDSStationsRelation findStation(List<KDSStationsRelation> lst, String stationID)
//    {
//        int ncount = lst.size();
//        for (int i=0; i< ncount; i++)
//        {
//            if (lst.get(i).getID().equals(stationID))
//                return lst.get(i);
//        }
//        return null;
//    }
//
//
//
//    protected void init_variables(View view) {
//
//        m_txtError = (TextView)view.findViewById(R.id.txtError);
//        m_lstStations = (ListView)view.findViewById(R.id.lstStations);
//        List  lst =  new ArrayList<KDSStationsRelation>();
//        MyAdapter adapter = new MyAdapter(this.getActivity().getApplicationContext(), lst);
//        m_lstStations.setAdapter(adapter);
//        m_lstStations.setEnabled(true);
//
//        Button btnNew =  (Button)view.findViewById(R.id.btnNew);
//        btnNew.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onNew(v);
//            }
//        });
//
//        Button btnDel =  (Button)view.findViewById(R.id.btnDel);
//        btnDel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onDel(v);
//            }
//        });
//
//        Button btnUpdate =  (Button)view.findViewById(R.id.btnUpdate);
//        btnUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onBroadcastUpdate(v);
//            }
//        });
//
//
//        Button btnShowID =(Button)view.findViewById(R.id.btnShowID);
//        btnShowID.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onShowStationID(v);
//            }
//        });
//
//        Button btnRetrieve =(Button)view.findViewById(R.id.btnRetrieve);
//        btnRetrieve.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onRetrieveRelations(v);
//            }
//        });
//
//        Button btnReset =(Button)view.findViewById(R.id.btnReset);
//        btnReset.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                PreferenceFragmentStations.this.onResetRelations(v);
//            }
//        });
//
//        load();
//        adapter.notifyDataSetChanged();
//
//    }
//
//
//
//    public void save()
//    {
//
//        // if (!saveScreenDataToBuffer()) return;
//        KDSStationsRelation.save(this.getActivity().getApplicationContext(), (ArrayList) ((MyAdapter) (m_lstStations.getAdapter())).getListData());
//        List<KDSStationsRelation> ar =((MyAdapter)(m_lstStations.getAdapter() )).getListData();
//        ((MyAdapter)(m_lstStations.getAdapter() )).cloneToOriginalArray();
//        ((MyAdapter)(m_lstStations.getAdapter() )).notifyDataSetChanged();
//
//    }
//    public void load()
//    {
//        if (this == null) return;
//        if (this.getActivity() == null) return;
//        if (this.getActivity().getApplicationContext() == null) return;
//        ArrayList<KDSStationsRelation> ar =  KDSStationsRelation.loadStationsRelation(this.getActivity().getApplicationContext());
//        sortStations(ar);
//        ((MyAdapter)(m_lstStations.getAdapter() )).setListData(ar);
//
//    }
//
//    private void sortStations(ArrayList<KDSStationsRelation> arStationsRelation)
//    {
//        if (arStationsRelation.size() <=1 )
//            return;
//        Collections.sort(arStationsRelation, new Comparator() {
//                    @Override
//                    public int compare(Object o1, Object o2) {
//                        KDSStationsRelation c1 = (KDSStationsRelation) o1;
//                        KDSStationsRelation c2 = (KDSStationsRelation) o2;
//                        String name1 = c1.getID();//.makeDurationString();
//                        String pre = "000000000000000000000";
//                        name1 = pre + name1;
//                        name1 = name1.substring(name1.length()-20);
//                        String name2 = c2.getID();
//                        name2 = pre + name2;
//                        name2 = name2.substring(name2.length()-20);
//                        return name1.compareTo(name2);
//                    }
//                }
//        );
//    }
//
//
//
//
//    public void reloadRelations()
//    {
//        load();
//        ( (MyAdapter)(m_lstStations.getAdapter())).notifyDataSetChanged();
//    }
//
//    /**
//     * listview client [edit] popup dialog
//     */
//
//    private class MyAdapter extends BaseAdapter {
//
//        public class ViewHolder{
//            EditText m_txtStationID = null;
//            // EditText m_txtIP = null;
//            //EditText m_txtPort = null;
//            Spinner m_spinnerFunc = null;
//            EditText m_txtExp = null;
//            TextView m_txtSlave = null;
//            Spinner m_spinnerSlaveFunc = null;
//            Spinner m_spinnerStatus = null;
//            ImageView m_viewImg = null;
//            ImageView m_viewNetwork = null;
//
//        }
//
//
//        private class CustomTextWatcher implements TextWatcher {
//            private EditText mEditText;
//
//            public CustomTextWatcher(EditText e) {
//                mEditText = e;
//            }
//
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            public void afterTextChanged(Editable s) {
//            }
//            public EditText getEditText()
//            {
//                return mEditText;
//            }
//        }
//
//        /**
//         * while the spinner clicked in relation table, this class will work!
//         */
//        private class CustomSpinnerItemSelectedListener implements  AdapterView.OnItemSelectedListener
//        {
//            private Spinner m_spinner = null;
//
//            public CustomSpinnerItemSelectedListener(Spinner e) {
//                m_spinner = e;
//            }
//
//            public Spinner getSpinner()
//            {
//                return m_spinner;
//            }
//
//            KDSStationsRelation m_relationBeforeShowSlaveDialog = null;
//            View m_viewBeforeShowSlaveDialog = null;
//            int m_npositionBeforeShowSlaveDialog = 0;
//            SlaveFunction m_slaveFunctionBeforeShowSlaveDialog = null;
//            AlertDialog m_dlgShowSlave = null;
//
//            List<KDSStationsRelation> m_lstBeforeShowDialog = null;
//
//            private void confirmRemoveExpo2(KDSStationsRelation relation)
//            {
//                String expoID = relation.getID();
//
//                String strComfirm = PreferenceFragmentStations.this.getString(R.string.remove_relations_expo);
//                strComfirm = strComfirm.replace("#", "#" + expoID);
//
//                MyAdapter.this.removeExpeditorFromAll(expoID);
//                MyAdapter.this.notifyDataSetChanged();
//
//                int duration = Toast.LENGTH_LONG;
//                Toast t = Toast.makeText(KDSApplication.getContext(), strComfirm, duration);
//                t.show();
//
//            }
//
//            /**
//             * change primary slave function description according to new slave function choose it as primary.
//             * @param primaryStationID
//             * @param slaveStationID
//             * @param slaveStationFunc
//             */
//            private void changeStationSlaveFunctionAccordingItsSlaveStatioin(String primaryStationID, String slaveStationID, KDSSettings.StationFunc slaveStationFunc)
//            {
//                KDSStationsRelation primaryRelation = getStationRelation(primaryStationID);
//                if (primaryRelation == null) return;
//                KDSSettings.SlaveFunc slaveFunc = KDSSettings.SlaveFunc.Unknown;
//                switch (slaveStationFunc)
//                {
//                    case Normal:
//                        slaveFunc =  KDSSettings.SlaveFunc.Unknown;
//                        break;
//                    case Expeditor:
//                    case Queue_Expo:
//                        slaveFunc =  KDSSettings.SlaveFunc.Unknown;
//                        break;
//                    case Queue:
//                        slaveFunc =  KDSSettings.SlaveFunc.Order_Queue_Display;
//                        //2.0.11, allow prep as primary of queue
//                        if (primaryRelation.getFunction() != KDSSettings.StationFunc.Expeditor &&
//                                primaryRelation.getFunction() != SettingsBase.StationFunc.Normal)
//                            return;
//                        break;
//                    case Mirror:
//                        slaveFunc =  KDSSettings.SlaveFunc.Mirror;
//                        if (primaryRelation.getFunction() != KDSSettings.StationFunc.Normal)
//                                return;
//                        break;
//                    case Backup:
//                        slaveFunc =  KDSSettings.SlaveFunc.Backup;
//                        if (primaryRelation.getFunction() != KDSSettings.StationFunc.Normal
//                           &&primaryRelation.getFunction() != KDSSettings.StationFunc.Expeditor
//                                &&primaryRelation.getFunction() != KDSSettings.StationFunc.Backup &&
//                                primaryRelation.getFunction() != KDSSettings.StationFunc.Queue_Expo)
//
//                            return;
//                        break;
//                    case Workload:
//                        slaveFunc =  KDSSettings.SlaveFunc.Automatic_work_loan_distribution;
//                        if (primaryRelation.getFunction() != KDSSettings.StationFunc.Normal )
//                            return;
//                        break;
//                    case Duplicate:
//                        slaveFunc =  KDSSettings.SlaveFunc.Duplicate_station;
//                        if (primaryRelation.getFunction() != KDSSettings.StationFunc.Normal )
//                            return;
//                        break;
//                }
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(primaryStationID))
//                    {
//                        MyAdapter.this.getListData().get(i).setSlaveFunc(slaveFunc);
//                        MyAdapter.this.getListData().get(i).setSlaveStations(slaveStationID);
//                    }
//                }
//            }
//
//            private String getMyPrimaryStation(String slaveStationID)
//            {
//                return MyAdapter.this.getMyPrimaryStation(slaveStationID);
//
//            }
//
//            private KDSStationsRelation getStationRelation(String stationID)
//            {
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
//                    {
//
//                        return  MyAdapter.this.getListData().get(i);
//
//                    }
//                }
//                return null;
//            }
//
//            private void onInputPrimaryIDDlgOK()
//            {
//
//                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                String primaryID = editText.getText().toString();
//                //m_relationBeforeShowSlaveDialog.setSlaveStations(slaveID);
//                changeStationSlaveFunctionAccordingItsSlaveStatioin(primaryID,m_relationBeforeShowSlaveDialog.getID(), m_relationBeforeShowSlaveDialog.getFunction() );
//
//                KDSSettings.StationFunc sfunc = MyAdapter.this.getStationChainFunction(m_relationBeforeShowSlaveDialog.getID(), "");
//                if ( sfunc == KDSSettings.StationFunc.Expeditor ||
//                        sfunc == KDSSettings.StationFunc.Queue_Expo)
//                    MyAdapter.this.addExpeditorToAll(m_relationBeforeShowSlaveDialog.getID());
//                else
//                    MyAdapter.this.removeExpeditorFromAll(m_relationBeforeShowSlaveDialog.getID());
//
//                MyAdapter.this.notifyDataSetChanged();
//            }
//
//            /**
//             * input the slave station's primary station ID.
//             *  e.g: the queue station need a expo/prep station as its primary.
//             * @param stationFuncSpinner
//             * @param position
//             * @param relation
//             */
//            private void inputPrimaryStationID(Spinner stationFuncSpinner, int position, KDSStationsRelation relation)
//            {
//                m_relationBeforeShowSlaveDialog = relation;
//                String strComfirm = PreferenceFragmentStations.this.getString(R.string.please_input_primary_station);
//                strComfirm = strComfirm.replace("#", relation.getID());
//
//
//                String strOK = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), R.string.ok, KDSSettings.ID.Bumpbar_OK);
//
//                String strCancel = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
//                m_viewBeforeShowSlaveDialog = LayoutInflater.from(KDSApplication.getContext()).inflate(R.layout.kdsui_dlg_input_slave, null);
//
//                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
//                        .setTitle(PreferenceFragmentStations.this.getString(R.string.input))
//                        .setMessage(strComfirm)
//                        .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        onInputPrimaryIDDlgOK();
//                                    }
//                                }
//                        )
//                        .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                MyAdapter.this.notifyDataSetChanged();
//                            }
//                        })
//                        .create();
//                d.setView(m_viewBeforeShowSlaveDialog);
//                m_dlgShowSlave = d;
//                m_viewBeforeShowSlaveDialog.setFocusable(true);
//
//                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                editText.setText(getMyPrimaryStation(relation.getID() ));
//
//
//                editText.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    }
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    }
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                        Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
//                        String primaryID = s.toString();
//                        if (primaryID.isEmpty()) {
//                            m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
//                            if (btn != null) btn.setEnabled(false);
//                        }
//                        else
//                        {
//                            String err = validateInputPrimary(m_relationBeforeShowSlaveDialog.getID(), m_relationBeforeShowSlaveDialog.getFunction(), primaryID);
//                            if (err.isEmpty()) {
//                                m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
//                                if (btn != null) btn.setEnabled(true);
//                            }
//                            else {
//
//                                    m_dlgShowSlave.setTitle(err);
//                                    if (btn != null) btn.setEnabled(false);
//                                }
//                        }
//                    }
//                });
//
//
//                d.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                        //KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//                        KDSSettings.ID evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
//                        if (evID == KDSSettings.ID.Bumpbar_OK) {
//                            m_relationBeforeShowSlaveDialog.setSlaveFunc(KDSSettings.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
//                            EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                            m_relationBeforeShowSlaveDialog.setSlaveStations(editText.getText().toString());
//
//                            dialog.dismiss();
//                            return true;
//                        } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
//                            dialog.cancel();
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                //d.getButton(AlertDialog.BUTTON_POSITIVE).setTag(relation.getTag());
//
//                d.show();
//                Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
//                if (relation.getSlaveStations().isEmpty()) {
//                    if (btn != null)
//                        btn.setEnabled(false);
//                }
//            }
//
//            /**
//             * check if inputed station ID is a fit relations rules.
//             * @param stationID
//             * @param stationFunc
//             * @param primaryID
//             * @return
//             */
//            private String validateInputPrimary(String stationID, KDSSettings.StationFunc stationFunc, String primaryID)
//            {
//                String strErr = "";
//                if (stationID.equals(primaryID))
//                    return  PreferenceFragmentStations.this.getString(R.string.error_primary_myself);
//                switch (stationFunc)
//                {
//
//                    case Normal:
//                    case Expeditor:
//                    case TableTracker:
//                        break;
//                    case Queue:
//                        //2.0.11, comment it, allow prep station has queue slave.
//                        if ( (!isExpoStation(primaryID)) && (!isPrepStation(primaryID)))
//                            strErr =  PreferenceFragmentStations.this.getString(R.string.error_not_expo);
//                        break;
//                    case Mirror:
//
//                    case Workload:
//                    case Duplicate:
//                        if (!isPrepStation(primaryID))
//                            strErr = PreferenceFragmentStations.this.getString(R.string.error_not_prep);
//                        break;
//
//                    case Backup:
//                        if (!isExpoStation(primaryID) &&
//                                !isPrepStation(primaryID) &&
//                                !isStationFunc(primaryID, KDSSettings.StationFunc.Backup))
//                            strErr = PreferenceFragmentStations.this.getString(R.string.error_not_expo_prep_backup);
//                        break;
//
//
//
//                }
//                return strErr;
//            }
//            private KDSSettings.SlaveFunc getSlaveFuncFromStationFunc(KDSSettings.StationFunc stationFunc)
//            {
//                KDSSettings.SlaveFunc slaveFunc = KDSSettings.SlaveFunc.Unknown;
//                switch (stationFunc)
//                {
//
//                    case Normal:
//                        slaveFunc =  KDSSettings.SlaveFunc.Unknown;
//                        break;
//                    case Expeditor:
//                        slaveFunc =  KDSSettings.SlaveFunc.Unknown;
//                        break;
//                    case TableTracker:
//                        slaveFunc =  KDSSettings.SlaveFunc.Unknown;
//                        break;
//                    case Queue:
//                        slaveFunc =  KDSSettings.SlaveFunc.Order_Queue_Display;
//
//                        break;
//                    case Mirror:
//                        slaveFunc =  KDSSettings.SlaveFunc.Mirror;
//
//                        break;
//                    case Backup:
//                        slaveFunc =  KDSSettings.SlaveFunc.Backup;
//
//                        break;
//                    case Workload:
//                        slaveFunc =  KDSSettings.SlaveFunc.Automatic_work_loan_distribution;
//
//                        break;
//                    case Duplicate:
//                        slaveFunc =  KDSSettings.SlaveFunc.Duplicate_station;
//
//                        break;
//                }
//                return slaveFunc;
//            }
//
//            /**
//             *
//             * @param slaveStationID
//             *  The slave station
//             * @param slaveStationFunc
//             *  This slave station's station function
//             */
//            private void removeSlaveStation(String slaveStationID, KDSSettings.StationFunc slaveStationFunc)
//            {
//                boolean bChanged = false;
//                KDSSettings.SlaveFunc slaveFunc =  getSlaveFuncFromStationFunc(slaveStationFunc);
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getSlaveStations().equals(slaveStationID))
//                    {
//                        if (MyAdapter.this.getListData().get(i).getSlaveFunc() == slaveFunc) {
//                            MyAdapter.this.getListData().get(i).setSlaveFunc(KDSSettings.SlaveFunc.Unknown);
//                            MyAdapter.this.getListData().get(i).setSlaveStations("");
//                            bChanged = true;
//                        }
//
//                    }
//                }
//                if (bChanged)
//                    MyAdapter.this.notifyDataSetChanged();
//            }
//
//
//            private void showOnlyOneTrackerAllowed() {
//                Toast t = Toast.makeText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.tracker_only_one_allowed),  Toast.LENGTH_LONG);
//                t.show();
//            }
//
//            private void showPressTTButtonMessage()
//            {
//                Toast t = Toast.makeText(KDSApplication.getContext(), KDSApplication.getContext().getString(R.string.tt_authen_press_button),  Toast.LENGTH_LONG);
//                t.show();
//            }
//
//            public void onStationFunctionItemSelected2(AdapterView<?> parent, View view, int position, long id) {
//                //TextView v1 = (TextView) view;
//                //v1.setTextColor(Color.BLACK);
//                Spinner t = this.getSpinner();
//                View viewRow = (View) t.getTag();
//                int nposition = (int) viewRow.getTag();
//                if (nposition >= MyAdapter.this.getListData().size())
//                    return;
//                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);
//
//                //KDSSettings.StationFunc newStationFunc =  KDSSettings.StationFunc.values()[position];
//                KDSSettings.StationFunc newStationFunc = ((MyStationFuncSpinnerAdapter) t.getAdapter()).getItem(position).getFunction();
//
//                if (newStationFunc == KDSSettings.StationFunc.TableTracker)
//                { //only one tracker allowed
//                    if (isMoreThanOneTrackerStation(relation))
//                    {
//                        MyAdapter.this.notifyDataSetChanged();
//                        showOnlyOneTrackerAllowed();
//                        return;
//                    }
//                    else
//                    {
//                        if (relation.getFunction() != newStationFunc) {
//                            //if (KDSSettings.loadTrackerAuthen().isEmpty())
//                                showPressTTButtonMessage();
//                        }
//                    }
//                }
//
//                Spinner slaveFuncSpinner = (Spinner) viewRow.findViewById(R.id.spinnerSlaveFunc);
//                boolean bRemoveExpo = false;
//                switch (newStationFunc)
//                {
//
//                    case Normal:
//
//                        if (relation.getFunction() == KDSSettings.StationFunc.Expeditor ||
//                                relation.getFunction() == KDSSettings.StationFunc.Queue_Expo)
//                            bRemoveExpo = true;
//                        KDSSettings.StationFunc sfunc = MyAdapter.this.getStationChainFunction(relation.getID(), "");
//                        if (sfunc == KDSSettings.StationFunc.Expeditor ||
//                                sfunc == KDSSettings.StationFunc.Queue_Expo)
//                            bRemoveExpo = true;
//                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  KDSSettings.StationFunc.Normal);
//
//                        //relation.setFunction(KDSSettings.StationFunc.Normal);
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//
//                        relation.setFunction(newStationFunc);
//                        if (bRemoveExpo)
//                            confirmRemoveExpo2(relation);
//                        break;
//                    case Expeditor:
//
//                        //Spinner slaveFuncSpinner = (Spinner) viewRow.findViewById(R.id.spinnerSlaveFunc);
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//
//
//                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  KDSSettings.StationFunc.Expeditor);
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//                        if (relation.getFunction() != KDSSettings.StationFunc.Expeditor) {
//                            relation.setFunction(KDSSettings.StationFunc.Expeditor);
//                            relation.setExpStations(""); //clear expo'expo
//                            MyAdapter.this.addExpeditorToAll(relation.getID());
//                            MyAdapter.this.notifyDataSetChanged();
//                        }
//                        break;
//                    case Queue_Expo:
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//
//
//                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  KDSSettings.StationFunc.Queue_Expo);
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//                        if (relation.getFunction() != KDSSettings.StationFunc.Queue_Expo) {
//                            relation.setFunction(KDSSettings.StationFunc.Queue_Expo);
//                            relation.setExpStations(""); //clear expo'expo
//                            MyAdapter.this.addExpeditorToAll(relation.getID());
//                            MyAdapter.this.notifyDataSetChanged();
//                        }
//                        break;
//                    case TableTracker:
//                    {
//                        KDSSettings.StationFunc oldFunc = relation.getFunction();
//                        if (relation.getFunction() == KDSSettings.StationFunc.Expeditor ||
//                                relation.getFunction() == KDSSettings.StationFunc.Queue_Expo)
//                            bRemoveExpo = true;
//                        KDSSettings.StationFunc stationfunc = MyAdapter.this.getStationChainFunction(relation.getID(), "");
//                        if ( stationfunc == KDSSettings.StationFunc.Expeditor ||
//                                stationfunc == KDSSettings.StationFunc.Queue_Expo)
//                            bRemoveExpo = true;
//                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  KDSSettings.StationFunc.TableTracker);
//                        relation.setExpStations("");
//                        //relation.setFunction(KDSSettings.StationFunc.Normal);
//                        if (relation.getFunction() != newStationFunc)
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//
//                        relation.setFunction(newStationFunc);
//                        relation.setSlaveStations("");
//                        if (oldFunc != relation.getFunction())
//                            MyAdapter.this.notifyDataSetChanged();
//                        if (bRemoveExpo)
//                            confirmRemoveExpo2(relation);
//
//                        break;
//                    }
//
//                    case Queue:
//                    case Mirror:
//                    case Backup:
//                    case Workload:
//                    case Duplicate:
//                        boolean bIsQueue = (relation.getFunction() == KDSSettings.StationFunc.Queue);
//
//                        if (relation.getFunction() != newStationFunc) {
//                            removeSlaveStation(relation.getID(), relation.getFunction());
//                            if (newStationFunc == KDSSettings.StationFunc.Queue )
//                                relation.setExpStations("");
//                        }
//                        if (relation.getFunction() == KDSSettings.StationFunc.Expeditor ||
//                                relation.getFunction() == KDSSettings.StationFunc.Queue_Expo)
//                            bRemoveExpo = true;
//                        KDSSettings.StationFunc sfunction = MyAdapter.this.getStationChainFunction(relation.getID(), "");
//                        if ( (sfunction == KDSSettings.StationFunc.Expeditor|| sfunction == KDSSettings.StationFunc.Queue_Expo )
//                                && newStationFunc != KDSSettings.StationFunc.Backup) {
//                            if (!bIsQueue) //if this is queue, don't remove it as expo
//                                bRemoveExpo = true;
//                        }
//                        initSlaveFunctionSpinner(KDSApplication.getContext(), slaveFuncSpinner,  newStationFunc);
//                        if (relation.getFunction() != newStationFunc) {
//                            relation.setFunction(newStationFunc);
//                            MyAdapter.this.notifyDataSetChanged();
//
//                            inputPrimaryStationID(t, position, relation);
//                        }
//                        if (bRemoveExpo && !bIsQueue)
//                            confirmRemoveExpo2(relation);
//
//                        break;
//
//                }
//
//
//            }
//
//
//            /**
//             * after input slave ID, change this station function to select function.
//             * @param stationID
//             *  The old slave station ID, this station worked as slaveFunc. Change station function.
//             * @param
//             */
//            private void changeStationToGiveFunctionAccordingSlaveFunction(String stationID, KDSSettings.SlaveFunc slaveFunc, boolean bCheckPrimaryLoop)
//            {
//                KDSSettings.StationFunc stationFunc = KDSSettings.StationFunc.Normal;
//                String primaryStation = getMyPrimaryStation(stationID);
//                if (bCheckPrimaryLoop)
//                    if (!primaryStation.isEmpty()) return; //this station work as others station's slave (the backup allow loop), return it.
//                switch (slaveFunc)
//                {
//
//                    case Unknown:
//                        stationFunc = KDSSettings.StationFunc.Normal;
//                        break;
//                    case Backup:
//                        stationFunc = KDSSettings.StationFunc.Backup;
//                        break;
//                    case Mirror:
//                        stationFunc = KDSSettings.StationFunc.Mirror;
//                        break;
//                    case Automatic_work_loan_distribution:
//                        stationFunc = KDSSettings.StationFunc.Workload;
//                        break;
//                    case Duplicate_station:
//                        stationFunc = KDSSettings.StationFunc.Duplicate;
//                        break;
//                    case Order_Queue_Display:
//                        stationFunc = KDSSettings.StationFunc.Queue;
//                        break;
//                }
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
//                    {
//                        MyAdapter.this.getListData().get(i).setFunction(stationFunc);
//                    }
//                }
//            }
//
//            private void onInputSlaveStationIDDlgOK()
//            {
//                //m_relationBeforeShowSlaveDialog.setSlaveFunc(KDSSettings.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
//                m_relationBeforeShowSlaveDialog.setSlaveFunc(m_slaveFunctionBeforeShowSlaveDialog.getFunction());
//                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                String slaveID = editText.getText().toString();
//                m_relationBeforeShowSlaveDialog.setSlaveStations(slaveID);
//                changeStationToGiveFunctionAccordingSlaveFunction(slaveID,m_slaveFunctionBeforeShowSlaveDialog.getFunction(), false );
//                KDSSettings.StationFunc sfunc = MyAdapter.this.getStationChainFunction(slaveID, "");
//
//                if (( sfunc == KDSSettings.StationFunc.Expeditor  || sfunc == KDSSettings.StationFunc.Queue_Expo )&&
//                        m_relationBeforeShowSlaveDialog.getSlaveFunc() == KDSSettings.SlaveFunc.Backup)
//                    MyAdapter.this.addExpeditorToAll(slaveID);
//                else
//                    MyAdapter.this.removeExpeditorFromAll(slaveID);
//                MyAdapter.this.notifyDataSetChanged();
//            }
//            private void inputSlaveStationID(Spinner slaveFuncSpinner, int position, KDSStationsRelation relation)
//            {
//                m_relationBeforeShowSlaveDialog = relation;
//                m_lstBeforeShowDialog = MyAdapter.this.cloneListData();
//
//                String strComfirm = PreferenceFragmentStations.this.getString(R.string.please_input_slave_station);
//                strComfirm = strComfirm.replace("#", relation.getID());
//                strComfirm = strComfirm.replace("$", ((MySlaveSpinnerArrayAdapter)slaveFuncSpinner.getAdapter()).getSlaveFunction(position).toString());
//                //String strOK = KDSUIDialogBase.makeButtonText(view.getContext().getApplicationContext(), android.R.string.ok, KDSSettings.ID.Bumpbar_OK);
//                String strOK = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), R.string.ok, KDSSettings.ID.Bumpbar_OK);
//                //String strCancel = KDSUIDialogBase.makeButtonText(view.getContext().getApplicationContext(), android.R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
//                String strCancel = KDSUIDialogBase.makeCtrlEnterButtonText(KDSApplication.getContext(), R.string.cancel, KDSSettings.ID.Bumpbar_Cancel);
//                m_viewBeforeShowSlaveDialog = LayoutInflater.from(KDSApplication.getContext()).inflate(R.layout.kdsui_dlg_input_slave, null);
//
//                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
//                        .setTitle(PreferenceFragmentStations.this.getString(R.string.input))
//                        .setMessage(strComfirm)
//                        .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        onInputSlaveStationIDDlgOK();
//                                    }
//                                }
//                        )
//                        .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                m_relationBeforeShowSlaveDialog.setSlaveFunc(KDSSettings.SlaveFunc.Unknown);
//                                MyAdapter.this.notifyDataSetChanged();
//                            }
//                        })
//                        .create();
//
//                d.setView(m_viewBeforeShowSlaveDialog);
//                d.setCancelable(false);
//                d.setCanceledOnTouchOutside(false);
//
//                m_dlgShowSlave = d;
//                m_viewBeforeShowSlaveDialog.setFocusable(true);
//
//                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                editText.setText(relation.getSlaveStations());
//
//
//                editText.addTextChangedListener(new TextWatcher() {
//                    @Override
//                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                    }
//                    @Override
//                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                    }
//                    @Override
//                    public void afterTextChanged(Editable s) {
//                        Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
//                        if (s.toString().isEmpty()) {
//                            m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
//                            if (btn != null) btn.setEnabled(false);
//                        }
//                        else
//                        {
//                            String slaveID = s.toString();
//                            String strErr = validateInputSlave(m_relationBeforeShowSlaveDialog.getID(),m_slaveFunctionBeforeShowSlaveDialog.getFunction(), slaveID );
//                            if (strErr.isEmpty()) {
//                                m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.input));
//                                if (btn != null) btn.setEnabled(true);
//                            }
//                            else {
//                                    if (btn != null) btn.setEnabled(false);
//                                    m_dlgShowSlave.setTitle(PreferenceFragmentStations.this.getString(R.string.error_slave));
//                                }
//                        }
//                    }
//                });
//
//
//                d.setOnKeyListener(new DialogInterface.OnKeyListener() {
//                    @Override
//                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                        //KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
//                        KDSSettings.ID evID = KDSUIDialogBase.checkCtrlEnterEvent(keyCode, event);
//                        if (evID == KDSSettings.ID.Bumpbar_OK) {
//                            m_relationBeforeShowSlaveDialog.setSlaveFunc(KDSSettings.SlaveFunc.values()[m_npositionBeforeShowSlaveDialog]);
//                            EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                            m_relationBeforeShowSlaveDialog.setSlaveStations(editText.getText().toString());
//
//                            dialog.dismiss();
//                            return true;
//                        } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
//                            dialog.cancel();
//                            return true;
//                        }
//                        return false;
//                    }
//                });
//                //d.getButton(AlertDialog.BUTTON_POSITIVE).setTag(relation.getTag());
//
//                d.show();
//                Button btn =m_dlgShowSlave.getButton(DialogInterface.BUTTON_POSITIVE);
//                if (relation.getSlaveStations().isEmpty()) {
//                    if (btn != null)
//                        btn.setEnabled(false);
//                }
//                if (btn != null)
//                {
//                    btn.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (onSlaveIDDlgOKClicked()) {
//                                m_dlgShowSlave.hide();
//                                onInputSlaveStationIDDlgOK();
//                            }
//
//                        }
//                    });
//                }
//            }
//
//            private String validateInputSlave(String stationID, KDSSettings.SlaveFunc slaveFunc, String slaveID)
//            {
//                String strErr = "";
//                if (stationID.equals(slaveID))
//                    return  PreferenceFragmentStations.this.getString(R.string.error_slave_myself);
//                strErr = KDSStationsRelation.checkSlaveConflict(m_lstBeforeShowDialog,  stationID,slaveID,  slaveFunc);
//                return strErr;
//            }
//
//            private boolean onSlaveIDDlgOKClicked()
//            {
//                EditText editText =(EditText) m_viewBeforeShowSlaveDialog.findViewById(R.id.txtText);
//                String slaveID = editText.getText().toString();
//                if (!isExpoStation(slaveID)) return true;
//                //expo allow expo as backup
//                if (m_relationBeforeShowSlaveDialog.getFunction() == KDSSettings.StationFunc.Expeditor ||
//                        m_relationBeforeShowSlaveDialog.getFunction() == KDSSettings.StationFunc.Queue_Expo )
//                    return true;
//                AlertDialog d = new AlertDialog.Builder(PreferenceFragmentStations.this.getActivity())
//                        .setTitle(PreferenceFragmentStations.this.getString(R.string.error))
//                        .setMessage(PreferenceFragmentStations.this.getString(R.string.relation_error_expo_as_slave))
//                        .setPositiveButton(PreferenceFragmentStations.this.getString(R.string.ok), new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//
//                                    }
//                                }
//                        )
//                        .create();
//                d.show();
//                return false;
//            }
//            private boolean isExpoStation(String stationID)
//            {
//               if (stationID.isEmpty()) return false;
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
//                    {
//                        if (MyAdapter.this.getListData().get(i).getFunction() == KDSSettings.StationFunc.Expeditor ||
//                                MyAdapter.this.getListData().get(i).getFunction() == KDSSettings.StationFunc.Queue_Expo)
//                            return true;
//                    }
//                }
//                return false;
//
//            }
//
//            private boolean isPrepStation(String stationID)
//            {
//                if (stationID.isEmpty()) return false;
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
//                    {
//                        if (MyAdapter.this.getListData().get(i).getFunction() == KDSSettings.StationFunc.Normal)
//                            return true;
//                    }
//                }
//                return false;
//
//            }
//
//            private boolean isStationFunc(String stationID, KDSSettings.StationFunc func)
//            {
//                if (stationID.isEmpty()) return false;
//                for (int i=0; i<  MyAdapter.this.getListData().size(); i++)
//                {
//                    if (MyAdapter.this.getListData().get(i).getID().equals(stationID))
//                    {
//
//                        if (MyAdapter.this.getListData().get(i).getFunction() == func)
//                            return true;
//
//                    }
//                }
//                return false;
//
//            }
//            public void onSlaveFunctionItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                ///////////////////////////////////////////////////////////////////////////
//                if (view == null) return;
//                TextView v1 = (TextView) view;
//                //v1.setTextColor(Color.BLACK);
//                Spinner t = this.getSpinner();
//                View viewRow = (View) t.getTag();
//                int nposition = (int) viewRow.getTag();
//                if (nposition >= MyAdapter.this.getListData().size())
//                    return;
//                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);
//
//                SlaveFunction slaveFunctionValue = ((MySlaveSpinnerArrayAdapter) t.getAdapter()).getSlaveFunction(position);
//                if (relation.getSlaveFunc() == slaveFunctionValue.getFunction()) return;
//                String oldSlaveID = relation.getSlaveStations();
//                relation.setSlaveFunc(slaveFunctionValue.getFunction()); //20170224
//
//                m_npositionBeforeShowSlaveDialog = position;
//                m_slaveFunctionBeforeShowSlaveDialog = slaveFunctionValue;
//
//                if (slaveFunctionValue.getFunction() != KDSSettings.SlaveFunc.Unknown) {
//                    inputSlaveStationID(t, position, relation);
//
//                }
//                else
//                {
//
//                    relation.setSlaveFunc(KDSSettings.SlaveFunc.Unknown);
//
//                    relation.setSlaveStations("");
//                    if (!oldSlaveID.isEmpty())
//                    {
//                        changeStationToGiveFunctionAccordingSlaveFunction(oldSlaveID,KDSSettings.SlaveFunc.Unknown, true );
//                        if (relation.getFunction() == KDSSettings.StationFunc.Expeditor || relation.getFunction() == KDSSettings.StationFunc.Queue_Expo)
//                            MyAdapter.this.removeExpeditorBackupFromExpoList(oldSlaveID);
//
//                    }
//                    MyAdapter.this.notifyDataSetChanged();
//
//                }
//
//            }
//
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                TextView v1 = (TextView) view;
//
//                Spinner t = this.getSpinner();
//                View viewRow = (View) t.getTag();
//                int nposition = (int)viewRow.getTag();
//                if (nposition >= MyAdapter.this.getListData().size())
//                    return;
//                KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);
//
//                if (t.getId() == R.id.spinnerFunc)
//                {
//                    onStationFunctionItemSelected2(parent, view, position, id);
//
//                }
//                else if (t.getId() == R.id.spinnerStatus)
//                {
//                    if (position == 0)
//                        relation.setStatus(KDSSettings.StationStatus.Enabled);
//                    else
//                        relation.setStatus(KDSSettings.StationStatus.Disabled);
//                }
//                else if (t.getId() == R.id.spinnerSlaveFunc)
//                {//slave function
//                    onSlaveFunctionItemSelected(parent, view, position, id);
//                }
//                setImageEditingIcon((ImageView)viewRow.findViewById(R.id.imgEdit), relation);
//                //PreferenceFragmentStations.this.save();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        }
//
//
//        View m_viewEditing = null;
//        private LayoutInflater mInflater;
//
//        public List<KDSStationsRelation> m_listData; //KDSStationsRelation class array
//        public List<KDSStationsRelation> m_listOriginal; //check changed.
//
//        public View getEditingView()
//        {
//            return m_viewEditing;
//        }
//        public MyAdapter(Context context, List<KDSStationsRelation> data) {
//            this.mInflater = LayoutInflater.from(context);
//            m_listData = data;
//        }
//        public List<KDSStationsRelation> getListData()
//        {
//            return m_listData;
//        }
//        public void setListData(List<KDSStationsRelation> lst)
//        {
//            m_listData = lst;
//            cloneToOriginalArray(); //backup it.
//        }
//
//        public List<KDSStationsRelation> cloneListData()
//        {
//            ArrayList<KDSStationsRelation> lst = new ArrayList<>();
//
//            for (int i=0; i< m_listData.size(); i++)
//            {
//                KDSStationsRelation r = m_listData.get(i);
//                KDSStationsRelation relation = new KDSStationsRelation();
//                relation.copyFrom(r);
//                lst.add(relation);
//            }
//            return lst;
//        }
//
//        private boolean isExpoAllowed(KDSSettings.StationFunc func)
//        {
//            if (func == KDSSettings.StationFunc.Expeditor ||
//                 func == KDSSettings.StationFunc.Queue   ||
//                    func == KDSSettings.StationFunc.TableTracker ||
//                    func == KDSSettings.StationFunc.Queue_Expo)
//                return false;
//            return true;
//        }
//        /**
//         * add this expo to all stations
//         * @param expoID
//         */
//        public void addExpeditorToAll(String expoID)
//        {
//
//            boolean bExistedInExpo = false;
//            for (int i=0; i< m_listData.size(); i++)
//            {
//                KDSStationsRelation r = m_listData.get(i);
//                if ( !isExpoAllowed(r.getFunction()))
//                    continue;
//
//                String s = r.getExpStations();
//                if (KDSStationsRelation.existedStation(s, expoID))
//                    bExistedInExpo = true;
//            }
//            if (bExistedInExpo) //if this existed in any station, don't auto add it.
//                return;
//
//            for (int i=0; i< m_listData.size(); i++)
//            {
//                KDSStationsRelation r = m_listData.get(i);
//                if ( !isExpoAllowed(r.getFunction()))
//                    continue;
//                KDSSettings.StationFunc sfunc =getStationChainFunction(r.getID(), "");
//                if (sfunc == KDSSettings.StationFunc.Expeditor ||
//                        sfunc == KDSSettings.StationFunc.Queue_Expo) {
//                    r.setExpStations("");
//                    continue;
//                }
//                r.addExpoStation(expoID);
//            }
//        }
//
//        public void removeExpeditorFromAll(String expoID)
//        {
//            removeExpeditorChainFromAll(expoID);
//
//        }
//
//        public void removeExpeditorFromList(String expoID)
//        {
//
//            for (int i=0; i< m_listData.size(); i++)
//            {
//                KDSStationsRelation r = m_listData.get(i);
//                if ( !isExpoAllowed(r.getFunction()))
//                    continue;
//
//                r.removeExpoStation(expoID);
//            }
//        }
//
//        public void removeExpeditorBackupFromExpoList(String expoSlaveID)
//        {
//            // removeExpeditorChainFromAll(expoID);
//            KDSStationsRelation r = getRelation(expoSlaveID);
//            if (r == null) return;
//            removeExpeditorFromList(expoSlaveID);
//            for (int i=0; i< 1000; i++)
//            {
//                if (r == null) return;
//                if (r.getSlaveStations().isEmpty()) return;
//                removeExpeditorFromList(r.getSlaveStations());
//                r =  getRelation(r.getSlaveStations());
//
//            }
//
//        }
//        public void removeExpeditorChainFromAll(String expoID)
//        {
//
//            KDSStationsRelation expo =  getRelation(expoID);
//
//            for (int i=0; i< m_listData.size(); i++)
//            {
//                KDSStationsRelation r = m_listData.get(i);
//                KDSStationsRelation topRelation =  getStationChainPrimaryToppest(r.getID(), "");
//                if (topRelation == null) continue;
//                if (topRelation.getID() == expoID)
//                    removeExpeditorFromList(r.getID());
//
//
//            }
//            removeExpeditorFromList(expoID);
//        }
//        /**
//         * check if editing something.
//         * @return
//         */
//        public boolean isChanged()
//        {
//            return isDifferent(m_listData);
//
//        }
//
//        /**
//         * compare this new relation with showing
//         * @param ar
//         * @return
//         */
//        public boolean isDifferent(List<KDSStationsRelation> ar)
//        {
//            if (ar.size() != m_listOriginal.size())
//                return true;
//            for (int i=0; i< ar.size(); i++) {
//                KDSStationsRelation r = ar.get(i);
//
//                KDSStationsRelation relationOriginal = findStation(m_listOriginal, r.getID());
//                if (relationOriginal == null)
//                    return true;
//                if  (!relationOriginal.isRelationEqual(r))
//                    return true;
//
//            }
//            return false;
//        }
//        private void initSpinner(Context context, Spinner spinner, List<String> list)
//        {
//
//
//            MySpinnerArrayAdapter adapter;
//            adapter = new MySpinnerArrayAdapter(context, list);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(adapter);
//        }
//
//
//        ArrayList<StationFunction> m_stationFuncSpinnerListHaveExpo = new ArrayList<>();
//        ArrayList<StationFunction> m_stationFuncSpinnerListNoExpo = new ArrayList<>();
//        private void initStationfunctionSpinner2(Context context, Spinner spinner,KDSStationsRelation relation)
//        {
//
//            SpinnerAdapter oldAdapter =  spinner.getAdapter();
//            if (oldAdapter instanceof MyStationFuncSpinnerAdapter)
//            {
//                if (isExpoExisted())
//                {
//                    if (oldAdapter.getCount() == KDSSettings.StationFunc.MAX_COUNT.ordinal()) return;
//                }
//                else
//                {
//                    //2.0.11, remove MAX_COUNT.ordinal()-1
//                    if (oldAdapter.getCount() == KDSSettings.StationFunc.MAX_COUNT.ordinal()) return;
//                }
//
//            }
//            MyStationFuncSpinnerAdapter adapter;
//            ArrayList<StationFunction> list = new ArrayList<>();
//
//            List<String> arFuncStrings = Arrays.asList(context.getResources().getStringArray(R.array.station_function));
//
//            if (isExpoExisted()) {
//                if (m_stationFuncSpinnerListHaveExpo.size()<=0) {
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Normal.ordinal()), KDSSettings.StationFunc.Normal));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Expeditor.ordinal()), KDSSettings.StationFunc.Expeditor));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Queue.ordinal()), KDSSettings.StationFunc.Queue));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Mirror.ordinal()), KDSSettings.StationFunc.Mirror));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Backup.ordinal()), KDSSettings.StationFunc.Backup));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Workload.ordinal()), KDSSettings.StationFunc.Workload));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Duplicate.ordinal()), KDSSettings.StationFunc.Duplicate));
//
//
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.TableTracker.ordinal()), KDSSettings.StationFunc.TableTracker));
//                    m_stationFuncSpinnerListHaveExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Queue_Expo.ordinal()), KDSSettings.StationFunc.Queue_Expo));
//                }
//                list = m_stationFuncSpinnerListHaveExpo;
//
//            }
//            else {
//                if (m_stationFuncSpinnerListNoExpo.size() <=0) {
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Normal.ordinal()), KDSSettings.StationFunc.Normal));
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Expeditor.ordinal()), KDSSettings.StationFunc.Expeditor));
//                    //2.0.11, allow queue in prep station
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Queue.ordinal()), KDSSettings.StationFunc.Queue));
//                    //
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Mirror.ordinal()), KDSSettings.StationFunc.Mirror));
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Backup.ordinal()), KDSSettings.StationFunc.Backup));
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Workload.ordinal()), KDSSettings.StationFunc.Workload));
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Duplicate.ordinal()), KDSSettings.StationFunc.Duplicate));
//
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.TableTracker.ordinal()), KDSSettings.StationFunc.TableTracker));
//                    m_stationFuncSpinnerListNoExpo.add(new StationFunction(arFuncStrings.get(KDSSettings.StationFunc.Queue_Expo.ordinal()), KDSSettings.StationFunc.Queue_Expo));
//                }
//                list = m_stationFuncSpinnerListNoExpo;
//            }
//
//
//            adapter = new MyStationFuncSpinnerAdapter(context, list);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(adapter);
//            spinner.setEnabled(list.size()>1);
//
//
//
//        }
//
//        public String getMyPrimaryStation(String slaveStationID)
//        {
//            for (int i=0; i<  this.getListData().size(); i++)
//            {
//                if (this.getListData().get(i).getSlaveStations().equals(slaveStationID))
//                {
//
//                    return  this.getListData().get(i).getID();
//
//                }
//            }
//            return "";
//        }
//
//        public KDSStationsRelation getMyPrimaryRelation(String slaveStationID)
//        {
//            for (int i=0; i<  this.getListData().size(); i++)
//            {
//                if (this.getListData().get(i).getSlaveStations().equals(slaveStationID))
//                {
//
//                    return  this.getListData().get(i);
//
//                }
//            }
//            return null;
//        }
//
//        public KDSStationsRelation getRelation(String stationID)
//        {
//            for (int i=0; i<  this.getListData().size(); i++)
//            {
//                if (this.getListData().get(i).getID().equals(stationID))
//                {
//
//                    return  this.getListData().get(i);
//
//                }
//            }
//            return null;
//        }
//        /**
//         * check expo, queue and prep
//         * @param stationID
//         * @return
//         */
//        public KDSSettings.StationFunc getStationChainFunction(String stationID, String whoUseMeAsSlave)
//        {
//
//            KDSStationsRelation r = getRelation(stationID);
//            if (r == null) return  KDSSettings.StationFunc.Queue; //show nothing
//            if (r.getFunction() != KDSSettings.StationFunc.Expeditor &&
//                    r.getFunction() != KDSSettings.StationFunc.Normal &&
//                    r.getFunction() != KDSSettings.StationFunc.Queue_Expo) {
//                String primary = getMyPrimaryStation(stationID);
//                if (primary.isEmpty()) return KDSSettings.StationFunc.Queue;
//                if (primary.equals(whoUseMeAsSlave)) return r.getFunction();
//                return getStationChainFunction(primary, stationID);
//            }
//            else
//                return r.getFunction();
//
//        }
//
//        public KDSStationsRelation getStationChainPrimaryToppest(String stationID, String whoUseMeAsPrimary)
//        {
//
//            KDSStationsRelation r = getRelation(stationID);
//            if (r == null) return  null; //show nothing
//            if (r.getFunction() != KDSSettings.StationFunc.Expeditor &&
//                    r.getFunction() != KDSSettings.StationFunc.Normal &&
//                    r.getFunction()!= KDSSettings.StationFunc.TableTracker &&
//                    r.getFunction() != KDSSettings.StationFunc.Queue_Expo) {
//                String primary = getMyPrimaryStation(stationID);
//                if (primary.isEmpty()) return null;
//                if (primary.equals(whoUseMeAsPrimary)) return r;
//                return getStationChainPrimaryToppest(primary, stationID);
//            }
//            else
//                return r;
//
//        }
//
//        public boolean isExpoExisted()
//        {
//            for (int i=0; i<  this.getListData().size(); i++)
//            {
//                if (this.getListData().get(i).getFunction() == KDSSettings.StationFunc.Expeditor ||
//                        this.getListData().get(i).getFunction() == KDSSettings.StationFunc.Queue_Expo    )
//                {
//
//                    return true;
//
//                }
//            }
//            return false;
//        }
//
//        public boolean isMoreThanOneTrackerStation(KDSStationsRelation trackerRelation)
//        {
//            for (int i=0; i<  this.getListData().size(); i++)
//            {
//                if (this.getListData().get(i).getFunction() == KDSSettings.StationFunc.TableTracker)
//                {
//                    if (this.getListData().get(i) != trackerRelation)
//                        return true;
//
//                }
//            }
//            return false;
//        }
//
//        //2.0.11, change it from 5 o 6, allow queue for prep station.
//        final static int PREP_SLAVE_OPTIONS = 6;
//        //
//        final static int EXPO_SLAVE_OPTIONS = 3;
//
//        final static int NO_SLAVE_OPTIONS = 1;
//        final static int MIRROR_SLAVE_OPTIONS = 1;
//        final static int WORKLOAD_SLAVE_OPTIONS = 1;
//        final static int DUPLICATE_SLAVE_OPTIONS = 1;
//        final static int BACKUP_SLAVE_OPTIONS = 2;
//        final static int Queue_Expo_SLAVE_OPTIONS = 2;
//
//        ArrayList<SlaveFunction> m_slaveFuncNormalSpinnerList = new ArrayList<>();
//        ArrayList<SlaveFunction> m_slaveFuncExpoSpinnerList = new ArrayList<>();
//        ArrayList<SlaveFunction> m_slaveFuncQueueExpoSpinnerList = new ArrayList<>();
//        ArrayList<SlaveFunction> m_slaveFuncBackupSpinnerList = new ArrayList<>();
//        ArrayList<SlaveFunction> m_slaveFuncOtherSpinnerList = new ArrayList<>();
//
//        private void initSlaveFunctionSpinner(Context context, Spinner spinner, KDSSettings.StationFunc stationFunction)
//        {
//            SpinnerAdapter oldAdapter =  spinner.getAdapter();
//            if (oldAdapter instanceof MySlaveSpinnerArrayAdapter)
//            {
//                switch (stationFunction)
//                {
//
//                    case Normal:
//                        if (oldAdapter.getCount() == PREP_SLAVE_OPTIONS) return;
//                        break;
//                    case Expeditor:
//
//                        if (oldAdapter.getCount() == EXPO_SLAVE_OPTIONS) return;//no slave, backup,  queue
//                        break;
//                    case Queue:
//                    case Mirror:
//                    case Workload:
//                    case Duplicate:
//                    case TableTracker:
//                        if (oldAdapter.getCount() == NO_SLAVE_OPTIONS) return;//no slave
//                        break;
//                    case Backup:
//                        if (oldAdapter.getCount() == BACKUP_SLAVE_OPTIONS) return;//no slave, backup
//                        break;
//                    case Queue_Expo:
//                        if (oldAdapter.getCount() == Queue_Expo_SLAVE_OPTIONS) return;//no slave, backup,  queue
//                        break;
//                }
//            }
//            MySlaveSpinnerArrayAdapter adapter;
//            ArrayList<SlaveFunction> list = new ArrayList<>();
//
//            List<String> arFuncStrings = Arrays.asList(context.getResources().getStringArray(R.array.slave_function));
//
//            switch (stationFunction)
//            {
//
//                case Normal:
//                    if (m_slaveFuncNormalSpinnerList.size()<=0) {
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Unknown.ordinal()), KDSSettings.SlaveFunc.Unknown));
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Backup.ordinal()), KDSSettings.SlaveFunc.Backup));
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Mirror.ordinal()), KDSSettings.SlaveFunc.Mirror));
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Automatic_work_loan_distribution.ordinal()), KDSSettings.SlaveFunc.Automatic_work_loan_distribution));
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Duplicate_station.ordinal()), KDSSettings.SlaveFunc.Duplicate_station));
//                        //2.0.11, allow queue in prep station
//                        m_slaveFuncNormalSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Order_Queue_Display.ordinal()), KDSSettings.SlaveFunc.Order_Queue_Display));
//                    }
//                    list = m_slaveFuncNormalSpinnerList;
//                    break;
//                case Expeditor:
//                    if (m_slaveFuncExpoSpinnerList.size() <=0) {
//                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Unknown.ordinal()), KDSSettings.SlaveFunc.Unknown));
//                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Backup.ordinal()), KDSSettings.SlaveFunc.Backup));
//                        m_slaveFuncExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Order_Queue_Display.ordinal()), KDSSettings.SlaveFunc.Order_Queue_Display));
//                    }
//                    list = m_slaveFuncExpoSpinnerList;
//                    break;
//                case Queue_Expo:
//                    if (m_slaveFuncQueueExpoSpinnerList.size() <=0) {
//                        m_slaveFuncQueueExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Unknown.ordinal()), KDSSettings.SlaveFunc.Unknown));
//                        m_slaveFuncQueueExpoSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Backup.ordinal()), KDSSettings.SlaveFunc.Backup));
//                    }
//                    list = m_slaveFuncQueueExpoSpinnerList;
//                    break;
//                case Queue:
//                case Mirror:
//                case Workload:
//                case Duplicate:
//                case TableTracker:
//                    if (m_slaveFuncOtherSpinnerList.size() <=0) {
//                        m_slaveFuncOtherSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Unknown.ordinal()), KDSSettings.SlaveFunc.Unknown));
//                    }
//                    list = m_slaveFuncOtherSpinnerList;
//                    break;
//                case Backup:
//                    if (m_slaveFuncBackupSpinnerList.size() <=0) {
//                        m_slaveFuncBackupSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Unknown.ordinal()), KDSSettings.SlaveFunc.Unknown));
//                        m_slaveFuncBackupSpinnerList.add(new SlaveFunction(arFuncStrings.get(KDSSettings.SlaveFunc.Backup.ordinal()), KDSSettings.SlaveFunc.Backup));
//                    }
//                    list = m_slaveFuncBackupSpinnerList;
//                    break;
//            }
//            adapter = new MySlaveSpinnerArrayAdapter(context, list);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(adapter);
//            spinner.setEnabled(list.size()>1);
//        }
//
//
//        /**
//         * check if has error in it.
//         *
//         *   prevent t
//         * @return
//         */
//        public boolean isError()
//        {
//
//            for (int i=0; i< m_listData.size(); i++) {
//                KDSStationsRelation r = m_listData.get(i);
//                KDSStationsRelation.EditingState state = checkEditingState(r, false);
//                if (state != KDSStationsRelation.EditingState.OK &&
//                        state != KDSStationsRelation.EditingState.Changed)
//                    return true;
//
//
//            }
//            return false;
//        }
//
//        public void reset()
//        {
//            for (int i=0; i< m_listData.size(); i++) {
//                KDSStationsRelation r = m_listData.get(i);
//                r.reset();
//
//
//            }
//            this.notifyDataSetChanged();
//        }
//        public void cloneToOriginalArray()
//        {
//            List<KDSStationsRelation> lst = m_listData;
//            if (m_listOriginal == null)
//                m_listOriginal = new ArrayList<>();
//            m_listOriginal.clear();
//            for (int i=0; i< lst.size(); i++)
//            {
//                KDSStationsRelation r = new KDSStationsRelation();
//                r.copyFrom(lst.get(i));
//                m_listOriginal.add(r);
//
//            }
//        }
//
//        /**
//         *
//         * @param r
//         * @param bShowErrorsMessage
//         *  prevent the "isError" dead loop.
//         * @return
//         */
//        private KDSStationsRelation.EditingState checkEditingState(KDSStationsRelation r, boolean bShowErrorsMessage)
//        {
//            if (r.getID().isEmpty()) return KDSStationsRelation.EditingState.Error_ID;
//            KDSStationsRelation relationOriginal = findStation(m_listOriginal, r.getID());
//            ArrayList<String> errors = new ArrayList<>();
//            KDSStationsRelation.EditingState err = checkValidation(r, errors);
//            if (errors.size()>0) {
//                PreferenceFragmentStations.this.m_txtError.setText(errors.get(0));
//            }
//            else
//            {
//                if (bShowErrorsMessage)
//                    if (!isError()) PreferenceFragmentStations.this.m_txtError.setText("");
//
//            }
//            if (err != KDSStationsRelation.EditingState.OK)
//                return err;
//
//            if (relationOriginal == null)
//                return KDSStationsRelation.EditingState.Changed;
//
//            if (relationOriginal.isRelationEqual(r)) {
//                return KDSStationsRelation.EditingState.OK;
//            } else {
//
//                return KDSStationsRelation.EditingState.Changed;
//            }
//
//        }
//
//        private KDSStationsRelation.EditingState checkValidation(KDSStationsRelation r, ArrayList<String> errorsMsg)
//        {
//            String myStationID = r.getID();
//
//            if (KDSGlobalVariables.getKDS().getStationsConnections().findActivedStationCountByID(myStationID)>1)
//                return KDSStationsRelation.EditingState.Error_IP;
//
//            String strErr = KDSStationsRelation.checkSlaveConflict(m_listData, myStationID,r.getSlaveStations(), r.getSlaveFunc());
//            if (!strErr.isEmpty())
//            {
//                String s = PreferenceFragmentStations.this.getActivity().getApplicationContext().getString(R.string.error_relations_repeat_backup_mirror);// String.format("Stations: %s works as backup and mirror.", strErr);
//                s = s.replace("#", strErr);
//                if (errorsMsg != null)
//                    errorsMsg.add(s);
//
//
//                return KDSStationsRelation.EditingState.Error_Repeat_BackupMirror;
//            }
//            return KDSStationsRelation.EditingState.OK;
//        }
//
//
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
//
//
//        public  void setSelectItem(int selectItem) {
//            this.m_selectItem = selectItem;
//        }
//        public int getSelectedItem()
//        {
//            return m_selectItem;
//        }
//        private int  m_selectItem=-1;
//
//        private void init_view_focus_event(View v, Object objTag)
//        {
//            v.setTag(objTag);
//            v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View v, boolean hasFocus) {
//                    if (hasFocus) {
//
//                        PreferenceFragmentStations.this.onListItemClicked((View) (v.getTag()));
//                        MyAdapter.this.m_viewEditing = v;
//
//                    }
//                }
//            });
//
//
//
//        }
//        private void init_edittext_changed_event(EditText v)
//        {
//
//            v.addTextChangedListener(new CustomTextWatcher(v) {
//                public void afterTextChanged(Editable s) {
//                    //PreferenceFragmentStations.this.save();
//                    EditText t = this.getEditText();
//                    View viewRow = (View) t.getTag();
//                    int nposition = (int) viewRow.getTag();
//                    KDSStationsRelation relation = MyAdapter.this.getListData().get(nposition);
//                    ViewHolder holder = (ViewHolder) relation.getTag();
//
//                    //relation.setEditingState(KDSStationsRelation.EditingState.Changed);
//                    if (t.getId() == R.id.txtStationID) {
//                        relation.setID(s.toString());
//                    }
//                    else if (t.getId() == R.id.txtExpStations) {
//                        relation.setExpStations(s.toString());
//                    } else if (t.getId() == R.id.txtSlaveStations) {
//                        relation.setSlaveStations(s.toString());
//                    }
//
//                    ImageView img = (ImageView) viewRow.findViewById(R.id.imgEdit);
//                    setImageEditingIcon(img, relation);
//                }
//            });
//        }
//
//        public void setImageEditingIcon(ImageView img, KDSStationsRelation relation)
//        {
//            KDSStationsRelation.EditingState state = checkEditingState(relation, true);
//            ViewHolder holder = (ViewHolder) relation.getTag();
//            if (state == KDSStationsRelation.EditingState.Changed) {
//                if (img != null)
//                    img.setImageResource(R.drawable.edit_24px_16);
//
//            } else if (state != KDSStationsRelation.EditingState.OK) {
//                if (img != null)
//                    img.setImageResource(R.drawable.delete_24px_32);
//            } else {
//                if (img != null)
//                    img.setImageResource(0);
//            }
//        }
//
//        private void init_view_click_event(View v, Object objTag)
//        {
//            v.setTag(objTag);
//            v.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    PreferenceFragmentStations.this.onListItemClicked((View) (v.getTag()));
//                    MyAdapter.this.m_viewEditing = v;
//
//                }
//            });
//        }
//
//        private void buildSlaveFunctionSpinner(Spinner spinner, View convertView,KDSStationsRelation relation )
//        {
//
//            initSlaveFunctionSpinner(getActivity().getApplicationContext(), spinner,relation.getFunction());
//
//
//            if (spinner.getOnItemSelectedListener() == null) {
//                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));
//
//            }
//
//            boolean bChanged = false;
//            KDSSettings.SlaveFunc oldSlaveFunc = relation.getSlaveFunc();
//            int nIndex = ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).getIndex(relation.getSlaveFunc());
//            spinner.setSelection(nIndex);
//            //maybe the slave function was changed. While station function changed, the slave options is different, more or less than before.
//            relation.setSlaveFunc(((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).getSlaveFunction(nIndex).getFunction());
//
//
//            init_view_focus_event(spinner, convertView);
//            bChanged = (oldSlaveFunc != relation.getSlaveFunc());
//
//
//            if (bChanged) {
//                spinner.setSelection(0); //no slave
//                relation.setSlaveFunc(KDSSettings.SlaveFunc.Unknown);
//                relation.setSlaveStations("");
//                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
//                return;
//            }
//
//            if (relation.getSlaveFunc() == KDSSettings.SlaveFunc.Unknown)
//            {
//                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).setTextColor(Color.GRAY);
//
//            }
//            else {
//                ((MySlaveSpinnerArrayAdapter) spinner.getAdapter()).resetTextColor();
//
//            }
//
//        }
//
//        private void buildStationFunctionSpinner2(Spinner spinner, View convertView,KDSStationsRelation relation )
//        {
//
//            initStationfunctionSpinner2(getActivity().getApplicationContext(), spinner,relation);
//
//            if (spinner.getOnItemSelectedListener() == null) {
//                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));
//
//            }
//
//            boolean bChanged = false;
//            KDSSettings.StationFunc oldStationFunc = relation.getFunction();
//            int nIndex = ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).getIndex(relation.getFunction());
//            spinner.setSelection(nIndex);
//            //maybe the slave function was changed. While station function changed, the slave options is different, more or less than before.
//            relation.setFunction( ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).getStationFunction(nIndex).getFunction());
//
//            init_view_focus_event(spinner, convertView);
//            bChanged = (oldStationFunc != relation.getFunction());
//
//            if (bChanged) {
//                spinner.setSelection(0); //no slave
//                relation.setFunction(KDSSettings.StationFunc.Normal);
//                relation.setSlaveFunc(KDSSettings.SlaveFunc.Unknown);
//                relation.setSlaveStations("");
//
//                ((MyStationFuncSpinnerAdapter) spinner.getAdapter()).notifyDataSetChanged();
//                return;
//            }
//
//
//
//        }
//
//        public View getView(int position, View convertView, ViewGroup parent) {
//            //ViewHolder holder = null;
//            KDSStationsRelation r = m_listData.get(position);
//            //boolean bLoadingData = false;
//            ViewHolder viewHolder = null;
//            // viewHolder =(ViewHolder) r.getTag();
//            if ( r.getTag() == null) {
//                viewHolder = new ViewHolder();
//                r.setTag(viewHolder);
//            }
//            else
//            {
//                viewHolder =(ViewHolder) r.getTag();
//            }
//
//            boolean bNewView = false;
//            if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.kdsui_listitem_stations_setting, null);
//                bNewView = true;
//                //bLoadingData = true;
//            }
//
//            convertView.setTag(position);
//            EditText txtStationID = ((EditText) convertView.findViewById(R.id.txtStationID));//
//            viewHolder.m_txtStationID = txtStationID;
//            txtStationID.setText(r.getID());
//            init_view_focus_event(txtStationID, convertView);
//            if (bNewView)
//                init_edittext_changed_event(txtStationID);
//
//            EditText txtExp = ((EditText) convertView.findViewById(R.id.txtExpStations));
//            viewHolder.m_txtExp = txtExp;
//            txtExp.setText(r.getExpStations());
//            init_view_focus_event(txtExp, convertView);
//            if (bNewView)
//                init_edittext_changed_event(txtExp);
//
//            //EditText txtSlave = ((EditText) convertView.findViewById(R.id.txtSlaveStations));
//            TextView txtSlave = ((TextView) convertView.findViewById(R.id.txtSlaveStations));
//            viewHolder.m_txtSlave = txtSlave;
//            txtSlave.setText(r.getSlaveStations());
//            init_view_focus_event(txtSlave, convertView);
//            init_view_click_event(txtSlave, convertView);
//            //init_edittext_changed_event(txtSlave);
//
//            Spinner spinnerSlaveFunc = ((Spinner) convertView.findViewById(R.id.spinnerSlaveFunc));
//            viewHolder.m_spinnerSlaveFunc = spinnerSlaveFunc;
//            spinnerSlaveFunc.setTag(convertView);
//
//            buildSlaveFunctionSpinner(spinnerSlaveFunc, convertView, r);
//
//
//            ImageView imgEdit = ((ImageView) convertView.findViewById(R.id.imgEdit));
//            viewHolder.m_viewImg = imgEdit;
//            init_view_click_event(imgEdit, convertView);
//
//            ImageView imgNetwork = ((ImageView) convertView.findViewById(R.id.imgNetwork));
//            viewHolder.m_viewNetwork = imgNetwork;
//            init_view_click_event(imgNetwork, convertView);
//
//
//
//            Spinner spinner = ((Spinner) convertView.findViewById(R.id.spinnerFunc));//..setText(r.getFunctionString());
//            viewHolder.m_spinnerFunc = spinner;
//            spinner.setTag(convertView);
//
//            buildStationFunctionSpinner2(spinner, convertView, r);
//           // ((MyStationFuncSpinnerAdapter)spinner.getAdapter()).notifyDataSetChanged();
//
//            //
//            spinner = ((Spinner) convertView.findViewById(R.id.spinnerStatus));//..setText(r.getFunctionString());
//            viewHolder.m_spinnerStatus = spinner;
//            spinner.setTag(convertView);
//
//            if (bNewView)
//                initSpinner(getActivity().getApplicationContext(), viewHolder.m_spinnerStatus, Arrays.asList( getActivity().getApplicationContext().getResources().getStringArray(R.array.station_status)));
//
//
//            if (spinner.getOnItemSelectedListener() == null) {
//                spinner.setOnItemSelectedListener(new CustomSpinnerItemSelectedListener(spinner));
//
//            }
//
//            if (r.getStatus() == KDSSettings.StationStatus.Enabled) {
//                spinner.setSelection(0);
//            } else {
//                spinner.setSelection(1);
//            }
//
//            init_view_focus_event(spinner, convertView);
//            setImageEditingIcon(imgEdit, r);
//
//
//            boolean bUnderLineLocalStation = (r.getMac().equals(KDSGlobalVariables.getKDS().getLocalMacAddress()));
//            //{
//
//                TextPaint tp = viewHolder.m_txtStationID.getPaint();
//                tp.setUnderlineText(bUnderLineLocalStation);
//                tp.setFakeBoldText(bUnderLineLocalStation);
//
//            //}
////            else
////            {
////                TextPaint tp = viewHolder.m_txtStationID.getPaint();
////                tp.setFakeBoldText(false);
////                tp.setUnderlineText(false);
////
////            }
//            return convertView;
//
//        }
//    }
//
//    private class SlaveFunction
//    {
//        String m_strDescription = "";
//        KDSSettings.SlaveFunc m_slaveFunction = KDSSettings.SlaveFunc.Unknown;
//
//        public SlaveFunction(String strDescription, KDSSettings.SlaveFunc func)
//        {
//            setDescription(strDescription);
//            setFunction(func);
//        }
//        public void setDescription(String strDescription)
//        {
//            m_strDescription = strDescription;
//        }
//        public  String getDescription()
//        {
//            return m_strDescription;
//        }
//        public  void setFunction(KDSSettings.SlaveFunc func)
//        {
//            m_slaveFunction = func;
//        }
//        public KDSSettings.SlaveFunc getFunction()
//        {
//            return m_slaveFunction;
//        }
//
//        @Override
//        public String toString()
//        {
//            return m_strDescription;
//        }
//    }
//
//    public class MySlaveSpinnerArrayAdapter extends ArrayAdapter<SlaveFunction> {
//        private Context mContext;
//        private List<SlaveFunction> mStringArray;
//        private int m_textColor = Color.BLACK;
//
//        public MySlaveSpinnerArrayAdapter(Context context, List<SlaveFunction> stringArray) {
//            super(context, android.R.layout.simple_spinner_item, stringArray);
//            mContext = context;
//            mStringArray=stringArray;
//        }
//
//        public void setTextColor(int nColor)
//        {
//            m_textColor = nColor;
//        }
//        public void resetTextColor()
//        {
//            m_textColor = Color.BLACK;
//        }
//
//        public SlaveFunction getSlaveFunction(int nPosition)
//        {
//            if (nPosition>=mStringArray.size())
//                return mStringArray.get(0);
//            return mStringArray.get(nPosition);
//        }
//
//        @Override
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//
//            if (convertView == null) {
//                LayoutInflater inflater = LayoutInflater.from(mContext);
//                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
//                convertView.setBackgroundColor(Color.WHITE);
//            }
//
//
//            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
//            tv.setText(mStringArray.get(position).toString());
//            tv.setTextColor(Color.BLACK);
//
//            return convertView;
//
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            // 修改Spinner选择后结果的字体颜色
//            if (convertView == null) {
//                LayoutInflater inflater = LayoutInflater.from(mContext);
//                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
//            }
//
//            //此处text1是Spinner默认的用来显示文字的TextView
//            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
//            tv.setText(mStringArray.get(position).toString());
//
//            if (mStringArray.get(position).getFunction() == KDSSettings.SlaveFunc.Unknown)
//                tv.setTextColor(Color.GRAY);
//            else
//                tv.setTextColor(Color.BLACK);
//
//            tv.setGravity(Gravity.CENTER);
//            return convertView;
//        }
//        public int getIndex( KDSSettings.SlaveFunc func)
//        {
//            for (int i=0; i< mStringArray.size(); i++)
//            {
//                if (mStringArray.get(i).getFunction() == func)
//                    return i;
//            }
//            return 0;
//        }
//
//    }
//    private class StationFunction
//    {
//        String m_strDescription = "";
//        KDSSettings.StationFunc m_stationFunc = KDSSettings.StationFunc.Normal;
//
//        public StationFunction(String strDescription, KDSSettings.StationFunc func)
//        {
//            setDescription(strDescription);
//            setFunction(func);
//        }
//        public void setDescription(String strDescription)
//        {
//            m_strDescription = strDescription;
//        }
//        public  String getDescription()
//        {
//            return m_strDescription;
//        }
//        public  void setFunction(KDSSettings.StationFunc func)
//        {
//            m_stationFunc = func;
//        }
//        public KDSSettings.StationFunc getFunction()
//        {
//            return m_stationFunc;
//        }
//
//        @Override
//        public String toString()
//        {
//            return m_strDescription;
//        }
//    }
//
//
//    public class MyStationFuncSpinnerAdapter extends ArrayAdapter<StationFunction> {
//        private Context mContext;
//        private List<StationFunction> mStringArray;
//        private int m_textColor = Color.BLACK;
//
//        public MyStationFuncSpinnerAdapter(Context context, List<StationFunction> stringArray) {
//            super(context, android.R.layout.simple_spinner_item, stringArray);
//            mContext = context;
//            mStringArray=stringArray;
//        }
//
//        public void setTextColor(int nColor)
//        {
//            m_textColor = nColor;
//        }
//        public void resetTextColor()
//        {
//            m_textColor = Color.BLACK;
//        }
//
//        public StationFunction getStationFunction(int nPosition)
//        {
//            if (nPosition>=mStringArray.size())
//                return mStringArray.get(0);
//            return mStringArray.get(nPosition);
//        }
//
//        @Override
//        public View getDropDownView(int position, View convertView, ViewGroup parent) {
//            //修改Spinner展开后的字体颜色
//            if (convertView == null) {
//                LayoutInflater inflater = LayoutInflater.from(mContext);
//                convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
//                convertView.setBackgroundColor(Color.WHITE);
//            }
//
//            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
//            tv.setText(mStringArray.get(position).toString());
//
//            tv.setTextColor(Color.BLACK);
//
//            return convertView;
//
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            // 修改Spinner选择后结果的字体颜色
//            if (convertView == null) {
//                LayoutInflater inflater = LayoutInflater.from(mContext);
//                convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
//            }
//
//
//            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
//            KDSSettings.StationFunc func = mStringArray.get(position).getFunction();
//
//            int relationPosition = -1;
//            if (((View)parent.getParent()).getTag()!= null) {
//                relationPosition = (int)((View)parent.getParent()).getTag();
//
//            }
//            String text = mStringArray.get(position).getDescription();//.toString();
//            switch (func)
//            {
//
//                case Normal:
//                case Expeditor:
//                case TableTracker:
//                    break;
//
//                case Queue:
//                case Mirror:
//                case Backup:
//                case Workload:
//                case Duplicate:
//                    if (relationPosition >=0 )
//                    {
//                        KDSStationsRelation relation = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getListData().get(relationPosition);
//                        KDSStationsRelation relationPrimary =((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getMyPrimaryRelation(relation.getID());
//                        //String primary = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getMyPrimaryStation(relation.getID());
//                        if (text.indexOf(">")<0)
//                        {
//                            // if (!primary.isEmpty()) {
//                            if (relationPrimary != null){
//                                //text = "#" +primary + "'s " + text;
//                                //text += " [" + primary + "]";
//                                if (func == KDSSettings.StationFunc.Backup) {
//                                    KDSSettings.StationFunc primaryFunc = ((MyAdapter) PreferenceFragmentStations.this.m_lstStations.getAdapter()).getStationChainFunction(relationPrimary.getID(), "");
//
//                                    if (primaryFunc == KDSSettings.StationFunc.Expeditor ||
//                                            primaryFunc == KDSSettings.StationFunc.Queue_Expo)
//                                        text = this.getContext().getString(R.string.expo_backup);//"Expo/Backup";
//                                    else if (primaryFunc == KDSSettings.StationFunc.Normal)
//                                        text = this.getContext().getString(R.string.prep_backup);// "Prep/Backup";
//                                }
//                                text += " -> " + relationPrimary.getID() ;
//
//                            }
//                        }
//                    }
//                    break;
//
//
//            }
//
//            tv.setText(text);
//            tv.setTextColor(m_textColor);
//
//            tv.setGravity(Gravity.CENTER);
//
//            return convertView;
//        }
//        public int getIndex( KDSSettings.StationFunc func)
//        {
//            for (int i=0; i< mStringArray.size(); i++)
//            {
//                if (mStringArray.get(i).getFunction() == func)
//                    return i;
//            }
//            return 0;
//        }
//
//    }
//
//}
//
