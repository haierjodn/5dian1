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
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.BrowsePlaylistActivity.Mode;
import net.dian1.player.adapter.PurpleEntry;
import net.dian1.player.adapter.PurpleListener;
import net.dian1.player.api.Album;
import net.dian1.player.api.Playlist;
import net.dian1.player.dialog.AboutDialog;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.common.VersionLatest;
import net.dian1.player.util.DialogUtils;
import net.dian1.player.util.MockUtils;
import net.dian1.player.widget.OnAlbumClickListener;

import java.util.ArrayList;

/**
 * Home activity of the jamendo, central navigation place
 *
 * @author Lukasz Wisniewski
 * @author Marcin Gil
 */
public class MainActivity extends Activity implements OnAlbumClickListener, OnClickListener {

    private static final String TAG = "MainActivity";

    private final int[] blocksId = new int[]{
            R.id.v_listen_any, R.id.v_repo_online, R.id.v_download, R.id.v_listen_local
    };

    /**
     * Launch Home activity helper
     *
     * @param c context where launch home from (used by SplashActivity)
     */
    public static void launch(Context c) {
        Intent intent = new Intent(c, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        fillHomeListView();
        checkUpdate();
    }

    @Override
    public void onAlbumClicked(Album album) {
        //PlayerActivity.launch(this, album);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.v_listen_any:
                PlayerActivity.launch(this);
                //				Playlist playlist = new DatabaseImpl(HomeActivity.this).getFavorites();
//				JamendroidApplication.getInstance().getPlayerEngine().openPlaylist(playlist);
//				PlaylistActivity.launch(HomeActivity.this, true);
                break;
            case R.id.v_repo_online:
                RepoActivity.launch(MainActivity.this);

                //Album album = (Album) adapterView.getItemAtPosition(position);
                //PlayerActivity.launch(MainActivity.this, album);
                break;
            case R.id.v_download:
                DownloadActivity.launch(MainActivity.this);
                break;
            case R.id.v_listen_local:
                LocalBrowserActivity.launch(this);
                //HomeActivity.launch(this);
                break;
            case R.id.tv_setting_version:
                SearchActivity.launch(MainActivity.this);
                break;
            case R.id.tv_setting_about:
                new AboutDialog(this).show();
                break;
            case R.id.tv_setting_adjust:
                SettingsActivity.launch(this);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Dian1Application.getInstance().getPlayerEngineInterface() == null
                || Dian1Application.getInstance().getPlayerEngineInterface().getPlaylist() == null) {
            menu.findItem(R.id.player_menu_item).setVisible(false);
        } else {
            menu.findItem(R.id.player_menu_item).setVisible(true);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.player_menu_item:
                PlayerActivity.launch(this, (Playlist) null);
                break;

            case R.id.about_menu_item:
                new AboutDialog(this).show();
                break;

            case R.id.settings_menu_item:
                SettingsActivity.launch(this);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Fills ListView with clickable menu items
     */
    private void fillHomeListView() {
        for (int resId : blocksId) {
            findViewById(resId).setOnClickListener(this);
        }
        findViewById(R.id.tv_setting_version).setOnClickListener(this);
        findViewById(R.id.tv_setting_about).setOnClickListener(this);
        findViewById(R.id.tv_setting_adjust).setOnClickListener(this);
    }

    /**
     * Launches menu actions
     */
    private OnItemClickListener mHomeItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int index,
                                long time) {
            try {
                PurpleListener listener = ((PurpleEntry) adapterView.getAdapter().getItem(index)).getListener();
                if (listener != null) {
                    listener.performAction();
                }
            } catch (ClassCastException e) {
                Log.w(TAG, "Unexpected position number was occurred");
            }
        }
    };

    private void checkUpdate() {
        // 升级
        ApiManager.getInstance().send(new ApiRequest(this, ApiData.VersionLatestApi.URL, VersionLatest.class, new OnResultListener<VersionLatest>() {

            @Override
            public void onResult(final VersionLatest response) {
                if (response != null && response.hasUpdate && response.versionInfo != null && response.versionInfo.popup) {
                    DialogUtils.showAppUpgradeDialog(MainActivity.this, response);
                }
            }

            @Override
            public void onResultError(String msg, String code) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        }));
    }


}
