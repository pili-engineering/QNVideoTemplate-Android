package com.qiniu.droid.video.template.demo.selector.inetrnal;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.util.Util;

import java.nio.ByteBuffer;
import java.security.MessageDigest;

public class GlideCenterCropRoundCornerTransform extends BitmapTransformation {

    private static final String ID = "GlideCenterCropRoundCornerTransform";
    private static final byte[] ID_BYTES  = "com.bumptech.glide.load.resource.bitmap.RoundedCorners".getBytes(CHARSET);
    private final int mCorner;

    public GlideCenterCropRoundCornerTransform(int corner){
        mCorner = corner;
    }

    @Override
    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        Bitmap bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
        return TransformationUtils.roundedCorners(pool, bitmap, mCorner);
    }

    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
        byte[] radiusData = ByteBuffer.allocate(4).putInt(mCorner).array();
        messageDigest.update(radiusData);
    }

    @Override
    public int hashCode() {
        return Util.hashCode(ID.hashCode(), Util.hashCode(mCorner));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof GlideCenterCropRoundCornerTransform) {
            GlideCenterCropRoundCornerTransform other = (GlideCenterCropRoundCornerTransform) obj;
            return mCorner == other.mCorner;
        }
        return false;
    }

}
