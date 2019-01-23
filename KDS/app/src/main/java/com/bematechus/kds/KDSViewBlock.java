package com.bematechus.kds;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SumPathEffect;
import android.graphics.Typeface;
import android.view.View;

//import javax.swing.JPanel;
//import java.awt.*;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseListener;
//import javax.swing.*;
import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.*;

/**
 *
 * @author David.Wong
 */
public class KDSViewBlock {

    public enum BorderStyle {
        BorderStyle_Line,
        BorderStyle_Break,
        Unknown,
    }

    public enum BorderSide {
        Left,
        Right,
        Top,
        Bottom
    }

    ArrayList<KDSViewBlockCell> m_arCells = new ArrayList<KDSViewBlockCell>();

    private int m_nCols = 1;
    //While this changed, we need to calculate all rows
    private Rect m_rcBounds = new Rect(0, 0, 0, 0);
    //there are 3 zone in block
    /* 1. Title.
            Its rows is setten by code
        2. Data
            Its rows are calculated.
        3. Footer

    */
    //private int m_nTitleRows = 1;
    //private int m_nFooterRows = 0;
    private int m_nDataRows = 0; //this rows was calcuated by this block code.

    //private int m_nZoneGap = 0; //the gap between title/data/footer
    private ArrayList m_arRowsHeight = new ArrayList();
    private ArrayList m_arColsWidth = new ArrayList();

    //
    private BorderStyle m_borderStyleLeftSide = BorderStyle.BorderStyle_Break;
    private BorderStyle m_borderStyleRightSide = BorderStyle.BorderStyle_Line;
    private BorderStyle m_borderStyleTopSide = BorderStyle.BorderStyle_Break;
    private BorderStyle m_borderStyleBottomSide = BorderStyle.BorderStyle_Break;

    // private Color m_clrBorder = Color.white;
    //Insets m_borderInsets = new Insets(0,0,0,0);
    private KDSView m_parentViewer = null;

    private int m_borderColor = 0;

    private boolean m_bDrawBorderInsideLine = false; //in order to sharp the border.

    public KDSViewBlock(KDSView parent) {
        m_parentViewer = parent;
        m_arCells.clear();
        //m_panelEnv.getBorderInsets().set(m_nDefaultInset, m_nDefaultInset, m_nDefaultInset, m_nDefaultInset);
        //m_viewerEnv.setPanelFont(this.getFont());
        //this.setOpaque(true);
    }

    /**
     * Set this block location
     *
     * @param rcBounds
     */
    public void setBounds(Rect rcBounds) {
        m_rcBounds = rcBounds;
        calculateAllRowsValue();
        calculateAllColsValue();

        // debug_me();
    }

    private void debug_me() {
        m_arCells.clear();
        for (int i = 0; i < 5; i++) {
            KDSViewBlockCell c = new KDSViewBlockCell();
            c.getFont().setBG(Color.GREEN);
            c.getFont().setBG(Color.WHITE);

            m_arCells.add(c);
        }
    }

    /**
     * @return The drawing area
     */
    public Rect getDrawableRect() {
        int n = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
        Rect r = new Rect(m_rcBounds);

        r.inset(n, n);
        return r;
        //m_rcBounds.inset(n, n);
    }

    /**
     * @return The real rect without the border insets.
     */
    public Rect getBounds() {
        return m_rcBounds;

    }

    public int getBoundsWidth() {
        return this.getBounds().width();
    }

    public int getBoundsHeight() {
        return this.getBounds().height();
    }

    public int getDrawbleHeight() {
        return getDrawableRect().height();
    }

    public int getDrawableWidth() {
        return getDrawableRect().width();
    }

    public int getDataAreaHeight() {
        return getDrawableRect().height() - getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) * 2;
    }

    public int getDataAreaWidth() {
        return getDrawableRect().width() - getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) * 2;
    }


    public int getDataRows() {
        return m_nDataRows; //it was updated by code
    }

    public int getBestRowHeight() {
        //KDSViewFontFace ff = this.getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace);
        //Typeface tf = this.getEnv().getSettings().getViewBlockFont();
        KDSViewFontFace ff = this.getEnv().getSettings().getViewBlockFont();//.getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace);

        Paint paint = new Paint();
        paint.setTypeface(ff.getTypeFace());
        // paint.setTypeface(tf);
        int nsize = this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Row_Height);
        //paint.setTextSize(ff.getFontSize());
        paint.setTextSize(nsize);

        //paint.getFontMetrics().a
        return (int) ((paint.getFontMetrics().descent - paint.getFontMetrics().ascent) + 1);

    }

    public int getBestBlockHeightForRows(int nRows) {
        int nBestRowsHeight = getBestHeightForRows(nRows);
        nBestRowsHeight += (this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) * 2);
        nBestRowsHeight += (this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset) * 2);
        return nBestRowsHeight;

    }

    public int getBestHeightForRows(int nRows) {
        int nbestH = getBestRowHeight();
        int nGapCount = 0;
        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) > 0)
            nGapCount++;
        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0)
            nGapCount++;
        nbestH = nbestH * nRows + getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap) * nGapCount;
        return nbestH;
    }

    private void calculateAllRowsValue() {
        int nGapCount = 0;
        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) > 0)
            nGapCount++;
        if (getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0)
            nGapCount++;
        int nGapPixels = nGapCount * getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap);
        int nValidAreaHeight = this.getDrawbleHeight() - nGapPixels - this.getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset) * 2;
        int nBestRowHeight = getBestRowHeight();
        int nrows = nValidAreaHeight / nBestRowHeight;
//        if (nrows <=1)
//        {
//            int a = 1;
//        }
        int nLeftOverPixel = nValidAreaHeight % nBestRowHeight;

        //1. Reset all rows height
        m_arRowsHeight.clear();
        for (int i = 0; i < nrows; i++) {
            int h = nBestRowHeight;
            if (i < nLeftOverPixel)
                h++;
            m_arRowsHeight.add(h);

        }
        //2. data rows count
        m_nDataRows = nrows - getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) - getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);
//        if (m_nDataRows <=0)
//        {
//            int b = 1;
//            b = 3;
//        }

    }

    private void calculateAllColsValue() {
        m_arColsWidth.clear();
        //ArrayList ar = new ArrayList();
        int w = getBounds().width();// getWidth();

        // w -= getEnv().getBlockBorderInsets()*2*m_nCols;
        // w -= getInsetBeforeCell()*2;//*m_nCols;
        //w -= getEnv().getBlockBorderInsets();

        int naverage = (int) (w / m_nCols);
        int nleft = w % m_nCols;
        for (int i = 0; i < m_nCols; i++) {
            m_arColsWidth.add(naverage);
        }
        for (int i = 0; i < nleft; i++) {
            m_arColsWidth.set(m_nCols - 1 - i, naverage + 1);
        }
        // return ar;
    }

    public KDSView getViewer() {
        return m_parentViewer;
    }

    public void setBorderSide(BorderStyle nLeft, BorderStyle nRight, BorderStyle nTop, BorderStyle nBottom) {
        m_borderStyleLeftSide = nLeft;
        m_borderStyleRightSide = nRight;
        m_borderStyleTopSide = nTop;
        m_borderStyleBottomSide = nBottom;

    }

    public void setBorderAllSide(BorderStyle nStyle) {
        m_borderStyleLeftSide = nStyle;
        m_borderStyleRightSide = nStyle;
        m_borderStyleTopSide = nStyle;
        m_borderStyleBottomSide = nStyle;
    }

    public BorderStyle getBorderStyle(BorderSide side) {
        if (side == BorderSide.Left) {
            return m_borderStyleLeftSide;
        } else if (side == BorderSide.Right) {
            return m_borderStyleRightSide;
        } else if (side == BorderSide.Top) {
            return m_borderStyleTopSide;
        } else if (side == BorderSide.Bottom) {
            return m_borderStyleBottomSide;
        }
        return BorderStyle.Unknown;

    }

    public KDSViewSettings getEnv() {
        return getViewer().getEnv();// m_viewerEnv;
    }

    //    public void setEnv(KDSGUIViewerEnv env)
//    {
//        m_viewerEnv = env;
//    }
//    
    public ArrayList<KDSViewBlockCell> getCells() {
        return m_arCells;
    }

    public void setCells(ArrayList ar) {
        m_arCells = ar;
    }

    public KDSViewBlockCell getCell(int nIndex) {
        if (nIndex >= m_arCells.size())
            return null;
        if (nIndex < 0) return null;
        return (KDSViewBlockCell) m_arCells.get(nIndex);
    }

    public int getCellIndex(KDSViewBlockCell c) {
        for (int i =0; i< m_arCells.size(); i++)
        {
            if (m_arCells.get(i) == c)
                return i;
        }
        return -1;
    }

    public Object getFirstRowData() {
        if (m_arCells.size() <= 0) return null;
        return m_arCells.get(0).getData();
    }

    public void setCols(int nCols) {
        m_nCols = nCols;
        calculateAllRowsValue();
        calculateAllColsValue();
        //resetCellsCount( getTotalRows() * m_nCols);
        //resetCellsSize();
    }

    public int getTotalRows() {
        return getColTotalRows() * getCols();
    }

    public int getColTotalRows() {
        return m_nDataRows + getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) + getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows);
    }

    public int getCols() {
        return m_nCols;
    }

//    /**
//     * 1. More than current count, don't care
//     * 2. less then current count, remove them
//     *
//     * @param nCount
//     * @return
//     */
//    private boolean resetCellsCount(int nCount) {
//        int noriginal = m_arCells.size();
//        int nBalance = nCount - noriginal;
//        if (nBalance < 0) {
//            int n = Math.abs(nBalance);
//            int nIndex = nCount - 1;
//            for (int i = 0; i < n; i++) {
//                m_arCells.remove(nIndex);
//            }
//        }
//        /*
//        else if (nBalance >0)
//        {
//            int n = nBalance;
//            for (int i=0; i< n; i++)
//            {
//                KDSViewBlockCell c = new KDSViewBlockCell();
//
//                //this.add(c);
//                m_arCells.add(c);
//            }
//        }
//        */
//        return true;
//
//    }

    public ArrayList getRowsHeight() {

        return m_arRowsHeight;

    }

    public int getCalculatedAverageRowHeight()
    {
        if (m_arRowsHeight.size() >0)
            return ((int) m_arRowsHeight.get(0));
        return 0;
    }

    private Rect getCellRelativeRect(int nRow, int nCol) {
        int ninset = this.getInsetBeforeCell();// getEnv().getBlockInsets();

        int y = getRowY(m_arRowsHeight, nRow);

        int x = getColX(m_arColsWidth, nCol) + ninset;
        //if (nCol >0)
        //   x += ninset;
        int w = (int) m_arColsWidth.get(nCol) - ninset * 2;
        int h = (int) m_arRowsHeight.get(nRow);
        return new Rect(x, y, x + w, y + h);
    }


    private Rect getCellAbsoluteRect(int nRow, int nCol) {
        Rect rc = getCellRelativeRect(nRow, nCol);
        return convertRelativeToAbsolute(rc);

    }

    private Rect convertRelativeToAbsolute(Rect rect) {
        Rect rc = new Rect(rect);
        int w = rc.width();
        int h = rc.height();
        rc.left += this.getBounds().left;
        rc.top += this.getBounds().top;
        rc.right = rc.left + w;
        rc.bottom = rc.top + h;
        return rc;
    }

    private Rect getCellAbsoluteRect(int nIndex) {
        int nCol = nIndex / this.getColTotalRows();
        int nRow = nIndex % this.getColTotalRows();
        return getCellAbsoluteRect(nRow, nCol);
    }

    private int getRowY(ArrayList arRowsHeight, int nRow) {
        int y = getInsetBeforeCell();// getEnv().getBlockBorderInsets();
        for (int i = 0; i < nRow; i++) {
            y += ((int) arRowsHeight.get(i));
        }
        if (nRow >= getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) &&
                getEnv().getSettings().getInt(KDSSettings.ID.Order_Title_Rows) > 0)
            y += getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap);
        if (nRow >= this.getColTotalRows() - 1 &&
                getEnv().getSettings().getInt(KDSSettings.ID.Order_Footer_Rows) > 0) //last row
        {
            y += getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Zone_Gap);// m_nZoneGap;
        }
        return y;
    }

    public int getInsetBeforeCell() {
        int n = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        n += getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
        return n;
    }

    /**
     * the Block top-left is base point
     *
     * @param arColsWidth
     * @param nCol
     * @return
     */
    private int getColX(ArrayList arColsWidth, int nCol) {
        //int ninset =  getInsetBeforeCell();
        int x = 0;// ninset;

        for (int i = 0; i < nCol; i++) {
            //add border
            // if (i>0)
            //    x += getEnv().getBlockBorderInsets() *2;
            x += ((int) arColsWidth.get(i));
            //x += ninset;
        }
        return x;
    }

    private void setRoundCornerClip(Canvas g, Rect rt)
    {
        Path clipPath = new Path();
        int w = rt.width();
        int h = rt.height();
        clipPath.addRoundRect(new RectF(rt), CanvasDC.ROUND_CORNER_DX, CanvasDC.ROUND_CORNER_DY, Path.Direction.CW);


        g.clipPath(clipPath);
        //super.onDraw(canvas);

    }

    public void onDraw(Canvas g, int nbg) {

        g.setDrawFilter(new PaintFlagsDrawFilter(0,Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        int bg = nbg;// getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG();//.getPanelBG();
        //g.save();
        //setRoundCornerClip(g, this.getDrawableRect());
        CanvasDC.fillRect(g, bg, this.getDrawableRect());
        //CanvasDC.drawRoundRect(g, this.getDrawableRect(), bg, false, false);

        paintCells(g);
        //g.restore();
        //g.clipPath(new Path());
    }

    public void invalidateCaptionAndFooter(View view) {
        int ncount = m_arCells.size();

        for (int i = 0; i < ncount; i++) {
            KDSViewBlockCell c = (KDSViewBlockCell) m_arCells.get(i);
            if (!(c.getData() instanceof KDSDataOrder))
                continue;

            Rect rc = this.getCellAbsoluteRect(i);
            view.invalidate(rc.left, rc.top, rc.right, rc.bottom);

        }
    }

    public void onJustDrawCaptionAndFooter(Canvas g) {
        int ncount = m_arCells.size();
        int colRows = this.getColTotalRows();

        for (int i = 0; i < ncount; i++) {
            KDSViewBlockCell c = (KDSViewBlockCell) m_arCells.get(i);
            if (!(c.getData() instanceof KDSDataOrder))
                continue;
            int ncol = i / colRows;
            //g.save();
            //Rect rc = this.getCellAbsoluteRect(i);
            //   g.clipRect(rc);
            if (i ==0)
                c.onDraw(g, this.getCellAbsoluteRect(i), getEnv(), ncol, this, true);
            else
                c.onDraw(g, this.getCellAbsoluteRect(i), getEnv(), ncol, this, false);
            // g.restore();
        }

    }

    public boolean isTextWrap()
    {
        return this.getEnv().getSettings().getBoolean(KDSSettings.ID.Text_wrap);
    }

    private Rect getCombinedCellAbsoluteRect(KDSViewBlockCell c, boolean bTextWrap, int index, int nMaxCount)
    {
        Rect rcAbsoluteCell = this.getCellAbsoluteRect(index);
        if (!bTextWrap)
            return rcAbsoluteCell;
        KDSViewBlockCell cell = c;
        if (bTextWrap)
        {
            for (int i=index+1; i< nMaxCount; i++)
            {
                cell = getCell(i);
                if (cell == null) break;
                if (cell.getData() == c.getData()) {
                    Rect rc = getCellAbsoluteRect(i);
                    rcAbsoluteCell.bottom = rc.bottom;
                }
                else
                    break;

            }
        }
        return rcAbsoluteCell;
    }
    /**
     * draw cells
     * @param g
     */
    public void paintCells(Canvas g) {
        int ncount = m_arCells.size();
        int colRows = this.getColTotalRows();
        boolean bTextWrap = isTextWrap();

        for (int i = 0; i < ncount; i++) {
            KDSViewBlockCell c = (KDSViewBlockCell) m_arCells.get(i);
            int ncol = i / colRows;
            //Rect rcAbsoluteCell = this.getCellAbsoluteRect(i);
            Rect rcAbsoluteCell = this.getCombinedCellAbsoluteRect(c,bTextWrap, i, (ncol + 1)* getColTotalRows());
//            if (i == 0)
//                c.onDraw(g,rcAbsoluteCell , getEnv(), ncol, this, true);
//            else
                c.onDraw(g,rcAbsoluteCell , getEnv(), ncol, this, false);
        }
        drawBorders(g);
    }

    int m_borderPhase = 0;

    private void drawBorderInsideLines(Canvas g) {

        int panelBg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace).getBG();
        if (m_borderColor == 0) return;

        int x = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);//getEnv().getBlockBorderInsets();
        int y = x;
        int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        Rect rect = new Rect(x, y, x + getDrawableRect().width(), y + getDrawableRect().height());
        rect.inset(nBorderInset / 2, nBorderInset / 2);
        rect = convertRelativeToAbsolute(rect);


        Paint paint = new Paint();
        paint.setColor(getBorderInsideLineColor(getBorderColor(), panelBg));
        paint.setStrokeWidth(nBorderInset / 2);
        /**
         * The intervals array must contain an even number of entries (>=2), with
         * the even indices specifying the "on" intervals, and the odd indices
         * specifying the "off" intervals. phase is an offset into the intervals
         * array (mod the sum of all of the intervals). The intervals array
         * controls the length of the dashes. The paint's strokeWidth controls the
         * thickness of the dashes.
         * Note: this patheffect only affects drawing with the paint's style is set
         * to STROKE or FILL_AND_STROKE. It is ignored if the drawing is done with
         * style == FILL.
         * @param intervals array of ON and OFF distances
         * @param phase offset into the intervals array
         */

        m_borderPhase += 8;
        if (m_borderPhase > Integer.MAX_VALUE - 100)
            m_borderPhase = 0;
        PathEffect effects = new DashPathEffect(new float[]{8, 4, 3, 4}, m_borderPhase);

        paint.setPathEffect(effects);
        //top
        if (m_borderStyleTopSide != BorderStyle.BorderStyle_Break)
            g.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
        if (m_borderStyleRightSide != BorderStyle.BorderStyle_Break)
            g.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
        if (m_borderStyleBottomSide != BorderStyle.BorderStyle_Break)
            g.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, paint);
        if (m_borderStyleLeftSide != BorderStyle.BorderStyle_Break)
            g.drawLine(rect.left, rect.top, rect.left, rect.bottom, paint);
        //

    }

    /**
     * Use Line to impress focus
     */
    static public final int LINE_SIZE = 3;

    private void drawBorderInsideLines2(Canvas g) {

        int panelBg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace).getBG();
        if (m_borderColor == 0) return;

        int x = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);//getEnv().getBlockBorderInsets();
        int y = x;
        int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
        Rect rect = new Rect(x, y, x + getDrawableRect().width(), y + getDrawableRect().height());

        rect.inset(nBorderInset - LINE_SIZE, nBorderInset - LINE_SIZE);//nBorderInset / 2, nBorderInset / 2);
        rect = convertRelativeToAbsolute(rect);

        Paint paint = new Paint();
        paint.setColor(getBorderInsideLineColor(getBorderColor(), panelBg));
        paint.setStrokeWidth(LINE_SIZE);//nBorderInset / 2);

        int nSpace = nBorderInset - LINE_SIZE;
        //top
        if (m_borderStyleTopSide != BorderStyle.BorderStyle_Break)
            g.drawRect(rect.left, rect.top, rect.right, rect.top + LINE_SIZE, paint);
        //g.drawLine(rect.left, rect.top+4, rect.right, rect.top+4, paint);
        if (m_borderStyleRightSide != BorderStyle.BorderStyle_Break)
            g.drawRect(rect.right - LINE_SIZE, rect.top, rect.right, rect.bottom, paint);
        //g.drawLine(rect.right-5, rect.top, rect.right-5, rect.bottom, paint);
        if (m_borderStyleBottomSide != BorderStyle.BorderStyle_Break)
            g.drawRect(rect.left, rect.bottom - LINE_SIZE, rect.right, rect.bottom, paint);
        if (m_borderStyleLeftSide != BorderStyle.BorderStyle_Break)
            g.drawRect(rect.left, rect.top, rect.left + LINE_SIZE, rect.bottom, paint);
        //

    }

    public void drawBorders(Canvas g) {


        drawLeftBorderSide(g);
        drawRightBorderSide(g);
        drawTopBorderSide(g);
        drawBottomBorderSide(g);
        drawBorderInsideLines2(g);


    }

    public int getBorderInsideLineColor(int borderColor, int panelColor) {

        return KDSUtil.getContrastVersionForColor(borderColor);


    }


    private void draw_border(Canvas g, Rect rect, BorderStyle style, boolean bLeft2Right) {

        if (style == BorderStyle.BorderStyle_Line) {
            //CanvasDC.fillRect(g, getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color), rect );
            CanvasDC.fillRect(g, getBorderColor(), rect);

        } else if (style == BorderStyle.BorderStyle_Break) {

            //int bg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG();
            int bg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace).getBG();
            CanvasDC.fillRect(g, bg, rect);
            // g.drawColor(getEnv().getBlockBorderColor());
            if (bLeft2Right) {
                int h = rect.height();
                int ncount = rect.width() / h;
                int nleft = rect.width() % h;
                int x = rect.left;
                int y = rect.top;

                for (int i = 0; i < ncount; i++) {
                    int w = h;
                    if (i < nleft) w = h + 1;
                    int color = 0;
                    if (i % 2 == 0) {
                        color = bg;// (getEnv().getViewerBG());

                    } else {
                        color = getBorderColor();// (getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color));
                    }
                    CanvasDC.fillRect(g, color, new Rect(x, y, x + w, y + h));
                    //g2.fillRect(x, y, w, h);
                    x += w;

                }
            } else {
                int w = rect.width();
                int ncount = rect.height() / w;
                int nleft = (rect.height() % w);
                int x = rect.left;
                int y = rect.top;
                for (int i = 0; i < ncount; i++) {
                    int h = w;
                    if (i < nleft) h = w + 1;
                    int color = 0;
                    if ((i % 2) == 0) {
                        color = bg;// (getEnv().getViewerBG());

                    } else {
                        color = getBorderColor();//(getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color));
                    }
                    CanvasDC.fillRect(g, color, new Rect(x, y, x + w, y + h));
                    //g2.fillRect(x, y, w, h);
                    y += h;

                }
            }
        }
    }

    private Rect getLeftBorderAbsoluteRect(boolean bFocused) {
        Rect rect = null;
        if (bFocused) {
            int x = 0;//
            int nblockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);//getEnv().getBlockBorderInsets();
            int y = x;
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
            rect = new Rect(x, y, x + nBorderInset + nblockInset, y + getDrawableRect().height() + nblockInset * 2);
        } else {
            int x = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);//getEnv().getBlockBorderInsets();
            int y = x;
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
            rect = new Rect(x, y, x + nBorderInset, y + getDrawableRect().height());
        }
        return rect;
    }

    private void drawLeftBorderSide(Canvas g) {
        // Graphics2D g2 = (Graphics2D)g;
        if (!isFocused())
        {//clear old block inset focus color first
            Rect rc = getLeftBorderAbsoluteRect(true);
            rc = convertRelativeToAbsolute(rc);
            int nviewbg = getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            CanvasDC.fillRect(g, nviewbg, rc);
        }
        Rect rect = getLeftBorderAbsoluteRect(isFocused());


        rect = convertRelativeToAbsolute(rect);
        draw_border(g, rect, m_borderStyleLeftSide, false);

        //g2.setColor(c);
    }

    private Rect getRightBorderAbsoluteRect(boolean bFocused)
    {
        Rect rect = null;
        if (bFocused) {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);

            int x = getBoundsWidth() - nBorderInset - nBlockInset;//getEnv().getBlockBorderInsets();
            int y = 0;//nBlockInset;
            rect = new Rect(x, y, x + nBorderInset+nBlockInset, y + getDrawableRect().height()+nBlockInset * 2);
        } else {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);

            int x = getBoundsWidth() - nBorderInset - nBlockInset;//getEnv().getBlockBorderInsets();
            int y = nBlockInset;
            rect = new Rect(x, y, x + nBorderInset, y + getDrawableRect().height());
        }
        return rect;
    }
    private void drawRightBorderSide(Canvas g) {

        if (!isFocused())
        {//clear old block inset focus color first
            Rect rc = getRightBorderAbsoluteRect(true);
            rc = convertRelativeToAbsolute(rc);
            int nviewbg = getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            CanvasDC.fillRect(g, nviewbg, rc);
        }

        Rect rect = getRightBorderAbsoluteRect(isFocused());
        rect = convertRelativeToAbsolute(rect);
        draw_border(g,rect, m_borderStyleRightSide, false );

        
    }
    private Rect getTopBorderAbsoluteRect(boolean bFocused)
    {
        Rect rect = null;
        if (bFocused) {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
            int x = 0;//nBlockInset;
            int y = 0;//nBlockInset;
            rect = new Rect(x, y, x + getDrawableWidth()+nBlockInset*2, y + nBorderInset+nBlockInset);
        } else {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);
            int x = nBlockInset;
            int y = nBlockInset;
            rect = new Rect(x, y, x + getDrawableWidth(), y + nBorderInset);
        }
        return rect;
    }
    private void drawTopBorderSide(Canvas g)
    {

        if (!isFocused())
        {//clear old block inset focus color first
            Rect rc = getTopBorderAbsoluteRect(true);
            rc = convertRelativeToAbsolute(rc);
            int nviewbg = getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            CanvasDC.fillRect(g, nviewbg, rc);
        }

        Rect rect = getTopBorderAbsoluteRect(isFocused());
        rect = convertRelativeToAbsolute(rect);
        draw_border(g, rect, m_borderStyleTopSide, true);

    }
    private Rect getBottomBorderAbsoluteRect(boolean isFocused)
    {
        Rect rect = null;
        if (isFocused) {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);

            int x = 0;
            int y = getBoundsHeight() - nBlockInset - nBorderInset;
            //int n = getDrawbleHeight() -  getEnv().getBlockBorderInsets();
            // Color c = g2.getColor();
            rect = new Rect(x, y, x + getDrawableWidth()+nBlockInset * 2, y + nBorderInset+nBlockInset);
        } else {
            int nBlockInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Inset);
            int nBorderInset = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset);

            int x = nBlockInset;
            int y = getBoundsHeight() - nBlockInset - nBorderInset;
            //int n = getDrawbleHeight() -  getEnv().getBlockBorderInsets();
            // Color c = g2.getColor();
            rect = new Rect(x, y, x + getDrawableWidth(), y + nBorderInset);
        }
        return rect;
    }
    private void drawBottomBorderSide(Canvas g)
    {

        if (!isFocused())
        {//clear old block inset focus color first
            Rect rc = getBottomBorderAbsoluteRect(true);
            rc = convertRelativeToAbsolute(rc);
            int nviewbg = getEnv().getSettings().getInt(KDSSettings.ID.Panels_View_BG);
            CanvasDC.fillRect(g, nviewbg, rc);
        }


        Rect rect = getBottomBorderAbsoluteRect(isFocused());
        rect = convertRelativeToAbsolute(rect);
        draw_border(g, rect, m_borderStyleBottomSide, true);


    }
    
    /***************************************************************************
     * Split this panel to two panels, 
     * From nStart
     */
    /***************************************************************************
     * 
     * @param nStartCol
     * from this col index, previous will kept, others will create new panel
     * and return new panel
     * ????????????????????????????????????????????????
     * NOTICE: Please notice, this panel size and position don't set.
     * ???????????????????????????????????????????????????
     *  TODO: the coordinate is wrong !!
     * @return 
     * return split new panel
     */
    public KDSViewBlock split(int nStartCol)
    {
        KDSViewBlock p = new KDSViewBlock(getViewer());
        int nCols = this.getCols() - nStartCol + 1;
        p.setCols( nCols);
        int nIndex = nStartCol * this.getColTotalRows();
        ArrayList rows = this.getCells();
        ArrayList newRows = p.getCells();
        //copy data
        for (int i=nIndex; i< rows.size(); i++)
        {
            KDSViewBlockCell c = (KDSViewBlockCell) rows.get(i);
            newRows.add(c);


        }
        //remove old cell from this panel
        nCols = this.getCols() - nCols;
        this.setCols( nCols);
        return p;
        
    }

    
    public boolean setCellData(int nIndex, Object obj)
    {
        return getCell(nIndex).setData(obj);
    }

    public  int getTotalRowsInBlock()
    {
        return getColTotalRows() * getCols();
    }
    public boolean isFull()
    {
        return ( getTotalRowsInBlock() == getCells().size());

    }
    public int getFreeRows()
    {
        return ( getColTotalRows() - getCells().size());
    }

    public KDSViewBlockCell getClickedCell(int x, int y)
    {
        for (int i=0; i< m_arCells.size(); i++)
        {
            Rect rc = this.getCellAbsoluteRect(i);
            if (rc.contains(x, y))
                return this.getCell(i);
        }
        return null;
    }

    /**
     *
     * @param nColor
     */
    public void setBorderColor(Canvas g,  int nColor)
    {

        if (nColor == m_borderColor)
        {
            if (m_borderColor !=0)
                drawBorders(g); //make the border moving
            return;
        }


        m_borderColor = nColor;

        drawBorders(g);
    }
    public void setBorderColorToDefault(Canvas g)
    {
        setBorderColor(g, 0);
    }
    public int getBorderColor()
    {
        if (m_borderColor != 0)
            return m_borderColor;
        else
            return  getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color);// getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG();
    }

    public boolean isFocused()
    {
        int defaultBorderColor = getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color);
        return (getBorderColor() != defaultBorderColor);
    }

    public void setDrawBorderInsideLine(boolean bInset)
    {
        m_bDrawBorderInsideLine = bInset;
    }
    public boolean getDrawBorderInsideLine()
    {
        return m_bDrawBorderInsideLine;
    }

    /**
     * return the panel which contains this block
     * @return
     */
    public KDSViewPanel getMyPanel()
    {
        if (this.m_parentViewer == null)
            return null;
        KDSViewPanel panel =  this.m_parentViewer.getBlockPanel(this);
        return panel;
    }


//    private void draw_border2(Canvas g, Rect rect, BorderStyle style, boolean bLeft2Right) {
//
//        if (style == BorderStyle.BorderStyle_Line) {
//            //CanvasDC.fillRect(g, getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color), rect );
//            CanvasDC.fillRect(g, getBorderColor(), rect);
//
//        } else if (style == BorderStyle.BorderStyle_Break) {
//
//            //int bg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG();
//            int bg = getEnv().getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace).getBG();
//            CanvasDC.fillRect(g, bg, rect);
//            // g.drawColor(getEnv().getBlockBorderColor());
//            if (bLeft2Right) { //left to right
//                int h = rect.height();
//                int ncount = rect.width() / h;
//                int nleft = rect.width() % h;
//                int x = rect.left;
//                int y = rect.top;
//
//                for (int i = 0; i < ncount; i++) {
//                    int w = h;
//                    if (i < nleft) w = h + 1;
//                    int color = 0;
//                    if (i % 2 == 0) {
//                        color = bg;// (getEnv().getViewerBG());
//
//                    } else {
//                        color = getBorderColor();// (getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color));
//                    }
//                    CanvasDC.fillRect(g, color, new Rect(x, y, x + w, y + h));
//                    //g2.fillRect(x, y, w, h);
//                    x += w;
//
//                }
//            } else { //up to down
//                int w = rect.width()*2;
//                int ncount = rect.height() / w;
//                int nleft = (rect.height() % w);
//                int x = rect.left;
//                int y = rect.top;
//                Path path = new Path();
//                //x += rect.width()/2;
//                path.moveTo(x, y); //start point
//                for (int i = 0; i < ncount; i++) {
//                    int h = w;
//                    if (i < nleft) h = w + 1;
//                    path.rQuadTo(h, h/2, 0, h);
//                    //path.rQuadTo(0, -1*h/2, 0, h);
//
//
////                    int color = 0;
////                    if ((i % 2) == 0) {
////                        color = bg;// (getEnv().getViewerBG());
////
////                    } else {
////                        color = getBorderColor();//(getEnv().getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Color));
////                    }
////                    CanvasDC.fillRect(g, color, new Rect(x, y, x + w, y + h));
////                    //g2.fillRect(x, y, w, h);
////                    y += h;
//
//                }
//                Paint p = new Paint();
//                p.setColor(getBorderColor());
//                p.setStrokeWidth(1);
//                g.drawPath(path, p);
//            }
//        }
//    }


}
