package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.SettingsBase;

/**
 * Created by David.Wong on 2019/12/25.
 * Rev:
 */
public class KDSDlgOrderZoom implements KDSLayout.KDSLayoutEvents,KDSContextMenu.OnContextMenuItemClickedReceiver {

    public enum ZoomViewItemOperation
    {
        None,
        Bump,
        Unbump,
//        Transfer,
//        BuildCard,
//        Training,
    }
    public interface ZoomViewEvents
    {
        public boolean zoomViewItemOperations(ZoomViewItemOperation operation, String orderGuid, String itemGuid);

    }
    KDSIOSView m_viewOrder = null;
    KDSDataOrder m_order = null;
    KDSLayout m_layout = null;

    private AlertDialog createDlg(Context context)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.order_zoom, null);
        //ScrollView sv = view.findViewById(R.id.scrollView);
        //sv.requestDisallowInterceptTouchEvent(true);//.requestDisallowInterceptTouchEvent

        m_viewOrder = view.findViewById(R.id.viewOrder);


        KDSSettings settings = new KDSSettings(KDSApplication.getContext());
        settings.loadSettings(KDSApplication.getContext());
        settings.setTabCurrentFunc(SettingsBase.StationFunc.Prep);//KDSGlobalVariables.getKDS().getSettings().getTabFunc());
        settings.setTabEnableLineItemsView(false);//KDSGlobalVariables.getKDS().getSettings().getTabLineItemsTempEnabled());
        m_viewOrder.getEnv().setSettings(settings);

        AlertDialog d = new AlertDialog.Builder(context)
                .create();

        // kill all padding from the dialog window
        d.setView(view, 0, 0, 0, 0);

        return d;
    }

    public boolean showOrder(Context context, KDSDataOrder order)
    {
        m_order = order;//save it for items operations.
        AlertDialog d = createDlg(context);

        m_viewOrder.setRowsCols(1, 1);
        m_viewOrder.setLayoutFormat(KDSSettings.LayoutFormat.Vertical);
        changeSettingsForZoom();

        KDSDataOrders orders = new KDSDataOrders();
        orders.addComponent(order);

        m_layout = new KDSLayout(m_viewOrder);
        m_layout.setEventsReceiver(this);
        KDSLayoutOrder dressedOrder = m_layout.createLayoutOrder(order);
        //t.debug_print_Duration("showOrder1");
        if (dressedOrder == null)
            return false; //"The "showing paid order" items showing method maybe return null
        int nRows = m_layout.getOrderNeedRows(dressedOrder);
        int nRowH = Math.round(m_viewOrder.getBestBlockRowHeight()*1.5F);//getBlockAverageHeight();
        int nViewHeight = nRows * (nRowH +1);
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_viewOrder.getLayoutParams();

        linearParams.height=((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, nViewHeight, KDSApplication.getContext().getResources().getDisplayMetrics()));

        m_viewOrder.setLayoutParams(linearParams);

        m_layout.showOrders(orders);

        d.show();

        return true;

    }

    private boolean refresh()
    {
        KDSDataOrders orders = new KDSDataOrders();
        orders.addComponent(m_order);

        m_layout.showOrders(orders);
        return true;
    }
    final float ZOOM_FACTOR = 1.5F;

    private void changeSettingsForZoom()
    {
        KDSSettings settings = m_viewOrder.getSettings(); //this settings is one copy of KDS settings. So, we can change anything.
        settings.set(KDSSettings.ID.Panels_Show_Number, false);

        float flt = settings.getInt(KDSSettings.ID.Panels_Row_Height);
        flt *= ZOOM_FACTOR;
        settings.set(KDSSettings.ID.Panels_Row_Height, Math.round( flt));

        KDSViewFontFace ff = settings.getKDSViewFontFace(KDSSettings.ID.Item_Default_FontFace);
        ff.setFontSize(Math.round (ff.getFontSize()*ZOOM_FACTOR));

        ff = settings.getKDSViewFontFace(KDSSettings.ID.Condiment_Default_FontFace);
        ff.setFontSize(Math.round (ff.getFontSize()*ZOOM_FACTOR));

        ff = settings.getKDSViewFontFace(KDSSettings.ID.Message_Default_FontFace);
        ff.setFontSize(Math.round (ff.getFontSize()*ZOOM_FACTOR));

        ff = settings.getKDSViewFontFace(KDSSettings.ID.Order_Normal_FontFace);
        ff.setFontSize(Math.round (ff.getFontSize()*ZOOM_FACTOR));

    }

    public  void onViewPanelDoubleClicked(KDSLayout layout)
    {

        //Log.i("Zoom", "double click");
    }
    public  void onViewPanelClicked(KDSLayout layout){}
    public void onViewDrawingFinished(KDSLayout layout){}

    public void onViewLongPressed(KDSLayout layout)
    {
        String focusedItem = m_viewOrder.getEnv().getStateValues().getFocusedItemGUID();
        KDSContextMenu.ContextMenuType menuType = KDSContextMenu.ContextMenuType.Item;
        if (focusedItem.isEmpty()) return ;
        //just show item context menu
        m_contextMenu.showContextMenu(m_viewOrder.getContext(), this, menuType, m_viewOrder.getSettings());

    }

    public boolean onViewSlipping( KDSLayout layout,MotionEvent e1, MotionEvent e2,KDSView.SlipDirection slipDirection, KDSView.SlipInBorder slipInBorder)
    {
        return false;
    }

    KDSContextMenu m_contextMenu = new KDSContextMenu();


    public void onContextMenuItemClicked(KDSContextMenu.ContextMenuItemID nItemID)
    {
        switch(nItemID){
            case order_bump:
            case order_unbump:
            case order_unbump_last:
            case order_park:
            case order_unpark:
            case order_print:
            case order_sum:
            case order_transfer:
            case order_sort:
            case order_more:
            case order_page:
            case order_test:

                break;
            case item_bump:
                opBumpItem();

                break;
            case item_unbump:
                //opUnbump(getFocusedUserID());
                break;
            case item_transfer:
                //opTransfer(getFocusedUserID());
                break;
            case item_buildcard:
                //doMoreFunc_BuildCard(getFocusedUserID());
                break;
            case item_training:
                //doMoreFunc_Training_Video(getFocusedUserID());
                break;

        }
    }
    private void opBumpItem()
    {
        String itemGuid = m_viewOrder.getEnv().getStateValues().getFocusedItemGUID();
        if (itemGuid.isEmpty()) return;
        KDSDataItem item = m_order.getItems().getItemByGUID(itemGuid);
        item.setLocalBumped(true);
        refresh();
    }
}
