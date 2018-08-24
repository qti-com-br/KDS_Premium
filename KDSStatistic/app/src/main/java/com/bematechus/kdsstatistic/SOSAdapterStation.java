package com.bematechus.kdsstatistic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2016/8/1.
 */
public class SOSAdapterStation extends BaseAdapter {
    private LayoutInflater mInflater;
    //public List<Map<String, Object>> m_listData; //KDSStationsRelation class array
    public List<SOSKDSStationSOSInfo> m_listData; //KDSStationsRelation class array


    public SOSAdapterStation(Context context, List<SOSKDSStationSOSInfo> data) {
        this.mInflater = LayoutInflater.from(context);
        m_listData = data;
    }
    public List<SOSKDSStationSOSInfo> getListData()
    {
        return m_listData;
    }
    public void setListData(List<SOSKDSStationSOSInfo> lst)
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
        SOSKDSStationSOSInfo r =  m_listData.get(position);
        if (convertView == null) {
            //holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.sos_listitem_station, null);
//                // convertView.setTag(r);
////                holder.m_txtNumber = (TextView) convertView.findViewById(R.id.txtNumber);
////                holder.m_txtIP = (TextView) convertView.findViewById(R.id.txtIP);
////                holder.m_txtPort = (TextView) convertView.findViewById(R.id.txtPort);
////
////                convertView.setTag(holder);
//
//                convertView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (null != mListener) {
//                            // Notify the active callbacks interface (the activity, if the
//                            // fragment is attached to one) that an item has been selected.
//                            mListener.onCategoryListFragmentInteraction((KDSRouterDataCategory) (v.getTag()));
//                        }
//                    }
//
//
//                });



        }
        else
        {


        }
        convertView.setTag(r);

        ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());

        ((TextView) convertView.findViewById(R.id.txtIP)).setText(r.getIP());
        //((TextView) convertView.findViewById(R.id.txtIP)).setText("892.999.999.999");
      //  KDSStationSOSInfo SOSInfo = (KDSStationSOSInfo)r;
        ((TextView) convertView.findViewById(R.id.txtInfo)).setText(r.getInfo());
       // ((TextView) convertView.findViewById(R.id.txtLastDate)).setText(r.getLastUpdateDateString());
        ProgressBar pb = ((ProgressBar) convertView.findViewById(R.id.pbUpdating));
        if (r.getStatus() == SOSKDSStationSOSInfo.StationSOSStatus.Updating)
            pb.setVisibility(View.VISIBLE);
        else
            pb.setVisibility(View.GONE);
//        pb.setMax(100);
//        if (r.getTotalSize() >0) {
//            pb.setVisibility(View.VISIBLE);
//            int n =(int) (((float)r.getFinishedSize()/(float)r.getTotalSize()) * 100);
//            pb.setProgress(n);
//        }
//        else
//            pb.setVisibility(View.GONE);

        return convertView;
    }
}
