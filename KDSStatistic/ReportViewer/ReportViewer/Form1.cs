using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Net;
using System.Threading;
using System.Xml;//.Xaml;

using System.IO;


namespace ReportViewer
{
    public partial class Form1 : Form
    {
        //private enum ReportType
        //{
        //    Daily,
        //    Weekly,
        //    Monthly,
        //    OneTime,

        //}
        //private enum TimeSlot
        //{
        //    mins15,
        //    mins30,
        //    hr1,
        //    hr8,
        //    hr12,
        //}
        //private enum ArrangeReport
        //{
        //    FullDate,
        //    PerMonth,
        //    PerWeek,
        //}
       
        Socket m_client = null;
        KDSSocketTCPCommandBuffer m_commandBuffer = new KDSSocketTCPCommandBuffer();


        public Form1()
        {
            InitializeComponent();
            init_stations(cmbStationFrom);
            cmbStationFrom.SelectedIndex = 0;
            init_stations(cmbStationTo);
            cmbStationTo.SelectedIndex = 5;
            cmbReportType.SelectedIndex = 0;

            cmbTimeSlot.SelectedIndex = 1;
            cmbDayOfWeek.SelectedIndex = 0;
            cmbArrange.SelectedIndex = 0;
            btnDisconnect.Enabled = false;

            dtpTimeFrom.Text = "8:00";
            dtpTimeTo.Text = "22:00";
            //dtpDateFrom.Text = "2016-08-01";

        }

        private void btnConnect_Click(object sender, EventArgs e)
        {
            //
            int nport = int.Parse(txtPort.Text);
            String ip = txtIP.Text;

            m_client = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            try
            {
                m_client.Connect(ip, nport);
                timerSocket.Start();
                btnConnect.Enabled = false;
                btnDisconnect.Enabled = true;
                MessageBox.Show("Connected");
            }
            catch (Exception err)
            {
                //Console.WriteLine(">>Error:" + e.Message);
                return;
            }
        }

        private void btnDisconnect_Click(object sender, EventArgs e)
        {
            m_client.Close();
            timerSocket.Stop();
            btnDisconnect.Enabled = false;
            btnConnect.Enabled = true;
        }

        private void btnSend_Click(object sender, EventArgs e)
        {

        }

        private byte[] buildXmlCommand(String strXml)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(strXml);
            int ncount = 7 + bytes.Length;

            byte[] ar = new byte[ncount];
            ar[0] = 0x02; //start
            ar[1] = 0x16; //command
            //data length
            int nlength = bytes.Length;
            byte b0 = (byte)(nlength & 0xffL);
            byte b1 = (byte)((nlength & 0xff00L) >> 8);
            byte b2 = (byte)((nlength & 0xff0000L) >> 16);
            byte b3 = (byte)((nlength & 0xff000000L) >> 24);
            //HTL
            ar[2] = b3;
            ar[3] = b2;
            ar[4] = b1;
            ar[5] = b0;

            for (int i = 0; i < nlength; i++)
            {
                ar[6 + i] = bytes[i];
            }
            ar[6 + nlength] = 0x03;

            return ar;


        }

        /************************************************************************/
        /* the length is two bytes                                                                     */
        /************************************************************************/
        private byte[] buildWinKdsXmlCommand(String strXml)
        {
            byte[] bytes = Encoding.UTF8.GetBytes(strXml);
            int ncount = 5 + bytes.Length;

            byte[] ar = new byte[ncount];
            ar[0] = 0x02; //start
            ar[1] = 0x05; //command
            //data length
            int nlength = bytes.Length;
            byte b0 = (byte)(nlength & 0xffL);
            byte b1 = (byte)((nlength & 0xff00L) >> 8);
            //byte b2 = (byte)((nlength & 0xff0000L) >> 16);
            //byte b3 = (byte)((nlength & 0xff000000L) >> 24);
            //HTL
            ar[2] = b1;
            ar[3] = b0;
            //ar[4] = b1;
            //ar[5] = b0;

            for (int i = 0; i < nlength; i++)
            {
                ar[4 + i] = bytes[i];
            }
            ar[4 + nlength] = 0x03;

            return ar;


        }

        private void init_stations(ComboBox cmb)
        {

            for (int i = 0; i < 50; i++)
            {

                cmb.Items.Add(i.ToString());
            }
        }

        private String getConditionXmlString()
        {

            Condition c = new Condition();
            c.setStationFrom(cmbStationFrom.Text);
            c.setStationTo(cmbStationTo.Text);
            c.setReportType((Condition.ReportType)cmbReportType.SelectedIndex);
            c.setTimeSlot((Condition.TimeSlot)cmbTimeSlot.SelectedIndex);
            c.setTimeFrom(dtpTimeFrom.Text);
            c.setTimeTo(dtpTimeTo.Text);

            c.setDateFrom(dtpDateFrom.Text);
            c.setDateTo(dtpDateTo.Text);
            c.setEnableDayOfWeek(chkDayOfWeek.Checked);
            c.setDayOfWeek((DayOfWeek)cmbDayOfWeek.SelectedIndex);
            c.setReportArrange((Condition.ReportArrangement)cmbArrange.SelectedIndex);
            return c.export2XmlString();

        }

        void btnRetrieve_Click(object sender, EventArgs e)
        {
            if (m_client == null ||( !m_client.Connected))
            {
                MessageBox.Show("Please connect statistic app first.");
                return;
            }
            try
            {
                //txtReport.Text = "";
                web.DocumentText = "";
                String s = getConditionXmlString();
                //MessageBox.Show(s);
                byte[] command = buildXmlCommand(s);
                int nsend = m_client.Send(command);

                


            }
            catch (IOException ex)
            {
                //Console.WriteLine("An IOException has been thrown!");
                //Console.WriteLine(ex.ToString());
                //Console.ReadLine();
                String strerr = ex.ToString();
                return;
            }
        }
        static public int BUFFER_SIZE = 10240;
        byte[] buffer = new byte[BUFFER_SIZE];
        private void timerSocket_Tick(object sender, EventArgs e)
        {
            if (m_client == null) return;
            if (!m_client.Connected) return;
            if (m_client.Available>0)
            {
                int nsize = m_client.Receive(buffer);
                m_commandBuffer.appendData(buffer, nsize);
                doCommand();
            }
        }
        /**********************************************************************/
        /**
         *
         */
        protected void doCommand()
        {
            while (true)
            {
                m_commandBuffer.skip_to_STX();
                if (m_commandBuffer.fill() <= 1)
                    return;
                byte command = m_commandBuffer.command();
                if (command == 0)
                    return;
                switch (command)
                {
                    case KDSSocketTCPCommandBuffer.XML_COMMAND:
                        {//the command send by xml format
                            //1. parse the xml text
                            int ncommand_end = m_commandBuffer.command_end();
                            if (ncommand_end == 0)
                                return; //need more data

                            byte[] bytes = m_commandBuffer.xml_command_data();
                            m_commandBuffer.remove(ncommand_end);
                            String utf8 = KDSUtil.convertUtf8BytesToString(bytes);
                            doXmlCommand(utf8);

                        }
                        break;
                    default:
                        {
                            m_commandBuffer.remove(1);
                            break;
                        }
                }
            }
        }

        protected void doXmlCommand(String strXml)
        {
            //txtReport.Text = strXml;

            XmlDocument x = new XmlDocument();
            x.LoadXml(strXml);
            web.DocumentXml = x;
            //    DocumentText = strXml;
            //MessageBox.Show(strXml);
        }

        private void showReport(String strXml)
        {


        }

    }
}
