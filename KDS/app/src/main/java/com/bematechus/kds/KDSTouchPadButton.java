package com.bematechus.kds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/11/11 0011.
 */
public class KDSTouchPadButton {

    public enum TouchPadID
    {
        NULL,
        Next,
        Prev,
        Up,
        Down,
        Bump,
        Unbump,
        Sum,
        Transfer,
        Sort,
        Test,
        ActiveStations,
        Park,
        Unpark,
        More,
        Print,
        BuildCard,
        Training,
        UnbumpLast,
        Page,
        //2.0.25 Add two more button to touch button, Next guest_paging/ Prev guest_paging which go to next guest_paging directory; also apply this to Bumpbar key assignment.
        Next_Page,
        Prev_Page,
        Move, //kp-78 move order feature.
    }

    String m_strText = "";
    TouchPadID m_id = TouchPadID.NULL;
    boolean m_bIsDown = false;

    public KDSTouchPadButton(TouchPadID id, String strText)
    {
        m_id = id;
        m_strText = strText;

    }


    public TouchPadID getID()
    {
        return m_id;

    }
    public  void setTouchPadID(TouchPadID id)
    {
        m_id = id;
    }
    public String getText()
    {
        return m_strText;
    }

    public String toString()
    {
        return m_strText;
    }

    public void setPressDown(boolean bDown)
    {
        m_bIsDown = bDown;
    }
}
