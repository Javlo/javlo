package org.javlo.fields;

import org.javlo.context.ContentContext;
import org.javlo.service.ListService;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;
import org.javlo.user.UserFactory;

public class UserList extends FieldList {
	
	public class UserFieldBean extends FieldBean  {
		
		private IUserInfo userInfo;

		public UserFieldBean(ContentContext ctx, String value) {
			super(ctx);
			User user  = UserFactory.createUserFactory(ctx.getRequest()).getUser(value);
			if (user != null) {
				userInfo = user.getUserInfo();
			}
		}
		
		public IUserInfo getUserInfo() {
			return userInfo;
		}
		
	}
	
	@Override
	public String getType() {
		return "users";
	}
	
	@Override
	public String getListName() {
		return ListService.SPECIAL_LIST_VIEW_USERS;
	}
	
	@Override
	public FieldBean getBean(ContentContext ctx) {
		return new UserFieldBean(ctx, getValue());
	}
	
	@Override
	protected FieldBean newFieldBean(ContentContext ctx) {
		return new UserFieldBean(ctx, getValue());
	}
}
