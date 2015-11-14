package net.dian1.player.model.authority;

/**
 * Created by Dmall on 2015/11/2.
 */
public class Authority {

    /** 随便听, 默认3首授权，金砖会员不受限制 */
    public boolean listenAny = false;

    /** 每日推荐, 默认授权 */
    public boolean musicDay = true;

    /** 搜索出的音乐专辑全部播放权，可以搜索，
     * 但只能播放每个专辑里的第1和第2首歌，
     * 且所有歌曲都不能下载，当想播放第3首以上的歌，
     * 或想下载时，弹出金钻会员说明*/
    public boolean searchAlbumAll = false;

    /** 金转会员才有下载权限 */
    public boolean downloadAuth = false;

    public Authority() {
    }

    /**
     *
     * @param isVip 是否金转会员
     */
    public Authority(int isVip) {
        listenAny = (isVip == 1);
        musicDay = true;
        searchAlbumAll = (isVip == 1);
        downloadAuth = (isVip == 1);
    }

 }
