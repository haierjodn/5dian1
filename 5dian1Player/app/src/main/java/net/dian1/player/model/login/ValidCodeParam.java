package net.dian1.player.model.login;


import net.dian1.player.http.ApiParam;

public class ValidCodeParam extends ApiParam {

    public String act;

    public String uid;

    public String phone;

    public ValidCodeParam(String act, String uid, String phone) {
        this.act = act;
        this.uid = uid;
        this.phone = phone;
    }
}
