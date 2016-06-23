package com.wkkyo.sample;

import java.util.ArrayList;
import java.util.List;

import com.wkkyo.android.orm.DBConfig;
import com.wkkyo.android.orm.R;
import com.wkkyo.sample.db.dao.UserDao;
import com.wkkyo.sample.db.entity.User;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	UserDao dao;
	
	ListView listView;
	
	TextView editText;
	
	List<User> users;

	BaseAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//配置数据库参数
//		DBConfig.DB_PATH = Environment.getExternalStorageDirectory()+"/database";
//		DBConfig.DB_NAME = "test.db";
		
		//声明dao
		dao = new UserDao(this);
		
		addUIListeners();
	}

	private void addUIListeners() {
		findViewById(R.id.addBtn).setOnClickListener(this);
		findViewById(R.id.addsBtn).setOnClickListener(this);
		findViewById(R.id.updateBtn).setOnClickListener(this);
		listView = (ListView) findViewById(R.id.listView);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				User user = users.get(position);
				dao.delete(user.getId());
				users.remove(user);
				adapter.notifyDataSetChanged();
				showToast("删除成功");
				return false;
			}
		});
		users = dao.query();
		adapter = new MyAdapter(this, users);
		listView.setAdapter(adapter);
		editText = (TextView) findViewById(R.id.editText);
	}

	@Override
	public void onClick(View v) {
		String userName = editText.getText().toString();
		if(userName.equals("")){
			showToast("请先输入内容");
			return;
		}
		switch (v.getId()) {
		case R.id.addBtn:
			//新增记录
			User user = new User();
			user.setUsername(userName);
			user.setPassword("123456");
			dao.save(user);
			users.add(user);
			break;
		case R.id.updateBtn:
			//更新记录
			if(users.size() > 0){
				User user2 = users.get(0);
				user2.setUsername("这是更新的记录");
				dao.save(user2);
				showToast("更新第一条记录");
			}		
			break;
		case R.id.addsBtn:
			//新增多条记录
			List<User> addUsers = new ArrayList<>();
			for(int i = 0;i<10;i++){
				User player = new User();
				player.setUsername(userName+"_"+i);
				player.setPassword("123456");
				addUsers.add(player);
			}
			dao.save(addUsers);
			users.addAll(addUsers);
			showToast("新增10条记录");
			break;

		default:
			break;
		}
		
		adapter.notifyDataSetChanged();
	}

	private final void showToast(String str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}
	
	class MyAdapter extends BaseAdapter{
		
		Context mContext;
		List<User> mUsers;
		
		
		public MyAdapter(Context context,List<User> users) {
			this.mContext = context;
			this.mUsers = users;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, null);
			User user = mUsers.get(position);
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			textView.setText(user.getUsername());
			return convertView;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mUsers.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mUsers.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}
	}
}
