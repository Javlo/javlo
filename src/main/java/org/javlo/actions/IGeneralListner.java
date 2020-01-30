package org.javlo.actions;

import javax.servlet.ServletContext;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.user.User;

public interface IGeneralListner {
	
	public String onPublish(ContentContext ctx) throws ListerException;
	
	public String onInit(ServletContext application) throws ListerException;
	
	public String onAdminLogin(GlobalContext globalContext, User user) throws ListerException;
	

}
