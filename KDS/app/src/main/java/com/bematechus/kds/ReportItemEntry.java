package com.bematechus.kds;

import com.bematechus.kdslib.TimeSlotEntry;

/**
 * Created by Administrator on 2017/7/24.
 */
public class ReportItemEntry extends TimeSlotEntry {

    public void addItem(String stationID, String itemDescription, int ordersCount, int bumpSeconds)
    {
        super.add(stationID, ordersCount, bumpSeconds);
        this.m_timeslotStart = itemDescription;

    }
}
