package org.javlo.actions;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.User;

public class DefaultGeneralLister implements IGeneralListner {
	
	private static Logger logger = Logger.getLogger(DefaultGeneralLister.class.getName());

	@Override
	public String onPublish(ContentContext ctx) throws ListerException {
		logger.info("default general listner");
		return null;
	}

	@Override
	public String onInit(ServletContext application) throws ListerException {
		logger.info("default general listner");
		return null;
	}

	@Override
	public String onAdminLogin(GlobalContext globalContext, User user) throws ListerException {
		logger.info("default general listner");
		return null;
	}

}
