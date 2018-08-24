package com.bematechus.kds;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by Administrator on 2015/10/21 0021.
 */
public class KDSSummaryItem {

    public static final String CONDIMENT_TAG = "\f"; //for sum condiments

    public enum SumSrcMode
    {
        Item,
        Condiment, //sum the condiment
    }
    float m_fltQty = 0;
    float m_fltSmartHidenQty = 0;

    String m_description = "";
    String m_category = "";
    String m_originalDescription = ""; //before the translate
    ArrayList<String> m_arCondiments = new ArrayList<>();
    SumSrcMode m_srcMode = SumSrcMode.Item;

    public void setSumSrcType(SumSrcMode mode)
    {
        m_srcMode = mode;
    }

    public SumSrcMode getSumSrcType()
    {
        return m_srcMode;
    }
    public void setOriginalDescription(String description)
    {
        m_originalDescription = description;
    }
    public String getOriginalDescription()
    {
        return m_originalDescription;
    }

    /**
     * in advanced summary, we need to check which summary item will been show.There are filter in it.
     * If the orignal description is existed, compare the orignal description.
     * @param strFilterDesciption
     * @return
     */
    public boolean isShowingItem(String strFilterDesciption)
    {
        if (m_originalDescription.isEmpty())
        {
            return m_description.equals(strFilterDesciption);
        }
        else
            return m_originalDescription.equals(strFilterDesciption);
    }
    public void setDescription(String strDescription)
    {
        m_description = strDescription;
    }
    public String getDescription()
    {
        return buildSummaryDescription();//m_description;
    }

    public String getDescription(boolean bTopSum)
    {
        if (bTopSum)
            return buildSummaryDescription();//m_description;
        else
            return buildSummaryDescriptionWithCondimentsRows();//m_description;
    }

    /**
     * If it needs the condiments, show them
     */
    private String buildSummaryDescription()
    {
        if (m_arCondiments.size() <=0)
            return m_description;
        else
        {
            String s = m_description;
            //
            if (m_arCondiments.size()>0)
                s +="\n\t";

            for (int i=0; i< m_arCondiments.size(); i++ )
            {
                //s +="\n\t";
                if (i >0)
                    s += " , ";
                s += m_arCondiments.get(i);
            }
            return s;
        }

    }

    private String buildSummaryDescriptionWithCondimentsRows()
    {
        if (m_arCondiments.size() <=0)
            return m_description;
        else
        {
            String s = m_description;

            for (int i=0; i< m_arCondiments.size(); i++ )
            {
                s +="\n\t";

                s += m_arCondiments.get(i);
            }
            return s;
        }

    }

    public void setQty(float fltQty)
    {
        m_fltQty = fltQty;
    }
    public float getQty()
    {
        return m_fltQty;
    }

    public void setSmartHidenQty(float fltQty)
    {
        m_fltSmartHidenQty = fltQty;
    }

    public float getSmartHidenQty()
    {
        return m_fltSmartHidenQty;
    }

    public String getQtyString()
    {
        return KDSUtil.convertIntToString((int)getQty());
    }

    public String getAdvSumQtyString()
    {
        int n = (int)(m_fltQty-m_fltSmartHidenQty);
        if (n <0) n=0;
        String s = String.format("%d(%d)",n ,(int) m_fltSmartHidenQty);
        return s;

    }
    public void setCategory(String str)
    {
        m_category = str;
    }
    public String getCategory()
    {
        return m_category;
    }

    public void setCondiments(ArrayList<String> arCondiments)
    {
        m_arCondiments.addAll(arCondiments);
    }
    public ArrayList<String> getCondiments()
    {
        return m_arCondiments;
    }

}

