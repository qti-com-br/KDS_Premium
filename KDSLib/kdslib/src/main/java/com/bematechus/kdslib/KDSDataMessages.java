
package com.bematechus.kdslib;

import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author David.Wong
 */
public class KDSDataMessages extends KDSDataArray{
    
    public void copyTo(KDSDataMessages objs)
    {
        synchronized (m_locker) {
            objs.clear();
            Vector ar = this.getComponents();
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

    public String toEachLineString()
    {
        String s = "";
        synchronized (m_locker) {
            Vector ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {

                KDSDataMessage original = (KDSDataMessage) ar.get(i);
                if (!s.isEmpty())
                {
                    s += STRINGS_EACH_LINE_SEPARATOR;
                }

                s += original.getMessage();
            }
        }
        return s;
    }

    /**
     * for firebase
     * @param strMessages
     * @param forWhat
     *      static public final int FOR_Order = 0;
     *     static public final int FOR_Item = 1;
     *     static public final int FOR_Condiment = 2;
     * @return
     */
    static public KDSDataMessages parseString(String strMessages, String parentGuid, int forWhat)
    {
        ArrayList<String> ar =  KDSUtil.spliteString(strMessages, STRINGS_EACH_LINE_SEPARATOR);
        KDSDataMessages messages = new KDSDataMessages();

        for (int i = 0; i < ar.size(); i++) {
            String s = ar.get(i);
            KDSDataMessage m = new KDSDataMessage();
            m.setComponentGUID(parentGuid);
            m.setForComponentType(forWhat);
            m.setMessage(s);
            messages.addComponent(m);

        }

        return messages;
    }
}
