package net.dian1.player.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.R;
import net.dian1.player.activity.BaseActivity;
import net.dian1.player.activity.MainActivity;
import net.dian1.player.db.DatabaseImpl;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.log.LogUtil;
import net.dian1.player.model.LoginParam;
import net.dian1.player.model.LoginResponse;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private EditText etUserName;

    private EditText etPassword;

    private TextView tvLogin;

    private ImageView ivUserName;
    private ImageView ivPassword;


    /**
     * 忘记密码
     */
    private LinearLayout ll_forget_pwd;

    /**
     * 是否显示密码
     */
    private ImageView ivShowPwd;

    private boolean isShowPwd = true;

    /**
     * @param ctx
     */
    public static final void startAction(Context ctx) {
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
    }

    protected void initView() {
        etUserName = (EditText) findViewById(R.id.et_username);
        etPassword = (EditText) findViewById(R.id.et_password);
        tvLogin = (TextView) findViewById(R.id.tv_login);

        etUserName.setText("zccbillion@qq.com");
        etPassword.setText("zimeng55");
        updateLoginState();

        etUserName.addTextChangedListener(new StateTextWatcher());
        etPassword.addTextChangedListener(new StateTextWatcher());
        ivShowPwd = (ImageView) findViewById(R.id.iv_show_pwd);

        ivUserName = (ImageView) findViewById(R.id.iv_username);
        ivPassword = (ImageView) findViewById(R.id.iv_password);

        initListener();
    }

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

    /**
     * 更新登录按钮状态
     */
    private void updateLoginState() {
        if (etUserName.getText().toString().trim().length() == 0 || etPassword.getText().toString().trim().length() == 0) {
            tvLogin.setBackgroundColor(getResources().getColor(R.color.green_transparent_50));
            tvLogin.setClickable(false);
        } else {
            tvLogin.setBackgroundColor(getResources().getColor(R.color.green));
            tvLogin.setClickable(true);
        }
    }

    protected void initListener() {
        ivShowPwd.setOnClickListener(this);
        tvLogin.setOnClickListener(this);
        etUserName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ivUserName.setImageDrawable(getResources().getDrawable(R.drawable.ic_loginname_selected));
                } else {
                    ivUserName.setImageDrawable(getResources().getDrawable(R.drawable.ic_loginname));
                }
            }
        });
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ivPassword.setImageDrawable(getResources().getDrawable(R.drawable.ic_password_selected));
                } else {
                    ivPassword.setImageDrawable(getResources().getDrawable(R.drawable.ic_password));
                }
            }
        });
        findViewById(R.id.ll_forget_pwd).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_login:
                String acc = etUserName.getText().toString().trim();
                final String pwd = etPassword.getText().toString().trim();

                // // TODO:登录 cid
                ApiManager.getInstance().send(new ApiRequest(this, ApiData.LoginApi.URL, LoginResponse.class,
                        ApiData.LoginApi.setParams(new LoginParam(acc, pwd, "1")), new OnResultListener<LoginResponse>() {

                    @Override
                    public void onResult(final LoginResponse response) {
                        //dismissDialog();
                        if (response != null) {
                            app.setUser(response.user);
                            MainActivity.launch(LoginActivity.this);
                            new DatabaseImpl(LoginActivity.this).addOrUpdateUserInfo(response.user);
                        }
                        finish();
                    }

                    @Override
                    public void onResultError(String msg, String code) {
                        //dismissDialog();
                        showToastSafe(msg, Toast.LENGTH_SHORT);
                    }
                }));

                if (app.getUser() != null) {
                    LogUtil.i("start login, token:" + app.getUser().getToken());
                } else {
                    LogUtil.i("user token null");
                }
                break;
            case R.id.iv_show_pwd:
                // 是否显示密码
                if (isShowPwd) {
                    etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.icon_pwd_noshow);
                } else {
                    etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    ivShowPwd.setImageResource(R.drawable.icon_pwd_show);
                }
                isShowPwd = !isShowPwd;

                break;
            case R.id.ll_forget_pwd:
                PwdForgetActivity.startAction(this);
                break;
        }
    }

    // 更新用户信息
    public static void saveOrUpdate(/*UserInfo user*/) {
//		List<UserInfo> users = UserInfo.where(UserInfo.Columns.LOGINID + " = ?", user.getLoginId()).find(UserInfo.class);
//		if (users != null && users.size() > 0) {
//			// 更新
//			users.get(0).setEmpno(user.getEmpno());
//			users.get(0).setPortrait(user.getPortrait());
//			users.get(0).setNickname(user.getNickname());
//			users.get(0).setPhone(user.getPhone());
//			users.get(0).setRealName(user.getRealName());
//			users.get(0).setToken(user.getToken());
//			users.get(0).updateAll(UserInfo.Columns.LOGINID + " = ?", user.getLoginId());
//		} else {
//			user.save();
//		}
    }
}
