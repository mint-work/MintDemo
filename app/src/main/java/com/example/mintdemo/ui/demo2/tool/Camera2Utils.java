package com.example.mintdemo.ui.demo2.tool;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.nio.ByteBuffer;
import java.util.Collections;


public class Camera2Utils {
    public CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private CaptureRequest.Builder captureBuilder;


    private static final String TAG = "Camera2Utils";
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    public Context context;

    public Camera2Utils(Context context) {
        this.context = context;
    }

    //判断是否有相机
    private boolean isCamera2Supported() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    //获取后置摄像头id
    private String setUpCamera() {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
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
            throw new RuntimeException(e);
        }
        return null;
    }





        /**
         * 初始化 Camera2 相机
         */
        public void initCamera2() {
            if(!isCamera2Supported()){//判读手机硬件是否支持相机
                return;
            }
            String lensFacingBack = setUpCamera();//获取后置摄像头id
            if (lensFacingBack != null) {
                    // 获取相机预览尺寸
                StreamConfigurationMap map = null;
                try {
                    map = cameraManager.getCameraCharacteristics(lensFacingBack).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                    Size previewSize = getOptimalPreviewSize(map.getOutputSizes(SurfaceTexture.class));
                    // 创建 ImageReader
                    imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
                    // 打开相机
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraManager.openCamera(lensFacingBack,new CameraDevice.StateCallback() {
                            @Override
                            public void onOpened(@NonNull CameraDevice camera) {
                                cameraDevice = camera;

//                                // 准备拍照
//                                try {
//                                    // 创建 CaptureRequest.Builder 实例
//                                    final CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//                                    // 设置使用的摄像头设备
//                                    requestBuilder.addTarget(imageReader.getSurface());
//                                    // 获取 CameraCaptureSession 实例
//                                    cameraDevice.createCaptureSession(Arrays.asList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
//                                        @Override
//                                        public void onConfigured(@NonNull CameraCaptureSession session) {
//                                            // CameraCaptureSession 配置成功，保存 CameraCaptureSession 对象
//                                            captureSession = session;
//
//                                            // 设置 CaptureRequest.Builder 的其他参数
//                                            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                                            requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
//
//                                            takePicture(new OnTakePictureCallback() {
//                                                @Override
//                                                public void onSuccess(Bitmap bitmap) {
//
//                                                }
//
//                                                @Override
//                                                public void onFailure(String message) {
//
//                                                }
//                                            });
//                                        }
//
//                                        @Override
//                                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
//                                            // CameraCaptureSession 配置失败
//                                            Log.e(TAG, "CameraCaptureSession configuration failed");
//                                        }
//                                    }, null);
//
//                                } catch (CameraAccessException e) {
//                                    e.printStackTrace();
//                                }

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
                    }

                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
                }

        }

    /**
         * 拍照并获取 Bitmap
         *
         * @param callback 拍照回调
         */
        public void takePicture(final OnTakePictureCallback callback) {
            try {
                if(cameraDevice==null) return;
                // 创建捕获请求
                captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(imageReader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                // 创建捕获会话
                cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        try {
                            // 启动捕获请求
                            CaptureRequest captureRequest = captureBuilder.build();
                            captureSession.capture(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                @Override
                                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                    super.onCaptureCompleted(session, request, result);
                                    // 获取 ImageReader 中的图像数据
                                    Image image = imageReader.acquireLatestImage();
                                    if (image != null) {
                                        // 将 Image 转换成 Bitmap
                                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                                        byte[] data = new byte[buffer.remaining()];
                                        buffer.get(data);
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                        image.close();
                                        callback.onSuccess(bitmap);
                                    } else {
                                        callback.onFailure("Image is null");
                                    }
                                }

                                @Override
                                public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                    super.onCaptureFailed(session, request, failure);
                                    callback.onFailure("Capture failed: " + failure.toString());
                                }
                            }, null);
                        } catch (CameraAccessException e) {
                            Log.e(TAG, "takePicture: CameraAccessException", e);
                            callback.onFailure("CameraAccessException: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e(TAG, "takePicture: Capture session configuration failed");
                        callback.onFailure("Capture session configuration failed");
                    }
                }, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "takePicture: CameraAccessException", e);
                callback.onFailure("CameraAccessException: " + e.getMessage());
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
         * 获取最优的相机预览尺寸
         *
         * @param sizes 支持的尺寸列表
         * @return 最优的尺寸
         */
        private Size getOptimalPreviewSize(Size[] sizes) {
            int maxWidth = MAX_PREVIEW_WIDTH;
            int maxHeight = MAX_PREVIEW_HEIGHT;
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
         * 拍照回调接口
         */
        public interface OnTakePictureCallback {
            void onSuccess(Bitmap bitmap);

            void onFailure(String message);
        }


}
