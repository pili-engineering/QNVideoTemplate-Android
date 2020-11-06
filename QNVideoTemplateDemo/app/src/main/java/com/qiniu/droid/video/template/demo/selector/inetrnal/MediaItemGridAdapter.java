package com.qiniu.droid.video.template.demo.selector.inetrnal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;

import java.util.ArrayList;
import java.util.List;

public class MediaItemGridAdapter extends RecyclerView.Adapter<MediaItemGridHolder> {
    private final List<MediaSelectorItem> mSelectorItemList = new ArrayList<>();
    private OnItemClickListener mListener;

    public void setListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setItemList(List<? extends MediaStoreHelper.MediaStorageBase> mediaItemList, boolean multi) {
        mSelectorItemList.clear();
        for (MediaStoreHelper.MediaStorageBase mediaItem : mediaItemList) {
            mSelectorItemList.add(new MediaSelectorItem(mediaItem, multi));
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MediaItemGridHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.template_holder_media_item_grid, null);
        return new MediaItemGridHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaItemGridHolder holder, int position) {
        holder.bind(mSelectorItemList.get(position));
        holder.setOnClickListener(mListener);
    }

    @Override
    public int getItemCount() {
        return mSelectorItemList.size();
    }
}
