package com.zbv.meeting.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.telit.zhkt_three.MyApplication;
import com.telit.zhkt_three.Utils.EncryptUtil;

/**
 * SharedPreference工具类
 */
public class SharedPreferenceUtil {

	private static String SP_NAME = "huimiaomiao_share_date";
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;
	private static SharedPreferenceUtil instance;


	public static synchronized SharedPreferenceUtil getInstance(Context context) {
		if (instance == null) {
			instance = new SharedPreferenceUtil(context);
		}
		return instance;
	}

	/**
	 * 构造函数
	 *
	 * @param context
	 */
	private SharedPreferenceUtil(Context context) {
		init(context);
	}

	/**
	 * 初使化
	 */
	private void init(Context context) {

		if (context != null) {
			sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
			editor = sp.edit();
		}
	}


	/**
	 * 这个是加密
	 * encrypt function
	 *
	 * @return cipherText base64
	 */
	private String encryptPreference(String plainText) {
		return EncryptUtil.getInstance(MyApplication.getInstance()).encrypt(plainText);
	}

	/**
	 * 这个是解密
	 * decrypt function
	 *
	 * @return plainText
	 */
	private String decryptPreference(String cipherText) {
		return EncryptUtil.getInstance(MyApplication.getInstance()).decrypt(cipherText);
	}


	/**
	 * 添加String
	 *
	 * @param key
	 * @param value
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public void setString(String key, String value) {
		editor.putString(key, encryptPreference(value));
		editor.commit();
	}

	/**
	 * 获取String
	 *
	 * @param key
	 * @param defValue 默认值
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public String getString(String key, String defValue) {
		String encryptValue = sp.getString(key, defValue);
		return encryptValue == null ? defValue : decryptPreference(encryptValue);
	}

	/**
	 * 获取String
	 *
	 * @param key
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public String getString(String key) {
		return getString(key, null);
	}

	/**
	 * 添加Int
	 *
	 * @param key
	 * @param value
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public void setInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}


	/**
	 * 获取Int
	 *
	 * @param key
	 * @param defValue 默认值
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public int getInt(String key, int defValue) {
		return sp.getInt(key, defValue);
	}

	/**
	 * 获取Int
	 *
	 * @param key
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public int getInt(String key) {
		return getInt(key, 0);
	}


	/**
	 * 添加float
	 *
	 * @param key
	 * @param value
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public void setFloat(String key, float value) {
		editor.putFloat(key, value);
		editor.commit();
	}

	/**
	 * 获取float
	 *
	 * @param key
	 * @param defValue 默认值
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public float getFloat(String key, float defValue) {
		return sp.getFloat(key, defValue);
	}

	/**
	 * 获取float
	 *
	 * @param key
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public float getFloat(String key) {
		return getFloat(key, 0.0f);
	}

	/**
	 * 添加boolean
	 *
	 * @param key
	 * @param value
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public void setBoolean(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}

	/**
	 * 获取boolean
	 *
	 * @param key
	 * @param defValue
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public boolean getBoolean(String key, boolean defValue) {
		return sp.getBoolean(key, defValue);
	}

	/**
	 * 获取boolean
	 *
	 * @param key
	 * @return
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}


	/**
	 * 删除
	 *
	 * @param key
	 * @Description
	 * @Author zhaoqianpeng(zqp @ yitong.com.cn) 2014-7-18
	 */
	public void delContent(String key) {
		editor.remove(key);
		editor.commit();
	}

//	public final static String SP_NAME = "SP_NAME_1";

//	public static String getInfoFromShared(String key) {
//		SharedPreferences preferences = MyApplication.mApp
//				.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//		return preferences.getString(key, null);
//	}
//
//	public static String getInfoFromShared(String key, String defValue) {
//		SharedPreferences preferences = MyApplication.mApp
//				.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//		return preferences.getString(key, defValue);
//	}
//
//	public static boolean setInfoToShared(String key, String value) {
//		SharedPreferences preferences = MyApplication.mApp
//				.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
//		Editor editor = preferences.edit();
//		editor.putString(key, value);
//		editor.commit();
//		return true;
//	}
}
