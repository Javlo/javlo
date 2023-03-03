package org.javlo.servlet.security;

import javax.servlet.http.HttpServletRequest;

import org.javlo.helper.NetHelper;
import org.javlo.utils.TimeMap;

public class AccessSecurity {

	private TimeMap<String, Integer> requestByMinute = new TimeMap<>(60);

	private static final int MAX_REQUEST_BY_MINUTE = 250;

	private static AccessSecurity instance = new AccessSecurity();

	public static AccessSecurity getInstance(HttpServletRequest request) {
		return instance;
	}

	public boolean isIpBlock(HttpServletRequest request) {
		String ip = NetHelper.getIp(request);
		Integer requestCount = requestByMinute.get(ip);
		if (requestCount == null) {
			requestCount = 1;
			requestByMinute.put(ip, 1);
		} else {
			requestCount++;
			requestByMinute.update(ip, requestCount);
		}
		return requestCount > MAX_REQUEST_BY_MINUTE;
	}

}
