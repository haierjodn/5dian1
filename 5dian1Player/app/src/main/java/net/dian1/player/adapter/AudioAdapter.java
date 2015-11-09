package net.dian1.player.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import net.dian1.player.R;
import net.dian1.player.activity.PlayerActivity;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.media.local.AudioLoaderManager;
import net.dian1.player.util.AudioUtils;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter implements AudioLoaderManager.DataListener {

    private List<Music> mData = new ArrayList<Music>();
    private Activity mActivity = null;
    private LayoutInflater mInflater;
    private Playlist playlist;

    private int mMode = -1;

    public AudioAdapter(Activity context) {
        mActivity = context;
        mInflater = LayoutInflater.from(context);
        AudioLoaderManager.getInstance().addDataListener(this);
        playlist = new Playlist();
    }

    public void setData(List<Music> data) {
        mData.clear();
        if (data == null || data.isEmpty()) {
        } else {
            mData.addAll(data);
        }
        buildPlaylist();
        notifyDataSetChanged();
    }

    public void setPlaylist(List<PlaylistEntry> entries) {
        mData.clear();
        if(entries != null && entries.size() > 0) {
            for(int i=0; i < entries.size(); i++) {
                PlaylistEntry entry = entries.get(i);
                playlist.addPlaylistEntry(entry);
                mData.add(entry.getMusic());
            }
        }
        notifyDataSetChanged();
    }

    public void setMode(int mode) {
        if (mMode == mode) {
            return;
        }
        mMode = mode;
        onDataChange();

        notifyDataSetChanged();
    }

    public void buildPlaylist() {
        if (mData != null && !mData.isEmpty()) {
            playlist.removeAll();
            for(int i=0; i < mData.size(); i++) {
                //PlaylistEntry entry = new PlaylistEntry();
                playlist.addTrack(mData.get(i), null);
                //playlist.addPlaylistEntry(entry);
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(mInflater.inflate(R.layout.audio_item, null));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        final Music item = mData.get(position);
        viewHolder.mTextAudioName.setText(item.getName());
        String artist = item.getArtist();
        viewHolder.mTextAudioSinger.setText(artist);
        viewHolder.mTextAudioSinger.setVisibility(TextUtils.isEmpty(artist) ? View.GONE : View.VISIBLE);
        viewHolder.mAudioStatus.setVisibility(AudioUtils.isPlaying(item) ? View.VISIBLE : View.INVISIBLE);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PlayerActivity.launch(mActivity, playlist);
                playlist.select(position);
            }
        });
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private final class ViewHolder extends RecyclerView.ViewHolder {

        TextView mTextAudioName;
        TextView mTextAudioSinger;
        View mAudioStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextAudioName = (TextView) itemView
                    .findViewById(R.id.audio_name);
            mTextAudioSinger = (TextView) itemView
                    .findViewById(R.id.audio_singer);
            mAudioStatus = itemView
                    .findViewById(R.id.audio_status);
        }
    }

    @Override
    public void onDataChange() {
        setData(AudioLoaderManager.getInstance().getViewSongs());
    }

    @Override
    public List<Music> getSeletedSongs() {
        List<Music> songs = new ArrayList<Music>();
        return mData;
    }

    public List<Music> getAllSongs() {
        return mData;
    }

}
