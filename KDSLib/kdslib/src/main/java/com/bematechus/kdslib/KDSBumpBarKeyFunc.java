package com.bematechus.kdslib;

import android.view.KeyEvent;

import java.util.ArrayList;

/**
 * Used in KDSbumpBarFunctions class
 */
public class KDSBumpBarKeyFunc {


    public enum KeyboardType
    {
        Standard,
        BumpBar_A,
        BumpBar_B,
        BumpBar_C,
        BumpBar_D,
        BumpBar_E,

        BumpBar_G,
    }
    int m_keyCodes = 0;
    boolean m_bCtrl = false;
    boolean m_bAlt = false;
    boolean m_bShift = false;
    //KDSSettings.ID m_funcID = KDSSettings.ID.NULL; //don't save this value in preference.
    int m_funcID = 0; //don't save this value in preference.

    public void setKeyCode(int nKeyCode)
    {
        m_keyCodes = nKeyCode;

    }
    public  int getKeyCode()
    {
        return m_keyCodes;
    }
    public  void setCtrl(boolean bCtrl)
    {
        m_bCtrl = bCtrl;
    }
    public  boolean getCtrl()
    {
        return m_bCtrl;
    }

    public  void setAlt(boolean bAlt)
    {
        m_bAlt = bAlt;
    }
    public  boolean getAlt()
    {
        return m_bAlt;
    }

    public  void setShift(boolean bShift)
    {
        m_bShift = bShift;
    }
    public  boolean getShift()
    {
        return m_bShift;
    }

//    public void setFunctionID(KDSSettings.ID func)
//    {
//        m_funcID = func;
//    }
//    public KDSSettings.ID getFunctionID()
//    {
//        return m_funcID;
//    }
    public void setFunctionID(int func)
{
    m_funcID = func;
}
    public int getFunctionID()
    {
        return m_funcID;
    }

    final int CTRL_KEYNAME_INDEX = 1;
    public String getSummaryString(KeyboardType kbType)
    {
        String s = "";
        if (getAlt()) s += "[Alt]";
        if (getCtrl())
        {
            if (!s.isEmpty()) s += "+";
            if (kbType != KeyboardType.Standard)
            {
                String[] ar = getKeyNames(kbType);
                s += ar[CTRL_KEYNAME_INDEX];
                s = "[" + s + "]";
            }
            else
                s += "[Ctrl]";
        }

        if (getShift())
        {
            if (!s.isEmpty()) s += "+";
            s += "[Shift]";
        }

        if (getKeyCode() != 0)
        {
            if (!s.isEmpty()) s += "+";
            String keyname = KDSBumpBarKeyFunc.getKeyName(kbType, getKeyCode());
            if (!keyname.isEmpty()) {
                keyname = "[" + keyname + "]";
                s += keyname;
            }

        }
        return s;

    }
    public void copyFrom(KDSBumpBarKeyFunc bumpbarKey)
    {
        this.setAlt(bumpbarKey.getAlt());
        this.setCtrl(bumpbarKey.getCtrl());
        this.setShift(bumpbarKey.getShift());
    }

    public boolean isFitWithMyEventWithoutKbd(KeyEvent ev, KDSKbdRecorder kbd)
    {
        if (ev.getKeyCode() != this.getKeyCode())
            return false;
        if (this.getAlt()) {
            if (!ev.isAltPressed()) return false;
        }
        if (this.getCtrl())
        {
            if (!ev.isCtrlPressed())
                return false;
            if (ev.getKeyCode() != KeyEvent.KEYCODE_CTRL_LEFT &&
                    ev.getKeyCode() != KeyEvent.KEYCODE_CTRL_RIGHT )
                return false;

        }
        if (this.getShift())
        {
            if (!ev.isShiftPressed())
                return false;
        }
        return true;


    }

    public boolean isCombinationKeys()
    {
        if (getCtrl() ||
                getShift() ||
                getAlt())
            return  true;
        return false;
    }

    public boolean isFitWithMyEvent(KeyEvent ev, KDSKbdRecorder kbd)
    {
        if (kbd == null)
        {
            return isFitWithMyEventWithoutKbd(ev, kbd);
        }
        if (this.getCtrl()) {
            if (ev.getKeyCode() != KeyEvent.KEYCODE_CTRL_LEFT &&
                    ev.getKeyCode() != KeyEvent.KEYCODE_CTRL_RIGHT) {
                if (!kbd.isDown(KeyEvent.KEYCODE_CTRL_LEFT) &&
                        !kbd.isDown(KeyEvent.KEYCODE_CTRL_RIGHT))
                    return false;
            }
        }
        if (this.getAlt())
        {
            if (ev.getKeyCode() != KeyEvent.KEYCODE_ALT_LEFT &&
                    ev.getKeyCode() != KeyEvent.KEYCODE_ALT_RIGHT) {
                if (!kbd.isDown(KeyEvent.KEYCODE_ALT_LEFT) &&
                        !kbd.isDown(KeyEvent.KEYCODE_ALT_RIGHT))
                    return false;
            }
        }

        if (this.getShift())
        {
            if (ev.getKeyCode() != KeyEvent.KEYCODE_SHIFT_LEFT &&
                    ev.getKeyCode() != KeyEvent.KEYCODE_SHIFT_RIGHT) {
                if (!kbd.isDown(KeyEvent.KEYCODE_SHIFT_LEFT) &&
                        !kbd.isDown(KeyEvent.KEYCODE_SHIFT_RIGHT))
                    return false;
            }
        }

        int keycode = ev.getKeyCode();
        //ctrl up first, check other
        if (keycode ==KeyEvent.KEYCODE_CTRL_LEFT ||
                keycode == KeyEvent.KEYCODE_CTRL_RIGHT)
        {
            if (getCtrl())
            {
                if (kbd.isDown(this.getKeyCode()))
                    return true;
            }
        }

        //alt up first, check other
        if (keycode ==KeyEvent.KEYCODE_ALT_LEFT ||
                keycode == KeyEvent.KEYCODE_ALT_RIGHT)
        {
            if (getAlt())
            {
                if (kbd.isDown(this.getKeyCode()))
                    return true;
            }
        }

        //shift up first, check other
        if (keycode ==KeyEvent.KEYCODE_SHIFT_LEFT ||
                keycode == KeyEvent.KEYCODE_SHIFT_RIGHT)
        {
            if (getShift())
            {
                if (kbd.isDown(this.getKeyCode()))
                    return true;
            }
        }

        return (ev.getKeyCode() == this.getKeyCode());



    }

    /**
     * format:
     *  Code,Alt,Ctrl,Shift,Func
     * @return
     */
    @Override
    public String toString()
    {
        return makeKeysString(getKeyCode(), getAlt(), getCtrl(), getShift());


    }

    static public String getKeyName(KeyboardType kbType, int nKeyCode)
    {
        int values[] = KDSBumpBarKeyFunc.getKeyValues(kbType);
        String[] arName = KDSBumpBarKeyFunc.getKeyNames(kbType);

        for (int i=0; i< values.length; i++)
        {
            if (values[i] == nKeyCode)
            {
                return arName[i];
            }
        }
        return "";
    }

    static public String[] getKeyNames(KeyboardType kbType)
    {
        String[] names = null;

        switch (kbType)
        {
            case Standard:
                names = KDSBumpBarKeyFunc.STANDARD_KEY_NAME;
                break;
            case BumpBar_A:
                names =KDSBumpBarKeyFunc. BUMPBAR_A_KEY_NAME;
                break;
            case BumpBar_B:
                names =KDSBumpBarKeyFunc. BUMPBAR_B_KEY_NAME;
                break;
            case BumpBar_C:
                names = KDSBumpBarKeyFunc.BUMPBAR_C_KEY_NAME;
                break;
            case BumpBar_D:
                names =KDSBumpBarKeyFunc. BUMPBAR_D_KEY_NAME;
                break;
            case BumpBar_E:
                names =KDSBumpBarKeyFunc. BUMPBAR_E_KEY_NAME;
                break;
            //case BumpBar_F:
            //    break;
            case BumpBar_G:
                names = KDSBumpBarKeyFunc.BUMPBAR_G_KEY_NAME;
                break;
        }
       return names;

    }

    static public int[] getKeyValues(KeyboardType kbType)
    {
            int values[] = null;
            switch (kbType)
            {

                case Standard:
                    values =KDSBumpBarKeyFunc.STANDARD_KEY_VALUES;
                    break;
                case BumpBar_A:
                case BumpBar_B:
                case BumpBar_C:
                case BumpBar_D:
                case BumpBar_E:
                case BumpBar_G:
                    values = KDSBumpBarKeyFunc.BUMPBAR_KEY_VALUE;
                    break;
            }
        return values;

    }


    static public KDSBumpBarKeyFunc parseString(String strSettings)
    {
        ArrayList<String> ar = KDSUtil.spliteString(strSettings, ",");
        KDSBumpBarKeyFunc bumpbarKey = new KDSBumpBarKeyFunc();
        if (ar.size() <4)
            return bumpbarKey;
        //keycode
        String s = ar.get(0);
        bumpbarKey.setKeyCode(KDSUtil.convertStringToInt(s, 0));
        //alt
        s = ar.get(1);
        bumpbarKey.setAlt((KDSUtil.convertStringToInt(s, 0) == 1));
        //ctrl
        s = ar.get(2);
        bumpbarKey.setCtrl((KDSUtil.convertStringToInt(s, 0) == 1));
        //shift
        s = ar.get(3);
        bumpbarKey.setShift((KDSUtil.convertStringToInt(s, 0) == 1));
        //func
//        s = ar.get(4);
//        int nfunc = KDSUtil.convertStringToInt(s, 0);
//        bumpbarKey.setFunction(BumpBarFunc.values()[nfunc]);
        return bumpbarKey;

    }

    /**
     * create the keys settings string
     * @param keyCode
     * @param bAltDown
     * @param bCtrlDown
     * @param bShiftDown
     * @return
     */
    static public String makeKeysString(int keyCode, boolean bAltDown, boolean bCtrlDown,boolean bShiftDown )
    {
        String s = KDSUtil.convertIntToString(keyCode);
        s +=",";
        s += (bAltDown?"1":"0");
        s +=",";
        s += (bCtrlDown?"1":"0");
        s +=",";
        s += (bShiftDown?"1":"0");
        return s;
    }

    //bump bar Option-A legend
    static final  public String[] BUMPBAR_A_KEY_NAME = new String[]{
            "",
            "Ctrl",		"-",		"Down", "Left","Right",
            "Space",		"0",		"1",		"2",		"3",
            "4", 			"5",		"6",		"7",		"8",
            "9",			"Enter",

    };
    //bump bar Option-B legend
    static final public String[] BUMPBAR_B_KEY_NAME = new String[] {
            "",
            "Bump", "Recall", "Sum", "Page", "Up",
            "Down",      "1",  "2", "3", "4",
            "5", 	"6", "7", "8", "9",
            "10", "Redraw",

    };
    //bump bar Option-C legend
    static final public String[] BUMPBAR_C_KEY_NAME= new String[] {
            "",
            "Bump Item", "Left", "Right", "Up","Down",
            "Bump Ticket",      "0",  "1", "2", "3",
            "4", 	"5", "6", "7", "8",
            "9", "Menu",
    };
    //bump bar Option-D legend
    static final public String[] BUMPBAR_D_KEY_NAME= new String[]  {
            "",
            "Bump", "Recall", "Scroll", "Mode", "Home",
            "Summary",      "0/10",  "1", "2", "3",
            "4", 	"5", "6", "7", "8",
            "9", "Enter",

    };//
    //bump bar Option-E legend
    static final public String[] BUMPBAR_E_KEY_NAME= new String[] {
            "",
            "View", "Park", "Language", "Zoom", "Recall",
            "Bump",      "Reset",  "1", "2", "3",
            "4", 	"5", "6", "7", "8",
            "Left", "Right",

    };//
    //bump bar Option-G legend
    static final public String[] BUMPBAR_G_KEY_NAME = new String[] {
            "",
            "Bump Item",	"Recall",	"Summary",	"Park/Serve",	"In Progress",
            "Menu",			"0",		"1",		"2",			"3",
            "4", 			"5",		"6",		"7",			"8",
            "9",			"Enter",

    };
    static final public int[] BUMPBAR_KEY_VALUE = new int[] {
            0,
            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_NUMPAD_SUBTRACT/* .KEYCODE_MINUS*/, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_DPAD_RIGHT,
            KeyEvent.KEYCODE_SPACE,	   KeyEvent.KEYCODE_0,                                 	KeyEvent.KEYCODE_1,		KeyEvent.KEYCODE_2,		KeyEvent.KEYCODE_3,
            KeyEvent.KEYCODE_4,		   KeyEvent.KEYCODE_5,                                    KeyEvent.KEYCODE_6,		KeyEvent.KEYCODE_7,		KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9,		   KeyEvent.KEYCODE_ENTER,
    };


    static final String[] STANDARD_KEY_NAME = new String[]{
            "",
            "Space",  "'" ,   "," ,  "-" , "." , //0
            "/"    ,  "0" ,   "1" ,  "2" , "3",  //1
            "4"    ,  "5" ,   "6" ,  "7" , "8",  //2
            "9"    ,  ";" ,   "=" ,	 "[" , "\\", //3
            "]"    ,  "`" ,   "a" ,  "b" , "c",  //4
            "d"    ,  "e" ,   "f" ,  "g" , "h",  //5
            "i"    ,  "j" ,   "k" ,  "l" , "m",  //6
            "n"    ,  "o" ,   "p" ,	 "q" , "r",  //7
            "s"    ,  "t" ,   "u" ,  "v" , "w",  //8
            "x"    ,  "y" ,   "z" ,  "Ctrl", "Scroll Lock", //9
            "F1"   ,  "F2",   "F3",  "F4",  "F5", //10
            "F6"   ,  "F7",   "F8",  "F9",  "F10", //11
            "F11"  ,  "F12",  "Backspace", "Tab", "Enter", //12
            "Caps Lock",	"ESC",			"Alt Left",		"Shift Left",	"Shift Right", //13
            "Num Lock",		"Pad *",		"Pad -",		"Pad +",		//"Pad 5",  //14
            //"Pad 8 (^)",	"Pad 2 (Arrow Down)",	"Pad 6 ( -> )",  "Pad 4 ( <- )",  "Pad 0 (Ins)", //15
            //"Pad . (Del)",	"Pad 7 (Home)", "Pad 1 (End)",  "Pad 9 (PgUp)", "Pad 3 (PgDn)", //16
           /* "Pause",		"Print Screen",*/ "Ctrl Right",	/*"Win left",		"Win Right", *///17
            "Alt Right",	"Menu",			"Up",		"Down",	"Right", //18
            "Left",	"Insert",		"Delete",		"Home",			"End", //19
            "Page Up",		"Page Down"//"Pad Enter",	"Pad /" //20
    };
    static final int[] STANDARD_KEY_VALUES = new int[]{
            0,
            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SPACE/*  "'"*/ , KeyEvent.KEYCODE_COMMA ,KeyEvent.KEYCODE_MINUS ,KeyEvent.KEYCODE_PERIOD , //0
            KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_0 , KeyEvent.KEYCODE_1 ,KeyEvent.KEYCODE_2 ,KeyEvent.KEYCODE_3,  //1
            KeyEvent.KEYCODE_4    , KeyEvent.KEYCODE_5 ,KeyEvent.KEYCODE_6 ,KeyEvent.KEYCODE_7  ,KeyEvent.KEYCODE_8,  //2
            KeyEvent.KEYCODE_9    , KeyEvent.KEYCODE_SEMICOLON ,KeyEvent.KEYCODE_EQUALS ,KeyEvent.KEYCODE_LEFT_BRACKET ,KeyEvent.KEYCODE_BACKSLASH, //3
            KeyEvent.KEYCODE_RIGHT_BRACKET,             KeyEvent.KEYCODE_SPACE/* "`"*/ , KeyEvent.KEYCODE_A ,  KeyEvent.KEYCODE_B ,KeyEvent.KEYCODE_C,  //4
            KeyEvent.KEYCODE_D    , KeyEvent.KEYCODE_E ,KeyEvent.KEYCODE_F ,KeyEvent.KEYCODE_G ,KeyEvent.KEYCODE_H,  //5
            KeyEvent.KEYCODE_I    , KeyEvent.KEYCODE_J ,KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L ,KeyEvent.KEYCODE_M,  //6
            KeyEvent.KEYCODE_N    , KeyEvent.KEYCODE_O ,KeyEvent.KEYCODE_P ,KeyEvent.KEYCODE_Q ,KeyEvent.KEYCODE_R,  //7
            KeyEvent.KEYCODE_S    , KeyEvent.KEYCODE_T ,KeyEvent.KEYCODE_U ,KeyEvent.KEYCODE_V ,KeyEvent.KEYCODE_W,  //8
            KeyEvent.KEYCODE_X    , KeyEvent.KEYCODE_Y ,KeyEvent.KEYCODE_Z ,KeyEvent.KEYCODE_CTRL_LEFT,KeyEvent.KEYCODE_SCROLL_LOCK, //9
            KeyEvent.KEYCODE_F1   , KeyEvent.KEYCODE_F2,KeyEvent.KEYCODE_F3,KeyEvent.KEYCODE_F4,KeyEvent.KEYCODE_F5, //10
            KeyEvent.KEYCODE_F6   , KeyEvent.KEYCODE_F7,   KeyEvent.KEYCODE_F8,  KeyEvent.KEYCODE_F9,  KeyEvent.KEYCODE_F10, //11
            KeyEvent.KEYCODE_F11  , KeyEvent.KEYCODE_F12,  KeyEvent.KEYCODE_BACK,KeyEvent.KEYCODE_TAB,KeyEvent.KEYCODE_ENTER, //12
            KeyEvent.KEYCODE_CAPS_LOCK,KeyEvent.KEYCODE_ESCAPE,KeyEvent.KEYCODE_ALT_LEFT,KeyEvent.KEYCODE_SHIFT_LEFT,KeyEvent.KEYCODE_SHIFT_RIGHT, //13
            KeyEvent.KEYCODE_NUM_LOCK,KeyEvent.KEYCODE_NUMPAD_MULTIPLY,	KeyEvent.KEYCODE_NUMPAD_SUBTRACT,KeyEvent.KEYCODE_NUMPAD_ADD,		//"Pad 5",  //14
            //"Pad 8 (^)",	"Pad 2 (Arrow Down)",	"Pad 6 ( -> )",  "Pad 4 ( <- )",  "Pad 0 (Ins)", //15
            //"Pad . (Del)",	"Pad 7 (Home)", "Pad 1 (End)",  "Pad 9 (PgUp)", "Pad 3 (PgDn)", //16
            /*"Pause",		"Print Screen",*/KeyEvent.KEYCODE_CTRL_RIGHT,/*	"Win left",		"Win Right",*/ //17
            KeyEvent.KEYCODE_ALT_RIGHT,KeyEvent.KEYCODE_MENU,	KeyEvent.KEYCODE_DPAD_UP,	KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT, //18
            KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_INSERT,KeyEvent.KEYCODE_DEL,KeyEvent.KEYCODE_MOVE_HOME,KeyEvent.KEYCODE_MOVE_END, //19
            KeyEvent.KEYCODE_PAGE_UP,KeyEvent.KEYCODE_PAGE_DOWN //"Pad Enter",	"Pad /" //20
    };



}
