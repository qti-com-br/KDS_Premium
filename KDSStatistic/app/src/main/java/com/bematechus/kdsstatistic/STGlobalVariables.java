package com.bematechus.kdsstatistic;


/**
 *
 */
public class STGlobalVariables {
    static private STKDSStatistic m_kdsStatistic = null;

    static public void setKDSStatistic(STKDSStatistic kdsStatistic)
    {
        m_kdsStatistic = kdsStatistic;
    }

    static public STKDSStatistic getKDS()
    {
        return m_kdsStatistic;
    }





}
