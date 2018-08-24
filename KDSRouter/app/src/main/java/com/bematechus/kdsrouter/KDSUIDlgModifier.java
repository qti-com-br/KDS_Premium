package com.bematechus.kdsrouter;

import android.content.Context;
import android.widget.EditText;

import com.bematechus.kdslib.KDSUtil;

/**
 * Created by Administrator on 2018/1/29.
 */
public class KDSUIDlgModifier extends KDSUIDialogBase  {

    EditText m_txtDescription = null;
    EditText m_txtMinutes = null;
    EditText m_txtSeconds = null;

    KDSRouterDataItemModifier m_modifier = null;

    boolean m_bAddNew = false;
    public boolean isAddNew()
    {
        return m_bAddNew;
    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return m_modifier;
    }
    public void onOkClicked()
    {
        save(m_modifier);

    }



    public KDSUIDlgModifier(final Context context, KDSUIDialogBase.KDSDialogBaseListener listener, KDSRouterDataItemModifier modifier) {
        this.int_dialog(context, listener, R.layout.kdsui_dlg_modifier, "");
        this.setTitle("New modifier");

        m_txtDescription = (EditText)this.getView().findViewById(R.id.txtDescription);
        m_txtMinutes = (EditText) this.getView().findViewById(R.id.txtMins);

        m_txtSeconds = (EditText) this.getView().findViewById(R.id.txtSecs);

        m_modifier = modifier;
        if (m_modifier == null) {
            this.setTitle(context.getString(R.string.new_item));
            m_modifier = new KDSRouterDataItemModifier();

            m_bAddNew = true;
        }
        else
            this.setTitle(m_modifier.getDescription());

        show(m_modifier);



    }

    private void save(KDSRouterDataItemModifier modifier)
    {
        modifier.setDescription(m_txtDescription.getText().toString());
        String mins = m_txtMinutes.getText().toString();
        String secs = m_txtSeconds.getText().toString();
        int nSeconds = KDSUtil.convertStringToInt(mins, 0) * 60+ KDSUtil.convertStringToInt(secs, 0);

        modifier.setPrepTime(nSeconds);

    }
    private void show(KDSRouterDataItemModifier modifier)
    {
        m_txtDescription.setText(modifier.getDescription());
        m_txtMinutes.setText( KDSUtil.convertIntToString( modifier.getPrepTimeMins()));
        m_txtSeconds.setText( KDSUtil.convertIntToString( modifier.getPrepTimeSeconds()));

    }



}
