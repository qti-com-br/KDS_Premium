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
public class STAdapterStation  extends BaseAdapter {
    private LayoutInflater mInflater;

    public List<STStationStatisticInfo> m_listData;


    public STAdapterStation(Context context, List<STStationStatisticInfo> data) {
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

        STStationStatisticInfo r =  m_listData.get(position);
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.st_listitem_station, null);
        }
        else
        {

        }
        convertView.setTag(r);
        ((TextView) convertView.findViewById(R.id.txtID)).setText(r.getID());
        ((TextView) convertView.findViewById(R.id.txtIP)).setText(r.getIP());
        ((TextView) convertView.findViewById(R.id.txtInfo)).setText(r.getInfo());
        ProgressBar pb = ((ProgressBar) convertView.findViewById(R.id.pbUpdating));
        pb.setMax(100);
        if (r.getTotalSize() >0) {
            pb.setVisibility(View.VISIBLE);
            int n =(int) (((float)r.getFinishedSize()/(float)r.getTotalSize()) * 100);
            pb.setProgress(n);
        }
        else
            pb.setVisibility(View.GONE);

        return convertView;
    }
}
