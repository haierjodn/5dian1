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
import android.text.TextUtils;

import net.dian1.player.api.Music;
import net.dian1.player.model.MusicUrl;

import java.util.List;

public class TrackDatabaseBuilder extends DatabaseBuilder<Music> {

	private static final String TRACK_ID = "track_id";
	private static final String TRACK_NAME = "track_name";
	private static final String TRACK_DURATION = "track_duration";
	private static final String TRACK_URL = "track_url";
	private static final String TRACK_PATH = "track_path";
	private static final String TRACK_FILE_SIZE = "track_file_size";
	private static final String TRACK_STREAM = "track_stream";
	private static final String TRACK_RATING = "track_rating";
	private static final String ALBUM_TRACK_NUM = "album_track_num";
	private static final String TRACK_FORMAT = "track_format";

	@Override
	public Music build(Cursor query) {
		int columnName = query.getColumnIndex(TRACK_NAME);
		int columnStream = query.getColumnIndex(TRACK_STREAM);
		int columnUrl = query.getColumnIndex(TRACK_URL);
		int columnPath = query.getColumnIndex(TRACK_PATH);
		int columnFileSize = query.getColumnIndex(TRACK_FILE_SIZE);
		int columnDuration = query.getColumnIndex(TRACK_DURATION);
		int columnId = query.getColumnIndex(TRACK_ID);
		int columnRating = query.getColumnIndex(TRACK_RATING);
		int columnAlbumTrackNum = query.getColumnIndex(ALBUM_TRACK_NUM);
		int columnArtist = query.getColumnIndex(AlbumDatabaseBuilder.ARTIST_NAME);
		int columnCover = query.getColumnIndex(AlbumDatabaseBuilder.ALBUM_IMAGE);
		//int columnFormat = query.getColumnIndex(TRACK_FORMAT);
		
		Music music = new Music();
		music.setDuration(query.getInt(columnDuration));
		music.setId(query.getInt(columnId));
		music.setName(query.getString(columnName));
		music.setRating(query.getDouble(columnRating));
		MusicUrl musicUrl = new MusicUrl();
		musicUrl.setId(query.getInt(columnId));
		musicUrl.setFileSize(String.valueOf(query.getInt(columnFileSize)));
		musicUrl.setUrl(query.getString(columnUrl));
		musicUrl.setLocalUrl(query.getString(columnPath));
		music.addSongUrlList(musicUrl);
		music.setNumAlbum(query.getInt(columnAlbumTrackNum));
		if(columnArtist > -1) {
			music.setArtist(query.getString(columnArtist));
		}
		if(columnCover > -1) {
			music.setAlbum(query.getString(columnCover));
		}
		return music;
	}

	@Override
	public ContentValues deconstruct(Music music) {
		ContentValues values = new ContentValues();
		values.put(TRACK_NAME, music.getName());
		values.put(TRACK_DURATION, music.getDuration());
		values.put(TRACK_ID, music.getId());
		values.put(TRACK_RATING, music.getRating());
		values.put(ALBUM_TRACK_NUM, music.getNumAlbum());
		List<MusicUrl> musicUrlList = music.getSongUrlList();
		if(musicUrlList != null && musicUrlList.size() > 0) {
			MusicUrl musicUrl = musicUrlList.get(0);
			values.put(TRACK_FILE_SIZE, musicUrl.getFileSize());
			values.put(TRACK_PATH, musicUrl.getLocalUrl());
			values.put(TRACK_URL, musicUrl.getUrl());
		}
		if(!TextUtils.isEmpty(music.getAlbum())) {
			values.put(AlbumDatabaseBuilder.ALBUM_IMAGE, music.getAlbum());
		}
		if(!TextUtils.isEmpty(music.getArtist())) {
			values.put(AlbumDatabaseBuilder.ARTIST_NAME, music.getArtist());
		}
		return values;
	}

}
