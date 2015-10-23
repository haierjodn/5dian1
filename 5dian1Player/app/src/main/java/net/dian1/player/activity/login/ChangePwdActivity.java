package net.dian1.player.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.BaseActivity;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.DMSResponse;
import net.dian1.player.model.login.PwdUpdateParam;


/**
 * Created by Six.SamA on 2015/8/26.
 * Email:MephistoLake@gmail.com
 */
public class ChangePwdActivity extends BaseActivity implements View.OnClickListener{

	private EditText et_old;

	private EditText et_new;

	private EditText et_renew;

	private TextView tv_confirm;

	public static void startAction(Context ctx) {
		Intent intent = new Intent(ctx, ChangePwdActivity.class);
		ctx.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwd_change);
		initView();
	}

	protected void initView() {
		tv_confirm = (TextView) findViewById(R.id.tv_confirm);
		tv_confirm.setOnClickListener(this);
		et_old = (EditText) findViewById(R.id.et_old_password);
		et_new = (EditText) findViewById(R.id.et_new_password1);
		et_renew = (EditText) findViewById(R.id.et_new_password2);

		et_old.addTextChangedListener(new StateTextWatcher());
		et_new.addTextChangedListener(new StateTextWatcher());
		et_renew.addTextChangedListener(new StateTextWatcher());

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_confirm:
			String oldPwd = et_old.getText().toString().trim();
			String newPwd = et_new.getText().toString().trim();
			String renewPwd = et_renew.getText().toString().trim();
			if (newPwd.length() < 6 || renewPwd.length() < 6) {
				et_new.setError(getString(R.string.change_pwd_length));
				return;
			}
			if (newPwd.length() > 12 || renewPwd.length() > 12) {
				et_renew.setError(getString(R.string.change_pwd_length));
				return;
			}

			if (!newPwd.equals(renewPwd)) {
				et_new.setError(getString(R.string.change_inconformity));
				return;
			}
			//String md5Old = ComUtils.MD5(oldPwd);
			//String md5New = ComUtils.MD5(newPwd);
			String oldPassword = et_old.getText().toString();
			String newPassword = et_new.getText().toString();
			//showDialog();
			// ÐÞ¸ÄÃÜÂë
			ApiManager.getInstance().send(new ApiRequest(this, ApiData.PwdUpdateApi.URL, DMSResponse.class,
					ApiData.PwdUpdateApi.setParams(new PwdUpdateParam(oldPassword, newPassword)), new OnResultListener<DMSResponse>() {

				@Override
				public void onResult(DMSResponse response) {
					//dismissDialog();
					Toast.makeText(ChangePwdActivity.this, R.string.change_success, Toast.LENGTH_SHORT).show();
					Dian1Application.getInstance().exitApp();
					LoginActivity.startAction(ChangePwdActivity.this);
				}

				@Override
				public void onResultError(String msg, String code) {
					//dismissDialog();
					Toast.makeText(ChangePwdActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
			}));
			break;
		}
	}

	class StateTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			et_old.setError("");
			et_new.setError("");
			et_renew.setError("");
			updateLoginState();
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}

	/**
	 * ¸üÐÂµÇÂ¼°´Å¥×´Ì¬
	 */
	private void updateLoginState() {
		if (et_old.getText().toString().trim().length() == 0 || et_new.getText().toString().trim().length() == 0 || et_renew.getText().toString().trim().length() == 0) {
			tv_confirm.setBackgroundColor(getResources().getColor(R.color.green_transparent_50));
			tv_confirm.setClickable(false);
		} else {
			tv_confirm.setBackgroundColor(getResources().getColor(R.color.green));
			tv_confirm.setClickable(true);
		}
	}
}
