package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/1/31.
 */
public class KDSDataModifiers extends KDSDataCondiments {
    public KDSDataModifier getModifier(int nIndex)
    {
        return (KDSDataModifier)get(nIndex);
    }

    public void copyTo(KDSDataArray objs)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataModifier msg = new KDSDataModifier();
                KDSDataModifier original = (KDSDataModifier) ar.get(i);
                original.copyTo(msg);
                objs.addComponent(msg);
            }
        }
    }
    public KDSDataModifier findModifier(String modifierDescription)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataModifier original = (KDSDataModifier) ar.get(i);
                if (original.getDescription().toUpperCase().equals(modifierDescription.toUpperCase()))
                    return original;

            }
        }
        return null;
    }
}
