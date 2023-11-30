package com.github.mr5.live.util;

import android.text.TextUtils;
import android.widget.Toast;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.github.mr5.live.base.App;
import com.github.mr5.live.bean.IJKCode;
import com.github.tvbox.osc.bean.LiveChannelGroup;
import com.github.tvbox.osc.bean.LiveChannelItem;
import com.github.tvbox.osc.server.ControlManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelHandler {

    public static List<IJKCode> ijkCodes;

    private static String defaultIjkStr = " [\n" +


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

    public static List<LiveChannelGroup> liveChannelGroupList = new ArrayList<>();

    public static List<IJKCode> getIjkCodes() {
        return ijkCodes;
    }

    public static void setIjkCodes(List<IJKCode> ijkCodes) {
        ChannelHandler.ijkCodes = ijkCodes;
    }

    public static List<LiveChannelGroup> getLiveChannelGroupList() {
        return liveChannelGroupList;
    }

    public static void setLiveChannelGroupList(List<LiveChannelGroup> liveChannelGroupList) {
        ChannelHandler.liveChannelGroupList = liveChannelGroupList;
    }

    public static JsonArray getDefaultIjk() {
        return defaultIjk;
    }

    public static void setDefaultIjk(JsonArray defaultIjk) {
        ChannelHandler.defaultIjk = defaultIjk;
    }


    public static void saxUrl(String url, CallBack success, CallBack failed) {

        if (url.equals("")) {
            failed.run();
            return;
        }

        int groupType = Hawk.<Integer>get(HawkConfig.CHANNEL_GROUP_TYPE, 0);
        String groupApi = Hawk.get(HawkConfig.CHANNEL_CONFIG_API, "");

        if (isUseCache()) {
            if (groupType == 1 && !groupApi.equals("")) {
                setLiveChannelGroupList(Hawk.get(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, new ArrayList<>()));
            } else {
                setLiveChannelGroupList(Hawk.get(HawkConfig.CACHE_CHANNEL_RESULT, new ArrayList<>()));
            }

            if (!liveChannelGroupList.isEmpty()) {
                success.run();
                return;
            }
        }

        if (url.startsWith("clan://")) {
            url = clanToAddress(url);
        }
        Log.d("load:" + url);

        Toast.makeText(App.getInstance(), "正在加载。。。。", Toast.LENGTH_SHORT).show();
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
                    liveChannelGroupList.clear();
                    String body = response.body();

                    //m3u
                    if (body.contains("#EXTM3U")) {
                        parseM3uContent(parseM3UContent(body));
                    } else if (body.contains("#genre#")) {
                        parseNormal(body);
                    } else {
                        throw new Exception();
                    }
                    //普通模式
                    Toast.makeText(App.getInstance(), "加载直播源成功。。。", Toast.LENGTH_SHORT).show();
                    Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT, ChannelHandler.getLiveChannelGroupList());
                    Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT_TIME, System.currentTimeMillis());

                    if (groupType == 1&& !groupApi.equals("")) {
                        ChannelCustomerGroup.channelLayoutHandler(groupApi,success, failed);
                    } else {
                        success.run();
                    }

                } catch (Exception e) {
                    Log.e("加载失败", e);
                    Toast.makeText(App.getInstance(), "加载失败。。。", Toast.LENGTH_SHORT).show();
                    liveChannelGroupList.clear();
                    failed.run();
                }
            }

        });
    }

    private static boolean isUseCache() {
        return System.currentTimeMillis() - Hawk.<Long>get(HawkConfig.CACHE_CHANNEL_RESULT_TIME, System.currentTimeMillis()) < 24 * 60 * 60 * 1000;
    }

    public static void clearCache() {
        //更改配置的时候重新加载缓存
        Hawk.put(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, null);
        Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT, null);
    }

    public static String toSimplifiedChinese(String traditionalChinese) {
        return ZhConverterUtil.convertToSimple(traditionalChinese);
    }

    static JsonArray defaultIjk = new Gson().fromJson(defaultIjkStr, JsonArray.class);

    static {
        getIjk();
    }

    public static ArrayList<ChannelInfo> parseM3UContent(String m3uContent) {
        ArrayList<ChannelInfo> channels = new ArrayList<>();
        String[] lines = m3uContent.split("\n");

        ChannelInfo channel = null;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || (line.startsWith("#") && !line.startsWith("#EXTINF:"))) {
                //
            } else if (line.startsWith("#EXTINF:")) {
                channel = new ChannelInfo();
                extractInfoFromExtInf(line, channel);
            } else if (channel != null) {
                channel.setUrl(line);
                channels.add(channel);
                channel = null;
            }
        }

        return channels;
    }

    private static void extractInfoFromExtInf(String line, ChannelInfo channel) {
        Matcher matcher = Pattern.compile("tvg-id=\"(.*?)\"|tvg-name=\"(.*?)\"|tvg-logo=\"(.*?)\"|group-title=\"(.*?)\",(.*?)$").matcher(line);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                channel.setTvgId(matcher.group(1));
            }
            if (matcher.group(2) != null) {
                channel.setTvgName(matcher.group(2));
            }
            if (matcher.group(3) != null) {
                channel.setTvgLogo(matcher.group(3));
            }
            if (matcher.group(4) != null) {
                channel.setGroupTitle(matcher.group(4));
            }
            if (matcher.group(5) != null) {
                channel.setTitle(matcher.group(5));
            }
        }
    }


    private static void parseM3uContent(ArrayList<ChannelInfo> channelInfos) {
        liveChannelGroupList.clear();
        LiveChannelGroup group;
        ArrayList<LiveChannelItem> channels;
        LiveChannelItem channel;

        Map<String, LiveChannelItem> liveMap = new HashMap<>();
        Map<String, LiveChannelGroup> groupMap = new HashMap<>();

        int index = 0;
        for (ChannelInfo channelInfo : channelInfos) {
            String groupName = toSimplifiedChinese(channelInfo.getGroupTitle());
            String groupTitle = toSimplifiedChinese(channelInfo.getGroupTitle());
            String name = toSimplifiedChinese(channelInfo.getTvgName() == null ? channelInfo.getTitle() : channelInfo.getTvgName());
            String url = channelInfo.getUrl();
            String tvLogo = channelInfo.getTvgLogo();

            if (groupMap.containsKey(groupTitle)) {
                group = groupMap.get(groupName);
                channels = group.getLiveChannels();
            } else {
                group = new LiveChannelGroup();
                liveChannelGroupList.add(group);
                channels = new ArrayList<>();
                group.setGroupName(groupTitle);
                group.setGroupIndex(liveChannelGroupList.size() - 1);
                group.setLiveChannels(channels);
                group.setGroupPassword("");
                groupMap.put(groupName, group);
            }


            if (liveMap.containsKey(name.trim().toLowerCase())) {
                channel = liveMap.get(name.trim().toLowerCase());
                channel.getChannelUrls().add(url);
                channel.getChannelSourceNames().add("源" + channel.getChannelUrls().size());
            } else {
                channel = new LiveChannelItem();
                liveMap.put(name.trim().toLowerCase(), channel);
                channels.add(channel);
                channel.setChannelIndex(channels.size());
                channel.setChannelNum(index++);
                channel.setChannelName(name);
                channel.setLogoUrl(tvLogo);
                channel.setChannelUrls(new ArrayList<>());
                channel.getChannelUrls().add(url);
                channel.setChannelSourceNames(new ArrayList<>());
                channel.getChannelSourceNames().add("源" + channel.getChannelUrls().size());
            }

        }

    }

    static String[] splitFixs = new String[]{",", "，", "http:", "https:", "rtmp:"};

    private static void parseNormal(String body) {
        liveChannelGroupList.clear();
        String[] all = body.split("\\r\\n");
        LiveChannelGroup tvs;
        ArrayList<LiveChannelItem> channels = new ArrayList<>();
//                liveChannelGroupList.addAll();

        LiveChannelItem lives;

        Map<String, LiveChannelItem> liveMap = new HashMap<>();

        int index = 0;

        for (String item : all) {

            if (item.trim().toLowerCase().isEmpty() || item.trim().startsWith("#")) {

            } else if (item.trim().toLowerCase().contains("#genre#")) {
                tvs = new LiveChannelGroup();
                channels = new ArrayList<>();
                liveChannelGroupList.add(tvs);
                tvs.setGroupName(toSimplifiedChinese(item.split(",")[0]));
                tvs.setGroupIndex(liveChannelGroupList.size() - 1);
                tvs.setLiveChannels(channels);
                tvs.setGroupPassword("");
            } else {

                String split = "";

                int appendIndex = 0;

                for (String tmp : splitFixs) {
                    appendIndex++;
                    if (item.contains(tmp)) {
                        split = tmp;
                        break;
                    }
                }

                if (split.isEmpty()) {
                    break;
                }

                String name = toSimplifiedChinese(item.split(split)[0].trim().toLowerCase());
                String url = appendIndex > 1 ? item.split(split)[1] : split + item.split(split)[1];

                if (liveMap.containsKey(name)) {
                    lives = liveMap.get(name);
                    lives.getChannelUrls().add(url);
                    lives.getChannelSourceNames().add("源" + lives.getChannelUrls().size());
                } else {
                    lives = new LiveChannelItem();
                    liveMap.put(name, lives);
                    channels.add(lives);
                    lives.setChannelIndex(channels.size() - 1);
                    lives.setChannelNum(index++);
                    lives.setChannelName(name);
                    lives.setChannelUrls(new ArrayList<>());
                    lives.getChannelUrls().add(url);
                    lives.setChannelSourceNames(new ArrayList<>());
                    lives.getChannelSourceNames().add("源" + lives.getChannelUrls().size());
                }

            }
        }
    }


    public static void getIjk() {
        boolean foundOldSelect = false;
        String ijkCodec = Hawk.get(HawkConfig.IJK_CODEC, "");
        ijkCodes = new ArrayList<>();

        JsonArray ijk = defaultIjk.getAsJsonArray();

        for (JsonElement opt : ijk) {
            JsonObject obj = (JsonObject) opt;
            String name = obj.get("group").getAsString();
            LinkedHashMap<String, String> baseOpt = new LinkedHashMap<>();
            for (JsonElement cfg : obj.get("options").getAsJsonArray()) {
                JsonObject cObj = (JsonObject) cfg;
                String key = cObj.get("category").getAsString() + "|" + cObj.get("name").getAsString();
                String val = cObj.get("value").getAsString();
                baseOpt.put(key, val);
            }
            IJKCode codec = new IJKCode();
            codec.setName(name);
            codec.setOption(baseOpt);
            if (name.equals(ijkCodec) || TextUtils.isEmpty(ijkCodec)) {
                codec.selected(true);
                ijkCodec = name;
                foundOldSelect = true;
            } else {
                codec.selected(false);
            }
            ijkCodes.add(codec);
        }


        if (!foundOldSelect && ijkCodes.size() > 0) {
            ijkCodes.get(0).selected(true);
        }
    }


    public static IJKCode getIJKCodec(String name) {
        for (IJKCode code : ijkCodes) {
            if (code.getName().equals(name))
                return code;
        }
        return ijkCodes.get(0);
    }

    public static IJKCode getCurrentIJKCode() {
        String codeName = Hawk.get(HawkConfig.IJK_CODEC, "");
        return getIJKCodec(codeName);
    }


    public interface CallBack {
        public void run();
    }

    static String clanToAddress(String lanLink) {
        if (lanLink.startsWith("clan://localhost/")) {
            return lanLink.replace("clan://localhost/", ControlManager.get().getAddress(true) + "file/");
        } else {
            String link = lanLink.substring(7);
            int end = link.indexOf('/');
            return "http://" + link.substring(0, end) + "/file/" + link.substring(end + 1);
        }
    }

    String clanContentFix(String lanLink, String content) {
        String fix = lanLink.substring(0, lanLink.indexOf("/file/") + 6);
        return content.replace("clan://", fix);
    }

}