package com.wkkyo.android.orm;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wkkyo.android.orm.annotation.BusinessId;
import com.wkkyo.android.orm.annotation.Column;
import com.wkkyo.android.orm.annotation.Id;
import com.wkkyo.android.orm.annotation.ManyToOne;
import com.wkkyo.android.orm.annotation.OneToMany;
import com.wkkyo.android.orm.annotation.Table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public final class TableHelper {
	
	private final static Map<String,Boolean> TABLES = new HashMap<String, Boolean>();
	
	private static String dbPath;
	
	public final static boolean checkTableExist(SQLiteDatabase db,Class<?> clazz){
		String tableName = clazz.getAnnotation(Table.class).name();
		if(tableName == null || "".equals(tableName)){
			tableName = clazz.getSimpleName();
		}
		if(!db.getPath().equals(dbPath)){
			dbPath = db.getPath();
			TABLES.clear();
		}
		if(TABLES.containsKey(tableName)){
			return true;
		}
		Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"+ tableName + "'";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                	TABLES.put(tableName, true);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null){
                cursor.close();
            }
            cursor = null;
        }
		return false;
	}
	
	/**
	 * 创建表.
	 *
	 * @param <T> the generic type
	 * @param db 根据映射的对象创建表.
	 * @param clazz 对象映射
	 */
	public final static <T> void createTable(SQLiteDatabase db,Class<T> clazz) {
		if(!clazz.isAnnotationPresent(Table.class)){
			return;
		}else{
			String tableName = clazz.getAnnotation(Table.class).name();
			if(tableName == null || "".equals(tableName)){
				tableName = clazz.getSimpleName();
			}
			StringBuilder sql = new StringBuilder();
			sql.append("CREATE TABLE IF NOT EXISTS ");
			sql.append(tableName);
			sql.append(" (");
			List<Field> columns = getColumnFields(clazz);
			for(Field field:columns){
				if(field.isAnnotationPresent(Column.class)){
					Column column = field.getAnnotation(Column.class);
					String columnType = getColumnType(field.getType());
					String columnName = column.name();
					if(field.isAnnotationPresent(ManyToOne.class) && columnName.equals("")){
						Field foreignField = TableHelper.getIdColumnField(field.getType());
						column = foreignField.getAnnotation(Column.class);
						columnName = column.name();
						if(columnName.equals("")){
							columnName = foreignField.getName();
						}
					}else if(columnName.equals("")){
						columnName = field.getName();
					}
					sql.append(columnName).append(" ").append(columnType);
					if(field.isAnnotationPresent(Id.class)){
						boolean autoincrement = field.getAnnotation(Id.class).autoincrement();
						if(autoincrement && (field.getType() == Integer.TYPE 
								|| field.getType() == Integer.class 
								|| field.getType() == Long.TYPE 
								|| field.getType() == Long.class)){
							sql.append(" PRIMARY KEY AUTOINCREMENT NOT NULL");
						}else{
							sql.append(" PRIMARY KEY NOT NULL");
						}
					}else if(field.isAnnotationPresent(BusinessId.class)){
						sql.append(" PRIMARY KEY NOT NULL");
					}
					sql.append(",");
				}			
			}
			sql.delete(sql.length()-1, sql.length());
			sql.append(")");
			db.execSQL(sql.toString());
			TABLES.put(tableName, true);
		}
	}
	
	public final static List<Field> getColumnFields(Class<?> clazz){
		List<Field> fields = new ArrayList<Field>();
		for(Field field:clazz.getDeclaredFields()){
			if(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(OneToMany.class)){
				if(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(BusinessId.class)){
					fields.add(0,field);
				}else{
					fields.add(field);
				}
			}
		}
		for(Field field:clazz.getSuperclass().getDeclaredFields()){
			if(field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(OneToMany.class)){
				if(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(BusinessId.class)){
					fields.add(0,field);
				}else{
					fields.add(field);
				}
			}
		}
		return fields;
	}
	
	public final static Field getIdColumnField(Class<?> clazz){
		Field idField = null;
		for(Field field:clazz.getDeclaredFields()){
			if(field.isAnnotationPresent(Column.class)){
				if(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(BusinessId.class)){
					idField = field;
					break;
				}
			}
		}
		return idField;
	}

	private final static String getColumnType(Class<?> clazz) {
		String type;
		if (clazz == String.class) {
			type = "TEXT";
		} else if (Integer.TYPE == clazz || clazz == Integer.class) {
			type = "INTEGER";
		} else if (Short.TYPE == clazz || clazz == Short.class) {
			type = "SHORT";
		} else if (Long.TYPE == clazz || clazz == Long.class) {
			type = "TEXT";
		} else if (Double.TYPE == clazz || clazz == Double.class) {
			type = "DOUBLE";
		} else if (Float.TYPE == clazz || clazz == Float.class) {
			type = "FLOAT";
		} else if (clazz == Blob.class) {
			type = "BLOB";
		} else if(clazz instanceof Class){
			type = getColumnType(getIdColumnField(clazz).getType());
		} else{
			type = "TEXT";
		}
		return type;
	}

	
	/**
	 * 创建表.
	 *
	 * @param <T> the generic type
	 * @param db 根据映射的对象创建表.
	 * @param clazz 对象映射
	 */
	/*public static <T> void createTable(SQLiteDatabase db, Class<T> clazz,boolean bool) {
		String tableName = "";
		if (clazz.isAnnotationPresent(Table.class)) {
			Table table = (Table) clazz.getAnnotation(Table.class);
			tableName = table.name();
		}
		if(AbStrUtil.isEmpty(tableName)){
			Log.d(TAG, "想要映射的实体["+clazz.getName()+"],未注解@Table(name=\"?\"),被跳过");
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ").append(tableName).append(" (");

		List<Field> allFields = AbTableHelper.joinFieldsOnlyColumn(clazz.getDeclaredFields(), clazz.getSuperclass().getDeclaredFields());
		for (Field field : allFields) {
			if (!field.isAnnotationPresent(Column.class)) {
				continue;
			}

			Column column = (Column) field.getAnnotation(Column.class);

			String columnType = "";
			if (column.type().equals(""))
				columnType = getColumnType(field.getType());
			else {
				columnType = column.type();
			}

			sb.append(column.name() + " " + columnType);

			if (column.length() != 0) {
				sb.append("(" + column.length() + ")");
			}
			//实体类定义为Integer类型后不能生成Id异常
			if ((field.isAnnotationPresent(Id.class)) 
					&& ((field.getType() == Integer.TYPE) || (field.getType() == Integer.class)))
				sb.append(" primary key autoincrement");
			else if (field.isAnnotationPresent(Id.class)) {
				sb.append(" primary key");
			}

			sb.append(", ");
		}

		sb.delete(sb.length() - 2, sb.length() - 1);
		sb.append(")");

		String sql = sb.toString();

		Log.d(TAG, "create table [" + tableName + "]: " + sql);

		db.execSQL(sql);
	}*/
}