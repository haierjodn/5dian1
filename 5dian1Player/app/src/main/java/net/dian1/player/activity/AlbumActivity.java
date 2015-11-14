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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.adapter.ArrayListAdapter;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.common.Extra;
import net.dian1.player.dialog.AlbumLoadingDialog;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.Album;
import net.dian1.player.model.Music;
import net.dian1.player.model.authority.Authority;
import net.dian1.player.util.AudioUtils;
import net.dian1.player.util.DialogUtils;
import net.dian1.player.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;


public class AlbumActivity extends BaseActivity implements View.OnClickListener {

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
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);
        musicListView = (ListView) findViewById(R.id.lv_music);

        mustListAdapter = new MustListAdapter(this);
        musicListView.setAdapter(mustListAdapter);
        musicListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position > 1 && !Dian1Application.getInstance().getUserAuthority().searchAlbumAll) {
                    DialogUtils.showNoAuthority12AndJumpPage(AlbumActivity.this);
                } else {
                    PlayerActivity.launch(AlbumActivity.this,
                            mAlbum.buildPlaylist(position)/*MockUtils.buildSamplePlaylist()*/);
                }
            }
        });
        findViewById(R.id.tv_play).setOnClickListener(this);
        findViewById(R.id.tv_download).setOnClickListener(this);
        findViewById(R.id.tv_select).setOnClickListener(this);

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

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create();

        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_play: {
                List<Music> list = mustListAdapter.getSelectedList();
                if (list == null || list.size() < 1) {
                    showToastSafe(getString(R.string.error_tips_no_select), Toast.LENGTH_SHORT);
                } else {
                    PlayerActivity.launch(AlbumActivity.this, AudioUtils.buildPlaylist(mAlbum, list, 0));
                }
                break;
            }
            case R.id.tv_select: {
                mustListAdapter.selectAll();
                final Authority authority = Dian1Application.getInstance().getUserAuthority();
                if (authority != null && !authority.searchAlbumAll) {
                    showToastSafe(R.string.exceed_tried_times_album_ordinary_user_toast, Toast.LENGTH_SHORT);
                }
                break;
            }
            case R.id.tv_download: {
                final Authority authority = Dian1Application.getInstance().getUserAuthority();
                if(authority != null && authority.downloadAuth) {
                    List<Music> list = mustListAdapter.getSelectedList();
                    if (list == null || list.size() < 1) {
                        showToastSafe(getString(R.string.error_tips_no_select), Toast.LENGTH_SHORT);
                    } else {
                        for(final Music music : list) {
                            PlaylistEntry entry = new PlaylistEntry();
                            net.dian1.player.api.Album album = new net.dian1.player.api.Album();
                            album.setId((int) mAlbum.getId());
                            //album.setArtistName(get);
                            album.setName(mAlbum.getName());
                            album.setRating(0.5d);
                            album.setImage(mAlbum.getPic());
                            entry.setAlbum(album);
                            net.dian1.player.api.Music music1 = AudioUtils.convertMusic(music);
                            entry.setMusic(music1);
                            Dian1Application.getInstance().getDownloadManager().download(entry);
                        }
                        DownloadActivity.launch(AlbumActivity.this);
                    }
                } else {
                    showToastSafe(R.string.vip_download_limited, Toast.LENGTH_SHORT);
                }
                break;
            }
        }
    }

    /**
     * Adapter representing reviews
     *
     * @author Lukasz Wisniewski
     */
    public class MustListAdapter extends ArrayListAdapter<Music> {

        int maxSelectable = Dian1Application.getInstance().getUserAuthority().searchAlbumAll ? Integer.MAX_VALUE : 2;

        boolean[] selected = null;

        public MustListAdapter(Activity context) {
            super(context);
        }

        public void selectAll() {
            if(selected != null && selected.length > 0) {
                boolean flag = selected[0];
                for(int i=0; i < selected.length; i++) {
                    selected[i] = (i < maxSelectable ? !flag : false);
                }
                notifyDataSetChanged();
            }
        }

        public List<Music> getSelectedList() {
            List<Music> selectedList = new ArrayList<>();
            for(int i=0; i < mList.size(); i++) {
                if(selected[i]) {
                    selectedList.add(mList.get(i));
                }
            }
            return selectedList;
        }

        public void setList(List<Music> list){
            if(list != null && list.size() > 0) {
                selected = new boolean[list.size()];
            }
            super.setList(list);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder holder;
            if (row == null) {
                row = LayoutInflater.from(mContext).inflate(R.layout.album_music_item, null);
                holder = new ViewHolder();
                holder.tvArtist = (TextView) row.findViewById(R.id.tv_artist);
                holder.tvTitle = (TextView) row.findViewById(R.id.tv_title);
                holder.tvPosition = (TextView) row.findViewById(R.id.tv_number);
                holder.cbSelect = (CheckBox) row.findViewById(R.id.cb_select);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            Music music = (Music) getItem(position);
            holder.tvTitle.setText(music.getName());
            holder.tvArtist.setText(TextUtils.isEmpty(music.getSinger()) ? "--" : music.getSinger());
            holder.tvPosition.setText(String.valueOf(position + 1));
            holder.cbSelect.setChecked(selected == null ? false : selected[position]);
            holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    selected[position] = isChecked;
                }
            });
            holder.cbSelect.setEnabled(position < maxSelectable);
            return row;
        }

        class ViewHolder {
            TextView tvPosition;
            TextView tvTitle;
            TextView tvArtist;
            CheckBox cbSelect;
        }

    }

}
