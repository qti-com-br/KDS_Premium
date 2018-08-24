package com.bematechus.kdslib;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

import java.util.ArrayList;

/**
 * Create or parse CSV string
 */
public class CSVStrings {
    ArrayList<String> m_arFiles =new ArrayList<>();

    public void add(String s)
    {
        m_arFiles.add(s);
    }
    public String get(int nIndex)
    {
        return m_arFiles.get(nIndex);
    }

    public int getCount()
    {
        return m_arFiles.size();
    }
    public ArrayList<String> getArray()
    {
        return m_arFiles;
    }
    static public CSVStrings parse(String csvString)
    {
        String s = csvString;
        ArrayList<String> ar =  KDSUtil.spliteString(s, ",");
        CSVStrings c = new CSVStrings();
        c.getArray().clear();
        for (int i=0; i< ar.size(); i++) {
            if (!ar.get(i).isEmpty())
                c.getArray().add(ar.get(i));
        }


        return c;
    }
    public String toCSV()
    {
        String s = "";
        for (int i=0; i< m_arFiles.size(); i++)
        {
            if (!s.isEmpty()) s += ",";
            s += m_arFiles.get(i);

        }
        return s;
    }

    public void clear()
    {
        m_arFiles.clear();
    }
    public void addAll(ArrayList<String> ar)
    {
        m_arFiles.addAll(ar);
    }

}
