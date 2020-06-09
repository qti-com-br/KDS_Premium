package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.Activation;
import com.bematechus.kdslib.ActivityLogin;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.StationAnnounceEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static com.bematechus.kdslib.Activation.getDevices;

/**
 * Created by Administrator on 2016/2/1 0001.
 */
public class KDSUIDialogInputID extends KDSUIDialogBase implements StationAnnounceEvents {
    TextView m_txtText = null;
    TextView m_txtDescription = null;
    String m_stationID = "";
    ListView m_lstStations = null;
    ArrayList<String> m_lstIPs = new ArrayList<String>();

   // public static KDSUIDialogInputID m_instance = null;

    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_stationID;
    }
    public void onOkClicked()
    {
        m_stationID = m_txtText.getText().toString();
    }
    @Override
    public AlertDialog createOkButtonsDialog(Context context)
    {
        //String strOK = makeButtonText(context, android.R.string.ok, KDSSettings.ID.Bumpbar_OK);

//        String strOK = context.getString( R.string.ok);
//
//        String strFunc = "[Enter]";
//
//        strOK = strOK  + strFunc ;
//
        String strOK = makeCtrlEnterButtonText(context, context.getString( R.string.ok), DialogEvent.OK);// KDSSettings.ID.Bumpbar_OK );
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ( ((String)getResult()).isEmpty())
                            return;

                        onOkButtonClicked();

                        //saveToSmb();
                        if (KDSUIDialogInputID.this.listener != null) {
                            KDSUIDialogInputID.this.listener.onKDSDialogOK(KDSUIDialogInputID.this, getResult());
                        }
                    }
                })
                .create();
        return d;
    }

    public void init_input_dialog(Context context, KDSDialogBaseListener listener, int resDlgID, boolean bCancelButton) {
        this.listener = listener;
        m_view = LayoutInflater.from(context).inflate(resDlgID, null);
        if (!bCancelButton)
            dialog = createOkButtonsDialog(context);
        else
            dialog = createOkCancelButtonsDialog(context);
        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
//                if (m_kdsUser == null)
//                    return false;
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    return false;
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER )

                {
                    if (!KDSUIDialogInputID.this.onOkButtonClicked())
                        return true;
                    dialog.dismiss();
                    if (KDSUIDialogInputID.this.listener != null) {
                        KDSUIDialogInputID.this.listener.onKDSDialogOK(KDSUIDialogInputID.this, getResult());
                    }
                    return true;
                }
                //onKeyPressed(event);
                return false;
            }
        });

        if (bCancelButton)
        {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (KDSUIDialogInputID.this.listener != null) {
                        KDSUIDialogInputID.this.listener.onKDSDialogCancel(KDSUIDialogInputID.this);
                    }
                }
            });
        };

    }

    public KDSUIDialogInputID(final Context context,String strTitle, String strDescription, String strInitText, KDSUIDialogBase.KDSDialogBaseListener listener) {
        buildDialog(context, strTitle, strDescription, strInitText, listener, false);

//        this.init_input_dialog(context, listener, R.layout.kdsui_dlg_input_id);//, "");
//        this.setTitle(strTitle);
//        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
//        m_txtText.setText(strInitText);
//        m_txtText.setSelected(true); //kpp1-302
//        m_txtDescription = (TextView)this.getView().findViewById(R.id.txtInfo);
//        m_txtDescription.setText(strDescription);
//        m_lstStations = (ListView)this.getView().findViewById(R.id.lstStations);
//
//        m_lstStations.setAdapter(new ArrayAdapter<String>(context,
//                android.R.layout.simple_list_item_1, m_lstIPs));
//
//        this.getDialog().setCancelable(false);//.setFinishOnTouchOutside(false);
//        this.getDialog().setCanceledOnTouchOutside(false);


    }
    public void showErrorMessage(String strMsg)
    {
        AlertDialog d = new AlertDialog.Builder(this.getDialog().getContext())
                .setTitle(this.getDialog().getContext().getString(R.string.error))
                .setMessage(strMsg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                )
                .create();
        d.show();
    }
    public boolean onOkButtonClicked()
    {
        onOkClicked();
        String id = (String)getResult();

        if ( id.isEmpty()) {
            showErrorMessage( this.getDialog().getContext().getString(R.string.error_id_is_empty));

            return false;
        }
        if (id.length() >2) //just accept 0 -- 99
        {
            showErrorMessage( this.getDialog().getContext().getString(R.string.error_id_out_range));
            return false;
        }

        //kpp1-298 don't allow station id 0.
        if (!KDSUtil.isValidStationID(id))
        {
            showErrorMessage( this.getDialog().getContext().getString(R.string.error_id_out_range));
            return false;
        }


        if (findStationByID(id)) {
            String s = this.getDialog().getContext().getString(R.string.error_isnot_unique_id);
            s = s.replace("#", "#"+id);
            showErrorMessage(s);
            return false;
        }
        //saveToSmb();
        if (KDSUIDialogInputID.this.listener != null) {
            KDSUIDialogInputID.this.listener.onKDSDialogOK(KDSUIDialogInputID.this, getResult());
        }
        return  true;
    }
    public void show() {
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (KDSUIDialogInputID.this.onOkButtonClicked())
                    dialog.dismiss();

            }
        });
    }
    private boolean findStationByIP(String ip)
    {


        int ncount = m_lstIPs.size();
        for (int i=0; i< ncount; i++) {
            String s = m_lstIPs.get(i);
            int index =  s.indexOf(":");
            if (index<0) continue;
            String strIP = s.substring(index + 1);
            strIP = strIP.trim();
            if (strIP.equals(ip))
                return true;

        }
        return false;
    }

    private boolean findStationByID(String id)
    {
        int ncount = m_lstIPs.size();
        for (int i=0; i< ncount; i++) {
            String s = m_lstIPs.get(i);
            int index =  s.indexOf(":");
            if (index<0) continue;
            String strID = s.substring(0, index);
            strID = strID.trim();
            if (strID.equals(id))
                return true;
        }
        if (Activation.findDuplicatedDeviceIDAfterSetNewID(id))
            return true;
        return false;
    }
    public void onReceivedStationAnnounce(KDSStationIP stationReceived)//String stationID, String ip, String port, String mac)
    {

            String localIP =  KDSGlobalVariables.getKDS().getLocalIpAddress();
            if (localIP.equals(stationReceived.getIP()))
                return;



        if (findStationByIP(stationReceived.getIP()))
            return;

        m_lstIPs.add(stationReceived.getID() + " : " + stationReceived.getIP());
        sortStations();
        ((ArrayAdapter) m_lstStations.getAdapter()).notifyDataSetChanged();

    }
    private void sortStations()
    {
        Collections.sort(m_lstIPs, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }

    /**
     * KPP1-302 Duplicate Station IDs
     * @param context
     * @param strTitle
     * @param strDescription
     * @param strInitText
     * @param listener
     * @param bCancelButton
     */
    private void buildDialog(final Context context,String strTitle, String strDescription, String strInitText, KDSUIDialogBase.KDSDialogBaseListener listener, boolean bCancelButton)
    {
        this.init_input_dialog(context, listener, R.layout.kdsui_dlg_input_id, bCancelButton );//, "");
        this.setTitle(strTitle);
        m_txtText = (TextView)this.getView().findViewById(R.id.txtText);
        m_txtText.setText(strInitText);
        if (bCancelButton)
            m_txtText.setSelectAllOnFocus(true); //kpp1-302
        m_txtDescription = (TextView)this.getView().findViewById(R.id.txtInfo);
        m_txtDescription.setText(strDescription);
        m_lstStations = (ListView)this.getView().findViewById(R.id.lstStations);

        m_lstStations.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_1, m_lstIPs));
        if (!bCancelButton) { //In prefenece settings, we allow cancel it by click outside.
            this.getDialog().setCancelable(false);//.setFinishOnTouchOutside(false);
            this.getDialog().setCanceledOnTouchOutside(false);
        }

    }

    /**
     * KPP1-302 Duplicate Station IDs
     * @param context
     * @param strTitle
     * @param strDescription
     * @param strInitText
     * @param listener
     * @param bCancelButton
     */
    public KDSUIDialogInputID(final Context context,String strTitle, String strDescription, String strInitText, KDSUIDialogBase.KDSDialogBaseListener listener, boolean bCancelButton) {

        buildDialog(context, strTitle, strDescription, strInitText, listener, bCancelButton);

    }

    /**
     * KPP1-302 Duplicate Station IDs
     * @param context
     * @return
     */
    public AlertDialog createOkCancelButtonsDialog(Context context)
    {

        String strOK = makeCtrlEnterButtonText(context, context.getString( R.string.ok), DialogEvent.OK);// KDSSettings.ID.Bumpbar_OK );
        String strCancel = makeCtrlEnterButtonText(context, context.getString( R.string.cancel), DialogEvent.Cancel);// KDSSettings.ID.Bumpbar_OK );
        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(strOK, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ( ((String)getResult()).isEmpty())
                            return;

                        onOkButtonClicked();

                        //saveToSmb();
                        if (KDSUIDialogInputID.this.listener != null) {
                            KDSUIDialogInputID.this.listener.onKDSDialogOK(KDSUIDialogInputID.this, getResult());
                        }
                    }
                })
                .setNegativeButton(strCancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onCancelButtonClicked();
                    }
                })
                .create();
        return d;
    }

    /**
     * KPP1-302 Duplicate Station IDs
     * @return
     */
    public boolean onCancelButtonClicked()
    {
        if (KDSUIDialogInputID.this.listener != null) {
            KDSUIDialogInputID.this.listener.onKDSDialogCancel(KDSUIDialogInputID.this);
        }
        return true;
    }
}
