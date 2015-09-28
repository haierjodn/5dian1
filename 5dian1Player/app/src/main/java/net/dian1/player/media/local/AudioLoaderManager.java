package net.dian1.player.media.local;

import android.content.Context;

import net.dian1.player.api.Music;

import java.util.ArrayList;
import java.util.List;

public class AudioLoaderManager {
	private static AudioLoaderManager sInstance = new AudioLoaderManager();

	public final static int SEARCH_BY_NAME = 0;
	public final static int SEARCH_BY_SINGER = 1;

	public interface DataListener {
		void onDataChange();

		List<Music> getSeletedSongs();
	}

	private List<DataListener> mListeners = new ArrayList<DataListener>();

	private Context mContext = null;
	private List<Music> mInteralSongs = new ArrayList<Music>();

	private AudioLoaderManager() {
	}

	public void init(Context context) {
		mContext = context;
	}

	public static AudioLoaderManager getInstance() {
		return sInstance;
	}

	public void addDataListener(DataListener listener) {
		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public void removeDataListener(DataListener listener) {
		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public void loadData() {
		// ToastUtils.showToast(mContext, "正在识别音乐"); //TODO custom bug
		new AudioLoaderTask(mContext, sInstance).loadData();
	}

	public void clear() {
		mInteralSongs.clear();
	}


	public void add(Music song) {
		mInteralSongs.add(song);
	}

	public void add(List<Music> songs) {
		mInteralSongs.addAll(songs);
	}


	public List<Music> getViewSongs() {
			return mInteralSongs;
	}


	public void deleteSongs(List<Music> songs) {
			mInteralSongs.removeAll(songs);

		notifyDataChange();
	}

	public void notifyDataChange() {
		for (DataListener listener : mListeners) {
			listener.onDataChange();
		}
	}
}
