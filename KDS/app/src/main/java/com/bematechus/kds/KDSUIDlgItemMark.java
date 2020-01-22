package com.bematechus.kds;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSBGFG;
import com.bematechus.kdslib.KDSUIBGFGPickerDialog;
import com.bematechus.kdslib.KDSUIDialogBase;
import com.bematechus.kdslib.MySpinnerArrayAdapter;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/11/27.
 */
public class KDSUIDlgItemMark extends KDSUIDialogBase implements KDSUIBGFGPickerDialog.OnBGFGPickerDlgListener {


    ItemMark m_itemMark = null;

    Spinner m_spinnerFormat = null;
    ImageView m_imgIcon = null;
    EditText m_txtMarkString = null;
    TextView m_txtColor = null;

    View m_layoutIcon = null;
    View m_layoutString = null;
    View m_layoutColor = null;


    @Override
    public void onOkClicked()
    {//save data here
        int n = m_spinnerFormat.getSelectedItemPosition();
        ItemMark.MarkFormat format =ItemMark.MarkFormat.values()[n];
        m_itemMark.setFormat(format);
        m_itemMark.clearValues();
        switch (format)
        {

            case Icon:
                m_itemMark.setInternalIcon((ItemMark.MarkType) m_imgIcon.getTag());
                break;
            case Char:
                m_itemMark.setMarkString(m_txtMarkString.getText().toString());
                break;
            case Color:
                m_itemMark.getMarkColor().setFG(m_txtColor.getTextColors().getDefaultColor());
                m_itemMark.getMarkColor().setBG(getTextViewBGColor(m_txtColor, 0));

                break;
        }

    }

    /**
     * it will been overrided by child
     * @return
     */
    @Override
    public Object getResult()
    {
        return m_itemMark;


    }

    public KDSUIDlgItemMark(final Context context, ItemMark itemMark, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_item_mark, "");
        m_spinnerFormat =(Spinner) this.getView().findViewById(R.id.spinnerMarkFormat);
        m_imgIcon = (ImageView)  this.getView().findViewById(R.id.imgIcon);
        m_txtMarkString = (EditText)  this.getView().findViewById(R.id.txtMarkString);
        m_txtColor = (TextView)  this.getView().findViewById(R.id.txtColor);

        m_layoutIcon =   this.getView().findViewById(R.id.layoutIcon);
        m_layoutString =   this.getView().findViewById(R.id.layoutString);
        m_layoutColor =   this.getView().findViewById(R.id.layoutColor);

        MySpinnerArrayAdapter adapter = new MySpinnerArrayAdapter(context, getArray(context));
        m_spinnerFormat.setAdapter(adapter);

        m_spinnerFormat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFormatChanged(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        m_txtColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onColorButtonClicked();
            }
        });

        m_itemMark = new ItemMark(itemMark);
        showMark(m_itemMark);

    }

    public void onColorButtonClicked()
    {
        KDSUIBGFGPickerDialog dlg = new KDSUIBGFGPickerDialog(this.getView().getContext(), m_itemMark.getMarkColor(), this);
        dlg.show();
    }

    public void showMark(ItemMark itemMark)
    {
        updateGui(itemMark.getFormat());
        m_spinnerFormat.setSelection(itemMark.getFormat().ordinal());
        switch (itemMark.getFormat())
        {

            case Icon:
                m_imgIcon.setImageResource(ItemMark.getIconResID(itemMark.getInternalIcon()));
                m_imgIcon.setTag(itemMark.getInternalIcon());
                break;
            case Char:

                m_txtMarkString.setText(itemMark.getMarkString());
                break;
            case Color:
                m_txtColor.setTextColor(itemMark.getMarkColor().getFG());
                m_txtColor.setBackgroundColor(itemMark.getMarkColor().getBG());

                break;
        }
    }

    public int getTextViewBGColor(TextView t, int nDefault)
    {
        return ((ColorDrawable)t.getBackground()).getColor();
    }
    public void onFormatChanged(int nFormat)
    {
        if (nFormat <0) return ;
        ItemMark.MarkFormat f = ItemMark.MarkFormat.values()[nFormat];
        updateGui(f);
        m_itemMark.setFormat(f);
        showMark(m_itemMark);

    }

    public void updateGui(ItemMark.MarkFormat format)
    {
        switch (format)
        {

            case Icon:
                m_layoutIcon.setVisibility(View.VISIBLE);
                m_layoutString.setVisibility(View.GONE);
                m_layoutColor.setVisibility(View.GONE);

                break;
            case Char:
                m_layoutIcon.setVisibility(View.GONE);
                m_layoutString.setVisibility(View.VISIBLE);
                m_layoutColor.setVisibility(View.GONE);
                break;
            case Color:
                m_layoutIcon.setVisibility(View.GONE);
                m_layoutString.setVisibility(View.GONE);
                m_layoutColor.setVisibility(View.VISIBLE);
                break;
        }
    }

    ArrayList<String> getArray(Context context)
    {
        ArrayList<String> ar = new ArrayList<>();
        //No manually.
        ar.add(context.getString(R.string.internal_icon));//context.getString(R.string.sort_time_ascend));
        ar.add(context.getString(R.string.character));//context.getString(R.string.sort_time_descen));
        ar.add(context.getString(R.string.color));//context.getString(R.string.sort_name_ascend));


        return ar;
    }

    public void onCancel(KDSUIBGFGPickerDialog dialog)
    {

    }

    public void onOk(KDSUIBGFGPickerDialog dialog, KDSBGFG ff)
    {
        m_itemMark.setMarkColor(ff);
        showMark(m_itemMark);
    }
}
