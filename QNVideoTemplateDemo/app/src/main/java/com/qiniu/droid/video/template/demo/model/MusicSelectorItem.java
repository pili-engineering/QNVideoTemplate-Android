package com.qiniu.droid.video.template.demo.model;

public class MusicSelectorItem {

    public final String musicName;
    public final String musicCover;
    public final int musicCoverResId;
    public final String musicPath;
    public boolean selected;

    public MusicSelectorItem(String musicName, String musicPath, String musicCover, int musicCoverResId) {
        this.musicName = musicName;
        this.musicPath = musicPath;
        this.musicCover = musicCover;
        this.musicCoverResId = musicCoverResId;
    }
}
