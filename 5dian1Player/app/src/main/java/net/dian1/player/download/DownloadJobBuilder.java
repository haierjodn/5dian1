/*
 * Copyright (C) 2011 Teleca Poland Sp. z o.o. <android@teleca.com>
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

package net.dian1.player.download;

import android.content.ContentValues;
import android.database.Cursor;

import net.dian1.player.Dian1Application;
import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.db.AlbumDatabaseBuilder;
import net.dian1.player.db.DatabaseBuilder;
import net.dian1.player.db.TrackDatabaseBuilder;

/**
 * Download job builder
 * 
 * @author Bartosz Cichosz
 * 
 */
public class DownloadJobBuilder extends DatabaseBuilder<DownloadJob> {

	private static final String DOWNLOADED = "downloaded";

	@Override
	public DownloadJob build(Cursor query) {
		Music music = new TrackDatabaseBuilder().build(query);
		Album album = new AlbumDatabaseBuilder().build(query);

		PlaylistEntry pEntry = new PlaylistEntry();
		pEntry.setAlbum(album);
		pEntry.setMusic(music);

		DownloadJob dJob = new DownloadJob(pEntry, DownloadHelper
				.getDownloadPath(), 0, Dian1Application.getInstance()
				.getDownloadFormat());
		int progress = query.getInt(query.getColumnIndex(DOWNLOADED));
		if (progress == 1) {
			dJob.setProgress(100);
		}
		return dJob;
	}

	@Override
	public ContentValues deconstruct(DownloadJob t) {
		ContentValues values = new ContentValues();

		values.putAll(new TrackDatabaseBuilder().deconstruct(t
				.getPlaylistEntry().getMusic()));
		values.putAll(new AlbumDatabaseBuilder().deconstruct(t
				.getPlaylistEntry().getAlbum()));
		values.put(DOWNLOADED, (t.getProgress() == 100) ? 1 : 0);

		return values;
	}

}
