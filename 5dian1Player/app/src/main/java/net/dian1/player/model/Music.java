package net.dian1.player.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Desmond on 2015/10/14.
 */
public class Music implements Serializable {
    private long id;
    private String name;
    private String singer;
    private String shichang;
    private long albumId;
    private String albumName;
    private List<MusicUrl> songUrlList;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public List<MusicUrl> getSongUrlList() {
        return songUrlList;
    }

    public void setSongUrlList(List<MusicUrl> songUrlList) {
        this.songUrlList = songUrlList;
    }

    public String getShichang() {
        return shichang;
    }

    public void setShichang(String shichang) {
        this.shichang = shichang;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }
}
