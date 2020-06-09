package com.bematechus.kds;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSViewFontFace;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by David.Wong on 2019/12/11.
 * Rev:
 */
public class KDSContextMenu {


    public enum ContextMenuType
    {
        Order,
        Item,
    }
    public enum ContextMenuItemID
    {
        unknown,
        order_bump,
        order_unbump,
        order_unbump_last,
        order_park,
        order_unpark,
        order_print,
        order_sum,
        order_transfer,
        order_sort,
        order_more,
        order_page,
        order_test,

        item_bump,
        item_unbump,
        item_transfer,
        item_buildcard,
        item_training,

    }
    AlertDialog menuDialog;
    ContextMenuType m_menuType = ContextMenuType.Order;
    //private Menu m_menu;
    //GridView menuGrid;
    //View menuView;
    OnContextMenuItemClickedReceiver m_onItemClickListener;
    //private int size = 0;

    public interface OnContextMenuItemClickedReceiver
    {
        public void onContextMenuItemClicked(ContextMenuItemID nItemID);
    }
    public void setOnItemClickListener(final OnContextMenuItemClickedReceiver listener) {
        this.m_onItemClickListener = listener;
    }
    int m_nFG = 0;
    private AlertDialog create(Context context, ContextMenuType menuType, KDSSettings settings)
    {
        View menuView = View.inflate(context, R.layout.context_menu, null);

        menuDialog = new AlertDialog.Builder(context).create();
        menuDialog.setView(menuView);

        GridView menuGrid = (GridView) menuView.findViewById(R.id.gridview);

        KDSViewFontFace ff = settings.getKDSViewFontFace(KDSSettings.ID.Touch_fontface);
        menuGrid.setBackgroundColor(ff.getBG());
        m_nFG = ff.getFG();
        if (menuType == ContextMenuType.Order)
            menuGrid.setAdapter(getMenuAdapter(context, m_arOrderMenuItems));
        else
            menuGrid.setAdapter(getMenuAdapter(context, m_arItemMenuItems));

        menuGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                ContextMenuItemID nMenuItemID = ContextMenuItemID.unknown;
                if (m_menuType == ContextMenuType.Order)
                    nMenuItemID = m_arOrderMenuItems.get(position).nID;
                else
                    nMenuItemID = m_arItemMenuItems.get(position).nID;
                m_onItemClickListener.onContextMenuItemClicked(nMenuItemID);
                menuDialog.hide();
                //menuGrid.invalidate();
            }
        });


        return menuDialog;

    }
//    public long getItemId(long position){
//        if(menu!=null && id<size){
//            return (long)(menu.getItem((int)id).getItemId());
//        }
//        return -1;
//    }

    private SimpleAdapter getMenuAdapter(Context context, ArrayList<ContextMenuItem> menuItems){
        int size = menuItems.size();
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < size; i++){
            //Log.i("","menuItemID:"+menu.getItem(i).getItemId()+",i:"+i);
            Drawable icon = context.getResources().getDrawable(menuItems.get(i).iconID);// menu.getItem(i).getIcon();
            String text = menuItems.get(i).text;// menu.getItem(i).getTitle().toString();
            if(icon==null)
                icon =context.getResources().getDrawable(android.R.drawable.ic_menu_help);

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("itemImage", icon);
            map.put("itemText", text);
            data.add(map);
        }
        SimpleAdapter Adapter = new SimpleAdapter(context, data,
                R.layout.context_menu_item, new String[] { "itemImage", "itemText" },
                new int[] { R.id.item_image, R.id.item_text });
        Adapter.setViewBinder(new  SimpleAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Object data, String arg2) {

                if(view instanceof ImageView && data instanceof Drawable){
                    ImageView iv=(ImageView)view;
                    iv.setImageDrawable((Drawable)data);
                    return true;
                }
                else if (view instanceof TextView)
                {
                    ((TextView) view).setTextColor(m_nFG);

                }
                return false;
            }
        });
        return Adapter;
    }

    ArrayList<ContextMenuItem> m_arOrderMenuItems = new ArrayList();
    ArrayList<ContextMenuItem> m_arItemMenuItems = new ArrayList();

    private void init_menu_items(Context context,  KDSSettings settings)
    {
        if (settings.getBoolean(KDSSettings.ID.Touch_bump))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_bump), R.drawable.bump, ContextMenuItemID.order_bump);
        if (settings.getBoolean(KDSSettings.ID.Touch_unbump_last))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_unbump_last), R.drawable.unbump_last, ContextMenuItemID.order_unbump_last);
        if (settings.getBoolean(KDSSettings.ID.Touch_unbump))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_unbump), R.drawable.unbump, ContextMenuItemID.order_unbump);
        if (settings.getBoolean(KDSSettings.ID.Touch_sum))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_sum), R.drawable.summary, ContextMenuItemID.order_sum);

        if (settings.getBoolean(KDSSettings.ID.Touch_transfer))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_transfer), R.drawable.transfer, ContextMenuItemID.order_transfer);
        if (settings.getBoolean(KDSSettings.ID.Touch_sort))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_sort), R.drawable.sort, ContextMenuItemID.order_sort);
        if (settings.getBoolean(KDSSettings.ID.Touch_park))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_park), R.drawable.park, ContextMenuItemID.order_park);
        if (settings.getBoolean(KDSSettings.ID.Touch_unpark))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_unpark), R.drawable.unpark, ContextMenuItemID.order_unpark);
        if (settings.getBoolean(KDSSettings.ID.Touch_print))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_print), R.drawable.ticket_print, ContextMenuItemID.order_print);
        if (settings.getBoolean(KDSSettings.ID.Touch_more))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_more), R.drawable.more, ContextMenuItemID.order_more);
        if (settings.getBoolean(KDSSettings.ID.Touch_page))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_page), R.drawable.guest_paging, ContextMenuItemID.order_page);
        if (settings.getBoolean(KDSSettings.ID.Touch_test))
            addItem(m_arOrderMenuItems, context.getString(R.string.touchpad_test), R.drawable.testing, ContextMenuItemID.order_test);

        ////////////// ITEMS menu //////////////////
        if (settings.getBoolean(KDSSettings.ID.Touch_bump))
            addItem(m_arItemMenuItems, context.getString(R.string.touchpad_bump), R.drawable.bump, ContextMenuItemID.item_bump);
        if (settings.getBoolean(KDSSettings.ID.Touch_unbump))
            addItem(m_arItemMenuItems, context.getString(R.string.touchpad_unbump), R.drawable.unbump, ContextMenuItemID.item_unbump);
        if (settings.getBoolean(KDSSettings.ID.Touch_transfer))
            addItem(m_arItemMenuItems, context.getString(R.string.touchpad_transfer), R.drawable.transfer, ContextMenuItemID.item_transfer);
        if (settings.getBoolean(KDSSettings.ID.Touch_BuildCard))
            addItem(m_arItemMenuItems, context.getString(R.string.touchpad_buildcard), R.drawable.buildcard, ContextMenuItemID.item_buildcard);
        if (settings.getBoolean(KDSSettings.ID.Touch_Training))
            addItem(m_arItemMenuItems, context.getString(R.string.touchpad_training), R.drawable.training_video, ContextMenuItemID.item_training);


    }
    public void showContextMenu(Context context, OnContextMenuItemClickedReceiver receiver, ContextMenuType menuType, KDSSettings settings)
    {
        //if (m_arOrderMenuItems.size() <=0)
        m_arOrderMenuItems.clear();
        m_arItemMenuItems.clear();
        m_menuType = menuType;
        init_menu_items(context,settings);
        this.setOnItemClickListener(receiver);

        AlertDialog dlg = create(context, menuType, settings);
        dlg.show();
        //dlg.getWindow().setLayout(200, ViewGroup.LayoutParams.MATCH_PARENT);

    }
    private void addItem( ArrayList<ContextMenuItem> menu, String text, int icon, ContextMenuItemID nID)
    {
        ContextMenuItem item = new ContextMenuItem();
        item.text = text;
        item.iconID = icon;
        item.nID = nID;
        menu.add(item);
    }

    class ContextMenuItem
    {
        String text;
        int iconID;
        ContextMenuItemID nID;
    }
}

