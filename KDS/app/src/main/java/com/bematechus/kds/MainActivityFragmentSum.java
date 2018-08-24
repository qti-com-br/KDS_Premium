package com.bematechus.kds;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSBGFG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class MainActivityFragmentSum extends Fragment {

    ListView m_lstSum = null;
    TextView m_txtSumTitle = null;

    ArrayList<Map<String,Object>> m_arData= new ArrayList<Map<String,Object>>();
    public MainActivityFragmentSum() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        m_lstSum = (ListView)view.findViewById(R.id.lstSum);
        m_txtSumTitle= (TextView)view.findViewById(R.id.txtSumTitle);

        SumSimpleAdapter adapter = new SumSimpleAdapter(this.getActivity().getApplicationContext(),m_arData,R.layout.listitem_summary,
                new String[]{"qty","name"},new int[]{android.R.id.text1,android.R.id.text2});
        m_lstSum.setAdapter(adapter);

        return view;
    }


    public void refreshSummary()
    {
        refreshSummary(KDSUser.USER.USER_A);
    }

    public void refreshSummary(KDSUser.USER userID)
    {

        if (!((MainActivity)this.getActivity()).isSummaryVisible(userID))
            return;
        m_arData.clear();
        KDS kds = KDSGlobalVariables.getKDS();

        String bgfg = kds.getSettings().getString(KDSSettings.ID.Sum_bgfg);
        KDSBGFG bf = KDSBGFG.parseString(bgfg);
        m_lstSum.setBackgroundColor(bf.getBG());

        boolean advSumEnabled = kds.getSettings().getBoolean(KDSSettings.ID.AdvSum_enabled);
        boolean bSmartEnabled = kds.getSettings().getBoolean(KDSSettings.ID.Smart_Order_Enabled);
        //boolean bSmartEnabled = (KDSSettings.SmartMode.values()[ kds.getSettings().getInt(KDSSettings.ID.Smart_mode)] == KDSSettings.SmartMode.Normal );

        ArrayList<KDSSummaryItem> arSumItems = kds.summary(userID);
        if (arSumItems == null) return;
        for (int i=0; i< arSumItems.size(); i++)
        {
            Map<String,Object> item = new HashMap<String,Object>();
            if (bSmartEnabled && advSumEnabled)
                item.put("qty", arSumItems.get(i).getAdvSumQtyString());
            else
                item.put("qty", arSumItems.get(i).getQtyString());
            item.put("name", arSumItems.get(i).getDescription(false));
            m_arData.add(item);
        }
        ((SimpleAdapter) m_lstSum.getAdapter()).notifyDataSetChanged();

        if (kds.isMultpleUsersMode())
        {
            if (userID == KDSUser.USER.USER_A) {
                String textA = kds.getSettings().getString(KDSSettings.ID.Screen_subtitle_a_text);
                String s = this.getActivity().getString(R.string.summary_for);
                s += " " + textA;
                m_txtSumTitle.setText(s);
            }
            else
            {
                String textB = kds.getSettings().getString(KDSSettings.ID.Screen_subtitle_b_text);
                String s = this.getActivity().getString(R.string.summary_for);
                s += " " + textB;
                m_txtSumTitle.setText(s);
            }
        }
        else
        {
            String s = this.getActivity().getString(R.string.summary);
            m_txtSumTitle.setText(s);
        }
    }

    class SumSimpleAdapter extends SimpleAdapter
    {
        public SumSimpleAdapter(Context context, List<? extends Map<String, ?>> data,
                                int resource, String[] from, int[] to)
        {
            super(context, data, resource, from, to);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            String bgfg = KDSGlobalVariables.getKDS().getSettings().getString(KDSSettings.ID.Sum_bgfg);
            KDSBGFG bf = KDSBGFG.parseString(bgfg);
            //m_viewTopSum.setBackgroundColor(bf.getBG());
            ((TextView)v.findViewById(android.R.id.text1)).setTextColor(bf.getFG());
            ((TextView)v.findViewById(android.R.id.text2)).setTextColor(bf.getFG());
            return v;
        }

    }
}
