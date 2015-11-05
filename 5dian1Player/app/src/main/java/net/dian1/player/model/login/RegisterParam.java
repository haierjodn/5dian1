package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;


public class RegisterParam extends ApiParam {

	// 手机号码
	private String phone;

	// 密码
	private String pwd;

	// 验证码
	private String authCode;

	public RegisterParam() {
	}

	public RegisterParam(String phone, String pwd, String authCode) {
		this.phone = phone;
		this.pwd = pwd;
		this.authCode = authCode;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getAuthCode() {
		return authCode;
	}

	public void setAuthCode(String authCode) {
		this.authCode = authCode;
	}
}
