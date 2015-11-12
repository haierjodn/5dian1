package net.dian1.player.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.R;
import net.dian1.player.activity.BaseActivity;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.DMSResponse;
import net.dian1.player.model.login.SecurityParam;
import net.dian1.player.model.login.ValidCodeParam;


/**
 * Created by Six.SamA on 2015/8/17.
 * Email:MephistoLake@gmail.com
 */
public class PwdForgetActivity extends BaseActivity implements View.OnClickListener{

	/**
	 * 倒计时
	 */
	public static final int FORGET_TIME_PROCESS_MESSAGE = 0x0002;

	/**
	 * 倒计时结束
	 */
	public static final int FORGET_TIME_FINISH_MESSAGE = 0x0001;

	private EditText et_phone;

	private EditText et_code;

	private EditText et_pwd;

	private ImageView iv_show_pwd;

	private TextView tv_confirm;

	private TextView tv_gain_code;

	private boolean isShowPwd = true;

	private int time = 60;//sec

	private Context ctx = this;

	public static void startAction(Context ctx) {
		Intent intent = new Intent(ctx, PwdForgetActivity.class);
		ctx.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwd_forget);
		initView();
	}

	protected void initView() {
		setupHeader(R.string.forget_password);
		findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);

		et_phone = (EditText) findViewById(R.id.et_phone);
		et_code = (EditText) findViewById(R.id.et_code);
		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_phone.addTextChangedListener(new StateTextWatcher());
		et_code.addTextChangedListener(new StateTextWatcher());
		et_pwd.addTextChangedListener(new StateTextWatcher());

		iv_show_pwd = (ImageView) findViewById(R.id.iv_show_pwd);
		tv_confirm = (TextView) findViewById(R.id.tv_confirm);
		tv_gain_code = (TextView) findViewById(R.id.tv_gain_code);

		iv_show_pwd.setOnClickListener(this);
		tv_confirm.setOnClickListener(this);
		tv_gain_code.setOnClickListener(this);

		tv_confirm.setClickable(false);
	}

	@Override
	public void onClick(View v) {
		String phone = et_phone.getText().toString().trim();
		switch (v.getId()) {
		case R.id.iv_show_pwd:
			if (isShowPwd) {
				et_pwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				iv_show_pwd.setImageResource(R.drawable.icon_pwd_noshow);
			} else {
				et_pwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				iv_show_pwd.setImageResource(R.drawable.icon_pwd_show);
			}
			isShowPwd = !isShowPwd;
			break;
		case R.id.tv_confirm:
			String pwd = et_pwd.getText().toString().trim();
			String code = et_code.getText().toString().trim();
			//健壮性 但是貌似没什么卵用
			if (TextUtils.isEmpty(phone)) {
				//forget_phone_empty
				showToastSafe(R.string.forget_input_error, Toast.LENGTH_SHORT);
				return;
			}
			if (TextUtils.isEmpty(pwd)) {
				//login_pwd_empty
				showToastSafe(R.string.forget_input_error, Toast.LENGTH_SHORT);
				return;
			}

			if (TextUtils.isEmpty(code)) {
				//forget_code_empty
				showToastSafe(R.string.forget_input_error, Toast.LENGTH_SHORT);
				return;
			}
			showDialog(null, false);
			final String password = et_pwd.getText().toString();
			ApiManager.getInstance().send(new ApiRequest(ctx, ApiData.SecurityApi.URL, DMSResponse.class,
					ApiData.SecurityApi.getParams(SecurityParam.getPwdForgetParam(code, phone, password)),
					new OnResultListener<DMSResponse>() {
				@Override
				public void onResult(DMSResponse response) {
					dismissDialog();
					Toast.makeText(ctx, R.string.forget_reset_success, Toast.LENGTH_SHORT).show();
					finish();
				}
				@Override
				public void onResultError(String msg, String code) {
					Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
					dismissDialog();
				}
			}));
			break;
		case R.id.tv_gain_code:

			if (TextUtils.isEmpty(phone)) {
				showToastSafe(R.string.forget_phone_empty, Toast.LENGTH_SHORT);
				return;
			}

			if (phone.length() < 11) {
				showToastSafe(R.string.forget_phone_error, Toast.LENGTH_SHORT);
				return;
			}
			showDialog(getString(R.string.forget_gain_code), true);
			ApiManager.getInstance().send(new ApiRequest(ctx, ApiData.ValidCodeApi.URL, ApiData.ValidCodeApi.getParams(
					new ValidCodeParam("getcode_findpwd", "", phone)), new OnResultListener() {

				@Override
				public void onResult(Object response) {
					dismissDialog();
					Toast.makeText(ctx, R.string.forget_sms_code, Toast.LENGTH_SHORT).show();
					// 验证码倒计时
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (time != 0) {
								mHandler.sendEmptyMessage(FORGET_TIME_PROCESS_MESSAGE);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								time--;
							}
							time = 10;
							mHandler.sendEmptyMessage(FORGET_TIME_FINISH_MESSAGE);

						}
					}).start();
				}

				@Override
				public void onResultError(String msg, String code) {
					dismissDialog();
					showToastSafe(msg, Toast.LENGTH_SHORT);
				}
			}));
			break;
		}
	}

	/**
	 * EDITTEXT监听器
	 */
	class StateTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			updateLoginState();
		}

		@Override
		public void afterTextChanged(Editable s) {

		}
	}

	@Override
	protected void onHandleMessage(Message msg) {
		switch (msg.what) {
		case FORGET_TIME_FINISH_MESSAGE:
			tv_gain_code.setClickable(true);
			tv_gain_code.setText(R.string.forget_gain_code);
			tv_gain_code.setBackgroundColor(getResources().getColor(R.color.green));
			break;
		case FORGET_TIME_PROCESS_MESSAGE:
			tv_gain_code.setClickable(false);
			tv_gain_code.setText(time + getString(R.string.forget_regain_code));
			tv_gain_code.setBackgroundColor(getResources().getColor(R.color.green_transparent_50));
			break;
		}
	}

	/**
	 * 更新登录按钮状态
	 */
	private void updateLoginState() {
		if (et_phone.getText().toString().trim().length() == 0 || et_code.getText().toString().trim().length() == 0 || et_pwd.getText().toString().trim().length() == 0) {
			tv_confirm.setBackgroundColor(getResources().getColor(R.color.green_transparent_50));
			tv_confirm.setClickable(false);
		} else {
			tv_confirm.setBackgroundColor(getResources().getColor(R.color.green));
			tv_confirm.setClickable(true);
		}
	}
}
