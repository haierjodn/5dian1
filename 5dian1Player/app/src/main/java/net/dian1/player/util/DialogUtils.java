package net.dian1.player.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.common.Constants;
import net.dian1.player.model.common.VersionLatest;
import net.dian1.player.preferences.CommonPreference;

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

    public static void showCommonDialog(final Context ctx, final String title, final String content,
                                        final View.OnClickListener cancelListener,
                                        final View.OnClickListener confirmListener) {
        showCommonDialog(ctx, title, content, null, null, cancelListener, confirmListener);

    }

    /**
     *
     * @param ctx
     * @param content
     */
    public static void showCommonDialog(final Context ctx, final String title, final String content,
                                        final String btnConfirmText,final String btnCancelText,
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
        if(!TextUtils.isEmpty(btnConfirmText)) {
            tvConfirm.setText(btnConfirmText);
        }
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
        if(!TextUtils.isEmpty(btnCancelText)) {
            tvCancel.setText(btnCancelText);
        }
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

    public static void showNoAuthorityAndJumpPage(final Context context) {
        showCommonDialog(context, context.getString(R.string.user_alert),
                context.getString(R.string.exceed_tried_times_ordinary_user),
                context.getString(R.string.btn_goto_vip), null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommonPreference.saveCountDay(0);
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CommonPreference.saveCountDay(0);
                        ComUtils.openBrowser(context, Constants.URL_VIP_INRO, null);
                    }
                });
    }

    public static void showNoAuthority12AndJumpPage(final Context context) {
        showCommonDialog(context, context.getString(R.string.user_alert),
                context.getString(R.string.exceed_tried_times_album_ordinary_user),
                context.getString(R.string.btn_goto_vip), null,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                },
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ComUtils.openBrowser(context, Constants.URL_VIP_INRO, null);
                    }
                });
    }

    public static void showImageChooserDialog(final Activity activity) {
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.picture_chooser_dialog);

        TextView tvExplorer = (TextView) dialog.findViewById(R.id.tv_choose_explorer);
        tvExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ImageUploadUtils.chooseImageInCategory(activity);
            }
        });
        TextView tvShot = (TextView) dialog.findViewById(R.id.tv_choose_shot);
        tvShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    ImageUploadUtils.takeCapture(activity);
                } else {
                    Toast.makeText(activity, activity.getString(R.string.common_external_storage_unmount), Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }


    public static void showUserInfoEditDialog(final Activity activity, String label,
                                              final OnEditAction onEditAction) {
        final Dialog dialog = new Dialog(activity);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_userinfo_edit);

        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        tvTitle.setText(label);

        final EditText etContent = (EditText) dialog.findViewById(R.id.et_content);

        TextView tvConfirm = (TextView) dialog.findViewById(R.id.tv_confirm);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (onEditAction != null) {
                    onEditAction.onEditComplete(etContent.getText().toString());
                }
            }
        });
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public interface OnEditAction {
        void onEditComplete(String content);
    }
}
