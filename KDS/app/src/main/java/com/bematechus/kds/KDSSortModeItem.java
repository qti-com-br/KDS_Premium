package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/19.
 * For sorted mode view.
 * Use it in settings
 */
public class KDSSortModeItem {

    KDSSettings.OrdersSort m_sortMethod = KDSSettings.OrdersSort.Waiting_Time_Ascend;
    String m_strDescription = "";

    public String toString()
    {
        String s = getSortName(m_sortMethod);
        if (m_strDescription.isEmpty())
            return s;
        else
        {
            return s + TEXT_SEPARATOR + m_strDescription;
        }
    }

    static public String getSortName(KDSSettings.OrdersSort ordersSort)
    {
        switch (ordersSort)
        {

            case Manually:
                return "Manually";

            case Waiting_Time_Ascend:
                return "Time asc.";

            case Waiting_Time_Decend:
                return "Time dec.";

            case Order_Number_Ascend:
                return "Number asc.";

            case Order_Number_Decend:
                return "Number dec.";

            case Items_Count_Ascend:
                return "Count asc.";

            case Items_Count_Decend:
                return "Count dec.";

            case Preparation_Time_Ascend:
                return "Prep asc.";

            case Preparation_Time_Decend:
                return "Prep dec.";
        }
        return "";
    }
    public boolean isSame(KDSSortModeItem item)
    {
        if ((item.getSortMethod() == m_sortMethod) &&
                (item.getDisplayText().equals(m_strDescription)))
            return true;
        return false;
    }
    public KDSSortModeItem clone()
    {
        KDSSortModeItem item = new KDSSortModeItem();
        item.setDisplayText(m_strDescription);
        item.setSortMethod(m_sortMethod);
        return item;
    }

    public void setSortMethod(KDSSettings.OrdersSort method)
    {
        m_sortMethod = method;
    }
    public KDSSettings.OrdersSort getSortMethod()
    {
        return m_sortMethod;
    }

    public void setDisplayText(String text)
    {
        m_strDescription = text;
    }
    public String getDisplayText()
    {
        return m_strDescription;
    }

    public String getShowingText()
    {
        if (m_strDescription.isEmpty())
            return getSortName(m_sortMethod);
        else
            return m_strDescription;
    }
    static public final String TABDISP_SEPARATOR = "\n";

    static public String TEXT_SEPARATOR = "-->";

    static public String createSortModeSaveItemString(KDSSortModeItem item)
    {
        String s = KDSUtil.convertIntToString(item.getSortMethod().ordinal()) +TEXT_SEPARATOR +item.getDisplayText();
        return s;
    }

    static public KDSSortModeItem parseSortModeSaveItemString(String savedString)
    {
        ArrayList<String> ar = new ArrayList<>();
        ar =  KDSUtil.spliteString(savedString, TEXT_SEPARATOR);
        if (ar.size() <2)
            return null;
        String sort = ar.get(0);
        int n = KDSUtil.convertStringToInt(sort, 0);
        KDSSettings.OrdersSort ordersSort = KDSSettings.OrdersSort.values()[n];
        String text = ar.get(1);
        KDSSortModeItem item = new KDSSortModeItem();
        item.setSortMethod(ordersSort);
        item.setDisplayText(text);
        return item;

    }

    public static ArrayList<KDSSortModeItem> parseSortModeSaveString(String destString)
    {

        ArrayList<String> ar = new ArrayList<>();
        ArrayList<KDSSortModeItem> arReturn = new ArrayList<>();

        ar =  KDSUtil.spliteString(destString, TABDISP_SEPARATOR);
        for (int i=0; i< ar.size(); i++)
        {
            String s = ar.get(i);
            if (s.isEmpty()) continue;
            KDSSortModeItem item = parseSortModeSaveItemString(ar.get(i));
            arReturn.add(item);
        }
        return arReturn;
    }


    static public String getSaveString( ArrayList<KDSSortModeItem> ar)
    {
        String s = "";
        for (int i=0; i< ar.size(); i++)
        {
            KDSSortModeItem m = ar.get(i);
            String str = createSortModeSaveItemString(m);
            if (!s.isEmpty())
            {
                s += TABDISP_SEPARATOR;
            }
            s += str;


        }
        return s;
    }
}
