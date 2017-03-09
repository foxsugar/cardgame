package com.code.server.cardgame.encoding;

import net.sf.json.JSONObject;

public class Notice {
	
	
	private String message;
	
	private String sendUserId;
	
	private String acceptUserId;	
	
	private String messageType;
	

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
	
	public String getSendUserId() {
		return sendUserId;
	}

	public void setSendUserId(String sendUserId) {
		this.sendUserId = sendUserId;
	}

	public String getAcceptUserId() {
		return acceptUserId;
	}

	public void setAcceptUserId(String acceptUserId) {
		this.acceptUserId = acceptUserId;
	}

	public JSONObject toJSONObject() {
		JSONObject jSONObject = new JSONObject();
		jSONObject.put("sendUserId", this.sendUserId);
		jSONObject.put("acceptUserId", this.acceptUserId);
		jSONObject.put("message", this.message);
		jSONObject.put("messageType", this.messageType);
		return jSONObject;
	}
	
}
