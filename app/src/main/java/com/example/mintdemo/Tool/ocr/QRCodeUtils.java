package com.example.mintdemo.Tool.ocr;


//zxing工具类QRCodeUtils,二维码工具类
//参考网址 https://blog.csdn.net/jingzz1/article/details/84395685
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
/**
*注意：二维码的实现是调用 zxing 的二维码类库 要在grdale文件中引用以下
 *     //二维码识别库
 *     implementation 'com.google.zxing:core:3.3.3'
 *
 * 以下是整理后的代码其中中文注释是手写的测试是没有问题的
 * 英文注释是在网上集成的功能代码，没有测试，在使用之前最好测试一下
 * 注意二维码识别最好放在线程去执行防止ui冲突
 * 在识别二维码的时候最好优化一下图片提高识别性能
 * 该代码适合的二维码编码格式有很多
 * ASCII码  扩展的ASCII码 Unicode 字符集 Utf-8
 * GBK /GB2312 /GB18030 基本上所有主流编码格式都支持
 * 对应都编码格式要记得修改
 */
public class QRCodeUtils {
    /**
     * 解析二维码
     *
     * @param path
     * @return
     */
    public static String readQRCode(@NonNull String path) {
        return analysisImage2(path);
    }

    /**
     * 解析二维码
     *
     * @param file
     * @return
     */
    public static String readQRCode(File file) {
        if (file != null && file.exists()) {
            //先直接生成bitmap看能不能解析成功，不能的话再从系统文件获取bitmap然后解析
            return readQRCode(file.getPath());
        } else
            return null;
    }

    /**
     * 解析二维码
     *
     * @param context
     * @param uri     图片uri
     * @return
     */
    public static String readQRCode(Context context, Uri uri) {
        return readQRCode(AllClass.Utils.uriToPath(context, uri));
    }

    public static String readQRCode(Bitmap bitmap) {
        bitmap = AllClass.Utils.compressScale(bitmap);
        String codeText = null;
        codeText = analysisImage(bitmap);
        return codeText;
    }

    /**
     * 解析二维码图片,支持条形码
     *
     * @param scanBitmap
     * @return
     */
    public static String analysisImage(Bitmap scanBitmap) {
        byte[] data = getYUV420sp(scanBitmap.getWidth(), scanBitmap.getHeight(), scanBitmap);
        Hashtable<DecodeHintType, Object> hints = new Hashtable();
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        //支持多种类型
//        if (decodeFormats == null || decodeFormats.isEmpty()) {
//            //设置支持的类型
//            decodeFormats.addAll(AllClass.DecodeFormatManager.PRODUCT_FORMATS);
//            decodeFormats.addAll(AllClass.DecodeFormatManager.ONE_D_FORMATS);
//            decodeFormats.addAll(AllClass.DecodeFormatManager.QR_CODE_FORMATS);
//            decodeFormats.addAll(AllClass.DecodeFormatManager.DATA_MATRIX_FORMATS);
//        }

        //一般支持QR_CODE类型足够了
        decodeFormats.add(BarcodeFormat.QR_CODE);


        hints.put(DecodeHintType.CHARACTER_SET, "UTF-8"); // 设置二维码内容的编码
        //hints.put(DecodeHintType.CHARACTER_SET, "GB2312");

        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                0, 0,
                scanBitmap.getWidth(),
                scanBitmap.getHeight(),
                false);

        BinaryBitmap bitmap1 = new BinaryBitmap(new GlobalHistogramBinarizer(source));
        BinaryBitmap bitmap2 = new BinaryBitmap(new HybridBinarizer(source));
        MultiFormatReader reader = new MultiFormatReader();
        Result result = null;
        String text = null;

        //GlobalHistogramBinarizer 扫描效率较高，但精确度不如HybridBinarizer，适合小图片扫描
        //而HybridBinarizer精确度高，但效率相对较低，适合二维码较复杂的情况，这里两种都使用
        try {
            result = reader.decode(bitmap1, hints);
            text = result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (text == null || text.length() == 0) {
                result = reader.decode(bitmap2, hints);
                text = result.getText();
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }


        return text;
    }


    private static String analysisImage2(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Bitmap mBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小

        int sampleSize = (int) (options.outWidth / (float) 256);

        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        mBitmap = BitmapFactory.decodeFile(path, options);
        String codeText = null;
        codeText = analysisImage(mBitmap);
        return codeText;
    }

    private static byte[] getYUV420sp(int inputWidth, int inputHeight, Bitmap scaled) {
        int[] argb = new int[inputWidth * inputHeight];

        scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

        int requiredWidth = inputWidth % 2 == 0 ? inputWidth : inputWidth + 1;
        int requiredHeight = inputHeight % 2 == 0 ? inputHeight : inputHeight + 1;

        byte[] yuv = new byte[requiredWidth * requiredHeight * 3 / 2];

        encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

        scaled.recycle();
        return yuv;

    }

    private static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {
        // 帧图片的像素大小
        final int frameSize = width * height;
        // ---YUV数据---
        int Y, U, V;
        // Y的index从0开始
        int yIndex = 0;
        // UV的index从frameSize开始
        int uvIndex = frameSize;

        // ---颜色数据---
        int R, G, B;
        //
        int argbIndex = 0;

        // ---循环所有像素点，RGB转YUV---
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                R = (argb[argbIndex] & 0xff0000) >> 16;
                G = (argb[argbIndex] & 0xff00) >> 8;
                B = (argb[argbIndex] & 0xff);
                argbIndex++;

                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                Y = Math.max(0, Math.min(Y, 255));
                U = Math.max(0, Math.min(U, 255));
                V = Math.max(0, Math.min(V, 255));
                yuv420sp[yIndex++] = (byte) Y;
                if ((j % 2 == 0) && (i % 2 == 0)) {
                    //
                    yuv420sp[uvIndex++] = (byte) V;
                    //
                    yuv420sp[uvIndex++] = (byte) U;
                }
            }
        }
    }

    /**
     *解析图片二维码 完
     */


    //生成二维码开始

    /**
     * 生成二维码图片
     *
     * @param content         内容
     * @param width           要生成的二维码宽度
     * @param height          要生成的二维码高度
     * @param foregroundColor 二维码前景色
     * @param backgroundColor 二维码背景色
     * @param logoBmp         logo图片
     * @param margin          边框的宽
     * @return 生成的二维码
     */
    private static Bitmap writeQRImage(String content, int width, int height
            , int foregroundColor, int backgroundColor, Bitmap logoBmp, int margin
            , Bitmap backBmp,int qRCodeInBaskRatio,QRCodeInBack qrCodeInBack) {
        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 图像数据转换，使用了矩阵转换
        BitMatrix bitMatrix = null;
        Bitmap bitmap = null;
        try {
            bitMatrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
            //更改边框大小，更改后需要重新计算宽高
            bitMatrix = updateBit(bitMatrix, margin);
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();

            int[] pixels = new int[width * height];
            // 按照二维码的算法，逐个生成二维码的图片，两个for循环是图片横列扫描的结果
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * width + x] = foregroundColor;
                    } else//其他的地方为白色
                        pixels[y * width + x] = backgroundColor;
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            //设置像素矩阵的范围
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (logoBmp != null && bitmap != null)
            bitmap = addLogoToQRImage(logoBmp, bitmap, width, height);


        if (backBmp != null)
            bitmap = addBackQRImage(backBmp, bitmap,qRCodeInBaskRatio,qrCodeInBack);

        return bitmap;
    }

    /**
     * 给二维码添加背景图片
     *
     * @param backBmp 背景图片
     * @param qRImage 二维码
     * @return
     */
    private static Bitmap addBackQRImage(Bitmap backBmp, Bitmap qRImage,int qRCodeInBaskRatio,QRCodeInBack qrCodeInBack) {
        // 获取图片宽高

        if(qRCodeInBaskRatio <=0)
            qRCodeInBaskRatio = 3;

        int qrWidth = qRImage.getWidth();
        int qrheight = qRImage.getHeight();
        if (qrWidth == 0 || qrheight == 0) {
            return backBmp;
        }
        //背景图片大小

        int width = backBmp.getWidth();
        int height = backBmp.getHeight();
        int scale = Math.min(width, height);

        // 图片绘制在二维码中央，合成二维码图片
        // 二维码大小为整体大小的1/2
        float scaleFactor = (float) scale * 1.0f / qRCodeInBaskRatio / qrWidth;
        Canvas canvas = new Canvas(backBmp);
        canvas.drawBitmap(backBmp, 0, 0, null);
        switch (qrCodeInBack){
            case BOTTOM_RIGHT:
                //右下角
                canvas.scale(scaleFactor, scaleFactor, width,
                        height);
                canvas.drawBitmap(qRImage, (width - qrWidth),
                        (height - qrheight), null);
                break;
            case CENTRE:
                //中间
                canvas.scale(scaleFactor,scaleFactor,width/2,height/2);
                canvas.drawBitmap(qRImage,(width - qrWidth)/2,(height - qrheight)/2,null);
                break;
            case TOP_LEFT:
                //左上角
                canvas.scale(scaleFactor,scaleFactor,0,0);
                canvas.drawBitmap(qRImage,0,0,null);
                break;
            case BOTTOM_LIFT:
                //左下角
                canvas.scale(scaleFactor,scaleFactor,0,height);
                canvas.drawBitmap(qRImage,0,(height - qrheight),null);
                break;
            case TOP_RIGHT:
                //右上角
                canvas.scale(scaleFactor,scaleFactor,width,0);
                canvas.drawBitmap(qRImage,(width - qrWidth),0,null);
                break;
        }
        canvas.save();
        canvas.restore();
        return backBmp;
    }


    public enum QRCodeInBack{
        TOP_LEFT,
        BOTTOM_RIGHT,
        TOP_RIGHT,
        BOTTOM_LIFT,
        CENTRE;

    }


    /**
     * 设置边框大小
     *
     * @param matrix
     * @param margin 边框大小
     * @return
     */
    private static BitMatrix updateBit(BitMatrix matrix, int margin) {

        int tempM = margin * 2;
        int[] rec = matrix.getEnclosingRectangle(); // 获取二维码图案的属性
        int resWidth = rec[2] + tempM;
        int resHeight = rec[3] + tempM;
        BitMatrix resMatrix = new BitMatrix(resWidth, resHeight); // 按照自定义边框生成新的BitMatrix
        resMatrix.clear();
        for (int i = margin; i < resWidth - margin; i++) { // 循环，将二维码图案绘制到新的bitMatrix中
            for (int j = margin; j < resHeight - margin; j++) {
                if (matrix.get(i - margin + rec[0], j - margin + rec[1])) {
                    resMatrix.set(i, j);
                }
            }
        }
        return resMatrix;
    }

    /**
     * 添加logo
     *
     * @param logoBmp logo
     * @param qRImage 二维码图片
     * @param width   二维码宽度
     * @param height  二维码高度
     * @return
     */
    private static Bitmap addLogoToQRImage(Bitmap logoBmp, Bitmap qRImage, int width, int height) {
        // 获取图片宽高
        int logoWidth = logoBmp.getWidth();
        int logoHeight = logoBmp.getHeight();
        if (logoWidth == 0 || logoHeight == 0) {
            return qRImage;
        }
        // 图片绘制在二维码中央，合成二维码图片
        // logo大小为二维码整体大小的1/6
        float scaleFactor = width * 1.0f / 6 / logoWidth;
        try {
            Canvas canvas = new Canvas(qRImage);
            canvas.drawBitmap(qRImage, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, width / 2,
                    height / 2);
            canvas.drawBitmap(logoBmp, (width - logoWidth) / 2,
                    (height - logoHeight) / 2, null);
            canvas.save();
            canvas.restore();
            return qRImage;
        } catch (Exception e) {
            qRImage = null;
            e.getStackTrace();
        }
        return qRImage;
    }

    //生成二维码图片,这里使用构建者模式
    public static WriteQRImage writeQRImage(Context context) {
        return new WriteQRImage(context);
    }

    public static class WriteQRImage {
        private int width;
        private int height;
        private int foregroundColor;
        private int backgroundColor;
        private Bitmap logoBmp = null;
        private CallBackQRImage callBackQRImage;
        private int margin = 0;
        private Bitmap backBmp = null;
        private int qRCodeInBaskRatio;
        private QRCodeInBack qrCodeInBack;
        private Resources resources;
        private Context context;

        private WriteQRImage(@NonNull Context context) {
            resources = context.getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            int widthWin = dm.widthPixels;
            int heightWin = dm.heightPixels;
            this.context = context;
            width = Math.min(widthWin, heightWin) / 2;
            height = width;
            foregroundColor = Color.BLACK;
            backgroundColor = Color.WHITE;
        }

        //设置二维码宽高，默认为屏幕的1/2
        public WriteQRImage setWidthAndHeight(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        //二维码前景色，默认为黑色
        public WriteQRImage setForegroundColor(int foregroundColor) {
            this.foregroundColor = foregroundColor;
            return this;
        }

        //二维码背景色，默认为白色
        public WriteQRImage setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }
        //设置logog
        public WriteQRImage setLogoBmp(Bitmap logoBmp) {
            this.logoBmp = logoBmp;
            return this;
        }

        //设置logog
        public WriteQRImage setLogoBmp(@DrawableRes int logoRes) {
            this.logoBmp = AllClass.Utils.drawable2Bitmap(ContextCompat.getDrawable(context,logoRes));
            return this;
        }

        //设置边框大小,默认为30
        public WriteQRImage setMargin(int margin) {
            this.margin = margin;
            return this;
        }

        //设置背景图片
        public WriteQRImage setBackBmp(Bitmap backBmp,int qRCodeInBaskRatio,QRCodeInBack qrCodeInBack) {
            this.backBmp = backBmp;
            this.qRCodeInBaskRatio = qRCodeInBaskRatio;
            this.qrCodeInBack = qrCodeInBack;
            return this;
        }

        /**
         * 生成二维码
         *
         * @param content         二维码内容
         * @param callBackQRImage
         */
        public void builde(final String content, CallBackQRImage callBackQRImage) {

            this.callBackQRImage = callBackQRImage;
            if (margin == 0)
                margin = 30;

            if (content == null) {
                if (callBackQRImage != null)
                    callBackQRImage.callBackQRImage(null);
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = writeQRImage(content, width, height, foregroundColor, backgroundColor, logoBmp, margin, backBmp,qRCodeInBaskRatio,qrCodeInBack);
                    Message message = Message.obtain();
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }
            }).start();
        }

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Bitmap bitmap = (Bitmap) msg.obj;
                if (callBackQRImage != null)
                    callBackQRImage.callBackQRImage(bitmap);
            }
        };

    }


    public interface CallBackQRImage {
        void callBackQRImage(Bitmap qRImage);
    }

    //生成二维码，完

    //其他需要的类
    private static class AllClass {
        //内置工具类
        private static class Utils {

            /**
             * bitmap压缩
             *
             * @param image
             * @return
             */
            public static Bitmap compressScale(Bitmap image) {
                int pixelW = 400;
                float pixelH = 400;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
                if (baos.toByteArray().length / 1024 > 1024) {
                    baos.reset();// 重置baos即清空baos
                    image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 这里压缩50%，把压缩后的数据存放到baos中
                }
                ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
                BitmapFactory.Options newOpts = new BitmapFactory.Options();
                // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
                newOpts.inJustDecodeBounds = true;
                Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
                newOpts.inJustDecodeBounds = false;
                int w = newOpts.outWidth;
                int h = newOpts.outHeight;

                float hh = pixelH;
                float ww = pixelW;
                // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
                int be = 1;// be=1表示不缩放
                if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
                    be = (int) (newOpts.outWidth / ww);
                } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
                    be = (int) (newOpts.outHeight / hh);
                }
                if (be <= 0)
                    be = 1;
                newOpts.inSampleSize = be; // 设置缩放比例
                System.out.println(be + "<<<<<<<be");
                // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565

                // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
                isBm = new ByteArrayInputStream(baos.toByteArray());
                bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

                return bitmap;// 压缩好比例大小后再进行质量压缩

                //return bitmap;
            }


            /**
             * uri 转path
             *
             * @param context
             * @param uri
             * @return
             */
            public static String uriToPath(final Context context, final Uri uri) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri))
                    uriToPath2(context, uri);
                else
                    return uriToPath1(context, uri);
                return null;
            }


            /**
             * 4.4以上uri转path
             *
             * @param context
             * @param uri
             * @return
             */
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            private static String uriToPath2(Context context, final Uri uri) {
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                } else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{split[1]};

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
                return null;
            }


            /**
             * android4.4以下uri转path
             *
             * @param context
             * @param uri
             * @return
             */
            private static String uriToPath1(final Context context, final Uri uri) {
                if (null == uri) return null;
                final String scheme = uri.getScheme();
                String data = null;
                if (scheme == null)
                    data = uri.getPath();
                else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
                    data = uri.getPath();
                } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
                    if (null != cursor) {
                        if (cursor.moveToFirst()) {
                            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                            if (index == -1) {
                                index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            }
                            if (index > -1) {
                                data = cursor.getString(index);
                            }
                        }
                        cursor.close();
                    }
                }
                return data;
            }


            /**
             * Get the value of the data column for this Uri. This is useful for
             * MediaStore Uris, and other file-based ContentProviders.
             *
             * @param context       The context.
             * @param uri           The Uri to query.
             * @param selection     (Optional) Filter used in the query.
             * @param selectionArgs (Optional) Selection arguments used in the query.
             * @return The value of the _data column, which is typically a file path.
             */
            private static String getDataColumn(Context context, Uri uri, String selection,
                                                String[] selectionArgs) {

                Cursor cursor = null;
                final String column = "_data";
                final String[] projection = {column};

                try {
                    cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int column_index = cursor.getColumnIndexOrThrow(column);
                        return cursor.getString(column_index);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
                return null;
            }

            /**
             * @param uri The Uri to check.
             * @return Whether the Uri authority is ExternalStorageProvider.
             */
            private static boolean isExternalStorageDocument(Uri uri) {
                return "com.android.externalstorage.documents".equals(uri.getAuthority());
            }

            /**
             * @param uri The Uri to check.
             * @return Whether the Uri authority is DownloadsProvider.
             */
            private static boolean isDownloadsDocument(Uri uri) {
                return "com.android.providers.downloads.documents".equals(uri.getAuthority());
            }

            /**
             * @param uri The Uri to check.
             * @return Whether the Uri authority is MediaProvider.
             */
            private static boolean isMediaDocument(Uri uri) {
                return "com.android.providers.media.documents".equals(uri.getAuthority());
            }

            //uri转path完

            public static Bitmap drawable2Bitmap(Drawable drawable) {
                if(drawable == null)
                    return null;
                if (drawable instanceof BitmapDrawable) {
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                    if (bitmapDrawable.getBitmap() != null) {
                        return bitmapDrawable.getBitmap();
                    }
                }
                Bitmap bitmap;
                if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                    bitmap = Bitmap.createBitmap(1, 1,
                            drawable.getOpacity() != PixelFormat.OPAQUE
                                    ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
                } else {
                    bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE
                                    ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
                }
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            }

        }

        //配置解码格式类
        private static class DecodeFormatManager {

            private static final Pattern COMMA_PATTERN = Pattern.compile(",");

            public static final Vector<BarcodeFormat> PRODUCT_FORMATS;
            public static final Vector<BarcodeFormat> ONE_D_FORMATS;
            public static final Vector<BarcodeFormat> QR_CODE_FORMATS;
            public static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;

            static {
                PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
                PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
                PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
                PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
                PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
                // PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
                ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
                ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
                ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
                ONE_D_FORMATS.add(BarcodeFormat.ITF);
                QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
                QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
                DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
                DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
            }

            private DecodeFormatManager() {
            }

            static Vector<BarcodeFormat> parseDecodeFormats(Intent intent) {
                List<String> scanFormats = null;
                String scanFormatsString = intent.getStringExtra(Intents.Scan.SCAN_FORMATS);
                if (scanFormatsString != null) {
                    scanFormats = Arrays.asList(COMMA_PATTERN.split(scanFormatsString));
                }
                return parseDecodeFormats(scanFormats, intent.getStringExtra(Intents.Scan.MODE));
            }

            static Vector<BarcodeFormat> parseDecodeFormats(Uri inputUri) {
                List<String> formats = inputUri.getQueryParameters(Intents.Scan.SCAN_FORMATS);
                if (formats != null && formats.size() == 1 && formats.get(0) != null) {
                    formats = Arrays.asList(COMMA_PATTERN.split(formats.get(0)));
                }
                return parseDecodeFormats(formats, inputUri.getQueryParameter(Intents.Scan.MODE));
            }

            private static Vector<BarcodeFormat> parseDecodeFormats(Iterable<String> scanFormats,
                                                                    String decodeMode) {
                if (scanFormats != null) {
                    Vector<BarcodeFormat> formats = new Vector<BarcodeFormat>();
                    try {
                        for (String format : scanFormats) {
                            formats.add(BarcodeFormat.valueOf(format));
                        }
                        return formats;
                    } catch (IllegalArgumentException iae) {
                        // ignore it then
                    }
                }
                if (decodeMode != null) {
                    if (Intents.Scan.PRODUCT_MODE.equals(decodeMode)) {
                        return PRODUCT_FORMATS;
                    }
                    if (Intents.Scan.QR_CODE_MODE.equals(decodeMode)) {
                        return QR_CODE_FORMATS;
                    }
                    if (Intents.Scan.DATA_MATRIX_MODE.equals(decodeMode)) {
                        return DATA_MATRIX_FORMATS;
                    }
                    if (Intents.Scan.ONE_D_MODE.equals(decodeMode)) {
                        return ONE_D_FORMATS;
                    }
                }
                return null;
            }

            private static final class Intents {
                private Intents() {
                }

                public static final class Scan {
                    /**
                     * Send this intent to open the Barcodes app in scanning mode, find a barcode, and return
                     * the results.
                     */
                    public static final String ACTION = "com.google.zxing.client.android.SCAN";

                    /**
                     * By default, sending Scan.ACTION will decode all barcodes that we understand. However it
                     * may be useful to limit scanning to certain formats. Use Intent.putExtra(MODE, value) with
                     * one of the values below ({@link #PRODUCT_MODE}, {@link #ONE_D_MODE}, {@link #QR_CODE_MODE}).
                     * Optional.
                     * <p>
                     * Setting this is effectively shorthnad for setting explicit formats with {@link #SCAN_FORMATS}.
                     * It is overridden by that setting.
                     */
                    public static final String MODE = "SCAN_MODE";

                    /**
                     * Comma-separated list of formats to scan for. The values must match the names of
                     * {@link BarcodeFormat}s, such as {@link BarcodeFormat#EAN_13}.
                     * Example: "EAN_13,EAN_8,QR_CODE"
                     * <p>
                     * This overrides {@link #MODE}.
                     */
                    public static final String SCAN_FORMATS = "SCAN_FORMATS";

                    /**
                     * @see DecodeHintType#CHARACTER_SET
                     */
                    public static final String CHARACTER_SET = "CHARACTER_SET";

                    /**
                     * Decode only UPC and EAN barcodes. This is the right choice for shopping apps which get
                     * prices, reviews, etc. for products.
                     */
                    public static final String PRODUCT_MODE = "PRODUCT_MODE";

                    /**
                     * Decode only 1D barcodes (currently UPC, EAN, Code 39, and Code 128).
                     */
                    public static final String ONE_D_MODE = "ONE_D_MODE";

                    /**
                     * Decode only QR codes.
                     */
                    public static final String QR_CODE_MODE = "QR_CODE_MODE";

                    /**
                     * Decode only Data Matrix codes.
                     */
                    public static final String DATA_MATRIX_MODE = "DATA_MATRIX_MODE";

                    /**
                     * If a barcode is found, Barcodes returns RESULT_OK to onActivityResult() of the app which
                     * requested the scan via startSubActivity(). The barcodes contents can be retrieved with
                     * intent.getStringExtra(RESULT). If the user presses Back, the result code will be
                     * RESULT_CANCELED.
                     */
                    public static final String RESULT = "SCAN_RESULT";

                    /**
                     * Call intent.getStringExtra(RESULT_FORMAT) to determine which barcode format was found.
                     * See Contents.Format for possible values.
                     */
                    public static final String RESULT_FORMAT = "SCAN_RESULT_FORMAT";

                    /**
                     * Setting this to false will not save scanned codes in the history.
                     */
                    public static final String SAVE_HISTORY = "SAVE_HISTORY";

                    private Scan() {
                    }
                }

                public static final class Encode {
                    /**
                     * Send this intent to encode a piece of data as a QR code and display it full screen, so
                     * that another person can scan the barcode from your screen.
                     */
                    public static final String ACTION = "com.google.zxing.client.android.ENCODE";

                    /**
                     * The data to encode. Use Intent.putExtra(DATA, data) where data is either a String or a
                     * Bundle, depending on the type and format specified. Non-QR Code formats should
                     * just use a String here. For QR Code, see Contents for details.
                     */
                    public static final String DATA = "ENCODE_DATA";

                    /**
                     * The type of data being supplied if the format is QR Code. Use
                     * Intent.putExtra(TYPE, type) with one of Contents.Type.
                     */
                    public static final String TYPE = "ENCODE_TYPE";

                    /**
                     * The barcode format to be displayed. If this isn't specified or is blank,
                     * it defaults to QR Code. Use Intent.putExtra(FORMAT, format), where
                     * format is one of Contents.Format.
                     */
                    public static final String FORMAT = "ENCODE_FORMAT";

                    private Encode() {
                    }
                }

                public static final class SearchBookContents {
                    /**
                     * Use Google Book Search to search the contents of the book provided.
                     */
                    public static final String ACTION = "com.google.zxing.client.android.SEARCH_BOOK_CONTENTS";

                    /**
                     * The book to search, identified by ISBN number.
                     */
                    public static final String ISBN = "ISBN";

                    /**
                     * An optional field which is the text to search for.
                     */
                    public static final String QUERY = "QUERY";

                    private SearchBookContents() {
                    }
                }

                public static final class WifiConnect {
                    /**
                     * Internal intent used to trigger connection to a wi-fi network.
                     */
                    public static final String ACTION = "com.google.zxing.client.android.WIFI_CONNECT";

                    /**
                     * The network to connect to, all the configuration provided here.
                     */
                    public static final String SSID = "SSID";

                    /**
                     * The network to connect to, all the configuration provided here.
                     */
                    public static final String TYPE = "TYPE";

                    /**
                     * The network to connect to, all the configuration provided here.
                     */
                    public static final String PASSWORD = "PASSWORD";

                    private WifiConnect() {
                    }
                }

                public static final class Share {
                    /**
                     * Give the user a choice of items to encode as a barcode, then render it as a QR Code and
                     * display onscreen for a friend to scan with their phone.
                     */
                    public static final String ACTION = "com.google.zxing.client.android.SHARE";

                    private Share() {
                    }
                }
            }

        }
    }
}