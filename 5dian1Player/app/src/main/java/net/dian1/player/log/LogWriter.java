/*
 * @(#)LogUtil.java		       Project: crash
 * Date:2014-5-27
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
package net.dian1.player.log;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 *
 */
public class LogWriter {

    private static final int TYPE_LOG = 0;

    private static final int TYPE_CRASH = 1;

    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS",
            Locale.getDefault());

    private static final SimpleDateFormat fileNameFormatter = new SimpleDateFormat("yyyy-MM-dd",
            Locale.getDefault());

    public static synchronized void writeCrash(String tag, String message, Throwable tr) {
        writeLogFile(getLogFile(TYPE_CRASH), tag, message, tr);
    }

    public static synchronized void writeLog(String tag, String message, Throwable tr) {
        writeLogFile(getLogFile(TYPE_LOG), tag, message, tr);
    }

    /**
     *
     * @param logFile
     * @param tag
     * @param message
     * @param tr
     */
    public static void writeLogFile(File logFile, String tag, String message, Throwable tr) {
        if(logFile == null) {
            Log.e("LogWriter", "log file is null and create failed!");
            return;
        }
        String time = timeFormat.format(Calendar.getInstance().getTime());
        synchronized (logFile) {
            FileWriter fileWriter = null;
            BufferedWriter bufdWriter = null;
            PrintWriter printWriter = null;
            try {
                fileWriter = new FileWriter(logFile, true);
                bufdWriter = new BufferedWriter(fileWriter);
                printWriter = new PrintWriter(fileWriter);
                StringBuilder content = new StringBuilder();
                content.append(time).append(" ").append("E").append('/').append(tag).append(" ")
                        .append(message).append('\n');
                bufdWriter.append(new String(content.toString().getBytes(), "utf-8"));
                bufdWriter.flush();
                if(tr != null) {
                    tr.printStackTrace(printWriter);
                }
                printWriter.flush();
                fileWriter.flush();
            } catch (IOException e) {
                closeQuietly(fileWriter);
                closeQuietly(bufdWriter);
                closeQuietly(printWriter);
            }
        }
    }

    private static File getLogFile(int type) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Log.e("LogWriter", "external storage mounted failed!");
            return null;
        }
        long currentTime = Calendar.getInstance().getTimeInMillis();
        String fileName = fileNameFormatter.format(currentTime);
        File logFile;
        if(TYPE_CRASH == type) {
            logFile = new File(LogConstants.LOG_PATH + LogConstants.LOG_PREFIX_CRASH + fileName);
        } else {
            logFile = new File(LogConstants.LOG_PATH + LogConstants.LOG_PREFIX_COMMON + fileName);
        }
        if(logFile == null || !logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return logFile;
    }

    /**
     *
     * @param closeable
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
