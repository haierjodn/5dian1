package net.dian1.player.model;


/**
 * Created by Six.SamA on 2015/9/6.
 * Email:MephistoLake@gmail.com
 */
public class UserInfo {

	// 登录Id
	private int loginId;

	// 用户认证凭据TOKEN
	private String token;

	// 用户头像
	private String portrait;

	// 电话号码
	private String phone;

	// 昵称
	private String nickname;

	// 真实姓名
	private String realName;

	//email
	private String email;

	private int level;

	private String levelName;

	//1:金砖会员
	private int isappvip;

	// 工号
	private String expiredTime;


	public int getLoginId() {
		return loginId;
	}

	public void setLoginId(int loginId) {
		this.loginId = loginId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getLevelName() {
		return levelName;
	}

	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	public int getIsappvip() {
		return isappvip;
	}

	public void setIsappvip(int isappvip) {
		this.isappvip = isappvip;
	}

	public String getExpiredTime() {
		return expiredTime;
	}

	public void setExpiredTime(String expiredTime) {
		this.expiredTime = expiredTime;
	}
}
