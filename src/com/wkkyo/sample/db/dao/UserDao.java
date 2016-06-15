package com.wkkyo.sample.db.dao;

import com.wkkyo.android.orm.dao.DBDao;
import com.wkkyo.sample.db.entity.User;

import android.content.Context;

public class UserDao extends DBDao<User> {

	public UserDao(Context context) {
		super(context);
	}
}
