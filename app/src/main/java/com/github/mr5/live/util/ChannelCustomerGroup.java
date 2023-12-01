package com.github.mr5.live.util;

import android.widget.Toast;

import com.github.mr5.live.base.App;
import com.github.mr5.live.bean.LiveChannelGroup;
import com.github.mr5.live.bean.LiveChannel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ChannelCustomerGroup {


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

    public static List<String> otherTypes = Arrays.asList(new String[]{"否", "是", "源"});
    public static int otherType = 0;
    public final static LinkedHashMap<String, String> groupRules = new LinkedHashMap<>();

    public static void saxUrl(String url, ChannelHandler.CallBack success, ChannelHandler.CallBack failed) {

        if (url.startsWith("clan://")) {
            url = ChannelHandler.clanToAddress(url);
        }
        Log.d("load:" + url);

        Toast.makeText(App.getInstance(), "正在加载布局文件。。。。", Toast.LENGTH_SHORT).show();
        OkGo.<String>get(url).execute(new AbsCallback<String>() {

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                Toast.makeText(App.getInstance(), "加载失败。。。", Toast.LENGTH_SHORT).show();
                failed.run();
            }

            @Override
            public String convertResponse(okhttp3.Response response) throws Throwable {
                return response.body().string();
            }

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {


                try {
                    String body = response.body();

                    Log.i(body);

                    saxChannelConfig(body);

                    //普通模式
                    Toast.makeText(App.getInstance(), "加载布局文件成功。。。", Toast.LENGTH_SHORT).show();
                    success.run();
                } catch (Exception e) {
                    Log.e("加载布局文件失败", e);
                    Toast.makeText(App.getInstance(), "加载布局文件失败。。。", Toast.LENGTH_SHORT).show();
                    failed.run();
                }
            }

        });
    }

    private static void saxChannelConfig(String body) {

        String[] lines = body.split("\\r\\n");

        for (String line : lines) {
            if (line.isEmpty() || line.startsWith("#")) {

            }
            if (line.startsWith("其他")) {
                otherType = otherTypes.indexOf(line.split(":")[1]);
            } else {
                groupRules.put(line.split(":")[0], line.split(":")[1]);
            }

        }


    }

    public static void channelLayoutHandler(String configUrl, ChannelHandler.CallBack success, ChannelHandler.CallBack failed) {

        saxUrl(configUrl, () -> {
            handler();
            success.run();
        }, () -> failed.run());
    }

    private static void handler() {
        LinkedHashMap<String, LiveChannelGroup> tmp = new LinkedHashMap<>();
        int index = 0;
        for (String key : groupRules.keySet()) {
            LiveChannelGroup liveChannelGroup = new LiveChannelGroup();
            tmp.put(key, liveChannelGroup);
            liveChannelGroup.setLiveChannels(new ArrayList<>());
            liveChannelGroup.setIndex(index++);
            liveChannelGroup.setName(key);
            liveChannelGroup.setGroupPassword("");
        }


        List<LiveChannelGroup> liveChannelGroupList = ChannelHandler.liveChannelGroupList;

        for (LiveChannelGroup liveChannelGroup : liveChannelGroupList) {

            for (LiveChannel liveChannel : liveChannelGroup.getLiveChannels()) {
                for (String key : groupRules.keySet()) {

                    String[] values = groupRules.get(key).split(",");
                    for (String value : values) {
                        if (value.toLowerCase(Locale.CHINESE).contains(liveChannel.getName().toLowerCase(Locale.CHINESE)) ||
                                liveChannel.getName().toLowerCase(Locale.CHINESE).contains(value.toLowerCase(Locale.CHINESE))) {

                            liveChannel.setIndex(tmp.get(key).getLiveChannels().size());
                            tmp.get(key).getLiveChannels().add(liveChannel);
                        }
                    }
                }
            }
        }

        int num = 0;

        for (LiveChannelGroup group : tmp.values()) {
            Collections.sort(group.getLiveChannels(), new Comparator<LiveChannel>() {
                Collator collator = Collator.getInstance(Locale.CHINA);

                @Override
                public int compare(LiveChannel o1, LiveChannel o2) {
                    String[] parts1 = o1.getName().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String[] parts2 = o2.getName().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                    // 依次比较字母和数字部分
                    for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
                        int result;
                        if (Character.isDigit(parts1[i].charAt(0)) && Character.isDigit(parts2[i].charAt(0))) {
                            // 如果都是数字，按数字比较
                            result = Integer.compare(Integer.parseInt(parts1[i]), Integer.parseInt(parts2[i]));
                        } else {
                            // 否则按字母比较
                            result = collator.compare(parts1[i], parts2[i]);
                        }

                        if (result != 0) {
                            return result;
                        }
                    }

                    // 如果前面部分相同，比较长度
                    return Integer.compare(parts1.length, parts2.length);

                }
            });


             index=0;
            for (LiveChannel liveChannel : group.getLiveChannels()) {
                liveChannel.setNum(num++);
                liveChannel.setIndex(index++);
            }
        }

        ChannelHandler.setLiveChannelGroupList(new ArrayList<>(tmp.values()));
        Hawk.put(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, ChannelHandler.getLiveChannelGroupList());
    }

}
