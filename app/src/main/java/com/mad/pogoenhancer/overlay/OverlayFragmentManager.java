package com.mad.pogoenhancer.overlay;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.mad.pogoenhancer.Constants;
import com.mad.pogoenhancer.R;

public abstract class OverlayFragmentManager extends OverlayView {
    protected OverlayManager _overlayManager;
    protected CardView _viewContent;


    public OverlayFragmentManager(OverlayManager overlayManager, Context context) {
        super(context);

        this._overlayManager = overlayManager;
    }

    public boolean isVisible() {
        if (this._enclosingLayout == null) {
            return false;
        }
        return this._enclosingLayout.getVisibility() == View.VISIBLE;
    }

    public void hideFragmentEntirely() {
        this._enclosingLayout.setVisibility(View.GONE);
    }

    public void showFragment() {
        this._enclosingLayout.setVisibility(View.VISIBLE);
    }

    public void toggleMoveButtonVisibility() {
        this.toggleVisibilityOfView(this._moveButton);
    }

    public void toggleFragmentVisibility() {
        this.toggleVisibilityOfView(this._enclosingLayout);
        this.storeVisibility(this._enclosingLayout.getVisibility() == View.VISIBLE);
    }

    protected abstract void storeVisibility(boolean visible);

    @Override
    protected void specificSetup() {
        this._viewContent = this._enclosingLayout.findViewById(R.id.overlay_partials);

        // params for the content
        float dpFactor = this._context.getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams layoutParams = this._viewContent.getLayoutParams();
        int overlayScaling = this._sharedPreferences.getInt(
                Constants.SHAREDPERFERENCES_KEYS.OVERLAY_SCALING,
                Constants.DEFAULT_VALUES.OVERLAY_SCALING
        );
        int baseWidth = this.getBaseWidth();
        if (baseWidth > 0) {
            layoutParams.width = (int) (dpFactor * (this.getBaseWidth() + overlayScaling));
        } else {
            layoutParams.width = baseWidth;
        }
        this._viewContent.setLayoutParams(layoutParams);

        // finally, add the view we have built
        restoreVisibility();
    }

    public void restoreVisibility() {
        if (!fragmentPreviouslyShown()) {
            this._enclosingLayout.setVisibility(View.GONE);
        } else {
            this._enclosingLayout.setVisibility(View.VISIBLE);
        }
    }

    protected abstract int getBaseWidth();

    protected abstract boolean fragmentPreviouslyShown();

    @Override
    protected View getMoveButton() {
        return this._enclosingLayout.findViewById(R.id.overlay_position_lock);
    }

    @Override
    protected int getEnclosingLayoutRes() {
        return R.layout.overlay;
    }
}
