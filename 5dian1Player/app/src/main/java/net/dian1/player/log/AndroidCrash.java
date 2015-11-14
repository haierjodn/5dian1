package net.dian1.player.log;

import android.content.Context;
import android.util.Log;

import net.dian1.player.log.handler.AbstractCrashHandler;
import net.dian1.player.log.handler.Dian1CrashHandler;

/**
 * User: Geek_Soledad(msdx.android@qq.com)
 * Date: 2014-11-03
 * Time: 21:37
 * FIXME
 */
public class AndroidCrash {

    private static final AndroidCrash instance = new AndroidCrash();

    private AbstractCrashHandler mReporter;

    private AndroidCrash(){}

    public static AndroidCrash getInstance() {
        return instance;
    }

    /**
     * 设置报告处理
     * @param reporter
     * @return
     */
    public AndroidCrash setCrashReporter(AbstractCrashHandler reporter) {
        mReporter = reporter;
        return this;
    }

    public void init(Context mContext) {
        Dian1CrashHandler.getInstance().init(mReporter);
        Thread.setDefaultUncaughtExceptionHandler(Dian1CrashHandler.getInstance());
        Log.d("AndroidCrash", "init success: " + Thread.getDefaultUncaughtExceptionHandler().getClass());
    }

}