
package com.bematechus.kdslib;

import android.content.Context;

import java.util.ArrayList;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */

/**
 * Created by Administrator on 2015/10/8 0008.
 */
public class KDSMyStationRelations {

    ArrayList<KDSStationsRelation> m_arStationsRelations = new ArrayList<KDSStationsRelation>();

    private Object m_locker = new Object();
    ArrayList<KDSStationIP> m_arExpStations = new ArrayList<KDSStationIP>(); //my expeditor stations
    ArrayList<KDSStationIP> m_arSlaveStations = new ArrayList<KDSStationIP>(); //my slave stations

    ArrayList<KDSStationIP> m_arPrimaryOfSlaveBackupStations = new ArrayList<KDSStationIP>(); //who use me as backup station.
    ArrayList<KDSStationIP> m_arPrimaryOfSlaveWorkLoadStations = new ArrayList<KDSStationIP>(); //who use me as work load station.
    ArrayList<KDSStationIP> m_arPrimaryOfSlaveDuplicatedStations = new ArrayList<KDSStationIP>(); //who use me as duplicated station.
    ArrayList<KDSStationIP> m_arPrimaryOfSlaveMirrorStations = new ArrayList<KDSStationIP>(); //who use me as mirror


    ArrayList<KDSStationIP> m_arTTStations = new ArrayList<KDSStationIP>(); //my expeditor stations
    //save all valid stations in this array
    ArrayList<KDSStationIP> m_arAllValidStations = new ArrayList<>();

    KDSStationsRelation m_myRelation = null;

    public void refreshRelations(Context context, String stationID)
    {
        synchronized (m_locker) {

            m_arStationsRelations = KDSStationsRelation.loadStationsRelation(context);

            m_arTTStations = KDSStationsRelation.findTTStation(m_arStationsRelations);
            //find all stations relations
            m_arExpStations = KDSStationsRelation.findExpOfStation(m_arStationsRelations, stationID);
            m_arSlaveStations = KDSStationsRelation.findSlaveOfStation(m_arStationsRelations, stationID);
            m_arPrimaryOfSlaveBackupStations =  KDSStationsRelation.findPrimaryWhoUseMeAsBackup(m_arStationsRelations, stationID);

            m_arPrimaryOfSlaveWorkLoadStations =  KDSStationsRelation.findPrimaryWhoUseMeAsWorkLoad(m_arStationsRelations, stationID);
            m_arPrimaryOfSlaveDuplicatedStations =  KDSStationsRelation.findPrimaryWhoUseMeAsDuplicated(m_arStationsRelations, stationID);
            m_arPrimaryOfSlaveMirrorStations =  KDSStationsRelation.findPrimaryWhoUseMeAsMirror(m_arStationsRelations, stationID);

            //m_arBackupStations = KDSStationsRelation.findBackupOfStation(m_arStationsRelations, stationID);
            //m_arBackupPrimaryStations = KDSStationsRelation.findBackupPrimaryOfStation(m_arStationsRelations, stationID);

            m_myRelation = KDSStationsRelation.findStation(m_arStationsRelations, stationID);
            if (m_myRelation == null)
                m_myRelation = new KDSStationsRelation();

            refreshAllValidStations();

        }

    }

    public boolean isExistedExpoInKDS()
    {
        for (int i=0; i< m_arStationsRelations.size(); i++)
        {
            if (m_arStationsRelations.get(i).getFunction() == SettingsBase.StationFunc.Expeditor)
                return true;
        }
        return false;

    }
    public ArrayList<KDSStationIP> getAllValidStations()
    {
        return m_arAllValidStations;
    }
    public void refreshAllValidStations()
    {
        m_arAllValidStations.clear();
        int ncount =  m_arStationsRelations.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation relation = m_arStationsRelations.get(i);
            KDSStationIP station = relation.getStationIP();
            if (!findAndMergeStationIP(m_arAllValidStations, station))
                m_arAllValidStations.add(station);

            String stationID = relation.getID();

            ArrayList<KDSStationIP> arExpStations = KDSStationsRelation.findExpOfStation(m_arStationsRelations, stationID);
            addToUniqueArray(m_arAllValidStations, arExpStations);

            ArrayList<KDSStationIP> arSlaveStations = KDSStationsRelation.findSlaveOfStation(m_arStationsRelations, stationID);
            addToUniqueArray(m_arAllValidStations, arSlaveStations);


        }
    }

    private void addToUniqueArray(ArrayList<KDSStationIP> arDest, ArrayList<KDSStationIP> arSrc)
    {
        for (int i=0; i< arSrc.size(); i++)
        {
            KDSStationIP station =  arSrc.get(i);
            if (!findAndMergeStationIP(m_arAllValidStations, station))
                m_arAllValidStations.add(station);
        }
    }

    private boolean findAndMergeStationIP(ArrayList<KDSStationIP> arDest, KDSStationIP station)
    {
        boolean bfind = false;
        for (int i=0; i< arDest.size(); i++)
        {
            KDSStationIP stationDest =  arDest.get(i);
            if (stationDest.getID().equals(station.getID()))
            {
                bfind = true;
                stationDest.mergeStation(station); //update ip and port if possible.
            }

        }
        return bfind;
    }

    public boolean isBackupStation()
    {

        return (m_arPrimaryOfSlaveBackupStations.size() >0);

    }
    public boolean isMirrorStation()
    {
        return (m_arPrimaryOfSlaveMirrorStations.size() >0);
    }
    public boolean isWorkLoadStation()
    {
        return (m_arPrimaryOfSlaveWorkLoadStations.size() >0);
    }
    public boolean isDuplicatedStation()
    {
        return (m_arPrimaryOfSlaveDuplicatedStations.size() >0);
    }

    public ArrayList<KDSStationsRelation> getRelationsSettings()
    {
        return m_arStationsRelations;
    }

    public void setExpStations( ArrayList<KDSStationIP> ar)
    {
        m_arExpStations = ar;
    }
    public ArrayList<KDSStationIP> getExpStations()
    {
        return m_arExpStations;
    }

    /**
     * get all expo
     * @return
     */
    public ArrayList<String> getAllExpoStations()
    {
        ArrayList<String> ar = new ArrayList<>();
        for (int i=0; i< m_arStationsRelations.size(); i++)
        {
            if (m_arStationsRelations.get(i).getFunction() == SettingsBase.StationFunc.Expeditor)
                ar.add(m_arStationsRelations.get(i).getID());
        }
        return ar;
    }


    public void setSlaveStations( ArrayList<KDSStationIP> ar)
    {
        m_arSlaveStations = ar;
    }
    public ArrayList<KDSStationIP> getSlaveStations()
    {
        return m_arSlaveStations;
    }

    public ArrayList<KDSStationIP> getBackupStations()
    {
        if (m_myRelation!= null && m_myRelation.getSlaveFunc() == SettingsBase.SlaveFunc.Backup)
            return m_arSlaveStations;
        else
        {
            return new ArrayList<KDSStationIP>();
        }
    }

    public ArrayList<KDSStationIP> getMirrorStations()
    {
        if (m_myRelation!= null && m_myRelation.getSlaveFunc() == SettingsBase.SlaveFunc.Mirror)
            return m_arSlaveStations;
        else
        {
            return new ArrayList<KDSStationIP>();
        }
    }

    public ArrayList<KDSStationIP> getWorkLoadStations()
    {
        if (m_myRelation!= null && m_myRelation.getSlaveFunc() == SettingsBase.SlaveFunc.Automatic_work_loan_distribution)
            return m_arSlaveStations;
        else
        {
            return new ArrayList<KDSStationIP>();
        }
    }

    public ArrayList<KDSStationIP> getDuplicatedStations()
    {
        if (m_myRelation!= null && m_myRelation.getSlaveFunc() == SettingsBase.SlaveFunc.Duplicate_station)
            return m_arSlaveStations;
        else
        {
            return new ArrayList<KDSStationIP>();
        }
    }


    public void setPrimaryStationsWhoUseMeAsMirror( ArrayList<KDSStationIP> ar)
    {
        m_arPrimaryOfSlaveMirrorStations = ar;
    }
    public ArrayList<KDSStationIP> getPrimaryStationsWhoUseMeAsMirror()
    {
        return m_arPrimaryOfSlaveMirrorStations;
    }
    public void setPrimaryStationsWhoUseMeAsBackup( ArrayList<KDSStationIP> ar)
    {
        m_arPrimaryOfSlaveBackupStations = ar;
    }
    public ArrayList<KDSStationIP> getPrimaryStationsWhoUseMeAsBackup()
    {
        return m_arPrimaryOfSlaveBackupStations;
    }

    public void setPrimaryStationsWhoUseMeAsWorkLoad( ArrayList<KDSStationIP> ar)
    {
        m_arPrimaryOfSlaveWorkLoadStations = ar;
    }
    public ArrayList<KDSStationIP> getPrimaryStationsWhoUseMeAsWorkLoad()
    {
        return m_arPrimaryOfSlaveWorkLoadStations;
    }

    public void setPrimaryStationsWhoUseMeAsDuplicated( ArrayList<KDSStationIP> ar)
    {
        m_arPrimaryOfSlaveDuplicatedStations = ar;
    }
    public ArrayList<KDSStationIP> getPrimaryStationsWhoUseMeAsDuplicated()
    {
        return m_arPrimaryOfSlaveDuplicatedStations;
    }

    public ArrayList<KDSStationIP> getAllStationsNeedToConnect()
    {
        ArrayList<KDSStationIP> ar = new ArrayList<>();
        ar.addAll(m_arExpStations);
        ar.addAll(m_arPrimaryOfSlaveMirrorStations);
        ar.addAll(m_arPrimaryOfSlaveBackupStations);

        ar.addAll(m_arPrimaryOfSlaveWorkLoadStations);
        ar.addAll(m_arPrimaryOfSlaveDuplicatedStations);

        return ar;

    }

    /**
     * check if give station is a primary mirror station of others
     * @param stationID
     * @return
     * return the slave mirror station
     */
    public ArrayList<KDSStationIP> getItsMirrorStation(String stationID)
    {

        return KDSStationsRelation.findMirrorOfStation(m_arStationsRelations, stationID);

    }
    public ArrayList<KDSStationIP> getItsBackupStation(String stationID)
    {

        return KDSStationsRelation.findBackupOfStation(m_arStationsRelations, stationID);

    }

    public ArrayList<KDSStationIP> getItsWorkLoadStation(String stationID)
    {

        return KDSStationsRelation.findWorkLoadOfStation(m_arStationsRelations, stationID);

    }

    public ArrayList<KDSStationIP> getItsDuplicatedStation(String stationID)
    {

        return KDSStationsRelation.findDuplicatedOfStation(m_arStationsRelations, stationID);

    }
    /**
     * the stations relation just set in relations settings table.
     * @param stationID
     * @return
     */
    public SettingsBase.StationFunc getStationFunction(String stationID, String stationSlaveID)
    {
        SettingsBase.StationFunc func = SettingsBase.StationFunc.Normal;

        int ncount =  m_arStationsRelations.size();
        for (int i=0; i< ncount; i++)
        {
            KDSStationsRelation relation = m_arStationsRelations.get(i);
            if (stationID.equals( relation.getID()))
            {
                func = relation.getFunction();
            }

        }


        switch (func)
        {

            case Normal:

            case Expeditor:

            case Queue:
                break;
            case Mirror:
            case Backup:
            case Workload:
            case Duplicate:
            {//find its parent station function
               return getMyPrimaryStationFunction(stationID,stationSlaveID, func);
            }

        }
        return func;
    }

    private SettingsBase.StationFunc getMyPrimaryStationFunction(String stationID, String stationSlaveID, SettingsBase.StationFunc myStationFunction)
    {
        String parentStationID = "";
        switch (myStationFunction)
        {

            case Normal:
            case Expeditor:
            case Queue:
                return myStationFunction;

            case Mirror:
                if (getPrimaryStationsWhoUseMeAsMirror().size()>0)
                    parentStationID = getPrimaryStationsWhoUseMeAsMirror().get(0).getID();
                break;
            case Backup:
                if (getPrimaryStationsWhoUseMeAsBackup().size()>0)
                    parentStationID = getPrimaryStationsWhoUseMeAsBackup().get(0).getID();
                break;
            case Workload:
                if (getPrimaryStationsWhoUseMeAsWorkLoad().size()>0)
                    parentStationID = getPrimaryStationsWhoUseMeAsWorkLoad().get(0).getID();
                break;
            case Duplicate:
                if (getPrimaryStationsWhoUseMeAsDuplicated().size()>0)
                    parentStationID = getPrimaryStationsWhoUseMeAsDuplicated().get(0).getID();
                break;

        }
        if (stationID.equals(stationSlaveID))
            return myStationFunction;

        if (!parentStationID.isEmpty())
            return getStationFunction(parentStationID, stationID);
        return SettingsBase.StationFunc.Normal;
    }
    public KDSStationsRelation getMyRelations()
    {

        return m_myRelation;
    }

    public boolean isEnabled(String stationID)
    {
        KDSStationsRelation station =  KDSStationsRelation.findStation(m_arStationsRelations, stationID);
        if (station == null)
            return false;
        return (station.getStatus() == SettingsBase.StationStatus.Enabled);
    }

    //    /**
//     * check if this ID is avlid station ID
//     * @param stationID
//     * @return
//     */
    public boolean isValidNormalStationID(String stationID)
    {
        int ncount = m_arAllValidStations.size();
        for (int i=0; i< ncount; i++)
        {
            if (m_arAllValidStations.get(i).getID().equals(stationID))
                return true;
        }
        return false;
    }


    /**
     * get the order queue display stations for expo
     * @return
     */
    public ArrayList<KDSStationIP> getQueueStations()
    {
        if (m_myRelation!= null && m_myRelation.getSlaveFunc() == SettingsBase.SlaveFunc.Order_Queue_Display)
            return m_arSlaveStations;
        else
        {
            return new ArrayList<KDSStationIP>();
        }
    }


    /**
     * 2.0.18
     * @param queueStationID
     * @return
     */
    public String getQueueAttachedPrepStationID(String queueStationID)
    {
        ArrayList<KDSStationIP> arPrimary = KDSStationsRelation.findPrimaryWhoUseMeAsQueue(m_arStationsRelations, queueStationID);
        if (arPrimary.size()<=0) return "";

        for (int i=0; i< arPrimary.size();i++)
        {
            KDSStationsRelation r= KDSStationsRelation.findStation(m_arStationsRelations,  arPrimary.get(i).getID());
            if (r.getFunction() == SettingsBase.StationFunc.Normal)
                return r.getID();
        }
        return "";
    }

    public ArrayList<KDSStationIP> getTTStations()
    {
        return m_arTTStations;

    }
    public ArrayList<KDSStationIP> getPrepStationsWhoUseMeAsExpo(String expoStationID)
    {
        ArrayList<KDSStationIP> arReturn = new ArrayList<>();

        KDSStationsRelation relation = null;
        for (int i=0; i< m_arStationsRelations.size(); i++)
        {

            relation = m_arStationsRelations.get(i);
            if (relation.getFunction() != SettingsBase.StationFunc.Normal)
                continue;
            if (relation.getID().equals(expoStationID))
                continue;
            String expos = relation.getExpStations();
            ArrayList<KDSStationIP> ar =  KDSStationsRelation.parseStationsString(expos);
            for (int j=0; j<ar.size(); j++)
            {
                if (ar.get(j).getID().equals(expoStationID)) {
                    arReturn.add(relation);
                    break;
                }
            }

        }
        return arReturn;
    }
}
