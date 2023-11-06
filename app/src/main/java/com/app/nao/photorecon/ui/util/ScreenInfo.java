package com.app.nao.photorecon.ui.util;

import java.lang.reflect.Method;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.View;
// ref: https://qiita.com/a_nishimura/items/f557138b2d67b9e1877c

// 回転にアクティブに対応できるようにMainActivityでは呼ばずに画面呼び出し時に逐次宣言する．
public class ScreenInfo {

    public static Point getDisplaySize(Activity activity){
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        return point;
    }

    public static Point getRealSize(Activity activity) {

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point point = new Point(0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // Android 4.2~
            display.getRealSize(point);
            return point;

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            // Android 3.2~
            try {
                Method getRawWidth = Display.class.getMethod("getRawWidth");
                Method getRawHeight = Display.class.getMethod("getRawHeight");
                int width = (Integer) getRawWidth.invoke(display);
                int height = (Integer) getRawHeight.invoke(display);
                point.set(width, height);
                return point;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return point;
    }

    public static Point getViewSize(View View){
        Point point = new Point(0, 0);
        point.set(View.getWidth(), View.getHeight());

        return point;
    }

}
