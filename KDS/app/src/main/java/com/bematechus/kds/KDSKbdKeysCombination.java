package com.bematechus.kds;

import android.view.KeyEvent;

/**
 * Created by Administrator on 2016/3/11 0011.
 * >>>>>>>>>>>>>>>>> Call its function in activity dispatchKeyEvent event<<<<<<<<<<
 */
public class KDSKbdKeysCombination {

    boolean m_keyFirstIsDown = false;
    boolean m_keySecondIsDown = false;
    int m_firstKeyCode = 0;
    int m_secondKeyCode = 0;

    public KDSKbdKeysCombination()
    {

    }
    public KDSKbdKeysCombination(int nFirstKeyCode, int nSecondKeyCode)
    {
        this.reset();
        m_firstKeyCode = nFirstKeyCode;
        m_secondKeyCode = nSecondKeyCode;
    }

    public void setFirstKeyCode(int nKeyCode)
    {
        m_firstKeyCode = nKeyCode;
    }
    public void setSecondKeyCode(int nKeyCode)
    {
        m_secondKeyCode = nKeyCode;
    }
    public void reset()
    {
        m_keyFirstIsDown = false;
        m_keySecondIsDown = false;
        m_firstKeyCode = 0;
        m_secondKeyCode = 0;
    }
    /**
     *
     * @param event
     * @return
     *    True: the keys was pressed.
     *    false: it is not pressed.
     */
    public boolean dispatchKeyEventCheckPressed(KeyEvent event) {

        // 判断普通按键
        int keyCode = event.getKeyCode();
        if (keyCode == m_firstKeyCode
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (m_secondKeyCode == 0) {
                m_keyFirstIsDown = false;
                m_keySecondIsDown = false;
                return true;
            }
            else {
                m_keyFirstIsDown = true;
            }
        } else if (keyCode == m_secondKeyCode
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (m_firstKeyCode == 0) {
                m_keyFirstIsDown = false;
                m_keySecondIsDown = false;
                return true;
            }
            else {
                m_keySecondIsDown = true;
            }
        }else if((keyCode ==m_secondKeyCode && !m_keyFirstIsDown)||(keyCode == m_firstKeyCode && !m_keySecondIsDown)){
            m_keyFirstIsDown = false;
            m_keySecondIsDown = false;
        }else if (m_keyFirstIsDown
                && m_keySecondIsDown
                && (keyCode == m_secondKeyCode || keyCode == m_firstKeyCode)
                && event.getAction() == KeyEvent.ACTION_UP) {
            m_keyFirstIsDown = false;
            m_keySecondIsDown = false;
            return true;
            // Toast.makeText(Main.this, "Q + A", 0).show();

        }else{
            m_keyFirstIsDown = false;
            m_keySecondIsDown = false;
        }
        return false;

    }
}
