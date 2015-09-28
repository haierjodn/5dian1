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

import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;

import net.dian1.player.Dian1Application;
import net.dian1.player.activity.AlbumActivity;
import net.dian1.player.api.Album;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.Music;
import net.dian1.player.api.Review;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;

/**
 * pre-AlbumActivity loading (gets Tracks and Reviews)
 * 
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class AlbumLoadingDialog extends LoadingDialog<Object, Integer>{
	
	Review[] mReviews;
	Album mAlbum;
	int mSelectedReviewId = -1;
	
	public AlbumLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
		super(activity, loadingMsg, failMsg);
	}

	@Override
	public Integer doInBackground(Object... params) {
		mAlbum = (Album) params[0];
		if(params.length > 1){
			mSelectedReviewId = (Integer)params[1];
		}
		try {
			loadReviews(mAlbum);
			loadTracks(mAlbum);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (WSError e) {
			publishProgress(e);
			this.cancel(true);
		}
		return 1;
	}

	@Override
	public void doStuffWithResult(Integer result) {
		
		ArrayList<Review> reviews = new ArrayList<Review>();

		for(Review review : mReviews)
			reviews.add(review);
		
		Intent intent = new Intent(mActivity, AlbumActivity.class);
		intent.putExtra("album", mAlbum);
		intent.putExtra("reviews", reviews);
		intent.putExtra("selectedReviewId", mSelectedReviewId);
		mActivity.startActivity(intent);
		
	}

	private void loadReviews(Album album) throws JSONException, WSError {
		JamendoGet2Api server = new JamendoGet2ApiImpl();
		mReviews = server.getAlbumReviews(album);
	}
	
	private void loadTracks(Album album) throws JSONException, WSError{
		JamendoGet2Api service = new JamendoGet2ApiImpl();
		Music[] musics = service.getAlbumTracks(album, Dian1Application.getInstance().getStreamEncoding());
		album.setMusics(musics);
	}
}
