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
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.ArrayListAdapter;
import net.dian1.player.api.Review;
import net.dian1.player.common.Extra;
import net.dian1.player.dialog.AlbumLoadingDialog;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.Album;
import net.dian1.player.model.Music;
import net.dian1.player.util.DialogUtils;
import net.dian1.player.util.ImageUtils;
import net.dian1.player.util.MockUtils;


public class AlbumActivity extends BaseActivity {

    private long albumId;
    private Album mAlbum;
    private ListView musicListView;
    private MustListAdapter mustListAdapter;

    public static void launch(Activity context, long albumId) {
        Intent intent = new Intent(context, AlbumActivity.class);
        intent.putExtra(Extra.ALBUM_ID, albumId);
        context.startActivity(intent);
    }

    /**
     * Launch this Activity from the outside
     *
     * @param c     Activity from which AlbumActivity should be started
     * @param album Album to be presented
     */
    public static void launch(Activity c, Album album) {
        new AlbumLoadingDialog(c, R.string.album_loading, R.string.album_fail).execute(album);
    }

    public static void launch(
            IntentDistributorActivity c, Album album,
            int reviewId) {
        new AlbumLoadingDialog(c, R.string.album_loading, R.string.album_fail).execute(album, reviewId);
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_album);
        albumId = getIntent().getLongExtra(Extra.ALBUM_ID, -1);
        mAlbum = (Album) getIntent().getSerializableExtra("album");
        initView();
        refreshData();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.album, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            menu.findItem(R.id.download_menu_item).setVisible(true);
        } else {
            menu.findItem(R.id.download_menu_item).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_menu_item:
                downloadAlbum();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        setupHeader(-1);
        musicListView = (ListView) findViewById(R.id.lv_music);

        mustListAdapter = new MustListAdapter(this);
        musicListView.setAdapter(mustListAdapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dian1Application.getInstance().getUserAuthority();
                if(position > 1 && !Dian1Application.getInstance().getUserAuthority().searchAlbumAll) {
                    DialogUtils.showNoAuthorityAndJumpPage(AlbumActivity.this);
                } else {
                    PlayerActivity.launch(AlbumActivity.this,
                            mAlbum.buildPlaylist(position)/*MockUtils.buildSamplePlaylist()*/);
                }
            }
        });

        int selectedReviewId = getIntent().getIntExtra("selectedReviewId", -1);
        if (selectedReviewId != -1) {
            selectReview(selectedReviewId);
        }
    }

    private void refreshData() {
        ApiManager.getInstance().send(new ApiRequest(this, ApiData.AbumDetailApi.URL, Album.class,
                ApiData.AbumDetailApi.getParams(albumId), new OnResultListener<Album>() {

            @Override
            public void onResult(Album response) {
                //dismissDialog();
                updateView(response);
            }

            @Override
            public void onResultError(String msg, String code) {
                //dismissDialog();
                //showToastSafe(msg, Toast.LENGTH_SHORT);
            }
        }));
    }

    private void updateView(Album album) {
        if (album != null) {
            mAlbum = album;
            ImageView ivAlbumCover = (ImageView) findViewById(R.id.iv_album);
            TextView tvTitle = (TextView) findViewById(R.id.tv_title);
            TextView tvDesc = (TextView) findViewById(R.id.tv_desc);
            ImageUtils.showImage(ivAlbumCover, album.getPic());
            tvTitle.setText(album.getName());
            String desc = album.getDesc();
            if(!TextUtils.isEmpty(desc)) {
                tvDesc.setText(Html.fromHtml(album.getDesc()));
                tvDesc.setMovementMethod(ScrollingMovementMethod.getInstance());
            }
            mustListAdapter.setList(album.getSongList());
        }
    }


    /**
     * Add whole album to the download queue
     */
    private void downloadAlbum() {

        AlertDialog alertDialog = new AlertDialog.Builder(AlbumActivity.this)
                .setTitle(R.string.download_album_q)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

//				DownloadManager downloadManager = Dian1Application.getInstance().getDownloadManager();
//				for(Music track : mAlbum.getMusics()) {
//					PlaylistEntry entry = new PlaylistEntry();
//					entry.setAlbum(mAlbum);
//					entry.setMusic(track);
//					downloadManager.download(entry);
//				}

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        alertDialog.show();
    }


    private void selectReview(int selectedReviewId) {
        for (int i = 0; i < mustListAdapter.getCount(); i++) {
            if (((Review) mustListAdapter.getItem(i)).getId() == selectedReviewId) {
                musicListView.setSelection(i);
                return;
            }
        }
    }


    /**
     * Adapter representing reviews
     *
     * @author Lukasz Wisniewski
     */
    public class MustListAdapter extends ArrayListAdapter<Music> {

        public MustListAdapter(Activity context) {
            super(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                row = LayoutInflater.from(mContext).inflate(R.layout.album_music_item, null);
                holder = new ViewHolder();
                holder.tvArtist = (TextView) row.findViewById(R.id.tv_artist);
                holder.tvTitle = (TextView) row.findViewById(R.id.tv_title);
                holder.tvPosition = (TextView) row.findViewById(R.id.tv_number);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            Music music = (Music) getItem(position);
            holder.tvTitle.setText(music.getName());
            holder.tvArtist.setText(TextUtils.isEmpty(music.getSinger()) ? "--" : music.getSinger());
            holder.tvPosition.setText(String.valueOf(position + 1));
            return row;
        }

        class ViewHolder {
            TextView tvPosition;
            TextView tvTitle;
            TextView tvArtist;
        }

    }

}
