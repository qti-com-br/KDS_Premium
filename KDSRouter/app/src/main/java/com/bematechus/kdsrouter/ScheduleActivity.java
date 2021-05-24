package com.bematechus.kdsrouter;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.ScrollView;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSLog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.ThemeUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleActivity extends Activity implements WeekEvtView.WeekEventViewEvents, KDSUIDialogBase.KDSDialogBaseListener {

    static final String TAG = "ScheduleActivity";

    PowerManager.WakeLock m_wakeLock = null;

    WeekEvtView m_evtView = null;
    WeekEvtHeader m_header = null;

    ArrayList<WeekEvent> m_arEvents = new ArrayList<>();

    KDSScrollV m_svSchedule = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_evtView = (WeekEvtView)this.findViewById(R.id.schView);
        m_header =  (WeekEvtHeader)this.findViewById(R.id.schHeader);
        m_svSchedule = (KDSScrollV) this.findViewById(R.id.svSchedule);

        m_svSchedule.setOnScrollListener(new KDSScrollV.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int x, int y, int oldxX, int oldY) {
                onScrollViewChanged(x, y, oldxX, oldY);
            }
        });

        m_evtView.setEventsReceiver(this);
        m_arEvents = KDSGlobalVariables.getKDS().getRouterDB().scheduleGetAll();
        m_evtView.setItems(m_arEvents);
        m_evtView.refresh();

        ImageView imgMenu =  this.findViewById(R.id.imgMenu);
        imgMenu.setColorFilter(ThemeUtil.getAttrColor(this.getApplicationContext(), R.attr.kds_title_fg));
        checkViewPortForMore();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {

        if (hasFocus)
        {
            checkViewPortForMore();
        }
    }

    public void onScrollViewChanged(int x, int y, int oldxX, int oldY )
    {
        checkViewPortForMore();
        KDSLog.d(TAG, "x="+ KDSUtil.convertIntToString(x) +",y="+KDSUtil.convertIntToString(y));
        KDSLog.d(TAG, "oldx="+KDSUtil.convertIntToString(oldxX) +",oldy="+KDSUtil.convertIntToString(oldY));
    }

    public void checkViewPortForMore()
    {
        int nY = m_svSchedule.getScrollY();
        ArrayList<Integer> arMoreDay = new ArrayList<>();
        ArrayList<Integer> arPrevDay = new ArrayList<>();

        m_evtView.checkMoreOrLessInViewPort(nY,m_svSchedule.getHeight(), arMoreDay, arPrevDay);
        m_header.refreshPrevNextIcon(arMoreDay, arPrevDay);
    }

    public void onScheduleAddNewItem(int nWeekDay, WeekEvent.FloatTime initTime)
    {
        KDSUIDlgScheduleEvent dlg = new KDSUIDlgScheduleEvent(this, this, null);
        dlg.setWeekDay(nWeekDay);
        dlg.setTime(initTime);
        dlg.show();

    }
    public void onScheduleDeleteItem(WeekEvent item)
    {
        m_evtView.delete(item);
        KDSGlobalVariables.getKDS().getRouterDB().scheduleDelete(item.getGUID());
        checkViewPortForMore();
    }
    public void onScheduleClearAllItems()
    {
        m_evtView.clearAll();
        KDSGlobalVariables.getKDS().getRouterDB().scheduleClearAll();
        checkViewPortForMore();
    }
    public void onScheduleEditItem(WeekEvent item)
    {
        KDSUIDlgScheduleEvent dlg = new KDSUIDlgScheduleEvent(this, this, item);
        dlg.show();
    }
    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        KDSUIDlgScheduleEvent dlg = (KDSUIDlgScheduleEvent) dialog;
        if (dlg.isAddNew())
        {
            WeekEvent evt = new WeekEvent((WeekEvent)dlg.getResult());
            m_evtView.add(evt);
            KDSGlobalVariables.getKDS().getRouterDB().scheduleAdd(evt);
            checkViewPortForMore();

        }
        else
        { //edit
            WeekEvent evt = new WeekEvent((WeekEvent)dlg.getResult());
            KDSGlobalVariables.getKDS().getRouterDB().scheduleDelete(evt.getGUID());
            KDSGlobalVariables.getKDS().getRouterDB().scheduleAdd(evt);
            checkViewPortForMore();
        }
        m_evtView.refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_schedule_add) {
            Calendar c = Calendar.getInstance();
            int nDay = c.get(Calendar.DAY_OF_WEEK)-1;
            WeekEvent.FloatTime f = new WeekEvent.FloatTime();
            f.set(c.getTime());

            onScheduleAddNewItem(nDay, f);
        }

        else if (id == R.id.action_schedule_clearall) {
            onScheduleClearAllItems();
        }
        return super.onOptionsItemSelected(item);
    }
    private void showPopupMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);  //建立PopupMenu对象
        popup.getMenuInflater().inflate(R.menu.menu_schedule,   //压入XML资源文件
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return ScheduleActivity.this.onOptionsItemSelected(item);
            }
        });

        popup.show();

        //>>>>>>>>>>>>>>>>>>>>>>>>>> IMPORTANT <<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //this will map the keycode, please check it if use this KDS in new android API.
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            Method method = field.get(popup).getClass().getMethod("getPopup");
            ListPopupWindow lw = (ListPopupWindow) method.invoke(field.get(popup));

            lw.getListView().setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN)
                    {
                        if (event.getRepeatCount()==0)
                            KDSKbdRecorder.convertKeyEvent(keyCode, event);
                    }

                    return false;
                }
            });

        } catch (Exception err) {
            KDSLog.e(TAG,KDSLog._FUNCLINE_() ,err);//+ err.toString());
            //KDSLog.e(TAG, KDSUtil.error( err));
        }

    }

    public void onMenuClicked(View v)
    {
        showPopupMenu(v);
    }
}
