package com.bematechus.kds;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bematechus.kdslib.KDSBumpBarKeyFunc;
import com.bematechus.kdslib.KDSConst;
import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSDataOrder;
import com.bematechus.kdslib.KDSDataOrders;
import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * unbump order
 */
public class KDSActivityUnbump extends Activity  {

    ListView m_lstOrders;
    KDSIOSView m_viewOrder;
    KDSDBCurrent m_db = null;
    String m_selectedOrderGuid = "";
    Button m_btnUnparkUnbump = null;
    int m_nScreen = 0;
    TextView m_txtHelp = null;
    KDSBumpBarKeyFunc.KeyboardType m_kbdType = KDSBumpBarKeyFunc.KeyboardType.Standard;
    int m_nIndexBase = 0;

    int m_nMaxReservedOrders = 30;
   // MyHandler m_handler = new MyHandler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_kdsactivity_unbump);

        m_lstOrders =(ListView) this.findViewById(R.id.lstOrders);
        m_viewOrder =(KDSIOSView) this.findViewById(R.id.viewOrder);
        m_txtHelp =(TextView) this.findViewById(R.id.txtHelp);
        m_btnUnparkUnbump=(Button) this.findViewById(R.id.btnRestore);
        KDSSettings settings = new KDSSettings(this.getApplicationContext());
        settings.loadSettings(this.getApplicationContext());

        settings.setTabCurrentFunc(KDSGlobalVariables.getKDS().getSettings().getTabFunc());
        settings.setTabEnableLineItemsView(KDSGlobalVariables.getKDS().getSettings().getTabLineItemsTempEnabled());

        m_viewOrder.getEnv().setSettings(settings);
        m_kbdType =  KDSBumpBarKeyFunc.KeyboardType.values()[settings.getInt(KDSSettings.ID.Bumpbar_Kbd_Type)];
        m_nIndexBase = settings.getInt(KDSSettings.ID.Panels_Panel_Number_Base);

        m_nMaxReservedOrders = settings.getInt(KDSSettings.ID.Bump_Max_Reserved_Count) * 10; //kpp1-412, move it here, before show dialog.

        showByIntent();

        boolean bHide = settings.getBoolean(KDSSettings.ID.Hide_navigation_bar);
        hideNavigationBar(bHide);



    }

    private void hideNavigationBar(boolean bHide)
    {
        if (bHide) {
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, false);
            view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    KDSUtil.enableSystemVirtualBar(KDSActivityUnbump.this.getWindow().getDecorView(), false);
                }
            });
        }
        else
        {
            View view = this.getWindow().getDecorView();
            KDSUtil.enableSystemVirtualBar(view, true);
            view.setOnSystemUiVisibilityChangeListener(null);
        }
    }
    public boolean isLineItemsMode()
    {
        return m_viewOrder.getEnv().getSettings().getLineItemsViewEnabled();

    }

    public  void onBtnRestoreClicked(View v)
    {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.putExtra("guid", m_selectedOrderGuid);
        intent.putExtra("screen", m_nScreen);

        if (isLineItemsMode())
        {
            String itemGuid = m_viewOrder.getEnv().getStateValues().getFocusedItemGUID();
            intent.putExtra("itemguid", itemGuid);
        }
        setResult(RESULT_OK, intent);
        this.finish();
    }
    public void onBtnBackClicked(View v)
    {
        setResult(RESULT_CANCELED);
        this.finish();
    }

    private void showHelp(int nResID)
    {
        String strHelp = this.getString(nResID);
        String strRecall = KDSBumpBarKeyFunc.getKeyName(m_kbdType, KeyEvent.KEYCODE_NUMPAD_SUBTRACT);
        String strEnter = KDSBumpBarKeyFunc.getKeyName(m_kbdType, KeyEvent.KEYCODE_ENTER );
        String s = "[" + strEnter + "]+["+strRecall + "]";
        strHelp = strHelp.replace("#", s);
        m_txtHelp.setText(strHelp);
    }
    private void showUnbumpDlg(String stationIDs, int screen)
    {

        m_btnUnparkUnbump.setText(R.string.restore);

        showHelp(R.string.unbump_order_help);

        m_db = KDSDBCurrent.open(getApplicationContext());

        ArrayList ar = KDSUtil.spliteString(stationIDs, ",");

        LinkedHashMap<String , String> m = null;

        //2.0.37
       m_db.clearExpiredBumpedOrders(m_nMaxReservedOrders);

        if (isLineItemsMode())
        {
            m = m_db.ordersLoadRecentBumpedOrderNameForLineItems(ar,screen);
        }
        else
            m = m_db.ordersLoadRecentBumpedOrderName(ar, screen);

        showDialog(m);

    }

    private void showDialog(LinkedHashMap<String , String> m)
    {
        ArrayList<Map<String,Object>> arData= new ArrayList<Map<String,Object>>();
        Iterator iterator = m.keySet().iterator();
        int ncounter = 0;

        while (iterator.hasNext()) {
            Object key = iterator.next();
            Map<String,Object> item = new HashMap<String,Object>();
            if (ncounter<10)
                item.put("name", "["+KDSUtil.convertIntToString(ncounter+m_nIndexBase)+"]");
            else
                item.put("name", "");
            item.put("title", m.get(key));
            item.put("guid", key);
            arData.add(item);
            ncounter ++;

        }

        SimpleAdapter adapter = new SimpleAdapter(this,arData,android.R.layout.simple_list_item_2,
                new String[]{"title","name"},new int[]{android.R.id.text1,android.R.id.text2});
        m_lstOrders.setAdapter(adapter);
        m_lstOrders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position,
                                    long id) {
                onItemClicked(position, view);
            }
        });

        m_lstOrders.setItemsCanFocus(true);
        m_lstOrders.setFocusable(true);
        adapter.notifyDataSetChanged();

    }

    private void showUnparkDlg(String stationIDs, int screen)
    {
        m_btnUnparkUnbump.setText(R.string.unpark);
        showHelp(R.string.unpark_order_help);
        m_db = KDSDBCurrent.open(getApplicationContext());

        ArrayList ar = KDSUtil.spliteString(stationIDs, ",");

        LinkedHashMap<String , String> m = m_db.ordersLoadParkedOrdersName(ar, screen);
        showDialog(m);

    }

    /**
     * show order detail
     * @param nposition
     * @param viewItem
     */
    public void onItemClicked(int nposition, View viewItem)
    {

        highlightListPosition(nposition, viewItem);

        Map<String, Object> item = (Map<String, Object>) m_lstOrders.getAdapter().getItem(nposition);
        String guid = (String) item.get("guid");
        m_selectedOrderGuid = guid;
        previewOrder(guid);


    }

    public void focusFirstOne()
    {
        if (m_lstOrders.getCount()>0)
        {
            onItemClicked(0, null);


        }
    }
    public void  showByIntent()
    {
        Intent intent = this.getIntent();
        String stationIDs = intent.getStringExtra("station");
        int screen = intent.getIntExtra("screen", 0);
        m_nScreen = screen;
        int func = intent.getIntExtra("func", KDSConst.SHOW_UNBUMP_DLG);
        if (func == KDSConst.SHOW_UNBUMP_DLG)
            showUnbumpDlg(stationIDs, screen);
        else if (func == KDSConst.SHOW_UNPARK_DLG)
            showUnparkDlg(stationIDs, screen);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (m_lstOrders.getTag() == null)
            focusFirstOne();
       // Log.v(LOG_TAG, "(" + this + ") onWindowFocusChanged() called, hasFocus = " + Boolean.toString(hasFocus));
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean bHide = (KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar));
        hideNavigationBar(bHide);



    }

    private void highlightListPosition(int nposition, View viewItem)
    {
        if (viewItem == null) {
            viewItem= m_lstOrders.getChildAt(nposition);
            if (viewItem == null)
                return;
        }
        if (m_lstOrders.getTag() != null)
        {
            ((View)m_lstOrders.getTag()).setBackgroundColor(Color.TRANSPARENT);
        }
        if (viewItem != null)
            viewItem.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        m_lstOrders.setTag(viewItem);
    }


    private String getGuid(int nposition)
    {
        if (nposition >= m_lstOrders.getCount()) return "";
        Map<String, Object> item = (Map<String, Object>) m_lstOrders.getAdapter().getItem(nposition);
        if (item == null) return "";
        String guid = (String) item.get("guid");
        return guid;
    }

    private  void previewOrder(String guid)
    {
        m_viewOrder.setRowsCols(1, 1);
        m_viewOrder.setLayoutFormat(KDSSettings.LayoutFormat.Vertical);
        KDSDataOrder order = m_db.orderGet(guid);
        if (isLineItemsMode())
        {
            String firstVisibleItemGuid = "";
            if (m_db.orderGetBumped(guid)) {
                for (int i = 0; i < order.getItems().getCount(); i++) {
                    if (i ==0)
                        firstVisibleItemGuid =order.getItems().getItem(i).getGUID();
                    order.getItems().getItem(i).setLocalBumped(false);
                }
            }
            else
            {
                for (int i = 0; i < order.getItems().getCount(); i++) {
                    KDSDataItem item =order.getItems().getItem(i);
                    boolean b = item.getLocalBumped();
                    boolean bb = b?false:true;
                    item.setLocalBumped(bb);
                    if (!bb)
                    {
                        if (firstVisibleItemGuid.isEmpty())
                            firstVisibleItemGuid =item.getGUID();
                    }
                }
            }
            m_viewOrder.getEnv().getStateValues().setFirstShowingOrderGUID(guid);//kpp1-432,
            //
            m_viewOrder.getEnv().getStateValues().setFirstItemGuid(firstVisibleItemGuid);//kpp1-432
            m_viewOrder.getEnv().getStateValues().setFocusedOrderGUID("");
            m_viewOrder.getEnv().getStateValues().setFocusedItemGUID("");

        }
        KDSDataOrders orders = new KDSDataOrders();
        orders.addComponent(order);

        KDSLayout layout = new KDSLayout(m_viewOrder);

        layout.showOrders(orders);
        if (isLineItemsMode()) {
            layout.focusFirstShowingLineItem();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_kdsactivity_unbump, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_0 ||
                keyCode == KeyEvent.KEYCODE_1 ||
                keyCode == KeyEvent.KEYCODE_2 ||
                keyCode == KeyEvent.KEYCODE_3 ||
                keyCode == KeyEvent.KEYCODE_4 ||
                keyCode == KeyEvent.KEYCODE_5 ||
                keyCode == KeyEvent.KEYCODE_6 ||
                keyCode == KeyEvent.KEYCODE_7 ||
                keyCode == KeyEvent.KEYCODE_8 ||
                keyCode == KeyEvent.KEYCODE_9
                )
        {
            int position = keyCode - KeyEvent.KEYCODE_0;
            String guid = getGuid(position);
            if (guid.isEmpty()) return false;
            if (guid.equals(m_selectedOrderGuid)) {
                onBtnRestoreClicked(null);
            }
            else
            {
                onItemClicked(position, null);

            }
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        KDSKbdRecorder.convertKeyEvent(keyCode, event);
        return super.onKeyUp(keyCode, event);
    }
    KDSKbdKeysCombination m_keysCombinationChecker = new KDSKbdKeysCombination(KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_NUMPAD_SUBTRACT );

//    boolean m_enterIsDown = false;
//    boolean m_substractIsDown = false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (m_keysCombinationChecker.dispatchKeyEventCheckPressed(event))
        {
            onBtnBackClicked(null);
        }
        return super.dispatchKeyEvent(event);
    }

}
