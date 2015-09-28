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

import java.util.Comparator;

import net.dian1.player.api.Music;

public class TrackComparator implements Comparator<Music> {

	@Override
	public int compare(Music music1, Music music2) {
		if(music1.getNumAlbum() > music2.getNumAlbum()){
			return 1;
		} else if(music1.getNumAlbum() < music2.getNumAlbum()){
			return -1;
		} else
			return 0;
	}
	
}
