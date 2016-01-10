package net.dian1.player.activity.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.BaseActivity;
import net.dian1.player.activity.MainActivity;
import net.dian1.player.db.DatabaseImpl;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.log.LogUtil;
import net.dian1.player.model.DMSResponse;
import net.dian1.player.model.LoginResponse;
import net.dian1.player.model.UploadImageParam;
import net.dian1.player.model.UserInfo;
import net.dian1.player.model.UserinfoEditParam;
import net.dian1.player.model.login.SecurityParam;
import net.dian1.player.model.login.ValidCodeParam;
import net.dian1.player.util.DialogUtils;
import net.dian1.player.util.Helper;
import net.dian1.player.util.ImageUploadUtils;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener{


	private Context mContext = this;

	private ImageView ivPortrait;

	private boolean uploadSuccess = false;

	//private UploadFaceTask uploadFaceTask;

	public static void startAction(Activity ctx) {
		Intent intent = new Intent(ctx, UserInfoActivity.class);
		ctx.startActivityForResult(intent, 0);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo);
		initView();
	}

	protected void initView() {
		setupHeader(R.string.member_info);
		findViewById(R.id.iv_search).setVisibility(View.INVISIBLE);
		updateUserInfo();
	}

	private void updateUserInfo() {
		UserInfo userInfo = Dian1Application.getInstance().getUser();
		if(userInfo != null) {
			ivPortrait = (ImageView) findViewById(R.id.iv_portrait);
			ImageView ivGold = (ImageView) findViewById(R.id.iv_gold);
			TextView tvUserName = (TextView) findViewById(R.id.tv_nickname);
			TextView tvLevel = (TextView) findViewById(R.id.tv_level);
			tvUserName.setText(getString(R.string.nickname_prefix, userInfo.getNickname()));
			findViewById(R.id.iv_edit_nickname).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//修改昵称
					clickToEditItem(EDIT_NICKNAME);
				}
			});
			if(userInfo.getIsappvip() == 1) {
				tvLevel.setText(getString(R.string.user_gold_expired ,
						Helper.getTimeLeftFromNow(userInfo.getExpiredTime())));
				ivGold.setImageResource(R.drawable.icon_level_gold);
			} else {
				tvLevel.setText(R.string.user_ordinary);
				ivGold.setImageResource(R.drawable.icon_level_siliver);
			}
			showImage(ivPortrait, userInfo.getPortrait());
			ivGold.setOnClickListener(this);
			ivPortrait.setOnClickListener(this);

			setInfoItem(R.id.ll_phone, R.string.phone, userInfo.getPhone());
			findViewById(R.id.ll_phone).findViewById(R.id.iv_edit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//修改手机号码调整
					clickToEditItem(EDIT_PHONE);
				}
			});

			setInfoItem(R.id.ll_email, R.string.email, userInfo.getEmail());
			findViewById(R.id.ll_email).findViewById(R.id.iv_edit).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//修改email
					clickToEditItem(EDIT_EMAIL);
				}
			});

			setInfoItem(R.id.ll_level, R.string.level, userInfo.getLevelName());
			findViewById(R.id.ll_level).findViewById(R.id.iv_edit).setVisibility(View.GONE);
		}
	}

	private void setInfoItem(int layoutId, int labelResId, String value) {
		View container = findViewById(layoutId);
		if(container != null) {
			TextView tvLabel = (TextView) container.findViewById(R.id.tv_label);
			tvLabel.setText(labelResId);
			TextView tvValue = (TextView) container.findViewById(R.id.tv_value);
			tvValue.setText(TextUtils.isEmpty(value) ? "--" : value);
		}
	}

	private final static int EDIT_NICKNAME = 1;
	private final static int EDIT_PHONE = 2;
	private final static int EDIT_EMAIL = 3;
	private void clickToEditItem(int type) {
		switch (type) {
			case EDIT_NICKNAME:
				DialogUtils.showUserInfoEditDialog(UserInfoActivity.this, "修改昵称", new DialogUtils.OnEditAction() {
					@Override
					public void onEditComplete(String content) {
						uploadUserInfo(content, null);
					}
				});
				break;
			case EDIT_PHONE:
				BindActivity.actionToChangePhone(this);
				break;
			case EDIT_EMAIL:
				DialogUtils.showUserInfoEditDialog(UserInfoActivity.this, "修改Email", new DialogUtils.OnEditAction() {
					@Override
					public void onEditComplete(String content) {
						uploadUserInfo(null, content);
					}
				});
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_portrait:
				DialogUtils.showImageChooserDialog(this);
				break;
		}
	}

	@Override
	public void onBackPressed() {
		setResult(uploadSuccess ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
		super.onBackPressed();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case ImageUploadUtils.IMAGE_REQUEST_CODE:
					ImageUploadUtils.startCrop(this, data.getData());
					//resizeImage(data.getData());
					break;
				case ImageUploadUtils.CAMERA_REQUEST_CODE:
					ImageUploadUtils.startCrop(this, ImageUploadUtils.getCaptureImageFileUri(this));
					break;
				case ImageUploadUtils.RESIZE_REQUEST_CODE:
					if (data != null) {
						Bitmap bitmap = ImageUploadUtils.getBitmap(data);
						if(bitmap != null) {
							ivPortrait.setImageBitmap(bitmap);
						}
						/*if(uploadFaceTask == null) {
							uploadFaceTask = new UploadFaceTask();
						}
						uploadFaceTask.execute(bitmap);*/
						uploadFaceTask(bitmap);
					}
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uploadFaceTask(Bitmap bitmap) {
		if(bitmap != null) {
			String imageStr = Bitmap2StrByBase64(bitmap);
			final UserInfo userInfo = Dian1Application.getInstance().getUser();
			/**
			 * RequestParams paramters = ApiData.FaceUploadApi.getParams(userInfo.getLoginId(), imageStr);
			 showDialog(getString(R.string.userinfo_portrait_uploading));
			 LogUtil.i("UploadImageParam", "pa:"+paramters.toString());
			 ApiManager.getInstance().send(new ApiRequest(this, ApiData.FaceUploadApi.URL, LoginResponse.class,
			 paramters, new OnResultListener<LoginResponse>() {
			 */
			UploadImageParam uploadImageParam = new UploadImageParam(userInfo.getLoginId(), imageStr);
			showDialog(getString(R.string.userinfo_portrait_uploading));
			LogUtil.i("UploadImageParam", ApiData.FaceUploadApi.setParams(uploadImageParam).toString());
			ApiManager.getInstance().send(new ApiRequest(this, ApiData.FaceUploadApi.URL, LoginResponse.class,
					ApiData.FaceUploadApi.setParams(uploadImageParam), new OnResultListener<LoginResponse>() {

				@Override
				public void onResult(final LoginResponse response) {
					dismissDialog();
					if (response != null) {
						uploadSuccess = true;
						app.setUser(response.user);
						new DatabaseImpl(UserInfoActivity.this).addOrUpdateUserInfo(response.user);
						showToastSafe(getString(R.string.userinfo_portrait_upload_success), Toast.LENGTH_SHORT);
					}
				}

				@Override
				public void onResultError(String msg, String code) {
					dismissDialog();
					uploadSuccess = false;
					showToastSafe(msg, Toast.LENGTH_SHORT);
				}
			}));
		}
	}

	/**
	 * 通过Base32将Bitmap转换成Base64字符串
	 * @return
	 */
	public String Bitmap2StrByBase64(Bitmap bitmap){
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);//参数100表示不压缩
		byte[] bytes=bos.toByteArray();
		return Base64.encodeToString(bytes, Base64.DEFAULT);
	}

	/**
	 * 会员资料修改上传
	 */
	public void uploadUserInfo(String newNickName, String newEmail) {
		final UserInfo userInfo = Dian1Application.getInstance().getUser();
		if(userInfo == null) {
			return;
		}
		UserinfoEditParam userinfoEditParam = new UserinfoEditParam();
		userinfoEditParam.uid = String.valueOf(userInfo.getLoginId());
		userinfoEditParam.token = userInfo.getToken();
		if(!TextUtils.isEmpty(newNickName)) {
			userinfoEditParam.user = newNickName;
		}
		if(!TextUtils.isEmpty(newEmail)) {
			userinfoEditParam.email = newEmail;
		}
		showDialog(getString(R.string.userinfo_upload_loading));
		LogUtil.i("UserinfoEditParam", ApiData.UserInfoApi.setParams(userinfoEditParam).toString());
		ApiManager.getInstance().send(new ApiRequest(this, ApiData.UserInfoApi.URL, LoginResponse.class,
				ApiData.UserInfoApi.setParams(userinfoEditParam), new OnResultListener<LoginResponse>() {

			@Override
			public void onResult(final LoginResponse response) {
				dismissDialog();
				if (response != null) {
					uploadSuccess = true;
					app.setUser(response.user);
					new DatabaseImpl(UserInfoActivity.this).addOrUpdateUserInfo(response.user);
					showToastSafe(getString(R.string.userinfo_upload_success), Toast.LENGTH_SHORT);
				}
			}

			@Override
			public void onResultError(String msg, String code) {
				dismissDialog();
				uploadSuccess = false;
				showToastSafe(msg, Toast.LENGTH_SHORT);
			}
		}));
	}

}
