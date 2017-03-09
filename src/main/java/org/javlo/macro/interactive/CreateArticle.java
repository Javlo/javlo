package org.javlo.macro.interactive;

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.MapHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.mailing.Mail;
import org.javlo.mailing.Mailing;
import org.javlo.message.MessageRepository;
import org.javlo.module.macro.MacroModuleContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserInfo;

public class CreateArticle implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(CreateArticle.class.getName());

	@Override
	public String getName() {
		return "create-article";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getActionGroupName() {
		return "macro-create-article";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/create-article.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		Map<String, String> rootPages = new HashMap<String, String>();
		try {
			for (MenuElement page : MacroHelper.searchArticleRoot(ctx)) {
				rootPages.put(page.getName(), page.getTitle(ctx));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		rootPages = MapHelper.sortByValue(rootPages);
		ctx.getRequest().setAttribute("pages", rootPages);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		if (globalContext.getTags().size() > 0) {
			ctx.getRequest().setAttribute("tags", globalContext.getTags());
		}

		List<String> roles = new LinkedList<String>();
		Set<String> roleSet = new HashSet<String>();
		for (String role : ctx.getGlobalContext().getAdminUserRoles()) {
			roleSet.clear();
			roleSet.add(role);
			if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
				roles.add(role);
			}
		}
		Collections.sort(roles);
		ctx.getRequest().setAttribute("adminRoles", roles);

		return null;
	}

	public static String performCreate(RequestService rs, EditContext editCtx, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String pageName = rs.getParameter("root", null);
		String date = rs.getParameter("date", null);

		boolean create = rs.getParameter("create", null) != null;
		boolean duplicate = rs.getParameter("duplicate", null) != null;
		String message = null;
		String newURL = null;
		String newEditURL = null;

		String lang = rs.getParameter("lang", null);
		ContentContext ctxLg = ctx;
		if (lang != null) {
			ctxLg = new ContentContext(ctx);
			ctxLg.setContentLanguage(lang);
		}

		if (pageName == null) {
			return "page or date not found.";
		}
		try {
			Date articleDate;
			if (date != null && date.trim().length() > 0) {
				articleDate = StringHelper.parseDate(date);
				Calendar cal = Calendar.getInstance();
				cal.setTime(articleDate);
				Calendar now = Calendar.getInstance();
				cal.set(Calendar.HOUR, now.get(Calendar.HOUR));
				cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
				articleDate = cal.getTime();
			} else {
				articleDate = new Date();
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(articleDate);
			MenuElement rootPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctxLg).searchChildFromName(pageName);
			MenuElement newPage = null;
			if (rootPage != null) {
				List<String> roles = new LinkedList<String>();
				Set<String> roleSet = new HashSet<String>();
				for (String role : ctx.getGlobalContext().getAdminUserRoles()) {
					roleSet.clear();
					roleSet.add(role);
					if (ctx.getCurrentEditUser().validForRoles(roleSet)) {
						roles.add(role);
					}
				}
				String yearPageName = rootPage.getName() + "-" + cal.get(Calendar.YEAR);
				MenuElement yearPage = MacroHelper.addPageIfNotExist(ctxLg, rootPage.getName(), yearPageName, true);
				MacroHelper.createMonthStructure(ctxLg, yearPage);
				String mountPageName = MacroHelper.getMonthPageName(ctxLg, yearPage.getName(), articleDate);
				MenuElement mountPage = ContentService.getInstance(ctx.getRequest()).getNavigation(ctxLg).searchChildFromName(mountPageName);
				if (mountPage != null) {
					newPage = MacroHelper.createArticlePageName(ctx, mountPage);
					if (newPage != null) {
						if (duplicate) {
							ContentService content = ContentService.getInstance(ctx.getRequest());
							MenuElement page = content.getNavigation(ctx).searchChildFromName(ctx.getRequest().getParameter("page"));
							ContentContext noAreaCtx = ctx.getContextWithArea(null);
							ContentElementList contentList = page.getContent(noAreaCtx);
							String parent = "0";
							while (contentList.hasNext(noAreaCtx)) {
								IContentVisualComponent comp = contentList.next(noAreaCtx);
								if (!comp.isRepeat()) {
									ComponentBean bean = new ComponentBean(comp.getComponentBean());
									bean.setId(StringHelper.getRandomId());
									parent = content.createContent(ctx, bean, parent, false);
								}
							}
							ctx.getCurrentPage().releaseCache();
						} else if (create) {
							GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
							Collection<String> tags = null;
							if (globalContext.getTags().size() > 0) {
								tags = new LinkedList<String>();
								for (String tag : globalContext.getTags()) {
									if (rs.getParameter("tag-" + tag, null) != null) {
										tags.add(tag);
									}
								}
							}
							MacroHelper.addContentInPage(ctxLg, newPage, rootPage.getName().toLowerCase(), articleDate, tags);
						}
						newURL = URLHelper.createURL(ctxLg.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), newPage);
						newEditURL = URLHelper.createURL(ctxLg.getContextWithOtherRenderMode(ContentContext.EDIT_MODE), newPage);

						List<String> selectedRole = new LinkedList<String>();
						for (String role : roles) {
							if (rs.getParameter("role-" + role, null) != null) {
								newPage.addEditorRole(role);
								selectedRole.add(role);
							}
						}

						if (rs.getParameter("email", null) != null) {
							GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
							AdminUserFactory userFact = AdminUserFactory.createAdminUserFactory(globalContext, ctx.getRequest().getSession());

							Set<InternetAddress> receivers = new HashSet<InternetAddress>();
							for (IUserInfo userInfo : userFact.getUserInfoList()) {
								Set<String> userRoles = new HashSet<String>(userInfo.getRoles());
								userRoles.retainAll(selectedRole);
								if (userRoles.size() > 0) {
									try {
										InternetAddress address = new InternetAddress(userInfo.getEmail());
										if (address != null) {
											receivers.add(address);
										}
									} catch (Exception e) {
										logger.warning(e.getMessage());
									}
								}
							}

							Mail mail = ctxLg.getCurrentTemplate().getMail(ctxLg, "create-article", ctxLg.getRequestContentLanguage());
							if (mail == null) {
								mail = new Mail("new page created.", "a new page was create on : " + URLHelper.createURL(ctxLg.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.VIEW_MODE)));
							} else {
								ContentContext mailContext = ctxLg.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
								mailContext.setPath(newPage.getPath());
								mail.setContent(XHTMLHelper.replaceJSTLData(mailContext, mail.getContent()));
								mail.setSubject(XHTMLHelper.replaceJSTLData(mailContext, mail.getSubject()));
							}

							Mailing m = new Mailing();
							m.setFrom(new InternetAddress(globalContext.getAdministratorEmail()));
							m.setReceivers(receivers);
							m.setSubject(mail.getSubject());
							m.setAdminEmail(globalContext.getAdministratorEmail());
							m.setNotif(null);
							m.setContent(mail.getContent());
							m.setHtml(mail.isHtml());
							Calendar calendar = Calendar.getInstance();
							calendar.roll(Calendar.MINUTE, 5);
							m.setSendDate(calendar.getTime());
							m.store(ctx.getRequest().getSession().getServletContext());
						}
					}
				} else {
					message = "mount page not found : " + mountPageName;
				}
			} else {
				message = pageName + " not found.";
			}
			MacroModuleContext.getInstance(ctx.getRequest()).setActiveMacro(null);
			if (ctx.isEditPreview()) {
				if (newURL != null && create) {
					newEditURL = URLHelper.addParam(newEditURL, "module", "content");
					newEditURL = URLHelper.addParam(newEditURL, "webaction", "editPreview");
					newEditURL = URLHelper.addParam(newEditURL, "webaction", "edit.changeArea");
					newEditURL = URLHelper.addParam(newEditURL, ContentContext.PREVIEW_EDIT_PARAM, "true");
					newEditURL = URLHelper.addParam(newEditURL, "lightEdit", "true");
					newEditURL = URLHelper.addParam(newEditURL, "area", ComponentBean.DEFAULT_AREA);
				}
				if (create) {
					NetHelper.sendRedirectTemporarily(ctx.getResponse(), newEditURL);
				} else {
					ctx.setParentURL(newURL);
					ctx.setClosePopup(true);
				}

				// ctx.getRequest().getRequestDispatcher(editPressrealseURL).forward(ctx.getRequest(),
				// ctx.getResponse());

				/*
				 * ctx.setClosePopup(true); if (newURL != null) {
				 * ctx.setParentURL(newURL); }
				 */
			}
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return message;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return true;
	}

	@Override
	public boolean isInterative() {
		return true;
	}
}
