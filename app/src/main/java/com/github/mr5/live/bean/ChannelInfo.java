package com.github.mr5.live.bean;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ChannelInfo {
    private String duration;
    private String tvgId;
    private String tvgName;
    private String tvgLogo;
    private String groupTitle;
    private String title;
    private String url;

}