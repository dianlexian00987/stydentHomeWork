package com.telit.zhkt_three.Service;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.telit.zhkt_three.Activity.HomeScreen.MainActivity;
import com.telit.zhkt_three.BuildConfig;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.R;
import com.zbv.basemodel.AutoUpdateAccessService;
import com.zbv.basemodel.OkHttp3_0Utils;
import com.zbv.basemodel.QZXTools;
import com.zbv.basemodel.UpdateAppBean;
import com.zbv.basemodel.UpdateBean;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class UpDataApkServer extends Service {
    private MyHandler myHandler = new MyHandler();
    private static final int Update_App_Dialog = 0;
    private static final int Update_Check_Failed = 1;
    private static final int Is_New_Version = 2;
    private static final int NEET_SERVER = 3;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestCheckVersion();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //创建通知
        createNotification();

        return super.onStartCommand(intent, flags, startId);
    }

    private void requestCheckVersion() {
        String url = UrlUtils.BaseUrl + UrlUtils.AppUpdate;
        // url=UrlUtils.BaseUrl+url;
        // String url = "http://192.168.110.207:8080/download/wisdomclass.apk";
        //String url = "http://resource.ahtelit.com/filesystem/softupdate/wisdomclass-v3.0.apk";

        OkHttp3_0Utils.getInstance(this).asyncGetOkHttp(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    return;
                }
                QZXTools.logE("检测更新版本失败 " + e, null);
                myHandler.sendEmptyMessage(Update_Check_Failed);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resultJson = response.body().string();
                    QZXTools.logE("resultJson=" + resultJson, null);
                    try {
                        Gson gson = new Gson();
                        UpdateAppBean updateAppBean = gson.fromJson(resultJson, UpdateAppBean.class);
                        UpdateBean updateBean = updateAppBean.getResult().get(0);
                        int currentCode = QZXTools.getVersionCode(MyApplication.getInstance());
                        int newCode = updateBean.getVersionCode();
                        if (currentCode < newCode) {
                            Message message = myHandler.obtainMessage();
                            message.what = Update_App_Dialog;
                            message.obj = updateBean;
                            myHandler.sendMessage(message);
                        } else {

                            myHandler.sendEmptyMessage(Is_New_Version);
                        }
                    } catch (Exception e) {
                        e.fillInStackTrace();
                        myHandler.sendEmptyMessage(NEET_SERVER);
                    }

                } else {
                    QZXTools.logE("检测版本应该是404", null);
                    myHandler.sendEmptyMessage(Update_Check_Failed);
                }
            }
        });
    }

    public class MyHandler extends Handler {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NEET_SERVER:
                    //  QZXTools.popToast(MyApplication.getInstance(),"当前网络不佳....",true);
                    break;
                case Update_App_Dialog:

                    //有新版本
                    UpdateBean updateBean = (UpdateBean) msg.obj;
                    String updateUrl = updateBean.getUpdateUrl();

                    //开始下载
                    if (!TextUtils.isEmpty(updateUrl)) {

//                        startUpdataApk(updateUrl);

                        startDownloadUpdateApk(updateUrl);
                    }
                    break;
                case Update_Check_Failed:
                    Toast.makeText(MyApplication.getInstance(), "检测更新失败，请重试！", Toast.LENGTH_SHORT).show();
                    break;
                case Is_New_Version:
                    //Log.i(TAG, ": Is_New_Version"+Is_New_Version);
                    //Toast.makeText(MyApplication.getInstance(), "已是最新版本", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startUpdataApk(String downloadUrl) {
        if (downloadUrl != null) {
        QZXTools.logE("qin0006666 ............我是服务开始下载apk ...........11111111111111",null);

            OkHttp3_0Utils.getInstance(this).downloadSingleFileForOnce(downloadUrl, null,
                    new OkHttp3_0Utils.DownloadCallback() {
                        @Override
                        public void downloadProcess(int value) {
                            QZXTools.logE("qin0006666 ............我是服务开始下载apk ...........22222222...."+value,null);
                        }

                        @Override
                        public void downloadComplete(String filePath) {
                            QZXTools.logE("qin0006666 ............我是服务下载apk完成 ...........3333333",null);

                            SharedPreferences sharedPreferences = getSharedPreferences("access_mode", Context.MODE_PRIVATE);
                            boolean hadAccess = sharedPreferences.getBoolean("had_access", false);
                            sharedPreferences.edit().putString("localFilePathApk", filePath).commit();


                            if (hadAccess) {
                                AutoUpdateAccessService.INVOKE_TYPE = AutoUpdateAccessService.TYPE_INSTALL_APP;

                            } else {
                                AutoUpdateAccessService.reset();
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                boolean b = getPackageManager().canRequestPackageInstalls();
                                if (b) {

                                } else {

                                }
                                QZXTools.installApk(UpDataApkServer.this, filePath);
                            } else {
                                QZXTools.installApk(UpDataApkServer.this, filePath);
                            }


                        }

                        @Override
                        public void downloadFailure() {
                            QZXTools.logE("下载失败",null);
                        }
                    },"upDataApk");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startDownloadUpdateApk(String downloadUrl) {
        if (downloadUrl != null) {
            String parentDir = QZXTools.getRootStorageForFiles(MyApplication.getInstance()) + File.separator+"Download";
            PostRequest<File> request = OkGo.<File>post(downloadUrl);

            OkDownload.request(downloadUrl, request)
                    .folder(parentDir)
                    .save()
                    .register(new DownloadListener(this) {
                        @Override
                        public void onStart(Progress progress) {
                            QZXTools.logE("onStart",null);
                        }

                        @Override
                        public void onProgress(Progress progress) {
                            QZXTools.logE("onProgress："+""+progress.currentSize+" "+progress.totalSize,null);
                        }

                        @Override
                        public void onError(Progress progress) {
                            QZXTools.logE("onError",null);
                        }

                        @Override
                        public void onFinish(File file, Progress progress) {
                            QZXTools.logE("onFinish",null);

                            if (file!=null&&file.length()>0){
                                installApk(file.getPath());
                            }
                        }

                        @Override
                        public void onRemove(Progress progress) {
                            QZXTools.logE("onRemove",null);
                        }
                    })
            .start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void installApk(String filePath){
        SharedPreferences sharedPreferences = MyApplication.getInstance()
                .getSharedPreferences("access_mode", Context.MODE_PRIVATE);
        boolean hadAccess = sharedPreferences.getBoolean("had_access", false);
        sharedPreferences.edit().putString("localFilePathApk",filePath).commit();

        if (hadAccess) {
            AutoUpdateAccessService.INVOKE_TYPE = AutoUpdateAccessService.TYPE_INSTALL_APP;

        } else {
            AutoUpdateAccessService.reset();
        }

        QZXTools.installApk(this, filePath);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消下载
        QZXTools.logE("qin0006666 ............服务关闭下载取消 ..........",null);

        OkHttp3_0Utils.getInstance(this).cancelTagRequest("upDataApk");

        stopForeground(true);
    }

    /**
     * 创建通知
     */
    private void createNotification(){
        String channelId = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = BuildConfig.APPLICATION_ID + ".server";
            String channelName = "录屏";
            NotificationChannel chan = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);

        String title = getAppName(MyApplication.getInstance());

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("下載中...")
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(2, builder.build());
    }

    /**
     * 获取应用名称
     *
     * @param context
     * @return
     */
    public String getAppName(Context context) {
        if (context == null) {
            return null;
        }
        try {
            PackageManager packageManager = context.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(context.getApplicationInfo()));
        } catch (Throwable e) {
        }
        return null;
    }
}
