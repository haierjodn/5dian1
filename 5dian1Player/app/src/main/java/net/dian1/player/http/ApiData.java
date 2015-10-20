package net.dian1.player.http;


import com.lidroid.xutils.http.RequestParams;

import net.dian1.player.model.LoginParam;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class ApiData extends Api {

	private static final String TAG = "ApiData";



	// 2.20. 退出登录
//	public static class LogoutApi {
//
//		public static final String URL = API_URL + "/logout";
//		public static ApiRequestParams setParams(LogoutParam param) {
//			return new ApiData().new ApiRequestParams(param);
//		}
//
//	}




	// 登录
	public static class LoginApi {

		public static final String URL = API_URL + "/login.asp";

		public static ApiRequestParams setParams(LoginParam param) {
			return new ApiData().new ApiRequestParams(param);
		}
	}


	// 2.2. 获取验证码
//	public static class ValidCodeApi {
//
//		public static final String URL = API_URL + "/validCode";
//
//		public static ApiRequestParams setParams(ValidCodeParam param) {
//			return new ApiData().new ApiRequestParams(param);
//		}
//	}


	// 2.5 修改密码
//	public static class PwdUpdateApi {
//
//		public static final String URL = API_URL + "/updatePwd";
//
//		public static ApiRequestParams setParams(PwdUpdateParam param) {
//			return new ApiData().new ApiRequestParams(param);
//		}
//	}

	// 2.6 获取版本信息
	public static class VersionLatestApi {

		public static final String URL = API_URL + "/version/latestVersion";

	}



	public static class MusicDayApi {

		public static final String URL = API_URL + "/musicday.asp";

		public static ApiRequestParams getParams() {
			return null;
		}

	}

	public static class AbumDetailApi {

		public static final String URL = API_URL + "/musicZhuanji.asp";

		public static RequestParams getParams(long albumId) {
			final RequestParams params = new RequestParams();
			params.addQueryStringParameter("id", String.valueOf(albumId));
			return params;
		}

	}

	public static class MusicDetailApi {

		public static final String URL = API_URL + "/musicSong.asp";

		public static RequestParams getParams(long musicId) {
			final RequestParams params = new RequestParams();
			params.addQueryStringParameter("id", String.valueOf(musicId));
			return params;
		}

	}

	public static class MusicSearchApi {

		public static final String URL = API_URL + "/musicSearch.asp";

		public static RequestParams getParams(long musicId) {
			final RequestParams params = new RequestParams();
			//params.addQueryStringParameter("id", String.valueOf(musicId));
			return params;
		}

	}
}
