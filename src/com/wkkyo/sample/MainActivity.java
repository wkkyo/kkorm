package com.wkkyo.sample;

import com.wkkyo.android.orm.DBConfig;
import com.wkkyo.android.orm.R;
import com.wkkyo.android.orm.R.id;
import com.wkkyo.android.orm.R.layout;
import com.wkkyo.android.orm.R.menu;
import com.wkkyo.sample.db.dao.UserDao;
import com.wkkyo.sample.db.entity.User;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		UserDao dao = new UserDao(this);
		
		//新增
		User user = new User();
		user.setUsername("克里斯蒂亚诺罗纳尔多");
		user.setPassword("123456");
		dao.save(user);
		
		//更新
		User user2 = dao.get(user.getId());
		user2.setUsername("贝尔");
		dao.save(user2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
