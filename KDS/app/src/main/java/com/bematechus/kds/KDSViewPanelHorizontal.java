package com.bematechus.kds;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by Administrator on 2015/8/17 0017.
 */
public class KDSViewPanelHorizontal extends KDSViewPanel {

    public KDSViewPanelHorizontal(KDSView parent)
    {
        super(parent);
      //  setCols(1);
        
    }
    protected int getMaxBlocksFromStartPointInSameRow()
    {
        Rect rcParent  = this.getParentValidRect();
        int nwidth = rcParent.right - m_ptStartLocation.x;
        return nwidth/ getBlocksColsWidth();
    }
    protected  int getMaxBlocksRowsFromStartPoint()
    {
        Rect rcParent  = this.getParentValidRect();
        int nheight = rcParent.bottom - m_ptStartLocation.y;
        return nheight/ getBlocksRowsHeight();
    }
    public boolean expendCol()
    {
        int ncount = m_arBlocks.size();
        ncount++;
        return setCols(ncount);
    }
    public boolean setCols(int nCols)
    {
        m_arBlocks.clear();
        if (nCols <=0) {
            m_arBlocks.clear();
            return true;
        }

        KDSViewBlock block = null;
        if (m_arBlocks.size()<=0)
        {
            block = new KDSViewBlock(getParent());

        }
        else
        {
            block = m_arBlocks.get(m_arBlocks.size() -1);
        }
        if (nCols <= getMaxBlocksFromStartPointInSameRow()) //same row
        {
            block.setBounds(getBlockRect(m_ptStartLocation, nCols));
            block.setCols(nCols);
            block.setBorderAllSide(KDSViewBlock.BorderStyle.BorderStyle_Line);
            if (!isBlockInParentRect(block))
                return false;
            m_arBlocks.add(block);
        }
        else
        { //more than one row
            int nFirstCols = getMaxBlocksFromStartPointInSameRow();
            block.setBounds(getBlockRect(m_ptStartLocation, nFirstCols));
            block.setCols(nFirstCols);
            block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Line,
                    KDSViewBlock.BorderStyle.BorderStyle_Break,
                    KDSViewBlock.BorderStyle.BorderStyle_Line,
                    KDSViewBlock.BorderStyle.BorderStyle_Line);
            if (!isBlockInParentRect(block))
                return false;
           m_arBlocks.add(block);
            //block.setBorderSide().getBorderStyle()
            int nLeftOverCols =  nCols - nFirstCols;//
            int nNeedsRows = nLeftOverCols / getEnv().getSettingsCols();// getEnv().getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols);
            int nLastCols = nLeftOverCols %  getEnv().getSettingsCols();//getEnv().getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols);
            int nLoop = nNeedsRows ;
            if (nLastCols >0)
                nLoop ++;
            for (int i=0; i< nLoop; i++)
            {
                if ( i< nLoop -1 || nLastCols ==0)
                {
                    int x = getBlockStartPointX(0);
                    int y = getBlockStartPointY(getStartPoint(), i+1);
                    Rect rcBlock = getBlockRect(new Point(x, y), getEnv().getSettingsCols());// getEnv().getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols));
                    block = new KDSViewBlock(getParent());
                    block.setBounds(rcBlock);
                    block.setCols( getEnv().getSettingsCols());//getEnv().getSettings().getInt(KDSSettings.ID.Panels_Blocks_Cols));
                    block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Break,
                            KDSViewBlock.BorderStyle.BorderStyle_Break,
                            KDSViewBlock.BorderStyle.BorderStyle_Line,
                            KDSViewBlock.BorderStyle.BorderStyle_Line);
                }
                else
                { //last row
                    int x = getBlockStartPointX(0);
                    int y = getBlockStartPointY(getStartPoint(), i+1);
                    Rect rcBlock = getBlockRect(new Point(x, y), nLastCols);
                    block = new KDSViewBlock(getParent());
                    block.setBounds(rcBlock);
                    block.setCols(nLastCols);
                    block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Break,
                            KDSViewBlock.BorderStyle.BorderStyle_Line,
                            KDSViewBlock.BorderStyle.BorderStyle_Line,
                            KDSViewBlock.BorderStyle.BorderStyle_Line);

                }
                if (!isBlockInParentRect(block))
                    return false;
                m_arBlocks.add(block);
            }

        }
        return true;
    }

    /**
     *
     * @param ptStart
     * @param nContainCols
     *      Make sure all cols is in same row
     * @return
     */
    private Rect getBlockRect(Point ptStart, int nContainCols)
    {
        int nWidth = this.getBlocksColsWidth();
        int nHeight = this.getBlocksRowsHeight();
        nWidth *= nContainCols;
        Rect rc = new Rect(ptStart.x, ptStart.y, ptStart.x + nWidth, ptStart.y + nHeight);
        return rc;
    }

    /**
     *
     * @param ptStart
     * @param nRow
     *      How many rows from pt start. !!!!!
     * @return
     */
    private int getBlockStartPointY(Point ptStart, int nRow)
    {
        int nWidth = this.getBlocksColsWidth();
        int nHeight = this.getBlocksRowsHeight();
        nHeight *= nRow;
        return   ptStart.y + nHeight;
    }




}
