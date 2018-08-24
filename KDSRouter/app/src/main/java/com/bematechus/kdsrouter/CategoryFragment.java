package com.bematechus.kdsrouter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
//import android.support.v7.widget.GridLayoutManager;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdsrouter.dummy.DummyContent;
import com.bematechus.kdsrouter.dummy.DummyContent.DummyItem;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnCategoryListFragmentInteractionListener}
 * interface.
 */
public class CategoryFragment extends Fragment implements KDSUIDlgCategory.KDSDialogBaseListener {

    public interface onCategoryOperations
    {
        public void onCategoryAddNew(KDSRouterDataCategory category);
        public void onCategoryDelete(KDSRouterDataCategory category);
    }
    private OnCategoryListFragmentInteractionListener mListener;
    ListView m_lstCategory = null;

    private onCategoryOperations m_categoryOperationsListener = null;
    public void  setOnCateogryOperationsListener(onCategoryOperations listener)
    {
        m_categoryOperationsListener = listener;
    }

    public CategoryFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category_list, container, false);

        m_lstCategory = (ListView)view.findViewById(R.id.lstCategory);
        List  lst =  new ArrayList<KDSRouterDataCategory>();
        MyAdapter adapter = new MyAdapter(this.getActivity(), lst);
        m_lstCategory.setAdapter(adapter);
        adapter.setListener(mListener);

        ( view.findViewById(R.id.btnCategoryNew)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CategoryFragment.this.addNew();
            }
        });

        m_lstCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((MyAdapter)m_lstCategory.getAdapter()).changeSelect(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        load();
        adapter.notifyDataSetChanged();

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnCategoryListFragmentInteractionListener) {
            mListener = (OnCategoryListFragmentInteractionListener) activity;
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
    public interface OnCategoryListFragmentInteractionListener {

        public void onCategoryListFragmentInteraction(KDSRouterDataCategory category);
        public void onCategoryRemovedAll();
    }
    public void setSelected(KDSRouterDataCategory category)
    {
        ((MyAdapter) m_lstCategory.getAdapter()).changeSelect(category);
    }
    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    KDSDBRouter getDB()
    {
        return KDSDBRouter.open(this.getActivity().getApplicationContext());
    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof  KDSUIDlgCategory)
        {
            KDSRouterDataCategory c = (KDSRouterDataCategory)obj;
            if (((KDSUIDlgCategory) dialog).isAddNew()) {

                ((MyAdapter) m_lstCategory.getAdapter()).getListData().add(c);
                //getDB().categoryAdd(c);

            }
            save(c);

            ((MyAdapter) m_lstCategory.getAdapter()).notifyDataSetChanged();
            if (m_categoryOperationsListener != null)
                m_categoryOperationsListener.onCategoryAddNew(c);
        }
        else  if (dialog instanceof KDSUIDialogConfirm)
        {
            KDSRouterDataCategory c = (KDSRouterDataCategory)obj;
            ((MyAdapter)m_lstCategory.getAdapter()).getListData().remove(c);
            ((MyAdapter)m_lstCategory.getAdapter()).notifyDataSetChanged();
            delete(c);
            if (m_categoryOperationsListener != null)
                m_categoryOperationsListener.onCategoryDelete(c);

        }
    }


    public void addNew()
    {

        KDSUIDlgCategory dlg = new KDSUIDlgCategory(CategoryFragment.this.getActivity(),CategoryFragment.this, null);
        dlg.show();
    }

    public void delete(KDSRouterDataCategory category)
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        db.categoryDelete(category.getGUID());
        if (m_lstCategory.getCount()<=0)
        {
            if (mListener != null)
                mListener.onCategoryRemovedAll();
        }
    }

    public void save(KDSRouterDataCategory category)
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        if (db.categoryExisted(category.getGUID()))
            db.categoryInfoUpdate(category);
        else
            db.categoryAddInfo(category);


    }
    public void load()
    {
        KDSDBRouter db = KDSDBRouter.open(this.getActivity().getApplicationContext());
        ArrayList<KDSRouterDataCategory> ar = db.categoryLoadAllInfo();
        ((MyAdapter)m_lstCategory.getAdapter()).setListData(ar);
        ((MyAdapter) m_lstCategory.getAdapter()).notifyDataSetChanged();


    }

    public KDSRouterDataCategory getFirstCategory()
    {
       List<KDSRouterDataCategory> ar = ((MyAdapter)m_lstCategory.getAdapter()).getListData();
        if (ar.size()<=0) return null;
        return ar.get(0);
    }

    private class MyAdapter extends BaseAdapter {
        private KDSRouterDataCategory m_selected = null;

        private LayoutInflater mInflater;

        public List<KDSRouterDataCategory> m_listData; //KDSStationsRelation class array
        private OnCategoryListFragmentInteractionListener mListener;

        public void setListener(OnCategoryListFragmentInteractionListener l)
        {
            mListener = l;
        }

        public MyAdapter(Context context, List<KDSRouterDataCategory> data) {
            this.mInflater = LayoutInflater.from(context);
            m_listData = data;
        }
        public List<KDSRouterDataCategory> getListData()
        {
            return m_listData;
        }
        public void setListData(List<KDSRouterDataCategory> lst)
        {
            m_listData = lst;
        }

        public void changeSelect(int position)
        {
            changeSelect( m_listData.get(position));

        }

        public  void changeSelect(Object obj)
        {
            if (m_selected == obj) return;
            m_selected =  (KDSRouterDataCategory)obj;
            notifyDataSetChanged();
            if (null != mListener) {

                mListener.onCategoryListFragmentInteraction(m_selected);
            }
        }
        public KDSRouterDataCategory getSelected()
        {
            return m_selected;
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
            //ViewHolder holder = null;
            KDSRouterDataCategory r =  m_listData.get(position);
            if (convertView == null) {
                //holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.fragment_category, null);


                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeSelect((KDSRouterDataCategory) (v.getTag()));
                    }


                });


                ImageView btnDel = ((ImageView)convertView.findViewById(R.id.txtDel));
                btnDel.setTag(r);

                btnDel.setOnClickListener(new View.OnClickListener() {
                                              @Override
                                              public void onClick(View v) {
                                                  KDSRouterDataCategory c = (KDSRouterDataCategory) v.getTag();

                                                  KDSUIDialogConfirm dlg = new KDSUIDialogConfirm(CategoryFragment.this.getActivity(),
                                                          CategoryFragment.this.getString(R.string.confirm_remove_category) + c.getDescription(),
                                                                                                 CategoryFragment.this );
                                                  dlg.setTitle( CategoryFragment.this.getString(R.string.confirm));
                                                  dlg.setTag(c);
                                                  dlg.show();

                                              }
                                          }

                );
            }
            else
            {
                ImageView btnDel = ((ImageView)convertView.findViewById(R.id.txtDel));
                btnDel.setTag(r);

            }
            convertView.setTag(r);

            ((TextView) convertView.findViewById(R.id.txtDescription)).setText(r.getDescription());
            if (m_selected == r)
            {
                convertView.setBackgroundColor(getResources().getColor( R.color.listview_focus_bg));
            }
            else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
                if (r.isAssignedColor()) {
                    ((TextView) convertView.findViewById(R.id.txtDescription)).setBackgroundColor(r.getBG());
                    ((TextView) convertView.findViewById(R.id.txtDescription)).setTextColor(r.getFG());
                } else {
                    ((TextView) convertView.findViewById(R.id.txtDescription)).setBackgroundColor(Color.TRANSPARENT);
                    ((TextView) convertView.findViewById(R.id.txtDescription)).setTextColor(Color.BLACK);
                }
            }

            return convertView;
        }

    }

    public void refreshListWithoutLoadDB()
    {
        ((MyAdapter)m_lstCategory.getAdapter()).notifyDataSetChanged();
    }

}
