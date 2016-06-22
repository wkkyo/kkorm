package com.wkkyo.android.orm.dao;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import com.wkkyo.android.orm.DBConfig;
import com.wkkyo.android.orm.DBOpenHelper;
import com.wkkyo.android.orm.TableHelper;
import com.wkkyo.android.orm.annotation.BusinessId;
import com.wkkyo.android.orm.annotation.Column;
import com.wkkyo.android.orm.annotation.Id;
import com.wkkyo.android.orm.annotation.ManyToOne;
import com.wkkyo.android.orm.annotation.OneToMany;
import com.wkkyo.android.orm.annotation.Table;
import com.wkkyo.android.util.KKLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * 数据库交互Dao
 * @author wkkyo
 * 
 */
public abstract class DBDao<T> implements IDBDao<T> {

	private DBOpenHelper dbHelper;

	private Class<T> clazz;

	private String idColumn;

	private String tableName;
	
	private List<Field> columnFields;
	
    private static final ReentrantLock lock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	public DBDao(Context context) {
		ParameterizedType pt = (ParameterizedType) this.getClass().getGenericSuperclass();
		this.clazz = (Class<T>)pt.getActualTypeArguments()[0];
		this.dbHelper = DBOpenHelper.getInstance(context.getApplicationContext(),DBConfig.DB_PATH,DBConfig.DB_NAME);
		if(!clazz.isAnnotationPresent(Table.class)){
			KKLog.d("Model 对象未注记Table");
			return;
		}
		this.tableName = clazz.getAnnotation(Table.class).name();
		if(this.tableName.equals("")){
			this.tableName = clazz.getSimpleName().toUpperCase();
		}
		this.columnFields = TableHelper.getColumnFields(clazz);
		for(Field field:columnFields){
			if(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(BusinessId.class)){
				Column column = field.getAnnotation(Column.class);
				String columnName = column.name();
				if(columnName.equals("")){
					columnName = field.getName();
				}
				this.idColumn = columnName;
				break;
			}
		}
		SQLiteDatabase db = startReadableDatabase();
		if(!TableHelper.checkTableExist(db, clazz)){
			db = startWritableDatabase();
			TableHelper.createTable(db, clazz);
			closeDatabase(true);
		}
		closeDatabase(false);
	}
	
	public DBOpenHelper getDbHelper() {
		return dbHelper;
	}

//	private static byte[] bmpToByteArray(Bitmap bmp) {
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		try {
//			bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//			bos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return bos.toByteArray();
//	}

	@Override
	public T save(T t) {
		try {
			lock.lock();
			SQLiteDatabase db = startWritableDatabase();
			mSave(t,db);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			closeDatabase(true);
			lock.unlock();
		}
		return t;
	}
	
	public List<T> save(List<T> list){
		try{
			lock.lock();
			SQLiteDatabase db = startWritableDatabase();
			for(T t:list){
				mSave(t,db);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}finally{
			closeDatabase(true);
			lock.unlock();
		}
		return list;
	}
	
	@Override
	public T get(Serializable id) {
		List<T> list = query(idColumn + " = " + id);
		if (list.size() == 1) {
			return list.get(0);
		}
		return null;
	}
	
	@Override
	public List<T> query() {
		return query(null);
	}

	/* (non-Javadoc)
	 * @see com.wkkyo.android.orm.dao.IDBDao#query(java.lang.String)
	 */
	@Override
	public List<T> query(String where) {
		List<T> list = new ArrayList<T>();
		try {
			lock.lock();
			SQLiteDatabase db = startReadableDatabase();
			StringBuilder sql = new StringBuilder();
			sql.append("select * from ");
			sql.append(tableName);
			if (where != null) {
				sql.append(" where ");
				sql.append(where);
			}
			Cursor cursor = db.rawQuery(sql.toString(),null);
			while (cursor.moveToNext()) {
				T t = renderBean(cursor,db);
				list.add(t);
			}
			cursor.close();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			closeDatabase(false);
			lock.unlock();
		}
		return list;
	}

	@Override
	public List<T> queryOrder(String orderBy) {
		List<T> list = new ArrayList<T>();
		try {
			lock.lock();
			SQLiteDatabase db = startReadableDatabase();
			StringBuilder sql = new StringBuilder();
			sql.append("select * from ");
			sql.append(tableName);
			if (orderBy != null) {
				sql.append(" ");
				sql.append(orderBy);
			}
			Cursor cursor = db.rawQuery(sql.toString(),null);
			while (cursor.moveToNext()) {
				T t = renderBean(cursor,db);
				list.add(t);
			}
			cursor.close();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} finally {
			closeDatabase(false);
			lock.unlock();
		}
		return list;
	}
	
	@Override
	public List<T> query(String selection, String[] selectionArgs) {
		return query(selection, selectionArgs, null, null, null);
	}

	@Override
	public List<T> query(String selection, String[] selectionArgs,
			String groupBy, String having, String orderBy) {
		List<T> list = new ArrayList<T>();
		try {
			lock.lock();
			SQLiteDatabase db = startReadableDatabase();
			Cursor cursor = db.query(tableName, null, selection, selectionArgs,
					groupBy, having, orderBy);
			while (cursor.moveToNext()) {
				T t = renderBean(cursor,db);
				list.add(t);
			}
			cursor.close();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			closeDatabase(false);
			lock.unlock();
		}
		return list;
	}
	
	@Override
	public long delete(Serializable id) {
		long row = 0;
		try {
			lock.lock();
			SQLiteDatabase db = startWritableDatabase();
			String where = idColumn + " = ?";
			String[] args = new String[] { String.valueOf(id) };
			row = db.delete(tableName, where, args);
		}finally{
			closeDatabase(true);
			lock.unlock();
		}
		return row;
	}
	
	@Override
	public long deleteAll() {
		long row = 0;
		try {
			lock.lock();
			SQLiteDatabase db = startWritableDatabase();
			row = db.delete(this.tableName,null,null);
		}finally{
			closeDatabase(true);
			lock.unlock();
		}
		return row;
	}
	
	@Override
	public long getCount(String where){
		long count = 0;
		try {
			lock.lock();
			SQLiteDatabase db = startReadableDatabase();
			count = getCount(where, db);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} finally {
			closeDatabase(false);
			lock.unlock();
		}
		return count;
	}
	
	@Override
	public long getCount(){
		return getCount(null);
	}
	
	/**
	 * 主键生成器，默认采用“前缀+随机UUID+后缀”，子类根据实际需求覆盖
	 * @param prefix 前缀
	 * @param suffix 后缀
	 * @return
	 */
	public String generatorBusinessId(String prefix,String suffix){
		String businessIdValue = UUID.randomUUID().toString().replace("-", "");
		return prefix+businessIdValue+suffix;
	}
	
	private long getCount(String where,SQLiteDatabase db){
		StringBuilder sql = new StringBuilder();
		sql.append("select count(*) from ");
		sql.append(tableName);
		if (where != null) {
			sql.append(" where ");
			sql.append(where);
		}
		long count = 0;
		Cursor cursor = db.rawQuery(sql.toString(),null);
		while (cursor.moveToNext()) {
			count = cursor.getLong(0);
		}
		cursor.close();
		return count;
	}
	
	private void mSave(T entity,SQLiteDatabase db) throws IllegalArgumentException, IllegalAccessException{
		long row = 0;
		Field idField = null;
		List<Field> fields = TableHelper.getColumnFields(clazz);
		for(Field field:fields){
			if(field.isAnnotationPresent(Id.class)){
				idField = field;
				break;
			}
		}
		
		Field businessField = null;
		for(Field field:fields){
			if(field.isAnnotationPresent(BusinessId.class)){
				businessField = field;
				break;
			}
		}
		if(idField == null && businessField == null){
			KKLog.d("Model 对象未注解主键");
			return;
		}
		if(businessField == null){
			idField.setAccessible(true);
			Object idValue = idField.get(entity);
			ContentValues contentValues = setContentValues(entity);
			boolean isUpdate = true;
			if (idValue == null || "".equals(idValue)) {
				isUpdate = false;
			}else if(Integer.TYPE == idField.getType() || idField.getType() == Integer.class) {
				int id = Integer.parseInt(idValue.toString());
				if(id == 0){
					isUpdate = false;
				}
			}else if(Long.TYPE == idField.getType() || idField.getType() == Long.class){
				long id = Long.parseLong(idValue.toString());
				if(id == 0){
					isUpdate = false;
				}
			}else if(Short.TYPE == idField.getType() || idField.getType() == Short.class){
				long id = Short.parseShort(idValue.toString());
				if(id == 0){
					isUpdate = false;
				}
			}
			if(isUpdate){
				Column column = idField.getAnnotation(Column.class);
				String columnName = column.name();
				if(columnName.equals("")){
					columnName = idField.getName();
				}
				String where = columnName + " = ?";
				String[] args = new String[] { String.valueOf(idValue) };
				row = db.update(tableName, contentValues, where, args);
			}else{
				row = db.insert(tableName, null, contentValues);
				if(row > -1){
					if (Integer.TYPE == idField.getType() || idField.getType() == Integer.class) {
						idField.set(entity, (int)row);
					}else if (Long.TYPE == idField.getType() || idField.getType() == Long.class) {
						idField.set(entity, (long)row);
					}else if (Short.TYPE == idField.getType() || idField.getType() == Short.class) {
						idField.set(entity, (short)row);
					}else{
						idField.set(entity, row);
					}
				}
			}
		}else{
			businessField.setAccessible(true);
			Object businessIdValue = businessField.get(entity);
			BusinessId businessId = businessField.getAnnotation(BusinessId.class);
			if(businessIdValue == null && !businessId.automatic()){
				KKLog.d("Model 对象业务主键的值不能为null");
				return;
			}else{
				boolean isUpdate = true;
				if(businessIdValue == null && businessId.automatic()){
					String prefix = businessId.prefix();
					String suffix = businessId.suffix();
					businessIdValue = generatorBusinessId(prefix,suffix);
					businessField.set(entity, businessIdValue);
				}
				Column column = businessField.getAnnotation(Column.class);
				String columnName = column.name();
				if(columnName.equals("")){
					columnName = businessField.getName();
				}
				long count = getCount(columnName+" = '"+String.valueOf(businessIdValue)+"'",db);
				if(count == 0){
					isUpdate = false;
				}
				ContentValues contentValues = setContentValues(entity);
				if(isUpdate){
					String where = columnName + " = ?";
					String[] args = new String[] { String.valueOf(businessIdValue) };
					row = db.update(tableName, contentValues, where, args);
				}else{
					row = db.insert(tableName, null, contentValues);
				}
			}
		}
	}
	
	/**
	 * 获取写数据库，启用事务.
	 *
	 */
	private SQLiteDatabase startWritableDatabase(){
		return this.dbHelper.getWritableDatabase();
	}
	
	/**
	 * 获取只读数据库.
	 */
	private SQLiteDatabase startReadableDatabase(){
		return this.dbHelper.getReadableDatabase();
	}
	
	/**
	 * 关闭数据库并提交事务.
	 */
	private void closeDatabase(boolean writable){
		if(writable){
			dbHelper.closeWritableDatabase();
		}else{
			dbHelper.closeReadableDatabase();
		}
	}
	
	private final static Object getCursorValue(Cursor cursor,Field field){
		String name = "";
		if(field.isAnnotationPresent(Column.class)){
			Column column = field.getAnnotation(Column.class);
			name = column.name();
			if(field.isAnnotationPresent(ManyToOne.class)){
				field = TableHelper.getIdColumnField(field.getType());
			}
		}
		if(name.equals("")){
			name = field.getName();
		}
		int index = cursor.getColumnIndex(name);
		if(index == -1){
			name = name.toUpperCase();
			index = cursor.getColumnIndex(name);
		}
		Object value = null;
		if(index > -1){
			if (field.getType() == String.class) {
				value = cursor.getString(index);
			}else if (Integer.TYPE == field.getType() || field.getType() == Integer.class) {
				value = cursor.getInt(index);
			}else if (Short.TYPE == field.getType() || field.getType() == Short.class) {
				value = cursor.getShort(index);
			} else if (Long.TYPE == field.getType() || field.getType() == Long.class) {
				value = cursor.getLong(index);
			} else if (Double.TYPE == field.getType() || field.getType() == Double.class) {
				value = cursor.getDouble(index);
			} else if (Float.TYPE == field.getType() || field.getType() == Float.class) {
				value = cursor.getFloat(index);
			} else if (field.getType() == Blob.class) {
				value = cursor.getBlob(index);
			} else {
				value = cursor.getString(index);
			}
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private T renderBean(Cursor cursor,SQLiteDatabase db) throws IllegalArgumentException,
			IllegalAccessException, InstantiationException {
		T t = this.clazz.newInstance();
		List<Field> fields = TableHelper.getColumnFields(clazz);
		for (Field field : fields) {
			field.setAccessible(true);
			if(field.isAnnotationPresent(ManyToOne.class)){
				ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
				boolean lazyLoad = manyToOne.lazy();
				if(!lazyLoad){
					if (field.getType() instanceof Class) {
						String foreignValue = String.valueOf(getCursorValue(cursor, field));
						String name = "";
						if(name.equals("")){
							name = field.getName();
						}
						int index = cursor.getColumnIndex(name);
						if(index == -1){
							name = name.toUpperCase();
							index = cursor.getColumnIndex(name);
						}
						if(field.getType().isAnnotationPresent(Table.class)){
							if(TableHelper.checkTableExist(db, field.getType())){
								String relationsTableName = field.getType().getAnnotation(Table.class).name();
								if(relationsTableName.equals("")){
									relationsTableName = field.getType().getSimpleName().toUpperCase();
								}
								
								Field relationsIdField = TableHelper.getIdColumnField(field.getType());
								String foreignKey = relationsIdField.getAnnotation(Column.class).name();
								if(foreignKey.equals("")){
									foreignKey = relationsIdField.getName();
								}
								StringBuilder sql = new StringBuilder();
								sql.append("select * from ");
								sql.append(relationsTableName);
								sql.append(" where ");
								sql.append(foreignKey);
								sql.append(" = '"+foreignValue);
								sql.append("'");
								Cursor foreignCursor = db.rawQuery(sql.toString(),null);
								while (foreignCursor.moveToNext()) {
									Object foreign = renderBean(field.getType(), foreignCursor);
									field.set(t,foreign);
								}
								foreignCursor.close();
							}
						}
					}
				}
			}else if(field.isAnnotationPresent(OneToMany.class)){
				OneToMany oneToMany = field.getAnnotation(OneToMany.class);
				boolean lazyLoad = oneToMany.lazy();
				if(!lazyLoad){
					if(field.getType().isAssignableFrom(List.class)){
						Class<?> listEntityClazz = null;
						Type fc = field.getGenericType();
						if (fc == null)
							continue;
						if (fc instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) fc;
							listEntityClazz = (Class<?>) pt.getActualTypeArguments()[0];
						}
						if(listEntityClazz != null && TableHelper.checkTableExist(db, listEntityClazz)){
							String relationsTableName = listEntityClazz.getAnnotation(Table.class).name();
							if(relationsTableName.equals("")){
								relationsTableName = field.getType().getSimpleName().toUpperCase();
							}
							String foreignKey;
							Field relationsIdField = TableHelper.getIdColumnField(clazz);
							if(oneToMany.joinColumn().equals("")){
								foreignKey = relationsIdField.getAnnotation(Column.class).name();
								if(foreignKey.equals("")){
									foreignKey = relationsIdField.getName();
								}
							}else{
								foreignKey = oneToMany.joinColumn();
							}
							String foreignValue = String.valueOf(getCursorValue(cursor, relationsIdField));
							StringBuilder sql = new StringBuilder();
							sql.append("select * from ");
							sql.append(relationsTableName);
							sql.append(" where ");
							sql.append(foreignKey);
							sql.append(" = '"+foreignValue);
							sql.append("'");
							Cursor foreignCursor = db.rawQuery(sql.toString(),null);
							List<T> list = new ArrayList<T>();
							while (foreignCursor.moveToNext()) {
								Object foreign = renderBean(listEntityClazz, foreignCursor);
								list.add((T)foreign);
							}
							foreignCursor.close();
							if(list.size() > 0){
								field.set(t,list);
							}
						}
					}
						
						
//					String relationsTableName = field.getType().getAnnotation(Table.class).name();
//					if(relationsTableName.equals("")){
//						relationsTableName = field.getType().getSimpleName().toUpperCase();
//					}
//					Field relationsIdField = TableHelper.getIdColumnField(field.getType());
//					String foreignKey = relationsIdField.getAnnotation(Column.class).name();
//					if(foreignKey.equals("")){
//						foreignKey = relationsIdField.getName();
//					}
//					StringBuilder sql = new StringBuilder();
//					sql.append("select * from ");
//					sql.append(relationsTableName);
//					sql.append(" where ");
//					sql.append(foreignKey);
//					sql.append(" = '"+foreignValue);
//					sql.append("'");
//					Cursor foreignCursor = db.rawQuery(sql.toString(),null);
//					while (foreignCursor.moveToNext()) {
//						Object foreign = renderBean(field.getType(), foreignCursor);
//						field.set(t,foreign);
//					}
//					foreignCursor.close();
					
				}
			}else{
				field.set(t,getCursorValue(cursor, field));
			}
		}
		return t;
	}
	
	private <K> K renderBean(Class<K> entity,Cursor cursor) throws InstantiationException, IllegalAccessException{
		K k = entity.newInstance();
		List<Field> fields = TableHelper.getColumnFields(entity);
		for (Field field : fields) {
			field.setAccessible(true);
			field.set(k,getCursorValue(cursor, field));
		}
		return k;
	}
	
	private ContentValues setContentValues(T t) {
		try {
			ContentValues contentValues = new ContentValues();
			List<Field> fields = TableHelper.getColumnFields(clazz);
			for (Field field : fields) {
				if(field.isAnnotationPresent(Column.class)){
					if(!field.isAnnotationPresent(Id.class)){
						field.setAccessible(true);
						Object fieldValue = field.get(t);
						if (fieldValue != null) {
							Column column = field.getAnnotation(Column.class);
							String columnName = column.name();
							if(field.isAnnotationPresent(ManyToOne.class)){
								Field foreignField = TableHelper.getIdColumnField(fieldValue.getClass());
								foreignField.setAccessible(true);
								fieldValue = foreignField.get(fieldValue);
								if(columnName.equals("")){
									column = foreignField.getAnnotation(Column.class);
									columnName = column.name();
								}
								if(columnName.equals("")){
									columnName = foreignField.getName();
								}
							}else if(columnName.equals("")){
								columnName = field.getName();
							}
							contentValues.put(columnName, fieldValue.toString());
						}
					}
				}
			}
			return contentValues;
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
