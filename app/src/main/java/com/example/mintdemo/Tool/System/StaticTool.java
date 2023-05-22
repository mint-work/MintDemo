package com.example.mintdemo.Tool.System;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 创建人:Mint
 * 创建时间:2022/11/18/17:33
 * 功能描述:剪切板相关功能
 */
public class StaticTool {

    /**
     * 创建人:Mint
     * 创建时间:2022/7/20/15:25
     * @param context
     * @return 获取剪切版数据
     * 功能描述:
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
                        CharSequence text = clipData.getItemAt(0).getText();
                        String tring = text.toString();
                        Message msg = Message.obtain();
                        msg.what = 0x01; // 消息标识
                        msg.obj = tring;
                        header.sendMessage(msg);
                        break;
                    }else{
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 获取剪切板数据
     * @return
     */
    @SuppressWarnings("deprecation")
    public static String pasteStringFromSystem(Context context) {
        String clipResult = null;
        if (android.os.Build.VERSION.SDK_INT > 11) {
            ClipboardManager c = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData primaryClip = c.getPrimaryClip();
            ClipData.Item itemAt = null;
            if (primaryClip != null) {
                itemAt = primaryClip.getItemAt(0);
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
     * 复制文本到剪切板
     * @param text
     * @return
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
     * 清空剪切板
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
