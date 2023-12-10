package com.github.mr5.live.bean;

import com.github.mr5.live.util.Log;
import lombok.Data;

import java.util.ArrayList;

/**
 * channelIndex : 频道索引号
 * channelNum : 频道名称
 * channelSourceNames : 频道源名称
 * channelUrls : 频道源地址
 * sourceIndex : 频道源索引
 * sourceNum : 频道源总数
 */
@Data
public class Channel {

    private int index;
    private int num;
    private String name;
    private ArrayList<String> urls;
    private int groupIndex;

    private int sourceIndex = 0;

    private String logoUrl;

    public void preSource() {
        sourceIndex = sourceIndex - 1 < 0 ? urls.size() - 1 : sourceIndex - 1;
    }

    public void nextSource() {
        sourceIndex = sourceIndex + 1 >= urls.size() ? 0:sourceIndex + 1 ;
    }

    public String getSourceName() {
        return "源"+sourceIndex;
    }

    public String getUrl() {
        return urls.get(sourceIndex);
    }
}