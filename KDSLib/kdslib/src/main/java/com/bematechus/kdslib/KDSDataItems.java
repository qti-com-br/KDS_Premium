

package com.bematechus.kdslib;
import java.util.ArrayList;

/**
 *
 * @author David.Wong
 */
public class KDSDataItems extends KDSDataArray {
    
    private int m_nLastAddonGroupID = -1;


    public boolean existedKDSStationChangedToBackupItem()
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) this.getComponent(i);
                if (item.getStationChangedToBackup())
                    return true;
            }
            return false;
        }
    }
    
    public void setOrderID(int nOrderID)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) this.getComponent(i);
                item.setOrderID(nOrderID);
            }
        }
    }

    public void setOrderGUID(String orderGUID)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) this.getComponent(i);
                item.setOrderGUID(orderGUID);
            }
        }
    }

    public int createNewAddonGroup()
    {
        m_nLastAddonGroupID ++;
        return getLastAddonGroupID();
    }
    public void addItemToAddonGroup(int nGroupID, KDSDataItem item)
    {
        item.setAddOnGroup(nGroupID);
        this.addComponent(item);
    }
    public KDSDataItems getAddonGroup(int nGroupID)
    {
        synchronized (m_locker) {
            KDSDataItems items = new KDSDataItems();
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) this.getComponent(i);
                if (item.getAddOnGroup() == nGroupID)
                    items.addComponent(item);
            }
            return items;
        }
    }
    public ArrayList getAddonGroupIDs()
    {
        synchronized (m_locker) {
            ArrayList ar = new ArrayList();

            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem item = (KDSDataItem) this.getComponent(i);
                int nGroupID = item.getAddOnGroup();
                if (nGroupID < 0) continue;
                if (KDSUtil.isExistedInArray(ar, nGroupID))
                    continue;
                ar.add(nGroupID);
            }
            return ar;
        }
    }
    public int getLastAddonGroupID()
    {
         return m_nLastAddonGroupID;
        
    }
    private int findMaxGroupID()
    {
        synchronized (m_locker) {
            ArrayList ar = getAddonGroupIDs();
            int nMax = KDSUtil.findMaxValue(ar);
            return nMax;
        }
    }
    public KDSDataItem getItem(int nIndex)
    {
        return (KDSDataItem) get(nIndex);
    }
    public KDSDataItem getDataItem(int nIndex)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            int nDataItemIndex = -1;
            for (int i = 0; i < ar.size(); i++) {

                Object original = ar.get(i);
                if (original instanceof KDSDataMoreIndicator)
                    continue;
                else if (original instanceof KDSDataFromPrimaryIndicator)
                    continue;
                else
                    nDataItemIndex++;
                if (nDataItemIndex == nIndex)
                    return (KDSDataItem) original;

            }
            return null;
        }
    }
    public void copyTo(KDSDataItems objs)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataItem c = new KDSDataItem();
                KDSDataItem original = (KDSDataItem) ar.get(i);
                original.copyTo(c);
                objs.addComponent(c);
            }
        }
    }
    
    public KDSDataItem getItemByName(String itemName)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {

                KDSDataItem item = (KDSDataItem) ar.get(i);
                if (item.getItemName().equals(itemName))
                    return item;

            }
            return null;
        }
    }
    
    public KDSDataItem getItemByGUID(String itemGUID)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            for (int i = 0; i < ar.size(); i++) {
                KDSDataItem item = (KDSDataItem) ar.get(i);
                if (item.getGUID().equals(itemGUID))
                    return item;

            }
            return null;
        }
    }
    
    public int getItemIndexByGUID(String itemGUID)
    {
        synchronized (m_locker) {
            ArrayList ar = this.getComponents();
            KDSDataItem item = getItemByGUID(itemGUID);
            if (item == null) return -1;
            return ar.indexOf(item);
        }
         
    }
    
    public boolean deleteItem(String itemName)
    {
        synchronized (m_locker) {
            KDSDataItem item = getItemByName(itemName);
            if (item == null)
                return true;
            return deleteItem(item);
        }
    }
    public boolean deleteItem(KDSDataItem item)
    {
        synchronized (m_locker) {
            this.getComponents().remove(item);
            return true;
        }
    }
    
    public boolean outputXml(KDSXML xml)
    {
        synchronized (m_locker) {
            int count = this.getCount();
            for (int i = 0; i < count; i++) {
                this.getItem(i).outputXml(xml);


            }
            return true;
        }

    }

    /**
     * find same ID item
     * @param item
     * @return
     */
    public KDSDataItem findSameItemByName(KDSDataItem item)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem c = this.getItem(i);
                if (c.getItemName().equals(item.getItemName()))
                    return c;
            }
            return null;
        }
    }

    public ArrayList<KDSDataItem> findSameItemByDescription(KDSDataItem item)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            ArrayList<KDSDataItem> ar = new ArrayList<>();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem c = this.getItem(i);
                if (c.getDescription().equals(item.getDescription())) {
                    ar.add(c);
                }
            }
            return ar;
        }
    }
    /**
     * find same item before get itselft in array.
     * it is for consolidate items
     * @param item
     * @return
     */
    public KDSDataItem findSameItemBeforeIt(KDSDataItem item)
    {
        synchronized (m_locker) {
            int ncount = this.getCount();
            for (int i = 0; i < ncount; i++) {
                KDSDataItem c = this.getItem(i);
                if (c == item) return null;
                if (c.isSameShowingItem(item))
                    return c;


            }
            return null;
        }
    }

    public boolean consolidateItems()
    {
        synchronized (m_locker) {
            int count = this.getCount();
            int nIndex = 0;
            for (int i = 0; i < count; i++) {
                if (nIndex >= this.getCount())
                    return true;
                KDSDataItem c = this.getItem(nIndex);
                if (c instanceof KDSDataCategoryIndicator)//2.0.47
                {
                    nIndex++;
                    continue;
                }
                KDSDataItem findIt = findSameItemBeforeIt(c);
                if (findIt == null) //keep it.
                {
                    nIndex++;
                    continue;
                }
                //consolidate it to found item.
                findIt.addConsolidatedItem(c.getGUID(), c.getShowingQty());//.getQty());
                //remove same item
                this.removeComponent(c);


            }
            return true;
        }
    }

    public float getTotalQty()
    {
        synchronized (m_locker) {
            int count = this.getCount();
            float flt = 0;
            for (int i = 0; i < count; i++) {

                KDSDataItem c = this.getItem(i);
                flt += c.getQty();

            }
            return flt;
        }
    }

    public int getActiveItemsCount()
    {
        int ncounter = 0;
        synchronized (m_locker) {
            int count = this.getCount();

            for (int i = 0; i < count; i++) {

                KDSDataItem c = this.getItem(i);
                if (c.isReady() || c.isMarked() ||
                        c.getLocalBumped())
                    continue;
                ncounter ++;


            }
            return ncounter;
        }
    }

    public boolean isLastActiveItem(String itemGuid)
    {
        synchronized (m_locker) {
            int count = this.getCount();

            for (int i = 0; i < count; i++) {

                KDSDataItem c = this.getItem(i);
                if (c.getGUID().equals(itemGuid)) continue;

                if (!c.getLocalBumped())
                    return false;



            }
            return true;
        }
    }

    public String getFirstUnbumpedItemGuid()
    {
        synchronized (m_locker) {
            int count = this.getCount();
            for (int i = 0; i < count; i++) {

                KDSDataItem c = this.getItem(i);

                if (c.getLocalBumped()) continue;

                return c.getGUID();
            }
            return "";
        }
    }

    public void removeLineItemOfQtyChanged(String parentGuid)
    {
        ArrayList<KDSDataItem> ar = new ArrayList<>();

        synchronized (m_locker) {
            int count = this.getCount();
            for (int i = 0; i < count; i++) {
                KDSDataItem c = this.getItem(i);
                if (c.getParentGuid().equals(parentGuid))
                    ar.add(c);
            }
            //remove them
            for (int i=0; i< ar.size(); i++)
            {
                this.removeComponent(ar.get(i));
            }
            ar.clear();

        }
    }
}
