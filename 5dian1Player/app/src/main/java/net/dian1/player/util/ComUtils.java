/**
 * @Title: ComUtils.java
 * @Package com.wm.dmall.util
 * @Description: TODO(用一句话描述该文件做什么)
 * @author SixSamA mephistolake@gmail.com
 * @date 2015年5月12日 下午6:49:11
 * @version V1.0
 */
package net.dian1.player.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;


import net.dian1.player.activity.BrowserActivity;

import java.security.MessageDigest;
import java.util.List;

/**
 * @ClassName: ComUtils
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author SixSamA
 * @date 2015年5月12日 下午6:49:11
 */
public class ComUtils {

	/**
	 * 判断字符串是否为空
	 * @param value
	 * @return
	 */
	public static boolean isEmpty(String value) {
		if (value != null && !"".equalsIgnoreCase(value.trim()) && !"null".equalsIgnoreCase(value.trim())) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * @Title: isShowKeyboard
	 * @Description: 显示隐藏软键盘
	 * @param @param pContext
	 * @param @param et_search
	 * @param @param isShow 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public static void isShowKeyboard(Context pContext, EditText et_search, boolean isShow) {
		InputMethodManager imm = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (isShow) {
			imm.showSoftInput(et_search, 0);
		} else {
			imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
		}
	}

	public static void isShowKeyboard2(final Context pContext, final EditText et_search, final boolean isShow) {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) pContext.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (isShow) {
					imm.showSoftInput(et_search, 0);
				} else {
					imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
				}
			}
		}, 500);
	}

	/**
	 * @Title: getProcessName
	 * @Description: 获取进程名字
	 * @param @param cxt
	 * @param @param pid
	 * @param @return 设定文件
	 * @return String 返回类型
	 * @throws
	 */
	public static String getProcessName(Context cxt, int pid) {
		ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
		if (runningApps == null) {
			return null;
		}
		for (RunningAppProcessInfo procInfo : runningApps) {
			if (procInfo.pid == pid) {
				return procInfo.processName;
			}
		}
		return null;
	}

	/**
	 * @Title: getStatusBarHeight
	 * @Description: 状态栏高度（不能在onCreate里面调用）
	 * @param @param act
	 * @param @return 设定文件
	 * @return int 返回类型
	 * @throws
	 */
	public static int getStatusBarHeight(Activity act) {
		Rect frame = new Rect();
		act.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		return frame.top;
	}


	/**
	 * 网络连接是否可用
	 * @param context
	 * @return
	 */
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] netinfo = cm.getAllNetworkInfo();
		if (netinfo == null) {
			return false;
		}
		for (int i = 0; i < netinfo.length; i++) {
			if (netinfo[i].isConnected()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @Title: getVersion
	 * @Description: 获取当前应用版本
	 * @param context
	 * @return String 返回类型
	 * @throws
	 */
	public static String getAppVersion(Context context) {
		// 1.获取当前应用程序的包的apk信息
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo packInfo = pm.getPackageInfo(context.getPackageName(), 0);
			return packInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			// 不可能发生
			return "";
		}
	}

	public static String getVersion() {
		return "Android-" + Build.VERSION.RELEASE;
	}

	/**
	 * uos 实为 user-agent，包括厂商，机器型号，操作系统号
	 */
	public static String getUserAgent() {
		return Build.BRAND + " " + Build.MODEL + " " + Build.DISPLAY;
	}

	// 输入汉字和其他字符都没有问题
	public static String MD5(String origString) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] strTemp = origString.getBytes("utf-8");
			// 如果输入“SHA”，就是实现SHA加密。
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static void openBrowser(Context context, String url, String title) {
		Intent intent = new Intent(context, BrowserActivity.class);
		intent.putExtra("url", url);
		if(!TextUtils.isEmpty(title)) {
			intent.putExtra("title", title);
		}
		context.startActivity(intent);
	}
}
