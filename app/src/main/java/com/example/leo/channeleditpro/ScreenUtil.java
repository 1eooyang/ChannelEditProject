package com.example.leo.channeleditpro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.lang.reflect.Method;


public class ScreenUtil {

    // Screen Params
    public static final int BASE_SCREEN_WIDTH = 720;
    public static final int BASE_SCREEN_HEIGHT = 1280;

    public static float sScale = 1;

    /**
     * Set the screen scale value
     *
     * @param context current activity
     */
    public static void initScaleValue(Context context) {
        int width;
        DisplayMetrics displayMetrics = context.getResources()
                .getDisplayMetrics();
        width = displayMetrics.widthPixels;
        sScale = (float) width / BASE_SCREEN_WIDTH;
        Log.i("scale factor:", sScale + "");
    }

    public static ViewGroup.LayoutParams scaleParams(
            ViewGroup.LayoutParams params) {
        if (params == null) {
            throw new RuntimeException("params not's null");
        }
        if (params.width > 0) {
            params.width = getScalePxValue(params.width);
        }

        if (params.height > 0) {
            params.height = getScalePxValue(params.height);
        }

        if (params instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) params;
            lp.topMargin = getScalePxValue(lp.topMargin);
            lp.leftMargin = getScalePxValue(lp.leftMargin);
            lp.bottomMargin = getScalePxValue(lp.bottomMargin);
            lp.rightMargin = getScalePxValue(lp.rightMargin);
        }

        return params;
    }

    /**
     * Scale the view
     *
     * @param view The scale view
     * @return null if the view is null. the view all param value.
     */
    public static void processScale(View view) {
        if (view == null)
            return;

        // set padding
        int left = getScalePxValue(view.getPaddingLeft());
        int top = getScalePxValue(view.getPaddingTop());
        int right = getScalePxValue(view.getPaddingRight());
        int bottom = getScalePxValue(view.getPaddingBottom());

        view.setPadding(left, top, right, bottom);

        // set drawable
        if (view instanceof TextView) {
            Drawable[] drawables = ((TextView) view).getCompoundDrawables();
            setCompoundDrawablesWithIntrinsicBounds((TextView) view,
                    drawables[0], drawables[1], drawables[2], drawables[3]);

            ((TextView) view)
                    .setCompoundDrawablePadding(getScalePxValue(((TextView) view)
                            .getCompoundDrawablePadding()));
        }

        view.setLayoutParams(scaleParams(view.getLayoutParams()));

    }

    private static void setCompoundDrawablesWithIntrinsicBounds(TextView view,
                                                                Drawable left, Drawable top, Drawable right, Drawable bottom) {

        if (left != null) {
            // left.setBounds(0, 0,
            // ScreenUtil.getScalePxValue(left.getIntrinsicWidth()),
            // ScreenUtil.getScalePxValue(left.getIntrinsicHeight()));
            scaleBoundsDrawable(left);
        }
        if (right != null) {
            // right.setBounds(0, 0,
            // ScreenUtil.getScalePxValue(right.getIntrinsicWidth()),
            // ScreenUtil.getScalePxValue(right.getIntrinsicHeight()));
            scaleBoundsDrawable(right);
        }
        if (top != null) {
            // top.setBounds(0, 0,
            // ScreenUtil.getScalePxValue(top.getIntrinsicWidth()),
            // ScreenUtil.getScalePxValue(top.getIntrinsicHeight()));
            scaleBoundsDrawable(top);
        }
        if (bottom != null) {
            // bottom.setBounds(0, 0,
            // ScreenUtil.getScalePxValue(bottom.getIntrinsicWidth()),
            // ScreenUtil.getScalePxValue(bottom.getIntrinsicHeight()));
            scaleBoundsDrawable(bottom);
        }

        view.setCompoundDrawables(left, top, right, bottom);
    }

    public static Drawable scaleBoundsDrawable(Drawable drawable) {
        drawable.setBounds(0, 0,
                ScreenUtil.getScalePxValue(drawable.getIntrinsicWidth()),
                ScreenUtil.getScalePxValue(drawable.getIntrinsicHeight()));
        return drawable;
    }

    /**
     * Scale the textview's font size
     *
     * @param view
     */
    public static void processTextSizeScale(TextView view) {
        if (view == null)
            return;
        Object isScale = view.getTag(R.id.is_scale_font_size_tag);
        if (isScale instanceof Boolean) {
            if ((Boolean) isScale == true) {
                return;
            }
        }
        float size = view.getTextSize();
        size *= sScale;
        // Size's unit use pixel,so param unit use TypedValue.COMPLEX_UNIT_PX.
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
    }

    /**
     * Scale the textview
     *
     * @param view The scale view
     * @return
     */
    public static void processTextViewScale(TextView view) {
        if (view == null)
            return;

        processScale(view);
        processTextSizeScale(view);
    }

    public static int getScalePxValue(int value) {
        if (value <= 4) {
            return value;
        }
        return (int) Math.ceil(sScale * value);
    }

    public static void initScale(View v) {
        if (v != null) {
            if (v instanceof ViewGroup) {
                scaleViewGroup((ViewGroup) v);
            } else {
                scaleView(v);
            }
        }
    }

    private static void scaleViewGroup(ViewGroup viewGroup) {

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup) {
                scaleViewGroup((ViewGroup) view);
            }
            scaleView(view);
        }

    }

    private static void scaleView(View view) {
        Object isScale = view.getTag(R.id.is_scale_tag);
        if (isScale instanceof Boolean) {
            if ((Boolean) isScale) {
                return;
            }
        }
        if (view instanceof TextView) {
            ScreenUtil.processTextViewScale((TextView) view);
        } else
            processScale(view);
        view.setTag(R.id.is_scale_tag, true);
    }

    /**
     * 获得屏幕高度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    /**
     * 获得屏幕宽度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.heightPixels;
    }

    /**
     * 获得状态栏的高度
     *
     * @param context
     * @return
     */
    private static int statusHeight = -1;

    public static int getStatusHeight(Context context) {
        if (statusHeight > 0) {
            return statusHeight;
        } else {
            try {
                Class<?> clazz = Class.forName("com.android.internal.R$dimen");
                Object object = clazz.newInstance();
                int height = Integer.parseInt(clazz.getField("status_bar_height").get(object).toString());
                statusHeight = context.getApplicationContext().getResources().getDimensionPixelSize(height);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return statusHeight;
        }
    }

    /**获取虚拟功能键高度 */
    public static int getVirtualBarHeight(Context context) {
        int vh=0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    /**
     * 获取当前屏幕截图，包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, 0, width, height);
        view.destroyDrawingCache();
        return bp;

    }

    /**
     * 获取当前屏幕截图，不包含状态栏
     *
     * @param activity
     * @return
     */
    public static Bitmap snapShotWithoutStatusBar(Activity activity) {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;

        int width = getScreenWidth(activity);
        int height = getScreenHeight(activity);
        Bitmap bp = null;
        bp = Bitmap.createBitmap(bmp, 0, statusBarHeight, width, height
                - statusBarHeight);
        view.destroyDrawingCache();
        return bp;

    }

    /**
    * 透明状态栏
    */
    @SuppressLint("InlinedApi")
    public static void initSystemBar(Activity acvitity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 透明状态栏
            acvitity.getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 透明导航栏
            //acvitity.getWindow().addFlags(
            // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
    * 设置状态栏颜色
    */
    public static void setStatusColor(Activity acvitity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                acvitity.getWindow().setStatusBarColor(color);
        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
