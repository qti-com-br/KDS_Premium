package com.bematechus.kds;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSPOSMessage;
import com.bematechus.kdslib.KDSPOSMessages;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import javax.xml.transform.sax.TransformerHandler;

public class POSMessagesView implements Runnable{

    View mListLeft = null;
    View mListBottom = null;
    ArrayList<KDSPOSMessage> mData= new ArrayList<>();

    KDSSettings.POSMsgPosition msgPosition = KDSSettings.POSMsgPosition.Hide;
    KDSViewFontFace mFont = null;
    KDSSettings.POSMsgScrollSpeed mScrollSpeed = KDSSettings.POSMsgScrollSpeed.Slow;
    boolean mAutoRemove = false;
    int mRemoveTimeout = 30; //seconds

    KDSPOSMessages mPosMessages = null;

    public void setViews(KDSSettings settings, View left, View bottom)
    {
        mListLeft = left;
        mListBottom = bottom;
        init(KDSApplication.getContext());
        updateSettings(settings);

    }

    public void init(Context c)
    {
        ((ListView)mListLeft).setAdapter( new POSMsgAdapter(c,mData) );
        ((ListView)mListBottom).setAdapter( new POSMsgAdapter(c,mData) );
    }

    protected Thread m_Thread = null;
    private boolean mThreadRunning = true;
    private void start()
    {
        if (m_Thread == null ||
                !m_Thread.isAlive()) {
            mThreadRunning = true;
            m_Thread = (new Thread(this));//.start();
            m_Thread.setName("POSMsgThread");
            m_Thread.start();
        }
    }
    private void close()
    {
        mThreadRunning = false;
        if (m_Thread != null)
        {
            try {
                m_Thread.join(3000);
            }
            catch (Exception e)
            {

            }
        }
    }

    /**
     * for scrolling
     */
    private void on100msTimer()
    {

    }


    Handler mHander = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            on100msTimer();

            return true;
        }
    });
    public void run()
    {
        //doRun();
        while (mThreadRunning)
        {
            try
            {
                Thread.sleep(100);
                mHander.sendEmptyMessage(1);
            }
            catch (Exception e)
            {

            }
        }
    }

    public void updateSettings(KDSSettings settings)
    {
        int n = settings.getInt(KDSSettings.ID.PosInfo_position);
        KDSSettings.POSMsgPosition position = KDSSettings.POSMsgPosition.values()[n];
        //position
        msgPosition = position;
        switch (position)
        {

            case Hide:{
                mListLeft.setVisibility(View.GONE);
                mListBottom.setVisibility(View.GONE);
            }
            break;
            case Left:{
                mListLeft.setVisibility(View.VISIBLE);
                mListBottom.setVisibility(View.GONE);
            }
            break;
            case Bottom:{
                mListLeft.setVisibility(View.GONE);
                mListBottom.setVisibility(View.VISIBLE);
            }
            break;
        }
        //font
        mFont = settings.getKDSViewFontFace(KDSSettings.ID.PosInfo_font);
        mListLeft.setBackgroundColor(mFont.getBG());
        mListBottom.setBackgroundColor(mFont.getBG());
        //speed
        n = settings.getInt(KDSSettings.ID.PosInfo_scroll_speed);
        mScrollSpeed = KDSSettings.POSMsgScrollSpeed.values()[n];
        //

        mAutoRemove = settings.getBoolean(KDSSettings.ID.PosInfo_auto_remove);
        //
        mRemoveTimeout = settings.getInt(KDSSettings.ID.PosInfo_remove_seconds);

    }

    public void on1sTimer()
    {
        if (mAutoRemove)
        {
            checkAutoRemove();
        }
    }

    private void checkAutoRemove()
    {
        ArrayList<KDSPOSMessage> ar = new ArrayList<>();
        for (int i=0;i< mPosMessages.getArray().size(); i++)
        {
            KDSPOSMessage m =  mPosMessages.getArray().get(i);
            if (m.isTimeout(mRemoveTimeout * 1000))
            {
                ar.add(m);
            }

        }

        mPosMessages.getArray().removeAll(ar);
        refreshView(mPosMessages);
    }

    public void refreshView(KDSPOSMessages messages)
    {
        mPosMessages = messages;
        mData.clear();

        switch (msgPosition)
        {
            case Hide: {
                break;
            }
            case Left: {
                mData.addAll(messages.getArray());
                ((POSMsgAdapter)((ListView)mListLeft).getAdapter()).notifyDataSetChanged();
            }
            break;
            case Bottom:
            {
                mData.addAll(messages.getArray());
                ((POSMsgAdapter)((ListView)mListBottom).getAdapter()).notifyDataSetChanged();
            }
                break;
        }
    }

    public class POSMsgAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<KDSPOSMessage> m_listData;

        public POSMsgAdapter(Context context, List<KDSPOSMessage> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
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

            KDSPOSMessage message =  m_listData.get(position);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.simple_list_item_pos_message, null);

            }
            TextView txtMsg = ((TextView) convertView.findViewById(R.id.txtInfo));
            setViewFont(txtMsg);
            txtMsg.setText(message.getMessage());

            TextView txtUser = ((TextView) convertView.findViewById(R.id.txtUser));
            setViewFont(txtUser);

            return convertView;
        }

        private void setViewFont(TextView t)
        {
            t.setTypeface(mFont.getTypeFace());
            t.setTextSize(mFont.getFontSize());
            t.setBackgroundColor( mFont.getBG());
            t.setTextColor(mFont.getFG());
        }
    }
}
