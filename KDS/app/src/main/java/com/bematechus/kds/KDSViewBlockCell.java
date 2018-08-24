package com.bematechus.kds;



import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSViewFontFace;

/**
 *
 * @author David.Wong
 * 
 * Use this cell to build a Panel.
 * row by row.
 * Just draw it, don't use base component as its ancestor
 */
public class KDSViewBlockCell {
    
    private Object m_data = null;
    KDSViewFontFace m_cellFont = new KDSViewFontFace();
    //for combine cells. The text wrap feature need this.
//    public KDSLayoutCell m_prevCell = null;
//    public KDSLayoutCell m_nextCell = null;


    public KDSViewBlockCell()
    {
        m_cellFont.setBG(Color.WHITE);
        m_cellFont.setFG(Color.BLACK);

    }
    public KDSViewFontFace getFont()
    {
        return m_cellFont;
    }
    public void setFontFace(KDSViewFontFace ff)
    {
        m_cellFont.copyFrom(ff);

    }
    public Object getData()
    {
        return m_data;
    }
    public boolean setData(Object obj)
    {
        m_data = obj;
        return true;
    }
    


    /**
     *
     * @param g
     * @param rcAbsolute
     *      This cell absolute location
     * @param env
     * @param , int nColInBlock
     *          the col number in block, it for expend title background color
     * @return
     */
    public boolean onDraw(Canvas g,Rect rcAbsolute,  KDSViewSettings env, int nColInBlock, KDSViewBlock block)
    {

            return drawCell(g,rcAbsolute,  env, nColInBlock, block);
        
    }
    

    /**
     * override by children
     * @param g
     * @param rcAbsolute
     * @param env
     * @return
     */
    protected boolean drawCell(Canvas g,Rect rcAbsolute,KDSViewSettings env, int nColInBlock, KDSViewBlock block)
    {
        this.drawBackground(g, rcAbsolute, env, this.getFont().getBG());

        return true;
    }
    private void drawBackground(Canvas g,Rect rcAbsolute,KDSViewSettings env, int bg)
    {

        Rect rt = rcAbsolute;//this.getBounds();
        CanvasDC.fillRect(g, bg, rt);

        
    }

}
