package net.dian1.player.model;

import net.dian1.player.http.ApiParam;


public class UploadImageParam extends ApiParam {

	public int loginId;
	public String image;

	public UploadImageParam(int loginId, String image) {
		this.loginId = loginId;
		this.image = image;
	}

}
