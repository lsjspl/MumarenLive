package com.github.mr5.live.util;

import android.text.TextUtils;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.github.mr5.live.bean.ChannelInfo;
import com.github.mr5.live.bean.IJKCode;
import com.github.mr5.live.bean.ChannelGroup;
import com.github.mr5.live.bean.Channel;
import com.github.mr5.live.server.ControlManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.orhanobut.hawk.Hawk;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class ChannelHandler {

    static String[] splitFixs = new String[]{",", "，", "http:", "https:", "rtmp:"};

    @Getter
    static String[] updateTime = new String[]{"不缓存", "1天", "3天", "7天", "15天", "1个月", "3个月", "半年", "不更新"};
    static Long[] updateTimeIndex = new Long[]{0L, 1L, 3L, 7L, 15L, 30L, 90L, 180L, 3000L};
    static HashMap<Integer, Integer> useSource = getUseSource();

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
                        fillUseSource();
                        Log.d("缓存生效");
                        Log.d(AppConfig.getInstance().getChannelGroupList().toString());
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

                Log.d("实时数据");

            } catch (Exception e) {
                AppConfig.getInstance().setLoading(false);
                throw new RuntimeException(e);
            } finally {
                callBack.run();
            }

        });
    }

    private static void fillUseSource() {
        for (ChannelGroup channelGroup : AppConfig.getInstance().getChannelGroupList()) {
            for (Channel channel : channelGroup.getChannels()) {
                channel.setSourceIndex(useSource.containsKey(channel.getNum()) ? useSource.get(channel.getNum()) : 0);
            }
        }
    }

    private static List<ChannelGroup> handler(String url) throws IOException {

        if (url.startsWith("clan://")) {
            url = clanToAddress(url);
        }
        Log.d("load:" + url);


        okhttp3.Response response = OkGo.<String>get(url).execute();

        String body = response.body().string();
        Log.d(body);
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
            String groupName = toSimplifiedChinese(channelInfo.getGroupTitle());
            String groupTitle = toSimplifiedChinese(channelInfo.getGroupTitle());
            String name = toSimplifiedChinese(
                    channelInfo.getTvgName() == null ||
                            channelInfo.getTvgName().isEmpty() ||
                            channelInfo.getTvgName().toLowerCase().contains("null") ?
                            channelInfo.getTitle() : channelInfo.getTvgName());
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
                channel = liveMap.get(nameClean);
                channel.getUrls().add(url);
            } else {
                channel = new Channel();
                liveMap.put(nameClean, channel);
                channel.setName(name);
                channel.setLogoUrl(tvLogo);
                channel.setUrls(new ArrayList<>());
                channel.getUrls().add(url);
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
                    if (item.contains(tmp)) {
                        split = tmp;
                        break;
                    }
                    appendIndex++;
                }

                if (split.isEmpty()) {
                    break;
                }

                String name = toSimplifiedChinese(item.split(split)[0].trim().toLowerCase());
                String url = appendIndex > 1 ? split + item.split(split)[1] : item.split(split)[1];

                String nameClean = name.trim().replaceAll("\\s|-|_", "").toLowerCase();

                if (liveMap.containsKey(nameClean)) {
                    channel = liveMap.get(nameClean);
                    channel.getUrls().add(url);
                } else {
                    channel = new Channel();
                    liveMap.put(nameClean, channel);
                    channel.setName(name);
                    channel.setUrls(new ArrayList<>());
                    channel.getUrls().add(url);
                    channels.add(channel);
                }

            }
        }

        return groups;
    }

    private static List<ChannelGroup> parserMumarenTv(String body) {


        Set<Future<List<ChannelGroup>>> futures = new HashSet<>();
        Log.d(body);

        String[] bodys = body.split("\n|\r");
        boolean isBanStart = false;
        List<String> banUrls = new ArrayList<>();
        for (String line : bodys) {
            if (line.trim().isEmpty() || line.startsWith("#")) {
                if (line.startsWith("#mumaren")) {
                } else if (line.startsWith("#ban")) {
                    isBanStart = true;
                }
            } else if (isBanStart) {
                banUrls.add(line.toLowerCase().trim());
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

        Map<String, ChannelGroup> name2Group = new LinkedHashMap<>();

        for (List<ChannelGroup> groups : futureResults) {

            for (ChannelGroup group : groups) {
                ChannelGroup oldGroup = null;
                String tmpGroupName = group.getName().trim().replaceAll("\\s|-|_", "").toLowerCase();

                for (String key : name2Group.keySet()) {
                    String tmpKey = key.trim().replaceAll("\\s|-|_", "").toLowerCase();
                    if (tmpKey.contains(tmpGroupName) || tmpGroupName.contains(tmpKey)) {
                        oldGroup = name2Group.get(key);
                        break;
                    }
                }

                if (oldGroup != null) {
                    ArrayList<Channel> channels = group.getChannels();
                    ArrayList<Channel> channelsOld = oldGroup.getChannels();

                    LinkedHashMap<String, Channel> name2Channel = new LinkedHashMap<>();
                    for (Channel channelOld : channelsOld) {
                        name2Channel.put(channelOld.getName().trim().replaceAll("\\s|-|_", "").toLowerCase(), channelOld);
                    }


                    for (Channel channel : channels) {

                        String tmpName = channel.getName().trim().replaceAll("\\s|-|_", "").toLowerCase();

                        if (tmpName.startsWith("cctv1")) {
                            Log.d(tmpName + " " + tmpName.length() + tmpName.toCharArray());
                        }

                        if (name2Channel.containsKey(tmpName)) {
                            Channel oldChannel = name2Channel.get(tmpName);

                            oldChannel.getUrls().addAll(channel.getUrls());

                            if (oldChannel.getLogoUrl() == null || oldChannel.getLogoUrl().isEmpty()) {
                                oldChannel.setLogoUrl(channel.getLogoUrl());
                            }

                        } else {
                            name2Channel.put(tmpName, channel);
                        }

                    }

                    group.getChannels().addAll(name2Channel.values());
                } else {
                    name2Group.put(tmpGroupName, group);
                }
            }

        }

        return banUrls(name2Group.values(), banUrls);
    }

    @NotNull
    private static ArrayList<ChannelGroup> banUrls(Collection<ChannelGroup> values, List<String> banUrls) {
        Iterator<ChannelGroup> groupIterator = values.iterator();

        while (groupIterator.hasNext()) {
            ChannelGroup group = groupIterator.next();
            Iterator<Channel> channelIterator = group.getChannels().iterator();

            while (channelIterator.hasNext()) {
                Channel channel = channelIterator.next();
                Iterator<String> urlIte = channel.getUrls().iterator();
                while (urlIte.hasNext()) {
                    String url = urlIte.next().trim().toLowerCase();
                    for (String ban : banUrls) {
                        if (url.contains(ban)) {
                            urlIte.remove();
                            break;
                        }
                    }
                }

                if (channel.getUrls().isEmpty()) {
                    channelIterator.remove();
                }

            }

            if (group.getChannels().isEmpty()) {
                groupIterator.remove();
            }
        }

        return new ArrayList<>(values);
    }


    private static void extractInfoFromExtInf(String line, ChannelInfo channel) {

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
        return System.currentTimeMillis() - Hawk.<Long>get(HawkConfig.CACHE_CHANNEL_RESULT_TIME, System.currentTimeMillis()) <
                updateTimeIndex[Hawk.<Integer>get(HawkConfig.CACHE_CHANNEL_RESULT_UPDATE_TIME, 2)] * 24 * 60 * 60 * 1000 * 7;
    }

    public static void clearCache() {
        //更改配置的时候重新加载缓存
        Hawk.delete(HawkConfig.CACHE_CHANNEL_LAYOUT_RESULT);
        Hawk.delete(HawkConfig.CACHE_CHANNEL_RESULT);
        Hawk.delete(HawkConfig.LIVE_CHANNEL);
        Hawk.delete(HawkConfig.CACHE_CHANNEL_USED_SOURCE);
    }

    public static String toSimplifiedChinese(String traditionalChinese) {
        return ZhConverterUtil.convertToSimple(traditionalChinese);
    }


    public static void saveUseSource(Channel currentChannel) {
        useSource.put(currentChannel.getNum(), currentChannel.getSourceIndex());
        Hawk.put(HawkConfig.CACHE_CHANNEL_USED_SOURCE, useSource);
    }

    public static HashMap<Integer, Integer> getUseSource() {
        return Hawk.get(HawkConfig.CACHE_CHANNEL_USED_SOURCE, new HashMap<>());
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


