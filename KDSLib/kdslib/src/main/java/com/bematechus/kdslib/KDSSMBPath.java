package com.bematechus.kdslib;

/**
 * Created by Administrator on 2015/10/19 0019.
 * smb path format:
 * smb://WORKGROUP;user:password@ip_address/Share
 */
public class KDSSMBPath {
    String m_strDomain = "";
    String m_strPCName = "";
    String m_strUser = "";
    String m_strPwd = "";
    String m_strFolder = "";

    public KDSSMBPath()
    {

    }

    public String getDomain()
    {
        return m_strDomain;
    }
    public void setDomain(String domain)
    {
        m_strDomain = domain;
    }

    public String getPCName()
    {
        return m_strPCName;
    }
    public void setPCName(String name)
    {
        m_strPCName = name;
    }
    public String getUserID()
    {
        return m_strUser;
    }
    public void setUserID(String name)
    {
        m_strUser = name;
    }
    public String getPwd()
    {
        return m_strPwd;
    }
    public void setPwd(String name)
    {
        m_strPwd = name;
    }
    public String getFolder()
    {
        return m_strFolder;
    }
    public void setFolder(String name)
    {
        m_strFolder = name;
    }

    static public boolean isSmbFile(String s )
    {
        return (s.indexOf("smb://") >=0);

    }
    /**
     * smb://WORKGROUP;user:password@ip_address/Share
     * String str = "smb://Administrator:13188223394@192.168.1.197/";
     * @return
     */
    static public KDSSMBPath parseString(String s)
    {
        KDSSMBPath smb = new KDSSMBPath();
        if (s.indexOf("smb://") <0)
            return smb;

        s = s.replace("smb://", "");
        int nDomain = s.indexOf(";");
        if (nDomain >=0) {
            smb.setDomain(s.substring(0, nDomain));
            s = s.substring(nDomain + 1);
        }
        int n = s.indexOf(":");
        if (n >= 0) {
            smb.setUserID(s.substring(0, n));
        }
        s = s.substring(n + 1);

        n = s.indexOf("@");
        if (n >=0)
        {
            smb.setPwd( s.substring(0, n));
        }
        s = s.substring(n +1);

        n = s.indexOf("/");
        if (n >=0)
            smb.setPCName(s.substring(0, n));
        s = s.substring(n +1);

        smb.setFolder(s);
        //m_strFolder = s;

        return smb;
    }

    /**
     *  smb://WORKGROUP;user:password@ip_address/Share
     * String str = "smb://Administrator:13188223394@192.168.1.197/";
     * @return
     */
    public String toString()
    {

        String s = "smb://";
        if (!m_strDomain.isEmpty())
            s += m_strDomain +";";
        s += m_strUser;
        s += ":";
        s += m_strPwd;
        s += "@";
        s += m_strPCName;
        s += "/";
        s += m_strFolder;
        return s;

    }

    public String toDisplayString()
    {

        String s = "smb://";
//        s += m_strUser;
//        s += ":";
//        s += "******";
//        s += "@";
        s += m_strPCName;
        s += "/";
        s += m_strFolder;
        return s;

    }

    public String toFolderString()
    {

        String s = "/";
        s += m_strFolder;
        return s;

    }

    public void copyFrom(KDSSMBPath smbPath)
    {
        m_strDomain = smbPath.getDomain();
        m_strPCName = smbPath.getPCName();
        m_strUser = smbPath.getUserID();
        m_strPwd = smbPath.getPwd();
        m_strFolder = smbPath.getFolder();
    }
}
