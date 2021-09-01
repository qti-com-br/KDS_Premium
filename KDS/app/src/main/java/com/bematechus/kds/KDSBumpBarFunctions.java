package com.bematechus.kds;

import android.view.KeyEvent;

import com.bematechus.kdslib.KDSBumpBarKeyFunc;
import com.bematechus.kdslib.KDSKbdRecorder;

import java.util.ArrayList;

/**
 *
 * load all bumpbar key assignment to here
 */
public class KDSBumpBarFunctions {

    KDSBumpBarKeyFunc.KeyboardType m_kbdType = KDSBumpBarKeyFunc.KeyboardType.Standard;

    ArrayList<KDSBumpBarKeyFunc> m_arKeyFunc = new ArrayList<>();

    boolean m_bEnablePanelNumberFocus = false;

    public int getCount()
    {
        return m_arKeyFunc.size();
    }
    public void updateSettings(KDSSettings settings)
    {
        int ntype  = settings.getInt(KDSSettings.ID.Bumpbar_Kbd_Type);
        m_kbdType = KDSBumpBarKeyFunc.KeyboardType.values()[ntype];

        KDSSettings.ID[] ids = new KDSSettings.ID[]{
                //bump bar
                KDSSettings.ID.Bumpbar_OK,//0
                KDSSettings.ID.Bumpbar_Cancel,
                KDSSettings.ID.Bumpbar_Next,
                KDSSettings.ID.Bumpbar_Prev,
                KDSSettings.ID.Bumpbar_Up,//4
                KDSSettings.ID.Bumpbar_Down,
                KDSSettings.ID.Bumpbar_Bump,
                KDSSettings.ID.Bumpbar_Unbump,
                KDSSettings.ID.Bumpbar_Unbump_Last,
                KDSSettings.ID.Bumpbar_Sum,//
                KDSSettings.ID.Bumpbar_Transfer,//10
                KDSSettings.ID.Bumpbar_Sort,
                KDSSettings.ID.Bumpbar_Park,
                KDSSettings.ID.Bumpbar_Unpark,
                KDSSettings.ID.Bumpbar_Print,
                KDSSettings.ID.Bumpbar_More,//15
                KDSSettings.ID.Bumpbar_BuildCard,
                KDSSettings.ID.Bumpbar_Training,
                //KDSSettings.ID.Bumpbar_Page,//unused.
                KDSSettings.ID.Bumpbar_Menu,
                KDSSettings.ID.Bumpbar_QExpo_Pickup,//20
                KDSSettings.ID.Bumpbar_QExpo_Unpickup,
                KDSSettings.ID.Bumpbar_tab_next,
                KDSSettings.ID.Bumpbar_Clean, //kpp1-339
                KDSSettings.ID.Bumpbar_move, //kp-78, move order feature.
                KDSSettings.ID.Bumpbar_inputmsg,

                KDSSettings.ID.Bumpbar_page_next,
                KDSSettings.ID.Bumpbar_page_prev,
                //Add new function here !!!!!!!

                KDSSettings.ID.Bumpbar_Switch_User, //must keep this at last position
        };


        m_arKeyFunc.clear();
        int nlen = ids.length;
        int nmode = settings.getInt(KDSSettings.ID.Users_Mode);
        KDSSettings.KDSUserMode userMode = KDSSettings.KDSUserMode.values()[nmode];


        if (userMode == KDSSettings.KDSUserMode.Single)
            nlen --;

        for (int i=0; i< nlen; i++)
        {

            String str = settings.getString(ids[i]);
            KDSBumpBarKeyFunc bumpbarKey = KDSBumpBarKeyFunc.parseString(str);
            bumpbarKey.setFunctionID(ids[i].ordinal());
            m_arKeyFunc.add(bumpbarKey);
        }

        m_bEnablePanelNumberFocus = settings.getBoolean(KDSSettings.ID.Bumpbar_Panelnum_Focus);


    }

    public KDSSettings.ID checkPanelNumberFocus(KeyEvent ev)
    {
        if (!m_bEnablePanelNumberFocus) return KDSSettings.ID.NULL;;
        if (ev.isAltPressed() || ev.isCtrlPressed() ||
                ev.isShiftPressed())
            return KDSSettings.ID.NULL;
        if (ev.getKeyCode() == KeyEvent.KEYCODE_0 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_1 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_2 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_3 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_4 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_5 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_6 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_7 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_8 ||
                ev.getKeyCode() == KeyEvent.KEYCODE_9 ) {
            int n = KDSSettings.ID.Bumpbar_Focus_0.ordinal() + ev.getKeyCode() - KeyEvent.KEYCODE_0;
            return KDSSettings.ID.values()[n];
        }

        return KDSSettings.ID.NULL;

    }

    /**
     *
     * @param ev
     * @param kbd
     * @param bCleanFeatureEnabled
     *  Unused now!
     *  True: clean habits feature enabled.
     *  false:
     * @return
     */
    public KDSSettings.ID getKDSKeyboardEvent(KeyEvent ev, KDSKbdRecorder kbd, boolean bCleanFeatureEnabled)
    {
        int ncount = m_arKeyFunc.size();

        //check combination first
        for (int i=0; i< ncount; i++)
        {
            KDSBumpBarKeyFunc keyFunc = m_arKeyFunc.get(i);
            if (KDSSettings.intToID(keyFunc.getFunctionID()) ==  KDSSettings.ID.Bumpbar_OK||
                    KDSSettings.intToID(keyFunc.getFunctionID()) ==KDSSettings.ID.Bumpbar_Cancel)
                continue;
            if (!keyFunc.isCombinationKeys()) continue;
            if (keyFunc.isFitWithMyEvent(ev, kbd))
                return KDSSettings.intToID(keyFunc.getFunctionID());
        }
        //check single key second.
        for (int i=0; i< ncount;i++)
        {
            KDSBumpBarKeyFunc keyFunc = m_arKeyFunc.get(i);
            if (KDSSettings.intToID(keyFunc.getFunctionID()) ==  KDSSettings.ID.Bumpbar_OK||
                    KDSSettings.intToID(keyFunc.getFunctionID()) ==KDSSettings.ID.Bumpbar_Cancel)
                continue;
            if (keyFunc.isCombinationKeys()) continue;
            if (keyFunc.isFitWithMyEvent(ev, kbd))
                return KDSSettings.intToID(keyFunc.getFunctionID());
        }


        //check panel number focus
        KDSSettings.ID panelFocus = checkPanelNumberFocus(ev);
        if (panelFocus!=  KDSSettings.ID.NULL)
            return panelFocus;
        return KDSSettings.ID.NULL;
    }

    private boolean isQexpoKey(KDSSettings.ID eventID)
    {
        if (eventID ==  KDSSettings.ID.Bumpbar_Bump ||
                eventID ==  KDSSettings.ID.Bumpbar_Unbump ||
                eventID ==  KDSSettings.ID.Bumpbar_QExpo_Pickup ||
                eventID ==  KDSSettings.ID.Bumpbar_QExpo_Unpickup)
            return true;
        return false;
    }

    public KDSSettings.ID getQexpoKeyboardEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        int ncount = m_arKeyFunc.size();

        //check combination first
        for (int i=0; i< ncount; i++)
        {
            KDSBumpBarKeyFunc keyFunc = m_arKeyFunc.get(i);
            if (KDSSettings.intToID(keyFunc.getFunctionID()) ==  KDSSettings.ID.Bumpbar_OK||
                    KDSSettings.intToID(keyFunc.getFunctionID()) ==KDSSettings.ID.Bumpbar_Cancel)
                continue;
            if (!isQexpoKey(KDSSettings.intToID(keyFunc.getFunctionID()))) continue;
            if (!keyFunc.isCombinationKeys()) continue;
            if (keyFunc.isFitWithMyEvent(ev, kbd))
                return KDSSettings.intToID(keyFunc.getFunctionID());
        }
        //check single key second.
        for (int i=0; i< ncount;i++)
        {
            KDSBumpBarKeyFunc keyFunc = m_arKeyFunc.get(i);
            if (KDSSettings.intToID(keyFunc.getFunctionID()) ==  KDSSettings.ID.Bumpbar_OK||
                    KDSSettings.intToID(keyFunc.getFunctionID()) ==KDSSettings.ID.Bumpbar_Cancel)
                continue;
            if (!isQexpoKey(KDSSettings.intToID(keyFunc.getFunctionID()))) continue;
            if (keyFunc.isCombinationKeys()) continue;
            if (keyFunc.isFitWithMyEvent(ev, kbd))
                return KDSSettings.intToID(keyFunc.getFunctionID());
        }


        //check panel number focus
        KDSSettings.ID panelFocus = checkPanelNumberFocus(ev);
        if (panelFocus!=  KDSSettings.ID.NULL)
            return panelFocus;
        return KDSSettings.ID.NULL;
    }


    public KDSBumpBarKeyFunc getKeySettings(KDSSettings.ID funcID)
    {
        int ncount = m_arKeyFunc.size();
        for (int i=0; i< ncount; i++)
        {
            KDSBumpBarKeyFunc keyFunc = m_arKeyFunc.get(i);
            if (KDSSettings.intToID(keyFunc.getFunctionID()) == funcID)
                return keyFunc;

        }
        return null;
    }

    public KDSSettings.ID getKDSDlgEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        KDSSettings.ID[] ids = new KDSSettings.ID[]{
                //bump bar
                KDSSettings.ID.Bumpbar_OK,
                KDSSettings.ID.Bumpbar_Cancel,
        };

        int ncount = ids.length;
        for (int i=0; i< ncount; i++)
        {
            KDSBumpBarKeyFunc keyFunc =  getKeySettings(ids[i]);
            if (keyFunc == null) continue;

            if (keyFunc.isFitWithMyEvent(ev, kbd))
                return KDSSettings.intToID(keyFunc.getFunctionID());
        }
        return KDSSettings.ID.NULL;
    }

    public String getKeyString(KDSSettings.ID evID)
    {
        KDSBumpBarKeyFunc keyFunc = this.getKeySettings(evID);
        return keyFunc.getSummaryString(m_kbdType);
    }

    public KDSBumpBarKeyFunc.KeyboardType getKbdType()
    {
        return m_kbdType;
    }

}
