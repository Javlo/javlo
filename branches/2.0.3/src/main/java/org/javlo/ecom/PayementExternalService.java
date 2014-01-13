package org.javlo.ecom;

import java.util.LinkedList;
import java.util.List;

import org.javlo.helper.StringHelper;

public class PayementExternalService {

	private String name = "";
	private final String initialName;
	private String appId = "";
	private String secretKey = "";
	private String URL = "";
	private String returnPage = "";
	
	public PayementExternalService() {
		initialName = "";
	}

	public PayementExternalService(String contentRAW) {
		List<String> contentList = StringHelper.stringToCollection(contentRAW, ",");
		try {
			setName(contentList.get(0));
			setAppId(contentList.get(1));
			setSecretKey(contentList.get(2));
			setURL(contentList.get(3));
			setReturnPage(contentList.get(4));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		initialName = getName();
	}

	@Override
	public String toString() {
		List<String> contentList = new LinkedList<String>();
		contentList.add(getName());
		contentList.add(getAppId());
		contentList.add(getSecretKey());
		contentList.add(getURL());
		contentList.add(getReturnPage());
		return StringHelper.collectionToString(contentList, ",");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getInitialName() {
		return initialName;
	}

	public String getReturnPage() {
		return returnPage;
	}

	public void setReturnPage(String returnPage) {
		this.returnPage = returnPage;
	}

}
