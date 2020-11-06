package com.qiniu.droid.video.template.demo.widget.trim.internal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.RegionIterator;
import android.view.View;

import com.qiniu.droid.video.template.demo.R;

public class VideoTrimDrawable {

    private final Region mMaskRegion;
    private final Rect mBorder;
    private final Paint mMaskPaint;
    private final Paint mBorderPaint;

    public VideoTrimDrawable(View view) {
        mBorder = new Rect(view.getMeasuredWidth() / 4, 0, view.getMeasuredWidth() / 4 * 3, view.getMeasuredHeight());
        mMaskRegion = new Region();
        mMaskRegion.set(new Rect(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight()));
        mMaskRegion.op(new Rect(mBorder), Region.Op.XOR);

        mMaskPaint = new Paint();
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(view.getResources().getColor(R.color.template_colorBackgroundBlackTransparent));

        mBorderPaint = new Paint();
        mBorderPaint.setStrokeWidth(6);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(view.getResources().getColor(R.color.template_colorPrimary));
    }

    public Rect getBounds() {
        return mBorder;
    }

    public void draw(Canvas canvas) {
        RegionIterator regionIterator = new RegionIterator(mMaskRegion);
        Rect mask = new Rect();
        while (regionIterator.next(mask)) {
            canvas.drawRect(mask, mMaskPaint);
        }

        canvas.drawRect(mBorder, mBorderPaint);
    }

}
