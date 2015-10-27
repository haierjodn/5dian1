package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/9/7.
 * Email:MephistoLake@gmail.com
 */
public class PwdResetParam extends ApiParam {

	// 手机号码
	public String loginId;
	// 密码 客户端进行md5加密后，传给服务端

	public String pwd;

	// 验证码
	public String authCode;

	public PwdResetParam(String loginId, String pwd, String authCode) {
		this.loginId = loginId;
		this.pwd = pwd;
		this.authCode = authCode;
	}
}
