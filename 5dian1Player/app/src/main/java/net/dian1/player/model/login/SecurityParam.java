package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

/**
 * token	String 	在“修改密码”“绑定手机”“修改绑定”时，需要
 * act	String 	true	固定值（英文部分），4选一
 * resetpwd：
 * 重设密码（找回密码）
 * newpwd：修改密码
 * bindphone：绑定手机
 * uid	Int 	在“修改密码”“绑定手机”“修改绑定”时，需要	会员ID
 * phone	String	根据act要求有或无	手机号码
 * oldPwd	String	根据act要求有或无	原密码
 * newPwd	String 	根据act要求有或无	新密码
 * authCode	String 	在“找回密码”“绑定手机”“修改绑定”时，需要	验证码
 */
public class SecurityParam extends ApiParam {

    public String act;

    public String uid;

    public String phone;

    public String oldPwd;

    public String newPwd;

    public String authCode;

    public SecurityParam() {
    }

    public SecurityParam(String pageNum, String pageSize, String act, String uid, String phone, String oldPwd, String newPwd, String authCode) {
        this.act = act;
        this.uid = uid;
        this.phone = phone;
        this.oldPwd = oldPwd;
        this.newPwd = newPwd;
        this.authCode = authCode;
    }

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOldPwd() {
        return oldPwd;
    }

    public void setOldPwd(String oldPwd) {
        this.oldPwd = oldPwd;
    }

    public String getNewPwd() {
        return newPwd;
    }

    public void setNewPwd(String newPwd) {
        this.newPwd = newPwd;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }
}
