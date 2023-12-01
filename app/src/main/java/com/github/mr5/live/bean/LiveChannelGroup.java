package com.github.mr5.live.bean;

import lombok.Data;

import java.util.ArrayList;

/**
 * groupIndex : 分组索引号
 * groupName : 分组名称
 * password : 分组密码
 */
@Data
public class LiveChannelGroup {
    private int index;
    private String name;
    private String groupPassword;
    private ArrayList<LiveChannel> liveChannels;

}
