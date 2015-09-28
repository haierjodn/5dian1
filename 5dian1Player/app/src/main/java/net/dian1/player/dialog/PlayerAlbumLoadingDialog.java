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

package net.dian1.player.dialog;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;

import net.dian1.player.Dian1Application;
import net.dian1.player.activity.PlayerActivity;
import net.dian1.player.api.Album;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.Music;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;

/**
 * pre-Player album loading dialog
 * 
 * @author Łukasz Wiśniewski
 */
public class PlayerAlbumLoadingDialog extends LoadingDialog<Album, Music[]>{
	
	private Album mAlbum;

	public PlayerAlbumLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
		super(activity, loadingMsg, failMsg);
	}

	@Override
	public Music[] doInBackground(Album... params) {
		mAlbum = params[0];
		
		JamendoGet2Api service = new JamendoGet2ApiImpl();
		Music[] musics = null;
		
		try {
			musics = service.getAlbumTracks(mAlbum, Dian1Application.getInstance().getStreamEncoding());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (WSError e) {
			publishProgress(e);
			cancel(true);
		}
		return musics;
		
	}

	@Override
	public void doStuffWithResult(Music[] musics) {
		
		Intent intent = new Intent(mActivity, PlayerActivity.class);
		Playlist playlist = new Playlist();
		mAlbum.setMusics(musics);
		playlist.addTracks(mAlbum);

		intent.putExtra("playlist", playlist);
		mActivity.startActivity(intent);
	}

}
