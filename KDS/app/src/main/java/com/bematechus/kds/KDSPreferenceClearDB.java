package com.bematechus.kds;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/4/8 0008.
 */
public class KDSPreferenceClearDB  extends Preference implements KDSUIDialogBase.KDSDialogBaseListener {

    public KDSPreferenceClearDB(Context context, AttributeSet attrs) {
        super(context, attrs);


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
    public void onKDSDialogOK(KDSUIDialogBase dlg, Object obj)// ArrayList<String> stations)
    {
        KDSGlobalVariables.getKDS().clearAll();

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                KDSGlobalVariables.getKDS().getBroadcaster().broadcastClearDBCommand();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override protected void onClick() {

        KDSUIDialogBase d = new KDSUIDialogBase();
        d.createOkCancelDialog(this.getContext(),
                MainActivity.Confirm_Dialog.Clear_DB,
                this.getContext().getString(R.string.confirm),
                this.getContext().getString(R.string.confirm_clear_db), false, this);
        d.show();



    }


}
