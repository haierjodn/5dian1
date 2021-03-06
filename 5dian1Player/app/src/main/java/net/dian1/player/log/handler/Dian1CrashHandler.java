/*
 * @(#)CrashHandler.java		       Project: crash
 * Date:2014-5-26
 *
 * Copyright (c) 2014 CFuture09, Institute of Software, 
 * Guangdong Ocean University, Zhanjiang, GuangDong, China.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dian1.player.log.handler;

import android.os.Looper;
import android.util.Log;

import net.dian1.player.log.CrashListener;
import net.dian1.player.log.LogUtil;
import net.dian1.player.log.LogWriter;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 *
 */
public class Dian1CrashHandler implements UncaughtExceptionHandler {
    private static final String LOG_TAG = Dian1CrashHandler.class.getSimpleName();

    private static final Dian1CrashHandler sHandler = new Dian1CrashHandler();

    private CrashListener mListener;

    public static Dian1CrashHandler getInstance() {
        return sHandler;
    }

    @Override
    public void uncaughtException(final Thread thread, final Throwable ex) {
        try {
            LogWriter.writeCrash("Dian1CrashHandler", ex.getMessage(), ex);
            LogUtil.e("[Dian1CrashHandler]:" + ex.getMessage());
        }catch (Exception e) {
            LogUtil.e(e.getMessage());
        }
        if(mListener != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    try {
                        mListener.closeApp(thread, ex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Looper.loop();
                }
            }).start();
        } else {
            //((Dian1Application) Dian1Application.getContext()).quitApp();
        }
    }

    /**
     * 初始化日志文件及CrashListener对象
     * 
     * @param listener
     *            回调接口
     */
    public void init(CrashListener listener) {
        mListener = listener;
    }
}
