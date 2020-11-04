package com.qiniu.droid.video.template.demo.selector;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Size;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.acitvity.ImageCropActivity;
import com.qiniu.droid.video.template.demo.acitvity.VideoCropActivity;
import com.qiniu.droid.video.template.demo.selector.inetrnal.MediaItemSelectedAdapter;
import com.qiniu.droid.video.template.demo.selector.inetrnal.MediaSelectorItem;
import com.qiniu.droid.video.template.demo.selector.inetrnal.OnItemClickListener;
import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;
import com.qiniu.droid.video.template.demo.utils.UriUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import static androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT;

public class MediaFileSelectorActivity extends AppCompatActivity {

    public static final int K_REQUEST_CODE_IMAGE = 100;
    public static final int K_REQUEST_CODE_VIDEO = 101;
    public static final String K_SELECTION_NUM = "K_SELECTION_NUM";
    public static final String K_SELECTION_SIZE = "K_SELECTION_SIZE";
    public static final String K_SELECTION_DURATION = "K_SELECTION_DURATION";
    public static final String K_RESULT = "K_RESULT";
    private TextView mTvCancel;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private LinearLayout mLLMulti;
    private RecyclerView mRVSelected;
    private TextView mTvMultiTip;
    private TextView mTvNext;
    private MediaItemSelectedAdapter mAdapter;
    private MediaFileSelectorFragment mImageFragment;
    private MediaFileSelectorFragment mVideoFragment;
    private int mSelectionNum;
    private ArrayList<Size> mSelectionSizeList;
    private ArrayList<Long> mSelectionDurationList;
    private final ArrayList<MediaSelectorItem> mSelectedItemList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_media_file_selector);

        mSelectionNum = getIntent().getIntExtra(K_SELECTION_NUM, 1);
        mSelectionSizeList = (ArrayList<Size>) getIntent().getSerializableExtra(K_SELECTION_SIZE);
        mSelectionDurationList = (ArrayList<Long>) getIntent().getSerializableExtra(K_SELECTION_DURATION);

        mTvCancel = findViewById(R.id.tv_cancel);
        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager = findViewById(R.id.view_pager);
        mLLMulti = findViewById(R.id.ll_multi);
        mRVSelected = findViewById(R.id.rv_selected);
        mTvMultiTip = findViewById(R.id.tv_multi_tip);
        mTvNext = findViewById(R.id.tv_next);

        if (mSelectionNum > 1) {
            mLLMulti.setVisibility(View.VISIBLE);
            mTvMultiTip.setText("推荐选择" + mSelectionNum + "张素材");
            mTvNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent result = new Intent();
                    result.putExtra(K_RESULT, mSelectedItemList);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
            mAdapter = new MediaItemSelectedAdapter();
            mAdapter.setListener(new OnItemClickListener() {
                @Override
                public void onItemClick(MediaSelectorItem item, int what) {
                    if (what == 0) {
                        // todo edit
                    } else if (what == 1) {
                        item.selected = false;
                        mSelectedItemList.remove(item);
                        if (item.item instanceof MediaStoreHelper.MediaStorageImage) {
                            mImageFragment.notifyDataChanged(item.position);
                        } else {
                            mVideoFragment.notifyDataChanged(item.position);
                        }
                        mAdapter.setItemList(mSelectedItemList);
                    }
                }
            });
            mRVSelected.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
            mRVSelected.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    outRect.set(16, 0, 0, 0);
                }
            });
            mRVSelected.setAdapter(mAdapter);
        } else {
            mLLMulti.setVisibility(View.GONE);
        }

        mImageFragment = MediaFileSelectorFragment.newInstance(false);
        mImageFragment.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(MediaSelectorItem item, int position) {
                if (mSelectionNum > 1) {
                    // 判断是否超过最大个数
                    item.selected = !item.selected;
                    if (item.selected && mSelectedItemList.size() == mSelectionNum) {
                        item.selected = false;
                        Toast.makeText(MediaFileSelectorActivity.this, "最多可选择" + mSelectionNum + "个", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (item.selected) {
                        mSelectedItemList.add(item);
                    } else {
                        mSelectedItemList.remove(item);
                    }
                    mAdapter.setItemList(mSelectedItemList);
                } else {
                    // 裁剪
                    Intent intent = new Intent(MediaFileSelectorActivity.this, ImageCropActivity.class);
                    intent.putExtra(ImageCropActivity.K_IMAGE_CROP_URI, item.item.uri);
                    intent.putExtra(ImageCropActivity.K_IMAGE_CROP_WIDTH, mSelectionSizeList.get(0).getWidth());
                    intent.putExtra(ImageCropActivity.K_IMAGE_CROP_HEIGHT, mSelectionSizeList.get(0).getHeight());
                    startActivityForResult(intent, K_REQUEST_CODE_IMAGE);
                }
            }
        });

        mVideoFragment = MediaFileSelectorFragment.newInstance(true);
        mVideoFragment.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(MediaSelectorItem item, int position) {
                if (mSelectionNum > 1) {
                    item.selected = !item.selected;
                    if (item.selected && mSelectedItemList.size() == mSelectionNum) {
                        item.selected = false;
                        Toast.makeText(MediaFileSelectorActivity.this, "最多可选择" + mSelectionNum + "个", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (item.selected) {
                        mSelectedItemList.add(item);
                    } else {
                        mSelectedItemList.remove(item);
                    }
                    mAdapter.setItemList(mSelectedItemList);
                } else {
                    long durationMs = ((MediaStoreHelper.MediaStorageVideo) item.item).durationMs;
                    if (durationMs <= mSelectionDurationList.get(0)) {
                        File file = UriUtils.copyToCacheDir(MediaFileSelectorActivity.this, item.item.uri);
                        if (file == null) {
                            finish();
                            return;
                        }
                        // 返回当前裁剪的视频路径
                        Intent result = new Intent();
                        result.putExtra(K_RESULT, new MediaSelectorResult(MediaSelectorResult.Type.VIDEO, file.getAbsolutePath()));
                        setResult(RESULT_OK, result);
                        finish();
                    } else {
                        // trim
                        Intent intent = new Intent(MediaFileSelectorActivity.this, VideoCropActivity.class);
                        intent.putExtra(VideoCropActivity.K_VIDEO_CROP_URI, item.item.uri);
                        intent.putExtra(VideoCropActivity.K_VIDEO_CROP_DURATION, mSelectionDurationList.get(0));
                        startActivityForResult(intent, K_REQUEST_CODE_VIDEO);
                    }
                }
            }
        });

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager(), BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return mImageFragment;
                } else {
                    return mVideoFragment;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return "图片";
                } else {
                    return "视频";
                }
            }
        });
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == K_REQUEST_CODE_IMAGE) {
            if (resultCode == RESULT_OK) {
                // 返回当前裁剪的图片路径
                String path = data.getStringExtra(ImageCropActivity.K_IMAGE_CROP_RESULT_PATH);
                Intent result = new Intent();
                result.putExtra(K_RESULT, new MediaSelectorResult(MediaSelectorResult.Type.IMAGE, path));
                setResult(RESULT_OK, result);
                finish();
            }
        } else if (requestCode == K_REQUEST_CODE_VIDEO) {
            if (resultCode == RESULT_OK) {
                // 返回当前裁剪的视频路径
                String path = data.getStringExtra(VideoCropActivity.K_VIDEO_CROP_RESULT_PATH);
                Intent result = new Intent();
                result.putExtra(K_RESULT, new MediaSelectorResult(MediaSelectorResult.Type.VIDEO, path));
                setResult(RESULT_OK, result);
                finish();
            }
        }
    }

    public int getSelectionNum() {
        return mSelectionNum;
    }

    public static class MediaSelectorResult implements Serializable {
        public final Type type;
        public final String path;
        public MediaSelectorResult(Type type, String path) {
            this.type = type;
            this.path = path;
        }

        public enum Type {
            IMAGE,
            VIDEO
        }
    }

}
