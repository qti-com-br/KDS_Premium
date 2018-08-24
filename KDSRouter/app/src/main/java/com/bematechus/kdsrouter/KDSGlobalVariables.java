package com.bematechus.kdsrouter;

import android.content.Context;

/**
 * Created by Administrator on 2015/9/20 0020.
 */
public class KDSGlobalVariables {
    static private KDSRouter m_kdsRouter = null;



    static public void createKDS(Context contextApp)
    {
        KDSGlobalVariables.m_kdsRouter = new KDSRouter(contextApp);

    }

    static public KDSRouter getKDSRouter()
    {
        return m_kdsRouter;
    }
    static public void setKDSRouter(KDSRouter kdsRouter)
    {
        m_kdsRouter = kdsRouter;
    }

    static public KDSRouter getKDS()
    {
        return m_kdsRouter;
    }





}
