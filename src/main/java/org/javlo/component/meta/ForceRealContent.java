package org.javlo.component.meta;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

public class ForceRealContent extends AbstractVisualComponent {

	public static final String TYPE = "force-real-content";

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) {
		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.println("<div class=\"checkbox\"><input type=\"hidden\" name=\""+getInputName("submit")+"\" value=\"true\" />");
			String checked = "";
			if (StringHelper.isTrue(getValue())) {
				checked = " checked=\"checked\"";
			}
			out.println("<label><input type=\"checkbox\" name=\""+getContentName()+"\" "+checked+"/>"+i18nAccess.getText("component.real-content.not-real", "force not real content."));
			out.println();
			out.println("</label></div>");			
			out.close();
			return new String(outStream.toByteArray());		 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), "false");
		if (requestService.getParameter(getInputName("submit"), null) != null) {
			if (!getValue().equals(newContent)) {
				setValue(newContent);
				setModify();
			}
		}
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return AbstractVisualComponent.COMPLEXITY_ADMIN;
	}

}
