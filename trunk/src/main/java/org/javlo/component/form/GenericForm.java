package org.javlo.component.form;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.IAction;
import org.javlo.actions.MailingActions;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.MailingManager;
import org.javlo.message.GenericMessage;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class GenericForm extends AbstractVisualComponent implements IAction {
	
	private static Logger logger = Logger.getLogger(GenericForm.class.getName());
	
	private Properties bundle;
	
	@Override
	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {	
		super.init(bean, ctx);
		if (getValue() == null || getValue().trim().length() == 0) {			
			setValue(StringHelper.sortText(getConfig(ctx).getRAWConfig(ctx, ctx.getCurrentTemplate(), getType())));	
		}
	}

	@Override
	public String getType() {
		return "generic-form";		
	}
	
	protected boolean isCaptcha(ContentContext ctx) {
		return StringHelper.isTrue(getConfig(ctx).getProperty("captcha", "true"));
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		ctx.getRequest().setAttribute("ci18n", getTranslation());
	}

	@Override
	public String getActionGroupName() { 
		return "gform";
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.WEB2_COLOR;
	}
	
	@Override
	public boolean isEmpty(ContentContext ctx) {
		return false;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return true;
	}
	
	public Properties getTranslation() {		
		if (bundle == null) {
			bundle = new Properties();
			try {
				bundle.load(new StringReader(getValue()));
			} catch (IOException e) {				
				e.printStackTrace();
			}			
		}		
		return bundle;
	}
	
	public static final String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		ContentContext ctx = ContentContext.getContentContext(request, response);
		ContentService content = ContentService.createContent(request);
		GenericForm comp = (GenericForm)content.getComponent(ctx, requestService.getParameter("comp_id", null));
		
		/** check captcha **/
		String captcha = requestService.getParameter("captcha", null);		
		if (captcha == null || !CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
			GenericMessage msg = new GenericMessage (comp.getTranslation().getProperty("error.captcha"), GenericMessage.ERROR);
			request.setAttribute("msg", msg);
			request.setAttribute("error_captcha", "true");
			return null;
		}
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		
		String subject = "GenericForm submit : "+globalContext.getGlobalTitle();
		
		Map<String,Object> params = request.getParameterMap();
		Collection<String> keys = params.keySet();
		for (String key : keys) {
			if (!key.equals("webaction") && !key.equals("comp_id") && !key.equals("captcha")) {
				Object value = params.get(key);
				if (value instanceof Object[]) {
					out.println(key+'='+StringHelper.arrayToString((Object[])params.get(key), ","));
				} else {
					out.println(key+'='+params.get(key));
				}
			}
		}
		out.println("");
		
		out.close();
		String mailContent =  new String(outStream.toByteArray());
		
		logger.info("mail content : "+StringHelper.sortText(mailContent));
		
		MailingManager mailingManager = MailingManager.getInstance(globalContext.getStaticConfig());
		InternetAddress adminEmail = new InternetAddress(globalContext.getAdministratorEmail());
		InternetAddress bccEmail = new InternetAddress("p@noctis.be");
		mailingManager.sendMail(adminEmail, adminEmail, bccEmail, subject, mailContent, false);
		
		GenericMessage msg = new GenericMessage (comp.getTranslation().getProperty("message.thanks"), GenericMessage.INFO);
		request.setAttribute("msg", msg);		
		request.setAttribute("valid", "true");
		
		return null;
	}
}
