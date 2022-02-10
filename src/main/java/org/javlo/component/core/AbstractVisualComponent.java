/*
 * Created on 9 oct. 2003
 */
package org.javlo.component.core;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.DataAction;
import org.javlo.cache.ICache;
import org.javlo.component.column.TableBreak;
import org.javlo.component.column.TableComponent;
import org.javlo.component.config.ComponentConfig;
import org.javlo.component.container.IContainer;
import org.javlo.component.links.MirrorComponent;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.context.UserInterfaceContext;
import org.javlo.exception.ResourceNotFoundException;
import org.javlo.helper.*;
import org.javlo.helper.Comparator.StringSizeComparator;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.i18n.I18nAccess;
import org.javlo.image.ExtendedColor;
import org.javlo.message.GenericMessage;
import org.javlo.module.file.FileAction;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.rendering.Device;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.service.visitors.CookiesService;
import org.javlo.servlet.IVersion;
import org.javlo.template.Template;
import org.javlo.user.AdminUserFactory;
import org.javlo.user.AdminUserSecurity;
import org.javlo.user.User;
import org.javlo.utils.DebugListening;
import org.javlo.utils.StructuredProperties;
import org.javlo.utils.SuffixPrefix;
import org.owasp.encoder.Encode;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <li>{@link #getComponentCssClass(ContentContext)}</li>
 * </ul>
 * 
 * @author Patrick Vandermaesen
 */
public abstract class AbstractVisualComponent implements IContentVisualComponent {

	private static boolean LOCAL_HELP = true;

	private static final Map<String, String> helpText = Collections.synchronizedMap(new HashMap<>());

	private static final String MIRROR_WRAPPED = "mirrorWrapped";

	public static final String SCROLL_TO_COMP_ID_ATTRIBUTE_NAME = "_new_id";

	public static final String NOT_EDIT_PREVIEW_PARAM_NAME = "_not_edit_preview";

	public static final String CACHE_KEY_SUFFIX_PARAM_NAME = "_cache_key_suffix";

	protected static final String EDIT_CLASS = "_edit_empty_component";

	public static Logger logger = Logger.getLogger(AbstractVisualComponent.class.getName());

	public static final String COMPONENT_KEY = "wcms_component";

	public static final String I18N_FILE = "component_[lg].properties";

	private static final String CACHE_NAME = "component";

	public static final String TIME_CACHE_NAME = "component-time";

	private static final String TIME_KEY_PREFIX = "_TIME_CRT_";

	protected static final String VALUE_SEPARATOR = "-";

	public static final String HIDDEN = "hidden";

	public static final String MOBILE_TYPE = "mobile-only";

	private static final List<Integer> DEFAULT_COLUMN_SIZE = new LinkedList<Integer>(Arrays.asList(new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }));

	public static final String FORCE_COMPONENT_ID = "___FORCE_COMPONENT_ID";

	private static final Integer[] ALL_MODES = new Integer[] { ContentContext.MODULE_DESKTOP_SPECIAL_MODE, ContentContext.MODULE_MOBILE_SPECIAL_MODE, ContentContext.VIEW_MODE, ContentContext.PREVIEW_MODE, ContentContext.PAGE_MODE, ContentContext.TIME_MODE };

	private Map<String, Properties> i18nView = Collections.EMPTY_MAP;

	private ComponentBean componentBean = new ComponentBean();

	private GenericMessage msg;

	private IContentVisualComponent nextComponent = null;

	private IContentVisualComponent previousComponent = null;

	private boolean needRefresh = false;

	private boolean visible = false;

	private boolean hidden = false;

	private Map<String, String> replacement = Collections.EMPTY_MAP;

	private Properties viewData = null;

	private MenuElement page = null;

	protected ComponentConfig config = null;

	private String configTemplate = null;

	private String group = null;

	private GenericMessage localMessage = null;

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

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
		getRemplacement().clear();
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
		GlobalContext globalContext = ctx.getGlobalContext();
		String helpURL = globalContext.getHelpURL();
		if (!StringHelper.isURL(helpURL)) {
			helpURL = URLHelper.createStaticURL(ctx, helpURL);
		} else {
			LOCAL_HELP = false;
		}
		if (helpURL.contains("${language}")) {
			helpURL = helpURL.replace("${language}", globalContext.getEditLanguage(ctx.getRequest().getSession()));
		} else {
			helpURL = URLHelper.mergePath(helpURL + 'v' + IVersion.VERSION.substring(0, 3).replace('.', '_'), globalContext.getEditLanguage(ctx.getRequest().getSession()));
		}
		return helpURL;
	}

	/**
	 * search the equivalent component in the default language content.
	 * 
	 * @throws Exception
	 */
	@Override
	public IContentVisualComponent getReferenceComponent(ContentContext ctx) throws Exception {
		if (ctx.getRequestContentLanguage().equals(ctx.getGlobalContext().getDefaultLanguage())) {
			return this;
		}
		int componentPosition = ComponentHelper.getComponentPosition(ctx, this);

		if (componentPosition == -1) {
			logger.severe("bad component position : " + componentPosition + "  type=" + getType() + "  area=" + this.getArea());
		}

		ContentContext lgCtx = ctx.getContextForDefaultLanguage();
		IContentVisualComponent refComp = ComponentHelper.getComponentWidthPosition(lgCtx, getPage(), getArea(), getType(), componentPosition);
		if (refComp == null) {
			logger.warning("ref component not found : type=" + getType() + "  position=" + componentPosition);
			return null;
		} else {
			return refComp;
		}
	}

	@Override
	public boolean isHelpURL(ContentContext ctx) {
		if (LOCAL_HELP) {
			try {
				return getHelpText(ctx) != null;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		} else {
			return !StringHelper.isEmpty(ctx.getGlobalContext().getHelpURL()) && !StringHelper.isEmpty(getHelpURI(ctx));
		}
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
		GlobalContext globalContext = ctx.getGlobalContext();
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
	 * webapps. normaly this localisation is the name of the component direcoty in
	 * the src.
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
		GlobalContext globalContext = ctx.getGlobalContext();
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
		String contextKey = ctx.getGlobalContext().getContextKey();
		if (ctx.getGlobalContext().getMainContext() != null) {
			contextKey = ctx.getGlobalContext().getMainContext().getContextKey();
		}

		int mobile = 0;
		if (ctx.getDevice() != null && ctx.getDevice().isMobileDevice()) {
			mobile = 1;
		}

		String keySuffix = contextKey + '-' + ctx.getLanguage() + '-' + ctx.getRequestContentLanguage() + '-' + ctx.getRenderMode() + '-' + templateId + '-' + pageId + '-' + mobile;
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		if (requestService.getParameter(CACHE_KEY_SUFFIX_PARAM_NAME, null) != null) {
			keySuffix = keySuffix + '-' + requestService.getParameter(CACHE_KEY_SUFFIX_PARAM_NAME, null);
		}

		if (isContentCachableByQuery(ctx)) {
			keySuffix = keySuffix + '_' + ctx.getRequest().getQueryString() + '_' + StringHelper.neverNull(ctx.getCurrentUserId());
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
		GlobalContext globalContext = ctx.getGlobalContext();
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

	protected String getForcedRenderer(ContentContext ctx) {
		return (String) ctx.getRequest().getAttribute("frenderer-" + getId());
	}

	protected void setForcedRenderer(ContentContext ctx, String renderer) {
		ctx.getRequest().setAttribute("frenderer-" + getId(), renderer);
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

	@Override
	public List<String> extractFieldsFromRenderer(ContentContext ctx) throws IOException {
		String cr = getRenderer(ctx);
		if (cr != null) {
			if (cr.contains("?")) {
				cr = cr.substring(0, cr.indexOf('?'));
			}
			String realPath = ctx.getRequest().getSession().getServletContext().getRealPath(cr);
			if (realPath == null) {
				logger.severe("coun't not convert : " + cr + " to path.");
				return null;
			}
			File renderer = new File(realPath);
			if (renderer.exists()) {
				Pattern pattern = Pattern.compile("(name=\")(.+?)(\")");
				Matcher matcher = pattern.matcher(ResourceHelper.loadStringFromFile(renderer));
				LinkedList<String> outFields = new LinkedList<String>();
				while (matcher.find()) {
					String group = matcher.group();
					group = group.substring(0, group.length() - 1);
					String field = group.replaceFirst("name=\"", "");
					if (!field.equals("webaction") && !field.equals("comp-id") && !outFields.contains(field)) {
						outFields.add(field);
					}
				}
				return outFields;
			} else {
				logger.warning("renderer not found : " + renderer);
			}
		}
		return null;
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

	/**
	 * return the wysiwyg editor complexity
	 * 
	 * @return null if no wysywig editor
	 */
	protected String getEditorComplexity(ContentContext ctx) {
		return getConfig(ctx).getProperty("editor-complexity", null);
	}

	@Override
	public final boolean isColumnable(ContentContext ctx) {
		try {
			if (!ctx.getCurrentTemplate().isColumnable()) {
				return false;
			}
			return StringHelper.isTrue(getConfig(ctx).getProperty("columnable", null), getColumnableDefaultValue());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	protected boolean getColumnableDefaultValue() {
		return false;
	}

	@Override
	public int getColumnSize(ContentContext ctx) {
		return componentBean.getColumnSize();
	}

	public void setColumnSize(int size) {
		if (componentBean.getColumnSize() != size) {
			componentBean.setColumnSize(size);
			setModify();
		}
	}

	protected String getInputNameColomn() {
		return getInputName("_columnSize");
	}

	protected String drawColumn(ContentContext ctx, int size) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"column column-" + size + "\">");
		out.println("<table><tr>");
		if (size == 0) {
			out.print("<td></td>");
			out.print("<td class=\"main-col\" style=\"width:69%;\"></td>");
		} else if (size < getColumnMaxSize(ctx)) {
			out.print("<td class=\"main-col\" style=\"width:" + Math.round(100 * size / getColumnMaxSize(ctx)) + "%;\"></td>");
			out.print("<td></td>");
		} else {
			out.print("<td class=\"main-col\" ></td>");
		}
		out.println("</tr></table></div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	protected String getColumn(ContentContext ctx) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"column-selection-block\"><div class=\"column-selection\">");
		for (Integer colSize : getColumnSizes(ctx)) {
			String cssClass = "";
			if (getColumnSize(ctx) == colSize) {
				cssClass = " class=\"active\" ";
			}
			out.println("<label" + cssClass + " for=\"" + (getInputNameColomn() + colSize) + "\"  style=\"width: " + Math.round(100 / getColumnSizes(ctx).size()) + "%\" >");
			out.println("<div class=\"select-col select-col-" + colSize + "\">");
			String select = "";
			if (getColumnSize(ctx) == colSize) {
				select = " checked=\"checked\" ";
			}
			out.println("<input type=\"radio\" id=\"" + (getInputNameColomn() + colSize) + "\" name=\"" + getInputNameColomn() + "\"" + select + "value=\"" + colSize + "\" />");
			out.println("<div class=\"fraction\">" + (colSize == 0 ? "auto" : (colSize + "/" + getColumnMaxSize(ctx))) + "</div></div>");
			out.print(drawColumn(ctx, colSize) + "</label>");
		}
		out.println("</div><hr /></div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	protected List<Integer> getColumnSizes(ContentContext ctx) {
		List<Integer> colSizes;
		try {
			colSizes = ctx.getCurrentTemplate().getColumnableSizes();
			if (colSizes != null) {
				return colSizes;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DEFAULT_COLUMN_SIZE;
	}

	protected Integer getColumnMaxSize(ContentContext ctx) {
		return getColumnSizes(ctx).get(getColumnSizes(ctx).size() - 1);
	}

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

	public String getEditRenderer(ContentContext ctx) {
		return null;
	}

	public void prepareEdit(ContentContext ctx) throws Exception {
		ctx.getRequest().setAttribute("compid", getId());
		ctx.getRequest().setAttribute("value", getValue());
		ctx.getRequest().setAttribute("inputName", getContentName());
		prepareView(ctx);
		return;
	}

	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		StringBuffer finalCode = new StringBuffer();
		finalCode.append(getDebugHeader(ctx));
		finalCode.append(getSpecialInputTag());
		finalCode.append("<textarea class=\"form-control resizable-textarea full-width\" id=\"" + getContentName() + "\" name=\"" + getContentName() + "\"");
		finalCode.append(" rows=\"" + (countLine() + 1) + "\">");
		finalCode.append(getValue());
		finalCode.append("</textarea>");
		if (getEditorComplexity(ctx) != null) {
			Map<String, String> filesParams = new HashMap<String, String>();
			String path = FileAction.getPathPrefix(ctx);
			filesParams.put("path", path);
			filesParams.put("webaction", "changeRenderer");
			filesParams.put("page", "meta");
			filesParams.put("select", "_TYPE_");
			filesParams.put(ContentContext.PREVIEW_EDIT_PARAM, "true");

			String chooseImageURL = URLHelper.createModuleURL(ctx, ctx.getPath(), "file", filesParams);
			finalCode.append("<script type=\"text/javascript\">jQuery(document).ready(loadWysiwyg('#" + getContentName() + "','" + getEditorComplexity(ctx) + "','" + chooseImageURL + "'));</script>");
		}
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

	protected boolean isFreeInputLayout() {
		return false;
	}

	protected boolean isAutoDeletable() {
		return false;
	}

	@Override
	public String getXHTMLConfig(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String repeatHidden = "";
		boolean showRepeat = true;
		UserInterfaceContext uiContext = UserInterfaceContext.getInstance(ctx.getRequest().getSession(), ctx.getGlobalContext());
		if (ctx.getGlobalContext().isMailingPlatform()) {
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

		if (isNoLinkable() && ctx.getGlobalContext().isReversedLink()) {
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
		if (isAutoDeletable() && !ctx.getGlobalContext().isMailing()) {
			out.println("<div class=\"line\">");
			out.println("<label for=\"deldate-" + getId() + "\">" + i18nAccess.getText("component.delete-date", "delete on") + "</label>");
			Date delDate = getDeleteDate(ctx);
			String value = "";
			if (delDate != null) {
				value = " value=\"" + StringHelper.renderSortableDate(delDate) + "\"";
			}
			out.println("<input type=\"date\" name=\"deldate-" + getId() + "\"" + value + " />");
			out.println("</div>");
		}
		if (isHiddable()) {
			out.println("<div class=\"line\">");
			out.println("<label for=\"hidden-" + getId() + "\">" + i18nAccess.getText("global.hidden") + "</label>");
			out.println(XHTMLHelper.getCheckbox("hidden-" + getId(), isDisplayHidden()));
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
				out.println("<div class=\"line layout\">");
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, layout.isBold()));
				out.println(i18nAccess.getText("component.layout.bold", "bold"));
				out.println("</label>");
				id = "layout-italic-" + getId();
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, layout.isItalic()));
				out.println(i18nAccess.getText("component.layout.italic", "italic"));
				out.println("</label></div>");
				id = "font-family-" + getId();
				out.println("<div class=\"line layout\">");
				out.println("<label for=\"" + id + "\">" + i18nAccess.getText("font") + "</label>");
				out.println(XHTMLHelper.getInputOneSelectFirstItem(id, ctx.getCurrentTemplate().getFonts(), layout.getFont(), i18nAccess.getText("global.inherited"), "form-control"));
				out.println("</label></div>");
			}
			out.println("</div>");
		}

		if (isCanAddClass() && !uiContext.isLight()) {
			out.println("<div class=\"line\">");
			String inputName = getInputName("cssclass");
			out.println("<label for=\"" + inputName + "\">" + i18nAccess.getText("component.cssclass", "css class") + "</label>");
			out.println("<input id=\"" + inputName + "\" name=\"" + inputName + "\" class=\"form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getManualCssClass()) + "\" />");
			out.println("</div>");
		}

		if (getConfig(ctx).isChooseBackgoundColor()) {
			out.println("<div class=\"line\">");
			String bgColInputName = "bgcol-" + getId();
			out.println("<label for=\"" + bgColInputName + "\">" + i18nAccess.getText("component.background-color") + "</label>");
			List<ExtendedColor> colors = ctx.getCurrentTemplate().getColorList();
			if (colors.size() > 0) {
				out.println(XHTMLHelper.renderColorChooser(bgColInputName, "", colors, StringHelper.neverNull(getBackgroundColor())));
			} else {
				out.println("<input id=\"" + bgColInputName + "\" name=\"" + bgColInputName + "\" class=\"color form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getBackgroundColor()) + "\" />");
			}
			out.println("</div>");
		}

		if (getConfig(ctx).isChooseTextColor()) {
			out.println("<div class=\"line\">");
			String textColInputName = "textcol-" + getId();
			out.println("<label for=\"" + textColInputName + "\">" + i18nAccess.getText("component.text-color") + "</label>");
			List<ExtendedColor> colors = ctx.getCurrentTemplate().getColorList();
			if (colors.size() > 0) {
				out.println(XHTMLHelper.renderColorChooser(textColInputName, "", colors, StringHelper.neverNull(getTextColor())));
			} else {
				out.println("<input id=\"" + textColInputName + "\" name=\"" + textColInputName + "\" class=\"color form-control\" type=\"text\" value=\"" + StringHelper.neverNull(getTextColor()) + "\" />");
			}
			out.println("</div>");
		}

		String[] styles = getStyleList(ctx);
		if (!isFreeInputLayout() && styles.length > 1) {
			String[] stylesLabel = getStyleLabelList(ctx);
			if (styles.length != stylesLabel.length) {
				throw new ComponentException("size of styles is'nt the same than size of styles label.");
			}
			out.println("<div class=\"line\">");
			out.println("<label for=\"style-" + getId() + "\">" + getStyleTitle(ctx) + "</label>");
			out.println(XHTMLHelper.getInputOneSelect("style-" + getId(), styles, stylesLabel, getStyle(), "form-control", null, false));
			out.println("</div>");
		}
		if (isFreeInputLayout()) {
			String[] stylesLabel = getStyleLabelList(ctx);
			if (styles.length != stylesLabel.length) {
				throw new ComponentException("size of styles is'nt the same than size of styles label.");
			}
			out.println("<div class=\"line\">");
			out.println("<label for=\"style-" + getId() + "\">" + getStyleTitle(ctx) + "</label>");
			out.println("<input id=\"" + "style-" + getId() + "\" name=\"" + "style-" + getId() + "\" class=\"form-control\" type=\"text\" value=\"" + getStyle() + "\" />");
			out.println("</div>");
		}

		if (getRenderes(ctx) == null || getRenderes(ctx).size() > 1) {
			out.println(getSelectRendererXHTML(ctx, isAutoRenderer()));
		}

		if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			out.println("<div class=\"line\">");
			out.println("<label>" + i18nAccess.getText("component.display-modes") + "</label>");
			for (int mode : ALL_MODES) {
				String id = "display-mode-" + mode + "-" + getId();
				out.println("<label for=\"" + id + "\">");
				out.println(XHTMLHelper.getCheckbox(id, !isHiddenInMode(ctx, mode, null)));
				out.println(ContentContext.getRenderModeKey(mode));
				out.println("</label>");
			}
			out.println("</div>");
		}

		if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) && (!isContentCachable(ctx) || isForceCachable())) {
			out.println("<div class=\"line" + repeatHidden + "\">");
			if (showRepeat) {
				out.println("<label for=\"cachable-" + getId() + "\">" + i18nAccess.getText("content.force-cachable", "force cachable") + "</label>");
			}
			out.println(XHTMLHelper.getCheckbox("forceCachable-" + getId(), isForceCachable()));
			out.println("</div>");
		} else {
			out.println("<input type=\"hidden\" name=\"forceCachable-" + getId() + "\" value=\"" + isForceCachable() + "\" />");
		}

		if (ctx.getGlobalContext().isCookies()) {
			out.println("<div class=\"line\">");
			out.println("<label>" + i18nAccess.getText("component.display-cookies") + "</label>");

			String id = "display-cookies-" + getId();
			out.println("<label>");
			out.println(XHTMLHelper.getRadio(id, "" + CookiesService.ALWAYS_STATUS, "" + getCookiesDisplayStatus()));
			out.println(i18nAccess.getText("component.display-cookies.always"));
			out.println("</label>");

			out.println("<label>");
			out.println(XHTMLHelper.getRadio(id, "" + CookiesService.ACCEPTED_STATUS, "" + getCookiesDisplayStatus()));
			out.println(i18nAccess.getText("component.display-cookies.accepted"));
			out.println("</label>");

			out.println("<label>");
			out.println(XHTMLHelper.getRadio(id, "" + CookiesService.NOT_ACCEPTED_STATUS, "" + getCookiesDisplayStatus()));
			out.println(i18nAccess.getText("component.display-cookies.not-accepted"));
			out.println("</label>");

			out.println("<label>");
			out.println(XHTMLHelper.getRadio(id, "" + CookiesService.REFUSED_STATUS, "" + getCookiesDisplayStatus()));
			out.println(i18nAccess.getText("component.display-cookies.refused"));
			out.println("</label>");

			out.println("<label>");
			out.println(XHTMLHelper.getRadio(id, "" + CookiesService.NOCHOICE_STATUS, "" + getCookiesDisplayStatus()));
			out.println(i18nAccess.getText("component.display-cookies.nochoice"));
			out.println("</label>");

			out.println("</div>");
		}

		out.close();
		return new String(outStream.toByteArray());
	}

	protected boolean isHiddable() {
		return false;
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

		if (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser())) {
			boolean isForceCachable = requestService.getParameter("forceCachable-" + getId(), null) != null;
			if (isForceCachable != isForceCachable()) {
				setForceCachable(isForceCachable);
				setModify();
				setNeedRefresh(true);
			}
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

		boolean hidden = requestService.getParameter("hidden-" + getId(), null) != null;
		if (isHiddable() && hidden != isDisplayHidden()) {
			setDisplayHidden(hidden);
			setModify();
			setNeedRefresh(true);
		}

		String cssClass = requestService.getParameter("cssclass-" + getId(), null);
		if (cssClass != null && !cssClass.equals(getManualCssClass())) {
			setManualCssClass(cssClass);
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

		String delDate = requestService.getParameter("deldate-" + getId(), null);
		if (delDate != null) {
			if (StringHelper.isEmpty(delDate)) {
				getComponentBean().setDeleteDate(null);
			} else {
				Date date = StringHelper.parseSortableDate(delDate);
				getComponentBean().setDeleteDate(date);
			}
			setModify();
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
			layout.setFont(requestService.getParameter("font-family-" + getId()));
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
			for (int mode : ALL_MODES) {
				String id = "display-mode-" + mode + "-" + getId();
				boolean visible = requestService.getParameter(id, null) != null;
				setHiddenInMode(mode, !visible);
			}
		}

		/** cookies **/
		if (ctx.getGlobalContext().isCookies()) {
			getComponentBean().setCookiesDisplayStatus(Integer.parseInt(requestService.getParameter("display-cookies-" + getId(), "0")));
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
			GlobalContext globalContext = ctx.getGlobalContext();
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			if (editCtx.isPreviewEditionMode() && !ctx.isPreviewOnly()) {
				MenuElement currentPage = ctx.getCurrentPage();
				if (currentPage.equals(getPage())) { // not edit component
					String prefix = "";
					String suffix = "";
					if (!isWrapped(ctx)) {
						prefix = getForcedPrefixViewXHTMLCode(ctx);
						suffix = getForcedSuffixViewXHTMLCode(ctx);
					}
					return (prefix + "<div " + getPrefixCssClass(ctx, "pc_empty-component") + getSpecialPreviewCssId(ctx) + ">" + getEmptyXHTMLCode(ctx) + "</div>" + suffix);
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
		String resources = getConfig(ctx).getProperty("resources", null);
		Template template = null;
		try {
			template = ctx.getCurrentTemplate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (resources != null && template != null) {
			List<String> linkResource = StringHelper.stringToCollection(resources, ",");
			List<String> outResource = new LinkedList<String>();
			for (String url : linkResource) {
				if (url.startsWith("/")) {
					try {
						outResource.add(URLHelper.createStaticTemplateURLWithoutContext(ctx, template, url));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					outResource.add(url);
				}
			}
			return outResource;
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getFirstPrefix(ContentContext ctx) {
		if (!componentBean.isList()) {
			return getConfig(ctx).getProperty("prefix.first", "");
		} else {
			String cssClass = "";
			if (getComponentCssClass(ctx) != null && getComponentCssClass(ctx).trim().length() > 0) {
				cssClass = ' ' + getComponentCssClass(ctx);
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
	public String getHelpText(ContentContext ctx) throws IOException {
		if (!LOCAL_HELP) {
			return null;
		}
		String editLang = ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession());
		final String cacheKey = getType() + "_" + editLang;
		String txt = helpText.get(cacheKey);
		if (txt != null) {
			if (StringHelper.isEmpty(txt)) {
				return null;
			} else {
				return txt;
			}
		} else {
			String helpPath = "/help/" + editLang + "/components/" + getType() + ".html";
			helpPath = ctx.getRequest().getSession().getServletContext().getRealPath(helpPath);
			File helpFile = new File(helpPath);
			if (!helpFile.exists()) {
				if (!editLang.equals("en")) {
					helpPath = "/help/en/components/" + getType() + ".html";
					helpPath = ctx.getRequest().getSession().getServletContext().getRealPath(helpPath);
					helpFile = new File(helpPath);
				}
			}
			txt = "";
			if (helpFile.exists()) {
				txt = ResourceHelper.loadStringFromFile(helpFile);
			} else {
				txt = "";
			}
			// helpText.put(cacheKey, txt); //TODO: restore cache
			if (StringHelper.isEmpty(txt)) {
				return null;
			} else {
				return txt;
			}
		}
	}

	@Override
	public final String getHelpURL(ContentContext ctx) {
		User user = AdminUserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession()).getCurrentUser(ctx.getRequest().getSession());
		if (user == null) {
			return null;
		}
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

		GlobalContext globalContext = ctx.getGlobalContext();
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

	public String getDisplayMessage() {
		GenericMessage message = getMessage();
		if (message != null) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);
			out.print("<div class=\"alert alert-" + message.getBootstrapType() + "\" role=\"alert\">");
			out.print(message.getMessage());
			out.println("</div>");
			out.close();
			return new String(outStream.toByteArray());
		} else {
			return "";
		}
	}

	@Override
	public IContentVisualComponent getNextComponent() {
		return nextComponent;
	}

	@Override
	public MenuElement getPage() {
		return page;
	}

	public void setContainerPage(ContentContext ctx, MenuElement page) {
		ctx.getRequest().setAttribute("page-" + getId(), page);
	}

	/**
	 * get the page of the container if component is mirrored else get the page of
	 * the component.
	 * 
	 * @param ctx
	 * @return
	 */
	public MenuElement getContainerPage(ContentContext ctx) {
		String key = "page-" + getId();
		if (ctx.getRequest().getAttribute(key) != null) {
			return (MenuElement) ctx.getRequest().getAttribute(key);
		} else {
			return getPage();
		}
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

	protected boolean isOpenRow(ContentContext ctx) {
		int max = getColumnMaxSize(ctx);
		IContentVisualComponent prev = getPreviousComponent();
		if (prev instanceof IContainer) {
			prev = ((IContainer) prev).getOpenComponent(ctx);
		}
		boolean open = false;
		if (prev == null) {
			open = true;
		} else {
			if (ctx.getColumnableSize(ctx.getColumnableDepth()) <= 0) {
				open = true;
			}
			if (ctx.getColumnableSize(ctx.getColumnableDepth()) + getColumnSize(ctx) > max || prev.getColumnSize(ctx) <= 0) {
				open = true;
			}
		}
		if (this instanceof IContainer) {
			IContainer container = (IContainer) this;
			if (container.isOpen(ctx)) {
				ctx.setColumnableDepth(ctx.getColumnableDepth() + 1);
			}
		}
		return open;
	}

	protected boolean isCloseRow(ContentContext ctx) {

		int colSize = getColumnSize(ctx);

		if (this instanceof IContainer) {
			IContainer container = (IContainer) this;
			if (!container.isOpen(ctx) && container.getOpenComponent(ctx) != null) {
				colSize = container.getOpenComponent(ctx).getColumnSize(ctx);
				ctx.setColumnableDepth(ctx.getColumnableDepth() - 1);
				if (ctx.getColumnableDepth() < 0) {
					logger.severe("bad component structure columnable depth is negative : " + ctx.getRequest().getRequestURL());
				}
			}
		}

		int max = getColumnMaxSize(ctx);
		IContentVisualComponent next = getNextComponent();
		boolean close = false;
		/* auto */
		if (getColumnSize(ctx) == 0) {
			ctx.setColumnableSize(0, ctx.getColumnableDepth());
			return true;
		}
		ctx.setColumnableSize(ctx.getColumnableSize(ctx.getColumnableDepth()) + colSize, ctx.getColumnableDepth());
		if (next != null) {
			if (ctx.getColumnableSize(ctx.getColumnableDepth()) + next.getColumnSize(ctx) > max || next.getColumnSize(ctx) < 0 || !next.isColumnable(ctx)) {
				close = true;
				ctx.setColumnableSize(0, ctx.getColumnableDepth());
			}
		} else {
			close = true;
			ctx.setColumnableSize(0, ctx.getColumnableDepth());
		}
		return close;
	}

	protected String getColomnablePrefix(ContentContext ctx) {
		String colPrefix = "";
		int columnSize = getColumnSize(ctx);
		if (this instanceof IContainer) {
			IContainer container = (IContainer) this;
			if (!container.isOpen(ctx)) {
				return "";
			}
		}
		if (!ctx.getGlobalContext().getStaticConfig().isProd()) {
			colPrefix = "<!-- type=" + getType() + " isColumnable(ctx)=" + isColumnable(ctx) + " - columnSize=" + columnSize + " - getColumnMaxSize(ctx)=" + getColumnMaxSize(ctx) + " -->";
		}
		if (isColumnable(ctx) && columnSize >= 0 && columnSize != getColumnMaxSize(ctx)) {
			try {
				Template tpl = ctx.getCurrentTemplate();
				colPrefix += "<div class=\"component-row-wrapper\"><" + tpl.getColumnableRowTag() + " style=\"" + tpl.getColumnableRowStyle() + "\" class=\"" + tpl.getColumnableRowClass() + " component-row component-row-" + getType() + "\">";
				String firstClass = "columnalbe-first-col ";
				if (!StringHelper.isEmpty(tpl.getColumnableRowTagIn())) {
					colPrefix = colPrefix + '<' + tpl.getColumnableRowTagIn() + '>';
				}
				if (!isOpenRow(ctx)) {
					colPrefix = "";
					firstClass = "";
				}
				int currentSize = ctx.getColumnableSize(ctx.getColumnableDepth());
				int leftSize = getColumnMaxSize(ctx) - currentSize;

				colPrefix = colPrefix + "<" + tpl.getColumnableColTag() + " style=\"" + tpl.getColumnableColStyle(getColumnSize(ctx)) + "\" class=\"" + firstClass + tpl.getColumnableColClass(getColumnSize(ctx), currentSize, leftSize) + "\">";
				if (!StringHelper.isEmpty(tpl.getColumnableColTagIn())) {
					colPrefix = colPrefix + '<' + tpl.getColumnableColTagIn() + " class=\"" + tpl.getColumnableClassTagIn() + "\" style=\"" + tpl.getColumnableStyleTagIn() + "\">";
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return colPrefix;
	}

	protected String getColomnableSuffix(ContentContext ctx) {
		String colSuffix = "";
		int columnSize = getColumnSize(ctx);
		if (this instanceof IContainer) {
			IContainer container = (IContainer) this;
			if (!container.isOpen(ctx) && container.getOpenComponent(ctx) != null) {
				columnSize = container.getOpenComponent(ctx).getColumnSize(ctx);
			} else {
				return "";
			}
		}
		if (isColumnable(ctx) && columnSize >= 0 && columnSize != getColumnMaxSize(ctx)) {
			Template tpl;
			try {
				tpl = ctx.getCurrentTemplate();
				colSuffix = "</" + tpl.getColumnableRowTag() + "> </div><!-- close row : " + getId() + " -->";
				if (!StringHelper.isEmpty(tpl.getColumnableRowTagIn())) {
					colSuffix = "</" + tpl.getColumnableRowTagIn() + '>' + colSuffix;
				}
				if (!isCloseRow(ctx)) {
					colSuffix = "";
				}
				colSuffix = "</" + tpl.getColumnableColTag() + "> <!-- close col : " + getId() + " -->" + colSuffix;
				if (!StringHelper.isEmpty(tpl.getColumnableColTagIn())) {
					colSuffix = "</" + tpl.getColumnableColTagIn() + "> <!-- close in col : " + getId() + " -->" + colSuffix;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!ctx.getGlobalContext().getStaticConfig().isProd()) {
			colSuffix += "<!-- /type=" + getType() + " isColumnable(ctx)=" + isColumnable(ctx) + " - columnSize=" + columnSize + " - getColumnMaxSize(ctx)=" + getColumnMaxSize(ctx) + " -->";
		}
		return colSuffix;
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		String colPrefix = getColomnablePrefix(ctx);
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return "";
		}

		if (isWrapped(ctx)) {
			return colPrefix + getForcedPrefixViewXHTMLCode(ctx);
		} else {
			return colPrefix;
		}
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		String closeComment = "<!-- /close comp:" + getType() + " -->";
		if (isDisplayHidden() && ctx.isAsViewMode()) {
			return closeComment;
		}
		String colSuffix = getColomnableSuffix(ctx) + closeComment;
		if (isWrapped(ctx)) {
			return getForcedSuffixViewXHTMLCode(ctx) + colSuffix;
		} else {
			return colSuffix;
		}
	}

	protected String contructViewStyle(ContentContext ctx) {
		IContentVisualComponent previousComp = null;
		try {
			previousComp = ComponentHelper.getPreviousComponent(this, ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		IContentVisualComponent nextComp = null;
		try {
			nextComp = ComponentHelper.getNextComponent(this, ctx);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String style = getComponentCssClass(ctx);
		if (style != null) {
			style = style + ' ';
		} else {
			style = "";
		}
		if (isBackgroundColored()) {
			style = style + " colored-wrapper";
		}
		if (previousComp == null || !previousComp.getType().equals(getType())) {
			style = style + " first ";
		}
		if (previousComp != null && previousComp instanceof TableComponent && !(previousComp instanceof TableBreak)) {
			style = style + " first-in-cell ";
		}
		if (previousComp == null) {
			style = style + " first-component ";
		}
		if (nextComp == null) {
			style = style + " last-component ";
		}
		if (nextComp == null || !nextComp.getType().equals(getType())) {
			style = style + " last ";
		}
		if (getCookiesDisplayStatus() == CookiesService.NOCHOICE_STATUS) {
			style = style + " _cookie-nochoice ";
		}
		if (getCookiesDisplayStatus() == CookiesService.NOT_ACCEPTED_STATUS) {
			style = style + " _cookie-notacceptedchoice ";
		}
		return style;
	}

	protected String getForcedPrefixViewXHTMLCode(ContentContext ctx) {

		if (getConfig(ctx).getProperty("prefix", null) != null) {
			return getConfig(ctx).getProperty("prefix", null);
		}
		String style = contructViewStyle(ctx);
		String prefix;
		if (!componentBean.isList()) {
			prefix = "<" + getTag(ctx) + " " + getPrefixCssClass(ctx, style) + getSpecialPreviewCssId(ctx) + " " + getInlineStyle(ctx) + ">";
		} else {
			prefix = "<" + getListItemTag(ctx) + getPrefixCssClass(ctx, style) + getSpecialPreviewCssId(ctx) + " >";
		}
		if (isAjaxWrapper(ctx)) {
			prefix = prefix + "<div id=\"" + getAjaxId() + "\">";
		}

		return prefix;
	}

	protected String getCSSStyle(ContentContext ctx) {
		String inlineStyle = "";
		if (getBackgroundColor() != null && getBackgroundColor().length() > 2) {
			inlineStyle = " overflow: hidden; background-color: " + getBackgroundColor() + "; border-color: " + getBackgroundColor() + ';';
		}
		if (getTextColor() != null && getTextColor().length() > 2) {
			inlineStyle = inlineStyle + " color: " + getTextColor() + ';';
		}
		if (getLayout() != null) {
			inlineStyle = inlineStyle + ' ' + getLayout().getStyle();
		}
		return inlineStyle;
	}

	protected String getInlineStyle(ContentContext ctx) {
		String inlineStyle = getCSSStyle(ctx);
		if (inlineStyle.length() > 0) {
			inlineStyle = " style=\"" + inlineStyle + "\"";
		}
		return inlineStyle;
	}

	@Override
	public IContentVisualComponent getPreviousComponent() {
		return previousComponent;
	}

	/**
	 * return true if the previous component have the same type.
	 * 
	 * @return
	 */
	public boolean isPreviousSame() {
		if (getPreviousComponent() == null) {
			return false;
		} else {
			return getPreviousComponent().getType().equals(getType());
		}
	}

	/**
	 * return true if the next component have the same type.
	 * 
	 * @return
	 */
	public boolean isNextSame() {
		IContentVisualComponent nextComp = getNextComponent();
		if (nextComp == null) {
			return false;
		} else {
			return nextComp.getType().equals(getType());
		}
	}

	public boolean isNextSame(ContentContext ctx) {
		IContentVisualComponent nextComp = getNextComponent();
		if (nextComp == null) {
			return false;
		} else {
			try {
				return ComponentHelper.getFinalType(ctx, this).equals(ComponentHelper.getFinalType(ctx, nextComp));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}

	public boolean isPreviousSame(ContentContext ctx) {
		IContentVisualComponent previousComp = getPreviousComponent();
		if (previousComp == null) {
			return false;
		} else {
			try {
				return ComponentHelper.getFinalType(ctx, this).equals(ComponentHelper.getFinalType(ctx, previousComp));
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
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

		GlobalContext globalContext = ctx.getGlobalContext();
		Collection<String> lgs = globalContext.getContentLanguages();
		for (String lg : lgs) {
			Locale locale = new Locale(lg);
			sufixPreffix = new SuffixPrefix("<span lang=\"" + locale.getLanguage() + "\">", "</span>", locale.getDisplayLanguage(new Locale(ctx.getRequestContentLanguage())));
			out.add(sufixPreffix);
		}
		return out;
	}

	public Map<String, String> getRemplacement() {
		return replacement;
	}

	private Map<String, String> getRemplacementEditable() {
		if (replacement == Collections.EMPTY_MAP) {
			replacement = new HashMap();
		}
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
			renderer = renderers.get(currentRenderer + '.' + ctx.getDevice());
			if (renderer == null) {
				renderer = renderers.get(currentRenderer);
			}
			if (renderer == null && renderers.size() > 0) {
				renderer = renderers.values().iterator().next();
			}
		}
		if (getForcedRenderer(ctx) != null) {
			renderer = getForcedRenderer(ctx);
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

		GlobalContext globalContext = ctx.getGlobalContext();
		I18nAccess i18nAccess = I18nAccess.getInstance(globalContext, ctx.getRequest().getSession());

		if (getRenderes(ctx).size() > 1) {

			out.println("<fieldset class=\"display\">");
			out.println("<legend>" + i18nAccess.getText("content.page-teaser.display-type") + "</legend><div class=\"line\">");

			if (getRendererTitle() != null) {
				out.println("<div class=\"line\">");
				out.println("<label for=\"" + getInputNameRendererTitle() + "\">" + i18nAccess.getText("global.title") + " : </label>");
				out.println("<input type=\"text\" id=\"" + getInputNameRendererTitle() + "\" name=\"" + getInputNameRendererTitle() + "\" value=\"" + getRendererTitle() + "\"  />");
				out.println("</div>");
			}

			if (justDisplay) {
				out.println("<p>" + getCurrentRenderer(ctx) + "</p>");
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
	 * create technical input tag. sample : for know the type of component with only
	 * the <code>request</code>
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

	protected String getPreviewCssClass(ContentContext ctx, String currentClass) {

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
			GlobalContext globalContext = ctx.getGlobalContext();
			EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
			try {
				String classPrefix = "not-";
				if (!globalContext.isOnlyCreatorModify() || (ctx.getCurrentEditUser() != null && (AdminUserSecurity.getInstance().isAdmin(ctx.getCurrentEditUser()) || getAuthors().equals(ctx.getCurrentEditUser().getLogin())))) {
					if (!AdminUserSecurity.getInstance().haveRole(ctx.getCurrentEditUser(), AdminUserSecurity.LIGHT_INTERFACE_ROLE) || getComplexityLevel(ctx) == IContentVisualComponent.COMPLEXITY_EASY || getComplexityLevel(ctx) == IContentVisualComponent.COMPLEXITY_STANDARD) {
						classPrefix = "";
					}
				}
				RequestService rs = RequestService.getInstance(ctx.getRequest());
				if (!StringHelper.isTrue(rs.getParameter(NOT_EDIT_PREVIEW_PARAM_NAME, null)) && !ctx.isPreviewOnly()) {
					StaticConfig sc = ctx.getGlobalContext().getStaticConfig();
					if (getConfig(ctx).isPreviewEditable() && editCtx.isPreviewEditionMode() && (!isRepeat() || getPage().equals(ctx.getCurrentPage()) || sc.isEditRepeatComponent()) && AdminUserSecurity.canModifyPage(ctx, ctx.getCurrentPage(), true)) {
						// if (getConfig(ctx).isPreviewEditable() && editCtx.isPreviewEditionMode() &&
						// AdminUserSecurity.canModifyPage(ctx, ctx.getCurrentPage(), true)) {
						I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
						String type = i18nAccess.getText("content." + getType(), getType());
						String hint = "<b>" + type + "</b><br />" + i18nAccess.getViewText("preview.hint", "click for edit or drag and drop to move.");
						String newClass = "";
						if (isNew(ctx)) {
							newClass = " new-component";
							if (isEditOnCreate(ctx)) {
								newClass = newClass + " edit-component";
							}
						}
						String mirror = "not-mirror-wrapped";
						if (AbstractVisualComponent.isMirrorWrapped(ctx, this)) {
							mirror = "mirror-wrapped";
						}
						String name = i18nAccess.getText("content." + getType(), getType());

						// String debug = ""+getNextComponent().getColumnSize()+" <>
						// "+getNextComponent().isColumnable(ctx)+" <> "+ctx.getColumnableSize();
						if (isColumnable(ctx) && getColumnSize(ctx) >= 0) {
							name = name + " <br /> <span class='glyphicon glyphicon-arrow-left'></span> " + (getColumnSize(ctx) == 0 ? "a" : getColumnSize(ctx)) + " <span class='glyphicon glyphicon-arrow-right'></span>";
							// if (debug != null) {
							// name = name +" <br /> "+debug;
							// }
						}
						return specificClass + classPrefix + "editable-component " + mirror + currentClass + newClass + ' ' + getType() + "\" data-hint=\"" + hint + "\" data-name=\"" + name;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (currentClass != null && currentClass.trim().length() > 0) {
			return specificClass + currentClass.trim() + ' ' + getType();
		} else {
			return specificClass + getType();
		}
	}

	protected String getPrefixCssClass(ContentContext ctx, String currentClass) {
		String cssClass = getPreviewCssClass(ctx, currentClass);
		if (!StringHelper.isEmpty(getManualCssClass())) {
			cssClass += ' ' + getManualCssClass();
		}
		if (getLayout() != null && !StringHelper.isEmpty(getLayout().getCssClass())) {
			cssClass = getLayout().getCssClass().trim() + ' ' + cssClass.trim();
		}
		cssClass = cssClass.trim();
		if (cssClass.length() > 0) {
			return " class=\"" + cssClass + "\"";
		} else {
			return "";
		}
	}

	protected String getForcedId(ContentContext ctx) {
		/* user for mirror mecanism */
		String compID = (String) ctx.getRequest().getAttribute(FORCE_COMPONENT_ID);
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

	public String getPreviewCssId(ContentContext ctx) {
		if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE) {
			return "cp_" + getForcedId(ctx);
		} else {
			return "";
		}
	}

	public String getSpecialPreviewCssId(ContentContext ctx) {
		String cssId = getPreviewCssId(ctx);
		if (!StringHelper.isEmpty(cssId)) {
			return " id=\"" + cssId + "\"";
		} else {
			if (getViewID(ctx) != null) {
				return " id=\"" + getViewID(ctx) + "\"";
			} else {
				return "";
			}
		}
	}

	protected String getViewID(ContentContext ctx) {
		return null;
	}

	public boolean isAjaxWrapper(ContentContext ctx) {
		return false;
	}

	public String getAjaxId() {
		return "cp-" + getId();
	}

	public String getStyle() {
		return componentBean.getStyle();
	}

	@Override
	public final String getComponentCssClass(ContentContext ctx) {
		if (componentBean.getStyle() == null) {
			if ((getStyleList(ctx) != null) && (getStyleList(ctx).length > 0)) {
				if (getConfig(ctx).getDefaultStyle() == null) {
					componentBean.setStyle(getStyleList(ctx)[0]);
				} else {
					componentBean.setStyle(getConfig(ctx).getDefaultStyle());
				}
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
				if (!getPage().equals(ctx.getCurrentPage())) {
					style = style + " repeated";
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			try {
				if (getPage() != null && getPage().equals(ctx.getCurrentPage())) {
					style = style + " first-repeat";
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// if (!StringHelper.isEmpty(getManualCssClass())) {
		// style = style + ' ' + getManualCssClass() + ' ';
		// }
		if (!StringHelper.isEmpty(getSpecificCssClass(ctx))) {
			style = style + ' ' + getSpecificCssClass(ctx) + ' ';
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

	public String getSpecificCssClass(ContentContext ctx) {
		return null;
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

	protected String getForcedSuffixViewXHTMLCode(ContentContext ctx) {
		if (getConfig(ctx).getProperty("suffix", null) != null) {
			return getConfig(ctx).getProperty("suffix", null);
		}
		String suffix;
		if (!componentBean.isList()) {
			suffix = "</" + getTag(ctx) + "> <!-- /forced suffix t:" + getType() + " -->";
		} else {
			suffix = "</" + getListItemTag(ctx) + '>';
		}
		if (isAjaxWrapper(ctx)) {
			suffix = suffix + "</div>";
		}
		return suffix;
	}

	@Override
	public String getTextForSearch(ContentContext ctx) {
		return StringEscapeUtils.unescapeHtml4(getValue());
	}

	@Override
	public String getTextLabel(ContentContext ctx) {
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

	protected String getManualCssClass() {
		return componentBean.getManualCssClass();
	}

	private void setManualCssClass(String css) {
		componentBean.setManualCssClass(css);
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
		GlobalContext globalContext = ctx.getGlobalContext();
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
					if (i18nView == Collections.EMPTY_MAP) {
						i18nView = new HashMap<String, Properties>();
					}
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
		return executeRenderer(ctx, getRenderer(ctx), ctx.getRequest(), ctx.getResponse());
	}

	protected String executeRenderer(ContentContext ctx, String url, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

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
			if (url.endsWith(".html")) {
				return ServletHelper.executeThymeleaf(request, response);
			} else {

				return ServletHelper.executeJSP(ctx, url);
			}

		} else {
			return null;
		}
	}

	protected boolean isStyleHidden(ContentContext ctx) {
		String style = getStyle();
		return HIDDEN.equals(style) || MOBILE_TYPE.equals(style);
	}

	protected String renderViewXHTMLCode(ContentContext ctx) throws Exception {
		if (isStyleHidden(ctx) || isDisplayHidden()) {
			if (ctx.getRenderMode() == ContentContext.PREVIEW_MODE && EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession()).isPreviewEditionMode() && !ctx.isPreviewOnly()) {
				String prefix = "";
				String suffix = "";
				if (!isWrapped(ctx)) {
					prefix = getForcedPrefixViewXHTMLCode(ctx);
					suffix = getForcedSuffixViewXHTMLCode(ctx);
				}
				I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
				return prefix + "<span class=\"" + EDIT_CLASS + "\">" + i18nAccess.getText("content." + getType(), getType()) + "</span>" + suffix;
			} else {
				return "";
			}
		}
		String contentHTML;
		if (getRenderer(ctx) != null) {
			contentHTML = executeCurrentRenderer(ctx);
		} else {
			if (isNeedRenderer()) {
				if (ctx.isAsPreviewMode()) {
					return "<div class=\"error\">No renderer found for '" + getType() + "' in template '" + ctx.getCurrentTemplate().getName() + "'.</div>";
				}
			}
			contentHTML = getViewXHTMLCode(ctx);
		}
		if (isVisibleFromCookies(ctx)) {
			return contentHTML;
		} else {
			CookiesService cookiesService = CookiesService.getInstance(ctx);
			if (cookiesService.getAccepted() == null) {
				return "<div class=\"_cookie-cache\" data-status=\"" + getCookiesDisplayStatus() + "\" data-html=\"" + Encode.forHtmlAttribute(getPrefixViewXHTMLCode(ctx) + contentHTML + getPrefixViewXHTMLCode(ctx)) + "\" style=\"display:none;\"></div>";
			} else {
				return "";
			}
		}
	}

	protected String cleanValue(ContentContext ctx, String value) {
		if (ctx.getGlobalContext().getStaticConfig().isHighSecure()) {
			return Encode.forHtmlUnquotedAttribute(value);
		} else {
			return value;
		}
	}

	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return cleanValue(ctx, getValue());
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
	public String getXHTMLCode(ContentContext ctx) {
		try {
			if (isNeedDelete(ctx)) {
				return "";
			}

			setNeedRefresh(false);
			ctx.getRequest().setAttribute("comp", this);

			if (ctx.getRenderMode() != ContentContext.EDIT_MODE) {
				processView(ctx);
			}

			GlobalContext globalContext = ctx.getGlobalContext();

			if ((ctx.getRenderMode() == ContentContext.PREVIEW_MODE)) {
				EditContext editCtx = EditContext.getInstance(globalContext, ctx.getRequest().getSession());
				if (editCtx.isPreviewEditionMode() && isDefaultValue(ctx)) {
					String emptyCode = getEmptyCode(ctx);
					if (emptyCode != null) {
						return emptyCode;
					}
				}
			}

			if (ctx.getRenderMode() == ContentContext.EDIT_MODE) {
				String editXHTML;
				if (getEditRenderer(ctx) != null) {
					prepareEdit(ctx);
					editXHTML = executeJSP(ctx, getEditRenderer(ctx));
				} else {
					editXHTML = getEditXHTMLCode(ctx);
				}

				if (isColumnable(ctx) && !ctx.isExport()) { // no columable with exportComponent servlet
					return getColumn(ctx) + editXHTML;
				} else {
					return editXHTML;
				}
			} else {
				if (isHiddenInMode(ctx, ctx.getRenderMode(), ctx.isMobile())) {
					String emptyCode = getEmptyCode(ctx);
					if (emptyCode == null) {
						emptyCode = "";
					}
					return emptyCode;
				}
				ctx.getRequest().setAttribute(COMP_ID_REQUEST_PARAM, getId());
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentCachable(ctx) && !ctx.isNoCache() && globalContext.isPreviewMode() && getCookiesDisplayStatus() == CookiesService.ALWAYS_STATUS) {
					if (getContentCache(ctx) != null) {
						return getContentCache(ctx);
					}
					synchronized (getLock(ctx)) {
						if (getContentCache(ctx) != null) {
							return getContentCache(ctx);
						}
					}
				}
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentTimeCachable(ctx) && !ctx.isNoCache() && globalContext.isPreviewMode() && getCookiesDisplayStatus() == CookiesService.ALWAYS_STATUS) {
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
				if (ctx.getRenderMode() == ContentContext.VIEW_MODE && isContentCachable(ctx) && globalContext.isPreviewMode() && getCookiesDisplayStatus() == CookiesService.ALWAYS_STATUS) {
					logger.fine("add content in cache for component " + getType() + " in page : " + ctx.getPath());
					long beforeTime = System.currentTimeMillis();
					String content;
					synchronized (getLock(ctx)) {
						if (getRenderer(ctx) != null) {
							prepareView(ctx);
						}
						content = renderViewXHTMLCode(ctx);
						if (!ctx.isNoCache()) {
							setContentCache(ctx, content);
						}
					}
					logger.fine("render content cache '" + getType() + "' : " + (System.currentTimeMillis() - beforeTime) / 1000 + " sec.");
					return content;
				} else {
					String content;
					if (isContentTimeCachable(ctx) && globalContext.isPreviewMode() && getCookiesDisplayStatus() == CookiesService.ALWAYS_STATUS) {
						long beforeTime = System.currentTimeMillis();
						synchronized (getLock(ctx)) {
							if (getRenderer(ctx) != null) {
								prepareView(ctx);
							}
							content = renderViewXHTMLCode(ctx);
							logger.fine("render content time cache '" + getType() + "' : " + (System.currentTimeMillis() - beforeTime) / 1000 + " sec.");
							if (!ctx.isNoCache()) {
								setContentTimeCache(ctx, content);
							}
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
			DebugListening.getInstance().sendError(ctx, e, "error in component : " + getType());
			e.printStackTrace();
			return "";
		}
	}

	protected Object getLock(ContentContext ctx) {
		return this;
	}

	protected String getPreviewAttributes(ContentContext ctx) {
		return getPrefixCssClass(ctx, contructViewStyle(ctx)) + getSpecialPreviewCssId(ctx);
	}

	/**
	 * prepare the rendering of a component. default attributes put in request :
	 * style, value, type, compid
	 * 
	 * @param ctx
	 * @throws Exception
	 */
	public void prepareView(ContentContext ctx) throws Exception {

		setLocalMessage(null);

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
		ctx.getRequest().setAttribute("previewAttributes", getPreviewAttributes(ctx));
		ctx.getRequest().setAttribute("previewCSS", getPreviewCssClass(ctx, contructViewStyle(ctx)));
		ctx.getRequest().setAttribute("previewClass", getPreviewCssClass(ctx, null));
		ctx.getRequest().setAttribute("previewID", getPreviewCssId(ctx));
		ctx.getRequest().setAttribute("cssStyle", getCSSStyle(ctx));
		ctx.getRequest().setAttribute("cssClass", contructViewStyle(ctx));
		ctx.getRequest().setAttribute("manualCssClass", getManualCssClass());
		if (!AbstractVisualComponent.isMirrorWrapped(ctx, this)) {
			ctx.getRequest().setAttribute(MIRROR_WRAPPED, false);
			ctx.getRequest().setAttribute("nextSame", isNextSame(ctx));
			ctx.getRequest().setAttribute("previousSame", isPreviousSame(ctx));
		} else {
			ctx.getRequest().setAttribute(MIRROR_WRAPPED, true);
		}
		if (isAskWidth(ctx) && getWidth() != null) {
			String width = getWidth().trim();
			if (width.endsWith("%")) {
				Float withInt = Float.parseFloat(width.substring(0, width.length() - 1));
				NumberFormat df = DecimalFormat.getInstance(Locale.ENGLISH);
				ctx.getRequest().setAttribute("componentOpositeWidth", df.format((99 - withInt)) + "%");
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

		if (componentBean.getHiddenModes() != null && !componentBean.getHiddenModes().contains(ContentContext.MODULE_DESKTOP_SPECIAL_MODE) && !componentBean.getHiddenModes().contains(ContentContext.MODULE_MOBILE_SPECIAL_MODE)) {
			componentBean.getHiddenModes().add(ContentContext.MODULE_DESKTOP_SPECIAL_MODE);
			componentBean.getHiddenModes().add(ContentContext.MODULE_MOBILE_SPECIAL_MODE);
		}

		init();
	}

	@Override
	public void forceInit(ComponentBean bean, ContentContext ctx) throws Exception {
		init(bean, ctx);
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
		return isForceCachable();
	}

	public boolean isContentCachableByQuery(ContentContext ctx) {
		return false;
	}

	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return StringHelper.isEmpty(getValue());
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

	protected boolean isCanAddClass() {
		return true;
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

	protected Date getDeleteDate(ContentContext ctx) {
		return componentBean.getDeleteDate();
	}

	protected boolean isNeedDelete(ContentContext ctx) throws Exception {
		if (getDeleteDate(ctx) == null) {
			return false;
		} else {
			Date now = new Date();
			if (getDeleteDate(ctx).getTime() < now.getTime()) {
				deleteMySelf(ctx);
				return true;
			} else {
				return false;
			}
		}
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

	public boolean isForceCachable() {
		return componentBean.isForceCachable();
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

	@Override
	public boolean isVisible(ContentContext ctx) {
		return true;
	}

	public boolean isVisibleFromCookies(ContentContext ctx) {
		if (ctx.getGlobalContext().isCookies()) {
			EditContext editContext = EditContext.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
			if (editContext.isPreviewEditionMode()) {
				return true;
			}
			try {
				if (getCookiesDisplayStatus() == CookiesService.ALWAYS_STATUS) {
					return true;
				} else {
					CookiesService cookiesService = CookiesService.getInstance(ctx);
					if (cookiesService.getAccepted() == null) {
						return getCookiesDisplayStatus() == CookiesService.NOCHOICE_STATUS || getCookiesDisplayStatus() == CookiesService.NOT_ACCEPTED_STATUS;
					} else if (cookiesService.getAccepted()) {
						return getCookiesDisplayStatus() == CookiesService.ACCEPTED_STATUS;
					} else {
						return getCookiesDisplayStatus() == CookiesService.REFUSED_STATUS || getCookiesDisplayStatus() == CookiesService.NOT_ACCEPTED_STATUS;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return true;
			}
		} else {
			return true;
		}
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
	public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx, MenuElement page) throws Exception {
		AbstractVisualComponent res = (AbstractVisualComponent) this.clone();
		res.setPage(page);
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

	protected void performColumnable(ContentContext ctx) {
		if (isColumnable(ctx)) {
			RequestService rs = RequestService.getInstance(ctx.getRequest());
			String newWidth = rs.getParameter(getInputNameColomn(), "" + getColumnMaxSize(ctx));
			if (StringHelper.isDigit(newWidth)) {
				setColumnSize(Integer.parseInt(newWidth));
			}
		}
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		performColumnable(ctx);

		List<String> arraysContent = requestService.getParameterListValues(getContentName(), Collections.EMPTY_LIST);
		String newContent;
		if (arraysContent.size() < 2) {
			newContent = requestService.getParameter(getContentName(), null);
			if (getEditorComplexity(ctx) != null && getEditorComplexity(ctx).equals("soft")) {
				newContent = XHTMLHelper.removeTag(newContent, "p");
			}

		} else {
			newContent = StringHelper.collectionToString(arraysContent, ",");
		}
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
			getRemplacementEditable().putAll(replacement);
		}
	}

	@Override
	public void replaceInContent(String source, String target) {
		getRemplacementEditable().put(source, target);
	}

	public void resetContentCache(ContentContext ctx) {
		GlobalContext globalContext = ctx.getGlobalContext();
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
		GlobalContext globalContext = ctx.getGlobalContext();
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
		GlobalContext globalContext = ctx.getGlobalContext();
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

	@Override
	public void setDisplayHidden(boolean hidden) {
		componentBean.setHidden(hidden);
	}

	@Override
	public boolean isDisplayHidden() {
		return componentBean.isHidden();
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
		updateCache();
	}

	protected void updateCache() {

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

	public void setForceCachable(boolean inForceCachable) {
		if (inForceCachable == componentBean.isForceCachable()) {
			return;
		} else {
			componentBean.setForceCachable(inForceCachable);
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
		} else if (getStyle() == null && inStyle != null) {
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
	 * if content of the component is a list of properties (key=value) this method
	 * must return true. If this method return true prepare method will add a mal
	 * called "properties" in request attrivute and this map can be used in renderer
	 * (jsp).
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

	@Override
	public boolean isHiddenInMode(ContentContext ctx, int mode, Boolean mobile) {
		if (componentBean.getHiddenModes() == null) {
			return false;
		} else {
			if (!ctx.isPreviewEditionMode()) {
				if (mobile != null) {
					if (mobile) {
						if (componentBean.getHiddenModes().contains(ContentContext.MODULE_MOBILE_SPECIAL_MODE)) {
							return true;
						}
					} else if (componentBean.getHiddenModes().contains(ContentContext.MODULE_DESKTOP_SPECIAL_MODE)) {
						return true;
					}
				}
			}
			return componentBean.getHiddenModes().contains(mode);
		}
	}

	public int getCookiesDisplayStatus() {
		return componentBean.getCookiesDisplayStatus();
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
		if (isHiddenInMode(ctx, ctx.getRenderMode(), ctx.isMobile()) || !AdminUserSecurity.getInstance().canModifyConponent(ctx, getId())) {
			return "";
		} else {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			String prefix = "";
			String suffix = "";
			if (!isWrapped(ctx)) {
				prefix = getForcedPrefixViewXHTMLCode(ctx);
				suffix = getForcedSuffixViewXHTMLCode(ctx);
			}
			return prefix + "<span class=\"" + EDIT_CLASS + "\">" + getType() + "</span>" + i18nAccess.getText("content." + getType(), getType()) + "</span>" + suffix;
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
	 * 
	 * @param ctx
	 * @return
	 */
	public void markAsNew(ContentContext ctx) {
		ctx.getRequest().setAttribute("new-component", getId());
	}

	/**
	 * check if this component has maked has new in the current request
	 * 
	 * @param ctx
	 * @return
	 */
	public boolean isNew(ContentContext ctx) {
		return ctx.getRequest().getAttribute("new-component") != null && ctx.getRequest().getAttribute("new-component").equals(getId());
	}

	/**
	 * return true if the component is directly edited when it is insered.
	 * 
	 * @param ctx
	 * @return
	 */
	public boolean isEditOnCreate(ContentContext ctx) {
		return ctx.getGlobalContext().getStaticConfig().isEditOnCreate();
	}

	@Override
	public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(BeanHelper.bean2Map(getComponentBean()));
		map.put("path", getPage().getPath());
		return map;
	}

	public static void setMirrorWrapped(ContentContext ctx, IContentVisualComponent comp, MirrorComponent mirror) {
		if (comp != null) {
			ctx.getRequest().setAttribute("_mirror_wrapped_" + comp.getId(), true);
			ctx.getRequest().setAttribute("_mirror_comp_" + comp.getId(), mirror);
		}
	}

	public static boolean isMirrorWrapped(ContentContext ctx, IContentVisualComponent comp) {
		if (comp != null) {
			return ctx.getRequest().getAttribute("_mirror_wrapped_" + comp.getId()) != null;
		} else {
			return false;
		}
	}

	public static MirrorComponent getMirrorWrapper(ContentContext ctx, IContentVisualComponent comp) {
		if (comp != null) {
			return (MirrorComponent) ctx.getRequest().getAttribute("_mirror_comp_" + comp.getId());
		} else {
			return null;
		}
	}

	@Override
	public boolean isMirroredByDefault(ContentContext ctx) {
		return ctx.getRequestContentLanguage().equals(getComponentBean().getLanguage());
	}

	@Override
	public String getContentAsText(ContentContext ctx) {
		return StringHelper.removeTag(getValue(ctx));
	}

	public static String getImportFolderPath(ContentContext ctx, MenuElement page) throws Exception {
		String importFolder = ctx.getGlobalContext().getStaticConfig().getImportFolder();
		if (importFolder.length() > 1 && importFolder.startsWith("/")) {
			importFolder = importFolder.substring(1);
		}
		return importFolder + '/' + DataAction.createImportFolder(page);
	}

	public String getImportFolderPath(ContentContext ctx) throws Exception {
		return getImportFolderPath(ctx, getPage());
	}

	protected boolean isXML() {
		return false;
	}

	@Override
	public String getErrorMessage(ContentContext ctx) {
		if (isXML() && !StringHelper.isEmpty(getValue())) {
			try {
				XMLManipulationHelper.searchAllTag("<div>" + getValue() + "</div>", true);
			} catch (BadXMLException e) {
				return Encode.forHtml(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		String xhtmlCode = getXHTMLCode(ctx);
		return !(xhtmlCode != null && StringHelper.removeTag(xhtmlCode).trim().length() == 0 && !xhtmlCode.toLowerCase().contains("<img") && isDispayEmptyXHTMLCode(ctx));
	}

	@Override
	public boolean isRestMatch(ContentContext ctx, Map<String, String> params) {
		if (params.size() == 0) {
			return true;
		}
		if (params.containsKey("content")) {
			return getValue().contains(params.get("content"));
		}
		return true;
	}

	@Override
	public String getFontAwesome() {
		return "square";
	}

	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	protected boolean isValueTranslatable() {
		return false;
	}

	@Override
	public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
		if (!isValueTranslatable()) {
			return false;
		} else {
			boolean translated = true;
			String newValue = translator.translate(ctx, getValue(), lang, ctx.getRequestContentLanguage());
			if (newValue == null) {
				translated = false;
				newValue = ITranslator.ERROR_PREFIX + getValue();
			}
			setValue(newValue);
			return translated;
		}
	}

	public GenericMessage getLocalMessage() {
		return localMessage;
	}

	public void setLocalMessage(GenericMessage localMessage) {
		this.localMessage = localMessage;
	}

	public String getConfigInValue(String key, String defaultValue) {
		StructuredProperties prop = new StructuredProperties();
		StringReader sr = new StringReader(getValue());
		try {
			prop.load(sr);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			sr.close();
		}
		String value = prop.getProperty(key);
		if (value == null) {
			prop.setProperty(key, StringHelper.neverNull(defaultValue));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				prop.store(out, "add key : " + key);
				setValue(out.toString(ContentContext.CHARACTER_ENCODING));
				setModify();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return defaultValue;
		} else {
			return value;
		}
	}

	public String renderOtherComponent(ContentContext ctx, IContentVisualComponent comp) {
		if (comp == null) {
			return null;
		}
		if (!(comp instanceof AbstractVisualComponent)) {
			logger.severe("comp is'nt instance of AbstractVisualComponent");
			return "comp is'nt instance of AbstractVisualComponent";
		}
		AbstractVisualComponent.setForcedId(ctx, getId());
		// comp.prepareView(ctx);
		ctx.getRequest().setAttribute("nextSame", isNextSame(ctx));
		ctx.getRequest().setAttribute("previousSame", isPreviousSame(ctx));
		setContainerPage(ctx, getPage());
		boolean emptyPage = false;
		if (comp.getPage() == null) {
			comp.setPage(getPage());
			emptyPage = true;
		}
		String prefix = ((AbstractVisualComponent) comp).getPrefixViewXHTMLCode(ctx);
		String xhtml = prefix;
		xhtml += ((AbstractVisualComponent) comp).getXHTMLCode(ctx);
		String suffix = ((AbstractVisualComponent) comp).getSuffixViewXHTMLCode(ctx);
		xhtml += suffix;
		AbstractVisualComponent.setForcedId(ctx, null);
		if (emptyPage) {
			comp.setPage(null);
		}
		return xhtml;
	}

	@Override
	public String toString() {
		if (getPage() != null) {
			return "id:" + getId() + " type:" + getType() + " lang:" + getPage().getContentLanguage() + " page:" + getPage().getName() + " class:" + getClassName() + " hash:" + hashCode();
		} else {
			return "id:" + getId() + " type:" + getType() + " class:" + getClassName() + " hash:" + hashCode();
		}
	}

}