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
	 * 数据库文件完整路径，默认SD卡根目录<br/>
	 * 即：sdcard/db，建议根据实际需要进行修改
	 */
	public static String DB_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator+"db";
	
	/**
	 * 数据库文件名，默认为data.db
	 */
	public static String DB_NAME = "data.db";
	
	 /** 数据库版本号. */
    public static int DB_VERSION = 1;
	
	/**
	 * 是否调试，如果是true，则每次启动都会删除数据库文件。
	 */
	public static boolean IS_DEBUG = false;
	
}
