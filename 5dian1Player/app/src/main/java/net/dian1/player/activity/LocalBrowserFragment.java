package net.dian1.player.activity;

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
import net.dian1.player.media.local.AudioLoaderManager;

/**
 * Created by Desmond on 2015/9/23.
 */
public class LocalBrowserFragment extends Fragment {

    private View rootView;

    private RecyclerView recyclerView;

    private AudioAdapter audioAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflateAndSetupView(inflater, container, savedInstanceState, R.layout.fragment_local_browser);
    }

    private View inflateAndSetupView(LayoutInflater inflater, ViewGroup container,
                                     Bundle savedInstanceState, int layoutResourceId) {
        rootView = inflater.inflate(layoutResourceId, container, false);
        initUI();
        return rootView;
    }

    private View findViewById(int resId) {
        return rootView.findViewById(resId);
    }

    private void initUI() {
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        audioAdapter = new AudioAdapter(getActivity());
        audioAdapter.setData(AudioLoaderManager.getInstance().getViewSongs());
        recyclerView.setAdapter(audioAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        audioAdapter.notifyDataSetChanged();
    }
}