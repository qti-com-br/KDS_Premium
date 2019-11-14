package com.bematechus.kdsrouter;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bematechus.kdslib.KDSUIDialogBase;

import java.util.HashMap;
import java.util.Map;
/**
 *  >>>>>>> SAME AS KDS APP FILE <<<<<<<
 */
/**
 *
 */
public class KDSUIDialogConfirm extends KDSUIDialogBase {
    TextView m_txtInfo = null;
    Object m_tag = null;
    HashMap<String, Object> m_tags = new HashMap<>();

    public void setTag(Object obj)
    {
        m_tag = obj;
    }
    public Object getTag()
    {
        return m_tag;
    }

    public void setTag(String key,  Object obj)
    {
        m_tags.put(key, obj);
    }
    public Object getTage(String key)
    {
        return m_tags.get(key);
    }
    /**
     * it will been overrided by child
     * @return
     */
    public Object getResult()
    {
        return getTag();
    }
    public KDSUIDialogConfirm(final Context context, String strMessage, KDSDialogBaseListener listener) {
        this.int_dialog(context, listener, R.layout.kdsui_confirm, "");
        this.setTitle(context.getString(R.string.confirm));
        m_txtInfo = (TextView)this.getView().findViewById(R.id.txtInfo);
        m_txtInfo.setText(strMessage);

    }
}
