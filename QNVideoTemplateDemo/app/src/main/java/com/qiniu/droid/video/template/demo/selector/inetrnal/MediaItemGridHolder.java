package com.qiniu.droid.video.template.demo.selector.inetrnal;

import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;

import java.security.MessageDigest;
import java.text.DecimalFormat;

public class MediaItemGridHolder extends RecyclerView.ViewHolder {

    private OnItemClickListener mListener;
    private final ImageView mIv;
    private final TextView mTvDuration;
    private final CheckBox mCheckBox;

    public MediaItemGridHolder(@NonNull View itemView) {
        super(itemView);
        mIv = itemView.findViewById(R.id.iv_image);
        mTvDuration = itemView.findViewById(R.id.tv_duration);
        mCheckBox = itemView.findViewById(R.id.check_box);
    }

    public void bind(final MediaSelectorItem item) {
        item.position = getAdapterPosition();
        fixItemSize();
        if (item.enableSelection) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(item.selected);
        } else {
            mCheckBox.setVisibility(View.GONE);
        }
        if (item.item instanceof MediaStoreHelper.MediaStorageVideo) {
            mTvDuration.setText(parseDuration(((MediaStoreHelper.MediaStorageVideo) item.item).durationMs));
        }
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
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(item, getAdapterPosition());
                }
            }
        });
    }

    private String parseDuration(long durationMs) {
        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(durationMs / 1000.0) + "s";
    }

    private void fixItemSize() {
        ViewGroup.LayoutParams layoutParams = itemView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
        }
        int width = itemView.getWidth();
        if (width == 0) {
            width = itemView.getResources().getDisplayMetrics().widthPixels / 3;
        }
        layoutParams.height = width;
        itemView.setLayoutParams(layoutParams);
    }

    public void setOnClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
}
