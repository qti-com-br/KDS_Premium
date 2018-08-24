package com.bematechus.kds;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.bematechus.kdslib.KDSKbdRecorder;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class KDSUIDialogBumpBarKey extends  KDSUIDialogBase {



    CheckBox m_chkAlt = null;
    CheckBox m_chkCtrl = null;
    CheckBox m_chkShift = null;

    Spinner m_spinnerKeys = null;

    KDSBumpBarKeyFunc.KeyboardType m_kbType = KDSBumpBarKeyFunc.KeyboardType.Standard;
    KDSBumpBarKeyFunc m_keyEditing = new KDSBumpBarKeyFunc();
    @Override
    public void onOkClicked()
    {//save data here
        int keyval = getSelectedKeyVal();
        m_keyEditing.setKeyCode(keyval);
        m_keyEditing.setAlt(m_chkAlt.isChecked());
        m_keyEditing.setCtrl(m_chkCtrl.isChecked());
        m_keyEditing.setShift(m_chkShift.isChecked());
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_keyEditing;

    }

    public KDSUIDialogBumpBarKey(final Context context, KDSBumpBarKeyFunc ff, KDSDialogBaseListener listener, KDSBumpBarKeyFunc.KeyboardType kbType) {
        this.int_dialog(context, listener,R.layout.kdsui_dlg_bumpbar_key, "" );
        //get all widgets
        m_chkAlt =(CheckBox) this.getView().findViewById(R.id.chkAlt);
        m_chkCtrl =(CheckBox) this.getView().findViewById(R.id.chkCtrl);
        m_chkShift =(CheckBox) this.getView().findViewById(R.id.chkShift);
        m_spinnerKeys=(Spinner) this.getView().findViewById(R.id.spinnerKeys);
        m_kbType = kbType;
        m_keyEditing = ff;
        init_keys_list();

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });

    }

    final int CTRL_KEYNAME_INDEX = 1;
    private void init_keys_list()
    {
        String[] names = KDSBumpBarKeyFunc.getKeyNames(m_kbType);


        List<String> list = Arrays.asList(names);


        //第二步：为下拉列表定义一个适配器，这里就用到里前面定义的list。
        ArrayAdapter<String>  adapter = new ArrayAdapter<String>(this.getDialog().getContext(),android.R.layout.simple_spinner_item, list);
        //第三步：为适配器设置下拉列表下拉时的菜单样式。
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //第四步：将适配器添加到下拉列表上
        m_spinnerKeys.setAdapter(adapter);

        int nindex = getKeyCodeValueIndexOfArray(m_keyEditing.getKeyCode());
        m_spinnerKeys.setSelection(nindex);
        if (m_kbType == KDSBumpBarKeyFunc.KeyboardType.Standard)
        {

        }
        else
        {
            m_chkAlt.setVisibility(View.INVISIBLE);
            m_chkShift.setVisibility(View.INVISIBLE);
           // String[] names = KDSBumpBarKeyFunc.getKeyNames(m_kbType);
            m_chkCtrl.setText(names[CTRL_KEYNAME_INDEX]); //ctrl key name
        }

        m_chkAlt.setChecked(m_keyEditing.getAlt());
        m_chkCtrl.setChecked(m_keyEditing.getCtrl());
        m_chkShift.setChecked(m_keyEditing.getShift());

    }

    private int getSelectedIndex()
    {
        return m_spinnerKeys.getSelectedItemPosition();
    }
    private int getSelectedKeyVal()
    {
        int index = getSelectedIndex();
        int values[] = KDSBumpBarKeyFunc.getKeyValues(m_kbType);

        return values[index];

    }
    private int getKeyCodeValueIndexOfArray(int nKeyCode)
    {
        int values[] = KDSBumpBarKeyFunc.getKeyValues(m_kbType);

        int ncount = values.length;
        for (int i=0; i< ncount; i++)
        {
            if (values[i] == nKeyCode)
                return i;
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
//
//    //bump bar Option-A legend
//    static final  public String[] BUMPBAR_A_KEY_NAME = new String[]{
//            "Ctrl",		"-",		"Down", "Left","Right",
//            "Space",		"0",		"1",		"2",		"3",
//            "4", 			"5",		"6",		"7",		"8",
//            "9",			"Enter",
//
//    };
//    //bump bar Option-B legend
//    static final public String[] BUMPBAR_B_KEY_NAME = new String[] {
//            "Bump", "Recall", "Sum", "Page", "Up",
//            "Down",      "1",  "2", "3", "4",
//            "5", 	"6", "7", "8", "9",
//                "10", "Redraw",
//
//    };
//    //bump bar Option-C legend
//    static final public String[] BUMPBAR_C_KEY_NAME= new String[] {
//            "Bump Item", "Left", "Right", "Up","Down",
//            "Bump Ticket",      "0",  "1", "2", "3",
//            "4", 	"5", "6", "7", "8",
//            "9", "Menu",
//    };
//    //bump bar Option-D legend
//    static final public String[] BUMPBAR_D_KEY_NAME= new String[]  {
//        "Bump", "Recall", "Scroll", "Mode", "Home",
//                "Summary",      "0/10",  "1", "2", "3",
//                "4", 	"5", "6", "7", "8",
//                "9", "Enter",
//
//    };//
//    //bump bar Option-E legend
//    static final public String[] BUMPBAR_E_KEY_NAME= new String[] {
//        "View", "Park", "Language", "Zoom", "Recall",
//                "Bump",      "Reset",  "1", "2", "3",
//                "4", 	"5", "6", "7", "8",
//                "Left", "Right",
//
//    };//
//    //bump bar Option-G legend
//    static final public String[] BUMPBAR_G_KEY_NAME = new String[] {
//            "Bump Item",	"Recall",	"Summary",	"Park/Serve",	"In Progress",
//            "Menu",			"0",		"1",		"2",			"3",
//            "4", 			"5",		"6",		"7",			"8",
//            "9",			"Enter",
//
//    };
//    static final public int[] BUMPBAR_KEY_VALUE = new int[] {
//            KeyEvent.KEYCODE_CTRL_LEFT, KeyEvent.KEYCODE_NUMPAD_SUBTRACT/* .KEYCODE_MINUS*/, KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_DPAD_RIGHT,
//            KeyEvent.KEYCODE_SPACE,	   KeyEvent.KEYCODE_0,                                 	KeyEvent.KEYCODE_1,		KeyEvent.KEYCODE_2,		KeyEvent.KEYCODE_3,
//            KeyEvent.KEYCODE_4,		   KeyEvent.KEYCODE_5,                                    KeyEvent.KEYCODE_6,		KeyEvent.KEYCODE_7,		KeyEvent.KEYCODE_8,
//            KeyEvent.KEYCODE_9,		   KeyEvent.KEYCODE_ENTER,
//    };
//
//
//    static final String[] STANDARD_KEY_NAME = new String[]{
//            "Space",  "'" ,   "," ,  "-" , "." , //0
//            "/"    ,  "0" ,   "1" ,  "2" , "3",  //1
//            "4"    ,  "5" ,   "6" ,  "7" , "8",  //2
//            "9"    ,  ";" ,   "=" ,	 "[" , "\\", //3
//            "]"    ,  "`" ,   "a" ,  "b" , "c",  //4
//            "d"    ,  "e" ,   "f" ,  "g" , "h",  //5
//            "i"    ,  "j" ,   "k" ,  "l" , "m",  //6
//            "n"    ,  "o" ,   "p" ,	 "q" , "r",  //7
//            "s"    ,  "t" ,   "u" ,  "v" , "w",  //8
//            "x"    ,  "y" ,   "z" ,  "Ctrl", "Scroll Lock", //9
//            "F1"   ,  "F2",   "F3",  "F4",  "F5", //10
//            "F6"   ,  "F7",   "F8",  "F9",  "F10", //11
//            "F11"  ,  "F12",  "Backspace", "Tab", "Enter", //12
//            "Caps Lock",	"ESC",			"Alt Left",		"Shift Left",	"Shift Right", //13
//            "Num Lock",		"Pad *",		"Pad -",		"Pad +",		//"Pad 5",  //14
//            //"Pad 8 (^)",	"Pad 2 (Arrow Down)",	"Pad 6 ( -> )",  "Pad 4 ( <- )",  "Pad 0 (Ins)", //15
//            //"Pad . (Del)",	"Pad 7 (Home)", "Pad 1 (End)",  "Pad 9 (PgUp)", "Pad 3 (PgDn)", //16
//           /* "Pause",		"Print Screen",*/ "Ctrl Right",	/*"Win left",		"Win Right", *///17
//            "Alt Right",	"Menu",			"Up",		"Down",	"Right", //18
//            "Left",	"Insert",		"Delete",		"Home",			"End", //19
//            "Page Up",		"Page Down"//"Pad Enter",	"Pad /" //20
//        };
//    static final int[] STANDARD_KEY_VALUES = new int[]{
//            KeyEvent.KEYCODE_SPACE, KeyEvent.KEYCODE_SPACE/*  "'"*/ , KeyEvent.KEYCODE_COMMA ,KeyEvent.KEYCODE_MINUS ,KeyEvent.KEYCODE_PERIOD , //0
//            KeyEvent.KEYCODE_SLASH, KeyEvent.KEYCODE_0 , KeyEvent.KEYCODE_1 ,KeyEvent.KEYCODE_2 ,KeyEvent.KEYCODE_3,  //1
//            KeyEvent.KEYCODE_4    , KeyEvent.KEYCODE_5 ,KeyEvent.KEYCODE_6 ,KeyEvent.KEYCODE_7  ,KeyEvent.KEYCODE_8,  //2
//            KeyEvent.KEYCODE_9    , KeyEvent.KEYCODE_SEMICOLON ,KeyEvent.KEYCODE_EQUALS ,KeyEvent.KEYCODE_LEFT_BRACKET ,KeyEvent.KEYCODE_BACKSLASH, //3
//            KeyEvent.KEYCODE_RIGHT_BRACKET,             KeyEvent.KEYCODE_SPACE/* "`"*/ , KeyEvent.KEYCODE_A ,  KeyEvent.KEYCODE_B ,KeyEvent.KEYCODE_C,  //4
//            KeyEvent.KEYCODE_D    , KeyEvent.KEYCODE_E ,KeyEvent.KEYCODE_F ,KeyEvent.KEYCODE_G ,KeyEvent.KEYCODE_H,  //5
//            KeyEvent.KEYCODE_I    , KeyEvent.KEYCODE_J ,KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L ,KeyEvent.KEYCODE_M,  //6
//            KeyEvent.KEYCODE_N    , KeyEvent.KEYCODE_O ,KeyEvent.KEYCODE_P ,KeyEvent.KEYCODE_Q ,KeyEvent.KEYCODE_R,  //7
//            KeyEvent.KEYCODE_S    , KeyEvent.KEYCODE_T ,KeyEvent.KEYCODE_U ,KeyEvent.KEYCODE_V ,KeyEvent.KEYCODE_W,  //8
//            KeyEvent.KEYCODE_X    , KeyEvent.KEYCODE_Y ,KeyEvent.KEYCODE_Z ,KeyEvent.KEYCODE_CTRL_LEFT,KeyEvent.KEYCODE_SCROLL_LOCK, //9
//            KeyEvent.KEYCODE_F1   , KeyEvent.KEYCODE_F2,KeyEvent.KEYCODE_F3,KeyEvent.KEYCODE_F4,KeyEvent.KEYCODE_F5, //10
//            KeyEvent.KEYCODE_F6   , KeyEvent.KEYCODE_F7,   KeyEvent.KEYCODE_F8,  KeyEvent.KEYCODE_F9,  KeyEvent.KEYCODE_F10, //11
//            KeyEvent.KEYCODE_F11  , KeyEvent.KEYCODE_F12,  KeyEvent.KEYCODE_BACK,KeyEvent.KEYCODE_TAB,KeyEvent.KEYCODE_ENTER, //12
//            KeyEvent.KEYCODE_CAPS_LOCK,KeyEvent.KEYCODE_ESCAPE,KeyEvent.KEYCODE_ALT_LEFT,KeyEvent.KEYCODE_SHIFT_LEFT,KeyEvent.KEYCODE_SHIFT_RIGHT, //13
//            KeyEvent.KEYCODE_NUM_LOCK,KeyEvent.KEYCODE_NUMPAD_MULTIPLY,	KeyEvent.KEYCODE_NUMPAD_SUBTRACT,KeyEvent.KEYCODE_NUMPAD_ADD,		//"Pad 5",  //14
//            //"Pad 8 (^)",	"Pad 2 (Arrow Down)",	"Pad 6 ( -> )",  "Pad 4 ( <- )",  "Pad 0 (Ins)", //15
//            //"Pad . (Del)",	"Pad 7 (Home)", "Pad 1 (End)",  "Pad 9 (PgUp)", "Pad 3 (PgDn)", //16
//            /*"Pause",		"Print Screen",*/KeyEvent.KEYCODE_CTRL_RIGHT,/*	"Win left",		"Win Right",*/ //17
//            KeyEvent.KEYCODE_ALT_RIGHT,KeyEvent.KEYCODE_MENU,	KeyEvent.KEYCODE_DPAD_UP,	KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT, //18
//            KeyEvent.KEYCODE_DPAD_LEFT,KeyEvent.KEYCODE_INSERT,KeyEvent.KEYCODE_DEL,KeyEvent.KEYCODE_MOVE_HOME,KeyEvent.KEYCODE_MOVE_END, //19
//            KeyEvent.KEYCODE_PAGE_UP,KeyEvent.KEYCODE_PAGE_DOWN //"Pad Enter",	"Pad /" //20
//    };

}
