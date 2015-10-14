package net.dian1.player.util;

import android.text.TextUtils;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;

import net.dian1.player.R;

/**
 * Created by Desmond on 2015/10/14.
 */
public class ImageUtils {
    public static void showImage(ImageView imageView, String imageUrl) {
        if (!TextUtils.isEmpty(imageUrl)) {
            BitmapUtils bitmapUtils = new BitmapUtils(imageView.getContext());
            bitmapUtils.configDefaultLoadingImage(R.drawable.icon_splash);// 默认背景图片
            bitmapUtils.configDefaultLoadFailedImage(R.drawable.icon_splash);// 加载失败图片
            bitmapUtils.display(imageView, imageUrl);
        }
    }
}
