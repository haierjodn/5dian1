/**
 * @Title: BasePreference.java
 * @author SixSamA mephistolake@gmail.com
 * @version V1.0
 */
package net.dian1.player.preferences;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import net.dian1.player.Dian1Application;
import net.dian1.player.media.PlayerEngineImpl;

import java.util.Calendar;

/**
 *
 */
public class CommonPreference {

    public final static String TAG = CommonPreference.class.getSimpleName();

    private static SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(
            Dian1Application.getInstance());

    public static void save(String key, String value) {
        if(TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        mPreferences.edit().putString(key, value).apply();
    }

    public static void saveLong(String key, Long value) {
        if(TextUtils.isEmpty(key)) {
            return;
        }
        mPreferences.edit().putLong(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        if(TextUtils.isEmpty(key)) {
            return defValue;
        }
        return mPreferences.getString(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        if(TextUtils.isEmpty(key)) {
            return defValue;
        }
        return mPreferences.getLong(key, defValue);
    }

    public static void saveInt(String key, int value) {
		if (TextUtils.isEmpty(key)) {
			return;
		}
		mPreferences.edit().putInt(key, value).apply();
	}

	public static int getInt(String key, int defValue) {
		if (TextUtils.isEmpty(key)) {
			return defValue;
		}
		return mPreferences.getInt(key, defValue);
	}


    public static int getCountDay() {
        int maskData = CommonPreference.getInt(CommonPreference.PLAY_COUNT, 0);
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);
        int count = maskData & 0xf;
        int day = (maskData & 0xfff0) >> 4;
        if(day > 365) {
            return PlayerEngineImpl.MAX_PLAY_COUNT_DAY;
        } else if(day == today) {
            return count;
        }
        return 0;
    }

    public static void saveCountDay(int count) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR);
        int maskData = ((today & 0xfff) << 4) + (count & 0xf);
        CommonPreference.saveInt(CommonPreference.PLAY_COUNT, maskData);
    }

    /************* Preference Constants ********************/
    /** 每日推荐 */
    public final static String MUSIC_DAY = "music.day";

    /** 随便听每日播放次数 */
    public final static String PLAY_COUNT = "play.count.day";

    /** 缓存随便听音乐风格 */
    public final static String LISTEN_ANY_STYLE = "music.listen.any.style";

}
