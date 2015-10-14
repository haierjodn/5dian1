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
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.util.LogUtils;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.api.Album;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.Playlist.PlaylistPlaybackMode;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.PlaylistRemote;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;
import net.dian1.player.dialog.AddToPlaylistDialog;
import net.dian1.player.dialog.LoadingDialog;
import net.dian1.player.dialog.LyricsDialog;
import net.dian1.player.dialog.PlayerAlbumLoadingDialog;
import net.dian1.player.dialog.PlaylistRemoteLoadingDialog;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.media.PlayerEngineListener;
import net.dian1.player.media.local.AudioLoaderTask;
import net.dian1.player.util.Helper;
import net.dian1.player.util.OnSeekToListenerImp;
import net.dian1.player.util.SeekToMode;

import org.json.JSONException;

import java.util.List;

/**
 * Central part of the UI. Touching cover fades in 4-way media buttons.
 * 4-way media buttons fade out after certain amount of time. Other parts
 * of layout are progress bar, total play time, played time, song title,
 * artist name and jamendo slider.<br><br>
 * <p/>
 * License information is implemented overlaying CreativeCommons logo over
 * the album picture. Information about type of license is retrieved concurrently
 * to track bufferring.
 *
 * @author Lukasz Wisniewski
 */
public class PlayerActivity extends Activity implements OnClickListener {

    private PlayerEngine getPlayerEngine() {
        return Dian1Application.getInstance().getPlayerEngineInterface();
    }

    private Playlist mPlaylist;

    private Album mCurrentAlbum;

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

    /**
     * Launch this Activity from the outside, with defined playlist
     *
     * @param c        context from which Activity should be started
     * @param playlist to be played
     */
    public static void launch(Context c, Playlist playlist) {
        Intent intent = new Intent(c, PlayerActivity.class);
        intent.putExtra("playlist", playlist);

		/*
         * For example, consider a task consisting of the activities:
		 * A, B, C, D. If D calls startActivity() with an Intent that
		 * resolves to the component of activity B, then C and D will
		 * be finished and B receive the given Intent, resulting in 
		 * the stack now being: A, B. 
		 */
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

        //mReflectableLayout = (ReflectableLayout) findViewById(R.id.ReflectableLayout);
        //mReflectiveSurface = (ReflectiveSurface) findViewById(R.id.ReflectiveSurface);

        //if (mReflectableLayout != null && mReflectiveSurface != null) {
            //mReflectableLayout.setReflectiveSurface(mReflectiveSurface);
            //mReflectiveSurface.setReflectableLayout(mReflectableLayout);
        //}

        handleIntent();

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

        //mGesturesOverlayView = (GestureOverlayView) findViewById(R.id.gestures);
        //mGesturesOverlayView.addOnGesturePerformedListener(Dian1Application
        //        .getInstance().getPlayerGestureHandler());

        bitmapUtils = new BitmapUtils(this);
        bitmapUtils.configDefaultLoadingImage(R.drawable.icon_portrait);// 默认背景图片
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon_portrait);// 加载失败图片

        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);

        mProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
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

    @Override
    public void onResume() {
        super.onResume();
        Log.i(Dian1Application.TAG, "PlayerActivity.onResume");

        // register UI listener
        Dian1Application.getInstance().setPlayerEngineListener(mPlayerEngineListener);

        // refresh UI
        if (getPlayerEngine() != null) {
            // the playlist is empty, abort playback, show message
            if (getPlayerEngine().getPlaylist() == null || getPlayerEngine().getPlaylist().getSelectedTrack() == null) {
                //if playlist comes from link service, don't close activity and wait for playlist
                if (mUriLoadingDialog != null) {
                    mUriLoadingDialog = null;

                } else {
                    Toast.makeText(this, R.string.no_tracks, Toast.LENGTH_LONG).show();
                    finish();
                }

                return;
            }
            mPlayerEngineListener.onTrackChanged(getPlayerEngine().getPlaylist().getSelectedTrack());
        }

        //refresh shuffle and repeat icons
        if (getPlayerEngine().getPlaylist() != null) {
            switch (getPlayerEngine().getPlaylist().getPlaylistPlaybackMode()) {
                case NORMAL:
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat);
                    //ibDownload.setImageResource(R.drawable.player_repeat_off);
                    break;
                case REPEAT:
                    mShuffleImageButton.setImageResource(R.drawable.player_repeat_one);
                    //ibDownload.setImageResource(R.drawable.player_repeat_on);
                    break;
                case SHUFFLE:
                    mShuffleImageButton.setImageResource(R.drawable.player_random);
                    //ibDownload.setImageResource(R.drawable.player_repeat_off);
                    break;
                case SHUFFLE_AND_REPEAT:
                    mShuffleImageButton.setImageResource(R.drawable.player_random);
                    //ibDownload.setImageResource(R.drawable.player_repeat_on);
                    break;
            }
        }

        //boolean gesturesEnabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("gestures", true);
        //mGesturesOverlayView.setEnabled(gesturesEnabled);
    }


    public void doCloseActivity() {
        finish();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(Dian1Application.TAG, "PlayerActivity.onPause");

        // unregister UI listener
        Dian1Application.getInstance().setPlayerEngineListener(null);
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
            //when to shop
            //getPlayerEngine().stop();
        }

    };

    /**
     * next button action
     */
    private OnSeekToListenerImp mOnForwardTouchListener = new OnSeekToListenerImp(
            this, getPlayerEngine(), SeekToMode.EForward);

    /**
     * prev button action
     */
    private OnSeekToListenerImp mOnRewindTouchListener = new OnSeekToListenerImp(
            this, getPlayerEngine(), SeekToMode.ERewind);


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
            //goto download
            DownloadActivity.launch(PlayerActivity.this);
            //TODO 直接下载某首歌
//            switch (getPlayerEngine().getPlaybackMode()) {
//                case NORMAL:
//                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.REPEAT);
//                    ibDownload.setImageResource(R.drawable.player_repeat_one);
//                    break;
//                case REPEAT:
//                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.NORMAL);
//                    ibDownload.setImageResource(R.drawable.player_repeat);
//                    break;
//                case SHUFFLE:
//                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE_AND_REPEAT);
//                    ibDownload.setImageResource(R.drawable.player_random);
//                    break;
//                case SHUFFLE_AND_REPEAT:
//                    getPlayerEngine().setPlaybackMode(PlaylistPlaybackMode.SHUFFLE);
//                    ibDownload.setImageResource(R.drawable.player_random);
//                    break;
//            }
        }
    };

    /**
     * PlayerEngineListener implementation, manipulates UI
     */
    private PlayerEngineListener mPlayerEngineListener = new PlayerEngineListener() {

        @Override
        public void onTrackChanged(PlaylistEntry playlistEntry) {
            mCurrentAlbum = playlistEntry.getAlbum();
            if(mCurrentAlbum != null) {
                mArtistTextView.setText("-- " + playlistEntry.getAlbum().getArtistName() + " --");
                bitmapUtils.display(mCoverImageView, playlistEntry.getAlbum().getImage());
            }
            Music music = playlistEntry.getMusic();
            String localAlbumPath = AudioLoaderTask.getAlbumArt(getContentResolver(), music.getAlbumId());
            if(!TextUtils.isEmpty(localAlbumPath)) {
                bitmapUtils.display(mCoverImageView, localAlbumPath);
            }

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
        Log.i(Dian1Application.TAG, "PlayerActivity.handleIntent");

        // This will be result of this intent handling
        Playlist playlist = null;

        // We need to handle Uri
        if (getIntent().getData() != null) {

            // Check if this intent was already once parsed
            // we don't need to do that again
            if (!getIntent().getBooleanExtra("handled", false)) {
                mUriLoadingDialog = (LoadingDialog) new UriLoadingDialog(this, R.string.loading, R.string.loading_fail).execute();
            }

        } else {
            playlist = (Playlist) getIntent().getSerializableExtra("playlist");
            loadPlaylist(playlist);
        }
    }

    private void loadPlaylist(Playlist playlist) {
        Log.i(Dian1Application.TAG, "PlayerActivity.loadPlaylist");
        if (playlist == null)
            return;

        mPlaylist = playlist;
        if (mPlaylist != getPlayerEngine().getPlaylist()) {
            //getPlayerEngine().stop();
            getPlayerEngine().openPlaylist(mPlaylist);
            getPlayerEngine().play();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }

    /**
     * This creates playlist based on url that was passed in the intent,
     * e.g. http://www.jamendo.com/pl/track/325654 or http://www.jamendo.com/pl/album/7505
     *
     * @author Lukasz Wisniewski
     */
    private class UriLoadingDialog extends LoadingDialog<Void, Playlist> {

        public UriLoadingDialog(Activity activity, int loadingMsg, int failMsg) {
            super(activity, loadingMsg, failMsg);
        }

        @Override
        public Playlist doInBackground(Void... params) {
            Playlist playlist = null;

            Intent intent = getIntent();
            String action = intent.getAction();

            if (Intent.ACTION_VIEW.equals(action)) {
                playlist = new Playlist();

                List<String> segments = intent.getData().getPathSegments();
                String mode = segments.get((segments.size() - 2));
                int id = 0;
                try {
                    id = Integer.parseInt(segments.get((segments.size() - 1)));
                } catch (NumberFormatException e) {
                    doCancel();
                    return playlist;
                }
                JamendoGet2Api service = new JamendoGet2ApiImpl();

                if (mode.equals("track")) {
                    try {
                        Music[] musics = service.getTracksByTracksId(new int[]{id}, Dian1Application.getInstance().getStreamEncoding());
                        Album[] albums = service.getAlbumsByTracksId(new int[]{id});
                        albums[0].setMusics(musics);
                        playlist.addTracks(albums[0]);
                    } catch (JSONException e) {
                        Log.e(Dian1Application.TAG, "sth went completely wrong");
                        PlayerActivity.this.finish();
                        e.printStackTrace();
                    } catch (WSError e) {
                        publishProgress(e);
                    }
                }

                if (mode.equals("album")) {
                    try {
                        Album album = service.getAlbumById(id);
                        Music[] musics = service.getAlbumTracks(album, Dian1Application.getInstance().getStreamEncoding());

                        album.setMusics(musics);
                        playlist.addTracks(album);
                    } catch (JSONException e) {
                        Log.e("jamendroid", "sth went completely wrong");
                        PlayerActivity.this.finish();
                        e.printStackTrace();
                    } catch (WSError e) {
                        publishProgress(e);
                    }
                }
            }

            intent.putExtra("handled", true);
            return playlist;
        }

        @Override
        public void doStuffWithResult(Playlist result) {
            loadPlaylist(result);
        }

    }

    public void albumClickHandler(View target) {
        //AlbumActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum());
    }

    public void artistClickHandler(View target) {
        ArtistActivity.launch(this, getPlayerEngine().getPlaylist().getSelectedTrack().getAlbum().getArtistName());
    }

    public void playlistClickHandler(View target) {
        PlaylistActivity.launch(this, false);
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

    public void lyricsOnClick(View v) {
        Music music = getPlayerEngine().getPlaylist().getSelectedTrack().getMusic();
        new LyricsDialog(PlayerActivity.this, music).show();
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

    public void shareOnClick(View v) {
        if (mPlaylist == null || mPlaylist.getSelectedTrack() == null) {
            return;
        }
        PlaylistEntry entry = mPlaylist.getSelectedTrack();
        Helper.share(PlayerActivity.this, entry);
    }




}
