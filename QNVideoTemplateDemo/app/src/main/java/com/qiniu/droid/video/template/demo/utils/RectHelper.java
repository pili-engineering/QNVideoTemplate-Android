package com.qiniu.droid.video.template.demo.utils;

import android.graphics.Rect;

public class RectHelper {

    public static void scale(Rect rect, float scaleX, float scaleY, boolean center) {
        if (center) {
            int newHalfWidth = (int) (rect.width() * scaleX / 2);
            int newHalfHeight = (int) (rect.height() * scaleY / 2);
            rect.left = rect.centerX() - newHalfWidth;
            rect.top = rect.centerY() - newHalfHeight;
            rect.right = rect.centerX() + newHalfWidth;
            rect.bottom = rect.centerY() + newHalfHeight;
        } else {
            rect.right = (int) (rect.left + rect.width() * scaleX);
            rect.bottom = (int) (rect.top + rect.height() * scaleY);
        }
    }

}
