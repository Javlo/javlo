package org.javlo.macro;

import java.util.Collection;
import java.util.Map;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;

/**
 * merge component meta data defined in the template and meta data define in content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class EncryptVisitorsPasswordMacro extends AbstractMacro {

	@Override
	public String getName() {
		return "encrypt-visitors-password";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		Collection<IUserInfo> allUserInfo = userFactory.getUserInfoList();
		for (IUserInfo iUserInfo : allUserInfo) {
			if (iUserInfo.getPassword() != null && iUserInfo.getPassword().trim().length() > 0) {
				iUserInfo.setPassword(StringHelper.encryptPassword(iUserInfo.getPassword()));
			}
		}
		userFactory.store();

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
