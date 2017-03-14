package com.code.server.cardgame.response;

import net.sf.json.JSONObject;

import java.util.ArrayList;

public class GameOfResult {
	
	private ArrayList<UserOfResult> userList;

	public ArrayList<UserOfResult> getUserList() {
		return userList;
	}

	public void setUserList(ArrayList<UserOfResult> userList) {
		this.userList = userList;
	}
	
	public JSONObject toJSONObject() {
		JSONObject jSONObject = new JSONObject();
		jSONObject.put("userList", this.userList);
		return jSONObject;
	}
}
