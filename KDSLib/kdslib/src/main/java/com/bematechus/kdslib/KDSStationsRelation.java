package com.bematechus.kdslib;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2015/9/24 0024.
 */
public class KDSStationsRelation extends KDSStationIP {

    static final String TAG = "KDSStationsRelation";
    static final String SEPERATOR = "_";
    SettingsBase.StationFunc m_nFunction;

    String m_strExpStations = "";
    String m_strSlaveStations = "";
    SettingsBase.SlaveFunc m_slaveFunc = SettingsBase.SlaveFunc.Unknown;

    SettingsBase.StationStatus m_nStatus;

    //for editing relation
    Object m_objTag = null;
    public enum EditingState
    {
        OK,
        Changed,
        Error,
        Error_ID,
        Error_IP,
        Error_Exp,
        Error_Slave,
        Error_SlaveFunc,
        Error_Repeat_BackupMirror
    }



    /////////////////////////////////////////////
    public KDSStationsRelation()
    {
        reset();
    }

    public void setTag(Object obj)
    {
        m_objTag = obj;
    }
    public Object getTag()
    {
        return m_objTag;
    }
    public void reset()
    {
        m_nFunction = SettingsBase.StationFunc.Normal;
        // m_strStationID = "";
        m_strExpStations = "";
        m_strSlaveStations = "";
        m_slaveFunc = SettingsBase.SlaveFunc.Unknown;

        m_nStatus = SettingsBase.StationStatus.Enabled;
        m_objTag = null;
    }
    public KDSStationIP getStationIP()
    {

        KDSStationIP station =  new KDSStationIP();
        station.copyFrom(this);

        return station;
    }

    public void setFunction(SettingsBase.StationFunc func)
    {
        m_nFunction = func;
        switch (func)
        {

            case Normal:
                break;
            case Expeditor:
                setExpStations("");
                break;
            case Queue:

                setExpStations("");
                setSlaveFunc(SettingsBase.SlaveFunc.Unknown);
                setSlaveStations("");
                break;
            case Backup:
            case Mirror:
            case Workload:
            case Duplicate:
                //allow expo 20170224
                //setExpStations("");
                break;
        }
    }
    public SettingsBase.StationFunc getFunction()
    {
        return m_nFunction;
    }
    public SettingsBase.StationStatus getStatus()
    {
        return m_nStatus;
    }

    public void setStatus(SettingsBase.StationStatus status)
    {
        m_nStatus = status;
    }




    public void setExpStations(String stations)
    {
        m_strExpStations = stations;
    }
    public String getExpStations()
    {
        return m_strExpStations;
    }

    public void setSlaveStations(String stations)
    {
        m_strSlaveStations = stations;
        if (stations.isEmpty())
            m_slaveFunc =  SettingsBase.SlaveFunc.Unknown;
    }
    public String getSlaveStations()
    {
        return m_strSlaveStations;
    }

    public void setSlaveFunc(SettingsBase.SlaveFunc func)
    {
        m_slaveFunc = func;
        switch (func)
        {

            case Unknown:
                setSlaveStations("");
                break;
            case Backup:
                break;
            case Mirror:
                break;
            case Automatic_work_loan_distribution:
                break;
            case Duplicate_station:
                break;
            case Order_Queue_Display:
                break;
        }
    }
    public SettingsBase.SlaveFunc getSlaveFunc()
    {
        return m_slaveFunc;
    }


    public String toString()
    {

        String s = m_strID;//  m_strStationID;
        s +=SEPERATOR;
        s += m_strIP;
        s +=SEPERATOR;
        s += m_strPort;
        s +=SEPERATOR;
        s +=  KDSUtil.convertIntToString(m_nFunction.ordinal());
        s +=SEPERATOR;
        s += m_strExpStations;
        s +=SEPERATOR;
        s +=  m_strSlaveStations;
        s +=SEPERATOR;
        s += KDSUtil.convertIntToString(m_slaveFunc.ordinal());
        s += SEPERATOR;
        s +=  KDSUtil.convertIntToString(m_nStatus.ordinal());
        s +=SEPERATOR;
        s += m_strMac;
        return s;
    }

    public void copyFrom(KDSStationsRelation station)
    {
        m_nFunction = station.getFunction();
        super.copyFrom(station);


        m_strExpStations = station.getExpStations();
        m_strSlaveStations = station.getSlaveStations();
        m_slaveFunc = station.getSlaveFunc();
        //m_strBackupStations = station.getBackupStations();
        m_nStatus = station.getStatus();
        m_objTag = station.getTag();
    }


    public boolean isRelationEqual(KDSStationsRelation station)
    {
        if (m_nFunction != station.getFunction()) return false;
        if (!super.isRelationEqual(station)) return false;

        if (!m_strExpStations.equals(station.getExpStations())) return false;
        if (!m_strSlaveStations.equals(station.getSlaveStations())) return false;
        if (m_slaveFunc != station.getSlaveFunc()) return false;
        //if (!m_strBackupStations.equals(station.getBackupStations())) return false;
        if (m_nStatus != station.getStatus()) return false;
        return true;

    }

    static public KDSStationsRelation parseString(String s)
    {
        ArrayList<String> ar = KDSUtil.spliteString(s, SEPERATOR);
        if (ar.size() <7) return null;
        KDSStationsRelation station = new KDSStationsRelation();
        station.setID(ar.get(0));
        station.setIP(ar.get(1));
        station.setPort(ar.get(2));
        station.setFunction(SettingsBase.StationFunc.values()[KDSUtil.convertStringToInt(ar.get(3), 0)]);
        station.setExpStations(ar.get(4));
        station.setSlaveStations(ar.get(5));
        station.setSlaveFunc(SettingsBase.SlaveFunc.values()[KDSUtil.convertStringToInt(ar.get(6), 0)] );
        //station.setBackupStations(ar.get(6));
        if (ar.size() >7)
            station.setStatus(SettingsBase.StationStatus.values()[KDSUtil.convertStringToInt(ar.get(7), 0)]);
        else
            station.setStatus(SettingsBase.StationStatus.Enabled);
        if (ar.size() >8)
            station.setMac(ar.get(8));

        return station;

    }

    /**
     *
     * @param context
     * @return
     */
    public static ArrayList<KDSStationsRelation> loadStationsRelation(Context context )
    {
        return SettingsBase.loadStationsRelation(context);
    }

    static public void save(Context context, ArrayList<KDSStationsRelation> ar)
    {
        SettingsBase.saveStationsRelation(context, ar);
    }


    /**
     *
     * @param strStations
     *  Format:
     *      1,2,3,4
     * @return
     */
    static ArrayList<KDSStationIP> parseStationsString(String strStations)
    {
        ArrayList<String> ar = KDSUtil.spliteString(strStations, ",");
        int ncount = ar.size();
        ArrayList<KDSStationIP> arReturn = new ArrayList<KDSStationIP>();
        for (int i=0; i< ncount; i++)
        {
            String s = ar.get(i);
            s = s.trim();
            if (s.isEmpty()) continue;;
            KDSStationIP station = new KDSStationIP();
            station.setID(s);
            arReturn.add(station);

        }
        return arReturn;
    }

    /**
     * Check if stationID existed in given stations relations string.
     * @param strStations
     * format;
     *  1,3,5,66,
     * @param stationID
     *  find this staiton if existed in strStations.
     * @return
     */
    public static boolean existedStation(String strStations, String stationID)
    {
        ArrayList<String> ar = KDSUtil.spliteString(strStations, ",");

        for (int i=0; i< ar.size(); i++)
        {
            String s = ar.get(i);
            s = s.trim();
            if (s.equals(stationID))
                return true;
        }
        return false;
    }

    static KDSStationsRelation findStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        for (int i=0; i< arRelations.size(); i++)
        {
            String s = arRelations.get(i).getID();
            s = s.trim();
            if (s.equals(stationID))
                return arRelations.get(i);
        }
        return null;
    }
    /**
     * find give station expeditors
     * @param arRelations
     * @param stationID
     * @return
     */
    static ArrayList<KDSStationIP> findExpOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        // ArrayList<KDSTCPStation> ar = new ArrayList<KDSTCPStation>();
        KDSStationsRelation relation = findStation(arRelations, stationID);
        if (relation == null)
            return new ArrayList<KDSStationIP>();
        ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getExpStations());
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, arReturn);

    }

    static ArrayList<KDSStationIP> findTTStation(ArrayList<KDSStationsRelation> arRelations)
    {
        // ArrayList<KDSTCPStation> ar = new ArrayList<KDSTCPStation>();
        ArrayList<KDSStationIP> ar = new ArrayList<KDSStationIP>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getFunction() == SettingsBase.StationFunc.TableTracker)
                ar.add(arRelations.get(i));

        }
        return ar;

    }

    /**
     * if relations setup the ip address and its port, load them to TCPStation.
     * @param arRelations
     * @param arStations
     * @return
     */
    static ArrayList<KDSStationIP> setIpFromSettenRelations(ArrayList<KDSStationsRelation> arRelations, ArrayList<KDSStationIP> arStations )
    {
        //get station ip and port settings.
        for (int i=0; i< arStations.size(); i++)
        {
            String id = arStations.get(i).getID();
            KDSStationsRelation settenStation =  findStation(arRelations, id);
            if (settenStation != null)
            {
                arStations.get(i).setIP(settenStation.getIP());
                arStations.get(i).setPort(settenStation.getPort());
            }
        }
        return arStations;
    }

    /**
     * find slave of give station
     * @param arRelations
     * @param stationID
     * @return
     */
    static public ArrayList<KDSStationIP> findSlaveOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        KDSStationsRelation relation = findStation(arRelations, stationID);
        if (relation == null)
            return ar;
        ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getSlaveStations());
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, arReturn);
    }


    static ArrayList<KDSStationIP> findMirrorOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        KDSStationsRelation relation = findStation(arRelations, stationID);
        if (relation == null)
            return ar;
        if (relation.getSlaveFunc() != SettingsBase.SlaveFunc.Mirror)
            return ar;
        ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getSlaveStations());
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, arReturn);
    }



    static public ArrayList<KDSStationIP> findBackupOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID) {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        KDSStationsRelation relation = findStation(arRelations, stationID);

            if (relation == null)
                return ar;
            if (relation.getSlaveFunc() != SettingsBase.SlaveFunc.Backup)
                return ar;
            ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getSlaveStations());
            //get station ip and port settings.
            return setIpFromSettenRelations(arRelations, arReturn);

    }

    static ArrayList<KDSStationIP> findWorkLoadOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        KDSStationsRelation relation = findStation(arRelations, stationID);
        if (relation == null)
            return ar;
        if (relation.getSlaveFunc() != SettingsBase.SlaveFunc.Automatic_work_loan_distribution)
            return ar;
        ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getSlaveStations());
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, arReturn);
    }

    static ArrayList<KDSStationIP> findDuplicatedOfStation(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        KDSStationsRelation relation = findStation(arRelations, stationID);
        if (relation == null)
            return ar;
        if (relation.getSlaveFunc() != SettingsBase.SlaveFunc.Duplicate_station)
            return ar;
        ArrayList<KDSStationIP> arReturn = parseStationsString(relation.getSlaveStations());
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, arReturn);
    }



    static ArrayList<KDSStationIP> findPrimaryWhoUseMeAsBackup(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getSlaveFunc() != SettingsBase.SlaveFunc.Backup)
                continue;
            String slaves = arRelations.get(i).getSlaveStations();
            if (existedStation(slaves, stationID))
            {
                KDSStationIP station = new KDSStationIP();
                station.setID(arRelations.get(i).getID());
                ar.add(station);
            }
        }
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, ar);

    }

    static ArrayList<KDSStationIP> findPrimaryWhoUseMeAsMirror(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getSlaveFunc() != SettingsBase.SlaveFunc.Mirror)
                continue;
            String slaves = arRelations.get(i).getSlaveStations();
            if (existedStation(slaves, stationID))
            {
                KDSStationIP station = new KDSStationIP();
                station.setID(arRelations.get(i).getID());
                ar.add(station);
            }
        }
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, ar);

    }

    static ArrayList<KDSStationIP> findPrimaryWhoUseMeAsWorkLoad(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getSlaveFunc() != SettingsBase.SlaveFunc.Automatic_work_loan_distribution)
                continue;
            String slaves = arRelations.get(i).getSlaveStations();
            if (existedStation(slaves, stationID))
            {
                KDSStationIP station = new KDSStationIP();
                station.setID(arRelations.get(i).getID());
                ar.add(station);
            }
        }
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, ar);

    }

    static ArrayList<KDSStationIP> findPrimaryWhoUseMeAsDuplicated(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getSlaveFunc() != SettingsBase.SlaveFunc.Duplicate_station)
                continue;
            String slaves = arRelations.get(i).getSlaveStations();
            if (existedStation(slaves, stationID))
            {
                KDSStationIP station = new KDSStationIP();
                station.setID(arRelations.get(i).getID());
                ar.add(station);
            }
        }
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, ar);

    }



    static public KDSStationsRelation getRelation(List<KDSStationsRelation> arAllRelations, String stationID)
    {
        for (int i=0; i<arAllRelations.size(); i++) {
            if (arAllRelations.get(i).getID().equals(stationID))
                return arAllRelations.get(i);
        }
        return null;
    }



    /**
     * check if there is station set to backup and mirror at same time.
     *  We don't allow station work as mirror and backup simultaneously.
     * @param arAllRelations
     *  The relations we saved.
     * @param myStationID
     *  The current editing id.

     * @return
     *  error string
     */
    static public String checkSlaveConflict(List<KDSStationsRelation> arAllRelations, String myStationID, String strSlaves, SettingsBase.SlaveFunc slaveFunc)
    {
        if (arAllRelations == null) return "";
        if (strSlaves.isEmpty()) return "";
        //  ArrayList<String> allSlaves = new ArrayList<>();
        // ArrayList<String> arMirrors = new ArrayList<>();

        ArrayList<KDSStationIP> myStationSlaves = KDSStationsRelation.parseStationsString(strSlaves);
        for (int i=0; i< myStationSlaves.size(); i++) {
            if (existedSameSlaveSetting(arAllRelations, myStationID, myStationSlaves.get(i).getID(), slaveFunc))
            {
                if (!existedSameSlaveSettingButAllIsBackup(arAllRelations, myStationID, myStationSlaves.get(i).getID(), slaveFunc))
                    return  myStationSlaves.get(i).getID();
            }

        }
        return "";
    }

    /**
     * return conflict stations
     * @param arAllRelations
     * @param myStationID
     * @param mySlaveStationID
     * @param slaveFunc
     * @return
     */
    static public boolean existedSameSlaveSetting(List<KDSStationsRelation> arAllRelations, String myStationID, String mySlaveStationID, SettingsBase.SlaveFunc slaveFunc)
    {
        for (int i=0; i< arAllRelations.size(); i++)
        {
            String stationID = arAllRelations.get(i).getID();
            if (stationID.equals(myStationID)) continue;

            KDSStationsRelation relation =  getRelation(arAllRelations, stationID);

            if (relation == null) continue;

            String existedStationSlaves = relation.getSlaveStations();
            ArrayList<KDSStationIP> existedStationSlavesIP = KDSStationsRelation.parseStationsString(existedStationSlaves);
            for (int j=0; j<existedStationSlavesIP.size(); j++)
            {
                if (existedStationSlavesIP.get(j).getID().equals(mySlaveStationID)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * return conflict stations
     * @param arAllRelations
     * @param myStationID
     * @param mySlaveStationID
     * @param slaveFunc
     * @return
     */
    static public boolean existedSameSlaveSettingButAllIsBackup(List<KDSStationsRelation> arAllRelations, String myStationID, String mySlaveStationID, SettingsBase.SlaveFunc slaveFunc)
    {
        KDSStationsRelation myRelation =  getRelation(arAllRelations, myStationID);
        if (myRelation.getSlaveFunc() != SettingsBase.SlaveFunc.Backup) return false;
        for (int i=0; i< arAllRelations.size(); i++)
        {
            String stationID = arAllRelations.get(i).getID();
            if (stationID.equals(myStationID)) continue;

            KDSStationsRelation relation =  getRelation(arAllRelations, stationID);

            if (relation == null) continue;

            String existedStationSlaves = relation.getSlaveStations();
            ArrayList<KDSStationIP> existedStationSlavesIP = KDSStationsRelation.parseStationsString(existedStationSlaves);
            for (int j=0; j<existedStationSlavesIP.size(); j++)
            {
                if (existedStationSlavesIP.get(j).getID().equals(mySlaveStationID) &&
                       relation.getSlaveFunc() != SettingsBase.SlaveFunc.Backup ) {
                    return false;
                }
            }
        }
        return true;
    }




    public boolean addExpoStation(String expoID)
    {
        String s = this.getExpStations();
        //ArrayList<KDSStationIP> ar =  parseStationsString(s);
        if (s.isEmpty()) {
            this.setExpStations(expoID);
            return true;
        }
        else
        {
           if (existedStation(s, expoID))
               return true;
            else
           {
               s +=",";
               s += expoID;
               this.setExpStations(s);
               return true;
           }
        }
    }

    public boolean removeExpoStation(String expoID)
    {
        String s = this.getExpStations();
        //ArrayList<KDSStationIP> ar =  parseStationsString(s);
        if (s.isEmpty()) {
            return true;
        }
        else
        {
            if (!existedStation(s, expoID))
                return true;
            else
            {
                ArrayList<KDSStationIP> ar = parseStationsString( s);
                int ncount = ar.size();
                for (int i=ncount-1; i>=0; i--)
                {
                    if (ar.get(i).getID().equals(expoID) )
                    {
                        ar.remove(i);
                    }

                }
                ncount =ar.size();
                s = "";
                for (int i=0; i< ncount; i++)
                {
                    if (s.isEmpty())
                        s = ar.get(i).getID();
                    else
                    {
                        s += ",";
                        s += ar.get(i).getID();
                    }
                }
                this.setExpStations(s);
                return true;
            }
        }
    }

    static public String makeStationsString(ArrayList<String> arStations)
    {
        int ncount =arStations.size();
        String s = "";
        for (int i=0; i< ncount; i++)
        {
            if (s.isEmpty())
                s = arStations.get(i);
            else
            {
                s += ",";
                s += arStations.get(i);
            }
        }
        return s;
    }


    static ArrayList<KDSStationIP> findPrimaryWhoUseMeAsQueue(ArrayList<KDSStationsRelation> arRelations, String stationID)
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        for (int i=0; i< arRelations.size(); i++)
        {
            if (arRelations.get(i).getSlaveFunc() != SettingsBase.SlaveFunc.Order_Queue_Display)
                continue;
            String slaves = arRelations.get(i).getSlaveStations();
            if (existedStation(slaves, stationID))
            {
                KDSStationIP station = new KDSStationIP();
                station.setID(arRelations.get(i).getID());
                ar.add(station);
            }
        }
        //get station ip and port settings.
        return setIpFromSettenRelations(arRelations, ar);

    }

}
