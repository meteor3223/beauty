package com.xym.beautygallery.base;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 软引用Handler对象，建议在Activity中使用这个Handler以免造成内存泄漏
 */
public class CommonHandler extends Handler {
    private WeakReference<MessageHandler> mMessageHandler;

    public CommonHandler(MessageHandler msgHandler) {
        mMessageHandler = new WeakReference<>(msgHandler);
    }

    @Override
    public void handleMessage(Message msg) {
        MessageHandler realHandler = mMessageHandler.get();
        if (realHandler != null) {
            realHandler.handleMessage(msg);
        }
    }

    public interface MessageHandler {
        void handleMessage(Message msg);
    }
}