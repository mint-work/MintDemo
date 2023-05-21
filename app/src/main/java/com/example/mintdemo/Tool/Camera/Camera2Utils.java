package com.example.mintdemo.Tool.Camera;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;


/**
 * 使用 Camera2 API 进行相机开发的一般流程如下：
 * 获取相机权限：在 AndroidManifest.xml 文件中添加相机权限声明，然后在运行时请求用户授权。
 * 获取 CameraManager 实例：调用 Context.getSystemService() 方法获取系统服务的 CameraManager 对象。
 * 获取相机列表：调用 CameraManager 的 getCameraIdList() 方法获取可用相机的 ID 列表。
 * 打开相机：调用 CameraManager 的 openCamera() 方法打开指定 ID 的相机，并实现 CameraDevice.StateCallback 接口的回调方法，以处理相机打开和关闭事件。
 * 创建预览会话：在相机打开后，调用 CameraDevice 的 createCaptureSession() 方法创建一个预览会话，并实现 CameraCaptureSession.StateCallback 接口的回调方法，以处理会话创建和关闭事件。
 * 配置预览请求：使用 CaptureRequest.Builder 对象创建一个预览请求，并设置预览尺寸、帧率、对焦模式等参数。
 * 开始预览：调用 CameraCaptureSession 的 setRepeatingRequest() 方法开始预览。
 * 拍照：创建一个拍照请求，并调用 CameraCaptureSession 的 capture() 方法拍照。
 * 处理预览帧：通过实现 CameraCaptureSession.CaptureCallback 接口的回调方法，可以获取每一帧预览图像数据，进行图像处理。
 */

public class Camera2Utils {
    private static final String TAG = "Camera2Utils";
    private static Camera2Utils instance;
    public CameraManager cameraManager; //相机管理器
    private CameraDevice cameraDevice;  //摄像设备
    private CameraCaptureSession captureSession;//相机捕获
    private ImageReader imageReader; //预览图片参数
    private CaptureRequest.Builder captureBuilder;
    public String cameraID; //摄像头id
    public Size previewSize; //预览图片尺寸
    public TextureView textureView; //预览窗口

    public Context context;

    /**
     * 单利模式保证系统只有一个相机
     */
    public static Camera2Utils getInstance() {
        if (instance == null) {
            synchronized (Camera2Utils.class) {
                if (instance == null) {
                    instance = new Camera2Utils();
                }
            }
        }
        return instance;
    }

    /**
     * 权限检查
     */
    private Boolean openCameraDevice() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG,"相机权限请检查权限");
            return false;
        }
        return true;
    }

    /**
     * 判断是否有相机
     */
    private Boolean isCamera2Supported() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e(TAG,"设备没有相机");
            return false;
        }
        return true;
    }

    /**
     * 获取后置摄像头id
     */
    private String setUpCamera() {
        String[] cameraIds;
        try {
            cameraIds = cameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG,e.toString());
        }
        return "0";
    }

    /**
     * 获取最优的相机预览尺寸
     *
     * @param sizes 支持的尺寸列表
     * @return 最优的尺寸
     */
    private Size getOptimalPreviewSize(Size[] sizes) {
        int maxWidth = 1920;
        int maxHeight = 1080;
        Size optimalSize = null;
        for (Size size : sizes) {
            if (size.getWidth() <= maxWidth && size.getHeight() <= maxHeight) {
                if (optimalSize == null || size.getWidth() * size.getHeight() > optimalSize.getWidth() * optimalSize.getHeight()) {
                    optimalSize = size;
                }
            }
        }
        if (optimalSize == null) {
            optimalSize = sizes[0];
        }
        return optimalSize;
    }

    /**
     * 曝光率最优
     */
    private Range<Integer> getRange(String cameraID) {
        CameraManager mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics chars = null;
        try {
            chars = mCameraManager.getCameraCharacteristics(cameraID);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        Range<Integer> result = null;
        for (Range<Integer> range : ranges) {
            //帧率不能太低，大于10
            if (range.getLower() < 10)
                continue;
            if (result == null)
                result = range;
                //FPS下限小于15，弱光时能保证足够曝光时间，提高亮度。range范围跨度越大越好，光源足够时FPS较高，预览更流畅，光源不够时FPS较低，亮度更好。
            else if (range.getLower() <= 15 && (range.getUpper() - range.getLower()) > (result.getUpper() - result.getLower()))
                result = range;
        }
        return result;
    }


    /**
     * 环境参数初始化
     */
    public Camera2Utils setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * 设置摄像头位置
     */
    public Camera2Utils setCameraID(int cameraID) {
        this.cameraID = String.valueOf(cameraID);
        return this;
    }

    /**
     * 设置摄像头预览图片尺寸
     */
    public Camera2Utils setPreviewSize(Size previewSize) {
        this.previewSize = previewSize;
        return this;
    }

    /**
     * 设置摄像头预览位置
     */
    public Camera2Utils setTextureView(TextureView textureView) {
        this.textureView = textureView;
        return this;
    }

    /**
     * 初始化 Camera2 相机
     */
    public Camera2Utils initCamera2(StartupResults startupResults) throws CameraAccessException {
        if(!openCameraDevice()&&!isCamera2Supported()){//权限检查 检查相机是否有相机硬件
            startupResults.onFailure(null);
        }
        if (cameraID == null) {
            cameraID = setUpCamera();//获取后置摄像头id
        }
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        if (previewSize == null) {
            StreamConfigurationMap map = cameraManager.getCameraCharacteristics(cameraID).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = getOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class));
        }
        imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 1);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Size finalPreviewSize = previewSize;
            cameraManager.openCamera(cameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    try {
                        captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                captureSession = cameraCaptureSession;
                                try {
                                    captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                    cameraCaptureSession.setRepeatingRequest(captureBuilder.build(), null, null);
                                    startupResults.onSuccess();
                                } catch (CameraAccessException e) {
                                    Log.e(TAG,"无法启动相机预览"+e.getMessage());
                                    startupResults.onFailure(e);
                                }
                            }
                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                Log.e(TAG,"无法配置相机捕获会话");
                                startupResults.onFailure(null);
                            }
                        };
                        if (textureView != null) {
                            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
                            surfaceTexture.setDefaultBufferSize(finalPreviewSize.getWidth(), finalPreviewSize.getHeight());
                            Surface previewSurface = new Surface(surfaceTexture);
                            Surface imageReaderSurface = imageReader.getSurface();
                            captureBuilder.addTarget(previewSurface);
                            captureBuilder.addTarget(imageReaderSurface);
                            cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReaderSurface), stateCallback, null);

                        } else {
                            Surface imageReaderSurface = imageReader.getSurface();
                            captureBuilder.addTarget(imageReaderSurface);
                            cameraDevice.createCaptureSession(Arrays.asList(imageReaderSurface), stateCallback, null);

                        }
                    } catch (CameraAccessException e) {
                        Log.e(TAG,"捕获服务创建失败"+ e.getMessage());
                        startupResults.onFailure(e);
                    }


                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    closeCamera();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    closeCamera();
                }
            }, null);
        } else {
            Log.e(TAG,"打开相机失败");
            startupResults.onFailure(null);
            return null;
        }
        return this;
    }

    /**
     * 拍照并获取 Bitmap
     *
     * @param callback 拍照回调
     */
    public void takePicture(final OnTakePictureCallback callback) throws CameraAccessException {
        CaptureRequest captureRequest = captureBuilder.build();
        captureSession.capture(captureRequest, new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                // 获取 ImageReader 中的图像数据
                Image image = imageReader.acquireLatestImage();
                if (image == null) {
                    Bitmap bitmap = new YUV_420_888toNV21().YUV_420_888ToBitmap(image);
                    image.close();
                    callback.onSuccess(bitmap);
                }else {
                    Log.e(TAG,"捕获的图片为空");
                }

            }

            @Override
            public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                super.onCaptureFailed(session, request, failure);
                Log.e(TAG,"图片捕获失败" + failure.toString());
            }
        }, null);
    }

    /**
     * 切换相机
     * 注意如果有参数变化要集合改变
     */
    public void switchCameras(int cameraID,StartupResults startupResults) {
        closeCamera();
        setCameraID(cameraID);
        try {
            initCamera2(startupResults);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }


    /**
     * 关闭相机
     */
    public void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    /**
     * 拍照回调接口
     */
    public interface OnTakePictureCallback {
        void onSuccess(Bitmap bitmap);
    }
    /**
     * 相机启动结果
     */
    public interface StartupResults{
        void onSuccess();
        void onFailure(CameraAccessException e);
    }

}
