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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.R;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.Album;
import net.dian1.player.model.SearchResult;
import net.dian1.player.util.AudioUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends BaseActivity implements OnClickListener {

	private View btnSearch;

	private EditText editText;

	private GridView gvRepoList;

	private List<Album> musicDayList;

	private SearchAdapter searchAdapter;

	private int mScreenWidth;
	private int mScreenHeight;


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
		Display display = getWindowManager().getDefaultDisplay();
		mScreenHeight= display.getHeight();
		mScreenWidth = display.getWidth();

		musicDayList = new ArrayList<>();

		initView();

		refreshData();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void initView() {
		initHeader();
		btnSearch = findViewById(R.id.tv_search);
		editText = (EditText) findViewById(R.id.et_search);
		searchAdapter = new SearchAdapter(this);
		gvRepoList = (GridView)findViewById(R.id.gv_search);
		gvRepoList.setAdapter(searchAdapter);
		gvRepoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Object item = searchAdapter.getItem(position);
				if(item != null && item instanceof Album) {
					AlbumActivity.launch(SearchActivity.this, ((Album) item).getId());
				}
			}
		});

		btnSearch.setOnClickListener(this);
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
			case R.id.tv_search:
				refreshData();
				break;
		}
	}

	private void refreshData() {
		String keyWords = editText.getText().toString();
		if(TextUtils.isEmpty(keyWords)) {
			return;
		}
		ApiManager.getInstance().send(new ApiRequest(this, ApiData.MusicSearchApi.URL, SearchResult.class,
				ApiData.MusicSearchApi.getParams(keyWords), new OnResultListener<SearchResult>() {

			@Override
			public void onResult(SearchResult response) {
				updateView(response);
			}

			@Override
			public void onResultError(String msg, String code) {
				showToastSafe(getString(R.string.search_failed) + "(" + msg + ")", Toast.LENGTH_SHORT);
			}
		}));
		hideSoftInputFromWindow();
	}

	private void hideSoftInputFromWindow() {
		InputMethodManager imm = (InputMethodManager)getSystemService(
				Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
	}

	private void updateView(SearchResult response) {
		musicDayList.clear();
		if(response != null) {
			musicDayList.addAll(response.getAlbumList());
		}
		searchAdapter.notifyDataSetChanged();
	}

	class SearchAdapter extends BaseAdapter{

		private Context context;

		SearchAdapter(Context context){
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
				BitmapUtils bitmapUtils = new BitmapUtils(SearchActivity.this);
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
