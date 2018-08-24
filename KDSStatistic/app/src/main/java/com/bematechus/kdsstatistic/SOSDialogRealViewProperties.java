package com.bematechus.kdsstatistic;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by David.Wong on 2018/5/15.
 * Rev:
 */
public class SOSDialogRealViewProperties extends SOSDialogPropertiesBase {

    SOSRealTimeView m_realView = null;


    public void onOkClicked()
    {
        saveProperties(m_realView.m_properties);
    }

    public SOSDialogRealViewProperties(final Context context, SOSRealTimeView realView, KDSUIDialogBase.KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.sos_dialog_realview_properties, "");
        m_realView = realView;
        init();
        this.setTitle(context.getString(R.string.real_prop));

        showProperties(realView.m_properties);

        //m_properties.copyFrom(properties);


        //m_txtText = (TextView)this.getView().findViewById(R.id.txtText);


    }

    public void init()
    {
        Button btn = (Button) this.getView().findViewById(R.id.btnBorderColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);


            }
        });

        btn = (Button) this.getView().findViewById(R.id.btnPercentColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);
            }
        });

        btn = (Button) this.getView().findViewById(R.id.btnRealColor);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseColor(v);
            }
        });

        btn = (Button) this.getView().findViewById(R.id.btnCountColor);
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



    private void showProperties(SOSRealViewProperties p)
    {
        TextView t = (TextView) this.getView().findViewById(R.id.txtStationID);
        t.setText(p.m_stationID);

        t = (TextView) this.getView().findViewById(R.id.txtTitle);
        t.setText(p.m_strTitle);

        Button btn = (Button) this.getView().findViewById(R.id.btnBorderColor);
        showButtonColor(btn, p.m_borderColor.getBG(), p.m_borderColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnPercentColor);
        showButtonColor(btn, p.m_percentColor.getBG(), p.m_percentColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnRealColor);
        showButtonColor(btn, p.m_realColor.getBG(), p.m_realColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnAlertColor);
        showButtonColor(btn, p.m_alertColor.getBG(), p.m_alertColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnCountColor);
        showButtonColor(btn, p.m_countColor.getBG(), p.m_countColor.getFG());

        btn = (Button) this.getView().findViewById(R.id.btnTarget);
        showButtonTime(btn, p.getTargetSeconds());
    }

    private void saveProperties(SOSRealViewProperties p)
    {
        TextView t = (TextView) this.getView().findViewById(R.id.txtStationID);
        p.m_stationID = t.getText().toString();

        t = (TextView) this.getView().findViewById(R.id.txtTitle);
        p.m_strTitle = t.getText().toString();

        Button btn = (Button) this.getView().findViewById(R.id.btnBorderColor);
        p.m_borderColor.setBG( getButtonBG(btn, p.m_borderColor.getBG()));
        p.m_borderColor.setFG( getButtonFG(btn, p.m_borderColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnPercentColor);
        p.m_percentColor.setBG(  getButtonBG(btn, p.m_percentColor.getBG()));
        p.m_percentColor.setFG(  getButtonFG(btn, p.m_percentColor.getFG()));


        btn = (Button) this.getView().findViewById(R.id.btnRealColor);
        p.m_realColor.setBG(  getButtonBG(btn, p.m_realColor.getBG()));
        p.m_realColor.setFG(  getButtonFG(btn, p.m_realColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnAlertColor);
        p.m_alertColor.setBG(  getButtonBG(btn, p.m_alertColor.getBG()));
        p.m_alertColor.setFG(  getButtonFG(btn, p.m_alertColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnCountColor);
        p.m_countColor.setBG(  getButtonBG(btn, p.m_countColor.getBG()));
        p.m_countColor.setFG(  getButtonFG(btn, p.m_countColor.getFG()));

        btn = (Button) this.getView().findViewById(R.id.btnTarget);
        p.setTargetSeconds(getButtonTime(btn));

    }


}
