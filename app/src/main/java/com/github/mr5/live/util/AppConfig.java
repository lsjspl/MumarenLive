package com.github.mr5.live.util;

import com.github.mr5.live.bean.IJKCode;
import com.github.mr5.live.bean.Channel;
import com.github.mr5.live.bean.ChannelGroup;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import lombok.Data;
import lombok.Getter;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Data
public class AppConfig {

    private boolean isLoading = false;

    private ExecutorService executors = Executors.newFixedThreadPool(5);

    private List<IJKCode> ijkCodes = new ArrayList<>();

    private String defaultIjkStr = " [\n" +

            "        {\n" +
            "          \"group\": \"硬解码\",\n" +
            "          \"options\": [\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"opensles\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"overlay-format\",\n" +
            "              \"value\": \"842225234\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"framedrop\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"soundtouch\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"start-on-prepared\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 1,\n" +
            "              \"name\": \"http-detect-range-support\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 1,\n" +
            "              \"name\": \"fflags\",\n" +
            "              \"value\": \"fastseek\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 2,\n" +
            "              \"name\": \"skip_loop_filter\",\n" +
            "              \"value\": \"48\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"reconnect\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"max-buffer-size\",\n" +
            "              \"value\": \"5242880\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"enable-accurate-seek\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-auto-rotate\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-handle-resolution-change\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-hevc\",\n" +
            "              \"value\": \"1\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"group\": \"软解码\",\n" +
            "          \"options\": [\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"opensles\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"overlay-format\",\n" +
            "              \"value\": \"842225234\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"framedrop\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"soundtouch\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"start-on-prepared\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 1,\n" +
            "              \"name\": \"http-detect-range-support\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 1,\n" +
            "              \"name\": \"fflags\",\n" +
            "              \"value\": \"fastseek\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 2,\n" +
            "              \"name\": \"skip_loop_filter\",\n" +
            "              \"value\": \"48\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"reconnect\",\n" +
            "              \"value\": \"1\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"max-buffer-size\",\n" +
            "              \"value\": \"5242880\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"enable-accurate-seek\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-auto-rotate\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-handle-resolution-change\",\n" +
            "              \"value\": \"0\"\n" +
            "            },\n" +
            "            {\n" +
            "              \"category\": 4,\n" +
            "              \"name\": \"mediacodec-hevc\",\n" +
            "              \"value\": \"0\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +


            "      ]";

    private List<ChannelGroup> channelGroupList = new ArrayList<>();

    private JsonArray defaultIjk = new Gson().fromJson(defaultIjkStr, JsonArray.class);


    @Getter
    private static AppConfig instance = new AppConfig();

    private Comparator<Channel> comparator = new Comparator<Channel>() {
        final Collator collator = Collator.getInstance(Locale.CHINA);

        @Override
        public int compare(Channel s1, Channel s2) {
            // 判断字符串是否包含数字或英文
            boolean hasNumericOrEnglish1 = s1.getName().matches(".*[0-9a-zA-Z]+.*");
            boolean hasNumericOrEnglish2 = s1.getName().matches(".*[0-9a-zA-Z]+.*");

            // 如果两个字符串都包含数字或英文，则按照 Collator 进行比较
            if (hasNumericOrEnglish1 && hasNumericOrEnglish2) {
                return collator.compare(s1.getName(), s2.getName());
            }

            // 数字和英文优先于中文
            if (hasNumericOrEnglish1 && !hasNumericOrEnglish2) {
                return -1;
            } else if (!hasNumericOrEnglish1 && hasNumericOrEnglish2) {
                return 1;
            } else {
                // 使用 Collator 进行比较
                return collator.compare(s1.getName(), s2.getName());
            }
        }
    };

    public void sort(List<ChannelGroup> list) {
        int index = 0;
        int num = 0;
        int groupIndex = 0;

        for (ChannelGroup group : list) {
            group.setIndex(groupIndex++);

            Collections.sort(group.getChannels(), AppConfig.getInstance().getComparator());

            index = 0;

            for (Channel channel : group.getChannels()) {
                channel.setNum(num++);
                channel.setIndex(index++);
                channel.setGroupIndex(group.getIndex());
            }
        }
    }

    public void fill(List<ChannelGroup> list) {
        int index = 0;
        int num = 0;
        int groupIndex = 0;

        for (ChannelGroup group : list) {
            group.setIndex(groupIndex++);

            index = 0;

            for (Channel channel : group.getChannels()) {
                channel.setNum(num++);
                channel.setIndex(index++);
                channel.setGroupIndex(group.getIndex());
            }
        }
    }

}
