package net.dian1.player.http;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import net.dian1.player.R;
import net.dian1.player.log.LogUtil;
import net.dian1.player.http.error.DefaultErrorHandler;
import net.dian1.player.http.error.IErrorHandler;
import net.dian1.player.model.DMSResponse;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.http.ResponseStream;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class ApiManager {

	private static final String TAG = "ApiManager";

	private static ApiManager instance;

	private HttpUtils mHttpUtils;

	private IErrorHandler errorHandler;

	private Context mContext;

	private ApiManager() {
		mHttpUtils = new HttpUtils(Api.HTTP_CONNECT_TIMEOUT);
	}

	public static void init(Context context) {
		if(instance == null) {
			synchronized (ApiManager.class) {
				instance = new ApiManager();
			}
		}
		instance.setContext(context);
	}

	public static ApiManager getInstance() {
		return instance;
	}

	private void setContext(Context context) {
		mContext = context;
		errorHandler = new DefaultErrorHandler(context);
	}

	// 异步调用
	public void send(ApiRequest request) {
		request.setErrorHandler(errorHandler);
		LogUtil.e(TAG, "请求类型:" + request.mMethod + " 地址:" + request.mUrl);
		mHttpUtils.send(request.mMethod, request.mUrl, request.mRequestParams, request.mOnCallBack);
	}

	/**
	 * 同步调用
	 *
	 * @return 发生错误时返回null
 	 */
	public <T> T executeSync(ApiRequest request, Class<T> clazz) {
		DMSResponse response = null;
		try {
			ResponseStream responseStream = mHttpUtils.sendSync(request.mMethod, request.mUrl, request.mRequestParams);
			String responseJSONString = responseStream.readString();
			LogUtil.i(TAG, request.mUrl + " " + responseJSONString);
			T result = null;
			// TODO:第一次连接360免费wifi会返回html
			response = JSONObject.parseObject(responseJSONString, DMSResponse.class);
			if(clazz != null && clazz != DMSResponse.class) {
				result = JSONObject.parseObject(response.getData(), clazz);
				return result;
			}
		} catch (Exception e) {
			response = new DMSResponse(mContext.getString(R.string.net_error),
					String.valueOf(Api.RESULT_CODE_JSONFAIL));
			LogUtil.e(TAG, request.mUrl + " " + e.getMessage());
		}
		if (errorHandler != null) {
			errorHandler.onHandle(response);
		}
		if(clazz == DMSResponse.class) {
			return (T)response;
		} else {
			return null;
		}
	}


}
