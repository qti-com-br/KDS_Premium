package com.bematechus.kdslib;

import android.graphics.Color;
import android.graphics.Typeface;

/**
 * Created by Administrator on 2015/8/13 0013.
 */
public class KDSViewFontFace {
    //static public final String DEFULT_FONT_FILE =  "/system/fonts/DroidSans.ttf";
    //static public final String DEFULT_FONT_FILE =  "/system/fonts/NanumGothic.ttf";
    //static public final String DEFULT_FONT_FILE =  "/system/fonts/Clockopia.ttf";
    static public final String DEFULT_FONT_FILE =  "/system/fonts/Roboto-Regular.ttf";


    static public final int FONT_SIZE_SMALL = 12;
    static public final int FONT_SIZE_NORMAL = 16;
    static public final int FONT_SIZE_MIDDLE = 18;
    static public final int FONT_SIZE_LARGE = 22;
    static public final int DEFAULT_FONT_SIZE = FONT_SIZE_NORMAL;

    static public final int DEFAULT_BASE = 32;

    static public final int DEFAULT_FONT_SIZE_HEADER_FOOTER = DEFAULT_BASE + 6;
    static public final int DEFAULT_FONT_SIZE_PREMESSAGE = DEFAULT_BASE;
	static public final int DEFAULT_FONT_SIZE_ITEM = DEFAULT_BASE;
    static public final int DEFAULT_FONT_SIZE_CONDIMENT = DEFAULT_BASE;

    static public String FLAG_DEFAULT_FONT = "default_font";
    static public String FLAG_SIZE_NORMAL = "normal_size";
    static public String FLAG_SIZE_MIDDLE = "middle_size";
    static public String FLAG_SIZE_LARGE = "large_size";
    static public String FLAG_SIZE_SMALL = "small_size";

    static public String FLAG_SIZE_HF = "hfooter_size";//header/footer size, default=32
    static public String FLAG_SIZE_PREMESSAGE = "msg_size";//header/footer size, default=32
    static public String FLAG_SIZE_ITEM = "item_size";//header/footer size, default=32
    static public String FLAG_SIZE_CONDIMENT = "condiment_size";//header/footer size, default=32

    static public final int DEFAULT_BG = Color.WHITE;
    static public final int DEFAULT_FG = Color.BLACK;
    ////////////////////////////////////////////////////////////////////
    private int m_colorBG = Color.WHITE;
    private int m_colorFG = Color.BLACK;
    private Typeface m_tfFont = null;
    private int m_nFontSize = DEFAULT_FONT_SIZE; //default;
    private String m_strFontFilePath = DEFULT_FONT_FILE;//"/system/fonts/DroidSans.ttf";
    public KDSViewFontFace(int bg, int fg, String strFontFilePath, int nFontSize)
    {
        setBG(bg);
        setFG(fg);
        if (strFontFilePath != null)
            m_strFontFilePath = strFontFilePath;
        //setFont(tf);
        m_nFontSize = nFontSize;
    }

    public KDSViewFontFace()
    {

    }
    @Override
    public String toString()
    {
        String s = m_strFontFilePath;
        if (m_strFontFilePath.equals(DEFULT_FONT_FILE))
            s = FLAG_DEFAULT_FONT;
        s += ",";
        s +=  Integer.toString(m_colorBG);
        s += ",";
        s +=  Integer.toString(m_colorFG);
        s += ",";
        if (m_nFontSize == FONT_SIZE_NORMAL)
            s += FLAG_SIZE_NORMAL;
        else if (m_nFontSize == FONT_SIZE_MIDDLE)
            s += FLAG_SIZE_MIDDLE;
        else if (m_nFontSize == FONT_SIZE_LARGE)
            s += FLAG_SIZE_LARGE;
        else if (m_nFontSize == FONT_SIZE_SMALL)
            s += FLAG_SIZE_SMALL;
        else if (m_nFontSize == DEFAULT_FONT_SIZE_HEADER_FOOTER)
            s += FLAG_SIZE_PREMESSAGE;
        else if (m_nFontSize == DEFAULT_FONT_SIZE_ITEM)
            s += FLAG_SIZE_ITEM;
        else if (m_nFontSize == DEFAULT_FONT_SIZE_PREMESSAGE)
            s += FLAG_SIZE_PREMESSAGE;
        else if (m_nFontSize == DEFAULT_FONT_SIZE_CONDIMENT)
            s += FLAG_SIZE_CONDIMENT;
        else
            s +=  Integer.toString(m_nFontSize);
        return s;
    }



    /**
     *
     * file, bg, fg, size
     * @param str
     * @return
     */
    static public KDSViewFontFace parseString(String str)
    {
        KDSViewFontFace ff = new KDSViewFontFace();
        String s = new String(str);
        int n = s.indexOf(",", 0);
        if (n <0) return ff;
        ff.m_strFontFilePath = s.substring(0, n );
        if (ff.m_strFontFilePath.equals(FLAG_DEFAULT_FONT))
            ff.m_strFontFilePath = DEFULT_FONT_FILE;
        s = s.substring(n + 1);

        n = s.indexOf(",", 0);
        if (n <0) return ff;
        String bg = s.substring(0, n );
        ff.m_colorBG = convertColor(bg);// Integer.parseInt(s.substring(0, n ));
        s = s.substring(n + 1);

        n = s.indexOf(",", 0);
        if (n <0) return ff;
        String fg = s.substring(0, n );
        ff.m_colorFG = convertColor(fg);//  Integer.parseInt(s.substring(0, n ));
        s = s.substring(n + 1);

//        n = s.indexOf(",", 0);
//        if (n <0) return ff;
        if (s.indexOf(FLAG_SIZE_NORMAL)>=0)
            ff.m_nFontSize = FONT_SIZE_NORMAL;
        else if (s.indexOf(FLAG_SIZE_MIDDLE) >=0)
            ff.m_nFontSize = FONT_SIZE_MIDDLE;
        else if (s.indexOf(FLAG_SIZE_LARGE) >=0)
            ff.m_nFontSize = FONT_SIZE_LARGE;
        else if (s.indexOf(FLAG_SIZE_SMALL) >=0)
            ff.m_nFontSize = FONT_SIZE_SMALL;
        else if (s.indexOf(FLAG_SIZE_HF) >= 0)
            ff.m_nFontSize = DEFAULT_FONT_SIZE_HEADER_FOOTER;
        else if (s.indexOf(FLAG_SIZE_PREMESSAGE) >= 0)
            ff.m_nFontSize = DEFAULT_FONT_SIZE_PREMESSAGE;
        else if (s.indexOf(FLAG_SIZE_ITEM) >= 0)
            ff.m_nFontSize = DEFAULT_FONT_SIZE_ITEM;
        else if (s.indexOf(FLAG_SIZE_CONDIMENT) >= 0)
            ff.m_nFontSize = DEFAULT_FONT_SIZE_CONDIMENT;
        else
            ff.m_nFontSize = KDSUtil.convertStringToInt(s, FONT_SIZE_NORMAL);

        return ff;
    }

    static private int convertColor(String strColor)
    {
        int ncolor = 0;
        if (strColor.indexOf("@color/")>=0)
        {
            int nBgID = KDSApplication.getContext().getResources().getIdentifier(strColor, "color", KDSApplication.getContext().getPackageName());
            ncolor = KDSApplication.getContext().getResources().getColor(nBgID);
        }
        else
        {
            ncolor = Integer.parseInt(strColor);
        }
        return ncolor;
    }

    public void setBG(int nBG)
    {
        m_colorBG = nBG;
    }
    public int getBG()
    {
        return m_colorBG;
    }
    public void setFG(int nFG)
    {
        m_colorFG = nFG;
    }
    public int getFG()
    {
        if (m_colorFG == Color.TRANSPARENT)
            return Color.BLACK;
        return m_colorFG;
        //return ((0xff000000)|m_colorFG);
    }
    //    public void setFont(Typeface tf)
//    {
    //       m_tfFont = tf;
    //   }
    public Typeface getTypeFace()
    {
        if (m_strFontFilePath.isEmpty())
            return null;

        if (m_tfFont == null)
            m_tfFont = Typeface.createFromFile(m_strFontFilePath);
        return m_tfFont;
    }
    public void setFontSize(int nSize)
    {
        m_nFontSize = nSize;
    }
    public int getFontSize()
    {
        return m_nFontSize;
    }
    public void setFontFilePath(String filePath)
    {
        m_strFontFilePath = filePath;
    }
    public String getFontFilePath()
    {
        return m_strFontFilePath;
    }

    public void copyFrom(KDSViewFontFace ff)
    {
        m_colorBG = ff.getBG();
        m_colorFG = ff.getFG();
        m_strFontFilePath = ff.getFontFilePath();
        m_nFontSize = ff.getFontSize();
        m_tfFont = ff.getTypeFace();
    }

    public void resetTyptFace()
    {
        m_tfFont = null;
    }

    public void setTypeFace(Typeface tf)
    {
        m_tfFont = tf;
    }
}


