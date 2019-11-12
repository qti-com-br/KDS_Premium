package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.bematechus.kdslib.KDSBumpBarKeyFunc;
import com.bematechus.kdslib.KDSKbdRecorder;

import java.util.List;

/**
 * Created by Administrator on 2017/4/10.
 */
public class KDSUIDlgTTMap  extends KDSUIDialogBase {

    ListView m_lstView = null;
    TTHolderMaps m_maps = new TTHolderMaps();
    String m_strMaps = "";

    @Override
    public void onOkClicked()
    {//save data here
        m_strMaps = m_maps.toString();

    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_strMaps;

    }

    public KDSUIDlgTTMap(final Context context, String strOriginal, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener,R.layout.kdsui_dlg_tt_map, "");
        //this.setTitle("Table Tracker Map");
        //get all widgets
        m_lstView = (ListView) this.getView().findViewById(R.id.lstData);
        m_maps = TTHolderMaps.parseString(strOriginal);
        m_lstView.setAdapter(new MyAdapter(context,m_maps.getArray() ));
        m_strMaps = strOriginal;

        Button btn = (Button) this.getView().findViewById(R.id.btnNew);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnNewClicked();
            }
        });
        btn = (Button) this.getView().findViewById(R.id.btnRemove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnRemoveClicked();
            }
        });





    }

    private void onBtnNewClicked()
    {
        m_maps.getArray().add(new TTHolderMaps.TTHolderMap());
        ((MyAdapter)m_lstView.getAdapter()).notifyDataSetChanged();
    }

    private void onBtnRemoveClicked()
    {
        TTHolderMaps.TTHolderMap map = ((MyAdapter)m_lstView.getAdapter()).getFocused();
        m_maps.getArray().remove(map);
        ((MyAdapter)m_lstView.getAdapter()).focus(null);
        ((MyAdapter)m_lstView.getAdapter()).notifyDataSetChanged();

    }

    protected String makeButtonText2(Context context, int nResID, KDSSettings.ID funcKey )
    {
        String s = context.getString(nResID);

        String strFunc = "";

        if (funcKey == KDSSettings.ID.Bumpbar_OK) {
            KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
            func.setKeyCode(KeyEvent.KEYCODE_ENTER);
            strFunc = func.getSummaryString(KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKbdType());
            //strFunc = KDSSettings.getOkKeyString(context);
        }
        else if (funcKey == KDSSettings.ID.Bumpbar_Cancel){
            KDSBumpBarKeyFunc func = new KDSBumpBarKeyFunc();
            func.setCtrl(true);
            strFunc = func.getSummaryString( KDSGlobalVariables.getKDS().getBumpbarKeysFunc().getKbdType());


        }
        if (!strFunc.isEmpty())
            s = s  +  strFunc ;
        //s = s  + "[" + strFunc +"]";
        return s;
    }

    protected void init_dialog_events(final AlertDialog dlg)
    {
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // KDSSettings.ID evID = KDSGlobalVariables.getKDS().checkKDSDlgKbdEvent(event, null);
                if (event.getRepeatCount() > 0) return false;
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                KDSSettings.ID evID = KDSSettings.ID.NULL;
                if (keyCode == KeyEvent.KEYCODE_CTRL_LEFT ||
                        keyCode == KeyEvent.KEYCODE_CTRL_RIGHT)
                    evID =  KDSSettings.ID.Bumpbar_Cancel;
                else if (keyCode == KeyEvent.KEYCODE_ENTER)
                    evID =  KDSSettings.ID.Bumpbar_OK;

                if (evID == KDSSettings.ID.Bumpbar_OK) {
                    onOkClicked();
                    dialog.dismiss();
                    if (KDSUIDlgTTMap.this.listener != null)
                        KDSUIDlgTTMap.this.listener.onKDSDialogOK(KDSUIDlgTTMap.this, getResult());
                    return true;
                } else if (evID == KDSSettings.ID.Bumpbar_Cancel) {
                    dialog.cancel();
                    if (KDSUIDlgTTMap.this.listener != null)
                        KDSUIDlgTTMap.this.listener.onKDSDialogCancel(KDSUIDlgTTMap.this);
                    return true;
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });
    }




    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        //public List<Map<String, Object>> m_listData; //KDSStationsRelation class array
        public List<TTHolderMaps.TTHolderMap> m_listData; //KDSStationsRelation class array
        TTHolderMaps.TTHolderMap m_focusedMap = null;

        public MyAdapter(Context context, List<TTHolderMaps.TTHolderMap> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<TTHolderMaps.TTHolderMap> getListData()
        {
            return m_listData;
        }
        public void setListData(List<TTHolderMaps.TTHolderMap> lst)
        {
            m_listData = lst;
        }
        public int getCount() {

            return m_listData.size();
        }
        public TTHolderMaps.TTHolderMap getFocused()
        {
            return m_focusedMap;
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        private void focus(TTHolderMaps.TTHolderMap map)
        {
            m_focusedMap = map;
        }
        private void initTextView(View convertView)
        {
            EditText holderID =  ((EditText) convertView.findViewById(R.id.txtHolderID));
            holderID.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText t = (EditText) v;
                    m_bEnabledEdit = hasFocus;

                    if (!hasFocus)
                    {

                        ((TTHolderMaps.TTHolderMap)t.getTag()).setHolderID( t.getText().toString());
                        //focus(null);
                    }
                    else
                    {
                        focus(((TTHolderMaps.TTHolderMap)t.getTag()));
                    }
                }
            });
            holderID.addTextChangedListener(new MyTextWatcher(holderID, MyTextWatcher.Holder_ID));

            EditText tableName =  ((EditText) convertView.findViewById(R.id.txtTableName));
           tableName.addTextChangedListener(new MyTextWatcher(tableName, MyTextWatcher.Table_Name));
            tableName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    EditText t = (EditText) v;
                    m_bEnabledEdit = hasFocus;
                    if (!hasFocus)
                    {

                        //focus(null);
                        ((TTHolderMaps.TTHolderMap)t.getTag()).setTableName(t.getText().toString());
                    }
                    else
                    {
                        focus(((TTHolderMaps.TTHolderMap)t.getTag()));
                    }
                }
            });
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            TTHolderMaps.TTHolderMap r =  m_listData.get(position);
            if (convertView == null) {
                //holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listitem_tt_map, null);
                initTextView(convertView);

            }
            else
            {


            }
            convertView.setTag(r);
            ((EditText) convertView.findViewById(R.id.txtHolderID)).setTag(r);
            ((EditText) convertView.findViewById(R.id.txtHolderID)).setText(r.getHolderID());

            ((EditText) convertView.findViewById(R.id.txtTableName)).setTag(r);
            ((EditText) convertView.findViewById(R.id.txtTableName)).setText(r.getTableName());


            return convertView;
        }

    }
    boolean m_bEnabledEdit = false;
    private class  MyTextWatcher implements TextWatcher
    {
        EditText m_txt = null;
        static final int Holder_ID = 0;
        static final int Table_Name = 1;

        int m_nField = 0;

        public MyTextWatcher(EditText t, int nField)
        {
            m_txt = t;
            m_nField = nField;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (m_txt == null) return;
            if (m_txt.getTag() == null) return;
            if (!m_bEnabledEdit) return;
            switch (m_nField)
            {
                case Holder_ID:
                {

                    ((TTHolderMaps.TTHolderMap) m_txt.getTag()).setHolderID(s.toString());
                }
                break;
                case Table_Name:
                {
                    ((TTHolderMaps.TTHolderMap) m_txt.getTag()).setTableName(s.toString());
                }
                break;
            }

        }
    }




}
