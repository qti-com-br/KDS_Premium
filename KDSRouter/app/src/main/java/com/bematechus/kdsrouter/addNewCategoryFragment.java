package com.bematechus.kdsrouter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * to handle interaction events.
 * Use the {@link addNewCategoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class addNewCategoryFragment extends Fragment  implements KDSUIDlgCategory.KDSDialogBaseListener {

    private OnNewCategoryFragmentInteractionListener mListener;

    public addNewCategoryFragment() {
        // Required empty public constructor
    }

    public void setListener(OnNewCategoryFragmentInteractionListener l)
    {
        mListener = l;
    }
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment addNewCategoryFragment.
     */
    public static addNewCategoryFragment newInstance(String param1, String param2) {
        addNewCategoryFragment fragment = new addNewCategoryFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_add_new_category, container, false);
        Button btn = (Button) view.findViewById(R.id.btnNewCategory);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNewCategory();
            }
        });

        ImageView img = (ImageView) view.findViewById(R.id.imgAddNewCategory);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNewCategory();
            }
        });
        return view;
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

            save(c);
            if (mListener != null)
                mListener.onNewCategoryFragmentInteraction(c);
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

    public void onAddNewCategory()
    {
        KDSUIDlgCategory dlg = new KDSUIDlgCategory(addNewCategoryFragment.this.getActivity(),addNewCategoryFragment.this, null);
        dlg.show();
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnNewCategoryFragmentInteractionListener {

        void onNewCategoryFragmentInteraction(KDSRouterDataCategory c);
    }
}
