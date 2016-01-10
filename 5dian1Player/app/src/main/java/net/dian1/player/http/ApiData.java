package net.dian1.player.http;


import android.text.TextUtils;

import com.lidroid.xutils.http.RequestParams;

import net.dian1.player.model.LoginParam;
import net.dian1.player.model.UploadImageParam;
import net.dian1.player.model.UserinfoEditParam;
import net.dian1.player.model.login.PwdResetParam;
import net.dian1.player.model.login.PwdUpdateParam;
import net.dian1.player.model.login.RegisterParam;
import net.dian1.player.model.login.SecurityParam;
import net.dian1.player.model.login.ValidCodeParam;

import org.w3c.dom.Text;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class ApiData extends Api {

    private static final String TAG = "ApiData";


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

        public static final String URL = API_URL + "/latestVersion.asp";

    }

    public static class MusicStyleApi {

        public static final String URL = API_URL + "/musicCategory.asp";

        public static ApiRequestParams getParams() {
            return null;
        }

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

        /**
         * keywords	搜索关键字	String 	True
         * orderby	排序	Sting 	False 	popularity_down：人气降序
         * popularity_down：人气升序
         * pagenum	获取第几页	Int 	False	默认：返回第一页
         * pagesize	设定pagesize	Int 	False 	默认：20
         *
         * @return
         */
        public static RequestParams getParams(String keywords) {
            final RequestParams params = new RequestParams();
            if (!TextUtils.isEmpty(keywords)) {
                params.addQueryStringParameter("keywords", keywords);
            }
            params.addQueryStringParameter("pagesize", String.valueOf(50));
            return params;
        }

    }

    public static class MusicSuibianApi {

        public static final String URL = API_URL + "/musicSuibian.asp";

        public static RequestParams getParams(String fengge) {
            final RequestParams params = new RequestParams();
            if (!TextUtils.isEmpty(fengge)) {
                params.addQueryStringParameter("fengge", fengge);
            }
            return params;
        }

    }

    public static class SecurityApi {

        public static final String URL = API_URL + "/security.asp";

        public static RequestParams getParams(SecurityParam param) {
            return new ApiData().new ApiRequestParams(param);
        }

    }

    public static class PwdUpdateApi {

        public static final String URL = API_URL + "/updatePwd";

        public static ApiRequestParams setParams(PwdUpdateParam param) {
            return new ApiData().new ApiRequestParams(param);
        }
    }



    // 2.3 找回密码
    public static class PwdResetApi {

        public static final String URL = API_URL + "/resetPwd";

        public static ApiRequestParams setParams(PwdResetParam param) {
            return new ApiData().new ApiRequestParams(param);
        }
    }

    public static class ValidCodeApi {

        public static final String URL = API_URL + "/getcode.asp";

        public static ApiRequestParams getParams(ValidCodeParam param) {
            return new ApiData().new ApiRequestParams(param);
        }
    }

    public static class RegisterApi {

        public static final String URL = API_URL + "/reg.asp";

        public static ApiRequestParams getParams(RegisterParam param) {
            return new ApiData().new ApiRequestParams(param);
        }
    }

    public static class FaceUploadApi {
        //http://www.5dian1.net/api/app/uptouxiang/upload.asp
        public static final String URL = API_URL + "/touxiang.asp";

        public static ApiRequestParams setParams(UploadImageParam param) {
            return new ApiData().new ApiRequestParams(param);
        }

        /*public static RequestParams getParams(int loginId, String image) {
            final RequestParams params = new RequestParams();
            if (!TextUtils.isEmpty(image)) {
                params.addBodyParameter("image", image);
                params.addBodyParameter("loginId", String.valueOf(loginId));
            }
            return params;
        }*/

    }

    /**
     * 会员资料编辑
     */
    public static class UserInfoApi {
        public static final String URL = API_URL + "/regdata.asp";

        public static ApiRequestParams setParams(UserinfoEditParam param) {
            return new ApiData().new ApiRequestParams(param);
        }

    }

}
