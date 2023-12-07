package com.github.tvbox.osc.api;

import com.github.catvod.crawler.JarLoader;
import com.github.catvod.crawler.Spider;
import com.github.mr5.live.base.App;
import com.github.tvbox.osc.bean.ParseBean;
import com.github.tvbox.osc.bean.SourceBean;
import com.github.mr5.live.server.ControlManager;
import com.github.mr5.live.util.HawkConfig;
import com.github.tvbox.osc.util.MD5;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pj567
 * @date :2020/12/18
 * @description:
 */
public class ApiConfig {
    private static ApiConfig instance;
    private LinkedHashMap<String, SourceBean> sourceBeanList;
    private SourceBean mHomeSource;
    private ParseBean mDefaultParse;
    private List<ParseBean> parseBeanList;
    private List<String> vipParseFlags;
    private String spider = null;

    private SourceBean emptyHome = new SourceBean();

    private JarLoader jarLoader = new JarLoader();


    private ApiConfig() {
        sourceBeanList = new LinkedHashMap<>();
        parseBeanList = new ArrayList<>();
    }

    public static ApiConfig get() {
        if (instance == null) {
            synchronized (ApiConfig.class) {
                if (instance == null) {
                    instance = new ApiConfig();
                }
            }
        }
        return instance;
    }

//    public void loadConfig(boolean useCache, LoadConfigCallback callback, Activity activity) {
//        String apiUrl = Hawk.get(HawkConfig.API_URL, "");
//        if (apiUrl.isEmpty()) {
//            callback.error("-1");
//            return;
//        }
//        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/" + MD5.encode(apiUrl));
//        if (useCache && cache.exists()) {
//            try {
//                parseJson(apiUrl, cache);
//                callback.success();
//                return;
//            } catch (Throwable th) {
//                th.printStackTrace();
//            }
//        }
//        String apiFix = apiUrl;
//        if (apiUrl.startsWith("clan://")) {
//            apiFix = clanToAddress(apiUrl);
//        }
//        OkGo.<String>get(apiFix)
//                .execute(new AbsCallback<String>() {
//                    @Override
//                    public void onSuccess(Response<String> response) {
//                        try {
//                            String json = response.body();
//                            parseJson(apiUrl, response.body());
//                            try {
//                                File cacheDir = cache.getParentFile();
//                                if (!cacheDir.exists())
//                                    cacheDir.mkdirs();
//                                if (cache.exists())
//                                    cache.delete();
//                                FileOutputStream fos = new FileOutputStream(cache);
//                                fos.write(json.getBytes("UTF-8"));
//                                fos.flush();
//                                fos.close();
//                            } catch (Throwable th) {
//                                th.printStackTrace();
//                            }
//                            callback.success();
//                        } catch (Throwable th) {
//                            th.printStackTrace();
//                            callback.error("解析配置失败");
//                        }
//                    }
//
//                    @Override
//                    public void onError(Response<String> response) {
//                        super.onError(response);
//                        if (cache.exists()) {
//                            try {
//                                parseJson(apiUrl, cache);
//                                callback.success();
//                                return;
//                            } catch (Throwable th) {
//                                th.printStackTrace();
//                            }
//                        }
//                        callback.error("拉取配置失败\n" + (response.getException() != null ? response.getException().getMessage() : ""));
//                    }
//
//                    public String convertResponse(okhttp3.Response response) throws Throwable {
//                        String result = "";
//                        if (response.body() == null) {
//                            result = "";
//                        } else {
//                            result = response.body().string();
//                        }
//                        if (apiUrl.startsWith("clan")) {
//                            result = clanContentFix(clanToAddress(apiUrl), result);
//                        }
//                        return result;
//                    }
//                });
//    }


    public void loadJar(boolean useCache, String spider, LoadConfigCallback callback) {
        String[] urls = spider.split(";md5;");
        String jarUrl = urls[0];
        String md5 = urls.length > 1 ? urls[1].trim() : "";
        File cache = new File(App.getInstance().getFilesDir().getAbsolutePath() + "/csp.jar");

        if (!md5.isEmpty() || useCache) {
            if (cache.exists() && (useCache || MD5.getFileMd5(cache).equalsIgnoreCase(md5))) {
                if (jarLoader.load(cache.getAbsolutePath())) {
                    callback.success();
                } else {
                    callback.error("");
                }
                return;
            }
        }

        OkGo.<File>get(jarUrl).execute(new AbsCallback<File>() {

            @Override
            public File convertResponse(okhttp3.Response response) throws Throwable {
                File cacheDir = cache.getParentFile();
                if (!cacheDir.exists())
                    cacheDir.mkdirs();
                if (cache.exists())
                    cache.delete();
                FileOutputStream fos = new FileOutputStream(cache);
                fos.write(response.body().bytes());
                fos.flush();
                fos.close();
                return cache;
            }

            @Override
            public void onSuccess(Response<File> response) {
                if (response.body().exists()) {
                    if (jarLoader.load(response.body().getAbsolutePath())) {
                        callback.success();
                    } else {
                        callback.error("");
                    }
                } else {
                    callback.error("");
                }
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                callback.error("");
            }
        });
    }

//    private void parseJson(String apiUrl, File f) throws Throwable {
//        System.out.println("从本地缓存加载" + f.getAbsolutePath());
//        BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
//        StringBuilder sb = new StringBuilder();
//        String s = "";
//        while ((s = bReader.readLine()) != null) {
//            sb.append(s + "\n");
//        }
//        bReader.close();
//        parseJson(apiUrl, sb.toString());
//    }

    private void parseJson(String apiUrl, String jsonStr) {

    }

    public String getSpider() {
        return spider;
    }

    public Spider getCSP(SourceBean sourceBean) {
        return jarLoader.getSpider(sourceBean.getKey(), sourceBean.getApi(), sourceBean.getExt());
    }

    public Object[] proxyLocal(Map param) {
        return jarLoader.proxyInvoke(param);
    }

    public JSONObject jsonExt(String key, LinkedHashMap<String, String> jxs, String url) {
        return jarLoader.jsonExt(key, jxs, url);
    }

    public JSONObject jsonExtMix(String flag, String key, String name, LinkedHashMap<String, HashMap<String, String>> jxs, String url) {
        return jarLoader.jsonExtMix(flag, key, name, jxs, url);
    }

    public interface LoadConfigCallback {
        void success();

        void retry();

        void error(String msg);
    }

    public interface FastParseCallback {
        void success(boolean parse, String url, Map<String, String> header);

        void fail(int code, String msg);
    }

    public SourceBean getSource(String key) {
        if (!sourceBeanList.containsKey(key))
            return null;
        return sourceBeanList.get(key);
    }

    public void setSourceBean(SourceBean sourceBean) {
        this.mHomeSource = sourceBean;
        Hawk.put(HawkConfig.HOME_API, sourceBean.getKey());
    }

    public void setDefaultParse(ParseBean parseBean) {
        if (this.mDefaultParse != null)
            this.mDefaultParse.setDefault(false);
        this.mDefaultParse = parseBean;
        Hawk.put(HawkConfig.DEFAULT_PARSE, parseBean.getName());
        parseBean.setDefault(true);
    }

    public ParseBean getDefaultParse() {
        return mDefaultParse;
    }

    public List<SourceBean> getSourceBeanList() {
        return new ArrayList<>(sourceBeanList.values());
    }

    public List<ParseBean> getParseBeanList() {
        return parseBeanList;
    }

    public List<String> getVipParseFlags() {
        return vipParseFlags;
    }

    public SourceBean getHomeSourceBean() {
        return mHomeSource == null ? emptyHome : mHomeSource;
    }


    String clanToAddress(String lanLink) {
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