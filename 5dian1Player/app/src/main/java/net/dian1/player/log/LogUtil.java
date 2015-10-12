package net.dian1.player.log;

import android.content.Context;
import android.util.Log;

import java.io.File;

/**
 *
 */
public class LogUtil {

	/**
	 * default tag for logger.
	 */
	private static final String TAG = "DMS_LOG";

	public static final int VERBOSE = 2;

	public static final int DEBUG = 3;

	public static final int INFO = 4;

	public static final int WARN = 5;

	public static final int ERROR = 6;

	public static final int TRACE = 7;

	/** the flag whether show the output log. */
	public static int sLogLevel = VERBOSE;

	public static void init(Context context) {
		AndroidCrash.getInstance().init(context);
		sLogLevel = VERBOSE;
		File logParentDir = new File(LogConstants.LOG_PATH);
		if(logParentDir == null || !logParentDir.exists()) {
			boolean result = logParentDir.mkdirs();
			LogUtil.e("" + result);
		}
	}

	/**
	 * Description : print the verbose info.
	 * @return the number of the bytes have been written
	 */
	public static void v(String msg) {
		v(TAG, msg);
	}

	public static void v(String tag, String msg) {
		v(tag, msg, null);
	}

	public static void v(String tag, String msg, Throwable e) {
		println(VERBOSE, tag, msg, e);
	}

	/**
	 * Description : print the debug info.
	 * @return the number of the bytes have been written
	 */
	public static void d(String msg) {
		v(TAG, msg);
	}

	public static void d(String tag, String msg) {
		d(tag, msg, null);
	}

	public static void d(String tag, String msg, Throwable e) {
		println(DEBUG, tag, msg, e);
	}

	/**
	 * Description : print the information.
	 * @return the number of the bytes have been written
	 */
	public static void i(String msg) {
		i(TAG, msg);
	}

	public static void i(String tag, String msg) {
		i(tag, msg, null);
	}

	public static void i(String tag, String msg, Throwable e) {
		println(INFO, tag, msg, e);
	}

	/**
	 * Description : print the warn info.
	 * @return the number of the bytes have been written
	 */
	public static void w(String msg) {
		v(TAG, msg);
	}

	public static void w(String tag, String msg) {
		w(tag, msg, null);
	}

	public static void w(String tag, String msg, Throwable e) {
		println(WARN, tag, msg, e);
	}

	/**
	 * Description : print the error info.
	 * @return the number of the bytes have been written
	 */
	public static void e(String msg) {
		v(TAG, msg);
	}

	public static void e(String tag, String msg) {
		e(tag, msg, null);
	}

	public static void e(String tag, String msg, Throwable e) {
		println(ERROR, tag, msg, e);
	}

	/**
	 * Description : print the trace info.
	 * @return the number of the bytes have been written
	 */
	public static void t(String msg) {
		v(TAG, msg);
	}

	public static void t(String tag, String msg) {
		t(tag, msg, null);
	}

	public static void t(String tag, String msg, Throwable e) {
		println(TRACE, tag, msg, e);
	}

	private static void println(int priority, String tag, String msg, Throwable throwable) {
		if (priority >= sLogLevel) {
			if (msg != null) {
				Log.println(priority, tag, msg);
				LogWriter.writeLog(tag, msg, throwable);
			}
		}
	}
}
