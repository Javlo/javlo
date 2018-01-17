package org.javlo.component.date;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentContext;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.calendar.CalendarBean;
import org.javlo.service.calendar.ICalFilter;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.User;

public class CalendarComponent extends AbstractPropertiesComponent implements IAction {

	public static final String TYPE = "calendar";
	
	public static final List<String> FIELDS = new LinkedList<String> (Arrays.asList(new String[] { "cssclass", "summary", "eventcategories" } ) );

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		prepareCalendar(ctx,0);
	}

	private void prepareCalendar(ContentContext ctx, int step) throws Exception {
		CalendarBean bean;
		if (ctx.getRequest().getParameter("calkey") != null) {
			bean = CalendarBean.getInstance(ctx, ctx.getRequest().getParameter("calkey"), step);			
			ctx.getRequest().setAttribute("calendarPage", true);
		} else {
			bean = CalendarBean.getInstance(ctx);
		}		
		if (!StringHelper.isAllEmpty(getFieldValue("summary"), getFieldValue("eventcategories"))) {
			bean.setFilter(new ICalFilter(getFieldValue("summary"), getFieldValue("eventcategories")));
		}
	}

	@Override
	public String getFontAwesome() {
		return "calendar";
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}
	
	public static String performUpdatecalendar(ContentContext ctx, EditContext editContext, GlobalContext globalContext, User currentUser, ContentService content, ComponentContext componentContext, RequestService rs, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {
		CalendarComponent comp = (CalendarComponent)ComponentHelper.getComponentFromRequest(ctx);
		comp.prepareCalendar(ctx,0);
		if (ctx.getRequest().getParameter("calzone") != null) {
			ctx.getAjaxZone().put(ctx.getRequest().getParameter("calzone"), comp.getXHTMLCode(ctx));
		}
		return null;
	}
	
	public static String performUpdatecalendargroup(ContentContext ctx, EditContext editContext, GlobalContext globalContext, User currentUser, ContentService content, ComponentContext componentContext, RequestService rs, I18nAccess i18nAccess, MessageRepository messageRepository, Module currentModule, AdminUserFactory adminUserFactory) throws Exception {
		CalendarComponent comp = (CalendarComponent)ComponentHelper.getComponentFromRequest(ctx);
		Integer step = null;
		if (ctx.getRequest().getParameter("step") != null) {
			step = Integer.parseInt(ctx.getRequest().getParameter("step"));
		}
		comp.prepareCalendar(ctx, step);
		if (ctx.getRequest().getParameter("calzone") != null) {
			ctx.getAjaxZone().put(ctx.getRequest().getParameter("calzone"), comp.getXHTMLCode(ctx));
		}
		return null;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
}
