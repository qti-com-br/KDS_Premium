package com.bematechus.kds;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/14 0014.
 */
public class KDSUIDialogOrderStatus extends  KDSUIDialogBase {
    public enum OrderState {
        Waiting,
        Processing,
        Delivering_to_counter,
        Sold_out,
        Others,

    }

    ListView m_lstData = null;
    // ArrayList<String> m_lstSortOptions = new ArrayList<>();
    OrderState m_selectedState = OrderState.Waiting;
    Object m_objData = null;
    String m_strOrderName = "";
//
    public void setOrderName(String orderName)
    {
        m_strOrderName = orderName;
    }

    public String getOrderName()
    {
        return m_strOrderName;
    }

    static public String getStatusString(Context context,OrderState orderState)
    {
        switch (orderState)
        {

            case Waiting:
                return context.getString(R.string.order_state_waiting);// "Waiting";

            case Processing:
                return context.getString(R.string.order_state_processing);

            case Delivering_to_counter:
                return context.getString(R.string.order_state_delivering);

            case Sold_out:
                return context.getString(R.string.order_state_sold_out);

            case Others:
                return context.getString(R.string.order_state_others);
            default:
                return "";
        }
    }

    public void setData(Object obj)
    {
        m_objData = obj;
    }

    public Object getData()
    {
        return m_objData;
    }

    @Override
    public void onOkClicked()
    {//save data here
        int ncount = m_lstData.getCount();
        for (int i=0; i< ncount; i++) {
            if ( m_lstData.isItemChecked(i))
            {
                m_selectedState = OrderState.values()[i];
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
        return m_selectedState;


    }

    public void int_dialog(Context context, KDSDialogBaseListener listener, int resDlgID, String neutralButtonText) {
        this.listener = listener;
        m_view = LayoutInflater.from(context).inflate(resDlgID, null);

        dialog = create1ButtonsDialog(context);

        // kill all padding from the dialog window
        dialog.setView(m_view, 0, 0, 0, 0);
        init_dialog_events(dialog);

    }

    public KDSUIDialogOrderStatus(final Context context, KDSDialogBaseListener listener, String orderName) {
        this.int_dialog(context, listener, R.layout.kdsui_sort_orders, "");
        m_strOrderName = orderName;
        m_lstData =(ListView) this.getView().findViewById(R.id.lstData);
        m_lstData.setAdapter(new ArrayAdapter<String>(context,
                android.R.layout.simple_list_item_single_choice, getArray()));
        m_lstData.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        //int n = initSort.ordinal();
        m_lstData.setItemChecked(0, true);
        String strTitle = context.getString(R.string.select_order_status);
        strTitle = strTitle.replace("#", orderName);
        this.setTitle(strTitle);//"Select [" + orderName + "] status.");

    }

    ArrayList<String> getArray()
    {
        ArrayList<String> ar = new ArrayList<>();
        ar.add("[1] " + this.getView().getContext().getString(R.string.order_state_waiting));// "Waiting time ascend");
        ar.add("[2] " + this.getView().getContext().getString(R.string.order_state_processing));//"Waiting time decend");
        ar.add("[3] " + this.getView().getContext().getString(R.string.order_state_delivering));//"Order number ascend");
        ar.add("[4] " + this.getView().getContext().getString(R.string.order_state_sold_out));//"Order number decend");
        ar.add("[5] " + this.getView().getContext().getString(R.string.order_state_others));//"Items count ascend");

        return ar;
    }

    @Override
    public boolean onKeyPressed(KeyEvent ev)
    {
        boolean bAccept = false;
        switch (ev.getKeyCode())
        {
            case KeyEvent.KEYCODE_1:
                m_selectedState = OrderState.Waiting;
                bAccept = true;
                break;
            case KeyEvent.KEYCODE_2:
                m_selectedState = OrderState.Processing;
                bAccept = true;
                break;
            case KeyEvent.KEYCODE_3:
                m_selectedState = OrderState.Delivering_to_counter;
                bAccept = true;
                break;
            case KeyEvent.KEYCODE_4:
                m_selectedState = OrderState.Sold_out;
                bAccept = true;
                break;
            case KeyEvent.KEYCODE_5:
                m_selectedState = OrderState.Others;
                bAccept = true;
                break;
        }
        if (this.listener != null) {
            if (bAccept ) {
                dialog.dismiss();
                this.listener.onKDSDialogOK(this, getResult());
                return true;
            }
        }
        return false;
    }
}
