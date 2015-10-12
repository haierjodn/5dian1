package net.dian1.player.http.error;

import net.dian1.player.model.DMSResponse;

/**
 * HTTP网络请求异常处理，用于统一处理Cookie失效等
 *
 * Created by Desmond on 2015/8/31.
 */
public interface IErrorHandler {

    /**
     * 根据API REQUEST解析结果，做统一处理
     * 对于后续仍需处理回调的，不能返回true
     *
     * @param result 错误等信息
     * @return 标识是否截取，即后续还是否需要回调该请求
     */
    boolean onHandle(DMSResponse result);

}
