package com.example.mintdemo.ui.demo2.tool;

/**
 * 相机工具类
 */
public class CameraFunction {
    private static CameraFunction instance;
    private CameraFunction (){}
    public static synchronized CameraFunction getInstance() {
        if (instance == null) {
            instance = new CameraFunction();
        }
        return instance;
    }






    /**
     * 获取相机参数
     */
    public CameraData cameraParameters(){

        return null;
    }
    /**
     * 设置相机参数
     * 适配重新设置相机参数
     */
    public void setCamera(){

    }
    /**
     * 打开相机
     */
   public void openCamera(){

   }
    /**
     * 切换相机
     */
    public void switchCameras(){

    }
    /**
     * 关闭相机
     */
    public void offCamera(){

    }
}
