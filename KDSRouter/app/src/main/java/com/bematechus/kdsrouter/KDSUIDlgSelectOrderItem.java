package com.bematechus.kdsrouter;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/18.
 */
public class KDSUIDlgSelectOrderItem extends KDSUIDialogBase {

    ArrayList<KDSDataShortName> m_arCategory = new ArrayList<>();

    ArrayList<KDSDataShortName> m_arItems = new ArrayList<>();
    String m_strCurrentCategoryGuid = "";
    KDSDBRouter m_db = null;

    String m_strItemDescription = "";
    ListView m_lstCategory = null;
    ListView m_lstItems = null;

    String m_toStation = "";
    String m_strCategory = "";
    boolean m_bAddNew = false;
    public boolean isAddNew()
    {
        return m_bAddNew;
    }
    @Override
    public void onOkClicked()
    {
    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_strItemDescription;
    }

    public String getToStation()
    {
        return m_toStation;
    }

    public String getCategory()
    {
        return m_strCategory;
    }
    private void setListViewData(ListView lv, List<KDSDataShortName> arAdatper,  List<KDSDataShortName> arData)
    {
        arAdatper.clear();
        arAdatper.addAll(arData);

        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
    }
    public KDSUIDlgSelectOrderItem(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, KDSDBRouter db) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_select_orderitem, "");
        this.setTitle(context.getString(R.string.select_item));//"Select item");
        m_db = db;
        m_lstCategory = (ListView)getView().findViewById(R.id.lstCategory);
        m_lstCategory.setAdapter(new ArrayAdapter(context,android.R.layout.simple_list_item_single_choice, m_arCategory));
        setListViewData(m_lstCategory, m_arCategory, db.categoryGetAllShortNames());

        m_lstCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_strCurrentCategoryGuid = m_arCategory.get(position).getGuid();
                refreshItems();
            }
        });

        m_lstItems =  (ListView)getView().findViewById(R.id.lstItems);
        m_lstItems.setAdapter(new ArrayAdapter(context,android.R.layout.simple_list_item_single_choice, m_arItems));
        refreshItems();

        m_lstItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                saveSelectedItem();

            }
        });
    }
    private int getListViewSelectedItemIndex(ListView lv)
    {
        for (int i=0; i<lv.getCount(); i++)
        {
            if (lv.isItemChecked(i))
                return i;
        }
        return -1;

    }
    public void saveSelectedItem()
    {
        int category = getListViewSelectedItemIndex(m_lstCategory);
        int item = getListViewSelectedItemIndex(m_lstItems);

        m_strItemDescription =  m_arItems.get(item).getDescription();
        String tostation = m_arCategory.get(category).getToStation();
        if (!m_arItems.get(item).getToStation().isEmpty())
            tostation = m_arItems.get(item).getToStation();
        m_toStation = tostation;

        m_strCategory = m_arCategory.get(category).getDescription();

    }

    public void refreshItems()
    {

        setListViewData(m_lstItems, m_arItems, m_db.itemGetAllItemsShortNames(m_strCurrentCategoryGuid));
    }


}
