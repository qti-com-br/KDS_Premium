package com.bematechus.kdsstatistic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.bematechus.kdslib.KDSKbdRecorder;
import com.bematechus.kdslib.KDSUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MainActivity extends Activity {
    TextView m_txtTitle = null;

    PowerManager.WakeLock m_wakeLock = null;


    public boolean isMacMatch() {
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            DialogAbout.showAbout(this, getVersionName());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isMacMatch()) {
            showErrorMac();
            return;
        }
        lockAndroidWakeMode(true);

        STSettings.Language language =  STSettings.loadLanguageOption(this.getApplicationContext());
        KDSUtil.setLanguage(this.getApplicationContext(), language);


        setContentView(R.layout.activity_main_gui);

        LinearLayout btnStatistic = (LinearLayout)this.findViewById(R.id.btnStatistic);
        btnStatistic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callStatistic();
            }
        });

        LinearLayout btnSOS = (LinearLayout)this.findViewById(R.id.btnSOS);
        btnSOS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSOS();
            }
        });

        LinearLayout btnSettings = (LinearLayout)this.findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callSettings();
            }
        });


        LinearLayout btnExit = (LinearLayout)this.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callExit();
            }
        });

        m_txtTitle = (TextView)this.findViewById(R.id.txtTitle);
       updateTitle();

    }



    private String getVersionName()
    {
        String appVersion="";
        PackageManager manager = this.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            appVersion = info.versionName; //version name, set it in build.gradle file.

        } catch (Exception e) {

            e.printStackTrace();
        }
        return appVersion;
    }
    public void updateTitle()
    {
        m_txtTitle.setTextColor(this.getResources().getColor(R.color.kds_title_fg));
        String s = getString(R.string.main_title);
        s += " " + getVersionName();

        m_txtTitle.setText(s);
    }

    private void callExit()
    {
        this.finish();
    }

    private void callStatistic()
    {
        Intent intent = new Intent(this, STMainActivity.class);
        startActivity(intent);

        //startActivityForResult( intent, 0);
    }

    private void callSOS()
    {
        Intent intent = new Intent(this, SOSMainActivity.class);
        startActivity(intent);

    }
    private void callSettings()
    {
        Intent i = new Intent(this, GenActivitySettings.class);
        startActivity(i);//, KDSConst.SHOW_PREFERENCES);

    }

    public void showErrorMac()
    {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.error))
                .setMessage(this.getString(R.string.error_match_mac))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        MainActivity.this.killMe();
                    }
                })
                .setCancelable(false)

                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity.this.killMe();
                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }
    public void killMe()
    {
        this.finish();
    }
    public void lockAndroidWakeMode(boolean bLock)
    {
        if (m_wakeLock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            m_wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }
        if (bLock) {
            m_wakeLock.acquire();
            this.getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else {

            m_wakeLock.release();
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }
    public void onBtnLogoClicked(View v)
    {
        showPopupMenu(v);
    }
    private void showPopupMenu(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);  //建立PopupMenu对象
        popup.getMenuInflater().inflate(R.menu.menu_main,   //压入XML资源文件
                popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return MainActivity.this.onOptionsItemSelected(item);
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
            err.printStackTrace();
        }

    }
}
