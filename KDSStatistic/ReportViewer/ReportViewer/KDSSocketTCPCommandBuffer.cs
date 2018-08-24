
/**
 *
 * @author David.Wong
 * All command use same format:
 * STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
 */
namespace ReportViewer
{
    public class KDSSocketTCPCommandBuffer
    {

        static public byte STX = 0x02;
        static public byte ETX = 0x03;

        //command

        //data lenght bytes is High to Low
        public const  byte XML_COMMAND = 0x16; //following xml format command text, uft8,,,, STX, Command, LEN_Byte3,LEN_Byte2,LEN_Byte1,LEN_Byte0,d0,d1,d2 ... ETX

        //public static byte UDP_REQ_STATION = 0x17;
        //public static byte UDP_RET_STATION = 0x18;
        //public static byte UDP_SHOW_ID = 0x19;
        //public static byte UDP_CLEAR_DB = 0x20;
        //public static byte UDP_ASK_RELATIONS = 0x21;
        //public static byte UDP_ASK_BROADCAST_RELATIONS = 0x22;
        //public static byte UDP_STATISTIC_REQ_STATION = 0x23;
        //sync database
        //    static public final byte REQ_DB = 0x05; //STX, REQ_DB, ETX
        //    static public final byte DB_DATA = 0x07; //STX, DB_DATA, LEN0,LEN1,LEN2,LEN3,LEN4,LEN5, d0,d1,d2 ... ETX
        //    static public final int DB_DATA_CMD_HEADER = 8;
        //upload file to server
        //from client to server
        //    static public final byte UPLOAD_FILTER_DB = 0x08;
        //    static public final byte UPLOAD_SETTINGS = 0x09;

        //static public final byte FILE_DATA = 0x09;

        //
        static public int BUFFER_SIZE = 102400; //100k
        private byte[] m_buffer = new byte[BUFFER_SIZE];// new ByteBuffer();

        private int m_fill = 0;
        //
        public KDSSocketTCPCommandBuffer()
        {
            //m_buffer = ByteBuffer.allocate(BUFFER_SIZE);
            m_fill = 0;
        }
        public byte[] buffer()
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
            if (command_end > m_fill)				//		If too short for packet
            {
                return 0;							//			Report endless.
            }
            switch (command())
            {
                case XML_COMMAND: // STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
                    {
                        if (m_fill < 7)
                            return 0;
                        int nlen = this.getDataLength();
                        command_end = nlen + 7;
                    }
                    break;
                //case UDP_REQ_STATION:
                //case UDP_SHOW_ID:
                //case UDP_ASK_RELATIONS:
                //case UDP_ASK_BROADCAST_RELATIONS:
                //{
                //    command_end = 3;
                //}
                //break;
                //case UDP_RET_STATION:
                //{//max 256 string
                //    int n = m_buffer.get(2);
                //    command_end = n + 4;
                //}
                //break;

                //            case DB_DATA:
                //            case UPLOAD_FILTER_DB:
                //            case UPLOAD_SETTINGS:
                //                return DB_DATA_CMD_HEADER;
                default:
                    m_fill = 0;						// 		Trash the buffer.
                    return 0;

            }

            if (command_end <= m_fill)				//		If buffer contains whole packet
                return command_end;
            else
                return 0;							//			Report endless.
        }

        public void remove(int ncount)						// Start of valid data.
        {
            int len = 0;
            len = m_fill - ncount;
            if (len < 0) len = 0;
            for (int i = ncount; i < m_fill; i++)
                m_buffer[i - ncount] = m_buffer[i];

            m_fill = len;

        }

        public void skip_to_STX()
        {
            // System.out.println( m_buffer.toString());
            if (m_fill != 0 && m_buffer[0] != STX)
            {
                int i = 0;
                for (i = 0; i < m_fill; i++)
                {
                    byte b = m_buffer[i];

                    if (m_buffer[i] == STX)
                        break;
                }
                if (i > 0)
                    remove(i);
            }
        }

        public byte command()
        {
            if (m_fill <= 1)
                return 0;
            return m_buffer[1];		// Assuming command at buffer[1]
        }

        public int appendData(byte[] buffer, int ncount)
        {
            int nidx = 0;
            int naccept = 0;
            for (int i = m_fill; i < BUFFER_SIZE; i++)
            {
                nidx = i - m_fill;
                if (nidx < ncount)
                {
                    m_buffer[i] = buffer[i - m_fill];
                    naccept++;
                }
                else
                    break;
            }
            m_fill += naccept;
            return nidx;
        }

        public void reset()
        {
            //m_buffer.clear();
            m_fill = 0;
        }
        /**
         * 
         * for DB_DATA command
         * @return 
         */
        public int getDataLength()
        {
            int b0 = (m_buffer[5] & 0x0ff);
            int b1 = (m_buffer[4] & 0x0ff);
            int b2 = (m_buffer[3] & 0x0ff);
            int b3 = (m_buffer[2] & 0x0ff);
            //long b4 = (m_buffer.get(3) & 0x0ff);
            //long b5 = (m_buffer.get(2) & 0x0ff);

            int l = 0;
            l |= b0;
            l |= (b1 << 8);
            l |= (b2 << 16);
            l |= (b3 << 24);
            //l |= (b4 << 32);
            //l |= (b5 << 40);

            return l;
        }

        //// STX, Command, LEN0,LEN1,LEN2,LEN3,d0,d1,d2 ... ETX
        public byte[] xml_command_data()
        {
            int ncount = getDataLength();
            byte[] bytes = new byte[ncount];
            for (int i = 0; i < ncount; i++)
            {
                bytes[i] = m_buffer[i + 6];
            }
            return bytes;
        }

        //    static public void setDataLength(byte[] buf, int nLen)
        //    {
        //        byte b0 = (byte)(nLen & 0xffL);
        //        byte b1 = (byte)((nLen & 0xff00L) >> 8);
        //        byte b2 = (byte)((nLen & 0xff0000L) >> 16);
        //        byte b3 = (byte)((nLen & 0xff000000L) >> 24);
        ////        byte b4 = (byte)((nLen & 0xff00000000L) >> 32);
        ////        byte b5 = (byte)((nLen & 0xff0000000000L) >> 40);
        ////        buf.put( b5);
        ////        buf.put( b4);
        //        buf.put( b3);
        //        buf.put( b2);
        //        buf.put( b1);
        //        buf.put(b0);


        //    }
        //
        //    static public ByteBuffer buildRequireDatabaseCommand()
        //    {
        //        ByteBuffer buf =ByteBuffer.allocate(3);
        //
        //        buf.clear();
        //
        //        buf.put((byte)STX);
        //        buf.put((byte)REQ_DB);
        //        buf.put((byte)ETX);
        //        buf.rewind();
        //        return buf;
        //
        //
        //    }
        // static public int buildXMLCommand(ByteBuffer buf, String txtXml)
        //{
        //    buf.put(STX);
        //    buf.put(XML_COMMAND);
        //    byte[] bytes = KDSUtil.convertStringToUtf8Bytes(txtXml);
        //    int nlen = bytes.length;
        //    setDataLength(buf, nlen);
        //    buf.put(bytes);
        //    buf.put(ETX);
        //    return nlen + 7;

        //}

        //static public ByteBuffer buildXMLCommand(String txtXml)
        //{

        //    byte[] bytes = KDSUtil.convertStringToUtf8Bytes(txtXml);
        //    int nlen = bytes.length;

        //    ByteBuffer buf = ByteBuffer.allocate(nlen + 2 + 4 + 1); //STX , Code , length(4 bytes) , ETX
        //    buf.put(STX);
        //    buf.put(XML_COMMAND);
        //    setDataLength(buf, nlen);
        //    buf.put(bytes);
        //    buf.put(ETX);
        //    buf.position(0);
        //    return buf;

        //}

        //static public ByteBuffer buildRequireStationsCommand()
        //{
        //    byte[] bytes = new byte[3];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_REQ_STATION;
        //    bytes[2] = ETX;
        //    return ByteBuffer.wrap(bytes);


        //}

        ///**
        // * <Command><IP0><IP1><IP2><IP3>
        // * @param localIP
        // * @return
        // */
        //static public ByteBuffer buildStatisticRequireStationsCommand(ArrayList<Byte> localIP)
        //{
        //    byte[] bytes = new byte[7];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_STATISTIC_REQ_STATION;
        //    for (int i=0; i< localIP.size(); i++)
        //        bytes[2 + i] = localIP.get(i);
        //    bytes[2+localIP.size()] = ETX;
        //    return ByteBuffer.wrap(bytes);


        //}

        //static public ByteBuffer buildShowStationIDCommand()
        //{
        //    byte[] bytes = new byte[3];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_SHOW_ID;
        //    bytes[2] = ETX;
        //    return ByteBuffer.wrap(bytes);


        //}

        //static public ByteBuffer buildRequireRelationsCommand()
        //{
        //    byte[] bytes = new byte[3];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_ASK_BROADCAST_RELATIONS;
        //    bytes[2] = ETX;
        //    return ByteBuffer.wrap(bytes);


        //}

        //static public ByteBuffer buildAskStationsRelationsCommand()
        //{
        //    byte[] bytes = new byte[3];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_ASK_RELATIONS;
        //    bytes[2] = ETX;
        //    return ByteBuffer.wrap(bytes);
        //}

        //static public ByteBuffer buildClearDBCommand()
        //{
        //    byte[] bytes = new byte[3];
        //    bytes[0] = STX;
        //    bytes[1] = UDP_CLEAR_DB;
        //    bytes[2] = ETX;
        //    return ByteBuffer.wrap(bytes);


        //}

        //static public ByteBuffer buildReturnStationIPCommand(String stationID, String IP, String strPort, String macAddress)
        //{
        //    String s = stationID;
        //    s += ",";
        //    s += IP;
        //    s +=",";
        //    s += strPort;
        //    s +=",";
        //    s += macAddress;
        //    byte[] bytes = KDSUtil.convertStringToUtf8Bytes(s);
        //    ByteBuffer buf = ByteBuffer.allocate(bytes.length + 1+1+1+1); //STX , Code , length(1 bytes) ,data,  ETX
        //    buf.put(STX);
        //    buf.put(UDP_RET_STATION);
        //    buf.put((byte)(bytes.length));
        //    buf.put(bytes);
        //    buf.put(ETX);
        //    buf.position(0);
        //    return buf;
        //}

        public int length()
        {
            return m_fill;
        }

        //public byte[] station_info_command_data()
        //{
        //    int ncount = m_buffer.get(2);

        //    byte[] bytes = new byte[ncount];
        //    for (int i=0; i< ncount; i++)
        //    {
        //        bytes[i] = m_buffer.get(i + 3);
        //    }
        //    return bytes;
        //}
    }
}