package org.javlo.servlet.security;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.javlo.helper.NetHelper;
import org.javlo.utils.TimeMap;

public class AccessSecurity {
	
	private static int logPrint = 0;
	
	private static Logger logger = Logger.getLogger(AccessSecurity.class.getName());

	private TimeMap<String, Integer> requestByMinute = new TimeMap<>(60);

	private static final int MAX_REQUEST_BY_MINUTE = 2000;

	private static AccessSecurity instance = new AccessSecurity();

	public static AccessSecurity getInstance(HttpServletRequest request) {
		return instance;
	}

	public boolean isIpBlock(HttpServletRequest request) {
		String ip = NetHelper.getIp(request);
		Integer requestCount = requestByMinute.get(ip);
		if (requestCount == null) {
			requestCount = 1;
		} else {
			requestCount++;
		}
		requestByMinute.update(ip, requestCount);
		
		boolean out = requestCount > MAX_REQUEST_BY_MINUTE;
		if (out) {
			logPrint++;
			if (logPrint%MAX_REQUEST_BY_MINUTE == 0) {
				logger.severe("BLOCK IP : "+NetHelper.getIp(request));
			}
		}
				
		return out;
	}

}
