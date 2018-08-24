package com.bematechus.kdslib;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2016/8/24.
 */
public class MySpinnerArrayAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private List<String> mStringArray;
    private int m_textColor = Color.BLACK;

    public MySpinnerArrayAdapter(Context context, List<String> stringArray) {
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
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        //修改Spinner展开后的字体颜色
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent,false);
            convertView.setBackgroundColor(Color.WHITE);
        }

        //此处text1是Spinner默认的用来显示文字的TextView
        TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
        tv.setText(mStringArray.get(position));
        // tv.setTextSize(22f);
        tv.setTextColor(Color.BLACK);
        //tv.setGravity(Gravity.CENTER);
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
        tv.setText(mStringArray.get(position));
        //tv.setTextSize(15f);
        //tv.setTextColor(Color.BLACK);
        tv.setTextColor(m_textColor);
        //tv.setTextColor(Color.RED);
        tv.setGravity(Gravity.CENTER);
//            if (!convertView.isEnabled())
//            {
//                tv.setTextColor(Color.GRAY);
//            }

        return convertView;
    }

}
