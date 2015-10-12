package net.dian1.player.model;

import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/8/31.
 * Email:MephistoLake@gmail.com
 */
public class LoginParam extends ApiParam {

	public String user;

	public String pwd;

	public String clientId;

	public LoginParam(String user, String pwd, String clientId) {
		this.user = user;
		this.pwd = pwd;
		this.clientId = clientId;
	}

}
