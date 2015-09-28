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

package net.dian1.player.api.impl;

import org.json.JSONException;
import org.json.JSONObject;

import net.dian1.player.api.Music;

/**
 * @author Lukasz Wisniewski
 */
public class TrackBuilder extends JSONBuilder<Music>  {

	@Override
	public Music build(JSONObject jsonObject) throws JSONException {
		Music music = new Music();
		music.setDuration(jsonObject.getInt("duration"));
		music.setId(jsonObject.getInt("id"));
		music.setName(jsonObject.getString("name"));
		music.setStream(jsonObject.getString("stream"));
		try {
			music.setRating(jsonObject.getDouble("rating"));
		} catch (JSONException e) {
			music.setRating(-1);
		}
		music.setUrl(jsonObject.getString("url"));
		try {
			music.setNumAlbum(jsonObject.getInt("numalbum"));
		} catch (JSONException e) {
			music.setNumAlbum(0);
		}
		return music;
	}

}
