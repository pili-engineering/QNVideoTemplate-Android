package com.qiniu.droid.video.template.demo.widget.trim.internal;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class VideoFrameRectInfo {

    public final Rect dstRect;
    public final long ptsMs;
    public final long durationMs;
    public Rect srcRect;
    public Bitmap bitmap;

    public VideoFrameRectInfo(Rect srcRect, Rect dstRect, long ptsMs, long durationMs) {
        this.srcRect = srcRect;
        this.dstRect = dstRect;
        this.ptsMs = ptsMs;
        this.durationMs = durationMs;
    }

    public void release() {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

}
