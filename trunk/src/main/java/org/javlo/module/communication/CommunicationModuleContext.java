package org.javlo.module.communication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.javlo.bean.LinkToRenderer;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.GlobalContextFactory;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.core.AbstractModuleContext;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModuleException;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.IMService;
import org.javlo.service.exception.ServiceException;
import org.javlo.user.AdminUserSecurity;

public class CommunicationModuleContext extends AbstractModuleContext {

	public static final String MODULE_NAME = "communication";

	private LinkToRenderer homeLink = null;
	private List<LinkToRenderer> navigation = new LinkedList<LinkToRenderer>();
	private Set<String> canSpeakSites = new HashSet<String>();

	/**
	 * use getInstance on AbstractModuleContext or smart instance in action method for instantiate.
	 */
	@Override
	public void init() {
	}

	public static CommunicationModuleContext getInstance(HttpServletRequest request) throws FileNotFoundException, InstantiationException, IllegalAccessException, IOException, ModuleException {
		GlobalContext globalContext = GlobalContext.getInstance(request);
		Module module = ModulesContext.getInstance(request.getSession(), globalContext).searchModule(MODULE_NAME);
		return (CommunicationModuleContext) AbstractModuleContext.getInstance(request.getSession(), globalContext, module, CommunicationModuleContext.class);
	}

	public void loadNavigation(ContentContext ctx) throws ServiceException, Exception {
		List<LinkToRenderer> navigation = new LinkedList<LinkToRenderer>();
		Set<String> canSpeakSites = new LinkedHashSet<String>();
		LinkToRenderer homeLink = null;

		HttpServletRequest request = ctx.getRequest();

		AdminUserSecurity adminUserSecurity = AdminUserSecurity.getInstance();

		String currentSite = globalContext.getContextKey();

		boolean canListAll = adminUserSecurity.isMaster(ctx.getCurrentEditUser()) || adminUserSecurity.isGod(ctx.getCurrentEditUser());

		if (canListAll) {
			Collection<GlobalContext> allContext = GlobalContextFactory.getAllGlobalContext(request.getSession().getServletContext());
			for (GlobalContext context : allContext) {
				if (context.getAliasOf() == null || context.getAliasOf().trim().isEmpty()) {
					String contextKey = context.getContextKey();
					canSpeakSites.add(contextKey);
					if (!context.getAllPrincipals().isEmpty()) {
						LinkToRenderer link = new LinkToRenderer(contextKey, contextKey, "jsp/empty.jsp");
						if (currentSite.equals(contextKey)) {
							homeLink = link;
						}
						navigation.add(link);
					}
				}
			}
			navigation.add(new LinkToRenderer(I18nAccess.getInstance(ctx).getText("communication.all-sites"),
					IMService.ALL_SITES, "jsp/empty.jsp"));
			canSpeakSites.add(IMService.ALL_SITES);
		} else {
			navigation.add(new LinkToRenderer(globalContext.getContextKey(), globalContext.getContextKey(), "jsp/empty.jsp"));
			canSpeakSites.add(globalContext.getContextKey());
		}

		if (getCurrentLink() == null) {
			setCurrentLink(currentSite);
		}

		this.navigation = navigation;
		this.homeLink = homeLink;
		this.canSpeakSites = canSpeakSites;
	}

	@Override
	public List<LinkToRenderer> getNavigation() {
		return navigation;
	}

	@Override
	public LinkToRenderer getHomeLink() {
		return homeLink;
	}

	public String getCurrentSite() {
		return getCurrentLink();
	}

	public Set<String> getCanSpeakSites() {
		return canSpeakSites;
	}

}
