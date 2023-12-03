package com.github.mr5.live.util;

import android.text.TextUtils;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.github.mr5.live.bean.ChannelInfo;
import com.github.mr5.live.bean.IJKCode;
import com.github.mr5.live.bean.ChannelGroup;
import com.github.mr5.live.bean.Channel;
import com.github.tvbox.osc.server.ControlManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.orhanobut.hawk.Hawk;
import lombok.Data;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ChannelHandler {

    static String[] splitFixs = new String[]{",", "，", "http:", "https:", "rtmp:"};

    static {
        getIjk();
    }

    public static void handler(String url, CallBack callBack) {

        AppConfig.getInstance().getExecutors().execute(() -> {
            try {

                AppConfig.getInstance().setLoading(true);

                AppConfig.getInstance().getChannelGroupList().clear();

                if (url.isEmpty()) {
                    return;
                }

                int groupType = Hawk.<Integer>get(HawkConfig.CHANNEL_GROUP_TYPE, 1);
                String groupApi = Hawk.get(HawkConfig.CHANNEL_CONFIG_API, "");

                if (isUseCache()) {
                    if (groupType == 1 && !groupApi.isEmpty()) {
                        AppConfig.getInstance().getChannelGroupList().addAll((Hawk.get(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, new ArrayList<>())));
                    } else {
                        AppConfig.getInstance().getChannelGroupList().addAll(Hawk.get(HawkConfig.CACHE_CHANNEL_RESULT, new ArrayList<>()));
                    }

                    if (!AppConfig.getInstance().getChannelGroupList().isEmpty()) {
                        return;
                    }
                }

                List<ChannelGroup> groups = handler(url);
                AppConfig.getInstance().fill(groups);

                Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT_TIME, System.currentTimeMillis());
                Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT, new ArrayList<>(groups));

                if (groupType == 1 && !groupApi.isEmpty()) {
                    List<ChannelGroup> result = ChannelGroupHandler.handler(groupApi, groups);
                    AppConfig.getInstance().getChannelGroupList().addAll(result);
                    Hawk.put(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, result);
                } else {
                    AppConfig.getInstance().getChannelGroupList().addAll(groups);
                }

            } catch (Exception e) {
                AppConfig.getInstance().setLoading(false);
                throw new RuntimeException(e);
            } finally {
                callBack.run();
            }

        });
    }

    private static List<ChannelGroup> handler(String url) throws IOException {

        if (url.startsWith("clan://")) {
            url = clanToAddress(url);
        }
        Log.d("load:" + url);


        okhttp3.Response response = OkGo.<String>get(url).execute();

        String body = response.body().string();

        if (body.toLowerCase().contains("#extm3u")) {
            return parseM3U(parseM3U(body));
        } else if (body.toLowerCase().contains("#genre#")) {
            return parseNormal(body);
        } else if (body.toLowerCase().contains("#mumaren")) {
            return parserMumarenTv(body);
        } else {
            return new ArrayList<>();
        }

    }

    public static ArrayList<ChannelInfo> parseM3U(String m3uContent) {
        ArrayList<ChannelInfo> channels = new ArrayList<>();
        String[] lines = m3uContent.split("\\r\\n|\\n");

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

    private static List<ChannelGroup> parseM3U(ArrayList<ChannelInfo> channelInfos) {
        List<ChannelGroup> groups = new ArrayList<>();
        ChannelGroup group;
        ArrayList<Channel> channels;
        Channel channel;

        Map<String, Channel> liveMap = new HashMap<>();
        Map<String, ChannelGroup> groupMap = new HashMap<>();

        for (ChannelInfo channelInfo : channelInfos) {
            Log.d(channelInfo.toString());
            String groupName = toSimplifiedChinese(channelInfo.getGroupTitle());
            String groupTitle = toSimplifiedChinese(channelInfo.getGroupTitle());
            String name = toSimplifiedChinese(channelInfo.getTvgName() == null || channelInfo.getTvgName().isEmpty() ? channelInfo.getTitle() : channelInfo.getTvgName());
            String url = channelInfo.getUrl();
            String tvLogo = channelInfo.getTvgLogo();

            if (groupMap.containsKey(groupTitle)) {
                group = groupMap.get(groupName);
                channels = group.getChannels();
            } else {
                group = new ChannelGroup();
                groups.add(group);
                channels = new ArrayList<>();
                group.setName(groupTitle);
                group.setChannels(channels);
                group.setGroupPassword("");
                groupMap.put(groupName, group);
            }


            String nameClean = name.trim().replaceAll("\\s|-|_", "").toLowerCase();
            if (liveMap.containsKey(nameClean)) {
                channel = liveMap.get(name.trim().toLowerCase());
                channel.getUrls().add(url);
                channel.getSourceNames().add("源" + channel.getUrls().size());
            } else {
                channel = new Channel();
                liveMap.put(nameClean, channel);
                channel.setName(name);
                channel.setLogoUrl(tvLogo);
                channel.setUrls(new ArrayList<>());
                channel.getUrls().add(url);
                channel.setSourceNames(new ArrayList<>());
                channel.getSourceNames().add("源" + channel.getUrls().size());
                channels.add(channel);
            }

        }

        return groups;

    }


    private static List<ChannelGroup> parseNormal(String body) {
        List<ChannelGroup> groups = new ArrayList<>();
        String[] all = body.split("\\r\\n|\\n");
        ChannelGroup group = null;
        ArrayList<Channel> channels = new ArrayList<>();
//                liveChannelGroupList.addAll();

        Channel channel;

        Map<String, Channel> liveMap = new HashMap<>();

        int index = 0;

        boolean isStart = false;

        for (String item : all) {

            if (item.trim().toLowerCase().contains("#genre#")) {
                isStart = true;
            }

            if (!isStart) {
                continue;
            }

            if (item.trim().isEmpty() || item.trim().startsWith("#")) {

            } else if (item.trim().toLowerCase().contains("#genre#")) {
                group = new ChannelGroup();
                channels = new ArrayList<>();
                groups.add(group);
                group.setName(toSimplifiedChinese(item.split(",")[0]));
                group.setChannels(channels);
                group.setGroupPassword("");
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
                    channel = liveMap.get(name);
                    channel.getUrls().add(url);
                    channel.getSourceNames().add("源" + channel.getUrls().size());
                } else {
                    channel = new Channel();
                    liveMap.put(name, channel);
                    channel.setName(name);
                    channel.setUrls(new ArrayList<>());
                    channel.getUrls().add(url);
                    channel.setSourceNames(new ArrayList<>());
                    channel.getSourceNames().add("源" + channel.getUrls().size());
                    channels.add(channel);
                }

            }
        }

        return groups;
    }

    private static List<ChannelGroup> parserMumarenTv(String body) {


        Set<Future<List<ChannelGroup>>> futures = new HashSet<>();

        String[] bodys = body.split("\n|\r\n");
        for (String line : bodys) {
            if (line.contains("mumaren") || line.trim().isEmpty()) {
            } else {
                futures.add(AppConfig.getInstance().getExecutors().submit(() -> handler(line)));
            }
        }

        List<List<ChannelGroup>> futureResults = new ArrayList<>();

        for (Future<List<ChannelGroup>> future : futures) {
            try {
                futureResults.add(future.get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Map<String, ChannelGroup> groupMap = new LinkedHashMap<>();

        for (List<ChannelGroup> groups : futureResults) {

            for (ChannelGroup group : groups) {
                ChannelGroup oldGroup = null;
                for (String key : groupMap.keySet()) {

                    String tmpKey = key.replaceAll("\\s|-", "").toLowerCase();
                    String tmpName = group.getName().replaceAll("\\s|-", "").toLowerCase();

                    if (tmpKey.contains(tmpName) || tmpName.contains(tmpKey)) {
                        oldGroup = groupMap.get(key);
                        break;
                    }
                }

                if (oldGroup != null) {
                    ArrayList<Channel> channels = group.getChannels();
                    ArrayList<Channel> channelsOld = oldGroup.getChannels();

                    LinkedHashMap<String, Channel> oldChannelMap = new LinkedHashMap<>();
                    for (Channel channelOld : channelsOld) {
                        oldChannelMap.put(channelOld.getName().replaceAll("\\s|-", "").toLowerCase(), channelOld);
                    }


                    for (Channel channel : channels) {

                        String tmpName = channel.getName().replaceAll("\\s|-", "").toLowerCase();

                        if (oldChannelMap.containsKey(tmpName)) {
                            Channel oldChannel = oldChannelMap.get(tmpName);

                            oldChannel.getUrls().addAll(channel.getUrls());
                            oldChannel.getSourceNames().addAll(channel.getSourceNames());

                            if (oldChannel.getLogoUrl() == null || oldChannel.getLogoUrl().isEmpty()) {
                                oldChannel.setLogoUrl(channel.getLogoUrl());
                            }

                        } else {
                            oldChannelMap.put(channel.getName(), channel);
                        }

                    }

                    group.getChannels().addAll(oldChannelMap.values());
                } else {
                    groupMap.put(group.getName(), group);
                }
            }

        }


        return new ArrayList<>(groupMap.values());
    }

    private static void extractInfoFromExtInf(String line, ChannelInfo channel) {

        Log.d(line);

        Pattern pattern = Pattern.compile("(?:tvg-name=\"(.*?)\".*?)?" +
                "(?:tvg-id=\"(.*?)\".*?)?" +
                "(?:tvg-logo=\"(.*?)\".*?)?" +
                "(?:group-title=\"(.*?)\".*?)?");

        Matcher matcher = pattern.matcher(line);


        while (matcher.find()) {

            String result = matcher.group();

            if (result.contains("tvg-name")) {
                channel.setTvgName(result.split("=")[1].replace("\"", ""));
            } else if (result.contains("tvg-id")) {
                channel.setTvgId(result.split("=")[1].replace("\"", ""));
            } else if (result.contains("tvg-logo")) {
                channel.setTvgLogo(result.split("=")[1].replace("\"", ""));
            } else if (result.contains("group-title")) {
                channel.setGroupTitle(result.split("=")[1].replace("\"", ""));
            }
        }

        channel.setTitle(line.contains(",") ? line.substring(line.lastIndexOf(",") + 1) : null);
    }

    private static boolean isUseCache() {
        return System.currentTimeMillis() - Hawk.<Long>get(HawkConfig.CACHE_CHANNEL_RESULT_TIME, System.currentTimeMillis()) < 24 * 60 * 60 * 1000 * 7;
    }

    public static void clearCache() {
        //更改配置的时候重新加载缓存
        Hawk.put(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT, null);
        Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT, null);
        Hawk.put(HawkConfig.LIVE_CHANNEL, null);
    }

    public static String toSimplifiedChinese(String traditionalChinese) {
        return ZhConverterUtil.convertToSimple(traditionalChinese);
    }

    public static void saveChange(Channel currentChannel) {

//        AppConfig.getInstance().getChannelGroupList().get(currentChannel.getGroupIndex())
//                .getChannels()
//                .get(currentChannel.getIndex())
//                .setSourceIndex(currentChannel.getSourceIndex());
//        int groupType = Hawk.<Integer>get(HawkConfig.CHANNEL_GROUP_TYPE, 1);
//        Hawk.put(groupType == 1 ? HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT : HawkConfig.CACHE_CHANNEL_RESULT, AppConfig.getInstance().getChannelGroupList());
    }

    public static void getIjk() {
        boolean foundOldSelect = false;
        List<IJKCode> ijkCodes = AppConfig.getInstance().getIjkCodes();
        String ijkCodec = Hawk.get(HawkConfig.IJK_CODEC, "");

        JsonArray ijk = AppConfig.getInstance().getDefaultIjk().getAsJsonArray();

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


        if (!foundOldSelect && !ijkCodes.isEmpty()) {
            ijkCodes.get(0).selected(true);
        }
    }

    public static IJKCode getIJKCodec(String name) {
        for (IJKCode code : AppConfig.getInstance().getIjkCodes()) {
            if (code.getName().equals(name))
                return code;
        }
        return AppConfig.getInstance().getIjkCodes().get(0);
    }

    public static IJKCode getCurrentIJKCode() {
        String codeName = Hawk.get(HawkConfig.IJK_CODEC, "");
        return getIJKCodec(codeName);
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

    public interface CallBack {
        void run();
    }
}


