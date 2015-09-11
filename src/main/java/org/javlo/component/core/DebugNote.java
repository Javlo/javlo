/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;

/**
 * @author pvandermaesen
 */
public class DebugNote extends AbstractPropertiesComponent {
	
	private static final String PRIORITY = "priority";

	private static final String TEXT = "text";

	private static final String STATUS = "status";

	private static final String USER = "user";
	
	private static final String MODIF_DATE = "modif_date";

	private static final List<String> FIELDS = Arrays.asList(new String[] { TEXT, STATUS, USER, PRIORITY, MODIF_DATE });

	public static final String TYPE = "debug-note";
	
	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {	
		super.init(bean, newContext);
		if (!getValue().contains("=")) { // old format
			properties.clear();
			setText(getValue());
			setPriority("0");
			setModifDate(StringHelper.renderSortableTime(new Date()));
			storeProperties();
			setModify();
		}
	}
	
	public String getText() {
		return getFieldValue(TEXT);
	}
	
	public void setText(String text) {
		setFieldValue(TEXT, text);
	}

	public String getStatus() {
		return getFieldValue(STATUS);
	}
	
	public void setStatus(String status) {
		setFieldValue(STATUS, status);
	}

	public String getUser() {
		return getFieldValue(USER);
	}
	
	public List<String> getUserList() {
		return StringHelper.stringToCollection(getUser(), getListSeparator());
	}
	
	public void setUser(String user) {
		setFieldValue(USER, user);
	}

	public void setUserList(List<String> list) {
		setUser(StringHelper.collectionToString(list, getListSeparator()));
	}

	public String getPriority() {
		return getFieldValue(PRIORITY);
	}
	
	public void setPriority(String priority) {
		setFieldValue(PRIORITY, priority);
	}
	
	public String getModifDate() {
		return getFieldValue(MODIF_DATE);
	}
	
	public void setModifDate(String text) {
		setFieldValue(MODIF_DATE, text);
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()) + getSpecialPreviewCssId(ctx) + " >";
		} else {
			return "";
		}
	}	

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "</div>";
		} else {
			return "";
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "<div class=\"priority-"+getPriority()+"\">"+getText() + " <span class=\"priority\">("+getPriority()+")</span></div>";
		} else {
			return "";
		}
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		out.println("<div class=\"form-group\">");
		out.println("<input type=\"hidden\" name=\""+createKeyWithField(MODIF_DATE)+"\" value=\""+getModifDate()+"\" />");
		out.println("<textarea class=\"form-control\" placeholder=\""+i18nAccess.getText("debug.text", "text")+"\" rows=\"3\" name=\""+createKeyWithField(TEXT)+"\">"+getText()+"</textarea>");
		
		out.println("</div>");
		UserFactory userFactory = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
		out.println("<fieldset><legend>"+i18nAccess.getText("debug.user", "user")+"</legend>");
		for (IUserInfo userInfo : userFactory.getUserInfoList()) {
			out.println("<label class=\"checkbox-inline\">");
			String checkedString = "";
			if (getUserList().contains(userInfo.getLogin())) {
				checkedString = " checked=\"checked\"";
			}
			out.println("<input type=\"checkbox\" name=\""+createKeyWithField(USER)+"\" value=\""+userInfo.getLogin()+"\" "+checkedString+" /> "+userInfo.getLogin());
			out.println("</label>");
		}
		out.println("</fieldset>");		
		out.println("<fieldset><legend>"+i18nAccess.getText("debug.priority", "priority")+"</legend>");
		String checkedString = "";
		if (getPriority().equals("0")) {
			checkedString = " checked=\"checked\"";
		}
		out.println("<label class=\"radio-inline\"><input type=\"radio\" name=\""+createKeyWithField(PRIORITY)+"\" value=\"0\""+checkedString+" />"+i18nAccess.getText("debug.prority.0", "none")+"</label>");
		checkedString = "";
		if (getPriority().equals("1")) {
			checkedString = " checked=\"checked\"";
		}
		out.println("<label class=\"radio-inline\"><input type=\"radio\" name=\""+createKeyWithField(PRIORITY)+"\" value=\"1\""+checkedString+" />"+i18nAccess.getText("debug.prority.1", "low")+"</label>");
		checkedString = "";
		if (getPriority().equals("2")) {
			checkedString = " checked=\"checked\"";
		}
		out.println("<label class=\"radio-inline\"><input type=\"radio\" name=\""+createKeyWithField(PRIORITY)+"\" value=\"2\""+checkedString+" />"+i18nAccess.getText("debug.prority.2", "middle")+"</label>");
		checkedString = "";
		if (getPriority().equals("3")) {
			checkedString = " checked=\"checked\"";
		}
		out.println("<label class=\"radio-inline\"><input type=\"radio\" name=\""+createKeyWithField(PRIORITY)+"\" value=\"3\""+checkedString+" />"+i18nAccess.getText("debug.prority.3", "high")+"</label>");		
		out.println("</fieldset>");
		out.flush();
		out.close();
		return writer.toString();
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return ctx.getRenderMode() != ContentContext.PREVIEW_MODE;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_EASY);
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {	
		String msg = super.performEdit(ctx);
		if (isModify()) {
			setModifDate(StringHelper.renderSortableTime(new Date()));
			storeProperties();
		}
		return msg;
	}

}
