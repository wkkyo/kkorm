package com.wkkyo.sample;

import java.util.ArrayList;
import java.util.List;

import com.wkkyo.android.orm.R;
import com.wkkyo.sample.db.dao.UserDao;
import com.wkkyo.sample.db.entity.User;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	
	UserDao dao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//配置数据库参数
//		DBConfig.DB_PATH = Environment.getExternalStorageDirectory()+"/database";
//		DBConfig.DB_NAME = "test.db";
		
		//声明dao
		dao = new UserDao(this);
		
		//新增记录
		User user = new User();
		user.setUsername("克里斯蒂亚诺罗纳尔多");
		user.setPassword("123456");
		dao.save(user);
		
		//新增多条记录
		List<User> users = new ArrayList<>();
		for(int i = 0;i<10;i++){
			User player = new User();
			player.setUsername("克里斯蒂亚诺罗纳尔多_"+i);
			player.setPassword("123456");
			users.add(player);
		}
		dao.save(users);
		
		
		
		addUIListeners();
	}

	private void addUIListeners() {
		findViewById(R.id.addBtn).setOnClickListener(this);
		findViewById(R.id.addsBtn).setOnClickListener(this);
		findViewById(R.id.updateBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.addBtn:
			
			break;
		case R.id.updateBtn:
			//更新记录
			List<User> users = dao.query();
			if(users.size() > 0){
				User user = users.get(0);
				user.setUsername("贝尔");
				dao.save(user);
				
			}
			
			break;
		case R.id.addsBtn:
			
			break;

		default:
			break;
		}
	}

	private void trast(String str){
		
	}
}
