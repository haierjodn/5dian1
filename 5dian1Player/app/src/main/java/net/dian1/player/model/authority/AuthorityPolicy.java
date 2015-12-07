package net.dian1.player.model.authority;

/**
 * Created by Dmall on 2015/11/2.
 */
public class AuthorityPolicy {
    /**
     *
     * @param isVip 是否金转会员
     * @return
     */
    public static Authority getAuthority(int isVip) {
        Authority auth = new Authority(isVip);
        //auth = new Authority(1);
        return auth;
    }

 }
