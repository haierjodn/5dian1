package net.dian1.player.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import net.dian1.player.R;
import net.dian1.player.dialog.AboutDialog;


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
		findViewById(R.id.tv_setting_update).setOnClickListener(this);
		findViewById(R.id.tv_setting_about).setOnClickListener(this);
		findViewById(R.id.tv_setting_exit).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.tv_setting_adjust:
				break;
			case R.id.tv_setting_update:
				break;
			case R.id.tv_setting_about:
				new AboutDialog(this).show();
				break;
			case R.id.tv_setting_exit:
				break;
		}
	}
}
