package com.qiniu.droid.video.template.demo.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.FillType;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;

import com.qiniu.droid.video.template.demo.utils.RectHelper;

import java.io.FileNotFoundException;

public final class ImageCropViewFixArea extends View {

    // view
    private final Rect mViewRect = new Rect();
    // crop
    private final Rect mCropAreaRect = new Rect();
    private final Path mCropAreaMaskPath = new Path();
    private final Paint mCropPaint = new Paint();
    // image
    private final Rect mImageSrcRect = new Rect();
    private final Rect mImageScaledRect = new Rect();
    private final Rect mImageDrawRect = new Rect();
    private final Paint mImagePaint = new Paint();
    // crop area margin
    private final int mCropAreaMarginVertical = 48;
    private final int mCropAreaMarginHorizontal = 48;
    // scale factor
    private final float mMinScaleFactor = 1f;
    private final float mMaxScaleFactor = 5f;
    // color
    private float mCropStrokeWidth = 8f;
    private int mCropStrokeColor = Color.RED;
    private int mCropMaskColor = Color.TRANSPARENT;
    private float mCropRatio = 1f;
    private Bitmap mImageBitmap;
    private boolean mRefresh = false;
    // scale and offset
    private float mImageScale = 1f;
    private int mImageOffsetX = 0;
    private int mImageOffsetY = 0;
    // detector
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector mScrollDetector;
    public ImageCropViewFixArea(Context context) {
        this(context, null);
    }

    public ImageCropViewFixArea(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageCropViewFixArea(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new SimpleOnScaleGestureListener() {
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            public boolean onScale(ScaleGestureDetector detector) {
                float newScale = mImageScale * detector.getScaleFactor();

                if (newScale <= mMinScaleFactor) {
                    newScale = mMinScaleFactor;
                } else if (newScale >= mMaxScaleFactor) {
                    newScale = mMaxScaleFactor;
                }

                Rect rect = new Rect(mImageScaledRect);
                RectHelper.scale(rect, newScale, newScale, true);
                rect.offset(mImageOffsetX, mImageOffsetY);
                int newOffsetX = mImageOffsetX;
                int newOffsetY = mImageOffsetY;
                if (rect.left > mCropAreaRect.left) {
                    newOffsetX -= rect.left - mCropAreaRect.left;
                    rect.offset(-mImageOffsetX, 0);
                    rect.offset(newOffsetX, 0);
                } else if (rect.right < mCropAreaRect.right) {
                    newOffsetX += mCropAreaRect.right - rect.right;
                    rect.offset(-mImageOffsetX, 0);
                    rect.offset(newOffsetX, 0);
                }

                if (rect.top > mCropAreaRect.top) {
                    newOffsetY -= rect.top - mCropAreaRect.top;
                    rect.offset(0, -mImageOffsetY);
                    rect.offset(0, newOffsetY);
                } else if (rect.bottom < mCropAreaRect.bottom) {
                    newOffsetY += mCropAreaRect.bottom - rect.bottom;
                    rect.offset(0, -mImageOffsetY);
                    rect.offset(0, newOffsetY);
                }

                if (rect.contains(mCropAreaRect)) {
                    mImageScale = newScale;
                    mImageOffsetX = newOffsetX;
                    mImageOffsetY = newOffsetY;
                    postInvalidate();
                }
                return true;
            }
        });

        mScrollDetector = new GestureDetector(context, (OnGestureListener) (new SimpleOnGestureListener() {
            public boolean onDown(MotionEvent e) {
                return true;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                int newOffsetX = (int) ((float) mImageOffsetX - distanceX);
                int newOffsetY = (int) ((float) mImageOffsetY - distanceY);
                Rect rect = new Rect(mImageScaledRect);
                RectHelper.scale(rect, mImageScale, mImageScale, true);
                rect.offset(newOffsetX, newOffsetY);
                if (rect.left < mCropAreaRect.left && rect.right > mCropAreaRect.right) {
                    mImageOffsetX = newOffsetX;
                }

                if (rect.top < mCropAreaRect.top && rect.bottom > mCropAreaRect.bottom) {
                    mImageOffsetY = newOffsetY;
                }

                postInvalidate();
                return true;
            }
        }));
    }

    public final void setImagePath(String imagePath, float cropRatio) {
        mCropRatio = cropRatio;
        mImageBitmap = BitmapFactory.decodeFile(imagePath);
        if (mImageBitmap == null) {
            Log.e(getClass().getSimpleName(), "setImagePath, decodeFile fail");
            return;
        }
        mRefresh = true;
        requestLayout();
    }

    public final void setImageUri(Uri imageUri, float cropRatio) {
        mCropRatio = cropRatio;
        try {
            mImageBitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(imageUri));
        } catch (FileNotFoundException e) {
            Log.e(getClass().getSimpleName(), "setImageUri, decodeStream fail", e);
            return;
        }
        mRefresh = true;
        requestLayout();
    }

    public final void setCropAreaProp(float strokeWidth, int strokeColor, int maskColor) {
        mCropStrokeWidth = strokeWidth;
        mCropStrokeColor = strokeColor;
        mCropMaskColor = maskColor;
        mRefresh = true;
        requestLayout();
    }

    public final ImageCropViewFixArea.CropResult getCropResult() {
        Rect fullRect = new Rect(mImageSrcRect);
        Rect cropRect = new Rect();
        Rect imageScaledRect = new Rect(mImageScaledRect);
        RectHelper.scale(imageScaledRect, mImageScale, mImageScale, true);
        imageScaledRect.offset(mImageOffsetX, mImageOffsetY);
        Rect imageCropRect = new Rect(mCropAreaRect);
        int distanceX = imageScaledRect.left;
        int distanceY = imageScaledRect.top;
        imageScaledRect.offset(-distanceX, -distanceY);
        imageCropRect.offset(-distanceX, -distanceY);
        float ratio = (float) imageScaledRect.width() / 1.0F / (float) fullRect.width();
        cropRect.set((int) ((float) imageCropRect.left / ratio), (int) ((float) imageCropRect.top / ratio), (int) ((float) imageCropRect.right / ratio), (int) ((float) imageCropRect.bottom / ratio));
        return new ImageCropViewFixArea.CropResult(fullRect, cropRect);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if ((mRefresh || !new Rect(0, 0, getMeasuredWidth(), getMeasuredHeight()).equals(mViewRect)) && mImageBitmap != null) {
            mViewRect.set(0, 0, getMeasuredWidth(), getMeasuredHeight());
            mRefresh = false;
            float viewRatio = (float) getMeasuredWidth() / 1.0F / (float) getMeasuredHeight();
            int cropWidth;
            int cropHeight;
            if (mCropRatio > viewRatio) {
                cropWidth = getMeasuredWidth() - mCropAreaMarginHorizontal * 2;
                cropHeight = (int) ((float) cropWidth / mCropRatio);
                mCropAreaRect.set(mCropAreaMarginHorizontal, (getMeasuredHeight() - cropHeight) / 2, getMeasuredWidth() - mCropAreaMarginHorizontal, getMeasuredHeight() - (getMeasuredHeight() - cropHeight) / 2);
            } else {
                cropWidth = getMeasuredHeight() - mCropAreaMarginVertical * 2;
                cropHeight = (int) (mCropRatio * (float) cropWidth);
                mCropAreaRect.set((getMeasuredWidth() - cropHeight) / 2, mCropAreaMarginVertical, getMeasuredWidth() - (getMeasuredWidth() - cropHeight) / 2, getMeasuredHeight() - mCropAreaMarginVertical);
            }

            mCropAreaMaskPath.reset();
            mCropAreaMaskPath.addRect(new RectF(0.0F, 0.0F, (float) getMeasuredWidth(), (float) getMeasuredHeight()), Direction.CW);
            mCropAreaMaskPath.addRect(new RectF(mCropAreaRect), Direction.CW);
            mCropAreaMaskPath.setFillType(FillType.EVEN_ODD);
            mCropAreaMaskPath.close();
            Bitmap bitmap = mImageBitmap;
            if (bitmap != null) {
                mImageSrcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
                float imageRatio = (float) bitmap.getWidth() / 1.0F / (float) bitmap.getHeight();
                int minImageHeight;
                if (mCropRatio > imageRatio) {
                    minImageHeight = (int) ((float) mCropAreaRect.width() / imageRatio);
                    mImageScaledRect.set(mCropAreaRect.left, mCropAreaRect.top - (minImageHeight - mCropAreaRect.height()) / 2, mCropAreaRect.right, mCropAreaRect.bottom + (minImageHeight - mCropAreaRect.height()) / 2);
                } else {
                    minImageHeight = (int) (imageRatio * (float) mCropAreaRect.height());
                    mImageScaledRect.set(mCropAreaRect.left - (minImageHeight - mCropAreaRect.width()) / 2, mCropAreaRect.top, mCropAreaRect.right + (minImageHeight - mCropAreaRect.width()) / 2, mCropAreaRect.bottom);
                }
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap bitmap = mImageBitmap;
        if (bitmap != null) {
            // scale and offset
            mImageDrawRect.set(mImageScaledRect);
            RectHelper.scale(mImageDrawRect, mImageScale, mImageScale, true);
            mImageDrawRect.offset(mImageOffsetX, mImageOffsetY);
            // draw bitmap
            canvas.drawBitmap(bitmap, mImageSrcRect, mImageDrawRect, mImagePaint);
            // draw crop mask
            mCropPaint.setColor(mCropMaskColor);
            mCropPaint.setStyle(Style.FILL);
            canvas.drawPath(mCropAreaMaskPath, mCropPaint);
            // draw crop rect
            mCropPaint.setColor(mCropStrokeColor);
            mCropPaint.setStrokeWidth(mCropStrokeWidth);
            mCropPaint.setStyle(Style.STROKE);
            canvas.drawRect(mCropAreaRect, mCropPaint);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleDetector.onTouchEvent(event);
        mScrollDetector.onTouchEvent(event);
        return true;
    }

    public static final class CropResult {
        public final Rect full;
        public final Rect crop;

        public CropResult(Rect full, Rect crop) {
            this.full = full;
            this.crop = crop;
        }

        @Override
        public String toString() {
            return "CropResult{" +
                    "full=" + full.toShortString() +
                    ", crop=" + crop.toShortString() +
                    '}';
        }
    }
}
