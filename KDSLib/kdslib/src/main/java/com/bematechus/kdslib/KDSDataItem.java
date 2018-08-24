
package com.bematechus.kdslib;


import android.graphics.Paint;
import android.graphics.Point;

import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSDataItem extends KDSData {

    //2.0.9
    //This is for lineitems show qty change in new line
    // I use this tag to distingush it with normal item
    //This tag will be prefix of item guid. See KDSStationFunc.orderAddChangedItemEntry function.
    final static public String TAG_QTY_CHANGED_APPEND_ITEM = "QTYCHG";
    //
    protected int     m_nOrderID = -1;
    protected String m_strOrderGUID = "";
    protected String m_strItemName = "";
    protected String m_strDescription = "";
    protected float   m_fltQty = 0;
    protected String m_strCategory = "";
    protected int     m_nBG = 0;
    protected int     m_nFG = 0;
    protected int     m_nAddOnGroup = -1;
    protected int     m_nMarked = 0;
    protected int     m_nRemoved = 0;

    protected int     m_nReady = 0; //0 false, 1 true, in expeditor station, use it.


    protected KDSToStations m_toStations = new KDSToStations();
    protected KDSToStations m_bumpedStations = new KDSToStations(); //for expeditor stations

    protected KDSToStations m_hiddenStations = new KDSToStations(); //just for parse xml. If this station is hidden, set "hidden" variable.

    protected KDSDataCondiments m_arCondiments = new KDSDataCondiments();
    protected KDSDataMessages m_arMessages = new KDSDataMessages();

    protected KDSDataModifiers m_modifiers = new KDSDataModifiers();

    /***************************************************************************/
    //for parse xml file
    protected int m_nTransactionType = KDSDataOrder.TRANSTYPE_ADD;

    //
    protected boolean m_bLocalBumped = false; //local station bumped this item
    protected boolean m_bDeletedByRemoteCommand = false;
    protected  boolean m_bHidden = false;

    protected float m_fltItemDelay = 0; //smart order
    protected float m_fltPreparationTime = 0; //smart order, minutes

    //for items qty changed
    protected float m_fltChangedQty = 0;
    //2.0.9
    protected int m_nItemTimerStartDelay = 0; // add increase qty to new line(LineItems mode). And, the timer from qty changed.
    protected String m_lineItemParentGuid = ""; //for new item of qty changed. This is the parent item guid.
    //
    ArrayList<KDSDataConsolidatedItem> m_arConsolidatedItems = new ArrayList<>();

    boolean m_bKDSStationChangedToBackup = false; //

    protected CSVStrings m_arBuildCard = new CSVStrings();
    protected CSVStrings m_arVideo = new CSVStrings();

    protected KDSDataSumNames m_sumNames = new KDSDataSumNames();
    protected  boolean m_sumNamesEnabled = false;

    protected int m_nScheduleReadQty = 0; //for schedule order process.

    Paint.Align m_alignText = Paint.Align.LEFT;

    protected float m_fltCategoryDelay=0; //preparation time mode.


    protected int m_nCategoryPriority = -1; //2.0.47
    /////////////////////
    //public ArrayList<Point> m_tempShowMeNeedBlockLines = new ArrayList<>();//1; //for text wrap, saveing it here is for  efficiency.
    /**
     * for expeditor item
     * Item tage with itemtype attribute.
     */
    public enum ITEM_TYPE
    {
        Normal, //normal item
        Exp, //expeditor item
    }

    public enum VALID_ITEM_XML_FIELD
    {
        Name,
        Description,
        Qty,
        Category,
        ToStations,
        BG,
        FG,
        Messages,
        ItemDelay,
        PreparationTime,
        BuildCard,
        TrainingVideo,
        Schedule_Ready_Qty,
        CategoryDelay,
        //Destination,
        Count
    };
    protected boolean[] m_arValidFields;

    /***************************************************************************/


    /***************************************************************************
     *
     */
    public KDSDataItem()
    {
        m_arValidFields = new boolean[VALID_ITEM_XML_FIELD.Count.ordinal()];
        // m_nComponentType = ComponentType.Item;
        resetXmlFieldsValidFlag();
    }
    public KDSDataItem(String orderGUID)
    {
        this.setOrderGUID(orderGUID);
        m_arValidFields = new boolean[VALID_ITEM_XML_FIELD.Count.ordinal()];

        resetXmlFieldsValidFlag();

    }

    public KDSDataItem(String orderGUID, String myGuid)
    {
        super(myGuid);
        this.setOrderGUID(orderGUID);
        m_arValidFields = new boolean[VALID_ITEM_XML_FIELD.Count.ordinal()];

    }

    public void resetXmlFieldsValidFlag()
    {
        for (int i = 0; i< VALID_ITEM_XML_FIELD.Count.ordinal(); i++ )
        {
            m_arValidFields[i] = false;
        }
    }
    public void setXmlFieldValid(VALID_ITEM_XML_FIELD field)
    {
        m_arValidFields[field.ordinal()] = true;
    }
    public boolean getXmlFieldValid(VALID_ITEM_XML_FIELD field)
    {
        return m_arValidFields[field.ordinal()];
    }

    public boolean getLocalBumped()
    {
        return m_bLocalBumped;
    }
    public void setLocalBumped(boolean bBumped)
    {
        m_bLocalBumped = bBumped;
    }

    public boolean getDeleteByRemoteCommand()
    {
        return m_bDeletedByRemoteCommand;
    }
    public void setDeleteByRemoteCommand(boolean bDeleteByXml)
    {
        m_bDeletedByRemoteCommand = bDeleteByXml;
    }

    public void setTransType(int nType)
    {
        m_nTransactionType = nType;
    }
    public int getTransType()
    {
        return m_nTransactionType;
    }
    public KDSDataModifiers getModifiers()
    {
        return m_modifiers;
    }
    public void setModifiers(KDSDataModifiers modifiers)
    {
        m_modifiers = modifiers;
    }

    public KDSDataCondiments getCondiments()
    {
        return m_arCondiments;
    }
    public void setCondiments(KDSDataCondiments condiments)
    {
        m_arCondiments = condiments;
    }
    public KDSDataMessages getMessages()
    {
        return m_arMessages;
    }
    public void setMessages(KDSDataMessages msgs)
    {
        m_arMessages = msgs;
    }
    public int getOrderID()
    {
        return m_nOrderID;
    }
    public void setOrderID(int nID)
    {
        m_nOrderID = nID;
    }

    public String getOrderGUID()
    {
        return m_strOrderGUID;
    }
    public void setOrderGUID(String strGUID)
    {
        m_strOrderGUID = strGUID;
    }

    public void setItemName(String name)
    {
        m_strItemName = name;
    }
    public String getItemName()
    {
        return m_strItemName;
    }
    public void setDescription(String strDescriptioin)
    {
        m_strDescription = strDescriptioin;
    }
    public String getDescription()
    {
        return m_strDescription;
    }
    public float getQty()
    {
        return m_fltQty;
    }

    public float getChangedQty()
    {
        return m_fltChangedQty;
    }
    public void setChangedQty(float fltChangedQty)
    {
        m_fltChangedQty = fltChangedQty;
    }

    /**
     * count the consolidated items, and show this qty
     * @return
     */
    public float getShowingQty()
    {
        return getQty() + getConsolidatedQty() + getChangedQty();
    }
    public void setQty(float fltQty)
    {
        m_fltQty = fltQty;
    }
    public void setCategory(String strCategory)
    {
        m_strCategory = strCategory;
    }
    public String getCategory()
    {
        return m_strCategory;
    }

    public boolean isQtyChanged()
    {
        return (getChangedQty() !=0);
    }

    public int getBG()
    {
        return m_nBG;
    }
    public void setBG(int nBG)
    {
        m_nBG = nBG;
    }
    public int getFG()
    {
        return m_nFG;
    }
    public void setFG(int nFG)
    {
        m_nFG = nFG;
    }
    public boolean isAssignedColor()
    {
        if (m_nFG != 0 || m_nBG != 0)
            return true;
        return false;
    }
    public int getAddOnGroup()
    {
        return m_nAddOnGroup;
    }
    public void setAddOnGroup(int nGroup)
    {
        m_nAddOnGroup = nGroup;
    }

    public int getMarked()
    {
        return m_nMarked;
    }
    /**
     * mark it in local station
     * @param nMarked
     */
    public void setMarked(int nMarked)
    {
        m_nMarked = nMarked;
    }

    public void setToStationsString(String strToStations)
    {
        m_toStations.parseString(strToStations);

    }
    public String getToStationsString()
    {

        return m_toStations.getString();

    }

    public void setStationChangedToBackup(boolean bBackup)
    {
        m_bKDSStationChangedToBackup = bBackup;
    }
    public boolean getStationChangedToBackup()
    {
        return m_bKDSStationChangedToBackup;
    }


    public KDSToStations getToStations()
    {

        return m_toStations;

    }
    /**
     *
     * @return
     *      1: Ready
     *      0: false
     */
    public int getReady()
    {
        return m_nReady;
    }
    /**
     *
     * Expeditor station get normal station item ready notification
     * then, set item to ready status
     * @param nReady
     */
    public void setReady(int nReady)
    {
        m_nReady = nReady;
    }

    public String sqlAddNew(String tblName)
    {
        if (tblName.isEmpty())
            tblName = "items";

        String sql = "insert into "
                + tblName
                + " ("
                //+ "OrderID ,GUID , OrderGUID, Name ,Description ,Qty ,"
                + "GUID,OrderGUID,Name,Description,Qty,QtyChanged,"
                + "Category,BG,FG,Grp,Marked,"
                + "LocalBumped,Ready,Hiden,ToStations,BumpedStations,ItemDelay,PreparationTime,DeleteByRemote,ItemType,BuildCard,TrainingVideo,SumTransEnable,SumTrans,r0,r1,r2) values ("
                //+  getOrderID() + ","
                + "'" + getGUID() + "',"
                + "'" + getOrderGUID() + "'," //use order guid as id
                + "'" + fixSqliteSingleQuotationIssue( getItemName()) + "',"
                + "'" + fixSqliteSingleQuotationIssue( getDescription()) + "',"
                + KDSUtil.convertFloatToString(getQty())
                + ",0,"
                + "'" +fixSqliteSingleQuotationIssue(  getCategory()) + "',"
                + KDSUtil.convertIntToString(getBG()) + ","
                + KDSUtil.convertIntToString(getFG()) + ","
                + KDSUtil.convertIntToString(getAddOnGroup()) + ","
                + KDSUtil.convertIntToString(getMarked()) + ","
                + KDSUtil.convertBoolToString(getLocalBumped()) + ","
                //+"'" + getToStationsString() + "',"
                + KDSUtil.convertIntToString(getReady()) + ","
                + KDSUtil.convertBoolToString(getHidden()) + ",'"
                + getToStationsString() + "','"
                + getBumpedStationsString() + "',"
                + KDSUtil.convertFloatToString(getItemDelay()) + ","
                + KDSUtil.convertFloatToString(getPreparationTime()) + ","
                + KDSUtil.convertBoolToString(getDeleteByRemoteCommand()) + ","
                + KDSUtil.convertIntToString(getItemType().ordinal()) +  ",'"
                + getBuildCard().toCSV() +"','"
                + getTrainingVideo().toCSV() +"',"
                + KDSUtil.convertBoolToString(getSumNamesEnabled()) + ",'"
                + getSumNames().toString() +"'"
                + ",'" + KDSUtil.convertIntToString(getTimerDelay()) + "'"
                +",'" + getParentGuid() +"'"
                +"," + KDSUtil.convertIntToString(getCategoryPriority()) //2.0.47
                +")";
        return sql;


    }

    public String sqlUpdate()
    {
        String sql = "update items set "
                //   + "OrderID='" + getOrderID() + "',"
                // + "GUID='"+ getGUID() + "',"
                + "OrderGUID='"+ getOrderGUID() + "'," //the order guid.
                + "Name='"+ getItemName() + "',"
                + "Description='" +KDSUtil.fixSqliteSingleQuotationIssue( getDescription()) + "',"
                + "Qty=" + KDSUtil.convertFloatToString(getQty()) + ","
                + "Category='" +KDSUtil.fixSqliteSingleQuotationIssue( getCategory()) + "',"
                + "BG=" + KDSUtil.convertIntToString(getBG()) + ","
                + "FG="+ KDSUtil.convertIntToString(getFG()) + ","
                + "Grp=" + KDSUtil.convertIntToString(getAddOnGroup()) + ","
                + "Marked=" + KDSUtil.convertIntToString(getMarked()) + ","
                + "LocalBumped=" + KDSUtil.convertBoolToString(getLocalBumped()) + ","
                //+ "Dest='" + getToStationsString() + "',"
                + "Ready="+ KDSUtil.convertIntToString(getReady()) + ","
                + "DeleteByRemote="+ KDSUtil.convertBoolToString(getDeleteByRemoteCommand()) + ","
                + "Hiden="+ KDSUtil.convertBoolToString(getHidden()) + ","
                + "ToStations='"+getToStationsString() + "',"
                + "BumpedStations='"+getBumpedStationsString() + "',"
                + "QtyChanged=" + KDSUtil.convertFloatToString(getChangedQty()) + ","
                + "ItemDelay=" + KDSUtil.convertFloatToString(getItemDelay()) + ","
                + "PreparationTime=" + KDSUtil.convertFloatToString(getPreparationTime()) + ","
                + "ItemType=" + KDSUtil.convertIntToString(getItemType().ordinal()) + ","
                + "BuildCard='"+getBuildCard().toCSV() +"',"
                + "TrainingVideo='"+getTrainingVideo().toCSV() +"',"
                + "SumTransEnable=" + KDSUtil.convertBoolToString(getSumNamesEnabled())
                + ",SumTrans='"+getSumNames().toString()+"'"
                + ",r0='" + KDSUtil.convertIntToString( getTimerDelay()) +"'"
                + ",r1='" + getParentGuid() +"'"
                + ",r2=" +  KDSUtil.convertIntToString(getCategoryPriority()) //2.0.47
                + ",DBTimeStamp='"+ KDSUtil.convertDateToString(getTimeStamp()) +"'"
                //+"' where id=" + Common.KDSUtil.ConvertIntToString(getID());
                +" where guid='" + getGUID() + "'";

        return sql;

    }

    public String sqlModify()
    {
        String sql = "update items set ";
        ArrayList<String> ar = new ArrayList<>();

        //+ "OrderGUID='"+ getOrderGUID() + "',"; //the order guid.
        //+ "Name='"+ getItemName() + "',"
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.Description))
            ar.add("Description='" + getDescription() + "'");

        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.Qty))
            ar.add("Qty=" + KDSUtil.convertFloatToString(getQty())) ;
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.Category)) {
            ar.add("Category='" + getCategory() + "'");
            ar.add("CategoryPriority="+KDSUtil.convertIntToString(getCategoryPriority()));//2.0.47
        }
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.BG))
            ar.add("BG=" + KDSUtil.convertIntToString(getBG())) ;
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.FG))
            ar.add(",FG="+ KDSUtil.convertIntToString(getFG()) );

        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.ToStations))
            ar.add("ToStations='"+getToStationsString() + "'");
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.ItemDelay))
            ar.add("ItemDelay="+ KDSUtil.convertFloatToString(getItemDelay())) ;
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.PreparationTime))
            ar.add( "PreparationTime="+ KDSUtil.convertFloatToString(getPreparationTime()) );
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.BuildCard))
            ar.add("BuildCard='"+ getBuildCard().toCSV()+"'");
        if (this.getXmlFieldValid(VALID_ITEM_XML_FIELD.TrainingVideo))
            ar.add("TrainingVideo='"+ getBuildCard().toCSV()+"'");

        for (int i=0; i< ar.size(); i++)
        {
            if (i !=0) sql += ",";
            sql += ar.get(i);
        }

        sql +=" where guid='" + getGUID() + "'";

        return sql;

    }

    public String sqlDelete()
    {

        String sql = sqlDelete("items", getGUID());
        return sql;
    }


    public static String sqlDelete(String tblName, String strGUID)
    {
        if (tblName.isEmpty())
            tblName = "items";
        String sql = "delete from ";
        sql += tblName ;
        sql += " where guid='"+ strGUID + "'";

        return sql;
    }

    public boolean isEqual(KDSDataItem item)
    {
        if (this.getBG() != item.getBG())
            return false;
        if (!this.getCategory().equals(item.getCategory()))
            return false;
        if (!this.getDescription().equals(item.getDescription()))
            return false;
        if (this.getFG() != item.getFG())
            return false;

        if (!this.getToStationsString().equals(item.getToStationsString()))
            return false;

        //check condiments
        if  (!this.getCondiments().isEquals(item.getCondiments()))
            return false;
        if (!this.getMessages().isEquals(item.getMessages()))
            return false;
        return true;
    }
    /**
     *
     * modify this item according to received item
     * @param
     * @return
     */
    public boolean modifyItem(KDSDataItem itemReceived)
    {
        boolean bResult = false;
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.BG))
        {
            this.setBG(itemReceived.getBG());
            bResult = true;
        }
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.Category))
        {
            this.setCategory(itemReceived.getCategory());
            bResult = true;
        }
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.Description))
        {
            this.setDescription(itemReceived.getDescription());
            bResult = true;
        }
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.FG))
        {
            this.setFG(itemReceived.getFG());
            bResult = true;
        }
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.Messages))
        {
            itemReceived.getMessages().copyTo(this.getMessages());
            bResult = true;
        }
        if (itemReceived.getXmlFieldValid(VALID_ITEM_XML_FIELD.Qty))
        {
            if (this.getQty() != itemReceived.getQty()) {
                this.setChangedQty(itemReceived.getQty() - this.getQty());
                //this.setQty(itemReceived.getQty()); //20171206, use the changed qty to identify qty modification.
                bResult = true;
            }
        }
        return bResult;
    }

    /**
     * Format:
     *  <Item>
     *      ....
     *  </Item>
     *
     * @return
     */
    public String createXml()
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root(KDSXMLParserOrder.DBXML_ELEMENT_ITEM);
        if (!outputDataToXml(xml))
            return "";
        return xml.get_xml_string();

    }



    public boolean outputXml(KDSXML pxml)
    {
        pxml.back_to_root();
        pxml.getFirstGroup(KDSXMLParserOrder.DBXML_ELEMENT_ORDER);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM, true);
        return outputDataToXml(pxml);
    }

    private boolean outputDataToXml(KDSXML pxml)
    {

        if (this.isMarked())
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_MARKED, "1");
        if (this.getLocalBumped())
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_LOCAL_BUMPED, "1");
        if (this.getItemType() == ITEM_TYPE.Exp)
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_ITEM_TYPE, "1");

        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID, this.getItemName(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_GUID, this.getGUID(), false);
        if (this.getLocalBumped())
        {
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE, KDSConst.KDS_Transaction_Type_Del, false);
        }
        else
        {
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE, KDSUtil.convertIntToString(this.getTransType()), false);
            //20180301 comments it, we need original transtype.
            //pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE, KDSConst.KDS_Transaction_Type_New, false);
        }

        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_NAME, this.getDescription(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_CATEGORY,this.getCategory(), false);

        String s;


        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_QTY, KDSUtil.convertFloatToString(this.getQty()), false);
        pxml.newGroup("QtyChanged", KDSUtil.convertFloatToString(this.getChangedQty()), false);
        pxml.newGroup("ScheduleReadyQty", KDSUtil.convertFloatToString(this.getScheduleProcessReadyQty()), false);
        //output pre-modifiers properties
        KDSDataMessages messages = this.getMessages();
        KDSDataOrder.outputKDSMessages(pxml, messages, KDSXMLParserOrder.DBXML_ELEMENT_PREMOIDIFIER);

        if (this.getBG() != this.getFG()) { //the color is valid
            pxml.newGroup(KDSConst.KDS_Str_RGBColor, true);
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_BG, KDSUtil.convertIntToString(this.getBG()));
            pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_FG, KDSUtil.convertIntToString(this.getFG()));
            pxml.back_to_parent();
        }
        String stations =this.getToStationsString();
        if (!stations.isEmpty()) {
            if (this.getStationChangedToBackup()) {
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION, stations, true);
                pxml.setAttribute("isbackup", "1");
                pxml.back_to_parent();
            }
            else
            {
                pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_KDSSTATION, stations, false);
            }

        }
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ITEM_DELAY,KDSUtil.convertFloatToString(this.getItemDelay()), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_BUILD_CARD,this.getBuildCard().toCSV(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRAINING_VIDEO,this.getTrainingVideo().toCSV(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_TRANSLATE, this.getSumNames().toString(), true);
        pxml.setAttribute(KDSXMLParserOrder.DBXML_ELEMENT_SUMMARY_ENABLED, this.getSumNamesEnabled()?"1":"0" );
        pxml.back_to_parent();

        KDSDataModifiers modifiers =  this.getModifiers();
        int count = modifiers.getCount();
        for (int i=0; i< count; i++)
        {
            KDSDataModifier c = modifiers.getModifier(i);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_MODIFIER, true);
            outputCondimentToXml(pxml, c);//modifier almost is same as condiment.
            float flt = KDSUtil.convertSecondsToMins( c.getPrepTime()); //PLEASE notice: XML use unit seconds, but in code, I use seconds
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_PREPARATION_TIME,KDSUtil.convertFloatToString(flt), false);

            pxml.back_to_parent();
        }

        KDSDataCondiments condiments =  this.getCondiments();
        count = condiments.getCount();
        for (int i=0; i< count; i++)
        {
            KDSDataCondiment c = condiments.getCondiment(i);
            pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_CONDIMENT, true);
            outputCondimentToXml(pxml, c);

            pxml.back_to_parent();
        }
        return true;
    }

    /**
     * without the "condiment" group name, just output condiment internal data.
     * It is for sharing code in condiment and modifier
     * Notice:
     *  The prepTime in xml is minutes unit.
     *  But, in code, it is seconds.
     * @param pxml
     * @param c
     */
    private void outputCondimentToXml(KDSXML pxml, KDSDataCondiment c)
    {
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ID,c.getCondimentName(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_GUID,c.getGUID(), false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_TRANSTYPE,KDSConst.KDS_Transaction_Type_New, false);
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_NAME,c.getDescription(), false);
        pxml.newGroup( KDSConst.KDS_Str_RGBColor, true);
        pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_BG, KDSUtil.convertIntToString(c.getBG()));
        pxml.newAttribute(KDSXMLParserOrder.DBXML_ELEMENT_COLOR_FG, KDSUtil.convertIntToString(c.getFG()));
        pxml.back_to_parent();
        pxml.newGroup(KDSXMLParserOrder.DBXML_ELEMENT_ACTION, "1", false);
        KDSDataMessages messages = c.getMessages();
        KDSDataOrder.outputKDSMessages(pxml, messages,KDSXMLParserOrder.DBXML_ELEMENT_PREMOIDIFIER);

    }
    public void toggleMark()
    {
        int n = this.getMarked();
        if (n == 0)
            n =1;
        else
            n = 0;
        this.setMarked(n);


    }

    public boolean isReady()
    {
        return ( this.getReady() != 0 );
    }

    public boolean isMarked()
    {
        return (this.getMarked() != 0);
    }
    @Override
    public void copyTo(KDSData obj)
    {
        super.copyTo(obj);
        KDSDataItem to = (KDSDataItem) obj;
        to.setLocalBumped(this.getLocalBumped());
        to.setTransType(this.getTransType());
        to.setLocalBumped(this.getLocalBumped());
        to.setAddOnGroup(this.getAddOnGroup());
        to.setBG(this.getBG());
        to.setFG(this.getFG());
        to.setCategory(this.getCategory());
        to.setDescription(this.getDescription());
        to.setItemName(this.getItemName());
        to.setDeleteByRemoteCommand(this.getDeleteByRemoteCommand());
        to.setMarked(this.getMarked());
        to.setOrderGUID(this.getOrderGUID());
        to.setOrderID(this.getOrderID());
        to.setQty(this.getQty());
        to.setChangedQty(this.getChangedQty());
        to.setReady(this.getReady());
        to.setToStationsString(this.getToStationsString());
        to.setHidden(this.getHidden());
        to.setBumpedStationString(this.getBumpedStationsString());
        to.setPreparationTime(this.getPreparationTime());//smart order
        to.setItemDelay(this.getItemDelay());//smart order
        to.setItemType(this.getItemType());
        to.setCategoryDelay(this.getCategoryDelay());

        to.setAlign(this.getAlign());
        to.setScheduleProcessReadyQty(this.getScheduleProcessReadyQty());

        //2.0.9
        to.setTimerDelay(this.getTimerDelay());
        to.setParentItemGuid(this.getParentGuid());

        //2.0.47
        to.setCategoryPriority(this.getCategoryPriority());

        this.getModifiers().copyTo(to.getModifiers());

        this.getMessages().copyTo(to.getMessages());
        this.getCondiments().copyTo(to.getCondiments());
    }
    public void setHidden(boolean bHidden)
    {
        m_bHidden = bHidden;
    }
    public boolean getHidden()
    {
        return m_bHidden;
    }

    public String getBumpedStationsString()
    {
        return m_bumpedStations.getString();
    }
    public void setBumpedStationString(String strStations)
    {
        m_bumpedStations.parseString(strStations);
    }
    public boolean addRemoteBumpedStation(String stationID)
    {
        m_bumpedStations.addStation(stationID);
        return true;
    }

    public boolean removeRemoteBumpedStation(String stationID)
    {
        m_bumpedStations.removeStation(stationID);
        return true;
    }

    /**
     * check if the name and its condiments is same, except the qty.
     * we don't check qty at here.
     * This is for consolidate items.
     * @param item
     * @return
     */
    public boolean isSameShowingItem(KDSDataItem item)
    {
        if (!item.getDescription().equals(this.getDescription()))
            return false;
        KDSDataCondiments condiments = this.getCondiments();
        return condiments.isEqualsNoSort(item.getCondiments());

    }
    public ArrayList<KDSDataConsolidatedItem> getConsolidatedItems()
    {
        return m_arConsolidatedItems;
    }

    public void addConsolidatedItem(String itemGuid, float qty)
    {
        m_arConsolidatedItems.add(new KDSDataConsolidatedItem(itemGuid, qty));
    }

    /**
     * how many qty in consolidate item array.
     * @return
     */
    public float getConsolidatedQty()
    {
        float fltqty = 0;
        for (int i=0; i< m_arConsolidatedItems.size(); i++)
        {
            fltqty += m_arConsolidatedItems.get(i).getQty();
        }
        return fltqty;
    }

    public boolean isAddonItem()
    {
        return (m_nAddOnGroup>=0);
    }

    public void setItemDelay(float fltDelay)
    {
        m_fltItemDelay = fltDelay;
    }

    public float getItemDelay()
    {
        return m_fltItemDelay;
    }
    public void setPreparationTime(float fltPreparationTime)
    {
        m_fltPreparationTime = fltPreparationTime;
    }

    public float getPreparationTime()
    {
        return m_fltPreparationTime;
    }

    public float getTotalPrepTime()
    {
        return getPreparationTime() + getModifiersTotalPrepTimeAsMins();// getModifiersMaxPrepTimeAsMins();
    }
    /**
     * for exp item
     */
    protected ITEM_TYPE m_itemType = ITEM_TYPE.Normal;
    public ITEM_TYPE getItemType()
    {
        return m_itemType;
    }

    public void setItemType(ITEM_TYPE t)
    {
        m_itemType = t;
    }
    public boolean isExpitem()
    {
        return (m_itemType == ITEM_TYPE.Exp);
    }


    public CSVStrings getBuildCard()
    {
        return m_arBuildCard;
    }
    public CSVStrings getTrainingVideo()
    {
        return m_arVideo;
    }

    public void setBuildCard(String csvString)
    {
        m_arBuildCard = CSVStrings.parse(csvString);
    }

    public void setTrainingVideo(String csvString)
    {
        m_arVideo = CSVStrings.parse(csvString);
    }

    public void setSumNamesEnabled(boolean bEnabled)
    {
        m_sumNamesEnabled = bEnabled;
    }
    public boolean getSumNamesEnabled()
    {
        return m_sumNamesEnabled;
    }

    public void setSumNames(KDSDataSumNames sumNames)
    {
        m_sumNames = sumNames;
    }
    public KDSDataSumNames getSumNames()
    {
        return m_sumNames;
    }

    public void setScheduleProcessReadyQty(int nQty)
    {
        m_nScheduleReadQty = nQty;
    }
    public int getScheduleProcessReadyQty()
    {
        return m_nScheduleReadQty;
    }
    public void addScheduleProcessReadyQty(int nIncreasedQty)
    {
        m_nScheduleReadQty += nIncreasedQty;
    }

    public void setAlign(Paint.Align align)
    {
        m_alignText = align;
    }
    public Paint.Align getAlign()
    {
        return m_alignText;
    }


    public void setDimColor(boolean bDim)
    {
        super.setDimColor(bDim);
        int ncount = m_arCondiments.getCount();
        for (int i=0; i< ncount; i++)
        {
            m_arCondiments.get(i).setDimColor(bDim);
        }
        ncount = m_arMessages.getCount();
        for (int i=0; i< ncount ; i++)
        {
            m_arMessages.get(i).setDimColor(bDim);
        }

        ncount = m_modifiers.getCount();
        for (int i=0; i< ncount; i++)
        {
            m_modifiers.get(i).setDimColor(bDim);
        }
    }

    /**
     * keep it while parse xml string. Don't save to database.
     * It is different with windows KDS.
     * After do filter, it will change to "hidden" variable.
     * @param hiddenStations
     */
    public void setHiddenStations(String hiddenStations)
    {

        m_hiddenStations.parseString(hiddenStations);
    }
    public boolean isHiddenStation(String stationID)
    {
        if (!m_hiddenStations.isAssigned()) return false;
        return (m_hiddenStations.findStation(stationID) != KDSToStations.PrimarySlaveStation.Unknown);
    }

    public void setHiddenAccordingToHiddenStations(String stationID)
    {
        setHidden(isHiddenStation(stationID));
        for (int i=0; i< getCondiments().getCount(); i++)
        {
            getCondiments().getCondiment(i).setHiddenAccordingToHiddenStations(stationID);
        }
    }

    public void setCategoryDelay(float flt)
    {
        m_fltCategoryDelay = flt;
    }
    public float getCategoryDelay()
    {
        return m_fltCategoryDelay;
    }

//    /**
//     * Unit is "Seconds".
//     * @return
//     */
//    public float getModifiersMaxPrepTimeAsMins()
//    {
//        if (m_modifiers.getCount()<=0) return 0;
//        int nMax = 0;
//        for (int i=0; i<m_modifiers.getCount(); i++)
//        {
//            if (m_modifiers.getModifier(i).getPrepTime() > nMax)
//                nMax = m_modifiers.getModifier(i).getPrepTime();
//        }
//
//        return ((float) nMax/60f);
//    }
    /**
     * Unit is "Seconds".
     * @return
     */
    public float getModifiersTotalPrepTimeAsMins()
    {
        if (m_modifiers.getCount()<=0) return 0;
        int nTotal = 0;
        for (int i=0; i<m_modifiers.getCount(); i++)
        {
            if (m_modifiers.getModifier(i).getPrepTime() > 0)
                nTotal += m_modifiers.getModifier(i).getPrepTime();
        }

        return ((float) nTotal/60f);
    }

    /**
     * This is for line items mode
     * @param nSeconds
     *  The seconds from order started
     */
    public void setTimerDelay(int nSeconds)
    {
        m_nItemTimerStartDelay = nSeconds;
    }
    public int getTimerDelay()
    {
        return m_nItemTimerStartDelay;
    }

    //2.0.9
    public void setParentItemGuid(String guid)
    {
        if (guid == null) guid = "";//2.0.17
        m_lineItemParentGuid = guid;
    }
    public String getParentGuid()
    {
        return m_lineItemParentGuid;
    }
    public boolean isQtyChangeLineItem()
    {
        return (!m_lineItemParentGuid.isEmpty());
    }

    public void updateCondimentsModifersMessagesParentGuid()
    {
        for (int i=0; i<m_modifiers.getCount(); i++)
        {
            m_modifiers.getModifier(i).setItemGUID(this.getGUID());
        }

        for (int i=0; i< m_arCondiments.getCount(); i++)
            m_arCondiments.getCondiment(i).setItemGUID(this.getGUID());

        for (int i=0; i< m_arMessages.getCount(); i++)
            m_arMessages.getMessage(i).setComponentGUID(this.getGUID());
    }

    /**
     * If item goes to multiple station, this check if all station bumped this item.
     * It will been call in expo
     * @return
     */
    public boolean isAllStationBumpedInExp(ArrayList<String> arExpo)
    {

        KDSToStations bumpedStations = m_bumpedStations;

        if (bumpedStations.getCount()<=0) return false;
        KDSToStations toStations = this.getToStations();
        if (toStations.getCount()  <=0) return true;
        for (int i=0; i< toStations.getCount(); i++)
        {
            if (KDSUtil.isExistedInArray(arExpo, toStations.getToStation(i).getPrimaryStation()))
                continue;
            if (bumpedStations.findStation(toStations.getToStation(i).getPrimaryStation()) != KDSToStations.PrimarySlaveStation.Unknown)
                continue;
            return false;

        }
        return true;
    }

    /**
     * 2.0.47
     * @param nPriority
     */
    public void setCategoryPriority(int nPriority)
    {
        if (nPriority!=-1)
            m_nCategoryPriority = nPriority;
    }
    public int getCategoryPriority()
    {
        return m_nCategoryPriority;
    }
}
