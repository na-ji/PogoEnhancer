package com.mad.pogoenhancer.overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;

import com.mad.pogoenhancer.R;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public abstract class OverlayView implements View.OnTouchListener {
    private volatile boolean moving;
    protected Context _context;
    protected WindowManager _windowManager;
    protected SharedPreferences _sharedPreferences;
    protected View _moveButton;
    protected View _topLeftCorner;
    protected View _enclosingLayout;

    private float offsetX;
    private float offsetY;
    private float originalXPos;
    private float originalYPos;

    public OverlayView(Context context) {
        this._context = context;
        this._windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        this._sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this._context);
    }

    protected int getLayoutFlag() {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        return LAYOUT_FLAG;
    }

    protected void toggleVisibilityOfView(View view) {
        if (view == null) {
            return;
        }
        int visibility = view.getVisibility();
        if (visibility == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (this._moveButton != null && v == this._moveButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getRawX();
                float y = event.getRawY();

                moving = false;

                int[] location = new int[2];
                this._enclosingLayout.getLocationOnScreen(location);

                originalXPos = location[0];
                originalYPos = location[1];

                offsetX = originalXPos - x;
                offsetY = originalYPos - y;

                return true;
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                int[] topLeftLocationOnScreen = new int[2];
                this._topLeftCorner.getLocationOnScreen(topLeftLocationOnScreen);

                float x = event.getRawX();
                float y = event.getRawY();

                WindowManager.LayoutParams params = (WindowManager.LayoutParams) this._enclosingLayout.getLayoutParams();

                int newX = (int) (offsetX + x);
                int newY = (int) (offsetY + y);

                if (Math.abs(newX - originalXPos) < 1 && Math.abs(newY - originalYPos) < 1 && !moving) {
                    return false;
                }

                params.x = newX - (topLeftLocationOnScreen[0]);
                params.y = newY - (topLeftLocationOnScreen[1]);

                this._windowManager.updateViewLayout(this._enclosingLayout, params);
                moving = true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                int[] location = new int[2];
                this._enclosingLayout.getLocationOnScreen(location);

                this.storeLocation(location[0], location[1]);
                if (!moving) {
                    v.performClick();
                }
                return moving;
            }

            return false;
        } else {
            if (!moving) {
                v.performClick();
            }
            return false;
        }
    }

    public void setup() {
        LayoutInflater li = (LayoutInflater) this._context.getSystemService(LAYOUT_INFLATER_SERVICE);
        this._enclosingLayout = li.inflate(getEnclosingLayoutRes(), null);

        CardView content = this._enclosingLayout.findViewById(R.id.overlay_partials);
        li.inflate(getContentLayoutRes(), content);
        this._moveButton = this.getMoveButton();

        this.specificSetup();

        // in order to know where our overlay is being moved, we need an anchor for the offset
        this._topLeftCorner = new View(this._context);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                this.getLayoutFlag(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.START | Gravity.TOP;
        topLeftParams.x = 0;
        topLeftParams.y = 0;
        topLeftParams.width = 0;
        topLeftParams.height = 0;

        // parameters for the entire fragment
        WindowManager.LayoutParams params = this.getLayoutParams();
        this._windowManager.addView(this._topLeftCorner, topLeftParams);
        this._windowManager.addView(this._enclosingLayout, params);
        if (this._moveButton != null) {
            this._moveButton.setOnTouchListener(this);
        }
    }

    public final void cleanup() {
        this._windowManager.removeView(this._enclosingLayout);
        this._windowManager.removeView(this._topLeftCorner);
        this.specificCleanup();
    }

    protected abstract void specificCleanup();

    protected abstract WindowManager.LayoutParams getLayoutParams();

    protected abstract void specificSetup();

    protected abstract @LayoutRes
    int getContentLayoutRes();

    protected abstract View getMoveButton();

    protected abstract void storeLocation(int offsetX, int offsetY);

    protected abstract @LayoutRes
    int getEnclosingLayoutRes();
}
