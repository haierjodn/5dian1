/*
 * Copyright (C) 2009 Teleca Poland Sp. z o.o. <android@teleca.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dian1.player.media;

import java.io.FileDescriptor;
import java.io.IOException;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.Playlist.PlaylistPlaybackMode;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.log.LogUtil;
import net.dian1.player.model.Music;
import net.dian1.player.util.AudioUtils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.lidroid.xutils.http.client.HttpRequest;

/**
 * Player core engine allowing playback, in other words, a
 * wrapper around Android's <code>MediaPlayer</code>, supporting
 * <code>Playlist</code> classes
 * 
 * @author Lukasz Wisniewski
 */
public class PlayerEngineImpl implements PlayerEngine {
	
	/**
	 * Time frame - used for counting number of fails within that time 
	 */
	private static final long FAIL_TIME_FRAME = 1000;
	
	/**
	 * Acceptable number of fails within FAIL_TIME_FRAME
	 */
	private static final int ACCEPTABLE_FAIL_NUMBER = 2;
	
	/**
	 * Beginning of last FAIL_TIME_FRAME
	 */
	private long mLastFailTime;
	
	/**
	 * Number of times failed within FAIL_TIME_FRAME
	 */
	private long mTimesFailed; 
	
	/**
	 * Simple MediaPlayer extensions, adds reference to the current track
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class InternalMediaPlayer extends MediaPlayer {

		/**
		 * Keeps record of currently played track, useful when dealing
		 * with multiple instances of MediaPlayer
		 */
		public PlaylistEntry playlistEntry;

		/**
		 * Still buffering
		 */
		public boolean preparing = false;

		/**
		 * Determines if we should play after preparation,
		 * e.g. we should not start playing if we are pre-buffering
		 * the next track and the old one is still playing
		 */
		public boolean playAfterPrepare = false;

	}

	/**
	 * InternalMediaPlayer instance (maybe add another one for cross-fading)
	 */
	private InternalMediaPlayer mCurrentMediaPlayer;
	
	/**
	 * Listener to the engine events
	 */
	private PlayerEngineListener mPlayerEngineListener;

	/**
	 * Playlist
	 */
	private Playlist mPlaylist = null;
	
	/**
	 * Playlist of song played before
	 */
	private Playlist prevPlaylist = null;
	
	/**
	 * Handler to the context thread
	 */
	private Handler mHandler;
	
	/**
     * Runnable periodically querying Media Player
     * about the current position of the track and
     * notifying the listener
     */
    private Runnable mUpdateTimeTask = new Runnable() {
            public void run() {

                    if(mPlayerEngineListener != null){
                        // TODO use getCurrentPosition less frequently (usage of currentTimeMillis or uptimeMillis)
                    	if(mCurrentMediaPlayer != null)
                    		mPlayerEngineListener.onTrackProgress(mCurrentMediaPlayer.getCurrentPosition()/1000);
                        mHandler.postDelayed(this, 1000);
                    }
            }
    };

	/**
	 * Default constructor
	 */
	public PlayerEngineImpl() {
		mLastFailTime = 0;
		mTimesFailed = 0;
		mHandler = new Handler();
	}

	@Override
	public void next() {
		if(mPlaylist != null){
			if(mPlaylist.isPlaylistMode(PlaylistPlaybackMode.LISTEN_ANY)) {
				playNextListenAny();
			} else {
				stop();
				mPlaylist.selectNext();
				play();
			}
		}
	}

	@Override
	public void openPlaylist(Playlist playlist) {
		if(!playlist.isEmpty()){
			prevPlaylist = mPlaylist;
			mPlaylist = playlist;
		}
		else
			mPlaylist = null;
	}

	@Override
	public void pause() {
		if(mCurrentMediaPlayer != null){
			// still preparing
			if(mCurrentMediaPlayer.preparing){
				mCurrentMediaPlayer.playAfterPrepare = false;
				return;
			}

			// check if we play, then pause
			if(mCurrentMediaPlayer.isPlaying()){
				mCurrentMediaPlayer.pause();
				if(mPlayerEngineListener != null)
					mPlayerEngineListener.onTrackPause();
				return;
			}
		}
	}

	/**
	 * 1. 根据ID去获取歌曲详情->下载歌曲->播放
	 * 2. 下载歌曲->播放
	 * 3. 播放
	 */
	@Override
	public void play() {
		
		if( mPlayerEngineListener.onTrackStart() == false ){
			return; // apparently sth prevents us from playing tracks
		}

		// check if there is anything to play
		if(mPlaylist != null){
			final PlaylistEntry playlistEntry = mPlaylist.getSelectedTrack();

			fillPlaylistEntry(mPlaylist.getSelectedTrack(), new OnResultListener<Music>() {
				@Override
				public void onResult(Music response) {
					playlistEntry.setMusic(AudioUtils.convertMusic(response));
					play();
				}
				@Override
				public void onResultError(String msg, String code) {
					Toast.makeText(Dian1Application.getInstance(), R.string.common_music_info_error, Toast.LENGTH_SHORT).show();
				}
			});

			// check if media player is initialized
			if(mCurrentMediaPlayer == null){
				mCurrentMediaPlayer = build(playlistEntry);
			}

			// check if current media player is set to our song
			if(mCurrentMediaPlayer != null && mCurrentMediaPlayer.playlistEntry != mPlaylist.getSelectedTrack()){
				cleanUp(); // this will do the cleanup job				
				mCurrentMediaPlayer = build(playlistEntry);
			}
			
			// check if there is any player instance, if not, abort further execution 
			if(mCurrentMediaPlayer == null)
				return;

			// check if current media player is not still buffering
			if(!mCurrentMediaPlayer.preparing){

				// prevent double-press
				if(!mCurrentMediaPlayer.isPlaying()){
					// i guess this mean we can play the song
					Log.i(Dian1Application.TAG, "Player [playing] "+mCurrentMediaPlayer.playlistEntry.getMusic().getName());
					
					final Dian1Application app = Dian1Application.getInstance();

					// starting timer
                    mHandler.removeCallbacks(mUpdateTimeTask);
                    mHandler.postDelayed(mUpdateTimeTask, 1000);
                    
                    // Maintain the settings of the equalizer for the new media
                    Equalizer newEqualizer = new Equalizer(0, mCurrentMediaPlayer.getAudioSessionId());
                    short preset = app.getEqualizerPreset();
                    // special case when the preset was chosen when there was no media stream running
                    if (preset > -2) {
                        newEqualizer.usePreset(preset);
                        app.setEqualizerPreset((short) -2);
                    } else {
	                    Equalizer.Settings eqSettings = app.getEqualizerSettigns();
	                    if (eqSettings != null) {
	                        newEqualizer.setProperties(eqSettings);
						}
                    }
                    // save settings for the next equalizer
                    app.updateEqualizerSettings(newEqualizer.getProperties());
                    // Enable equalizer before media starts
                    Dian1Application.getInstance().setMyEqualizer(newEqualizer);
                    Dian1Application.getInstance().getMyEqualizer().setEnabled(true);
                    mCurrentMediaPlayer.start();
				}
			} else {
				// tell the mediaplayer to play the song as soon as it ends preparing
				mCurrentMediaPlayer.playAfterPrepare = true;
			}
		}
		
		// Change application media
		Dian1Application.getInstance().setMyCurrentMedia(mCurrentMediaPlayer);
	}

	@Override
	public void prev() {
		if(mPlaylist != null){
			stop();
			mPlaylist.selectPrev();
			play();
		}
	}

	@Override
	public void skipTo(int index) {
		mPlaylist.select(index);
		play();
	}
	
	@Override
	public void stop() {
		cleanUp();
			
		if(mPlayerEngineListener != null){
			mPlayerEngineListener.onTrackStop();
		}
	}

	/**
	 * Stops & destroys media player
	 */
	private void cleanUp(){
		// nice clean-up job
		if(mCurrentMediaPlayer != null) {
			try{
				mCurrentMediaPlayer.stop();
			} catch (IllegalStateException e){
				// this may happen sometimes
			} finally {
				mCurrentMediaPlayer.release();
				mCurrentMediaPlayer = null;
			}
			// reset equalizer - it cannot be reused that way on API level 14+
			Equalizer eq = Dian1Application.getInstance().getMyEqualizer();
			if (eq != null) {
				eq.release();
				Dian1Application.getInstance().setMyEqualizer(null);
			}
		}
	}

	/**
	 * 1. 根据ID去获取歌曲详情->下载歌曲->播放
	 * 2. 下载歌曲->播放
	 * 3. 播放
	 */
	private void fillPlaylistEntry(PlaylistEntry playlistEntry, OnResultListener<Music> onResultListener) {
		if(playlistEntry == null) {
			return;
		}

		String path = playlistEntry.getPlayUrl();

		if(TextUtils.isEmpty(path)) {
			int musicId = playlistEntry.getMusic().getId();
			ApiManager.getInstance().send(new ApiRequest(Dian1Application.getInstance(), ApiData.MusicDetailApi.URL, Music.class,
					ApiData.MusicDetailApi.getParams(musicId), onResultListener).setHttpMethod(HttpRequest.HttpMethod.GET));
		}
	}

	private void playNextListenAny() {
		ApiManager.getInstance().send(new ApiRequest(Dian1Application.getInstance(), ApiData.MusicSuibianApi.URL, Music.class,
				ApiData.MusicSuibianApi.getParams(null), new OnResultListener<Music>() {
			@Override
			public void onResult(Music response) {
				//stop();
				//final PlaylistEntry playlistEntry = mPlaylist.getSelectedTrack();
				//playlistEntry.setMusic(AudioUtils.convertMusic(response));
				mPlaylist.addPlaylistEntry(AudioUtils.buildPlaylistEntry(response, 0));
				mPlaylist.selectNext();
				play();
			}
			@Override
			public void onResultError(String msg, String code) {

			}
		}).setHttpMethod(HttpRequest.HttpMethod.GET));
	}

	/**
	 * 1. 根据ID去获取歌曲详情->下载歌曲->播放
	 * 2. 下载歌曲->播放
	 * 3. 播放
	 *
	 * @param playlistEntry
	 * @return
	 */
	private InternalMediaPlayer build(PlaylistEntry playlistEntry){
		final InternalMediaPlayer mediaPlayer = new InternalMediaPlayer();
		
		// try to setup local path
		String path = playlistEntry.getPlayUrl();
		LogUtil.d("Player path:" + path);
		// some albums happen to contain empty stream url, notify of error, abort playback
		if(TextUtils.isEmpty(path)){
			if(mPlayerEngineListener != null){
				mPlayerEngineListener.onTrackStreamError();
				mPlayerEngineListener.onTrackChanged(mPlaylist.getSelectedTrack());
			}
			stop();
			return null;
		}
		try {
			mediaPlayer.setDataSource(path);

			mediaPlayer.playlistEntry = playlistEntry;
			//mediaPlayer.setScreenOnWhilePlaying(true);

			mediaPlayer.setOnCompletionListener(new OnCompletionListener(){

				@Override
				public void onCompletion(MediaPlayer mp) {
					if(!mPlaylist.isLastTrackOnList()
							|| mPlaylist.getPlaylistPlaybackMode() == PlaylistPlaybackMode.REPEAT
							|| mPlaylist.getPlaylistPlaybackMode() == PlaylistPlaybackMode.LISTEN_ANY
							|| mPlaylist.getPlaylistPlaybackMode() == PlaylistPlaybackMode.SHUFFLE_AND_REPEAT ){
						next();
					}else{
						stop();
					}
				}

			});

			mediaPlayer.setOnPreparedListener(new OnPreparedListener(){

				@Override
				public void onPrepared(MediaPlayer mp) {
					mediaPlayer.preparing = false;

					// we may start playing
					if(mPlaylist.getSelectedTrack() == mediaPlayer.playlistEntry 
							&& mediaPlayer.playAfterPrepare){
						mediaPlayer.playAfterPrepare = false;
						play();
					}

				}

			});
			
			mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener(){

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					if(mPlayerEngineListener != null){
						mPlayerEngineListener.onTrackBuffering(percent);
					}
				}
				
			});
			
			mediaPlayer.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.w(Dian1Application.TAG, "PlayerEngineImpl fail, what ("+what+") extra ("+extra+")");
						
					if(what == MediaPlayer.MEDIA_ERROR_UNKNOWN){
						// we probably lack network
						if(mPlayerEngineListener != null){
							mPlayerEngineListener.onTrackStreamError();
						}
						stop();
						return true;
					}
					
					// not sure what error code -1 exactly stands for but it causes player to start to jump songs
					// if there are more than 5 jumps without playback during 1 second then we abort 
					// further playback
					if(what == -1){
						long failTime = System.currentTimeMillis();
						if(failTime - mLastFailTime > FAIL_TIME_FRAME){
							// outside time frame
							mTimesFailed = 1;
							mLastFailTime = failTime;
							Log.w(Dian1Application.TAG, "PlayerEngineImpl "+mTimesFailed+" fail within FAIL_TIME_FRAME");
						} else {
							// inside time frame
							mTimesFailed++;
							if(mTimesFailed > ACCEPTABLE_FAIL_NUMBER){
								Log.w(Dian1Application.TAG, "PlayerEngineImpl too many fails, aborting playback");
								if(mPlayerEngineListener != null){
									mPlayerEngineListener.onTrackStreamError();
								}
								stop();
								return true;
							}
						}
					}
					return false;
				}
			});

			// start preparing
			Log.i(Dian1Application.TAG, "Player [buffering] "+mediaPlayer.playlistEntry.getMusic().getName());
			mediaPlayer.preparing = true;
			mediaPlayer.prepareAsync();
			
			// this is a new track, so notify the listener
			if(mPlayerEngineListener != null){
				mPlayerEngineListener.onTrackChanged(mPlaylist.getSelectedTrack());
			}

			return mediaPlayer;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Playlist getPlaylist() {
		return mPlaylist;
	}

	@Override
	public boolean isPlaying() {

		// no media player instance
		if(mCurrentMediaPlayer == null)
			return false;

		// so there is one, let's see if it's not preparing
		if(mCurrentMediaPlayer.preparing)
			return false;

		// finally
		return mCurrentMediaPlayer.isPlaying();
	}

	@Override
	public void addListener(PlayerEngineListener playerEngineListener) {
		mPlayerEngineListener = playerEngineListener;
	}

	@Override
	public void removeListener(PlayerEngineListener playerEngineListener) {
		mPlayerEngineListener = null;
	}

	@Override
	public void setPlaybackMode(PlaylistPlaybackMode aMode) {
		mPlaylist.setPlaylistPlaybackMode(aMode);
	}

	@Override
	public PlaylistPlaybackMode getPlaybackMode() {
		return mPlaylist.getPlaylistPlaybackMode();
	}

	public void forward(int time) {
		if(mCurrentMediaPlayer != null) {
			mCurrentMediaPlayer.seekTo(mCurrentMediaPlayer.getCurrentPosition() + time);
		}
	}

	@Override
	public void rewind(int time) {
		if(mCurrentMediaPlayer != null) {
			mCurrentMediaPlayer.seekTo(mCurrentMediaPlayer.getCurrentPosition() - time);
		}
	}

	@Override
	public void seekTo(int msec) {
		if(mCurrentMediaPlayer != null) {
			mCurrentMediaPlayer.seekTo(msec);
		}
	}

	@Override
	public MediaPlayer getMediaPlayer() {
		return mCurrentMediaPlayer;
	}
	
	@Override
	public void prevList() {
		if(prevPlaylist != null){
			openPlaylist(prevPlaylist);
			play();
		}
	}
}
