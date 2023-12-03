package com.github.mr5.live.ui;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.event.RefreshEvent;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.adapter.ApiHistoryDialogAdapter;
import com.github.tvbox.osc.ui.dialog.ApiHistoryDialog;
import com.github.tvbox.osc.ui.dialog.BaseDialog;
import com.github.tvbox.osc.ui.tv.QRCodeGen;
import com.github.mr5.live.util.HawkConfig;
import com.github.mr5.live.util.ChannelHandler;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.orhanobut.hawk.Hawk;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

/**
 * 描述
 *
 * @author pj567
 * @since 2020/12/27
 */
public class ApiDialog extends BaseDialog {


    private ImageView ivQRCode;
    private TextView tvAddress;
    private EditText inputApi;

    private EditText channelConfigApi;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refresh(RefreshEvent event) {
        if (event.type == RefreshEvent.TYPE_API_URL_CHANGE) {
            inputApi.setText((String) event.obj);
        } else if (event.type == RefreshEvent.TYPE_CHANNEL_CONFIG_CHANGE) {
            channelConfigApi.setText((String) event.obj);
        }
    }

    public ApiDialog(@NonNull @NotNull Context context) {
        super(context);
        setContentView(R.layout.dialog_api_live);
        setCanceledOnTouchOutside(false);
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAddress = findViewById(R.id.tvAddress);
        inputApi = findViewById(R.id.input);
        inputApi.setText(Hawk.get(HawkConfig.API_URL, ""));

        channelConfigApi = findViewById(R.id.channelConfigApi);
        channelConfigApi.setText(Hawk.get(HawkConfig.CHANNEL_CONFIG_API, ""));


        findViewById(R.id.channelConfigApiSubmit).setOnClickListener(v -> {

            String newApi = inputApi.getText().toString().trim();
            Hawk.put(HawkConfig.API_URL, newApi);
            if (!newApi.isEmpty() && (newApi.startsWith("http") || newApi.startsWith("clan"))) {
                ArrayList<String> history = Hawk.get(HawkConfig.API_HISTORY, new ArrayList<>());
                if (!history.contains(newApi))
                    history.add(0, newApi);
                if (history.size() > 10)
                    history.remove(10);
                Hawk.put(HawkConfig.API_HISTORY, history);
            }

            String layoutApi = channelConfigApi.getText().toString().trim();
            Hawk.put(HawkConfig.CHANNEL_CONFIG_API, layoutApi);
            if (!layoutApi.isEmpty() && (layoutApi.startsWith("http") || layoutApi.startsWith("clan"))) {
                ArrayList<String> history = Hawk.get(HawkConfig.CHANNELCONFIGAPIHISTORY, new ArrayList<>());
                if (!history.contains(layoutApi))
                    history.add(0, layoutApi);
                if (history.size() > 10)
                    history.remove(10);
                Hawk.put(HawkConfig.CHANNELCONFIGAPIHISTORY, history);
            }


            ChannelHandler.clearCache();
            listener.onchange(newApi);
        });


        findViewById(R.id.apiHistory).setOnClickListener(v -> {
            ArrayList<String> history = Hawk.get(HawkConfig.API_HISTORY, new ArrayList<String>());
            if (history.isEmpty())
                return;
            String current = Hawk.get(HawkConfig.API_URL, "");
            int idx = 0;
            if (history.contains(current)) {
                idx = history.indexOf(current);
            }

            ApiHistoryDialog dialog = new ApiHistoryDialog(getContext());
            dialog.setTip("历史配置列表");
            dialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                @Override
                public void click(String value) {
                    inputApi.setText(value);
                }

                @Override
                public void del(String value, ArrayList<String> data) {
                    Hawk.put(HawkConfig.API_HISTORY, data);
                }
            }, history, idx);
            dialog.show();
        });

        findViewById(R.id.channelConfigApiHistory).setOnClickListener(v -> {
            ArrayList<String> history = Hawk.get(HawkConfig.CHANNELCONFIGAPIHISTORY, new ArrayList<>());
            if (history.isEmpty())
                return;
            String current = Hawk.get(HawkConfig.CHANNEL_CONFIG_API, "");
            int idx = 0;
            if (history.contains(current))
                idx = history.indexOf(current);
            ApiHistoryDialog dialog = new ApiHistoryDialog(getContext());
            dialog.setTip("历史配置列表");
            dialog.setAdapter(new ApiHistoryDialogAdapter.SelectDialogInterface() {
                @Override
                public void click(String value) {
                    channelConfigApi.setText(value);
                }

                @Override
                public void del(String value, ArrayList<String> data) {
                    Hawk.put(HawkConfig.CHANNELCONFIGAPIHISTORY, data);
                }
            }, history, idx);
            dialog.show();
        });


        findViewById(R.id.storagePermission).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (XXPermissions.isGranted(getContext(), Permission.Group.STORAGE)) {
                    Toast.makeText(getContext(), "已获得存储权限", Toast.LENGTH_SHORT).show();
                } else {
                    XXPermissions.with(getContext())
                            .permission(Permission.Group.STORAGE)
                            .request(new OnPermissionCallback() {
                                @Override
                                public void onGranted(List<String> permissions, boolean all) {
                                    if (all) {
                                        Toast.makeText(getContext(), "已获得存储权限", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onDenied(List<String> permissions, boolean never) {
                                    if (never) {
                                        Toast.makeText(getContext(), "获取存储权限失败,请在系统设置中开启", Toast.LENGTH_SHORT).show();
                                        XXPermissions.startPermissionActivity((Activity) getContext(), permissions);
                                    } else {
                                        Toast.makeText(getContext(), "获取存储权限失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        refreshQRCode();
    }

    private void refreshQRCode() {
        String address = ControlManager.get().getAddress(false);
        tvAddress.setText(String.format("扫描二维码/浏览器访问：\n%s", address));
        ivQRCode.setImageBitmap(QRCodeGen.generateBitmap(address, AutoSizeUtils.mm2px(getContext(), 300), AutoSizeUtils.mm2px(getContext(), 300)));
    }

    public void setOnListener(OnListener listener) {
        this.listener = listener;
    }

    OnListener listener = null;

    public interface OnListener {
        void onchange(String api);
    }
}
