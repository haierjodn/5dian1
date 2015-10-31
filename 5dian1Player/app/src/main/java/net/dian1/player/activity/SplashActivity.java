/**
 * 
 */
package net.dian1.player.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.login.LoginActivity;
import net.dian1.player.dialog.TutorialDialog;
import net.dian1.player.model.UserInfo;

/**
 * @author Marcin Gil
 *
 */
public class SplashActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.splashscreen);

		findViewById(R.id.splashlayout).postDelayed(new Runnable() {
			@Override
			public void run() {
				checkToLogin();
			}
		}, 2500);
	}
	
	final void checkToLogin() {
		UserInfo userInfo = Dian1Application.getInstance().getUser();
		if(userInfo != null && !TextUtils.isEmpty(userInfo.getToken())) {
			MainActivity.launch(SplashActivity.this);
		} else {
			LoginActivity.startAction(SplashActivity.this);
		}
		finish();
	}

	public void updateUserInfo() {

	}
}
