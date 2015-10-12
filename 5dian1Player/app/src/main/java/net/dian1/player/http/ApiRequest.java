package net.dian1.player.http;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.log.LogUtil;
import net.dian1.player.http.error.IErrorHandler;
import net.dian1.player.model.DMSResponse;
import net.dian1.player.model.UserInfo;
import net.dian1.player.util.ComUtils;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.RequestParams;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.http.client.HttpRequest;

/**
 * token	String	true	用户登录后的标识，登录时由服务端返回
 * version	String	True	客户端版本号，用于版本比较，例如：1.0.1
 * sysVersion	String	True	操作系统版本号
 * platform	String	true	客户端平台信息（ANDROID、IOS）
 * device	String	True	设备型号，例如：三星I9000
 * venderId	String	True	商家id
 * apiVersion	String	True	由服务提供，每次打包固定一个版本，格式为：1.2.0
 * <p/>
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class ApiRequest {

    public static final String TAG = "ApiRequest";

    private Context mContext;

    private IErrorHandler errorHandler;

    public final Class mClazz;

    public final OnResultListener mOnResultListener;

    public RequestParams mRequestParams;

    public OnCallBack mOnCallBack;

    public HttpRequest.HttpMethod mMethod;

    public String mUrl;

    /**
     * 接口版本号,后端做接口版本兼容所需 *
     */
    public final static String API_VERSION_VALUE = "1.0.0";

    public ApiRequest(Context ctx, String url, Class clazz, RequestParams params, OnResultListener resultListener) {
        super();
        mClazz = clazz;
        mOnResultListener = resultListener;
        mContext = ctx;
        // 默认POST
        mMethod = HttpRequest.HttpMethod.POST;
        mUrl = url;
        mOnCallBack = new OnCallBack();
        if (params == null) {
            mRequestParams = new RequestParams();
        } else {
            mRequestParams = params;
        }
        setupHeaders(mRequestParams);
    }

    // 无参数
    public ApiRequest(Context ctx, String url, Class clazz, OnResultListener l) {
        this(ctx, url, clazz, null, l);
    }

    // 无回调
    public ApiRequest(Context ctx, String url, Class clazz, RequestParams params) {
        this(ctx, url, clazz, params, null);
    }

    // 无回调
    public ApiRequest(Context ctx, String url, RequestParams params, OnResultListener l) {
        this(ctx, url, null, params, l);
    }

    public ApiRequest setErrorHandler(IErrorHandler handler) {
        errorHandler = handler;
        return this;
    }

    public ApiRequest setHttpMethod(HttpRequest.HttpMethod method) {
        if(method != null) {
            mMethod = method;
        }
        return this;
    }

	private void setupHeaders(RequestParams requestParams) {
		UserInfo user = ((Dian1Application) mContext.getApplicationContext()).getUser();
		if (user != null) {
            LogUtil.i(TAG, user.getToken());
			mRequestParams.addHeader(Api.HEADER_TOKEN, user.getToken());
		}
		mRequestParams.addHeader(Api.HEADER_APP_VERSION, ComUtils.getAppVersion(mContext)); // app版本号
		mRequestParams.addHeader(Api.HEADER_SYS_VERSION, ComUtils.getVersion()); // 系统版本号版本号
		mRequestParams.addHeader(Api.HEADER_PLATFORM, "ANDROID"); // 平台信息
		mRequestParams.addHeader(Api.HEADER_DEVICE, ComUtils.getUserAgent()); // 设备信息
		mRequestParams.addHeader(Api.HEADER_APP_VERSION, API_VERSION_VALUE); // 接口版本
		// TODO Add venderId String True 商家id

	}

    public class OnCallBack extends RequestCallBack<String> {

        @Override
        public void onSuccess(ResponseInfo<String> responseInfo) {
            LogUtil.e(TAG, mUrl + " 返回:"+ responseInfo.result);
            DMSResponse response = null;
            try {
                // TODO:第一次连接360免费wifi会返回html
                response = JSONObject.parseObject(responseInfo.result, DMSResponse.class);
                onResult(response);
            } catch (Exception e) {
                LogUtil.e(TAG, mUrl + " " + e.getMessage());
                onResult(new DMSResponse(String.valueOf(Api.RESULT_CODE_JSONFAIL),
                        mContext.getString(R.string.net_error)));
                return;
            }
        }

        @Override
        public void onFailure(HttpException error, String msg) {
            LogUtil.e(TAG, mUrl + " " + "msg: " + msg + "error：" + error.getMessage());
            onResult(new DMSResponse(String.valueOf(Api.RESULT_CODE_NETFAIL),
                    mContext.getString(R.string.net_error_tips)));
        }

        /**
         * ErrorHandler截取处理的，OnResultListener不会执行
         *
         * @param response
         */
		private void onResult(DMSResponse response) {
			if (errorHandler != null && errorHandler.onHandle(response)) {
				return;
			}
			if (mOnResultListener != null && response != null) {
				if (response.isSuccess()) {
					if (mClazz == null) {
						mOnResultListener.onResult(response.getData());
					} else {
						mOnResultListener.onResult(JSONObject.parseObject(response.getData(), mClazz));
					}
				} else {
					mOnResultListener.onResultError(response.getResult(), response.getCode());
				}
			}
		}
    }
}
