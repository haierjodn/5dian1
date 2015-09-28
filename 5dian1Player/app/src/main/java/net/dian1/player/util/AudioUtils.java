package net.dian1.player.util;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.media.PlayerEngine;

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

}
