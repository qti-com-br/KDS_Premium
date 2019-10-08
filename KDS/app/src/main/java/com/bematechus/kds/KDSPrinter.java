package com.bematechus.kds;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.TextView;

import com.bematechus.bemaLibrary.BemaPrinter;
import com.bematechus.bemaLibrary.PrinterInfo;
import com.bematechus.bemaLibrary.PrinterStatus;
import com.bematechus.bemaUtils.PortInfo;
import com.bematechus.bemaUtils.UsbPort;
import com.bematechus.kdslib.BuildVer;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataCategoryIndicator;
import com.bematechus.kdslib.KDSDataCondiment;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataItems;
import com.bematechus.kdslib.KDSDataModifier;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSSocketTCPSideBase;
import com.bematechus.kdslib.KDSUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

/**
 *
 * print order to printer
 */
public class KDSPrinter {

    static final private String TAG = "KDSPrinter";

    static final private String TAG_LEFT = "<";
    static final private String TAG_RIGHT = ">";
    static final private char CHAR_Start_Order = 0x301; //identify a new order printing start/end
    static final private char CHAR_End_Order = 0x302;
    final private int MAX_PRINT_LINES = 10000;

    static final private String TAG_START_BOLD = "<B>";
    static final private String TAG_END_BOLD = "</B>";
    static final private String TAG_PAPER_CUT = "<PC>";

    static final private char CMD_START_BOLD = 0x01;//"<B>";
    static final private char CMD_END_BOLD = 0x02;//"</B>";
    static final private char CMD_PAPER_CUT = 0x03;//"<PC>";

    static final private String TAG_CR = "<CR>";
    static final private String TAG_ITEMS = "<ITEMS>";
    static final private String TAG_CONDIMENTS = "<CONDIMENTS>";
    static final private String TAG_MODIFIERS = "<MODIFIERS>";


    public enum PrinterPortType{
        USB,
        Socket,
        Serial,
    }

    public enum CRTagsLineType
    {
        TAGS_LINE_NORMAL ,
        TAGS_LINE_ITEM ,
        TAGS_LINE_CONDIMENT  ,
        TAGS_LINE_MODIFIER,
    }

    public enum TextAlign
    {
        ALIGN_LEFT,
        ALIGN_CENTER,
        ALIGN_RIGHT,
    }

    public enum CommandIndex{
        TICKET_PRN_TAG_B, //0
        TICKET_PRN_TAG_EB,

        TICKET_PRN_TAG_PC,

        TICKET_COMMANDS_COUNT
    }

    public enum PrinterType{
        LR2000,
        MP4200,
    }

    public enum HowToPrintOrder
    {
        Manual,
        WhileBump,
        WhileReceive,
        WhileTransfer,
    }

    public enum SerialBaudrate
    {
        B9600, B19200, B38400, B57600, B115200
    }

    private TextAlign  m_nCurrentAlignment = TextAlign.ALIGN_LEFT;


    private ArrayList<String> m_arLinesTags = null;
    private  ArrayList<String> m_arCmdSet = new ArrayList<String>();
    private  boolean m_bPrintWithCondiments = true;
    private boolean m_bSimulateBold = false;
    private int m_nLineWidth = 40;
    private KDS m_kds = null;
    private PrinterType m_nPrinterType = PrinterType.LR2000;
    PrinterPortType m_nPortType = PrinterPortType.USB;

    //private ArrayList<String> m_printerData = new ArrayList<String>();//the data will been printed
    Vector<String> m_printerData = new Vector<>();//the data will been printed

    private int m_nCopies = 1; //print how many copies

    private BemaPrinter m_bemaPrinter = null;// new BemaPrinter();

    UsbManager m_usbManager = null;
    PendingIntent m_PermissionIntent = null;
    BemaPrinter.CodePage m_codepage =  BemaPrinter.CodePage.CP437;
    Object m_locker = new Object();
    boolean m_bNetworkPrinterValid = false; //check if the network printer valid, update this value in onping function. Use it will not lock app.
    boolean m_bSerialPrinterValid = false;
    HowToPrintOrder m_howtoPrint = HowToPrintOrder.Manual;

    boolean m_bGroupCategory = false; //2.0.48
    /**********************************************************************************************/
    /**
     *
     * @param kds
     */
    public KDSPrinter(KDS kds)
    {
        m_kds = kds;
        init_printer_commands();
    }
    private KDS getKDS()
    {
        return m_kds;
    }

    /**
     * init this array. Set all to zero
     */
    private void init_printer_commands()
    {
        for (int i=0; i< CommandIndex.TICKET_COMMANDS_COUNT.ordinal(); i++) {
            m_arCmdSet.add("");
        }
        init_lr2000_commands();


    }

    private void init_lr2000_commands()
    {

        m_arCmdSet.set(CommandIndex.TICKET_PRN_TAG_B.ordinal(),"1b,45,1"); //0
        m_arCmdSet.set(CommandIndex.TICKET_PRN_TAG_EB.ordinal(),"1b,45,0");
        //m_arCmdSet.set(CommandIndex.TICKET_PRN_TAG_EI.ordinal(),"");
        //m_arCmdSet.set(CommandIndex.TICKET_PRN_TAG_I.ordinal(),"");
        m_arCmdSet.set(CommandIndex.TICKET_PRN_TAG_PC.ordinal(),"1b,6d");
    }
    public void setTemplate(String strTemplate)
    {
        if (m_arLinesTags != null)
            m_arLinesTags.clear();
        m_arLinesTags = parseTemplate(strTemplate);
    }
/************************************************************************/
/*
	e.g:1234567890<red>abcdef</red>
	return: arrray:
				1234567890
				<red>
				abcdef
				</red>
*/
    /************************************************************************/
    public ArrayList<String> parseTemplate(String s)
    {
        ArrayList<String> arSplited = new ArrayList<>();
        ArrayList<String> arLeft = new ArrayList<>();
        //CStringArray arleft, ar;
        String str;

        str = s + "<cr>";
        str = str.replace("\r", "");

        //str.Remove('\r');
        str = str.replace("\n", "");

        //s = s+"<cr>";
        boolean bret = false;
        boolean findtag = false;
       // CTokenEx t;
        //Use"<" to separate this given string
        //e.g: 1234567890<red>abcdef</red>
        // result: 1234567890
        //			<red>abcdef
        //			</red>

        arLeft = KDSUtil.spliteString(str, TAG_LEFT);
        for (int i=0; i< arLeft.size(); i++)
        {
            String strRow = arLeft.get(i);
            if (strRow.indexOf(TAG_RIGHT) >=0) {
                strRow = TAG_LEFT + strRow;
                arLeft.set(i, strRow);
            }
        }

        findtag = false;

        //separate the above string again
        // use ">" to separate string again
        //// Above: 1234567890
        //			<red>abcdef
        //			</red>
        //result:
        //1234567890
        //<red>
        //abcdef
        //</red>

        for (int i=0; i< arLeft.size(); i++)
        {
            String strleft = arLeft.get(i);
            ArrayList<String> arRight = KDSUtil.spliteString(strleft, TAG_RIGHT);
            for (int j = 0; j<arRight.size(); j++)
            {
                String strRight =arRight.get(j);
                if (strRight.indexOf(TAG_LEFT)>=0)
                    strRight += TAG_RIGHT;
                arSplited.add(strRight);
            }
        }

        arSplited = clearEmptyStrings(arSplited);

        ArrayList<String> arLines = new ArrayList<>();

        //join no-tag strings
        for (int i=0; i< arSplited.size(); i++)
        {
            String strtag = arSplited.get(i);
            if (strtag.indexOf(TAG_LEFT)>=0 &&
                    strtag.indexOf(TAG_RIGHT)>=0)
            {
                arLines.add(strtag);

            }
            else
            {
                if (arLines.size() <1)
                    arLines.add(strtag);
                else
                {

                    String strsaved = arLines.get(arLines.size()-1);
                    if (strsaved.indexOf(TAG_LEFT )>=0 &&
                            strsaved.indexOf(TAG_RIGHT)>=0)
                    {
                        arLines.add(strtag);

                    }
                    else
                    {
                        //add to last
                        arLines.set(arLines.size()-1, strsaved + strtag);
                    }
                }
            }
        }

        return arLines;
    }

    private ArrayList<String> clearEmptyStrings(ArrayList<String> ar)
    {
        int ncount = ar.size();
        int nindex = 0;
        for (int i=0; i< ncount; i++ )
        {
            if (nindex >=ar.size()) break;
            if (ar.get(nindex).isEmpty())
            {
                ar.remove(nindex);
                continue;
            }
            nindex ++;

        }
        return ar;
    }
    /************************************************************************/
/*
remove all unprintable item from order.
And the premodifiers strings will been delete with its unprintable item.

*/
    /************************************************************************/
    private void rebuild_order_for_printable_options(KDSDataOrder pOrder)
    {
        //check if all item is printable, and if with its condiments
        KDSDataItems items = pOrder.getItems();

        int nItemsCount = items.getCount();
        //CItems *pitems = pOrder->GetItemsDataPointer();
        String name = ("");
        String category = ("");
        //int i=0;
        KDSDataItem item = null;
        //while (i <nItemsCount) {
        for (int i=0; i< nItemsCount; i++){
            item = null;
            if (i >= items.getCount()) break;
            item = items.getItem(i);
            if (item == null) break;
            if (!m_bPrintWithCondiments)
                item.getCondiments().clear();


        }

    }


    /************************************************************************/
/*
return how many physical printing lines
*/
    /************************************************************************/
    private int getLines()
    {
        int count = 0;
        String s = "";

        for (int i=0; i< m_arLinesTags.size(); i++)
        {
            s = m_arLinesTags.get(i);
            s = s.trim();
            s = s.toUpperCase();


            if (s.equals(TAG_CR))
                count++;
        }

        return count;
    }


    /************************************************************************/
/*
return given CR in which index of tag array
*/
    /************************************************************************/
    private int getCRIndexInTagsArray(int nIndex)
    {
        String s = "";
        int count = 0;

        for (int i=0; i< m_arLinesTags.size(); i++)
        {
            s = m_arLinesTags.get(i);
            s = s.trim();
            s = s.toUpperCase();


            if (s.equals(TAG_CR))
                count++;

            if (count -1 == nIndex) return i;
        }

        return -1;
    }

    private CRTagsLineType getCRLineType(ArrayList<String> arTags)
    {
        int count = arTags.size();
        String s = "";
        for (int i=0; i< count; i++)
        {
            s = arTags.get(i);
            s = s.trim();
            s = s.toUpperCase();

            if (s.equals(TAG_ITEMS))
                return  CRTagsLineType.TAGS_LINE_ITEM;
            if (s.equals(TAG_CONDIMENTS))
                return CRTagsLineType.TAGS_LINE_CONDIMENT;
            if (s.equals(TAG_MODIFIERS))
                return CRTagsLineType.TAGS_LINE_MODIFIER;

        }

        return CRTagsLineType.TAGS_LINE_NORMAL;
    }


    /************************************************************************/
/*
return whole line tags
*/
    /************************************************************************/
    private ArrayList<String> getCRLineTags(int nLine)
    {
        ArrayList<String> arTags = new ArrayList<>();

        int sindex, eindex;
        //arTags->RemoveAll();
        if (nLine <0) return arTags;
        sindex = getCRIndexInTagsArray(nLine -1);
        eindex = getCRIndexInTagsArray(nLine);
        sindex ++;

        for (int i= sindex; i<=eindex; i++)
        {
            arTags.add(m_arLinesTags.get(i));

        }
        return arTags;
    }

    private  String addBlankSpaceToString(String oldString, int nBlank, boolean bAppendToEnd)
    {
        String s = oldString;
        int i;
        if (bAppendToEnd)
        {
            for ( i=0; i< nBlank; i++)
            {
                s += (" ");
            }
        }
        else
        {
            for ( i=0; i< nBlank; i++)
            {
                s = (" ") + s;
            }

        }
        return s;
    }
    private String makeTabString(String tag)
    {
        //<t23 ...>

        if (tag.length()<3) return ("");
        String s;
        s = tag.substring(2, 3);
        //s = s.Left(s.GetLength() - 1);
        int len = KDSUtil.convertStringToInt(s, 0);// _ttoi(s);

        if (len<=0) return ("");
        s = ("");
        s = addBlankSpaceToString(s, len, true);
        return s;
    }


    private ArrayList<Byte> convertCommandStringToHex(String s)
    {
        String str = s;
        int counter = 0;
        boolean berror = false;
        ArrayList<String> arCodes = KDSUtil.spliteString(str, ",");
        ArrayList<Byte> arReturn = new ArrayList<>();

        for (int i=0; i< arCodes.size(); i++)
        {

            String strhex = arCodes.get(i);// t.GetString(str, _T(","), false, &bret);
            if (!strhex.isEmpty())
            {

                arReturn.add( KDSUtil.hex_string_to_hex(strhex));
                counter++;
            }

        }
        return arReturn;

    }

    //private final int CHAR_ZERO = 0x300;

    /**
     *
     * @param tag
     * @return
     * Format:
     *  <command,command ...>
     */
    private  String  getCmdSet(String tag)
    {
        char ch = 0;
        if (tag.equals(TAG_START_BOLD) )
            ch = CMD_START_BOLD;
        if (tag.equals(TAG_END_BOLD) )
            ch = CMD_END_BOLD;
        if (tag.equals(TAG_PAPER_CUT)) {
            ch = CMD_PAPER_CUT;
        }
        if (ch !=0)
        {
            String s = "";
            s += ch;
            return s;
        }
        return "";


    }

    private  String getAddonString(KDSDataOrder pOrder)
    {
        if (pOrder.isVoidOrder()) return (""); //maybe void the addon items
        if (pOrder.isAddonOrder()) 	return ("ADD-ON");

        return ("");
    }

    private String getStartTime24String(Date tm)
    {
        Date dt = tm;


        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        String str = sdf.format(dt);
        return str;

    }
    private String getStartTime12String(Date tm)
    {
        Date dt = tm;

        Calendar    date    =    Calendar.getInstance();
        date.setTime(dt);
        int n = date.get(Calendar.HOUR_OF_DAY);
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");

        String str = sdf.format(dt);
        if (n <=12)
            str += " AM";
        else
            str += " PM";
        return str;

    }

    private String getStartDateString(Date tm)
    {
        String time = null;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //DateFormat dateFormat = new SimpleDateFormat();
        time = dateFormat.format(tm.getTime());
        return time;
        //System.out.println(time);

    }

    private String getStartTimeString(Date tm)
    {
        String time = null;
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        //DateFormat dateFormat = new SimpleDateFormat();
        time = dateFormat.format(tm.getTime());
        return time;
        //System.out.println(time);

    }

    /**
     *
     * @param tag
     *        The printer tag
     * @param pOrder
     * @param pItem
     * @param pCondiment
     * @return
     */
    private String makeTagString(String tag, KDSDataOrder pOrder, KDSDataItem pItem, KDSDataCondiment pCondiment, KDSDataModifier pModifier)
    {
        String t = tag;
        t = t.trim();
        t = t.toUpperCase();

        String s = ("");
        if (t.equals(TAG_CR))
        {

            m_nCurrentAlignment = TextAlign.ALIGN_LEFT;
            return s;
        }
        if (t.equals("<C>"))
        {
            m_nCurrentAlignment = TextAlign.ALIGN_CENTER;
            return s;
        }
        if (t.equals("<R>"))
        {
            m_nCurrentAlignment = TextAlign.ALIGN_RIGHT;
            return s;

        }

        if (t.length() >3 &&
                t.charAt(1) =='T' &&
                t.charAt(0) == '<' &&
                Character.isDigit(t.charAt(2)))
        {
            s = makeTabString(t);
            return s;
        }
        if (t.equals("<B>") || t.equals("</B>") ||
                t.equals("<RED>") || t.equals("</RED>") ||
                t.equals("<I>") || t.equals("</I>") ||
                t.equals("<PC>"))
        {
            s += getCmdSet(t);
            return s;
        }

        if ( t.equals("<STATIONNUMBER>"))
        {
            s += this.getKDS().getStationID();
            return s;
        }

        if (t.equals("<ORDERNUMBER>"))
        {
            s += pOrder.getOrderName();
            return s;
        }

        if (t.equals("<ADDON>") )
        {
            s += getAddonString(pOrder);
            return s;

        }

        if (t.equals("<TABLENUMBER>"))
        {
            s += pOrder.getToTable();
            return s;
        }

        if (t.equals("<ORDERTYPE>"))
        {
            s += pOrder.getOrderType();
            return s;
        }
        if (t.equals("<EMPLOYEE>"))
        {
            s += pOrder.getWaiterName();
            return s;

        }

        if (t.equals("<ORDERDESTINATION>"))
        {
            s += pOrder.getDestination();
            return s;

        }
        if (t.equals("<ORDERUSERDEFINED>")) //2.5.3.1 add this
        {
            s += pOrder.getCustomMsg();
            return s;
        }
        if (t.equals("<ORDERSTATUS>"))
        {
            int n = pOrder.getStatus();
            if (n==0)
            {
                s += ("UNPAID");
            }
            else
            {
                s += ("PAID");
            }
            return s;

        }

        if (t.equals("<ORDERTIME>")) //24 hour format
        {

            Date tm = pOrder.getStartTime();
            if (pOrder.isAddonOrder() || pOrder.isVoidOrder())
            {
                tm.setTime(System.currentTimeMillis());//
            }

            s += getStartTime24String(tm);//.toString();//.Format(_T("%H:%M"));

            return s;

        }

        if (t.equals("<ORDERTIME12H>")) //12Hour format
        {
            Date tm = pOrder.getStartTime();
            if (pOrder.isAddonOrder() || pOrder.isVoidOrder())
            {
                tm.setTime(System.currentTimeMillis());//
            }

            s += getStartTime12String(tm);

            return s;
        }

        if (t.equals("<QTY>"))
        {
            if (pItem == null) return ("");

            float qty = 0;

            qty = pItem.getShowingQty();
            s = KDSUtil.convertIntToString((int)qty);


            return s;
        }

        if (t.equals("<VOID>"))
        {
            if (pItem == null) return ("");

            if (pItem.getShowingQty() == 0)
                return ("VOID ");
            else
                return ("");
        }
        if (t.equals(TAG_ITEMS))
        {
            if (pItem == null) return ("");

            return pItem.getDescription();// ->GetText();
        }

        if (t.equals(TAG_CONDIMENTS))
        {
            if (pCondiment == null) return ("");
            return pCondiment.getDescription();
        }

        if (t.equals("<DATE>")) //2.5.4.35
        {
            if (pOrder == null) return ("");
            Date dt = pOrder.getStartTime();//
            return  getStartDateString(dt);// dt.Format(VAR_DATEVALUEONLY   );
        }

        if (t.equals("<TIME>"))//2.5.4.35
        {
            if (pOrder == null) return ("");
            return getStartTimeString(pOrder.getStartTime());

        }
        if (t.equals(TAG_MODIFIERS))
        {
            if (pModifier == null) return ("");
            return pModifier.getDescription();
        }
        if (t.equals(TAG_MODIFIERS))
        {
            if (pModifier == null) return ("");
            return pModifier.getDescription();
        }
        return tag;

    }

    /**
     *
     * @param strAlignTexts
     *   Must equal 3, left, center, and right
     * @param tagString
     * @return
     */
    private  void addNewTagStringToLine( ArrayList<String> strAlignTexts, String tagString)
    {

        String s;
        switch (m_nCurrentAlignment)
        {
            case ALIGN_LEFT:
            {
                s = strAlignTexts.get(TextAlign.ALIGN_LEFT.ordinal()) + tagString;
                strAlignTexts.set(TextAlign.ALIGN_LEFT.ordinal(), s);
            }
            break;
            case ALIGN_RIGHT:
            {
                s = strAlignTexts.get(TextAlign.ALIGN_RIGHT.ordinal()) + tagString;
                strAlignTexts.set(TextAlign.ALIGN_RIGHT.ordinal(), s);

            }
            break;
            case ALIGN_CENTER:
            {
                s = strAlignTexts.get(TextAlign.ALIGN_CENTER.ordinal()) + tagString;
                strAlignTexts.set(TextAlign.ALIGN_CENTER.ordinal(), s);


            }
            break;
            default:
            {
                s = strAlignTexts.get(TextAlign.ALIGN_LEFT.ordinal()) + tagString;
                strAlignTexts.set(TextAlign.ALIGN_LEFT.ordinal(), s);
            }
            break;
        }

    }
    private  String makeTagsString(ArrayList<String> arTags, KDSDataOrder pOrder, KDSDataItem pItem, KDSDataCondiment pCondiment, KDSDataModifier pModifier)
    {
        //one line
        String str="", s="";


        String tag="";
        String strL= (""), strC=(""), strR=("");
        ArrayList<String> arLineAlign = new ArrayList<>();
        arLineAlign.add(strL);
        arLineAlign.add(strC);
        arLineAlign.add(strR);

        for (int i=0; i< arTags.size(); i++)
        {
            tag = arTags.get(i);
            s = makeTagString(tag, pOrder, pItem, pCondiment, pModifier);
            addNewTagStringToLine(arLineAlign, s);
        }
        return makePrintString(arLineAlign.get(0), arLineAlign.get(1), arLineAlign.get(2));

    }
    private boolean isUnicode(char ch)
    {
        if ((((ch & 0xff00)>>8) & 0x00ff) > 0)
            return true;
        return false;
    }
    private boolean isPrintable(char ch)
    {
        return (ch >=0x20);


    }
/************************************************************************/
/*
return the ascii string len, it is unicode len
                                                                     */
    /************************************************************************/
    private  int getStringPrintLen(String s)
    {
        int count=0;
        char ch;
        int nunicode = 0;
        for (int i=0; i< s.length(); i++)
        {
            ch = s.charAt(i);
            if (isUnicode(ch))
                nunicode++;
            else
            {
                if (isPrintable(ch))
                {
                    count ++;
                }
            }
        }
        double d = 1.7142857;
        double d1 = nunicode;
        d = d * d1;
        int n = (int)(d+0.5);
        return count + n;
    }

    private int getPrinterLineWidth()
    {
        return m_nLineWidth;
    }

    private String stringLeft(String strOriginal, int ncount)
    {
        return  strOriginal.substring(0, ncount);//.Left(lsp);
    }

    private String stringRight(String strOriginal, int ncount)
    {
        return  strOriginal.substring(strOriginal.length()-ncount);

    }
    private String stringMid(String strOriginal, int nStartIndex, int nCount)
    {
        return  strOriginal.substring(nStartIndex, nStartIndex + nCount);
    }

    private String stringDel(String strOriginal , int nStartIndex, int nCount)
    {
        String l = strOriginal.substring(0, nStartIndex);
        String r = strOriginal.substring(nStartIndex + nCount);
        return l + r;
    }
    private String makePrintString(String strL, String strC, String strR)
    {
        int w = getPrinterLineWidth();
        int lenl, lenc, lenr;
        lenl = getStringPrintLen(strL);
        lenc = getStringPrintLen(strC);
        lenr = getStringPrintLen(strR);


        String s="";
        String str="";
        int len;
        if (lenc == 0)
        {
            s = strL + strC;
            len =w- lenl - lenr;
            s = addBlankSpaceToString(s, len, true);
            s += strR;
            //return s;
        }
        else
        {
            int lsp = (w-lenc) /2;
            int rsp = (w-lenc)/2;
            s = addBlankSpaceToString(strL, lsp, true);
            s = stringLeft(s, lsp);// s.substring(0, lsp);//.Left(lsp);

            s += strC;

            str = addBlankSpaceToString(strR, rsp, false);
            //str = str.substring(str.length()-rsp);//.Right(rsp);
            str = stringRight(str, rsp);
            s += str;
            //return s;


        }
        String ch;
            ch = ("\r\n");
        s += ch;

        return s;


    }


    /**
     *
     * @param arBuffer
     *      convert data to printable string, and save to this buffer.
     * @param s
     */

    private void addPrintLines( ArrayList<String> arBuffer, String s)
    {
//	char buffer[20];

        String cmdRed = getCmdSet(("<RED>"));
        String cmdERed = getCmdSet(("</RED>"));
        String cmdBold = getCmdSet(("<B>"));
        String cmdEBold = getCmdSet(("</B>"));

        boolean bBold=false, bRed= false;

        int index;
        if (!cmdRed.isEmpty() )
        {
            index = s.indexOf(cmdRed);
            if (index >=0) bRed = true;
        }
        if (!cmdBold.isEmpty() )
        {

            index = s.indexOf(cmdBold);
            if (index >=0) bBold = true;
        }


        if (!bBold && !bRed)
        {
            arBuffer.add(s);
            return;
        }


        if (m_bSimulateBold && bBold)
        {

            replaceTagStringToLine(arBuffer, cmdBold, cmdEBold, s, false);
        }
        {
            arBuffer.add(s);
        }

    }

    private void replaceTagStringToLine(ArrayList<String> arBuffer, String cmdStart, String cmdEnd, String s, boolean bRed)
    {


            replaceBoldTagStringToLine(arBuffer, cmdStart, cmdEnd, s);
    }


    private  void replaceBoldTagStringToLine(ArrayList<String>  arBuffer, String cmdStart, String cmdEnd, String s)
    {
        int indexS=0, indexE=0;
        String str="";
        String str1="", str2="";
        int count;
        int len;
        while(true)
        {
            indexE=0;
            indexS = s.indexOf(cmdStart, indexE);
            //RAFAEL
            if ( indexS >= 0 )
                indexE = s.indexOf(cmdEnd, indexS);
            if (indexS <0 || indexE <0) break;
            //get <tag> string
            count = indexE-indexS+cmdEnd.length();
            str = stringMid(s, indexS, count);// s->Mid(indexS, count);
            //replace <tag></tag> string with blank string

            s = stringDel(s, indexE, cmdEnd.length());// s->Delete(indexE, cmdEnd.GetLength());
            s = stringDel(s, indexS, cmdStart.length());//s->Delete(indexS, cmdStart.GetLength());

            if (count == (cmdStart.length() + cmdEnd.length()))
                continue; //null data
            //remove <tag></tag>
            str = stringLeft(str, count-cmdEnd.length());
            str = stringRight(str, str.length() - cmdStart.length());
            //add previous blank string
            str1 = stringLeft(s, indexS);
            len = getStringPrintLen(str1);
            str = addBlankSpaceToString(str, len, false);
            str += ("\r");
            arBuffer.add(str);

        }

    }

    private int getCondimentTagsLineNumber()
    {
        int count = 0;
        String s;

        for (int i=0; i< m_arLinesTags.size(); i++)
        {
            s = m_arLinesTags.get(i);
            s = s.trim();
            s = s.toUpperCase();

            if (s.equals(TAG_CR))
                count++;
            if (s.equals(TAG_CONDIMENTS))
                return count;
                //break;
        }

        return -1;

    }

    private int getModifierTagsLineNumber()
    {
        int count = 0;
        String s;

        for (int i=0; i< m_arLinesTags.size(); i++)
        {
            s = m_arLinesTags.get(i);
            s = s.trim();
            s = s.toUpperCase();

            if (s.equals(TAG_CR))
                count++;
            if (s.equals(TAG_MODIFIERS))
                return count;
                //break;
        }

        return -1;

    }

    /**
     *
     *
     * @param parPrint
     * @param parItemTags
     * @param parCondimentTags
     * @param pOrder
     * @param parModifierTags
     * @param nCondimentLineIndex
     *  The line number in template.
     *      Use it to find out condiment is in front of modifier or not.
     * @param nModifierLineIndex
     *  The line number in template.
     *  Use it to find out modifier is in front of condiment or not.
     */
    private void makeItemsStrings(ArrayList<String> parPrint, ArrayList<String> parItemTags, ArrayList<String> parCondimentTags, KDSDataOrder pOrder,ArrayList<String> parModifierTags, int nCondimentLineIndex,int nModifierLineIndex)
    {


        int itemcount = pOrder.getItems().getCount();
        int condimentcount;
        String s=("");
        for (int i=0; i< itemcount; i++)
        {
            KDSDataItem pItem = pOrder.getItems().getItem(i);
            s = ("");
            if (pItem instanceof KDSDataCategoryIndicator)
            {//2.0.48
                s = KDSDataCategoryIndicator.makeDisplayString ( (KDSDataCategoryIndicator)pItem);
                String ch;//2.0.49
                ch = ("\r\n");
                s += ch;

            }
            else {
                s = makeTagsString(parItemTags, pOrder, pItem, null, null);
            }
            addPrintLines(parPrint, s);
            //check if print condiment first
            if (nCondimentLineIndex >=0) {
                if (nCondimentLineIndex < nModifierLineIndex) {
                    condimentcount = pItem.getCondiments().getCount();
                    for (int j = 0; j < condimentcount; j++) {
                        KDSDataCondiment pCondiment = pItem.getCondiments().getCondiment(j);
                        s = makeTagsString(parCondimentTags, pOrder, null, pCondiment, null); // pItem, pCondiment); //2.5.4.31
                        //parPrint->Add(s);
                        addPrintLines(parPrint, s);

                    }
                }
            }
            //print modifiers
            int modifiersCount = pItem.getModifiers().getCount();
            if (nModifierLineIndex >= 0) {
                for (int j = 0; j < modifiersCount; j++) {
                    KDSDataModifier pModifier = pItem.getModifiers().getModifier(j);
                    s = makeTagsString(parModifierTags, pOrder, null, null, pModifier); // pItem, pCondiment); //2.5.4.31
                    //parPrint->Add(s);
                    addPrintLines(parPrint, s);

                }
            }
            //check if print condiment after modifiers
            if (nCondimentLineIndex >=0) {
                if (nCondimentLineIndex >= nModifierLineIndex) {
                    condimentcount = pItem.getCondiments().getCount();
                    for (int j = 0; j < condimentcount; j++) {
                        KDSDataCondiment pCondiment = pItem.getCondiments().getCondiment(j);
                        s = makeTagsString(parCondimentTags, pOrder, null, pCondiment, null); // pItem, pCondiment); //2.5.4.31
                        //parPrint->Add(s);
                        addPrintLines(parPrint, s);

                    }
                }
            }

        }

    }

    public void printOrderToBuffer(KDSDataOrder order)
    {
        printToBuffer(order);
    }
    /*
print order data to  buffer, socket will send this buffer to serial port
@pOrder: the COrderDisplay, the all data is in this order. We can change its data in this function.

*/
    public void printToBuffer(KDSDataOrder order)
    {
        if (order == null) return;
        if (!isEnabled()) return;
        int len = m_printerData.size();
        int msz = MAX_PRINT_LINES;

        if (len >= msz) return;

        if (m_bGroupCategory)
        {
            KDSDataOrder printOrder = new KDSDataOrder();
            order.copyTo(printOrder);
            KDSLayoutOrder.buildGroupCategory(printOrder);
            order = printOrder;
        }

        rebuild_order_for_printable_options(order);
        KDSDataItems items  = order.getItems();
        if (items.getCount() <=0) return; //don't print empty order

        ArrayList<String> arPrint = new ArrayList<String>();
        ArrayList<String> arLineTags = new ArrayList<String>();

        ArrayList<String> arItemTags = new ArrayList<>();
        ArrayList<String> arCondimentTags = new ArrayList<>();
        ArrayList<String> arModifierTags = new ArrayList<>();


        //return how many physical printing lines
        int lines = getLines();

        String s = "";
        String tag = "";

        int i = 0;
        for ( i=0; i< lines; i++)
        {
            //get all tags in given physcal printing line
            arLineTags =getCRLineTags(i);
            switch (getCRLineType(arLineTags))
            {
                case TAGS_LINE_NORMAL: {
                    s = makeTagsString(arLineTags, order,null, null, null);
                    addPrintLines(arPrint, s);
                    //arPrint.Add(s);
                }
                break;
                case TAGS_LINE_ITEM: {
                    arItemTags.addAll(arLineTags);
                    int nCondimentLineIndex = getCondimentTagsLineNumber();
                    if (nCondimentLineIndex >=0)
                    {
                        arCondimentTags = getCRLineTags(nCondimentLineIndex);
                    }
                    int nModifierLineIndex = getModifierTagsLineNumber();
                    if (nModifierLineIndex >=0)
                    {
                        arModifierTags = getCRLineTags(nModifierLineIndex);
                    }

                    makeItemsStrings(arPrint, arItemTags, arCondimentTags, order, arModifierTags, nCondimentLineIndex, nModifierLineIndex);
                }
                break;
                case TAGS_LINE_CONDIMENT:
                case TAGS_LINE_MODIFIER:
                {
                    continue;
                }

                //break;
                default:
                {
                }
                break;
            }


        }
        //synchronized (m_locker)
        {
            s = "";
            s += (CHAR_Start_Order);

            m_printerData.add(s);
            for (i = 0; i < m_nCopies; i++) {
                m_printerData.addAll(arPrint);
            }
            s = "";
            s += (CHAR_End_Order);
            m_printerData.add(s);
        }
        arPrint.clear();
        //arrangeBuffer();

    }

    /**
     * check if the buffer is full,
     */
    private void arrangeBuffer()
    {
        synchronized (m_locker) {
            int len = m_printerData.size();
            int msz = MAX_PRINT_LINES;

            if (len <= msz) return;
            int n = len - msz;

            for (int i = 0; i < n; i++) {
                m_printerData.remove(msz);
            }
        }
    }


    public void set_debug_templete()
    {
        String s =  "<c><b>Order #: <ordernumber></b><cr>\n" +
                "<red><c><addon><cr>\n" +
                "</red>Station #: <stationnumber><r>Table #: <tablenumber><cr>\n" +
                "<ordertype><r>Cashier: <employee><cr>\n" +
                "<r><orderdestination><cr>\n" +
                "<cr>\n" +
                "<i>Qty <t5>Items</i><cr>\n" +
                "</red><Qty><t5><void><items><cr>\n" +
                "<red><t7><condiments><cr>\n" +
                "</red>\n" +
                "----------------------------------------<cr>\n" +
                "<orderstatus><r><ordertime><cr>\n" +
                "<cr><ordertime12h>\n" +
                "<c>========END=============<cr>\n" +
                "<cr>\n" +
                "<cr>\n" +
                "<cr>\n" +
                "<cr>\n" +
                "<cr>\n" +
                "<cr>\n" +
                "<cr><pc>";
        this.setTemplate(s);

    }


    private PortInfo getPortInfo(KDSSettings settings)
    {
        PrinterType printerType = PrinterType.values()[ settings.getInt(KDSSettings.ID.Printer_Type)];
        PrinterPortType printerPortType = PrinterPortType.values()[ settings.getInt(KDSSettings.ID.Printer_Port)];
        String ip = settings.getString(KDSSettings.ID.Printer_ip);
        String ipport = settings.getString(KDSSettings.ID.Printer_ipport);
        String comNumber =  settings.getString(KDSSettings.ID.Printer_serial);

        PortInfo portInfo = null;// new PortInfo();
        switch (printerPortType)
        {

            case USB:

                portInfo = new PortInfo("USB", 0);

                break;
            case Socket:
                portInfo = new PortInfo(ip, KDSUtil.convertStringToInt(ipport, 3000));



                break;
            case Serial:
                portInfo = new PortInfo("COM"+comNumber, 0);
                int baudrate =  settings.getInt(KDSSettings.ID.Printer_baudrate);
                SerialBaudrate sb = SerialBaudrate.values()[baudrate];
                switch (sb)
                {

                    case B9600:
                        portInfo.setBaudRate(9600);
                        break;
                    case B19200:
                        portInfo.setBaudRate(19200);
                        break;
                    case B38400:
                        portInfo.setBaudRate(38400);
                        break;
                    case B57600:
                        portInfo.setBaudRate(57600);
                        break;
                    case B115200:
                        portInfo.setBaudRate(115200);
                        break;
                    default:
                        portInfo.setBaudRate(19200);
                }
                break;
        }
        return portInfo;


    }

    /**
     * don't call showmsg function in this.
     * This was called by thread, maybe. The showmsg cause crush!! while thread call it.
     * @return
     */
    public boolean open(boolean bFromThread)
    {

       if (!isEnabled()) return false;
        if (m_bemaPrinter == null) return false;
//        if (isOpened()) return true;
        KDSSettings settings = m_kds.getSettings();
        PortInfo portInfo = getPortInfo(settings);
        portInfo.setUsbManager(m_usbManager);
        portInfo.setIntent(m_PermissionIntent);

        int nResult = m_bemaPrinter.open(portInfo);
        boolean bOpen = (nResult == BemaPrinter.OK);
        if (bOpen)
        {
            if (!bFromThread)
                showMsg("Printer open successfully");
            init_printer_after_open();
            //m_bemaPrinter.setCodePage(m_codepage);

        }
        else
        {
            if (!bFromThread) {
                showMsg("Printer open failed err=" + KDSUtil.convertIntToString(nResult));
            }
        }
        return bOpen;

    }

    final byte[] m_init_command = new byte[]{0x1b, 0x40} ;//reset printer
    public void init_printer_after_open()
    {
        m_bemaPrinter.write(m_init_command);
        m_bemaPrinter.setCodePage(m_codepage);


    }

    public  void showMsg(String msg)
    {
        m_kds.showMessage(msg);
    }
    public boolean close()
    {
        if (m_bemaPrinter == null) return false;
        return (m_bemaPrinter.close() == BemaPrinter.OK);
    }

    public void updateSettings(KDSSettings settings)
    {

        //
        String s = settings.getString(KDSSettings.ID.Printer_template);
        this.setTemplate(s);

        m_nCopies = settings.getInt(KDSSettings.ID.Printer_copies);
        m_nPortType = PrinterPortType.values()[ settings.getInt(KDSSettings.ID.Printer_Port)];
        try {
            m_howtoPrint = HowToPrintOrder.values()[settings.getInt(KDSSettings.ID.Printer_howtoprint)];
        }
        catch (Exception e)
        {

        }

        m_bGroupCategory = settings.getBoolean(KDSSettings.ID.Item_group_category);//2.0.48


        updateCodepage(settings);
        if (m_bemaPrinter!= null) {
            if (this.isEnabled()) {
                this.close();
                this.open(false);
            }
        }


    }

    public void updateCodepage(KDSSettings settings)
    {
        int n = settings.getInt(KDSSettings.ID.Printer_codepage);
        BemaPrinter.CodePage cp = BemaPrinter.CodePage.values()[n];
        m_codepage = cp;
    }


    private final int PING_TIMES = 2;
    /**
     * it is for tcp/ip printer,
     * Call this function from KDS class, ping thread.
     * every 1 second
     */
    public void onPing()
    {
        if (!isEnabled()) return;
        switch (m_nPortType)
        {

            case USB:
                checkUsbPrinter();
                break;
            case Socket:
                checkTcpPrinter(); //don't block the app. check it here, others function just check variable m_bNetworkPrinterValid.
                break;
            case Serial:
                checkSerialPrinter();
                break;
        }

        //2.0.13
        //remove it from onping function, use its own thread.
        //writeToPrinter();

    }

    private void checkSerialPrinter()
    {
        if (m_bemaPrinter == null) return;
        PrinterStatus ps = m_bemaPrinter.getStatus();
        m_bSerialPrinterValid =  ps.isOnline();
        if (!m_bSerialPrinterValid)
        {
            this.reset();
        }
    }
    /**
     * check if the usb printer existed.
     */
    private void checkUsbPrinter()
    {
        if (isUsbPrinterValid())
        {
            if (isOpened()) return;
            this.open(true);//from thread call it.
        }
        else
        {
            this.close();
        }
    }
    private void checkTcpPrinter()
    {
        String ip = m_kds.getSettings().getString(KDSSettings.ID.Printer_ip);
        if (ip.isEmpty()) {
            m_bNetworkPrinterValid = false;
            return;
        }
        if (!KDSSocketTCPSideBase.ping(ip, PING_TIMES)) {
            m_bNetworkPrinterValid = false;
            m_kds.getPrinter().close();
        } else
            m_bNetworkPrinterValid = true;
    }

    private static final String ACTION_USB_PERMISSION = "com.bematechus.kds.USB_PERMISSION";

    public void initBemaPrinter(Context context, UsbManager manager)
    {
        m_usbManager = manager;

        //PendingIntent mPermissionIntent;

        m_PermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        m_bemaPrinter = new BemaPrinter(null, manager, m_PermissionIntent);
    }

    private boolean isUsbPrinterValid()
    {
        try {
            return UsbPort.findPrinter(m_usbManager, UsbPort.LR2000_VID, UsbPort.LR2000_PID);
        }
        catch (Exception e)
        {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() , e);
            //KDSLog.e(TAG, KDSUtil.error( e));
            return false;
        }

    }
    public boolean isPrinterValid()
    {

        KDSSettings settings = m_kds.getSettings();
        PortInfo portInfo = this.getPortInfo(settings);
        switch (portInfo.getType())
        {

            case SERIAL:
                return m_bSerialPrinterValid;


            case USB:
            {
              return isUsbPrinterValid();
            }

            case TCP: //ping it
                return m_bNetworkPrinterValid;

            case UNDEFINED:
                break;
        }
        return false;

    }
    public boolean isOpened()
    {

        return m_bemaPrinter.isOpened();
    }
    public  boolean isEnabled()
    {
        boolean bEnabled = m_kds.getSettings().getBoolean(KDSSettings.ID.Printer_Enabled);
        return bEnabled;
    }

    public void printOrder(KDSDataOrder order)
    {
        if (!isEnabled()) return;
        //debug
        //String strDebug = m_bemaPrinter.debug_searchForUsbPrinters();
        if ((m_bemaPrinter.getCommunicationPort()!= null) && m_bemaPrinter.getCommunicationPort().isOpen())
            showMsg("Printer is opened");
        else
            showMsg("Printer is not opened");

        if (isPrinterValid())
            showMsg("Printer valid");
        else
            showMsg("Printer invalid");

        this.printOrderToBuffer(order);
        if (!isOpened())
        {
            this.open(false);
        }
        startPrintingThread();
//        if (isOpened())
//        {
//            showMsg("Write data to printer");
//            writeToPrinter();
//        }
    }

    byte[] LR2000_START_BOLD = new byte[]{0x1b, 0x45, 1};
    byte[] LR2000_END_BOLD = new byte[]{0x1b, 0x45, 0};
    byte[] LR2000_PAPER_CUT = new byte[]{0x1b, 0x6d};


    /**
     * each time, we write how many lines to printer.
     * This is prevent the port buffer full!!!
     */
    private final int MAX_WRITE_COUNT = 100;

    /**
     *
     */
    private void writeToPrinter()
    {
        if (!isPrinterValid())
            return;

        int ncount = m_printerData.size();
        //if (BuildVer.isDebug())
        //System.out.println("printer buffer lines=" + ncount);
        if (ncount <=0) return;
        //2.0.13
        int nWriteCount = ncount > MAX_WRITE_COUNT?MAX_WRITE_COUNT:ncount;
        synchronized (m_locker) {
            for (int i = 0; i < nWriteCount; i++) {
                String s = m_printerData.get(0); //2.0.13
                writeString(s);
                //debug
                //Log.e(TAG, s);
                m_printerData.remove(0); //2.0.13
            }
        }
    }
    private void writeString(String s)
    {
        String willPrint = "";
        char ch = 0;
        for (int i=0; i< s.length(); i++)
        {
            ch = s.charAt(i);
            if (ch == CMD_START_BOLD)
            {
                if (!willPrint.isEmpty())
                    m_bemaPrinter.printText(willPrint);
                willPrint = "";
                m_bemaPrinter.write(LR2000_START_BOLD);

            }
            else if (ch == CMD_END_BOLD)
            {
                if (!willPrint.isEmpty())
                    m_bemaPrinter.printText(willPrint);
                willPrint = "";
                m_bemaPrinter.write(LR2000_END_BOLD);
            }
            else if (ch == CMD_PAPER_CUT)
            {
                if (!willPrint.isEmpty())
                    m_bemaPrinter.printText(willPrint);
                willPrint = "";
                m_bemaPrinter.write(LR2000_PAPER_CUT);
            }
            else
            {
                willPrint += ch;
            }
        }

        if (!willPrint.isEmpty())
            m_bemaPrinter.printText(willPrint);
    }

    Thread m_threadPrinting = null;

    /**
     * Move some timer functions to here.
     * Just release main UI.
     * All feature in this thread are no ui drawing request.
     * And, in checkautobumping function, it use message to refresh UI.
     */
    public void startPrintingThread()
    {
        if (m_threadPrinting == null ||
                !m_threadPrinting.isAlive())
        {
            m_threadPrinting = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (getKDS().isThreadRunning())
                    {
                        try {
                            if (m_threadPrinting != Thread.currentThread())
                                return;
                            if (m_printerData.size() > 0)
                                onPing();
                            writeToPrinter();
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {

                            }
                        }
                        catch ( Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            m_threadPrinting.setName("Printing");
            m_threadPrinting.start();
        }
    }

    public void reset()
    {
        this.close();
        try
        {
            Thread.sleep(200);
        }
        catch (Exception e)
        {

        }
        this.open(true);
    }
}
