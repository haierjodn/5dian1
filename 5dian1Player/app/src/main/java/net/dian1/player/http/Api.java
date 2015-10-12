package net.dian1.player.http;

import net.dian1.player.log.LogUtil;
import com.lidroid.xutils.http.RequestParams;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class Api {

	private static final String TAG = "Api";

	public interface URLS {

		public final static String URL1 = "http://www.5dian1.net/api/app";

	}

	// 地址
	public static final String API_URL = URLS.URL1;

	// http超时
	public static final int HTTP_CONNECT_TIMEOUT = 25 * 1000;

	// 消息头
	public static final String HEADER_TOKEN = "token";

	public static final String HEADER_APP_VERSION = "version"; // 当前app版本号

	public static final String HEADER_SYS_VERSION = "sysVersion";

	public static final String HEADER_PLATFORM = "platform";

	public static final String HEADER_DEVICE = "device";

	/** 商家id **/
	public static final String HEADER_VENDER_ID = "venderId";

	/** 接口版本号 **/
	public static final String HEADER_API_VERSION = "apiVersion";

	// 返回数据
	public static final String RESULT_CODE = "code";

	public static final String RESULT_CODE_SUCCESS = "0000";

	public static final String RESULT_INFO = "result";

	public static final String RESULT_DATA = "data";

	// 请求参数
	public static final String PARAM = "param";

	// 分页参数
	public static final String DEFAULT_PAGESIZE = "20";

	public static final String DEFAULT_PAGESIZE_15 = "15";

	public static final String DEFAULT_PAGESIZE_30 = "30";

	public static final String DEFAULT_PAGESIZE_MAX = Integer.MAX_VALUE + "";

	// 设定参数
	public class ApiRequestParams extends RequestParams {

		public ApiRequestParams(ApiParam param) {
			addBodyParameter(PARAM, param.toJson(param));
			LogUtil.e(TAG, "参数:" + param.toJson(param));
		}
	}

	// 失败code
	public static final int RESULT_CODE_NETFAIL = -1;

	public static final int RESULT_CODE_JSONFAIL = -2;


}
