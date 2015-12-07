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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.DownloadJobAdapter;
import net.dian1.player.api.Playlist;
import net.dian1.player.download.DownloadJob;
import net.dian1.player.download.DownloadManager;
import net.dian1.player.download.DownloadObserver;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.util.AudioUtils;
import net.dian1.player.util.MockUtils;
import net.dian1.player.widget.swipelist.BaseSwipeListViewListener;
import net.dian1.player.widget.swipelist.SettingsManager;
import net.dian1.player.widget.swipelist.SwipeListView;

import java.util.ArrayList;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadActivity extends BaseActivity implements DownloadObserver,
        CompoundButton.OnCheckedChangeListener {

    private SwipeListView downloadingListView;
    private SwipeListView downloadedListView;
    private DownloadJobAdapter downloadedJobAdapter, downloadingJobAdapter;

    //private ViewPager viewPager;
    //private DownloadPageAdapter downloadPageAdapter;

    private RadioButton cbDownloaded, cbDownloading;

    private DownloadManager mDownloadManager;
    private PlayerEngine mPlayerInterface;

    private Handler mHandler;

    /**
     * Launch this Activity from the outside
     *
     * @param c context from which Activity should be started
     */
    public static void launch(Context c) {
        Intent intent = new Intent(c, DownloadActivity.class);
        c.startActivity(intent);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_download);

        mDownloadManager = Dian1Application.getInstance().getDownloadManager();
        mPlayerInterface = Dian1Application.getInstance().getPlayerEngineInterface();

        mHandler = new Handler();

        initView();
    }

    @Override
    protected void onPause() {
        //mHandler.removeCallbacks(mUpdateTimeTask);
        mDownloadManager.deregisterDownloadObserver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mDownloadManager.registerDownloadObserver(this);
        super.onResume();
    }

    private void initView() {
        setupHeader(getString(R.string.download_manager));
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);

        cbDownloaded = (RadioButton) findViewById(R.id.cb_downloaded);
        cbDownloading = (RadioButton) findViewById(R.id.cb_downloading);
        cbDownloaded.setOnCheckedChangeListener(this);
        cbDownloading.setOnCheckedChangeListener(this);

        setupListView();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_downloaded:
                    switchToPage(true);
                    break;
                case R.id.cb_downloading:
                    switchToPage(false);
                    break;
            }
        }
    }


    private void setupListView() {
        downloadedListView = (SwipeListView) findViewById(R.id.slv_downloaded);
        downloadedJobAdapter = new DownloadJobAdapter(this, DownloadJobAdapter.TYPE_COMPLETED);
        downloadedListView.setAdapter(downloadedJobAdapter);
        downloadingListView = (SwipeListView) findViewById(R.id.slv_downloading);
        downloadingJobAdapter = new DownloadJobAdapter(this, DownloadJobAdapter.TYPE_DOWNLOADING);
        downloadingListView.setAdapter(downloadingJobAdapter);
        downloadedJobAdapter.setList(mDownloadManager.getCompletedDownloads());
        downloadingJobAdapter.setList(mDownloadManager.getQueuedDownloads());
        downloadedListView.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                PlayerActivity.launch(DownloadActivity.this, AudioUtils.buildPlaylistFromDownloadJob(
                        mDownloadManager.getCompletedDownloads(), position));
            }
        });
        setupSwipeList();
    }

    private void switchToPage(boolean isDownloaded) {
        downloadedListView.setVisibility(isDownloaded ? View.VISIBLE : View.GONE);
        downloadingListView.setVisibility(isDownloaded ? View.GONE : View.VISIBLE);
    }

    private void setupSwipeList() {
        downloadedListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        downloadedListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        downloadedListView.setOffsetLeft(convertDpToPixel(250));
        downloadedListView.setAnimationTime(50);
        downloadedListView.setSwipeOpenOnLongPress(false);

        downloadingListView.setSwipeMode(SwipeListView.SWIPE_MODE_LEFT);
        downloadingListView.setSwipeActionLeft(SwipeListView.SWIPE_ACTION_REVEAL);
        downloadingListView.setOffsetLeft(convertDpToPixel(250));
        downloadingListView.setAnimationTime(50);
        downloadingListView.setSwipeOpenOnLongPress(false);
    }

    public int convertDpToPixel(float dp) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return (int) px;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.DownloadListView) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.download_context, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.add_to_playlist:
//			addToPlaylist(getJob(info.position));
                return true;
            case R.id.play_download:
                //playNow(info.position);
                return true;
            case R.id.delete_download:
//			deleteJob(getJob(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteJob(DownloadJob job) {
        mDownloadManager.deleteDownload(job);
//		DownloadJobAdapter adapter = (DownloadJobAdapter) mListView.getAdapter();
//		adapter.notifyDataSetChanged();
    }

    private void addToPlaylist(DownloadJob job) {
        Playlist playlist = mPlayerInterface.getPlaylist();
        if (playlist == null) {
            playlist = new Playlist();
            playlist.addPlaylistEntry(job.getPlaylistEntry());
            mPlayerInterface.openPlaylist(playlist);
        } else {
            playlist.addPlaylistEntry(job.getPlaylistEntry());
        }
    }

    @Override
    public void onDownloadChanged(DownloadJob downloadJob) {
        mHandler.post(new UpdateTimeTask(downloadJob));
    }

    /**
     * Runnable periodically querying DownloadService about
     * downloads
     */
    private class UpdateTimeTask implements Runnable {
        private DownloadJob downloadJob;
        public UpdateTimeTask(DownloadJob downloadJob) {
            this.downloadJob = downloadJob;
        }
        public void run() {
            downloadedListView.closeOpenedItems();
            downloadingListView.closeOpenedItems();
            downloadedJobAdapter.updateItem(downloadJob);
            downloadingJobAdapter.updateItem(downloadJob);
            mHandler.removeCallbacks(this);
        }
    }

}
