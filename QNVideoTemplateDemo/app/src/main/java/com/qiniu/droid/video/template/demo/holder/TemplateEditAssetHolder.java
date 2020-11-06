package com.qiniu.droid.video.template.demo.holder;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.qiniu.droid.video.template.QNVTAssetProperty;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.adapter.TemplateEditAssetAdapter;
import com.qiniu.droid.video.template.demo.model.RelatedAssetGroup;

import java.security.MessageDigest;
import java.text.DecimalFormat;

public class TemplateEditAssetHolder extends RecyclerView.ViewHolder {

    private final FrameLayout mFLHeader;
    private final FrameLayout mFLText;
    private final FrameLayout mFLImageVideo;
    private final TextView mTvAssetText;
    private final ImageView mIvAssetImage;
    private final TextView mTvAssetType;
    private final TextView mTvTemplateInPoint;

    private TemplateEditAssetAdapter.OnItemClickListener mListener;

    private final DecimalFormat timestampFormat = new DecimalFormat("0.0");

    public TemplateEditAssetHolder(@NonNull View itemView) {
        super(itemView);
        mFLHeader = itemView.findViewById(R.id.fl_header);
        mFLText = itemView.findViewById(R.id.fl_text);
        mFLImageVideo = itemView.findViewById(R.id.fl_iamge_video);
        mTvAssetText = itemView.findViewById(R.id.tv_asset);
        mIvAssetImage = itemView.findViewById(R.id.iv_image_asset);
        mTvAssetType = itemView.findViewById(R.id.tv_asset_type);
        mTvTemplateInPoint = itemView.findViewById(R.id.tv_in_point);
    }

    public void bind(final Object obj) {
        mFLHeader.setVisibility(View.INVISIBLE);
        mFLText.setVisibility(View.INVISIBLE);
        mFLImageVideo.setVisibility(View.INVISIBLE);
        mTvTemplateInPoint.setVisibility(View.INVISIBLE);
        if (obj instanceof String) {
            // header
            mFLHeader.setVisibility(View.VISIBLE);
        } else {
            RelatedAssetGroup assetGroup = (RelatedAssetGroup) obj;
            mTvTemplateInPoint.setVisibility(View.VISIBLE);
            mTvTemplateInPoint.setText(timestampFormat.format(assetGroup.firstInPoint) + "s");
            if (assetGroup.assetType == QNVTAssetProperty.TYPE_TEXT) {
                // text
                mFLText.setVisibility(View.VISIBLE);
                mTvAssetText.setText(assetGroup.assetList.get(0).text);
            } else if (assetGroup.assetType == QNVTAssetProperty.TYPE_IMAGE || assetGroup.assetType == QNVTAssetProperty.TYPE_VIDEO) {
                mFLImageVideo.setVisibility(View.VISIBLE);
                mTvAssetType.setText(assetGroup.assetType == QNVTAssetProperty.TYPE_IMAGE ? "图片" : "视频");
                Glide.with(itemView).load(assetGroup.assetList.get(0).path)
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
                        .into(mIvAssetImage);
            }
        }

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(obj);
                }
            }
        });
    }

    public void setOnItemClickListener(TemplateEditAssetAdapter.OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }
}
