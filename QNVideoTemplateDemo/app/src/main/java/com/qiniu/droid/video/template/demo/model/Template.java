package com.qiniu.droid.video.template.demo.model;

import java.io.Serializable;

public class Template implements Serializable {

    private final String mName;
    private final String mDesc;
    private final String mDir;
    private final int mWidth;
    private final int mHeight;
    private final String mCover;
    private final String mDemoPath;

    public Template(String name, String desc, String dir, int width, int height, String cover, String demoPath) {
        mName = name;
        mDesc = desc;
        mDir = dir;
        mWidth = width;
        mHeight = height;
        mCover = cover;
        mDemoPath = demoPath;
    }

    public String getName() {
        return mName;
    }

    public String getDesc() {
        return mDesc;
    }

    public String getDir() {
        return mDir;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getCover() {
        return mCover;
    }

    public String getPreviewPath() {
        return mDemoPath;
    }
}
