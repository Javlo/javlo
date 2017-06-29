package org.javlo.macro.interactive;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.mailing.Mailing;
import org.javlo.mailing.MailingFactory;

public class MailingStat implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(MailingStat.class.getName());

	@Override
	public String getName() {
		return "mailing-stat";
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
		return "mailing-stat";
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/mailing-stat.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		MailingFactory mailingService = MailingFactory.getInstance(ctx.getRequest().getSession().getServletContext());
		try {
			String pageId = ctx.getCurrentPage().getId();
			if (ctx.getCurrentPage().isChildrenOfAssociation()) {
				pageId = ctx.getCurrentPage().getRootOfChildrenAssociation().getFirstChild().getId();
			}
			List<Mailing> mailingList = mailingService.getOldMailingListByContext(ctx.getGlobalContext().getContextKey());
			Iterator<Mailing> mailingIte = mailingList.iterator();
			while (mailingIte.hasNext()) {
				Mailing nextMailing = mailingIte.next(); 
				if (nextMailing.isTest() || nextMailing.getPageId() == null || !nextMailing.getPageId().equals(pageId)) {
					mailingIte.remove();
				}
			}
			ctx.getRequest().setAttribute("mailingList", mailingList);
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return false;
	}

	@Override
	public boolean isInterative() {
		return true;
	}
}
