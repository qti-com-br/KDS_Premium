package com.bematechus.kdsstatistic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bematechus.kdslib.CanvasDC;
import com.bematechus.kdslib.KDSApplication;
import com.bematechus.kdslib.KDSUtil;
import com.bematechus.kdslib.KDSXML;

import java.util.ArrayList;

/**
 * Created by David.Wong on 2018/5/8.
 * Rev:
 */
public class SOSLinearLayout extends ViewGroup implements KDSUIDialogBase.KDSDialogBaseListener{

    public static final String TAG = "SOSLinearLayout";



    public interface OnSOSLinearLayoutEvents
    {
        void onSOSLinearLayoutChanged();
        void onShowTips(String str);
    }
    public enum LayoutMode
    {
        Design,
        Running,
    }

    OnSOSLinearLayoutEvents m_eventReceiver = null;

    LayoutMode m_mode = LayoutMode.Design;

    GridBlock m_block = new GridBlock();
    static GridBlock m_selectedBlock = null;
    GestureDetector m_gesture = null;//new GestureDetector(this);

    int m_nBorderColor = Color.BLACK;//.WHITE;

    boolean m_bShowBorder = false;//if draw border, in running mode.

    public boolean getShowBorder()
    {
        return m_bShowBorder;
    }

    public void setShowBorder(boolean bShow)
    {
        m_bShowBorder = bShow;

    }

    public void setBorderColor(int nColor)
    {
        m_nBorderColor = nColor;
    }

    public int getBorderColor()
    {
        return m_nBorderColor;
    }

    public void setEventReceiver(OnSOSLinearLayoutEvents r)
    {
        m_eventReceiver = r;
    }
    public SOSLinearLayout(Context context)
    {
        super(context);
        init();
    }
    public SOSLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public SOSLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }
    public void setLayoutMode(LayoutMode mode)
    {
        m_mode = mode;
        refreshGui();
    }
    public void init()
    {
        if (m_mode == LayoutMode.Design) {
            this.setWillNotDraw(false);
            init_gesture();
        }
        this.setClickable(true);
    }
    private void init_gesture()
    {
        m_gesture = new GestureDetector(this.getContext(), new MyGestureListener());


    }

    public boolean dispatchTouchEvent(MotionEvent event)
    {
//        //m_gesture.onTouchEvent(event);
        if (m_mode == LayoutMode.Design) {
            if (event.getAction() == MotionEvent.ACTION_MOVE ||
                    event.getAction() == MotionEvent.ACTION_HOVER_MOVE)
            {
                onMouseMove(event);
            }
            else if (event.getAction() == MotionEvent.ACTION_UP )
            {
                onMouseUp(event);
            }

            if (m_gesture.onTouchEvent(event)) {
                event.setAction(MotionEvent.ACTION_CANCEL);
            }
            return true;
            //https://stackoverflow.com/questions/23725102/onintercepttouchevent-never-receives-action-move
        }
        else
            return super.dispatchTouchEvent(event);
    }
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        if (ev.getAction() == MotionEvent.ACTION_MOVE)
//        {
//            onMouseMove(ev);
//        }
//        return false;
//    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {


        @Override
        public boolean onDown(MotionEvent event) {
            // Log.d(DEBUG_TAG,"onDown: " + event.toString());
            SOSLinearLayout.this.onTouchDown(event);
            return false;
        }
        public void onLongPress(MotionEvent e) {
            SOSLinearLayout.this.onLongTouchDown(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            SOSLinearLayout.this.onLongTouchDown(e);

            return true;
        }
//        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
//        {
//            return SOSLinearLayout.this.onMove(e1,e2,distanceX,distanceY);
//        }
//        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//                               float velocityY) {
//            return false;
//        }

    }

    public Rect getRect()
    {
        Rect rt = new Rect();
        this.getDrawingRect(rt);
        return rt;

    }
    public GridBlock findBlock(int x, int y)
    {
        return m_block.findBlock(getRect(),x,y);
    }
    /**
     *
     * @param bRow
     *  Add row or col
     *  true: row;
     *  false: col
     * @param percent
     * @return
     */
    public void addBlock(GridBlock blockParent, boolean bRow,float percent)
    {
        if (blockParent == null)
            return;

        blockParent.addBlock(bRow? GridBlock.BreakXY.Y: GridBlock.BreakXY.X, percent);
        refreshGui();
    }

    public GridBlock insertBlock(GridBlock blockParent,GridBlock blockAfter, boolean bRow,float percent)
    {
        if (blockParent == null)
            return null;
        if (percent == 0f)
            return blockAfter;
        GridBlock block = blockParent.insertBlock( blockAfter,bRow? GridBlock.BreakXY.Y: GridBlock.BreakXY.X, percent);
        refreshGui();
        if (m_eventReceiver != null)
            m_eventReceiver.onSOSLinearLayoutChanged();
        return block;
    }

    public void refreshGui()
    {
        this.invalidate();
        requestLayout();
    }



    @Override
    public void addView(View child) {

        //LayoutParams p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutParams p = new LayoutParams(10, 10);
        child.setLayoutParams(p);
        super.addView(child);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //if (m_mode == LayoutMode.Design)
        {
            Rect rt = new Rect();
            this.getDrawingRect(rt);

            m_block.onDraw(canvas, rt, m_mode,getShowBorder(), m_nBorderColor);
        }
//        canvas.drawRect(rt, new Paint(Color.RED));
//        canvas.drawColor(Color.RED);

    }



//    LongPressRunnable mLongPressed = new LongPressRunnable();
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//
//
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//            {
//
//
//            }
//            break;
//
//            case MotionEvent.ACTION_MOVE:
//            {
//                onMouseMove(event);
//            }
//            break;
//            case MotionEvent.ACTION_CANCEL:
//
//                break;
//            case MotionEvent.ACTION_UP: {
//
//                return true;
//            }
//
//            default:
//
//                break;
//        }
//        return false;
//
//    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = this.getChildCount();

        Rect rt = new Rect(l, t, r, b);
        this.getDrawingRect(rt);

        for (int i = 0; i < childCount; i++) {
            View child = this.getChildAt(i);
            m_block.onLayout(rt, child);
        }


    }

    public GridBlock getSelectedBlock()
    {
        if (m_selectedBlock == null)
            return m_block;
        return m_selectedBlock;
    }

    GridBlock m_selectBorderBlock = null;
    public void onTouchDown(MotionEvent event)
    {
        int x = Math.round(event.getX());
        int y = Math.round(event.getY());
        GridBlock block =  m_block.findBlock(getRect(), x,y);
        if (block != null) {
            m_selectedBlock = block;
            refreshGui();
        }
        m_selectBorderBlock = m_block.checkMouseIsInBorder(getRect(), x, y);
        if (m_selectBorderBlock != null) {
            //don't move first one.
            if (m_selectBorderBlock.m_pointAxisPercent == 0)
                m_selectBorderBlock = null;
        }


    }
    public void onLongTouchDown(MotionEvent event)
    {
        popupMenu(event);
    }

    public void onMouseMove(MotionEvent event)
    {
        Log.i(TAG, "x="+event.getX() +", y="+event.getY());
        if (m_selectBorderBlock != null)
        {
            Log.i(TAG, "Move border");
            changeBreakPoint(event, m_selectBorderBlock);
           // requestLayout();
            refreshGui();

        }
//        GridBlock block = m_block.checkMouseIsInBorder(this.getRect(), Math.round(event.getX()),Math.round( event.getY()));
//        if(block!=null)
//        {
//            changeBreakPoint(event, block);
//            refreshGui();
//            //Log.i(TAG, "Find border");
//        }
    }

    public void onMouseUp(MotionEvent event)
    {
        if (m_selectBorderBlock != null)
        {
            if (m_eventReceiver != null)
                m_eventReceiver.onSOSLinearLayoutChanged();
            m_selectBorderBlock = null;
        }
    }

    MotionEvent m_beforPopupMenu = null;
    PopupWindow m_popupMenu = null;

    private String buildMenuText(int resID)
    {
        String s = getContext().getString(resID);
        s = "  " + s;
        return s;
    }
    private void menuItemAddRow(LinearLayout menu)
    {
        TextView btnAddRow = new TextView(this.getContext());
        init_menu_item(btnAddRow);
        btnAddRow.setText(buildMenuText(R.string.add_row));
        btnAddRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                addBreakPoint(m_beforPopupMenu, GridBlock.BreakXY.Y);
            }
        });
        menu.addView(btnAddRow);

        btnAddRow.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_row));
                return false;
            }
        });
    }

    private void menuItemAddCol(LinearLayout menu)
    {
        TextView btnAddCol = new TextView(this.getContext());
        init_menu_item(btnAddCol);
        btnAddCol.setText(buildMenuText(R.string.add_col));
        btnAddCol.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                addBreakPoint(m_beforPopupMenu, GridBlock.BreakXY.X);
            }
        });
        menu.addView(btnAddCol);
        btnAddCol.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_col));
                return false;
            }
        });

    }


    private void menuItemAddRows(LinearLayout menu)
    {
        TextView btnAddRow = new TextView(this.getContext());
        init_menu_item(btnAddRow);
        btnAddRow.setText(buildMenuText(R.string.add_rows));
        btnAddRow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                //addBreakPoints(m_beforPopupMenu, GridBlock.BreakXY.Y);
                showInputPointsCountDlg(GridBlock.BreakXY.Y);
            }
        });
        menu.addView(btnAddRow);

        btnAddRow.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_rows));
                return false;
            }
        });
    }

    private void menuItemAddCols(LinearLayout menu)
    {
        TextView btnAddCol = new TextView(this.getContext());
        init_menu_item(btnAddCol);
        btnAddCol.setText(buildMenuText(R.string.add_cols));
        btnAddCol.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                showInputPointsCountDlg(GridBlock.BreakXY.X);
                //addBreakPoints(m_beforPopupMenu, GridBlock.BreakXY.X);
            }
        });
        menu.addView(btnAddCol);

        btnAddCol.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_cols));
                return false;
            }
        });
    }

    private void menuItemRemove(LinearLayout menu)
    {
        TextView btnRemove = new TextView(this.getContext());
        init_menu_item(btnRemove);
        btnRemove.setText(buildMenuText(R.string.remove));
        btnRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                showConfirmRemoveBlockDialog();
            }
        });
        menu.addView(btnRemove);

        btnRemove.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_remove));
                return false;
            }
        });
    }

    private void menuItemClearAll(LinearLayout menu)
    {
        TextView btnRemoveAll = new TextView(this.getContext());
        init_menu_item(btnRemoveAll);
        btnRemoveAll.setText(buildMenuText( R.string.clear_all));
        btnRemoveAll.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                removeAllBlocks();
            }
        });
        menu.addView(btnRemoveAll);

        btnRemoveAll.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_clear_all));
                return false;
            }
        });
    }

    private void menuItemSetReal(LinearLayout menu)
    {
        TextView btnToReal = new TextView(this.getContext());
        init_menu_item(btnToReal);
        btnToReal.setText(buildMenuText( R.string.set_real_view));
        btnToReal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                setBlockWithRealView();
            }
        });
        menu.addView(btnToReal);

        btnToReal.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_real));
                return false;
            }
        });

    }

    private void menuItemSetGraph(LinearLayout menu)
    {
        TextView btnToGraph = new TextView(this.getContext());
        init_menu_item(btnToGraph);
        btnToGraph.setText(buildMenuText( R.string.set_graph));
        btnToGraph.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                setBlockWithGraphView();
            }
        });
        menu.addView(btnToGraph);

        btnToGraph.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_add_graph));
                return false;
            }
        });

    }

    private void menuItemProperties(LinearLayout menu)
    {
        TextView btnProp = new TextView(this.getContext());
        init_menu_item(btnProp);
        btnProp.setText(buildMenuText( R.string.properties));
        btnProp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                showProperties();
            }
        });
        menu.addView(btnProp);

        btnProp.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_prop));
                return false;
            }
        });
    }

    private void menuItemLayoutProperties(LinearLayout menu)
    {
        TextView btnProp = new TextView(this.getContext());
        init_menu_item(btnProp);
        btnProp.setText(buildMenuText(R.string.layout_prop));
        btnProp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_popupMenu.dismiss();
                showLayoutProperties();
            }
        });
        menu.addView(btnProp);

        btnProp.setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_layout_prop));
                return false;
            }
        });
    }

    final int MAX_MENU_COUNT = 8;
    private void popupMenu(MotionEvent event)
    {
        if (m_popupMenu!= null) return;
        m_beforPopupMenu = event;

        LinearLayout menu = new LinearLayout(this.getContext());
        menu.setOrientation(LinearLayout.VERTICAL);

        menuItemAddRow(menu);
        menuItemAddCol(menu);

        addItemSeparator(menu);

        menuItemAddRows(menu);
        menuItemAddCols(menu);

        addItemSeparator(menu);

        menuItemRemove(menu);
        menuItemClearAll(menu);

        addItemSeparator(menu);

        menuItemSetReal(menu);
        menuItemSetGraph(menu);

        addItemSeparator(menu);


        menuItemProperties(menu);
        menuItemLayoutProperties(menu);



        m_popupMenu = new PopupWindow(menu,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        m_popupMenu.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F8F8F8")));

        m_popupMenu.setFocusable(true);

        m_popupMenu.setOutsideTouchable(true);
        m_popupMenu.update();

        int[] location = new int[2];
        this.getLocationOnScreen(location);

        m_popupMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                m_popupMenu = null;
                if (m_eventReceiver != null)
                    m_eventReceiver.onShowTips(KDSApplication.getContext().getString(R.string.tips_call_menu));
            }

        });

        //window.showAsDropDown(this, Math.round(-1*event.getX()), Math.round(-1*event.getY()));
        //https://blog.csdn.net/klx502/article/details/47723499
        int y = Math.round(event.getY() );
        int x = Math.round( event.getX() );
        int ntop = location[1] + Math.round(event.getY());
        int nleft = location[0] + Math.round(event.getX());

  //      m_popupMenu.showAtLocation(this,Gravity.NO_GRAVITY,nleft,ntop);

        int h = MIN_MENU_ITEM_HEIGHT* MAX_MENU_COUNT;


        if (y > this.getRect().height()/2)
            ntop -= h;


        m_popupMenu.showAtLocation(this,Gravity.NO_GRAVITY,nleft,ntop);
       // m_popupMenu.showAtLocation(this,Gravity.NO_GRAVITY, Math.round(event.getX()), Math.round(event.getY()));
    }
    private void addItemSeparator(LinearLayout parent)
    {
        View v = new View(this.getContext());
        //v.setText("");
        v.setBackgroundColor(Color.GRAY);
        v.setMinimumHeight(2);

        LayoutParams p = new LayoutParams(120, 2);
        v.setLayoutParams(p);
        parent.addView(v);
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(l, t, r, b);
        v.setLayoutParams(params);


    }

    final int MIN_MENU_ITEM_HEIGHT = 30;
    final int MENU_ITEM_MARGIN = 3;

    private void init_menu_item(TextView v)
    {


        v.setMinimumHeight(MIN_MENU_ITEM_HEIGHT);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setFocusable(true);
        v.setClickable(true);


       // setMargins(v, 5, 0,0,0);
    }

    public void addBreakPoint(MotionEvent event, GridBlock.BreakXY breakType)
    {
        if (m_selectedBlock == null) return;
        GridBlock blockWillAddBreakPoint = m_selectedBlock;

        GridBlock.BreakXY currentBreakType = blockWillAddBreakPoint.getBreakPointType();
        GridBlock parentBlock = blockWillAddBreakPoint.getParentBlock();
        if (parentBlock != null) {
            if (currentBreakType == GridBlock.BreakXY.Unknown && parentBlock.getBreakPointType() == breakType) {//add it to parent
                blockWillAddBreakPoint = blockWillAddBreakPoint.getParentBlock();
            }
        }
        if (blockWillAddBreakPoint == null) return;
        Rect rtSelected = m_block.findBlockRect(getRect(), blockWillAddBreakPoint);
        if (rtSelected == null) return;
        int x =Math.round( event.getX());
        int y = Math.round(event.getY());

        if (!rtSelected.contains(x,y)) return;
        int distanceX =x - rtSelected.left;
        int distanceY = y - rtSelected.top;

        float percentX = (float)distanceX/(float) rtSelected.width();
        float percentY = (float)distanceY/(float)rtSelected.height();
        if (breakType == GridBlock.BreakXY.Y)
            insertBlock(blockWillAddBreakPoint,m_selectedBlock, true, percentY);
        else if (breakType == GridBlock.BreakXY.X)
            insertBlock(blockWillAddBreakPoint,m_selectedBlock, false, percentX);


        requestLayout();


    }

    public void showInputPointsCountDlg(GridBlock.BreakXY breakType)
    {
        SOSDialogInputCount dlg = new SOSDialogInputCount(this.getContext(), this);
        dlg.setTag(breakType);
        dlg.show();
    }

    public void addBreakPoints( GridBlock.BreakXY breakType, int nCount)
    {
        if (nCount<=1) return;
        if (m_selectedBlock == null) return;
        GridBlock blockWillAddBreakPoint = m_selectedBlock;

        GridBlock.BreakXY currentBreakType = blockWillAddBreakPoint.getBreakPointType();
        GridBlock parentBlock = blockWillAddBreakPoint.getParentBlock();
        if (parentBlock != null) {
            if (currentBreakType == GridBlock.BreakXY.Unknown && parentBlock.getBreakPointType() == breakType) {//add it to parent
                blockWillAddBreakPoint = blockWillAddBreakPoint.getParentBlock();
            }
        }
        if (blockWillAddBreakPoint == null) return;

        float startPercent = m_selectedBlock.m_pointAxisPercent;
        float fltDistance = m_selectedBlock.getToPercent() - m_selectedBlock.m_pointAxisPercent;
        if (m_selectedBlock == blockWillAddBreakPoint)
        {//it is the parent block. We will add multiple rows/cols to parent
            startPercent =0f;
            fltDistance = 1f;
        }


        float percent = fltDistance/nCount;
        GridBlock blockBefore = m_selectedBlock;

        for (int i=0; i< nCount; i++)
        {

            if (breakType == GridBlock.BreakXY.Y)
                blockBefore = insertBlock(blockWillAddBreakPoint,blockBefore, true,i* percent + startPercent);
            else if (breakType == GridBlock.BreakXY.X)
                blockBefore = insertBlock(blockWillAddBreakPoint,blockBefore, false,i* percent+ startPercent);
        }

        requestLayout();

    }

    public void changeBreakPoint(MotionEvent event, GridBlock block)
    {
        if (block == null) return;



        GridBlock parentBlock = block.getParentBlock();
        if (parentBlock == null) return;


        Rect rtSelected = m_block.findBlockRect(getRect(), parentBlock);
        if (rtSelected == null) return;
        int x =Math.round( event.getX());
        int y = Math.round(event.getY());

        if (!rtSelected.contains(x,y)) return;
        int distanceX =x - rtSelected.left;
        int distanceY = y - rtSelected.top;

        float percentX = (float)distanceX/(float) rtSelected.width();
        float percentY = (float)distanceY/(float)rtSelected.height();

        if (parentBlock.getBreakPointType() == GridBlock.BreakXY.Y)
            block.m_pointAxisPercent = percentY;

        else if (parentBlock.getBreakPointType() == GridBlock.BreakXY.X)
            block.m_pointAxisPercent = percentX;





    }


    /**
     * remove selected block
     * @param
     */
    public void removeBlock()
    {
        if (m_selectedBlock == null) return;

        if (m_selectedBlock.m_viewAttached!= null) {
            this.removeView(m_selectedBlock.m_viewAttached);
            m_selectedBlock.m_viewAttached = null;
            if (m_eventReceiver != null)
                m_eventReceiver.onSOSLinearLayoutChanged();
            return;
        }
        if (m_selectedBlock.getParentBlock() == null) return;
        GridBlock parentBlock = m_selectedBlock.getParentBlock();
        parentBlock.removeBlock(m_selectedBlock);
        m_selectedBlock = null;
        refreshGui();
        requestLayout();
        if (m_eventReceiver != null)
            m_eventReceiver.onSOSLinearLayoutChanged();
    }

    public void removeAllBlocks()
    {
        showConfirmRemoveAllDialog();

    }

    private void showConfirmRemoveAllDialog()
    {
        AlertDialog d = new AlertDialog.Builder(this.getContext())
                .setTitle(this.getContext().getString(R.string.confirm))
                .setMessage(this.getContext().getString(R.string.confirm_remove_all))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SOSLinearLayout.this.clearAll();

                    }
                })
                .setCancelable(false)

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })
                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    private void showConfirmRemoveBlockDialog()
    {
        String strInfo = this.getContext().getString(R.string.confirm_remove_block);
        if (m_selectedBlock != null)
        {
            if (m_selectedBlock.m_viewAttached != null)
            {
                if (m_selectedBlock.m_viewAttached instanceof SOSRealTimeView)
                {
                    strInfo = this.getContext().getString(R.string.confirm_remove_real);
                }
                else if (m_selectedBlock.m_viewAttached instanceof  SOSGraphView)
                {
                    strInfo = this.getContext().getString(R.string.confirm_remove_graph);
                }
            }
        }
        AlertDialog d = new AlertDialog.Builder(this.getContext())
                .setTitle(this.getContext().getString(R.string.confirm))
                .setMessage(strInfo)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SOSLinearLayout.this.removeBlock();

                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                    }
                })

                .create();
        d.setCanceledOnTouchOutside(false);
        d.show();
    }

    public void setBlockView(View v)
    {
        if (m_selectedBlock == null) return;
        if (m_selectedBlock.m_viewAttached != null) {
            this.removeView(m_selectedBlock.m_viewAttached);
            m_selectedBlock.m_viewAttached = null;
        }
        m_selectedBlock.m_viewAttached = v;
        this.addView(v);
        if (m_eventReceiver != null)
            m_eventReceiver.onSOSLinearLayoutChanged();


    }


    private void showInputStationIDDlog()
    {

        SOSDlgInputStationID dlg = new SOSDlgInputStationID(this.getContext(), this);

        dlg.setTag(m_selectedBlock);
        dlg.show();
    }
    public void setBlockWithRealView()
    {
        SOSRealTimeView v = new SOSRealTimeView(this.getContext());
        setBlockView(v);
        showRealTimeViewProperties(v);
        //showInputStationIDDlog();
//        String s= m_block.getXmlString();
//        Log.i(TAG, s);
//        String s = "<Block Percent=\"0.01\"><Block Percent=\"0.02\"/><Block Percent=\"0.03\"><Block Percent=\"0.31\"/><Block Percent=\"0.32\"/></Block><Block Percent=\"0.04\"/></Block>";
//        m_block.parseString(s, getContext());
//        Log.i(TAG, s);
    }

    public void setBlockWithGraphView()
    {
        SOSGraphView v = new SOSGraphView(this.getContext());
       // v.setBackgroundColor(Color.BLACK);
        v.showDemo();
        setBlockView(v);
        showGraphViewProperties(v);

        //showInputStationIDDlog();
    }

    public void showProperties()
    {
        if (m_selectedBlock == null) return;
        View v = m_selectedBlock.m_viewAttached;
        if (v == null)
        {
            showLayoutProperties();
        }
        else if (v instanceof SOSRealTimeView)
        {
            showRealTimeViewProperties((SOSRealTimeView) v);
        }
        else if (v instanceof SOSGraphView)
        {
            showGraphViewProperties((SOSGraphView)v);
        }
    }
    private void showRealTimeViewProperties(SOSRealTimeView v)
    {
        SOSDialogRealViewProperties dlg = new SOSDialogRealViewProperties(this.getContext(), v, this);
        dlg.show();
    }

    private void showGraphViewProperties(SOSGraphView v)
    {
        SOSDialogGraphViewProperties dlg = new SOSDialogGraphViewProperties(this.getContext(), v, this, SOSKDSGlobalVariables.getKDSSOS().getSettings());
        dlg.show();
    }

    private void showLayoutProperties()
    {
        SOSDialogLayoutProperties dlg = new SOSDialogLayoutProperties(this.getContext(), this, this);
        dlg.show();
    }
    /*********************************************************************************************
     *
     */
    static public class GridBlock
    {
        public enum BreakXY
        {
            Unknown,
            X,
            Y,
        }


        GridBlock m_parentBlock = null;
        BreakXY m_breakPointType = BreakXY.Unknown;
        float m_pointAxisPercent = 0f;
        View m_viewAttached = null;
        int m_nBG = Color.TRANSPARENT;


        //It contains one hide block from 0 to first one.
        ArrayList<GridBlock> m_childBlocks = new ArrayList<>();

        public GridBlock()
        {

        }

        public GridBlock(GridBlock blockParent, float axisPercent, View viewAttached)
        {
            m_parentBlock = blockParent;
            m_pointAxisPercent = axisPercent;
            m_viewAttached = viewAttached;
        }

        public void setBG(int nColor)
        {
            m_nBG = nColor;
        }
        public int getBG()
        {
            return m_nBG;
        }
        public BreakXY getBreakPointType()
        {
            return m_breakPointType;
        }

        public GridBlock getParentBlock()
        {
            return m_parentBlock;
        }

        /**
         * update teh break points first, then reset block values
         * @param breakXY
         * @param pointPercentAxis
         *  The break point percent in axis, it is not the block occupy area percent.
         * @return
         */
        public GridBlock addBlock(BreakXY breakXY, float pointPercentAxis)
        {
            GridBlock block = null;
            if (m_childBlocks.size()==0) {
                block = new GridBlock(this, 0, null);
                m_childBlocks.add(block);
                block.m_viewAttached = this.m_viewAttached;
                this.m_viewAttached = null;

            }
            block =  new GridBlock(this,pointPercentAxis, null);
            m_childBlocks.add( block );
            m_breakPointType = breakXY;
            return block;

        }

        public GridBlock insertBlock(GridBlock blockAfter, BreakXY breakXY, float pointPercentAxis)
        {
            GridBlock block = null;
            if (blockAfter == this) {
                block = addBlock(breakXY, pointPercentAxis);

            }
            else
            {
                int nindex = m_childBlocks.indexOf(blockAfter);
                if (nindex<0) {
                    return addBlock(breakXY, pointPercentAxis);

                }
                block = new GridBlock(this, pointPercentAxis, null);
                m_childBlocks.add(nindex+1,block );
            }
            m_breakPointType = breakXY;
            return block;

        }
        /**
         * remove give block
         * @param block
         * @return
         */
        public boolean removeBlock(GridBlock block)
        {
            if (m_childBlocks.indexOf(block) ==0)
            {//it is the first one, from percent 0,
                //if it removed, and just one existed, remove all.
                if (m_childBlocks.size()>1)
                {
                    m_childBlocks.get(1).m_pointAxisPercent = 0;
                }

            }
            boolean b = m_childBlocks.remove(block);
            //if just one existed, set data to parent
            if (m_childBlocks.size() == 1)
            {
                GridBlock blockLastOne = m_childBlocks.get(0);
                m_childBlocks.clear();
                //copy data to parent.
                GridBlock parentBlock = blockLastOne.getParentBlock();
                parentBlock.copyLastOneToMe(blockLastOne);

            }
            return b;

        }

        public void copyLastOneToMe(GridBlock block)
        {
            m_breakPointType = block.getBreakPointType();
            m_viewAttached = block.m_viewAttached;
            m_childBlocks.clear();
            m_childBlocks.addAll(block.m_childBlocks);

            //don't copy parent

        }

        public static final int BORDER_MARGIN = 2;
        public static final int CHILD_VIEW_MARGIN = BORDER_MARGIN*2;
        /**
         *
         * @param rtParent
         *  The absolute coordinate
         * @param child
         * @return
         *  true: find this child
         *  false: don't get this child
         *
         */
        public boolean onLayout(Rect rtParent, View child)
        {

            //check myself first
            if (m_viewAttached == child)
            {
                child.layout(rtParent.left+CHILD_VIEW_MARGIN, rtParent.top+CHILD_VIEW_MARGIN, rtParent.right-CHILD_VIEW_MARGIN, rtParent.bottom-CHILD_VIEW_MARGIN);
                return true;
            }
            //check children
            for (int i=0; i< m_childBlocks.size(); i++)
            {
                Rect rt = getChildAbsoluteCoordinate(rtParent, i);

                if (m_childBlocks.get(i).m_viewAttached == child) {
                    child.layout(rt.left+CHILD_VIEW_MARGIN, rt.top+CHILD_VIEW_MARGIN, rt.right-CHILD_VIEW_MARGIN, rt.bottom-CHILD_VIEW_MARGIN);
                    return true;
                }
                else
                {
                    m_childBlocks.get(i).onLayout(rt, child);
                }
            }
            return false;
        }

        static public void drawBox(Canvas g, Rect rect, int color, int lineStrokeWidth, boolean bDotLine)
        {

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(lineStrokeWidth);
            if (bDotLine) {
                DashPathEffect pathEffect = new DashPathEffect(new float[]{1, 2}, 1);
                paint.setPathEffect(pathEffect);
            }
            Rect rt = new Rect(rect);
            rt.inset(BORDER_MARGIN,BORDER_MARGIN);
            g.drawRect(rt, paint);

        }
        public void onDraw(Canvas canvas, Rect rtMine, LayoutMode mode,boolean bShowBorder, int nBorderColor)
        {
            if (getBG() != Color.TRANSPARENT)
            {
                CanvasDC.fillRect( canvas, getBG(), rtMine);
            }
            if (mode == LayoutMode.Design) {
                if (isFocused())
                    drawBox(canvas, rtMine, nBorderColor, BORDER_SIZE*2, false);
                else {

                    drawBox(canvas, rtMine, nBorderColor, BORDER_SIZE, true);
                }
            }
            else
            {
                if (bShowBorder) {
                    Rect rt = new Rect(rtMine);
                    rt.inset( (-1) * BORDER_MARGIN*1,(-1) *BORDER_MARGIN*1);
                    drawBox(canvas, rt, nBorderColor, BORDER_SIZE * 1 , false);
                }
            }
            //check children
            for (int i = 0; i < m_childBlocks.size(); i++) {
                Rect rt = getChildAbsoluteCoordinate(rtMine, i);

                m_childBlocks.get(i).onDraw(canvas, rt, mode,bShowBorder, nBorderColor);
            }

        }
        public float getToPercent()
        {
            if (m_parentBlock == null)
                return 1f;
            GridBlock nextBlock = m_parentBlock.getNextBlock(this);
            if (nextBlock == null)
                return 1f;
            return nextBlock.m_pointAxisPercent;
        }
        public GridBlock getNextBlock(GridBlock block)
        {
            int index = -1;
            for (int i=0; i< m_childBlocks.size(); i++)
            {
                if (m_childBlocks.get(i) == block) {
                    index = i;
                    break;
                }
            }
            if (index<0) return null;
            index ++;
            if (index>= m_childBlocks.size())
                return null;
            return m_childBlocks.get(index);

        }
        /**
         *
         * @param rtParent
         * @param nChild
         * @return
         */
        public Rect getChildAbsoluteCoordinate(Rect rtParent, int nChild)
        {

            float fltFrom = m_childBlocks.get(nChild).m_pointAxisPercent;
            float fltTo = 1f;
            //it is not last one, get next point percent
            if (nChild < m_childBlocks.size()-1)
            {
                fltTo = m_childBlocks.get(nChild+1).m_pointAxisPercent;
            }
            int wParent = rtParent.width();
            int hParent = rtParent.height();
            int x=0, y=0, xx=0, yy=0;
            if (m_breakPointType == BreakXY.X)
            {
                x = getPercentValue(wParent, fltFrom) + rtParent.left;
                y = rtParent.top;
                xx = getPercentValue(wParent, fltTo) + rtParent.left;
                yy = rtParent.bottom;
            }
            else if (m_breakPointType == BreakXY.Y)
            {
                x =rtParent.left;//
                y = getPercentValue(hParent, fltFrom) + rtParent.top;
                xx = rtParent.right;//
                yy = getPercentValue(hParent, fltTo) + rtParent.top;
            }
            return new Rect(x, y, xx, yy);

        }

        private int getPercentValue(int nValue, float percent)
        {
            float flt = nValue * percent;
            return Math.round(flt);
        }

        public Rect findBlockRect(Rect rtMine, GridBlock block)
        {
            if (block == this) return rtMine;
            if (m_childBlocks.size() <=0)
                return null;
            else
            {
                for (int i=0; i< m_childBlocks.size(); i++)
                {
                    Rect rt = getChildAbsoluteCoordinate(rtMine, i);

                    Rect rtBlock = m_childBlocks.get(i).findBlockRect(rt, block);
                    if (rtBlock != null)
                        return rtBlock;
                }
            }
            return null;
        }
        public GridBlock findBlock(Rect rtMine, int x, int y)
        {
            if (!rtMine.contains(x,y)) return null;
            if (m_childBlocks.size() <=0)
                return this;
            else
            {
                for (int i=0; i< m_childBlocks.size(); i++)
                {
                    Rect rt = getChildAbsoluteCoordinate(rtMine, i);

                    GridBlock block = m_childBlocks.get(i).findBlock(rt, x,y);
                    if (block != null)
                        return block;
                }
            }
            return null;
        }

        public boolean isFocused()
        {
            return (this == m_selectedBlock);
        }
        final int BORDER_SIZE = 1;
        final int BORDER_CLICK_SIZE = 10;
        public GridBlock checkMouseIsInBorder(Rect rtMine, int x, int y)
        {
            if (!rtMine.contains(x,y)) return null;
            if (this.getParentBlock() != null)
            {
                if (this.getParentBlock().getBreakPointType() == BreakXY.X)
                {
                    Rect rt = new Rect(rtMine.left, rtMine.top, rtMine.left + BORDER_CLICK_SIZE, rtMine.bottom);
                    if (rt.contains(x,y))
                        return this;
                }
                else if (this.getParentBlock().getBreakPointType() == BreakXY.Y)
                {
                    Rect rt = new Rect(rtMine.left, rtMine.top, rtMine.right, rtMine.top + BORDER_CLICK_SIZE);
                    if (rt.contains(x,y))
                        return this;
                }
            }
            //check children
            if (m_childBlocks.size() <=0)
                return null;
            else
            {
                for (int i=0; i< m_childBlocks.size(); i++)
                {
                    Rect rt = getChildAbsoluteCoordinate(rtMine, i);

                    GridBlock block = m_childBlocks.get(i).checkMouseIsInBorder(rt, x,y);
                    if (block != null)
                        return block;
                }
            }
            return null;
        }

        public String getXmlString()
        {
            KDSXML xml = new KDSXML();
            outputToXml(xml);
            return xml.get_xml_string();

        }

        /**
         * format:
         *  <Block percent=0 >
         *      <View type></View>
         *      <Block percent=0>
         *
         *      </Block>
         *      <Block percent=0.55></Block>
         *  </Block>
         * @param xml
         * @return
         */
        public boolean outputToXml(KDSXML xml)
        {
            xml.newGroup("Block", true);
            xml.setAttribute("bg",KDSUtil.convertIntToString( m_nBG) );

            xml.setAttribute("Percent", KDSUtil.convertFloatToString(m_pointAxisPercent));
            xml.setAttribute("Direction",KDSUtil.convertIntToString( m_breakPointType.ordinal()));
            if (m_viewAttached!=null)
            {

                xml.newGroup("View", true);
                if (m_viewAttached instanceof SOSRealTimeView) {
                    ((SOSRealTimeView)m_viewAttached).outputToXml(xml);

                }
                else {
                    ((SOSGraphView)m_viewAttached).outputToXml(xml);

                }
                xml.back_to_parent();
            }

            for (int i=0; i< m_childBlocks.size(); i++)
                m_childBlocks.get(i).outputToXml(xml);
            xml.back_to_parent();
            return true;

        }

//        public boolean parseString(String s, Context context, SOSLinearLayout layout)
//        {
//            if (s.isEmpty()) return true;
//            KDSXML xml = new KDSXML();
//            if (!xml.loadString(s))
//                return false;
//            xml.back_to_root();
//
//            return  parseXml(xml,this, context,layout);
//
//
//        }
        private boolean parseXml(KDSXML xml, GridBlock blockMe,GridBlock parent, Context context, SOSLinearLayout layout)
        {

            String s= xml.getAttribute("Percent", "0");
            blockMe.m_pointAxisPercent = KDSUtil.convertStringToFloat(s, 0f);

            s = xml.getAttribute("bg", KDSUtil.convertIntToString(Color.TRANSPARENT));
            blockMe.m_nBG = KDSUtil.convertStringToInt(s, Color.TRANSPARENT);


            s = xml.getAttribute("Direction", "0");
            int n = KDSUtil.convertStringToInt(s, 0);
            blockMe.m_breakPointType = BreakXY.values()[n];
            blockMe.m_parentBlock = parent;

            if (xml.getFirstGroup("View"))
            {
                s = xml.getAttribute("Type", "0");
                if (s.equals("0"))
                {
                    SOSRealTimeView v = new SOSRealTimeView(context);
                    v.parseXml(xml);
                    blockMe.m_viewAttached = v;
                    layout.addView(v);

                }
                else
                {
                    SOSGraphView v = new SOSGraphView(context);

                    v.parseXml(xml);
                    blockMe.m_viewAttached = v;
                    if (layout.m_mode == LayoutMode.Design)
                        v.showDemo();
                    layout.addView(v);
                }
                xml.back_to_parent();
            }
            if (xml.getFirstGroup("Block"))
            {
               do {
                   GridBlock blockChild = new GridBlock();
                   parseXml(xml, blockChild,blockMe, context,layout);
                   blockMe.m_childBlocks.add(blockChild);
                   //xml.back_to_parent();
               }while (xml.getNextGroup("Block"));
                xml.back_to_parent();
            }
//            xml.back_to_parent();
            return true;

        }
    }

    public void onKDSDialogCancel(KDSUIDialogBase dialog)
    {

    }
    public void onKDSDialogOK(KDSUIDialogBase dialog, Object obj)
    {
        if (dialog instanceof SOSDialogInputCount)
        {
            int n = (int)obj;
            if ( n <=0) return;
            GridBlock.BreakXY breaktype =  (GridBlock.BreakXY) dialog.getTag();
            addBreakPoints(breaktype, n);
        }
        else if (dialog instanceof SOSDlgInputStationID)
        {
            String s = (String) ((SOSDlgInputStationID)dialog).getResult();
            if (s == null) return;
            GridBlock block =(GridBlock) ((SOSDlgInputStationID)dialog).getTag();
            if (block == null) return;
            View v = block.m_viewAttached;
            if (v instanceof SOSGraphView)
            {
                ((SOSGraphView)v).m_properties.m_stationID = s;
                ((SOSGraphView)v).showDemo();
            }
            else if (v instanceof SOSRealTimeView)
            {
                ((SOSRealTimeView)v).m_properties.m_stationID = s;

            }

        }
        //else
        //{
            refreshGui();
            if (m_eventReceiver != null)
                m_eventReceiver.onSOSLinearLayoutChanged();
        //}
    }

    public boolean parseString(String s, Context context)
    {
        if (s.isEmpty()) {
            init_with_default_values();
            return true;
        }
        KDSXML xml = new KDSXML();
        if (!xml.loadString(s))
            return false;
        xml.back_to_root();
        String str = xml.getAttribute("bg",KDSUtil.convertIntToString(SOSDialogLayoutProperties.DEFAULT_BG ));
        int nbg = KDSUtil.convertStringToInt(str, SOSDialogLayoutProperties.DEFAULT_BG);
        this.setBackgroundColor(nbg);

        str  = xml.getAttribute("fg",KDSUtil.convertIntToString(SOSDialogLayoutProperties.DEFAULT_FG ));
        int nfg = KDSUtil.convertStringToInt(str, SOSDialogLayoutProperties.DEFAULT_FG);
        this.setBorderColor(nfg);

        str = xml.getAttribute("border","0");
        m_bShowBorder = str.equals("1");




        if (!xml.getFirstGroup("Block"))
            return false;
        boolean b =  m_block.parseXml(xml,m_block,null, context,this);

        //boolean b = m_block.parseString(s, context, this);
        refreshGui();
        return b;
    }
    private void init_with_default_values()
    {
        int nbg =  SOSDialogLayoutProperties.DEFAULT_BG;
        this.setBackgroundColor(nbg);
    }
    /**
     * <Layout>
     *     <Block>
     *         <Block></Block>
     *         <Block></Block>
     *     </Block>
     * </Layout>
     * @return
     */
    public String outputToString()
    {
        KDSXML xml = new KDSXML();
        xml.newGroup("Layout", true);
        int n = SOSDialogPropertiesBase.getButtonBG(this, SOSDialogLayoutProperties.DEFAULT_BG);

        xml.setAttribute("bg",KDSUtil.convertIntToString(n) );

        n = m_nBorderColor;
        xml.setAttribute("fg",KDSUtil.convertIntToString(n) );


        xml.setAttribute("border",m_bShowBorder?"1":"0" );

        m_block.outputToXml(xml);//.getXmlString();
        return xml.get_xml_string();
    }

    public void clearAll()
    {
        m_block.m_viewAttached= null;
        m_block.m_breakPointType = GridBlock.BreakXY.Unknown;
        m_block.m_childBlocks.clear();
        this.removeAllViews();
        if (m_eventReceiver != null)
            m_eventReceiver.onSOSLinearLayoutChanged();
    }





}
