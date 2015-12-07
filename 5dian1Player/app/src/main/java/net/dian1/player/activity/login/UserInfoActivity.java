package net.dian1.player.activity.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.dian1.player.Dian1Application;
import net.dian1.player.R;
import net.dian1.player.activity.BaseActivity;
import net.dian1.player.activity.MainActivity;
import net.dian1.player.http.ApiData;
import net.dian1.player.http.ApiManager;
import net.dian1.player.http.ApiRequest;
import net.dian1.player.http.OnResultListener;
import net.dian1.player.model.DMSResponse;
import net.dian1.player.model.UserInfo;
import net.dian1.player.model.login.SecurityParam;
import net.dian1.player.model.login.ValidCodeParam;
import net.dian1.player.util.DialogUtils;
import net.dian1.player.util.Helper;
import net.dian1.player.util.ImageUploadUtils;

import org.w3c.dom.Text;

public class UserInfoActivity extends BaseActivity implements View.OnClickListener{


	private Context mContext = this;

	private ImageView ivPortrait;

	private UploadFaceTask uploadFaceTask;

	public static void startAction(Context ctx) {
		Intent intent = new Intent(ctx, UserInfoActivity.class);
		ctx.startActivity(intent);
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
			setInfoItem(R.id.ll_email, R.string.phone, null);
			setInfoItem(R.id.ll_level, R.string.level, userInfo.getLevelName());
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_portrait:
				DialogUtils.showImageChooserDialog(this);
				break;
		}
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
						if(uploadFaceTask == null) {
							uploadFaceTask = new UploadFaceTask();
						}
						uploadFaceTask.execute(bitmap);
						//ImageUploadUtils.uploadFile(this, bitmap);
						//ImageUploadUtils.onCropResult(data);
						//showResizeImage(data);
					}
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}


	class UploadFaceTask extends AsyncTask<Bitmap, Void, Boolean> {

		@Override
		protected void onPreExecute() {
			showDialog("头像上传中");
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Bitmap... params) {
			return ImageUploadUtils.uploadFile(UserInfoActivity.this, params[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			dismissDialog();
			showToastSafe(result ? "头像上传成功" : "头像上传失败", Toast.LENGTH_SHORT);
			super.onPostExecute(result);
		}
	}

}
