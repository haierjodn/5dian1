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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.Playlist.PlaylistPlaybackMode;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.dialog.AddToPlaylistDialog;
import net.dian1.player.dialog.LoadingDialog;
import net.dian1.player.dialog.LyricsDialog;
import net.dian1.player.download.DownloadManager;
import net.dian1.player.download.DownloadObserver;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.log.LogUtil;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.media.PlayerEngineListener;
import net.dian1.player.media.local.AudioLoaderTask;
import net.dian1.player.model.SearchResult;
import net.dian1.player.util.AudioUtils;
import net.dian1.player.util.Helper;
import net.dian1.player.util.OnSeekToListenerImp;
import net.dian1.player.util.SeekToMode;

/**
 * 1. Common Music Player
 * 2. 支持随便听模式, playlist = null
 *
 */
public class PlayerActivity extends Activity implements OnClickListener {

    private PlayerEngine getPlayerEngine() {
        return Dian1Application.getInstance().getPlayerEngineInterface();
    }

    private Playlist mPlaylist;

    private Album mCurrentAlbum;
    private PlaylistEntry mPlaylistEntry;

    // XML layout
    private TextView mArtistTextView;
    private TextView mSongTextView;
    private TextView mCurrentTimeTextView;
    private TextView mTotalTimeTextView;
    private SeekBar mProgressBar;

    private ImageButton mPlayImageButton;
    private ImageButton mNextImageButton;
    private ImageButton mPrevImageButton;
    private ImageButton mFavorImageButton;
    private ImageButton mShuffleImageButton;
    private ImageButton ibDownload;
    private ImageView mCoverImageView;

    //private GestureOverlayView mGesturesOverlayView;

    //private ReflectableLayout mReflectableLayout;
    //private ReflectiveSurface mReflectiveSurface;

    private String mBetterRes;
    private LoadingDialog mUriLoadingDialog;


    private BitmapUtils bitmapUtils;

    public static void launch(Context c, Playlist playlist) {
        Intent intent = new Intent(c, PlayerActivity.class);
        intent.putExtra("playlist", playlist);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
    }

    public static void launch(Context c, boolean startPlay) {
        Intent intent = new Intent(c, PlayerActivity.class);
        intent.putExtra("toPlay", startPlay);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        c.startActivity(intent);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Dian1Application.TAG, "PlayerActivity.onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.player);

        bitmapUtils = new BitmapUtils(this);
        bitmapUtils.configDefaultLoadingImage(R.drawable.player_albumcover_default);// 默认背景图片
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.player_albumcover_default);// 加载失败图片

        initView();

        handleIntent();

        Dian1Application.getInstance().getDownloadManager().registerDownloadObserver(new DownloadObserver() {
            @Override
            public void onDownloadChanged(DownloadManager manager) {

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Dian1Application.TAG, "PlayerActivity.onResume");

        // register UI listener
        Dian1Application.getInstance().addPlayerEngineListener(mPlayerEngineListener);

        //refresh shuffle and repeat icons
        if (getPlayerEngine().getPlaylist() != null) {
            switch (getPlayerEngine().getPlaylist().getPlaylistPlaybackMode()) {
                case NORMAL:
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat);
                    break;
                case REPEAT:
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat_one);
                    break;
                case SHUFFLE:
                    mShuffleImageButton.setImageResource(R.drawable.player_random);
                    break;
                case SHUFFLE_AND_REPEAT:
                    mShuffleImageButton.setImageResource(R.drawable.player_random);
                    break;
            }
        }
    }

    private void initView() {
        // XML binding
        mBetterRes = getResources().getString(R.string.better_res);

        mArtistTextView = (TextView) findViewById(R.id.ArtistTextView);
        mSongTextView = (TextView) findViewById(R.id.SongTextView);
        //AutoScrolling of long song titles
        mSongTextView.setEllipsize(TruncateAt.MARQUEE);
        mSongTextView.setHorizontallyScrolling(true);
        mSongTextView.setSelected(true);

        mCurrentTimeTextView = (TextView) findViewById(R.id.CurrentTimeTextView);
        mTotalTimeTextView = (TextView) findViewById(R.id.TotalTimeTextView);

        mCoverImageView = (ImageView) findViewById(R.id.CoverImageView);
        mCoverImageView.setOnClickListener(mCoverOnClickListener);

        mProgressBar = (SeekBar) findViewById(R.id.ProgressBar);

        mPlayImageButton = (ImageButton) findViewById(R.id.PlayImageButton);
        mPlayImageButton.setOnClickListener(mPlayOnClickListener);

        mNextImageButton = (ImageButton) findViewById(R.id.NextImageButton);
        mNextImageButton.setOnTouchListener(mOnForwardTouchListener);

        mPrevImageButton = (ImageButton) findViewById(R.id.PrevImageButton);
        mPrevImageButton.setOnTouchListener(mOnRewindTouchListener);

        mShuffleImageButton = (ImageButton) findViewById(R.id.ShuffleImageButton);
        mShuffleImageButton.setOnClickListener(mShuffleOnClickListener);

        ibDownload = (ImageButton) findViewById(R.id.ib_download);
        ibDownload.setOnClickListener(downloadOnClickListener);

        mFavorImageButton = (ImageButton) findViewById(R.id.ib_favorite);
        mFavorImageButton.setOnClickListener(favorOnClickListener);

        mCurrentAlbum = null;

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                int progress = mProgressBar.getProgress();
                getPlayerEngine().seekTo(progress * 1000);
            }
        });
    }

    public void doCloseActivity() {
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(Dian1Application.TAG, "PlayerActivity.onPause");
        // unregister UI listener
        Dian1Application.getInstance().removePlayerEngineListener(mPlayerEngineListener);
    }

    /**
     * Makes 4-way media visible
     */
    private void setMediaVisible() {
        mPlayImageButton.setVisibility(View.VISIBLE);
        mNextImageButton.setVisibility(View.VISIBLE);
        mPrevImageButton.setVisibility(View.VISIBLE);
        //mStopImageButton.setVisibility(View.VISIBLE);
        mShuffleImageButton.setVisibility(View.VISIBLE);
        ibDownload.setVisibility(View.VISIBLE);
    }

    /**
     * Makes 4-way media gone
     */
    private void setMediaGone() {
        mPlayImageButton.setVisibility(View.GONE);
        mNextImageButton.setVisibility(View.GONE);
        mPrevImageButton.setVisibility(View.GONE);
        //mStopImageButton.setVisibility(View.GONE);
        mShuffleImageButton.setVisibility(View.GONE);
        ibDownload.setVisibility(View.GONE);
    }

    private void getMusicDetailFromNetwork() {
        long musicId = 1;
        ApiManager.getInstance().send(new ApiRequest(this, ApiData.MusicSuibianApi.URL, net.dian1.player.model.Music.class,
                ApiData.MusicSuibianApi.getParams(null), new OnResultListener<net.dian1.player.model.Music>() {

            @Override
            public void onResult(net.dian1.player.model.Music response) {
                //dismissDialog();
                updateView(response);
            }

            @Override
            public void onResultError(String msg, String code) {
                //dismissDialog();
                //showToastSafe(msg, Toast.LENGTH_SHORT);
            }
        }));
    }

    private void updateView(net.dian1.player.model.Music music) {
        if(music != null) {

        }
    }

    /**
     * Launches fade in/out sequence
     */
    private OnClickListener mCoverOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (mPlayImageButton.getVisibility() == View.GONE) {
                setMediaVisible();
            }
        }

    };

    /**
     * on click play/pause and open playlist if necessary
     */
    private OnClickListener mPlayOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (getPlayerEngine().isPlaying()) {
                getPlayerEngine().pause();
            } else {
                getPlayerEngine().play();
            }
        }

    };

    /**
     * next button action
     */
    private OnSeekToListenerImp mOnForwardTouchListener = new OnSeekToListenerImp(
            getPlayerEngine(), SeekToMode.EForward);

    /**
     * prev button action
     */
    private OnSeekToListenerImp mOnRewindTouchListener = new OnSeekToListenerImp(
            getPlayerEngine(), SeekToMode.ERewind);


    /**
     * stop button action
     */
    private OnClickListener mStopOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            getPlayerEngine().stop();
        }

    };

    /**
     * favor button action
     */
    private OnClickListener favorOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mFavorImageButton.setImageResource(R.drawable.player_favor_already);
        }

    };

    /**
     * shuffle button action
     */
    private OnClickListener mShuffleOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (getPlayerEngine().getPlaybackMode()) {
                case NORMAL:
                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE);
                    mShuffleImageButton.setImageResource(R.drawable.player_random);
                    break;
                case REPEAT:
                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.NORMAL);
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat);
                    break;
                case SHUFFLE:
                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.REPEAT);
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat_one);
                    break;
                case SHUFFLE_AND_REPEAT:
                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.REPEAT);
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat_one);
                    break;
            }
        }

    };

    /**
     * repeat button action
     */
    private OnClickListener downloadOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if(mPlaylistEntry != null) {
                Dian1Application.getInstance().getDownloadManager().download(mPlaylistEntry);
            }
            //goto download
            DownloadActivity.launch(PlayerActivity.this);
        }
    };

    /**
     * PlayerEngineListener implementation, manipulates UI
     */
    private PlayerEngineListener mPlayerEngineListener = new PlayerEngineListener() {

        @Override
        public void onTrackChanged(PlaylistEntry playlistEntry) {
            if(playlistEntry == null) {
                return;
            }
            mPlaylistEntry = playlistEntry;
            mCurrentAlbum = playlistEntry.getAlbum();
            if(mCurrentAlbum != null) {
                mArtistTextView.setText("-- " + playlistEntry.getAlbum().getArtistName() + " --");
                bitmapUtils.display(mCoverImageView, playlistEntry.getAlbum().getImage());
            }
            Music music = playlistEntry.getMusic();
            String albumPath = AudioLoaderTask.getAlbumArt(getContentResolver(), music.getAlbumId());
            if(TextUtils.isEmpty(albumPath)) {
                albumPath = mCurrentAlbum.getImage();
            }
            if(TextUtils.isEmpty(albumPath)) {
                albumPath = music.getAlbum();
            }
            bitmapUtils.display(mCoverImageView, albumPath);

            mSongTextView.setText(playlistEntry.getMusic().getName());
            ((TextView) findViewById(R.id.tv_title)).setText(playlistEntry.getMusic().getName());
            mCurrentTimeTextView.setText(Helper.secondsToString(0));
            mTotalTimeTextView.setText(Helper.secondsToString(playlistEntry.getMusic().getDuration()));

            //mCoverImageView.
            //mCoverImageView.setImageUrl(playlistEntry.getAlbum().getImage().replaceAll("1.100.jpg", mBetterRes)); // Get higher resolution image 300x300

            mProgressBar.setProgress(0);
            mProgressBar.setMax(playlistEntry.getMusic().getDuration());
            mCoverImageView.performClick();


            if (getPlayerEngine() != null) {
                if (getPlayerEngine().isPlaying()) {
                    mPlayImageButton.setImageResource(R.drawable.player_pause);
                } else {
                    mPlayImageButton.setImageResource(R.drawable.player_play);
                }
            }
        }

        @Override
        public void onTrackProgress(final int seconds) {
            mCurrentTimeTextView.setText(Helper.secondsToString(seconds));
            mProgressBar.setProgress(seconds);
        }

        @Override
        public void onTrackBuffering(int percent) {
			int secondaryProgress = (int) (((float)percent/100)*mProgressBar.getMax());
			mProgressBar.setSecondaryProgress(secondaryProgress);
        }

        @Override
        public void onTrackStop() {
            mPlayImageButton.setImageResource(R.drawable.player_play);
        }

        @Override
        public boolean onTrackStart() {
            mPlayImageButton.setImageResource(R.drawable.player_pause);
            return true;
        }

        @Override
        public void onTrackPause() {
            mPlayImageButton.setImageResource(R.drawable.player_play);
        }

        @Override
        public void onTrackStreamError() {
            Toast.makeText(PlayerActivity.this, R.string.stream_error, Toast.LENGTH_LONG).show();
        }

    };

    /**
     * Loads playlist to the PlayerEngine
     */
    private void handleIntent() {
        Intent intent = getIntent();
        Playlist playlist = null;
        if (intent != null) {
            playlist = (Playlist) intent.getSerializableExtra("playlist");
            boolean toPlay = intent.getBooleanExtra("toPlay", false);
            if(playlist != null) {
                setupPlaylist(playlist);
                mPlayerEngineListener.onTrackChanged(getPlayerEngine().getPlaylist().getSelectedTrack());
            } else {
                if (getPlayerEngine() != null && getPlayerEngine().getPlaylist() != null) {
                    mPlayerEngineListener.onTrackChanged(getPlayerEngine().getPlaylist().getSelectedTrack());
                }
                if (toPlay && !getPlayerEngine().isPlaying()) {
                    downloadPlaylist();
                }
            }
        }
    }

    private void setupPlaylist(Playlist playlist) {
        Log.i(Dian1Application.TAG, "PlayerActivity.loadPlaylist");
        if (playlist == null) {
            return;
        }
        mPlaylist = playlist;
        if (mPlaylist != getPlayerEngine().getPlaylist()) {
            getPlayerEngine().openPlaylist(mPlaylist);
            getPlayerEngine().play();
        }
    }

    /**
     * 随便听模式下载随机歌单
     */
    private void downloadPlaylist() {
        ApiManager.getInstance().send(new ApiRequest(this, ApiData.MusicSuibianApi.URL, net.dian1.player.model.Music.class,
                ApiData.MusicSuibianApi.getParams(null), new OnResultListener<net.dian1.player.model.Music>() {

            @Override
            public void onResult(net.dian1.player.model.Music response) {
                //updateView(response);
                setupPlaylist(AudioUtils.buildPlaylist(response, 0));
            }

            @Override
            public void onResultError(String msg, String code) {
            }
        }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    public void albumClickHandler(View target) {
        //AlbumActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum());
    }

    public void artistClickHandler(View target) {
        ArtistActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum().getArtistName());
    }

    public void homeClickHandler(View target) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void addOnClick(View v) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog(PlayerActivity.this);
        dialog.setPlaylistEntry(getPlayerEngine().getPlaylist().getSelectedTrack());
        dialog.show();
    }

    public void equalizerOnClick(View v) {
        Intent intent = new Intent(this, EqualizerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void downloadOnClick(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(PlayerActivity.this)
                .setTitle(R.string.download_track_q)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Dian1Application.getInstance().getDownloadManager().download(getPlayerEngine().getPlaylist().getSelectedTrack());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        alertDialog.show();
    }

}
