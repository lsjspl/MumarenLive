package com.github.mr5.live.base;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.multidex.MultiDexApplication;

import com.github.mr5.live.util.*;
import com.github.tvbox.osc.callback.EmptyCallback;
import com.github.tvbox.osc.callback.LoadingCallback;
import com.github.tvbox.osc.data.AppDataManager;
import com.github.mr5.live.server.ControlManager;
import com.kingja.loadsir.core.LoadSir;
import com.orhanobut.hawk.Hawk;

import lombok.Getter;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * @author pj567
 * @date :2020/12/17
 * @description:
 */
public class App extends MultiDexApplication {
    @Getter
    private static App instance;

    @Override
    public void onCreate() {

        super.onCreate();
        instance = this;
        initParams();

        if (Hawk.get(HawkConfig.DEBUG_OPEN, false)) {
            Log.logCatToFile();
        }

        // OKGo
        OkGoHelper.init();
        // 初始化Web服务器
        ControlManager.init(this);
        //初始化数据库
        AppDataManager.init();
        LoadSir.beginBuilder()
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .commit();
        AutoSizeConfig.getInstance().setCustomFragment(true).getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
        PlayerHelper.init();
    }



    private void initParams() {
        // Hawk
        Hawk.init(this).build();
        if (!Hawk.contains(HawkConfig.PLAY_TYPE)) {
            Hawk.put(HawkConfig.PLAY_TYPE, 1);
        }

        try {
            PackageInfo packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;

            String version = versionName + versionCode;
            String oldVersion = Hawk.get(HawkConfig.Version, "");

            Log.d("version:" + version + ":" + oldVersion);

            if (!oldVersion.equals(versionName + versionCode)) {
                ChannelHandler.clearCache();
                Hawk.put(HawkConfig.Version, version);
            }


        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }


    }


}