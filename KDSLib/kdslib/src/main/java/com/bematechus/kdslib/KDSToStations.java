/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bematechus.kdslib;

import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSToStations {

    public enum PrimarySlaveStation
    {
        Unknown,
        Primary,
        Slave,
    }

    ArrayList m_arStations = new ArrayList();

    public int getCount()
    {
        return m_arStations.size();
    }
    public KDSToStation getToStation(int nIndex)
    {
        return (KDSToStation)m_arStations.get(nIndex);
    }

    public boolean parseString(String toStations)
    {
        m_arStations.clear();
        if (toStations == null)
            return true;
        String[] ar = toStations.split(",");
        for (int i=0; i< ar.length; i++)
        {
            KDSToStation station = new KDSToStation();
            if (station.parseString(ar[i]))
                m_arStations.add(station);
        }
        return true;
    }
    public String getString()
    {
        String s = "";
        for (int i=0; i< m_arStations.size(); i++)
        {
            KDSToStation station =(KDSToStation) m_arStations.get(i);
            if (s.length() >0)
                s += ",";
            s += station.getString();
        }
        return s;
    }
    
    /***
     * 
     * @param stationID
     * @return
     *  KDSConst
     *    1: primary station
     *    2: slave station
     *    0: don't find it
     */
    public PrimarySlaveStation findStation(String stationID)
    {
        return findInToStationsArray(m_arStations, stationID);

//        if (stationID.isEmpty()) return PrimarySlaveStation.Unknown;// KDSConst.STATION_UNKNOWN;
//        for (int i=0; i< m_arStations.size(); i++)
//        {
//            KDSToStation station =(KDSToStation) m_arStations.get(i);
//            if (station.getPrimaryStation().equals(stationID))
//                return PrimarySlaveStation.Primary;// KDSConst.STATION_PRIMARY;
//            else if (station.getSlaveStation().equals(stationID))
//                return PrimarySlaveStation.Slave;// KDSConst.STATION_SLAVE;
//
//        }
//        return PrimarySlaveStation.Unknown;
    }
    /***
     * 
     * @param strStationID
     * @param nScreen
     * @return 
     *    1: primary station screen
     *    2: slave station screen
     *    0: don't find it screen
     */
    public int findScreen(String strStationID, int nScreen)
    {
        for (int i=0; i< m_arStations.size(); i++)
        {
            KDSToStation station =(KDSToStation) m_arStations.get(i);
            if (station.getPrimaryStation().equals(strStationID) &&
                station.getPrimaryScreen() == nScreen)
                return 1;
            else if (station.getSlaveStation().equals( strStationID) &&
                 station.getSlaveScreen() == nScreen)
                return 2;
            
        }   
        return 0;       
    }
    
    public boolean isAssigned()
    {
        return (m_arStations.size()>0);
    }
    
    public String findPrimaryStation(String strSlaveStationID)
    {
        for (int i=0; i< m_arStations.size(); i++)
        {
            KDSToStation station =(KDSToStation) m_arStations.get(i);
            if (station.getSlaveStation().equals(strSlaveStationID))
                return station.getPrimaryStation();
        }   
        return "";
    }

    public void addStation(String stationID)
    {
        //check if station existed.
        for (int i=0; i< m_arStations.size(); i++)
        {
            KDSToStation station =(KDSToStation) m_arStations.get(i);
            if (station.getPrimaryStation().equals(stationID))
                return;
        }

        KDSToStation station = new KDSToStation();
        station.setPrimaryStation(stationID);
        m_arStations.add(station);
    }
    public void removeStation(String stationID)
    {

        for (int i=0; i< m_arStations.size(); i++)
        {
            KDSToStation station =(KDSToStation) m_arStations.get(i);
            if (station.getPrimaryStation().equals(stationID)) {
                m_arStations.remove(i);
                return;

            }
        }


    }

    static  public PrimarySlaveStation findInToStationsArray(ArrayList<KDSToStation> ar, String stationID)
    {
        if (stationID.isEmpty()) return PrimarySlaveStation.Unknown;// KDSConst.STATION_UNKNOWN;
        for (int i=0; i< ar.size(); i++)
        {
            KDSToStation station =(KDSToStation) ar.get(i);
            if (station.getPrimaryStation().equals(stationID))
                return PrimarySlaveStation.Primary;// KDSConst.STATION_PRIMARY;
            else if (station.getSlaveStation().equals(stationID))
                return PrimarySlaveStation.Slave;// KDSConst.STATION_SLAVE;

        }
        return PrimarySlaveStation.Unknown;
    }
}
