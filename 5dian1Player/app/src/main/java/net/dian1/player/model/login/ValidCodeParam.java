package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/9/7.
 * Email:MephistoLake@gmail.com
 */
public class ValidCodeParam extends ApiParam {

	// id
	public String loginId;
	// ��֤�����ͷ��Ͷ��ŵ�����
	// password��������, register ע��, phone ���°��ֻ�

	public String type;

    public ValidCodeParam(String loginId, String type) {
        this.loginId = loginId;
        this.type = type;
    }
}
