package com.qiniu.droid.video.template.demo.widget.trim.internal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class VideoTrackDrawable {

    VideoTrackInfo mVideoTrackInfo;
    // 视频轨道总空间
    private final Rect mBounds;
    private final List<VideoFrameDrawable> mVideoFrameDrawableList = new ArrayList<>();

    private final Bitmap mPlaceHolder;
    private final WeakReference<View> mView;

    public VideoTrackDrawable(VideoTrackInfo videoTrackInfo, Rect trackBounds, View view) {
        mVideoTrackInfo = videoTrackInfo;
        mBounds = new Rect(trackBounds);
        mView = new WeakReference<>(view);

        Rect mFrameBounds = computeFrameBounds();
        mPlaceHolder = createPlaceHolder(mFrameBounds.width(), mFrameBounds.height());
        int frameNum = mBounds.width() / mFrameBounds.width() + 1;
        long frameRectInterval = mVideoTrackInfo.durationMs / frameNum;
        for (int i = 0; i < frameNum; ++i) {
            Rect bounds = new Rect(mBounds.left + i * mFrameBounds.width(),
                    mBounds.top,
                    mBounds.left + (i + 1) * mFrameBounds.width(),
                    mBounds.bottom);
            VideoFrameDrawable drawable = new VideoFrameDrawable(bounds, i * frameRectInterval, mPlaceHolder);
            mVideoFrameDrawableList.add(drawable);
        }
    }

    public Rect getBounds() {
        return mBounds;
    }

    public void draw(Canvas canvas, Rect bounds) {
        List<VideoFrameDrawable> drawFrameList = findDrawFrameList(bounds);
        for (VideoFrameDrawable drawable : drawFrameList) {
            if (!drawable.isFrameReady()) {
                mVideoTrackInfo.queueLoadingFrame(drawable, mView);
            }
            drawable.draw(canvas);
        }
    }

    private List<VideoFrameDrawable> findDrawFrameList(Rect bounds) {
        List<VideoFrameDrawable> drawableList = new ArrayList<>();
        for (VideoFrameDrawable frameDrawable : mVideoFrameDrawableList) {
            Rect frameBounds = frameDrawable.getBounds();
            if (frameBounds.right >= bounds.left || frameBounds.left <= bounds.right) {
                drawableList.add(frameDrawable);
            }
        }
        return drawableList;
    }

    private Rect computeFrameBounds() {
        float videoRatio = mVideoTrackInfo.width / 1f / mVideoTrackInfo.height;
        if (mVideoTrackInfo.rotation % 180 != 0) {
            videoRatio = mVideoTrackInfo.height / 1f / mVideoTrackInfo.width;
        }
        int frameHeight = mBounds.height();
        int frameWidth = (int) (videoRatio * frameHeight);
        return new Rect(0, 0, frameWidth, frameHeight);
    }

    private Bitmap createPlaceHolder(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint();
        p.setColor(Color.WHITE);
        p.setTextSize(16);
        p.setStrokeWidth(8);
        canvas.drawColor(Color.BLACK);
        canvas.drawText("loading", width / 2f, height / 2f, p);
        return bitmap;
    }
}
