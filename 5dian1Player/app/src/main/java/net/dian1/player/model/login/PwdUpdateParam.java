package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * Created by Six.SamA on 2015/9/7.
 * Email:MephistoLake@gmail.com
 */
public class PwdUpdateParam extends ApiParam {

	// ¾ÉÃÜÂë
	public String pwd;

	// ĞÂÃÜÂë
	public String newPwd;

	public PwdUpdateParam(String pwd, String newPwd) {
		this.pwd = pwd;
		this.newPwd = newPwd;
	}
}
