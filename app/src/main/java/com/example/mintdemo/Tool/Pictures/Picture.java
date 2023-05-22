package com.example.mintdemo.Tool.Pictures;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

//图片相关参考网站
// https://www.cnblogs.com/halfmanhuang/p/3848341.html
// https://www.pianshen.com/article/4795107723/
// https://blog.csdn.net/lhk147852369/article/details/81201943
// https://blog.csdn.net/weixin_31710835/article/details/117582392
// https://copyfuture.com/blogs-details/20210521222324165r

public class Picture {


    /**
     *通过rgb值生成纯色图片
     */
    public static Bitmap imagec(int r, int g, int b) {
        ColorDrawable drawable = new ColorDrawable(Color.parseColor("#000000"));
        Bitmap bitmap = Bitmap.createBitmap(10,10,Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas); //TODO  这样你就可以拿到这个bitmap了
        ColorMatrix colorMatrix=new ColorMatrix(new float[]{
                1,0,0,0,r*1,
                0,1,0,0,g*1,
                0,0,1,0,b*1,
                0,0,0,1,0});
        ColorMatrixColorFilter colorMatrixColorFilter=new ColorMatrixColorFilter(colorMatrix);
        Bitmap newBitmap=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas1=new Canvas(newBitmap);
        Paint paint=new Paint();
        paint.setColorFilter(colorMatrixColorFilter);
        canvas1.drawBitmap(bitmap,0,0,paint);
        return newBitmap;
    }




    //通过Uri获得显示图片
    public static Bitmap getpicture(Uri uri, Context context){
        Bitmap bitmap = null;
        ContentResolver cr = context.getContentResolver();
        try {
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    //根据uri获取图片的流
    private Bitmap ImageSizeCompress(Uri uri, Context context){
        InputStream Stream = null;
        InputStream inputStream = null;
        try {
            //根据uri获取图片的流
            inputStream = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options的in系列的设置了，injustdecodebouond只解析图片的大小，而不加载到内存中去
            options.inJustDecodeBounds = true;
            //1.如果通过options.outHeight获取图片的宽高，就必须通过decodestream解析同options赋值
            //否则options.outheight获取不到宽高
            BitmapFactory.decodeStream(inputStream,null,options);
            //2.通过 btm.getHeight()获取图片的宽高就不需要1的解析，我这里采取第一张方式
//            Bitmap btm = BitmapFactory.decodeStream(inputStream);
            //以屏幕的宽高进行压缩
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int heightPixels = displayMetrics.heightPixels;
            int widthPixels = displayMetrics.widthPixels;
            //获取图片的宽高
            int outHeight = options.outHeight;
            int outWidth = options.outWidth;
            //heightPixels就是要压缩后的图片高度，宽度也一样
            int a = (int) Math.ceil((outHeight/(float)heightPixels));
            int b = (int) Math.ceil(outWidth/(float)widthPixels);
            //比例计算,一般是图片比较大的情况下进行压缩
            int max = Math.max(a, b);
            if(max > 1){
                options.inSampleSize = max;
            }
            //解析到内存中去
            options.inJustDecodeBounds = false;
//            根据uri重新获取流，inputstream在解析中发生改变了
            Stream = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(Stream, null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(inputStream != null) {
                    inputStream.close();
                }
                if(Stream != null){
                    Stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return  null;
    }
    public Bitmap du(String src,Context context){
        //读取资源文件生成bitmap对象
        Bitmap b5 = null;
        InputStream is = null;
        try {
            AssetManager assetManager = context.getAssets();
            is=assetManager.open(src);    //直接写assets文件夹下的图片名就可以
            b5 = BitmapFactory.decodeStream(is);
            if(b5 != null && !b5.isRecycled()){
                Log.e("bt","读取成功");
            }
        }
        catch (IOException e){
            e.printStackTrace();
            Log.e("bt","读取失败");
        }
        return b5;
    }



    /**
     * 从网络获取图片并返回 Bitmap 对象
     * @param urlpath 网络URL
     * @return
     */
    public static Bitmap getImageFromUrl(String urlpath){
        InputStream inputStream = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlpath);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");   //设置请求方法为GET
            conn.setReadTimeout(5*1000);    //设置请求过时时间为5秒
            inputStream = conn.getInputStream();   //通过输入流获得图片数据
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            byte[] data = bos.toByteArray();//获得图片的二进制数据
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);  //生成位图
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 按正方形裁切图片
     */
    public static Bitmap ImageCrop(Bitmap bitmap) {
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();

        int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

        int retX = w > h ? (w - h) / 2 : 0;//基于原图，取正方形左上角x坐标
        int retY = w > h ? 0 : (h - w) / 2;

        //下面这句是关键
        return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
    }

    //使用Bitmap加Matrix来缩放
    public static Bitmap resizeImage(Bitmap bitmap, int w, int h)
    {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                height, matrix, true);
        if(!bitmap.isRecycled()){
            bitmap.recycle();
            bitmap = null;
        }
        return resizedBitmap;
    }

//    //使用Bitmap加Matrix来缩放   [path 图片路径]
//    public static Bitmap resizeImage(String path, int w, int h)
//    {
//        Bitmap bitmap = resizeImage(path);
//        //Bitmap bitmap = BitmapFactory.decodeFile(path);
//        //Bitmap bitmap = decodeFile(path,w,h);
//        int width = bitmap.getWidth();
//        int height = bitmap.getHeight();
//        int newWidth = w;
//        int newHeight = h;
//        float initScale=(float)0.99;
//        float scaleWidth = ((float) newWidth) / width;
//        float scaleHeight = ((float) newHeight) / height;
//
//        if(scaleWidth<1.0||scaleHeight<1.0){
//            initScale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
//        }
//        else{
//            return bitmap;
//        }
//        Matrix matrix = new Matrix();
//        matrix.postScale(initScale, initScale);
//
//        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,height, matrix, true);
//        if(!bitmap.isRecycled()){
//            bitmap.recycle();
//            bitmap = null;
//        }
//        return resizedBitmap;
//    }　　

    /*
     *图片圆角的设置
     *bitmap 传入的bitmap 对象
     *pixels 自定义圆角 角度
     */
    public  static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        Bitmap outputBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);
        return outputBitmap;
    }









}
