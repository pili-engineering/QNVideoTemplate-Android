package com.qiniu.droid.video.template.demo.widget.trim.internal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.media.MediaMetadataRetriever.OPTION_PREVIOUS_SYNC;

public class VideoTrackInfo {

    public final long durationMs;
    public final int width;
    public final int height;
    public final int rotation;

    private final MediaMetadataRetriever mRetriever;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    public VideoTrackInfo(Context context, String videoUri) {
        mRetriever = new MediaMetadataRetriever();
        mRetriever.setDataSource(context, Uri.parse(videoUri));
        durationMs = Long.parseLong(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        width = Integer.parseInt(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        height = Integer.parseInt(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        rotation = Integer.parseInt(mRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
    }

    public void queueLoadingFrame(final VideoFrameRectInfo info, final WeakReference<View> targetView) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (info.bitmap != null) {
                    return;
                }
                Bitmap bitmap = mRetriever.getFrameAtTime(info.ptsMs * 1000, OPTION_PREVIOUS_SYNC);
                if (bitmap != null) {
                    info.bitmap = Bitmap.createScaledBitmap(bitmap, info.srcRect.width(), info.srcRect.height(), false);
                    bitmap.recycle();
                    View view = targetView.get();
                    if (view != null && view.isAttachedToWindow()) {
                        view.postInvalidate();
                    }
                }
            }
        });
    }


    public void queueLoadingFrame(final VideoFrameDrawable drawable, final WeakReference<View> targetView) {
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (drawable.isFrameReady()) {
                    return;
                }
                Bitmap bitmap = mRetriever.getFrameAtTime(drawable.getFramePts() * 1000, OPTION_PREVIOUS_SYNC);
                if (bitmap != null) {
                    Rect frameSize = drawable.getBounds();
                    Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, frameSize.width(), frameSize.height(), false);
                    drawable.setFrameBitmap(scaledBitmap);
                    bitmap.recycle();
                    View view = targetView.get();
                    if (view != null && view.isAttachedToWindow()) {
                        view.postInvalidate();
                    }
                }
            }
        });
    }

    public void release() {
        mRetriever.release();
        mExecutor.shutdownNow();
    }
}
