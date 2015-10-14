package net.dian1.player.model;

import java.util.List;

/**
 * Created by Desmond on 2015/10/14.
 */
public class Album {
    private long id;
    private String name;
    private String pic;
    private String format;
    private String desc;
    private int popularity;
    private int num;
    private List<Music> songList;

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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPopularity() {
        return popularity;
    }

    public void setPopularity(int popularity) {
        this.popularity = popularity;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public List<Music> getSongList() {
        return songList;
    }

    public void setSongList(List<Music> songList) {
        this.songList = songList;
    }
}
