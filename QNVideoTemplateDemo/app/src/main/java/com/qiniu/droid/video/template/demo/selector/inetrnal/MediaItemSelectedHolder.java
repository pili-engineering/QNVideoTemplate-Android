package com.qiniu.droid.video.template.demo.selector.inetrnal;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.qiniu.droid.video.template.demo.R;

import java.security.MessageDigest;

public class MediaItemSelectedHolder extends RecyclerView.ViewHolder {

    private OnItemClickListener mListener;

    private final ImageView mIv;
    private final ImageView mIvDelete;

    public MediaItemSelectedHolder(@NonNull View itemView) {
        super(itemView);
        mIv = itemView.findViewById(R.id.iv_image);
        mIvDelete = itemView.findViewById(R.id.iv_delete);
    }

    public void bind(final MediaSelectorItem item) {
        Glide.with(itemView).load(item.item.uri)
                .transform(new BitmapTransformation() {
                    @Override
                    protected Bitmap transform(@NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
                        Bitmap bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
                        return TransformationUtils.roundedCorners(pool, bitmap, 16);
                    }

                    @Override
                    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
                    }
                })
                .into(mIv);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(item, 0);
                }
            }
        });
        mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(item, 1);
                }
            }
        });
    }

    public void setOnClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

}
