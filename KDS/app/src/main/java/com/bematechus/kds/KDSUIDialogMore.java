package com.bematechus.kds;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/7 0007.
 */
public class KDSUIDialogMore extends KDSUIDialogBase {

    public enum FunctionMore
    {
        Clear_Messages,
        Cook_Started,

    }

    ListView m_lstData = null;

    FunctionMore m_selectedFunc = FunctionMore.Clear_Messages;

    KDSUser.USER m_userID = KDSUser.USER.USER_A;

    public KDSUser.USER getUserID()
    {
        return m_userID;
    }
    @Override
    public void onOkClicked()
    {//save data here
        int ncount = m_lstData.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstData.isItemChecked(i))
            {
                m_selectedFunc = FunctionMore.values()[i]; //there is one "manually" in it
                return;
            }
        }
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_selectedFunc;


    }

    public KDSUIDialogMore(final Context context,KDSUser.USER userID, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsuit_dlg_more, "");
        m_userID = userID;
        m_lstData =(ListView) this.getView().findViewById(R.id.lstData);
        m_lstData.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, getArray()));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        if (m_lstData.getCount() >0)
            m_lstData.setItemChecked(0, true);
    }

    /**
     * The array sequence should same as the FunctionMore.
     * @return
     */
    ArrayList<String> getArray()
    {
        ArrayList<String> ar = new ArrayList<>();

        ar.add(this.getView().getContext().getString(R.string.clear_messages));
        ar.add(this.getView().getContext().getString(R.string.cook_started));


        return ar;
    }
}
