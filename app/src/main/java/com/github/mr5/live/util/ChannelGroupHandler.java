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

            for (String line : lines) {
                if (line.isEmpty() || line.startsWith("#")) {
                } else if (line.startsWith("其他")) {
                    otherType = otherTypes.indexOf(line.split(":")[1]);
                } else {
                    groupRules.put(line.split(":")[0], line.split(":")[1]);
                }
            }
            return groupRules;
        });

        return handler(submit.get(), groups);
    }

    private static List<ChannelGroup> handler(LinkedHashMap<String, String> groupRules, List<ChannelGroup> groups) {
        LinkedHashMap<String, ChannelGroup> tmp = new LinkedHashMap<>();
        for (String key : groupRules.keySet()) {
            ChannelGroup channelGroup = new ChannelGroup();
            tmp.put(key, channelGroup);
            channelGroup.setChannels(new ArrayList<>());
            channelGroup.setName(key);
            channelGroup.setGroupPassword("");
        }

        for (ChannelGroup channelGroup : groups) {

            for (Channel channel : channelGroup.getChannels()) {
                for (String key : groupRules.keySet()) {

                    String[] values = groupRules.get(key).split(",");
                    for (String value : values) {
                        if (value.toLowerCase().contains(channel.getName().toLowerCase()) ||
                                channel.getName().toLowerCase().contains(value.toLowerCase())) {

                            channel.setIndex(tmp.get(key).getChannels().size());
                            tmp.get(key).getChannels().add(channel);
                        }
                    }
                }
            }
        }

        List<ChannelGroup> results = new ArrayList<>(tmp.values());

        AppConfig.getInstance().sort(results);

        return results;
    }


}
