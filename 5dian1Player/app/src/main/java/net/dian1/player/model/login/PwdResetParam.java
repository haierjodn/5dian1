package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/9/7.
 * Email:MephistoLake@gmail.com
 */
public class PwdResetParam extends ApiParam {

	// �ֻ�����
	public String loginId;
	// ���� �ͻ��˽���md5���ܺ󣬴��������

	public String pwd;

	// ��֤��
	public String authCode;

	public PwdResetParam(String loginId, String pwd, String authCode) {
		this.loginId = loginId;
		this.pwd = pwd;
		this.authCode = authCode;
	}
}
