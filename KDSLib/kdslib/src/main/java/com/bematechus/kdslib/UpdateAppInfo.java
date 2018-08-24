package com.bematechus.kdslib;

import java.util.ArrayList;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2016/11/7.
 *  2. file format
 <app name="kds/kdsrouter/kdsstatistic" ver="1.0" rate=5>
 <-- name: the app name
 ver: Last version
 rate: the recommended rate, max=5. --/>
 <applink>http://www.bematechus.com/androidapp/****.apk</applink>
 <applink>http://www.bematechus1.com/androidapp/****.apk</applink>
 ...
 <applink>http://www.bematechus2.com/androidapp/****.apk</applink>
 <-- The link where we can download apk file.
 There are multiple link that we can get our apk.
 --/>
 <features>
 <-- The new feature for this version --/>
 <feature brief="mirror stations">two station work as mirror mode</feature>
 <-- brief: the feature summary description
 text: the feature detail information --/>
 <feature brief="unbump last">unbump last bumped order</feature>
 ...
 <feature brief="print order">Print selected order</feature>


 <features/>
 <app/>
 */
public class UpdateAppInfo {

    String m_appName = "";
    AppVersion m_version = new AppVersion();
    int m_nRate = 5;
    ArrayList<String> m_appLinks = new ArrayList<>();
    ArrayList<UpdateFeature> m_newFeatures = new ArrayList<>();

    public void setAppName(String name)
    {
        m_appName = name;
    }
    public String getAppName()
    {
        return m_appName;
    }

    public void setVersion(String ver)
    {
        m_version.parseString(ver);
    }
    public AppVersion getVersion()
    {
        return m_version;
    }

    public void setRate(String strRate)
    {
        m_nRate = KDSUtil.convertStringToInt(strRate, 0);
    }
    public int getRate()
    {
        return m_nRate;
    }

    public void addAppLink(String strLink)
    {
        m_appLinks.add(strLink);
    }
    public ArrayList<String> getAppLinks()
    {
        return m_appLinks;
    }

    public boolean isValid()
    {
        if (m_appLinks.size() <=0) return false;
        if (m_version.toString().isEmpty()) return false;
        if (m_appName.isEmpty()) return false;
        return true;
    }

    public void addFeature(UpdateFeature feature)
    {
        m_newFeatures.add(feature);
    }
    public ArrayList<UpdateFeature> getNewFeatures()
    {
        return m_newFeatures;
    }


    static public UpdateAppInfo parseFile(String fileName)
    {

        String s = KDSUtil.readFile(fileName);
        return parseString(s);
    }

//    static private String sample =" <app name=\"kds\" ver=\"2.0\" rate=\"5\">\n" +
//            "\t <applink>http://www.bematechus.com/Logic_FTP/KDS/KitchenGo/KitchenGO%20Premium%20KDS-1.0.apk</applink>\n" +
//            "\t <applink>http://www.bematechus1.com/androidapp/kds.apk</applink>\n" +
//            "\t <applink>http://www.bematechus2.com/androidapp/kds.apk</applink>\n" +
//            "\t <features>\n" +
//            "\t\t <feature brief=\"mirror stations\">two station work as mirror mode</feature>\n" +
//            "\t\t <feature brief=\"unbump last\">unbump last bumped order</feature>\n" +
//            "\t\t <feature brief=\"print order\">Print selected order</feature>\n" +
//            "\t </features>\n" +
//            " </app>";
    /**
     *
     * @param str
     * @return
     */
    static public UpdateAppInfo parseString(String str)
    {
        //str = sample;
        KDSXML xml = new KDSXML();
        UpdateAppInfo info = new UpdateAppInfo();
        if (!xml.loadString(str))
            return info;
        xml.back_to_root();
        String s = xml.getAttribute("name", "");
        info.setAppName(s);
        s = xml.getAttribute("ver", "");
        info.setVersion(s);

        s = xml.getAttribute("rate", "");
        info.setRate(s);
        if (xml.getFirstGroup("applink"))
        {
            s = xml.getCurrentGroupValue();
            info.addAppLink(s);
            while (xml.getNextGroup("applink"))
            {
                s = xml.getCurrentGroupValue();
                info.addAppLink(s);
            }

        }
        xml.back_to_root();
        if (xml.getFirstGroup("features"))
        {
            if (xml.getFirstGroup("feature"))
            {

                UpdateFeature f = new UpdateFeature();

                f.m_description = xml.getCurrentGroupValue();
                f.m_brief = xml.getAttribute("brief", "");
                info.addFeature(f);
                while (xml.getNextGroup("feature"))
                {
                    f = new UpdateFeature();
                    f.m_description = xml.getCurrentGroupValue();
                    f.m_brief = xml.getAttribute("brief", "");
                    info.addFeature(f);
                }
            }
        }
        return info;
    }

    public String toString()
    {
        KDSXML xml = new KDSXML();
        xml.new_doc_with_root("app");
        xml.setAttribute("name", this.getAppName());
        xml.setAttribute("ver", this.getVersion().toString());
        xml.setAttribute("rate",KDSUtil.convertIntToString( this.getRate()));
        for (int i=0; i< m_appLinks.size(); i++)
        {
            xml.newGroup("applink", m_appLinks.get(i), false);
        }
        xml.newGroup("features", true);

        for (int i=0; i< m_newFeatures.size(); i++)
        {
            xml.newGroup("feature", m_newFeatures.get(i).m_description, true);
            xml.newAttribute("brief", m_newFeatures.get(i).m_brief);
            xml.back_to_parent();

        }

        return xml.get_xml_string();
    }

    public String getNewFeaturesStrings()
    {
        String s = "";
        for (int i=0; i< m_newFeatures.size(); i++)
        {
            if (!s.isEmpty()) s+="\n";
            s += KDSUtil.convertIntToString(i+1) +". ";
            s += m_newFeatures.get(i).m_brief;
            s += "\n";
            s +=  "\t" + m_newFeatures.get(i).m_description;
        }
        return s;
    }

    static public class UpdateFeature
    {
        public String m_brief = "";
        public String m_description = "";
        public UpdateFeature()
        {

        }

    }

    /**
     *
     */
    static public class AppVersion
    {
        int m_nVer0 = 0;
        int m_nVer1 = 0;
        int m_nVer2 = 0;
        int m_nVer3 = 0;

        public AppVersion()
        {
            reset();
        }
        public void reset()
        {
            m_nVer0 = 0;
            m_nVer1 = 0;
            m_nVer2 = 0;
            m_nVer3 = 0;
        }
        /**
         * Format: 1.0.0.1
         * @param strVersion
         */
        public void parseString(String strVersion)
        {
            strVersion = strVersion.replace(".", ",");
            ArrayList<String > ar =  KDSUtil.spliteString(strVersion, ",");
            if (ar.size()<=0) return;
            m_nVer0 = KDSUtil.convertStringToInt(ar.get(0), 0);
            if (ar.size() >1)
                m_nVer1 = KDSUtil.convertStringToInt(ar.get(1), 0);
            if (ar.size() >2)
                m_nVer2 = KDSUtil.convertStringToInt(ar.get(2), 0);
            if (ar.size() >3)
                m_nVer3 = KDSUtil.convertStringToInt(ar.get(3), 0);
        }

        public boolean isOlderThanMine(AppVersion ver)
        {
            if (m_nVer0 > ver.m_nVer0 ) return true;
            if ( m_nVer1 > ver.m_nVer1 ) return true;
            if (m_nVer2 > ver.m_nVer2 ) return true;
            if (m_nVer3 > ver.m_nVer3 ) return true;

            return false;
        }
        public boolean isOlderThanMine(String versionName)
        {
            AppVersion ver = new AppVersion();
            ver.parseString(versionName);
            return isOlderThanMine(ver);
        }
        public String toString()
        {
            String s =  KDSUtil.convertIntToString(m_nVer0) + ".";
            s +=  KDSUtil.convertIntToString(m_nVer1) + ".";
            s +=  KDSUtil.convertIntToString(m_nVer2) + ".";
            s +=  KDSUtil.convertIntToString(m_nVer3);
            return s;

        }

    }
}
