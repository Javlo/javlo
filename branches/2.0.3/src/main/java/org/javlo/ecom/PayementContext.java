package org.javlo.ecom;

import java.util.Date;

import org.javlo.helper.StringHelper;

public class PayementContext {
	
	private String id = StringHelper.getRandomId();
	private String name;
	private String basketId;
	private String appId;
	private String token;
	private String secretKey;
	private Date creationDate = new Date();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBasketId() {
		return basketId;
	}
	public void setBasketId(String basketId) {
		this.basketId = basketId;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	} 
}
