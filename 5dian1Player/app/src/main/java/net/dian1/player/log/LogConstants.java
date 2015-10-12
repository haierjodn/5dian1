package net.dian1.player.log;

import android.os.Environment;

/**
 * Created by Desmond on 2015/9/30.
 */
public class LogConstants {

    private static String PACKAGE = "net.dian1.player";

    public static String LOG_PATH = Environment.getExternalStorageDirectory().getPath()
            + "/cache/" + PACKAGE + "/log";

    public static String LOG_PREFIX_COMMON = "/log-";

    public static String LOG_PREFIX_CRASH = "/crash-";

}
