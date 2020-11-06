package com.qiniu.droid.video.template.demo.widget.trim.internal;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

public class VideoFrameDrawable {

    private final Rect mBounds;
    private final long mPtsMs;
    private BitmapDrawable mFrameDrawable;
    private final BitmapDrawable mPlaceHolderDrawable;

    public VideoFrameDrawable(Rect bounds, long ptsMs, Bitmap placeHolder) {
        mBounds = bounds;
        mPtsMs = ptsMs;
        mPlaceHolderDrawable = new BitmapDrawable(Resources.getSystem(), placeHolder);
        mPlaceHolderDrawable.setBounds(mBounds);
    }

    public boolean isFrameReady() {
        return mFrameDrawable != null;
    }

    public long getFramePts() {
        return mPtsMs;
    }

    public Rect getBounds() {
        return mBounds;
    }

    public void setFrameBitmap(Bitmap frameBitmap) {
        mFrameDrawable = new BitmapDrawable(Resources.getSystem(), frameBitmap);
        mFrameDrawable.setBounds(mBounds);
    }

    public void draw(Canvas canvas) {
        if (mFrameDrawable == null) {
            mPlaceHolderDrawable.draw(canvas);
        } else {
            mFrameDrawable.draw(canvas);
        }
    }
}
