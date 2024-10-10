package org.javlo.macro;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;

import java.util.Collection;
import java.util.Map;

/**
 * merge component meta data defined in the template and meta data define in content (only meta data, don't touch to data)
 * 
 * @author pvandermaesen
 * 
 */
public class EncryptVisitorsPasswordMacro extends AbstractMacro {

	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EncryptVisitorsPasswordMacro.class.getName());

	@Override
	public String getName() {
		return "encrypt-visitors-password";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		IUserFactory userFactory = UserFactory.createUserFactory(globalContext, ctx.getRequest().getSession());
		Collection<IUserInfo> allUserInfo = userFactory.getUserInfoList();
		int c = 0;
		for (IUserInfo iUserInfo : allUserInfo) {
			if (iUserInfo.getPassword() != null && iUserInfo.getPassword().trim().length() > 0) {
				logger.info("encrypt password : "+iUserInfo.getLogin());
				iUserInfo.setPassword(StringHelper.encryptPassword(iUserInfo.getPassword()));
				c++;
			}
		}
		userFactory.store();

		return c+" password encrypted.";
	}

	@Override
	public boolean isPreview() {
		return true;
	}

};
