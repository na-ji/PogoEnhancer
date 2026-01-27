package com.mad.pogoenhancer.overlay.elements.toastNotifications;

import android.content.Context;
import android.view.WindowManager;

import com.mad.pogoenhancer.overlay.OverlayManager;
import com.mad.pogoenhancer.overlay.elements.OverlayToast;

public class NormalToastNotificationLayoutManager extends OverlayToast {
    public NormalToastNotificationLayoutManager(OverlayManager overlayManager, Context context) {
        super(overlayManager, context);
    }

    @Override
    protected WindowManager.LayoutParams getLayoutParams() {
        return null;
    }

    @Override
    protected void storeLocation(int offsetX, int offsetY) {

    }
}
