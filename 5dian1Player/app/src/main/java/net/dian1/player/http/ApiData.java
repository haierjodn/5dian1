package net.dian1.player.http;


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









}
