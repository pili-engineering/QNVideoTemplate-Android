package com.qiniu.droid.video.template.demo.selector;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.selector.inetrnal.MediaItemGridAdapter;
import com.qiniu.droid.video.template.demo.selector.inetrnal.MediaSelectorItem;
import com.qiniu.droid.video.template.demo.selector.inetrnal.OnItemClickListener;
import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;

import java.util.List;

public class MediaFileSelectorFragment extends Fragment {

    public static final String K_IS_VIDEO = "K_IS_VIDEO";

    private OnItemClickListener mListener;

    private RecyclerView mRV;
    private MediaItemGridAdapter mAdapter;
    private boolean mIsVideo;

    public static MediaFileSelectorFragment newInstance(boolean isVideo) {
        MediaFileSelectorFragment fragment = new MediaFileSelectorFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(K_IS_VIDEO, isVideo);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsVideo = getArguments().getBoolean(K_IS_VIDEO, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.template_fragment_rv, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRV = view.findViewById(R.id.rv);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new MediaItemGridAdapter();
        mAdapter.setListener(new OnItemClickListener() {
            @Override
            public void onItemClick(MediaSelectorItem item, int position) {
                if (mListener != null) {
                    mListener.onItemClick(item, position);
                }
                if (((MediaFileSelectorActivity) getActivity()).getSelectionNum() > 1) {
                    mAdapter.notifyItemChanged(position);
                }
            }
        });
        mRV.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRV.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                if (position % 3 == 0) {
                    outRect.set(16, 0, 0, 16);
                } else if (position % 3 == 1) {
                    outRect.set(16, 0, 16, 16);
                } else {
                    outRect.set(0, 0, 16, 16);
                }
            }
        });
        mRV.setAdapter(mAdapter);

        new Thread() {
            @Override
            public void run() {
                final List<? extends MediaStoreHelper.MediaStorageBase> itemList;
                if (mIsVideo) {
                    itemList = MediaStoreHelper.queryVideo(getContext(), true);
                } else {
                    itemList = MediaStoreHelper.queryImage(getContext(), true);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setItemList(itemList, ((MediaFileSelectorActivity) getActivity()).getSelectionNum() > 1);
                    }
                });
            }
        }.start();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void notifyDataChanged(int position) {
        if (position != -1) {
            mAdapter.notifyItemChanged(position);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }
}
