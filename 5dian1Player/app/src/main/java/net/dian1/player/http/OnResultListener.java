package net.dian1.player.http;

/**
 * Created by Six.SamA on 2015/8/20.
 * Email:MephistoLake@gmail.com
 */
public interface OnResultListener<T> {

	public void onResult(T response);

	public void onResultError(String msg, String code);
}
