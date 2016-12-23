package com.keeper.client;

import java.io.Serializable;
import java.util.Date;

/**
 *@author huangdou
 *@at 2016年12月23日上午9:51:52
 *@version 0.0.1
 */
public class Person implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2316310734739842108L;

	private String name ;
	
	private int age ;
	
	private boolean joinTheTeam ;
	
	private long joinTime ;
	
	private Date joinDate ;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isJoinTheTeam() {
		return joinTheTeam;
	}

	public void setJoinTheTeam(boolean joinTheTeam) {
		this.joinTheTeam = joinTheTeam;
	}

	public long getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(long joinTime) {
		this.joinTime = joinTime;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public String toString() {
		return "name : " + name + ",age : "+age+",joinTheTeam : "+joinTheTeam+",joinTime : " + joinTime+",joinDate : " + joinDate.toLocaleString();
	}

}
