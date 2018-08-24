package com.bematechus.kds;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by Administrator on 2015/8/17 0017.
 */
public class KDSViewPanelVertical extends KDSViewPanel {

    public KDSViewPanelVertical(KDSView parent) {
        super(parent);

    }

    protected  int getRows()
    {
        int ncount = m_arBlocks.size();
        int nrows = 0;
        for (int i=0; i< ncount; i++)
        {
            nrows = m_arBlocks.get(i).getColTotalRows();
        }
        return nrows;
    }
    public void expandRow()
    {
        int n = getRows();
        n++;
        setRows(n);
    }

    public boolean setRows(int nRows)
    {
        m_arBlocks.clear();
        KDSViewBlock block = null;
        if (m_arBlocks.size() <=0)
            block = new KDSViewBlock(this.getParent());
        else
        {//get last one
            block = m_arBlocks.get(m_arBlocks.size() -1);
        }
        int nCurrentRows = 0;//this.getRows();
        int nBalance = nRows - nCurrentRows;

        int nfirstRows =  getHowManyRowsFromPointInSameCol(block, getStartPoint());
        if (nBalance <= nfirstRows)
        {
            block.setBounds(getBlockRect(getStartPoint(), block, nBalance));
            block.setBorderAllSide(KDSViewBlock.BorderStyle.BorderStyle_Line);
            if (!isBlockInParentRect(block))
                return false;
            m_arBlocks.add(block);

        }
        else
        { //spit to multiple block
            //first block
            int x = this.getBlockStartPointX(this.getStartPoint(),0);
            int y = this.getStartPoint().y;//full rows
            int w = this.getBlocksColsWidth();
            int h = this.getParentValidRect().bottom - y;
            block =  new KDSViewBlock(this.getParent());
            block.setBounds(new Rect(x, y, x + w, y + h));
            block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Line,
                    KDSViewBlock.BorderStyle.BorderStyle_Line,
                    KDSViewBlock.BorderStyle.BorderStyle_Line,
                    KDSViewBlock.BorderStyle.BorderStyle_Break);
            if (!isBlockInParentRect(block))
                return false;
            m_arBlocks.add(block);

            //others block
            int nLeftOverRows = nBalance - nfirstRows;
            int nMaxRows = getMaxRowsInCol(block);
            int nloop = nLeftOverRows/nMaxRows;
            int nLastRows = (nLeftOverRows % nMaxRows);
            if (nLastRows > 0)
                nloop ++;

            for (int i=0; i< nloop; i++)
            {
                if ( i < nloop-1 || nLastRows ==0)
                { //full rows col
                     x = this.getBlockStartPointX(this.getStartPoint(), i + 1);
                     y = this.getBlockStartPointY(0);//full rows
                     w = this.getBlocksColsWidth();
                     h = this.getParentValidRect().height();
                    block =  new KDSViewBlock(this.getParent());
                    block.setBounds(new Rect(x, y, x + w, y + h));

                    if (i < nloop-1)
                        block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Line,
                                KDSViewBlock.BorderStyle.BorderStyle_Line,
                                KDSViewBlock.BorderStyle.BorderStyle_Break,
                                KDSViewBlock.BorderStyle.BorderStyle_Break);
                    else
                        block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Line,
                                KDSViewBlock.BorderStyle.BorderStyle_Line,
                                KDSViewBlock.BorderStyle.BorderStyle_Break,
                                KDSViewBlock.BorderStyle.BorderStyle_Line);
                }
                else
                { //last one block
                    block =  new KDSViewBlock(this.getParent());
                     x = this.getBlockStartPointX(this.getStartPoint(), i + 1);
                     y = this.getBlockStartPointY(0);//full rows
                     w = this.getBlocksColsWidth();
                     h =  block.getBestHeightForRows(nLastRows);
                     h += (this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset)* 2 );
                    h += (this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset)*2) ;
                    block.setBounds(new Rect(x, y, x + w, y + h));
                    block.setBorderSide(KDSViewBlock.BorderStyle.BorderStyle_Line,
                            KDSViewBlock.BorderStyle.BorderStyle_Line,
                            KDSViewBlock.BorderStyle.BorderStyle_Break,
                            KDSViewBlock.BorderStyle.BorderStyle_Line);


                }
                if (!isBlockInParentRect(block))
                    return false;
                m_arBlocks.add(block);
            }
        }
        return true;
    }


    protected int getMaxRowsInCol(KDSViewBlock block)
    {
        int h = this.getParentValidRect().height();
        return h / block.getBestRowHeight();
    }

    protected Rect getBlockRect(Point ptStart, KDSViewBlock block,int nRows)
    {
        int nH =  block.getBestBlockHeightForRows(nRows);
        int nW = getBlocksColsWidth();
        return new Rect(ptStart.x, ptStart.y, ptStart.x+nW, ptStart.y + nH );

    }

    public int getHowManyRowsFromPointInSameCol(KDSViewBlock block, Point ptStart)
    {
        Rect rcParent  = this.getParentValidRect();
        int nheight = rcParent.bottom - ptStart.y;
        nheight -= getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset)*2;
        nheight -= getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset)*2;
        return nheight/ block.getBestRowHeight();
    }
}