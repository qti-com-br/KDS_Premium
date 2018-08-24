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
public class KDSDataArray {
    protected ArrayList m_arComponents = new ArrayList();

    protected Object m_locker = new Object();

    public ArrayList getComponents()
    {
        return m_arComponents;
    }
    
    public void addComponent(KDSData obj)
    {
        synchronized (m_locker) {
            m_arComponents.add(obj);
        }
    }
    
    public void insertComponent(int index, KDSData obj)
    {
        synchronized (m_locker) {
            m_arComponents.add(index, obj);
        }
    }
    
    
    public KDSData getComponent(int nIndex)
    {
        return (KDSData)m_arComponents.get(nIndex);
    }
    
    public boolean removeComponent(int nIndex)
    {
        synchronized (m_locker) {
            m_arComponents.remove(nIndex);
            return true;
        }
    }
    
    public boolean removeComponent(Object obj)
    {
        synchronized (m_locker) {
            m_arComponents.remove(obj);
            return true;
        }
    }
    
    public boolean deleteComponent(int nIndex)
    {
        synchronized (m_locker) {
            m_arComponents.remove(nIndex);
            return true;
        }
    }
    public boolean clear()
    {
        synchronized (m_locker) {
            m_arComponents.clear();
            return true;
        }
    }
    public void copyTo(KDSDataArray objs)
    {
      
    }
    public int getCount()
    {
        synchronized (m_locker) {
            return m_arComponents.size();
        }
    }
    public int getItemsCountExceptAttached()
    {
        synchronized (m_locker) {
            int ncount = getCount();
            int ncounter = 0;
            for (int i = 0; i < ncount; i++) {
                if (m_arComponents.get(i) instanceof KDSDataFromPrimaryIndicator) continue;

                ncounter++;
            }
            return ncounter;
        }
    }
    public KDSData get(int nIndex)
    {
        synchronized (m_locker) {
            if (nIndex > m_arComponents.size() - 1)
                return null;
            if (nIndex < 0)
                return null;
            return (KDSData) m_arComponents.get(nIndex);
        }
    }
    
}
