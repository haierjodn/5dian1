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

package net.dian1.player.db.builder;

import android.content.ContentValues;
import android.database.Cursor;

import net.dian1.player.api.Album;
import net.dian1.player.db.DatabaseBuilder;
import net.dian1.player.db.table.UserInfoTable;
import net.dian1.player.model.UserInfo;

public class UserInfoDatabaseBuilder extends DatabaseBuilder<UserInfo> {

	@Override
	public UserInfo build(Cursor query) {
		int columnLoginId = query.getColumnIndex(UserInfoTable.LOGIN_ID);
		int columnPortrait = query.getColumnIndex(UserInfoTable.PORTRAIT);
		int columnToken = query.getColumnIndex(UserInfoTable.TOKEN);
		int columnPhone = query.getColumnIndex(UserInfoTable.PHONE);
		int columnLevel = query.getColumnIndex(UserInfoTable.LEVEL);
		int columnLevelName = query.getColumnIndex(UserInfoTable.LEVEL_NAME);
		int columnNickname = query.getColumnIndex(UserInfoTable.NICK_NAME);
		int columnRealName = query.getColumnIndex(UserInfoTable.REAL_NAME);
		int columnVip = query.getColumnIndex(UserInfoTable.IS_APP_VIP);
		int columnExpired = query.getColumnIndex(UserInfoTable.EXPIRED_TIME);
		
		UserInfo userInfo = new UserInfo();
		userInfo.setLoginId(query.getInt(columnLoginId));
		userInfo.setPortrait(query.getString(columnPortrait));
		userInfo.setToken(query.getString(columnToken));
		userInfo.setPhone(query.getString(columnPhone));
		userInfo.setLevel(query.getInt(columnLevel));
		userInfo.setLevelName(query.getString(columnLevelName));
		userInfo.setNickname(query.getString(columnNickname));
		userInfo.setRealName(query.getString(columnRealName));
		userInfo.setIsappvip(query.getInt(columnVip));
		userInfo.setExpiredTime(query.getString(columnExpired));
		return userInfo;
	}

	@Override
	public ContentValues deconstruct(UserInfo userInfo) {
		ContentValues values = new ContentValues();
		values.put(UserInfoTable.LOGIN_ID, userInfo.getLoginId());
		values.put(UserInfoTable.PORTRAIT, userInfo.getPortrait());
		values.put(UserInfoTable.TOKEN, userInfo.getToken());
		values.put(UserInfoTable.PHONE, userInfo.getPhone());
		values.put(UserInfoTable.LEVEL, userInfo.getLevel());
		values.put(UserInfoTable.LEVEL_NAME, userInfo.getLevelName());
		values.put(UserInfoTable.NICK_NAME, userInfo.getNickname());
		values.put(UserInfoTable.REAL_NAME, userInfo.getRealName());
		values.put(UserInfoTable.IS_APP_VIP, userInfo.getIsappvip());
		values.put(UserInfoTable.EXPIRED_TIME, userInfo.getExpiredTime());
		return values;
	}

}
