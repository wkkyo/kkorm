package com.wkkyo.android.orm;

import java.io.File;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * DBOpenHelper
 * @author Wkkyo
 * @date 2015-10-10
 * @version 1.0.0
 */
public class DBOpenHelper extends SQLiteOpenHelper{
    
    /** 数据库版本号. */
    private final static int mNewVersion = 1;
    
	// Instances
//  private static HashMap<Context, DBOpenHelper> mInstances;
    /** 数据库名. */
    private static String mName = "data.db";
    
    /** 数据库文件夹全路径 */
    private static String mPath;
    
    private static DBOpenHelper instance;
    
    /** 是否已经初始化过只读数据库. */
    private boolean mReadIsInitializing = false;
    /** 是否已经初始化过可写数据库. */
    private boolean mWritableIsInitializing = false;
    
    private SQLiteDatabase readDatabase;
    
    private SQLiteDatabase writeDatabase;
    
    private int writeOpenCount = 0;
    
    private int readOpenCount = 0;
    
	/**
	 * @param context
	 * @param dbPath 数据库文件全路径
	 * @return
	 */
	public synchronized static DBOpenHelper getInstance(Context context,String dbPath,String dbName) {
		/*if(mInstances == null)
            mInstances = new HashMap<Context, DBManager>();

        if(mInstances.get(context) == null)
            mInstances.put(context, new DBManager(context,sourceFile));

        return mInstances.get(context);*/
		if(dbName != null && !dbName.equals(mName)){
			mName = dbName;
		}
		if(instance == null || !dbPath.equals(mPath)){
			instance = new DBOpenHelper(context,dbPath);
		}
		return instance;
	}
	
	private DBOpenHelper(Context context,String dbPath) {
		super(new CustomPathContext(context, dbPath), mName, null, mNewVersion);
		mPath = dbPath;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTables(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO 数据库升级
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//TODO 数据库降级
	}
	
	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		writeOpenCount++;
        if (writeDatabase != null) {
            if (!writeDatabase.isOpen()) {
                // darn! the user closed the database by calling mDatabase.close()
            	writeDatabase = null;
            }
        }

        if (mWritableIsInitializing) {
            throw new IllegalStateException("getWritableDatabase called recursively");
        }

        // If we have a read-only database open, someone could be using it
        // (though they shouldn't), which would cause a lock to be held on
        // the file, and our attempts to open the database read-write would
        // fail waiting for the file lock.  To prevent that, we acquire the
        // lock on the read-only database, which shuts out other users.

        boolean success = false;
        SQLiteDatabase db = null;
        try {
        	mWritableIsInitializing = true;
            if (mName == null) {
                db = SQLiteDatabase.create(null);
            } else {
            	String path = mPath + File.separator + mName;
            	db = SQLiteDatabase.openOrCreateDatabase(path,null);
            }

            int version = db.getVersion();
            if (version != mNewVersion) {
                db.beginTransaction();
                try {
                    if (version == 0) {
                        onCreate(db);
                    } else {
                        if (version > mNewVersion) {
                            onDowngrade(db, version, mNewVersion);
                        } else {
                            onUpgrade(db, version, mNewVersion);
                        }
                    }
                    db.setVersion(mNewVersion);
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
            }

            onOpen(db);
            //启用事务
            db.beginTransaction();
            success = true;
            return db;
        } finally {
        	mWritableIsInitializing = false;
            if (success) {
                if (writeDatabase != null) {
                    try { writeDatabase.close(); } catch (Exception e) { }
                }
                writeDatabase = db;
            } else {
                if (db != null) db.close();
            }
        }
    }
	
	/**
     * 获取只读数据库.
     *
     * @return 数据库对象
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
    	readOpenCount++;
        if (readDatabase != null && readDatabase.isOpen()) {
        	//已经获取过
            return readDatabase; 
        }
        if (mReadIsInitializing) {
        	throw new IllegalStateException("getReadableDatabase called recursively");
        }
        
        SQLiteDatabase db = null;
		try {
			mReadIsInitializing = true;
			String path = mPath + File.separator + mName;
			File dbFile = new File(path);
			if(dbFile.exists()){
				db = SQLiteDatabase.openDatabase(path, null,SQLiteDatabase.OPEN_READONLY);
				if (db.getVersion() != mNewVersion) {
					throw new SQLiteException("Can't upgrade read-only database from version " +
	                        db.getVersion() + " to " + mNewVersion + ": " + path);
				}
				onOpen(db);
				readDatabase = db;
			}else{
				db = SQLiteDatabase.openOrCreateDatabase(path,null);
				onOpen(db);
				readDatabase = db;
			}
		} catch (SQLiteException e) {
//			e.printStackTrace();
		} finally {
			mReadIsInitializing = false;
			if (db != null && db != readDatabase)
				db.close();
		}
    	return readDatabase;
    }
	
    /**
	 * 关闭可写数据库并提交事务.
	 */
	public synchronized void closeWritableDatabase(){
		if(writeDatabase != null){
			writeOpenCount--;
			if(writeOpenCount <= 0){
				writeOpenCount = 0;
				if(writeDatabase.inTransaction()){
					writeDatabase.setTransactionSuccessful();
					writeDatabase.endTransaction();
				}
				if(writeDatabase.isOpen()){
					writeDatabase.close();
					writeDatabase = null;
				}
			}
		}
	}
    
	/**
	 * 关闭只读数据库.
	 */
	public synchronized void closeReadableDatabase(){
		if(readDatabase != null){
			readOpenCount--;
			if(readOpenCount <= 0){
				readOpenCount = 0;
				if(readDatabase.isOpen()){
					readDatabase.close();
					readDatabase = null;
				}
			}
		}
	}

	/**
	 * 创建新的数据库
	 * 
	 * @param path
	 * @return
	 */
	private void createTables(SQLiteDatabase db) {
		
	}
	
}

final class CustomPathContext extends ContextWrapper {

	private String mDbPath;

	public CustomPathContext(Context context, String dbPath) {
		super(context);
		this.mDbPath = dbPath;
		File file = new File(mDbPath);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	@Override
	public File getDatabasePath(String name) {
		File file = new File(mDbPath + File.separator + name);
		return file;
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory) {
		return super.openOrCreateDatabase(getDatabasePath(name)
				.getAbsolutePath(), mode, factory);
	}

	@Override
	public SQLiteDatabase openOrCreateDatabase(String name, int mode,
			CursorFactory factory, DatabaseErrorHandler errorHandler) {
		return super.openOrCreateDatabase(getDatabasePath(name)
				.getAbsolutePath(), mode, factory, errorHandler);
	}
}