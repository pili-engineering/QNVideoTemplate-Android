package com.qiniu.droid.video.template.demo.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.qiniu.droid.video.template.QNVTAssetProperty;
import com.qiniu.droid.video.template.QNVTTemplateEditor;
import com.qiniu.droid.video.template.demo.Config;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.acitvity.TemplateEditActivity;
import com.qiniu.droid.video.template.demo.model.RelatedAssetGroup;
import com.qiniu.droid.video.template.demo.model.Template;
import com.qiniu.droid.video.template.demo.utils.TemplateHelper;
import com.qiniu.droid.video.template.demo.widget.TitleBar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TemplateDetailFragment extends Fragment {

    private TitleBar mTitleBar;
    private TextView mTvTemplateName;
    private TextView mTvTemplateDesc;
    private PlayerView mPlayerView;
    private ImageView mIvPlay;
    private ImageView mIvPlayMask;
    private SeekBar mSeekBarProgress;
    private TextView mTvDuration;
    private TextView mTvPosition;

    private Template mTemplate;

    private boolean mUserStop;
    private ExoPlayer mPlayer;
    private Timer mProgressTimer;
    private final DecimalFormat mTsFormat = new DecimalFormat("0.0");

    public static TemplateDetailFragment newInstance(Template template) {
        TemplateDetailFragment fragment = new TemplateDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Template.class.getName(), template);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTemplate = (Template) getArguments().getSerializable(Template.class.getName());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.template_fragment_template_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTitleBar = view.findViewById(R.id.title_bar);
        mTvTemplateName = view.findViewById(R.id.tv_template_name);
        mTvTemplateDesc = view.findViewById(R.id.tv_template_desc);
        mPlayerView = view.findViewById(R.id.player_view);
        mIvPlay = view.findViewById(R.id.iv_play);
        mIvPlayMask = view.findViewById(R.id.iv_play_mask);
        mSeekBarProgress = view.findViewById(R.id.seek_bar_progress);
        mTvDuration = view.findViewById(R.id.tv_duration);
        mTvPosition = view.findViewById(R.id.tv_position);

        mTitleBar.setTitle("模板详情");
        mTitleBar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        mTitleBar.setRightListener("编辑", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), TemplateEditActivity.class);
                intent.putExtra(Template.class.getName(), mTemplate);
                startActivity(intent);
            }
        });
        mTvTemplateName.setText(mTemplate.getName());

        if (!TextUtils.isEmpty(mTemplate.getPreviewPath())) {
            mPlayer = new SimpleExoPlayer.Builder(getContext()).build();
            mPlayerView.setPlayer(mPlayer);
            mPlayer.setPlayWhenReady(true);
            mPlayer.setMediaSource(new LoopingMediaSource(new DefaultMediaSourceFactory(getContext()).createMediaSource(Uri.fromFile(new File(mTemplate.getPreviewPath())))));
            mPlayer.prepare();
            mPlayer.play();
        }

        mIvPlayMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        mIvPlay.setVisibility(View.VISIBLE);
                        mPlayer.pause();
                        mUserStop = true;
                    } else {
                        mUserStop = false;
                        mIvPlay.setVisibility(View.INVISIBLE);
                        mPlayer.play();
                    }
                }
            }
        });

        mSeekBarProgress.setMax(10000);
        mSeekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mPlayer != null) {
                    int position = (int) (seekBar.getProgress() / 10000f * mPlayer.getDuration());
                    mPlayer.seekTo(position);
                }
            }
        });
        mProgressTimer = new Timer();
        mProgressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            double duration = mPlayer.getDuration() / 1000.0;
                            double position = mPlayer.getCurrentPosition() / 1000.0;
                            mTvDuration.setText(mTsFormat.format(duration) + "s");
                            mTvPosition.setText(mTsFormat.format(position) + "s");
                            mSeekBarProgress.setMax(10000);
                            mSeekBarProgress.setProgress((int) (10000 * (position / duration)));
                        }
                    }
                });
            }
        }, 0, 100);

        prepareData();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayer != null && !mUserStop) {
            mPlayer.play();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mProgressTimer != null) {
            mProgressTimer.cancel();
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    private void prepareData() {
        new Thread() {
            @Override
            public void run() {
                QNVTTemplateEditor templateEditor = new QNVTTemplateEditor(getContext(), Config.LICENSE_NAME);
                templateEditor.setTemplate(mTemplate.getDir());
                List<RelatedAssetGroup> assetGroup = TemplateHelper.getAssetGroup(templateEditor);
                int pictureOrVideoNum = 0;
                int textNum = 0;
                final int audioNum = templateEditor.getBgmPath() == null ? 0 : 1;
                for (RelatedAssetGroup group : assetGroup) {
                    if (group.assetType == QNVTAssetProperty.TYPE_TEXT) {
                        textNum++;
                    } else if (group.assetType == QNVTAssetProperty.TYPE_IMAGE || group.assetType == QNVTAssetProperty.TYPE_VIDEO) {
                        pictureOrVideoNum++;
                    }
                }
                if (mTvTemplateDesc != null) {
                    final int finalPictureOrVideoNum = pictureOrVideoNum;
                    final int finalTextNum = textNum;
                    mTvTemplateDesc.post(new Runnable() {
                        @Override
                        public void run() {
                            mTvTemplateDesc.setText("推荐上传：" + finalPictureOrVideoNum + "张图片/视频素材，" + finalTextNum + "段文字，" + audioNum + "段音频");
                        }
                    });
                }
                templateEditor.release();
            }
        }.start();
    }
}
