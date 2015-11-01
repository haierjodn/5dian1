package net.dian1.player.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.model.common.VersionLatest;

/**
 * Created by Desmond on 2015/9/25.
 */
public class DialogUtils {

    /**
     * APP升级弹出框
     *
     * @param ctx
     * @param response
     */
    public static void showAppUpgradeDialog(final Context ctx, final VersionLatest response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle(R.string.upgrade);
        builder.setMessage(response.versionInfo.explain);
        final boolean forceUpdate = response.versionInfo.forcedUpdate;
        builder.setPositiveButton(R.string.upgrade, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (forceUpdate) {
                    Dian1Application.getInstance().exitApp();
                }
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(response.versionInfo.downloadPath));
                ctx.startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.common_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (forceUpdate) {
                    Dian1Application.getInstance().exitApp();
                }
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     *
     * @param ctx
     * @param content
     */
    public static void showCommonDialog(final Context ctx, final String title, final String content,
                                        final View.OnClickListener cancelListener,
                                        final View.OnClickListener confirmListener) {
        final Dialog dialog = new Dialog(ctx);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.common_alert_dialog);
        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        if(!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        TextView tvContent = (TextView) dialog.findViewById(R.id.tv_content);
        tvContent.setText(content);
        TextView tvConfirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (confirmListener != null) {
                    confirmListener.onClick(v);
                }
            }
        });
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tv_cancel);
        if(cancelListener != null) {
            dialog.findViewById(R.id.v_line).setVisibility(View.VISIBLE);
            tvCancel.setVisibility(View.VISIBLE);
            tvCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (cancelListener != null) {
                        cancelListener.onClick(v);
                    }
                }
            });
        }
        dialog.show();
    }
}