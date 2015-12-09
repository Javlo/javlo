/*
 * Created on 9 oct. 2003
 */
package org.javlo.component.core;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.javlo.cache.ICache;
import org.javlo.component.config.ComponentConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.ConfigHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.ServletHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.helper.Comparator.StringSizeComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.servlet.IVersion;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.DebugListening;
import org.javlo.utils.SuffixPrefix;

/**
 * This class is the first class for component.
 * <h4>exposed variables :</h4>
 * <ul>
 * <li>{@link String} compid : the id of the components. See {@link #getId()}
 * </li>
 * <li>{@link String} compPage : a page bean of the page contain's the
 * component.</li>
 * <li>{@link String} value : the raw value of the component. See
 * {@link #getValue()}</li>
 * <li>{@link String} type : the component type. See {@link #getType()}</li>
 * <li>{@link String} layout : the layout of the component is css (can be null).
 * </li>
 * <li>{@link STring} componentWidth : the width of component forced by
 * contributor (if component manage it)
 * <li>{@link String} style : the style selected for the component. See
 * <li>{@link String} previewAttributes : a string with attribute for preview
 * edition (class and data attribute).</li>
 * <li>{@link #getStyle(ContentContext)}</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 */
public abstract class AbstractVisualComponent implements IContentVisualComponent {

	public static final String SCROLL_TO_COMP_ID_ATTRIBUTE_NAME = "_new_id";

	public static final String NOT_EDIT_PREVIEW_PARAM_NAME = "_not_edit_preview";

	public static final String CACHE_KEY_SUFFIX_PARAM_NAME = "_cache_key_suffix";

	public static Logger logger = Logger.getLogger(AbstractVisualComponent.class.getName());

	public static final String COMPONENT_KEY = "wcms_component";

	public static final String I18N_FILE = "component_[lg].properties";

	private static final String CACHE_NAME = "component";

	public static final String TIME_CACHE_NAME = "component-time";

	private static final String TIME_KEY_PREFIX = "_TIME_CRT_";

	protected static final String VALUE_SEPARATOR = "-";

	public static final String HIDDEN = "hidden";

	public static final String FORCE_COMPONENT_ID = "___FORCE_COMPONENT_ID";

	private final Map<String, Properties> i18nView = new HashMap<String, Properties>();

	private ComponentBean componentBean = new ComponentBean();

	private GenericMessage msg;

	private IContentVisualComponent nextComponent = null;

	private IContentVisualComponent previousComponent = null;

	private boolean needRefresh = false;

	private boolean visible = false;

	private boolean hidden = false;

	private final Map<String, String> replacement = new HashMap<String, String>();

	private Properties viewData = null;

	private MenuElement page = null;

	protected ComponentConfig config = null;

	private String configTemplate = null;

	public static final String getComponentId(HttpServletRequest request) {
		return (String) request.getAttribute(COMP_ID_REQUEST_PARAM);
	}

	/**
	 * get a component in the request if there are.
	 * 
	 * @param request
	 *            the HTTP request
	 * @return a IContentVisualComponent, null if there are no composant in the
	 *         request
	 */
	public static final IContentVisualComponent getRequestComponent(HttpServletRequest request) {
		IContentVisualComponent res = (IContentVisualComponent) request.getAttribute(COMPONENT_KEY);
		return res;
	}

	protected void deleteMySelf(ContentContext ctx) throws Exception {
		MenuElement elem = getPage();
		elem.removeContent(ctx, getId());
	}

	protected String applyReplacement(String content) {
		Map<String, String> remp = getRemplacement();
		if (remp.size() > 0) {
			Collection<String> keys = remp.keySet();
			java.util.List<String> list = new LinkedList<String>();
			list.addAll(keys);
			Collections.sort(list, new StringSizeComparator());
			for (String key : keys) {
				content = StringUtils.replace(content, key, remp.get(key));
			}
		}
		return content;
	}

	@Override
	public void clearReplacement() {
		replacement.clear();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		try {
			return this.getClass().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			throw new CloneNotSupportedException(e.getMessage());
		}
	}

	protected int countLine() {
		StringReader strReader = new StringReader(getValue());
		BufferedReader bufReader = new BufferedReader(strReader);
		int countLine = 0;
		try {
			while (bufReader.readLine() != null) {
				countLine++;
			}
		} catch (IOException e) {
			// no error possible
		}
		return countLine;
	}

	@Override
	public void delete(ContentContext ctx) {
		if (getPreviousComponent() != null) {
			getPreviousComponent().setNextComponent(getNextComponent());
		}
		try {
			resetViewData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractVisualComponent)) {
			return false;
		}
		AbstractVisualComponent comp = (AbstractVisualComponent) obj;
		boolean eq = comp.getComponentBean().equals(getComponentBean());
		return eq;
	}

	protected String executeJSP(ContentContext ctx, String jsp) throws ServletException, IOException {
		if (jsp == null) {
			return "jsp null : " + getClass();
		}
		ctx.getRequest().setAttribute(COMPONENT_KEY, this);
		String url = jsp;
		if (!url.startsWith("/")) {
			url = URLHelper.createJSPComponentURL(ctx.getRequest(), jsp, getComponentPath());
		}
		logger.fine("execute jsp in '" + getType() + "' : " + url);
		return ServletHelper.executeJSP(ctx, url);
	}

	protected String executeRenderer(ContentContext ctx) throws ServletException, IOException {
		String renderer = getRenderer(ctx);
		if (renderer == null) {
			return "";
		} else {
			return executeJSP(ctx, getRenderer(ctx));
		}
	}

	@Override
	public String getArea() {
		if (componentBean.getArea() == null) {
			return "content"; // default value, needed for old DC web site
			// work correctly
		}
		return componentBean.getArea();
	}

	protected String getBaseHelpURL(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String helpURL = globalContext.getHelpURL();
		if(helpURL.contains("${language}")) {
			helpURL = helpURL.replace("${language}", globalContext.getEditLanguage(ctx.getRequest().getSession()));
		} else {
			helpURL = URLHelper.mergePath(helpURL +'v'+IVersion.VERSION.substring(0,3).replace('.', '_'), globalContext.getEditLanguage(ctx.getRequest().getSession()));
		}
		return helpURL;
	}
	
	@Override
	public boolean isHelpURL(ContentContext ctx) {
		return !StringHelper.isEmpty(ctx.getGlobalContext().getHelpURL()) && !StringHelper.isEmpty(getHelpURI(ctx));
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_EASY);
	}

	@Override
	public ComponentBean getComponentBean() {
		return componentBean;
	}

	protected String getComponentCSS(ServletContext application, String css) throws ServletException, IOException {
		InputStream stream = ResourceHelper.getStaticComponentResource(application, getComponentPath(), css);

		try {
			StringWriter strWriter = new StringWriter();
			PrintWriter out = new PrintWriter(strWriter);

			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
			String line = reader.readLine();
			while (line != null) {
				out.println(line);
				line = reader.readLine();
			}

			out.close();
			return strWriter.toString();
		} finally {
			ResourceHelper.closeResource(stream);
		}
	}

	@Override
	public String getComponentLabel(ContentContext ctx, String lg) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = null;
		try {
			i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());
			return i18nAccess.getText("content." + getType(), getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * the the localisation of the JSP files in the "component" directory in
	 * webapps. normaly this localisation is the name of the component direcoty
	 * in the src.
	 * 
	 * @return a part of a path
	 */
	protected String getComponentPath() {
		return getType();
	}

	@Override
	public ComponentConfig getConfig(ContentContext ctx) {
		if (config != null) {
			try {
				if (configTemplate != null && ctx.getCurrentTemplate() != null) {
					if (configTemplate.equals(ctx.getCurrentTemplate().getName())) {
						return config;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if ((ctx == null) || (ctx.getRequest() == null) || ((ctx.getRequest().getSession() == null))) {
			return ComponentConfig.getInstance();
		}

		ComponentConfig outConfig = ComponentConfig.getInstance(ctx, getType());
		if (ctx.isAsViewMode() && outConfig != ComponentConfig.EMPTY_INSTANCE) {
			try {
				configTemplate = ctx.getCurrentTemplate().getName();
				config = outConfig;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outConfig;
	}

	public String getContentTimeCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache cache = globalContext.getCache(TIME_CACHE_NAME);

		String contentKey = getContentCacheKey(ctx);
		String timeKey = TIME_KEY_PREFIX + contentKey;
		Long creationTime = (Long) cache.get(timeKey);
		if (creationTime != null) {
			Calendar currentTime = Calendar.getInstance();
			Calendar creationCal = Calendar.getInstance();
			creationCal.setTimeInMillis(creationTime);
			currentTime.add(Calendar.SECOND, -getTimeCache(ctx));
			if (currentTime.after(creationCal)) {
				cache.removeItem(contentKey);
				cache.removeItem(timeKey);
				return null;
			}
		}
		return (String) cache.get(contentKey);
	}

	private String getContentCacheKey(ContentContext ctx) {
		String templateId = "?";
		String pageId = "?";
		try {
			if (ctx.getCurrentTemplate() != null) {
				templateId = ctx.getCurrentTemplate().getId();
			}
			pageId = ctx.getCurrentPage().getId();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String keySuffix = ctx.getGlobalContext().getContextKey() + '-' + ctx.getLanguage() + '-' + ctx.getRequestContentLanguage() + '-' + ctx.getRenderMode() + '-' + templateId + '-' + pageId;
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (requestService.getParameter(CACHE_KEY_SUFFIX_PARAM_NAME, null) != null) {
			keySuffix = keySuffix + '-' + requestService.getParameter(CACHE_KEY_SUFFIX_PARAM_NAME, null);
		}

		if (isContentCachableByQuery(ctx)) {
			keySuffix = keySuffix + '_' + ctx.getRequest().getQueryString();
		}

		if (ctx.getDevice() == null) { // TODO: check why this method can return
										// "null"
			return Device.DEFAULT_DEVICE + '_' + getId() + keySuffix;
		}

		return ctx.getDevice().getCode() + '_' + getId() + keySuffix;
	}

	/**
	 * get the XHTML input field name for the content
	 * 
	 * @return a XHTML input field name.
	 */
	@Override
	public String getContentName() {
		return "data__" + getId();
	}

	public String getContentCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache cache = globalContext.getCache(CACHE_NAME);
		String contentKey = getContentCacheKey(ctx);
		return (String) cache.get(contentKey);
	}

	/**
	 * get the current page
	 * 
	 * @param componentPage
	 *            if true return the page of the component, if false return the
	 *            current page (in case of repeat component)
	 * @return a page.
	 * @throws Exception
	 */
	public MenuElement getCurrentPage(ContentContext ctx, boolean componentPage) throws Exception {
		if (componentPage) {
			return getPage();
		} else {
			return ctx.getCurrentPage();
		}
	}

	/**
	 * get current renderer key
	 */
	@Override
	public String getCurrentRenderer(ContentContext ctx) {
		if (componentBean.getRenderer() == null && getRenderes(ctx).size() > 0) {
			String defaultRenderer = getConfig(ctx).getDefaultRenderer();
			if (defaultRenderer != null) {
				return defaultRenderer;
			} else {
				return getRenderes(ctx).keySet().iterator().next();
			}
		} else {
			return componentBean.getRenderer();
		}
	}

	public String getDebugHeader(ContentContext ctx) {
		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (StringHelper.isTrue(requestService.getParameter("_debug", "false"))) {
			out.println("<div class=\"debug-header\"><dl>");
			out.println("<dt>id</dt>");
			out.println("<dd>" + getId() + "</dd>");
			out.println("</dl></div>");
			out.close();
		}
		return writer.toString();
	}

	/**
	 * @param context
	 */
	// public void setServletContext(ServletContext context) {
	// servletContext = context;
	// }

	@Override
	public String getEditText(ContentContext ctx, String key) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String text = i18nAccess.getComponentText(getComponentPath(), key);
			if (text == null) {
				text = "TEXT [" + key + "] NOT FOUND.";
			}
			return text;
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"form-control resizable-textarea full-width\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (countLine() + 1) + "\">");
		finalCode.append(getValue());
		finalCode.append("</textarea>");
		return finalCode.toString();
	}

	public boolean isAskWidth(ContentContext ctx) {
		return false;
	}

	public String getWidth() {
		return null;
	}

	public void setWidth(String width) {
	}

	@Override
	public String getXHTMLConfig(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String repeatHidden = "";
		boolean showRepeat = true;
		if (ctx.getGlobalContext().isMailingPlatform()) {
			UserInterfaceContext uiContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
			if (uiContext.isLight()) {
				repeatHidden = " hidden";
				showRepeat = false;
			}
		}

		if (isRepeatable()) {
			out.println("<div class=\"line" + repeatHidden + "\">");
			if (showRepeat) {
				out.println("<label for=\"repeat-" + getId() + "\">" + i18nAccess.getText("content.repeat") + "</label>");
			}
			out.println(XHTMLHelper.getCheckbox("repeat-" + getId(), isRepeat()));
			out.println("</div>");
		}
		
		if (isNoLinkable()) {
			out.println("<div class=\"line\">");
			if (showRepeat) {
				out.println("<label for=\"nolink-" + getId() + "\">" + i18nAccess.getText("content.nolink") + "</label>");
			}
			out.println(XHTMLHelper.getCheckbox("nolink-" + getId(), isNolink()));
			out.println("</div>");
		}

		if (isListable()) {
			out.println("<div class=\"line\">");
			out.println("<label for=\"inlist-" + getId() + "\">" + i18nAccess.getText("component.inlist") + "</label>");
			out.println(XHTMLHelper.getCheckbox("inlist-" + getId(), isList(ctx)));
			out.println("</div>");
		}
		if (isAskWidth(ctx)) {
			out.println("<div class=\"line\">");
			String inputName = getInputName("width");
			out.println("<label for=\"" + inputName + "\">" + i18nAccess.getText("component.width") + "</label>");
			out.println("<input id=\"" + inputName + "\" name=\"" + inputName + "\" class=\"form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getWidth()) + "\" />");
			out.println("</div>");
		}
		if (getLayout() != null) {
			ComponentLayout layout = getLayout();
			out.println("<div class=\"line layout\">");
			out.println("<label>" + i18nAccess.getText("component.layout") + "</label>");
			String id = "layout-default-" + getId();
			String name = "layout-align-" + getId();
			out.println("<label for=\"" + id + "\">");
			out.println(XHTMLHelper.getRadio(id, name, "default", !layout.isLeft() && !layout.isRight() && !layout.isCenter()));
			out.println(i18nAccess.getText("global.default"));
			out.println("</label>");
			id = "layout-left-" + getId();
			out.println("<label for=\"" + id + "\">");
			out.println(XHTMLHelper.getRadio(id, name, "left", layout.isLeft()));
			out.println(i18nAccess.getText("component.layout.left"));
			out.println("</label>");
			id = "layout-center-" + getId();
			out.println("<label for=\"" + id + "\">");
			out.println(XHTMLHelper.getRadio(id, name, "center", layout.isCenter()));
			out.println(i18nAccess.getText("component.layout.center"));
			out.println("</label>");
			id = "layout-right-" + getId();
			out.println("<label for=\"" + id + "\">");
			out.println(XHTMLHelper.getRadio(id, name, "right", layout.isRight()));
			out.println(i18nAccess.getText("component.layout.right"));
			out.println("</label>");

			if (getConfig(ctx).isFontStyle()) {
				id = "layout-bold-" + getId();
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, layout.isBold()));
				out.println(i18nAccess.getText("component.layout.bold", "bold"));
				out.println("</label>");
				id = "layout-italic-" + getId();
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, layout.isItalic()));
				out.println(i18nAccess.getText("component.layout.italic", "italic"));
				out.println("</label>");
			}
			out.println("</div>");
		}
		if (getConfig(ctx).isChooseBackgoundColor()) {
			out.println("<div class=\"line\">");
			String bgColInputName = "bgcol-" + getId();
			out.println("<label for=\"" + bgColInputName + "\">" + i18nAccess.getText("component.background-color") + "</label>");
			out.println("<input id=\"" + bgColInputName + "\" name=\"" + bgColInputName + "\" class=\"color form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getBackgroundColor()) + "\" />");
			out.println("</div>");
		}

		if (getConfig(ctx).isChooseTextColor()) {
			out.println("<div class=\"line\">");
			String textColInputName = "textcol-" + getId();
			out.println("<label for=\"" + textColInputName + "\">" + i18nAccess.getText("component.text-color") + "</label>");
			out.println("<input id=\"" + textColInputName + "\" name=\"" + textColInputName + "\" class=\"color form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getTextColor()) + "\" />");
			out.println("</div>");
		}

		String[] styles = getStyleList(ctx);
		if (styles.length > 1) {
			String[] stylesLabel = getStyleLabelList(ctx);
			if (styles.length != stylesLabel.length) {
				throw new ComponentException("size of styles is'nt the same than size of styles label.");
			}
			out.println("<div class=\"line\">");
			out.println("<label for=\"style-" + getId() + "\">" + getStyleTitle(ctx) + "</label>");
			out.println(XHTMLHelper.getInputOneSelect("style-" + getId(), styles, stylesLabel, getStyle(), "form-control", null, false));
			out.println("</div>");
		}

		if (getRenderes(ctx) == null || getRenderes(ctx).size() > 1) {
			out.println(getSelectRendererXHTML(ctx, isAutoRenderer()));
		}

		if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			out.println("<div class=\"line\">");
			out.println("<label>" + i18nAccess.getText("component.display-modes") + "</label>");
			for (int mode : new int[] { ContentContext.VIEW_MODE, ContentContext.PREVIEW_MODE, ContentContext.PAGE_MODE, ContentContext.TIME_MODE }) {
				String id = "display-mode-" + mode + "-" + getId();
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, !isHiddenInMode(mode)));
				out.println(ContentContext.getRenderModeKey(mode));
				out.println("</label>");
			}
			out.println("</div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	public boolean isConfig(ContentContext ctx) {
		try {
			return StringHelper.removeTag(getXHTMLConfig(ctx)).trim().length() > 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String performConfig(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		boolean isRepeat = requestService.getParameter("repeat-" + getId(), null) != null;
		if (isRepeat != isRepeat()) {
			setRepeat(isRepeat);
			setModify();
			setNeedRefresh(true);
		}
		
		boolean isNolink = requestService.getParameter("nolink-" + getId(), null) != null;
		if (isNolink != isNolink()) {
			setNolink(isNolink);
			setModify();
			setNeedRefresh(true);
		}

		if (isAskWidth(ctx)) {
			setWidth(requestService.getParameter(getInputName("width"), ""));
		}

		boolean isList = requestService.getParameter("inlist-" + getId(), null) != null;
		if (isListable() && isList != isList(ctx)) {
			setList(isList);
			setModify();
			setNeedRefresh(true);
		}

		String bgCol = requestService.getParameter("bgcol-" + getId(), null);
		if (bgCol != null && !bgCol.equals(getBackgroundColor())) {
			try {
				Color.getColor(bgCol);
				setBackgroundColor(bgCol);
				setModify();
				setNeedRefresh(true);
			} catch (Exception e) {
				logger.warning(e.getLocalizedMessage());
			}
		}

		String textCol = requestService.getParameter("textcol-" + getId(), null);
		if (textCol != null && !textCol.equals(getTextColor())) {
			try {
				Color.getColor(textCol);
				setTextColor(textCol);
				setModify();
				setNeedRefresh(true);
			} catch (Exception e) {
				logger.warning(e.getLocalizedMessage());
			}
		}

		if (getLayout() != null) {
			ComponentLayout layout = new ComponentLayout("");
			layout.setLeft(requestService.getParameter("layout-align-" + getId(), "").equals("left"));
			layout.setRight(requestService.getParameter("layout-align-" + getId(), "").equals("right"));
			layout.setCenter(requestService.getParameter("layout-align-" + getId(), "").equals("center"));
			layout.setBold(requestService.getParameter("layout-bold-" + getId(), null) != null);
			layout.setItalic(requestService.getParameter("layout-italic-" + getId(), null) != null);
			layout.setLineThrough(requestService.getParameter("layout-linethrough-" + getId(), null) != null);
			layout.setUnderline(requestService.getParameter("layout-underline-" + getId(), null) != null);
			if (!getLayout().getLayout().equals(layout.getLayout())) {
				getComponentBean().setLayout(layout);
				setModify();
			}
		}

		/** renderer **/
		String renderer = requestService.getParameter(getInputNameRenderer(), null);
		if (renderer != null) {
			if (!renderer.equals(getCurrentRenderer(ctx))) {
				setRenderer(ctx, renderer);
				setModify();
				setNeedRefresh(true);
			}
		}

		/** style **/
		String newStyle = requestService.getParameter("style-" + getId(), null);
		if (newStyle != null && !newStyle.equals(getStyle())) {
			setStyle(ctx, newStyle);
			setModify();
			setNeedRefresh(true);

		}

		/** in list **/
		if (getArea().equals(ctx.getArea())) {
			boolean newInlist = requestService.getParameter("inlist-" + getId(), null) != null;
			if (newInlist != isList(ctx)) {
				setList(newInlist);
				setModify();
				setNeedRefresh(true);
			}
		}

		/** display modes **/
		if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			for (int mode : new int[] { ContentContext.VIEW_MODE, ContentContext.PREVIEW_MODE, ContentContext.PAGE_MODE, ContentContext.TIME_MODE }) {
				String id = "display-mode-" + mode + "-" + getId();
				boolean visible = requestService.getParameter(id, null) != null;
				setHiddenInMode(mode, !visible);
			}
		}

		if (isModify()) {
			PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
		}

		return null;
	}

	/*
	 * public String getContentTimeCache(ContentContext ctx) { return
	 * viewTimeCache.get(getContentCacheKey(ctx)); }
	 */

	protected String getEmptyCode(ContentContext ctx) throws Exception {
		if ((ctx.getRenderMode() == ContentContext.PREVIEW_MODE)) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			if (editCtx.isEditPreview()) {
				MenuElement currentPage = ctx.getCurrentPage();
				if (currentPage.equals(getPage())) { // not edit component
					// is repeated and
					// user is not on
					// the definition
					// page
					String prefix = "";
					String suffix = "";
					if (!isWrapped(ctx)) {
						prefix = getForcedPrefixViewXHTMLCode(ctx);
						suffix = getForcedSuffixViewXHTMLCode(ctx);
					}
					return (prefix + "<div " + getSpecialPreviewCssClass(ctx, "pc_empty-component") + getSpecialPreviewCssId(ctx) + ">" + getEmptyXHTMLCode(ctx) + "</div>" + suffix);
				}
			}
		}
		return null;
	}

	@Override
	public boolean isDispayEmptyXHTMLCode(ContentContext ctx) throws Exception {
		return true;
	}

	@Override
	public String getErrorMessage(String fieldName) throws ResourceNotFoundException {
		return "no error message defined.";
	}

	/**
	 * @return
	 */
	// public ServletContext getServletContext() {
	// return servletContext;
	// }

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		return Collections.emptyList();
	}

	@Override
	public String getFirstPrefix(ContentContext ctx) {
		if (!componentBean.isList()) {
			return getConfig(ctx).getProperty("prefix.first", "");
		} else {
			String cssClass = "";
			if (getStyle(ctx) != null && getStyle(ctx).trim().length() > 0) {
				cssClass = ' ' + getStyle(ctx);
			}
			if (getListClass(ctx) != null) {
				cssClass = cssClass + ' ' + getListClass(ctx);
			}
			return "<" + getListTag(ctx) + " class=\"" + getType() + cssClass + "\">";
		}
	}

	protected String getListClass(ContentContext ctx) {
		return getConfig(ctx).getProperty("list.class", null);
	}

	protected String getListTag(ContentContext ctx) {
		return getConfig(ctx).getProperty("list.tag", "ul");
	}

	protected String getListItemTag(ContentContext ctx) {
		return getConfig(ctx).getProperty("list.item.tag", "li");
	}

	public String getFormName() {
		return "content_update";
	}

	@Override
	public String getHeaderContent(ContentContext ctx) {
		return null;
	}

	@Override
	public final String getHelpURL(ContentContext ctx) {
		User user = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
		String lang = "en";
		if (user.getUserInfo().getPreferredLanguage().length > 0) {
			lang = user.getUserInfo().getPreferredLanguage()[0];
		}
		if (getBaseHelpURL(ctx) == null || getBaseHelpURL(ctx).trim().length() == 0) {
			return null;
		}
		String baseURL = getBaseHelpURL(ctx);
		ContentContext lgCtx = new ContentContext(ctx);
		lgCtx.setAllLanguage(lang);
		String url = URLHelper.mergePath(baseURL, getHelpURI(ctx));
		return url;

	}
	
	protected String getHelpType() {
		return getType();
	}
	
	protected String getDefaultHelpURI(ContentContext ctx) {
		return "/components/" + getHelpType() + ".html";
	}

	protected String getHelpURI(ContentContext ctx) {
		return getConfig(ctx).getProperty("help.uri", getDefaultHelpURI(ctx));		
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public List<String> getI18nEditableKeys(ContentContext ctx) {
		return Collections.EMPTY_LIST;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getContentLanguage()
	 */
	// public String getLanguage() {
	// return ctx.getRequestContentLanguage();
	// }
	@Override
	public final String getId() {
		return componentBean.getId();
	}

	protected String getInputName(String field) {
		return field + "-" + getId();
	}

	@Override
	public String getInputNameRenderer() {
		return "_renderer_file_" + getId();
	}

	public String getInputNameRendererTitle() {
		return "_renderer_title_" + getId();
	}

	public List<SuffixPrefix> getItalicAndStrongLanguageMarkerList(ContentContext ctx) {
		List<SuffixPrefix> out = new LinkedList<SuffixPrefix>();

		I18nAccess i18nAccess = null;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		SuffixPrefix sufixPreffix = new SuffixPrefix("<em>", "</em>", i18nAccess.getText("component.marker.italic"));
		out.add(sufixPreffix);
		sufixPreffix = new SuffixPrefix("<strong>", "</strong>", i18nAccess.getText("component.marker.strong"));
		out.add(sufixPreffix);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			Locale locale = new Locale(lg);
			sufixPreffix = new SuffixPrefix("<span lang=\"" + locale.getLanguage() + "\">", "</span>", locale.getDisplayLanguage(new Locale(ctx.getRequestContentLanguage())));
			out.add(sufixPreffix);
		}
		return out;
	}

	@Override
	public String getJSOnSubmit() {
		return "";
	}

	@Override
	public String getKey() {
		return getClass().getName();
	}

	@Override
	public String getLastSufix(ContentContext ctx) {
		if (previousComponent != null) {
			if (((AbstractVisualComponent) previousComponent).componentBean.isList() && previousComponent.getType().equals(getType())) {
				return "</" + getListTag(ctx) + ">";
			}
		}
		if (componentBean.isList()) {
			return "</" + getListTag(ctx) + ">";
		}
		return getConfig(ctx).getProperty("suffix.last", "");
	}

	@Override
	public List<SuffixPrefix> getMarkerList(ContentContext ctx) {
		return null;
	}

	@Override
	public GenericMessage getMessage() {
		return msg;
	}

	@Override
	public IContentVisualComponent getNextComponent() {
		return nextComponent;
	}

	@Override
	public MenuElement getPage() {
		return page;
	}

	protected String getTag(ContentContext ctx) {
		return getConfig(ctx).getProperty("tag", "div");
	}

	protected boolean isWrapped(ContentContext ctx) {
		if (isList(ctx)) {
			return StringHelper.isTrue(getConfig(ctx).getProperty("list-wrapped", null), true);
		} else {
			return StringHelper.isTrue(getConfig(ctx).getProperty("wrapped", null), true);
		}
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		if (isWrapped(ctx)) {
			return getForcedPrefixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	protected String getForcedPrefixViewXHTMLCode(ContentContext ctx) {
		if (getConfig(ctx).getProperty("prefix", null) != null) {
			return getConfig(ctx).getProperty("prefix", null);
		}
		String style = getStyle(ctx);
		if (style != null) {
			style = style + ' ';
		} else {
			style = "";
		}
		if (isBackgroundColored()) {
			style = style + " colored-wrapper";
		}
		if (getPreviousComponent() == null || !getPreviousComponent().getType().equals(getType())) {
			style = style + " first ";
		}
		if (getPreviousComponent() == null) {
			style = style + " first-component ";
		}
		if (getNextComponent() == null || !getNextComponent().getType().equals(getType())) {
			style = style + " last ";
		}
		if (!componentBean.isList()) {
			return "<" + getTag(ctx) + " " + getSpecialPreviewCssClass(ctx, style + getType()) + getSpecialPreviewCssId(ctx) + " " + getInlineStyle(ctx) + ">";
		} else {
			return "<" + getListItemTag(ctx) + getSpecialPreviewCssClass(ctx, style + getType()) + getSpecialPreviewCssId(ctx) + " >";
		}
	}

	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			inlineStyle = " overflow: hidden; background-color: " + getBackgroundColor() + ';';
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}

		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}

	@Override
	public IContentVisualComponent getPreviousComponent() {
		return previousComponent;
	}

	public List<SuffixPrefix> getQuotationLanguageMarkerList(ContentContext ctx) {
		List<SuffixPrefix> out = new LinkedList<SuffixPrefix>();

		I18nAccess i18nAccess = null;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		SuffixPrefix sufixPreffix = new SuffixPrefix("<q>", "</q>", i18nAccess.getText("component.marker.quotation"));
		out.add(sufixPreffix);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			Locale locale = new Locale(lg);
			sufixPreffix = new SuffixPrefix("<span lang=\"" + locale.getLanguage() + "\">", "</span>", locale.getDisplayLanguage(new Locale(ctx.getRequestContentLanguage())));
			out.add(sufixPreffix);
		}
		return out;
	}

	protected Map<String, String> getRemplacement() {
		return replacement;
	}

	public String getDefaultRenderer(ContentContext ctx) {
		return null;
	}

	public Map<String, String> getRenderes(ContentContext ctx) {
		return getConfig(ctx).getRenderes();
	}

	/**
	 * return true if end user could not select the renderer.
	 * 
	 * @return
	 */
	protected boolean isAutoRenderer() {
		return false;
	}

	/**
	 * get current renderer file.
	 */
	@Override
	public String getRenderer(ContentContext ctx) {
		String renderer;
		String currentRenderer = getCurrentRenderer(ctx);
		Map<String, String> renderers = getRenderes(ctx);
		if (renderers == null || renderers.size() == 0 && currentRenderer == null) {
			renderer = getDefaultRenderer(ctx);
		} else if (renderers.size() == 1 || currentRenderer == null) {
			renderer = renderers.values().iterator().next();
		} else {
			renderer = renderers.get(currentRenderer + '.' + ctx.getArea());
			if (renderer == null) {
				renderer = renderers.get(currentRenderer);
			}
			if (renderer == null && renderers.size() > 0) {
				renderer = renderers.values().iterator().next();
			}
		}
		try {
			if (ctx.getCurrentTemplate() != null && renderer != null) {
				String workTemplateFolder = ctx.getCurrentTemplate().getWorkTemplateFolder();
				if (!renderer.startsWith('/' + workTemplateFolder)) {
					ContentContext notAbsCtx = new ContentContext(ctx);
					notAbsCtx.setAbsoluteURL(false);
					renderer = URLHelper.createStaticTemplateURLWithoutContext(notAbsCtx, ctx.getCurrentTemplate(), renderer);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return renderer;
	}

	protected String getRendererTitle() {
		return null;
	}

	@Override
	public int getSearchLevel() {
		return SEARCH_LEVEL_LOW;
	}

	protected String getSelectRendererXHTML(ContentContext ctx, boolean justDisplay) throws Exception, IOException {
		if (getCurrentRenderer(ctx) == null && (getRenderer(ctx) == null || getRenderer(ctx).length() <= 1)) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

		if (getRenderes(ctx).size() > 1) {

			out.println("<fieldset class=\"display\">");
			out.println("<legend>" + i18nAccess.getText("content.page-teaser.display-type") + "</legend><div class=\"line\">");

			if (getRendererTitle() != null) { /*
												 * for use title you must
												 * implement method
												 * getRendererTitle and return
												 * empty string if empty and not
												 * null (default value).
												 */
				out.println("<div class=\"line\">");
				out.println("<label for=\"" + getInputNameRendererTitle() + "\">" + i18nAccess.getText("global.title") + " : </label>");
				out.println("<input type=\"text\" id=\"" + getInputNameRendererTitle() + "\" name=\"" + getInputNameRendererTitle() + "\" value=\"" + getRendererTitle() + "\"  />");
				out.println("</div>");
			}

			if (justDisplay) {
				out.println("<p>"+getCurrentRenderer(ctx)+"</p>");
			} else {
				out.println("<div class=\"line\">");
				/* display as slide show */

				Map<String, String> renderers = getRenderes(ctx);
				List<String> keys = new LinkedList<String>(renderers.keySet());
				Collections.sort(keys);
				for (String key : keys) {
					if (!key.contains(".")) {
						out.println(XHTMLHelper.getRadio(getInputNameRenderer(), key, getCurrentRenderer(ctx)));
						out.println("<label for=\"" + key + "\">" + key + "</label></div><div class=\"line\">");
					}
				}
			}

			out.println("</fieldset>");
		} else {
			out.println("<div class=\"line\"><span class=\"label\">" + i18nAccess.getText("content.page-teaser.display-type") + " : </span><span class=\"value\">" + getCurrentRenderer(ctx) + "</span></div>");
		}

		out.close();
		return new String(outStream.toByteArray());

	}

	/**
	 * create technical input tag. sample : for know the type of component with
	 * only the <code>request</code>
	 * 
	 * @return a part of a form in XHTML
	 */
	protected String getSpecialInputTag() {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append("<input type=\"hidden\" name=\"");
		finalCode.append(getTypeInputName());
		finalCode.append("\" value=\"");
		finalCode.append(getType());
		finalCode.append("\"/>");
		return finalCode.toString();
	}

	/**
	 * override this method for add specific class to prefix.
	 * 
	 * @return
	 */
	public String getSpecificClass(ContentContext ctx) {
		return null;
	}

	public String getSpecialPreviewCssClass(ContentContext ctx, String currentClass) {
		if (currentClass == null) {
			currentClass = "";
		} else {
			currentClass = ' ' + currentClass.trim();
		}
		String specificClass = "";
		if (getSpecificClass(ctx) != null) {
			specificClass = getSpecificClass(ctx) + ' ';
		}
		if (getId().equals(ctx.getRequest().getAttribute(SCROLL_TO_COMP_ID_ATTRIBUTE_NAME))) {
			specificClass = specificClass + "scroll-to-me ";
		}
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			try {
				String classPrefix = "not-";
				if (!globalContext.isOnlyCreatorModify() || (ctx.getCurrentEditUser() != null && (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || getAuthors().equals(ctx.getCurrentEditUser().getLogin())))) {
					if (!AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser(), AdminUserSecurity.LIGHT_INTERFACE_ROLE) || getComplexityLevel(ctx) == IContentVisualComponent.COMPLEXITY_EASY) {
						classPrefix = "";
					}
				}
				RequestService rs = RequestService.getInstance(ctx.getRequest());
				if (!StringHelper.isTrue(rs.getParameter(NOT_EDIT_PREVIEW_PARAM_NAME, null))) {
					if (getConfig(ctx).isPreviewEditable() && editCtx.isEditPreview() && (!isRepeat() || getPage().equals(ctx.getCurrentPage())) && AdminUserSecurity.canModifyPage(ctx, ctx.getCurrentPage(), true)) {
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
						String type = i18nAccess.getText("content." + getType(), getType());
						String hint = "<b>" + type + "</b><br />" + i18nAccess.getViewText("preview.hint", "click for edit or drag and drop to move.");
						String newClass = "";
						if (isNew(ctx)) {
							newClass = " new-component";
							if (isEditOnCreate(ctx)) {
								newClass = newClass+" edit-component";
							}
						}
						return " class=\"" + specificClass + classPrefix + "editable-component" + currentClass + newClass + "\" data-hint=\"" + hint + "\" data-name=\"" + i18nAccess.getText("content." + getType(), getType()) + "\"";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (currentClass != null && currentClass.trim().length() > 0) {
			return " class=\"" + specificClass + currentClass.trim() + "\"";
		}
		return "";
	}

	protected String getForcedId(ContentContext ctx) {
		/* user for mirror mecanism */
		String compID = (String) ctx.getRequest().getAttribute(FORCE_COMPONENT_ID);
		// System.out.println("***** AbstractVisualComponent.getForcedId :
		// area="+getArea()+" type="+getType()+" compID = "+compID);
		// //TODO: remove debug trace
		if (compID == null) {
			compID = getId();
		}
		return compID;
	}

	protected static void setForcedId(ContentContext ctx, String id) {
		if (id == null) {
			/* user for mirror mecanism */
			ctx.getRequest().removeAttribute(FORCE_COMPONENT_ID);
		} else {
			/* user for mirror mecanism */
			ctx.getRequest().setAttribute(FORCE_COMPONENT_ID, id);
		}
	}

	public String getSpecialPreviewCssId(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return " id=\"cp_" + getForcedId(ctx) + "\"";
		} else {
			return "";
		}
	}

	public String getStyle() {
		return componentBean.getStyle();
	}

	@Override
	public final String getStyle(ContentContext ctx) {
		if (componentBean.getStyle() == null) {
			if ((getStyleList(ctx) != null) && (getStyleList(ctx).length > 0)) {
				componentBean.setStyle(getStyleList(ctx)[0]);
			}
		}
		String style = componentBean.getStyle();
		if (style == null) {
			style = "";
		} else {
			style = style + ' ';
		}
		if (isRepeat()) {
			style = style + "repeat";
			try {
				if (getPage() != null && getPage().equals(ctx.getCurrentPage())) {
					style = style + " first-repeat";
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (isColored()) {
			style = style + " colored";
		}
		String currentRenderer = getCurrentRenderer(ctx);
		if (currentRenderer != null) {
			style = style + ' ' + currentRenderer;
		}
		return style.trim();
	}

	public boolean isColored() {
		return (getBackgroundColor() != null && getBackgroundColor().length() > 2) || (getTextColor() != null && getTextColor().length() > 2);
	}

	public boolean isBackgroundColored() {
		return (getBackgroundColor() != null && getBackgroundColor().length() > 2);
	}

	@Override
	public String getStyleLabel(ContentContext ctx) {
		String[] styles = getStyleList(ctx);
		for (int i = 0; i < styles.length; i++) {
			if (styles[i].equals(getStyle())) {
				String style = getStyleLabelList(ctx)[i];
				if (style == null || style.trim().length() == 0) {
					return getStyle();
				} else {
					return style;
				}
			}
		}
		return "";
	}

	@Override
	public void setRenderer(ContentContext ctx, String renderer) {
		componentBean.setRenderer(renderer);
	}

	@Override
	public String[] getStyleLabelList(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String[] styleLabel = getConfig(ctx).getStyleLabelList();
			if (styleLabel.length != getStyleList(ctx).length) {
				return getStyleList(ctx);
			}
			for (int i = 0; i < styleLabel.length; i++) {
				styleLabel[i] = i18nAccess.getText(styleLabel[i], styleLabel[i]);
			}
			return styleLabel;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return getConfig(ctx).getStyleList();
	}

	@Override
	public String getStyleTitle(ContentContext ctx) {
		I18nAccess i18nAccess;
		try {
			i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String styleTitleKey = getConfig(ctx).getStyleTitle();
			if (styleTitleKey == null) {
				return "";
			} else {
				return i18nAccess.getText(styleTitleKey);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		if (isWrapped(ctx)) {
			return getForcedSuffixViewXHTMLCode(ctx);
		} else {
			return "";
		}
	}

	private String getForcedSuffixViewXHTMLCode(ContentContext ctx) {
		if (getConfig(ctx).getProperty("suffix", null) != null) {
			return getConfig(ctx).getProperty("suffix", null);
		}
		if (!componentBean.isList()) {
			return "</" + getTag(ctx) + ">";
		} else {
			return "</" + getListItemTag(ctx) + '>';
		}
	}

	@Override
	public String getTextForSearch(ContentContext ctx) {
		return StringEscapeUtils.unescapeHtml(getValue());
	}

	@Override
	public String getTextLabel() {
		return getValue();
	}

	@Override
	public String getTextTitle(ContentContext ctx) {
		return getValue();
	}

	@Override
	public int getTitleLevel(ContentContext ctx) {
		return 0;
	}

	protected String getTypeInputName() {
		return getId() + ID_SEPARATOR + "type";
	}

	public String getValue() {
		return componentBean.getValue();
	}

	public String getBackgroundColor() {
		return componentBean.getBackgroundColor();
	}

	public String getTextColor() {
		return componentBean.getTextColor();
	}

	@Override
	public String getValue(ContentContext ctx) {
		return getValue();
	}

	public Properties getViewData(ContentContext ctx) throws IOException {
		if (viewData == null) {
			loadViewData(ctx);
		}
		return viewData;
	}

	protected File getViewDataFile(ContentContext ctx) throws IOException {
		return getViewDataFile(ctx, true);
	}

	private File getViewDataFile(ContentContext ctx, boolean createFile) throws IOException {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		String folder = URLHelper.mergePath(globalContext.getDataFolder(), "components_view_data");
		File viewDataFile = new File(URLHelper.mergePath(folder, getId() + ".properties"));
		if (!viewDataFile.exists() && createFile) {
			viewDataFile.getParentFile().mkdirs();
			viewDataFile.createNewFile();
		}
		return viewDataFile;
	}

	@Override
	public String getViewText(ContentContext ctx, String key) throws ResourceNotFoundException {

		Properties i18n = i18nView.get(ctx.getRequestContentLanguage());
		if (i18n == null) {
			i18n = new Properties();
			String fileName = "/i18n/" + I18N_FILE.replaceAll("\\[lg\\]", ctx.getRequestContentLanguage());
			ServletContext srvCtx = ctx.getRequest().getSession().getServletContext();
			InputStream in = null;
			try {
				in = ConfigHelper.getComponentConfigResourceAsStream(srvCtx, getComponentPath(), fileName);
			} catch (ResourceNotFoundException e) {
				// resource can not be found.
				e.printStackTrace();
				logger.severe(e.getMessage());
			}
			if (in != null) {
				try {
					i18n.load(in);
					i18nView.put(ctx.getRequestContentLanguage(), i18n);
				} catch (Exception e) {
					throw new ResourceNotFoundException("can not load the resource : " + fileName);
				}
			}
		}
		String text = i18n.getProperty(key);
		if (text == null) {
			text = "TEXT [" + key + "] NOT FOUND.";
		}
		return text;
	}

	protected String executeCurrentRenderer(ContentContext ctx) throws ServletException, IOException {
		return executeRenderer(ctx, getRenderer(ctx));
	}
	
	protected String executeRenderer(ContentContext ctx, String url) throws ServletException, IOException {
		if (url != null) {
			ctx.getRequest().setAttribute(COMPONENT_KEY, this);
			if (!url.startsWith("/")) {
				url = URLHelper.createJSPComponentURL(ctx.getRequest(), url, getComponentPath());
			}
			logger.fine("execute view jsp in '" + getType() + "' : " + url);
			try {
				I18nAccess.getInstance(ctx);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ServletHelper.executeJSP(ctx, url);
		} else {
			return null;
		}
	}

	protected String renderViewXHTMLCode(ContentContext ctx) throws Exception {
		if (HIDDEN.equals(getStyle())) {
			if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(GlobalContext.getInstance(ctx.getRequest()), ctx.getRequest().getSession()).isEditPreview()) {
				String prefix = "";
				String suffix = "";
				if (!isWrapped(ctx)) {
					prefix = getForcedPrefixViewXHTMLCode(ctx);
					suffix = getForcedSuffixViewXHTMLCode(ctx);
				}
				return prefix + '[' + getType() + ']' + suffix;
			} else {
				return "";
			}
		}
		if (getRenderer(ctx) != null) {
			return executeCurrentRenderer(ctx);
		} else {
			if (isNeedRenderer()) {
				if (ctx.isAsPreviewMode()) {
					return "<div class=\"error\">No renderer found for '" + getType() + "' in template '" + ctx.getCurrentTemplate().getName() + "'.</div>";
				}
			}
			return getViewXHTMLCode(ctx);
		}
	}

	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return getValue();
	}

	@Override
	public int getWordCount(ContentContext ctx) {
		String value = getValue();
		if (value != null) {
			return value.split(" ").length;
		}
		return 0;
	}

	@Override
	public final String getXHTMLCode(ContentContext ctx) {

		setNeedRefresh(false);
		ctx.getRequest().setAttribute("comp", this);

		try {

			if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
				processView(ctx);
			}

			GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

			if ((ctx.getRenderMode() == ContentContext.PREVIEW_MODE)) {
				EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
				if (editCtx.isEditPreview() && isDefaultValue(ctx)) {
					String emptyCode = getEmptyCode(ctx);
					if (emptyCode != null) {
						return emptyCode;
					}
				}
			}

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				return getEditXHTMLCode(ctx);
			} else {
				if (isHiddenInMode(ctx.getRenderMode())) {
					String emptyCode = getEmptyCode(ctx);
					if (emptyCode == null) {
						emptyCode = "";
					}
					return emptyCode;
				}
				ctx.getRequest().setAttribute(COMP_ID_REQUEST_PARAM, getId());
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentCachable(ctx) && globalContext.isPreviewMode()) {
					if (getContentCache(ctx) != null) {
						return getContentCache(ctx);
					}
					synchronized (getLock(ctx)) {
						if (getContentCache(ctx) != null) {
							return getContentCache(ctx);
						}
					}
				}
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentTimeCachable(ctx) && globalContext.isPreviewMode()) {
					String timeContent = getContentTimeCache(ctx);
					if (timeContent != null) {
						return timeContent;
					}
					synchronized (getLock(ctx)) {
						timeContent = getContentTimeCache(ctx);
						if (timeContent != null) {
							return timeContent;
						}
					}
				}
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentCachable(ctx) && globalContext.isPreviewMode()) {
					logger.fine("add content in cache for component " + getType() + " in page : " + ctx.getPath());
					long beforeTime = System.currentTimeMillis();
					String content;
					synchronized (getLock(ctx)) {
						if (getRenderer(ctx) != null) {
							prepareView(ctx);
						}
						content = renderViewXHTMLCode(ctx);
						setContentCache(ctx, content);
					}
					logger.fine("render content cache '" + getType() + "' : " + (System.currentTimeMillis() - beforeTime) / 1000 + " sec.");
					return content;
				} else {
					String content;
					if (isContentTimeCachable(ctx) && globalContext.isPreviewMode()) {
						long beforeTime = System.currentTimeMillis();
						synchronized (getLock(ctx)) {
							if (getRenderer(ctx) != null) {
								prepareView(ctx);
							}
							content = renderViewXHTMLCode(ctx);
							logger.fine("render content time cache '" + getType() + "' : " + (System.currentTimeMillis() - beforeTime) / 1000 + " sec.");
							setContentTimeCache(ctx, content);
						}
					} else {
						if (getRenderer(ctx) != null) {
							prepareView(ctx);
						}
						content = renderViewXHTMLCode(ctx);
					}
					return content;
				}

			}

		} catch (Exception e) {
			DebugListening.getInstance().sendError(ctx.getRequest(), e, "error in component : " + getType());
			e.printStackTrace();
			return "";
		}
	}

	protected Object getLock(ContentContext ctx) {
		return this;
	}

	/**
	 * prepare the rendering of a component. default attributes put in request :
	 * style, value, type, compid
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public void prepareView(ContentContext ctx) throws Exception {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("load : " + getType() + " on : " + URLHelper.createURL(ctx));
		}
		ctx.getRequest().setAttribute("comp", this);
		ctx.getRequest().setAttribute("compPage", new PageBean(ctx, getPage()));
		ctx.getRequest().setAttribute("style", getStyle());
		ctx.getRequest().setAttribute("value", getValue());
		ctx.getRequest().setAttribute("type", getType());
		ctx.getRequest().setAttribute("compid", getForcedId(ctx));
		ctx.getRequest().setAttribute("renderer", getCurrentRenderer(ctx));
		ctx.getRequest().setAttribute("previewAttributes", getSpecialPreviewCssClass(ctx, getStyle(ctx)) + getSpecialPreviewCssId(ctx));	
		if (isAskWidth(ctx) && getWidth() != null) {
			String width = getWidth().trim();
			if (width.endsWith("%")) {			
				Float withInt = Float.parseFloat(width.substring(0,width.length()-1));		
				NumberFormat df = DecimalFormat.getInstance(Locale.ENGLISH);
				ctx.getRequest().setAttribute("componentOpositeWidth", df.format((100-withInt))+"%");
			}
			ctx.getRequest().setAttribute("componentWidth", width);
		} else {
			ctx.getRequest().removeAttribute("componentWidth");
		}
		if (getLayout() != null) {
			ctx.getRequest().setAttribute("layout", getLayout().getStyle());
		} else {
			ctx.getRequest().removeAttribute("layout");
		}
		if (isValueProperties()) {
			Properties p = new Properties();
			p.load(new StringReader(getValue()));
			ctx.getRequest().setAttribute("properties", p);
		}
	}

	protected void includeComponentJSP(ContentContext ctx, String jsp) throws ServletException, IOException {
		try {
			ctx.getRequest().setAttribute(COMPONENT_KEY, this);
			String url = URLHelper.createJSPComponentURL(ctx.getRequest(), jsp, getComponentPath());
			logger.fine("include jsp in '" + getType() + "' : " + url);
			includePage(ctx, url);
		} catch (Exception e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
	}

	protected String getDisplayAsInputName() {
		return "display-as-" + getId();
	}

	private String getDisplayType() {
		String[] values = getValue().split(VALUE_SEPARATOR);
		String out = null;
		if (values.length >= 5) {
			out = values[4];
			if (out.isEmpty()) {
				out = null;
			}
		}
		return out;
	}

	protected void includePage(ContentContext ctx, String jsp) throws ServletException, IOException {
		// ctx.getResponse().flushBuffer();
		try {
			ctx.getRequest().getRequestDispatcher(jsp).include(ctx.getRequest(), ctx.getResponse());
		} catch (ServletException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	protected void init() throws ResourceNotFoundException {
		// loadViewData();
		msg = null;
		config = null;
	}

	protected void init(ComponentBean bean, ContentContext ctx) throws Exception {
		assert bean != null;
		setComponentBean(bean);
		init();
	}

	/**
	 * insert a text in the component
	 * 
	 * @param text
	 *            the text to be insered
	 */
	@Override
	public void insert(String text) {
		setValue(text);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return isEmpty(ctx);
	}

	@Override
	public boolean isEmpty(ContentContext ctx) {
		return getValue().trim().length() == 0;
	}

	protected boolean isFirstElementOfRepeatSequence(ContentContext ctx) throws Exception {
		MenuElement currentPage = ctx.getCurrentPage();
		if (isRepeat() && currentPage.equals(getPage())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean isFirstRepeated() {

		IContentVisualComponent previousComp = getPreviousComponent();

		while (previousComp != null && previousComp.getArea().equals(getArea())) {
			if (!previousComp.isRepeat()) {
				return false;

			}
			previousComp = previousComp.getPreviousComponent();
		}

		return true;
	}

	@Override
	public boolean isHidden(ContentContext ctx) {
		return hidden;
	}

	@Override
	public boolean isInline() {
		return false;
	}

	/**
	 * you can insert a text in this component
	 * 
	 * @return true if a text is insertable
	 */
	@Override
	public boolean isInsertable() {
		return true;
	}

	@Override
	public int getLabelLevel(ContentContext ctx) {
		return 0;
	}

	@Override
	public boolean isList(ContentContext ctx) {
		return componentBean.isList();
	}

	@Override
	public boolean isListable() {
		return false;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#isModify()
	 */
	@Override
	public boolean isModify() {
		return componentBean.isModify();
	}

	@Override
	public boolean isNeedRefresh() {
		return needRefresh;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	/**
	 * @return Returns the repeat.
	 */
	@Override
	public boolean isRepeat() {
		return componentBean.isRepeat();
	}
	
	public boolean isNolink() {
		return componentBean.isNolink();
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}
	
	public boolean isNoLinkable() {
		return false;
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	protected boolean isViewDataFile(ContentContext ctx) throws IOException {
		return getViewDataFile(ctx, false).exists();
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	/**
	 * default : visible only in LARGE format.
	 */
	@Override
	public boolean isVisible(ContentContext ctx) {
		return true;
	}

	@Override
	public synchronized void loadViewData(ContentContext ctx) throws IOException {
		if (viewData == null) {
			viewData = new Properties();
		}
		if (isViewDataFile(ctx)) {
			InputStream in = null;
			try {
				in = new FileInputStream(getViewDataFile(ctx));
				Properties newViewData = new Properties();
				newViewData.load(in);
				viewData = newViewData;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				ResourceHelper.closeResource(in);
			}
		}
	}

	@Override
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx) throws Exception {
		AbstractVisualComponent res = (AbstractVisualComponent) this.clone();
		res.init(bean, newCtx);
		return res;
	}

	@Override
	public IContentVisualComponent next() {
		return nextComponent;
	}

	protected void onStyleChange(ContentContext ctx) {
	}

	@Override
	public IContentVisualComponent previous() {
		return previousComponent;
	}

	/**
	 * prepare the rendering of the page.
	 * 
	 * @param ctx
	 * @return
	 * @throws Exception
	 */
	protected String processView(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("config", getConfig(ctx));
		ctx.getRequest().setAttribute("style", getStyle());
		// ctx.getRequest().setAttribute("viewXHTML", getViewXHTMLCode(ctx));
		return null;
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		String newContent = requestService.getParameter(getContentName(), null);
		if (newContent != null) {
			if (!componentBean.getValue().equals(newContent)) {
				componentBean.setValue(newContent);
				setModify();
			}
		}
		return null;
	}

	public final void performUpdate(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		performEdit(ctx);
	}

	@Override
	public void replaceAllInContent(Map<String, String> replacement) {
		if (replacement != null) {
			this.replacement.putAll(replacement);
		}
	}

	@Override
	public void replaceInContent(String source, String target) {
		getRemplacement().put(source, target);
	}

	public void resetContentCache(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		globalContext.getCache(CACHE_NAME).removeAll();
	}

	@Override
	public void resetViewData(ContentContext ctx) throws IOException {
		if (isViewDataFile(ctx)) {
			try {
				getViewDataFile(ctx).delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void setComponentBean(ComponentBean componentBean) {
		this.componentBean = componentBean;
	}

	public void setContentCache(ContentContext ctx, String contentCache) {

		if (contentCache == null) {
			return;
		}
		if (contentCache.contains(";jsessionid=")) {
			logger.fine("couldn't put content with jsession id in cache on : " + getPage().getPath() + " - comp:" + getType());
			return;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache cache = globalContext.getCache(CACHE_NAME);

		String contentKey = getContentCacheKey(ctx);

		cache.put(contentKey, contentCache);
	}

	public void setContentTimeCache(ContentContext ctx, String contentCache) {

		if (contentCache == null) {
			return;
		}
		if (contentCache.contains(";jsessionid=")) {
			logger.fine("couldn't put content with jsession id in cache on : " + getPage().getPath() + " - comp:" + getType());
			return;
		}
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ICache cache = globalContext.getCache(TIME_CACHE_NAME);
		String contentKey = getContentCacheKey(ctx);
		String timeKey = TIME_KEY_PREFIX + contentKey;
		cache.put(contentKey, contentCache);
		cache.put(timeKey, new Long(System.currentTimeMillis()));
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	@Override
	public void setList(boolean inList) {
		componentBean.setList(inList);
	}

	public void setBackgroundColor(String bgcol) {
		componentBean.setBackgroundColor(bgcol);
	}

	public void setTextColor(String textcol) {
		componentBean.setTextColor(textcol);
	}

	public void setMessage(GenericMessage inMsg) {
		msg = inMsg;
	}

	public void setModify() {
		componentBean.setModify(true);
	}

	@Override
	public void setNeedRefresh(boolean needRefresh) {
		this.needRefresh = needRefresh;
	}

	@Override
	public void setNextComponent(IContentVisualComponent nextComponent) {
		this.nextComponent = nextComponent;
	}

	@Override
	public void setPage(MenuElement inPage) {
		page = inPage;
	}

	@Override
	public void setPreviousComponent(IContentVisualComponent previousComponent) {
		this.previousComponent = previousComponent;
	}

	@Override
	public void setRepeat(boolean newRepeat) {
		if (newRepeat == componentBean.isRepeat()) {
			return;
		} else {
			componentBean.setRepeat(newRepeat);
			setModify();
			setNeedRefresh(true);
		}
	}
	
	public void setNolink(boolean noLink) {
		if (noLink == componentBean.isNolink()) {
			return;
		} else {
			componentBean.setNolink(noLink);
			setModify();
			setNeedRefresh(true);
		}
	}

	@Override
	public void setStyle(ContentContext ctx, String inStyle) {
		boolean styleModify = false;
		if ((getStyle() != null) && (!getStyle().equals(inStyle))) {
			styleModify = true;
		}
		if (getStyle(ctx) == null && inStyle != null) {
			styleModify = true;
		}
		if (styleModify) {
			componentBean.setStyle(inStyle);
			setModify();
			onStyleChange(ctx);
		}
	}

	@Override
	public void setValid(boolean inVisible) {
		visible = inVisible;
	}

	@Override
	public void setValue(String inContent) {
		if (!inContent.equals(componentBean.getValue())) {
			componentBean.setValue(StringHelper.escapeWordChar(inContent));
			setModify();
		}
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#stored()
	 */
	@Override
	public void stored() {
		componentBean.setModify(false);
	}

	public void storeViewData(ContentContext ctx) throws IOException {
		ResourceHelper.writePropertiesToFile(getViewData(ctx), getViewDataFile(ctx), "view-data storage");
	}

	public InputStream stringToStream(String str) {
		return new ByteArrayInputStream(str.getBytes());
	}

	public Reader stringToReader(String str, String encoding) throws UnsupportedEncodingException {
		return new InputStreamReader(stringToStream(str), encoding);
	}

	public Reader stringToReader(String str) throws UnsupportedEncodingException {
		return stringToReader(str, ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * @deprecated use XHTMLHelper.textToXHTML(String text)
	 * @param text
	 * @return
	 */
	@Deprecated
	public String textToXHTML(String text) {
		String res = XHTMLHelper.autoLink(text);
		res = res.replaceAll("\n", "<br />\n");
		return res.replaceAll("  ", "&nbsp;&nbsp;");
	}

	@Override
	public boolean isMetaTitle() {
		return false;
	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}

	/**
	 * if content of the component is a list of properties (key=value) this
	 * method must return true. If this method return true prepare method will
	 * add a mal called "properties" in request attrivute and this map can be
	 * used in renderer (jsp).
	 * 
	 * @return true if content is a list of properties.
	 */
	public boolean isValueProperties() {
		return false;
	}

	@Override
	public String getVersion() {
		return "?";
	}

	@Override
	public String getDescription(Locale local) {
		return "";
	}

	@Override
	public String getAuthors() {
		return getComponentBean().getAuthors();
	}

	private int getTimeCache(ContentContext ctx) {
		String contentTimeCache = getConfig(ctx).getProperty("cache.time", null);
		if (contentTimeCache == null) {
			return 60 * 60; // default 1u
		}
		return Integer.parseInt(contentTimeCache);
	}

	@Override
	public boolean initContent(ContentContext ctx) throws Exception {
		return false;
	}

	@Override
	public Date getCreationDate() {
		return componentBean.getCreationDate();
	}

	@Override
	public Date getModificationDate() {
		return componentBean.getModificationDate();
	}

	public boolean isHiddenInMode(int mode) {
		if (componentBean.getHiddenModes() == null) {
			return false;
		} else {
			return componentBean.getHiddenModes().contains(mode);
		}
	}

	public void setHiddenInMode(int mode, boolean hidden) {
		if (hidden) {
			if (componentBean.getHiddenModes() == null) {
				componentBean.setHiddenModes(new HashSet<Integer>());
			}
			componentBean.getHiddenModes().add(mode);
		} else {
			if (componentBean.getHiddenModes() != null) {
				componentBean.getHiddenModes().remove(mode);
			}
		}
	}

	@Override
	public String getSpecialTagTitle(ContentContext ctx) throws Exception {
		return null;
	}

	@Override
	public String getSpecialTagXHTML(ContentContext ctx) throws Exception {
		return null;
	}

	/**
	 * return true if this component need renderer from template.
	 * 
	 * @return
	 */
	protected boolean isNeedRenderer() {
		return false;
	}

	@Override
	public int compareTo(IContentVisualComponent comp) {
		return getModificationDate().compareTo(comp.getModificationDate());
	}

	@Override
	public ComponentLayout getLayout() {
		return componentBean.getLayout();
	}

	@Override
	public boolean equals(ContentContext ctx, IContentVisualComponent comp) {
		return getComponentBean().compareTo(componentBean) == 0;
	}

	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception {
		if (isHiddenInMode(ctx.getRenderMode()) || !AdminUserSecurity.getInstance().canModifyConponent(ctx, getId())) {
			return "";
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String prefix = "";
			String suffix = "";
			if (!isWrapped(ctx)) {
				prefix = getForcedPrefixViewXHTMLCode(ctx);
				suffix = getForcedSuffixViewXHTMLCode(ctx);
			}
			return prefix + '[' + i18nAccess.getText("content." + getType(), getType()) + ']' + suffix;
		}
	}

	@Override
	public String getPageDescription(ContentContext ctx) {
		return null;
	}

	@Override
	public String getListGroup() {
		return getType();
	}

	public boolean isReversedLink(ContentContext ctx) {
		return ctx.getGlobalContext().isReversedLink();
	}

	@Override
	public GenericMessage getContentMessage(ContentContext ctx) {
		return null;
	}

	@Override
	public GenericMessage getTextMessage(ContentContext ctx) {
		return null;
	}

	@Override
	public GenericMessage getConfigMessage(ContentContext ctx) {
		return null;
	}
	
	/**
	 * mark component as new in the current request
	 * @param ctx
	 * @return
	 */
	public void markAsNew(ContentContext ctx) {
		ctx.getRequest().setAttribute("new-component", getId());
	}
	
	/**
	 * check if this component has maked has new in the current request
	 * @param ctx
	 * @return
	 */
	public boolean isNew(ContentContext ctx) {
		return ctx.getRequest().getAttribute("new-component") != null && ctx.getRequest().getAttribute("new-component").equals(getId()); 
	}
	
	/**
	 * return true if the component is directly edited when it is insered.
	 * @param ctx
	 * @return
	 */
	public boolean isEditOnCreate(ContentContext ctx) {
		return ctx.getGlobalContext().getStaticConfig().isEditOnCreate();
	}
	
	public static void main(String[] args) {
		String width = "12%";
		Float withInt = Float.parseFloat(width.substring(0,width.length()-1));
		System.out.println(withInt);
		NumberFormat df = DecimalFormat.getInstance(Locale.ENGLISH);
		System.out.println(df.format((100-withInt))+"%");
	}
	
	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();		
		map.putAll(BeanHelper.bean2Map(getComponentBean()));
		map.put("path", getPage().getPath());
		return map;
	}

}