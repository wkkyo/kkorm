package com.wkkyo.android.orm;

import java.io.File;

import android.os.Environment;

/**
 * 数据库配置
 * @author Wkkyo
 *
 */
public final class DBConfig {

	/**
	 * 数据库文件完整路径，默认SD卡根目录
	 */
	public static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"db";
}
