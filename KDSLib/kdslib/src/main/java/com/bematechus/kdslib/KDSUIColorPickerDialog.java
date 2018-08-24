
package com.bematechus.kdslib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class KDSUIColorPickerDialog {

    static public Object g_instance = null;

    public interface OnColorPickerDlgListener {
        public void onCancel(KDSUIColorPickerDialog dialog);

        public void onOk(KDSUIColorPickerDialog dialog, int color);
    }

    final AlertDialog dialog;
    private final boolean supportsAlpha;
    final OnColorPickerDlgListener listener;
    final View viewHue;
    final KDSUIColorPickerSquare viewSatVal;
    final ImageView viewCursor;
    final ImageView viewAlphaCursor;
    final View viewOldColor;
    final View viewNewColor;
    final View viewAlphaOverlay;
    final ImageView viewTarget;
    final ImageView viewAlphaCheckered;
    final ViewGroup viewContainer;
    final float[] currentColorHsv = new float[3];
    int alpha;

    boolean m_bEnableTextChangedEvent = false;
    EditText m_txtR = null;
    EditText m_txtG = null;
    EditText m_txtB = null;
    private Object m_objTag = null;
    public void  setTag(Object obj)
    {
        m_objTag = obj;
    }
    public Object getTag()
    {
        return m_objTag;
    }
    /**
     * Create an AmbilWarnaDialog.
     *
     * @param context activity context
     * @param color current color
     * @param listener an OnAmbilWarnaListener, allowing you to get back error or OK
     */
    public KDSUIColorPickerDialog(final Context context, int color, OnColorPickerDlgListener listener) {
        this(context, color, false, listener);
        g_instance = this;
    }

    /**
     * Create an AmbilWarnaDialog.
     *
     * @param context activity context
     * @param color current color
     * @param supportsAlpha whether alpha/transparency controls are enabled
     * @param listener an OnAmbilWarnaListener, allowing you to get back error or OK
     */
    public KDSUIColorPickerDialog(final Context context, int color, boolean supportsAlpha, OnColorPickerDlgListener listener) {
        g_instance = this;
        this.supportsAlpha = supportsAlpha;
        this.listener = listener;

        if (!supportsAlpha) { // remove alpha if not supported
            color = color | 0xff000000;
        }

        Color.colorToHSV(color, currentColorHsv);
        alpha = Color.alpha(color);

        //final View view = LayoutInflater.from(context).inflate(R.layout.kdsui_dlg_color_picker, null);
        final View view = LayoutInflater.from(context).inflate(R.layout.kdsui_dialog_color_picker, null);

        m_txtR = (EditText) view.findViewById(R.id.txtR);
        m_txtG = (EditText) view.findViewById(R.id.txtG);
        m_txtB = (EditText) view.findViewById(R.id.txtB);

        viewHue = view.findViewById(R.id.ambilwarna_viewHue);
        viewSatVal = (KDSUIColorPickerSquare) view.findViewById(R.id.ambilwarna_viewSatBri);
        viewCursor = (ImageView) view.findViewById(R.id.ambilwarna_cursor);
        viewOldColor = view.findViewById(R.id.ambilwarna_oldColor);
        viewNewColor = view.findViewById(R.id.ambilwarna_newColor);
        viewTarget = (ImageView) view.findViewById(R.id.ambilwarna_target);
        viewContainer = (ViewGroup) view.findViewById(R.id.ambilwarna_viewContainer);
        viewAlphaOverlay = view.findViewById(R.id.ambilwarna_overlay);
        viewAlphaCursor = (ImageView) view.findViewById(R.id.ambilwarna_alphaCursor);
        viewAlphaCheckered = (ImageView) view.findViewById(R.id.ambilwarna_alphaCheckered);

        { // hide/show alpha
            viewAlphaOverlay.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
            viewAlphaCursor.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
            viewAlphaCheckered.setVisibility(supportsAlpha? View.VISIBLE: View.GONE);
        }

        viewSatVal.setHue(getHue());
        viewOldColor.setBackgroundColor(color);
        viewNewColor.setBackgroundColor(color);
        showRGB(color);

        m_txtR.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                    enableTextChangedEvent(hasFocus);
            }
        });

        m_txtR.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {     }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {       }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    m_txtR.setText("0");
                }
                if (KDSUtil.convertStringToInt(s.toString(), 0) >255)
                {
                    m_txtR.setText("255");
                }
                onRGBChanged();
            }
        });

        m_txtG.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                enableTextChangedEvent(hasFocus);
            }
        });
        m_txtG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {     }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {       }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    m_txtG.setText("0");
                }
                if (KDSUtil.convertStringToInt(s.toString(), 0) >255)
                {
                    m_txtG.setText("255");
                }
                onRGBChanged();
            }
        });

        m_txtB.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                enableTextChangedEvent(hasFocus);
            }
        });
        m_txtB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {     }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {       }
            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    m_txtB.setText("0");
                }
                if (KDSUtil.convertStringToInt(s.toString(), 0) >255)
                {
                    m_txtB.setText("255");
                }
                onRGBChanged();
            }
        });
        viewHue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_MOVE
                        || event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP) {
                    makeAllTextLostFocus();
                    float y = event.getY();
                    if (y < 0.f) y = 0.f;
                    if (y > viewHue.getMeasuredHeight()) {
                        y = viewHue.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
                    }
                    float hue = 360.f - 360.f / viewHue.getMeasuredHeight() * y;
                    if (hue == 360.f) hue = 0.f;
                    setHue(hue);

                    // update view
                    viewSatVal.setHue(getHue());
                    //viewSatVal.setHue(hue);
                    moveCursor();
                    viewNewColor.setBackgroundColor(getColor());
                    showRGB(getColor());
                    updateAlphaView();
                    return true;
                }
                return false;
            }
        });

        if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if ((event.getAction() == MotionEvent.ACTION_MOVE)
                        || (event.getAction() == MotionEvent.ACTION_DOWN)
                        || (event.getAction() == MotionEvent.ACTION_UP)) {
                    makeAllTextLostFocus();
                    float y = event.getY();
                    if (y < 0.f) {
                        y = 0.f;
                    }
                    if (y > viewAlphaCheckered.getMeasuredHeight()) {
                        y = viewAlphaCheckered.getMeasuredHeight() - 0.001f; // to avoid jumping the cursor from bottom to top.
                    }
                    final int a = Math.round(255.f - ((255.f / viewAlphaCheckered.getMeasuredHeight()) * y));
                    KDSUIColorPickerDialog.this.setAlpha(a);

                    // update view
                    moveAlphaCursor();
                    int col = KDSUIColorPickerDialog.this.getColor();
                    int c = a << 24 | col & 0x00ffffff;
                    viewNewColor.setBackgroundColor(c);
                    showRGB(c);
                    return true;
                }
                return false;
            }
        });
        viewSatVal.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE
                        || event.getAction() == MotionEvent.ACTION_DOWN
                        || event.getAction() == MotionEvent.ACTION_UP) {
                    makeAllTextLostFocus();
                    float x = event.getX(); // touch event are in dp units.
                    float y = event.getY();

                    if (x < 0.f) x = 0.f;
                    if (x > viewSatVal.getMeasuredWidth()) x = viewSatVal.getMeasuredWidth();
                    if (y < 0.f) y = 0.f;
                    if (y > viewSatVal.getMeasuredHeight()) y = viewSatVal.getMeasuredHeight();

                    setSat(1.f / viewSatVal.getMeasuredWidth() * x);
                    setVal(1.f - (1.f / viewSatVal.getMeasuredHeight() * y));

                    // update view
                    moveTarget();
                    viewNewColor.setBackgroundColor(getColor());
                    showRGB(getColor());
                    return true;
                }
                return false;
            }
        });

        dialog = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIColorPickerDialog.this.listener != null) {
                            KDSUIColorPickerDialog.this.listener.onOk(KDSUIColorPickerDialog.this, getColor());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (KDSUIColorPickerDialog.this.listener != null) {
                            KDSUIColorPickerDialog.this.listener.onCancel(KDSUIColorPickerDialog.this);
                        }
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    // if back button is used, call back our listener.
                    @Override
                    public void onCancel(DialogInterface paramDialogInterface) {
                        if (KDSUIColorPickerDialog.this.listener != null) {
                            KDSUIColorPickerDialog.this.listener.onCancel(KDSUIColorPickerDialog.this);
                        }

                    }
                })
                .create();
        // kill all padding from the dialog window
        dialog.setView(view, 0, 0, 0, 0);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                g_instance = null;
            }
        });
        // move cursor & target on first draw
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                moveCursor();
                if (KDSUIColorPickerDialog.this.supportsAlpha) moveAlphaCursor();
                moveTarget();
                if (KDSUIColorPickerDialog.this.supportsAlpha) updateAlphaView();
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP)
                {
                    KDSKbdRecorder.convertKeyEvent(keyCode, event);
                }
                return false;
            }
        });

        View v = view.findViewById(R.id.llMain);
        v.requestFocus();
    }

    protected void onRGBChanged()
    {
        if (!isTextChangedEventEnabled()) return;
        String strR = m_txtR.getText().toString();
        String strG = m_txtG.getText().toString();
        String strB = m_txtB.getText().toString();
        int r = KDSUtil.convertStringToInt(strR, 0);
        int g = KDSUtil.convertStringToInt(strG, 0);
        int b = KDSUtil.convertStringToInt(strB, 0);
        int color = Color.argb(255, r, g, b);
        if (!supportsAlpha) { // remove alpha if not supported
            color = color | 0xff000000;
        }

        Color.colorToHSV(color, currentColorHsv);
        alpha = Color.alpha(color);
        viewSatVal.setHue(getHue());
       // if (color != -1 && color !=0xff000000)
          moveCursor();
        viewNewColor.setBackgroundColor(getColor());
        moveAlphaCursor();
        moveTarget();
    }
    protected void showRGB(int color)
    {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        m_txtR.setText(KDSUtil.convertIntToString(r));
        m_txtG.setText(KDSUtil.convertIntToString(g));
        m_txtB.setText(KDSUtil.convertIntToString(b));
    }
    protected void moveCursor() {
        float y = viewHue.getMeasuredHeight() - (getHue() * viewHue.getMeasuredHeight() / 360.f);
        if (y == viewHue.getMeasuredHeight()) y = 0.f;
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (viewHue.getLeft() - Math.floor(viewCursor.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (viewHue.getTop() + y - Math.floor(viewCursor.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
        viewCursor.setLayoutParams(layoutParams);
        viewContainer.requestLayout();
    }

    protected void moveTarget() {
        float x = getSat() * viewSatVal.getMeasuredWidth();
        float y = (1.f - getVal()) * viewSatVal.getMeasuredHeight();
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewTarget.getLayoutParams();
        layoutParams.leftMargin = (int) (viewSatVal.getLeft() + x - Math.floor(viewTarget.getMeasuredWidth() / 2) - viewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) (viewSatVal.getTop() + y - Math.floor(viewTarget.getMeasuredHeight() / 2) - viewContainer.getPaddingTop());
        viewTarget.setLayoutParams(layoutParams);
    }

    protected void moveAlphaCursor() {
        final int measuredHeight = this.viewAlphaCheckered.getMeasuredHeight();
        float y = measuredHeight - ((this.getAlpha() * measuredHeight) / 255.f);
        final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.viewAlphaCursor.getLayoutParams();
        layoutParams.leftMargin = (int) (this.viewAlphaCheckered.getLeft() - Math.floor(this.viewAlphaCursor.getMeasuredWidth() / 2) - this.viewContainer.getPaddingLeft());
        layoutParams.topMargin = (int) ((this.viewAlphaCheckered.getTop() + y) - Math.floor(this.viewAlphaCursor.getMeasuredHeight() / 2) - this.viewContainer.getPaddingTop());

        this.viewAlphaCursor.setLayoutParams(layoutParams);
    }

    private int getColor() {
        final int argb = Color.HSVToColor(currentColorHsv);
        return alpha << 24 | (argb & 0x00ffffff);
    }

    private float getHue() {
        return currentColorHsv[0];
    }

    private float getAlpha() {
        return this.alpha;
    }

    private float getSat() {
        return currentColorHsv[1];
    }

    private float getVal() {
        return currentColorHsv[2];
    }

    private void setHue(float hue) {
        currentColorHsv[0] = hue;
    }

    private void setSat(float sat) {
        currentColorHsv[1] = sat;
    }

    private void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    private void setVal(float val) {
        currentColorHsv[2] = val;
    }

    public void show() {
        dialog.show();
    }

    public AlertDialog getDialog() {
        return dialog;
    }

    private void updateAlphaView() {
        final GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
                Color.HSVToColor(currentColorHsv), 0x0
        });
        viewAlphaOverlay.setBackgroundDrawable(gd);
    }

    private void enableTextChangedEvent(boolean bEnabled)
    {
        m_bEnableTextChangedEvent = bEnabled;
    }
    private boolean isTextChangedEventEnabled()
    {
        return m_bEnableTextChangedEvent;
    }

    private void makeAllTextLostFocus()
    {
        m_txtR.clearFocus();
        m_txtG.clearFocus();
        m_txtB.clearFocus();
    }
}
