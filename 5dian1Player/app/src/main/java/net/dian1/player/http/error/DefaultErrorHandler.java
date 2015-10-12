package net.dian1.player.http.error;

import android.content.Context;

import net.dian1.player.model.DMSResponse;

/**
 * 默认网络异常处理类
 *
 * Created by Desmond on 2015/8/31.
 */
public class DefaultErrorHandler implements IErrorHandler{

    /**
     * 涉及Activity页面跳转
     */
    private Context mContext;

    public DefaultErrorHandler(Context context) {
        mContext = context;
    }

    @Override
    public boolean onHandle(DMSResponse result) {
        if(result != null) {
            if("1000".equals(result.getCode())) {

            } else if("1001".equals(result.getCode())) {

            }
        }
        return false;
    }

}
