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

package net.dian1.player.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.download.DownloadJob;
import net.dian1.player.R;

/**
 * @author Lukasz Wisniewski
 */
public class DownloadJobAdapter extends ArrayListAdapter<DownloadJob> {

	public final static int TYPE_COMPLETED = 0;
	public final static int TYPE_DOWNLOADING = 1;

	private int type = 0;

	public DownloadJobAdapter(Activity context, int listType) {
		super(context);
		type = listType;
	}

	public void updateItem(final DownloadJob downloadJob) {
		for (int i = 0; i < mList.size(); i++) {
			if(mList.get(i).getPlaylistEntry().getMusic().getId() == downloadJob.getPlaylistEntry().getMusic().getId()) {
				mList.set(i, downloadJob);
				notifyDataSetChanged();
				return;
			}
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			row = LayoutInflater.from(mContext).inflate(R.layout.download_row, null);
			holder = new ViewHolder();
			holder.songName = (TextView)row.findViewById(R.id.TrackRowName);
			holder.songArtistAlbum = (TextView)row.findViewById(R.id.TrackRowArtistAlbum);
			holder.songProgressText = (TextView)row.findViewById(R.id.TrackRowProgress);
			holder.tvDelete = row.findViewById(R.id.tv_delete);
			holder.songProgressText.setVisibility(type == TYPE_COMPLETED ? View.INVISIBLE : View.VISIBLE);
			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}

		final PlaylistEntry playlistEntry = mList.get(position).getPlaylistEntry();
		holder.songName.setText(playlistEntry.getMusic().getName());
		holder.songArtistAlbum.setText(playlistEntry.getAlbum().getArtistName()+" - "+playlistEntry.getAlbum().getName());

		if(type == TYPE_DOWNLOADING) {
			if (mList.get(position).getProgress() == 100) {
				holder.songProgressText.setText("");//COMPLETE
			} else {
				holder.songProgressText.setText(mList.get(position).getProgress() + "%");
			}
		}
		holder.tvDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Dian1Application.getInstance().getDownloadManager().deleteDownload(mList.get(position));
			}
		});

		return row;
	}

	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView songName;
		TextView songArtistAlbum;
		TextView songProgressText;
		View tvDelete;
	}

}
