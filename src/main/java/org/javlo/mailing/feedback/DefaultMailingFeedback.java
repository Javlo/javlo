package org.javlo.mailing.feedback;

import java.util.Date;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.mailing.FeedBackMailingBean;
import org.javlo.module.mailing.MailingAction;
import org.javlo.service.DataToIDService;
import org.javlo.service.RequestService;

public class DefaultMailingFeedback implements IMailingFeedback {

	@Override
	public void treatFeedback(ContentContext ctx) {
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String mfb = requestService.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
		if (mfb != null) {			
			DataToIDService serv = DataToIDService.getInstance(application);
			Map<String, String> params = StringHelper.uriParamToMap(serv.getData(mfb));
			String id = params.get("mailing");
			String ip = ctx.getRequest().getRemoteHost();
			Enumeration<String> names = ctx.getRequest().getHeaderNames();
			String userAgent = null;
			while (names.hasMoreElements()) {
				String name = names.nextElement();
				if (name.trim().equalsIgnoreCase("user-agent")) {
					userAgent = ctx.getRequest().getHeader(name);
				}
				if (name.trim().equalsIgnoreCase("x-forwarded-for")) {
					ip = ctx.getRequest().getHeader(name);; 
				}
			}
			if (id != null) {
				org.javlo.mailing.Mailing mailing = new org.javlo.mailing.Mailing();
				try {
					if (mailing.isExist(application, id)) {
						mailing.setId(StaticConfig.getInstance(application).getMailingStaticConfig(), id);
						FeedBackMailingBean bean = new FeedBackMailingBean();
						bean.setEmail(params.get("to"));
						bean.setAgent(userAgent);
						bean.setDate(new Date());
						bean.setUrl(ctx.getRequest().getPathInfo());
						bean.setWebaction(ctx.getRequest().getParameter("webaction"));
						bean.setIp(ip);	
						mailing.addFeedBack(bean);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}