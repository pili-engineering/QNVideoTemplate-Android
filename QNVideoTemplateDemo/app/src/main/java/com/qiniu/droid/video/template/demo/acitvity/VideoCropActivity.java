package com.qiniu.droid.video.template.demo.acitvity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ClippingMediaSource;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.utils.UriUtils;
import com.qiniu.droid.video.template.demo.widget.TitleBar;
import com.qiniu.droid.video.template.demo.widget.trim.VideoTrackTrimView;
import com.qiniu.droid.video.template.demo.widget.trim.internal.VideoTrackInfo;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTrimmer;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class VideoCropActivity extends AppCompatActivity {

    public static final String K_VIDEO_CROP_URI = "K_VIDEO_CROP_URI";
    public static final String K_VIDEO_CROP_DURATION = "K_VIDEO_CROP_DURATION";

    public static final String K_VIDEO_CROP_RESULT_PATH = "K_VIDEO_CROP_RESULT_PATH";
    public static final String K_VIDEO_CROP_RESULT_DURATION = "K_VIDEO_CROP_RESULT_PATH";

    private String mSrcUri;

    private TitleBar mTitleBar;
    private VideoTrackTrimView mVideoTrackTrimView;
    private PlayerView mPlayerView;

    private VideoTrackInfo mVideoTrackInfo;
    private ExoPlayer mPlayer;

    private ProgressDialog mDialog;

    private Timer mProgressTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_video_crop);

        mSrcUri = getIntent().getStringExtra(K_VIDEO_CROP_URI);
        long duration = getIntent().getLongExtra(K_VIDEO_CROP_DURATION, 2500L);

        mTitleBar = findViewById(R.id.title_bar);
        mVideoTrackTrimView = findViewById(R.id.video_track_trim_view);
        mPlayerView = findViewById(R.id.player_view);

        preparePlayer(createLoopingClippingSource(0, duration));

        mTitleBar.setTitle("视频剪裁");
        mTitleBar.setLeftIcon(R.drawable.template_ic_clear_light);
        mTitleBar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setRightListener("完成", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    mPlayer.pause();
                }
                mDialog = new ProgressDialog(VideoCropActivity.this);
                mDialog.setIndeterminate(true);
                mDialog.setMax(100);
                mDialog.setProgress(0);
                mDialog.show();
                final String dstPath = getCacheDir().getAbsolutePath() + "/crop_video/" + System.currentTimeMillis() + ".mp4";
                Pair<Long, Long> trimTime = mVideoTrackTrimView.getTrimTime();
                // 目前短视频没有办法兼容 android 10，只能拷贝到本地再做剪裁
                File file = UriUtils.copyToCacheDir(VideoCropActivity.this, mSrcUri);
                final PLShortVideoTrimmer trimmer = new PLShortVideoTrimmer(VideoCropActivity.this, file.getAbsolutePath(), dstPath);
                trimmer.trim(trimTime.first, trimTime.second, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(String s) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                trimmer.destroy();
                                Intent intent = new Intent();
                                intent.putExtra(K_VIDEO_CROP_RESULT_PATH, dstPath);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoFailed(int i) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                trimmer.destroy();
                                Toast.makeText(VideoCropActivity.this, "trim video fail", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                                trimmer.destroy();
                                Toast.makeText(VideoCropActivity.this, "trim video cancel", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    }

                    @Override
                    public void onProgressUpdate(final float v) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.setProgress((int) (100 * v));
                                mDialog.setMessage(100 * v + "%");
                            }
                        });
                    }
                });
            }
        });

        mVideoTrackInfo = new VideoTrackInfo(this, mSrcUri);
        mVideoTrackTrimView.setVideo(mVideoTrackInfo, duration);
        mVideoTrackTrimView.setScrollStateListener(new VideoTrackTrimView.OnScrollStateListener() {
            @Override
            public void onScrollChange(int scrollX, int oldScrollX) {

            }

            @Override
            public void onScrollStateChange(boolean start) {
                if (start) {
                    if (mPlayer != null && mPlayer.isPlaying()) {
                        mPlayer.pause();
                    }
                } else {
                    mVideoTrackTrimView.resetCurrentPlayingTime();
                    Pair<Long, Long> trimTime = mVideoTrackTrimView.getTrimTime();
                    preparePlayer(createLoopingClippingSource(trimTime.first.intValue(), trimTime.second.intValue()));
                }
            }
        });

        mProgressTimer = new Timer();
        mProgressTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mPlayer != null && mPlayer.isPlaying()) {
                            mVideoTrackTrimView.setCurrentPlayingTime(mPlayer.getCurrentPosition());
                        }
                    }
                });
            }
        }, 0, 100);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoTrackInfo.release();
        mProgressTimer.cancel();
        mPlayer.release();
    }

    private void preparePlayer(MediaSource mediaSource) {
        if (mPlayer == null) {
            mPlayer = new SimpleExoPlayer.Builder(this).build();
            mPlayerView.setPlayer(mPlayer);
        }
        if (mPlayer.isPlaying()) {
            mPlayer.stop(true);
        }
        mPlayer.setPlayWhenReady(true);
        mPlayer.setMediaSource(mediaSource);
        mPlayer.prepare();
        mPlayer.play();
    }

    private MediaSource createLoopingClippingSource(long beginMs, long endMs) {
        MediaSource mediaSource = new DefaultMediaSourceFactory(this).createMediaSource(Uri.parse(mSrcUri));
        ClippingMediaSource clippingMediaSource = new ClippingMediaSource(mediaSource, beginMs * 1000, endMs * 1000);
        return new LoopingMediaSource(clippingMediaSource);
    }
}
