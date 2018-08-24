package com.bematechus.kdsstatistic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 *
 */
public class STAdapterReportType extends BaseAdapter {


    private LayoutInflater mInflater;

    public List<STReportTypeDescription> m_listData;


    public STAdapterReportType(Context context, List<STReportTypeDescription> data) {
        this.mInflater = LayoutInflater.from(context);
        m_listData = data;
    }
    public List<STReportTypeDescription> getListData()
    {
        return m_listData;
    }
    public void setListData(List<STReportTypeDescription> lst)
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

        STReportTypeDescription r =  m_listData.get(position);
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.st_listitem_report_type, null);
        }
        else
        {


        }
        convertView.setTag(r);
        ((ImageView) convertView.findViewById(R.id.imgReport)).setImageResource(r.m_nIconID);
        ((TextView) convertView.findViewById(R.id.txtReportType)).setText(r.m_strReport);
        ((TextView) convertView.findViewById(R.id.txtReportDescription)).setText(r.m_strReportDescription);
        return convertView;
    }

}
