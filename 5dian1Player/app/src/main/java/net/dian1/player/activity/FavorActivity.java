package net.dian1.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.dian1.player.R;
import net.dian1.player.adapter.AudioAdapter;
import net.dian1.player.api.Music;
import net.dian1.player.api.Playlist;
import net.dian1.player.api.PlaylistEntry;
import net.dian1.player.db.DatabaseImpl;
import net.dian1.player.media.local.AudioLoaderManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Desmond on 2015/9/23.
 */
public class FavorActivity extends BaseActivity {

    private RecyclerView recyclerView;

    private AudioAdapter audioAdapter;

    private DatabaseImpl database;

    public static void launch(Context c) {
        Intent intent = new Intent(c, FavorActivity.class);
        c.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favor);

        database = new DatabaseImpl(this);

        initUI();

        fillData();
    }

    private void initUI() {
        setupHeader(R.string.my_favor);
        findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        audioAdapter = new AudioAdapter(this);
        recyclerView.setAdapter(audioAdapter);
    }

    private void fillData() {
        Playlist playlist = database.getFavorites();
        audioAdapter.setPlaylist(playlist.getAllPlaylistEntry());
    }

    @Override
    public void onResume() {
        super.onResume();
        audioAdapter.notifyDataSetChanged();
    }
}