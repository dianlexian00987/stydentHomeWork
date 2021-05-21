package com.telit.zhkt_three.Service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.telit.zhkt_three.Constant.UrlUtils;
import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.Utils.CheckVersionUtil;
import com.zbv.basemodel.AutoUpdateAccessService;
import com.zbv.basemodel.FileReceiveDialog;
import com.zbv.basemodel.OkHttp3_0Utils;
import com.zbv.basemodel.QZXTools;
import com.zbv.basemodel.TipsDialog;
import com.zbv.basemodel.UpdateAppBean;
import com.zbv.basemodel.UpdateBean;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.Provider;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpDataApkServer extends Service {
    private MyHandler myHandler = new MyHandler();
    ;
    private static final int Update_App_Dialog = 0;
    private static final int Update_Check_Failed = 1;
    private static final int Is_New_Version = 2;
    private static final int NEET_SERVER = 3;
    public static final int INSTALL_PACKAGES_REQUEST_CODE = 0x7;

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

                        startUpdataApk(updateUrl);
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


            OkHttp3_0Utils.getInstance(this).downloadSingleFileForOnce(downloadUrl, null,
                    new OkHttp3_0Utils.DownloadCallback() {
                        @Override
                        public void downloadProcess(int value) {

                        }

                        @Override
                        public void downloadComplete(String filePath) {


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
                                    //请求安装未知应用来源的权限
                                 /*   ActivityCompat.requestPermissions((Activity) mContext,
                                            new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES},
                                            INSTALL_PACKAGES_REQUEST_CODE);*/
                                }
                                QZXTools.installApk(UpDataApkServer.this, filePath);
                            } else {
                                QZXTools.installApk(UpDataApkServer.this, filePath);
                            }


                        }

                        @Override
                        public void downloadFailure() {

                        }
                    });
        }
    }


}
