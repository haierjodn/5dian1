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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.R;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.Album;
import net.dian1.player.model.MusicDayResponse;
import net.dian1.player.model.preference.MusicDay;
import net.dian1.player.preferences.CommonPreference;


import java.util.List;

public class RepoActivity extends BaseActivity implements OnClickListener {


	private RepoAdapter repoAdapter;

	private GridView gvRepoList;

	private List<Album> musicDayList;

	private int mScreenWidth;
	private int mScreenHeight;


	public static void launch(Context c){
		Intent intent = new Intent(c, RepoActivity.class);
		c.startActivity(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_repo);
		Display display = getWindowManager().getDefaultDisplay();
		mScreenHeight= display.getHeight();
		mScreenWidth = display.getWidth();

		initHeader();
		gvRepoList = (GridView)findViewById(R.id.gv_repo_list);
		repoAdapter = new RepoAdapter(this);
		gvRepoList.setAdapter(repoAdapter);
		gvRepoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object item = repoAdapter.getItem(position);
				if(item != null && item instanceof Album) {
					AlbumActivity.launch(RepoActivity.this, ((Album) item).getId());
				}
			}
		});
		refreshData();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initHeader() {
		//((TextView) findViewById(R.id.tv_title)).setText();
		findViewById(R.id.iv_back).setOnClickListener(this);
		findViewById(R.id.iv_search).setOnClickListener(this);
	}

	private void refreshData() {
		MusicDay musicDay = retrieveMusicDay();
		if(musicDay != null) {
			musicDayList = musicDay.musicDay.getMusicDayList();
			notifyDataSetChanged();
			return;
		}
		ApiManager.getInstance().send(new ApiRequest(this, ApiData.MusicDayApi.URL, MusicDayResponse.class,
				ApiData.MusicDayApi.getParams(), new OnResultListener<MusicDayResponse>() {

			@Override
			public void onResult(MusicDayResponse response) {
				//dismissDialog();
				if (response != null) {
					musicDayList = response.getMusicDayList();
					notifyDataSetChanged();
					saveMusicDay(response);
				}
			}

			@Override
			public void onResultError(String msg, String code) {
				//dismissDialog();
				//showToastSafe(msg, Toast.LENGTH_SHORT);
			}
		}));
	}

	private void notifyDataSetChanged() {
		if(musicDayList != null && musicDayList.size() > 0) {
			findViewById(R.id.tv_footer_content).setVisibility(View.VISIBLE);
			findViewById(R.id.tv_footer_content).setOnClickListener(this);
		} else {
			findViewById(R.id.tv_footer_content).setVisibility(View.GONE);
		}
		repoAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_back:
				onBackPressed();
				break;
			case R.id.tv_footer_content:
			case R.id.iv_search:
				SearchActivity.launch(this);
				break;
		}
	}

	public void saveMusicDay(MusicDayResponse httpResponse) {
		if(httpResponse != null) {
			MusicDay musicDay = new MusicDay();
			musicDay.updateTime = System.currentTimeMillis();
			musicDay.musicDay = httpResponse;
			String jsonString = JSONObject.toJSONString(musicDay);
			CommonPreference.save(CommonPreference.MUSIC_DAY, jsonString);
		}
	}

	public MusicDay retrieveMusicDay() {
		String jsonString = CommonPreference.getString(CommonPreference.MUSIC_DAY, null);
		if(!TextUtils.isEmpty(jsonString)) {
			try {
				MusicDay musicDay = JSONObject.parseObject(jsonString, MusicDay.class);
				if(System.currentTimeMillis() - musicDay.updateTime < 1000 * 60 * 60 * 24) {
					return musicDay;
				}
			} catch (Exception e) {
			}
		}
		return null;
	}

	class RepoAdapter extends BaseAdapter{

		private Context context;

		RepoAdapter(Context context){
			this.context = context;
		}

		public int getCount() {
			return musicDayList == null ? 0 : musicDayList.size();
		}

		public Object getItem(int position) {
			return position >= getCount() ? null : musicDayList.get(position);
		}

		public long getItemId(int id) {
			return id;
		}

		//创建View方法
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(R.layout.repo_list_item, null);
				viewHolder.ivAlbum = (ImageView) convertView.findViewById(R.id.iv_album);
				viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
				viewHolder.tvArtist = (TextView) convertView.findViewById(R.id.tv_artist);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewHolder.ivAlbum.getLayoutParams();
				params.height = (int)(mScreenHeight/3.8f);
				params.width = (mScreenWidth-40)/2;
				viewHolder.ivAlbum.setLayoutParams(params);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			Album album = (Album) getItem(position);
			if(album != null) {
				viewHolder.tvTitle.setText(album.getName());
				viewHolder.tvArtist.setText(album.getFormat());
				showImage(viewHolder.ivAlbum, album.getPic());
			}
			return convertView;
		}

		private void showImage(ImageView imageView, String imageUrl) {
			if (!TextUtils.isEmpty(imageUrl)) {
				BitmapUtils bitmapUtils = new BitmapUtils(RepoActivity.this);
				bitmapUtils.configDefaultLoadingImage(R.drawable.player_albumcover_default);// 默认背景图片
				bitmapUtils.configDefaultLoadFailedImage(R.drawable.player_albumcover_default);// 加载失败图片
				bitmapUtils.display(imageView, imageUrl);
			}
		}

		class ViewHolder {
			ImageView ivAlbum;
			TextView tvTitle;
			TextView tvArtist;
		}
	}
}
