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

package net.dian1.player.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import net.dian1.player.R;
import net.dian1.player.adapter.ArrayListAdapter;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.Album;
import net.dian1.player.model.Music;
import net.dian1.player.model.SearchResult;
import net.dian1.player.util.AudioUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends Activity implements OnClickListener {


	private ListView gvRepoList;

	private SearchAdapter searchAdapter;


	public static void launch(Context c){
		Intent intent = new Intent(c, SearchActivity.class);
		c.startActivity(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search);

		initHeader();
		searchAdapter = new SearchAdapter(this);
		gvRepoList = (ListView)findViewById(R.id.lv_music);
		gvRepoList.setAdapter(searchAdapter);
		gvRepoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//AlbumActivity.launch(SearchActivity.this);
				PlayerActivity.launch(SearchActivity.this, AudioUtils.buildPlaylist(searchAdapter.getList(), position));
			}
		});
		refreshData();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initHeader() {
		findViewById(R.id.iv_back).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_back:
				onBackPressed();
				break;
		}
	}

	private void refreshData() {
		ApiManager.getInstance().send(new ApiRequest(this, ApiData.MusicSearchApi.URL, SearchResult.class,
				ApiData.MusicSearchApi.getParams(1), new OnResultListener<SearchResult>() {

			@Override
			public void onResult(SearchResult response) {
				updateView(response);
			}

			@Override
			public void onResultError(String msg, String code) {
				//dismissDialog();
				//showToastSafe(msg, Toast.LENGTH_SHORT);
			}
		}));
	}

	private void updateView(SearchResult response) {
		if(response != null) {
			searchAdapter.setList(response.getSongList());
		}
	}

	//自定义适配器
	class SearchAdapter extends ArrayListAdapter<Music> {

		public SearchAdapter(Activity context) {
			super(context);
		}

		//创建View方法
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(mContext).inflate(R.layout.search_list_item, null);
				holder = new ViewHolder();
				holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
				holder.tvStyle = (TextView) convertView.findViewById(R.id.tv_style);
				holder.tvArtistAlbum = (TextView) convertView.findViewById(R.id.tv_artist_album);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			Music music = (Music) getItem(position);
			holder.tvName.setText(music.getName());
			holder.tvStyle.setText(music.getFengge());
			holder.tvArtistAlbum.setText((TextUtils.isEmpty(music.getSinger()) ? "" : music.getSinger() + " -- ")
					+ music.getAlbumName());
			return convertView;
		}

		class ViewHolder {
			TextView tvName;
			TextView tvStyle;
			TextView tvArtistAlbum;
		}
	}
}
