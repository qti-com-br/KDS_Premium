package com.bematechus.kds;

import android.app.Application;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSData;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataFromPrimaryIndicator;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataMessage;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataMoreIndicator;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataVoidItemIndicator;
import com.bematechus.kdslib.KDSDataVoidItemQtyChanged;
import com.bematechus.kdslib.KDSStationIP;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 *  For order view panel cell(row).
 */
public class KDSLayoutCell extends KDSViewBlockCell {

    public enum CellSubType
    {
        Unknown,
        OrderTitle_Second,
        OrderTitle_Expand,
        OrderTitle_Second_Expand,
        OrderFooter,
        OrderFooter_Last,
        Addon_String,
    }

//    public final  int DIM_BG = Color.GRAY;
//    public final int DIM_FG = Color.DKGRAY;

    CellSubType m_cellSubType = CellSubType.Unknown;

    public int m_nTextWrapRowIndex  =0;

    public KDSLayoutCell()
    {
        super();
    }

    public void setCellSubType(CellSubType t)
    {
        m_cellSubType = t;
    }
    public CellSubType getCellSubType()
    {
        return m_cellSubType;
    }



    public boolean onDraw(Canvas g,Rect rcAbsolute,  KDSViewSettings env, int nColInBlock, KDSViewBlock block, boolean bRoundCorner)
    {

        return drawCell(g, rcAbsolute, env,nColInBlock, block, bRoundCorner);

    }

    /**
     * draw text here
     * @param g
     * @param rcAbsolute
     * @param env
     * @return
     */
    @Override
    protected boolean drawCell(Canvas g,Rect rcAbsolute,KDSViewSettings env, int nColInBlock, KDSViewBlock block, boolean bRoundCorner)
    {
        Object obj = this.getData();
        if (obj == null) return true;

        if (obj instanceof KDSDataOrder)
        {
            return drawDataOrder(g, rcAbsolute, env,nColInBlock, block, bRoundCorner);
        }
        else if (obj instanceof KDSDataMessage)
        {
            return drawDataMessage(g, rcAbsolute, env, block, nColInBlock);
//            if (env.getSettings().getBoolean(KDSSettings.ID.Item_mark_with_char))
//                return drawDataMessage(g, rcAbsolute, env);
//            else
//                return drawDataMessageWithIcon(g, rcAbsolute, env);
        }
        else if (obj instanceof KDSDataMoreIndicator)
        {
            return drawMoreIndicator(g, rcAbsolute, env);
        }
        else if (obj instanceof KDSDataCategoryIndicator)
        { //2.0.47
            return drawCategoryIndicator(g, rcAbsolute, env, (KDSDataCategoryIndicator)obj);
        }
        else if (obj instanceof KDSDataFromPrimaryIndicator)
        {
            return drawFromPrimaryIndicator(g, rcAbsolute, env);
        }
        else if (obj instanceof KDSDataVoidItemIndicator)
        {//it should locate in front of kdsdataitem, as it is from kdsdataitem
            return drawVoidIndicator(g, rcAbsolute, env);
        }
        else if (obj instanceof KDSDataVoidItemQtyChanged)
        {//it should locate in front of kdsdataitem, as it is from kdsdataitem
            return drawVoidItemQtyChange(g, rcAbsolute, env);
        }
        else if (obj instanceof KDSDataItem)
        {
            return drawDataItem(g, rcAbsolute, env,nColInBlock, block);

//            if (env.getSettings().getBoolean(KDSSettings.ID.Item_mark_with_char))
//                return drawDataItem(g, rcAbsolute, env);
//            else
//                return drawDataItemWithIcon(g, rcAbsolute, env);

        }
        else if (obj instanceof KDSDataModifier) //it should above KDSDataCondiment as KDSDataModifier is child of KDSDataCondiment
        {
            return drawDataCondiment(g, rcAbsolute, env, block,nColInBlock);
        }
        else if (obj instanceof KDSDataCondiment)
        {
            return drawDataCondiment(g, rcAbsolute, env, block,nColInBlock);
//            if (env.getSettings().getBoolean(KDSSettings.ID.Item_mark_with_char))
//                return drawDataCondiment(g, rcAbsolute, env);
//            else
//                return drawDataCondimentWithIcon(g, rcAbsolute, env);
        }

        else
        {
            return true;
        }
    }

    /**
     * draw order data in title or footer
     * @param g
     * @param rcAbsolute
     * @param env
     * @return
     */
    protected boolean drawDataOrder(Canvas g,Rect rcAbsolute,KDSViewSettings env, int nColInBlock, KDSViewBlock block, boolean bRoundCorner)
    {

        CellSubType subtype =  this.getCellSubType();
        KDSDataOrder order = (KDSDataOrder)this.getData();

        if (this.getCellSubType() == CellSubType.OrderFooter_Last || this.getCellSubType() == CellSubType.OrderFooter)
            this.getFont().copyFrom( env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Footer_FontFace));
        else
            this.getFont().copyFrom( env.getSettings().getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace));

        int nBG = KDSView.getOrderCaptionBackgroundColor(order,env, this.getFont());
//        //get the background color according to the time.
//        int nBG = env.getSettings().getOrderTimeColorAccordingWaitingTime(order.getStartToCookTime(), this.getFont().getBG());
//        //exp alert
//        if (env.getSettings().isExpeditorStation())
//        { //the exp aler color
//            if (order.isItemsAllBumpedInExp())
//            {
//                nBG = env.getSettings().getExpAlertTitleBgColor(true, this.getFont().getBG());
//            }
//        }
//
//        if (order.isDimColor())
//            nBG = KDSConst.DIM_BG;

        //

        if (env.getStateValues().isFocusedOrderGUID(order.getGUID())) {

            int focusBorderBG = env.getSettings().getInt(KDSSettings.ID.Focused_BG);//.getKDSViewFontFace(KDSSettings.ID.Order_Focused_FontFace).getBG();// this.getFont().getBG();

            KDSViewPanel panel = block.getMyPanel();
            if (panel != null) {
                if (!env.getSettings().getBoolean(KDSSettings.ID.Blink_focus))
                    panel.setBorderColor(g, focusBorderBG);
                else
                {
                    if (KDSGlobalVariables.getBlinkingStep())
                        panel.setBorderColor(g, focusBorderBG);
                    else
                        panel.setBorderColorToDefault(g);

                }
            }

        }
        else
        {
            KDSViewPanel panel = block.getMyPanel();
            if (panel != null)
                panel.setBorderColorToDefault(g);

        }
        Rect rc = new Rect(rcAbsolute);
        if (nColInBlock >0)
        {//expend bg, make it seems same colors title

            rc.left -= env.getSettings().getInt(KDSSettings.ID.Panels_Block_Border_Inset)*2;
            rc.left -= env.getSettings().getInt(KDSSettings.ID.Panels_Block_Inset)*2;

        }
//        if (bRoundCorner)
//            CanvasDC.drawRoundRect(g, rc, nBG, true, false);
//        else
            CanvasDC.fillRect(g, nBG, rc);

        Object l = getOrderContentObject((KDSDataOrder) this.getData(),KDSSettings.TitlePosition.Left,this.getCellSubType(), env);
        Object r = getOrderContentObject((KDSDataOrder) this.getData(),KDSSettings.TitlePosition.Right,this.getCellSubType(), env);
        Object c = getOrderContentObject((KDSDataOrder) this.getData(),KDSSettings.TitlePosition.Center,this.getCellSubType(), env);

        KDSViewFontFace fontDef = new KDSViewFontFace();
        fontDef.copyFrom(this.getFont());
        fontDef.setBG(nBG);

        if (order.getCookState() == KDSDataOrder.CookState.Started
                && this.getCellSubType() == CellSubType.Unknown ) {//just first caption line show started icon

            Drawable drawable = env.getSettings().getOrderCookStartedImage();
            drawable.setBounds(rcAbsolute.left+KDSConst.IMAGE_GAP, rcAbsolute.top+KDSConst.IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-KDSConst.IMAGE_GAP, rcAbsolute.bottom-KDSConst.IMAGE_GAP);//, rcAbsolute.height());

            drawable.draw(g);
            rcAbsolute.left += rcAbsolute.height()+KDSConst.IMAGE_GAP;
        }

        rcAbsolute = drawOrderTitleContent(g, rcAbsolute, env, order, l, KDSSettings.TitlePosition.Left, Paint.Align.LEFT, fontDef);

//        if (l instanceof String) {
//            String strLeft = (String)l;
//            if (!strLeft.isEmpty()) {
//                KDSSettings.TitleContents content = getOrderContentType(KDSSettings.TitlePosition.Left, env);
//                KDSViewFontFace ff = getOrderContentFont(env, content, fontDef);
//                if (order.isDimColor()) {
//                    ff.setBG(DIM_BG);
//
//                }
//                CanvasDC.drawText(g, ff, rcAbsolute, strLeft, Paint.Align.LEFT);
//            }
//        }

        rcAbsolute = drawOrderTitleContent(g, rcAbsolute, env, order, r, KDSSettings.TitlePosition.Right, Paint.Align.RIGHT, fontDef);

//        if (r instanceof String) {
//            String strRight = (String)r;
//            if (!strRight.isEmpty()) {
//                KDSSettings.TitleContents content = getOrderContentType(KDSSettings.TitlePosition.Right, env);
//                KDSViewFontFace ff = getOrderContentFont(env, content, fontDef);
//                if (order.isDimColor()) ff.setBG(DIM_BG);
//                CanvasDC.drawText(g, ff, rcAbsolute, strRight, Paint.Align.RIGHT);
//            }
//        }

        rcAbsolute = drawOrderTitleContent(g, rcAbsolute, env, order, c, KDSSettings.TitlePosition.Center, Paint.Align.CENTER, fontDef);

//        if (c instanceof String) {
//            String strCenter = (String)c;
//            if (!strCenter.isEmpty()) {
//                KDSSettings.TitleContents content = getOrderContentType(KDSSettings.TitlePosition.Center, env);
//                KDSViewFontFace ff = getOrderContentFont(env, content, fontDef);
//                if (order.isDimColor()) ff.setBG(DIM_BG);
//                CanvasDC.drawText(g, ff, rcAbsolute, strCenter, Paint.Align.CENTER);
//            }
//        }

//        if (order.getIconIdx()>=0) //draw icon
//        {
//
//        }

        return true;
    }
    final int MAX_TITLE_INSET = 8;
    public Rect drawOrderTitleContent(Canvas g,Rect rcAbsolute,KDSViewSettings env, KDSDataOrder order, Object objContent,KDSSettings.TitlePosition titlePosition,Paint.Align align,KDSViewFontFace ftDef   )
    {
        if (objContent == null)
            return rcAbsolute;
        if (objContent instanceof String) {
            String str = (String)objContent;
            if (!str.isEmpty()) {
                KDSSettings.TitleContents content = getOrderContentType(titlePosition,this.getCellSubType(), env);
                KDSViewFontFace ff = getOrderContentFont(env, content, ftDef);
                if (order.isDimColor()) ff.setBG(KDSConst.DIM_BG);
                Rect rc = new Rect(rcAbsolute);
                int nInset = rc.height()/10;
                if (nInset >MAX_TITLE_INSET) nInset = MAX_TITLE_INSET;
                rc.inset(nInset, nInset);
                //rc.inset(8, 8);
                CanvasDC.drawText(g, ff, rc, str, align);
            }
        }
        else if ( objContent instanceof Drawable)
        {
            Drawable drawable = (Drawable)objContent;
            //Drawable drawable = env.getSettings().getItemFocusImage();
            int h = rcAbsolute.height();
            int w = h;
            int x = 0;
            int y = rcAbsolute.top;

            switch (titlePosition)
            {

                case Left:
                    x = rcAbsolute.left;
                    break;
                case Center:
                    x = rcAbsolute.left + (rcAbsolute.width() - w)/2;

                    break;
                case Right:
                    x = rcAbsolute.right - w;
                    break;
            }

            Rect rc = new Rect(x, y, x + w, y + h);
            int nsize = h;
            //drawable.setBounds(rc.left+IMAGE_GAP, rc.top+IMAGE_GAP, rc.left + nsize-IMAGE_GAP, rc.bottom-IMAGE_GAP);
            drawable.setBounds(rc.left, rc.top, rc.left + nsize, rc.bottom);

            drawable.draw(g);

        }

        return rcAbsolute;

    }

    static public Object getOrderContentObject(KDSDataOrder order,KDSSettings.TitlePosition position,CellSubType subtype, KDSViewSettings env)
    {
        KDSSettings.TitleContents content = getOrderContentType(position,subtype, env);
        return getOrderContentObject(order,content,env);
    }
    static public  Object getOrderContentObject(KDSDataOrder order, KDSSettings.TitleContents content, KDSViewSettings env)
    {
        //KDSDataOrder order = (KDSDataOrder)this.getData();
        switch (content)
        {
            case NULL:
            {
                return "";
            }
            case Name:
            {
                return order.getOrderName();
            }
            case Timer:
            {

                Date dt = order.getStartTime();
                return formatTimeDuration(dt);

//                Date dtNow = new Date(System.currentTimeMillis());
//                long l = dtNow.getTime() - dt.getTime();
//                long day=l/(24*60*60*1000);
//                long hour=(l/(60*60*1000)-day*24);
//                long min=((l/(60*1000))-day*24*60-hour*60);
//                long sec=(l/1000-day*24*60*60-hour*60*60-min*60);
//                long hours = hour + day * 24;
//
//
//                if (hours <=0)
//                    return String.format("%02d:%02d", min, Math.abs(sec));
//
//                else
//                    return String.format("%d:%02d:%02d",hours, Math.abs(min), Math.abs(sec));

            }

            case Waiter:
                return order.getWaiterName();

            case ToTable:
                return order.getToTable();

            case FromPOS:
                return order.getFromPOSNumber();

            case OrderType:
                return order.getOrderType();

            case Destination:
                return order.getDestination();
            case CustMessage:
                return order.getCustomMsg();
            case OrderStatus:
                {
                    if (order.getStatus() == KDSSettings.OrderStatus.Paid.ordinal())
                    {
                        return KDSApplication.getContext().getString(R.string.paid);
                    }
                    else if (order.getStatus() == KDSSettings.OrderStatus.Unpaid.ordinal())
                    {
                        return KDSApplication.getContext().getString(R.string.unpaid);
                    }
                    else if (order.getStatus() == KDSSettings.OrderStatus.Inprogress.ordinal())//2.0.30
                        return KDSApplication.getContext().getString(R.string.inprogress);
                    else
                    {
                        return KDSApplication.getContext().getString(R.string.unpaid);
                    }

                }
            case OrderIcon:
            {
//                if (!env.getSettings().getBoolean(KDSSettings.ID.Icon_enabled))
//                    return "";
                int n = order.getIconIdx();
                if (n<0) return "";
                return env.getSettings().getIcon(n);
            }
            //break;
            case HeaderFooterMessage:
            {
                return order.getHeaderFooterMessage();
            }
            //break;
            case CustomerName:
            {
                return order.getCustomer().getName();
            }
            case InputMsg:
            {
                return order.getInputMessage();
            }
            case CourseTime:
            {
                long ms = order.smart_get_sorts().course_start_time(order);
                if (ms >0)
                {
                    long n = order.getStartTime().getTime();
                    n += ms;
                    Date dt = new Date(n);
                    return formatTimeDuration(dt);

                }
                return "";
            }
            default:
                return "";
        }
    }

    private KDSViewFontFace getOrderContentFont( KDSViewSettings env, KDSSettings.TitleContents content, KDSViewFontFace ffDefault)
    {
        KDSDataOrder order = (KDSDataOrder)this.getData();
        switch (content)
        {
            case NULL:
            case Name:
            case Timer:
            case Waiter:
            case ToTable:
            case FromPOS:
            case CustMessage:
            case OrderStatus:
            case OrderIcon:
            case HeaderFooterMessage:
            case CustomerName:

                return ffDefault;
            case OrderType:
            {
                if (order.isRush()) {
                    if (env.getSettings().getBoolean(KDSSettings.ID.Highlight_rush_enabled))
                    {
                        KDSViewFontFace ff = new KDSViewFontFace();
                        ff.copyFrom(ffDefault);
                        String bgfg = env.getSettings().getString(KDSSettings.ID.Highlight_rush_bgfg);
                        KDSBGFG bf = KDSBGFG.parseString(bgfg);
                        int bg = bf.getBG();//
                        int fg = bf.getFG();//
                        ff.setBG(bg);
                        ff.setFG(fg);
                        return ff;
                    }

                }
                else if (order.isFire())
                {
                    if (env.getSettings().getBoolean(KDSSettings.ID.Highlight_fire_enabled))
                    {
                        KDSViewFontFace ff = new KDSViewFontFace();
                        ff.copyFrom(ffDefault);
                        String bgfg = env.getSettings().getString(KDSSettings.ID.Highlight_fire_bgfg);
                        KDSBGFG bf = KDSBGFG.parseString(bgfg);
                        int bg = bf.getBG();//
                        int fg = bf.getFG();//
                        ff.setBG(bg);
                        ff.setFG(fg);
                        return ff;
                    }
                }

                return ffDefault;
            }

            case Destination:
            {
                if (env.getSettings().getBoolean(KDSSettings.ID.Highlight_dest_enabled))
                {
                    KDSViewFontFace ff = new KDSViewFontFace();
                    ff.copyFrom(ffDefault);
                    String bgfg = env.getSettings().getString(KDSSettings.ID.Highlight_dest_bgfg);
                    KDSBGFG bf = KDSBGFG.parseString(bgfg);
                    int bg = bf.getBG();//
                    int fg = bf.getFG();//
                    ff.setBG(bg);
                    ff.setFG(fg);
                    return ff;
                }
            }
            break;
            default:
                break;
        }
        return ffDefault;
    }


    static  public KDSSettings.TitleContents getOrderContentType(KDSSettings.TitlePosition position,CellSubType subtype, KDSViewSettings env)
    {
        //CellSubType subtype =  this.getCellSubType();

        switch (subtype)
        {
            case Unknown:
                return env.getSettings().getTitleContent(0, position);//.getOrderTitleSettings().getTitleContent(0, position);
            case OrderTitle_Second:
                return env.getSettings().getTitleContent(1, position);

            case OrderTitle_Expand:
                break;
            case OrderTitle_Second_Expand:
                break;
            case OrderFooter:
                //return env.getSettings().getFooterContent(position);
                break;
            case OrderFooter_Last:
                return env.getSettings().getFooterContent(position);
                //break;
            default:
                break;
        }

        return KDSSettings.TitleContents.NULL;
    }

//    protected boolean drawDataMessage(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataMessage c =(KDSDataMessage) this.getData();
//
//        int bg = this.getFont().getBG();
//        //if the bg is same as panel color, transparent it.
//        int panelBg = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//       // if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )
//        if (bg == panelBg )
//            bg = Color.TRANSPARENT;
//
//        if (c.isDimColor())
//            bg = DIM_BG;
//
//        CanvasDC.fillRect(g, bg, rcAbsolute);
//
//        String strDescription = c.getMessage();
//
//        String s =getSpaces(7);
//        s += strDescription;
//
//        if (c.getFocusTag() != null) { //for hide item, show focus
//            Object obj = c.getFocusTag();
//            if (obj instanceof KDSDataItem) {
//                KDSDataItem item = (KDSDataItem) obj;
//                s = buildItemStateString(item, s, env);
//
//            }
//        }
//
//
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (c.isDimColor())
//        {
//            this.getFont().setFG(DIM_FG);
//        }
//        this.getFont().setBG(bg);
//        CanvasDC.drawText(g, this.getFont(), rcAbsolute,s,  Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//        return true;
//    }
//
//    protected boolean drawDataMessageWithIcon(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataMessage c =(KDSDataMessage) this.getData();
//
//        int bg = this.getFont().getBG();
//        //if the bg is same as panel color, transparent it.
//        int panelBg = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//        // if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )
//        if (bg == panelBg )
//            bg = Color.TRANSPARENT;
//
//        if (c.isDimColor())
//            bg = DIM_BG;
//
//        CanvasDC.fillRect(g, bg, rcAbsolute);
//
//        String strDescription = c.getMessage();
//
//        String s =getSpaces(7);
//        s += strDescription;
//
//        if (c.getFocusTag() != null) { //for hide item, show focus
//            Object obj = c.getFocusTag();
//            if (obj instanceof KDSDataItem) {
//                    KDSDataItem item = (KDSDataItem)obj;
//                    rcAbsolute = drawItemStateIcon(g, item, rcAbsolute, env);
//
////                    if (env.getStateValues().isFocusedItemGUID(item.getGUID())) {//draw focused symble
////
////                        Drawable drawable = env.getSettings().getItemFocusImage();
////                        drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////                        drawable.draw(g);
////                        rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
////                    }
//                }
//
//        }
//
//
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (c.isDimColor())
//        {
//            this.getFont().setFG(DIM_FG);
//        }
//        this.getFont().setBG(bg);
//        CanvasDC.drawText(g, this.getFont(), rcAbsolute,s,  Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//        return true;
//    }
    static public String getSpaces(int nCount)
    {
        String s = "";
        for (int i=0; i< nCount; i++)
            s += " ";
        return s;
    }

    /**
     * for hide item
     * @param g
     * @param rcAbsolute
     * @param env
     * @return
     */
    static public boolean drawMoreIndicator(Canvas g,Rect rcAbsolute,KDSViewSettings env)
    {
        Drawable drawable = env.getSettings().getItemMoreImage();
        int nsize = rcAbsolute.height();
        int nleft = rcAbsolute.left + (rcAbsolute.width()-nsize)/2;
        drawable.setBounds(nleft, rcAbsolute.top + 1, nleft + nsize, rcAbsolute.bottom - 1);//, rcAbsolute.height());

        drawable.draw(g);
        //rcAbsolute.left += nsize+IMAGE_GAP;// rcAbsolute.height();
        return true;
    }

    /**
     * 2.0.47
     * @param g
     * @param rcAbsolute
     * @param env
     * @param indicator
     * @return
     */
    static public boolean drawCategoryIndicator(Canvas g,Rect rcAbsolute,KDSViewSettings env, KDSDataCategoryIndicator indicator)
    {
        KDSDataCategoryIndicator c =indicator;
        //KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace);// env.getSettings().getKDSViewFontFace(KDSSettings.ID.From_primary_font);
        //kpp1-420, use category font
        KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Category_group_font);
        int bg = ff.getBG();// Color.WHITE;//c.getBG();
        int fg = ff.getFG();//Color.RED;

        //draw background
        CanvasDC.fillRect(g, bg, rcAbsolute);

        String strDescription = KDSDataCategoryIndicator.makeDisplayString(c);
//
//                c.getCategoryDescription();
//        strDescription = " -- " + strDescription + " --";
        CanvasDC.drawText(g, ff, rcAbsolute, strDescription, Paint.Align.LEFT);

        return true;
    }


    protected boolean drawFromPrimaryIndicator(Canvas g,Rect rcAbsolute,KDSViewSettings env)
    {
        KDSDataFromPrimaryIndicator c =(KDSDataFromPrimaryIndicator) this.getData();
        return drawFromPrimaryIndicator(g, rcAbsolute, env, c);
//        KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.From_primary_font);
//
//        int bg = ff.getBG();// Color.WHITE;//c.getBG();
//        int fg = ff.getFG();//Color.RED;
//
//        //draw background
//        CanvasDC.fillRect(g, bg, rcAbsolute);
//
//        String strDescription = env.getSettings().getString(KDSSettings.ID.From_primary_text);// "---From Primary---";//
//
//        CanvasDC.drawText(g, ff, rcAbsolute, strDescription, Paint.Align.CENTER);
//
//        return true;
    }

    static public boolean drawFromPrimaryIndicator(Canvas g,Rect rcAbsolute,KDSViewSettings env, KDSDataFromPrimaryIndicator c)
    {

        KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.From_primary_font);

        int bg = ff.getBG();// Color.WHITE;//c.getBG();
        int fg = ff.getFG();//Color.RED;

        //draw background
        CanvasDC.fillRect(g, bg, rcAbsolute);

        String strDescription = env.getSettings().getString(KDSSettings.ID.From_primary_text);// "---From Primary---";//

        CanvasDC.drawText(g, ff, rcAbsolute, strDescription, Paint.Align.CENTER);

        return true;
    }

//    protected boolean drawDataItem(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataItem item =(KDSDataItem) this.getData();
//
//        //draw background
//        int nbg = this.getFont().getBG();
//        //if the bg is same as panel color, transparent it.
//        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//
//        if (nbg == panelBG )
//            nbg = Color.TRANSPARENT;
//
//        if (item.isDimColor())
//        {
//            nbg = DIM_BG;
//
//        }
//        CanvasDC.fillRect(g, nbg, rcAbsolute);
//
//        String guid = item.getGUID();
//
//        float qty = item.getShowingQty();
//        String strDescription = item.getDescription();
//
//        String s = getSpaces(2);
//        if (item.getAlign() == Paint.Align.RIGHT)
//        {
//
//            s += strDescription;
//            s += getSpaces(3);
//            s += Integer.toString((int) qty);
//        }
//        else {
//            s += Integer.toString((int) qty);
//            s += getSpaces(3);
//            s += strDescription;
//        }
//
//        s = buildItemStateString( item, s, env);
//
//
//        //reset font bg.
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (item.isDimColor())
//        {
//
//            this.getFont().setFG(DIM_FG);
//        }
//        this.getFont().setBG(nbg);
//        if (item.getAlign() == Paint.Align.RIGHT)
//            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.RIGHT);
//        else
//            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//
//        return true;
//    }
//

//    protected String buildItemStateString(KDSDataItem item,String s, KDSViewSettings env)
//    {
//        String guid = item.getGUID();
//        //remote bumped in other stations, this is for expeditor
//        if (!item.getBumpedStationsString().isEmpty())
//        {
//            String strExpMarkBumpedInOthers = env.getSettings().getString(KDSSettings.ID.Item_Exp_Bumped_In_Others);
//            s = strExpMarkBumpedInOthers + s;
//            //s = "#" + s;
//        }
//        //bump item in normal station
//        if (item.getLocalBumped())
//        {
//            String strBumpedMark = env.getSettings().getString(KDSSettings.ID.Item_Bumped_Mark);
//            s = strBumpedMark + s;
//        }
//
//        if (env.getStateValues().isFocusedItemGUID(guid)) {//draw focused symble
//
//            String strFocusedMark = env.getSettings().getString(KDSSettings.ID.Item_Focused_Mark);
//
//            s = strFocusedMark + s;
//        }
//
//        if (item.getDeleteByRemoteCommand())
//        {
//            String strMark = env.getSettings().getString(KDSSettings.ID.Item_void_mark_with_char);
//            s = strMark + s;
//        }
//
//        if (item.getChangedQty() != 0)
//        {
//            String strMark = env.getSettings().getString(KDSSettings.ID.Item_Changed_mark_with_char);
//            s = strMark + s;
//        }
//
//        return s;
//
//    }

    //static final int IMAGE_GAP =1;
//    protected boolean drawDataItemWithIcon(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataItem item =(KDSDataItem) this.getData();
//
//        //draw background
//        int nbg = this.getFont().getBG();
//        //if the bg is same as panel color, transparent it.
//        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//
//        if (nbg == panelBG)
//            nbg = Color.TRANSPARENT;
//
//        if (item.isDimColor())
//        {
//            nbg = DIM_BG;
//        }
//        CanvasDC.fillRect(g, nbg, rcAbsolute);
//
//        String guid = item.getGUID();
//
//
//        float qty = item.getShowingQty();
//        String strDescription = item.getDescription();
//
//        String s = getSpaces(2);
//        if (item.getAlign() == Paint.Align.RIGHT)
//        {
//
//            s += strDescription;
//            s += getSpaces(3);
//            s += Integer.toString((int) qty);
//        }
//        else {
//            s += Integer.toString((int) qty);
//            s += getSpaces(3);
//            s += strDescription;
//        }
//
//        rcAbsolute = drawItemStateIcon(g, item, rcAbsolute, env);
////
////        if (env.getStateValues().isFocusedItemGUID(guid)) {//draw focused symble
////
////            Drawable drawable = env.getSettings().getItemFocusImage();
////            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////            drawable.draw(g);
////            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
////
////        }
////        //bump item in normal station
////        if (item.getLocalBumped())
////        {
////
////            Drawable drawable = env.getSettings().getItemBumpedImage();
////            int nsize = rcAbsolute.height();
////            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + nsize-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////            drawable.draw(g);
////            rcAbsolute.left += nsize+IMAGE_GAP;// rcAbsolute.height();
////
////        }
////        //remote bumped in other stations, this is for expeditor
////        if (!item.getBumpedStationsString().isEmpty())
////        {
////
////            Drawable drawable = env.getSettings().getItemBumpedInOthersImage();
////            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////            drawable.draw(g);
////            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
////
////        }
////
////        //xml command removed this item
////        if (item.getDeleteByRemoteCommand())
////        {
////            Drawable drawable = env.getSettings().getItemVoidByXmlCommandImage();
////            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////            drawable.draw(g);
////            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
////
////        }
////        if (item.getChangedQty() !=0)
////        {
////            Drawable drawable = env.getSettings().getItemChangedImage();
////            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
////
////            drawable.draw(g);
////            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
////        }
//
//
//        //reset font bg.
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (item.isDimColor())
//        {
//            this.getFont().setFG(DIM_FG);
//        }
//        this.getFont().setBG(nbg);
//        if (item.getAlign() == Paint.Align.RIGHT)
//            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.RIGHT);
//        else
//            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//        return true;
//    }

    static protected KDSBGFG getStateColor(KDSDataItem item,KDSViewSettings env, int defaultBG, int defaultFG)
    {
        int nbg = defaultBG;// this.getFont().getBG();
        //if the bg is same as panel color, transparent it.
//        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//
//        if (nbg == panelBG )
//            nbg = Color.TRANSPARENT;

        String guid = item.getGUID();
        KDSBGFG color = new KDSBGFG(defaultBG, defaultFG);//this.getFont().getBG(), this.getFont().getFG());
        color.setBG(nbg);

        if (item.getChangedQty() !=0)
        {
            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_qty_changed);
            ItemMark itemMark = ItemMark.parseString(s);
            if (itemMark.getFormat() == ItemMark.MarkFormat.Color)
            {
                color.setBG(itemMark.getMarkColor().getBG());
                color.setFG(itemMark.getMarkColor().getFG());
            }
        }

        //xml command removed this item
        if (item.getDeleteByRemoteCommand())
        {
            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_del_by_xml);
            ItemMark itemMark = ItemMark.parseString(s);
            if (itemMark.getFormat() == ItemMark.MarkFormat.Color)
            {
                color.setBG(itemMark.getMarkColor().getBG());
                color.setFG(itemMark.getMarkColor().getFG());
            }

        }

        //remote bumped in other stations, this is for expeditor
        if (!item.getBumpedStationsString().isEmpty())
        {

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_station_bumped);
            ItemMark itemMark = ItemMark.parseString(s);
            if (itemMark.getFormat() == ItemMark.MarkFormat.Color)
            {
                color.setBG(itemMark.getMarkColor().getBG());
                color.setFG(itemMark.getMarkColor().getFG());
            }

        }

        //bump item in normal station
        if (item.getLocalBumped())
        {

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_local_bumped);
            ItemMark itemMark = ItemMark.parseString(s);
            if (itemMark.getFormat() == ItemMark.MarkFormat.Color)
            {
                color.setBG(itemMark.getMarkColor().getBG());
                color.setFG(itemMark.getMarkColor().getFG());
            }

        }

        if (env.getSettings().getBoolean(KDSSettings.ID.Printer_item_bumped)) {
            if (item.getPrinted()) {

                String s = env.getSettings().getString(KDSSettings.ID.Item_mark_printed);
                ItemMark itemMark = ItemMark.parseString(s);
                if (itemMark.getFormat() == ItemMark.MarkFormat.Color) {
                    color.setBG(itemMark.getMarkColor().getBG());
                    color.setFG(itemMark.getMarkColor().getFG());
                }
            }
        }

        if (env.getStateValues().isFocusedItemGUID(guid)) {//draw focused symble

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_focused);
            ItemMark itemMark = ItemMark.parseString(s);
            if (itemMark.getFormat() == ItemMark.MarkFormat.Color)
            {
                color.setBG(itemMark.getMarkColor().getBG());
                color.setFG(itemMark.getMarkColor().getFG());
            }


        }



        if (item.isDimColor())
        {
            color.setBG( KDSConst.DIM_BG );
            color.setFG(KDSConst.DIM_FG);
        }


        return color;
    }

    static private Rect drawItemMark(Canvas g,KDSDataItem item, Rect rcAbsolute,KDSViewSettings env, ItemMark itemMark, KDSBGFG color, int nSize, KDSViewFontFace ff)
    {


        switch (itemMark.getFormat())
        {

            case Icon:
                Drawable drawable = ItemMark.getIconDrawable(itemMark.getInternalIcon(), env);
                int nsize = nSize;// rcAbsolute.height();
//                if (item.m_tempShowMeNeedBlockLines.size() >0)
//                    nsize /= item.m_tempShowMeNeedBlockLines.size();
                //drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
                drawable.setBounds(rcAbsolute.left+KDSConst.IMAGE_GAP, rcAbsolute.top+KDSConst.IMAGE_GAP, rcAbsolute.left + nsize-KDSConst.IMAGE_GAP,rcAbsolute.top + nsize-KDSConst.IMAGE_GAP);//, rcAbsolute.height());
                drawable.draw(g);
                rcAbsolute.left += nsize+KDSConst.IMAGE_GAP;
                break;
            case Char:
                String mark =  itemMark.getMarkString()+ " ";
                int noldbg = ff.getBG();
                int noldfg = ff.getFG();
                ff.setBG(color.getBG());
                ff.setFG(color.getFG());
                int nwidth = CanvasDC.drawText(g, ff, rcAbsolute,mark);
                ff.setBG(noldbg);
                ff.setFG(noldfg);
                rcAbsolute.left += nwidth;

                break;
            case Color:
                break;
        }
        return rcAbsolute;
    }
    /**
     *
     * @param g
     * @param item
     * @param rcAbsolute
     * @param env
     * @return
     *  The rect for draw description
     */
    static protected Rect drawItemState(Canvas g,KDSDataItem item, Rect rcAbsolute,KDSViewSettings env, KDSBGFG color, int nSize, KDSViewFontFace ff)
    {

//        KDSBGFG color = getStateColor(item, env);
//        stateReturnColor.setBG(color.getBG());
//        stateReturnColor.setFG(color.getFG());

        String guid = item.getGUID();

        if (env.getStateValues().isFocusedItemGUID(guid)) {//draw focused symble

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_focused);
            ItemMark itemMark = ItemMark.parseString(s);
            itemMark.setMarkType(ItemMark.MarkType.Focused);
            rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color,nSize, ff);



        }
        //bump item in normal station
        if (item.getLocalBumped())
        {

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_local_bumped);
            ItemMark itemMark = ItemMark.parseString(s);
            itemMark.setMarkType(ItemMark.MarkType.Local_bumped);
            rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color,nSize, ff);


        }
        //remote bumped in other stations, this is for expeditor
        if (!item.getBumpedStationsString().isEmpty())
        {

            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_station_bumped);
            ItemMark itemMark = ItemMark.parseString(s);
            itemMark.setMarkType(ItemMark.MarkType.Station_bumped_for_expo);
            //2.0.14
            if (env.getSettings().isExpeditorStation() ||
                    env.getSettings().isRunnerStation()
                    ) {

                ArrayList<String> arExpo = KDSGlobalVariables.getKDS().getStationsConnections().getRelations().getAllExpoStations();
                if (!item.isAllStationBumpedInExp(arExpo)) {
                    s = env.getSettings().getString(KDSSettings.ID.Item_mark_expo_partial_bumped);
                    itemMark = ItemMark.parseString(s);
                    itemMark.setMarkType(ItemMark.MarkType.Partial_bumped_in_expo);
                }
            }

            rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color,nSize, ff);

//            Drawable drawable = env.getSettings().getItemBumpedInOthersImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;

        }

        //xml command removed this item
        if (item.getDeleteByRemoteCommand() ||
                item.getShowingQty() == 0) //2.0.46, I can only get void item to show the change item icon.My question is how can I get the void icon to show X which is setup in setting?
        {
            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_del_by_xml);
            ItemMark itemMark = ItemMark.parseString(s);
            itemMark.setMarkType(ItemMark.MarkType.Delete_by_xml);
            rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color,nSize, ff);

//            Drawable drawable = env.getSettings().getItemVoidByXmlCommandImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;

        }
        if (item.getChangedQty() !=0 &&
                item.getShowingQty() !=0) //2.0.46,  I can only get void item to show the change item icon.My question is how can I get the void icon to show X which is setup in setting?
        {
            String s = env.getSettings().getString(KDSSettings.ID.Item_mark_qty_changed);
            ItemMark itemMark = ItemMark.parseString(s);
            itemMark.setMarkType(ItemMark.MarkType.Qty_changed);
            rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color,nSize, ff);

//            Drawable drawable = env.getSettings().getItemChangedImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
        }

        if (item.getPrinted())
        {
            if ( env.getSettings().getBoolean(KDSSettings.ID.Printer_item_bumped)) {
                String s = env.getSettings().getString(KDSSettings.ID.Item_mark_printed);
                ItemMark itemMark = ItemMark.parseString(s);
                itemMark.setMarkType(ItemMark.MarkType.Printed);
                rcAbsolute = drawItemMark(g, item, rcAbsolute, env, itemMark, color, nSize, ff);
            }

        }
        return rcAbsolute;
    }


//    /**
//     *
//     * @param item
//     * @param rcAbsolute
//     * @return
//     *  the new rect for next drawing.
//     */
//    protected Rect drawItemStateIcon(Canvas g,KDSDataItem item, Rect rcAbsolute,KDSViewSettings env)
//    {
//        String guid = item.getGUID();
//        if (env.getStateValues().isFocusedItemGUID(guid)) {//draw focused symble
//
//            Drawable drawable = env.getSettings().getItemFocusImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
//
//        }
//        //bump item in normal station
//        if (item.getLocalBumped())
//        {
//
//            Drawable drawable = env.getSettings().getItemBumpedImage();
//            int nsize = rcAbsolute.height();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + nsize-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += nsize+IMAGE_GAP;// rcAbsolute.height();
//
//        }
//        //remote bumped in other stations, this is for expeditor
//        if (!item.getBumpedStationsString().isEmpty())
//        {
//            Drawable drawable = env.getSettings().getItemBumpedInOthersImage();
//
//            //2.0.14
//            if (env.getSettings().isExpeditorStation()) {
//
//                ArrayList<String> arExpo = KDSGlobalVariables.getKDS().getStationsConnections().getRelations().getAllExpoStations();
//                if (!item.isAllStationBumpedInExp(arExpo))
//                    drawable = env.getSettings().getExpoItemPartialBumpedImage();
//            }
//
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
//
//        }
//
//        //xml command removed this item
//        if (item.getDeleteByRemoteCommand())
//        {
//            Drawable drawable = env.getSettings().getItemVoidByXmlCommandImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
//
//        }
//        if (item.getChangedQty() !=0)
//        {
//            Drawable drawable = env.getSettings().getItemChangedImage();
//            drawable.setBounds(rcAbsolute.left+IMAGE_GAP, rcAbsolute.top+IMAGE_GAP, rcAbsolute.left + rcAbsolute.height()-IMAGE_GAP, rcAbsolute.bottom-IMAGE_GAP);//, rcAbsolute.height());
//
//            drawable.draw(g);
//            rcAbsolute.left += rcAbsolute.height()+IMAGE_GAP;
//        }
//        return rcAbsolute;
//    }

//    protected boolean drawDataCondiment(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataCondiment c =(KDSDataCondiment) this.getData();
//
//        int bg = c.getBG();
//        int fg = c.getFG();
//        if (!c.isAssignedColor())
//        {
//            bg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getBG();
//            fg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getFG();
//        }
//
//        //if the bg is same as panel color, transparent it.
//        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//        //if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )
//        if (bg == panelBG )
//            bg = Color.TRANSPARENT;
//
//        if (c.isDimColor())
//        {
//            bg = DIM_BG;
//            fg = DIM_FG;
//        }
//        //draw background
//        CanvasDC.fillRect(g, bg, rcAbsolute);
//
//        String strDescription = c.getDescription();
//
//
//
//        int nStarting = env.getSettings().getInt(KDSSettings.ID.Condiment_Starting_Position);
//        if (nStarting <0)
//            nStarting = KDSSettings.COMDIMENT_LEADING_POSITION;
//        String s =getSpaces(nStarting);
//        s += strDescription;
//        if (c.getFocusTag() != null) { //for hide item, show focus
//            Object obj = c.getFocusTag();
//            if (obj instanceof KDSDataItem) {
//                KDSDataItem item = (KDSDataItem) obj;
//                s = buildItemStateString(item, s, env);
//
//
//            }
//        }
//        //reset font bg.
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (c.isDimColor()) this.getFont().setFG(DIM_FG);
//
//        this.getFont().setBG(bg);
//        CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//        return true;
//    }
//
//    protected boolean drawDataCondimentWithIcon(Canvas g,Rect rcAbsolute,KDSViewSettings env)
//    {
//        KDSDataCondiment c =(KDSDataCondiment) this.getData();
//
//        int bg = c.getBG();
//        int fg = c.getFG();
//        if (!c.isAssignedColor())
//        {
//            bg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getBG();
//            fg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getFG();
//        }
//
//        //if the bg is same as panel color, transparent it.
//        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
//        //if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )
//        if (bg == panelBG )
//            bg = Color.TRANSPARENT;
//
//        if (c.isDimColor())
//        {
//            bg = DIM_BG;
//            fg = DIM_FG;
//        }
//        //draw background
//        CanvasDC.fillRect(g, bg, rcAbsolute);
//
//        String strDescription = c.getDescription();
//        if (c.getFocusTag() != null) { //for hide item, show focus
//            Object obj = c.getFocusTag();
//            if (obj instanceof KDSDataItem) {
//                KDSDataItem item = (KDSDataItem) obj;
//                rcAbsolute = drawItemStateIcon(g, item, rcAbsolute, env);
//
//            }
//        }
//
//
//        int nStarting = env.getSettings().getInt(KDSSettings.ID.Condiment_Starting_Position);
//        if (nStarting <0)
//            nStarting = KDSSettings.COMDIMENT_LEADING_POSITION;
//        String s =getSpaces(nStarting);
//        s += strDescription;
//        //reset font bg.
//        int noldbg = this.getFont().getBG();
//        int noldfg = this.getFont().getFG();
//        if (c.isDimColor()) this.getFont().setFG(DIM_FG);
//
//        this.getFont().setBG(bg);
//        CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
//        this.getFont().setBG(noldbg);
//        this.getFont().setFG(noldfg);
//        return true;
//    }

    static public String getItemPrefix()
    {
        return getSpaces(1);
    }
    static public String getItemQtySuffix()
    {
        return getSpaces(1);
    }

//    static public int getItemQtyPixelsWidth(KDSViewFontFace ff,  int nQty)
//    {
//        String strQty = getItemPrefix();// getSpaces(2);
//        strQty += Integer.toString((int) nQty);
//        strQty += getItemQtySuffix() ;
//        return CanvasDC.getTextPixelsWidth(ff, "" + strQty +"x");
//    }
    static public int getItemQtyPixelsWidth(KDSViewFontFace ff,  float fltQty, KDSViewSettings env)
    {
        String strQty = getItemPrefix();// getSpaces(2);
        strQty += makeQtyString( fltQty ,
                                env.getSettings().getBoolean(KDSSettings.ID.Qty_as_fraction),
                                env.getSettings().getInt(KDSSettings.ID.Qty_precision));

        strQty += getItemQtySuffix() ;
        return CanvasDC.getTextPixelsWidth(ff, "" + strQty +"x");
    }

    static public int getCondimentPrefixPixelsWidth(KDSViewFontFace ff, KDSViewSettings env)
    {
        String str = getCondimentPrefix(env);// getSpaces(2);

        return CanvasDC.getTextPixelsWidth(ff, "" + str +"|");
    }

    static public int getMessagePrefixPixelsWidth(KDSViewFontFace ff)
    {
        String str = getMessagePrefix();

        return CanvasDC.getTextPixelsWidth(ff, "" + str +"|");
    }

    private  boolean isFirstBlockColDataRow(KDSViewBlock block,int nBlockCol)
    {
        int nIndex = block.getCellIndex(this);
        int nMin = nBlockCol * block.getColTotalRows();
        for (int i=nIndex-1; i>=nMin; i--)
        {
            Object obj =  block.getCells().get(i).getData();
            if (obj instanceof KDSDataOrder)
                return true;
            else
                return false;
        }
        return true;
    }
    /**
     * rev.:
     *      kp-99, if the text wrap enabled, the qty can not align with the text.
     *              I draw the qty and state icon after description drawing.
     * @param g
     * @param rcAbsolute
     *  My cell rect, include the combined cells
     * @param env
     * @param nColInBlock
     * @param block
     *  This cell in this block
     * @return
     */
    protected boolean drawDataItem(Canvas g,Rect rcAbsolute,KDSViewSettings env, int nColInBlock, KDSViewBlock block)
    {
        if (m_nTextWrapRowIndex != 0 && (!isFirstBlockColDataRow(block, nColInBlock))) return true;
        //if (m_prevCell != null) return true; //I am combined to prev

        KDSDataItem item =(KDSDataItem) this.getData();

        KDSBGFG color = getStateColor(item, env, this.getFont().getBG(), this.getFont().getFG());
        int nbg = color.getBG();//getOriginalBG();
//        if ( nbg == this.getFont().getBG())
//            nbg = Color.TRANSPARENT;
//        else
//            nbg = color.getBG();
        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
        if ( nbg == panelBG)
            nbg = Color.TRANSPARENT;

        CanvasDC.fillRect(g, nbg, rcAbsolute);
        //CanvasDC.fillRect(g, color.getBG(), rcAbsolute);
        Rect rcState = new Rect(rcAbsolute);//we need to draw again if text wrap enabled.
        rcAbsolute = drawItemState(g,item, rcAbsolute, env, color, block.getCalculatedAverageRowHeight(), this.getFont());
        rcState.right = rcAbsolute.left; //save it.
        //reset font bg.
        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();

        this.getFont().setFG(color.getFG());
        //this.getFont().setBG(color.getBG());
        this.getFont().setBG(nbg);

        Paint.Align align = Paint.Align.LEFT;
        if (item.getAlign() == Paint.Align.RIGHT)
            align = Paint.Align.RIGHT;

        //draw qty first
        float qty = item.getShowingQty();

        String strQty = getItemPrefix();// getSpaces(2);
        //strQty += Integer.toString((int) qty);
        strQty += makeQtyString(qty, env.getSettings().getBoolean(KDSSettings.ID.Qty_as_fraction), env.getSettings().getInt(KDSSettings.ID.Qty_precision));

        strQty += "x";
        strQty += getItemQtySuffix() ;

        Rect rcQty = new Rect(rcAbsolute);

        if (item.getAlign() == Paint.Align.RIGHT)
        {
            //Rect rc = new Rect(rcAbsolute);

            rcQty.left = rcQty.right - getItemQtyPixelsWidth(getFont(), qty, env);// CanvasDC.getTextPixelsWidth(this.getFont(), strQty);
            //if (m_nTextWrapRowIndex ==0)//move to below
            //    CanvasDC.drawWrapString(g, this.getFont(), rcQty, strQty, align, true);
            rcAbsolute.right = rcQty.left;
        }
        else {
            //Rect rc = new Rect(rcAbsolute);
            Paint paint = new Paint();

            paint.setTypeface(getFont().getTypeFace());
            paint.setTextSize(getFont().getFontSize());
            rcQty.right = rcQty.left + getItemQtyPixelsWidth(getFont(),qty, env) ;//  CanvasDC.getTextPixelsWidth(paint, "!" + strQty + "!");
            //Bug here! KP-99
            if (item.m_tempShowMeNeedBlockLines.size() >0) {
                if (!isLastBlockColDataRow(block, nColInBlock))//kp-99, add this condition.
                    rcQty.bottom = rcQty.top + rcQty.height() / item.m_tempShowMeNeedBlockLines.size();
            }
            //if (m_nTextWrapRowIndex ==0)//move to below.
            //    CanvasDC.drawText(g, this.getFont(), rcQty, strQty, align, true);
           // rc.right = rc.left + CanvasDC.getTextPixelsWidth(paint, strQty);
            rcAbsolute.left = rcQty.right;

        }

        //draw description
        String strDescription = item.getDescription();

        if (env.getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            int nTextWrapRows = getTextWrapRowsInSameBlockCol(block, nColInBlock);
            String strTextWrap = getTextWrapRowsDescription(item, strDescription,this.m_nTextWrapRowIndex, nTextWrapRows);
            //kp-99, as the text was wrapped, I have to move qty and icon to correct position.
            //          draw them here, after description finished.
            Point ptReturn = CanvasDC.drawWrapString(g, this.getFont(), rcAbsolute, strTextWrap, align, true);
            //ptReturn: x = font size in description drawing. y: the description start point.
            if (m_nTextWrapRowIndex ==0) { //draw qty
                int fontSize = ptReturn.x;
                int y = ptReturn.y;
                //int h = rcAbsolute.height();
                rcQty.top = y;
                rcQty.bottom =rcAbsolute.bottom;// rcQty.top + h;
                int size = this.getFont().getFontSize();
                this.getFont().setFontSize(fontSize);
                CanvasDC.drawQtyWrapText(g, this.getFont(), rcQty, strQty, align, true);
                this.getFont().setFontSize(size);
            }
            //draw state icon again after change position.
            CanvasDC.fillRect(g, nbg, rcState);//clear old image.
            //if (!isLastBlockColDataRow(block, nColInBlock) && //it is not la
            if ( rcState.height() != block.getCalculatedAverageRowHeight())//it is not single row.
                rcState.top = ptReturn.y-KDSConst.IMAGE_GAP;
            rcState.bottom = rcState.top + block.getCalculatedAverageRowHeight();//limit the icon size to one row.
            drawItemState(g,item, rcState, env, color, block.getCalculatedAverageRowHeight(), this.getFont());
        }
        else {
            CanvasDC.drawText(g, this.getFont(), rcAbsolute, strDescription, align, true);
            CanvasDC.drawText(g, this.getFont(), rcQty, strQty, align, true);
        }
        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);


        return true;
    }

    /**
     *
     * @param g
     * @param rcAbsolute
     * @param env
     * @param nColInBlock
     * @param block
     * @return
     */
    protected boolean drawDataItem2_old(Canvas g,Rect rcAbsolute,KDSViewSettings env, int nColInBlock, KDSViewBlock block)
    {
        if (m_nTextWrapRowIndex != 0 && (!isFirstBlockColDataRow(block, nColInBlock))) return true;
        //if (m_prevCell != null) return true; //I am combined to prev

        KDSDataItem item =(KDSDataItem) this.getData();

        KDSBGFG color = getStateColor(item, env, this.getFont().getBG(), this.getFont().getFG());
        int nbg = color.getBG();//getOriginalBG();
//        if ( nbg == this.getFont().getBG())
//            nbg = Color.TRANSPARENT;
//        else
//            nbg = color.getBG();
        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
        if ( nbg == panelBG)
            nbg = Color.TRANSPARENT;

        CanvasDC.fillRect(g, nbg, rcAbsolute);
        //CanvasDC.fillRect(g, color.getBG(), rcAbsolute);

        rcAbsolute = drawItemState(g,item, rcAbsolute, env, color, block.getCalculatedAverageRowHeight(), this.getFont());
        //reset font bg.
        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();

        this.getFont().setFG(color.getFG());
        //this.getFont().setBG(color.getBG());
        this.getFont().setBG(nbg);

        Paint.Align align = Paint.Align.LEFT;
        if (item.getAlign() == Paint.Align.RIGHT)
            align = Paint.Align.RIGHT;

        //draw qty first
        float qty = item.getShowingQty();

        String strQty = getItemPrefix();// getSpaces(2);
        //strQty += Integer.toString((int) qty);
        strQty += makeQtyString(qty, env.getSettings().getBoolean(KDSSettings.ID.Qty_as_fraction), env.getSettings().getInt(KDSSettings.ID.Qty_precision));

        strQty += "x";
        strQty += getItemQtySuffix() ;
        if (item.getAlign() == Paint.Align.RIGHT)
        {
            Rect rc = new Rect(rcAbsolute);

            rc.left = rc.right - getItemQtyPixelsWidth(getFont(), qty, env);// CanvasDC.getTextPixelsWidth(this.getFont(), strQty);
            if (m_nTextWrapRowIndex ==0)
                CanvasDC.drawWrapString(g, this.getFont(), rc, strQty, align, true);
            rcAbsolute.right = rc.left;
//            //s += strDescription;
//
//            s += Integer.toString((int) qty);
//            s += getItemQtySuffix();

        }
        else {
//            s += Integer.toString((int) qty);
//            s += getItemQtySuffix();
//            //s += strDescription;
            Rect rc = new Rect(rcAbsolute);
            Paint paint = new Paint();

            paint.setTypeface(getFont().getTypeFace());
            paint.setTextSize(getFont().getFontSize());
            rc.right = rc.left + getItemQtyPixelsWidth(getFont(),qty, env) ;//  CanvasDC.getTextPixelsWidth(paint, "!" + strQty + "!");
            //Bug here! KP-99
            if (item.m_tempShowMeNeedBlockLines.size() >0) {
                if (!isLastBlockColDataRow(block, nColInBlock))//kp-99, add this condition.
                    rc.bottom = rc.top + rc.height() / item.m_tempShowMeNeedBlockLines.size();
            }
            if (m_nTextWrapRowIndex ==0)
                CanvasDC.drawText(g, this.getFont(), rc, strQty, align, true);
            // rc.right = rc.left + CanvasDC.getTextPixelsWidth(paint, strQty);
            rcAbsolute.left = rc.right;

        }

        //draw description
        String strDescription = item.getDescription();

        if (env.getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            int nTextWrapRows = getTextWrapRowsInSameBlockCol(block, nColInBlock);
            String strTextWrap = getTextWrapRowsDescription(item, strDescription,this.m_nTextWrapRowIndex, nTextWrapRows);
            CanvasDC.drawWrapString(g, this.getFont(), rcAbsolute, strTextWrap, align, true);
        }
        else
            CanvasDC.drawText(g, this.getFont(), rcAbsolute, strDescription, align, true);
//        if (item.getAlign() == Paint.Align.RIGHT)
//            //CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.RIGHT);
//            CanvasDC.drawWrapString(g, this.getFont(), rcAbsolute, s, Paint.Align.RIGHT);
//        else
//            CanvasDC.drawWrapString(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
//            //CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);
        // Apr'21 Premium Facelift: Removed Line between items
//        if (!block.isFirstItemDataRow(this))
//            drawItemLine(g, rcAbsolute, color);
//        Paint paint = new Paint();
//        paint.setColor(Color.LTGRAY);
//        paint.setStrokeWidth(0);
//        LinearGradient backGradient = new LinearGradient(0, 0, rcAbsolute.width(), 1, new int[]{color.getBG(), this.getFont().getFG() ,color.getBG()}, null, Shader.TileMode.CLAMP);
//        paint.setShader(backGradient);
//
//        int nInset = rcAbsolute.width()/2;
//        //g.drawLine(rcAbsolute.left+ KDSIOSView.INSET_DX, rcAbsolute.top, rcAbsolute.right-nInset, rcAbsolute.top,paint );
//        g.drawLine(rcAbsolute.left+ KDSIOSView.INSET_DX, rcAbsolute.top, rcAbsolute.right-KDSIOSView.INSET_DX, rcAbsolute.top,paint );

        return true;
    }


    private void drawItemLine(Canvas g, Rect rcAbsolute, KDSBGFG color)
    {
        Paint paint = new Paint();
        //paint.setColor(Color.LTGRAY);
        paint.setStrokeWidth(0);
        LinearGradient backGradient = new LinearGradient(rcAbsolute.left+ KDSIOSView.INSET_DX, rcAbsolute.top, rcAbsolute.right-KDSIOSView.INSET_DX, rcAbsolute.top, new int[]{color.getBG(), this.getFont().getFG() ,color.getBG()}, null, Shader.TileMode.CLAMP);
        paint.setShader(backGradient);

        //int nInset = rcAbsolute.width()/2;
        //g.drawLine(rcAbsolute.left+ KDSIOSView.INSET_DX, rcAbsolute.top, rcAbsolute.right-nInset, rcAbsolute.top,paint );
        g.drawLine(rcAbsolute.left+ KDSIOSView.INSET_DX, rcAbsolute.top, rcAbsolute.right-KDSIOSView.INSET_DX, rcAbsolute.top,paint );

    }

    private String getTextWrapRowsDescription(KDSData data,String strDescription, int nFromRow, int nCount)
    {
        if (data.m_tempShowMeNeedBlockLines.size() <= nFromRow)
            return strDescription;
        Point ptFrom = data.m_tempShowMeNeedBlockLines.get(nFromRow);

        int nIndex = nFromRow + nCount -1;
        if (data.m_tempShowMeNeedBlockLines.size() <= nIndex)
            return strDescription;

        Point ptTo = data.m_tempShowMeNeedBlockLines.get(nFromRow + nCount -1);

        int nFromIndex = ptFrom.x;
        int nToIndex = ptTo.y;
        String s = strDescription;//data.getDescription();
        return s.substring(nFromIndex, nToIndex);
    }

    private int getTextWrapRowsInSameBlockCol(KDSViewBlock block, int nBlockCol)
    {
        int ncounter = 0;
        KDSViewBlockCell c = this;
        int nIndex = block.getCellIndex(c);
        int nMax = (nBlockCol+1)* block.getColTotalRows();

        for (int i=nIndex; i< nMax ;i++)
        {
            c = block.getCell(i);
            if (c == null) break;
            if (c.getData() == this.getData())
            {
                ncounter ++;
            }
            else
                break;
        }
        return ncounter;
    }

    static public String getCondimentPrefix(KDSViewSettings env)
    {
        int nStarting = env.getSettings().getInt(KDSSettings.ID.Condiment_Starting_Position);
        if (nStarting <0)
            nStarting = KDSSettings.COMDIMENT_LEADING_POSITION;
        String s =getSpaces(nStarting);
        return s;
    }

    /**
     * Draw condiments
     * Use m_attachedObj to link item.
     *
     * @param g
     * @param rcAbsolute
     * @param env
     * @param block
     * @param nColInBlock
     * @return
     */
    protected boolean drawDataCondiment(Canvas g,Rect rcAbsolute,KDSViewSettings env,KDSViewBlock block, int nColInBlock)
    {
        if (m_nTextWrapRowIndex != 0 && (!isFirstBlockColDataRow(block, nColInBlock))) return true;

        KDSDataCondiment c =(KDSDataCondiment) this.getData();

        int bg = c.getBG();
        int fg = c.getFG();
        if (!c.isAssignedColor())
        {
            bg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getBG();
            fg = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace).getFG();
        }

        //if the bg is same as panel color, transparent it.
        int panelBG = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
        //if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )


        if (c.isDimColor())
        {
            bg = KDSConst.DIM_BG;
            fg = KDSConst.DIM_FG;
        }
        //change expo color
        if (m_attachedObj != null) {
            if (m_attachedObj instanceof  KDSDataItem) {
                KDSDataItem item = (KDSDataItem)m_attachedObj;
                if (!item.getBumpedStationsString().isEmpty()) {
                    KDSBGFG color =getStateColor(item, env, bg, fg);
                    bg = color.getBG();
                    fg = color.getFG();
                }
            }
        }
        if (bg == panelBG )
            bg = Color.TRANSPARENT;


        //draw background
        CanvasDC.fillRect(g, bg, rcAbsolute);

        String strDescription = c.getDescription();



//        int nStarting = env.getSettings().getInt(KDSSettings.ID.Condiment_Starting_Position);
//        if (nStarting <0)
//            nStarting = KDSSettings.COMDIMENT_LEADING_POSITION;
        String qtyText = getCondimentPrefix(env);
        //String s = getCondimentPrefix(env);//getSpaces(nStarting);
        //kpp1-414
        if (c.getQty() >1)
        {
            qtyText += Integer.toString((int) c.getQty());
            //s += makeQtyString(c.getQty());// Integer.toString((int) c.getQty()); //kp-88
            qtyText += "x ";
        }
        String s = qtyText + strDescription;

        if (c.getFocusTag() != null) { //for hide item, show focus
            Object obj = c.getFocusTag();
            if (obj instanceof KDSDataItem) {
                KDSDataItem item = (KDSDataItem) obj;
                KDSBGFG color = getStateColor(item, env, this.getFont().getBG(), this.getFont().getFG());
                CanvasDC.fillRect(g, color.getBG(), rcAbsolute);
                rcAbsolute = drawItemState(g,item, rcAbsolute, env, color,block.getCalculatedAverageRowHeight(), this.getFont());
                bg = color.getBG();

                //s = buildItemStateString(item, s, env);


            }
        }
        //reset font bg.
        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();
        if (c.isDimColor()) this.getFont().setFG(KDSConst.DIM_FG);

        this.getFont().setBG(bg);
        this.getFont().setFG(fg); //use this text color
        Rect rtCondiment = new Rect(rcAbsolute);

        if (env.getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            rtCondiment.left += getCondimentPrefixPixelsWidth(getFont(), env);
            int nTextWrapRows = getTextWrapRowsInSameBlockCol(block, nColInBlock);
            String strTextWrap = getTextWrapRowsDescription(c,c.getDescription(), this.m_nTextWrapRowIndex, nTextWrapRows);
            //String strTextWrap = getTextWrapRowsDescription(c,s, this.m_nTextWrapRowIndex, nTextWrapRows);
            Point ptReturn = CanvasDC.drawWrapString(g, this.getFont(), rtCondiment, strTextWrap, Paint.Align.LEFT, false);
            if (c.getQty()>1)
            {
                if (this.m_nTextWrapRowIndex ==0) {
                    int oldFontSize = this.getFont().getFontSize();
                    this.getFont().setFontSize(ptReturn.x);
//                    String strQty = Integer.toString((int) c.getQty());
//                    strQty += "x ";
                    qtyText = qtyText.trim();
                    qtyText += " ";
                    int w = CanvasDC.getTextPixelsWidth(this.getFont(), "" + qtyText + "|");
                    Rect rcQty = new Rect(rtCondiment);
                    rcQty.top = ptReturn.y;
                    rcQty.left -= w;
                    rcQty.left = rcQty.left<rcAbsolute.left?rcAbsolute.left:rcQty.left;
                    rcQty.right = rtCondiment.left;
                    rcQty.bottom = rcQty.top +  block.getCalculatedAverageRowHeight();
                    rcQty.bottom = rcQty.bottom >rcAbsolute.bottom?rcAbsolute.bottom:rcQty.bottom;

                    CanvasDC.drawQtyWrapText(g, this.getFont(), rcQty, qtyText, Paint.Align.LEFT, false);
                    this.getFont().setFontSize(oldFontSize);
                }
            }
        }
        else
            CanvasDC.drawText(g, this.getFont(), rtCondiment, s, Paint.Align.LEFT);


        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);
        return true;
    }

    static public String getMessagePrefix()
    {
        return getSpaces(7);
    }
    protected boolean drawDataMessage(Canvas g,Rect rcAbsolute,KDSViewSettings env, KDSViewBlock block, int nColInBlock)
    {
        if (m_nTextWrapRowIndex != 0 && (!isFirstBlockColDataRow(block, nColInBlock))) return true;
        KDSDataMessage c =(KDSDataMessage) this.getData();

        int bg = this.getFont().getBG();
        //if the bg is same as panel color, transparent it.
        int panelBg = env.getSettings().getInt(KDSSettings.ID.Panels_BG);
        // if (bg == env.getSettings().getKDSViewFontFace(KDSSettings.ID.Panels_Default_FontFace).getBG() )


        if (c.isDimColor())
            bg = KDSConst.DIM_BG;

        //change expo color
        if (m_attachedObj != null) {
            if (m_attachedObj instanceof  KDSDataItem) {
                KDSDataItem item = (KDSDataItem)m_attachedObj;
                if (!item.getBumpedStationsString().isEmpty()) {
                    KDSBGFG color =getStateColor(item, env, bg, 0);
                    bg = color.getBG();
                    //fg = color.getFG();
                }
            }
        }
        if (bg == panelBg )
            bg = Color.TRANSPARENT;

        CanvasDC.fillRect(g, bg, rcAbsolute);

        String strDescription = c.getMessage();



        if (c.getFocusTag() != null) { //for hide item, show focus
            Object obj = c.getFocusTag();
            if (obj instanceof KDSDataItem) {
                KDSDataItem item = (KDSDataItem) obj;
                KDSBGFG color = getStateColor(item, env, this.getFont().getBG(), this.getFont().getFG());
                CanvasDC.fillRect(g, color.getBG(), rcAbsolute);
                rcAbsolute = drawItemState(g,item, rcAbsolute, env, color, block.getCalculatedAverageRowHeight(), this.getFont());
                bg = color.getBG();

            }
        }


        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();
        if (c.isDimColor())
        {
            this.getFont().setFG(KDSConst.DIM_FG);
        }
        this.getFont().setBG(bg);

        //CanvasDC.drawText(g, this.getFont(), rcAbsolute,s,  Paint.Align.LEFT);

        if (env.getSettings().getBoolean(KDSSettings.ID.Text_wrap)) {
            rcAbsolute.left += getMessagePrefixPixelsWidth(getFont());
            int nTextWrapRows = getTextWrapRowsInSameBlockCol(block, nColInBlock);
            String strTextWrap = getTextWrapRowsDescription(c,c.getMessage(), this.m_nTextWrapRowIndex, nTextWrapRows);
            CanvasDC.drawWrapString(g, this.getFont(), rcAbsolute, strTextWrap, Paint.Align.LEFT);
        }
        else {
            String s = getMessagePrefix();// getSpaces(7);
            s += strDescription;
            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
        }


        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);
        return true;
    }

    protected boolean drawVoidIndicator(Canvas g,Rect rcAbsolute,KDSViewSettings env)
    {
        KDSDataVoidItemIndicator item =(KDSDataVoidItemIndicator) this.getData();

        KDSViewFontFace ff = env.getSettings().getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace);


        CanvasDC.fillRect(g, ff.getBG(), rcAbsolute);


        String strDescription = item.getDescription();
        String s = getSpaces(2);
        strDescription = s + strDescription;

        //reset font bg.
        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();

        this.getFont().setFG(ff.getFG());
        this.getFont().setBG(ff.getBG());


        CanvasDC.drawText(g, this.getFont(), rcAbsolute, strDescription, Paint.Align.LEFT);
        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);

        return true;
    }

    static public String getVoidItemQtyString(KDSViewSettings env, float qty)
    {
        String strQty =  Integer.toString(Math.abs((int) qty));
        if (qty <0)
        {
            int n = env.getSettings().getInt(KDSSettings.ID.Void_qty_line_mark);
            KDSSettings.VoidQtyChangeMark mark = KDSSettings.VoidQtyChangeMark.values()[n];
            switch (mark)
            {

                case Brackets:
                    strQty = "(" + strQty + ")";
                    break;
                case Negative_sign:
                    strQty = "-" + strQty;
                    break;
            }

        }

        return strQty;
    }

    protected boolean drawVoidItemQtyChange(Canvas g,Rect rcAbsolute,KDSViewSettings env)
    {
        KDSDataVoidItemQtyChanged item =(KDSDataVoidItemQtyChanged) this.getData();

        KDSBGFG color = getStateColor(item, env, this.getFont().getBG(), this.getFont().getFG());

        CanvasDC.fillRect(g, color.getBG(), rcAbsolute);

        float qty = item.getChangedQty();
        String strDescription = item.getDescription();

        String s = getSpaces(2);


        String strQty = getVoidItemQtyString(env, qty);//  Integer.toString(Math.abs((int) qty));

//        if (qty <0)
//        {
//            int n = env.getSettings().getInt(KDSSettings.ID.Void_qty_line_mark);
//            KDSSettings.VoidQtyChangeMark mark = KDSSettings.VoidQtyChangeMark.values()[n];
//            switch (mark)
//            {
//
//                case Brackets:
//                    strQty = "(" + strQty + ")";
//                    break;
//                case Negative_sign:
//                    strQty = "-" + strQty;
//                    break;
//            }
//        }
        if (item.getAlign() == Paint.Align.RIGHT)
        {

            s += strDescription;
            s += getSpaces(3);
            s += strQty;//Integer.toString((int) qty);
        }
        else {
            s += strQty;//Integer.toString((int) qty);
            s += getSpaces(3);
            s += strDescription;
        }


        //reset font bg.
        int noldbg = this.getFont().getBG();
        int noldfg = this.getFont().getFG();

        this.getFont().setFG(color.getFG());
        this.getFont().setBG(color.getBG());

        if (item.getAlign() == Paint.Align.RIGHT)
            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.RIGHT);
        else
            CanvasDC.drawText(g, this.getFont(), rcAbsolute, s, Paint.Align.LEFT);
        this.getFont().setBG(noldbg);
        this.getFont().setFG(noldfg);

        return true;
    }

    Object m_attachedObj = null;
    public void setAttachedObject(Object obj)
    {
        m_attachedObj = obj;
    }
    public Object getAttachedObj()
    {
        return m_attachedObj;
    }

    /**
     * kp-88, fraction qty
     * @param qty
     * @return
     */
    static public String makeQtyString(float qty, boolean bEnableFraction, int nPrecision)
    {
        String strQty = "";
        int intValue = KDSUtil.floatIntPart(qty);
        float fltVal = KDSUtil.floatPart(qty);
        if (fltVal == 0 )
            strQty += Integer.toString((int) qty);
        else
        {
            if (intValue>0)
            {//[>1] value, use decimal format.
                return KDSUtil.convertFloatToString(qty, nPrecision);
            }
            else { //only {0, 1} float value need fraction.
                if (bEnableFraction) {
                    if (isFractionFloat(fltVal)) {
                        strQty += makeFractionQty(fltVal);
//                        ArrayList<Integer> arVal = KDSUtil.convertFloatToFraction(fltVal, 3);
//                        if (intValue > 0) {
//                            strQty += Integer.toString((int) intValue);
//                            strQty += "+";
//                        }
//                        strQty += arVal.get(0).toString() + "/" + arVal.get(1).toString();
                    } else {
                        strQty += String.format(Locale.ENGLISH, "%." + KDSUtil.convertIntToString(nPrecision) + "f", qty);
                    }
                } else {
                    strQty +=  KDSUtil.convertFloatToString(qty, nPrecision);
                }
            }


        }
        return strQty;
    }

    static final float ERROR_MARGIN = 0.005f;
    static final float[] VALID_FRACTIONS = new float[]{
            0.5f, //1/2
            0.25f, // 1/4
            0.333f, // 1/3
            0.666f, // 2/3
            0.75f,  // 3/4
            //0.125f, // 1/8
            0.375f, // 3/8
            0.625f, // 5/8
            0.875f // 7/8
    };
    static final String[] VALID_FRACTIONS_STRING = new String[]{
            "1/2",
            "1/4",
            "1/3",
            "2/3",
            "3/4",
            //"1/8",
            "3/8",
            "5/8",
            "7/8"
    };
    /**
     * kp-88,
     *  Just following float shows as fraction.
     * @param flt
     * @return
     */
    static private boolean isFractionFloat(float flt)
    {


        for (int i=0; i< VALID_FRACTIONS.length; i++)
        {
            if (Math.abs(flt - VALID_FRACTIONS[i]) < ERROR_MARGIN)
                return true;
        }
        return false;

    }

    static private String makeFractionQty(float qty)
    {
        for (int i=0; i< VALID_FRACTIONS.length; i++)
        {
            if (Math.abs(qty - VALID_FRACTIONS[i]) < ERROR_MARGIN) {
                return VALID_FRACTIONS_STRING[i];
            }
        }
        return "";
    }

    /**
     * check if thsi cell is the last cell in block.
     * @param block
     * @param nBlockCol
     * @return
     */
    private  boolean isLastBlockColDataRow(KDSViewBlock block,int nBlockCol)
    {
        int nIndex = block.getCellIndex(this);
        int nMin = (nBlockCol+1) * block.getColTotalRows();
        return (nIndex == nMin-1);

    }

    /**
     * KP-171
     * @param startTime
     * @return
     */
    static private String formatTimeDuration(Date startTime)
    {
        Date dtNow = new Date(System.currentTimeMillis());
        long l = dtNow.getTime() - startTime.getTime();
        long day=l/(24*60*60*1000);
        long hour=(l/(60*60*1000)-day*24);
        long min=((l/(60*1000))-day*24*60-hour*60);
        long sec=(l/1000-day*24*60*60-hour*60*60-min*60);
        long hours = hour + day * 24;


        if (hours <=0)
            return String.format("%02d:%02d", min, Math.abs(sec));

        else
            return String.format("%d:%02d:%02d",hours, Math.abs(min), Math.abs(sec));
    }

}
