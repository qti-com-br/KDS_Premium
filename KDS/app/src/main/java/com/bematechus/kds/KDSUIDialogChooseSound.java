package com.bematechus.kds;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/1/24.
 */
public class KDSUIDialogChooseSound extends KDSUIDialogBase {


    ListView m_lstData = null;
    String m_uriSelected = "";


    AudioPlayer m_player = new AudioPlayer();



    @Override
    public void onOkClicked() {//save data here

        KDSSound sound =  ((MyAdapter)m_lstData.getAdapter()).getCheckedSound();
        m_uriSelected = null;
        if (sound != null)
        {
            m_uriSelected = sound.toString();//.m_uri;

        }


    }

    /**
     * it will been overrided by child
     *
     * @return
     */
    @Override
    public Object getResult() {
        return m_uriSelected;


    }

    public KDSUIDialogChooseSound(final Context context, String uriDefault, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_sound, "");
        this.setTitle( context.getString(R.string.choose_sound));
        m_player.bindKDSService(context);
        //bindKDSService(context);
        //m_context = context;
        m_uriSelected = uriDefault;
        m_lstData = (ListView) this.getView().findViewById(R.id.lstData);
//        m_lstData.setAdapter(new ArrayAdapter<String>(context,
//                android.R.layout.simple_list_item_single_choice, getArray()));
        m_lstData.setAdapter(new MyAdapter(context, getArray(context)));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        KDSSound sound = KDSSound.parseString(m_uriSelected);
        ((MyAdapter)m_lstData.getAdapter()).setSelectedSound(sound);

//        if (m_lstData.getCount() > 0)
//            m_lstData.setItemChecked(0, true);
    }

    ArrayList<KDSSound> getArray(Context context) {
        return m_player.getAudioArray(context);

    }





    private class MyAdapter extends BaseAdapter  implements SoundService.SoundEventsReceiver {


        private KDSSound m_checkedSound =null;
        private ImageButton m_lastPlayButton = null;

        private LayoutInflater mInflater;
        //public List<Map<String, Object>> m_listData; //KDSStationsRelation class array
        public List<KDSSound> m_listData; //KDSStationsRelation class array

        public void setSelectedSound(KDSSound sound)
        {
            m_checkedSound = sound;
        }
        public MyAdapter(Context context, List<KDSSound> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<KDSSound> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSSound> lst)
        {
            m_listData = lst;
        }
        public KDSSound getCheckedSound()
        {
            return m_checkedSound;
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
            KDSSound r =  m_listData.get(position);
            if (convertView == null) {
                //holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.listitem_sound, null);


            }
            else
            {


            }
            convertView.setTag(r);

            String description = r.getDescription();
            if (description.isEmpty())
                description = "None";
            TextView txtDescription = ((TextView) convertView.findViewById(R.id.txtDescription));

            txtDescription.setText(description);
            txtDescription.setTag(r);
            txtDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_checkedSound =(KDSSound)( v.getTag());
                    ((MyAdapter)m_lstData.getAdapter()).notifyDataSetChanged();
                }
            });

            ImageButton btn = ((ImageButton) convertView.findViewById(R.id.btnPlay));//.setTag(r);
            btn.setTag(r);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPlayPauseSound(v, (KDSSound) v.getTag());
                }
            });




            if (r.getUri().isEmpty())
                btn.setVisibility(View.INVISIBLE);
            else
                btn.setVisibility(View.VISIBLE);
            RadioButton rb = ((RadioButton) convertView.findViewById(R.id.rbChecked));
            rb.setTag(r);
            if (m_checkedSound!= r && m_checkedSound.isEqual(r)) {
                m_checkedSound = r;
                m_checkedSound.setTag(rb);
                //rb.setChecked(true);
            }
            rb.setChecked( (m_checkedSound == r));
            //for initial sound



            rb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (m_checkedSound != null)
                    {
                        if (m_checkedSound.getTag() != v)
                        {
                            RadioButton rb = ((RadioButton)m_checkedSound.getTag());

                            if (rb != null )
                                rb.setChecked(false);
                            m_checkedSound.setTag(null);
                        }
                    }
                    m_checkedSound =(KDSSound) v.getTag();
                    m_checkedSound.setTag(v);
                    ((MyAdapter)m_lstData.getAdapter()).notifyDataSetChanged();

                }
            });
            return convertView;
        }

        public void onPlayPauseSound(View v, KDSSound r)
        {

            m_player.setSoundEvent(this);

            if (m_lastPlayButton == v) {
                resetLastPlayButton();
            }
            else
            {
                resetLastPlayButton();
                m_player.stop();
            }
            if (m_player.isPlaying())
            {//next, stop it

                ((ImageButton)v).setImageDrawable(v.getContext().getResources().getDrawable(android.R.drawable.ic_media_play));//.setImageBitmap();
                m_player.stop();
            }
            else
            {
                m_lastPlayButton = (ImageButton) v;
                ((ImageButton)v).setImageDrawable(v.getContext().getResources().getDrawable(android.R.drawable.ic_media_pause));//.setImageBitmap();
                m_player.play(r.m_uri, 3000);
            }


        }
        private void resetLastPlayButton()
        {
            if (m_lastPlayButton != null)
            {
                m_lastPlayButton.setImageDrawable(m_lastPlayButton.getContext().getResources().getDrawable(android.R.drawable.ic_media_play));//.setImageBitmap();
                m_lastPlayButton = null;

            }
        }
        public void SoundPlayFinished(String uri)
        {
            resetLastPlayButton();
            m_player.stop();
        }
        public void SoundPlayStarted(String uri)
        {

        }
        public void SoundPlayStop(String uri)
        {
            resetLastPlayButton();
        }

    }


}