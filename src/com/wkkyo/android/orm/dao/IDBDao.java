package com.wkkyo.android.orm.dao;

import java.io.Serializable;
import java.util.List;

/**
 * 数据库操作接口
 * @author wkkyo
 * @param <T>
 */
public interface IDBDao<T> {

	/**
	 * 保存实例到数据库。
	 * @param t
	 * @return
	 */
	T save(T t);
	
	/**
	 * 批量保存实例到数据库。
	 * @param list
	 * @return
	 */
	List<T> save(List<T> list);
	
	/**
	 * 根据sql语句查询记录
	 * @param where 格式为where语句条件部分，例如："dmlb = '52000' and name = 'XXX'" 
	 * @return
	 */
	List<T> query(String where);
	
	/**
	 * 根据sql语句查询记录，支持排序
	 * @param orderBy 格式为order by语句部分，例如："order by id desc" 
	 * @return
	 */
	List<T> queryOrder(String orderBy);
	
	/**
	 * 根据条件查询记录
	 * @param selection 带有?的条件筛选字符串，例如："dmlb = ? and fjm = ?"
	 * @param selectionArgs 条件?对应的筛选值数组
	 * @return
	 */
	List<T> query(String selection,String[] selectionArgs);
	
	/**
	 * 根据条件查询记录
	 * @param selection 带有?的条件筛选字符串，例如："dmlb = ? and fjm = ?"
	 * @param selectionArgs 条件?对应的筛选值数组
	 * @param groupBy 分组属性
	 * @param having 分组后过滤属性
	 * @param orderBy 排序属性
	 * @return
	 */
	List<T> query(String selection,String[] selectionArgs, String groupBy, String having,String orderBy);
	
	/**
	 * 返回所有记录
	 * @return
	 */
	List<T> query();
	
	/**
	 * 返回参数id指定的记录
	 * @param id
	 * @return
	 */
	T get(Serializable id);
	
	/**
	 * 删除指定id对应的数据库记录
	 * @param id
	 * @return 删除的记录数
	 */
	long delete(Serializable id);
	
	/**
	 * 清空表数据。
	 * @return 清空的记录数
	 */
	long deleteAll();
	
	/**
	 * 返回记录条数
	 * @param where 格式为where语句条件部分，例如："dmlb = '52000' and fjm = '000005'" 
	 * @return
	 */
	long getCount(String where);
	
	/**
	 * 返回记录条数
	 * @return
	 */
	public long getCount();
	
}
