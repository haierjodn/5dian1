package net.dian1.player.util;

import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.download.DownloadJob;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Desmond on 2015/9/15.
 */
public class MockUtils {

    public static PlaylistEntry buildSamplePlaylistEntry() {
        PlaylistEntry entry = new PlaylistEntry();
        Album album = new Album();
        album.setId(new Random().nextInt());
        album.setArtistName("Michael Jordan");
        album.setName("完美的真实感 丰富的现场感 孙露《别离");
        album.setRating(0.5d);
        album.setImage("assets/album_sample.png");
        album.setMusics(buildSampleMusicArray());

        Music music = new Music();
        music.setRating(0.6d);
        music.setName("伤了心的女人怎么了");
        music.setId(new Random().nextInt());
        music.setDuration(172);
        music.setUrl("/assets/music_sample.flac");
        music.setStream("/assets/music_sample.flac");
        entry.setAlbum(album);
        entry.setMusic(music);

        return entry;
    }

    public static Playlist buildSamplePlaylist() {
        Playlist playlist = new Playlist();
        playlist.setPlaylistPlaybackMode(Playlist.PlaylistPlaybackMode.SHUFFLE);
        Album album = new Album();
        album.setId(new Random().nextInt());
        album.setArtistName("Michael Jordan");
        album.setName("完美的真实感 丰富的现场感 孙露《别离");
        album.setRating(0.5d);
        album.setImage("assets/album_sample.png");
        album.setMusics(buildSampleMusicArray());
        playlist.addTracks(album);

        album = new Album();
        album.setId(new Random().nextInt());
        album.setArtistName("杨曼莉");
        album.setName("邓丽君神韵演绎传递最极致第一女声");
        album.setRating(0.5d);
        album.setImage("assets/album_sample.png");
        album.setMusics(buildSampleMusicArray());
        playlist.addTracks(album);

        return playlist;
    }

    public static Music[] buildSampleMusicArray() {
        Music[] musics = new Music[3];
        Music music = new Music();
        music.setRating(0.6d);
        music.setName("等你等了那么久");
        music.setId(new Random().nextInt());
        music.setDuration(192);
        music.setUrl("/assets/music_sample.flac");
        music.setStream("/assets/music_sample.flac");
        musics[0] = music;
        music = new Music();
        music.setRating(0.6d);
        music.setName("伤了心的女人怎么了");
        music.setId(new Random().nextInt());
        music.setDuration(172);
        music.setUrl("/assets/music_sample.flac");
        music.setStream("/assets/music_sample.flac");
        musics[1] = music;
        music = new Music();
        music.setRating(0.6d);
        music.setName("相见不如怀念");
        music.setId(new Random().nextInt());
        music.setDuration(172);
        music.setUrl("/assets/music_sample.flac");
        music.setStream("/assets/music_sample.flac");
        musics[2] = music;
        return musics;
    }

    public static ArrayList<DownloadJob> buildDownloadListSample() {
        ArrayList<DownloadJob> downloadJobs = new ArrayList<DownloadJob>();
        int startId = new Random().nextInt();
        DownloadJob downloadJob = new DownloadJob(buildSamplePlaylistEntry(), "destination", startId, "MP3");
        downloadJob.setProgress((new Random().nextInt(100)));
        downloadJobs.add(downloadJob);
        downloadJob = new DownloadJob(buildSamplePlaylistEntry(), "destination", startId, "MP3");
        downloadJob.setProgress((new Random().nextInt(100)));
        downloadJobs.add(downloadJob);
        downloadJob = new DownloadJob(buildSamplePlaylistEntry(), "destination", startId, "MP3");
        downloadJob.setProgress((new Random().nextInt(100)));
        downloadJobs.add(downloadJob);
        downloadJob = new DownloadJob(buildSamplePlaylistEntry(), "destination", startId, "MP3");
        downloadJob.setProgress((new Random().nextInt(100)));
        downloadJobs.add(downloadJob);
        return downloadJobs;
    }
}
