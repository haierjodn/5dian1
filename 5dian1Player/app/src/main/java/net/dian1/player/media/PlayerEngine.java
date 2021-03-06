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

import android.media.MediaPlayer;

import net.dian1.player.api.Playlist;
import net.dian1.player.api.Playlist.PlaylistPlaybackMode;

/**
 * 
 * Interface to the player engine
 * 
 * @author Lukasz Wisniewski
 */
public interface PlayerEngine {
	
	/**
	 * Opens playlist
	 * 
	 * @param playlist
	 */
	public void openPlaylist(Playlist playlist);
	
	/**
	 * Gets currently opened playlist
	 * 
	 * @return <code>Playlist</code> instance or <code>null</code>
	 */
	public Playlist getPlaylist();
	
	/**
	 * Start playing
	 */
	public void play();
	
	/**
	 * Checks whether player is playing or not 
	 * 
	 * @return boolean value
	 */
	public boolean isPlaying();
	
	/**
	 * Stop playing
	 */
	public void stop();
	
	/**
	 * Pause playing
	 */
	public void pause();
	
	/**
	 * Jump to the next song on the playlist
	 */
	public void next();
	
	/**
	 * Jump to the previous song on the playlist
	 */
	public void prev();
	
	/**
	 * Jump to the last playList
	 */
	public void prevList();
	
	/**
	 * Skip to the track on the playlist
	 * 
	 * @param index Music number on the playlist
	 */
	public void skipTo(int index);

	public void seekTo(int msec);

	/**
	 * Set events listener
	 * 
	 * @param playerEngineListener
	 */
	public void addListener(PlayerEngineListener playerEngineListener);

	/**
	 * Remove events listener
	 *
	 * @param playerEngineListener
	 */
	public void removeListener(PlayerEngineListener playerEngineListener);

	/**
	 * Set playback mode 
	 * @param aMode
	 */
	public void setPlaybackMode(PlaylistPlaybackMode aMode);

	/**
	 * Give playback mode 
	 * @return
	 */
	public PlaylistPlaybackMode getPlaybackMode();

	/**
	 * forward current song
	 * 
	 */
	public void forward(int time);

	/**
	 * rewind current song
	 * 
	 */
	public void rewind(int time);

	/**
	 *
	 * @return
	 */
	public MediaPlayer getMediaPlayer();
}
