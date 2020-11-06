package com.qiniu.droid.video.template.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.holder.TemplateEditAssetHolder;
import com.qiniu.droid.video.template.demo.model.RelatedAssetGroup;

import java.util.ArrayList;
import java.util.List;

public class TemplateEditAssetAdapter extends RecyclerView.Adapter<TemplateEditAssetHolder> {

    private final List<Object> mAssetList;
    private OnItemClickListener mOnItemClickListener;
    public TemplateEditAssetAdapter() {
        mAssetList = new ArrayList<>();
    }

    public void setAssetList(List<RelatedAssetGroup> assetList) {
        mAssetList.clear();
        if (!assetList.isEmpty()) {
            mAssetList.addAll(assetList);
        }
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TemplateEditAssetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_holder_template_edit_asset, parent, false);
        return new TemplateEditAssetHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateEditAssetHolder holder, int position) {
        holder.bind(mAssetList.get(position));
        holder.setOnItemClickListener(mOnItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mAssetList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Object asset);
    }

}
