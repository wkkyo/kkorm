package com.wkkyo.android.util;

import android.util.Log;

/**
 * 日志工具
 * @author Wkkyo
 * @date 2016-4-13
 * @version 1.0.0
 * 
 */
public final class KKLog {
	
	private static final String DEBUG_TAG = "KKAPI";
	
	public static int LEVEL = 1;

	public static void d(String msg){
		if(LEVEL == 1){
			Log.d(DEBUG_TAG, msg);
		}
	}
}

