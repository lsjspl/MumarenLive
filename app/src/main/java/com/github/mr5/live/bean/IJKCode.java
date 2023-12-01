package com.github.mr5.live.bean;

import com.github.mr5.live.util.HawkConfig;
import com.orhanobut.hawk.Hawk;
import lombok.Data;

import java.util.LinkedHashMap;

@Data
public class IJKCode {
    private String name;
    private LinkedHashMap<String, String> option;
    private boolean selected;

    public void selected(boolean selected) {
        this.selected = selected;
        if (selected) {
            Hawk.put(HawkConfig.IJK_CODEC, name);
        }
    }

}