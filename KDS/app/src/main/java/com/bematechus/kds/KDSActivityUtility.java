package com.bematechus.kds;

import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;

/**
 *
 * Backup or restore database.
 */

public class KDSActivityUtility extends Activity implements  KDSUIDialogBase.KDSDialogBaseListener {


    enum RestoreDBSource
    {
        Customer_Backup,
        Auto_Backup,
    }
    ListView m_lstData = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_kdsactivity_utility);

        m_lstData = (ListView) this.findViewById(R.id.lstData);
        init_restore_list(m_lstData);
        Button btn = (Button)this.findViewById(R.id.btnRestoreDB);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonRestoreClicked();
            }
        });

        btn = (Button)this.findViewById(R.id.btnExit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonExitClicked();
            }
        });
        boolean bHide = KDSGlobalVariables.getKDS().getSettings().getBoolean(KDSSettings.ID.Hide_navigation_bar);
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
                    KDSUtil.enableSystemVirtualBar(KDSActivityUtility.this.getWindow().getDecorView(), false);
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

    private void onButtonRestoreClicked()
    {
        RestoreDBSource src =  getSelectDbSource();
        switch (src)
        {

            case Customer_Backup:
                restoreFromCustomerBackup();
                break;
            case Auto_Backup:
                restoreFromAutoBackup();
                break;
        }
    }

    private void onButtonExitClicked()
    {
        this.finish();
    }

    private void restoreFromCustomerBackup()
    {
        showConfirmDialog(RestoreDBSource.Customer_Backup);
    }

    private void restoreFromAutoBackup()
    {
        showConfirmDialog(RestoreDBSource.Auto_Backup);
    }

    /**
     * ip selection dialog
     *
     * @param dialog
     */
    public void onKDSDialogCancel(KDSUIDialogBase dialog) {

    }


    /**
     * ip selection dialog
     *
     * @param dlg
     * @param obj
     */
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)//
    {
        RestoreDBSource dbSource = (RestoreDBSource) dlg.getTag();
        switch (dbSource)
        {

            case Customer_Backup:
                doRestoreFromCustomerBackup();
                break;
            case Auto_Backup:
                doRestoreFromAutoBackup();
                break;
        }
    }

    private void doRestoreFromCustomerBackup()
    {

        restoreDB(MainActivity.DEFAULT_BACKUP_FOLDER);
    }

    private void doRestoreFromAutoBackup()
    {

        restoreDB(MainActivity.AUTO_BACKUP_FOLDER);
    }

    private void restoreDB(String fromFolder)
    {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        boolean bresult = MainActivity.import_data2(fromFolder, usbManager);
        String info = "";
        if (bresult)
        {
            info = this.getString(R.string.restore_db_successfully);
        }
        else
        {
            info = this.getString(R.string.restore_db_failed);
        }

        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }

    private void showConfirmDialog(RestoreDBSource dbSource)
    {
        String info = this.getString(R.string.confirm_restore_db);


        KDSUIDialogBase d = new KDSUIDialogBase();
        d.createOkCancelDialog(this,
                dbSource,
                this.getString(R.string.confirm),
                info, false, this);
        d.show();
    }

    private RestoreDBSource getSelectDbSource()
    {
        int ncount = m_lstData.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstData.isItemChecked(i))
            {
                return RestoreDBSource.values()[i];
            }
        }
        return RestoreDBSource.Auto_Backup;
    }

    private void init_restore_list(ListView lst)
    {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice, getRestoreMethodArray());
        lst.setAdapter(adapter);
    }

    ArrayList<String> getRestoreMethodArray()
    {
        ArrayList<String> ar = new ArrayList<>();

        ar.add(this.getString(R.string.restore_customer_backup_db));
        ar.add(this.getString(R.string.restore_auto_backup_db));

        return ar;
    }

}
