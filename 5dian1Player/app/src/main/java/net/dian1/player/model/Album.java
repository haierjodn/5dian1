package net.dian1.player.model;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.Playlist;
import net.dian1.player.model.authority.Authority;
import net.dian1.player.util.AudioUtils;

import java.util.List;
import java.util.Random;

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

    public Playlist buildPlaylist(int position) {
        if(songList != null && songList.size() > 0) {
            Authority authority = Dian1Application.getInstance().getUserAuthority();
            int maxLen = authority.searchAlbumAll ? songList.size() : Math.min(songList.size(), 2);
            Playlist playlist = new Playlist();
            playlist.setPlaylistPlaybackMode(Playlist.PlaylistPlaybackMode.SHUFFLE);
            playlist.setPlaylistPlaybackMode(Playlist.PlaylistPlaybackMode.NORMAL);
            net.dian1.player.api.Album album = new net.dian1.player.api.Album();
            album.setId((int) getId());
            //album.setArtistName(get);
            album.setName(getName());
            album.setRating(0.5d);
            album.setImage(getPic());

            net.dian1.player.api.Music[] musics = new net.dian1.player.api.Music[maxLen];
            for(int i = 0; i < maxLen; i++) {
                final Music music = songList.get(i);
                net.dian1.player.api.Music music1 = AudioUtils.convertMusic(music);
                musics[(i + position) % musics.length] = music1;
            }

            album.setMusics(musics);
            playlist.addTracks(album);
            return playlist;
        }
        return null;
    }
}
