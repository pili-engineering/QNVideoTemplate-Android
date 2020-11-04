package com.qiniu.droid.video.template.demo.model;

import com.qiniu.droid.video.template.QNVTAssetProperty;

import java.util.ArrayList;
import java.util.List;

public class RelatedAssetGroup {
    public int assetType;
    public float firstInPoint;
    public List<QNVTAssetProperty> assetList = new ArrayList<>();
}