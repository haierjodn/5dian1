package net.dian1.player.http;

import com.alibaba.fastjson.JSONObject;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public class ApiParam {

	public String pageNum;

	public String pageSize;

	public ApiParam() {
	}

	public ApiParam(String pageNum, String pageSize) {
		this.pageNum = pageNum;
		this.pageSize = pageSize;
	}

	public String toJson(ApiParam parma) {
		return JSONObject.toJSONString(parma);
	}
}
