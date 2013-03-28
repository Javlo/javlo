package org.javlo.component.text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.service.RequestService;

public class DynamicParagraph extends Paragraph implements IAction {

	public static final String MESSAGE_ID_PARAM_NAME = "_dynpara_message_id";

	public static String TYPE = "dynamic-paragraph";

	private static Logger logger = Logger.getLogger(DynamicParagraph.class.getName());

	public String addMessage(ContentContext ctx, String msg) throws IOException {
		String msgId = StringHelper.getRandomId();
		getViewData(ctx).setProperty(msgId, msg);
		storeViewData(ctx);
		return msgId;
	}

	@Override
	public String getActionGroupName() {
		return "dynamic-paragraph";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		out.println("<p>");
		out.println("<textarea>");
		Enumeration keys = getViewData(ctx).keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			out.println(key + " = " + getViewData(ctx).getProperty(key));
		}
		out.println("</textarea>");
		out.println("</p>");
		out.println("# messages : " + getViewData(ctx).size());

		out.close();

		return new String(outStream.toByteArray());
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String msgId = requestService.getParameter(MESSAGE_ID_PARAM_NAME, null);
		if (msgId == null) {
			logger.warning("undefined message ID.");
			return "message id not found.";
		}

		if (getViewData(ctx).getProperty(msgId) == null) {
			return "content not defined : " + msgId + " lg:" + ctx.getLanguage() + " rlg:" + ctx.getRequestContentLanguage();
		}

		String msg = getViewData(ctx).getProperty(msgId);
		if (msg == null) {
			return "message not found : " + msgId;
		}
		msg = StringHelper.removeTag(msg);
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String value = XHTMLHelper.textToXHTML(msg, globalContext);
		if (value.trim().length() == 0) {
			logger.warning("empty content found with message ID : " + msgId);
		}
		return value;
	}

	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		super.init(bean, ctx);
		loadViewData(ctx);
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}

}
