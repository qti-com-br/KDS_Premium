package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSTimer;
import com.bematechus.kdslib.KDSViewFontFace;
import com.bematechus.kdslib.SettingsBase;

/**
 * Created by David.Wong on 2019/12/25.
 * Rev:
 */
public class KDSDlgOrderZoom implements KDSLayout.KDSLayoutEvents,
        KDSTimer.KDSTimerInterface{

    public enum ZoomViewItemOperation {
        None,
        Bump,
        Unbump,
//        Transfer,
//        BuildCard,
//        Training,
    }

    public interface ZoomViewEvents {
        public boolean zoomViewItemOperations(ZoomViewItemOperation operation,KDSUser.USER userID, String orderGuid, String itemGuid);

    }

    static KDSDlgOrderZoom m_instance = null;

    KDSIOSView m_viewOrder = null;
    KDSDataOrder m_order = null;
    KDSLayout m_layout = null;
    ZoomViewEvents m_receiver = null;
    KDSUser.USER m_user = KDSUser.USER.USER_A;
    AlertDialog m_dialog = null;
    KDSDataOrders m_orders = null;
    KDSTimer m_timer = new KDSTimer();

    static KDSDlgOrderZoom instance()
    {
        if (m_instance != null)
            m_instance.hide();
        m_instance = new KDSDlgOrderZoom();
        return m_instance;
    }

    public void setUser(KDSUser.USER userID)
    {
        m_user = userID;
    }
    public KDSUser.USER getUser()
    {
        return m_user;
    }
    public void setReceiver(ZoomViewEvents receiver)
    {
        m_receiver = receiver;
    }


    private AlertDialog createDlg(Context context, KDSSettings settingsOriginal)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.order_zoom, null);
        //ScrollView sv = view.findViewById(R.id.scrollView);
        //sv.requestDisallowInterceptTouchEvent(true);//.requestDisallowInterceptTouchEvent

        m_viewOrder = view.findViewById(R.id.viewOrder);


        KDSSettings settings = settingsOriginal.clone();
        //settings.loadSettings(KDSApplication.getContext());
        settings.setTabCurrentFunc(SettingsBase.StationFunc.Prep);//KDSGlobalVariables.getKDS().getSettings().getTabFunc());
        settings.setTabEnableLineItemsView(false);//KDSGlobalVariables.getKDS().getSettings().getTabLineItemsTempEnabled());
        m_viewOrder.getEnv().setSettings(settings);

        m_dialog = new AlertDialog.Builder(context)
                .create();

        // kill all padding from the dialog window
        m_dialog.setView(view, 0, 0, 0, 0);
        m_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                m_instance = null;
                m_timer.stop();
            }
        });
        m_dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                m_instance = null;
                m_timer.stop();
            }
        });

        m_timer.start(null, this, 1000);

        return m_dialog;
    }

    final float HEIGHT_FACTOR = 1.3F;
    ViewTreeObserver.OnGlobalLayoutListener mRunOnceListener = null;

    /**
     * rev.:
     *  kpp1-429, use layout finished event to show orders.
     * @param context
     * @param settings
     * @param order
     * @return
     */
    public boolean showOrder(Context context,KDSSettings settings,  KDSDataOrder order)
    {
        m_order = order;//save it for items operations.
        AlertDialog d = createDlg(context, settings);

        m_viewOrder.setRowsCols(1, 1);
        m_viewOrder.setLayoutFormat(KDSSettings.LayoutFormat.Vertical);
        changeSettingsForZoom();

//        KDSDataOrders orders = new KDSDataOrders();
//        orders.addComponent(order);
        m_orders = new KDSDataOrders();
        m_orders.addComponent(order);

        m_layout = new KDSLayout(m_viewOrder);
        m_layout.setEventsReceiver(this);
//        KDSLayoutOrder dressedOrder = m_layout.createLayoutOrder(order);
//        //t.debug_print_Duration("showOrder1");
//        if (dressedOrder == null)
//            return false; //"The "showing paid order" items showing method maybe return null
        //debug kpp1-429
        //d.show();
        /*
        int nRows = m_layout.getOrderNeedRows(dressedOrder);
        int nRowH = Math.round(m_viewOrder.getBestBlockRowHeight()*HEIGHT_FACTOR);//getBlockAverageHeight();
        int nViewHeight = (nRows+2) * (nRowH );
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_viewOrder.getLayoutParams();

        linearParams.height=((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, nViewHeight, KDSApplication.getContext().getResources().getDisplayMetrics()));

        m_viewOrder.setLayoutParams(linearParams);

        m_layout.showOrders(m_orders);
*/

        //d.getWindow().setBackgroundDrawable(null);
//        Window win = d.getWindow();
//        win.getDecorView().setPadding(15, 0, 15, 0);
//        WindowManager.LayoutParams lp = win.getAttributes();
//        lp.width = WindowManager.LayoutParams.FILL_PARENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        win.setAttributes(lp);

        mRunOnceListener = new  ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                //refreshView.startRefreshing();
                m_viewOrder.getViewTreeObserver().removeOnGlobalLayoutListener(mRunOnceListener);
                showOrderInDialog();


            }


        };

        m_viewOrder.getViewTreeObserver().addOnGlobalLayoutListener( mRunOnceListener);


        d.show();

        return true;

    }

    /**
     * kpp1-429 Order zoom view - order cutoff
     */
    private void showOrderInDialog()
    {
        KDSLayoutOrder dressedOrder = m_layout.createLayoutOrder(m_order);
        //t.debug_print_Duration("showOrder1");
        if (dressedOrder == null)
            return ;
        int nRows = m_layout.getOrderNeedRows(dressedOrder);
        int nRowH = Math.round(m_viewOrder.getBestBlockRowHeight()*HEIGHT_FACTOR);//getBlockAverageHeight();
        int nViewHeight = (nRows+2) * (nRowH );
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) m_viewOrder.getLayoutParams();

        linearParams.height=((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, nViewHeight, KDSApplication.getContext().getResources().getDisplayMetrics()));

        m_viewOrder.setLayoutParams(linearParams);

        m_layout.showOrders(m_orders);
    }

    private boolean refresh()
    {
//        KDSDataOrders orders = new KDSDataOrders();
//        orders.addComponent(m_order);

        m_layout.showOrders(m_orders);
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
        opBumpItem();
        //Log.i("Zoom", "double click");
    }
    public  void onViewPanelClicked(KDSLayout layout){}
    public void onViewDrawingFinished(KDSLayout layout){}

    public void onViewLongPressed(KDSLayout layout)
    {
//        String focusedItem = m_viewOrder.getEnv().getStateValues().getFocusedItemGUID();
//        KDSContextMenu.ContextMenuType menuType = KDSContextMenu.ContextMenuType.Item;
//        if (focusedItem.isEmpty()) return ;
//        //just show item context menu
//        m_contextMenu.showContextMenu(m_viewOrder.getContext(), this, menuType, m_viewOrder.getSettings());

    }

    public boolean onViewSlipping( KDSLayout layout,MotionEvent e1, MotionEvent e2,KDSView.SlipDirection slipDirection, KDSView.SlipInBorder slipInBorder)
    {
        return false;
    }

//    KDSContextMenu m_contextMenu = new KDSContextMenu();
//
//
//    public void onContextMenuItemClicked(KDSContextMenu.ContextMenuItemID nItemID)
//    {
//        switch(nItemID){
//            case order_bump:
//            case order_unbump:
//            case order_unbump_last:
//            case order_park:
//            case order_unpark:
//            case order_print:
//            case order_sum:
//            case order_transfer:
//            case order_sort:
//            case order_more:
//            case order_page:
//            case order_test:
//
//                break;
//            case item_bump:
//                opBumpItem();
//
//                break;
//            case item_unbump:
//                //opUnbump(getFocusedUserID());
//                break;
//            case item_transfer:
//                //opTransfer(getFocusedUserID());
//                break;
//            case item_buildcard:
//                //doMoreFunc_BuildCard(getFocusedUserID());
//                break;
//            case item_training:
//                //doMoreFunc_Training_Video(getFocusedUserID());
//                break;
//
//        }
//    }
    private void opBumpItem()
    {
        String itemGuid = m_viewOrder.getEnv().getStateValues().getFocusedItemGUID();
        if (itemGuid.isEmpty()) return;
        KDSDataItem item = m_order.getItems().getItemByGUID(itemGuid);
        if (m_receiver != null) {
            ZoomViewItemOperation opt = ZoomViewItemOperation.Bump;
            if (item.getLocalBumped())
                opt = ZoomViewItemOperation.Unbump;
            m_receiver.zoomViewItemOperations(opt,getUser(), m_viewOrder.getEnv().getStateValues().getFocusedOrderGUID(), itemGuid);

        }


        //item.setLocalBumped(!item.getLocalBumped());
        refresh();

    }
    public void hide()
    {
        if (m_dialog != null)
            m_dialog.hide();
    }

    public void onTime()
    {
        refresh();
    }
}
