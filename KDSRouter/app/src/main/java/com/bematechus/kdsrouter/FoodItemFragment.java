package com.bematechus.kdsrouter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

//import android.support.v7.widget.GridLayoutManager;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSDataItem;
import com.bematechus.kdslib.KDSUIColorPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUIDialogConfirm;
import com.bematechus.kdslib.KDSUtil;

import java.util.ArrayList;
import java.util.List;

public class FoodItemFragment extends Fragment implements KDSUIDlgFoodItem.KDSDialogBaseListener  , KDSUIColorPickerDialog.OnColorPickerDlgListener  {

    private OnItemListFragmentInteractionListener mListener;

    KDSRouterDataCategory m_categoryInfo = null; //the category has been clicked.
    ListView m_lstItems = null;

    TextView m_txtCategoryDescription = null;
    TextView m_txtStation = null;
    TextView m_txtScreen = null;
    TextView m_txtDelay = null;
    CheckBox m_chkPrintable = null;

    TextView m_txtItemsTitle = null;
    Object m_singleObj = null;
//    int m_nBG = 0;
//    int m_nFG =0;

    public void setSelectedCategory(KDSRouterDataCategory category)
    {
        m_bStartEdit = false;
        m_categoryInfo = category;
        if (category != null)
            showCategory(m_categoryInfo);
            //m_txtCategoryDescription.setText(category.getDescription());
        else
            clear();
            //m_txtCategoryDescription.setText("");
        this.load();
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FoodItemFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    boolean m_bStartEdit = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fooditem_list, container, false);

        m_txtItemsTitle = (TextView)view.findViewById(R.id.txtItemTitle);


        m_txtCategoryDescription = (EditText)view.findViewById(R.id.txtCategory);

        m_txtCategoryDescription.addTextChangedListener(new MyTextWatcher() );

        m_txtStation = (TextView) view.findViewById(R.id.txtStation);
        m_txtStation.addTextChangedListener(new MyTextWatcher() );

        m_txtScreen =(TextView) view.findViewById(R.id.txtScreen);
        m_txtScreen.addTextChangedListener(new MyTextWatcher() );

        m_txtDelay =(TextView) view.findViewById(R.id.txtDelay);
        m_txtDelay.addTextChangedListener(new MyTextWatcher() );
        m_chkPrintable = (CheckBox) view.findViewById(R.id.chkPrintable);
        m_chkPrintable.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                  checkCategoryChanged();
              }
          });
        Button btn = (Button) view.findViewById(R.id.btnBG);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBGButtonClicked(v);
            }
        });

        btn = (Button) view.findViewById(R.id.btnFG);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFGButtonClicked(v);
            }
        });


        m_lstItems = (ListView)view.findViewById(R.id.lstItems);
        List  lst =  new ArrayList<KDSRouterDataCategory>();
        MyAdapter adapter = new MyAdapter(this.getActivity(), lst);
        m_lstItems.setAdapter(adapter);

        ( view.findViewById(R.id.btnItemNew)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_categoryInfo != null)
                    FoodItemFragment.this.addNew();
            }
        });
        load();
        adapter.notifyDataSetChanged();

        return view;
    }


    static private final int TAG_BG = 0;
    static private final int TAG_FG = 1;


    /**
     *
     * @param v
     */
    private void onBGButtonClicked(View v)
    {

        if (m_categoryInfo == null) return;
        if (m_singleObj != null) return;
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getActivity(), m_categoryInfo.getBG(), this);
        m_singleObj = dlg;
        dlg.setTag(TAG_BG);
        dlg.show();
    }

    private void onFGButtonClicked(View v)
    {

        if (m_categoryInfo == null) return;
        if (m_singleObj != null) return;
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getActivity(), m_categoryInfo.getFG(), this);
        m_singleObj = dlg;
        dlg.setTag(TAG_FG);
        dlg.show();

    }

    public void addNew()
    {
        if (m_singleObj != null) return;
        KDSUIDlgFoodItem dlg = new KDSUIDlgFoodItem(FoodItemFragment.this.getActivity(),FoodItemFragment.this, null);
        m_singleObj = dlg;

        dlg.show();
    }

    public void delete(KDSDataItem item)
    {


        delete(item.getGUID());

    }
    public void delete(String itemGUID)
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        db.itemDelete(itemGUID);

    }

    public void save(KDSRouterDataItem item)
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        if (db.itemExisted(item.getGUID()))
            db.itemUpdate(item);
        else
            db.itemAdd(item);


    }
    public void load()
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());

        ArrayList<KDSRouterDataItem> ar = new ArrayList<>();
        if (m_categoryInfo != null)
            ar = db.itemsGetByCategory(m_categoryInfo.getGUID());
        ((MyAdapter)m_lstItems.getAdapter()).setListData(ar);
        ((MyAdapter) m_lstItems.getAdapter()).notifyDataSetChanged();

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnItemListFragmentInteractionListener) {
            mListener = (OnItemListFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {
        m_singleObj = null;
    }
    KDSDBRouter getDB()
    {
        return KDSDBRouter.open(this.getActivity().getApplicationContext());
    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        m_singleObj = null;
        if (dialog instanceof  KDSUIDlgFoodItem)
        {
            if (m_categoryInfo == null) return;
            KDSRouterDataItem c = (KDSRouterDataItem)obj;
            c.setCategoryGuid(m_categoryInfo.getGUID());
            if (((KDSUIDlgFoodItem) dialog).isAddNew()) {
                ((MyAdapter) m_lstItems.getAdapter()).getListData().add(c);
            }
            save(c);
            ((MyAdapter) m_lstItems.getAdapter()).notifyDataSetChanged();
        }
        else  if (dialog instanceof KDSUIDialogConfirm)
        {
            KDSRouterDataItem c = (KDSRouterDataItem)obj;
            ((MyAdapter)m_lstItems.getAdapter()).getListData().remove(c);
            ((MyAdapter)m_lstItems.getAdapter()).notifyDataSetChanged();
            delete(c.getGUID());

        }
    }


    private class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public List<KDSRouterDataItem> m_listData;

        public MyAdapter(Context context, List<KDSRouterDataItem> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<KDSRouterDataItem> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSRouterDataItem> lst)
        {
            m_listData = lst;
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
        public View getView(int position, View convertView, ViewGroup parent) {

            KDSRouterDataItem r =  m_listData.get(position);
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.fragment_fooditem, null);
                ImageView btnEdit = ((ImageView)convertView.findViewById(R.id.txtEdit));
                btnEdit.setTag(r);

                btnEdit.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View v) {
                                                   KDSRouterDataItem item = (KDSRouterDataItem) v.getTag();//  m_listData.get(position);
                                                   KDSUIDlgFoodItem dlg = new KDSUIDlgFoodItem(FoodItemFragment.this.getActivity(),FoodItemFragment.this, item);
                                                   dlg.show();
                                               }
                                           }

                );

                ImageView btnDel = ((ImageView)convertView.findViewById(R.id.txtDel));
                btnDel.setTag(r);

                btnDel.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  KDSRouterDataItem r = (KDSRouterDataItem) v.getTag();

                                                  KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(FoodItemFragment.this.getActivity(),
                                                          FoodItemFragment.this.getString(R.string.confirm_remove_fooditem) + r.getDescription(),
                                                          FoodItemFragment.this );
                                                  dlg.setTitle(FoodItemFragment.this.getString(R.string.confirm));
                                                  dlg.setTag(r);
                                                  dlg.show();
                                              }
                                          }

                );
            }
            else
            {
                ImageView btnEdit = ((ImageView)convertView.findViewById(R.id.txtEdit));
                btnEdit.setTag(r);
                ImageView btnDel = ((ImageView)convertView.findViewById(R.id.txtDel));
                btnDel.setTag(r);
            }


            ((TextView) convertView.findViewById(R.id.txtDescription)).setText(r.getDescription());
            if (r.isAssignedColor()) {
                ((TextView) convertView.findViewById(R.id.txtDescription)).setBackgroundColor(r.getBG());
                ((TextView) convertView.findViewById(R.id.txtDescription)).setTextColor(r.getFG());
            }
            else
            {
                ((TextView) convertView.findViewById(R.id.txtDescription)).setBackgroundColor(Color.TRANSPARENT);
                ((TextView) convertView.findViewById(R.id.txtDescription)).setTextColor(Color.BLACK);
            }
            ((TextView) convertView.findViewById(R.id.txtToStation)).setText(r.getToStation());
            ((TextView) convertView.findViewById(R.id.txtToScreen)).setText(r.getToScreen());
            ((TextView) convertView.findViewById(R.id.txtPrepTime)).setText(r.getPreparationTimeFormated());// KDSUtil.convertFloatToString(r.getPreparationTime()));
            ((CheckBox) convertView.findViewById(R.id.chkPrintable)).setChecked(r.getPrintable());
            ((CheckBox) convertView.findViewById(R.id.chkPrintable)).setEnabled(false);


            return convertView;
        }

    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnItemListFragmentInteractionListener {

        void onItemListFragmentInteraction(KDSRouterDataItem item);
    }

    public void onCancel(KDSUIColorPickerDialog dialog)
    {
        m_singleObj = null;
    }

    public void onOk(KDSUIColorPickerDialog dialog, int color)
    {
        m_singleObj = null;
        if (m_categoryInfo == null) return;
        int ntag = (int) dialog.getTag();
        if (ntag == TAG_BG)
            m_categoryInfo.setBG( color);
        else
            m_categoryInfo.setFG( color );
        showCategoryColor();
        updateDB();

    }

    private void showCategoryColor()
    {
        if ( (m_categoryInfo.getBG() != 0) && ( m_categoryInfo.getFG() !=0 )) {
            m_txtCategoryDescription.setBackgroundColor(m_categoryInfo.getBG());
            m_txtCategoryDescription.setTextColor(m_categoryInfo.getFG());
        }
        else
        {
            m_txtCategoryDescription.setBackgroundColor(Color.TRANSPARENT);
            m_txtCategoryDescription.setTextColor(Color.BLACK);
        }
    }

    private void showCategory(KDSRouterDataCategory category)
    {
        clear();
        showCategoryColor();

        String s = this.getString(R.string.items_for_category);
        s = s.replace("#", category.getDescription());
        m_txtItemsTitle.setText(s);

        m_txtCategoryDescription.setText(category.getDescription());
        m_txtStation.setText(category.getToStation());
        m_txtScreen.setText(category.getToScreen());
        if (category.getDelay() <=0)
            m_txtDelay.setText("");
        else
            m_txtDelay.setText( KDSUtil.convertFloatToString(category.getDelay()));
        m_chkPrintable.setChecked(category.getPrintable());
        m_bStartEdit = true;

    }
    private void clear()
    {
        m_txtItemsTitle.setText("");
        m_txtCategoryDescription.setText("");
        m_txtCategoryDescription.setBackgroundColor(Color.TRANSPARENT);
        m_txtCategoryDescription.setTextColor(Color.BLACK);
        m_txtStation.setText("");

        m_txtScreen.setText("");

            m_txtDelay.setText("");

        m_chkPrintable.setChecked(true);
    }

    private boolean saveCategory()
    {
        m_categoryInfo.setDescription(m_txtCategoryDescription.getText().toString());
        m_categoryInfo.setToStation(m_txtStation.getText().toString());
        m_categoryInfo.setToScreen(m_txtScreen.getText().toString());
        String s = m_txtDelay.getText().toString();
        float flt = KDSUtil.convertStringToFloat(s, 0);
        m_categoryInfo.setDelay(flt);
        m_categoryInfo.setPrintable(m_chkPrintable.isChecked());
        return true;
    }
    private void checkCategoryChanged()
    {
        if (m_categoryInfo == null) return;
        String s = m_txtCategoryDescription.getText().toString();
        boolean bChanged = false;
        if (!s.equals(m_categoryInfo.getDescription()))
            bChanged = true;
        s = m_txtStation.getText().toString();
        if (!s.equals(m_categoryInfo.getToStation()))
            bChanged = true;
        s = m_txtScreen.getText().toString();
        if (!s.equals(m_categoryInfo.getToScreen()))
            bChanged = true;
        s = m_txtDelay.getText().toString();
        float flt = KDSUtil.convertStringToFloat(s, 0);
        if (flt != m_categoryInfo.getDelay())
            bChanged = true;
        if (m_chkPrintable.isChecked() != m_categoryInfo.getPrintable())
            bChanged = true;


        saveCategory();
        if (bChanged) {
           updateDB();
        }
    }

    private void updateDB()
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        db.categoryInfoUpdate(m_categoryInfo);
        if (mListener!= null)
            mListener.onItemListFragmentInteraction(null);
    }

    class MyTextWatcher implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (m_bStartEdit)
                FoodItemFragment.this.checkCategoryChanged();
        }
    }

}
