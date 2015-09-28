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

package net.dian1.player.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.ArrayListAdapter;
import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.Review;
import net.dian1.player.dialog.AlbumLoadingDialog;
import net.dian1.player.download.DownloadManager;
import net.dian1.player.widget.RemoteImageView;

// TODO context menu for tracks
/**
 * Activity representing album
 * 
 * @author Lukasz Wisniewski
 */
public class AlbumActivity extends BaseActivity{

	private Album mAlbum;
	private ListView musicListView;
	private MustListAdapter mustListAdapter;

	public static void launch(Activity context){
		context.startActivity(new Intent(context, AlbumActivity.class));
	}

	/**
	 * Launch this Activity from the outside
	 *
	 * @param c Activity from which AlbumActivity should be started
	 * @param album Album to be presented
	 */
	public static void launch(Activity c, Album album){
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail).execute(album);
	}

	public static void launch(
			IntentDistributorActivity c, Album album,
			int reviewId) {
		new AlbumLoadingDialog(c,R.string.album_loading, R.string.album_fail).execute(album, reviewId);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_album);
		mAlbum = (Album) getIntent().getSerializableExtra("album");
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.album, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			menu.findItem(R.id.download_menu_item).setVisible(true);
		} else {
			menu.findItem(R.id.download_menu_item).setVisible(false);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.download_menu_item:
			downloadAlbum();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initView(){
		setupHeader(-1);
		musicListView = (ListView)findViewById(R.id.lv_music);

		mustListAdapter = new MustListAdapter(this);
		musicListView.setAdapter(mustListAdapter);

		int selectedReviewId = getIntent().getIntExtra("selectedReviewId", -1);
		if(selectedReviewId != -1){
			selectReview(selectedReviewId);
		}
	}
	
	/**
	 * Jump to the track (play it)
	 */
	private OnItemClickListener mOnTracksItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			
			Playlist playlist = Dian1Application.getInstance().getPlayerEngineInterface().getPlaylist();
			Music music = mAlbum.getMusics()[index];
			if (playlist == null) {
				// player's playlist is empty, create a new one with whole album and open it in the player
				playlist = new Playlist();
				playlist.addTracks(mAlbum);
				Dian1Application.getInstance().getPlayerEngineInterface().openPlaylist(playlist);
			} 
			playlist.selectOrAdd(music, mAlbum);
			Dian1Application.getInstance().getPlayerEngineInterface().play();
			PlayerActivity.launch(AlbumActivity.this, (Playlist)null);
		}

	};
	
	
	/**
	 * Add whole album to the download queue
	 */
	private void downloadAlbum(){
		
		AlertDialog alertDialog = new AlertDialog.Builder(AlbumActivity.this)
		.setTitle(R.string.download_album_q)
		.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				DownloadManager downloadManager = Dian1Application.getInstance().getDownloadManager();
				for(Music track : mAlbum.getMusics()) {
					PlaylistEntry entry = new PlaylistEntry();
					entry.setAlbum(mAlbum);
					entry.setMusic(track);
					downloadManager.download(entry);
				}
				
			}
		})
		.setNegativeButton(R.string.cancel, null)
		.create();
		
		alertDialog.show();
	}


	private void selectReview(int selectedReviewId) {
		for(int i = 0; i < mustListAdapter.getCount(); i++){
			if(((Review) mustListAdapter.getItem(i)).getId() == selectedReviewId){
				musicListView.setSelection(i);
				return;
			}
		}
	}



	/**
	 * Adapter representing reviews
	 *
	 * @author Lukasz Wisniewski
	 */
	public class MustListAdapter extends ArrayListAdapter<Review> {

		public MustListAdapter(Activity context) {
			super(context);
		}

		public int getCount() {
			return 11;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row=convertView;
			ViewHolder holder;
			if (row==null) {
				LayoutInflater inflater = mContext.getLayoutInflater();
				row=inflater.inflate(R.layout.album_music_item, null);

				holder = new ViewHolder();

				row.setTag(holder);
			}
			else{
				holder = (ViewHolder) row.getTag();
			}

			return row;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		class ViewHolder {
			RemoteImageView reviewAvatar;
			TextView reviewTitle;
			TextView reviewUserName;
			TextView reviewText;
			ProgressBar reviewRatingBar;
		}

		@Override
		public void setList(ArrayList<Review> list) {
			super.setList(list);
		}

	}

}
