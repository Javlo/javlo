package org.javlo.macro;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;

public class CreateRolesFromUserList extends AbstractMacro {

	@Override
	public String getName() {
		return "create-roles-from-user-list";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest()); 
	    IUserFactory fact = UserFactory.createUserFactory(ctx.getRequest());
	    Set<String> userRoles = new HashSet<String>(globalContext.getUserRoles());
	    int beforeSize = userRoles.size();
		for (IUserInfo userInfo : fact.getUserInfoList()) {
			userInfo.getRoles();
			for (String role : userInfo.getRoles()) {
				if (role != null && !userRoles.contains(role)) {
					userRoles.add(role);
				}
			}
		}
		int countChange = userRoles.size() - beforeSize;
		String msg = countChange + " role(s) created.";
		MessageRepository.getInstance(ctx).setGlobalMessage(new GenericMessage(msg, GenericMessage.INFO));
		
		globalContext.setUserRoles(userRoles);		
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}

	
}
