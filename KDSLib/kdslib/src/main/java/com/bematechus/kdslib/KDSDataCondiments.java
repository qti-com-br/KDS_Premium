/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSDataCondiments extends KDSDataArray {


    static public String STRINGS_SEPARATOR = ",   ";
    static public String STRINGS_EACH_LINE_SEPARATOR = "\n";
    public String toString()
    {
        String s = "";
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {

                KDSDataCondiment original = (KDSDataCondiment) ar.get(i);
                if (!s.isEmpty())
                {
                    s += STRINGS_SEPARATOR;
                }

                s += original.getDescription();
            }
        }
        return s;
    }

    public String toEachLineString()
    {
        String s = "";
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {

                KDSDataCondiment original = (KDSDataCondiment) ar.get(i);
                if (!s.isEmpty())
                {
                    s += STRINGS_EACH_LINE_SEPARATOR;
                }

                s += original.getDescription();
            }
        }
        return s;
    }

    public void copyTo(KDSDataArray objs)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataCondiment msg = new KDSDataCondiment();
                KDSDataCondiment original = (KDSDataCondiment) ar.get(i);
                original.copyTo(msg);
                objs.addComponent(msg);
            }
        }
    }
    public void setParentID(int nItemID)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) this.getComponent(i);
                condiment.setItemID(nItemID);
            }
        }
    }
    public boolean isEquals(KDSDataCondiments condiments)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            if (condiments.getCount() != ncount)
                return false;
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) this.getComponent(i);
                if (!condiment.isEqual((KDSDataCondiment) condiments.get(i)))
                    return false;
            }
            return true;
        }
    }

    public boolean isEqualsNoSort(KDSDataCondiments condiments)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            if (condiments.getCount() != ncount)
                return false;
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) this.getComponent(i);
                KDSDataCondiment findIt = condiments.getCondimentByDescription(condiment.getDescription());
                if (findIt == null)
                    return false;

            }

            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = condiments.getCondiment(i);//KDSDataCondiment) this.getComponent(i);
                KDSDataCondiment findIt = this.getCondimentByDescription(condiment.getDescription());
                if (findIt == null)
                    return false;
            }


            return true;
        }
    }

    public KDSDataCondiment getCondiment(int nIndex)
    {
        synchronized (m_locker) {
            Object obj = this.getComponent(nIndex);
            if (obj == null)
                return null;
            return (KDSDataCondiment) obj;
        }
        
    }
    public KDSDataCondiment getCondimentByName(String condimentName)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) this.getComponent(i);
                if (condiment.getCondimentName().equals(condimentName))
                    return condiment;

            }
            return null;
        }
    }

    public KDSDataCondiment getCondimentByDescription(String condimentDescription)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment condiment = (KDSDataCondiment) this.getComponent(i);
                if (condiment.getDescription().equals(condimentDescription))
                    return condiment;

            }
            return null;
        }
    }

    /**
     * find same condiment before get itselft in array.
     * it is for consolidate items
     * @param condiment
     * @return
     */
    public KDSDataCondiment findSameCondimentBeforeIt(KDSDataCondiment condiment)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataCondiment c = (KDSDataCondiment) this.getComponent(i);
                if (c == condiment) return null;
                if (c.getCondimentName().equals(condiment.getDescription()))
                    return c;

            }
            return null;
        }
    }

    /**
     * 
     * @param condimentName
     * @return 
     */
    public boolean deleteCondiment(String condimentName)
    {
        synchronized (m_locker) {
            KDSDataCondiment c = getCondimentByName(condimentName);
            if (c == null)
                return true;
            this.getComponents().remove(c);
            return true;
        }
    }
    
  
}
