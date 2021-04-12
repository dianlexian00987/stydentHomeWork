package com.telit.zhkt_three.ScreenLive;

import android.content.Context;
import android.content.Intent;
import android.view.SurfaceView;

/**
 * Created by gavin on 2018/1/23.
 */

public class PusherContract {
    public interface View extends BaseView<Presenter> {
        void changeViewStatus(int status, String URL);
    }

    public interface Presenter extends BasePresenter {
        void initView(Context context);
        /**
         *
         * @param context
         * @param pushDev 0 - 横屏  1 - 竖屏 2-前摄像头 3-后置摄像头 4-停止
         */
        void onStartPush(Context context, int pushDev, Intent capScreenIntent,
                         int capScreenCode, SurfaceView mSurfaceView);
        void onStopPush();
        int  getPushStatus();
        void onStartPushSuccess(Context context, int isEnableMulticast, String URL);
        void onStartPushFail(Context context, int result);
        void onStopPushSuccess(Context context);
    }
}
