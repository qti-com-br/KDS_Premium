package com.bematechus.kdsrouter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSUIColorPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.MySpinnerArrayAdapter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class KDSUIDlgScheduleEvent extends KDSUIDialogBase implements KDSUIDialogBase.KDSDialogBaseListener {
    static final String TAG = "KDSUIDlgScheduleItem";


    EditText m_txtDescription = null;
    EditText m_txtOrderID = null;
    EditText m_txtDuration = null;

    Spinner  m_spinnerWeekDay = null;
   // Spinner  m_spinnerHour = null;
    //Spinner  m_spinnerMins = null;
    Button m_btnStartTime = null;

    EditText m_txtScreen = null;

    Button m_btnNewItem = null;
    Button m_btnDelItem = null;
    Button m_btnQtyIncrease = null;
    Button m_btnQtyDecrease = null;

    ListView m_lstItems = null;

    List<KDSDataItem> m_arItems = new ArrayList<>();

    WeekEvent m_scheduleEvent = null; //edit this.

    boolean m_bAddNew = false;
    public boolean isAddNew()
    {
        return m_bAddNew;
    }
    @Override
    public void onOkClicked()
    {//save data here
        //show data
        m_scheduleEvent.setSubject(m_txtDescription.getText().toString());
        m_scheduleEvent.setWeekDay(m_spinnerWeekDay.getSelectedItemPosition());
        m_scheduleEvent.setOrderID(m_txtOrderID.getText().toString());

        m_scheduleEvent.setTimeFrom(m_btnStartTime.getText().toString());// m_spinnerHour.getSelectedItemPosition(), m_spinnerMins.getSelectedItemPosition());
        m_scheduleEvent.setDuration(KDSUtil.convertStringToInt( m_txtDuration.getText().toString(), 0));

        m_scheduleEvent.getItems().clear();
        m_scheduleEvent.getItems().addAll(m_arItems);

        m_scheduleEvent.resetState();


    }

    public void setWeekDay(int nWeekDay)
    {
        m_spinnerWeekDay.setSelection(nWeekDay);
    }
    public void setTime(WeekEvent.FloatTime initTime)
    {
        String s = initTime.toHourMinsString();
        m_btnStartTime.setText(s);

    }
    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_scheduleEvent;


    }
    private boolean checkAndShowErrorMessage()
    {
        if (isValid())
            return true;
        if (m_txtDescription.getText().toString().isEmpty() )
        {
            AlertDialog dlg = new AlertDialog.Builder(this.getView().getContext())
                    .setTitle(this.getView().getContext().getString(R.string.error))
                    .setPositiveButton(this.getView().getContext().getString(R.string.ok), null)
                    .setMessage(this.getView().getContext().getString(R.string.fill_description))//"Fill description before save")
                    .create();
            dlg.show();
            return false;
        }
        if (m_txtOrderID.getText().toString().isEmpty())
        {
            AlertDialog dlg = new AlertDialog.Builder(this.getView().getContext())
                            .setTitle(this.getView().getContext().getString(R.string.error))

                            .setPositiveButton(this.getView().getContext().getString(R.string.ok), null)
                            .setMessage(this.getView().getContext().getString(R.string.fill_order_id))
                            .create();
            dlg.show();
            return false;
        }
        return false;

    }
    @Override
    public void show() {
        super.show();

        this.getView().findViewById(R.id.llBackground).requestFocus();
        Button btn =dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (btn != null)
        {
            btn.setText(this.getView().getContext().getString(R.string.save));

        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkAndShowErrorMessage())
                    return;
                onOkClicked();

                if (KDSUIDlgScheduleEvent.this.listener != null) {
                    KDSUIDlgScheduleEvent.this.listener.onKDSDialogOK(KDSUIDlgScheduleEvent.this, getResult());
                }

                dialog.dismiss();

            }
        });

        checkValidation();

    }

    private void initSpinner(Context context, Spinner spinner, List<String> list)
    {


        MySpinnerArrayAdapter adapter;
        adapter = new MySpinnerArrayAdapter(context, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }


    public KDSUIDlgScheduleEvent(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, WeekEvent item) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_schedule_item, "");
        this.m_scheduleEvent = item;
        if (m_scheduleEvent == null) {
            this.setTitle(context.getString(R.string.new_schedule));
            m_scheduleEvent = new WeekEvent();
            item = m_scheduleEvent;
            m_bAddNew = true;
        }
        else
            this.setTitle(m_scheduleEvent.getSubject());


        m_txtDescription = (EditText)getView().findViewById(R.id.txtDescription);



        m_txtDuration =  (EditText)getView().findViewById(R.id.txtDuration);
        m_txtOrderID =  (EditText)getView().findViewById(R.id.txtOrderID);


        m_spinnerWeekDay = (Spinner) getView().findViewById(R.id.spinnerWeekDay);
        ArrayList<String> arWeekDayNames = new ArrayList<>();
        WeekEvtHeader.GetWeekDayNames(this.getView().getContext(), arWeekDayNames);
        initSpinner(this.getView().getContext(), m_spinnerWeekDay, arWeekDayNames);
        m_spinnerWeekDay.setSelection(item.getWeekDay());

        m_btnStartTime = (Button)getView().findViewById(R.id.btnTimeStart);
        m_btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onChooseStartTime();
            }
        });
        m_btnStartTime.setText(item.getTimeFrom().toHourMinsString());


        m_btnNewItem = (Button)getView().findViewById(R.id.btnNew);
        m_btnNewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNewItemButtonClicked(v);
            }
        });
        m_btnDelItem = (Button)getView().findViewById(R.id.btnDelete);
        m_btnDelItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteItemButtonClicked(v);
            }
        });


        m_btnQtyDecrease = (Button)getView().findViewById(R.id.btnQtyDecrease);
        m_btnQtyDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQtyDecreaseButtonClicked(v);
            }
        });
        m_btnQtyIncrease = (Button)getView().findViewById(R.id.btnQtyIncrease);
        m_btnQtyIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQtyIncreaseButtonClicked(v);
            }
        });
        //show data
        m_txtDescription.setText(item.getSubject());

        int nduration =  item.getDurationFloatTimeAsMinutes();
        if (nduration <=0) nduration = 30;
        m_txtDuration.setText(KDSUtil.convertIntToString(nduration));
        m_txtOrderID.setText(item.getOrderID());

        cloneItems(item.getItems(), m_arItems);

        m_lstItems = (ListView) getView().findViewById(R.id.lstItems);
        m_lstItems.setAdapter(new MyAdapter(context, m_arItems));
        //setListViewData(m_lstItems, m_arItems, m_scheduleEvent.getItems());
        showScrollBar(m_lstItems);
        m_txtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkValidation();
            }
        });
        m_txtOrderID.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {   }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {     }
            @Override
            public void afterTextChanged(Editable s) {
                checkValidation();
            }
        });
    }

    public void onChooseStartTime()
    {
        String s = m_btnStartTime.getText().toString();
        s = "1999-9-9 " + s + ":00";
        Date dt = KDSUtil.convertStringToDate(s);

        DialogTimePicker dlg = new DialogTimePicker(this.getView().getContext(),dt, this );
        //dlg.setTag();
        dlg.show();
    }
    private boolean isValid()
    {
        boolean bValid = true;
        if (m_txtDescription.getText().toString().isEmpty() )
            bValid = false;
        if (m_txtOrderID.getText().toString().isEmpty())
            bValid = false;
        return bValid;
    }
    private void checkValidation()
    {
    }

    private void cloneItems(ArrayList<KDSDataItem> src, List<KDSDataItem> target)
    {
        target.clear();
        for (int i=0; i< src.size() ; i++)
        {
            KDSDataItem d = new KDSDataItem();
            src.get(i).copyTo(d);
            target.add(d);
        }
    }

    public void onDeleteItemButtonClicked(View v)
    {
        KDSDataItem item =  ((MyAdapter)m_lstItems.getAdapter()).getFocusedItem();
        if (item == null) return;
        //m_arItems.remove(n);
        m_arItems.remove(item);
        m_lstItems.clearFocus();
        notifyListViewChanged(m_lstItems);
    }


    private void onAddNewItemButtonClicked(View v)
    {
        KDSUIDlgSelectOrderItem dlg = new KDSUIDlgSelectOrderItem(this.getView().getContext(), this, KDSGlobalVariables.getKDS().getRouterDB());
        dlg.show();

    }
    private void onQtyDecreaseButtonClicked(View v)
    {
        KDSDataItem item =  ((MyAdapter)m_lstItems.getAdapter()).getFocusedItem();
        if (item == null) return;
        //m_arItems.remove(n);
        int n =(int) item.getQty();
        if (n <=1) return;
        item.setQty( item.getQty() -1);

        notifyListViewChanged(m_lstItems);

    }

    private void onQtyIncreaseButtonClicked(View v)
    {
        KDSDataItem item =  ((MyAdapter)m_lstItems.getAdapter()).getFocusedItem();
        if (item == null) return;

        item.setQty( item.getQty() +1);

        notifyListViewChanged(m_lstItems);

    }

    public void onCancel(KDSUIColorPickerDialog dialog)
    {

    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void notifyListViewChanged(ListView lv)
    {
        ((MyAdapter)lv.getAdapter()).notifyDataSetChanged();
    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof KDSUIDlgSelectOrderItem)
        {
            KDSUIDlgSelectOrderItem dlg = (KDSUIDlgSelectOrderItem)dialog;
//
            String s = (String)dialog.getResult();
            if (s.isEmpty()) return;
            KDSDataItem item = new KDSDataItem(m_scheduleEvent.getGUID());
            item.setDescription(s);
            item.setQty(1);
            item.setToStationsString(dlg.getToStation());
            item.setOrderGUID(m_scheduleEvent.getGUID());
            item.setCategory(dlg.getCategory());
            m_arItems.add(item);
            notifyListViewChanged(m_lstItems);
        }
        else if (dialog instanceof DialogTimePicker)
        {
            DialogTimePicker dlg = (DialogTimePicker)dialog;

            Date dt =(Date) dlg.getResult();
            if (dt == null) return;
            WeekEvent.FloatTime ft = new WeekEvent.FloatTime();
            ft.set(dt);

            m_btnStartTime.setText(ft.toHourMinsString());
        }

    }
    private void showScrollBar(ListView lv)
    {

        lv.setScrollBarFadeDuration(0);
        lv.setScrollbarFadingEnabled(false);

    }

    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        List<KDSDataItem> m_listData = null;
        View m_viewEditing = null;
        View m_focusedView = null;
        public MyAdapter(Context context, List<KDSDataItem> objects) {
            //super(context, 0, 0, new ArrayList<KDSDataItem>());
            this.mInflater = LayoutInflater.from(context);
            m_listData = objects;
        }
        public KDSDataItem getFocusedItem()
        {
            if (m_focusedView == null) return null;
            return (KDSDataItem) m_focusedView.getTag();
        }
        public void clearFocuse()
        {
            m_focusedView = null;
        }
        public int getCount() {

            return m_listData.size();
        }
        public Object getItem(int arg0) {

            return m_listData.get(arg0);
        }
        public long getItemId(int arg0) {

            return arg0;
        }
        private void init_view_click_event(View v, Object objTag)
        {
            v.setTag(objTag);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //this.onListItemClicked((View) (v.getTag()));
                    MyAdapter.this.m_viewEditing = v;

                }
            });
        }
        private void init_view_focus_event(View v, Object objTag)
        {
            v.setTag(objTag);
            v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {

                        //PreferenceFragmentStations.this.onListItemClicked((View) (v.getTag()));
                        MyAdapter.this.m_viewEditing = v;
                        setFocusedView((View) v.getTag());

                    }
                }
            });



        }
        final static int TAG_KEY = 1001;
        private void init_edittext_changed_event(EditText viewText, KDSDataItem item)
        {

            CustomTextWatcher tw =  new CustomTextWatcher(viewText, item) {

                public void afterTextChanged(Editable s) {

                    EditText t = this.getEditText();
                    KDSDataItem item =(KDSDataItem) t.getTag(R.id.tag_edit);

                    if (item != null)
                        item.setToStationsString(s.toString());

                }
            };

            viewText.addTextChangedListener(tw);
            viewText.setTag(R.id.tag_textwatch, tw);

        }

        private void setFocusedView(View v)
        {
            if (MyAdapter.this.m_focusedView!= null)
                MyAdapter.this.m_focusedView.setBackgroundColor(Color.WHITE);
            MyAdapter.this.m_focusedView = v;//(View) v.getTag();

            int color = v.getContext().getResources().getColor(R.color.list_focuseditem_bg);
            MyAdapter.this.m_focusedView.setBackgroundColor( color);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            //ViewHolder holder = null;
            KDSDataItem r = m_listData.get(position);
            boolean bNewView = false;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.kdsui_schedule_items_listitem, null);
                bNewView = true;

            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setFocusedView(v);
                }
            });
            convertView.setTag(r);
            EditText txtToStation = ((EditText) convertView.findViewById(R.id.txtToStations));//


            init_view_focus_event(txtToStation, convertView);

            txtToStation.setTag(R.id.tag_edit,r);

            if (bNewView)
                init_edittext_changed_event(txtToStation, r);
            else
            {
                CustomTextWatcher tw = (CustomTextWatcher) txtToStation.getTag(R.id.tag_textwatch);
                tw.setmEditText(txtToStation);

            }
            txtToStation.setText(r.getToStationsString());

            TextView txtQty =(TextView) convertView.findViewById(R.id.txtQty);
            txtQty.setText(KDSUtil.convertIntToString((int)r.getQty()));
            init_view_focus_event(txtQty, convertView);

            TextView txtDescription =(TextView) convertView.findViewById(R.id.txtDescription);
            txtDescription.setText(r.getDescription());
            init_view_focus_event(txtDescription, convertView);


            return convertView;
        }

    }

    private class CustomTextWatcher implements TextWatcher {
        private EditText mEditText;
        private KDSDataItem m_Item = null;
        public void setmEditText(EditText t)
        {
            mEditText = t;
        }
        public CustomTextWatcher(EditText e, KDSDataItem item) {
            mEditText = e;
            m_Item = item;

        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void afterTextChanged(Editable s) {
        }
        public EditText getEditText()
        {
            return mEditText;
        }
        public KDSDataItem getItem()
        {
            return m_Item;
        }
    }

}
