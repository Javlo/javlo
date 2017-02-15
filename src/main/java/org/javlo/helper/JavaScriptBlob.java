package org.javlo.helper;

import java.io.IOException;

public class JavaScriptBlob {

	private byte[] data;
	private String contentType;

	public JavaScriptBlob(String dataBase64) throws IOException {
		String[] datas = dataBase64.split(",");
		if (datas.length == 2) {
			data = StringHelper.decodeBase64(datas[1]);			
			contentType = datas[0];
			contentType=contentType.replace("data:", "");
			contentType=contentType.substring(0, contentType.indexOf(";"));
		}
	}
	
	public byte[] getData() {
		return data;
	}
	
	public String getContentType() {
		return contentType;
	}
}
