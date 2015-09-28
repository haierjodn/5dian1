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
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.AlbumAdapter;
import net.dian1.player.adapter.PlaylistRemoteAdapter;
import net.dian1.player.api.Album;
import net.dian1.player.api.JamendoGet2Api;
import net.dian1.player.api.PlaylistRemote;
import net.dian1.player.api.WSError;
import net.dian1.player.api.impl.JamendoGet2ApiImpl;
import net.dian1.player.dialog.LoadingDialog;

import org.json.JSONException;

import java.util.ArrayList;

public class RepoActivity extends Activity implements OnClickListener {


	private GridView gvRepoList;


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

		initHeader();
		gvRepoList = (GridView)findViewById(R.id.gv_repo_list);
		gvRepoList.setAdapter(new MyAdapter(this));
		gvRepoList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				AlbumActivity.launch(RepoActivity.this);
			}
		});
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_back:
				onBackPressed();
				break;
			case R.id.iv_search:
				SearchActivity.launch(this);
				break;
		}
	}

	//自定义适配器
	class MyAdapter extends BaseAdapter{
		//上下文对象
		private Context context;

		MyAdapter(Context context){
			this.context = context;
		}
		public int getCount() {
			return 7;
		}

		public Object getItem(int item) {
			return item;
		}

		public long getItemId(int id) {
			return id;
		}

		//创建View方法
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.repo_list_item, null);
			} else {
			}
			return convertView;
		}
	}
}
