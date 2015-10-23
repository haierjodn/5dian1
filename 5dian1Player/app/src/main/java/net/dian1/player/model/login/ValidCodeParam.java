package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/9/7.
 * Email:MephistoLake@gmail.com
 */
public class ValidCodeParam extends ApiParam {

	// id
	public String loginId;
	// 验证码类型发送短信的类型
	// password更新密码, register 注册, phone 更新绑定手机

	public String type;

    public ValidCodeParam(String loginId, String type) {
        this.loginId = loginId;
        this.type = type;
    }
}
