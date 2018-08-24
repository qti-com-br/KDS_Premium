
package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSDataMessages extends KDSDataArray{
    
    public void copyTo(KDSDataMessages objs)
    {
        synchronized (m_locker) {
            objs.clear();
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataMessage msg = new KDSDataMessage();
                KDSDataMessage original = (KDSDataMessage) ar.get(i);
                original.copyTo(msg);
                objs.addComponent(msg);
            }
        }
    }
    

    public boolean isEquals(KDSDataMessages messages)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            if (messages.getCount() != ncount)
                return false;
            for (int i = 0; i < ncount; i++) {
                KDSDataMessage m = (KDSDataMessage) this.getComponent(i);
                if (!m.isEqual((KDSDataMessage) messages.get(i)))
                    return false;
            }
            return true;
        }
    }

    public KDSDataMessage getMessage(int nIndex)
    {
        synchronized (m_locker) {
            return (KDSDataMessage) get(nIndex);
        }
    }
}
