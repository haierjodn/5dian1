package net.dian1.player.model;

import net.dian1.player.http.Api;

/**
 * 配送所有结果返回的基础类，包含相同的返回数据结构
 * <p/>
 * code	String	True	返回接口请求状态码
 * 0000表示请求正常非0000表示请求出错
 * result	String	True	返回接口请求返回的消息描述，可返回错误信息
 * data	Object	true
 * <p/>
 * Created by Desmond on 2015/8/31.
 */
public class DMSResponse {

    private String code;//	String	True	返回接口请求状态码 0000表示请求正常非0000表示请求出错

    private String result;//result	String	True	返回接口请求返回的消息描述，可返回错误信息

    private String data;//data	Object	true

    public DMSResponse() {

    }

    /**
     * 初始化一下错误类型，如HTTP访问超时等，以便作为默认的错误类型的数据
     *
     * @param code
     * @param result
     */
    public DMSResponse(String code, String result) {
        this.code = code;
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 返回接口请求状态码 0000表示请求正常非0000表示请求出错
     * @return
     */
    public boolean isSuccess() {
        return Api.RESULT_CODE_SUCCESS.equals(code);
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
