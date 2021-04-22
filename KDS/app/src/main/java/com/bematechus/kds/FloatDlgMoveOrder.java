package com.bematechus.kds;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUIDialogBase;

/**
 * show float information in screen bottom when move order.
 *
 */
public class FloatDlgMoveOrder {

    String mMovingOrderGuid = "";

    View mView = null;
    private View createView()
    {
        Context context = KDSApplication.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        mView = inflater.inflate(R.layout.kdsui_dlg_float_move_order, null);

        ImageView v = mView.findViewById(R.id.imgIcon);
        v.setImageResource(R.drawable.move);

        ((TextView)mView.findViewById(R.id.txtCancel)).setText(context.getString(R.string.cancel) + KDSUIDialogBase.getBumpbarCancelKeyText(context));

        mView.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_0)
                    hide();
                return false;
            }
        });
        return mView;
    }

    final int DLG_HEIGHT = 60;

    private void showViewToTop(View v)
    {
        WindowManager wm = (WindowManager) KDSApplication.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        // window type
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        /*
         * if params.type = WindowManager.LayoutParams.TYPE_PHONE; the priority is lower a little.
         * If the message bar down, this is invisible
         */

        //params.format = PixelFormat.RGBA_8888; // image format, background transparent

        // Window flag
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        /*
         * 下面的flags属性的效果形同“锁定”。 悬浮窗不可触摸，不接受任何事件,同时不影响后面的事件响应。
         * wmParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
         * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
         */

        Display defaultDisplay = wm.getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        // 设置悬浮窗的长得宽
        params.width = point.x ;
        params.height = DLG_HEIGHT;
        params.y =point.y - params.height;

        wm.addView(v, params);

    }

    public void show()
    {
        createView();
        showViewToTop(mView);
    }
    public void hide()
    {
        WindowManager wm = (WindowManager) KDSApplication.getContext().getSystemService(
                Context.WINDOW_SERVICE);
        wm.removeView(mView);
        mView = null;
    }

    public boolean isVisible()
    {
        return (mView != null);
    }

    public void setMovingOrderGuid(String orderGuid)
    {
        mMovingOrderGuid = orderGuid;
    }

    public String getMovingOrderGuid()
    {
        return mMovingOrderGuid;
    }

    public boolean keyPressed(KeyEvent evt, int keyCode)
    {
        KDSUIDialogBase.DialogEvent event = KDSUIDialogBase.checkDialogKeyboardEvent(evt);
        if (event == KDSUIDialogBase.DialogEvent.Cancel)
        {
            hide();
            return true;
        }
        return false;
    }
}
