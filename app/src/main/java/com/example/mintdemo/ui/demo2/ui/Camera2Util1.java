package com.example.mintdemo.ui.demo2.ui;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import androidx.annotation.NonNull;

import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.Tool.Camera.YUV_420_888toNV21;
import com.example.mintdemo.Tool.Pictures.BitmapCut;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Camera2Utils1 {

    private static final String TAG = "Camera2Utils";

    // 单例模式
    private static Camera2Utils1 instance;

    // 相机相关变量
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size previewSize;
    private ImageReader imageReader;

    // 预览相关变量
    private TextureView textureView;
    private SurfaceTexture surfaceTexture;
    private Surface previewSurface;

    // 拍照相关变量
    private boolean isCapturing = false;
    private OnCaptureListener onCaptureListener;

    private Context context;
    private String  cameraId;
    private String[] cameraIds;

    // 构造函数私有化，保证单例模式
    private Camera2Utils1() { }

    // 获取单例对象
    public static synchronized Camera2Utils1 getInstance() {
        if (instance == null) {
            instance = new Camera2Utils1();
        }
        return instance;
    }
    /**
     * 环境参数初始化
     */
    public Camera2Utils1 setContext(Context context) {
        this.context = context;
        initCameraParams();
        return this;
    }
    /**
     * 设置摄像头位置
     */
    public Camera2Utils1 setCameraID(int cameraID) {
        if(cameraID<=cameraIds.length){
            this.cameraId= cameraIds[cameraID];
        }
        return this;
    }
    /**
     * 设置摄像头预览图片尺寸
     */
    public Camera2Utils1 setPreviewSize(Size previewSize) {
        chooseOptimalSize(previewSize.getWidth(),previewSize.getHeight());
        return this;
    }

    /**
     * 自动初始化相机镜头参数
     */
    private void initCameraParams() {
        try {
            // 获取相机管理器
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            // 获取可用的相机列表
            cameraIds = cameraManager.getCameraIdList();
            // 遍历相机列表，找到后置摄像头
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK) {
                    this.cameraId = cameraId;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选择最适合的预览尺寸
     * @return 最适合的预览尺寸
     */
    private void chooseOptimalSize(int width,int height) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            List<Size> bigEnough = new ArrayList<>();
            List<Size> notBigEnough = new ArrayList<>();
            for (Size size : sizes) {
                if (size.getWidth() >= width && size.getHeight() >= height) {
                    bigEnough.add(size);
                } else {
                    notBigEnough.add(size);
                }
            }
            if (bigEnough.size() > 0) {
                previewSize = Collections.min(bigEnough, new CompareSizesByArea());
            } else if (notBigEnough.size() > 0) {
                previewSize = Collections.max(notBigEnough, new CompareSizesByArea());
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size");
                previewSize = sizes[0];
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            previewSize = new Size(320,320);
        }
        imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
        imageReader.setOnImageAvailableListener(reader -> {
            isCapturing = false;
            if (onCaptureListener != null) {
                Image image = reader.acquireNextImage();
                if (image != null) {
                    Bitmap bitmap = new YUV_420_888toNV21().YUV_420_888ToBitmap(image);
                    onCaptureListener.onCapture(bitmap);
                    image.close();
                }
            }
        }, null);
    }

    /**
     * 初始化相机
     * @param textureView 预览控件
     */
    @SuppressLint("MissingPermission")
    public void initCamera(TextureView textureView) {
        if(previewSize==null)chooseOptimalSize(320,320);
        this.textureView = textureView;
        surfaceTexture = textureView.getSurfaceTexture();
        if (surfaceTexture == null) {
            Log.e(TAG, "SurfaceTexture is null");
            return;
        }
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        previewSurface = new Surface(surfaceTexture);
        try {
            // 打开相机
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照
     * @param onCaptureListener 拍照回调
     */
    public void takePicture(OnCaptureListener onCaptureListener) {
        if (isCapturing) {
            Log.e(TAG, "Camera is capturing");
            return;
        }
        this.onCaptureListener = onCaptureListener;
        try {
            // 创建拍照请求
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(imageReader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, null);
            isCapturing = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        closeCamera();
        if (cameraId.equals(CameraCharacteristics.LENS_FACING_BACK)) {
            cameraId = CameraCharacteristics.LENS_FACING_FRONT+"";
        } else {
            cameraId = CameraCharacteristics.LENS_FACING_BACK+"";
        }
        initCamera(textureView);
    }

    /**
     * 关闭预览或者后台
     */
    public void closeCamera() {
        try {
            if (cameraCaptureSession != null) {
                cameraCaptureSession.stopRepeating();
                cameraCaptureSession.abortCaptures();
                cameraCaptureSession.close();
                cameraCaptureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (imageReader != null) {
                imageReader.close();
                imageReader = null;
            }
            isCapturing = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 相机状态回调
    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            try {
                // 创建预览请求
                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(previewSurface);
                // 创建相机捕获会话
                cameraDevice.createCaptureSession(Arrays.asList(previewSurface, imageReader.getSurface()), captureSessionCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    // 相机捕获会话回调
    private CameraCaptureSession.StateCallback captureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            cameraCaptureSession = session;
            try {
                // 设置自动对焦和自动曝光
                captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                // 开始预览
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            Log.e(TAG, "Camera capture session configuration failed");
        }
    };

    // 拍照回调
    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            isCapturing = false;
            if (onCaptureListener != null) {
                Image image = imageReader.acquireNextImage();
                if (image != null) {
                    Bitmap bitmap = new YUV_420_888toNV21().YUV_420_888ToBitmap(image);
                    onCaptureListener.onCapture(bitmap);
                    image.close();
                }
            }
        }
    };

    // 拍照回调接口
    public interface OnCaptureListener {
        void onCapture(Bitmap image);
    }

    // 比较尺寸大小的比较器
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}
