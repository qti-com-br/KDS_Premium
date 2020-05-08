package com.bematechus.kds;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bematechus.kdslib.KDSUtil;

/**
 * Created by David.Wong on 2020/5/6.
 * Rev:
 */
public class DlgCleaningPinchout extends Dialog {

    Context context = null;

    static DlgCleaningPinchout m_instance = null;
    ScaleGestureDetector m_scaleGesture = null;
    GestureDetector m_gesture = null; //for test
    DlgCleaningAlarm.CleaningHabitsEvents m_receiver = null;

    public void setReceiver(DlgCleaningAlarm.CleaningHabitsEvents r)
    {
        m_receiver = r;
    }

    static public DlgCleaningPinchout instance(Context c)
    {
        if (m_instance != null) {
            if (m_instance.isShowing())
                return m_instance;
            else
                m_instance.hide();
        }
        m_instance = new DlgCleaningPinchout(c);
        return m_instance;

    }

    static public boolean isVisible()
    {
        if (m_instance == null)
            return false;
        return m_instance.isShowing();
    }

    static public void closeInstance()
    {
        if (m_instance == null)
            return ;
        m_instance.dismiss();
    }

    public DlgCleaningPinchout(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 这句代码换掉dialog默认背景，否则dialog的边缘发虚透明而且很宽
        // 总之达不到想要的效果
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View v = View.inflate(this.context, R.layout.dlg_cleaning_pinchout, null);
        setContentView(v);
//        // 这句话起全屏的作用
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        KDSUtil.enableSystemVirtualBar(getWindow().getDecorView(), false);
        m_scaleGesture = new ScaleGestureDetector(this.getContext(), new MyScaleGestureListener());
        if (CleaningHabitsManager._DEBUG)
            m_gesture = new GestureDetector(this.getContext(), new MyGestureListener());
        this.setCancelable(false);
        this.setCanceledOnTouchOutside(false);

        // initView();
       // initListener();
    }

    public void onScaleGestured()
    {
        if (m_receiver != null)
        {
            m_receiver.onCleaningHabitsEvent(DlgCleaningAlarm.CleaningEventType.PinchOut_Pinched, null);
        }
        this.dismiss();
    }

    public boolean dispatchTouchEvent(MotionEvent event)
    {
        //m_gesture.onTouchEvent(event);
        if (CleaningHabitsManager._DEBUG) {
            if (m_gesture.onTouchEvent(event)) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            }
        }

        if (m_scaleGesture.onTouchEvent(event))
            event.setAction(MotionEvent.ACTION_CANCEL);

        return super.dispatchTouchEvent(event);
    }

    class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector)
        {
            //一定要返回true才会进入onScale()这个函数
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector)
        {
           DlgCleaningPinchout.this.onScaleGestured();
        }
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener
    {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            DlgCleaningPinchout.this.onScaleGestured();
            return true;
        }
    }
}
