package com.bematechus.kdslib;


import java.util.ArrayList;

/**
 * Translate the summary name.
 * The data is from router.
 * Router add "SumTrans" tag to order xml.
 * One item can have multiple trans names
 */
public class KDSDataSumNames extends KDSDataArray{

    public boolean m_bEnabled = false; //for load data from database
    public void setEnabled(boolean bEnable)
    {
        m_bEnabled = bEnable;
    }
    public boolean getEnabled()
    {
        return m_bEnabled;
    }

    public  KDSDataSumName getSumName(int nIndex)
    {
        return ( KDSDataSumName)this.get(nIndex);
    }

    public ArrayList getArray()
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
                s += getSumName(i).toString();

            }
            return s;
        }
    }
    static public KDSDataSumNames parseString(String sumNames)
    {

        KDSDataSumNames names = new KDSDataSumNames();
        if (sumNames.isEmpty()) return names;


        String s = sumNames;
        s = s.replace("\r", "");
        ArrayList<String> ar = KDSUtil.spliteString(s, "\n");
        for (int i=0; i< ar.size(); i++)
        {
            KDSDataSumName name = KDSDataSumName.parseString(ar.get(i));
            if (name != null)
                names.addComponent(name);
        }
        return names;
    }

    public boolean isAllEmpty()
    {
        synchronized (m_locker) {
            for (int i = 0; i < this.getCount(); i++) {
                if (!this.getSumName(i).getDescription().isEmpty())
                    return false;
            }
            return true;
        }
    }
    public final static String CSV_INTERNAL_SEPARATOR = "_";
    public String toStringForCSV()
    {
        synchronized (m_locker) {
            String s = "";
            for (int i = 0; i < this.getCount(); i++) {
                if (i > 0)
                    s += CSV_INTERNAL_SEPARATOR;
                s += getSumName(i).toString();

            }
            return s;
        }
    }

    static public KDSDataSumNames parseStringForCSV(String sumNames)
    {

        KDSDataSumNames names = new KDSDataSumNames();
        if (sumNames.isEmpty()) return names;


        String s = sumNames;
        s = s.replace("\r", "");
        ArrayList<String> ar = KDSUtil.spliteString(s, CSV_INTERNAL_SEPARATOR);
        for (int i=0; i< ar.size(); i++)
        {
            KDSDataSumName name = KDSDataSumName.parseString(ar.get(i));
            if (name != null)
                names.addComponent(name);
        }
        return names;
    }
}
