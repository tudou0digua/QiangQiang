package com.cb.qiangqiang.util;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Created by cb on 2016/1/7.
 */
public class Utils {
    public static void initStatusAfterSetContentView(Activity activity, View top) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            int statusBarHeight = getStatusBarHeight(activity);
            top.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(
                "status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 是否显示系统软键盘
     *
     * @param isHideKeyboard
     * @param context
     */
    public static void hideSystemSofeKeyboard(boolean isHideKeyboard, Context context, View view) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (isHideKeyboard) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                if (view != null) {
                    ((EditText) view).setRawInputType(EditorInfo.TYPE_NULL);
                }
            } else {
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            manager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            //                manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

}
