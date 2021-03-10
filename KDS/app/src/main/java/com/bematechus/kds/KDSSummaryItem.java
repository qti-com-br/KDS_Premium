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
    //ArrayList<String> m_arCondiments = new ArrayList<>();
    ArrayList<KDSSummaryCondiment> m_arCondiments = new ArrayList<>(); //kpp1-421
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
                s += makeCondimentSummaryText(m_arCondiments.get(i));
//                s += (int)(m_arCondiments.get(i).getQty()) + "x "; //kpp1-421
//                s += m_arCondiments.get(i).getDescription(); //kpp1-421
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
                s += makeCondimentSummaryText(m_arCondiments.get(i));

//                if (m_arCondiments.get(i).getQty()>1)
//                    s += (int)(m_arCondiments.get(i).getQty()) + "x "; //kpp1-421
//                s += m_arCondiments.get(i).getDescription(); //kpp1-421
            }
            return s;
        }

    }

    public String makeCondimentSummaryText(KDSSummaryCondiment c)
    {
        String s = "";
        //kpp1-435
        int qty = (int)(c.getQty() * this.getQty());
        
        if (qty>1)
            s += qty + "x "; //kpp1-421, kpp1-421 comments
        s += c.getDescription(); //kpp1-421
        return s;
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

    /**
     * rev.:
     *  kpp1-421
     * @param arCondiments
     */
    public void setCondiments(ArrayList<KDSSummaryCondiment> arCondiments)
    {
        m_arCondiments.addAll(arCondiments);
    }

    /**
     * rev.:
     *  kpp1-421
     * @return
     */
    public ArrayList<KDSSummaryCondiment> getCondiments()
    {
        return m_arCondiments;
    }

    public String getItemDescription()
    {
        return m_description;
    }
}

