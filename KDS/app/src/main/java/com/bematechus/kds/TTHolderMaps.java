package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/4/11.
 */
public class TTHolderMaps {

    ArrayList<TTHolderMap> m_arMaps = new ArrayList<>();
    static final String MAP_SEPERATOR = "\n";


    public String toString()
    {
        String s = "";
        for (int i=0; i< m_arMaps.size(); i++)
        {
            if (!s.isEmpty())
                s += MAP_SEPERATOR;
            s += m_arMaps.get(i).toString();

        }
        return s;
    }
    public ArrayList<TTHolderMap> getArray()
    {
        return m_arMaps;
    }

    static public TTHolderMaps parseString(String s )
    {
        ArrayList<String> ar =  KDSUtil.spliteString(s,MAP_SEPERATOR );
        TTHolderMaps maps = new TTHolderMaps();
        for (int i=0; i< ar.size() ; i++)
        {
            TTHolderMap t = TTHolderMap.parseString(ar.get(i));
            if (t!= null)
                maps.getArray().add(t);
        }
        return maps;
    }

    public String getTableName(String holderID)
    {
        for (int i=0; i< m_arMaps.size(); i++)
        {
            if (m_arMaps.get(i).getHolderID().equals(holderID))
                return m_arMaps.get(i).getTableName();
        }
        return holderID;
    }

    /**
     *
     */
    static public class TTHolderMap
    {
        String m_holderID = "";
        String m_tableName = "";
        static final String SEPERATOR = "-->";

        public String getHolderID()
        {
            return m_holderID;
        }
        public String getTableName()
        {
            return m_tableName;
        }

        public void setHolderID(String holderID)
        {
            m_holderID = holderID;
        }
        public void setTableName(String tableName)
        {
            m_tableName = tableName;
        }

        public String toString()
        {
            return m_holderID + SEPERATOR + m_tableName;
        }
        static TTHolderMap parseString(String s)
        {
            if (s.isEmpty()) return null;
            int n = s.indexOf(SEPERATOR);
            TTHolderMap h = new TTHolderMap();
            if (n <0) {
                h.m_holderID = s;
                return h;
            }
            else
            {
                h.m_holderID = s.substring(0, n);
                h.m_tableName = s.substring(n+SEPERATOR.length());
                return h;
            }


        }
    }
}
