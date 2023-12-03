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
        public int compare(Channel c1, Channel c2) {
            String o1=c1.getName();
            String o2=c2.getName();
            int result = collator.compare(o1, o2);

            if (result != 0) {
                return result;
            }

            String[] parts1 = o1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            String[] parts2 = o2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

            int len = Math.min(parts1.length, parts2.length);

            for (int i = 0; i < len; i++) {
                if (isNumeric(parts1[i]) && isNumeric(parts2[i])) {
                    result = Integer.compare(Integer.parseInt(parts1[i]), Integer.parseInt(parts2[i]));

                    if (result != 0) {
                        return result;
                    }
                } else {
                    result = collator.compare(parts1[i], parts2[i]);

                    if (result != 0) {
                        return result;
                    }
                }
            }

            return 0;
        }

        private boolean isNumeric(String s) {
            return s.matches("\\d+");
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
