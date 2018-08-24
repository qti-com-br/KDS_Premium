package com.bematechus.kdslib;

/**
 *
 * In slave of backup, we need to show something.
 * In additional to change 3 above, we need to make some change in “current” order that shows
 * in salve station which let user easily tell which orders are from primary station. Idea is that
 * insert two message at the beginning of the order and end of the order, check example below include
 * message to use. Add this option in Order display – option name = “Message for transferred order
 * from primary station”, user is able to edit the message and change its font&color.
 */
public class KDSDataFromPrimaryIndicator  extends KDSDataItem {
}
