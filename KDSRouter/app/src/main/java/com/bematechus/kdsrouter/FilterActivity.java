package com.bematechus.kdsrouter;

import android.app.Activity;
import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import com.bematechus.kdslib.KDSKbdRecorder;

public class FilterActivity extends Activity implements CategoryFragment.OnCategoryListFragmentInteractionListener,
        FoodItemFragment.OnItemListFragmentInteractionListener,
        CategoryFragment.onCategoryOperations,
        addNewCategoryFragment.OnNewCategoryFragmentInteractionListener
{

    String m_strBeforeEditingChangesGuid = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_filter);
        CategoryFragment fragment = (CategoryFragment) this.getFragmentManager().findFragmentById(R.id.fragmentCategory);
        fragment.setOnCateogryOperationsListener(this);
        KDSRouterDataCategory c = fragment.getFirstCategory();
        if (c != null)
            onCategoryListFragmentInteraction(c);
        else
        {
            showNewData(true);


        }

        addNewCategoryFragment fragmentNewCategory = (addNewCategoryFragment) this.getFragmentManager().findFragmentById(R.id.fragmentNewCategory);
        fragmentNewCategory.setListener(this);

    }

    /**
     * for update the remote backup router database
     */
    @Override
    protected void onResume() {
        super.onResume();
        m_strBeforeEditingChangesGuid = KDSGlobalVariables.getKDSRouter().getRouterDB().getChangesGUID();


    }

    /**
     * check if the database changed
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        String afterEditingChangesGuid = KDSGlobalVariables.getKDSRouter().getRouterDB().getChangesGUID();

        if (m_strBeforeEditingChangesGuid.equals(afterEditingChangesGuid))
            return;

        KDSGlobalVariables.getKDSRouter().updateMyBackupDatabase();
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {

        boolean b= super.onKeyUp(keyCode, event);
        KDSKbdRecorder.convertKeyEvent(keyCode, event);
        return b;

    }
    public void onCategoryListFragmentInteraction(KDSRouterDataCategory category)
    {
        /**
         * if meet can not convertable error, check if the fragment is come from android.app.fragment.
         */
        if (category != null)
            showNewData(false);
        FoodItemFragment ff = (FoodItemFragment)( this.getFragmentManager().findFragmentById(R.id.fragmentItems));
        ff.setSelectedCategory(category);
        CategoryFragment f = (CategoryFragment)( this.getFragmentManager().findFragmentById(R.id.fragmentCategory));
        f.setSelected(category);


    }

    public void onCategoryRemovedAll()
    {
        showNewData(true);
    }

    public void onItemListFragmentInteraction(KDSRouterDataItem item)
    {
        /**
         * if meet can not convertable error, check if the fragment is come from android.app.fragment.
         */


        CategoryFragment ff = (CategoryFragment)( this.getFragmentManager().findFragmentById(R.id.fragmentCategory));
        ff.refreshListWithoutLoadDB();

    }
    public void onCategoryAddNew(KDSRouterDataCategory category)
    {
        onCategoryListFragmentInteraction(category);
    }
    public void onCategoryDelete(KDSRouterDataCategory category)
    {
        onCategoryListFragmentInteraction(null);
    }

    public void showNewData(boolean bShowNewDataGui)
    {
        if (bShowNewDataGui) {
            findViewById(R.id.fragmentlayout_fooditem).setVisibility(View.GONE);
            findViewById(R.id.main_empty_gui).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.fragmentlayout_fooditem).setVisibility(View.VISIBLE);
            findViewById(R.id.main_empty_gui).setVisibility(View.GONE);
        }
    }
    public void onNewCategoryFragmentInteraction(KDSRouterDataCategory c)
    {
        if (c != null) {

            showNewData(false);
            CategoryFragment ff = (CategoryFragment)( this.getFragmentManager().findFragmentById(R.id.fragmentCategory));
            ff.load();
            KDSRouterDataCategory category = ff.getFirstCategory();
            if (c != null)
                onCategoryListFragmentInteraction(category);
        }
    }
}
