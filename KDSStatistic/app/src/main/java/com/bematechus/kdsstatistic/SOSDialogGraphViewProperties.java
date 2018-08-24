package com.bematechus.kdsstatistic;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;

/**
 * Created by David.Wong on 2018/5/16.
 * Rev:
 */
public class SOSDialogGraphViewProperties extends SOSDialogPropertiesBase  {

    SOSGraphView m_graphView = null;
    SOSSettings m_settings = null;

    public void onOkClicked()
    {
        saveProperties(m_graphView.m_properties);
        m_graphView.showDemo();
    }

    public SOSDialogGraphViewProperties(final Context context, SOSGraphView view, KDSUIDialogBase.KDSDialogBaseListener listener, SOSSettings settings) {
        this.int_dialog(context, listener, R.layout.sos_dialog_graph_preperties, "");
        m_graphView = view;
        m_settings = settings;
        init();
        this.setTitle(context.getString(R.string.graph_prop));

        showProperties(view.m_properties);

    }

    public void init()
    {
        Button btn = (Button) this.getView().findViewById(R.id.btnGraphColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);
            }
        });

        btn = (Button) this.getView().findViewById(R.id.btnYColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);
            }
        });

        btn = (Button) this.getView().findViewById(R.id.btnTarget);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseDuration(v);
            }
        });

    }



    private void showProperties(SOSGraphViewProperties p)
    {
        TextView t = (TextView) this.getView().findViewById(R.id.txtStationID);
        t.setText(p.m_stationID);


        t = (TextView) this.getView().findViewById(R.id.txtTitle);
        t.setText(p.m_strTitle);
        t.setHint(KDSApplication.getContext().getString( R.string.station_number));

        t = (TextView) this.getView().findViewById(R.id.txtTitleX);
        t.setText(p.m_strTitleX);
        t.setHint(m_settings.getString(SOSSettings.ID.Graph_x_title));

        t = (TextView) this.getView().findViewById(R.id.txtTitleY);
        t.setText(p.m_strTitleY);
        t.setHint(m_settings.getString(SOSSettings.ID.Graph_y_title));


        Button btn = (Button) this.getView().findViewById(R.id.btnGraphColor);
        showButtonColor(btn, p.m_defaultColor.getBG(), p.m_defaultColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnYColor);
        showButtonColor(btn, p.m_yColor.getBG(), p.m_yColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnTarget);
        showButtonTime(btn, p.getTargetSeconds());
    }

    private void saveProperties(SOSGraphViewProperties p)
    {
        TextView t = (TextView) this.getView().findViewById(R.id.txtStationID);
        p.m_stationID = t.getText().toString();

        t = (TextView) this.getView().findViewById(R.id.txtTitle);
        p.m_strTitle = t.getText().toString();

        t = (TextView) this.getView().findViewById(R.id.txtTitleX);
        p.m_strTitleX = t.getText().toString();

        t = (TextView) this.getView().findViewById(R.id.txtTitleY);
        p.m_strTitleY = t.getText().toString();

        Button btn = (Button) this.getView().findViewById(R.id.btnGraphColor);
        p.m_defaultColor.setBG( getButtonBG(btn, p.m_defaultColor.getBG()));
        p.m_defaultColor.setFG( getButtonFG(btn, p.m_defaultColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnYColor);
        p.m_yColor.setBG(  getButtonBG(btn, p.m_yColor.getBG()));
        p.m_yColor.setFG(  getButtonFG(btn, p.m_yColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnTarget);
        p.setTargetSeconds(getButtonTime(btn));

    }

}
