package com.bematechus.kdslib;

import java.util.ArrayList;

public class KDSPOSMessages {

    ArrayList<KDSPOSMessage> mMessages = new ArrayList<>();

    public void doMessage(KDSPOSMessage msg)
    {
        if (msg.getDeleteMe())
        {
            KDSPOSMessage m = find(msg.getID());
            mMessages.remove(m);
        }
        else
            mMessages.add(msg);
    }

    public ArrayList<KDSPOSMessage> getArray()
    {
        return mMessages;
    }

    public KDSPOSMessage find(String id)
    {
        for (int i=0; i< mMessages.size(); i++)
        {
            if (mMessages.get(i).getID().equals(id))
                return mMessages.get(i);
        }
        return null;
    }

    public KDSPOSMessage pop()
    {
        if (mMessages.size() >0)
        {
            KDSPOSMessage m = mMessages.get(0);
            mMessages.remove(m);
            return m;
        }
        return null;
    }

}
