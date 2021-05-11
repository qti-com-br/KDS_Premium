package com.bematechus.kds;

import android.content.Context;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSPOSMessage;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;

public class POSMessageDlg {

    enum POSMsgDlgEvent
    {
        Closed,
    }
    interface POSMsgDlgEvents
    {
        public void onPOSMsgDlgEvent(POSMsgDlgEvent nEvent, ArrayList<Object> arParams);
    }

    POSMsgDlgEvents m_receiver = null;

    PopupWindow mPopWindow = null;
    View mView = null;
    KDSViewFontFace mFont = null;
    ///////////////////////////////////////////////////////

    public void setReceiver(POSMsgDlgEvents receiver)
    {
        m_receiver = receiver;
    }


    public POSMessageDlg(KDSViewFontFace font)
    {
        mFont = font;
    }
    private View createView(KDSPOSMessage message)
    {

        Context context = KDSApplication.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        mView = inflater.inflate(R.layout.kdsui_dlg_pos_msg, null);

        View vMain =  mView.findViewById(R.id.layoutMain);
        vMain.setBackgroundColor(mFont.getBG());

        TextView txtMsg = (TextView) mView.findViewById(R.id.txtMsg);
        setViewFont(txtMsg);
        txtMsg.setText(message.getMessage());
        vMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        //String s = context.getString(R.string.cancel);
        //s = s.toUpperCase();
        //((TextView)mView.findViewById(R.id.txtCancel)).setText(s + KDSUIDialogBase.getBumpbarCancelKeyText(context));
        String s = "Click this window, or press " + KDSUIDialogBase.getBumpbarCancelKeyText(context) + " to hide";
        ((TextView)mView.findViewById(R.id.txtCancel)).setText(s);

        mView.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                KDSUIDialogBase.DialogEvent evt = KDSUIDialogBase.checkDialogKeyboardEvent(event);

                if (evt == KDSUIDialogBase.DialogEvent.Cancel)
                    hide();
                return false;
            }
        });
        return mView;
    }
    private void setViewFont(TextView t)
    {
        t.setTypeface(mFont.getTypeFace());
        t.setTextSize(mFont.getFontSize());
        t.setBackgroundColor( mFont.getBG());
        t.setTextColor(mFont.getFG());
    }
    public void hide()
    {
        if (mPopWindow != null)
            mPopWindow.dismiss();
        mPopWindow = null;
        mView = null;
        if (m_receiver!= null) {
            ArrayList<Object> ar = new ArrayList<>();
            ar.add(this);
            m_receiver.onPOSMsgDlgEvent(POSMsgDlgEvent.Closed, ar);
        }
    }

    private void showPopWindow(View viewParent)
    {

        int w = viewParent.getWidth()/2;
        int h = viewParent.getHeight()/2;


        mPopWindow = new PopupWindow(mView, w, h);

        int x = (viewParent.getWidth() - mPopWindow.getWidth())/2;
        int y = (viewParent.getHeight() -mPopWindow.getHeight())/2;

        mPopWindow.showAtLocation( viewParent, Gravity.NO_GRAVITY, x,y);
    }
    public void show(View viewParent, KDSPOSMessage message)
    {
        createView(message);
        //showViewToTop(mView, bTouchBarVisible);
        showPopWindow( viewParent);
    }
}
