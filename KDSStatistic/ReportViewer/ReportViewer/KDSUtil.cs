using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ReportViewer
{
    class KDSUtil
    {

        static public String convertDateToDbString(DateTime dt)
        {

            return dt.ToString("yyyy-mm-dd");
        }
        static public String convertTimeToShortString(DateTime dt)
        {

            return dt.ToString("HH:mm");
            

            
        }
        static public int convertStringToInt(String str, int ndef)
        {
            if (str.Length == 0) return ndef;
            return int.Parse(str);
        }
        static public String convertUtf8BytesToString(byte[] buffer)
        {
            return Encoding.UTF8.GetString(buffer);
        }

        static public String convertFloatToShortString(float flt)
        {

            return flt.ToString("f2");
        }

    //    /**
    //*
    //* @param strDate
    //*  Format: yyyy/mm/dd
    //* @return
    //*/
    //    static public Date convertShortStringToDate(String strDate)
    //    {
    //        //TimeDog t = new TimeDog();

    //        DateTime.Parse()

    //        String s = strDate;
    //        SimpleDateFormat sdf = null;

    //        sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());


    //        try
    //        {
    //            Date date = sdf.parse(s, new ParsePosition(0));// null).parse(s);

    //            return date;
    //        }
    //        catch (Exception e)
    //        {
    //            return (new Date());
    //        }
    //    }
    }
}
