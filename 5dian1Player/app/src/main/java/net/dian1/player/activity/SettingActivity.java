package net.dian1.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.login.LoginActivity;
import net.dian1.player.dialog.AboutDialog;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.common.VersionLatest;
import net.dian1.player.util.DialogUtils;


/**
 *
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

	public static void startAction(Context ctx) {
		Intent intent = new Intent(ctx, SettingActivity.class);
		ctx.startActivity(intent);
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
	}

	private void initView() {
		findViewById(R.id.tv_setting_adjust).setOnClickListener(this);
		findViewById(R.id.tv_setting_adjust).setVisibility(View.GONE);
		findViewById(R.id.tv_setting_update).setOnClickListener(this);
		findViewById(R.id.tv_setting_about).setOnClickListener(this);
		findViewById(R.id.tv_setting_exit).setOnClickListener(this);
		setupHeader(R.string.settings);
		findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.tv_setting_adjust:
				break;
			case R.id.tv_setting_update:
				checkVersionUpdate();
				break;
			case R.id.tv_setting_about:
				new AboutDialog(this).show();
				break;
			case R.id.tv_setting_exit:
				logoff();
				break;
		}
	}

	private void checkVersionUpdate() {
		// 升级
		ApiManager.getInstance().send(new ApiRequest(this, ApiData.VersionLatestApi.URL, VersionLatest.class, new OnResultListener<VersionLatest>() {

			@Override
			public void onResult(final VersionLatest response) {
				if (response != null && response.hasUpdate && response.versionInfo != null) {
					DialogUtils.showAppUpgradeDialog(SettingActivity.this, response);
				}
			}

			@Override
			public void onResultError(String msg, String code) {
				Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		}));
	}

	private void logoff() {
		finish();
		Dian1Application.getInstance().logoff();
		LoginActivity.startAction(this);
	}
}
