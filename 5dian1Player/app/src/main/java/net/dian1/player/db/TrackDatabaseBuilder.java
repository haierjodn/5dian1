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

package net.dian1.player.db;

import android.content.ContentValues;
import android.database.Cursor;

import net.dian1.player.api.Music;

public class TrackDatabaseBuilder extends DatabaseBuilder<Music> {

	private static final String TRACK_ID = "track_id";
	private static final String TRACK_NAME = "track_name";
	private static final String TRACK_DURATION = "track_duration";
	private static final String TRACK_URL = "track_url";
	private static final String TRACK_STREAM = "track_stream";
	private static final String TRACK_RATING = "track_rating";
	private static final String ALBUM_TRACK_NUM = "album_track_num";

	@Override
	public Music build(Cursor query) {
		int columnName = query.getColumnIndex(TRACK_NAME);
		int columnStream = query.getColumnIndex(TRACK_STREAM);
		int columnUrl = query.getColumnIndex(TRACK_URL);
		int columnDuration = query.getColumnIndex(TRACK_DURATION);
		int columnId = query.getColumnIndex(TRACK_ID);
		int columnRating = query.getColumnIndex(TRACK_RATING);
		int columnAlbumTrackNum = query.getColumnIndex(ALBUM_TRACK_NUM);
		
		Music music = new Music();
		music.setDuration(query.getInt(columnDuration));
		music.setId(query.getInt(columnId));
		music.setName(query.getString(columnName));
		music.setRating(query.getDouble(columnRating));
//		music.setStream(query.getString(columnStream));
//		music.setUrl(query.getString(columnUrl));
		music.setNumAlbum(query.getInt(columnAlbumTrackNum));
		return music;
	}

	@Override
	public ContentValues deconstruct(Music music) {
		ContentValues values = new ContentValues();
		values.put(TRACK_NAME, music.getName());
//		values.put(TRACK_STREAM, music.getStream());
//		values.put(TRACK_URL, music.getUrl());
		values.put(TRACK_DURATION, music.getDuration());
		values.put(TRACK_ID, music.getId());
		values.put(TRACK_RATING, music.getRating());
		values.put(ALBUM_TRACK_NUM, music.getNumAlbum());
		return values;
	}

}
