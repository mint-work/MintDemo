package com.example.mintdemo.Tool.System;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

/**
 * | 功能描述:
 * | 剪切板工具类
 * +----------------------------------------------------------------------
 * | 时　　间: 2022/8/1/21
 * +----------------------------------------------------------------------
 * | 代码创建: Mint
 * +----------------------------------------------------------------------
 * | 版本信息: V1.0.0
 * +----------------------------------------------------------------------
 **/
public class Clipboard {
    /**
     * 创建人:Mint
     * 创建时间:2022/7/18/15:04
     * @param time   休眠时间
     * @return {@link null }
     * 功能描述: 线程休眠
     */
    public static void stop(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * 创建人:Mint
     * 创建时间:2022/7/20/15:25
     * @param context
     * @return {@link  }
     * 功能描述: 获取剪切板的字符串
     * 注意这个方法是在子线程中执行的有时候，会获取不到剪切版内容就只能重复去获取
     */
    public static void getPasteString(final Context context, final Handler header) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = clipboard.getPrimaryClip();
                    if (clipData != null) {
                        if(clipData.getItemCount() <= 0) {break;}
                        CharSequence text = clipData.getItemAt(0).getText();    //在这里是获取剪切版第一个内容
                        String tring = text.toString();
                        Message msg = Message.obtain();
                        msg.what = 0x01; // 消息标识
                        msg.obj = tring;
                        header.sendMessage(msg);
                        break;
                    }else{
                        stop(50);
                    }
                }
            }
        }).start();
    }
    /**
     * 创建人:Mint
     * 创建时间:2022/8/1/11:25
     * @param context
     * @return {@link String }
     * 功能描述: 获取剪切版内容，只执行一次，在调用线程中执行
     */
    public static String pasteStringFromSystem(Context context) {
        String clipResult = null;
        if (android.os.Build.VERSION.SDK_INT > 11) {
            ClipboardManager c = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData primaryClip = c.getPrimaryClip();
            ClipData.Item itemAt = null;
            if (primaryClip != null) {
                itemAt = primaryClip.getItemAt(0);    //在这里是获取剪切版第一个内容
            }
            if (itemAt != null) {
                String trim = itemAt.getText().toString().trim();
                clipResult = trim;
            }
        } else {
            android.text.ClipboardManager c = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            CharSequence text = c.getText();
            if (text != null) {
                clipResult = text.toString().trim();
            }
        }
        return clipResult;
    }
    /**
     * 创建人:Mint
     * 创建时间:2022/7/20/11:52
     * @param context
     * 功能描述:复制到粘贴板
     * 注意：要在ui线程中使用
     */
    public static void copyToClipboard (Context context,String src){
        ClipData myClip = ClipData.newPlainText("text", src);
        ClipboardManager myClipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        myClipboard.setPrimaryClip(myClip);
    }
    /**
     * 创建人:Mint
     * 创建时间:2022/7/20/11:52
     * @param context
     * 功能描述:复制到粘贴板
     * 注意：要在ui线程中使用 一般使用这个方法比较适配
     */
    @SuppressWarnings("deprecation")
    public static boolean copyStringToSystem(Context context,String text) {
        try {
            if (android.os.Build.VERSION.SDK_INT > 11) {
                ClipboardManager c = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                c.setText(text);
            } else {
                android.text.ClipboardManager c = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                c.setText(text);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    /**
     * 创建人:Mint
     * 创建时间:2022/8/1/11:27
     * @param context
     * 功能描述: 清空剪切板内容
     */
    public static void clear(Context context) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
