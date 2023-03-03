package org.javlo.user;

import java.util.Calendar;
import java.util.logging.Logger;

import org.javlo.context.GlobalContext;

public class MaxLoginService {
	
	private static Logger logger = Logger.getLogger(MaxLoginService.class.getName());
	
	private static MaxLoginService instance = new MaxLoginService();

	private static int maxErrorLoginHours = 0;
	
	private int errorCount = 0;
	
	private int hours = 0;
	
	private MaxLoginService() {}
	
	public static MaxLoginService getInstance() {
		return instance ;
	}

	public static void setMaxErrorLoginByHours(int maxErrorLoginHours) {
		MaxLoginService.maxErrorLoginHours = maxErrorLoginHours;
	}
	
	private int getCurrentHour() {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}
	
	public boolean isLoginAuthorised(GlobalContext globalContext) {
		if (maxErrorLoginHours == 0) {
			return true;
		} else {
			if (hours == getCurrentHour()) {
				if (errorCount < maxErrorLoginHours) {
					return true;
				} else  {
					logger.warning("too many errors on login [ bad login:"+errorCount+" ] : "+globalContext.getContextKey());
					return false;
				}
			} else {
				errorCount = 0;
				hours = getCurrentHour();
				return true;
			}
		}
	}
	
	public void addBadPassword() {
		errorCount++;
	}
	

}
