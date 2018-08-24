package com.bematechus.kdslib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 *
 */
public class KDSTextview extends TextView {


    int m_nLeftBorderColor = 0;
    int m_nRightBorderColor = 0;
    int m_nTopBorderColor = 0;
    int m_nBottomBorderColor = 0;

    int m_nBorderSize = 1;
    Paint m_paint = new Paint();
    public KDSTextview(Context context) {
        super(context);
    }
    public KDSTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.KDSTextview);

        m_nLeftBorderColor = a.getColor(R.styleable.KDSTextview_leftcolor, Color.TRANSPARENT);
        m_nRightBorderColor = a.getColor(R.styleable.KDSTextview_rightcolor, Color.TRANSPARENT);
        m_nTopBorderColor = a.getColor(R.styleable.KDSTextview_topcolor, Color.TRANSPARENT);
        m_nBottomBorderColor = a.getColor(R.styleable.KDSTextview_bottomcolor, Color.TRANSPARENT);

        m_nBorderSize = (int)a.getDimension(R.styleable.KDSTextview_bordersize, 1);


    }

        /**
         *
         * @param nLeft
         *      0: don't draw it.
         * @param nRight
         * @param nTop
         * @param nBottom
         */
    public void setBorderColor(int nLeft, int nRight, int nTop, int nBottom)
    {
        m_nLeftBorderColor = nLeft;
        m_nRightBorderColor= nRight;
        m_nTopBorderColor= nTop;
        m_nBottomBorderColor= nBottom;
    }

    private void drawBorderInsideLines(Canvas g) {


        if (m_nBorderSize == 0) return;

        int x = 0;
        int y = x;
        int nBorderInset = m_nBorderSize;
        Rect rect = new Rect(x, y, x + g.getWidth(), y + g.getHeight());

       // rect.inset(nBorderInset, nBorderInset);//nBorderInset / 2, nBorderInset / 2);


        //Paint paint = new Paint();
        //paint.setColor(getBorderInsideLineColor(getBorderColor(), panelBg));
        m_paint.setStrokeWidth(m_nBorderSize);//nBorderInset / 2);


        //top
        if (m_nTopBorderColor !=0) {
            m_paint.setColor(m_nTopBorderColor);
            g.drawRect(rect.left, rect.top, rect.right, rect.top + m_nBorderSize, m_paint);
        }

        if (m_nRightBorderColor != 0) {
            m_paint.setColor(m_nRightBorderColor);
            g.drawRect(rect.right - m_nBorderSize, rect.top, rect.right, rect.bottom, m_paint);
        }
        //g.drawLine(rect.right-5, rect.top, rect.right-5, rect.bottom, paint);
        if (m_nBottomBorderColor != 0) {
            m_paint.setColor(m_nBottomBorderColor);
            g.drawRect(rect.left, rect.bottom - m_nBorderSize, rect.right, rect.bottom, m_paint);
        }
        if (m_nLeftBorderColor != 0) {
            m_paint.setColor(m_nLeftBorderColor);
            g.drawRect(rect.left, rect.top, rect.left + m_nBorderSize, rect.bottom, m_paint);
        }
        //

    }

    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawBorderInsideLines(canvas);
    }
}
