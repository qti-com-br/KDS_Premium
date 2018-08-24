package com.bematechus.kdsstatistic;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSXML;

import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by David.Wong on 2018/5/16.
 * Rev:
 */
public class SOSGraphView extends LineChartView {
    public SOSGraphViewProperties m_properties = new SOSGraphViewProperties();

    public SOSGraphView(Context context) {
        super(context, (AttributeSet)null, 0);
    }

    public SOSGraphView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public SOSGraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void showDemo()
    {
        ArrayList<AxisValue> labels = new ArrayList<>();
        ArrayList<PointValue> points = new ArrayList<>();


        labels.add((new AxisValue(0)).setLabel( "12:00"));
        labels.add((new AxisValue(1)).setLabel( "12:10"));
        labels.add((new AxisValue(2)).setLabel( "12:20"));
        labels.add((new AxisValue(3)).setLabel( "12:30"));
        labels.add((new AxisValue(4)).setLabel( "12:40"));
        labels.add((new AxisValue(5)).setLabel( "12:50"));
        labels.add((new AxisValue(6)).setLabel( "13:00"));

        points.add(new PointValue(0, 10));
        points.add(new PointValue(1, 5));
        points.add(new PointValue(2, 15));
        points.add(new PointValue(3, 8));
        points.add(new PointValue(4, 3));
        points.add(new PointValue(5,0));
        //points.add(null);
        points.add(new PointValue(6, 0));

       // reverseAxisX(labels);
       // reversePoints(points);
        showLineChart(this, labels, points);



    }

    static public void reverseAxisX( List<AxisValue> xLabels)
    {
        ArrayList<AxisValue> ar = new ArrayList<>();
        for (int i=xLabels.size()-1; i>=0; i--) {
            ar.add(xLabels.get(i));
            xLabels.get(i).setValue(xLabels.size()-1-i);
        }
        xLabels.clear();
        xLabels.addAll(ar);
    }

    static public void reversePoints( List<PointValue> ptValues)
    {
        ArrayList<PointValue> ar = new ArrayList<>();
        for (int i=ptValues.size()-1; i>=0; i--) {
            ar.add(new PointValue(ptValues.size()-1-i, ptValues.get(i).getY()));

        }
        ptValues.clear();
        ptValues.addAll(ar);
    }

    static private void addTargetLine(SOSGraphView lineChart,List<Line> lines, List<PointValue> ptValues,float fltTarget )
    {
        //float fltTarget = lineChart.m_properties.getTargetMinutes();
        ArrayList<PointValue> arTarget = new ArrayList<>();
        if (ptValues.size() >0) {
            arTarget.add(new PointValue(ptValues.get(0).getX(), fltTarget));
            arTarget.add(new PointValue(ptValues.get(ptValues.size() - 1).getX(), fltTarget));
        }
        else
        {
            arTarget.add(new PointValue(0, fltTarget));
            arTarget.add(new PointValue(1, fltTarget));
        }
        Line lineTarget = new Line(arTarget).setColor(Color.RED);// Color.parseColor("#FFCD41"));  //折线的颜色（橙色）

        lineTarget.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        lineTarget.setCubic(false);//曲线是否平滑，即是曲线还是折线
        lineTarget.setFilled(false);//是否填充曲线的面积
        lineTarget.setHasLabels(false);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        lineTarget.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        lineTarget.setHasPoints(false);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        lineTarget.setStrokeWidth(1);

        lines.add(lineTarget);
    }
    static private void addDotsLine(SOSGraphView lineChart,List<Line> lines, List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {

        List<PointValue> values = new ArrayList<>();
        for (int i=0; i< ptValues.size(); i++)
        {
            values.add(new PointValue(i, 0));
        }

        Line line = new Line(values).setColor( lineChart.m_properties.m_defaultColor.getFG());// Color.parseColor("#FFCD41"));  //折线的颜色（橙色）

        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(false);//曲线的数据坐标是否加上备注
        line.setHasLines(false);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        line.setPointRadius(1);
        line.setStrokeWidth(1);

        lines.add(line);
    }

    static private void addDataLine(SOSGraphView lineChart,List<Line> lines, List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {
        Line line = new Line(ptValues).setColor( lineChart.m_properties.m_defaultColor.getFG());// Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
        line.setHasLabels(false);//曲线的数据坐标是否加上备注
//      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        boolean bHideDot = SOSKDSGlobalVariables.getKDSSOS().getSettings().getBoolean(SOSSettings.ID.Hide_data_dot);

        line.setHasPoints( (!bHideDot) );//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
        line.setPointRadius(2);
        line.setStrokeWidth(1);
        line.setFilled(true);
        line.setAreaTransparency(40);

        lines.add(line);
    }

    static private void addAxisX(SOSGraphView lineChart,LineChartData data , List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {
        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(lineChart.m_properties.m_defaultColor.getFG());//Color.WHITE);  //设置字体颜色
        String s = lineChart.m_properties.m_strTitleX;
        if (s.isEmpty())
            s = SOSKDSGlobalVariables.getKDSSOS().getSettings().getString(SOSSettings.ID.Graph_x_title);

        axisX.setName(s);// "Time");  //表格名称
        axisX.setTextSize(12);//设置字体大小
        axisX.setMaxLabelChars(1); //设置轴标签可显示的最大字符个数，范围在0-32之间7<=x<=mAxisXValues.length, 32 is the max
        axisX.setValues(axisXLabels);  //填充X轴的坐标名称
        axisX.setHasLines(false); //x 轴分割线
        axisX.setInside(false);
        axisX.setAutoGenerated(false);

        data.setAxisXBottom(axisX); //x 轴在底部
    }

    static private void addAxisY(SOSGraphView lineChart,LineChartData data , List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {
        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        String s = lineChart.m_properties.m_strTitleY;
        if (s.isEmpty())
            s = SOSKDSGlobalVariables.getKDSSOS().getSettings().getString(SOSSettings.ID.Graph_y_title);
        axisY.setName( s);//"Mins");//y轴标注
        axisY.setTextSize(12);//设置字体大小
        axisY.setHasLines(false);
        axisY.setLineColor(lineChart.m_properties.m_yColor.getFG());
        axisY.setTextColor(lineChart.m_properties.m_yColor.getFG());

        data.setAxisYLeft(axisY);  //y轴设置在右边
    }

    static private void addAxisTitle(SOSGraphView lineChart,LineChartData data , List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {
        //for title
        Axis axisTitle = new Axis(); //X轴
        axisTitle.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisTitle.setTextColor(lineChart.m_properties.m_defaultColor.getFG());//Color.WHITE);  //设置字体颜色
        axisTitle.setHasLines(false);
        axisTitle.setHasSeparationLine(false);
        String s = lineChart.m_properties.m_strTitle;

        if (s.isEmpty()) {
            if (lineChart.m_properties.m_stationID.equals(SOSSettings.OVERALL_STATION_ID))
                s = KDSApplication.getContext().getString(R.string.overall);// "Overall";
            else
                s =KDSApplication.getContext().getString(R.string.station_number) + lineChart.m_properties.m_stationID;
        }

        axisTitle.setName( s );//"Station #1");  //表格名称
        axisTitle.setTextSize(12);//设置字体大小
        axisTitle.setMaxLabelChars(0); //设置轴标签可显示的最大字符个数，范围在0-32之间7<=x<=mAxisXValues.length, 32 is the max
        ArrayList arTitles = new ArrayList();
        arTitles.add((new AxisValue(0).setLabel("")));
        axisTitle.setValues(arTitles);  //填充X轴的坐标名称
        data.setAxisXTop(axisTitle); //x 轴在底部
    }

    static private void adjustForZeroShowing(List<PointValue> ptValues)
    {
        int n = SOSKDSGlobalVariables.getKDSSOS().getSettings().getInt(SOSSettings.ID.Zero_value_show);
        SOSSettings.ZeroValueShow zeroShowing = SOSSettings.ZeroValueShow.values()[n];
        if (zeroShowing == SOSSettings.ZeroValueShow.Zero) {
            return;
        }

        float lastNoZero = 0;
        for (int i=0; i< ptValues.size(); i++)
        {

            if (ptValues.get(i).getY() >0)
                lastNoZero =ptValues.get(i).getY();
            else
            {
                float x = ptValues.get(i).getX();
                ptValues.get(i).set(x, lastNoZero);
            }
        }

    }
    static public void showLineChart(SOSGraphView lineChart,List<AxisValue> axisXLabels, List<PointValue> ptValues)
    {
        lineChart.setBackgroundColor(lineChart.m_properties.m_defaultColor.getBG());

        adjustForZeroShowing(ptValues);

        List<Line> lines = new ArrayList<>();

        addDataLine(lineChart, lines, axisXLabels, ptValues);

//        Line line = new Line(ptValues).setColor( lineChart.m_properties.m_defaultColor.getFG());// Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
//
//        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
//        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
//        line.setFilled(false);//是否填充曲线的面积
//        line.setHasLabels(false);//曲线的数据坐标是否加上备注
////      line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
//        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
//        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）
//        line.setPointRadius(2);
//        line.setStrokeWidth(1);
//        line.setFilled(true);
//        line.setAreaTransparency(40);
//
//        lines.add(line);

        float fltTarget = lineChart.m_properties.getTargetMinutes();
        //
        //add target line
        addTargetLine(lineChart, lines, ptValues, fltTarget);

        addDotsLine(lineChart, lines, axisXLabels, ptValues);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        addAxisX(lineChart, data, axisXLabels, ptValues);
//
//        //坐标轴
//        Axis axisX = new Axis(); //X轴
//        axisX.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
//        axisX.setTextColor(lineChart.m_properties.m_defaultColor.getFG());//Color.WHITE);  //设置字体颜色
//        String s = lineChart.m_properties.m_strTitleX;
//        if (s.isEmpty())
//            s = KDSGlobalVariables.getKDSSOS().getSettings().getString(KDSSOSSettings.ID.Graph_x_title);
//
//        axisX.setName(s);// "Time");  //表格名称
//        axisX.setTextSize(12);//设置字体大小
//        axisX.setMaxLabelChars(1); //设置轴标签可显示的最大字符个数，范围在0-32之间7<=x<=mAxisXValues.length, 32 is the max
//        axisX.setValues(axisXLabels);  //填充X轴的坐标名称
//        axisX.setHasLines(false); //x 轴分割线
//        axisX.setInside(false);
//        axisX.setAutoGenerated(false);
//
//        data.setAxisXBottom(axisX); //x 轴在底部


//        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
//        Axis axisY = new Axis();  //Y轴
//        s = lineChart.m_properties.m_strTitleY;
//        if (s.isEmpty())
//            s = KDSGlobalVariables.getKDSSOS().getSettings().getString(KDSSOSSettings.ID.Graph_y_title);
//        axisY.setName( s);//"Mins");//y轴标注
//        axisY.setTextSize(12);//设置字体大小
//        axisY.setHasLines(false);
//        axisY.setLineColor(lineChart.m_properties.m_yColor.getFG());
//        data.setAxisYLeft(axisY);  //y轴设置在右边
//
        addAxisY(lineChart, data, axisXLabels, ptValues);
        //for title
//        Axis axisTitle = new Axis(); //X轴
//        axisTitle.setHasTiltedLabels(false);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
//        axisTitle.setTextColor(lineChart.m_properties.m_defaultColor.getFG());//Color.WHITE);  //设置字体颜色
//        axisTitle.setHasLines(false);
//        axisTitle.setHasSeparationLine(false);
//        s = lineChart.m_properties.m_strTitle;
//
//        if (s.isEmpty()) {
//            if (lineChart.m_properties.m_stationID.equals(KDSSOSSettings.OVERALL_STATION_ID))
//                s = KDSApplication.getContext().getString(R.string.overall);// "Overall";
//            else
//                s =KDSApplication.getContext().getString(R.string.station_number) + lineChart.m_properties.m_stationID;
//        }
//
//        axisTitle.setName( s );//"Station #1");  //表格名称
//        axisTitle.setTextSize(12);//设置字体大小
//        axisTitle.setMaxLabelChars(0); //设置轴标签可显示的最大字符个数，范围在0-32之间7<=x<=mAxisXValues.length, 32 is the max
//        ArrayList arTitles = new ArrayList();
//        arTitles.add((new AxisValue(0).setLabel("")));
//        axisTitle.setValues(arTitles);  //填充X轴的坐标名称
//        data.setAxisXTop(axisTitle); //x 轴在底部
//
        addAxisTitle(lineChart, data, axisXLabels, ptValues);

        //设置行为属性，支持缩放、滑动以及平移
        //lineChart.setInteractive(true);
        // lineChart.setScrollEnabled(false);
        lineChart.setZoomType(ZoomType.VERTICAL);//.HORIZONTAL);
        //lineChart.setMaxZoom((float) 2);//最大方法比例
        //lineChart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
        lineChart.setLineChartData(data);

        lineChart.setVisibility(View.VISIBLE);

        /**注：下面的7，10只是代表一个数字去类比而已
         * 当时是为了解决X轴固定数据个数。见（http://forum.xda-developers.com/tools/programming/library-hellocharts-charting-library-t2904456/page2）;
         */
//        Viewport v = new Viewport(lineChart.getMaximumViewport());
//        v.left = 0;
//        v.right= 7;
        //Viewport max = lineChart.getMaximumViewport();
        Viewport v = null;
        if (ptValues.size()>0 ) {
            if (!isAllValuesZero(ptValues))
                v = new Viewport(0, getMaxValue(ptValues, fltTarget) * 21 / 20, ptValues.size() - 1, 0);

            else
                v = new Viewport(0,  fltTarget * 2, ptValues.size() - 1, 0);
        }
        else
            v = new Viewport(0,fltTarget*2, 1, 0);




        lineChart.setMaximumViewport(v);
        lineChart.setCurrentViewport(v);
        lineChart.setZoomLevel(0,0,1f);

        //lineChart.setFitsSystemWindows(true);

    }

    static private boolean isAllValuesZero( List<PointValue> ptValues)
    {
        for (int i=0; i<ptValues.size(); i++)
        {
            if (ptValues.get(i).getY() != 0)
                    return false;
        }
        return true;
    }

    static public float getMaxValue(List<PointValue> ptValues, float fltTarget)
    {
        float flt = 0;
        for (int i=0; i< ptValues.size(); i++)
        {
            if (flt <ptValues.get(i).getY())
                flt = ptValues.get(i).getY();
        }
        if ( flt < fltTarget )
            flt = fltTarget;
        return flt;
    }

    public void outputToXml(KDSXML xml)
    {

        xml.setAttribute("Type", "1");
        m_properties.outputToXml(xml);
    }

    public void parseXml(KDSXML xml)
    {
        m_properties.parseXml(xml);
    }

    public boolean isForStation(String stationID)
    {
        return m_properties.m_stationID.equals(stationID);
    }

    public void init_for_running()
    {
        ArrayList<AxisValue> labels = new ArrayList<>();
        ArrayList<PointValue> points = new ArrayList<>();

        showLineChart(this, labels, points);
    }
}
