package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * getcode_findpwd： 找回密码验证码
 * getcode_bindphone：绑定手机验证码
 * getcode_rebindphone：修改绑定验证码
 * getcode_reguser： 会员注册验证码
 */
public class ValidCodeParam extends ApiParam {

    public String act;

    public String uid;

    public String phone;

    public ValidCodeParam(String act, String uid, String phone) {
        this.act = act;
        this.uid = uid;
        this.phone = phone;
    }

    public static ValidCodeParam getForgetCodeParam(String phone) {
        return new ValidCodeParam("getcode_findpwd", "", phone);
    }

    public static ValidCodeParam getRegisterCodeParam(String phone) {
        return new ValidCodeParam("getcode_reguser", "", phone);
    }

    public static ValidCodeParam getBindCodeParam(String phone) {
        return new ValidCodeParam("getcode_bindphone", "", phone);
    }

    public static ValidCodeParam getReBindCodeParam(String uid, String phone) {
        return new ValidCodeParam("getcode_rebindphone", uid, phone);
    }

}
