/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bematechus.kdslib;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 *
 * @author David.Wong
 *
 * Parse the command buffer
 * All command use same format:
 * STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
 */
public class KDSSocketTCPCommandBuffer {

    static public final byte STX = 0x02;
    static public final byte ETX = 0x03;

    //command

    //data lenght bytes is Low to High
    static public final byte XML_COMMAND = 0x16; //following xml format command text, uft8,,,, STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
    //this command just have two byte for length
    static public final byte XML_COMMAND_WIN_KDS = 0x05; //following xml format command text, uft8,,,, STX, Command, LEN0,LEN1,d0,d1,d2 ... ETX

    public static final byte UDP_REQ_STATION = 0x17;
    public static final byte UDP_RET_STATION = 0x18;
    public static final byte UDP_SHOW_ID = 0x19;
    public static final byte UDP_CLEAR_DB = 0x20;
    public static final byte UDP_ASK_RELATIONS = 0x21;
    public static final byte UDP_ASK_BROADCAST_RELATIONS = 0x22;
    public static final byte UDP_STATISTIC_REQ_STATION = 0x23;//for statistic
    public static final byte UDP_QUEUE_EXPO_BUMP = 0x24;//Queue settings
    public static final byte UDP_TT_ORDER_BUMP = 0x25;//TT settings


    public static final byte UDP_ASK_ROUTER = 0x26; //for routers communication announce
    public static final byte UDP_RET_ROUTER = 0x27; //for routers communication announce

    public static final byte UDP_ITEM_BUMPED = 0x28; //for preparation time mode
    public static final byte UDP_ITEM_UNBUMPED = 0x29; //for preparation time mode
    public static final byte UDP_SMART_MODE_ENABLED = 0x30;//Queue settings
    //varibles
    private ByteBuffer m_buffer = ByteBuffer.allocate(BUFFER_SIZE);// new ByteBuffer();
    static public final int BUFFER_SIZE = 102400; //100k
    private int m_fill = 0;
    //
    public KDSSocketTCPCommandBuffer()
    {

        m_fill = 0;
    }
    public ByteBuffer buffer()
    {
        return m_buffer;
    }
    public int fill()
    {
        return m_fill;
    }
    public int command_end()
    {
        int command_end = 3;		// smallest possible packet.
	    if( command_end > m_fill )				//		If too short for packet
        {
            return 0;							//			Report endless.
        }
        switch( command() )
        {
            case XML_COMMAND: // STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
            case UDP_ITEM_BUMPED:
            case UDP_ITEM_UNBUMPED:
            {
                if (m_fill < 7)
                    return 0;
                int nlen = this.getDataLength();
                command_end = nlen + 7;
            }
            break;
            case XML_COMMAND_WIN_KDS: //// STX, Command, High byte,Low Byte,d0,d1,d2 ... ETX
            {
                if (m_fill < 5)
                    return 0;
                int nlen = this.getWinKdsXmlDataLength();
                command_end = nlen + 5;
            }
            break;
            case UDP_REQ_STATION:
            case UDP_SHOW_ID:
            case UDP_ASK_RELATIONS:
            case UDP_ASK_BROADCAST_RELATIONS:
            case UDP_ASK_ROUTER:
            case UDP_CLEAR_DB:
            {
                command_end = 3;
            }
            break;
            case UDP_RET_STATION:
            case UDP_RET_ROUTER:
            {//max 256 string
                int n = m_buffer.get(2);
                command_end = n + 4;
            }
            break;
            case UDP_STATISTIC_REQ_STATION:
            {
                command_end = 3 + 4;//4 bytes for ip address
            }
            break;
            case UDP_QUEUE_EXPO_BUMP:
            case UDP_SMART_MODE_ENABLED:
            {
                command_end = 3 +1;
            }
            break;
            default:
                m_fill = 0;						// 		Trash the buffer.
            return 0;

        }

        if( command_end <= m_fill )				//		If buffer contains whole packet
            return command_end;
        else
            return 0;							//			Report endless.
    }

    public void remove(int ncount)						// Start of valid data.
    {
        int len = 0;
        len = m_fill-ncount;
        if (len <0) len = 0;
        for (int i = ncount; i< m_fill; i++)
            m_buffer.put(i-ncount, m_buffer.get(i));

        m_fill = len;

    }
    
    public void skip_to_STX()
    {
       // System.out.println( m_buffer.toString());
        if( m_fill != 0 && m_buffer.get(0) != STX )
        {
            int i = 0;
            for( i=0; i< m_fill; i++)
            {
                byte b = m_buffer.get(i);

                if (m_buffer.get(i) == STX)
                    break;
            }
            if (i >0)
                remove( i );
        }
    }
    
    public byte command()
    {
        if (m_fill <= 1)
            return 0;
	    return m_buffer.get(1);		// Assuming command at buffer[1]
    }
    
    public int appendData(ByteBuffer buffer, int ncount)
    {

        int nidx = 0;
        int naccept = 0;
        for (int i=m_fill; i< BUFFER_SIZE; i++)
        {
            nidx = i-m_fill;
            if (nidx < ncount)
            {
                m_buffer.put(i, buffer.get(i-m_fill));
                naccept ++;
            }
            else
                break;
        }
        m_fill += naccept;
        return nidx;
    }

    public void reset()
    {
        m_buffer.clear();
        m_fill = 0;
    }
    /**
     * 
     * for DB_DATA command
     * @return 
     */
    public int getDataLength()
    {
        int b0 = (m_buffer.get(5) & 0x0ff);
        int b1 = (m_buffer.get(4) & 0x0ff);
        int b2 = (m_buffer.get(3) & 0x0ff);
        int b3 = (m_buffer.get(2) & 0x0ff);

        int l = 0;
        l |= b0;
        l |= (b1 << 8);
        l |= (b2 << 16);
        l |= (b3 << 24);
        //l |= (b4 << 32);
        //l |= (b5 << 40);

        return l;
    }

    /**
     *STX, Command, High byte,Low Byte,d0,d1,d2 ... ETX
     * for DB_DATA command
     * @return
     */
    public int getWinKdsXmlDataLength()
    {

        int b0 = (m_buffer.get(3) & 0x0ff);
        int b1 = (m_buffer.get(2) & 0x0ff);
        //long b4 = (m_buffer.get(3) & 0x0ff);
        //long b5 = (m_buffer.get(2) & 0x0ff);

        int l = 0;
        l |= b0;
        l |= (b1 << 8);

        return l;
    }

    //// STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
    public byte[] xml_command_data()
    {
        int ncount = getDataLength();
        byte[] bytes = new byte[ncount];
        for (int i=0; i< ncount; i++)
        {
            bytes[i] = m_buffer.get(i + 6);
        }
        return bytes;
    }

    //// STX, Command, LEN0,LEN1,d0,d1,d2 ... ETX
    public byte[] xml_winkds_command_data()
    {
        int ncount = getWinKdsXmlDataLength();
        byte[] bytes = new byte[ncount];
        for (int i=0; i< ncount; i++)
        {
            bytes[i] = m_buffer.get(i + 4);
        }
        return bytes;
    }

    static public void setDataLength(ByteBuffer buf, int nLen)
    {
        byte b0 = (byte)(nLen & 0xffL);
        byte b1 = (byte)((nLen & 0xff00L) >> 8);
        byte b2 = (byte)((nLen & 0xff0000L) >> 16);
        byte b3 = (byte)((nLen & 0xff000000L) >> 24);
        buf.put( b3);
        buf.put( b2);
        buf.put( b1);
        buf.put(b0);
        
        
    }



    static public ByteBuffer buildXMLCommand(String txtXml)
    {

        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(txtXml);
        int nlen = bytes.length;

        ByteBuffer buf = ByteBuffer.allocate(nlen + 2 + 4 + 1); //STX , Code , length(4 bytes) , ETX
        buf.put(STX);
        buf.put(XML_COMMAND);
        setDataLength(buf, nlen);
        buf.put(bytes);
        buf.put(ETX);
        buf.position(0);
        return buf;

    }

    static public ByteBuffer buildRequireStationsCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_REQ_STATION;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    /**
     * <Command><IP0><IP1><IP2><IP3>
     * @param localIP
     * @return
     */
    static public ByteBuffer buildStatisticRequireStationsCommand(ArrayList<Byte> localIP)
    {
        byte[] bytes = new byte[7];
        bytes[0] = STX;
        bytes[1] = UDP_STATISTIC_REQ_STATION;
        for (int i=0; i< localIP.size(); i++)
            bytes[2 + i] = localIP.get(i);
        bytes[2+localIP.size()] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    static public ByteBuffer buildShowStationIDCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_SHOW_ID;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    static public ByteBuffer buildRequireRelationsCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_ASK_BROADCAST_RELATIONS;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    static public ByteBuffer buildAskStationsRelationsCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_ASK_RELATIONS;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);
    }

    static public ByteBuffer buildClearDBCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_CLEAR_DB;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    static public ByteBuffer buildItemBumpUnbumpUdpCommand(String orderName, ArrayList<String> itemNames, boolean bBumped)
    {
        String s = orderName;

        for (int i=0; i< itemNames.size(); i++)
        {
            s += ",";
            s += itemNames.get(i);
        }

        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(s);
        ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1+1+4+1); //STX , Code , length(4 bytes) ,data,  ETX
        buf.put(STX);
        if (bBumped)
            buf.put(UDP_ITEM_BUMPED);
        else
            buf.put(UDP_ITEM_UNBUMPED);
        setDataLength(buf, bytes.length);
        //buf.put((byte)(bytes.length));
        buf.put(bytes);
        buf.put(ETX);
        buf.position(0);
        return buf;
    }

    /**
     * format:
     *  ip,port,mac
     * @param stationID
     * @param IP
     * @param strPort
     * @param macAddress
     * @return
     */
    static public ByteBuffer buildReturnStationIPCommand(String stationID, String IP, String strPort, String macAddress)
    {
        String s = stationID;
        s += ",";
        s += IP;
        s +=",";
        s += strPort;
        s +=",";
        s += macAddress;
        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(s);
        ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1+1+1+1); //STX , Code , length(1 bytes) ,data,  ETX
        buf.put(STX);
        buf.put(UDP_RET_STATION);
        buf.put((byte)(bytes.length));
        buf.put(bytes);
        buf.put(ETX);
        buf.position(0);
        return buf;
    }


    /**
     * format:
     *  ip,port,mac,ordersCount
     *  20170821
     *      ip,port,mac,ordersCount,usermode
     * @param stationID
     * @param IP
     * @param strPort
     * @param macAddress
     * @return
     */
    static public ByteBuffer buildReturnStationIPCommand2(String stationID, String IP, String strPort, String macAddress, int nActiveOrdersCount, int nUserMode)
    {
        String s = stationID;
        s += ",";
        s += IP;
        s +=",";
        s += strPort;
        s +=",";
        s += macAddress;
        s +=",";
        s += KDSUtil.convertIntToString(nActiveOrdersCount);
        s +=",";
        s += KDSUtil.convertIntToString(nUserMode);
        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(s);
        ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1+1+1+1); //STX , Code , length(1 bytes) ,data,  ETX
        buf.put(STX);
        buf.put(UDP_RET_STATION);
        buf.put((byte)(bytes.length));
        buf.put(bytes);
        buf.put(ETX);
        buf.position(0);
        return buf;
    }

    /**
     *
     * @param bEnabled
     * @return
     */
    static public ByteBuffer buildQueueExpoBumpSettingCommand(boolean bEnabled)
    {

        byte[] bytes = new byte[4];
        bytes[0] = STX;
        bytes[1] = UDP_QUEUE_EXPO_BUMP;
        bytes[2] = 1;
        if (!bEnabled)
            bytes[2] = 0;
        bytes[3] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    /**
     * announce this to every router.
     *  Just for prevent the multiple router enabled.
     * @param stationID
     * @param IP
     * @param strPort
     * @param macAddress
     * @param bEnabled
     * @return
     */
    static public ByteBuffer buildRouterStationAnnounceCommand(String stationID, String IP, String strPort, String macAddress, boolean bEnabled, boolean backupMode)
    {
        String s = stationID;
        s += ",";
        s += IP;
        s +=",";
        s += strPort;
        s +=",";
        s += macAddress;
        s +=",";
        if (bEnabled)
            s += "1";
        else
            s += "0";
        s +=",";
        if (backupMode)
            s += "1";
        else
            s += "0";

        byte[] bytes = KDSUtil.convertStringToUtf8Bytes(s);
        ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1+1+1+1); //STX , Code , length(1 bytes) ,data,  ETX
        buf.put(STX);
        buf.put(UDP_RET_ROUTER);
        buf.put((byte)(bytes.length));
        buf.put(bytes);
        buf.put(ETX);
        buf.position(0);
        return buf;
    }


    static public ByteBuffer buildAskRoutersCommand()
    {
        byte[] bytes = new byte[3];
        bytes[0] = STX;
        bytes[1] = UDP_ASK_ROUTER;
        bytes[2] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    public int length()
    {
        return  m_fill;
    }

    public byte[] station_info_command_data()
    {
        int ncount = m_buffer.get(2);

        byte[] bytes = new byte[ncount];
        for (int i=0; i< ncount; i++)
        {
            bytes[i] = m_buffer.get(i + 3);
        }
        return bytes;
    }

    public byte[] router_info_command_data()
    {
        int ncount = m_buffer.get(2);

        byte[] bytes = new byte[ncount];
        for (int i=0; i< ncount; i++)
        {
            bytes[i] = m_buffer.get(i + 3);
        }
        return bytes;
    }

    //// STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
    public byte[] item_bump_unbumed_command_data()
    {
        int ncount = getDataLength();
        byte[] bytes = new byte[ncount];
        for (int i=0; i< ncount; i++)
        {
            bytes[i] = m_buffer.get(i + 6);
        }
        return bytes;
    }


    /**
     *
     * @param nMode
     *  0: disabled,
     *  1. enabled
     * @return
     */
    static public ByteBuffer buildSmartModeEnabledSettingCommand(int nMode)
    {

        byte[] bytes = new byte[4];
        bytes[0] = STX;
        bytes[1] = UDP_SMART_MODE_ENABLED;
        bytes[2] = (byte)nMode;
//        if (!bEnabled)
//            bytes[2] = 0;
        bytes[3] = ETX;
        return ByteBuffer.wrap(bytes);


    }

    /**
     * 2.0.12
     * free the buffer size
     */
    public void freeBuffer()
    {
        m_buffer = ByteBuffer.allocate(0);
    }

}
