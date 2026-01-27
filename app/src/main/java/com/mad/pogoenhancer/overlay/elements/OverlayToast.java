package com.mad.pogoenhancer.overlay.elements;

import android.content.Context;
import android.content.res.Resources;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;
import com.mad.pogoenhancer.overlay.OverlayFragmentManager;
import com.mad.pogoenhancer.overlay.OverlayManager;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public abstract class OverlayToast extends OverlayFragmentManager {
    private int _durationMillis;
    protected CountDownTimer _remainingTimeCounter;
    protected TextView _toastText;
    protected ImageView _toastImage;
    protected ImageView _secondImage;

    public OverlayToast(OverlayManager overlayManager, Context context) {
        super(overlayManager, context);
    }

    private int getDrawableWidth() {
        float dip = 30f;
        Resources r = this._context.getResources();
        float px = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
        return (int) px;
    }

    public void setText(String text, @DrawableRes int toastImage, @DrawableRes int secondImage) {
        if (this._durationMillis > 0) {
            stopCountDownTimer();
        }

        // Set text...
        if (toastImage != 0) {
            _toastImage.setImageDrawable(this._context.getResources().getDrawable(toastImage));
            _toastImage.getLayoutParams().width = getDrawableWidth();
            _toastImage.setVisibility(View.VISIBLE);
        } else {
            _toastImage.setImageDrawable(null);
            _toastImage.setVisibility(View.GONE);
        }

        if (secondImage != 0) {
            _secondImage.setImageDrawable(this._context.getResources().getDrawable(secondImage));
            _secondImage.getLayoutParams().width = getDrawableWidth();
            _secondImage.setVisibility(View.VISIBLE);
        } else {
            _secondImage.setImageDrawable(null);
            _secondImage.setVisibility(View.GONE);
        }

        if (text != null) {
            _toastText.setText(text);
            //_toastText.getLayoutParams().width = getDrawableWidth();
        }

        if (this._durationMillis > 0) {
            startCountDownTimer(this._durationMillis, 1000);
        }
        this.showFragment();
    }

    private void stopCountDownTimer() {
        if (this._remainingTimeCounter != null) {
            this._remainingTimeCounter.cancel();
        }
    }

    @Override
    protected void specificCleanup() {
        this.stopCountDownTimer();
    }

    @Override
    protected int getContentLayoutRes() {
        return R.layout.iv_toast;
    }

    @Override
    protected int getBaseWidth() {
        return WRAP_CONTENT;
    }

    @Override
    protected void storeVisibility(boolean visible) {
        // do nothing, toasts are not there to stay
    }

    @Override
    protected boolean fragmentPreviouslyShown() {
        return false;
    }

    @Override
    protected void specificSetup() {
        super.specificSetup();

        String duration = this._sharedPreferences.getString(
                Constants.SHAREDPERFERENCES_KEYS.IV_TOAST_DURATION,
                Constants.DEFAULT_VALUES.IV_TOAST_DURATION
        );

        this._durationMillis = 2000;
        switch (duration) {
            case "2s":
                break;
            case "4s":
                this._durationMillis = 4000;
                break;
            case "6s":
                this._durationMillis = 6000;
                break;
            default:
                this._durationMillis = -1;
        }

        if (this._durationMillis == -1) {
            ImageView image = this._enclosingLayout.findViewById(R.id.custom_toast_image);
            image.setClickable(true);
            image.setImageDrawable(this._context.getResources().getDrawable(R.drawable.ic_dialog_close));
            image.setOnClickListener(v -> hideFragmentEntirely());
            image.getLayoutParams().width = getDrawableWidth();
            image.getLayoutParams().height = getDrawableWidth();
        }

        this._toastText = this._enclosingLayout.findViewById(R.id.custom_toast_message);

        this._toastImage = this._enclosingLayout.findViewById(R.id.toast_image);
        this._toastImage.setImageDrawable(null);
        this._toastImage.getLayoutParams().width = 10;

        this._secondImage = this._enclosingLayout.findViewById(R.id.toast_second_image);
        this._secondImage.setImageDrawable(null);
        this._secondImage.getLayoutParams().width = 10;
    }

    private void startCountDownTimer(long duration, long interval) {
        this._remainingTimeCounter = new CountDownTimer(duration, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                hideFragmentEntirely();
            }
        };
        this._remainingTimeCounter.start();
    }
}
