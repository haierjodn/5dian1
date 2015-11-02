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

package net.dian1.player;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Equalizer.Settings;
import android.preference.PreferenceManager;

import net.dian1.player.activity.EqualizerActivity;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.Playlist.PlaylistPlaybackMode;
import net.dian1.player.api.util.Caller;
import net.dian1.player.api.util.RequestCache;
import net.dian1.player.db.DatabaseImpl;
import net.dian1.player.gestures.GesturesHandler;
import net.dian1.player.gestures.PlayerGestureCommandRegiser;
import net.dian1.player.http.ApiManager;
import net.dian1.player.log.LogUtil;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.media.PlayerEngineListener;
import net.dian1.player.model.UserInfo;
import net.dian1.player.model.authority.Authority;
import net.dian1.player.model.authority.AuthorityPolicy;
import net.dian1.player.service.PlayerService;
import net.dian1.player.util.ImageCache;
import net.dian1.player.download.DownloadManager;
import net.dian1.player.download.DownloadManagerImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton with hooks to Player and Download Service
 * 
 * @author Lukasz Wisniewski
 */
public class Dian1Application extends Application {

	/**
	 * Tag used for DDMS logging
	 */
	public static String TAG = "jamendo";

	/**
	 * Singleton pattern
	 */
	private static Dian1Application instance;

	/**
	 * Image cache, one for all activities and orientations
	 */
	private ImageCache mImageCache;

	/**
	 * Web request cache, one for all activities and orientations
	 */
	private RequestCache mRequestCache;

	/**
	 * Service player engine
	 */
	public PlayerEngine mServicePlayerEngine;
	
	/**
	 * Media player playing
	 */
	private MediaPlayer mCurrentMedia;

	/**
	 * Equalizer instance for runtime manipulation
	 */
	private Equalizer mEqualizer;
	
	/**
	 * Equalizer settings
	 */
	private Settings mEqualizerSettings;

	/**
	 * Intent player engine
	 */
	private PlayerEngine mIntentPlayerEngine;

	/**
	 * Player engine listener
	 */
	private List<PlayerEngineListener> mPlayerEngineListeners;

	/**
	 * Stored in Application instance in case we destroy Player service
	 */

	private Playlist mPlaylist;

	/**
	 * Provides interface for download related actions.
	 */
	private DownloadManager mDownloadManager;

	/**
	 * Handler for player related gestures.
	 */
	private GesturesHandler mPlayerGestureHandler;

	// 用户登录信息
	private UserInfo user;

	// 用户权限
	private Authority userAuthority = new Authority();

	public static Dian1Application getInstance() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.init(this);
		ApiManager.init(this);

		mImageCache = new ImageCache();
		mRequestCache = new RequestCache();

		Caller.setRequestCache(mRequestCache);
		instance = this;

		mDownloadManager = new DownloadManagerImpl(this);

		restoreEqualizerSettings();

		new Thread(new Runnable() {
			@Override
			public void run() {
				initInBackground();
			}
		}).start();
	}

	private void initInBackground() {
		setUser(new DatabaseImpl(this).getCurrentUserInfo());
	}

	/**
	 * Access to global image cache across Activity instances
	 * 
	 * @return
	 */
	public ImageCache getImageCache() {
		return mImageCache;
	}

	/**
	 * This setter should be only used for setting real player engine interface,
	 * e.g. used to pass Player Service's engine
	 * 
	 * @param playerEngine
	 */
	public void setConcretePlayerEngine(PlayerEngine playerEngine) {
		this.mServicePlayerEngine = playerEngine;
	}
	
	public void setMyCurrentMedia(MediaPlayer player){
		this.mCurrentMedia = player;
	}

	public void setMyEqualizer(Equalizer equalizer){
		this.mEqualizer = equalizer;
	}
	
	public Equalizer getMyEqualizer(){
		return this.mEqualizer;
	}

	public Settings getEqualizerSettigns() {
		return mEqualizerSettings;
	}

	public void updateEqualizerSettings(Settings settings) {
		if (isEqualizerRunning()) {
			// update running equalizer
			try {
				mEqualizer.setProperties(settings);
			} catch (UnsupportedOperationException e) {
				// applying equalizer settings after resuming
				// from pause is not supported
				// it may be ignored - the settings will remain unchanged
			}
		}
		mEqualizerSettings = settings;
		storeEqualizerSettings(mEqualizerSettings);
	}

	private void storeEqualizerSettings(Settings equalizerSettings) {
		PreferenceManager.getDefaultSharedPreferences(this).edit()
			.putString(EqualizerActivity.PREFERENCE_EQUALIZER, equalizerSettings.toString())
			.apply();
	}

	private void restoreEqualizerSettings() {
		String settingsStr = PreferenceManager.getDefaultSharedPreferences(this)
			.getString(EqualizerActivity.PREFERENCE_EQUALIZER, null);
		if (settingsStr != null && settingsStr.length() > 0) {
			mEqualizerSettings = new Settings(settingsStr);
		}
	}

	public boolean isEqualizerRunning() {
		if (mEqualizer != null) {
			try {
				mEqualizer.getProperties();
				return true;
			} catch (RuntimeException e) {
				// this will be thrown if Equalizer instance is unusable
				// it happens e.g. on ICS and JB
			}
		}
		return false;
	}

	/**
	 * Equalizer preset to use when the next time an Equalizer
	 * instance is created.
	 * -1 is reserved for custom preset
	 * -2 is reserved for no preset
	 */
	private short mEqualizerPreset = -2;
	public void setEqualizerPreset(short preset) {
		mEqualizerPreset = preset;
	}

	public short getEqualizerPreset() {
		return mEqualizerPreset;
	}

	/**
	 * This getter allows performing logical operations on the player engine's
	 * interface from UI space
	 * 
	 * @return
	 */
	public PlayerEngine getPlayerEngineInterface() {
		// request service bind
		if (mIntentPlayerEngine == null) {
			mIntentPlayerEngine = new IntentPlayerEngine();
		}
		return mIntentPlayerEngine;
	}

	public GesturesHandler getPlayerGestureHandler(){
		if(mPlayerGestureHandler == null){
			mPlayerGestureHandler = new GesturesHandler(this, new PlayerGestureCommandRegiser(getPlayerEngineInterface()));
		}
		return mPlayerGestureHandler;
	}

	/**
	 * This function allows to add listener to the concrete player engine
	 * 
	 * @param l
	 */
	public void addPlayerEngineListener(PlayerEngineListener l) {
		getPlayerEngineInterface().addListener(l);
	}

	/**
	 * This function allows to remove listener to the concrete player engine
	 *
	 * @param l
	 */
	public void removePlayerEngineListener(PlayerEngineListener l) {
		getPlayerEngineInterface().removeListener(l);
	}

	/**
	 * This function is used by PlayerService on ACTION_BIND_LISTENER in order
	 * to get to Application's exposed listener.
	 * 
	 * @return
	 */
	public List<PlayerEngineListener> fetchPlayerEngineListener() {
		return mPlayerEngineListeners;
	}

	/**
	 * Returns current playlist, used in PlayerSerive in onStart method
	 * 
	 * @return
	 */
	public Playlist fetchPlaylist() {
		return mPlaylist;
	}

	/**
	 * Retrieves application's version number from the manifest
	 * 
	 * @return
	 */
	public String getVersion() {
		String version = "0.0.0";

		PackageManager packageManager = getPackageManager();
		try {
			PackageInfo packageInfo = packageManager.getPackageInfo(
					getPackageName(), 0);
			version = packageInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return version;
	}

	public String getDownloadFormat() {
		return PreferenceManager.getDefaultSharedPreferences(this).getString(
				"download_format", JamendoGet2Api.ENCODING_MP3);
	}

	public String getStreamEncoding() {
		// http://groups.google.com/group/android-developers/msg/c546760177b22197
		// According to JBQ: ogg files are supported but not streamable
		return JamendoGet2Api.ENCODING_MP3;
	}

	public DownloadManager getDownloadManager() {
		return mDownloadManager;
	}


	public UserInfo getUser() {
		if (this.user == null) {
			//user = UserInfo.findFirst(UserInfo.class);
		}
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
		userAuthority = AuthorityPolicy.getAuthority(user != null ? user.getIsappvip() : 0);
	}

	/**
	 * 返回用户权限
	 *
	 * @return
	 */
	public Authority getUserAuthority() {
		return userAuthority;
	}

	public void logoff() {
		if (user != null) {
			user = null;
		}
		new DatabaseImpl(this).deleteAllUserInfo();
	}

	public void exitApp() {
	}

	/**
	 * Since 0.9.8.7 we embrace "bindless" PlayerService thus this adapter. No
	 * big need of code refactoring, we just wrap sending intents around defined
	 * interface
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class IntentPlayerEngine implements PlayerEngine {

		@Override
		public Playlist getPlaylist() {
			return mPlaylist;
		}

		@Override
		public boolean isPlaying() {
			if (mServicePlayerEngine == null) {
				// service does not exist thus no playback possible
				return false;
			} else {
				return mServicePlayerEngine.isPlaying();
			}
		}

		@Override
		public void next() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.next();
			} else {
				startAction(PlayerService.ACTION_NEXT);
			}
		}

		@Override
		public void openPlaylist(Playlist playlist) {
			mPlaylist = playlist;
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.openPlaylist(playlist);
			}
		}

		@Override
		public void pause() {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.pause();
			}
		}

		@Override
		public void play() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.play();
			} else {
				startAction(PlayerService.ACTION_PLAY);
			}
		}

		@Override
		public void prev() {
			if (mServicePlayerEngine != null) {
				playlistCheck();
				mServicePlayerEngine.prev();
			} else {
				startAction(PlayerService.ACTION_PREV);
			}
		}

		@Override
		public void addListener(PlayerEngineListener playerEngineListener) {
			if(mPlayerEngineListeners == null) {
				mPlayerEngineListeners = new ArrayList<>();
			}
			mPlayerEngineListeners.add(playerEngineListener);
			// we do not want to set this listener if Service
			// is not up and a new listener is null
			if (mServicePlayerEngine != null || mPlayerEngineListeners != null) {
				startAction(PlayerService.ACTION_BIND_LISTENER);
			}
		}

		@Override
		public void removeListener(PlayerEngineListener playerEngineListener) {
			if(mPlayerEngineListeners.contains(playerEngineListener)) {
				mPlayerEngineListeners.remove(playerEngineListener);
			}
		}

		@Override
		public void skipTo(int index) {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.skipTo(index);
			}
		}

		@Override
		public void seekTo(int msec) {
			if (mServicePlayerEngine != null) {
				mServicePlayerEngine.seekTo(msec);
			}
		}

		@Override
		public void stop() {
			startAction(PlayerService.ACTION_STOP);
			// stopService(new Intent(Dian1Application.this,
			// PlayerService.class));
		}

		private void startAction(String action) {
			Intent intent = new Intent(Dian1Application.this,
					PlayerService.class);
			intent.setAction(action);
			startService(intent);
		}

		/**
		 * This is required if Player Service was binded but playlist was not
		 * passed from Application to Service and one of buttons: play, next,
		 * prev is pressed
		 */
		private void playlistCheck() {
			if (mServicePlayerEngine != null) {
				if (mServicePlayerEngine.getPlaylist() == null
						&& mPlaylist != null) {
					mServicePlayerEngine.openPlaylist(mPlaylist);
				}
			}
		}

		@Override
		public void setPlaybackMode(PlaylistPlaybackMode aMode) {
			mPlaylist.setPlaylistPlaybackMode(aMode);
		}

		@Override
		public PlaylistPlaybackMode getPlaybackMode() {
			return mPlaylist.getPlaylistPlaybackMode();
		}
		
		@Override
		public void forward(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.forward(time);
			}
			
		}

		@Override
		public void rewind(int time) {
			if(mServicePlayerEngine != null){				
				mServicePlayerEngine.rewind( time );
			}
			
		}

		@Override
		public MediaPlayer getMediaPlayer() {
			if(mServicePlayerEngine != null){
				return mServicePlayerEngine.getMediaPlayer();
			}
			return null;
		}

		@Override
		public void prevList() {
			if(mServicePlayerEngine != null){
				mServicePlayerEngine.prevList();
			}
		}
		
	}
}
