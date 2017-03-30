package com.code.server.cardgame.response;

import net.sf.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UserOfResult {
    
    private long userId;//id
    
    private String image; //头像 
    
    private String username;
    
    private String card;
    
    private int multiple;//倍数
    
    private String scores;//分数

	private int huNum;
	private int dianPaoNum;
	private int moBaoNum;
	private int lianZhuangNum;
	private long time;


	public long getUserId() {
		return userId;
	}

	public UserOfResult setUserId(long userId) {
		this.userId = userId;
		return this;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}




	public String getImage() {
		return image;
	}




	public void setImage(String image) {
		this.image = image;
	}




	public String getUsername() {
		return username;
	}




	public void setUsername(String username) {
		this.username = username;
	}




	public String getCard() {
		return card;
	}




	public void setCard(String card) {
		this.card = card;
	}




	public int getMultiple() {
		return multiple;
	}




	public void setMultiple(int multiple) {
		this.multiple = multiple;
	}




	public String getScores() {
		return scores;
	}




	public void setScores(String scores) {
		this.scores = scores;
	}

	public int getHuNum() {
		return huNum;
	}

	public UserOfResult setHuNum(int huNum) {
		this.huNum = huNum;
		return this;
	}

	public int getDianPaoNum() {
		return dianPaoNum;
	}

	public UserOfResult setDianPaoNum(int dianPaoNum) {
		this.dianPaoNum = dianPaoNum;
		return this;
	}

	public int getMoBaoNum() {
		return moBaoNum;
	}

	public UserOfResult setMoBaoNum(int moBaoNum) {
		this.moBaoNum = moBaoNum;
		return this;
	}

	public int getLianZhuangNum() {
		return lianZhuangNum;
	}

	public UserOfResult setLianZhuangNum(int lianZhuangNum) {
		this.lianZhuangNum = lianZhuangNum;
		return this;
	}

	public long getTime() {
		return time;
	}

	public UserOfResult setTime(long time) {
		this.time = time;
		return this;
	}

}