package com.github.mr5.live.util;


import com.github.mr5.live.bean.ChannelGroup;
import com.github.mr5.live.bean.Channel;
import com.lzy.okgo.OkGo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ChannelGroupHandler {


    /*
     * #频道排序规则
     * 编号:全局/分组/源
     * #是否把没有写入布局规则的内容包含进其他分组
     * 其他:否/是/源
     * CCTV:CCTV
     * 卫视:河南卫视,卫视
     *
     *
     *
     *
     * */

    // 全局正序/分组正序/源顺序

    public static List<String> otherTypes = Arrays.asList("否", "是", "源");
    public static int otherType = 0;

    public static List<ChannelGroup> handler(String configUrl, List<ChannelGroup> groups) throws ExecutionException, InterruptedException {
        List<String> banUrls = new ArrayList<>();
        Future<LinkedHashMap<String, String>> submit = AppConfig.getInstance().getExecutors().submit(() -> {

            String url = configUrl;
            if (url.startsWith("clan://")) {
                url = ChannelHandler.clanToAddress(url);
            }
            Log.d("load:" + url);


            okhttp3.Response response = OkGo.<String>get(url).execute();
            String body = response.body().string();
            String[] lines = body.split("\\r\\n|\\n");
            LinkedHashMap<String, String> groupRules = new LinkedHashMap<>();

            boolean isBanStart = false;


            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#")) {
                    if (line.startsWith("#mumaren")) {
                    } else if (line.startsWith("#ban")) {
                        isBanStart = true;
                    }
                } else if (line.startsWith("其他")) {
                    otherType = otherTypes.indexOf(line.split(":")[1]);
                } else if (isBanStart) {
                    banUrls.addAll(Arrays.asList(line.toLowerCase().trim().split(",")));
                } else {
                    groupRules.put(line.split(":")[0], line.split(":")[1]);
                }
            }
            return groupRules;
        });

        return handler(submit.get(), groups, banUrls);
    }

    private static List<ChannelGroup> handler(LinkedHashMap<String, String> groupRules, List<ChannelGroup> groups, List<String> banUrls) {
        LinkedHashMap<String, ChannelGroup> name2Group = new LinkedHashMap<>();
        for (String key : groupRules.keySet()) {
            ChannelGroup channelGroup = new ChannelGroup();
            name2Group.put(key, channelGroup);
            channelGroup.setChannels(new ArrayList<>());
            channelGroup.setName(key);
            channelGroup.setGroupPassword("");
        }

        for (ChannelGroup channelGroup : groups) {

            for (Channel channel : channelGroup.getChannels()) {

                if (isBan(banUrls, channel)) {
                    continue;
                }

                for (String key : groupRules.keySet()) {

                    String[] values = groupRules.get(key).split(",");
                    for (String value : values) {
                        String tmpValue = value.trim().replaceAll("\\s|-|_", "").toLowerCase();
                        String tmpName = channel.getName().trim().replaceAll("\\s|-|_", "").toLowerCase();
                        if (tmpValue.contains(tmpName) || tmpName.contains(tmpValue)) {


                            boolean isfind = false;

                            for (Channel oldChannel : name2Group.get(key).getChannels()) {
                                String oldChannelName = oldChannel.getName().trim().replaceAll("\\s|-|_", "").toLowerCase();
                                if (oldChannelName.equals(tmpName)) {
                                    isfind = true;
                                    oldChannel.getUrls().addAll(channel.getUrls());

                                    if (oldChannel.getLogoUrl() == null || oldChannel.getLogoUrl().isEmpty()) {
                                        oldChannel.setLogoUrl(channel.getLogoUrl());
                                    }
                                    break;
                                }
                            }

                            if (!isfind) {
                                channel.setIndex(name2Group.get(key).getChannels().size());
                                name2Group.get(key).getChannels().add(channel);
                            }

                            break;
                        }
                    }
                }
            }
        }

        List<ChannelGroup> results = new ArrayList<>(name2Group.values());


        AppConfig.getInstance().sort(results);

        return results;
    }

    private static boolean isBan(List<String> banUrls, Channel channel) {
        for (String ban : banUrls) {
            if (channel.getName().trim().toLowerCase().contains(ban.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }


}
