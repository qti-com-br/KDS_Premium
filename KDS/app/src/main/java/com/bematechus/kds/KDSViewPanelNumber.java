package com.bematechus.kds;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSViewFontFace;

/**
 * Created by Administrator on 2015/8/19 0019.
 * show panel number
 */
public class KDSViewPanelNumber {

    public enum NumberAlign{
        Left,
        Center,
        Right,
    }
    private NumberAlign m_numberAlign = NumberAlign.Right;//.Center;

    public void setNumberAlign(NumberAlign align)
    {
        m_numberAlign = align;
    }
    public NumberAlign getNumberAlign()
    {
        return m_numberAlign;
    }
    /**
     * draw the panel number
     * @param canvas
     */
    public void onDraw(Canvas canvas, KDSViewPanel panel, int nNumber)
    {
        KDSViewBlock block = panel.getFirstBlock();
        if (block == null) return;
        Rect rc = block.getBounds();
        //KDSViewFontFace fontDefault = panel.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace);
        KDSViewFontFace fontDefault = panel.getEnv().getSettings().getViewBlockFont();//
        //Typeface tf = panel.getEnv().getSettings().getViewBlockFont();

        KDSViewFontFace ff = new KDSViewFontFace();
        //ff.setTypeFace( tf);
        //ff.setFontSize(panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Row_Height));

        ff.copyFrom(fontDefault);

        //int bg = panel.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Panel_Number_FontFace).getBG();//.getPanelNumberFontFace().getBG();
        //int fg = panel.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Panel_Number_FontFace).getFG();//getPanelNumberFontFace().getFG();
        KDSBGFG bf = KDSBGFG.parseString(panel.getEnv().getSettings().getString(KDSSettings.ID.Panels_Panel_Number_BGFG));
        //int fg = panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Panel_Number_FG);//getPanelNumberFontFace().getFG();
        //int bg = panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Panel_Number_BG);//getPanelNumberFontFace().getFG();
        int bg = bf.getBG();
        int fg = bf.getFG();
        ff.setBG(bg);
        ff.setFG(fg);



        int x=0 , y=0, w=0, h=0;
        if (getNumberAlign() == NumberAlign.Left)
        {
            x = rc.left;
            y = rc.top;
            w = panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) + panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            h = w;
        }
        else if (getNumberAlign() == NumberAlign.Center)
        {

            y = rc.top;
            w = panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) + panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            h = w;
            x = rc.left + rc.width()/2 - w/2;
        }
        else if (getNumberAlign() == NumberAlign.Right)
        {
            w = panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) + panel.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            h = w;
            x = rc.right - w -1;
            y = rc.top;

        }
        Rect rect = new Rect(x, y, x + w, y + h);
        CanvasDC.drawCircle(canvas, bg, rect);

        String strNum = Integer.toString(nNumber);
        int nsize = CanvasDC.getBestMaxFontSize(rect, ff, strNum);
        ff.setFontSize(nsize);


        CanvasDC.drawText(canvas, ff, rect,Integer.toString(nNumber), Paint.Align.CENTER);

    }
}
