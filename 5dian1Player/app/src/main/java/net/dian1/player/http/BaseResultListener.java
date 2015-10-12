package net.dian1.player.http;

import com.lidroid.xutils.http.ResponseInfo;

/**
 * Created by Desmond on 2015/8/31.
 */
public abstract class BaseResultListener implements OnResultListener {

    protected ResponseInfo<String> mResponseInfo;

    BaseResultListener(ResponseInfo<String> responseInfo) {
        mResponseInfo = responseInfo;
    }

}
