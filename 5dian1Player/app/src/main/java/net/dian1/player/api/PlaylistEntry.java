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

package net.dian1.player.api;

import android.text.TextUtils;

import net.dian1.player.Dian1Application;

import java.io.Serializable;



/**
 * Single playlist entry
 * 
 * @author Lukasz Wisniewski
 */
public class PlaylistEntry implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Album album;
	
	private Music music;

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public Music getMusic() {
		return music;
	}

	public void setMusic(Music music) {
		this.music = music;
	}

	public String getPlayUrl() {
		String path = Dian1Application.getInstance().getDownloadManager().getTrackPath(this);
		if(music == null) {
			return null;
		}
		if(TextUtils.isEmpty(path)) {
			path = music.getFirstMusicLocalUrl();
		}
		//Dian1Application.getInstance().getDownloadManager().getProvider().queueDownload()
		if(TextUtils.isEmpty(path)) {
			path = music.getFirstMusicLocalUrl();
		}
		if(TextUtils.isEmpty(path)) {
			path = music.getFirstMusicNetUrl();
		}
		return path;
	}
	
}
