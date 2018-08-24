using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
    public class TimeSlotOrderReport
    {

        List<TimeSlotEntry> m_arData = new List<TimeSlotEntry>();

        //ArrayList<ReportOrderEntry> m_arReversedData = new ArrayList<>();

        Condition m_condition = null;


        public int getNeedCols()
        {
            int nFrom = KDSUtil.convertStringToInt(m_condition.getStationFrom(), 0);
            int nTo = KDSUtil.convertStringToInt(m_condition.getStationTo(), 0);
            return nTo - nFrom + 1 + 1 + 1; //fixed col, total col

        }

        public void setCondition(Condition condition)
        {
            m_condition = condition;
        }
        public Condition getCondition()
        {
            return m_condition;
        }

        public void add(TimeSlotEntry entry)
        {
            m_arData.Add(entry);
        }


       

        public List<TimeSlotEntry> getData()
        {
            return m_arData;
        }
        

        public void resetFixedColText()
        {

        }
        public void next()
        {

        }
        public void prev()
        {

        }
        public String getTitleString()
        {
            return "";
        }


        public String getFixedColString()
        {
            return "Time";
        }

        public int getStationTotalCounter(int nIndex)
        {
            float flt = 0;
            for (int i = 0; i < m_arData.Count(); i++)
            {
                flt += m_arData[i].getOrderCount(nIndex);
            }
            return (int)flt;
        }
        public int getStationTotalBumpTimeSeconds(int nIndex)
        {
            int flt = 0;
            for (int i = 0; i < m_arData.Count(); i++)
            {
                flt += (int)m_arData[i].getOrderBumpTimeSeconds(nIndex);
            }
            return flt;
        }

        public int getTotalOrderCount()
        {
            TimeSlotEntry entry = m_arData[m_arData.Count() - 1]; //last cell is the total
            TimeSlotEntryDetail detail = entry.getData()[entry.getData().Count() - 1];
            return (int)detail.getCounter();


        }

        /**
         * unit mins
         * @return
         */
        public float getTotalBumpTime()
        {
            TimeSlotEntry entry = m_arData[m_arData.Count() - 1];//last cell is the total
            TimeSlotEntryDetail detail = entry.getData()[entry.getData().Count() - 1];
            float flt = detail.getBumpTimeSeconds();

            return (flt / 60); //unit is minutes


        }


        public float getAverageOrderCountPerTimeslot()
        {
            int nTotalOrderCount = getTotalOrderCount();
            int n = m_arData.Count() - 1;
            if (n <= 0) return 0;
            return nTotalOrderCount / n;
        }

        /**
         * unit minutes
         * @return
         */
        public float getAverageOrderPrepTime()
        {
            int nTotalOrderCount = getTotalOrderCount();
            float nMins = getTotalBumpTime();


            if (nTotalOrderCount <= 0) return 0;
            return nMins / nTotalOrderCount;
        }

        /**
         * xml format:
         *  <StatisticReport reporttype=1>
         *      <Station from= to=></Station>
         *      <Date from=2016-01-02 to 2017-12-12></Date>
         *      <Time from= to=></Dtime>
         *      <TimeSlot>1</TimeSlot>
         *      <TotalOrderCount></TotalOrderCount>
         *      <TotalPrepTime></TotalPrepTime>
         *      <AverageCountEachTimeSlot></AverageCountEachTimeSlot>
         *      <AveragePrepTime></AveragePrepTime>
         *
         *      <OrdersCounter>
         *          <TimeSlot from=12:10>2,4,5,7</TimeSlot>
         *          ...
         *      </OrdersCounter>
         *      <PrepTime>
         *          <T12:00>0.2,4.0,5.3,7.0</T12:00>
         *          ...
         *      </PrepTime>
         *
         *
         *  </StatisticReport>
         * @return
         */
        public String export2Xml()
        {
            KDSXML xml = new KDSXML();
            xml.new_doc_with_root("StatisticReport");
            xml.new_attribute("ReportType", getCondition().getReportType().ToString());
            //String str =  xml.get_xml_string();

            xml.new_group("Station", true);
            xml.new_attribute("from", getCondition().getStationFrom());
            xml.new_attribute("to", getCondition().getStationTo());
            xml.back_to_parent();
            //str =  xml.get_xml_string();


            addDateGroup2Xml(xml);
            addTimeGroup2Xml(xml);
            addTimeSlotGroup2Xml(xml);

            xml.new_group("TotalOrderCount",getTotalOrderCount().ToString(), false);
            xml.new_group("TotalPrepTime", KDSUtil.convertFloatToShortString(getTotalBumpTime()), false);
            xml.new_group("AverageCountEachTimeSlot", KDSUtil.convertFloatToShortString(getAverageOrderCountPerTimeslot()), false);
            xml.new_group("AveragePrepTime", KDSUtil.convertFloatToShortString(getAverageOrderPrepTime()), false);

            xml.new_group("OrdersCounter", true);
            for (int i = 0; i < m_arData.Count(); i++)
            {
                String s = m_arData[i].getOrderCounterXmlText();
                xml.new_group("TimeSlot", s, true);
                xml.new_attribute("from", m_arData[i].getFixedText());
                xml.back_to_parent();
            }
            xml.back_to_parent();

            xml.new_group("PrepTime", true);
            for (int i = 0; i < m_arData.Count(); i++)
            {
                String s = m_arData[i].getPrepTimeXmlText();
                xml.new_group("TimeSlot", s, true);
                xml.new_attribute("from", m_arData[i].getFixedText());
                xml.back_to_parent();
            }
            xml.back_to_parent();

            return xml.get_xml_string();


        }

        public String getReportFileName()
        {
            return this.getCondition().getReportType().ToString();
        }
        protected void addDateGroup2Xml(KDSXML xml)
        {

        }

        protected void addTimeGroup2Xml(KDSXML xml)
        {

        }

        protected void addTimeSlotGroup2Xml(KDSXML xml)
        {

        }

        public bool importFromXml(String strXml)
        {
            return false;
        }

        //private static String FOLDER_NAME = "KDSStatisticReport";
        //static public String getStatisticFolder()
        //{
        //    return Environment.getExternalStorageDirectory() + "/" + FOLDER_NAME;
        //}
        //static public String getStatisticFolderFullPath()
        //{

        //    return getStatisticFolder() + "/";

        //}

        //static public bool exportToFile(TimeSlotOrderReport report)
        //{
        //    String s = report.export2Xml();
        //    KDSUtil.createFolder(TimeSlotOrderReport.getStatisticFolder());
        //    String filename = report.getReportFileName();
        //    filename = TimeSlotOrderReport.getStatisticFolderFullPath() + filename;
        //    return KDSUtil.fileWrite(filename, s);
        //}
    }
}