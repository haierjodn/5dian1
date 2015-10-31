package net.dian1.player.widget;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.PlayerActivity;
import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.media.PlayerEngine;
import net.dian1.player.media.PlayerEngineListener;
import net.dian1.player.media.local.AudioLoaderTask;
import net.dian1.player.util.OnSeekToListenerImp;
import net.dian1.player.util.SeekToMode;

/**
 * Created by Dmall on 2015/10/23.
 */
public class PlayerControllerBottom extends LinearLayout implements View.OnClickListener{

    private BitmapUtils bitmapUtils;
    private TextView tvName;
    private TextView tvArtist;
    private ImageButton imPrev;
    private ImageButton imPlay;
    private ImageButton imNext;
    private ImageView ivCover;

    public PlayerControllerBottom(Context context) {
        this(context, null);
    }

    public PlayerControllerBottom(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.player_controller_layout_bottom, this, true);
        init();
    }

    private void init() {
        tvName = (TextView) findViewById(R.id.tv_name);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        imPrev = (ImageButton) findViewById(R.id.ib_prev);
        imPlay = (ImageButton) findViewById(R.id.ib_play);
        imNext = (ImageButton) findViewById(R.id.ib_next);
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        imPrev.setOnTouchListener(onPrevTouchListener);
        imNext.setOnTouchListener(onNextTouchListener);
        imPlay.setOnClickListener(this);
        ivCover.setOnClickListener(this);
        bitmapUtils = new BitmapUtils(getContext());
        bitmapUtils.configDefaultLoadingImage(R.drawable.player_albumcover_default);// 默认背景图片
        bitmapUtils.configDefaultLoadFailedImage(R.drawable.player_albumcover_default);// 加载失败图片
    }

    private PlayerEngine getPlayerEngine() {
        return Dian1Application.getInstance().getPlayerEngineInterface();
    }

    /**
     * next button action
     */
    private OnSeekToListenerImp onPrevTouchListener = new OnSeekToListenerImp(
            getPlayerEngine(), SeekToMode.EForward);

    /**
     * prev button action
     */
    private OnSeekToListenerImp onNextTouchListener = new OnSeekToListenerImp(
            getPlayerEngine(), SeekToMode.ERewind);

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_play:
                if (getPlayerEngine().isPlaying()) {
                    getPlayerEngine().pause();
                } else {
                    getPlayerEngine().play();
                }
                break;
            case R.id.iv_cover:
                PlayerActivity.launch(getContext(), false);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Dian1Application.getInstance().addPlayerEngineListener(mPlayerEngineListener);
        Playlist playlist = getPlayerEngine().getPlaylist();
        if(playlist != null) {
            mPlayerEngineListener.onTrackChanged(playlist.getSelectedTrack());
        }
        setLayoutVisibility(playlist != null ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Dian1Application.getInstance().removePlayerEngineListener(mPlayerEngineListener);
    }

    private void setLayoutVisibility(int visibility) {
        findViewById(R.id.ll_outter).setVisibility(visibility);
    }

    private PlayerEngineListener mPlayerEngineListener = new PlayerEngineListener() {

        @Override
        public void onTrackChanged(PlaylistEntry playlistEntry) {
            setLayoutVisibility(playlistEntry != null ? View.VISIBLE : View.GONE);
            Album mCurrentAlbum = playlistEntry.getAlbum();
            Music music = playlistEntry.getMusic();
            String albumPath = AudioLoaderTask.getAlbumArt(getContext().getContentResolver(), music.getAlbumId());
            if(TextUtils.isEmpty(albumPath)) {
                albumPath = mCurrentAlbum.getImage();
            }
            bitmapUtils.display(ivCover, albumPath);

            tvName.setText(playlistEntry.getMusic().getName());

            tvArtist.setText(playlistEntry.getMusic().getArtist());

            if (getPlayerEngine() != null) {
                if (getPlayerEngine().isPlaying()) {
                    imPlay.setImageResource(R.drawable.player_pause);
                } else {
                    imPlay.setImageResource(R.drawable.player_play);
                }
            }
        }

        @Override
        public void onTrackProgress(final int seconds) {
        }

        @Override
        public void onTrackBuffering(int percent) {
        }

        @Override
        public void onTrackStop() {
            imPlay.setImageResource(R.drawable.player_play);
        }

        @Override
        public boolean onTrackStart() {
            imPlay.setImageResource(R.drawable.player_pause);
            return true;
        }

        @Override
        public void onTrackPause() {
            imPlay.setImageResource(R.drawable.player_play);
        }

        @Override
        public void onTrackStreamError() {
            //Toast.makeText(getContext(), R.string.stream_error, Toast.LENGTH_LONG).show();
        }

    };
}
