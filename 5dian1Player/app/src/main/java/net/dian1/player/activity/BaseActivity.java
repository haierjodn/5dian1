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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.ArrayListAdapter;
import net.dian1.player.api.Album;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.api.Review;
import net.dian1.player.dialog.AlbumLoadingDialog;
import net.dian1.player.download.DownloadManager;
import net.dian1.player.widget.RemoteImageView;

import java.util.ArrayList;

public class BaseActivity extends Activity{

	protected Dian1Application app;

	private BitmapUtils bitmapUtils;

	/** UI 线程ID */
	protected long mUIThreadId;

	protected Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			onHandleMessage(msg);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		mUIThreadId = android.os.Process.myTid();
		app = (Dian1Application) getApplication();

	}

	/** 由子类实现如何处理事件 */
	protected void onHandleMessage(Message msg) {

	}


	protected void setupHeader(int titleRes) {
		setupHeader(titleRes > 0 ? getString(titleRes) : null);
	}
	protected void setupHeader(String title) {
		TextView tvTitle = (TextView) findViewById(R.id.tv_title);
		if(!TextUtils.isEmpty(title)) {
			tvTitle.setText(title);
		}
		View btnBack = findViewById(R.id.iv_back);
		if(btnBack != null) {
			btnBack.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
	}

	/** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
	public void showToastSafe(final int resId, final int duration) {
		if (android.os.Process.myTid() == mUIThreadId) {
			// 调用在UI线程
			Toast.makeText(getBaseContext(), resId, duration).show();
		} else {
			// 调用在非UI线程
			post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), resId, duration).show();
				}
			});
		}
	}

	/** 对toast的简易封装。线程安全，可以在非UI线程调用。 */
	public void showToastSafe(final CharSequence text, final int duration) {
		if (android.os.Process.myTid() == mUIThreadId) {
			// 调用在UI线程
			Toast.makeText(getBaseContext(), text, duration).show();
		} else {
			// 调用在非UI线程
			post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(getBaseContext(), text, duration).show();
				}
			});
		}
	}

	public boolean post(Runnable run) {
		return mHandler.post(run);
	}

	public void showImage(ImageView imageView, String imagePath) {
		if(bitmapUtils == null) {
			bitmapUtils = new BitmapUtils(this);
			bitmapUtils.configDefaultLoadingImage(R.drawable.player_albumcover_default);// 默认背景图片
			bitmapUtils.configDefaultLoadFailedImage(R.drawable.player_albumcover_default);// 加载失败图片
		}
		if(!TextUtils.isEmpty(imagePath)) {
			bitmapUtils.display(imageView, imagePath);
		}
	}

}
