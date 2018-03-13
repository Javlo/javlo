package org.javlo.component.links;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import javax.servlet.ServletContext;

import org.javlo.actions.IAction;
import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ILink;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PageMeta;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.Module;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.resource.VisualResource;
import org.owasp.encoder.Encode;

public class SmartLink extends ComplexPropertiesLink implements ILink, IAction {

	public static final String TYPE = "smart-link";
	private Date latestValidDate;

	@Override
	public String getURL(ContentContext ctx) throws Exception {
		return getURL();
	}
	
	@Override
	public boolean isLinkValid(ContentContext ctx) throws Exception {
		return !StringHelper.isEmpty(getURL(ctx));
	}

	@Override
	public String getType() {
		return TYPE;
	}

	protected String getBodyId() {
		return "id_" + getId();
	}

	public String getURLInputName() {
		return "url_" + getId();
	}

	public String getTitleInputName() {
		return "title_" + getId();
	}

	public String getDateInputName() {
		return "date_" + getId();
	}

	public String getDescriptionInputName() {
		return "description_" + getId();
	}

	public String getImageInputName() {
		return "image_" + getId();
	}

	public String getURL() {
		return properties.getProperty("url", "");
	}

	public void setURL(String url) {
		properties.setProperty("url", url);
	}

	public String getDate() {
		return properties.getProperty("date", "");
	}

	public void setDate(String date) {
		properties.setProperty("date", date);
	}

	public Date getTime() {
		if (getDate().trim().length() > 0) {
			Date time;
			try {
				time = StringHelper.parseTime(getDate());
				return time;
			} catch (ParseException e) {
				logger.warning(e.getMessage());
			}
		}
		return null;
	}

	public String getDescription() {
		return properties.getProperty("description");
	}

	public void setDescription(String description) {
		properties.setProperty("description", description);
	}

	public String getTitle() {
		return properties.getProperty("title");
	}

	public void setTitle(String title) {
		properties.setProperty("title", title);
	}

	public String getImageURL() {
		return properties.getProperty("image");
	}

	public void setImageURL(String url) {
		properties.setProperty("image", url);
	}
	
	@Override
	protected boolean isAutoDeletable() {
		return true;
	}

	@Override
	public String getTextForSearch(ContentContext ctx) {
		return getValue(ctx) + ' ' + getDescription() + ' ' + getTitle() + ' ' + getURL() + ' ' + getImageURL();
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		String url = getImageURL();
		out.println("<div class=\"row\">");
		int colSize = 12;
		if (!StringHelper.isEmpty(url)) {
			colSize=9;
			out.println("<div class=\"col-sm-3\"><figure><img class=\"img-responsive img-fluid\" src=\"" + getImageURL() + "\" /></figure></div>");	
		}		
		out.println("<div class=\"col-sm-"+colSize+"\">");	
		String target="";
		if (ctx.getGlobalContext().isOpenExternalLinkAsPopup()) {
			target=" target=\"_blank\"";
		}
		out.println("<a"+target+" class=\"" + getType() + "\" href=\"" + getURL() + "\">");
		out.println("<h"+(ctx.getTitleDepth()+1)+">" + getTitle() + "</h"+(ctx.getTitleDepth()+1)+">");
		out.println("</a>");
		out.println("<div class=\"news-info\">");
		if (getDate().trim().length() > 0) {
			String time = "";
			if (!StringHelper.renderTimeOnly(getTime()).equals("00:00:00")) {
				time = "<span class=\"time\"> " + StringHelper.renderTimeOnly(getTime()) + "</span>";
			}
			out.println("<span class=\"date\">" + StringHelper.renderDate(getTime()) + time + "</span>");			
		}
		out.println("<span class=\"badge badge-secondary host\">"+StringHelper.extractHost(getURL())+"</span>");
		out.println("</div>");
		out.println("<p>" + getDescription() + "</p>");
		out.println("</div></div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Module currentModule = ModulesContext.getInstance(ctx.getRequest().getSession(), globalContext).getCurrentModule();
		String renderer = currentModule.getRealPath("/jsp/components/smart-links.jsp");

		try {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			out.println("<div class=\"main\">");
			out.println("<div class=\"form-group\">");
			out.println("<input type=\"text\" class=\"link form-control\" placeholder=\"" + i18nAccess.getText("global.link") + "\" name=\"" + getURLInputName() + "\" value=\"" + getURL() + "\" />");
			out.println("</div>");
			out.println("<div class=\"body\" id=\"" + getBodyId() + "\">");
			if (!StringHelper.isEmpty(getTitle()) || !StringHelper.isEmpty(getDescription())) {
				ctx.getRequest().setAttribute("title", getTitle());
				ctx.getRequest().setAttribute("description", getDescription());
				ctx.getRequest().setAttribute("image", getImageURL());
				ctx.getRequest().setAttribute("date", getDate());
				String xhtml = ServletHelper.executeJSP(ctx, renderer);
				out.println(xhtml);
			} 
			out.println("<div class=\"waiting\" style=\"display: none;\">");
			out.println("<div class=\"row\">");
			out.println("<div class=\"col-sm-5\">&nbsp;</div>");
			out.println("<div class=\"col-sm-2\"><img src=\""+InfoBean.getCurrentInfoBean(ctx).getAjaxLoaderURL()+"\" alt=\"waiting...\" /></div>");
			out.println("<div class=\"col-sm-5\">&nbsp;</div>");
			out.println("</div>");
			out.println("</div>");
			
			out.println("</div>");
			out.println("</div>");
			out.println("<script type=\"text/javascript\">initSmartLink();</script>");

		} catch (Exception e) {
			e.printStackTrace();
			out.println(e.getMessage());
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	public static String performLoadLink(RequestService rs, ContentContext ctx, ServletContext application, Module currentModule, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		String renderer = currentModule.getRealPath("/jsp/components/smart-links.jsp");

		String url = rs.getParameter("url", null);
		if (StringHelper.isURL(url)) {
			ContentService content = ContentService.getInstance(ctx.getRequest());
			SmartLink comp = (SmartLink) content.getComponent(ctx, rs.getParameter("comp_id", null));
			if (comp != null) {
				URL sourceURL = new URL(url);				
				String title;
				String description;				
				String date;
				PageMeta pageMeta = NetHelper.getPageMeta(new URL(url));
				Collection<VisualResource> images = new LinkedList<VisualResource>();
				if (pageMeta != null) {
					title = pageMeta.getTitle();
					description = pageMeta.getDescription();
					date = StringHelper.renderTime(pageMeta.getDate());
					URL imageURL = pageMeta.getImage();
					if(imageURL != null) {				 
						VisualResource vr = new VisualResource();
						vr.setUri(pageMeta.getImage().toString());
						images.add(vr);	
						comp.setImageURL(imageURL.toString());
					}
				} else {
					String remoteXHTML = NetHelper.readPageWithGet(sourceURL);				
					title = Encode.forHtmlAttribute(NetHelper.getPageTitle(remoteXHTML));					
					description = Encode.forHtmlAttribute(NetHelper.getPageDescription(remoteXHTML));					
					images = NetHelper.extractImage(sourceURL, remoteXHTML, true);
					int biggerImageSize = 0;
					VisualResource biggerImage = null;
					for (VisualResource resource : images) {
						float prop = (float)resource.getWidth()/(float)resource.getHeight();
						if (prop>1) {
							prop = (float)resource.getHeight()/(float)resource.getWidth();
						}					
						if (prop>0.4 && biggerImageSize < resource.getWidth()*resource.getHeight()) {
							biggerImageSize = resource.getWidth()*resource.getHeight();
							biggerImage = resource;
						}
						
					}
					if (biggerImage != null) {
						comp.setImageURL(biggerImage.getUri());
					}
					date = StringHelper.renderTime(new Date(NetHelper.readDate(sourceURL)));
				}
				ctx.getRequest().setAttribute("title", title);
				ctx.getRequest().setAttribute("description", description);
				ctx.getRequest().setAttribute("images", images);
				ctx.getRequest().setAttribute("comp", comp);
				ctx.getRequest().setAttribute("image", comp.getImageURL());
				ctx.getRequest().setAttribute("date", date);
				String xhtml = ServletHelper.executeJSP(ctx, renderer);
				ctx.getAjaxInsideZone().put(comp.getBodyId(), xhtml);
			} else {
				return "component not found : " + rs.getParameter("comp_id", null);
			}
		}

		return null;
	}

	@Override
	public String getActionGroupName() {
		return "smartlink";
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String oldValue = getValue();
		RequestService rs = RequestService.getInstance(ctx.getRequest());
		setImageURL(rs.getParameter(getImageInputName(), ""));
		setDescription((rs.getParameter(getDescriptionInputName(), "")));
		setTitle((rs.getParameter(getTitleInputName(), "")));
		setDate((rs.getParameter(getDateInputName(), "")));
		setURL(rs.getParameter(getURLInputName(), ""));
		storeProperties();
		if (oldValue.equals(getValue())) {
			setModify();
		}
		return null;
	}

	@Override
	public boolean isListable() {
		return true;
	}
	
	
	@Override
	public void setLatestValidDate(Date date) {
		latestValidDate = date;
	}

	@Override
	public Date getLatestValidDate() {
		return latestValidDate;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {	
		return !StringHelper.isEmpty(getValue());
	}
	
}
