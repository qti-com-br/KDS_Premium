package com.bematechus.kdsstatistic;
import com.bematechus.kdslib.ConditionBase;
/**
 *
 */
public class STActivityItemReport extends STActivityOrderReport {

    protected  void setReportMode()
    {
        m_fragmentReport.setReportMode(ConditionBase.ReportMode.Item);
        m_fragmentGeneral.setReportMode(ConditionBase.ReportMode.Item);
    }
    public void updateTitle()
    {
        m_txtTitle.setText(this.getString(R.string.item_report));
    }
}
