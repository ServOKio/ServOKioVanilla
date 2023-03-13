package net.servokio.vanilla.modules.mods.lockScreenWidget;

import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class LSWidgetHostView extends AppWidgetHostView {
    public static final String[] clickTypes = {"Single", "Double", "Long"};
    private int clickType = -1;
    private boolean unlockKeyguardWhenClicked = false;
    private GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
        private boolean flagLongPress = false;

        @Override
        public boolean onDown(MotionEvent e) {
            return clickType == -1;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (clickType == 0 && unlockKeyguardWhenClicked) sendBroadcastUnlockDevice();
            return clickType != 0;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            gestureDetector.setIsLongpressEnabled(false);
            if (clickType == 1 && unlockKeyguardWhenClicked) sendBroadcastUnlockDevice();
            return clickType != 1;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (clickType == 2 && unlockKeyguardWhenClicked) {
                sendBroadcastUnlockDevice();
                e.setAction(1);
                dispatchTouchEvent(e);
            }
        }
    });

    public LSWidgetHostView(Context context) {
        super(context);
    }

    public void updateHostViewWithConfiguration(HostViewConfiguration conf, AppWidgetProviderInfo info, Context context) {
        int minWidth;
        int minHeight;
        if (conf == null || info == null || context == null) {
            setClickType(-1);
            return;
        }
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        if (conf.isUseCustomWidth()) {
            lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getCustomWidth(), Resources.getSystem().getDisplayMetrics());
            minWidth = conf.getCustomWidth();
        } else {
            if (HostViewConfiguration.widthWidgetStockConverter[conf.getWidth()] == 100) {
                lp.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, info.minWidth, Resources.getSystem().getDisplayMetrics());
            } else {
                lp.width = HostViewConfiguration.widthWidgetStockConverter[conf.getWidth()];
            }
            minWidth = info.minWidth;
        }
        if (conf.isUseCustomHeight()) {
            lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getCustomHeight(), Resources.getSystem().getDisplayMetrics());
            minHeight = conf.getCustomHeight();
        } else {
            if (HostViewConfiguration.heightWidgetStockConverter[conf.getHeight()] == 101) {
                lp.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, info.minHeight, Resources.getSystem().getDisplayMetrics());
            } else {
                lp.height = HostViewConfiguration.heightWidgetStockConverter[conf.getHeight()];
            }
            minHeight = info.minHeight;
        }
        setTranslationX(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getTranslateX(), Resources.getSystem().getDisplayMetrics()));
        setTranslationY(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getTranslateY(), Resources.getSystem().getDisplayMetrics()));
        setScaleX(conf.getScaleX());
        setScaleY(conf.getScaleY());
        setRotationX(conf.getRotateX());
        setRotationY(conf.getRotateY());
        setRotation(conf.getRotateZ());
        lp.gravity = HostViewConfiguration.gravitiesConverter[conf.getGravity()];
        if (conf.isUseCustomMargin()) {
            lp.setMargins(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getLeftMargin(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getTopMargin(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getRightMargin(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getBottomMargin(), Resources.getSystem().getDisplayMetrics())
            );
        }
        setLayoutParams(lp);
        if (!conf.isUseCustomPadding()) {
            Rect padding = AppWidgetHostView.getDefaultPaddingForWidget(context, info.provider, null);
            setPadding(padding.left, padding.top, padding.right, padding.bottom);
        } else {
            setPadding(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getLeftPadding(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getTopPadding(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getRightPadding(), Resources.getSystem().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.getBottomPadding(), Resources.getSystem().getDisplayMetrics())
            );
        }
        //setClickType(conf.isClickable() ? clickTypesConverter[conf.getClickType()] : -1);
        //setUnlockKeyguardWhenClicked(conf.isUnlockWhenClicked());
        setBackgroundColor(conf.isUseBackColorElevation() ? conf.getBackgroundColor() : 0);
        setElevation((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, conf.isUseBackColorElevation() ? conf.getElevation() : 0, Resources.getSystem().getDisplayMetrics()));
        updateAppWidgetSize(null, minWidth, minHeight, minWidth, minHeight);
        requestLayout();
    }


    public void sendBroadcastUnlockDevice() {
        Intent intent = new Intent();
        intent.setAction(Actions.ACTION_UNLOCK_KEYGUARD);
        getContext().sendBroadcast(intent);
    }

    public void setClickType(int clickType) {
        this.clickType = clickType;
    }
}