package net.dian1.player.util;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.media.PlayerEngine;

import java.util.List;

/**
 * Created by Desmond on 2015/9/26.
 */
public class AudioUtils {

    public static boolean isPlaying(Music music) {
        if(music == null) {
            return false;
        }
        PlayerEngine playerEngine = Dian1Application.getInstance().getPlayerEngineInterface();
        if(playerEngine != null) {
            Playlist playlist = playerEngine.getPlaylist();
            if(playlist != null) {
                return isEquals(playlist.getSelectedTrack(), music);
            }
        }
        return false;
    }

    public static boolean isEquals(PlaylistEntry entry, Music music) {
        if(entry == null || music == null) {
            return false;
        }
        return music.getName().equals(entry.getMusic().getName());
    }

    public static Music convertMusic(net.dian1.player.model.Music music) {
        if(music == null) {
            return null;
        }
        Music engineMusic = new Music();
        engineMusic.setRating(0.6d);
        engineMusic.setName(music.getName());
        engineMusic.setId((int) music.getId());
        engineMusic.setDuration(192);
        engineMusic.setSongUrlList(music.getSongUrlList());
        return engineMusic;
    }

    public static Playlist buildPlaylist(List<net.dian1.player.model.Music> songList, int positionSelected) {
        if(songList != null && songList.size() > 0) {
            Playlist playlist = new Playlist();
            playlist.setPlaylistPlaybackMode(Playlist.PlaylistPlaybackMode.SHUFFLE);
            playlist.setPlaylistPlaybackMode(Playlist.PlaylistPlaybackMode.NORMAL);
            for(int i=0; i < songList.size(); i++) {
                net.dian1.player.model.Music music = songList.get((i + positionSelected) % songList.size());
                net.dian1.player.api.Album album = new net.dian1.player.api.Album();
                album.setId((int) music.getAlbumId());
                album.setName(music.getAlbumName());
                //album.setPic();
                net.dian1.player.api.Music[] musics = new net.dian1.player.api.Music[1];
                net.dian1.player.api.Music music1 = AudioUtils.convertMusic(music);
                musics[0] = music1;
                album.setMusics(musics);
                playlist.addTracks(album);
            }
            return playlist;
        }
        return null;
    }

}
