package com.bematechus.kds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/8/13 0013.
 */
public class KDSViewPanel extends KDSViewPanelBase {
    ArrayList<KDSViewBlock> m_arBlocks = new ArrayList<KDSViewBlock>();

    KDSView m_viewParent = null;
    Point m_ptStartLocation = new Point(0,0); //absolute location
    KDSViewPanelNumber m_panelNumber = new KDSViewPanelNumber();

    int m_nBG = 0;

    public KDSViewPanel(KDSView parent)
    {
        m_viewParent = parent;
    }
    public void setBG(int color)
    {
        m_nBG = color;
    }
    public int getBG()
    {
        return m_nBG;
    }



    protected  KDSView getParent()
    {
        return m_viewParent;
    }

    protected Rect getParentRect()
    {
        return m_viewParent.getBounds();
    }
    protected Rect getParentValidRect()
    {
        return getParent().getValidRect();
    }
    protected KDSViewSettings getEnv()
    {
        return m_viewParent.getEnv();
    }
    public void setStartPoint(Point pt)
    {
        m_ptStartLocation = pt;
    }
    public Point getStartPoint()
    {
        return m_ptStartLocation;
    }

    /**
     *
     * //use the average value
     *
     * @return
     */

    public int getBlocksRowsHeight()
    {
        return getParent().getBlockAverageHeight();

    }
    public int getBlocksColsWidth()
    {
        return getParent().getBlockAverageWidth();
    }


    public void onJustDrawCaptionAndFooter(Canvas canvas, int nIndex)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
            m_arBlocks.get(i).onJustDrawCaptionAndFooter(canvas);
        drawRoundCornerForPanel(canvas);
        drawPanelNumber(canvas, nIndex);
    }

    public  void invalidateCaptionAndFooter(View v)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
            m_arBlocks.get(i).invalidateCaptionAndFooter(v);
    }


    public void drawPanelNumber(Canvas canvas, int nPanelIndex)
    {
        if (this.getEnv().getSettings().getBoolean(KDSSettings.ID.Panels_Show_Number)) {
            String  strbase = this.getEnv().getSettings().getString(KDSSettings.ID.Panels_Panel_Number_Base);
            int nbase = KDSUtil.convertStringToInt(strbase, 0);

            m_panelNumber.onDraw(canvas, this, nPanelIndex+ nbase);
        }
    }

    public  void onDraw(Canvas canvas, int nIndex)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++) {
            //Rect rt = m_arBlocks.get(i).getDrawableRect();
            //CanvasDC.drawRoundRect(canvas, rt, Color.BLUE, true,false );
            m_arBlocks.get(i).onDraw(canvas, getBG());
        }

        drawRoundCornerForPanel(canvas);

        drawPanelNumber(canvas, nIndex);

    }

    private void drawRoundCornerForPanel(Canvas canvas)
    {
        int ncount = m_arBlocks.size();
        if (ncount <=0) return;
        int nViewBG = getEnv().getSettings().getInt(KDSSettings.ID.Panels_BG);
        boolean bFocused = m_arBlocks.get(0).isFocused();
        int ninset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        int nInsetBlock = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);

        //draw round corner for border.
        for (int i=0; i< ncount; i++) {
            Rect rt = m_arBlocks.get(i).getDrawableRect();
            if (bFocused)
                rt.inset(-1*nInsetBlock, -1*nInsetBlock);
            CanvasDC.drawLeftUpArc(canvas, rt, nViewBG );
            CanvasDC.drawLeftDownArc(canvas, rt, nViewBG );
            CanvasDC.drawRightDownArc(canvas, rt, nViewBG );
            CanvasDC.drawRightUpArc(canvas, rt, nViewBG );

        }
        //draw content round corner
        if (ncount == 1)
        {
            KDSViewBlock block = m_arBlocks.get(0);
            Rect rt = block.getDrawableRect();
            //int ninset = block.getInsetBeforeCell();

            rt.inset(ninset, ninset);
            int nBorderColor = block.getBorderColor();
            if (bFocused)
                nBorderColor =  block.getBorderInsideLineColor(block.getBorderColor(), nViewBG);
            CanvasDC.drawLeftUpArc(canvas, rt, nBorderColor );
            CanvasDC.drawLeftDownArc(canvas, rt, nBorderColor );
            CanvasDC.drawRightDownArc(canvas, rt, nBorderColor );
            CanvasDC.drawRightUpArc(canvas, rt, nBorderColor );
            if (bFocused)
            {
                int nFocusColor = getEnv().getSettings().getInt(KDSSettings.ID.Focused_BG);
                //nFocusColor = Color.RED;
                //rt.inset(-1*ninset, -1*ninset);
                rt.inset(-1*KDSViewBlock.LINE_SIZE, -1*KDSViewBlock.LINE_SIZE);
                CanvasDC.drawLeftUpArc(canvas, rt, nFocusColor );
                CanvasDC.drawLeftDownArc(canvas, rt, nFocusColor );
                CanvasDC.drawRightDownArc(canvas, rt, nFocusColor );
                CanvasDC.drawRightUpArc(canvas, rt, nFocusColor );
            }

        }
        else
        {
            KDSViewBlock block = m_arBlocks.get(0);
            Rect rt = block.getDrawableRect();
            //int ninset = block.getInsetBeforeCell();

            rt.inset(ninset, ninset);
            int nBorderColor = block.getBorderColor();
            if (bFocused)
                nBorderColor = block.getBorderInsideLineColor(block.getBorderColor(), nViewBG);// getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            CanvasDC.drawLeftUpArc(canvas, rt, nBorderColor );
            CanvasDC.drawLeftDownArc(canvas, rt, nBorderColor );
            if (bFocused)
            {
                int nFocusColor = getEnv().getSettings().getInt(KDSSettings.ID.Focused_BG);

                //rt.inset(-1*ninset, -1*ninset);
                rt.inset(-1*KDSViewBlock.LINE_SIZE, -1*KDSViewBlock.LINE_SIZE);
                CanvasDC.drawLeftUpArc(canvas, rt, nFocusColor );
                CanvasDC.drawLeftDownArc(canvas, rt, nFocusColor );
              //  CanvasDC.drawRightDownArc(canvas, rt, nFocusColor );
              //  CanvasDC.drawRightUpArc(canvas, rt, nFocusColor );
            }

            block = m_arBlocks.get(m_arBlocks.size()-1);
            rt = block.getDrawableRect();
            rt.inset(ninset, ninset);
            CanvasDC.drawRightDownArc(canvas, rt, nBorderColor );
            CanvasDC.drawRightUpArc(canvas, rt, nBorderColor );
            if (bFocused)
            {
                int nFocusColor = getEnv().getSettings().getInt(KDSSettings.ID.Focused_BG);

                //rt.inset(-1*ninset, -1*ninset);
                rt.inset(-1*KDSViewBlock.LINE_SIZE, -1*KDSViewBlock.LINE_SIZE);
                //CanvasDC.drawLeftUpArc(canvas, rt, nFocusColor );
                //CanvasDC.drawLeftDownArc(canvas, rt, nFocusColor );
                CanvasDC.drawRightDownArc(canvas, rt, nFocusColor );
                CanvasDC.drawRightUpArc(canvas, rt, nFocusColor );
            }
        }
    }

    protected int getBlockStartPointX( int nCol)
    {
        int nWidth = this.getBlocksColsWidth();
        int nHeight = this.getBlocksRowsHeight();
        nWidth *= nCol;
        return   this.getParentValidRect().left + nWidth;
    }

    protected int getBlockStartPointX(Point ptStart,  int nCol)
    {
        int nWidth = this.getBlocksColsWidth();

        nWidth *= nCol;
        return   ptStart.x + nWidth;
    }

    protected int getBlockStartPointY(int nRow)
    {
        int nHeight = this.getBlocksRowsHeight();
        nHeight *= nRow;
        return this.getParentValidRect().top  + nHeight;
    }

    protected  boolean isBlockInParentRect(KDSViewBlock block)
    {

        Rect rcBlock = block.getDrawableRect();
        return getParent().isRectInParentRect(rcBlock);

    }
    public KDSViewBlock getLastBlock()
    {
        if (m_arBlocks.size() <=0)
            return null;
        return m_arBlocks.get(m_arBlocks.size() - 1);
    }
    public KDSViewBlock getFirstBlock()
    {
        if (m_arBlocks.size() <=0)
            return null;
        return m_arBlocks.get(0);
    }

    public Object getFirstBlockFirstRowData()
    {
        KDSViewBlock block = getFirstBlock();
        if (block == null)
            return null;
        return block.getFirstRowData();


    }

    public void setBorderColor(Canvas g, int nColor)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewBlock block =  m_arBlocks.get(i);
            block.setBorderColor(g, nColor);

        }

    }
    public void setBorderColorToDefault(Canvas g)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewBlock block =  m_arBlocks.get(i);
            block.setBorderColorToDefault(g);

        }
    }
    public boolean containsBlock(KDSViewBlock blck)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewBlock block =  m_arBlocks.get(i);
            if (blck == block)
                return true;
        }
        return false;
    }
    /**
     *
     * @param cell
     * @return
     *   add to which block
     */
    public KDSViewBlock addCell(KDSViewBlockCell cell)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
        {
            KDSViewBlock block =  m_arBlocks.get(i);
            if ( block.isFull())
            { //full filled
                continue;
            }
            else
            {
                block.getCells().add(cell);
                return block;
            }
        }
        return null;
    }


    public KDSViewBlock addCellToLastRow(KDSViewBlockCell cell)
    {
        int ncount = m_arBlocks.size();
        KDSViewBlock block =  m_arBlocks.get(ncount - 1); //last
        if ( block.isFull())
        { //full filled
            return null;
        }
        else
        {
            int nfree = block.getFreeRows();
            for (int i=0; i< nfree-1; i++)
            {
                KDSViewBlockCell c = new KDSViewBlockCell();
                c.setData(null);
                block.getCells().add(c);
            }
            block.getCells().add(cell);
            return block;
        }


    }

    public  KDSViewBlock getClickedBlock(int x, int y)
    {
        int ncount = m_arBlocks.size();
        for (int i=0; i< ncount; i++)
        {
            if (m_arBlocks.get(i).getDrawableRect().contains(x, y))
                return m_arBlocks.get(i);
        }
        return null;
    }

    public boolean pointInMe(int x, int y)
    {
       return (getClickedBlock(x, y) != null);

    }

    public int getTotalRows()
    {
        int ncount = m_arBlocks.size();
        int ncounter = 0;
        for (int i=0; i< ncount; i++)
        {
            ncounter +=  m_arBlocks.get(i).getTotalRows();

        }
        return ncounter;
    }

}
