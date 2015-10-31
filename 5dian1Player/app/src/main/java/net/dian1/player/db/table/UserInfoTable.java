package net.dian1.player.db.table;

/**
 * Created by Dmall on 2015/10/31.
 */
public class UserInfoTable {
    public static final String TABLE_NAME = "users_info";

    public static final String LOGIN_ID = "login_id";
    public static final String TOKEN = "token";
    public static final String PORTRAIT = "portait";
    public static final String PHONE = "phone";
    public static final String NICK_NAME = "nick_name";
    public static final String REAL_NAME = "real_name";
    public static final String LEVEL = "level";
    public static final String LEVEL_NAME = "level_name";
    public static final String IS_APP_VIP = "is_app_vip";
    public static final String EXPIRED_TIME = "expired_time";

    public static final String CREATE_SQL = "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LOGIN_ID + " VARCHAR UNIQUE,"
            + TOKEN + " VARCHAR,"
            + PORTRAIT + " VARCHAR,"
            + PHONE + " VARCHAR,"
            + NICK_NAME + " VARCHAR,"
            + REAL_NAME + " VARCHAR,"
            + LEVEL + " INTEGER,"
            + LEVEL_NAME + " VARCHAR,"
            + IS_APP_VIP + " INTEGER,"
            + EXPIRED_TIME + " VARCHAR);";

}
