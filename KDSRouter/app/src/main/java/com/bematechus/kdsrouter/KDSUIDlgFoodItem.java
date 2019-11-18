package com.bematechus.kdsrouter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSDataSumName;
import com.bematechus.kdslib.KDSDataSumNames;
import com.bematechus.kdslib.KDSSMBPath;
import com.bematechus.kdslib.KDSUIColorPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.OpenFileDialog;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class KDSUIDlgFoodItem  extends KDSUIDialogBase implements KDSUIColorPickerDialog.OnColorPickerDlgListener, KDSUIDialogBase.KDSDialogBaseListener {

    static final String TAG = "KDSUIDlgFoodItem";

    static final int TAG_FOR_BUILDCARD = 0;
    static final int TAG_FOR_TRAINING_VIDEO = 1;

    EditText m_txtDescription = null;
    EditText m_txtStation = null;
    EditText m_txtScreen = null;
    //EditText m_txtDelay = null;
    CheckBox m_chkPrintable = null;
    EditText m_txtPrepTime = null;
    EditText m_txtSeconds = null;

    Button m_btnBG = null;
    Button m_btnFG = null;

    ListView m_lstBuildCards = null;
    ListView m_lstTrainingVideo = null;
    ListView m_lstSumNames = null;

    ListView m_lstModifiers = null;

    CheckBox m_chkSumTranslate = null;
    //CheckBox m_chkModifierEnabled = null;

    List<String> m_arBuildCards = new ArrayList<>();
    List<String> m_arTrainingVideo = new ArrayList<>();

    List<KDSDataSumName> m_arSumNames = new ArrayList<>();
    List<KDSRouterDataItemModifier> m_arModifiers = new ArrayList<>();


    KDSRouterDataItem m_item = new KDSRouterDataItem();
    int m_nBG = 0;
    int m_nFG =0;

    boolean m_bAddNew = false;
    public boolean isAddNew()
    {
        return m_bAddNew;
    }
    @Override
    public void onOkClicked()
    {//save data here
        //show data
        m_item.setDescription(m_txtDescription.getText().toString());

        m_item.setBG(m_nBG);
        m_item.setFG(m_nFG);

        m_item.setToStation(m_txtStation.getText().toString());
        m_item.setToScreen(m_txtScreen.getText().toString());

        String mins = m_txtPrepTime.getText().toString();
        String secs = m_txtSeconds.getText().toString();
        int nmins = KDSUtil.convertStringToInt(mins, 0);
        int nsecs = KDSUtil.convertStringToInt(secs, 0);
        float flt = nsecs;
        flt /=60;
        flt += nmins;

        //m_item.setDelay(KDSUtil.convertStringToFloat(m_txtDelay.getText().toString(), 0));
        m_item.setPreparationTime(flt);//KDSUtil.convertStringToFloat(m_txtPrepTime.getText().toString(), 0));
        m_item.setPrintable(m_chkPrintable.isChecked());

        m_item.getBuildCard().getArray().clear();
        m_item.getBuildCard().getArray().addAll(m_arBuildCards);
        m_item.getTrainingVideo().getArray().clear();
        m_item.getTrainingVideo().getArray().addAll(m_arTrainingVideo);

        m_item.setSumTranslateEnabled(m_chkSumTranslate.isChecked());
        m_item.getSumNames().getArray().clear();
        m_item.getSumNames().getArray().addAll(m_arSumNames);

        //m_item.setModifierEnabled(m_chkModifierEnabled.isChecked());
        m_item.getModifiers().getArray().clear();
        m_item.getModifiers().getArray().addAll(m_arModifiers);

    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_item;


    }

    @Override
    public void show() {
        super.show();
        this.getView().findViewById(R.id.rlBackground).requestFocus();

    }

    public KDSUIDlgFoodItem(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, KDSRouterDataItem item) {
        this.int_dialog(context, listener, R.layout.dlg_item, "");
        this.setCancelByClickOutside(false);
        m_item = item;
        if (m_item == null) {
            this.setTitle(context.getString(R.string.new_item));
            m_item = new KDSRouterDataItem();
            item = m_item;
            m_bAddNew = true;
        }
        else
            this.setTitle(m_item.getDescription());


        m_txtDescription = (EditText)getView().findViewById(R.id.txtDescription);

        m_chkSumTranslate = (CheckBox)getView().findViewById(R.id.chkSumTranslate);
        m_txtStation =  (EditText)getView().findViewById(R.id.txtStation);
        m_txtScreen =  (EditText)getView().findViewById(R.id.txtScreen);
        m_txtPrepTime =  (EditText)getView().findViewById(R.id.txtPrepTime);
        m_txtSeconds =  (EditText)getView().findViewById(R.id.txtSecs);

        m_chkPrintable =  (CheckBox)getView().findViewById(R.id.chkPrintable);

        //m_chkModifierEnabled = (CheckBox)getView().findViewById(R.id.chkModifier);

        m_btnBG = (Button)getView().findViewById(R.id.btnBG);
        m_btnBG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBGButtonClicked(v);
            }
        });
        m_btnFG = (Button)getView().findViewById(R.id.btnFG);
        m_btnFG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFGButtonClicked(v);
            }
        });

        //show data
        m_txtDescription.setText(item.getDescription());
        if (item.isAssignedColor())
        {
            m_txtDescription.setBackgroundColor(item.getBG());
            m_txtDescription.setTextColor(item.getFG());
            m_nBG = item.getBG();
            m_nFG = item.getFG();

        }
        m_txtStation.setText(item.getToStation());
        m_txtScreen.setText(item.getToScreen());


        m_txtPrepTime.setText(KDSUtil.convertIntToString(item.getPreparationTimeMins()));
        m_txtSeconds.setText(KDSUtil.convertIntToString(item.getPreparationTimeSecs()));

        m_chkPrintable.setChecked(item.getPrintable());

        m_chkSumTranslate.setChecked(item.getSumTranslateEnabled());


        m_lstBuildCards = (ListView) getView().findViewById(R.id.lstBuildCards);
        m_lstBuildCards.setAdapter(new MyAdapter(context,android.R.layout.simple_list_item_single_choice, m_arBuildCards));
        setListViewData(m_lstBuildCards, m_arBuildCards, m_item.getBuildCard().getArray());
        showScrollBar(m_lstBuildCards);
        m_lstTrainingVideo = (ListView) getView().findViewById(R.id.lstTraining);
        m_lstTrainingVideo.setAdapter(new MyAdapter(context,android.R.layout.simple_list_item_single_choice, m_arTrainingVideo));
        setListViewData(m_lstTrainingVideo, m_arTrainingVideo, m_item.getTrainingVideo().getArray());
        showScrollBar(m_lstTrainingVideo);

        m_chkSumTranslate = (CheckBox)getView().findViewById(R.id.chkSumTranslate);


        m_lstSumNames= (ListView) getView().findViewById(R.id.lstSumNames);
        m_lstSumNames.setAdapter(new ArrayAdapter(context,android.R.layout.simple_list_item_single_choice, m_arSumNames));
        setListViewData(m_lstSumNames, m_arSumNames, m_item.getSumNames());
        showScrollBar(m_lstSumNames);

        m_lstModifiers= (ListView) getView().findViewById(R.id.lstModifiers);
        m_lstModifiers.setAdapter(new ArrayAdapter(context,android.R.layout.simple_list_item_single_choice, m_arModifiers));
        setModifiersListViewData(m_lstModifiers, m_arModifiers, m_item.getModifiers());
        showScrollBar(m_lstModifiers);


        //m_chkModifierEnabled.setEnabled(item.getModifierEnabled());

        Button btn = (Button) getView().findViewById(R.id.btnBCRemove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardsRemoveClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnBCLocal);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardsLocalClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnBCEthernet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardsEthernetClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnBCInternet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardsInternetClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnBCUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardUpClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnBCDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnBuildCardDownClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTVRemove);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingRemoveClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTCLocal);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingLocalClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTVEthernet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingEthernetClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTVInternet);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingInternetClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTVUp);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingUpClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnTVDown);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnTrainingDownClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnSumNameNew);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSumNameNewClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnSumNameEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSumNameEditClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnSumNameDelete);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnSumNameDeleteClicked();
            }
        });

        //for modifier

        btn = (Button) getView().findViewById(R.id.btnModifierNew);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnModifierNewClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnModifierEdit);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnModifierEditClicked();
            }
        });

        btn = (Button) getView().findViewById(R.id.btnModifierDel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtnModifierDeleteClicked();
            }
        });

        //m_btnBG.requestFocus();
        showSumTransGui(m_item.getSumTranslateEnabled());
        m_chkSumTranslate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showSumTransGui(isChecked);
            }
        });

        m_chkSumTranslate.setChecked(m_item.getSumTranslateEnabled());
    }


    private void showSumTransGui(boolean bEnabled)
    {
        View[] ar = new View[]{
                m_lstSumNames,  getView().findViewById(R.id.btnSumNameNew),
                getView().findViewById(R.id.btnSumNameEdit),
                getView().findViewById(R.id.btnSumNameDelete)
        };
        for (int i=0; i< ar.length; i++)
        {
            ar[i].setEnabled(bEnabled);
        }

    }

    public void onBtnSumNameNewClicked()
    {
        KDSUIDlgSumName dlg = new KDSUIDlgSumName(this.getView().getContext(), this, null);

        dlg.show();
    }

    private boolean isValidListIndex(ListView lv, int nIndex)
    {
        if (nIndex >= lv.getAdapter().getCount() || nIndex<0) return false;
        return true;
    }
    public void onBtnSumNameEditClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstSumNames);

        if (!isValidListIndex(m_lstSumNames, n)) return;
        KDSDataSumName sumName = m_arSumNames.get(n);

        KDSUIDlgSumName dlg = new KDSUIDlgSumName(this.getView().getContext(), this, sumName);
        dlg.show();

    }
    public void onBtnSumNameDeleteClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstSumNames);

        if (!isValidListIndex(m_lstSumNames, n)) return;
        m_arSumNames.remove(n);
        notifyListViewChanged(m_lstSumNames);
    }

    private void listViewItemUp(ListView lv, List<String> arData)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        String s = arData.get(n);
        arData.remove(n);
        arData.add(n-1, s);
        notifyListViewChanged(lv);
    }
    private void listViewItemDown(ListView lv, List<String> arData)
    {
        int n = getListViewSelectedItemIndex(lv);
        if (!isValidListIndex(lv, n)) return;
        String s = arData.get(n);
        arData.remove(n);
        arData.add(n+1, s);
        notifyListViewChanged(lv);
    }

    private void onBtnBuildCardUpClicked()
    {
        listViewItemUp(m_lstBuildCards, m_arBuildCards);

    }

    private void onBtnBuildCardDownClicked()
    {
        listViewItemDown(m_lstBuildCards, m_arBuildCards);

    }

    private void onBtnTrainingDownClicked()
    {
        listViewItemDown(m_lstTrainingVideo, m_arTrainingVideo);


    }

    private void onBtnTrainingUpClicked()
    {
        listViewItemUp(m_lstTrainingVideo, m_arTrainingVideo);

    }

    private void setListViewData(ListView lv, List<String> arAdatper, ArrayList<String> arData)
    {
        arAdatper.clear();
        for (int i=0; i< arData.size(); i++)
            arAdatper.add(arData.get(i));
        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
    }

    private void setListViewData(ListView lv, List<KDSDataSumName> arAdatper, KDSDataSumNames arData)
    {
        arAdatper.clear();
        for (int i=0; i< arData.getCount(); i++)
            arAdatper.add(arData.getSumName(i));
        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
    }

    private void setModifiersListViewData(ListView lv, List<KDSRouterDataItemModifier> arAdatper, KDSRouterDataItemModifiers arData)
    {
        arAdatper.clear();
        for (int i=0; i< arData.getCount(); i++)
            arAdatper.add(arData.getModifier(i));
        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
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

    private void onBtnBuildCardsRemoveClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstBuildCards);
        if (!isValidListIndex(m_lstBuildCards, n)) return;
        m_arBuildCards.remove(n);
        notifyListViewChanged(m_lstBuildCards);
    }

    private String getValidImageExtensions()
    {
        String s = ".jpg;.gif;.png;.bmp;.jpeg;";
        return s;
    }

    private String getValidVideoExtensions()
    {

        String s = ".mov;.mkv;.mp4;.avi;";
        return s;
    }

    private void onBtnBuildCardsLocalClicked()
    {
        OpenFileDialog dlg = new OpenFileDialog(this.getView().getContext(),getValidImageExtensions(), this, OpenFileDialog.Mode.Choose_File );
        dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }
    private void onBtnBuildCardsEthernetClicked()
    {
        OpenSmbFileDialog dlg = new OpenSmbFileDialog(this.getView().getContext(),getValidImageExtensions(), this);
        dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }

    private void onBtnBuildCardsInternetClicked()
    {
        KDSUIDlgInternetFile dlg = new KDSUIDlgInternetFile(this.getView().getContext(), getValidImageExtensions(), this);
        dlg.setTag(TAG_FOR_BUILDCARD);
        dlg.show();
    }
    private void onBtnTrainingRemoveClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstTrainingVideo);
        if (n <0) return;
        m_arTrainingVideo.remove(n);
        notifyListViewChanged(m_lstTrainingVideo);
    }

    private void onBtnTrainingLocalClicked()
    {
        OpenFileDialog dlg = new OpenFileDialog(this.getView().getContext(),getValidVideoExtensions(), this,OpenFileDialog.Mode.Choose_File );
        dlg.setTag(TAG_FOR_TRAINING_VIDEO);
        dlg.show();
    }
    private void onBtnTrainingEthernetClicked()
    {
        OpenSmbFileDialog dlg = new OpenSmbFileDialog(this.getView().getContext(),getValidVideoExtensions(), this);
        dlg.setTag(TAG_FOR_TRAINING_VIDEO);
        dlg.show();
    }

    private void onBtnTrainingInternetClicked()
    {
        KDSUIDlgInternetFile dlg = new KDSUIDlgInternetFile(this.getView().getContext(),getValidVideoExtensions(), this);
        dlg.setTag(TAG_FOR_TRAINING_VIDEO);
        dlg.show();
    }


    static private final int TAG_BG = 0;
    static private final int TAG_FG = 1;
    /**
     *
     * @param v
     */
    private void onBGButtonClicked(View v)
    {
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getView().getContext(), m_nBG, this);
        dlg.setTag(TAG_BG);
        dlg.show();
    }

    private void onFGButtonClicked(View v)
    {
        KDSUIColorPickerDialog dlg = new KDSUIColorPickerDialog(this.getDialog().getContext(), m_nFG, this);
        dlg.setTag(TAG_FG);
        dlg.show();
    }

    public void onCancel(KDSUIColorPickerDialog dialog)
    {

    }

    public void onOk(KDSUIColorPickerDialog dialog, int color)
    {
        int ntag = (int) dialog.getTag();
        if (ntag == TAG_BG) {
            if (m_nBG == color) return;
            if (m_nBG == m_nFG)
                m_nFG = Color.BLACK;
            m_nBG = color;
        }
        else {
            if (m_nFG == color) return;
            if (m_nBG == m_nFG)
                m_nBG = Color.WHITE;
            m_nFG = color;
        }
        m_txtDescription.setBackgroundColor(m_nBG);
        m_txtDescription.setTextColor(m_nFG);
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void notifyListViewChanged(ListView lv)
    {
        ((ArrayAdapter)lv.getAdapter()).notifyDataSetChanged();
    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof OpenFileDialog)
        {
            int tag = (int)dialog.getTag();
            if (tag == TAG_FOR_BUILDCARD)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arBuildCards.add(s);
                notifyListViewChanged(m_lstBuildCards);
            }
            else if (tag == TAG_FOR_TRAINING_VIDEO)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arTrainingVideo.add(s);
                notifyListViewChanged(m_lstTrainingVideo);
            }
        }
        else if (dialog instanceof OpenSmbFileDialog)
        {
            int tag = (int)dialog.getTag();
            if (tag == TAG_FOR_BUILDCARD)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arBuildCards.add(s);
                notifyListViewChanged(m_lstBuildCards);
            }
            else if (tag == TAG_FOR_TRAINING_VIDEO)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arTrainingVideo.add(s);
                notifyListViewChanged(m_lstTrainingVideo);
            }
        }
        else if (dialog instanceof KDSUIDlgInternetFile)
        {
            int tag = (int)dialog.getTag();
            if (tag == TAG_FOR_BUILDCARD)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arBuildCards.add(s);
                notifyListViewChanged(m_lstBuildCards);
            }
            else if (tag == TAG_FOR_TRAINING_VIDEO)
            {
                String s = (String)dialog.getResult();
                if (s.isEmpty()) return;
                m_arTrainingVideo.add(s);
                notifyListViewChanged(m_lstTrainingVideo);
            }
        }
        else if (dialog instanceof KDSUIDlgSumName)
        {
            if (((KDSUIDlgSumName)dialog).isAddNew())
            {
                KDSDataSumName sumName = (KDSDataSumName) dialog.getResult();
                m_arSumNames.add(sumName);
            }
            notifyListViewChanged(m_lstSumNames);

        }
        else if (dialog instanceof KDSUIDlgModifier)
        {
            if (((KDSUIDlgModifier)dialog).isAddNew())
            {
                KDSRouterDataItemModifier modifier = (KDSRouterDataItemModifier) dialog.getResult();
                m_arModifiers.add(modifier);
            }
            notifyListViewChanged(m_lstModifiers);

        }
    }

    public void onBtnModifierNewClicked()
    {
        KDSUIDlgModifier dlg = new KDSUIDlgModifier(this.getView().getContext(), this, null);

        dlg.show();
    }

    public void onBtnModifierEditClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstModifiers);

        if (!isValidListIndex(m_lstModifiers, n)) return;
        KDSRouterDataItemModifier modifier = (KDSRouterDataItemModifier) m_arModifiers.get(n);

        KDSUIDlgModifier dlg = new KDSUIDlgModifier(this.getView().getContext(), this, modifier);
        dlg.show();

    }
    public void onBtnModifierDeleteClicked()
    {
        int n = getListViewSelectedItemIndex(m_lstModifiers);

        if (!isValidListIndex(m_lstModifiers, n)) return;
        m_arModifiers.remove(n);
        notifyListViewChanged(m_lstModifiers);
    }

    private class MyAdapter extends ArrayAdapter {
        public MyAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, 0, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            View v = super.getView(position, convertView, parent);
            TextView text = (TextView) v;
            String s = (String) this.getItem(position);
            //for smb file, hide its password.
            if (KDSSMBPath.isSmbFile(s))
            {
                KDSSMBPath p = KDSSMBPath.parseString(s);
                s =  p.toDisplayString();
            }

            text.setText(s);


            return v;
        }

    }

    private void showScrollBar(ListView lv)
    {

        lv.setScrollBarFadeDuration(0);
        lv.setScrollbarFadingEnabled(false);

    }


}
