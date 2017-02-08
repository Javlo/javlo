package org.javlo.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.client.response.OAuthAuthzResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.javlo.context.ContentContext;
import org.javlo.helper.NavigationHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;
import org.javlo.service.social.ISocialNetwork;
import org.javlo.service.social.SocialService;
import org.javlo.service.social.SocialUser;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.User;

public class OauthServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(OauthServlet.class.getName());

	public OauthServlet() {
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String socialNetworkName;
		boolean admin = request.getServletPath().contains("admin");
		if (request.getParameter("state") != null) {
			Map<String, String> params = StringHelper.stringToMap(request.getParameter("state"));
			socialNetworkName = params.get("name");
			try {
				ContentContext ctx = ContentContext.getContentContext(request, response);
				MenuElement targetPage = NavigationHelper.getPageById(ctx, params.get("page"));
				ISocialNetwork social = SocialService.getInstance(ctx).getNetwork(socialNetworkName);
				if (social == null || targetPage == null) {
					if (social == null) {
						logger.warning("social network not found : " + socialNetworkName);
					}
					if (targetPage == null) {
						logger.warning("page not found : " + params.get("page"));
					}
					response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				} else {
					social.performRedirect(request, response);
					ctx.setUser();
					params = new HashMap<String,String>();
					if (admin) {
						params.put("oauth", "true");
						ctx = ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE);
						AdminUserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), request.getSession());
						User adminUser = userFactory.getCurrentUser(request.getSession());
						if (adminUser == null) {
							I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
							params.put("err", i18nAccess.getText("user.error.msg"));
						} else {
							if (StringHelper.isEmpty(adminUser.getUserInfo().getPassword())) {								
								SocialUser socialUser = social.getSocialUser(request);
								if (socialUser != null) {
									logger.info("oauth login : "+socialUser.getFirstName());
									IUserInfo userInfo = adminUser.getUserInfo();
									userInfo.setFirstName(socialUser.getFirstName());
									userInfo.setLastName(socialUser.getLastName());
									userInfo.setAvatarURL(socialUser.getAvatarURL());
									userInfo.setPassword(StringHelper.getRandomId());
									userFactory.updateUserInfo(userInfo);
									userFactory.store();
								} else {
									logger.warning("socialUser not found.");
								}
							}
						}
					}										
					response.sendRedirect(URLHelper.createURL(ctx, targetPage, params));
				}
			} catch (Exception e) {
				throw new ServletException(e);
			}
		} else {
			OAuthAuthzResponse oar;
			try {
				oar = OAuthAuthzResponse.oauthCodeAuthzResponse(request);
				String code = oar.getCode();
			} catch (OAuthProblemException e) {
				e.printStackTrace();
			}			
		}		
	}
}