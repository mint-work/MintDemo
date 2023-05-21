package com.example.mintdemo.ui.demo2.tool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * 连接网络摄像头工具类
 */
public class CameraClient {
    private static final String TAG = "CameraClient";
    private String mServerAddress;
    private int mServerPort;
    private Socket mSocket;
    private InputStream mInputStream;
    private boolean mIsRunning;

    //初始化服务器
    public CameraClient(String serverAddress, int serverPort) {
        mServerAddress = serverAddress;
        mServerPort = serverPort;
    }

    //启动
    public void start(Outcome outcome) {
        new Thread(() -> {
            try {
                mSocket = new Socket(mServerAddress, mServerPort);
                mInputStream = new BufferedInputStream(mSocket.getInputStream());
                mIsRunning = true;
                int i = 0;
                while (mIsRunning) {
                    Bitmap bitmap = BitmapFactory.decodeStream(mInputStream);
                    if (bitmap != null) {
                        outcome.succeed(bitmap);
                    } else {
                        Thread.sleep(50); // 延迟 1 秒钟
                        Log.e(TAG, "正在连接第 " + i + " 次");
                        i++;
                        if (i > 5) { outcome.Fail("尝试连接次数超限制");return; }
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "无法连接到服务器", e);
                outcome.Fail("无法连接到服务器，连接超时请检查地址");
                close();
            } catch (InterruptedException e) {
                e.printStackTrace();
                close();
            } finally {
                close();
            }
        }).start();
    }

    //暂停
    public void stop() {
        mIsRunning = false;
        close();
    }

    //关闭
    private void close() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "无法关闭", e);
        }
    }

    /**
     * @创建时间 2022/9/17-0:06
     * @创建作者 Mint
     * command 控制方向
     * onestep 控制启动停止
     * @返回:
     * @注释: 控制摄像转动
     */
    public void camera_control(int command, int onestep) {
        try {
            URL url = new URL("http://" + mServerAddress + ":" + mServerPort + "/decoder_control.cgi?loginuse=admin&loginpas=888888&command=" + command + "&onestep=" + onestep);
            try {
                URLConnection urlConnection = url.openConnection();
                urlConnection.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, "请检查是否连接小车");
        }
    }

    public static interface Outcome {
        void succeed(Bitmap bitmap);

        void Fail(String e);
    }
}
