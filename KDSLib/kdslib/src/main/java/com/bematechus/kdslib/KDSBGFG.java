package com.bematechus.kdslib;

/**
 * Save background and foreground colors
 */
public class KDSBGFG {

    int m_bg = 0;
    int m_fg = 0;

    public KDSBGFG()
    {

    }
    public KDSBGFG(int bg, int fg)
    {
        m_bg = bg;
        m_fg = fg;
    }

    public int getBG()
    {

        return (0xff000000|m_bg);

    }
    public void setBG(int nBG)
    {
        m_bg = nBG;
    }

    public int getFG()
    {

            return (0xff000000|m_fg);

    }
    public void setFG(int nFG)
    {
        m_fg = nFG;
    }
    @Override
    public String toString()
    {
        return KDSUtil.convertIntToString2(m_bg) + "," + KDSUtil.convertIntToString2(m_fg);
    }

    public void copyFrom(KDSBGFG bf)
    {
        m_bg = bf.getBG();
        m_fg = bf.getFG();
    }
    static public KDSBGFG parseString(String s)
    {
        int n = s.indexOf(",");
        if (n<0) return new KDSBGFG();

        String bg = s.substring(0, n);
        String fg = s.substring(n + 1);
        int nbg = KDSUtil.convertStringToInt(bg, 0);
        int nfg = KDSUtil.convertStringToInt(fg, 0);
        KDSBGFG c = new KDSBGFG(nbg, nfg);

        return c;
    }
}
