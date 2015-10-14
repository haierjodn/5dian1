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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureOverlayView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.BrowsePlaylistActivity.Mode;
import net.dian1.player.adapter.ImageAdapter;
import net.dian1.player.adapter.PurpleAdapter;
import net.dian1.player.adapter.PurpleEntry;
import net.dian1.player.adapter.PurpleListener;
import net.dian1.player.adapter.SeparatedListAdapter;
import net.dian1.player.api.Album;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;
import net.dian1.player.dialog.AboutDialog;
import net.dian1.player.dialog.LoadingDialog;
import net.dian1.player.widget.FailureBar;
import net.dian1.player.widget.ProgressBar;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Home activity of the jamendo, central navigation place
 * 
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class HomeActivity extends BaseActivity {

	private static final String TAG = "HomeActivity";

	private ViewFlipper mViewFlipper;
	private Gallery mGallery;
	private ProgressBar mProgressBar;
	private FailureBar mFailureBar;
	private ListView mHomeListView;
	private PurpleAdapter mBrowseJamendoPurpleAdapter;
	private PurpleAdapter mMyLibraryPurpleAdapter;
	private GestureOverlayView mGestureOverlayView;

	/**
	 * Launch Home activity helper
	 * 
	 * @param c context where launch home from (used by SplashActivity)
	 */
	public static void launch(Context c){
		Intent intent = new Intent(c, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
		c.startActivity(intent);
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mHomeListView = (ListView)findViewById(R.id.HomeListView);
		mGallery = (Gallery)findViewById(R.id.Gallery);
		mProgressBar = (ProgressBar)findViewById(R.id.ProgressBar);
		mFailureBar = (FailureBar)findViewById(R.id.FailureBar);
		mViewFlipper = (ViewFlipper)findViewById(R.id.ViewFlipper);

		mGestureOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
		mGestureOverlayView.addOnGesturePerformedListener(Dian1Application
				.getInstance().getPlayerGestureHandler());

		new NewsTask().execute((Void)null);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// commented out, was causing "Wrong state class -- expecting View State" on view rotation
		// super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(Dian1Application.getInstance().getPlayerEngineInterface() == null || Dian1Application.getInstance().getPlayerEngineInterface().getPlaylist() == null){
			menu.findItem(R.id.player_menu_item).setVisible(false);
		} else {
			menu.findItem(R.id.player_menu_item).setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){

		case R.id.player_menu_item:
			PlayerActivity.launch(this, (Playlist)null);
			break;

		case R.id.about_menu_item:
			new AboutDialog(this).show();
			break;

		case R.id.settings_menu_item:
			SettingsActivity.launch(this);
			break;

		default:

		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// support for search key
		// TODO remove hardcoded '1' value
		if(keyCode == KeyEvent.KEYCODE_SEARCH){
			mHomeListView.setSelection(1);
			PurpleEntry entry = (PurpleEntry) mHomeListView.getAdapter().getItem(1);
			entry.getListener().performAction();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		fillHomeListView();
		boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
		mGestureOverlayView.setEnabled(gesturesEnabled);
		super.onResume();
	}
	
	/**
	 * Fills ListView with clickable menu items
	 */
	private void fillHomeListView(){
		mBrowseJamendoPurpleAdapter = new PurpleAdapter(this);
		mMyLibraryPurpleAdapter = new PurpleAdapter(this);
		ArrayList<PurpleEntry> browseListEntry = new ArrayList<PurpleEntry>();
		ArrayList<PurpleEntry> libraryListEntry = new ArrayList<PurpleEntry>();
		
		// BROWSE JAMENDO
		
		browseListEntry.add(new PurpleEntry(R.drawable.list_search, R.string.search, new PurpleListener(){
			@Override
			public void performAction() {
				SearchActivity.launch(HomeActivity.this);
			}
		}));

		browseListEntry.add(new PurpleEntry(R.drawable.list_radio, R.string.radio, new PurpleListener(){
			@Override
			public void performAction() {
				RadioActivity.launch(HomeActivity.this);
			}
		}));
		
		browseListEntry.add(new PurpleEntry(R.drawable.list_top, R.string.most_listened, new PurpleListener(){
			@Override
			public void performAction() {
				new Top100Task(HomeActivity.this, R.string.loading_top100, R.string.top100_fail).execute();
			}
		}));
		
		// MY LIBRARY
		
		libraryListEntry.add(new PurpleEntry(R.drawable.list_playlist, R.string.playlists, new PurpleListener(){
			@Override
			public void performAction() {
				BrowsePlaylistActivity.launch(HomeActivity.this, Mode.Normal);
			}
		}));
		
		// check if we have personalized client then add starred albums
		final String userName = PreferenceManager.getDefaultSharedPreferences(this).getString("user_name", null);
		if(userName != null && userName.length() > 0){
			libraryListEntry.add(new PurpleEntry(R.drawable.list_cd, R.string.albums, new PurpleListener(){
				@Override
				public void performAction() {
					StarredAlbumsActivity.launch(HomeActivity.this, userName);
				}
			}));
		}
		
		/* following needs jamendo authorization (not documented yet on the wiki)
		 * listEntry.add(new PurpleEntry(R.drawable.list_mail, "Inbox")); 
		 */

		// show this list item only if the SD Card is present
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			libraryListEntry.add(new PurpleEntry(R.drawable.list_download, R.string.download, new PurpleListener(){
				@Override
				public void performAction() {
					DownloadActivity.launch(HomeActivity.this);
				}
			}));
		}
		
//		listEntry.add(new PurpleEntry(R.drawable.list_star, R.string.favorites, new PurpleListener(){
//
//			@Override
//			public void performAction() {
//				Playlist playlist = new DatabaseImpl(HomeActivity.this).getFavorites();
//				JamendroidApplication.getInstance().getPlayerEngine().openPlaylist(playlist);
//				PlaylistActivity.launch(HomeActivity.this, true);
//			}
//
//		}));
		
		// attach list data to adapters
		mBrowseJamendoPurpleAdapter.setList(browseListEntry);
		mMyLibraryPurpleAdapter.setList(libraryListEntry);
		
		// separate adapters on one list
		SeparatedListAdapter separatedAdapter = new SeparatedListAdapter(this);
		separatedAdapter.addSection(getString(R.string.browse_jamendo), mBrowseJamendoPurpleAdapter);
		separatedAdapter.addSection(getString(R.string.my_library), mMyLibraryPurpleAdapter);
		
		mHomeListView.setAdapter(separatedAdapter);
		mHomeListView.setOnItemClickListener(mHomeItemClickListener);
	}

	/**
	 * Launches menu actions
	 */
	private OnItemClickListener mHomeItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int index,
				long time) {
			try{
				PurpleListener listener = ((PurpleEntry)adapterView.getAdapter().getItem(index)).getListener();
				if(listener != null){
					listener.performAction();
				}
			}catch (ClassCastException e) {
				Log.w(TAG, "Unexpected position number was occurred");
			}
		}
	};

	/**
	 * Executes news download, JamendoGet2Api.getPopularAlbumsWeek
	 * 
	 * @author Lukasz Wisniewski
	 */
	private class NewsTask extends AsyncTask<Void, WSError, Album[]> {

		@Override
		public void onPreExecute() {
			mViewFlipper.setDisplayedChild(0);
			mProgressBar.setText(R.string.loading_news);
			super.onPreExecute();
		}

		@Override
		public Album[] doInBackground(Void... params) {
			JamendoGet2Api server = new JamendoGet2ApiImpl();
			Album[] albums = null;
			try {
				albums = server.getPopularAlbumsWeek();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e){
				publishProgress(e);
			}
			return albums;
		}

		@Override
		public void onPostExecute(Album[] albums) {

			if(albums != null && albums.length > 0){
				mViewFlipper.setDisplayedChild(1);
				ImageAdapter albumsAdapter = new ImageAdapter(HomeActivity.this);
				//albumsAdapter.setList(albums);
				mGallery.setAdapter(albumsAdapter);
				//mGallery.setOnItemClickListener(mGalleryListener);
				mGallery.setSelection(albums.length/2, true); // animate to center

			} else {
				mViewFlipper.setDisplayedChild(2);
				mFailureBar.setOnRetryListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						new NewsTask().execute((Void)null);
					}

				});
				mFailureBar.setText(R.string.connection_fail);
			}
			super.onPostExecute(albums);
		}

		@Override
		protected void onProgressUpdate(WSError... values) {
			Toast.makeText(HomeActivity.this, values[0].getMessage(), Toast.LENGTH_LONG).show();
			super.onProgressUpdate(values);
		}
		
		

	}

	/**
	 * Background task fetching top 100 tracks from the remote server
	 * It's sort of ugly circumvention of Get2 API limitations<br>
	 * 1 rss + 2 json requests 
	 *  
	 * @author Lukasz Wisniewski
	 */
	private class Top100Task extends LoadingDialog<Void, Playlist>{

		public Top100Task(Activity activity, int loadingMsg, int failMsg) {
			super(activity, loadingMsg, failMsg);
		}

		@Override
		public Playlist doInBackground(Void... params) {
			JamendoGet2Api server = new JamendoGet2ApiImpl();
			int[] id = null;
			try {
				id = server.getTop100Listened();
				// if loading rss failed and no musics are there - report an error
				if (id == null) {
					publishProgress(new WSError((String) getResources().getText(R.string.top100_fail)));
					return null;
				}
				Album[] albums = server.getAlbumsByTracksId(id);
				Music[] musics = server.getTracksByTracksId(id, Dian1Application.getInstance().getStreamEncoding());
				if(albums == null || musics == null)
					return null;
				Hashtable<Integer, PlaylistEntry> hashtable = new Hashtable<Integer, PlaylistEntry>(); 
				for(int i = 0; i < musics.length; i++){
					PlaylistEntry playlistEntry = new PlaylistEntry();
					playlistEntry.setAlbum(albums[i]);
					playlistEntry.setMusic(musics[i]);
					hashtable.put(musics[i].getId(), playlistEntry);
				}

				// creating playlist in the correct order
				Playlist playlist = new Playlist();
				for(int i =0; i < id.length; i++){
					playlist.addPlaylistEntry(hashtable.get(id[i]));
				}
				return playlist;
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (WSError e) {
				publishProgress(e);
			}
			return null;
		}

		@Override
		public void doStuffWithResult(Playlist playlist) {
			if(playlist.size() <= 0){
				failMsg();
				return;
			}
			
			PlayerActivity.launch(HomeActivity.this, playlist);
		}

	}

}
