package com.bematechus.kdslib;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2016/1/24 0024.
 */
public class KDSToast {

    static public void showStationID(Context context, String stationID)
    {
        Toast toastCustom = new Toast(context);
        LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.kdsui_showid, null);
        TextView t = (TextView)v.findViewById(R.id.txtText);
        t.setText(stationID);
        toastCustom.setView(v);
        toastCustom.setDuration(Toast.LENGTH_LONG);
        toastCustom.setGravity(Gravity.CENTER, 0, 0);
        toastCustom.show();
    }

    static public void showMessage(Context context, String message)
    {

        int duration = Toast.LENGTH_SHORT;
        Toast t = Toast.makeText(context, message, duration);
        t.show();
    }
}
