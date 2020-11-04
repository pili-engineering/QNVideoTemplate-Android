package com.qiniu.droid.video.template.demo.widget.trim;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.qiniu.droid.video.template.demo.widget.trim.internal.VideoTrackDrawable;
import com.qiniu.droid.video.template.demo.widget.trim.internal.VideoTrackInfo;
import com.qiniu.droid.video.template.demo.widget.trim.internal.VideoTrimDrawable;


/**
 * 视频轨道操作及展示
 */
public class VideoTrackTrimView extends View {

    /**
     * 加速度爆发后持续滚动多一段距离的Handler
     */
    private final int SRCOLL_MESSAGE = 501;
    private long mTrimDurationMs;
    private boolean mReCompute;

    // 视频轨道信息
    private VideoTrackInfo mVideoTrackInfo;

    private int mCurrentPosition;

    private final Paint mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private GestureDetector mScrollDetector;
    private OnScrollStateListener mScrollListener;

    private VideoTrackDrawable mTrackDrawable;
    private VideoTrimDrawable mTrimDrawable;
    private int mMinScrollX;
    private int mMaxScrollX;
    private final SrcollHandler mSrcollHandler = new SrcollHandler();

    public VideoTrackTrimView(Context context) {
        this(context, null);
    }

    public VideoTrackTrimView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoTrackTrimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mScrollDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChange(true);
                }
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                float newScrollX = getScrollX() + distanceX;
                if (newScrollX < mMinScrollX) {
                    updateScroll(mMinScrollX);
                    return false;
                } else if (newScrollX > mMaxScrollX) {
                    updateScroll(mMaxScrollX);
                    return false;
                }
                updateScroll((int) newScrollX);
                return true;
            }

//            @Override
//            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                if (Math.abs(velocityX) > 1000) {
//                    Message message = mSrcollHandler.obtainMessage();
//                    int xVelocity = (int) velocityX;
//                    if (xVelocity > 8000) {//限制速率，不至于太快或太慢
//                        xVelocity = 8000;
//                    } else if (xVelocity < -8000) {
//                        xVelocity = -8000;
//                    }
//                    message.arg1 = xVelocity;
//                    message.what = SRCOLL_MESSAGE;
//                    mSrcollHandler.sendMessage(message);
//                    return true;
//                }
//
//                if(mScrollListener != null){
//                    mScrollListener.onScrollStateChange(true);
//                }
//                return true;
//            }
        });

        mIndicatorPaint.setColor(Color.WHITE);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setStrokeWidth(8);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 计算整条 track 的位置信息
        if (mReCompute) {
            mReCompute = false;

            mTrimDrawable = new VideoTrimDrawable(this);

            final int trackWidth = (int) (mTrimDrawable.getBounds().width() * (mVideoTrackInfo.durationMs / 1f / mTrimDurationMs));
            Rect videoTrackRect = new Rect(0, 0, trackWidth, getMeasuredHeight());
            mTrackDrawable = new VideoTrackDrawable(mVideoTrackInfo, videoTrackRect, this);

            // 最大以及最小滚动位置
            mMinScrollX = 0;
            mMaxScrollX = mTrackDrawable.getBounds().width() - mTrimDrawable.getBounds().width();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (mVideoTrackInfo == null) {
            return;
        }

        // draw frame
        canvas.save();
        canvas.translate(getMeasuredWidth() / 4f, 0);
        mTrackDrawable.draw(canvas, new Rect(getScrollX(), 0, getScrollX() + getMeasuredWidth(), getMeasuredHeight()));
        canvas.restore();

        // draw trim
        canvas.save();
        canvas.translate(getScrollX(), 0);
        mTrimDrawable.draw(canvas);
        canvas.restore();

        // 绘制 indicator
        canvas.save();
        canvas.translate(getScrollX() + getMeasuredWidth() / 4f, 0);
        canvas.drawLine(mCurrentPosition, 0, mCurrentPosition, getMeasuredHeight(), mIndicatorPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean ret = mScrollDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mScrollListener != null) {
                mScrollListener.onScrollStateChange(false);
            }
        }
        return ret;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVideoTrackInfo != null) {
            mVideoTrackInfo.release();
            mReCompute = true;
        }
    }

    public void setVideo(VideoTrackInfo videoTrackInfo, long trimDurationMs) {
        mVideoTrackInfo = videoTrackInfo;
        mTrimDurationMs = trimDurationMs;
        mReCompute = true;
        postInvalidate();
    }

    public Pair<Long, Long> getTrimTime() {
        if (mTrackDrawable == null) {
            return null;
        }
        // 返回裁剪时间
        int startX = getScrollX();
        long startTs = (long) (startX / 1f / mTrackDrawable.getBounds().width() * mVideoTrackInfo.durationMs);
        return new Pair<>(startTs, startTs + mTrimDurationMs);
    }

    public void setCurrentPlayingTime(long ts) {
        mCurrentPosition = (int) (ts / 1f / mTrimDurationMs * mTrimDrawable.getBounds().width());
        postInvalidate();
    }

    public void resetCurrentPlayingTime() {
        mCurrentPosition = 0;
        postInvalidate();
    }

    public void setScrollStateListener(OnScrollStateListener listener) {
        mScrollListener = listener;
    }

    private void updateScroll(int newScrollX) {
        int oldScrollX = getScrollX();
        scrollTo(newScrollX, 0);
        if (mScrollListener != null) {
            mScrollListener.onScrollChange(newScrollX, oldScrollX);
        }
    }
    public interface OnScrollStateListener {
        void onScrollChange(int scrollX, int oldScrollX);

        void onScrollStateChange(boolean start);
    }

    class SrcollHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int srcollVt = msg.arg1;
            int newScrollX = getScrollX() + (-srcollVt / 100);
            if (newScrollX < mMinScrollX) {
                updateScroll(mMinScrollX);
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChange(false);
                }
                return;
            } else if (newScrollX > mMaxScrollX) {
                updateScroll(mMaxScrollX);
                if (mScrollListener != null) {
                    mScrollListener.onScrollStateChange(false);
                }
                return;
            }
            updateScroll(newScrollX);
            invalidate();
            if (srcollVt < 0) {//左划
                Message message = mSrcollHandler.obtainMessage();
                message.arg1 = srcollVt + 100;
                message.what = SRCOLL_MESSAGE;
                if (srcollVt + 100 < -500) {
                    mSrcollHandler.sendMessageDelayed(message, 5);
                } else {
                }
            } else if (srcollVt > 0) {//右划
                Message message = mSrcollHandler.obtainMessage();
                message.arg1 = srcollVt - 100;
                message.what = SRCOLL_MESSAGE;
                if (srcollVt - 100 > 500) {
                    mSrcollHandler.sendMessageDelayed(message, 5);
                }
            }
        }
    }
}
