/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.IReverseLinkComponent;
import org.javlo.component.image.IImageTitle;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.CountThreadService;
import org.javlo.helper.NetHelper;
import org.javlo.helper.PatternHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.thread.AbstractThread;
import org.javlo.utils.TimeMap;

/**
 * @author pvandermaesen
 */

public class SmartExternalLink extends ComplexPropertiesLink implements IReverseLinkComponent, IImageTitle {
	
	private static Map<String,String> BAD_LINKS = Collections.synchronizedMap(new TimeMap<String, String>(60*60*24)); // 1 day cache for bad link

	public static class UndateInfo extends AbstractThread {

		private long timeout = 10 * 1000;

		Properties prop = new Properties();

		String logInfo = "";

		public String getCacheFodler() {
			return this.getField("cache-folder");
		}

		public String getDataFolder() {
			return this.getField("data-folder");
		}

		public String getForceImageURL() {
			return this.getField("image-url");
		}

		public File getPropFile() {
			return new File(getField("prop-file"));
		}

		@Override
		public long getTimeout() {
			return timeout; // time out thread after 10 sec.
		}

		public String getTitle() {
			return prop.getProperty(TITLE_KEY);
		}

		public String getURL() {
			return this.getField("url");
		}

		protected boolean isContentValid(String pageContent) {
			List<URL> externalLink = NetHelper.getLinks(pageContent, getURL());
			int linkToPictureCount = 0;
			for (URL url : externalLink) {
				if (StringHelper.isImage(url.getFile())) {
					linkToPictureCount = linkToPictureCount + 1;
				}
			}
			return linkToPictureCount > 6;
		}

		public Boolean isValidConnection() {
			String stringValue = prop.getProperty(VALID_CONNECTION_KEY, null);
			if (stringValue == null) {
				return null;
			}
			return new Boolean(StringHelper.isTrue(stringValue));
		}

		@Override
		public String logInfo() {
			return logInfo;
		}

		public boolean mustBeRemoved() {
			return StringHelper.isTrue(prop.getProperty("mustBeRemoved", "false"));
		}

		@Override
		public void run() {
			if (mustBeRemoved()) {
				return;
			}

			if (getTitle() != null) {
				return;
			}

			try {
				URL url = new URL(getURL());
				if (isValidConnection() == null || BAD_LINKS.containsKey(url.toString())) {					
					setValidConnection(NetHelper.isURLValid(url));
					storeViewData();

					if (isValidConnection()) {						
						timeout = 60 * 1000; // if connection valid thread can
						// run more time
						logInfo = "read : " + url;
						URLConnection conn = url.openConnection();						
						String pageContent = NetHelper.readPageGet(conn, true);
						url = conn.getURL();						
						if (pageContent == null) {
							setLinkValid(false);
							setMustBeRemoved(true);
							logger.info("url: " + url + " must de removed because content unredeable.");
						} else {
							if (!isContentValid(pageContent)) {
								BAD_LINKS.put(url.toString(), "");
								setLinkValid(false);
								setMustBeRemoved(true);
								logger.info("url: " + url + " must de removed because content unvalid.");
							} else {
								setTitle(NetHelper.getPageTitle(pageContent));
								setDescription(NetHelper.getPageDescription(pageContent));
								CRC32 crc32 = new CRC32();
								URL imageURL = null;
								if (getForceImageURL() != null && getForceImageURL().trim().length() > 0) {
									imageURL = new URL(getForceImageURL());
								}
								logInfo = "load image : " + StringHelper.neverNull(imageURL, "?");
								String uri = NetHelper.getLocalCopyOfPageImage(getCacheFodler(), getDataFolder(), url, imageURL, pageContent, crc32, true, false);								
								setImageCRC32(crc32.getValue());
								if (uri != null) {
									setImageURI(uri);
									setLinkValid(true);
									setMustBeRemoved(false);
								} else {
									BAD_LINKS.put(url.toString(), "");
									setLinkValid(false);
									setMustBeRemoved(true);
									logger.info("url: " + url + " must de removed because image not found.");
								}
							}
						}
					} else {
						BAD_LINKS.put(url.toString(), "");
						setMustBeRemoved(true);
						logger.info("url: " + url + " must de removed because unvalid connection.");
					}
					logger.fine("refresh smart url info [END] : " + url + " [THREAD:" + CountThreadService.getInstance().getCountThread() + "]");
				}
			} catch (Throwable t) {
				t.printStackTrace();
				logger.fine(t.getMessage());
				setLinkValid(false); // if problem -> invalid the component
			}
			try {
				storeViewData();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}

		public void setCacheFolder(String url) {
			this.setField("cache-folder", url);
		}

		public void setDataFolder(String folder) {
			this.setField("data-folder", folder);
		}

		public void setDescription(String desc) {
			if (desc != null) {
				prop.setProperty(DESCRIPTION_KEY, desc);
			}
		}

		public void setForceImageURL(String url) {
			this.setField("image-url", url);
		}

		public void setImageCRC32(long mustBeRemoved) {
			prop.setProperty("crc32", "" + mustBeRemoved);
		}

		public void setImageURI(String uri) {
			prop.setProperty(IMAGE_URI_KEY, uri);
		}

		public void setLinkValid(boolean visible) {
			prop.setProperty(VALID_LINK, "" + visible);
		}

		public void setMustBeRemoved(boolean mustBeRemoved) {
			prop.setProperty("mustBeRemoved", "" + mustBeRemoved);
		}

		public void setPropFile(File propFile) {
			setField("prop-file", propFile.getAbsolutePath());
		}

		public void setTitle(String title) {
			if (title != null) {
				prop.setProperty(TITLE_KEY, title);
			}
		}

		public void setURL(String url) {
			this.setField("url", url);
		}

		public void setValidConnection(boolean title) {
			prop.setProperty(VALID_CONNECTION_KEY, "" + title);
		}

		public void storeViewData() throws IOException {
			ResourceHelper.writePropertiesToFile(prop, getPropFile(), "create by smart external link thread at : " + StringHelper.renderTime(new Date()));
		}
	}

	protected static final String IMAGE_LINK_KEY = "force-image-link";

	public static final String STYLE_NORMAL = "normal";

	public static final String STYLE_PRIORITY = "priority";

	private static final String STYLE_UNVISIBLE = "unvisible";

	private static List<String> threadURL = new Vector<String>();

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(SmartExternalLink.class.getName());

	private static final String REVERSE_LINK_KEY = "reverse-link";

	protected static final String TITLE_KEY = "target.title";

	protected static final String DESCRIPTION_KEY = "target.description";

	protected static final String IMAGE_URI_KEY = "target.image-uri";

	protected static final String IMAGE_KEY = "target.image";

	protected static final String RESPONSE_KEY = "target.response";

	protected static final String VALID_CONNECTION_KEY = "target.connection-valid";

	protected static final String VALID_LINK = "target.valid";

	public static final String TYPE = "smart-external-link";

	private boolean dataLoaded = false;

	public void addRequestURL(ContentContext ctx, String url) {
		ctx.getRequest().setAttribute(getType() + url, "");
	}

	@Override
	public void delete(ContentContext ctx) {
		try {
			deleteImage(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.delete(ctx);
	}

	protected void deleteImage(ContentContext ctx) throws IOException {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
		String imagePath = URLHelper.mergePath(staticConfig.getAllDataFolder(), staticConfig.getImageFolder());
		if (getImageURI(ctx) != null) {
			File image = new File(URLHelper.mergePath(imagePath, getImageURI(ctx)));
			if (image.exists()) {
				image.delete();
			}
		}
	}
	
	private String getLinkDescription(ContentContext ctx) throws IOException {
		return getViewData(ctx).getProperty(DESCRIPTION_KEY, "");
	}

	protected String getDescriptionInputName() {
		return "desc" + ID_SEPARATOR + getId();
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		loadData(ctx);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		if (!StringHelper.isTrue(getViewData(ctx).getProperty(VALID_LINK, "true"))) {
			setMessage(null);
			return "<div class=\"line\">" + i18nAccess.getText("global.working") + "</div>";
		}

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		String link = properties.getProperty(LINK_KEY, "");
		String imageLink = properties.getProperty(IMAGE_LINK_KEY, "");

		out.println(getSpecialInputTag());

		try {

			String linkTitle = i18nAccess.getText("component.link.link");
			String imageLinkTitle = i18nAccess.getText("component.link.image");

			out.println("<div class=\"edit three-col-layout\">");
			if (getImageURI(ctx) != null) {
				out.println("<span class=\"image\" style=\"vertical-align: center; float: right; margin-right: 10px;\">");
				String imageURL = URLHelper.createTransformURL(ctx, getPage(), getImageURI(ctx), "list");
				out.println("<img src=\"" + imageURL + "\" alt=\"" + getTitle(ctx) + "\" /></span>");
			}
			out.println("<div class=\"line\">");
			out.println("<label for=\"" + getLinkName() + "\">" + linkTitle + "</label>");
			out.print(" : <input id=\"" + getLinkName() + "\" name=\"" + getLinkName() + "\" value=\"");
			out.print(link);
			out.println("\"/>");
			if (link.trim().length() > 0) {
				out.println("&nbsp;<a href=\"" + link + "\">&gt;&gt;</a>");
			}
			out.println("</div><div class=\"line\">");
			out.print("<label for=\"" + getImageLink() + "\">" + imageLinkTitle + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getTextInput(getImageLink(), imageLink));
			out.println("</div><br /><br /><div class=\"line\">");
			out.print("<label for=\"" + getTitleInputName() + "\">" + i18nAccess.getText("field.title") + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getTextInput(getTitleInputName(), getTitle(ctx)));
			out.println("</div><br /><div class=\"line\">");
			out.print("<label for=\"" + getDescriptionInputName() + "\">" + i18nAccess.getText("global.description") + "</label>");
			out.print(" : ");
			out.println(XHTMLHelper.getTextArea(getDescriptionInputName(), getLinkDescription(ctx), new String[][] { { "style", "width: 220px; margin-top: 16px;" } }));
			out.println("</div>");
			out.println("<div class=\"content_clear\"><span></span></div></div>");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// validation

		if (link.trim().length() > 0) {
			if (!PatternHelper.EXTERNAL_LINK_PATTERN.matcher(link).matches()) {
				setMessage(new GenericMessage(i18nAccess.getText("component.error.external-link"), GenericMessage.ERROR));
			}
		} else {
			setMessage(new GenericMessage(i18nAccess.getText("component.message.help.external_link"), GenericMessage.HELP));
		}

		return writer.toString();
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	public long getImageCRC32(ContentContext ctx) throws IOException {
		String CRCStr = getViewData(ctx).getProperty("crc32");
		if (CRCStr != null) {
			try {
				long crc = Long.parseLong(CRCStr);
				return crc;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		try {
			loadData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return getTitle(ctx);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	protected String getImageLink() {
		return "image-link" + ID_SEPARATOR + getId();
	}

	public String getImageURI(ContentContext ctx) throws IOException {
		return getViewData(ctx).getProperty(IMAGE_URI_KEY, null);
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		try {
			loadData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			return getImageURI(ctx);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getLabel(ContentContext ctx) throws IOException {
		String label = "";
		String link = properties.getProperty(LINK_KEY, "");
		if (label.trim().length() == 0) {
			label = getTitle(ctx);
			if ((label != null) && (label.trim().length() == 0)) {
				label = link;
			}
		}
		return label;
	}

	@Override
	public String getLinkText(ContentContext ctx) {
		try {
			return getLabel(ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public String getLinkURL(ContentContext ctx) {
		return properties.getProperty(LINK_KEY, "");
	}

	public String getReverseLinkName() {
		return getId() + ID_SEPARATOR + "reverse-lnk";
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		return getStyleList(ctx);
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { STYLE_NORMAL, STYLE_PRIORITY, STYLE_UNVISIBLE };
	}

	public String getTitle(ContentContext ctx) throws IOException {
		return getViewData(ctx).getProperty(TITLE_KEY, "");
	}

	protected String getTitleInputName() {
		return "title" + ID_SEPARATOR + getId();
	}

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		try {

			loadData(ctx);

			String link = properties.getProperty(LINK_KEY, "");
			Collection<String> pageURLList = (Collection<String>) ctx.getRequest().getAttribute("page-url-list");
			if (link.trim().length() > 0) {
				if (pageURLList == null) {
					pageURLList = new TreeSet<String>();
					ctx.getRequest().setAttribute("page-url-list", pageURLList);
				}
				if (pageURLList.contains(link)) {
					logger.info("must be deleted because url found in request.");
					setMustBeRemoved(ctx, true);
				}
			}

			if (mustBeRemoved(ctx) || isURLinRequest(ctx, getLinkURL(ctx))) {
				MenuElement elem = ctx.getCurrentPage();
				// elem.removeContent(ctx, getId(), false);
				elem.addCompToDelete(getId());
				logger.warning("delete component url : " + getLinkURL(ctx));
				return "";
			}

			Collection<Long> crc32List = (Collection<Long>) ctx.getRequest().getAttribute("crc-32-list");
			if (crc32List == null) {
				crc32List = new TreeSet<Long>();
				ctx.getRequest().setAttribute("crc-32-list", crc32List);
			}

			Long crc32 = new Long(getImageCRC32(ctx));
			if (crc32 >= 0) {
				if (crc32List.contains(crc32)) {
					return "";
				} else {
					crc32List.add(crc32);
				}
			}

			/** * refresh smart info ** */
			StringBuffer res = new StringBuffer();

			if (isDisplayable(ctx)) {

				if (!pageURLList.contains(link)) {
					pageURLList.add(link);
				}

				String cssClass = getStyle(ctx);
				String insertCssClass = "";
				if (cssClass != null) {
					insertCssClass = cssClass;
				}
				res.append("<div " + getSpecialPreviewCssClass(ctx, getStyle(ctx) + " " + getType()+" thumbnail") + getSpecialPreviewCssId(ctx) + " >");
				res.append("<a" + getSpecialPreviewCssClass(ctx, insertCssClass) + getSpecialPreviewCssId(ctx) + " href=\" ");
				res.append(link);
				res.append("\">");
				if (getImageURI(ctx) != null) {
					res.append("<span class=\"image\">");
					String imageURL = URLHelper.createTransformURL(ctx, getImageURI(ctx), "extern");
					String jsImage = "<img class=\"img-responsive lazy\" src=\""+InfoBean.getCurrentInfoBean(ctx).getViewAjaxLoaderURL()+"\" data-src=\"" + imageURL + "\" alt=\"" +getTitle(ctx).replace('\'', ' ') + "\" />";
					String noJsImage = "<img class=\"img-responsive\" src=\"" + imageURL + "\" alt=\"" + getTitle(ctx).replace('\'', ' ') + "\" />";
					res.append("<noscript>"+noJsImage+"</noscript>");
					res.append("<script>document.write('"+jsImage+"');</script>");
					res.append("</span>");
				}
				res.append("<div class=\"caption\"><h3>"+getTitle(ctx)+"</h3></div>");								
				res.append("</a></div>");
			} else {
				refreshAutoInfo(ctx);
			}

			return res.toString();

		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}

	}

	@Override
	public int getWordCount(ContentContext ctx) {
		String value;
		try {
			value = getLabel(ctx);
		} catch (IOException e) {
			return 0;
		}
		if (value != null) {
			return value.split(" ").length;
		}
		return 0;
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {

		dataLoaded = false;

		/* set default cookies manager */
		// CookieHandler.setDefault(new CookieManager());
		// super.init(bean, newContext);
		super.init();

		setComponentBean(bean);

		/* check if the content of db is correct version */
		if (getValue().trim().length() > 0) {
			reloadProperties();
		} else {
			properties.setProperty(LINK_KEY, "");
			properties.setProperty(IMAGE_LINK_KEY, "");
			properties.setProperty(REVERSE_LINK_KEY, "false");
		}

		loadViewData(newContext);
	}

	protected boolean isContentValid(ContentContext ctx, String pageContent) {
		List<URL> externalLink = NetHelper.getLinks(pageContent, getLinkURL(ctx));
		int linkToPictureCount = 0;
		for (URL url : externalLink) {
			if (StringHelper.isImage(url.getFile())) {
				linkToPictureCount = linkToPictureCount + 1;
			}
		}
		return linkToPictureCount > 10;
	}

	public boolean isDisplayable(ContentContext ctx) throws IOException {
		Boolean valid = isValidConnection(ctx);

		if (getStyle(ctx) != null && getStyle(ctx).equals(STYLE_UNVISIBLE)) {
			valid = false;
		}

		if (valid == null) {
			return false;
		} else {
			if (valid) {
				return StringHelper.isTrue(getViewData(ctx).getProperty(VALID_LINK, "false"));
			}
			return valid;
		}
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		try {
			loadData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (getStyle(ctx) != null && getStyle(ctx).equals(STYLE_UNVISIBLE)) {
			return false;
		}
		try {
			return (getImageURI(ctx) != null);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isInline() {
		return true;
	}

	@Override
	public boolean isListable() {
		return true;
	}

	@Override
	public boolean isOnlyFirstOccurrence() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public boolean isOnlyPreviousComponent() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_PREVIOUS_COMPONENT);
	}	

	@Override
	public boolean isOnlyThisPage() {
		return properties.getProperty(REVERSE_LINK_KEY, "none").equals(ReverseLinkService.ONLY_THIS_PAGE);
	}

	@Override
	public boolean isReverseLink() {
		return StringHelper.isTrue(properties.getProperty(REVERSE_LINK_KEY, "false"));
	}

	public boolean isURLinRequest(ContentContext ctx, String url) {
		return ctx.getRequest().getAttribute(getType() + url) != null;
	}

	public Boolean isValidConnection(ContentContext ctx) throws IOException {
		String stringValue = getViewData(ctx).getProperty(VALID_CONNECTION_KEY, null);
		if (stringValue == null) {
			return null;
		}
		return new Boolean(StringHelper.isTrue(stringValue));
	}

	protected void loadData(ContentContext ctx) throws IOException {
		if (!dataLoaded) {
			loadViewData(ctx);
			dataLoaded = true;
		}
	}

	public boolean mustBeRemoved(ContentContext ctx) throws IOException {
		return StringHelper.isTrue(getViewData(ctx).getProperty("mustBeRemoved", "false"));
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {

		loadData(ctx);

		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String imageLink = requestService.getParameter(getImageLink(), null);
		String link = requestService.getParameter(getLinkName(), "");
		String title = requestService.getParameter(getTitleInputName(), null);
		String description = requestService.getParameter(getDescriptionInputName(), null);

		String currentLink = properties.getProperty(LINK_KEY, "");
		String currentImageLink = properties.getProperty(IMAGE_LINK_KEY, "");
		
		boolean reverseLinkName = requestService.getParameter(getReverseLinkName(), null) != null;
		if (imageLink != null) {
			if (link != null) {
				if (!title.equals(getTitle(ctx))) {
					setTitle(ctx, title);
					storeViewData(ctx);
					setModify();
				}
				if (!description.equals(getLinkDescription(ctx))) {
					setDescription(ctx, description);
					storeViewData(ctx);
					setModify();
				}
				if (!imageLink.equals(currentImageLink) || !link.equals(currentLink)) {
					deleteImage(ctx);
					removeValidConnection(ctx);
					getViewData(ctx).setProperty(VALID_LINK, "false");
					getViewData(ctx).remove("thread-start");
					storeViewData(ctx);
					setModify();
					// modification
					properties.setProperty(LINK_KEY, link);
					properties.setProperty(IMAGE_LINK_KEY, imageLink);
					if (isReverseLink() != reverseLinkName) {
						properties.setProperty(REVERSE_LINK_KEY, "" + reverseLinkName);
						GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
						ReverseLinkService reverlinkService = ReverseLinkService.getInstance(globalContext);
						reverlinkService.clearCache();
					}
				} else if (!imageLink.equals(currentImageLink)) {
					properties.setProperty(IMAGE_LINK_KEY, imageLink);
					setModify();
				}
			}
		}
		storeProperties();
		return null;
	}

	protected void refreshAutoInfo(ContentContext ctx) throws Exception {
		synchronized (properties) {
			if (getViewData(ctx).getProperty("thread-start") == null) {
				StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
				UndateInfo updateInfo = (UndateInfo) AbstractThread.createInstance(staticConfig.getThreadFolder(), UndateInfo.class);
				updateInfo.setCacheFolder(staticConfig.getCacheFolder());
				updateInfo.setDataFolder(GlobalContext.getInstance(ctx.getRequest()).getDataFolder());
				updateInfo.setURL(getLinkURL(ctx));
				updateInfo.setPropFile(getViewDataFile(ctx));

				String imageLink = properties.getProperty(IMAGE_LINK_KEY, "");
				if (imageLink.trim().length() > 0) {
					updateInfo.setForceImageURL(imageLink);
				}

				updateInfo.store();
				getViewData(ctx).setProperty("thread-start", "true");
				storeViewData(ctx);

			} else {
				logger.info("thread allready found for : " + getId());
				loadViewData(ctx);
			}
		}
	}

	public void removeValidConnection(ContentContext ctx) throws IOException {
		getViewData(ctx).remove(VALID_CONNECTION_KEY);
	}

	public void setDescription(ContentContext ctx, String desc) throws IOException {
		if (desc != null) {
			getViewData(ctx).setProperty(DESCRIPTION_KEY, desc);
		}
	}

	public void setImageCRC32(ContentContext ctx, long mustBeRemoved) throws IOException {
		getViewData(ctx).setProperty("crc32", "" + mustBeRemoved);
	}

	public void setImageURI(ContentContext ctx, String uri) throws IOException {
		getViewData(ctx).setProperty(IMAGE_URI_KEY, uri);
	}

	public void setLinkValid(ContentContext ctx, boolean visible) throws IOException {
		getViewData(ctx).setProperty(VALID_LINK, "" + visible);
	}

	public void setMustBeRemoved(ContentContext ctx, boolean mustBeRemoved) throws IOException {
		getViewData(ctx).setProperty("mustBeRemoved", "" + mustBeRemoved);
	}

	public void setTitle(ContentContext ctx, String title) throws IOException {
		if (title != null) {
			getViewData(ctx).setProperty(TITLE_KEY, title);
		}
	}

	public void setValidConnection(ContentContext ctx, boolean isValid) throws IOException {

	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		return null;
	}
	
	@Override
	public int getPriority(ContentContext ctx) {
		if (getConfig(ctx).getProperty("image.priority", null) == null) {
			return 4;
		} else {
			return Integer.parseInt(getConfig(ctx).getProperty("image.priority", null));
		}
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	

}
