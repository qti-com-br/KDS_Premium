package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSDataArray;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Administrator on 2018/1/30.
 */
public class KDSRouterDataItemModifiers extends KDSDataArray {

    public boolean m_bEnabled = false; //for load data from database
    public void setEnabled(boolean bEnable)
    {
        m_bEnabled = bEnable;
    }
    public boolean getEnabled()
    {
        return m_bEnabled;
    }

    public  KDSRouterDataItemModifier getModifier(int nIndex)
    {
        return ( KDSRouterDataItemModifier)this.get(nIndex);
    }

    public Vector getArray()
    {
        return getComponents();
    }

    public String toString()
    {
        synchronized (m_locker) {
            String s = "";
            for (int i = 0; i < this.getCount(); i++) {
                if (i > 0)
                    s += "\n";
                s += getModifier(i).toString();

            }
            return s;
        }
    }
    static public KDSRouterDataItemModifiers parseString(String strModifiers)
    {

        KDSRouterDataItemModifiers names = new KDSRouterDataItemModifiers();
        if (strModifiers.isEmpty()) return names;


        String s = strModifiers;
        s = s.replace("\r", "");
        ArrayList<String> ar = KDSUtil.spliteString(s, "\n");
        for (int i=0; i< ar.size(); i++)
        {
            KDSRouterDataItemModifier name = KDSRouterDataItemModifier.parseString(ar.get(i));
            if (name != null)
                names.addComponent(name);
        }
        return names;
    }

    public boolean isAllEmpty()
    {
        synchronized (m_locker) {
            for (int i = 0; i < this.getCount(); i++) {
                if (!this.getModifier(i).getDescription().isEmpty())
                    return false;
            }
            return true;
        }
    }

    /**
     * for export database to csv file
     */

    public final static String CSV_INTERNAL_SEPARATOR = "_";
    public String toStringForCSV()
    {
        synchronized (m_locker) {
            String s = "";
            for (int i = 0; i < this.getCount(); i++) {
                if (i > 0)
                    s += CSV_INTERNAL_SEPARATOR;
                s += getModifier(i).toString();

            }
            return s;
        }
    }

    static public KDSRouterDataItemModifiers parseStringForCSV(String modifierNames)
    {

        KDSRouterDataItemModifiers names = new KDSRouterDataItemModifiers();
        if (modifierNames.isEmpty()) return names;

        String s = modifierNames;
        s = s.replace("\r", "");
        ArrayList<String> ar = KDSUtil.spliteString(s, CSV_INTERNAL_SEPARATOR);
        for (int i=0; i< ar.size(); i++)
        {
            KDSRouterDataItemModifier name = KDSRouterDataItemModifier.parseString(ar.get(i));
            if (name != null)
                names.addComponent(name);
        }
        return names;
    }
}
