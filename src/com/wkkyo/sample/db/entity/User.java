package com.wkkyo.sample.db.entity;

import com.wkkyo.android.orm.annotation.Column;
import com.wkkyo.android.orm.annotation.Id;
import com.wkkyo.android.orm.annotation.Table;

/**
 * 用户
 * @author wkkyo
 *
 */
@Table(name="user")
public class User implements java.io.Serializable {

	// Fields

	private static final long serialVersionUID = 1L;
	@Id
	@Column
	private Integer id;
	
	@Column
	private String username;

	@Column
	private String password;

	// Constructors

	/** default constructor */
	public User() {
	}

	/** full constructor */
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}