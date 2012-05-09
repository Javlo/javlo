package org.javlo.macro;

import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;

/**
 * merge component meta data defined in the template and meta data define in content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class EncryptPasswordComponent extends AbstractMacro {

	public String getName() {
		return "encrypt-password";
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory adminUserFactory = AdminUserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		IUserInfo[] allUserInfo = adminUserFactory.getUserInfoList();
		for (IUserInfo iUserInfo : allUserInfo) {
			iUserInfo.setPassword(StringHelper.encryptPassword(iUserInfo.getPassword()));
		}
		adminUserFactory.store();

		return null;
	}

};
