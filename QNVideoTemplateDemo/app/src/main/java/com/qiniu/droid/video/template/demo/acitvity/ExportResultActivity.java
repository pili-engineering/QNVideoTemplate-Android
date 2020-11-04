package com.qiniu.droid.video.template.demo.acitvity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.qiniu.droid.video.template.demo.R;
import com.qiniu.droid.video.template.demo.utils.MediaStoreHelper;
import com.qiniu.droid.video.template.demo.widget.TitleBar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class ExportResultActivity extends AppCompatActivity {

    public static final String EXPORT_RESULT_VIDEO_PATH = "EXPORT_RESULT_VIDEO_PATH";

    private TitleBar mTitleBar;
    private PlayerView mPlayerView;
    private ImageView mIvPlay;
    private ImageView mIvPlayMask;
    private SeekBar mSeekBarProgress;
    private TextView mTvDuration;
    private TextView mTvPosition;

    private ExoPlayer mPlayer;

    private Timer mProgressTimer;
    private final DecimalFormat mTsFormat = new DecimalFormat("0.0");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.template_activity_export_result);

        final String path = getIntent().getStringExtra(EXPORT_RESULT_VIDEO_PATH);

        mTitleBar = findViewById(R.id.title_bar);
        mPlayerView = findViewById(R.id.player_view);
        mIvPlay = findViewById(R.id.iv_play);
        mIvPlayMask = findViewById(R.id.iv_play_mask);
        mSeekBarProgress = findViewById(R.id.seek_bar_progress);
        mTvDuration = findViewById(R.id.tv_duration);
        mTvPosition = findViewById(R.id.tv_position);

        mTitleBar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mTitleBar.setTitle("模板编辑");
        mTitleBar.setRightListener("保存到相册", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToGallery(path);
            }
        });

        MediaSource mediaSource = new DefaultMediaSourceFactory(this).createMediaSource(Uri.fromFile(new File(path)));
        mPlayer = new SimpleExoPlayer.Builder(this).build();
        mPlayerView.setPlayer(mPlayer);
        mPlayer.setMediaSource(new LoopingMediaSource(mediaSource));
        mPlayer.setPlayWhenReady(true);
        mPlayer.prepare();
        mPlayer.play();

        mIvPlayMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer != null) {
                    if (mPlayer.isPlaying()) {
                        mIvPlay.setVisibility(View.VISIBLE);
                        mPlayer.pause();
                    } else {
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
                if (mPlayer != null && mPlayer.isPlaying()) {
                    int position = (int) (seekBar.getProgress() / 10000f * mPlayer.getDuration());
                    mPlayer.seekTo(position);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProgressTimer.cancel();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    private void saveToGallery(String path) {
        Uri storeUri = MediaStoreHelper.storeVideo(this, new File(path), "video/mp4");
        if (storeUri != null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(storeUri);
            sendBroadcast(intent);
            Toast.makeText(this, "保存到相册成功", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "保存到相册失败", Toast.LENGTH_SHORT).show();
        }
    }

}
