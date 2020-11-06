package com.qiniu.droid.video.template.demo.acitvity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.droid.video.template.QNVTAssetProperty;
import com.qiniu.droid.video.template.QNVTExportCallback;
import com.qiniu.droid.video.template.QNVTPlayerCallback;
import com.qiniu.droid.video.template.QNVTTemplateEditor;
import com.qiniu.droid.video.template.QNVTVideoSetting;
import com.qiniu.droid.video.template.demo.Config;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.adapter.TemplateEditAssetAdapter;
import com.qiniu.droid.video.template.demo.adapter.TemplateEditAudioAdapter;
import com.qiniu.droid.video.template.demo.model.MusicSelectorItem;
import com.qiniu.droid.video.template.demo.model.RelatedAssetGroup;
import com.qiniu.droid.video.template.demo.model.Template;
import com.qiniu.droid.video.template.demo.selector.MediaFileSelectorActivity;
import com.qiniu.droid.video.template.demo.utils.TemplateHelper;
import com.qiniu.droid.video.template.demo.widget.EditTextDialog;
import com.qiniu.droid.video.template.demo.widget.ExportParamsDialog;
import com.qiniu.droid.video.template.demo.widget.ProgressDialog;
import com.qiniu.droid.video.template.demo.widget.TitleBar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TemplateEditActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_IMAGE_VIDEO_SINGLE = 100;
    private static final int REQUEST_CODE_IMAGE_VIDEO_MULTI = 200;

    private TitleBar mTitleBar;
    private TextureView mTemplateSurfacePreview;
    private ImageView mIvPlay;
    private SeekBar mSeekBarProgress;
    private TextView mTvDuration;
    private TextView mTvPosition;
    private RecyclerView mRVEditAsset;
    private RecyclerView mRVEditAudio;
    private LinearLayout mLLAssetEdit;
    private LinearLayout mLLAudioEdit;
    private ImageView mIvAssetEditIcon;
    private TextView mTvAssetEdit;
    private ImageView mIvAudioEditIcon;
    private TextView mTvAudioEdit;

    private TemplateEditAssetAdapter mEditAssetAdapter;
    private TemplateEditAudioAdapter mEditAudioAdapter;

    private Template mTemplate;
    private QNVTTemplateEditor mEditor;
    private double mTemplateDuration;

    private final DecimalFormat mTimestampFormat = new DecimalFormat("0.0");

    private RelatedAssetGroup mCurrentAssetGroup;
    private MusicSelectorItem mSelectedMusicItem;

    private boolean mUserStop;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_template_edit);

        mTemplate = (Template) getIntent().getSerializableExtra(Template.class.getName());

        mTitleBar = findViewById(R.id.title_bar);
        mTemplateSurfacePreview = findViewById(R.id.texture_preview);
        mIvPlay = findViewById(R.id.iv_play);
        mSeekBarProgress = findViewById(R.id.seek_bar_progress);
        mTvDuration = findViewById(R.id.tv_duration);
        mTvPosition = findViewById(R.id.tv_position);
        mRVEditAsset = findViewById(R.id.rv_asset);
        mRVEditAudio = findViewById(R.id.rv_audio);
        mLLAssetEdit = findViewById(R.id.ll_asset_edit);
        mLLAudioEdit = findViewById(R.id.ll_audio_edit);
        mIvAudioEditIcon = findViewById(R.id.iv_audio_edit_icon);
        mIvAssetEditIcon = findViewById(R.id.iv_asset_edit_icon);
        mTvAssetEdit = findViewById(R.id.tv_asset_edit);
        mTvAudioEdit = findViewById(R.id.tv_audio_edit);

        setupUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_VIDEO_SINGLE) {
            if (resultCode == Activity.RESULT_OK) {
                MediaFileSelectorActivity.MediaSelectorResult result = (MediaFileSelectorActivity.MediaSelectorResult) data.getSerializableExtra(MediaFileSelectorActivity.K_RESULT);
                if (result == null) {
                    Toast.makeText(this, "选择媒体文件失败", Toast.LENGTH_SHORT).show();
                    mCurrentAssetGroup = null;
                    return;
                }
                String path = result.path;
                if (!new File(path).exists()) {
                    Toast.makeText(this, "选择媒体文件失败", Toast.LENGTH_SHORT).show();
                    mCurrentAssetGroup = null;
                    return;
                }
                if (result.type == MediaFileSelectorActivity.MediaSelectorResult.Type.IMAGE) {
                    mCurrentAssetGroup.assetType = QNVTAssetProperty.TYPE_IMAGE;
                    for (QNVTAssetProperty related : mCurrentAssetGroup.assetList) {
                        related.path = path;
                        related.type = QNVTAssetProperty.TYPE_IMAGE;
                        mEditor.updateAsset(related);
                    }
                } else {
                    mCurrentAssetGroup.assetType = QNVTAssetProperty.TYPE_VIDEO;
                    for (QNVTAssetProperty related : mCurrentAssetGroup.assetList) {
                        related.path = path;
                        related.type = QNVTAssetProperty.TYPE_VIDEO;
                        mEditor.updateAsset(related);
                    }
                }
                mEditAssetAdapter.setAssetList(TemplateHelper.getAssetGroup(mEditor));
            }
            mCurrentAssetGroup = null;
        } else if (requestCode == REQUEST_CODE_IMAGE_VIDEO_MULTI) {
            if (resultCode == Activity.RESULT_OK) {
                // 按照顺序替代当前视频和图片节点
                ArrayList<MediaFileSelectorActivity.MediaSelectorResult> resultList = (ArrayList<MediaFileSelectorActivity.MediaSelectorResult>) data.getSerializableExtra(MediaFileSelectorActivity.K_RESULT);
                QNVTAssetProperty[] replaceableAssets = mEditor.getReplaceableAssetList();
                int lastAssetIndex = -1;
                for (MediaFileSelectorActivity.MediaSelectorResult resultItem : resultList) {
                    for (int i = 0; i < replaceableAssets.length; i++) {
                        if (replaceableAssets[i].type == QNVTAssetProperty.TYPE_VIDEO || replaceableAssets[i].type == QNVTAssetProperty.TYPE_IMAGE) {
                            if (lastAssetIndex >= i) {
                                continue;
                            }
                            lastAssetIndex = i;

                            if (resultItem.type == MediaFileSelectorActivity.MediaSelectorResult.Type.IMAGE) {
                                replaceableAssets[i].type = QNVTAssetProperty.TYPE_IMAGE;
                            } else {
                                replaceableAssets[i].type = QNVTAssetProperty.TYPE_VIDEO;
                            }
                            replaceableAssets[i].path = resultItem.path;
                            mEditor.updateAsset(replaceableAssets[i]);
                            break;
                        }
                    }
                }
                mEditAssetAdapter.setAssetList(TemplateHelper.getAssetGroup(mEditor));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mEditor != null && !mUserStop) {
            mEditor.resumePlay();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mEditor != null) {
            mEditor.pausePlay();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mEditor != null) {
            mEditor.release();
        }
    }

    private void setupPlayer(final SurfaceTexture surfaceTexture, final int width, final int height) {
        new Thread() {
            @Override
            public void run() {
                if (mEditor != null) {
                    mEditor.setPreview(new Surface(surfaceTexture), width, height);
                    return;
                }
                mEditor = new QNVTTemplateEditor(TemplateEditActivity.this, Config.LICENSE_NAME);
                mEditor.setTemplate(mTemplate.getDir());
                mEditor.createPlayer(width / 2, height / 2);
                mEditor.setPreview(new Surface(surfaceTexture), width, height);
                mEditor.setPlayCallback(new QNVTPlayerCallback() {
                    @Override
                    public void onStateChanged(int state) {
                        if (state == QNVTTemplateEditor.PLAYER_STATE_STOP || state == QNVTTemplateEditor.PLAYER_STATE_PAUSE) {
                            if (!mUserStop) {
                                if (state == QNVTTemplateEditor.PLAYER_STATE_STOP) {
                                    mEditor.seek(0);
                                    mEditor.startPlay();
                                }
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mIvPlay.setVisibility(View.VISIBLE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mIvPlay.setVisibility(View.GONE);
                                }
                            });
                        }
                    }

                    @Override
                    public void onProgress(final double current) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mSeekBarProgress.setProgress((int) (current / mTemplateDuration * 10000));
                                mTvPosition.setText(mTimestampFormat.format(current) + "s");
                            }
                        });
                    }
                });
                mEditor.startPlay();


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // audio list
                        mEditAudioAdapter.setAudioList(getAudioListItem());
                        mTemplateDuration = mEditor.duration();
                        mEditAssetAdapter.setAssetList(TemplateHelper.getAssetGroup(mEditor));
                        mTvDuration.setText(mTimestampFormat.format(mTemplateDuration) + "s");
                    }
                });
            }
        }.start();
    }


    private void setupUI() {
        mTitleBar.setTitle("模板编辑");
        mTitleBar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTemplateSurfacePreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                setupPlayer(surfaceTexture, width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                if (mEditor != null) {
                    mEditor.setPreview(new Surface(surfaceTexture), width, height);
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });
        mTemplateSurfacePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEditor.playState() == QNVTTemplateEditor.PLAYER_STATE_PLAYING) {
                    mEditor.pausePlay();
                    mIvPlay.setVisibility(View.VISIBLE);
                    mUserStop = true;
                } else if (mEditor.playState() == QNVTTemplateEditor.PLAYER_STATE_PAUSE) {
                    mEditor.resumePlay();
                    mIvPlay.setVisibility(View.GONE);
                    mUserStop = false;
                } else {
                    mEditor.startPlay();
                    mIvPlay.setVisibility(View.GONE);
                    mUserStop = false;
                }
            }
        });
        mSeekBarProgress.setMax(10000);
        mSeekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mEditor != null) {
                        mEditor.seek(progress / 10000.0 * mTemplateDuration);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mEditAssetAdapter = new TemplateEditAssetAdapter();
        mEditAssetAdapter.setOnItemClickListener(new TemplateEditAssetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final Object obj) {
                if (obj instanceof String) {
                    // header
                    int num = 0;
                    for (RelatedAssetGroup group : TemplateHelper.getAssetGroup(mEditor)) {
                        if (group.assetType == QNVTAssetProperty.TYPE_IMAGE || group.assetType == QNVTAssetProperty.TYPE_VIDEO) {
                            num++;
                        }
                    }
                    Intent intent = new Intent(TemplateEditActivity.this, MediaFileSelectorActivity.class);
                    intent.putExtra(MediaFileSelectorActivity.K_SELECTION_NUM, num);
                    startActivityForResult(intent, REQUEST_CODE_IMAGE_VIDEO_MULTI);
                } else {
                    final RelatedAssetGroup group = (RelatedAssetGroup) obj;
                    if (group.assetType == QNVTAssetProperty.TYPE_TEXT) {
                        // text
                        EditTextDialog dialog = new EditTextDialog();
                        dialog.setOnClickListener(new EditTextDialog.OnClickListener() {
                            @Override
                            public void onClick(String content) {
                                if (content == null || content.isEmpty()) {
                                    Toast.makeText(TemplateEditActivity.this, "请输入要替换的文字", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                // 替换 group 所有文字
                                for (QNVTAssetProperty related : group.assetList) {
                                    related.text = content;
                                    mEditor.updateAsset(related);
                                }
                                mEditAssetAdapter.setAssetList(TemplateHelper.getAssetGroup(mEditor));
                            }
                        });
                        dialog.showNow(getSupportFragmentManager(), null);
                    } else if (group.assetType == QNVTAssetProperty.TYPE_IMAGE || group.assetType == QNVTAssetProperty.TYPE_VIDEO) {
                        mCurrentAssetGroup = group;
                        Intent intent = new Intent(TemplateEditActivity.this, MediaFileSelectorActivity.class);
                        // size
                        ArrayList<Size> sizes = new ArrayList<>();
                        sizes.add(new Size(group.assetList.get(0).width, group.assetList.get(0).height));
                        intent.putExtra(MediaFileSelectorActivity.K_SELECTION_SIZE, sizes);
                        // duration
                        ArrayList<Long> durations = new ArrayList<>();
                        float duration = (group.assetList.get(0).out - group.assetList.get(0).in) * 1000f / mEditor.fps();
                        durations.add((long) duration);
                        intent.putExtra(MediaFileSelectorActivity.K_SELECTION_DURATION, durations);
                        startActivityForResult(intent, REQUEST_CODE_IMAGE_VIDEO_SINGLE);
                    }
                }
            }
        });
        mRVEditAsset.setLayoutManager(new LinearLayoutManager(TemplateEditActivity.this, RecyclerView.HORIZONTAL, false));

        mRVEditAsset.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(16, 0, 0, 0);
            }
        });
        mRVEditAsset.setAdapter(mEditAssetAdapter);

        mEditAudioAdapter = new TemplateEditAudioAdapter();
        mEditAudioAdapter.setOnItemClickListener(new TemplateEditAudioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MusicSelectorItem asset) {
                if (mSelectedMusicItem != null) {
                    mSelectedMusicItem.selected = false;
                }
                asset.selected = true;
                mSelectedMusicItem = asset;
                mEditAudioAdapter.notifyDataSetChanged();
                mEditor.setBgmPath(mSelectedMusicItem.musicPath);
            }
        });
        mRVEditAudio.setLayoutManager(new LinearLayoutManager(TemplateEditActivity.this, RecyclerView.HORIZONTAL, false));
        mRVEditAudio.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.set(32, 0, 0, 0);
            }
        });
        mRVEditAudio.setAdapter(mEditAudioAdapter);

        mTitleBar.setRightListener("完成", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditor != null) {
                    ExportParamsDialog exportParamsDialog = new ExportParamsDialog();
                    exportParamsDialog.setOnClickListener(new ExportParamsDialog.OnClickListener() {
                        @Override
                        public void onClick(int width, int height, int bitrate) {
                            if (width > 4000 || height > 4000) {
                                Toast.makeText(TemplateEditActivity.this, "分辨率必须小于4K", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // check available memory
                            if (width * height > 2000 * 2000) {
                                ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                                activityManager.getMemoryInfo(memoryInfo);
                                if (memoryInfo.availMem < 1024 * 1024 * 1024) {
                                    Toast.makeText(TemplateEditActivity.this, "当前可用内存不足，无法导出高分辨率视频", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            // 停止预览
                            mEditor.pausePlay();
                            mIvPlay.setVisibility(View.VISIBLE);
                            mUserStop = true;

                            final ProgressDialog progressDialog = new ProgressDialog();
                            progressDialog.show(getSupportFragmentManager(), "");
                            progressDialog.setCancelable(false);
                            final String outputPath = getFilesDir().getAbsolutePath() + "/VIDEO_" + System.currentTimeMillis() + ".mp4";
                            QNVTVideoSetting setting = new QNVTVideoSetting(outputPath);
                            setting.dimension = new Size(width, height);
                            setting.bitrate = bitrate;
                            mEditor.startExport(setting, new QNVTExportCallback() {
                                @Override
                                public void onProgress(final float percent, final boolean complete, final boolean succeed) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressDialog.setProgress((int) (percent * 100));
                                            if (complete) {
                                                progressDialog.dismiss();
                                                if (succeed) {
                                                    Intent intent = new Intent(TemplateEditActivity.this, ExportResultActivity.class);
                                                    intent.putExtra(ExportResultActivity.EXPORT_RESULT_VIDEO_PATH, outputPath);
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(TemplateEditActivity.this, "导出失败, 请检查导出参数", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                    exportParamsDialog.show(getSupportFragmentManager(), null);
                }
            }
        });

        mLLAssetEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEditMode(true);
            }
        });
        mLLAudioEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEditMode(false);
            }
        });
        updateEditMode(true);
    }

    private void updateEditMode(boolean asset) {
        if (asset) {
            mIvAssetEditIcon.setImageResource(R.mipmap.template_ic_edit_asset_pressed);
            mIvAudioEditIcon.setImageResource(R.mipmap.template_ic_edit_audio_normal);
            mTvAssetEdit.setTextColor(getResources().getColor(R.color.template_colorPrimary));
            mTvAudioEdit.setTextColor(getResources().getColor(android.R.color.white));
            mRVEditAsset.setVisibility(View.VISIBLE);
            mRVEditAudio.setVisibility(View.GONE);
        } else {
            mIvAssetEditIcon.setImageResource(R.mipmap.template_ic_edit_asset_normal);
            mIvAudioEditIcon.setImageResource(R.mipmap.template_ic_edit_audio_pressed);
            mTvAssetEdit.setTextColor(getResources().getColor(android.R.color.white));
            mTvAudioEdit.setTextColor(getResources().getColor(R.color.template_colorPrimary));
            mRVEditAsset.setVisibility(View.GONE);
            mRVEditAudio.setVisibility(View.VISIBLE);
        }
    }

    private List<MusicSelectorItem> getAudioListItem() {
        List<MusicSelectorItem> list = TemplateHelper.getMusicList(this);
        // default audio
        MusicSelectorItem defaultItem = new MusicSelectorItem("默认", mEditor.getBgmPath(), null, R.mipmap.template_ic_music_cover_default);
        defaultItem.selected = true;
        list.add(0, defaultItem);
        mSelectedMusicItem = defaultItem;
        return list;
    }

}
