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

    /************* Preference Constants ********************/
    /** Ã¿ÈÕÍÆ¼ö */
    public final static String MUSIC_DAY = "music.day";


}
