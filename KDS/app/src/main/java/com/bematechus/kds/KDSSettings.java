package com.bematechus.kds;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSToast;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.KDSXML;
import com.bematechus.kdslib.SettingsBase;
import com.bematechus.kdslib.TimeDog;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/8/24 0024.
 */
public class KDSSettings extends SettingsBase {
    static final private String TAG = "KDSSEttings";

    static final public String SETTINGS_FILE_NAME = "settings.xml";

    static final public String SETTINGS_VERSION ="1.0";

    private Drawable m_itemBumpedInOthersImage = null;
    private Drawable m_itemFocusImage = null;//for item focus, it is not load from configurations
    private Drawable m_itemBumpedImage = null;
    private Drawable m_itemMoreImage = null;
    private Drawable m_itemVoidByXmlCommand = null;
    private Drawable m_itemChangedImage = null;
    private Drawable m_orderCookStartedImage = null;

    //2.0.14
    private Drawable m_itemExpoPartialImage = null;

    KDSViewFontFace m_kdsBlockFont = null;

    StationFunc m_nStationFunc = StationFunc.Normal; //this comes from stations relation setting.

    StationFunc m_nTabCurrentFuncView = StationFunc.MAX_COUNT; //this is tab display, default is unknown. User which view to show order.
    String m_tabDestinationFilter = "";
    boolean m_bTabEnableLineItemsView = false;
    OrdersSort m_currentOrdersSort = OrdersSort.Manually;



    static final public int UDP_ANNOUNCER_PORT = 5000;
    static final public int UDP_ROUTER_ANNOUNCER_PORT = 5001; //write data to router. Use two port is for KDS and KDSRouter running in same station.
    static final public int UDP_STATISTIC_ANNOUNCER_PORT = 5002; //write data to statistic app.
    static final public int COMDIMENT_LEADING_POSITION  = 10;

    static final public int DEFAULT_BLOCK_BORDER_INSET = 7;
    static final public int DEFAULT_BLOCK_INSET = 5;

    static final public String TRACKER_AUTHEN_KEY = "tracker_authen";

    /****************************************************************************
     * >>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<<<<
     * Please add new setting id to the end of it.
     *  Otherwise, the old version will messed
     */
    public enum ID
    {
        NULL,                   //0

        //general settings
        KDS_ID,
        KDS_Function,
        KDS_Data_Source,
        KDS_Data_TCP_Port, // 4
        KDS_Data_Folder, //5
        KDS_Station_Port,
        //
        View_Margin,
        Panels_Layout_Format,
        Panels_View_BG,
        //Panels_Default_FontFace,
        Panels_BG, //10
        Panels_Row_Height,
       // View_Panel_FontFace,
        Panels_Show_Number,
        Panels_Panel_Number_Base,
        Panels_Panel_Number_BGFG,
        //Panels_Panel_Number_BG,
        //Panels_Panel_Number_FG,
        Panels_Blocks_Rows, // 15
        Panels_Blocks_Cols,
        Panels_Block_Zone_Gap, //the gap between zone and zone. title and data rows.
        Panels_Block_Border_Inset, //for draw borders.
        Panels_Block_Inset, //for gap between blocks.
        Panels_Block_Border_Color, // 20

        Order_Title_Rows,
        Order_Footer_Rows,

        Order_Normal_FontFace,
        //Order_Focused_FontFace,

        Order_Title0_Content_Left,
        Order_Title0_Content_Center,//25
        Order_Title0_Content_Right,
        Order_Title1_Content_Left,
        Order_Title1_Content_Center,
        Order_Title1_Content_Right,
        Order_Footer_Content_Left, // 30
        Order_Footer_Content_Center,
        Order_Footer_Content_Right,
        Order_Footer_FontFace,
        Order_Timer_Stage0_Time,
        Order_Timer_Stage0_Color,//35
        Order_Timer_Stage1_Time,
        Order_Timer_Stage1_Color,
        Order_Timer_Stage2_Time,
        Order_Timer_Stage2_Color,
        // Items showing settings
        Item_Consolidate, // 40
        Item_Default_FontFace,
        //Item_Default_BG,
        //Item_Default_FG,
        Item_Focused_Showing_Method,//unused
        Item_Focused_FontFace,  //unused
        Item_Focused_Mark, //unused
        Item_Bumped_Mark,//45 //unused
        Item_Exp_Bumped_In_Others,//unused
        Item_mark_with_char, //unused
        //condiments settings,
        Condiment_Default_FontFace,
        Condiment_Starting_Position,
        //
        Message_Default_FontFace, // 50
        //bump bar
        Bumpbar_OK,
        Bumpbar_Cancel,
        Bumpbar_Kbd_Type,
        Bumpbar_Next,
        Bumpbar_Prev,//55
        Bumpbar_Up,
        Bumpbar_Down,
        Bumpbar_Bump,
        Bumpbar_Unbump,
        Bumpbar_Sum, // 60
        Bumpbar_Transfer,
        Bumpbar_Sort,
        Bumpbar_Park,
        Bumpbar_Unpark,
        Bumpbar_More,//65
        Bumpbar_Print,
        Bumpbar_BuildCard,
        Bumpbar_Training,
        Bumpbar_Panelnum_Focus,
        //user B bumpbar
 //       Bumpbar_UserB_Enabled,
        Bumpbar_Switch_User,// 70
        Bumpbar_Menu,

        Bumpbar_Focus_0, //panel number focus
        Bumpbar_Focus_1,
        Bumpbar_Focus_2,//74, 75
        Bumpbar_Focus_3,
        Bumpbar_Focus_4,
        Bumpbar_Focus_5,
        Bumpbar_Focus_6,
        Bumpbar_Focus_7, //79, 80
        Bumpbar_Focus_8,
        Bumpbar_Focus_9,
        //
        Users_Mode, //single, or multiple
        //printer
        Printer_Enabled,
        Printer_Type,//84, 85
        Printer_Port,
        Printer_ip,
        Printer_ipport,
        Printer_serial,
        Printer_baudrate, //89,  90
        Printer_width,
        Printer_copies,
        Printer_template,
        Printer_codepage,
        Printer_howtoprint,//94, 95
        //for smart order
        Smart_Order_Enabled, //unused
        Smart_Order_Showing,
        //beeper
        Beeper_Enabled,
        Beeper_Type,
        //highlight
        Highlight_rush_enabled, //99,  100
        Highlight_rush_bgfg,
        //Highlight_rush_fg,
        Highlight_fire_enabled,
        Highlight_fire_bgfg,
        //Highlight_fire_fg,
        Highlight_dest_enabled,
        Highlight_dest_bgfg, //104, 105
        //Highlight_dest_fg,
        Bumping_Days,//unused
        Exp_Alert_Enabled,
        Exp_Alert_Change_Whole_Panel_Color,
        Exp_Alert_Color_BG,
        Sum_Type, //109,  110
        Information_List_Enabled,
        Order_Sort,
        //
        Touch_fontface,
        Touch_next,
        Touch_prev,//114, 115
        Touch_up,
        Touch_down,
        Touch_bump,
        Touch_unbump,
        Touch_sum, //119,  120
        Touch_transfer,
        Touch_sort,
        Touch_park,
        Touch_unpark,
        Touch_print,//124, 125
        Touch_more,
        Touch_BuildCard,
        Touch_Training,
        Touch_test,
        //Touch_page, // 130 //************ Different with release 1.0

        Screen_title_fontface,
        Sum_position, //130
        Sum_order_by,

        Bumping_confirm,
        Focused_BG, //the color for focus //133, 135
        Screen_Show_Time,
        Bumping_PanelNum_Mode,
        From_primary_text,
        From_primary_font, //

        Hide_navigation_bar,//138, 140
        Settings_password_enabled,
        Settings_password,

        Message_item_above,
        Message_order_bottom,
        Item_showing_method,//20160706, //on the fly /One item behind / when order is paid.//143, 145
        POS_notification_enabled, //20160711
        Item_void_mark_with_char, //unused
        Item_Changed_mark_with_char, //unused

        Media_auto_slip_interval, //147,  149
        Media_default_vol,//148, 150
        Touch_unbump_last,//unbump last order
        Bumpbar_Unbump_Last,
        Auto_bump_enabled,
        Auto_bump_minutes,//152

        //1.0 release end ************************************************* Move this two settings to here **************
        Bumpbar_Page,
        Touch_page,
        //*******************************************************************
        //from here, all will same as new
        Orders_sort_rush_front,// 155

        //Queue(speed of service)
        Queue_cols,
        Queue_panel_height,
        Queue_show_order_ID, //  158
        Queue_order_ID_font,
        Queue_show_customer_name,// 160
        Queue_customer_name_font,
        Queue_show_order_timer,
        Queue_order_timer_font,
        Queue_show_custom_message,
        Queue_custom_message_font,// 165
        Queue_order_received_font,
        Queue_order_preparation_font,
        Queue_order_ready_font, //  168
        Queue_move_ready_to_front,
        Queue_flash_ready_order,// 170
        Queue_more_orders_message,

        Queue_double_bump_expo_order,

        Queue_order_pickup_font,
        Queue_view_bg,
        Queue_received_status,// 175
        Queue_preparation_status,
        Queue_ready_status,
        Queue_pickup_status, //178
        Queue_show_finished_at_right,
        Queue_title,// 180
        Queue_panel_ratio_percent,

        Queue_mode,
        Queue_simple_show_received_col,
        Queue_simple_show_preparation_col,
        Queue_simple_show_ready_col,// 185
        Queue_simple_show_pickup_col,
        Queue_auto_switch_duration,
        Queue_separator_color, // 188

        Sound_enabled,
        Sound_duration,// 190
        Sound_bump_order,
        Sound_new_order,
        Sound_unbump_order,
        Sound_modify_order,
        Sound_transfer_order, //- Alert when an order is received from another station bumped with “Transfer bump”.// 195
        Sound_backup_station_activation,
        Sound_backup_station_orders_received,
        Sound_timer_alert1,//When an order change timer alert status .  A different alert can be selected for each timer alert level.
        Sound_timer_alert2, //  199
        Sound_timer_alert3, // 200
        Sound_expo_order_complete,

        //tracker
        //Tracker_enabled,
//        Tracker_IP,
        Tracker_title,
        Tracker_viewer_bg,
        Tracker_viewer_cols,
        Tracker_cell_height,// 205
        Tracker_auto_switch_duration,
        Tracker_order_name_font,
        Tracker_table_name_font, //  208
        Tracker_more_orders_message,

        Tracker_use_userinfo,// 210
        Tracker_holder_map,
        Tracker_show_order_id,
        Tracker_show_timer,
        Tracker_order_timer_font,
        Tracker_authen, //save authen in this field.// 215
        Tracker_auto_assign_timeout,
        Tracker_enable_auto_bump,
        Tracker_timeout_auto_remove_after_expo, // 218
        Tracker_timeout_alert_not_bump,
        Tracker_alert_font,// 220

        Pager_enabled,
        Pager_use_userinfo,
        Pager_delay,

        Language, // 224
        Bump_Max_Reserved_Count,

        Enable_auto_backup,
        Auto_backup_hours,
        Bumpbar_QExpo_Pickup,
        Bumpbar_QExpo_Unpickup,

        Tracker_use_userinfo_guesttable,//
        Pager_use_userinfo_guesttable,

        Order_Timer_Stage0_Enabled,
        Order_Timer_Stage1_Enabled,
        Order_Timer_Stage2_Enabled,

        Tracker_reverse_color_ttid_empty,
        Tracker_show_tracker_id,
        Tracker_tracker_id_font,
        Tracker_enable_auto_assign_id,

        Screens_orientation,

        General_customized_title,

        General_screens_ratio,

        Screen_subtitle_a_text,
        Screen_subtitle_b_text,
        Screen_subtitle_font,

        //screen 1

        Screen1_Panels_Layout_Format,
        Screen1_Panels_Blocks_Rows, // 15
        Screen1_Panels_Blocks_Cols,

        Queue_combine_status1_to,
        Queue_combine_status2_to,
        Queue_combine_status3_to,
        Queue_combine_status4_to,

        LineItems_Enabled,
        LineItems_font,
        LineItems_caption_text,
        LineItems_caption_font,
        LineItems_cols,
        LineItems_col0_text,
        //LineItems_col0_size,
        LineItems_col0_content,
        LineItems_col1_text,
        //LineItems_col1_size,
        LineItems_col1_content,
        LineItems_col2_text,
        //LineItems_col2_size,
        LineItems_col2_content,
        LineItems_col3_text,
        //LineItems_col3_size,
        LineItems_col3_content,
        LineItems_col4_text,
        //LineItems_col4_size,
        LineItems_col4_content,
        LineItems_col5_text,
        //LineItems_col5_size,
        LineItems_col5_content,

        AdvSum_enabled,
        AdvSum_always_visible,
        AdvSum_items,

        Sum_bgfg,
        AdvSum_rows,
        AdvSum_cols,

        //tab display
        Tab_Enabled,
        Tab_buttons,
        Tab_destinations,
        Bumpbar_tab_next,
        Tab_bgfg,

        Statistic_keep_days,

        Icon_enabled, //unused
        Icon_0,
        Icon_1,
        Icon_2,
        Icon_3,
        Icon_4,
        Icon_5,
        Icon_6,
        Icon_7,
        Icon_8,
        Icon_9,
        Icon_10,
        Icon_11,
        Icon_12,
        Icon_13,
        Icon_14,
        Icon_15,
        Icon_16,
        Icon_17,
        Icon_18,
        Icon_19,
        Icon_20,
        Icon_21,
        Icon_22,
        Icon_23,
        Icon_24,
        Icon_25,
        Icon_26,
        Icon_27,
        Icon_28,
        Icon_29,
        Icon_30,
        Icon_31,
        Icon_32,
        Icon_folder_enabled,
        Icon_folder,

        Item_mark_focused,
        Item_mark_local_bumped,
        Item_mark_station_bumped,
        Item_mark_del_by_xml,
        Item_mark_qty_changed,

        Auto_bump_park_order_mins,

        Void_showing_method,
        //Void_item_change_color_enabled,
        //Void_item_color,
        Void_add_message_enabled,
        Void_add_message,
        Void_qty_line_color_enabled,
        Void_qty_line_color,
        Void_qty_line_mark,

        //notification
        Notification_order_status,
        Notification_item_qty,

        //blinking focus
        Blink_focus,
        //Tab display sort mode view
        Tab_sort_modes,

        //preparation time mode
        Prep_mode_enabled, //unused
        Smart_mode,//unused

        //for log
        Log_mode,
        Log_days,
        Text_wrap,
        LineItem_sort_smart,
        //
        Smart_timer_from_item_visible,//count the timer from item visible.
        Lineitems_modifier_condiment_each_line,//each condiment/modifier go to next line

        Queue_order_id_length,

        Orders_sort_finished_front,// 2.0.14
        Item_mark_expo_partial_bumped, //2.0.14

        //2.0.25
        Bumping_expo_confirmation,
        Touch_prev_page,
        Touch_next_page,
        //2.0.25
        Show_avg_prep_time,
        Avg_prep_period,

        //2.0.36
        Confirm_bump_unpaid,
        Confirm_bump_outstanding,
        //2.0.36
        Queue_status1_sort_mode,
        Queue_status2_sort_mode,
        Queue_status3_sort_mode,
        Queue_status4_sort_mode,

        //2.0.39
        /*
        -          Add a new order acknowledgement feature;
        -          It reply back received xml back to the source, if it is shared folder then create a new folder for it; if it is TCP/IP, create a new port(can be change) which POS open and read back received xml;
        -          Additional to the xml, if the xml format is correct and kds is able to display, just send back the xml;
        -          But if the received xml is in wrong format which means kds is not able to read and display the order then append string Wrong_Orderxxxx.xml to the file name and for TCP add a new tag <Error> below <Transaction> to indicate this is a wrong format; So it looks like <Transaction><Error>1</Error>…</Transaction>. <Error>n<Error>, n = which type of error, we can put all into 1 right now. When we can identify which error it is, then we can add more types.
       */
        Notification_order_acknowledgement,

       //2.0.47
       Item_group_category,
       Enable_smbv2, //2.0.51
        SMS_enabled, //SMS feature , KPP1-15
        Queue_auto_bump_timeout,

    }
    /*
     * >>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<<<<
     * Please add new setting id to the end of it.
     *  Otherwise, the old version will messed
     ******************************************************************************/

    public enum ScreenOrientation
    {
        Left_Right,
        Up_Down,
    }

    public enum SumPosition
    {
        Side,
        Top,
    }

    public  enum TitleContents
    {
        NULL,
        Name,
        Timer,
        Waiter,
        ToTable,
        FromPOS,
        OrderType, //rush fired ...
        Destination,
        CustMessage,

        OrderStatus,
        OrderIcon,
    }

    public enum ComponentFocusedMethod
    {
        Change_BG,
        Change_FG,
        Change_BGFG,
        Add_Previous_String,
    };

    public enum LayoutFormat{
        Horizontal,
        Vertical
    };

    public enum TitlePosition
    {
        Left,
        Center,
        Right,
    }

    public enum KDSScreen
    {
        Screen_A,
        Screen_B
    }


    public enum KDSUserMode
    {
        Single,
        Multiple,
    }

    public enum SmartOrderShowing //how to show delay item/order.
    {
        Gray,
        Hide,
    }


    public enum SumType
    {
        ItemWithoutCondiments,
        ItemWithCondiments,
    }

    public enum SumOrderBy
    {
        Descend,
        Ascend,
    }
    public enum OrdersSort
    {
        Manually,
        Waiting_Time_Ascend,
        Waiting_Time_Decend,
        Order_Number_Ascend,
        Order_Number_Decend,
        Items_Count_Ascend,
        Items_Count_Decend,
        Preparation_Time_Ascend,
        Preparation_Time_Decend,
    }

    public enum BumpingByPanelNum
    {
        Disabled,
        Double_Click,
    }

    public enum ItemShowingMethod
    {
        On_the_fly,
        One_item_behind,
        When_order_is_paid
    }
 //2.0.30, fix this bug
    public enum OrderStatus
    {
        Unpaid,
        Paid,
        Inprogress,

    }

    public enum TimeAlertLevel
    {
        None,
        Alert1,
        Alert2,
        Alert3
    }

    public enum QueueMode
    {
        Panels,
        Simple,
    }

//    public enum Language
//    {
//        English,
//        Chinese,
//    }

    public enum Bump_Reserved_Count
    {
        R_0,
        R_10,
        R_20,
        R_30,
        R_40,
        R_50,
        R_60,
        R_70,
        R_80,
        R_90,
        R_100,

    }

    public enum TrackerPager_ID_From_Tag
    {
        None,
        UserInfo,
        GuestTable,
    }

    public enum ScreensRatio
    {
        R1d2,
        R1d3,
        R2d3,
        R1d4,
        R3d4,
    }

    public enum LineItemsContent
    {
        Order_ID,
        Order_status,
        Order_type,
        Order_table,
        Item_description,
        Condiments,
        Quantity,
        Waiting_time,
        Modifiers,
        Modifiers_and_Condiments
    }


    /**
     * Expeditor tabs - Orders, Destination, Order Queue, Tracker, Pager
     */
    enum TabFunction
    {
        Orders,
        Destination,
        Queue,//speed of service, or "Order Queue Display"
        TableTracker,
        LineItems,
        Sort_orders,
        MAX_COUNT,
    }

    public enum VoidShowingMethod
    {
        Direct_Qty,
        Add_void,
    }

    public enum VoidQtyChangeMark
    {
        Brackets,
        Negative_sign,
    }

//    public enum SmartMode
//    {
//        Disabled,
//        Normal,
//        Advanced,
//    }
    //all data saved in this buffer
    private HashMap<ID, Object> m_mapSettings = new HashMap<ID, Object>();

    //from ID to pref data
    private HashMap<ID, String> m_mapPrefID = new HashMap<ID,String>();
    /********************************************************************************************/
    public KDSSettings(Context context)
    {
        setDefaultValues(context);
        init_pref_map();
    }

    /**
     * the map for setting id to pref saved string
     * data type + preference key
     * >>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<<<
     *  The combo/list widget settings value will save to preference as "string"!!!
     *
     * The text input int value is string type!!!!
     */
    private void init_pref_map()
    {

        //general settings
        m_mapPrefID.put(ID.KDS_ID,"string_kds_general_id");
        m_mapPrefID.put(ID.KDS_Function,"string_kds_general_stationfunc");
        m_mapPrefID.put(ID.KDS_Data_Source,"string_kds_general_datasrc");
        m_mapPrefID.put(ID.KDS_Data_TCP_Port,"string_kds_general_tcpport");
        m_mapPrefID.put(ID.KDS_Data_Folder,"string_kds_general_remote_folder");
        m_mapPrefID.put(ID.KDS_Station_Port,"string_kds_general_stationsport");
        m_mapPrefID.put(ID.Users_Mode, "string_kds_general_users");
        //m_mapPrefID.put(ID.View_Margin,""); //use defualt
        m_mapPrefID.put(ID.Panels_Layout_Format, "string_panels_mode");
        m_mapPrefID.put(ID.Panels_View_BG, "int_viewer_bg");
        //m_mapPrefID.put(ID.Panels_Default_FontFace, "fontface_panels_font");
        m_mapPrefID.put(ID.Panels_BG,"int_panel_bg");
        m_mapPrefID.put(ID.Panels_Row_Height,"string_panel_text_line_height");//"int_panel_text_line_height");

        m_mapPrefID.put(ID.Panels_Show_Number, "bool_panels_show_number");
        m_mapPrefID.put(ID.Panels_Panel_Number_BGFG, "string_panelnum_bgfg");
        //m_mapPrefID.put(ID.Panels_Panel_Number_FG, "int_panelnum_fg");
        m_mapPrefID.put(ID.Panels_Panel_Number_Base,"string_panelnum_base");

        m_mapPrefID.put(ID.Panels_Blocks_Rows, "string_panels_rows");
        m_mapPrefID.put(ID.Panels_Blocks_Cols, "string_panels_cols");
        // m_mapPrefID.put(ID.View_Block_Zone_Gap,""); //the gap between zone and zone. title and data rows.
        m_mapPrefID.put(ID.Panels_Block_Border_Inset,"string_panel_border_width");//"int_panel_border_width"); //for draw borders.
        //m_mapPrefID.put(ID.View_Block_Inset, "");//for gap between blocks.
        m_mapPrefID.put( ID.Panels_Block_Border_Color,"int_block_border_bg");



        // m_mapPrefID.put(ID.Order_Title_Rows,"");
        // m_mapPrefID.put(ID.Order_Footer_Rows,"");
        m_mapPrefID.put(ID.Order_Normal_FontFace, "fontface_caption_normal");
        //m_mapPrefID.put(ID.Order_Focused_FontFace, "fontface_caption_focus");
        m_mapPrefID.put(ID.Order_Title0_Content_Left, "string_caption_left");
        m_mapPrefID.put(ID.Order_Title0_Content_Center, "string_caption_center");
        m_mapPrefID.put(ID.Order_Title0_Content_Right, "string_caption_right");
        m_mapPrefID.put(ID.Order_Title1_Content_Left, "string_caption2_left");
        m_mapPrefID.put(ID.Order_Title1_Content_Center, "string_caption2_center");
        m_mapPrefID.put(ID.Order_Title1_Content_Right, "string_caption2_right");
        m_mapPrefID.put(ID.Order_Footer_Content_Left, "string_footer_left");
        m_mapPrefID.put(ID.Order_Footer_Content_Center, "string_footer_center");
        m_mapPrefID.put(ID.Order_Footer_Content_Right, "string_footer_right");
        m_mapPrefID.put(ID.Order_Footer_FontFace, "fontface_footer_font");
        m_mapPrefID.put(ID.Order_Timer_Stage0_Time, "string_caption_stage0_time");
        m_mapPrefID.put(ID.Order_Timer_Stage0_Color, "int_caption_stage0_color");
        m_mapPrefID.put(ID.Order_Timer_Stage1_Time, "string_caption_stage1_time");
        m_mapPrefID.put(ID.Order_Timer_Stage1_Color, "int_caption_stage1_color");
        m_mapPrefID.put(ID.Order_Timer_Stage2_Time, "string_caption_stage2_time");
        m_mapPrefID.put(ID.Order_Timer_Stage2_Color, "int_caption_stage2_color");
        m_mapPrefID.put(ID.Order_Timer_Stage0_Enabled,"bool_caption_enable_stage0");
        m_mapPrefID.put(ID.Order_Timer_Stage1_Enabled,"bool_caption_enable_stage1");
        m_mapPrefID.put(ID.Order_Timer_Stage2_Enabled,"bool_caption_enable_stage2");

        // Items showing settings
        m_mapPrefID.put(ID.Item_Consolidate, "bool_item_consolidate");
        m_mapPrefID.put(ID.Item_Default_FontFace, "fontface_item_font");

        //m_mapPrefID.put(ID.Item_Focused_Showing_Method, "");
        //m_mapPrefID.put(ID.Item_Focused_FontFace, "");
        //m_mapPrefID.put(ID.Item_Focused_Mark, "string_item_focused_mark");

        //m_mapPrefID.put(ID.Item_Bumped_Mark, "string_item_bumped");
        //m_mapPrefID.put(ID.Item_Exp_Bumped_In_Others,"string_item_exp_bumped_in_others");
        //m_mapPrefID.put(ID.Item_mark_with_char,"bool_item_mark_with_char");
        m_mapPrefID.put(ID.Item_showing_method, "string_item_showing_method");//20160706


        //condiments settings,
        m_mapPrefID.put(ID.Condiment_Default_FontFace, "fontface_condiment_font");
        m_mapPrefID.put(ID.Condiment_Starting_Position, "string_condiment_start_position");

        m_mapPrefID.put(ID.Message_Default_FontFace, "fontface_message_font");

        m_mapPrefID.put(ID.Bumpbar_Kbd_Type, "string_bumpbar_type");
        m_mapPrefID.put(ID.Bumpbar_OK, "string_bumpbar_func_ok");
        m_mapPrefID.put(ID.Bumpbar_Cancel, "string_bumpbar_func_cancel");
        m_mapPrefID.put(ID.Bumpbar_Next, "string_bumpbar_func_next");
        m_mapPrefID.put(ID.Bumpbar_Prev, "string_bumpbar_func_prev");
        m_mapPrefID.put(ID.Bumpbar_Up, "string_bumpbar_func_up");
        m_mapPrefID.put(ID.Bumpbar_Down, "string_bumpbar_func_down");
        m_mapPrefID.put(ID.Bumpbar_Bump, "string_bumpbar_func_bump");
        m_mapPrefID.put(ID.Bumpbar_Unbump, "string_bumpbar_func_unbump");
        m_mapPrefID.put(ID.Bumpbar_Sum, "string_bumpbar_func_sum");
        m_mapPrefID.put(ID.Bumpbar_Transfer, "string_bumpbar_func_transfer");
        m_mapPrefID.put(ID.Bumpbar_Sort, "string_bumpbar_func_sort");
        m_mapPrefID.put(ID.Bumpbar_Park, "string_bumpbar_func_park");
        m_mapPrefID.put(ID.Bumpbar_Unpark, "string_bumpbar_func_unpark");
        m_mapPrefID.put(ID.Bumpbar_More, "string_bumpbar_func_more");
        m_mapPrefID.put(ID.Bumpbar_Print, "string_bumpbar_func_print");
        m_mapPrefID.put(ID.Bumpbar_BuildCard, "string_bumpbar_func_buildcard");
        m_mapPrefID.put(ID.Bumpbar_Training, "string_bumpbar_func_training");
        m_mapPrefID.put(ID.Bumpbar_Panelnum_Focus,"bool_bumpbar_panelnum_focus");
        m_mapPrefID.put(ID.Bumpbar_Unbump_Last, "string_bumpbar_func_unbumplast");
        m_mapPrefID.put(ID.Bumpbar_Page,"string_bumpbar_func_page");
        m_mapPrefID.put(ID.Bumpbar_Menu, ""); //fixed value

        ////////////////
        //user B

        m_mapPrefID.put(ID.Bumpbar_Switch_User,"string_bumpbar_switch_user");


        //

        //printer
        m_mapPrefID.put(ID.Printer_Enabled,"bool_printer_enabled");
        m_mapPrefID.put(ID.Printer_Type, "string_printer_type");
        m_mapPrefID.put(ID.Printer_Port, "string_printer_port");
        m_mapPrefID.put(ID.Printer_ip, "string_printer_ip");
        m_mapPrefID.put(ID.Printer_ipport, "string_printer_ipport");
        m_mapPrefID.put(ID.Printer_serial, "string_printer_serial");
        m_mapPrefID.put(ID.Printer_baudrate, "string_printer_baudrate");

        m_mapPrefID.put(ID.Printer_copies, "string_printer_copies");
        m_mapPrefID.put(ID.Printer_width, "string_printer_width");
        m_mapPrefID.put(ID.Printer_template, "string_printer_template");
        m_mapPrefID.put(ID.Printer_codepage, "string_printer_codepage");
        m_mapPrefID.put(ID.Printer_howtoprint, "string_printer_howtoprint");

        m_mapPrefID.put(ID.Smart_Order_Enabled,"bool_smartorder_enabled");
        m_mapPrefID.put(ID.Smart_Order_Showing,"string_smartorder_showing");

        //beep
        m_mapPrefID.put(ID.Beeper_Enabled,"bool_beeper_enabled");
        m_mapPrefID.put(ID.Beeper_Type,"string_beeper_type");

        //highlight
        m_mapPrefID.put(ID.Highlight_rush_enabled,"bool_hightlight_rush");
        m_mapPrefID.put(ID.Highlight_rush_bgfg,"string_rush_bgfg");
        //m_mapPrefID.put(ID.Highlight_rush_fg,"int_rush_fg");
        m_mapPrefID.put(ID.Highlight_fire_enabled,"bool_hightlight_fire");
        m_mapPrefID.put(ID.Highlight_fire_bgfg,"string_fire_bgfg");
        //m_mapPrefID.put(ID.Highlight_fire_fg,"int_fire_fg");
        m_mapPrefID.put(ID.Highlight_dest_enabled,"bool_hightlight_dest");
        m_mapPrefID.put(ID.Highlight_dest_bgfg,"string_dest_bgfg");
        //m_mapPrefID.put(ID.Highlight_dest_fg,"int_dest_fg");

        //m_mapPrefID.put(ID.Bumping_Days, "string_bumping_days");

        m_mapPrefID.put(ID.Exp_Alert_Enabled,"bool_exp_alert_enabled");
        m_mapPrefID.put(ID.Exp_Alert_Change_Whole_Panel_Color,"bool_exp_alert_change_panel");
        m_mapPrefID.put(ID.Exp_Alert_Color_BG,"int_exp_alert_color");

        m_mapPrefID.put(ID.Sum_Type, "string_sum_type");
        m_mapPrefID.put(ID.Information_List_Enabled, "bool_kds_general_information_enable");
        m_mapPrefID.put(ID.Order_Sort, "string_orders_sort_method");

        m_mapPrefID.put(ID.Touch_fontface,"fontface_touch_font");
        m_mapPrefID.put(ID.Touch_next,"bool_touch_next_enabled");
        m_mapPrefID.put(ID.Touch_prev,"bool_touch_prev_enabled");
        m_mapPrefID.put(ID.Touch_up,"bool_touch_up_enabled");
        m_mapPrefID.put(ID.Touch_down,"bool_touch_down_enabled");
        m_mapPrefID.put(ID.Touch_bump,"bool_touch_bump_enabled");
        m_mapPrefID.put(ID.Touch_unbump,"bool_touch_unbump_enabled");
        m_mapPrefID.put(ID.Touch_unbump_last,"bool_touch_unbumplast_enabled");
        m_mapPrefID.put(ID.Touch_sum,"bool_touch_sum_enabled");
        m_mapPrefID.put(ID.Touch_transfer,"bool_touch_transfer_enabled");
        m_mapPrefID.put(ID.Touch_sort,"bool_touch_sort_enabled");
        m_mapPrefID.put(ID.Touch_park,"bool_touch_park_enabled");
        m_mapPrefID.put(ID.Touch_unpark,"bool_touch_unpark_enabled");
        m_mapPrefID.put(ID.Touch_print,"bool_touch_print_enabled");
        m_mapPrefID.put(ID.Touch_more,"bool_touch_more_enabled");
        m_mapPrefID.put(ID.Touch_BuildCard,"bool_touch_buildcard_enabled");
        m_mapPrefID.put(ID.Touch_Training,"bool_touch_training_enabled");
        m_mapPrefID.put(ID.Touch_test,"bool_touch_test_enabled");
        m_mapPrefID.put(ID.Touch_page,"bool_touch_page_enabled");


        m_mapPrefID.put(ID.Screen_title_fontface, "fontface_screen_title_font");
        m_mapPrefID.put(ID.Sum_position, "string_sum_position");
        m_mapPrefID.put(ID.Sum_order_by, "string_sum_order_by");

        m_mapPrefID.put(ID.Bumping_confirm, "bool_bumping_confirm");
        m_mapPrefID.put(ID.Focused_BG, "int_focus_bg");
        m_mapPrefID.put(ID.Screen_Show_Time, "bool_screen_show_time");
        m_mapPrefID.put(ID.Bumping_PanelNum_Mode, "string_bumping_by_panelnumber");
        m_mapPrefID.put(ID.From_primary_text,"string_from_primary_text");
        m_mapPrefID.put(ID.From_primary_font,"fontface_from_primary_font");
        m_mapPrefID.put(ID.Hide_navigation_bar, "bool_hide_navigation_bar");

        m_mapPrefID.put(ID.Settings_password, "string_kds_general_password");
        m_mapPrefID.put(ID.Settings_password_enabled, "bool_kds_general_enable_password");
        m_mapPrefID.put(ID.Message_item_above, "bool_kds_item_premsg_above");
        m_mapPrefID.put(ID.Message_order_bottom, "bool_kds_order_premsg_bottom");

        m_mapPrefID.put(ID.POS_notification_enabled, "bool_kds_general_notification_enable");

        //m_mapPrefID.put(ID.Item_void_mark_with_char,"string_item_void");
        //m_mapPrefID.put(ID.Item_Changed_mark_with_char,"string_item_changed");
        m_mapPrefID.put(ID.Media_auto_slip_interval, "string_media_auto_delay");//"int_media_auto_delay");

        m_mapPrefID.put(ID.Media_default_vol, "string_media_default_vol");//"int_media_default_vol");
        m_mapPrefID.put(ID.Auto_bump_enabled,"bool_bump_enable_auto");
        m_mapPrefID.put(ID.Auto_bump_minutes,"string_bumping_auto_minutes");//"int_bumping_auto_minutes");
        m_mapPrefID.put(ID.Orders_sort_rush_front,"bool_orders_sort_rush_front");

        //Speed of service, Queue display
        m_mapPrefID.put(ID.Queue_cols,"string_queue_cols");
        m_mapPrefID.put(ID.Queue_panel_height,"string_queue_cell_height");
        m_mapPrefID.put(ID.Queue_show_order_ID,"bool_queue_show_order_id");
        m_mapPrefID.put(ID.Queue_order_ID_font,"fontface_queue_order_id_font");
        m_mapPrefID.put(ID.Queue_show_customer_name,"bool_queue_show_customer_name");
        m_mapPrefID.put(ID.Queue_customer_name_font,"fontface_queue_customer_name_font");
        m_mapPrefID.put(ID.Queue_show_order_timer,"bool_queue_show_order_timer");
        m_mapPrefID.put(ID.Queue_order_timer_font,"fontface_queue_order_timer_font");
        m_mapPrefID.put(ID.Queue_show_custom_message,"bool_queue_show_custom_message");
        m_mapPrefID.put(ID.Queue_custom_message_font,"fontface_queue_custome_message_font");

        m_mapPrefID.put(ID.Queue_order_received_font,"fontface_queue_order_received_font");
        m_mapPrefID.put(ID.Queue_order_preparation_font,"fontface_queue_order_preparation_font");
        m_mapPrefID.put(ID.Queue_order_ready_font,"fontface_queue_order_ready_font");

        m_mapPrefID.put(ID.Queue_move_ready_to_front,"bool_queue_move_ready_to_front");
        m_mapPrefID.put(ID.Queue_flash_ready_order,"bool_queue_flash_ready_order");
        m_mapPrefID.put(ID.Queue_more_orders_message,"string_queue_more_orders_message");

        m_mapPrefID.put(ID.Queue_double_bump_expo_order,"bool_bump_double_queue");

        m_mapPrefID.put(ID.Queue_order_pickup_font,"fontface_queue_order_pickup_font");
        m_mapPrefID.put(ID.Queue_view_bg,"int_queue_view_bg");

        m_mapPrefID.put(ID.Queue_received_status,"string_queue_order_received_status");
        m_mapPrefID.put(ID.Queue_preparation_status,"string_queue_order_preparation_status");
        m_mapPrefID.put(ID.Queue_ready_status,"string_queue_order_ready_status");
        m_mapPrefID.put(ID.Queue_pickup_status,"string_queue_order_pickup_status");

        m_mapPrefID.put(ID.Queue_show_finished_at_right, "bool_queue_show_finished_at_right");

        m_mapPrefID.put(ID.Queue_title, "string_queue_title");

        m_mapPrefID.put(ID.Queue_panel_ratio_percent, "string_queue_panel_ratio");
        m_mapPrefID.put(ID.Queue_mode,"string_queue_mode");
        m_mapPrefID.put(ID.Queue_simple_show_received_col,"bool_queue_simple_show_received_col");
        m_mapPrefID.put(ID.Queue_simple_show_preparation_col,"bool_queue_simple_show_preparation_col");
        m_mapPrefID.put(ID.Queue_simple_show_ready_col,"bool_queue_simple_show_ready_col");
        m_mapPrefID.put(ID.Queue_simple_show_pickup_col,"bool_queue_simple_show_pickup_col");
        m_mapPrefID.put(ID.Queue_auto_switch_duration, "string_queue_auto_switch_duration");
        m_mapPrefID.put(ID.Queue_separator_color, "int_queue_simple_separator_color");

        //sound
        m_mapPrefID.put(ID.Sound_enabled,"bool_sound_enable");
        m_mapPrefID.put(ID.Sound_duration,"string_sound_duration");

        m_mapPrefID.put(ID.Sound_bump_order,"string_sound_bump_order");
        m_mapPrefID.put(ID.Sound_new_order,"string_sound_new_order");

        m_mapPrefID.put(ID.Sound_unbump_order,"string_sound_unbump_order");
        m_mapPrefID.put(ID.Sound_modify_order,"string_sound_order_changed");
        m_mapPrefID.put(ID.Sound_transfer_order,"string_sound_transfer_order");
        m_mapPrefID.put(ID.Sound_backup_station_activation,"string_sound_backup_activation");
        m_mapPrefID.put(ID.Sound_backup_station_orders_received,"string_sound_backup_orders_received");
        m_mapPrefID.put(ID.Sound_timer_alert1,"string_sound_order_timer_alert1");
        m_mapPrefID.put(ID.Sound_timer_alert2,"string_sound_order_timer_alert2");
        m_mapPrefID.put(ID.Sound_timer_alert3,"string_sound_order_timer_alert3");
        m_mapPrefID.put(ID.Sound_expo_order_complete,"string_sound_expo_order_complete");


       // m_mapPrefID.put(ID.Tracker_enabled,"bool_tracker_enabled");
//        m_mapPrefID.put(ID.Tracker_IP,"string_tracker_server_ip");
        m_mapPrefID.put(ID.Tracker_title,"string_tracker_title");
        m_mapPrefID.put(ID.Tracker_use_userinfo,"bool_tracker_number_from_userinfo");
        m_mapPrefID.put(ID.Tracker_holder_map,"string_tracker_holder_map");

        m_mapPrefID.put(ID.Tracker_viewer_bg,"int_tracker_view_bg");
        m_mapPrefID.put(ID.Tracker_viewer_cols,"string_tracker_cols");
        m_mapPrefID.put(ID.Tracker_cell_height,"string_tracker_cell_height");
        m_mapPrefID.put(ID.Tracker_auto_switch_duration,"string_tracker_auto_switch_duration");
        m_mapPrefID.put(ID.Tracker_order_name_font,"fontface_tracker_order_name_font");
        m_mapPrefID.put(ID.Tracker_table_name_font,"fontface_tracker_table_name_font");
        m_mapPrefID.put(ID.Tracker_more_orders_message,"string_tracker_more_orders_message");

        m_mapPrefID.put(ID.Tracker_show_timer,"bool_tracker_show_order_timer");
        m_mapPrefID.put(ID.Tracker_order_timer_font,"fontface_tracker_order_timer_font");

        m_mapPrefID.put(ID.Tracker_authen,"string_"+TRACKER_AUTHEN_KEY);
        m_mapPrefID.put(ID.Tracker_auto_assign_timeout, "string_tracker_auto_assign_timeout");
        m_mapPrefID.put(ID.Tracker_timeout_auto_remove_after_expo, "string_tracker_auto_remove_after_expo_bump_timeout");
        m_mapPrefID.put(ID.Tracker_timeout_alert_not_bump,"string_tracker_alert_not_bump_timeout");
        m_mapPrefID.put(ID.Tracker_alert_font,"fontface_tracker_alert_font");
        m_mapPrefID.put(ID.Tracker_enable_auto_bump,"bool_tracker_enable_auto_bump");


        m_mapPrefID.put(ID.Pager_enabled,"bool_pager_enabled");
        m_mapPrefID.put(ID.Pager_use_userinfo,"bool_pager_id_from_userinfo");
        m_mapPrefID.put(ID.Pager_delay,"string_pager_delay");
        m_mapPrefID.put(ID.Language,"string_kds_general_language");
        m_mapPrefID.put(ID.Bump_Max_Reserved_Count,"string_bumping_max_count");

        m_mapPrefID.put(ID.Enable_auto_backup,"bool_kds_general_enable_auto_backup");
        m_mapPrefID.put(ID.Auto_backup_hours,"string_kds_general_auto_backup_hours");

        m_mapPrefID.put(ID.Bumpbar_QExpo_Pickup,"string_bumpbar_func_qexpo_ready");
        m_mapPrefID.put(ID.Bumpbar_QExpo_Unpickup,"string_bumpbar_func_qexpo_unready");
        m_mapPrefID.put(ID.Tracker_use_userinfo_guesttable,"string_tracker_number_from_userinfo_guesttable");
        m_mapPrefID.put(ID.Pager_use_userinfo_guesttable,"string_pager_number_from_userinfo_guesttable");

        m_mapPrefID.put(ID.Tracker_reverse_color_ttid_empty,"bool_tracker_reverse_color_ttid_empty");

        m_mapPrefID.put(ID.Tracker_show_tracker_id,"bool_tracker_show_assigned_tracker_id");
        m_mapPrefID.put(ID.Tracker_tracker_id_font,"fontface_tracker_tracker_id_font");
        m_mapPrefID.put(ID.Tracker_enable_auto_assign_id,"bool_tracker_enable_auto_assign_id");

        m_mapPrefID.put(ID.Screens_orientation,"string_kds_general_users_orientation");
        m_mapPrefID.put(ID.General_customized_title,"string_kds_general_title");

        m_mapPrefID.put(ID.General_screens_ratio,"string_kds_general_users_ratio");

        m_mapPrefID.put(ID.Screen_subtitle_a_text,"string_kds_general_subtitle_a_title");
        m_mapPrefID.put(ID.Screen_subtitle_b_text,"string_kds_general_subtitle_b_title");
        m_mapPrefID.put(ID.Screen_subtitle_font,"fontface_kds_general_subtitle_font");

       // m_mapPrefID.put(ID.Screen1_Focused_BG,"int_screenb_focus_bg");
        //m_mapPrefID.put(ID.Screen1_Panels_Block_Border_Color,"int_screenb_block_border_bg"); // 20
        //m_mapPrefID.put(ID.Screen1_Panels_View_BG, "int_screenb_viewer_bg");
        //m_mapPrefID.put(ID.Screen1_Panels_BG,"int_screenb_panel_bg"); //10
        //m_mapPrefID.put(ID.Screen1_Panels_Row_Height,"string_screenb_panel_text_line_height");
        m_mapPrefID.put(ID.Screen1_Panels_Layout_Format,"string_screenb_panels_mode");
        m_mapPrefID.put(ID.Screen1_Panels_Blocks_Rows,"string_screenb_panels_rows"); // 15
        m_mapPrefID.put(ID.Screen1_Panels_Blocks_Cols, "string_screenb_panels_cols");
        m_mapPrefID.put(ID.Queue_combine_status1_to,"string_queue_simple_combine_status1");
        m_mapPrefID.put(ID.Queue_combine_status2_to,"string_queue_simple_combine_status2");
        m_mapPrefID.put(ID.Queue_combine_status3_to,"string_queue_simple_combine_status3");
        m_mapPrefID.put(ID.Queue_combine_status4_to,"string_queue_simple_combine_status4");

        //Line items display mode
        m_mapPrefID.put(ID.LineItems_Enabled,"bool_lineitems_enabled");
        m_mapPrefID.put(ID.LineItems_font,"fontface_lineitems_default_font");
        m_mapPrefID.put(ID.LineItems_caption_text,"string_lineitems_caption_text");
        m_mapPrefID.put(ID.LineItems_caption_font,"fontface_lineitems_caption_font");
        m_mapPrefID.put(ID.LineItems_cols,"string_lineitems_cols_size");
        m_mapPrefID.put(ID.LineItems_col0_text,"string_lineitems_col0_text");
        //m_mapPrefID.put(ID.LineItems_col0_size,"string_lineitems_col0_size");
        m_mapPrefID.put(ID.LineItems_col0_content,"string_lineitems_col0_content");
        m_mapPrefID.put(ID.LineItems_col1_text,"string_lineitems_col1_text");
        //m_mapPrefID.put(ID.LineItems_col1_size,"string_lineitems_col1_size");
        m_mapPrefID.put(ID.LineItems_col1_content,"string_lineitems_col1_content");
        m_mapPrefID.put(ID.LineItems_col2_text,"string_lineitems_col2_text");
        //m_mapPrefID.put(ID.LineItems_col2_size,"string_lineitems_col2_size");
        m_mapPrefID.put(ID.LineItems_col2_content,"string_lineitems_col2_content");
        m_mapPrefID.put(ID.LineItems_col3_text,"string_lineitems_col3_text");
        //m_mapPrefID.put(ID.LineItems_col3_size,"string_lineitems_col3_size");
        m_mapPrefID.put(ID.LineItems_col3_content,"string_lineitems_col3_content");
        m_mapPrefID.put(ID.LineItems_col4_text,"string_lineitems_col4_text");
        //m_mapPrefID.put(ID.LineItems_col4_size,"string_lineitems_col4_size");
        m_mapPrefID.put(ID.LineItems_col4_content,"string_lineitems_col4_content");
        m_mapPrefID.put(ID.LineItems_col5_text,"string_lineitems_col5_text");
        //m_mapPrefID.put(ID.LineItems_col5_size,"string_lineitems_col5_size");
        m_mapPrefID.put(ID.LineItems_col5_content,"string_lineitems_col5_content");

        m_mapPrefID.put(ID.AdvSum_enabled,"bool_"+PreferenceFragmentAdvSum.ADVSUM_KEY_ENABLE);
        m_mapPrefID.put(ID.AdvSum_always_visible,"bool_"+PreferenceFragmentAdvSum.ADVSUM_KEY_SUM_ALWAYS);
        m_mapPrefID.put(ID.AdvSum_items,"string_"+PreferenceFragmentAdvSum.ADVSUM_KEY_ITEMS);

        m_mapPrefID.put(ID.Sum_bgfg,"string_sum_bgfg");
        m_mapPrefID.put(ID.AdvSum_rows,"string_advsum_rows");
        m_mapPrefID.put(ID.AdvSum_cols,"string_advsum_cols");

        m_mapPrefID.put(ID.Tab_Enabled,"bool_tabdisp_enabled");
        m_mapPrefID.put(ID.Tab_buttons,"string_tabdisp_buttons");
        m_mapPrefID.put(ID.Tab_destinations,"string_tabdisp_dest");

        m_mapPrefID.put(ID.Bumpbar_tab_next,"string_bumpbar_func_tab_next");
        m_mapPrefID.put(ID.Tab_bgfg,"string_tabdisp_bg");
        m_mapPrefID.put(ID.Statistic_keep_days,"string_statistic_db_keep");
        
        //m_mapPrefID.put(ID.Icon_enabled,"bool_icon_enabled");
        m_mapPrefID.put(ID.Icon_0,"string_icon_0");
        m_mapPrefID.put(ID.Icon_1,"string_icon_1");
        m_mapPrefID.put(ID.Icon_2,"string_icon_2");
        m_mapPrefID.put(ID.Icon_3,"string_icon_3");
        m_mapPrefID.put(ID.Icon_4,"string_icon_4");
        m_mapPrefID.put(ID.Icon_5,"string_icon_5");
        m_mapPrefID.put(ID.Icon_6,"string_icon_6");
        m_mapPrefID.put(ID.Icon_7,"string_icon_7");
        m_mapPrefID.put(ID.Icon_8,"string_icon_8");
        m_mapPrefID.put(ID.Icon_9,"string_icon_9");
        m_mapPrefID.put(ID.Icon_10,"string_icon_10");
        m_mapPrefID.put(ID.Icon_11,"string_icon_11");
        m_mapPrefID.put(ID.Icon_12,"string_icon_12");
        m_mapPrefID.put(ID.Icon_13,"string_icon_13");
        m_mapPrefID.put(ID.Icon_14,"string_icon_14");
        m_mapPrefID.put(ID.Icon_15,"string_icon_15");
        m_mapPrefID.put(ID.Icon_16,"string_icon_16");
        m_mapPrefID.put(ID.Icon_17,"string_icon_17");
        m_mapPrefID.put(ID.Icon_18,"string_icon_18");
        m_mapPrefID.put(ID.Icon_19,"string_icon_19");
        m_mapPrefID.put(ID.Icon_20,"string_icon_20");
        m_mapPrefID.put(ID.Icon_21,"string_icon_21");
        m_mapPrefID.put(ID.Icon_22,"string_icon_22");
        m_mapPrefID.put(ID.Icon_23,"string_icon_23");
        m_mapPrefID.put(ID.Icon_24,"string_icon_24");
        m_mapPrefID.put(ID.Icon_25,"string_icon_25");
        m_mapPrefID.put(ID.Icon_26,"string_icon_26");
        m_mapPrefID.put(ID.Icon_27,"string_icon_27");
        m_mapPrefID.put(ID.Icon_28,"string_icon_28");
        m_mapPrefID.put(ID.Icon_29,"string_icon_29");
        m_mapPrefID.put(ID.Icon_30,"string_icon_30");
        m_mapPrefID.put(ID.Icon_31,"string_icon_31");
        m_mapPrefID.put(ID.Icon_32,"string_icon_32");

        m_mapPrefID.put(ID.Icon_folder_enabled,"bool_icon_folder_enabled");
        m_mapPrefID.put(ID.Icon_folder,"string_icon_folder");

        m_mapPrefID.put(ID.Item_mark_focused,"string_item_mark_focused");
        m_mapPrefID.put(ID.Item_mark_local_bumped,"string_item_mark_local_bumped");
        m_mapPrefID.put(ID.Item_mark_station_bumped,"string_item_mark_station_bumped");
        m_mapPrefID.put(ID.Item_mark_del_by_xml,"string_item_mark_del_by_xml");
        m_mapPrefID.put(ID.Item_mark_qty_changed,"string_item_mark_qty_changed");


        m_mapPrefID.put(ID.Auto_bump_park_order_mins,"string_bumping_auto_park_minutes");


        m_mapPrefID.put(ID.Void_showing_method,"string_void_showing_method");
        //m_mapPrefID.put(ID.Void_item_change_color_enabled,"bool_void_item_color_enabled");
        //m_mapPrefID.put(ID.Void_item_color,"int_void_item_bgfg");
        m_mapPrefID.put(ID.Void_add_message_enabled,"bool_void_dcq_add_message_enabled");
        m_mapPrefID.put(ID.Void_add_message,"string_void_dcq_message");
        m_mapPrefID.put(ID.Void_qty_line_color_enabled,"bool_void_addline_line_color_enabled");
        m_mapPrefID.put(ID.Void_qty_line_color,"string_void_addline_line_bgfg");
        m_mapPrefID.put(ID.Void_qty_line_mark,"string_void_addline_qty_mark");

        m_mapPrefID.put(ID.Notification_order_status,"bool_notification_order_status_enabled");
        m_mapPrefID.put(ID.Notification_item_qty,"bool_notification_qty_change_enabled");

        m_mapPrefID.put(ID.Blink_focus,"bool_blink_focus");
        m_mapPrefID.put(ID.Tab_sort_modes,"string_tabdisp_sort");
        //m_mapPrefID.put(ID.Prep_mode_enabled,"bool_prepmode_enabled");

        //init_option(ID.Smart_mode,"string_smart_mode",KDSUtil.convertIntToString( SmartMode.Disabled.ordinal()));

        init_option(ID.Log_mode,"string_log_mode",KDSUtil.convertIntToString( KDSLog.LogLevel.Basic.ordinal()));
        init_option(ID.Log_days,"string_log_days", "3");
        m_mapPrefID.put(ID.Text_wrap,"bool_text_wrap");
        m_mapPrefID.put(ID.LineItem_sort_smart,"bool_lineitems_smart");

        m_mapPrefID.put(ID.Smart_timer_from_item_visible,"bool_smartorder_timer_from_item_visible");
        m_mapPrefID.put(ID.Lineitems_modifier_condiment_each_line,"bool_lineitems_modifier_condiment_each_line");

        m_mapPrefID.put(ID.Queue_order_id_length,"string_queue_order_id_length");

        //2.0.14
        m_mapPrefID.put(ID.Orders_sort_finished_front,"bool_orders_sort_finished_front");
        //2.0.14
        m_mapPrefID.put(ID.Item_mark_expo_partial_bumped,"string_item_mark_expo_partial_bumped");

        //2.0.25
        init_option(ID.Bumping_expo_confirmation,"bool_bumping_expo_confirmation", false);

        init_option(ID.Touch_prev_page,"bool_touch_prev_page_enabled", true);
        init_option(ID.Touch_next_page,"bool_touch_next_page_enabled", true);

       //2.0.25
        init_option(ID.Show_avg_prep_time,"bool_show_avg_prep_time", false);
        init_option(ID.Avg_prep_period,"string_real_time_period","60");//60secs

        //init_option(ID.Queue_sort_mode,"string_queue_sort","0");//60secs

        init_option(ID.Confirm_bump_unpaid,"bool_bumping_confirm_unpaid",false);//
        init_option(ID.Confirm_bump_outstanding,"bool_bumping_confirm_outstanding",false);//
        //2.0.36
        init_option(ID.Queue_status1_sort_mode,"string_queue_status1_sort","0");//60secs
        init_option(ID.Queue_status2_sort_mode,"string_queue_status2_sort","0");//60secs
        init_option(ID.Queue_status3_sort_mode,"string_queue_status3_sort","0");//60secs
        init_option(ID.Queue_status4_sort_mode,"string_queue_status4_sort","0");//60secs

        init_option(ID.Notification_order_acknowledgement,"bool_notification_order_acknowledgement",false);// see Notification_order_acknowledgement definition


        init_option(ID.Item_group_category,"bool_item_group_category",false);//

        init_option(ID.Enable_smbv2,"bool_general_enable_smbv2",false);//2.0.51

        //SMS
        init_option(ID.SMS_enabled,"bool_kds_general_sms_enable",false);//

        init_option(ID.Queue_auto_bump_timeout,"string_queue_auto_bump_timeout","0");//


    }

    /**
     *
     * @param id
     * @param tag
     *      The pref key, and data type.
     * @param defaultVal
     */
    public void init_option(ID id, String tag, Object defaultVal)
    {
        m_mapPrefID.put(id, tag);
        set(id, defaultVal);

    }
    Context m_contextTmp = null;
    int getResColor(  int nID)
    {
        return m_contextTmp.getResources().getColor(nID);
    }
    String getResString(int nResID)
    {
        return m_contextTmp.getResources().getString(nResID);
    }

    String getResBGFG(int nBGID, int nFGID)
    {
      int bg = getResColor(nBGID);
      int fg = getResColor(nFGID);
      String s = String.format("%d,%d", bg, fg);
      return s;
    }
    String buildBGFG(int nBGID, int fgColor)
    {
     int bg = getResColor(nBGID);
     int fg = fgColor;
     String s = String.format("%d,%d", bg, fg);
     return s;
    }
    /**
     *
     */
    private void setDefaultValues(Context context)
    {
        m_contextTmp = context;
        //general settings
        set(ID.KDS_ID, "");
        set(ID.KDS_Function, 0);
        set(ID.KDS_Data_Source, KDSDataSource.TCPIP.ordinal());
        set(ID.KDS_Data_TCP_Port, 3000);
        set(ID.KDS_Data_Folder, "");
        set(ID.KDS_Station_Port, 3001);
        set(ID.Users_Mode, "0"); //default single user

        set(ID.View_Margin, 5);
        set(ID.Panels_Layout_Format, LayoutFormat.Horizontal.ordinal());
        set(ID.Panels_View_BG, getResColor(R.color.view_bg));
        //KDSViewFontFace ff =  new KDSViewFontFace(getResColor(R.color.panel_bg), getResColor(R.color.panel_fg), KDSViewFontFace.DEFULT_FONT_FILE, 14);
        //set(ID.Panels_Default_FontFace, new KDSViewFontFace(getResColor(R.color.panel_bg), getResColor(R.color.panel_fg), KDSViewFontFace.DEFULT_FONT_FILE, 14));
        set(ID.Panels_BG, getResColor(R.color.panel_bg));
        set(ID.Panels_Row_Height, 14);

        set(ID.Panels_Show_Number, true);
        set(ID.Panels_Panel_Number_BGFG, buildBGFG(R.color.panelnum_bg, -1));// "-16769076,-1");// getResColor(R.color.panelnum_bg));
        //set(ID.Panels_Panel_Number_FG, getResColor(R.color.panelnum_fg));
        set(ID.Panels_Panel_Number_Base, "0");
        set(ID.Panels_Blocks_Rows, 2);
        set(ID.Panels_Blocks_Cols, 4);
        set(ID.Panels_Block_Zone_Gap, 0); //the gap between zone and zone. title and data rows.
        set(ID.Panels_Block_Border_Inset,DEFAULT_BLOCK_BORDER_INSET); //for draw borders.
        set(ID.Panels_Block_Inset,DEFAULT_BLOCK_INSET); //for gap between blocks.
        set(ID.Panels_Block_Border_Color,getResColor(R.color.border_bg));



        set(ID.Order_Title_Rows,1);
        set(ID.Order_Footer_Rows,0);
        set(ID.Order_Normal_FontFace, new KDSViewFontFace(getResColor(R.color.caption_bg), getResColor(R.color.caption_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
       // set(ID.Order_Focused_FontFace, new KDSViewFontFace(getResColor(R.color.focus_bg), getResColor(R.color.focus_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        //set(ID.Order_Focused_FontFace, new KDSViewFontFace(Color.YELLOW,Color.BLACK, KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.Order_Title0_Content_Left, TitleContents.Name.ordinal());
        set(ID.Order_Title0_Content_Center, TitleContents.ToTable.ordinal());
        set(ID.Order_Title0_Content_Right, TitleContents.Timer.ordinal());
        set(ID.Order_Title1_Content_Left, TitleContents.NULL.ordinal());
        set(ID.Order_Title1_Content_Center, TitleContents.NULL.ordinal());
        set(ID.Order_Title1_Content_Right, TitleContents.NULL.ordinal());
        set(ID.Order_Footer_Content_Left, TitleContents.NULL.ordinal());
        set(ID.Order_Footer_Content_Center, TitleContents.NULL.ordinal());
        set(ID.Order_Footer_Content_Right, TitleContents.NULL.ordinal());
        set(ID.Order_Footer_FontFace, new KDSViewFontFace(getResColor(R.color.footer_bg), getResColor(R.color.footer_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.Order_Timer_Stage0_Time, 1);
        set(ID.Order_Timer_Stage0_Color, getResColor(R.color.stage0_bg));
        set(ID.Order_Timer_Stage1_Time, 2);
        set(ID.Order_Timer_Stage1_Color, getResColor(R.color.stage1_bg));
        set(ID.Order_Timer_Stage2_Time, 3);
        set(ID.Order_Timer_Stage2_Color, getResColor(R.color.stage2_bg));
        set(ID.Order_Timer_Stage0_Enabled,true);
        set(ID.Order_Timer_Stage1_Enabled,true);
        set(ID.Order_Timer_Stage2_Enabled,true);

        // Items showing settings
        set(ID.Item_Consolidate, false);
        set(ID.Item_Default_FontFace, new KDSViewFontFace(getResColor(R.color.item_bg), getResColor(R.color.item_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));

        //set(ID.Item_Focused_Showing_Method, ComponentFocusedMethod.Add_Previous_String.ordinal());
        //set(ID.Item_Focused_FontFace, new KDSViewFontFace(Color.WHITE, Color.BLACK, KDSViewFontFace.DEFULT_FONT_FILE, 12));
        //set(ID.Item_Focused_Mark, ">");

        //set(ID.Item_Bumped_Mark, "*");
        //set(ID.Item_Exp_Bumped_In_Others, "#");
        //set(ID.Item_mark_with_char,false);
        set(ID.Item_showing_method, ItemShowingMethod.On_the_fly.ordinal()); //20160706
        //condiments
        set(ID.Condiment_Default_FontFace, new KDSViewFontFace(getResColor(R.color.condiment_bg), getResColor(R.color.condiment_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.Condiment_Starting_Position, KDSUtil.convertIntToString(COMDIMENT_LEADING_POSITION));
        set(ID.Message_Default_FontFace, new KDSViewFontFace(getResColor(R.color.premsg_bg), getResColor(R.color.premsg_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));

        set(ID.Bumpbar_Kbd_Type, "0");
        set(ID.Bumpbar_OK,          KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_1, false, false, false));// "8,0,0,0"); //'1' //format: key + alt+ctrl+shift
        set(ID.Bumpbar_Cancel,     KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_0, false, false, false));//"7,0,0,0"); //'0'
        set(ID.Bumpbar_Next,        KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_RIGHT, false, false, false));// "22,0,0,0"); //right
        set(ID.Bumpbar_Prev,        KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_LEFT, false, false, false));//"21,0,0,0"); //left
        set(ID.Bumpbar_Up,          KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_UP, false, false, false));//"19,0,0,0"); //up
        set(ID.Bumpbar_Down,        KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_DOWN, false, false, false));// "20,0,0,0"); //down
        set(ID.Bumpbar_Bump,        KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_ENTER, false, false, false));// "66,0,0,0"); //enter
        set(ID.Bumpbar_Unbump, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_CTRL_LEFT, false, false, false));// "113,0,0,0"); //ctrl
        set(ID.Bumpbar_Sum, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, false, false, false));// "156,0,0,0"); //-
        set(ID.Bumpbar_Transfer,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_SPACE, false, false, false));// "62,0,0,0"); //space
        set(ID.Bumpbar_Sort, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_1, false, true, false));//"8,0,1,0");//ctrl + 1
        set(ID.Bumpbar_Park,  KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_2, false, true, false));//"9,0,1,0");//ctrl+2
        set(ID.Bumpbar_Unpark,  KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_3, false, true, false));//"10,0,1,0");//ctrl+3
        set(ID.Bumpbar_Print, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_4, false, true, false));// "11,0,1,0"); //ctrl  + 4

        set(ID.Bumpbar_BuildCard,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_5, false, true, false));// "11,0,1,0"); //ctrl  + 5
        set(ID.Bumpbar_Training,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_6, false, true, false));// "11,0,1,0"); //ctrl  + 6
        set(ID.Bumpbar_Unbump_Last,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_7, false, true, false));// "11,0,1,0"); //ctrl  + 7
        set(ID.Bumpbar_Page,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_8, false, true, false));// "11,0,1,0"); //ctrl  + 8

        set(ID.Bumpbar_More,  KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_9, false, true, false));//"16,0,1,0"); //ctrl  + 9
        set(ID.Bumpbar_Switch_User, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_DOWN, false, true, false));// "20,0,1,0"); //ctrl+down
        set(ID.Bumpbar_Panelnum_Focus,false);



        set(ID.Bumpbar_Menu, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_ENTER, false, true, false));// "66,0,0,0"); //enter + ctrl call menu

        //printer
        set(ID.Printer_Enabled, false);
        set(ID.Printer_Type,"0");
        set(ID.Printer_Port,"0");
        set(ID.Printer_ip,"192.168.1.100");
        set(ID.Printer_ipport,"9100");
        set(ID.Printer_serial,"4");
        set(ID.Printer_baudrate, "1"); //19200
        set(ID.Printer_width,40);
        set(ID.Printer_copies, 1);
        set(ID.Printer_codepage, 0); //default cp437
        set(ID.Printer_howtoprint, "0");//manual print
        set(ID.Printer_template,getResString( R.string.pref_kds_printer_template_default));

        set(ID.Smart_Order_Enabled,false);
        set(ID.Smart_Order_Showing, 0);

        set(ID.Beeper_Enabled, false);
        set(ID.Beeper_Type, 0);

        //highlight
        set(ID.Highlight_rush_enabled, false);
        set(ID.Highlight_rush_bgfg,buildBGFG(R.color.rush_bg, 0));// "-39424,0");// getResColor(R.color.rush_bg));
        //set(ID.Highlight_rush_fg, getResColor(R.color.rush_fg));
        set(ID.Highlight_fire_enabled, false);
        set(ID.Highlight_fire_bgfg, buildBGFG(R.color.fire_bg, 0));// "-4521814,0");//getResColor(R.color.fire_bg));
        //set(ID.Highlight_fire_fg, getResColor(R.color.fire_fg));
        set(ID.Highlight_dest_enabled, false);
        set(ID.Highlight_dest_bgfg, buildBGFG(R.color.dest_bg, 0));// "-5636045,0");//getResColor(R.color.dest_bg));
        //set(ID.Highlight_dest_fg, getResColor(R.color.fire_fg));

        //set(ID.Bumping_Days, "3");

        set(ID.Exp_Alert_Enabled, false);
        set(ID.Exp_Alert_Change_Whole_Panel_Color, false);
        set(ID.Exp_Alert_Color_BG, getResColor(R.color.expalert_bg));

        set(ID.Sum_Type, "0");
        set(ID.Information_List_Enabled, false);
        set(ID.Order_Sort, "0");

        //KDSViewFontFace ff = new KDSViewFontFace(Color.WHITE.TRANSPARENT, Color.DKGRAY, KDSViewFontFace.DEFULT_FONT_FILE, 12);
        set(ID.Touch_fontface,new KDSViewFontFace(getResColor(R.color.touch_button_bg),getResColor(R.color.touch_button_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.Touch_next,true);
        set(ID.Touch_prev,true);
        set(ID.Touch_up,true);
        set(ID.Touch_down,true);
        set(ID.Touch_bump,true);
        set(ID.Touch_unbump,true);
        set(ID.Touch_unbump_last,true);
        set(ID.Touch_sum,true);
        set(ID.Touch_transfer,true);
        set(ID.Touch_sort,true);
        set(ID.Touch_park,true);
        set(ID.Touch_unpark,true);
        set(ID.Touch_print,true);
        set(ID.Touch_more,true);
        set(ID.Touch_BuildCard,true);
        set(ID.Touch_Training,true);
        set(ID.Touch_test, true);
        set(ID.Touch_page,true);
       // KDSViewFontFace ff =  new KDSViewFontFace( getResColor(R.color.kds_title_bg),getResColor( R.color.kds_title_fg), KDSViewFontFace.DEFULT_FONT_FILE, 14);
        set(ID.Screen_title_fontface, new KDSViewFontFace( getResColor(R.color.kds_title_bg),getResColor( R.color.kds_title_fg), KDSViewFontFace.DEFULT_FONT_FILE, 20));

        set(ID.Sum_position, "0");
        set(ID.Sum_order_by, "0");

        set(ID.Bumping_confirm, false);
        set(ID.Focused_BG, getResColor(R.color.focus_bg) );
        set(ID.Screen_Show_Time, true);

        set(ID.Bumping_PanelNum_Mode, "0");
        set(ID.From_primary_text, getResString(R.string.from_primary_default_text));
        set(ID.From_primary_font,new KDSViewFontFace(getResColor(R.color.item_bg), getResColor(R.color.item_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.Hide_navigation_bar, false);
        set(ID.Settings_password_enabled, false);
        set(ID.Settings_password, "");
        set(ID.Message_item_above, false);
        set(ID.Message_order_bottom, false);
        set(ID.POS_notification_enabled, true);//2.0.22, default should been true.

        //set(ID.Item_void_mark_with_char,"(x)");
        //set(ID.Item_Changed_mark_with_char,"(E)");
        set(ID.Media_auto_slip_interval, 5);
        set(ID.Media_default_vol, 0);

        set(ID.Auto_bump_enabled,false);
        set(ID.Auto_bump_minutes,60);
        set(ID.Orders_sort_rush_front,false);

        //Speed of service
        set(ID.Queue_cols,"3");
        set(ID.Queue_panel_height,"80");
        set(ID.Queue_show_order_ID,true);
        int nsmallsize = 14;
        int nlargesize = 20;
        set(ID.Queue_order_ID_font, new KDSViewFontFace( getResColor(R.color.queue_order_id_bg),getResColor( R.color.queue_order_id_fg), KDSViewFontFace.DEFULT_FONT_FILE,nlargesize ));
        set(ID.Queue_show_customer_name,false);
        set(ID.Queue_customer_name_font, new KDSViewFontFace( getResColor(R.color.queue_cusomer_name_bg),getResColor( R.color.queue_cusomer_name_fg), KDSViewFontFace.DEFULT_FONT_FILE, nsmallsize));
        set(ID.Queue_show_order_timer,false);
        set(ID.Queue_order_timer_font, new KDSViewFontFace( getResColor(R.color.queue_order_timer_bg),getResColor( R.color.queue_order_timer_fg), KDSViewFontFace.DEFULT_FONT_FILE, nsmallsize));
        set(ID.Queue_show_custom_message,false);
        set(ID.Queue_custom_message_font, new KDSViewFontFace( getResColor(R.color.queue_cusom_message_bg),getResColor( R.color.queue_cusom_message_fg), KDSViewFontFace.DEFULT_FONT_FILE, nsmallsize));

        set(ID.Queue_order_received_font, new KDSViewFontFace( getResColor(R.color.queue_order_status_received_bg),getResColor( R.color.queue_order_status_received_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));

        set(ID.Queue_order_preparation_font, new KDSViewFontFace( getResColor(R.color.queue_order_status_preparation_bg),getResColor( R.color.queue_order_status_preparation_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));
        set(ID.Queue_order_ready_font, new KDSViewFontFace( getResColor(R.color.queue_order_status_ready_bg),getResColor( R.color.queue_order_status_ready_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));

        set(ID.Queue_move_ready_to_front,true);
        set(ID.Queue_flash_ready_order,true);
        set(ID.Queue_more_orders_message,getResString(R.string.more_orders));

        set(ID.Queue_double_bump_expo_order,true);

        set(ID.Queue_order_pickup_font, new KDSViewFontFace( getResColor(R.color.queue_order_status_pickup_bg),getResColor( R.color.queue_order_status_pickup_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));

        set(ID.Queue_view_bg,getResColor( R.color.queue_view_bg));

        set(ID.Queue_received_status, getResString(R.string.queue_status_received));
        set(ID.Queue_preparation_status,getResString(R.string.queue_status_preparation));
        set(ID.Queue_ready_status,getResString(R.string.queue_status_ready));
        set(ID.Queue_pickup_status,getResString(R.string.queue_status_pickup));
        set(ID.Queue_show_finished_at_right, false);

        set(ID.Queue_title,getResString(R.string.queue_default_title));
        set(ID.Queue_panel_ratio_percent, "40");
        set(ID.Queue_auto_switch_duration, "5");

        //sound
        set(ID.Sound_enabled,false);
        set(ID.Sound_duration,"3");


        set(ID.Sound_bump_order,getResString(R.string.sound_default_bump_order) );
        set(ID.Sound_new_order,getResString(R.string.sound_default_new_order));
       set(ID.Sound_unbump_order,getResString(R.string.sound_default_unbump_order));
       set(ID.Sound_modify_order, getResString(R.string.sound_default_order_changed) );
        set(ID.Sound_transfer_order,getResString(R.string.sound_default_transfer_order));
        set(ID.Sound_backup_station_activation, getResString(R.string.sound_default_backup_activation) );
        set(ID.Sound_backup_station_orders_received, getResString(R.string.sound_default_backup_orders_received) );
        set(ID.Sound_timer_alert1,getResString(R.string.pref_kds_sound_order_timer_alert1) );
        set(ID.Sound_timer_alert2,getResString(R.string.sound_default_timer_alert2));
        set(ID.Sound_timer_alert3,getResString(R.string.pref_kds_sound_order_timer_alert3) );
        set(ID.Sound_expo_order_complete,getResString(R.string.pref_kds_sound_expo_order_complete) );

        set(ID.Queue_mode,"0"); //panles mode
        set(ID.Queue_simple_show_received_col,true);
        set(ID.Queue_simple_show_preparation_col,true);
        set(ID.Queue_simple_show_ready_col,true);
        set(ID.Queue_simple_show_pickup_col,true);
        set(ID.Queue_separator_color,getResColor( R.color.queue_separator_color));

        //set(ID.Tracker_enabled,false);
//        set(ID.Tracker_IP,"");
        set(ID.Tracker_title,getResString(R.string.tracker_default_title));
        set(ID.Tracker_use_userinfo,false);
        set(ID.Tracker_holder_map,"");
        set(ID.Tracker_viewer_bg,getResColor( R.color.tracker_view_bg));
        set(ID.Tracker_viewer_cols,"3");
        set(ID.Tracker_cell_height,"80");
        set(ID.Tracker_auto_switch_duration,"5");
        set(ID.Tracker_order_name_font, new KDSViewFontFace( getResColor(R.color.tracker_order_name_bg),getResColor( R.color.tracker_order_name_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));
        set(ID.Tracker_table_name_font,new KDSViewFontFace( getResColor(R.color.tracker_table_name_bg),getResColor( R.color.tracker_table_name_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));
        set(ID.Tracker_more_orders_message,getResString(R.string.more_orders));

        set(ID.Tracker_show_timer,true);
        set(ID.Tracker_order_timer_font,new KDSViewFontFace( getResColor(R.color.tracker_order_name_bg),getResColor( R.color.tracker_order_name_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));

        set(ID.Tracker_authen,"");
        set(ID.Tracker_auto_assign_timeout, "30");
        set(ID.Tracker_timeout_auto_remove_after_expo, "1");
        set(ID.Tracker_timeout_alert_not_bump,"30");

        set(ID.Tracker_alert_font,new KDSViewFontFace( getResColor(R.color.tracker_alert_bg),getResColor( R.color.tracker_alert_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));

        set(ID.Tracker_enable_auto_bump,true);

        set(ID.Pager_enabled,true);
        set(ID.Pager_use_userinfo,false);
        set(ID.Pager_delay,"0");

        set(ID.Language,"0");

        set(ID.Bump_Max_Reserved_Count, KDSUtil.convertIntToString(Bump_Reserved_Count.R_30.ordinal()));

        set(ID.Enable_auto_backup,false);
        set(ID.Auto_backup_hours,"12");

        set(ID.Bumpbar_QExpo_Pickup,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_DPAD_DOWN, false, false, false));// "20,0,0,0"); //down);
        set(ID.Bumpbar_QExpo_Unpickup,KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_NUMPAD_SUBTRACT, false, false, false));// "156,0,0,0"); //-

        set(ID.Tracker_use_userinfo_guesttable,KDSUtil.convertIntToString(TrackerPager_ID_From_Tag.None.ordinal())); //default none

        set(ID.Pager_use_userinfo_guesttable,KDSUtil.convertIntToString(TrackerPager_ID_From_Tag.None.ordinal())); //default none
        set(ID.Tracker_reverse_color_ttid_empty,true);

        set(ID.Tracker_show_tracker_id,true);
        set(ID.Tracker_tracker_id_font,new KDSViewFontFace( getResColor(R.color.tracker_order_name_bg),getResColor( R.color.tracker_order_name_fg), KDSViewFontFace.DEFULT_FONT_FILE, nlargesize));
        set(ID.Tracker_enable_auto_assign_id,false);

        set(ID.Screens_orientation,"0");

        set(ID.General_customized_title,"");
        set(ID.General_screens_ratio,"0");
        set(ID.Screen_subtitle_a_text,KDSApplication.getContext().getString(R.string.screen_a));
        set(ID.Screen_subtitle_b_text,KDSApplication.getContext().getString(R.string.screen_b));
        set(ID.Screen_subtitle_font, new KDSViewFontFace(getResColor(R.color.subtitle_bg), getResColor(R.color.subtitle_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));


        set(ID.Screen1_Panels_Layout_Format,LayoutFormat.Horizontal.ordinal());
        set(ID.Screen1_Panels_Blocks_Rows,2); // 15
        set(ID.Screen1_Panels_Blocks_Cols, 4);

        set(ID.Queue_combine_status1_to,"1");
        set(ID.Queue_combine_status2_to,"2");
        set(ID.Queue_combine_status3_to,"3");
        set(ID.Queue_combine_status4_to,"2");


        set(ID.LineItems_Enabled,false);
        set(ID.LineItems_font,new KDSViewFontFace(getResColor(R.color.lineitems_bg), getResColor(R.color.lineitems_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.LineItems_caption_text,getResString(R.string.line_items_display));
        set(ID.LineItems_caption_font,new KDSViewFontFace(getResColor(R.color.lineitems_bg), getResColor(R.color.lineitems_fg), KDSViewFontFace.DEFULT_FONT_FILE, 12));
        set(ID.LineItems_cols,"25,25,25,25");

        set(ID.LineItems_col0_text,getResString(R.string.str_id));
        //set(ID.LineItems_col0_size,10);
        set(ID.LineItems_col0_content,LineItemsContent.Order_ID.ordinal());

        set(ID.LineItems_col1_text, getResString(R.string.description));
        //set(ID.LineItems_col1_size,30);
        set(ID.LineItems_col1_content,LineItemsContent.Item_description.ordinal());

        set(ID.LineItems_col2_text,getResString(R.string.condiments));
        //set(ID.LineItems_col2_size,50);
        set(ID.LineItems_col2_content,LineItemsContent.Condiments.ordinal());

        set(ID.LineItems_col3_text,getResString(R.string.timer));
        //set(ID.LineItems_col3_size,10);
        set(ID.LineItems_col3_content,LineItemsContent.Waiting_time.ordinal());

        set(ID.LineItems_col4_text,"");
        //set(ID.LineItems_col4_size,10);
        set(ID.LineItems_col4_content,LineItemsContent.Item_description.ordinal());

        set(ID.LineItems_col5_text,"");
        //set(ID.LineItems_col5_size,10);
        set(ID.LineItems_col5_content,LineItemsContent.Item_description.ordinal());
        //advanced summary
        set(ID.AdvSum_enabled,false); //Please check the PreferenceFragmentAdvSum load function, it contain default value too
        set(ID.AdvSum_always_visible,false);
        set(ID.AdvSum_items,"");

        set(ID.Sum_bgfg,  "-5724506,-16777216");
        set(ID.AdvSum_rows,"4");
        set(ID.AdvSum_cols,"4");

        //tab display
        set(ID.Tab_Enabled,false);
        set(ID.Tab_buttons,"");
        set(ID.Tab_destinations,"");

        set(ID.Bumpbar_tab_next, KDSBumpBarKeyFunc.makeKeysString(KeyEvent.KEYCODE_0, false, true, false));

        set(ID.Tab_bgfg,"-1,-16777216"); //white, black
        set(ID.Statistic_keep_days,"180");

        //set(ID.Icon_enabled,false);
        set(ID.Icon_0,"");
        set(ID.Icon_1,"");
        set(ID.Icon_2,"");
        set(ID.Icon_3,"");
        set(ID.Icon_4,"");
        set(ID.Icon_5,"");
        set(ID.Icon_6,"");
        set(ID.Icon_7,"");
        set(ID.Icon_8,"");
        set(ID.Icon_9,"");
        set(ID.Icon_10,"");
        set(ID.Icon_11,"");
        set(ID.Icon_12,"");
        set(ID.Icon_13,"");
        set(ID.Icon_14,"");
        set(ID.Icon_15,"");
        set(ID.Icon_16,"");
        set(ID.Icon_17,"");
        set(ID.Icon_18,"");
        set(ID.Icon_19,"");
        set(ID.Icon_20,"");
        set(ID.Icon_21,"");
        set(ID.Icon_22,"");
        set(ID.Icon_23,"");
        set(ID.Icon_24,"");
        set(ID.Icon_25,"");
        set(ID.Icon_26,"");
        set(ID.Icon_27,"");
        set(ID.Icon_28,"");
        set(ID.Icon_29,"");
        set(ID.Icon_30,"");
        set(ID.Icon_31,"");
        set(ID.Icon_32,"");

        set(ID.Icon_folder_enabled,false);
        set(ID.Icon_folder,"");

        set(ID.Item_mark_focused,"0_1");//format_value, icon_1
        set(ID.Item_mark_local_bumped,"0_2");
        set(ID.Item_mark_station_bumped,"0_3");
        set(ID.Item_mark_del_by_xml,"0_4");
        set(ID.Item_mark_qty_changed,"0_5");
        set(ID.Item_mark_expo_partial_bumped,"0_6");

        set(ID.Auto_bump_park_order_mins,60);


        set(ID.Void_showing_method,"0");
        //set(ID.Void_item_change_color_enabled,false);
        //set(ID.Void_item_color,"-1,-16777216");
        set(ID.Void_add_message_enabled,false);
        set(ID.Void_add_message, getResString(R.string.qty_changed));// "Qty changed");
        set(ID.Void_qty_line_color_enabled,false);
        set(ID.Void_qty_line_color,"-1,-16777216");
        set(ID.Void_qty_line_mark,"0");

        set(ID.Notification_order_status,false);
        set(ID.Notification_item_qty,false);

        set(ID.Blink_focus,false);
        set(ID.Tab_sort_modes,"");
        set(ID.Text_wrap,false);
        set(ID.LineItem_sort_smart,false);
        set(ID.Smart_timer_from_item_visible,false);

        set(ID.Lineitems_modifier_condiment_each_line,false);

        set(ID.Queue_order_id_length,"0");

        //2.0.14
        set(ID.Orders_sort_finished_front,false);
        //set(ID.Prep_mode_enabled,false);

        //set(ID.Smart_mode,KDSUtil.convertIntToString( SmartMode.Disabled.ordinal()));
    }


    public void loadSettings(Context appContext)
    {
        Context c =appContext;// app.getApplicationContext();
//
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//
//        // SharedPreferences pre =  this.getSharedPreferences("P1", Activity.MODE_PRIVATE);
//        String str = pre.getString("kds_general_tcpport", "3000");
        try {
            for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

                ID id = entry.getKey();
                String tag = entry.getValue();
                if (tag == null) continue;
                Object objdef = this.get(id);
                if (tag.isEmpty()) continue;//fixed value
                Object obj = getPrefValue(pre, tag, objdef);
                if (obj != null)
                    this.set(id, obj);
                //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
            }
        }
        catch (Exception e)
        {
            KDSToast.showMessage(KDSApplication.getContext(), "Error while load settings, please open settings dialog");
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSLog._FUNCLINE_() + " Error while load settings");
        }

        //caption lines
        boolean bEnableCaption2 = pre.getBoolean("caption_enable_caption2", false);
        if (bEnableCaption2)
            set(ID.Order_Title_Rows, 2);
        else
            set(ID.Order_Title_Rows, 1);

        boolean bEnableFooter = pre.getBoolean("footer_enable", false);
        if (bEnableFooter)
            set(ID.Order_Footer_Rows, 1);
        else
            set(ID.Order_Footer_Rows, 0);

        m_itemFocusImage = appContext.getResources().getDrawable(R.drawable.item_focus);
        m_itemBumpedImage = appContext.getResources().getDrawable(R.drawable.item_bumped);
        m_itemBumpedInOthersImage = appContext.getResources().getDrawable(R.drawable.others_bumped);
        m_itemMoreImage = appContext.getResources().getDrawable(R.drawable.down18px);
        m_itemVoidByXmlCommand = appContext.getResources().getDrawable(R.drawable.delete_24px_32);
        m_itemChangedImage  = appContext.getResources().getDrawable(R.drawable.edit_24px_16);
        m_orderCookStartedImage = appContext.getResources().getDrawable(R.drawable.chef);

        //2.0.14
        m_itemExpoPartialImage = appContext.getResources().getDrawable(R.drawable.partial_bumped);


    }

    static String getOkKeyString(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        String str = pre.getString("bumpbar_func_ok", "8,0,0,0");
        String strKbdType = pre.getString("bumpbar_type", "0");
        int nKbdType = KDSUtil.convertStringToInt(strKbdType, 0);

        KDSBumpBarKeyFunc.KeyboardType kbdType = KDSBumpBarKeyFunc.KeyboardType.values()[nKbdType];

        KDSBumpBarKeyFunc bumpbarKey = KDSBumpBarKeyFunc.parseString(str);
        String strKey = bumpbarKey.getSummaryString(kbdType);
        return strKey;
    }

    static String getCancelKeyString(Context c)
    {
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
        String str = pre.getString("bumpbar_func_cancel", "7,0,0,0");
        int ntype = pre.getInt("bumpbar_type", 0);

        KDSBumpBarKeyFunc.KeyboardType kbdType = KDSBumpBarKeyFunc.KeyboardType.values()[ntype];

        KDSBumpBarKeyFunc bumpbarKey = KDSBumpBarKeyFunc.parseString(str);
        String strKey = bumpbarKey.getSummaryString(kbdType);
        return strKey;
    }

    public void set(ID config, Object obj)
    {
        m_mapSettings.put(config, obj);
    }
    public Object get(ID config)
    {
         return m_mapSettings.get(config);
    }
    public boolean getBoolean(ID config)
    {
        Object obj = get(config);
        if (obj instanceof String)
        {
            int n = KDSUtil.convertStringToInt((String)obj, 0);
            return (n==1);
        }
        else if (obj instanceof Boolean) {
            return (boolean) get(config);
        }
        else
            return false;
    }

    public int getInt(ID config)
    {
        Object obj = get(config);
        if (obj instanceof Integer)
            return (int)obj;
        else if (obj instanceof  String)
        {
            String s = (String)obj;
            if (s.isEmpty())
                return 0;
            try {
                return Integer.parseInt((String) obj);
            }
            catch (Exception e)
            {
                KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
                return 0;
            }
        }
        else if (obj instanceof TitleContents)
        {
            return ((TitleContents)obj).ordinal();
        }
        return 0;//(int)obj;
    }

    public String getString(ID config)
    {
        Object obj = get(config);
        if (obj instanceof String)
            return (String) get(config);
        else
            return "";
    }

    public KDSViewFontFace getKDSViewFontFace(ID config)
    {
        //String s = getString(config);
        //return KDSViewFontFace.parseString(s);
        Object obj = get(config);
        if (!(obj instanceof KDSViewFontFace))
            return new KDSViewFontFace();
        return (KDSViewFontFace) get(config);
    }

    public float getFloat(ID config)
    {
        //return (float) get(config);
        Object obj = get(config);
        if (obj instanceof Integer) {
            int n = (int)obj;
            float flt = n;
            return flt;
        }
        else if (obj instanceof  String)
        {
            String s = (String)obj;
            if (s.isEmpty())
                return 0;
            return Float.parseFloat((String )obj);
        }
        else if (obj instanceof Float)
        {
            return (float)obj;
        }

        return 0;//(int)obj;
    }


    public LayoutFormat getLayoutFormat(ID config)
    {
        //int n = this.getInt(config);
        int n = getEnumIndexValues(this,LayoutFormat.class, config );
        return LayoutFormat.values()[n];
        //return    (LayoutFormat) get(config);
    }

    public TitleContents getTitleContent(int nRow, TitlePosition position)
    {
        if (nRow == 0)
        {
            if (position == TitlePosition.Left) {
                //int nleft = this.getInt(ID.Order_Title0_Content_Left);
                return TitleContents.values()[this.getInt(ID.Order_Title0_Content_Left)];
            }
            else if (position == TitlePosition.Center)
                return TitleContents.values()[this.getInt(ID.Order_Title0_Content_Center)];
            else if (position == TitlePosition.Right)
                return TitleContents.values()[this.getInt(ID.Order_Title0_Content_Right)];

        }
        else if (nRow == 1)
        {
            if (position == TitlePosition.Left)
                return TitleContents.values()[this.getInt(ID.Order_Title1_Content_Left)];
            else if (position == TitlePosition.Center)
                return TitleContents.values()[this.getInt(ID.Order_Title1_Content_Center)];
            else if (position == TitlePosition.Right)
                return TitleContents.values()[this.getInt(ID.Order_Title1_Content_Right)];
        }
        return TitleContents.NULL;

    }

    public TitleContents getFooterContent( TitlePosition position)
    {

        if (position == TitlePosition.Left)
            return TitleContents.values()[this.getInt(ID.Order_Footer_Content_Left)];
        else if (position == TitlePosition.Center)
            return TitleContents.values()[this.getInt(ID.Order_Footer_Content_Center)];
        else if (position == TitlePosition.Right)
            return TitleContents.values()[this.getInt(ID.Order_Footer_Content_Right)];
        return TitleContents.NULL;
    }



    /**
     * Output all settings to a xml text string. For sending to others station.
     * Format:
     *  <Settings>
     *      <config id=## >val</>
     *      <config id=## >val</>
     *  <Settings/>
     * @return
     */
    public String outputXmlText(Context appContex)
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root("Settings");
        xml.newAttribute("ver", SETTINGS_VERSION);

        for (Map.Entry<ID, Object> entry : m_mapSettings.entrySet()) {

            ID id = entry.getKey();
            if (id == ID.Bumpbar_Menu) continue; //fixed value.
            Object obj = entry.getValue();
            String strid = KDSUtil.convertIntToString(id.ordinal());
            String strVal = convertConfigValToString(obj);
            String strType = getConfigValTypeStrng(obj);
            xml.newGroup("config", true);
            xml.newAttribute("id", strid);
            xml.newAttribute("ty", strType);
            xml.setGroupValue(strVal);
            xml.back_to_parent();

        }

        xml.newGroup("StationsRelation", true);
        String strRelations = loadStationsRelationString(appContex, true);
        xml.setGroupValue(strRelations);
        xml.back_to_parent();
        return xml.get_xml_string();


    }

//    private Object convertConfigStringToType(String strVal, String toType)
//    {
//        if (toType.equals("int"))
//        {
//            if (strVal.isEmpty())
//                 return 0;
//            return Integer.parseInt(strVal);
//        }
//        else if (toType.equals("str"))
//        {
//            return strVal;
//        }
//        else if (toType.equals("bool"))
//        {
//            if (strVal.equals("0"))
//                return false;
//            else if (strVal.equals("1"))
//                return true;
//            else
//                return false;
//
//        }
//        else if (toType.equals("fontface"))
//        {
//            KDSViewFontFace ff = KDSViewFontFace.parseString(strVal);
//            return ff;
//        }
//        else
//        {
//            return strVal;
//        }
//    }

 /**
  * this is from retieve settings from remote station
  * @param appContext
  * @param strXml
     */
    public void parseXmlText(Context appContext, String strXml, boolean bExcludeStationID, boolean bExcludeBumpbarMenu)
    {
        KDSXML xml = new KDSXML();
        xml.loadString(strXml);

        xml.back_to_root();

        if (xml.getFirstGroup("config"))
        {
            do {
                String strid = xml.getAttribute("id", "");
                String strval = xml.getCurrentGroupValue();
                String strType = xml.getAttribute("ty", "");


                int n = KDSUtil.convertStringToInt(strid, -1);
                if (n <0) continue;
                ID id = ID.values()[n];
                if (bExcludeStationID) {
                    if (id == ID.KDS_ID) continue;
                }
                if (bExcludeBumpbarMenu) {
                    if (id == ID.Bumpbar_Menu) continue;
                }

                Object objVal = convertConfigStringToType(strval, strType);
                this.set(id, objVal);

            }while(xml.getNextGroup("config"));
        }
        xml.back_to_root();
        if (!xml.getFirstGroup("StationsRelation"))
            return;
        String strRelations = xml.getCurrentGroupValue();
        saveStationsRelation(appContext, strRelations);



    }

// /**
//  * This is for import settings.
//  * @param appContext
//  * @param strXml
//     */
//    public void parseXmlTextAll(Context appContext, String strXml)
//    {
//        KDSXML xml = new KDSXML();
//        xml.loadString(strXml);
//
//        xml.back_to_root();
//
//        if (xml.getFirstGroup("config"))
//        {
//            do {
//                String strid = xml.getAttribute("id", "");
//                String strval = xml.getCurrentGroupValue();
//                String strType = xml.getAttribute("ty", "");
//
//
//                int n = KDSUtil.convertStringToInt(strid, -1);
//                if (n <0) continue;
//                ID id = ID.values()[n];
//
//                if (id == ID.KDS_ID) continue; //exclude id.
//
//                Object objVal = convertConfigStringToType(strval, strType);
//                this.set(id, objVal);
//
//            }while(xml.getNextGroup("config"));
//        }
//        xml.back_to_root();
//        if (!xml.getFirstGroup("StationsRelation"))
//            return;
//        String strRelations = xml.getCurrentGroupValue();
//        saveStationsRelation(appContext, strRelations);
//
//
//
//    }


    /**
     * save all data to local preference settings
     */
    public void save(Context appContext)
    {
        Context c = appContext;//app.getApplicationContext();
//
        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);

        for (Map.Entry<ID, String> entry : m_mapPrefID.entrySet()) {

            ID id = entry.getKey();
            String tag = entry.getValue();
            Object objVal = this.get(id);
            setPrefValue(pre, tag, objVal);

        }

    }

    /**
     * get the color of order caption according to its waiting time.
     * @param dtOrderStart
     * @param nDefaultColor
     * @return
     */
    public int getOrderTimeColorAccordingWaitingTime(Date dtOrderStart, int nDefaultColor)
    {

        int crStage0 = this.getInt(ID.Order_Timer_Stage0_Color);
        int crStage1 = this.getInt(ID.Order_Timer_Stage1_Color);
        int crStage2 = this.getInt(ID.Order_Timer_Stage2_Color);

        TimeAlertLevel alert = getOrderTimeAlert(dtOrderStart);
        switch (alert)
        {

            case None:
                return nDefaultColor;

            case Alert1:
                return crStage0;

            case Alert2:
                return crStage1;

            case Alert3:
                return crStage2;

        }
        return nDefaultColor;
    }

    public TimeAlertLevel getOrderTimeAlert(Date dtOrderStart)
    {
        float tmStage1 = this.getFloat(ID.Order_Timer_Stage0_Time);

        float tmStage2 = this.getFloat(ID.Order_Timer_Stage1_Time);

        float tmStage3 = this.getFloat(ID.Order_Timer_Stage2_Time);


        TimeDog td = new TimeDog(dtOrderStart);
        if (this.getBoolean(ID.Order_Timer_Stage2_Enabled)) {
            if (td.is_timeout((int) (tmStage3 * 60 * 1000))) {

                return TimeAlertLevel.Alert3;
            }
        }
        if (this.getBoolean(ID.Order_Timer_Stage1_Enabled)) {
            if (td.is_timeout((int) (tmStage2 * 60 * 1000)))
                return TimeAlertLevel.Alert2;
        }
        if (this.getBoolean(ID.Order_Timer_Stage0_Enabled)) {
            if (td.is_timeout((int) (tmStage1 * 60 * 1000)))
                return TimeAlertLevel.Alert1;
        }
        return TimeAlertLevel.None;


    }

    public boolean isExpeditorStation()
    {
        return (m_nStationFunc == KDSSettings.StationFunc.Expeditor);

    }

    /**
     * this is for
     * @return
     */
    public int getExpAlertBgColor( boolean bAllItemsBumpedInExp)
    {
        //int nDefaultBG =  getKDSViewFontFace(ID.Panels_Default_FontFace).getBG();
        int nDefaultBG =  getInt(ID.Panels_BG);
        if (!isExpeditorStation())
        //if (!bIsExpo)
            return nDefaultBG;
        if (!getBoolean(KDSSettings.ID.Exp_Alert_Enabled))
            return nDefaultBG;
        if (!getBoolean(KDSSettings.ID.Exp_Alert_Change_Whole_Panel_Color))
            return nDefaultBG;
        //check exp order is all bumped in normal station.
        if (!bAllItemsBumpedInExp)
            return nDefaultBG;
        int expAlertBg = getInt(KDSSettings.ID.Exp_Alert_Color_BG);
        return expAlertBg;
    }

    /**
     * this is for
     * @return
     */
    public int getExpAlertTitleBgColor(boolean bAllItemsBumpedInExp, int nDefaultBG)
    {

        if (!isExpeditorStation())
            return nDefaultBG;
        if (!getBoolean(KDSSettings.ID.Exp_Alert_Enabled))
            return nDefaultBG;

        //check exp order is all bumped in normal station.
        if (!bAllItemsBumpedInExp)
            return nDefaultBG;
        int expAlertBg = getInt(KDSSettings.ID.Exp_Alert_Color_BG);
        return expAlertBg;
    }

   static public KDSConst.OrderSortBy getOrderSortBy(KDSSettings.OrdersSort orderSort) {
        switch (orderSort) {

            case Waiting_Time_Ascend:
            case Waiting_Time_Decend:
               return KDSConst.OrderSortBy.Waiting_Time;
            case Order_Number_Ascend:
            case Order_Number_Decend:
                return KDSConst.OrderSortBy.Order_Number;

            case Items_Count_Ascend:
            case Items_Count_Decend:
                return KDSConst.OrderSortBy.Items_Count;

            case Preparation_Time_Ascend:

            case Preparation_Time_Decend:
                return KDSConst.OrderSortBy.Preparation_Time;

            default:
                return KDSConst.OrderSortBy.Unknown;
        }
    }
   static public KDSConst.SortSequence getOrderSortSequence( KDSSettings.OrdersSort orderSort)
    {
        switch (orderSort) {

            case Waiting_Time_Ascend:
            case Order_Number_Ascend:
            case Items_Count_Ascend:
            case Preparation_Time_Ascend:
                return KDSConst.SortSequence.Ascend;

            case Waiting_Time_Decend:
            case Order_Number_Decend:
            case Items_Count_Decend:
            case Preparation_Time_Decend:
                return KDSConst.SortSequence.Descend;
            default:
                return KDSConst.SortSequence.Ascend;
        }
    }
    /**
     * convert the orders sort option to this dialog definition
     * @param sortBy
     * @param sortSequence
     * @return
     */
    static public KDSSettings.OrdersSort convertSortOption(KDSConst.OrderSortBy sortBy, KDSConst.SortSequence sortSequence)
    {
        switch (sortBy)
        {
            case Waiting_Time:
                if (sortSequence == KDSConst.SortSequence.Ascend)
                    return KDSSettings.OrdersSort.Waiting_Time_Ascend;
                else
                    return KDSSettings.OrdersSort.Waiting_Time_Decend;
            case Order_Number:
                if (sortSequence == KDSConst.SortSequence.Ascend)
                    return KDSSettings.OrdersSort.Order_Number_Ascend;
                else
                    return KDSSettings.OrdersSort.Order_Number_Decend;
            case Items_Count:
                if (sortSequence == KDSConst.SortSequence.Ascend)
                    return KDSSettings.OrdersSort.Items_Count_Ascend;
                else
                    return KDSSettings.OrdersSort.Items_Count_Decend;
            case Preparation_Time:
                if (sortSequence == KDSConst.SortSequence.Ascend)
                    return KDSSettings.OrdersSort.Preparation_Time_Ascend;
                else
                    return KDSSettings.OrdersSort.Preparation_Time_Decend;
            default:
                return OrdersSort.Manually;//.Waiting_Time_Decend;
        }
    }

    public void setStationFunc(StationFunc func)
    {
        m_nStationFunc = func;
    }
    public StationFunc getStationFunc()
    {
//        if (m_nTabCurrentFuncView != StationFunc.MAX_COUNT)
//            return m_nTabCurrentFuncView;
        return m_nStationFunc;
    }

//    public StationFunc getOriginalStationFunc()
//    {
//
//        return m_nStationFunc;
//    }

    /**
     * for unbump dialog preview
     * @return
     */
    public StationFunc getTabFunc()
    {
        return m_nTabCurrentFuncView;
    }

    public StationFunc getFuncView()
    {
        if (m_nTabCurrentFuncView != StationFunc.MAX_COUNT)
            return m_nTabCurrentFuncView;
        return m_nStationFunc;
    }

    public void setTabEnableLineItemsView(boolean benabled)
    {
        m_bTabEnableLineItemsView = benabled;
    }

    /**
     * for unbump dialog preview
     * @return
     */
    public boolean getTabLineItemsTempEnabled()
    {
        return m_bTabEnableLineItemsView;
    }

    /**
     * Maybe, in settings the lineitems view is disabled, but tab enable it.
     * This function is above case
     * @return
     */
    public boolean getLineItemsViewEnabled()
    {
        if (m_nTabCurrentFuncView != StationFunc.MAX_COUNT)
            return m_bTabEnableLineItemsView;
        return this.getBoolean(ID.LineItems_Enabled);

    }



    public void setLineItemsViewEnabled(boolean bEnabled)
    {
        this.set(ID.LineItems_Enabled, bEnabled);
    }


    public Drawable getItemFocusImage()
    {
        return m_itemFocusImage;
    }


    public Drawable getItemBumpedImage()
    {
        return m_itemBumpedImage;
    }


    public Drawable getItemBumpedInOthersImage()
    {
        return m_itemBumpedInOthersImage;
    }

    public Drawable getItemVoidByXmlCommandImage()
    {
        return m_itemVoidByXmlCommand;
    }

    public Drawable getItemChangedImage()
    {
        return m_itemChangedImage;
    }

    public Drawable getItemMoreImage()
    {
        return m_itemMoreImage;
    }
    public Drawable getOrderCookStartedImage()
    {
        return m_orderCookStartedImage;
    }

    public Drawable getExpoItemPartialBumpedImage()
    {
        return m_itemExpoPartialImage;
    }
    /**
     * for draw view block.
     * @return
     */
    public KDSViewFontFace getViewBlockFont(){
        if (m_kdsBlockFont == null) {
            m_kdsBlockFont = new KDSViewFontFace();
            m_kdsBlockFont.setTypeFace(Typeface.createFromFile("/system/fonts/DroidSans.ttf"));

        }
        m_kdsBlockFont.setFontSize(this.getInt(ID.Panels_Row_Height));
        m_kdsBlockFont.setBG(this.getInt(ID.Panels_BG));
        m_kdsBlockFont.setFG(this.getInt(ID.Panels_BG));

        return m_kdsBlockFont;
    }


    final int SOUND_TIMEOUT_ROUND = 3000;
    public TimeAlertLevel checkAlertSound(KDSDataOrder order)
    {
        if (!this.getBoolean(ID.Sound_enabled)) return TimeAlertLevel.None;

        float tmStage1 = this.getFloat(ID.Order_Timer_Stage0_Time);
        float tmStage2 = this.getFloat(ID.Order_Timer_Stage1_Time);
        float tmStage3 = this.getFloat(ID.Order_Timer_Stage2_Time);

        TimeDog td = new TimeDog(order.getStartTime());
        int nAlert3 = (int)(tmStage3 * 60 * 1000);

        if (this.getBoolean(ID.Order_Timer_Stage2_Enabled)) {
            if (td.is_timeout(nAlert3)) {
                if (td.is_timeout(nAlert3 + SOUND_TIMEOUT_ROUND))
                    return TimeAlertLevel.None;
                if (order.getAlert3SoundFired())
                    return TimeAlertLevel.None;
                else
                    return TimeAlertLevel.Alert3;
            }
        }
        int nAlert2 = (int)(tmStage2 * 60 * 1000);
        if (this.getBoolean(ID.Order_Timer_Stage1_Enabled)) {
            if (td.is_timeout(nAlert2)) {
                if (!this.getBoolean(ID.Order_Timer_Stage1_Enabled))
                    return TimeAlertLevel.None;
                if (td.is_timeout(nAlert2 + SOUND_TIMEOUT_ROUND))
                    return TimeAlertLevel.None;
                if (order.getAlert2SoundFired())
                    return TimeAlertLevel.None;
                else
                    return TimeAlertLevel.Alert2;
            }
        }

        int nAlert1 = (int)(tmStage1 * 60 * 1000);
        if (this.getBoolean(ID.Order_Timer_Stage0_Enabled)) {
            if (td.is_timeout(nAlert1)) {
                if (!this.getBoolean(ID.Order_Timer_Stage0_Enabled))
                    return TimeAlertLevel.None;
                if (td.is_timeout(nAlert1 + SOUND_TIMEOUT_ROUND))
                    return TimeAlertLevel.None;
                if (order.getAlert1SoundFired())
                    return TimeAlertLevel.None;
                else
                    return TimeAlertLevel.Alert1;
            }
        }
        return TimeAlertLevel.None;

    }

    static public void saveTrackerAuthen( String strAuthen)
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TRACKER_AUTHEN_KEY, strAuthen);
        editor.apply();
        editor.commit();


    }
    static public String loadTrackerAuthen()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(KDSApplication.getContext());
        String s = pref.getString(TRACKER_AUTHEN_KEY, "");
        return s;
    }

//    static Language loadLanguageOption(Context c)
//    {
//        SharedPreferences pre = PreferenceManager.getDefaultSharedPreferences(c);
//        String str = pre.getString("kds_general_language", "0");
//        int n = KDSUtil.convertStringToInt(str, 0);
//        return Language.values()[n];
//
//    }
//
//    static String getLanguageString(Language lan)
//    {
//        switch (lan)
//        {
//
//            case English:
//                return KDSApplication.getContext().getString(R.string.english);// "English";
//
//            case Chinese:
//                return KDSApplication.getContext().getString(R.string.chinese);//"Chinese";
//
//            default:
//                return KDSApplication.getContext().getString(R.string.english);//"English";
//        }
//    }

    /**
     *  EnumTest.enumValues(EnumTest.TestEnum.class);
     * @param settings
     * @param enumData
     * @param configID
     * @param <E>
     * @return
     */
    protected static <E extends Enum<E>> int getEnumIndexValues(KDSSettings settings, Class<E> enumData, ID configID)
    {
        int n = settings.getInt(configID);
        if (n <0) return 0;
        if (n >= enumData.getEnumConstants().length)
            return 0;

        return n;
    }

    public int getBumpReservedCount()
    {

        int n =  getEnumIndexValues(this, Bump_Reserved_Count.class, ID.Bump_Max_Reserved_Count);
        return n * 10;

    }

    /**
     *
     * @param context
     * @param folderName
     *  With last /
     * @return
     */
    public boolean exportToFolder(Context context, String folderName) {
        String settings = "";

        settings = outputXmlText(context);

        String targetFile = folderName + KDSSettings.SETTINGS_FILE_NAME;//"settings.xml";
        if (KDSUtil.fileExisted(targetFile))
            KDSUtil.remove(targetFile);
        return KDSUtil.fileWrite(targetFile, settings);

    }

    public void setTabCurrentFunc(StationFunc func)
    {
        m_nTabCurrentFuncView = func;
    }

    public void setTabDestinationFilter(String dest)
    {
        m_tabDestinationFilter = dest;
    }
    public String getTabDestinationFilter()
    {
        return m_tabDestinationFilter;
    }

    public void tabDisabled()
    {
        setTabCurrentFunc(KDSSettings.StationFunc.MAX_COUNT);
        setTabDestinationFilter("");
        setTabEnableLineItemsView(false);
    }

    HashMap<Integer, Drawable> m_arIcons = new HashMap<>();

    public Drawable getIcon(int nIndex)
    {
        if (nIndex <0) return null;
        if (m_arIcons.get(nIndex) != null)
            return m_arIcons.get(nIndex);
        String fileName = "";
        if (getBoolean(ID.Icon_folder_enabled))
        {
            String s = getString(ID.Icon_folder);

            s += "/" + KDSUtil.convertIntToString(nIndex);// + ".png";
            String prefix = s;
            s = prefix + ".png";
            if (!KDSUtil.fileExisted(s))
            {
                s = prefix +".jpg";
                if (!KDSUtil.fileExisted(s))
                {
                    s = prefix +".bmp";
                    if (!KDSUtil.fileExisted(s))
                        return null;
                }
            }
            fileName = s;
        }
        else {
            int n = ID.Icon_0.ordinal() + nIndex;
            if (n < ID.Icon_0.ordinal()) return null;
            if (n > ID.Icon_32.ordinal())
                return null;
            ID id = ID.values()[n];
            fileName = getString(id);
        }
        Bitmap bmp = BitmapFactory.decodeFile(fileName);
        Drawable d = new BitmapDrawable(KDSApplication.getContext().getResources(),bmp);
        //Drawable d = new BitmapDrawable(bmp);
        m_arIcons.put(nIndex, d);
        return d;


    }
    public void resetBufferedIcons()
    {
        m_arIcons.clear();
    }

    public void setCurrentOrdersSort(OrdersSort ordersSort)
    {
        m_currentOrdersSort = ordersSort;
    }

    public OrdersSort getCurrentOrdersSort()
    {
        return m_currentOrdersSort;
    }
    public void restoreOrdersSortToDefault()
    {
        m_currentOrdersSort = OrdersSort.values()[  this.getInt(ID.Order_Sort)];

    }

    /**
     * If the tab has lineitems mode, or lineitems mode enabled
     * This is for lineitems
     * @return
     */
    public boolean isLineItemsEnabled()
    {
        if (this.getBoolean(ID.LineItems_Enabled))
            return true;
        String s = this.getString(ID.Tab_buttons);
        ArrayList<TabDisplay.TabButtonData> ar = TabDisplay.parseTabButtons(s);
        for (int i=0; i< ar.size(); i++)
        {
            if (ar.get(i).getFunc() == TabFunction.LineItems)
                return true;
        }
        return false;
    }

}
