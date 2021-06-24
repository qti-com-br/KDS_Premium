package com.bematechus.kds;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.ThemeUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/24.
 */
public class ItemMark {

    public enum MarkFormat
    {
        Icon,
        Char,
        Color,
    }

    public enum MarkType
    {
        Null,
        Focused,
        Local_bumped,
        Station_bumped_for_expo,
        Delete_by_xml,
        Qty_changed,
        Partial_bumped_in_expo,//2.0.14
        Printed, //print item when bump
    }



    MarkType m_markType = MarkType.Null;

    private MarkFormat m_format = MarkFormat.Icon;
    MarkType m_internalIcon = MarkType.Null;
    String m_strCharValue = "";
    KDSBGFG m_colorValue = new KDSBGFG();

    public ItemMark()
    {

    }
    public ItemMark(ItemMark m)
    {
        m_markType = m.m_markType;
        m_format = m.getFormat();
        m_internalIcon = m.m_internalIcon;
        m_strCharValue = m.getMarkString();
        m_colorValue.setBG(m.getMarkColor().getBG());
        m_colorValue.setFG(m.getMarkColor().getFG());

    }

    public void setMarkType(MarkType markType)
    {
        m_markType = markType;
    }
    public MarkType getMarkType()
    {
        return m_markType;
    }

    /**
     *
     * @param markType
     *  Use the internalIcon to indentify the mark type
     * @return
     */
    static public int getDefaultIcon(MarkType markType)
    {
        return getIconResID(markType);


    }
    static public String getDefaultString(MarkType markType)
    {
        switch (markType)
        {

            case Null:
                return "";

            case Focused:
                return ">";

            case Local_bumped:
                return "*";

            case Station_bumped_for_expo:
                return "#";
            case Delete_by_xml:
                return "(x)";
            case Qty_changed:
                return "(E)";
            case Partial_bumped_in_expo://2.0.14
                return "@";
            case Printed:
                return "$";
        }
        return "";
    }

    static public KDSBGFG getDefaultColor(MarkType markType)
    {
        switch (markType)
        {

            case Null:
                return new KDSBGFG(0,0);

            case Focused:
                return new KDSBGFG(ThemeUtil.getAttrColor( KDSApplication.getContext(), R.attr.item_focused_bg),
                                    ThemeUtil.getAttrColor(KDSApplication.getContext(), R.attr.item_focused_fg));
                //return new KDSBGFG(KDSApplication.getContext().getResources().getColor(R.color.item_focused_bg),KDSApplication.getContext().getResources().getColor(R.color.item_focused_fg));

            case Local_bumped:
                return new KDSBGFG(Color.LTGRAY ,Color.GRAY);

            case Station_bumped_for_expo:
                return new KDSBGFG(Color.LTGRAY,Color.BLACK);
            case Delete_by_xml:
                return new KDSBGFG(Color.LTGRAY,Color.DKGRAY);
            case Qty_changed:
                return new KDSBGFG(Color.LTGRAY,Color.BLUE);
            case Partial_bumped_in_expo:
                return new KDSBGFG(Color.GRAY,Color.BLACK);
            case Printed:
                return return new KDSBGFG(ThemeUtil.getAttrColor( KDSApplication.getContext(), R.attr.item_focused_bg),
                                            ThemeUtil.getAttrColor(KDSApplication.getContext(), R.attr.item_focused_fg));
        }
        return new KDSBGFG(0,0);
    }

    static public MarkType getMarkType(String prefKey)
    {
        if (prefKey.equals("item_mark_focused"))
            return MarkType.Focused;
        else if (prefKey.equals("item_mark_local_bumped"))
            return MarkType.Local_bumped;
        else if (prefKey.equals("item_mark_station_bumped"))
            return MarkType.Station_bumped_for_expo;
        else if (prefKey.equals("item_mark_del_by_xml"))
            return MarkType.Delete_by_xml;
        else if (prefKey.equals("item_mark_qty_changed"))
            return MarkType.Qty_changed;
        else if (prefKey.equals("item_mark_expo_partial_bumped"))
            return MarkType.Partial_bumped_in_expo;
        else if (prefKey.equals("item_mark_printed")
            return MarkType.Printed;)
        return MarkType.Null;

    }

    static public ItemMark parseString(String str)
    {
        ItemMark itemMark = new ItemMark();
        ArrayList<String> ar = KDSUtil.spliteString(str, SEPARATOR);
        if (ar.size() <=0) return itemMark;

        String s = ar.get(0);
        int n = KDSUtil.convertStringToInt(s, 0);
        MarkFormat format = MarkFormat.values()[n];
        itemMark.setFormat(format);
        switch (format)
        {

            case Icon:
                if (ar.size()>=2)
                    s = ar.get(1);
                else
                    s = "0";
                n = KDSUtil.convertStringToInt(s, 0);
                itemMark.setInternalIcon(MarkType.values()[n]);

                break;
            case Char:
                if (ar.size()>=2)
                    s = ar.get(1);
                else
                    s = "";
                itemMark.setMarkString(s);
                break;
            case Color:
                if (ar.size()>=2)
                    s = ar.get(1);
                else
                    s = "";
                KDSBGFG c = KDSBGFG.parseString(s);
                itemMark.setMarkColor(c);
                break;
        }
        return itemMark;
    }

    static final  String SEPARATOR = "_";
    /**
     * format| icon|char|bg,fg
     * @return
     */
    public String toString()
    {

        String  s = KDSUtil.convertIntToString(m_format.ordinal());

        switch (m_format)
        {

            case Icon:
                s += SEPARATOR;
                s += KDSUtil.convertIntToString(m_internalIcon.ordinal());
                break;
            case Char:
                s += SEPARATOR;
                s += m_strCharValue;
                break;
            case Color:
                s += SEPARATOR;
                s += m_colorValue.toString();
                break;
        }
        return s;
    }

    public String getDescription()
    {
        switch (m_format)
        {

            case Icon:
                return "icon";

            case Char:
                return "Char";

            case Color:
                return "Color";

        }
        return "";
    }

    public MarkFormat getFormat()
    {
        return m_format;
    }
    public void setFormat(MarkFormat format)
    {
        m_format = format;
    }

    static public int getIconResID( MarkType icon)
    {
        switch (icon)
        {

            case Null:
                return -1;

            case Focused:
                return R.drawable.item_focus;

            case Local_bumped:
                return R.drawable.item_bumped;

            case Station_bumped_for_expo:
                return R.drawable.others_bumped;

            case Delete_by_xml:
                return com.bematechus.kdslib.R.drawable.delete_24px_32;

            case Qty_changed:
                return com.bematechus.kdslib.R.drawable.edit_24px_16;
            case Partial_bumped_in_expo:
                return R.drawable.partial_bumped;
            case Printed:
                return R.drawable.ticket_print;
        }
        return -1;
    }

    public MarkType getInternalIcon()
    {
        if (m_internalIcon == MarkType.Null)
            m_internalIcon = m_markType;
        return m_internalIcon;
    }
    public void setInternalIcon(MarkType icon)
    {
        m_internalIcon = icon;
    }

    public String getMarkString()
    {
        if (m_strCharValue.isEmpty())
            m_strCharValue = getDefaultString(m_markType);
        return m_strCharValue;
    }
    public void setMarkString(String str)
    {
        m_strCharValue = str;
    }

    public KDSBGFG getMarkColor()
    {
        if ( m_colorValue.toString().equals("0,0"))
            m_colorValue = getDefaultColor(m_markType);
        return m_colorValue;
    }

    public void setMarkColor(KDSBGFG color)
    {
        m_colorValue = color;
    }

    public void clearValues()
    {
        m_colorValue = new KDSBGFG();
        m_strCharValue = "";
        m_internalIcon = MarkType.Null;

    }

    static public Drawable getIconDrawable(MarkType icon,KDSViewSettings env)
    {
        switch (icon)
        {

            case Null:
                return null;

            case Focused:
                return env.getSettings().getItemFocusImage();
            case Local_bumped:
                return env.getSettings().getItemBumpedImage();

            case Station_bumped_for_expo:
                return env.getSettings().getItemBumpedInOthersImage();

            case Delete_by_xml:
                return env.getSettings().getItemVoidByXmlCommandImage();

            case Qty_changed:
                return env.getSettings().getItemChangedImage();
            case Partial_bumped_in_expo:
                return env.getSettings().getExpoItemPartialBumpedImage();

        }
        return null;
    }
}
