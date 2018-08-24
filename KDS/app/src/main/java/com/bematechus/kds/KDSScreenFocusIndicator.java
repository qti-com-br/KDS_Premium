package com.bematechus.kds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 *
 */
public class KDSScreenFocusIndicator extends View {
    public KDSScreenFocusIndicator(Context context) {
        super(context);
    }
    public KDSScreenFocusIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    static final int LINE_SIZE = 2;
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int bg =((ColorDrawable)this.getBackground()).getColor();
        if (bg ==0)
            return;

        Rect rect = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
        rect.inset(1, 1);
        Paint paint = new Paint();

        //paint.setColor(KDSUtil.getContrastVersionForColor(bg));
        paint.setColor(bg);

        //paint.setStrokeWidth(LINE_SIZE);//nBorderInset / 2);
        canvas.drawRect(rect.left, rect.top, rect.right, rect.top + LINE_SIZE, paint);
    }
}
