package com.wkkyo.android.orm;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
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
	
	final static Map<String,Boolean> TABLES = new HashMap<String, Boolean>();
	
	private static String dbPath;
	
	/**
	 * 判断表是否已经存在
	 * @param db
	 * @param clazz
	 * @return
	 */
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
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type = 'table' AND name = '"+ tableName + "'";
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
					if(column.defaultValue() != null && columnType.equals("TEXT")){
						sql.append(" DEFAULT ");
						sql.append("'");
						sql.append(column.defaultValue());
						sql.append("'");
					}
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
		} else if (clazz == byte[].class) {
			type = "BLOB";
		} else if (clazz == Date.class) {
			type = "TEXT";
		} else if(clazz instanceof Class){
			type = getColumnType(getIdColumnField(clazz).getType());
		} else{
			type = "TEXT";
		}
		return type;
	}
	
	public static String[] getColumnNames(SQLiteDatabase db, String tableName) {
		String[] columnNames = null;
		Cursor c = null;
		try {
			c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
			if (null != c) {
				int columnIndex = c.getColumnIndex("name");
				if (columnIndex == -1) {
					return null;
				}

				int index = 0;
				columnNames = new String[c.getCount()];
				for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
					columnNames[index] = c.getString(columnIndex);
					index++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			c.close();
		}
		return columnNames;
	}
	
	public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length())
                        + separator.length());

        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }
	
//	static void updateTables(SQLiteDatabase db, int oldVersion,int newVersion){
//		if(DBConfig.updateClasses != null){
//			for(Class<?> clazz:DBConfig.updateClasses){
//				if(!clazz.isAnnotationPresent(Table.class)){
//					KKLog.d("Model 对象未注记Table");
//					return;
//				}
//				if(checkTableExist(db, clazz)){
//					if(!clazz.isAnnotationPresent(Table.class)){
//						return;
//					}else{
//						String tableName = clazz.getAnnotation(Table.class).name();
//						if(tableName == null || "".equals(tableName)){
//							tableName = clazz.getSimpleName();
//						}
//						String tempTableName = tableName+"_temp";
//						
//						StringBuilder alterSql = new StringBuilder();
//						alterSql.append("ALTER TABLE ");
//						alterSql.append(tableName);
//						alterSql.append(" RENAME TO ");
//						alterSql.append(tempTableName);
//						db.execSQL(alterSql.toString());
//						
//						createTable(db, clazz);
//						
//						String[] columnArr = getColumnNames(db, tempTableName);
//						String columns = join(columnArr, ",", 0, columnArr.length);
//						
//						StringBuilder insertSql = new StringBuilder();
//						insertSql.append("INSERT INTO ");
//						insertSql.append(tableName);
//						insertSql.append(" (" + columns + ") ");
//						insertSql.append(" SELECT " + columns + " FROM " + tempTableName);
//						db.execSQL(insertSql.toString());
//						
//						StringBuilder dropSql = new StringBuilder();
//						dropSql.append("DROP TABLE IF EXISTS ");
//						dropSql.append(tempTableName);
//						db.execSQL(dropSql.toString());
//						
//						TABLES.put(tableName, true);
//					}
//				}
//			}
//		}
//	}
	
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
