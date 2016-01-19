package net.dian1.player.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import net.dian1.player.R;

import java.lang.ref.WeakReference;

public class BrowserActivity extends ActionBarActivity implements OnClickListener {

    protected Toolbar toolbar;

    private WebView mWebView;

    private String url;

    private String title;

    private WebviewHandler handler;

    static class WebviewHandler extends Handler {

        WeakReference<BrowserActivity> wrBrowserActivity;

        WebviewHandler(BrowserActivity browserActivity) {
            wrBrowserActivity = new WeakReference<BrowserActivity>(browserActivity);
        }

        public void handleMessage(Message msg) {
            BrowserActivity browserActivity = wrBrowserActivity.get();
            if (browserActivity != null && !Thread.currentThread().isInterrupted()) {
                TextView tvProgress = (TextView) browserActivity.findViewById(
                        R.id.tv_progress);
                tvProgress.setText(msg.what + "%");
                int visibility = (msg.what == 100 ? View.INVISIBLE : View.VISIBLE);
                tvProgress.setVisibility(visibility);
                browserActivity.findViewById(R.id.progress_bar).setVisibility(visibility);
            }
            super.handleMessage(msg);
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_browser);
        mWebView = (WebView) findViewById(R.id.web_view);
        init();
    }

    void init() {
        handler = new WebviewHandler(this);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        initToolbar(true);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        initWebView();
        loadUrl(url);
    }

    protected void initToolbar(boolean hasBack) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        if (hasBack) {
            final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    public class GeoWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // When user clicks a hyperlink, load in the existing WebView
//            if (url.startsWith("kkou")) {
//                Uri uri = Uri.parse(url);
//                String title = uri.getQueryParameter("title");
//                String target = uri.getQueryParameter("target");
//                Log.e("onPageStarted", "title = " + title + " target = " + target);
//                PageForwardUtils.forward(BrowserActivity.this, target, title);
//                return true;
//            }
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * WebChromeClient subclass handles UI-related calls
     * Note: think chrome as in decoration, not the Chrome browser
     */
    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,
                                                       GeolocationPermissions.Callback callback) {
            // Always grant permission since the app itself requires location
            // permission and the user has therefore already granted it
            callback.invoke(origin, true, false);
        }

        @Override
        public void onProgressChanged(WebView view, int progress) {
            handler.sendEmptyMessage(progress);
            super.onProgressChanged(view, progress);
        }
    }

    public void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setUseWideViewPort(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationDatabasePath(this.getFilesDir().getPath());
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDomStorageEnabled(true);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int mDensity = metrics.densityDpi;
        if (mDensity == 240) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        } else if (mDensity == 160) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        } else if(mDensity == 120) {
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.CLOSE);
        }else if(mDensity == DisplayMetrics.DENSITY_XHIGH){
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else if (mDensity == DisplayMetrics.DENSITY_TV){
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        }else{
            webSettings.setDefaultZoom(WebSettings.ZoomDensity.MEDIUM);
        }
        /**
         * 用WebView显示图片，可使用这个参数 设置网页布局类型： 1、LayoutAlgorithm.NARROW_COLUMNS ：
         * 适应内容大小 2、LayoutAlgorithm.SINGLE_COLUMN:适应屏幕，内容将自动缩放
         */
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);

        mWebView.setWebViewClient(new GeoWebViewClient());
        mWebView.setWebChromeClient(new GeoWebChromeClient());
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        findViewById(R.id.web_left).setOnClickListener(this);
        findViewById(R.id.web_right).setOnClickListener(this);
        findViewById(R.id.web_refresh).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void loadUrl(final String url) {
        handler.post(new Thread() {
            public void run() {
                handler.sendEmptyMessage(0);
                mWebView.loadUrl(url);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.web_left:
                mWebView.goBack();
                break;
            case R.id.web_right:
                mWebView.goForward();
                break;
            case R.id.web_refresh:
                mWebView.reload();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

}
