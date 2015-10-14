package net.dian1.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import net.dian1.player.R;
import net.dian1.player.common.Extra;
import net.dian1.player.media.local.AudioLoaderManager;

/**
 * Created by Desmond on 2015/9/23.
 */
public class LocalBrowserActivity extends BaseFragmentActivity implements ActionBar.TabListener {

    private LocalBrowserFragment mFragment1 = new LocalBrowserFragment();
    //private LocalBrowserFragment mFragment2 = new LocalBrowserFragment();
    //private LocalBrowserFragment mFragment3 = new LocalBrowserFragment();

    private static final int TAB_INDEX_COUNT = 1;

    private static final int TAB_INDEX_ONE = 0;
    private static final int TAB_INDEX_TWO = 1;
    private static final int TAB_INDEX_THREE = 2;

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    public static void launch(Context c) {
        Intent intent = new Intent(c, LocalBrowserActivity.class);
        c.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_browser);

        setUpActionBar();
        setUpViewPager();
        setUpTabs();
        AudioLoaderManager.getInstance().init(getApplicationContext());
        AudioLoaderManager.getInstance().loadData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("本地音乐");
    }

    private void setUpViewPager() {
        mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                final ActionBar actionBar = getSupportActionBar();
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        //TODO
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        //TODO
                        break;
                    default:
                        //TODO
                        break;
                }
            }
        });
    }

    private void setUpTabs() {
//        final ActionBar actionBar = getSupportActionBar();
//        for (int i = 0; i < mViewPagerAdapter.getCount(); ++i) {
//            actionBar.addTab(actionBar.newTab()
//                    .setText(mViewPagerAdapter.getPageTitle(i))
//                    .setTabListener(this));
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            // TODO Auto-generated constructor stub
        }

        @Override
        public Fragment getItem(int position) {
            // TODO Auto-generated method stub
            switch (position) {
                case TAB_INDEX_ONE:
                    return mFragment1;
//                case TAB_INDEX_TWO:
//                    return mFragment2;
//                case TAB_INDEX_THREE:
//                    return mFragment3;
            }
            throw new IllegalStateException("No fragment at position " + position);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return TAB_INDEX_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String tabLabel = null;
            switch (position) {
                case TAB_INDEX_ONE:
                    tabLabel = "T 1";
                    break;
                case TAB_INDEX_TWO:
                    tabLabel = "T 2";
                    break;
                case TAB_INDEX_THREE:
                    tabLabel = "T 3";
                    break;
            }
            return tabLabel;
        }
    }

    @Override
    public void onTabReselected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabSelected(android.support.v7.app.ActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        mViewPager.setCurrentItem(tab.getPosition());
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // TODO Auto-generated method stub
        mViewPager.setCurrentItem(tab.getPosition());
    }

}
