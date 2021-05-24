package com.bematechus.kds;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.ThemeUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ScreenLogoDraw {

    static  final String TAG = "ScreenLogoDraw";

    static String m_logoFileName = "";
    static Bitmap m_logoImage = null;
    static Drawable m_lciLogo = null;
    static final int LOGO_TRANSPARENT = 30;
    /**
     * KPP1-293
     * Draw screen logo image.
     * @param canvas
     */

    /**
     *     * KPP1-293
     *      * Draw screen logo image.
     * @param view
     * @param rcBounds
     * @param canvas
     * @param settings
     * @param bScreenEmpty
     * @param nLciLogoBottomOffset
     *  In queue view, we need move logo up, as there is a page number row at bottom of screen.
     */
    static public void drawScreenLogo(View view,Rect rcBounds, Canvas canvas, KDSSettings settings, boolean bScreenEmpty, int nLciLogoBottomOffset) {
        boolean bEnabled = settings.getBoolean(KDSSettings.ID.Screen_logo_enabled);
        if (!bEnabled) {
            m_logoFileName = "";
			m_logoImage = ((BitmapDrawable) view.getContext().getApplicationContext().getResources().getDrawable(R.drawable.lci_logo_bg)).getBitmap();
            int overlayColor = ThemeUtil.getAttrColor( KDSApplication.getContext(), R.attr.focus_bg);//.getResources().getColor(R.color.focus_bg);
            m_logoImage = changeBitmapColor(m_logoImage, overlayColor);
            m_lciLogo = null;
        }

        String fileName = settings.getString(KDSSettings.ID.Screen_logo_file);
        if (fileName.isEmpty()) {
            if (bEnabled) {
            	drawLciLogo(view, rcBounds, canvas,bScreenEmpty, nLciLogoBottomOffset);
				return;
			}
        }
        if (!m_logoFileName.equals(fileName))
        {
            m_logoImage = loadLogoImage(fileName);
            if (m_logoImage != null)
            {
                m_logoFileName = fileName;
            }
            else {
				if (bEnabled) drawLciLogo(view, rcBounds, canvas,bScreenEmpty, nLciLogoBottomOffset);
                return;
            }
        }

        if (m_logoImage == null) {
			if (bEnabled) drawLciLogo(view, rcBounds, canvas,bScreenEmpty, nLciLogoBottomOffset);
            return;
        }

        Rect rc = rcBounds;
        int imageW = m_logoImage.getWidth();
        int imageH = m_logoImage.getHeight();
        int offsetX = (rc.width() - imageW)/2;
        int offsetY = (rc.height() - imageH)/2;

        Paint pt = new Paint();
        pt.setAlpha(getLogoAlpha(bScreenEmpty));
        canvas.drawBitmap(m_logoImage, offsetX, offsetY, pt);

		if (bEnabled) drawLciLogo(view, rcBounds, canvas,bScreenEmpty, nLciLogoBottomOffset);


    }

    static private Bitmap loadLogoImage(String fileName)
    {
        try {
            FileInputStream fis = new FileInputStream(fileName);
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_(),  e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return null;
        }
    }
//    static final int LCI_LOGO_W = 500;
//    static final int LCI_LOGO_H = 100;


    static private void drawLciLogo(View view,Rect rcBounds, Canvas canvas, boolean bScreenEmpty, int nLciLogoBottomOffset)
    {
        if (m_lciLogo == null)
            m_lciLogo = view.getResources().getDrawable(R.drawable.lci_logo);
        //Rect rt = d.getBounds();
        Rect rtBG =  rcBounds;//view.getBounds();
        int w = 0;
        int h = 0;
        w = rtBG.width()/5;
        h = w/5;
        int x = rtBG.width() - w;
        int y = rtBG.height() - h - nLciLogoBottomOffset;

        m_lciLogo.setBounds(x, y, rtBG.width(), rtBG.height()- nLciLogoBottomOffset);
        int overlayColor = ThemeUtil.getAttrColor( KDSApplication.getContext(), R.attr.focus_bg);//.getResources().getColor(R.color.focus_bg);
        m_lciLogo.setColorFilter(overlayColor, PorterDuff.Mode.SRC_ATOP);
        ((BitmapDrawable)m_lciLogo).getPaint().setAlpha(getLogoAlpha(bScreenEmpty));

        m_lciLogo.draw(canvas);
    }

    static private int getLogoAlpha(boolean bScreenEmpty)
    {
        int nAlpha = LOGO_TRANSPARENT;
        if (bScreenEmpty )
            nAlpha = 255;
        return nAlpha;

    }
    public static Bitmap changeBitmapColor(Bitmap sourceBitmap, int color)
    {
        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(),true);
        Paint paint = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 1);
        paint.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, paint);
        return resultBitmap;
    }
}
