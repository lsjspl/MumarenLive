package com.github.mr5.live.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.mr5.live.util.AppConfig;
import com.github.tvbox.osc.R;
import com.github.mr5.live.base.App;
import com.github.mr5.live.base.BaseActivity;
import com.github.mr5.live.bean.ChannelGroup;
import com.github.mr5.live.bean.Channel;
import com.github.mr5.live.bean.LivePlayerManager;
import com.github.tvbox.osc.bean.LiveSettingGroup;
import com.github.tvbox.osc.bean.LiveSettingItem;
import com.github.mr5.live.player.controller.LiveController;
import com.github.tvbox.osc.server.ControlManager;
import com.github.tvbox.osc.ui.adapter.LiveChannelGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveChannelItemAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingGroupAdapter;
import com.github.tvbox.osc.ui.adapter.LiveSettingItemAdapter;
import com.github.tvbox.osc.ui.dialog.LivePasswordDialog;
import com.github.tvbox.osc.ui.tv.widget.ViewObj;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.mr5.live.util.HawkConfig;
import com.github.mr5.live.util.Log;
import com.github.mr5.live.util.ChannelHandler;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.greenrobot.eventbus.EventBus;
import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author pj567
 * @date :2021/1/12
 * @description:
 */
public class LivePlayActivity extends BaseActivity {
    private VideoView mVideoView;
    private TextView tvChannelInfo;

    private RelativeLayout tvBottomView;

    private ImageView tvIconView;
    private TextView tvNumView;
    private TextView tvNameView;
    private TextView tvInfoView;


    private TextView tvTime;
    private TextView tvNetSpeed;
    private LinearLayout tvLeftChannelListLayout;
    private TvRecyclerView mChannelGroupView;
    private TvRecyclerView mLiveChannelView;
    private LiveChannelGroupAdapter liveChannelGroupAdapter;
    private LiveChannelItemAdapter liveChannelItemAdapter;

    private LinearLayout tvRightSettingLayout;
    private TvRecyclerView mSettingGroupView;
    private TvRecyclerView mSettingItemView;
    private LiveSettingGroupAdapter liveSettingGroupAdapter;
    private LiveSettingItemAdapter liveSettingItemAdapter;
    private List<LiveSettingGroup> liveSettingGroupList = new ArrayList<>();

    private Handler mHandler = new Handler();

    private List<ChannelGroup> channelGroupList = new ArrayList<>();
    private int currentLiveChangeSourceTimes = 0;
    private Channel currentChannel = null;
    private LivePlayerManager livePlayerManager = new LivePlayerManager();
    private ArrayList<Integer> channelGroupPasswordConfirmed = new ArrayList<>();
    String currentLayoutAPi = "";
    String currentApiUrl = "";
    ApiDialog setDialog = null;

    @Override
    protected int getLayoutResID() {
        return R.layout.activity_live_play;
    }

    @Override
    protected void init() {

        setLoadSir(findViewById(R.id.live_root));
        mVideoView = findViewById(R.id.mVideoView);

        tvLeftChannelListLayout = findViewById(R.id.tvLeftChannnelListLayout);
        mChannelGroupView = findViewById(R.id.mGroupGridView);
        mLiveChannelView = findViewById(R.id.mChannelGridView);
        tvRightSettingLayout = findViewById(R.id.tvRightSettingLayout);
        mSettingGroupView = findViewById(R.id.mSettingGroupView);
        mSettingItemView = findViewById(R.id.mSettingItemView);

        tvChannelInfo = findViewById(R.id.tvChannel);
        tvTime = findViewById(R.id.tvTime);
        tvNetSpeed = findViewById(R.id.tvNetSpeed);


        tvBottomView = findViewById(R.id.tvBottomView);
        tvIconView = findViewById(R.id.tvIconView);
        tvNameView = findViewById(R.id.tvNameView);
        tvInfoView = findViewById(R.id.tvInfoView);
        tvNumView = findViewById(R.id.tvNumView);

        showLoading();

        try {
            ControlManager.get().startServer();

            initVideoView();
            initChannelGroupView();
            initLiveChannelView();
            initSettingGroupView();
            initSettingItemView();
            livePlayerManager.init(mVideoView);
            initLiveSettingGroupList();
            buildDialog();
            loadChannels(Hawk.get(HawkConfig.API_URL, ""));
        } catch (Exception e) {
            Log.e("",e);
            dialog.show();
        }
        Log.d("init");
    }

    private void loadChannels(String apiUrl) {
        try {

            currentApiUrl = apiUrl;
            ChannelHandler.handler(apiUrl, () -> {
                mHandler.post(this::initLiveState);
            });
        } catch (Exception e) {
            Log.e("", e);
            dialog.show();
        }
    }

    private void initLiveState() {

        channelGroupList = AppConfig.getInstance().getChannelGroupList();

        Log.d(channelGroupList.toString());
        if (channelGroupList.isEmpty()) {
            dialog.show();
            return;
        }

        showSuccess();

        showTime();
        showNetSpeed();
        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
        tvRightSettingLayout.setVisibility(View.INVISIBLE);

        liveChannelGroupAdapter.setNewData(channelGroupList);
        currentChannel = Hawk.get(HawkConfig.LIVE_CHANNEL, null);


        if (currentChannel != null) {
            boolean find = false;
            for (ChannelGroup channelGroup : channelGroupList) {
                if (!channelGroup.getChannels().isEmpty()) {
                    for (Channel channel : channelGroup.getChannels()) {
                        if (channel.getNum() == currentChannel.getNum()) {
                            channel.setSourceIndex(currentChannel.getSourceIndex());
                            currentChannel = channel;
                            find = true;
                            break;
                        }
                    }
                }
            }

            if (!find) {
                currentChannel = null;
            }
        }

        //todo 妥协代码

        if (currentChannel == null) {
            for (ChannelGroup channelGroup : channelGroupList) {
                if (!channelGroup.getChannels().isEmpty()) {
                    selectChannelGroup(channelGroup.getChannels().get(0).getGroupIndex(), false, channelGroup.getChannels().get(0).getIndex());
                    break;
                }
            }
        } else {
            int groupIndex = currentChannel.getGroupIndex();
            int channelIndex = currentChannel.getIndex();
            currentChannel = null;
            selectChannelGroup(groupIndex, false, channelIndex);
        }
    }

    @Override
    public void onBackPressed() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        } else if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        } else {
            mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
            mHandler.removeCallbacks(mUpdateNetSpeedRun);
            super.onBackPressed();
            exit();
        }
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        try {


            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                int keyCode = event.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    showSettingGroup();
                } else if (!isListOrSettingLayoutVisible()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_UP:
                            if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false))
                                playNext();
                            else
                                playPrevious();
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if (Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false)) {
                                Log.d("播放上一个");
                                playPrevious();
                            } else {
                                Log.d("播放下一个");
                                playNext();
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            Log.d("播放上一个源");
                            playPreSource();
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            Log.d("播放下一个源");
                            playNextSource();
                            break;
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                            showChannelList();
                            break;
                    }
                }
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
            }
        } catch (Exception e) {
            Log.e(",",e);
            dialog.show();
        }
        return super.dispatchKeyEvent(event);
    }

    TipDialog dialog = null;

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
    }

    @Override
    protected void onRestart() {
        Log.d("onRestart");
        super.onRestart();

        if (mVideoView != null) {
            mVideoView.resume();
        }

//        String apiUrl = Hawk.get(HawkConfig.API_URL, "");
//
//        if (!currentApiUrl.equals(apiUrl) || !currentLayoutAPi.equals(Hawk.get(HawkConfig.CHANNEL_CONFIG_API, ""))) {
//
//            currentLiveChangeSourceTimes = 0;
//            currentChannel = null;
//
//            loadLiveChannels(apiUrl);
//
//        } else if (AppConfig.getInstance().getChannelGroupList().isEmpty() && !setDialog.isShowing()) {
//            dialog.show();
//        }

    }

    private void buildDialog() {
        String apiUrl = Hawk.get(HawkConfig.API_URL, "");
        dialog = new TipDialog(LivePlayActivity.this, "系统异常或者频道信息为空，请选择：", "重试", "设置", new TipDialog.OnListener() {
            @Override
            public void left() {
                dialog.hide();
                loadChannels(apiUrl);
            }

            @Override
            public void right() {
                dialog.hide();
                setDialog.show();
            }

            @Override
            public void cancel() {
                dialog.hide();
            }
        });


        setDialog = new ApiDialog(this);
        EventBus.getDefault().register(setDialog);
        setDialog.setOnListener(api -> jumpActivity(LivePlayActivity.class));
        setDialog.setOnDismissListener(item -> EventBus.getDefault().unregister(item));
    }


    private long mExitTime = 0;

    private void exit() {
        if (System.currentTimeMillis() - mExitTime < 2000) {
            super.onBackPressed();
        } else {
            mExitTime = System.currentTimeMillis();
            Toast.makeText(mContext, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideoView != null) {
            mVideoView.release();
            mVideoView = null;
        }
    }

    private void showChannelList() {
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.INVISIBLE) {
            showChannelInfo();
            //重新载入上一次状态
            liveChannelItemAdapter.setNewData(getLiveChannels(currentChannel.getGroupIndex()));
            mLiveChannelView.scrollToPosition(currentChannel.getIndex());
            mLiveChannelView.setSelection(currentChannel.getIndex());
            mChannelGroupView.scrollToPosition(currentChannel.getGroupIndex());
            mChannelGroupView.setSelection(currentChannel.getGroupIndex());
            mHandler.postDelayed(mFocusCurrentChannelAndShowChannelList, 200);

            //
        } else {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
    }

    private Runnable mFocusCurrentChannelAndShowChannelList = new Runnable() {
        @Override
        public void run() {
            if (mChannelGroupView.isScrolling() || mLiveChannelView.isScrolling() || mChannelGroupView.isComputingLayout() || mLiveChannelView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                liveChannelGroupAdapter.setSelectedGroupIndex(currentChannel.getGroupIndex());
                liveChannelItemAdapter.setSelectedChannelIndex(currentChannel.getIndex());
                RecyclerView.ViewHolder holder = mLiveChannelView.findViewHolderForAdapterPosition(currentChannel.getIndex());
                if (holder != null)
                    holder.itemView.requestFocus();
                tvLeftChannelListLayout.setVisibility(View.VISIBLE);
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams());
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), -tvLeftChannelListLayout.getLayoutParams().width, 0);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mHandler.removeCallbacks(mHideChannelListRun);
                        mHandler.postDelayed(mHideChannelListRun, 5000);
                    }
                });
                animator.start();
            }
        }
    };

    private Runnable mHideChannelListRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvLeftChannelListLayout.getLayoutParams();
            if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(tvLeftChannelListLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginLeft", new IntEvaluator(), 0, -tvLeftChannelListLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvLeftChannelListLayout.setVisibility(View.INVISIBLE);
                    }
                });
                animator.start();
            }
        }
    };


    //显示台信息
    private void showChannelInfo() {
        tvChannelInfo.setText(String.format(Locale.getDefault(), "%d %s %s(%d/%d)", currentChannel.getNum(),
                currentChannel.getName(), currentChannel.getSourceName(),
                currentChannel.getSourceIndex() + 1, currentChannel.getUrls().size()));

        FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
            lParams.gravity = Gravity.LEFT;
            lParams.leftMargin = 60;
            lParams.topMargin = 30;
        } else {
            lParams.gravity = Gravity.RIGHT;
            lParams.rightMargin = 60;
            lParams.topMargin = 30;
        }
        tvChannelInfo.setLayoutParams(lParams);

        tvChannelInfo.setVisibility(View.VISIBLE);
        mHandler.removeCallbacks(mHideChannelInfoRun);
        mHandler.postDelayed(mHideChannelInfoRun, 5000);

        //bottom

        if (currentChannel.getLogoUrl() != null && !currentChannel.getLogoUrl().isEmpty()) {
            Picasso.get().load(currentChannel.getLogoUrl()).into(tvIconView);
            tvIconView.setVisibility(View.VISIBLE);
        }
        tvNumView.setText(currentChannel.getNum() + "");
        tvNameView.setText(currentChannel.getName());
        tvInfoView.setText(String.format(Locale.getDefault(), " %s(%d/%d)", currentChannel.getSourceName(),
                currentChannel.getSourceIndex() + 1, currentChannel.getUrls().size()));

        tvBottomView.setVisibility(View.VISIBLE);
    }

    private Runnable mHideChannelInfoRun = new Runnable() {
        @Override
        public void run() {
            tvIconView.setVisibility(View.GONE);
            tvBottomView.setVisibility(View.GONE);
            tvChannelInfo.setVisibility(View.INVISIBLE);
        }
    };

    private boolean playChannel(int channelGroupIndex, int liveChannelIndex, boolean changeSource) {
        if (currentChannel != null && (channelGroupIndex == currentChannel.getGroupIndex()
                && liveChannelIndex == currentChannel.getIndex() && !changeSource)
                || (changeSource && currentChannel.getSourceIndex() == 0)) {
            showChannelInfo();
            return true;
        }

        mVideoView.release();

        ArrayList<Channel> channels = getLiveChannels(channelGroupIndex);
        currentChannel = channels.get(channels.size() > liveChannelIndex ? liveChannelIndex : 0);
        if (!changeSource) {
            ChannelHandler.saveUseSource(currentChannel);
            livePlayerManager.getLiveChannelPlayer(mVideoView, currentChannel.getName());
        }

        Hawk.put(HawkConfig.LIVE_CHANNEL, currentChannel);

        mVideoView.setUrl(currentChannel.getUrl());
        showChannelInfo();
        mVideoView.start();
        return true;
    }

    private void playNext() {
        Integer[] groupChannelIndex = getNextChannel(1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    private void playPrevious() {
        Integer[] groupChannelIndex = getNextChannel(-1);
        playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
    }

    public void playPreSource() {
        currentChannel.preSource();
        playChannel(currentChannel.getGroupIndex(), currentChannel.getIndex(), true);
    }

    public void playNextSource() {
        currentChannel.nextSource();
        playChannel(currentChannel.getGroupIndex(), currentChannel.getIndex(), true);
    }

    //显示设置列表
    private void showSettingGroup() {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.post(mHideChannelListRun);
        }
        if (tvRightSettingLayout.getVisibility() == View.INVISIBLE) {
            //重新载入默认状态
            loadCurrentSourceList();
            liveSettingGroupAdapter.setNewData(liveSettingGroupList);
            selectSettingGroup(0, false);
            mSettingGroupView.scrollToPosition(0);
            mSettingItemView.scrollToPosition(currentChannel.getSourceIndex());
            mHandler.postDelayed(mFocusAndShowSettingGroup, 200);
        } else {
            mHandler.removeCallbacks(mHideSettingLayoutRun);
            mHandler.post(mHideSettingLayoutRun);
        }
    }

    private Runnable mFocusAndShowSettingGroup = new Runnable() {
        @Override
        public void run() {
            if (mSettingGroupView.isScrolling() || mSettingItemView.isScrolling() || mSettingGroupView.isComputingLayout() || mSettingItemView.isComputingLayout()) {
                mHandler.postDelayed(this, 100);
            } else {
                RecyclerView.ViewHolder holder = mSettingGroupView.findViewHolderForAdapterPosition(0);
                if (holder != null)
                    holder.itemView.requestFocus();
                tvRightSettingLayout.setVisibility(View.VISIBLE);
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
                if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                    ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                    ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), -tvRightSettingLayout.getLayoutParams().width, 0);
                    animator.setDuration(200);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mHandler.postDelayed(mHideSettingLayoutRun, 5000);
                        }
                    });
                    animator.start();
                }
            }
        }
    };

    private Runnable mHideSettingLayoutRun = new Runnable() {
        @Override
        public void run() {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) tvRightSettingLayout.getLayoutParams();
            if (tvRightSettingLayout.getVisibility() == View.VISIBLE) {
                ViewObj viewObj = new ViewObj(tvRightSettingLayout, params);
                ObjectAnimator animator = ObjectAnimator.ofObject(viewObj, "marginRight", new IntEvaluator(), 0, -tvRightSettingLayout.getLayoutParams().width);
                animator.setDuration(200);
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        tvRightSettingLayout.setVisibility(View.INVISIBLE);
                        liveSettingGroupAdapter.setSelectedGroupIndex(-1);
                    }
                });
                animator.start();
            }
        }
    };

    private void initVideoView() {
        LiveController controller = new LiveController(this);
        controller.setListener(new LiveController.LiveControlListener() {
            @Override
            public boolean singleTap() {
                showChannelList();
                return true;
            }

            @Override
            public void longPress() {
                showSettingGroup();
            }

            @Override
            public void playStateChanged(int playState) {
                switch (playState) {
                    case VideoView.STATE_IDLE:
                    case VideoView.STATE_PAUSED:
                        break;
                    case VideoView.STATE_PREPARED:
                    case VideoView.STATE_BUFFERED:
                    case VideoView.STATE_PLAYING:

                        currentLiveChangeSourceTimes = 0;
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        break;
                    case VideoView.STATE_ERROR:
                    case VideoView.STATE_PLAYBACK_COMPLETED:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.post(mConnectTimeoutChangeSourceRun);
                        break;
                    case VideoView.STATE_PREPARING:
                    case VideoView.STATE_BUFFERING:
                        mHandler.removeCallbacks(mConnectTimeoutChangeSourceRun);
                        mHandler.postDelayed(mConnectTimeoutChangeSourceRun, (Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1) + 1) * 5000);
                        break;
                }
            }

            @Override
            public void changeSource(int direction) {
                if (direction > 0)
                    playNextSource();
                else
                    playPreSource();
            }
        });
        controller.setCanChangePosition(false);
        controller.setEnableInNormal(true);
        controller.setGestureEnabled(true);
        controller.setDoubleTapTogglePlayEnabled(false);
        mVideoView.setVideoController(controller);
        mVideoView.setProgressManager(null);
    }

    private Runnable mConnectTimeoutChangeSourceRun = () -> {
        currentLiveChangeSourceTimes++;
        if (currentChannel.getSourceIndex() == currentLiveChangeSourceTimes) {
            currentLiveChangeSourceTimes = 0;
            Integer[] groupChannelIndex = getNextChannel(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false) ? -1 : 1);
            playChannel(groupChannelIndex[0], groupChannelIndex[1], false);
        } else {
            playNextSource();
        }
    };

    private void initChannelGroupView() {
        mChannelGroupView.setHasFixedSize(true);
        mChannelGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelGroupAdapter = new LiveChannelGroupAdapter();
        mChannelGroupView.setAdapter(liveChannelGroupAdapter);
        mChannelGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mChannelGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectChannelGroup(position, true, -1);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                if (isNeedInputPassword(position)) {
                    showPasswordDialog(position, -1);
                }
            }
        });

        //手机/模拟器
        liveChannelGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectChannelGroup(position, false, -1);
            }
        });
    }

    private void selectChannelGroup(int groupIndex, boolean focus, int liveChannelIndex) {
        if (focus) {
            liveChannelGroupAdapter.setFocusedGroupIndex(groupIndex);
            liveChannelItemAdapter.setFocusedChannelIndex(-1);
        }
        if ((groupIndex > -1 && groupIndex != liveChannelGroupAdapter.getSelectedGroupIndex()) || isNeedInputPassword(groupIndex)) {
            liveChannelGroupAdapter.setSelectedGroupIndex(groupIndex);
            if (isNeedInputPassword(groupIndex)) {
                showPasswordDialog(groupIndex, liveChannelIndex);
                return;
            }
            loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
        }
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initLiveChannelView() {
        mLiveChannelView.setHasFixedSize(true);
        mLiveChannelView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveChannelItemAdapter = new LiveChannelItemAdapter();
        mLiveChannelView.setAdapter(liveChannelItemAdapter);
        mLiveChannelView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }
        });

        //电视
        mLiveChannelView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveChannelGroupAdapter.setFocusedGroupIndex(-1);
                liveChannelItemAdapter.setFocusedChannelIndex(position);
                mHandler.removeCallbacks(mHideChannelListRun);
                mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickLiveChannel(position);
            }
        });

        //手机/模拟器
        liveChannelItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickLiveChannel(position);
            }
        });
    }

    private void clickLiveChannel(int position) {
        liveChannelItemAdapter.setSelectedChannelIndex(position);
        playChannel(liveChannelGroupAdapter.getSelectedGroupIndex(), position, false);
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
            mHandler.removeCallbacks(mHideChannelListRun);
            mHandler.postDelayed(mHideChannelListRun, 5000);
        }
    }

    private void initSettingGroupView() {
        mSettingGroupView.setHasFixedSize(true);
        mSettingGroupView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingGroupAdapter = new LiveSettingGroupAdapter();
        mSettingGroupView.setAdapter(liveSettingGroupAdapter);
        mSettingGroupView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingGroupView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                selectSettingGroup(position, true);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
            }
        });

        //手机/模拟器
        liveSettingGroupAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                selectSettingGroup(position, false);
            }
        });
    }

    private void selectSettingGroup(int position, boolean focus) {
        if (focus) {
            liveSettingGroupAdapter.setFocusedGroupIndex(position);
            liveSettingItemAdapter.setFocusedItemIndex(-1);
        }
        if (position == liveSettingGroupAdapter.getSelectedGroupIndex() || position < -1)
            return;

        liveSettingGroupAdapter.setSelectedGroupIndex(position);
        liveSettingItemAdapter.setNewData(liveSettingGroupList.get(position).getLiveSettingItems());

        switch (position) {
            case 0:
                liveSettingItemAdapter.selectItem(currentChannel.getSourceIndex(), true, false);
                break;
            case 1:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerScale(), true, true);
                break;
            case 2:
                liveSettingItemAdapter.selectItem(livePlayerManager.getLivePlayerType(), true, true);
                break;
        }
        int scrollToPosition = liveSettingItemAdapter.getSelectedItemIndex();
        if (scrollToPosition < 0) scrollToPosition = 0;
        mSettingItemView.scrollToPosition(scrollToPosition);
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private void initSettingItemView() {
        mSettingItemView.setHasFixedSize(true);
        mSettingItemView.setLayoutManager(new V7LinearLayoutManager(this.mContext, 1, false));

        liveSettingItemAdapter = new LiveSettingItemAdapter();
        mSettingItemView.setAdapter(liveSettingItemAdapter);
        mSettingItemView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }
        });

        //电视
        mSettingItemView.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
            }

            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                if (position < 0) return;
                liveSettingGroupAdapter.setFocusedGroupIndex(-1);
                liveSettingItemAdapter.setFocusedItemIndex(position);
                mHandler.removeCallbacks(mHideSettingLayoutRun);
                mHandler.postDelayed(mHideSettingLayoutRun, 5000);
            }

            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {
                clickSettingItem(position);
            }
        });

        //手机/模拟器
        liveSettingItemAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FastClickCheckUtil.check(view);
                clickSettingItem(position);
            }
        });
    }

    private void clickSettingItem(int position) {
        int settingGroupIndex = liveSettingGroupAdapter.getSelectedGroupIndex();

        switch (settingGroupIndex) {
            case 0://线路切换
                selectSetItem(position);
                currentChannel.setSourceIndex(position);
                playChannel(currentChannel.getGroupIndex(), currentChannel.getIndex(), true);
                break;
            case 1://画面比例
                selectSetItem(position);
                livePlayerManager.changeLivePlayerScale(mVideoView, position, currentChannel.getName());
                break;
            case 2://播放解码
                selectSetItem(position);
                mVideoView.release();
                livePlayerManager.changeLivePlayerType(mVideoView, position, currentChannel.getName());
                mVideoView.setUrl(currentChannel.getUrl());
                mVideoView.start();
                break;
            case 3://超时换源
                selectSetItem(position);
                Hawk.put(HawkConfig.LIVE_CONNECT_TIMEOUT, position);
                break;
            case 4://
                selectSetItem(position);
                Hawk.put(HawkConfig.CHANNEL_GROUP_TYPE, position);
                ChannelHandler.clearCache();
                jumpActivity(this.getClass());
                finish();
                break;
            case 5:
                boolean select = false;
                switch (position) {
                    case 0:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_TIME, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_TIME, select);
                        showTime();
                        break;
                    case 1:
                        select = !Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false);
                        Hawk.put(HawkConfig.LIVE_SHOW_NET_SPEED, select);
                        showNetSpeed();
                        break;
                    case 2:
                        select = !Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false);
                        Hawk.put(HawkConfig.LIVE_CHANNEL_REVERSE, select);
                        break;
                    case 3:
                        select = !Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false);
                        Hawk.put(HawkConfig.LIVE_CROSS_GROUP, select);
                        break;
                }
                liveSettingItemAdapter.selectItem(position, select, false);
                break;
            case 6:
                selectSetItem(position);
                Toast.makeText(App.getInstance(), "修改源更新时间", Toast.LENGTH_SHORT).show();
                Hawk.put(HawkConfig.CACHE_CHANNEL_RESULT_UPDATE_TIME, position);
                break;
            case 7://系统设置
                boolean sysSelect = false;
                switch (position) {
                    case 0:
                        selectSetItem(position);
                        setDialog.show();
                        break;
                    case 1:
                        sysSelect = !Hawk.get(HawkConfig.DEBUG_OPEN, false);
                        Hawk.put(HawkConfig.DEBUG_OPEN, sysSelect);
                        if (sysSelect) {
                            getPPPPPP(this);
                            Log.logCatToFile();
                            Toast.makeText(App.getInstance(), "开启DEBUG", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(App.getInstance(), "关闭DEBUG", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                liveSettingItemAdapter.selectItem(position, sysSelect, false);
                break;
        }
        mHandler.removeCallbacks(mHideSettingLayoutRun);
        mHandler.postDelayed(mHideSettingLayoutRun, 5000);
    }

    private boolean selectSetItem(int position) {
        if (position == liveSettingItemAdapter.getSelectedItemIndex())
            return true;
        liveSettingItemAdapter.selectItem(position, true, true);
        return false;
    }


    private void getPPPPPP(Context context) {
        if (XXPermissions.isGranted(context, Permission.Group.STORAGE)) {
            Toast.makeText(context, "已获得存储权限", Toast.LENGTH_SHORT).show();
        } else {
            XXPermissions.with(context)
                    .permission(Permission.Group.STORAGE)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                Toast.makeText(context, "已获得存储权限", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                Toast.makeText(context, "获取存储权限失败,请在系统设置中开启", Toast.LENGTH_SHORT).show();
                                XXPermissions.startPermissionActivity(context, permissions);
                            } else {
                                Toast.makeText(context, "获取存储权限失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    private boolean isListOrSettingLayoutVisible() {
        return tvLeftChannelListLayout.getVisibility() == View.VISIBLE || tvRightSettingLayout.getVisibility() == View.VISIBLE;
    }

    private void initLiveSettingGroupList() {
        ArrayList<String> groupNames = new ArrayList<>(Arrays.asList("线路选择", "画面比例", "播放解码", "超时换源", "频道分组", "偏好设置", "更新间隔", "系统设置"));
        ArrayList<ArrayList<String>> itemsArrayList = new ArrayList<>();
        ArrayList<String> sourceItems = new ArrayList<>();
        ArrayList<String> scaleItems = new ArrayList<>(Arrays.asList("默认", "16:9", "4:3", "填充", "原始", "裁剪"));
        ArrayList<String> playerDecoderItems = new ArrayList<>(Arrays.asList("系统", "ijk硬解", "ijk软解", "exo"));
        ArrayList<String> timeoutItems = new ArrayList<>(Arrays.asList("5s", "10s", "15s", "20s", "25s", "30s"));
        ArrayList<String> personalSettingItems = new ArrayList<>(Arrays.asList("显示时间", "显示网速", "换台反转", "跨选分类"));
        ArrayList<String> settingItems = new ArrayList<>(Arrays.asList("设置源", "Debug"));
        ArrayList<String> channelGroupItems = new ArrayList<>(Arrays.asList("源默认", "自定义"));
        ArrayList<String> updateSourceTime = new ArrayList<>(Arrays.asList(ChannelHandler.getUpdateTime()));
        itemsArrayList.add(sourceItems);
        itemsArrayList.add(scaleItems);
        itemsArrayList.add(playerDecoderItems);
        itemsArrayList.add(timeoutItems);
        itemsArrayList.add(channelGroupItems);
        itemsArrayList.add(personalSettingItems);
        itemsArrayList.add(updateSourceTime);
        itemsArrayList.add(settingItems);


        liveSettingGroupList.clear();
        for (int i = 0; i < groupNames.size(); i++) {
            LiveSettingGroup liveSettingGroup = new LiveSettingGroup();
            ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
            liveSettingGroup.setGroupIndex(i);
            liveSettingGroup.setGroupName(groupNames.get(i));
            for (int j = 0; j < itemsArrayList.get(i).size(); j++) {
                LiveSettingItem liveSettingItem = new LiveSettingItem();
                liveSettingItem.setItemIndex(j);
                liveSettingItem.setItemName(itemsArrayList.get(i).get(j));
                liveSettingItemList.add(liveSettingItem);
            }
            liveSettingGroup.setLiveSettingItems(liveSettingItemList);
            liveSettingGroupList.add(liveSettingGroup);
        }
        //"线路选择", "画面比例", "播放解码", "超时换源", "频道分组", "偏好设置","更新间隔", "系统设置"
        int index = 0;//线路选择
        //do something
        index++;//画面比例
        //do something
        index++;//播放解码
        //do something
        index++;//超时换源
        liveSettingGroupList.get(index).getLiveSettingItems().get(Hawk.get(HawkConfig.LIVE_CONNECT_TIMEOUT, 1)).setItemSelected(true);
        index++;//频道分组
        liveSettingGroupList.get(index).getLiveSettingItems().get(Hawk.get(HawkConfig.CHANNEL_GROUP_TYPE, 1)).setItemSelected(true);
        index++;//偏好设置
        liveSettingGroupList.get(index).getLiveSettingItems().get(0).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_TIME, false));
        liveSettingGroupList.get(index).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false));
        liveSettingGroupList.get(index).getLiveSettingItems().get(2).setItemSelected(Hawk.get(HawkConfig.LIVE_CHANNEL_REVERSE, false));
        liveSettingGroupList.get(index).getLiveSettingItems().get(3).setItemSelected(Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false));
        index++;//更新间隔
        liveSettingGroupList.get(index).getLiveSettingItems().get(Hawk.get(HawkConfig.CACHE_CHANNEL_RESULT_UPDATE_TIME, 2)).setItemSelected(true);
        index++;//系统设置
        liveSettingGroupList.get(index).getLiveSettingItems().get(1).setItemSelected(Hawk.get(HawkConfig.DEBUG_OPEN, false));


    }

    private void loadCurrentSourceList() {
        ArrayList<String> currentSourceNames = currentChannel.getSourceNames();
        ArrayList<LiveSettingItem> liveSettingItemList = new ArrayList<>();
        for (int j = 0; j < currentSourceNames.size(); j++) {
            LiveSettingItem liveSettingItem = new LiveSettingItem();
            liveSettingItem.setItemIndex(j);
            liveSettingItem.setItemName(currentSourceNames.get(j));
            liveSettingItemList.add(liveSettingItem);
        }
        liveSettingGroupList.get(0).setLiveSettingItems(liveSettingItemList);
    }

    void showTime() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_TIME, false)) {
            mHandler.post(mUpdateTimeRun);
            tvTime.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateTimeRun);
            tvTime.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateTimeRun = new Runnable() {
        @Override
        public void run() {
            Date day = new Date();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            tvTime.setText(df.format(day));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showNetSpeed() {
        if (Hawk.get(HawkConfig.LIVE_SHOW_NET_SPEED, false)) {
            mHandler.post(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.VISIBLE);
        } else {
            mHandler.removeCallbacks(mUpdateNetSpeedRun);
            tvNetSpeed.setVisibility(View.GONE);
        }
    }

    private Runnable mUpdateNetSpeedRun = new Runnable() {
        @Override
        public void run() {
            if (mVideoView == null) return;
            tvNetSpeed.setText(String.format("%.2fMB/s", (float) mVideoView.getTcpSpeed() / 1024.0 / 1024.0));
            mHandler.postDelayed(this, 1000);
        }
    };

    private void showPasswordDialog(int groupIndex, int liveChannelIndex) {
        if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
            mHandler.removeCallbacks(mHideChannelListRun);

        LivePasswordDialog dialog = new LivePasswordDialog(this);
        dialog.setOnListener(new LivePasswordDialog.OnListener() {
            @Override
            public void onChange(String password) {
                if (password.equals(channelGroupList.get(groupIndex).getGroupPassword())) {
                    channelGroupPasswordConfirmed.add(groupIndex);
                    loadChannelGroupDataAndPlay(groupIndex, liveChannelIndex);
                } else {
                    Toast.makeText(App.getInstance(), "密码错误", Toast.LENGTH_SHORT).show();
                }

                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE)
                    mHandler.postDelayed(mHideChannelListRun, 5000);
            }

            @Override
            public void onCancel() {
                if (tvLeftChannelListLayout.getVisibility() == View.VISIBLE) {
                    int groupIndex = liveChannelGroupAdapter.getSelectedGroupIndex();
                    liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
                }
            }
        });
        dialog.show();
    }

    private void loadChannelGroupDataAndPlay(int groupIndex, int liveChannelIndex) {
        liveChannelItemAdapter.setNewData(getLiveChannels(groupIndex));
        if (currentChannel != null && groupIndex == currentChannel.getGroupIndex()) {
            if (currentChannel.getIndex() > -1)
                mLiveChannelView.scrollToPosition(currentChannel.getIndex());
            liveChannelItemAdapter.setSelectedChannelIndex(currentChannel.getIndex());
        } else {
            mLiveChannelView.scrollToPosition(0);
            liveChannelItemAdapter.setSelectedChannelIndex(-1);
        }

        if (liveChannelIndex > -1) {
            clickLiveChannel(liveChannelIndex);
            mChannelGroupView.scrollToPosition(groupIndex);
            mLiveChannelView.scrollToPosition(liveChannelIndex);
//            playChannel(groupIndex, liveChannelIndex, false);
        }
    }

    private boolean isNeedInputPassword(int groupIndex) {
        return !channelGroupList.get(groupIndex).getGroupPassword().isEmpty()
                && !isPasswordConfirmed(groupIndex);
    }

    private boolean isPasswordConfirmed(int groupIndex) {
        for (Integer confirmedNum : channelGroupPasswordConfirmed) {
            if (confirmedNum == groupIndex)
                return true;
        }
        return false;
    }

    private ArrayList<Channel> getLiveChannels(int groupIndex) {
        if (!isNeedInputPassword(groupIndex) && channelGroupList.size() > groupIndex) {
            return channelGroupList.get(groupIndex).getChannels();
        } else {
            return new ArrayList<>();
        }
    }

    private Integer[] getNextChannel(int direction) {
        int channelGroupIndex = currentChannel.getGroupIndex();
        int liveChannelIndex = currentChannel.getIndex();

        //跨选分组模式下跳过加密频道分组（遥控器上下键换台/超时换源）
        if (direction > 0) {
            liveChannelIndex++;
            if (liveChannelIndex >= getLiveChannels(channelGroupIndex).size()) {
                liveChannelIndex = 0;
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex++;
                        if (channelGroupIndex >= channelGroupList.size())
                            channelGroupIndex = 0;
                    } while (!channelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannel.getGroupIndex());
                }
            }
        } else {
            liveChannelIndex--;
            if (liveChannelIndex < 0) {
                if (Hawk.get(HawkConfig.LIVE_CROSS_GROUP, false)) {
                    do {
                        channelGroupIndex--;
                        if (channelGroupIndex < 0)
                            channelGroupIndex = channelGroupList.size() - 1;
                    } while (!channelGroupList.get(channelGroupIndex).getGroupPassword().isEmpty() || channelGroupIndex == currentChannel.getGroupIndex());
                }
                liveChannelIndex = getLiveChannels(channelGroupIndex).size() - 1;
            }
        }

        Integer[] groupChannelIndex = new Integer[2];
        groupChannelIndex[0] = channelGroupIndex;
        groupChannelIndex[1] = liveChannelIndex;

        return groupChannelIndex;
    }

    private int getFirstNoPasswordChannelGroup() {
        for (ChannelGroup channelGroup : channelGroupList) {
            if (channelGroup.getGroupPassword().isEmpty() && !channelGroup.getChannels().isEmpty())
                return channelGroup.getIndex();
        }
        return -1;
    }

}