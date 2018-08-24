package com.bematechus.kdsrouter;

import com.bematechus.kdslib.KDSDataArray;

/**
 * Created by Administrator on 2015/12/16 0016.
 */
public class KDSRouterDataItems extends KDSDataArray {

    public KDSRouterDataItem getItem(int nIndex)
    {
        return (KDSRouterDataItem)this.get(nIndex);
    }

}
