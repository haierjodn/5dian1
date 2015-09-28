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
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
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
import net.dian1.player.util.MockUtils;

import java.util.ArrayList;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadActivity extends BaseActivity implements DownloadObserver,
        CompoundButton.OnCheckedChangeListener {

    /**
     * Runnable periodically querying DownloadService about
     * downloads
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            //updateListView(mDownloadSpinner.getSelectedItemPosition());
        }
    };

    private Handler mHandler;

    private ListView downloadingListView, downloadedListView;
    private DownloadJobAdapter downloadedJobAdapter, downloadingJobAdapter;
    private ViewPager viewPager;
    private DownloadPageAdapter downloadPageAdapter;
    private RadioButton cbDownloaded, cbDownloading;

    private DownloadManager mDownloadManager;
    private PlayerEngine mPlayerInterface;

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
        mHandler.removeCallbacks(mUpdateTimeTask);
        mDownloadManager.deregisterDownloadObserver(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mDownloadManager.registerDownloadObserver(this);
        super.onResume();
    }

    private void initView() {
        setupHeader("下载管理");
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);

        cbDownloaded = (RadioButton) findViewById(R.id.cb_downloaded);
        cbDownloading = (RadioButton) findViewById(R.id.cb_downloading);
        cbDownloaded.setOnCheckedChangeListener(this);
        cbDownloading.setOnCheckedChangeListener(this);

        setupListView();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        downloadPageAdapter = new DownloadPageAdapter();
        viewPager.setAdapter(downloadPageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                cbDownloaded.setChecked(position == 0 ? true : false);
                cbDownloading.setChecked(position == 1 ? true : false);
            }
        });
        viewPager.setCurrentItem(0);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.cb_downloaded:
                    viewPager.setCurrentItem(0, true);
                    //updateListView(1);
                    break;
                case R.id.cb_downloading:
                    viewPager.setCurrentItem(1, true);
                    //updateListView(2);
                    break;
            }
        }
    }


    private int lastSpinnerPosition = -1;

    private void updateListView(int position) {
        ArrayList<DownloadJob> jobs = null;
        switch (position) {
            case 0:
                // Display ALL
                jobs = mDownloadManager.getAllDownloads();
                break;

            case 1:
                // Display Completed
                jobs = mDownloadManager.getCompletedDownloads();
                break;

            case 2:
                // Display Queued
                jobs = mDownloadManager.getQueuedDownloads();
                break;

            default:
                break;
        }

    }

    private void setupListView() {
        downloadedListView = new ListView(this);
        downloadedJobAdapter = new DownloadJobAdapter(this, DownloadJobAdapter.TYPE_COMPLETED);
        //downloadedJobAdapter.setList(mDownloadManager.getCompletedDownloads());
        downloadedJobAdapter.setList(MockUtils.buildDownloadListSample());
        downloadedListView.setAdapter(downloadedJobAdapter);
        TextView tvEmpty = new TextView(this);
        tvEmpty.setText("EMPTY VIEW 1");
        downloadedListView.setEmptyView(tvEmpty);

        downloadingListView = new ListView(this);
        downloadingJobAdapter = new DownloadJobAdapter(this, DownloadJobAdapter.TYPE_DOWNLOADING);
        //downloadingJobAdapter.setList(mDownloadManager.getQueuedDownloads());
        downloadingJobAdapter.setList(MockUtils.buildDownloadListSample());
        downloadingListView.setAdapter(downloadingJobAdapter);
//        TextView tvEmpty2 = new TextView(this);
//        tvEmpty2.setText("EMPTY VIEW 2");
//        tvEmpty2.setTextColor(getResources().getColor(R.color.font_orange));
//        tvEmpty2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//        tvEmpty2.setGravity(Gravity.CENTER);
//        tvEmpty2.setVisibility(View.GONE);
//        ((ViewGroup)downloadingListView.getParent()).addView(tvEmpty2);
//        downloadingListView.setEmptyView(tvEmpty2);
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
                playNow(info.position);
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

    private void playNow(int position) {
//		Playlist playlist = new Playlist();
//		playlist.addPlaylistEntry(getJob(position).getPlaylistEntry());
//		playlist.select(0);
//		mPlayerInterface.openPlaylist(playlist);
//		mPlayerInterface.play();
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
    public void onDownloadChanged(DownloadManager manager) {
        mHandler.post(mUpdateTimeTask);
    }

    private class DownloadPageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = position == 0 ? downloadedListView : downloadingListView;
            container.addView(view);
            return view;
        }
    }

}
