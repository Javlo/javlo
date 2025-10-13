/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.dynamic;

import org.javlo.bean.Link;
import org.javlo.component.core.*;
import org.javlo.component.image.IImageTitle;
import org.javlo.component.meta.ITimeRange;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.*;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.service.PersistenceService;
import org.javlo.service.RequestService;
import org.javlo.service.exception.ServiceException;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.service.resource.Resource;
import org.javlo.template.Template;
import org.javlo.utils.StructuredProperties;
import org.javlo.ztatic.IStaticContainer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author pvandermaesen
 */
public class DynamicComponent extends AbstractVisualComponent implements IStaticContainer, IFieldContainer, IDate, ILink, IImageTitle, ISubTitle, ITimeRange {

    public static final String JSP_HEADER = "<%@ page contentType=\"text/html; charset=UTF-8\" %><%@ taglib uri=\"jakarta.tags.core\" prefix=\"c\"%><%@ taglib prefix=\"fn\" uri=\"http://java.sun.com/jsp/jstl/functions\"%><%@ taglib prefix=\"fmt\" uri=\"jakarta.tags.fmt\"%><%@ taglib uri=\"/WEB-INF/javlo.tld\" prefix=\"jv\"%>";

    public static final String HIDDEN = "hidden";

    private static final String DYNAMIC_ID_KEY = "_dynamic_id";

    private static final String NOTIFY_CREATION = "notify.creation";

    private Date latestValidDate = null;

    private Map<String, Group> groups = null;

    private static final List<Integer> DEFAULT_COLUMN_SIZE = new LinkedList<Integer>(Arrays.asList(new Integer[]{1, 2, 3, 4, 6, 12}));

    private java.util.List<Field> fields = null;

    private String textTitle = null;

    /**
     * create a static logger.
     */
    protected static Logger logger = Logger.getLogger(DynamicComponent.class.getName());

    @Override
    public void prepareView(ContentContext ctx) throws Exception {
        if (getNextComponent() == null) {
            setNextComponent(ComponentHelper.getNextComponent(this, ctx));
        }
        if (getPreviousComponent() == null) {
            setPreviousComponent(ComponentHelper.getPreviousComponent(this, ctx));
        }
        ctx.getRequest().setAttribute("colWidth", getColumnSize(ctx));

        /** group **/
        if (groups == null) {
            groups = new HashMap<>();
            getGroups().forEach(group -> {
                try {
                    Group newGroup = new Group(group, getGroupSize(ctx, group), getGroupNumber(ctx, group));
                    groups.put(group, newGroup);

                    getFields(ctx).forEach(field -> {
                        if (field.getGroup() != null && field.getGroup().equals(group)) {
                            if (!newGroup.getGroupNumberList().contains(field.getGroupNumber())) {
                                newGroup.getGroupNumberList().add(field.getGroupNumber());
                            }
                            newGroup.getFields().put(field.getGroupLabel(), field);
                            newGroup.getFieldsForDisplay().put(field.getGroupLabel()+"-"+field.getReferenceName(), field);
                        }
                    });

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        ctx.getRequest().setAttribute("groups", groups);

        super.prepareView(ctx);
    }

    @Override
    public String[] getStyleList(ContentContext ctx) {
        String[] superStyle = super.getStyleList(ctx);
        if (superStyle == null || superStyle.length == 0) {
            return new String[]{"standard", HIDDEN};
        } else {

            return superStyle;
        }
    }

    @Override
    public String[] getStyleLabelList(ContentContext ctx) {
        String[] superLabelStyle = super.getStyleLabelList(ctx);
        if (superLabelStyle == null || superLabelStyle.length == 0) {
            I18nAccess i18nAccess;
            try {
                i18nAccess = I18nAccess.getInstance(ctx.getRequest());
                return new String[]{"standard", i18nAccess.getText("global.hidden")};
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return superLabelStyle;
        }
    }

    public class FieldOrderComparator implements Comparator<Field> {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.getOrder() - o2.getOrder();
        }
    }

    protected StructuredProperties properties = null;

    private Properties configProperties = null;

    @Override
    public void init(ComponentBean bean, ContentContext newContext) throws Exception {
        super.init(bean, newContext);
        reloadProperties();
        if (StringHelper.isDigit(properties.getProperty("component.column.size", null))) {
            setColumnSize(Integer.parseInt(properties.getProperty("component.column.size", null)));
        }
    }

    public void reloadProperties() {
        try {
            if (properties != null) {
                properties.load(stringToStream(getValue()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String contructViewStyle(ContentContext ctx) {
        String outClass = super.contructViewStyle(ctx);
        if (StringHelper.isEmpty(getDynamicRenderer(ctx))) {
            outClass = outClass + " no-renderer";
        }
        return outClass;
    }

    @Override
    public String getPrefixViewXHTMLCode(ContentContext ctx) {
        if (!isWrapped()) {
            return getColomnablePrefix(ctx);
        } else {
            return super.getPrefixViewXHTMLCode(ctx);
        }
    }

    @Override
    public String getSuffixViewXHTMLCode(ContentContext ctx) {
        if (!isWrapped()) {
            return getColomnableSuffix(ctx);
        } else {
            return super.getSuffixViewXHTMLCode(ctx);
        }
    }

    @Override
    public String getViewListXHTMLCode(ContentContext ctx) throws Exception {
        return getViewXHTMLCode(ctx, true);
    }

    @Override
    public String getViewXHTMLCode(ContentContext ctx) throws Exception {
        if (getRenderer(ctx) == null) {
            prepareView(ctx);
        }
        return getViewXHTMLCode(ctx, false);
    }

    @Override
    public void prepareEdit(ContentContext ctx) throws Exception {
        super.prepareEdit(ctx);
    }

    public static void main(String[] args) {
        String testappendReplacement = "début <!-- start-module -->  <h5>\n" +
                "                    ${field.text.researchTitle.research.value}\n" +
                "                </h5> <!-- end-module --> gin";
        String out = replaceGroupTags(testappendReplacement);
        System.out.println(out);
    }

    private static String replaceGroupTags(String inputText) {
        // Pattern to match the special tags and capture the key inside
        Pattern pattern = Pattern.compile("<!-- start-(\\w+) -->");
        Matcher matcher = pattern.matcher(inputText);

        StringBuffer sb = new StringBuffer();
        List<String> groups = new LinkedList<>();
        while (matcher.find()) {
            String key = matcher.group(1);
            groups.add(key);
            String replacement = "<c:forEach var=\"num\" items=\"\\${groups['"+key+"'].groupNumberList}\"><c:set var=\"key\" value=\""+key+" - \\${num}\" />";
            matcher.appendReplacement(sb, replacement);
        }

        matcher.appendTail(sb);

        for (String group : groups) {
            Pattern fieldPattern = Pattern.compile("\\$\\{field\\.([a-zA-Z]+\\..([a-zA-Z]+\\.)"+group+"\\.(\\w+))}");
            Matcher fieldMatcher = fieldPattern.matcher(sb.toString());
            StringBuffer fieldSb = new StringBuffer();
            while (fieldMatcher.find()) {
                String key = fieldMatcher.group(1);
                String replacement = String.format("<c:set var=\"fieldKey\" value=\"\\${key}-"+key.split("\\.")[1]+"\" />\\${groups['"+group+"'].fieldsForDisplay[fieldKey].$3}");
                fieldMatcher.appendReplacement(fieldSb, replacement);
            }
            fieldMatcher.appendTail(fieldSb);
            sb = fieldSb;
        }

        pattern = Pattern.compile("<!-- end-(\\w+) -->");
        matcher = pattern.matcher(sb.toString());
        sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = String.format("</c:forEach> <!-- end module : %s -->", key);
            matcher.appendReplacement(sb, replacement);
        }

        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * Parses an HTML file to extract configuration key-value pairs from a specific comment.
     * @param htmlContent the content of the HTML file as a string.
     * @return a Map containing key-value pairs, or null if the comment is not found.
     */
    public static Map<String, String> parseConfigComment(String htmlContent) {
        // Define the regex pattern to find the config comment
        Pattern commentPattern = Pattern.compile("<!--config(.*?)-->", Pattern.DOTALL);
        Matcher matcher = commentPattern.matcher(htmlContent);

        if (matcher.find()) {
            // Extract the content inside the comment
            String configContent = matcher.group(1).trim();

            // Split the content into key-value pairs and populate the map
            Map<String, String> configMap = new HashMap<>();
            String[] keyValuePairs = configContent.split("\\s+"); // Split by whitespace

            for (String pair : keyValuePairs) {
                String[] keyValue = pair.split("=", 2); // Split into key and value
                if (keyValue.length == 2) {
                    configMap.put(keyValue[0], keyValue[1]);
                }
            }
            return configMap;
        }
        return null; // Return null if no config comment is found
    }

    public String getViewXHTMLCode(ContentContext ctx, boolean asList) throws Exception {
        ctx.getRequest().setAttribute("page", new PageBean(ctx, getContainerPage(ctx)));
        ctx.getRequest().setAttribute("containerId", getId());
        ctx.getRequest().setAttribute("authors", getAuthors());
        if (getStyle() != null && getStyle().equals(HIDDEN)) {
            String emptyCode = getEmptyCode(ctx);
            if (emptyCode != null) {
                return emptyCode;
            } else {
                return "";
            }
        }
        Collection<Field> fields = getFields(ctx);
        for (Field field : fields) {
            if (field instanceof MetaField) {
                MetaField mField = (MetaField) field;
                if (!mField.isPublished(ctx)) {
                    return "";
                }
            }
        }
        if (asList) {
            if (getListRenderer() != null) {
                for (Field field : fields) {
                    if (field != null) {
                        field.fillRequest(ctx);
                    }
                }
                if (ctx.getCurrentTemplate() != null) {
                    String linkToJSP = URLHelper.createStaticTemplateURLWithoutContext(ctx, ctx.getCurrentTemplate(), "" + getListRenderer());
                    ctx.getRequest().setAttribute("renderAsList", true);
                    String html = executeJSP(ctx, linkToJSP);
                    ctx.getRequest().removeAttribute("renderAsList");
                    return html;
                } else {
                    return "";
                }
            }
        } else {
            if (getDynamicRenderer(ctx) != null) {
                for (Field field : fields) {
                    if (field != null) {
                        field.fillRequest(ctx);
                    }
                }
                if (ctx.isAbsoluteURL()) {
                    ctx = new ContentContext(ctx);
                    ctx.setAbsoluteURL(false);
                }
                String linkToJSP = URLHelper.createStaticTemplateURLWithoutContext(ctx, ctx.getCurrentTemplate(), "" + getDynamicRenderer(ctx));
                if (StringHelper.isHTMLStatic(linkToJSP)) {
                    File htmlFile = new File(ctx.getRequest().getSession().getServletContext().getRealPath(linkToJSP));
                    File jspFile = new File(StringHelper.getFileNameWithoutExtension(htmlFile.getAbsolutePath()) + ".jsp");
                    if (!jspFile.exists()) {
                        String html = JSP_HEADER + ResourceHelper.loadStringFromFile(htmlFile);
                        html = replaceGroupTags(html);
                        for (Field field : getFields(ctx)) {
                            if (!StringHelper.isEmpty(field.getGroup())) {
                                String prefix = "${field." + field.getType() + "." + field.getName() + "." + field.getGroup() + '.' + field.getName();
                                String fieldRef = "<c:set var=\"fieldKey\" value=\"${key}-"+field.getName()+"\" />";
                                fieldRef += "${groups['"+field.getGroup()+"'].fieldsForDisplay[fieldKey].";
                                html = html.replace(prefix, fieldRef);
                            } else {
                                html = html.replace("field." + field.getType() + "." + field.getName() + '.', field.getName() + (field.isI18n()?".reference.":"."));
                            }
                        }
                        if (!isWrapped()) {
                            html = html.replace(Template.PREVIEW_EDIT_CODE, "${"+AbstractVisualComponent.PREVIEW_ATTRIBUTES+"}");
                        }

                        /* group boucle */
                        for (String group : getGroups()) {
                            html = html.replace("<!-- start-"+group+" -->", "<!-- start-"+group+" --> "+"<for:each var='field' values='${groups['"+group+"]}'].fields>");
                            html = html.replace("<!-- end-"+group+" -->", "</for:each> "+"<!-- end-"+group+" -->");
                        }

                        html = Template.minifyContent(html);

                        ResourceHelper.writeStringToFile(jspFile, html);
                        if (ctx.getGlobalContext().isProd()) {
                            Template.minifyJSP(ctx.getGlobalContext(), jspFile);
                        }
                    }
                    linkToJSP = StringHelper.getFileNameWithoutExtension(linkToJSP) + ".jsp";
                    Map<String, String> htmlConfig = parseConfigComment(ResourceHelper.loadStringFromFile(jspFile));

                    if (htmlConfig != null && !htmlConfig.isEmpty()) {
                        for (Map.Entry<String, String> entry : htmlConfig.entrySet()) {
                            properties.setProperty(entry.getKey(), entry.getValue());
                        }
                        storeProperties();
                    }
                }
                String prefix = "";
                String suffix = "";
                if (isWrapped()) {
                    String cssClass = "";
                    if (getCSSClass().trim().length() > 0) {
                        cssClass = ' ' + getCSSClass();
                    }
                    prefix = "<div class=\"" + cssClass + "\">";
                    suffix = "</div>";
                }
                return prefix + executeJSP(ctx, linkToJSP) + suffix;
            }
        }
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        boolean allValid = true;
        for (Field field : fields) {
            if (!field.validate()) {
                allValid = false;
            }
        }
        String cssClass = "";
        if (getCSSClass().trim().length() > 0) {
            cssClass = ' ' + getCSSClass();
        }
        out.println(getPrefix());
        if (isWrapped()) {
            if (allValid) {
                out.println("<div class=\"in-wrapper valid" + cssClass + "\">");
            } else {
                out.println("<div class=\"in-wrapper not-valid" + cssClass + "\">");
            }
        }
        String firstFiledClass = " first-field";
        Iterator<Field> ite = fields.iterator();
        while (ite.hasNext()) {
            Field field = ite.next();
            if (field != null) {
                field.setLast(ite.hasNext());
                if (field.getTranslation() != null) {
                    field.setCurrentLocale(ctx.getLocale());
                }
                if (field.isDiplayedInList(ctx) || !asList) {
                    if (field.isViewDisplayed() && field.isPertinent(ctx)) {
                        cssClass = "";
                        if (field.getCSSClass() != null && field.getCSSClass().trim().length() > 0) {
                            cssClass = ' ' + field.getCSSClass();
                        }
                        out.println(field.getFieldPrefix(ctx));
                        if (field.isWrapped()) {
                            out.println("<div class=\"field " + field.getName() + firstFiledClass + cssClass + "\">");
                        }
                        out.println(field.getViewXHTMLCode(ctx));
                        if (field.isWrapped()) {
                            out.println("</div>");
                        }
                        out.println(field.getFieldSuffix(ctx));
                        firstFiledClass = "";
                    }
                }
            }
        }
        if (isWrapped()) {
            out.println("<div class=\"end\"><span>&nbsp;</span></div>");
            out.println("</div>");
        }
        out.println(getSuffix());
        out.close();
        return writer.toString();
    }

    @Override
    public java.util.List<String> getFieldsNames(ContentContext ctx) {
        java.util.List<String> outFields = new LinkedList<String>();
        Collection keys = properties.keySet();
        for (Object keyObj : keys) {
            String key = (String) keyObj;
            if (key.startsWith("field.")) {
                String[] keySplit = key.split("\\.");
                if (keySplit.length > 1) {
                    String name = keySplit[1];
                    if (!outFields.contains(name)) {
                        outFields.add(name);
                    }
                }
            }
        }
        return outFields;
    }

    public void reset() {
        this.fields = null;
    }

    @Override
    public java.util.List<Field> getFields(ContentContext ctx) throws Exception {

        if (this.fields != null) {
            return this.fields;
        }

        StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
        I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
        java.util.List<Field> outFields = new LinkedList<Field>();
        java.util.List<String> fieldExecuted = new LinkedList<String>();
        Collection keys = properties.keySet();
        for (Object keyObj : keys) {
            String key = (String) keyObj;
            if (key.startsWith("field.")) {
                String[] keySplit = key.split("\\.");
                if (keySplit.length > 1) {
                    String name = keySplit[1];
                    if (!fieldExecuted.contains(name)) {
                        Field field = FieldFactory.getField(ctx, this, staticConfig, globalContext, i18nAccess, getProperties(), null, name, getGroup(name), getType(name), getId());
                        if (field != null) {
                            outFields.add(field);
                        }
                        fieldExecuted.add(name);
                    } else {
                        logger.fine("field not found : " + getType(name));
                    }
                }
            }
        }
        Collections.sort(outFields, new FieldOrderComparator());
        this.fields = outFields;
        return outFields;
    }

    @Override
    public Field getField(ContentContext ctx, String name) throws Exception {
        java.util.List<Field> fields = getFields(ctx);
        for (Field field : fields) {
            if (field.getName() != null && field.getName().equals(name)) {
                return field;
            }
        }
        return null;
    }

    @Override
    public String getFieldValue(ContentContext ctx, String name) throws Exception {
        Field field = getField(ctx, name);
        if (field != null) {
            return field.getValue();
        }
        return null;
    }

    public String getCSSClass() {
        return properties.getProperty("component.css-class", "");
    }

    public String getPrefix() {
        return properties.getProperty("component.prefix", "");
    }

    public String getSuffix() {
        return properties.getProperty("component.suffix", "");
    }

    public boolean isWrapped() {
        return StringHelper.isTrue(properties.getProperty("component.wrapped", null), true);
    }

    protected boolean getColumnableDefaultValue() {
        return StringHelper.isTrue(properties.getProperty("component.columnable", null), true);
    }

    private String getDynamicRenderer(ContentContext ctx) {
        String deviceRenderer = properties.getProperty("component.renderer." + ctx.getDevice().getCode());
        if (deviceRenderer != null) {
            return deviceRenderer;
        } else {
            return properties.getProperty("component.renderer", null);
        }
    }

    private String getListRenderer() {
        return properties.getProperty("component.list-renderer", null);
    }

    public String getDataPath() {
        return properties.getProperty("component.data.path", null);
    }

    @Override
    public Map<String, String> getList(ContentContext ctx, String listName, Locale locale) {
        Map<String, String> res = new HashMap<String, String>();
        for (int i = 0; i < 9999; i++) {
            String value = properties.getProperty("list." + listName + "." + i);
            if (value != null) {
                String[] splitedValue = value.split(",");
                if (splitedValue.length > 1) {
                    if (properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage()) != null) {
                        value = properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage());
                    } else {
                        value = splitedValue[1].trim();
                    }
                    res.put(splitedValue[0].trim(), value);
                } else {
                    if (properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage()) != null) {
                        value = properties.getProperty("list." + listName + "." + i + "." + locale.getLanguage());
                    }
                    res.put(value, value);
                }
            }
        }
        return res;
    }

    @Override
    public Map<String, String> getList(ContentContext ctx, String listName) {
        Map<String, String> res = new HashMap<String, String>();
        for (int i = 0; i < 9999; i++) {
            String value = properties.getProperty("list." + listName + "." + i);
            if (value != null) {
                String[] splitedValue = value.split(",");
                if (splitedValue.length > 1) {
                    res.put(splitedValue[0].trim(), splitedValue[1].trim());
                } else {
                    res.put(value, value);
                }
            }
        }
        return res;
    }

    @Override
    protected String getInputName(String field) {
        return field + "-" + getId();
    }

    protected String getType(String field) {
        String type = properties.getProperty("field." + field + ".type");
        if (type != null) {
            return type.trim();
        } else {
            return null;
        }
    }

    protected String getGroup(String field) {
        String type = properties.getProperty("field." + field + ".group");
        if (type != null) {
            return type.trim();
        } else {
            return null;
        }
    }

    public boolean isNotififyCreation(ContentContext ctx) throws ServiceException {
        boolean outNotif = StringHelper.isTrue(properties.getProperty(NOTIFY_CREATION), false);
        if (outNotif) {
            properties.setProperty(NOTIFY_CREATION, "false");
            storeProperties();
            PersistenceService.getInstance(ctx.getGlobalContext()).setAskStore(true);
        }
        return outNotif;
    }

    public String getNotififyPageName(ContentContext ctx) throws ServiceException {
        return properties.getProperty("notify.edit-page");
    }


    @Override
    public String getEditRenderer(ContentContext ctx) {
        return "/jsp/edit/component/dynamicComponent/edit_dynamicComponent.jsp";
    }

    @Override
    protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        Collection<Field> fields = getFields(ctx);
        boolean allValid = true;
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase("info")) {
                MessageRepository messageRepository = MessageRepository.getInstance(ctx);
                messageRepository.setGlobalMessage(new GenericMessage("field could not call info.", GenericMessage.ERROR));
            }
            if (!field.validate()) {
                allValid = false;
            }
        }
        if (allValid) {
            out.println("<div class=\"dynamic-component valid cols\">");
        } else {
            out.println("<div class=\"dynamic-component not-valid cols\">");
        }

        int colSize = 0;
        Iterator<Field> iter = fields.iterator();
        boolean first = true;
        while (iter.hasNext()) {
            Field field = iter.next();
            if (field != null) {
                field.setFirst(first);
                first = false;
                field.setLast(!iter.hasNext());
                if (colSize >= 12) {
                    colSize = 0;
                }
                out.println(field.getOpenRow(ctx));
                if (field.getTranslation() != null) {
                    I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
                    out.println("<fieldset><legend>" + i18nAccess.getText("field.translated") + "</legend>");
                }
                if (field.getName().equalsIgnoreCase("info")) {
                    out.println("<div class=\"alert alert-danger\" role=\"alert\">field could not call 'info'.</div>");
                }
                Collection<Locale> translatedField = new LinkedList<Locale>();
                if (field.getTranslation() == null) {
                    translatedField = new LinkedList<Locale>();
                    translatedField.add(null);
                } else {
                    translatedField = field.getTranslation();
                }
                for (Locale locale : translatedField) {
                    if (locale != null) {
                        out.println("<fieldset><legend>" + locale.getDisplayLanguage(new Locale(GlobalContext.getInstance(ctx.getRequest()).getEditLanguage(ctx.getRequest().getSession()))) + "</legend>");
                    }
                    field.setCurrentLocale(locale);
                    String editXHTML = field.getEditXHTMLCode(ctx);
                    if (editXHTML == null || editXHTML.trim().length() == 0) {
                        out.println("<div class=\"alert alert-danger\" role=\"alert\">field format error : " + field.getName() + ".</div>");
                    } else {
                        out.println(editXHTML);
                    }
                    if (locale != null) {
                        out.println("</fieldset>");
                    }
                }
                if (field.getTranslation() != null) {
                    out.println("</fieldset>");
                }
                if (iter.hasNext()) {
                    out.println("<hr />");
                }
                out.println(field.getCloseRow(ctx));
            }
        }
        out.println("</div>");
        out.close();
        return writer.toString();
    }

    @Override
    public String getType() {
        return properties.getProperty("component.type", "undefined dynamic component");
    }

    public void storeProperties() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            properties.store(out, "component: " + getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String res = new String(out.toByteArray());
        setValue(res);
    }

    private void updateOrder() {
        try {
            List<String> orderKeys = new LinkedList<>();
            for (String key : properties.stringPropertyNames()) {
                if (key.endsWith(".order")) {
                    orderKeys.add(key);
                }
            }
            Collections.sort(orderKeys, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.parseInt(properties.getProperty(o1)) - Integer.parseInt(properties.getProperty(o2));
                }
            });

            int num = 100;
            for (String key : orderKeys) {
                properties.setProperty(key, "" + num);
                num += 100;
            }

            storeProperties();
        } catch (NumberFormatException e) {
            logger.warning("Digital format error for a .order key : " + e.getMessage());
        }
    }

    private List<String> getGroups() {
        List<String> groups = new LinkedList<String>();
        Collection keys = properties.keySet();
        for (Object keyObj : keys) {
            String key = (String) keyObj;
            if (key.endsWith(".group")) {
                String group = properties.getProperty(key);
                if (!groups.contains(group)) {
                    groups.add(group);
                }
            }
        }
        return groups;
    }

    private static int extractNumber(String str) {
        Pattern pattern = Pattern.compile("\\[(\\d+)\\]");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        } else {
            return 0;
        }
    }

    /**
     * size of a group
     * @param ctx
     * @param group
     * @return
     * @throws Exception
     */
    private int getGroupSize(ContentContext ctx, String group) throws Exception {
        AtomicInteger size = new AtomicInteger(0);
        getFields(ctx).forEach(field -> {
            if (!field.getName().endsWith("]") && field.getGroup() != null && field.getGroup().equals(group)) {
                size.incrementAndGet();
            }
        });
        return size.get();
    }

    /**
     * number of group
     * @param ctx
     * @param group
     * @return
     * @throws Exception
     */
    private int getGroupNumber(ContentContext ctx, String group) throws Exception {
        AtomicInteger size = new AtomicInteger(0);
        getFields(ctx).forEach(field -> {
            if (field.getGroup() != null && field.getGroup().equals(group)) {
                size.incrementAndGet();
            }
        });
        return size.get() / getGroupSize(ctx, group);
    }

    private int getGroupNumberMaxOrder(ContentContext ctx, String group) throws Exception {
        return getFields(ctx).stream()
                .filter(field -> field.getGroup() != null && field.getGroup().equals(group))
                .mapToInt(Field::getOrder)
                .max()
                .orElse(0);
    }

    private void deleteGroup(ContentContext ctx, String groupLabel) throws Exception {

        if (groupLabel==null) {
            return;
        }

        List<String> names = new LinkedList<>();

        for (Field field : getFields(ctx)) {
            if (groupLabel.equals(field.getGroupLabel())) {
                names.add(field.getName());
            }
        }

        if (names.size() == 0) {
            throw new RuntimeException("Group not found: " + groupLabel);
        }

        Iterator<String> keys = properties.stringPropertyNames().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            for (String name : names) {
                if (key.startsWith("field." + name + ".")) {
                    properties.remove(key);
                }
            }
        }
        updateOrder();
    }

    private void addGroup(ContentContext ctx, String group) throws Exception {
        int maxGroupNumber = 0;
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("field.") && key.endsWith(".group")) {
                if (properties.getProperty(key).equals(group)) {
                    int number = extractNumber(key);
                    if (number > maxGroupNumber) {
                        maxGroupNumber = number;
                    }
                }
            }
        }

        // get latest order
        int index = 1;
        int max = getGroupNumberMaxOrder(ctx, group);
        for (String key : new LinkedList<>(properties.stringPropertyNames())) {
            if (key.startsWith("field.")) {
                String newKey = key;
                String[] parts = newKey.split("\\.", 4);
                String name = parts[1];
                Field field = getField(ctx, name);
                if (field.getGroup() != null) {
                    if (field.getGroup().equals(group)) {
                        if (extractNumber(key) > 0) {
                            name = name.replaceFirst("\\[(\\d+)\\]", "[" + (maxGroupNumber + 1) + "]");
                        } else {
                            name = name + "[" + (maxGroupNumber + 1) + "]";
                        }
                        if (parts.length >= 3) {
                            parts[1] = name;
                            newKey = String.join(".", parts);  // build string with modified segments
                        } else {
                            logger.warning("The format of the channel does not correspond to what is expected.");
                            return;  // stop if bad format
                        }
                        if (newKey.endsWith(".order")) {
                            properties.setProperty(newKey, "" + (max + index++));
                        } else {
                            properties.setProperty(newKey, properties.getProperty(key));
                        }
                    }
                }
            }
        }
        storeProperties();
        //updateOrder();
    }

    @Override
    public String performEdit(ContentContext ctx) throws Exception {

        java.util.List<Field> fieldsName = getFields(ctx);

        RequestService rs = RequestService.getInstance(ctx.getRequest());

        performColumnable(ctx);

        boolean valid = true;
        List<String> errorField = new LinkedList<String>();
        Iterator<Field> iter = fieldsName.iterator();
        while (iter.hasNext()) {
            Field field = iter.next();
            Collection<Locale> languages;
            if (field.getTranslation() == null) {
                languages = new LinkedList<Locale>();
                languages.add(null);
            } else {
                languages = field.getTranslation();
            }

            for (Locale locale : languages) {
                field.setCurrentLocale(locale);
                boolean modify = field.process(ctx);
                if (modify) {
                    setModify();
                    if (field.isNeedRefresh()) {
                        setNeedRefresh(true);
                    }
                }
            }

            if (!field.validate()) {
                valid = false;
                errorField.add(field.getUserLabel(ctx, new Locale(ctx.getGlobalContext().getEditLanguage(ctx.getRequest().getSession()))));
            }
        }

        /** add group **/
        if (rs.getParameter("addGroup") != null) {
            addGroup(ctx, rs.getParameter("addGroup"));
            ctx.setNeedRefresh(true);
        }

        /** delete group **/
        if (rs.getParameter("deleteGroup") != null) {
            deleteGroup(ctx, rs.getParameter("deleteGroup"));
            ctx.setNeedRefresh(true);
        }

        if (isModify()) {
            storeProperties();
            reset();
        }

        if (!valid) {
            ctx.setClosePopup(false);
            I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
            return i18nAccess.getText("content.dynamic-component.error.global") + " (" + StringHelper.collectionToString(errorField, ",") + ')';
        }

        groups = null;

        return null;
    }



    @Override
    public void setValue(String inContent) {
        super.setValue(inContent);
        reloadProperties();
    }

    @Override
    public String getHexColor() {
        return properties.getProperty("component.color", IContentVisualComponent.DYN_COMP_COLOR);
    }

    public StructuredProperties getProperties() {
        return properties;
    }

    public void setProperties(StructuredProperties properties) {
        this.properties = properties;
    }

    public void setConfigProperties(Properties properties) {
        this.configProperties = properties;
    }

    public Properties getConfigProperties() {
        return configProperties;
    }

    @Override
    public IContentVisualComponent newInstance(ComponentBean bean, ContentContext newCtx, MenuElement page) throws Exception {
        DynamicComponent res = (DynamicComponent) this.clone();
        StructuredProperties newProp = new StructuredProperties(true);
        if (getConfigProperties() != null) {
            newProp.putAll(getConfigProperties());
        }
        res.setProperties(newProp); // transfert meta-data of
        // dynamiccomponent
        res.setPage(page);
        res.init(bean, newCtx);
        return res;
    }

    @Override
    public String getComponentLabel(ContentContext ctx, String lg) {
        if (properties == null) {
            return super.getComponentLabel(ctx, lg);
        }
        String langLabel = properties.getProperty("component.label." + lg);
        if (langLabel != null) {
            return langLabel;
        }
        String genericLabel = properties.getProperty("component.label");
        if (genericLabel != null) {
            return genericLabel;
        }
        return super.getComponentLabel(ctx, lg);
    }

    @Override
    public String getKey() {
        return getType();
    }

    @Override
    public boolean isRealContent(ContentContext ctx) {
        MenuElement page = getPage();
        if (getMirrorWrapper(ctx, this) != null) {
            page = getMirrorWrapper(ctx, this).getPage();
        }
        if (!page.isActive(ctx)) {
            return false;
        }
        try {
            for (Field field : getFields(ctx)) {
                if (field.isRealContent(ctx)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isContentCachable(ContentContext ctx) {
        if (!StringHelper.isTrue(properties.getProperty("component.cachable"))) {
            return false;
        }
        try {
            java.util.List<Field> fieldsName = getFields(ctx);
            for (Field field : fieldsName) {
                if (!field.isContentCachable()) {
                    return false;
                }
                if (field instanceof MetaField) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return true;
    }

    @Override
    public boolean contains(ContentContext ctx, String uri) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IStaticContainer) {
                    if (((IStaticContainer) field).contains(ctx, uri)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Collection<Resource> getAllResources(ContentContext ctx) {
        Collection<Resource> outResources = new LinkedList<Resource>();
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IStaticContainer) {
                    outResources.addAll(((IStaticContainer) field).getAllResources(ctx));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outResources;
    }

    @Override
    public boolean renameResource(ContentContext ctx, File oldName, File newName) {
        boolean outRename = false;
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IStaticContainer) {
                    if (((IStaticContainer) field).renameResource(ctx, oldName, newName)) {
                        outRename = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (outRename) {
            setModify();
            storeProperties();
        }
        return outRename;
    }

    @Override
    public String getLabel(ContentContext ctx) {
        return properties.getProperty("component.label-" + ctx.getRequestContentLanguage(), properties.getProperty("component.label", properties.getProperty("component.type")));
    }

    @Override
    public boolean isContentTimeCachable(ContentContext ctx) {
        return false;
    }

    @Override
    public Collection<Link> getAllResourcesLinks(ContentContext ctx) {
        Collection<Link> outResources = new LinkedList<Link>();
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IStaticContainer) {
                    outResources.addAll(((IStaticContainer) field).getAllResourcesLinks(ctx));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outResources;
    }

    @Override
    public Date getDate(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IDate) {
                    return ((IDate) field).getDate(ctx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isValidDate(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IDate) {
                    if (((IDate) field).isValidDate(ctx)) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int getPopularity(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IStaticContainer) {
                    return ((IStaticContainer) field).getPopularity(ctx);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int contentHashCode() {
        StringBuffer value = new StringBuffer();
        List<String> keys = new LinkedList(properties.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            value.append(key);
            value.append("=");
            value.append(properties.get(key));
            value.append('/');
        }
        return value.toString().hashCode();
    }

    @Override
    public String getTextForSearch(ContentContext ctx) {
        StringBuffer outText = new StringBuffer();
        for (Object key : properties.keySet()) {
            outText.append(properties.get(key));
            outText.append(' ');
        }
        return outText.toString();
    }

    @Override
    public boolean initContent(ContentContext ctx) throws Exception {
        reloadProperties();
        boolean outInit = false;
        for (Field field : getFields(ctx)) {
            if (field.initContent(ctx)) {
                outInit = true;
            }
        }
        storeProperties();
        return outInit;
    }

    public void reload(ContentContext ctx) throws Exception {
        for (Field field : getFields(ctx)) {
            field.reload(ctx);
        }
    }

    @Override
    public String getURL(ContentContext ctx) throws Exception {
        for (Field field : getFields(ctx)) {
            if (field instanceof ILink) {
                return ((ILink) field).getURL(ctx);
            }
        }
        return null;
    }

    @Override
    public boolean isLinkValid(ContentContext ctx) throws Exception {
        return StringHelper.isEmpty(getURL(ctx));
    }

    protected FieldImage getImageField(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof FieldImage) {
                    return (FieldImage) field;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getImageDescription(ContentContext ctx) {
        FieldImage image = getImageField(ctx);
        if (image != null) {
            return image.getLabel();
        }
        return null;
    }

    @Override
    public String getResourceURL(ContentContext ctx) {
        FieldImage image = getImageField(ctx);
        if (image != null) {
            try {
                return ((FieldImage) image.getReference(ctx)).getFileURL(ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public String getImageLinkURL(ContentContext ctx) {
        FieldImage image = getImageField(ctx);
        if (image != null) {
            image.getCurrentLink();
        }
        return null;
    }

    @Override
    public boolean isImageValid(ContentContext ctx) {
        FieldImage field = getImageField(ctx);
        if (field != null) {
            return StringHelper.isImage(field.getCurrentFile());
        }
        return false;
    }

    @Override
    public int getLabelLevel(ContentContext ctx) {
        if (!StringHelper.isEmpty(getTextTitle(ctx))) {
            return HIGH_LABEL_LEVEL;
        }
        return 0;
    }

    @Override
    public String getTextTitle(ContentContext ctx) {
        if (textTitle != null) {
            return textTitle;
        }
        String title = null;
        try {
            for (Field field : getFields(ctx)) {
                if (field.isTitle()) {
                    title = StringHelper.neverNull(title).trim()+ ' ' + StringHelper.neverNull(field.getValue()).trim();
                }
            }
            if (StringHelper.isEmpty(title)) {
                for (Field field : getFields(ctx)) {
                    if (field instanceof FieldWysiwyg) {
                        String html = field.getValue();
                        if (html != null && html.length()>4) {
                            Document doc = Jsoup.parse(html);
                            Element h1 = doc.selectFirst("h1");
                            if (h1 != null) {
                                title = h1.text();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        textTitle = title;
        return textTitle;
    }

    @Override
    public String getTextLabel(ContentContext ctx) {
        return getTextTitle(ctx);
    }

    @Override
    public String getSpecificClass(ContentContext ctx) {
        return "dynamic-component";
    }

    @Override
    public String getPageDescription(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field.getPageDescription() != null) {
                    return field.getPageDescription();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getSubTitle(ContentContext ctx) {
        try {
            for (int i = 2; i < 6; i++) {
                for (Field field : getFields(ctx)) {
                    if (field.getType().equals("h" + i)) {
                        return field.getValue();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getSubTitleLevel(ContentContext ctx) {
        int outHierarchy = 0;
        try {
            for (int i = 2; i < 6; i++) {
                for (Field field : getFields(ctx)) {
                    if (field.getType().equals("h" + i)) {
                        outHierarchy = i;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outHierarchy;
    }

    @Override
    public int getPriority(ContentContext ctx) {
        if (getProperties().getProperty("image.priority", null) == null) {
            return 5;
        } else {
            return Integer.parseInt(getProperties().getProperty("image.priority", null));
        }
    }

    @Override
    public boolean isFieldContainer(ContentContext ctx) {
        return true;
    }

    @Override
    public String getContainerType(ContentContext ctx) {
        return getType();
    }

    @Override
    public String getXHTMLId(ContentContext ctx) {
        return getType() + '-' + getId();
    }

    public String getDynamicId() {
        return properties.getProperty(DYNAMIC_ID_KEY);
    }

    public void setDynamicId(String id) {
        properties.setProperty(DYNAMIC_ID_KEY, id);
        storeProperties();
        setModify();
    }

    @Override
    public int getComplexityLevel(ContentContext ctx) {
        String level = properties.getProperty("complexity");
        if (level == null) {
            return COMPLEXITY_STANDARD;
        } else {
            return Integer.parseInt(level);
        }
    }

    @Override
    public Map<String, Object> getContentAsMap(ContentContext ctx) throws Exception {
        Map<String, Object> content = super.getContentAsMap(ctx);
        List<Map<String, Object>> contentArray = new LinkedList<Map<String, Object>>();
        for (Field field : getFields(ctx)) {
            content.put(field.getName(), field.getContentAsMap(ctx));
        }
        content.put("value", contentArray);
        return content;
    }

    @Override
    public List<File> getFiles(ContentContext ctx) {
        // TODO need real impplementation
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getDirSelected(ContentContext ctx) {
        return null;
    }

    @Override
    public void setDirSelected(String dir) {
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
    public Collection<String> getExternalResources(ContentContext ctx) {
        String resources = properties.getProperty("resources");
        Template template = null;
        try {
            template = ctx.getCurrentTemplate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (resources != null && template != null) {
            List<String> linkResource = StringHelper.stringToCollection(resources, ",");
            List<String> outResource = new LinkedList<String>();
            for (String uri : linkResource) {
                if (uri.startsWith("/")) {
                    try {
                        outResource.add(URLHelper.createStaticTemplateURLWithoutContext(ctx, template, uri));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return outResource;
        } else {
            return super.getExternalResources(ctx);
        }
    }

    @Override
    public boolean isRestMatch(ContentContext ctx, Map<String, String> params) {
        if (params.size() == 0) {
            return super.isRestMatch(ctx, params);
        } else {
            try {
                for (Field field : getFields(ctx)) {
                    if (params.get(field.getName()) != null) {
                        if (!StringHelper.matchSimplePattern(field.getValue(), params.get(field.getName()), true)) {
                            return false;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override
    public String getIcon() {
        String defaultFont = "bi bi-postcard";
        if (StringHelper.isHTMLStatic(properties.getProperty("component.renderer", null))) {
            defaultFont = "bi bi-code-slash";
        }
        return properties.getProperty("icon", defaultFont);
    }

    protected boolean isAutoDeletable() {
        return true;
    }

    @Override
    protected boolean isValueTranslatable() {
        return true;
    }

    public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
        if (!isValueTranslatable()) {
            return false;
        } else {
            try {
                for (Field field : getFields(ctx)) {
                    field.transflateFrom(ctx, translator, lang);
                }
                setModify();
                updateCache();
                storeProperties();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public boolean isMobileOnly(ContentContext ctx) {
        return false;
    }

    @Override
    public LocalDateTime getTimeRangeStart(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IDate && field.getName().contains("start")) {
                    IDate date = (IDate) field;
                    if (date.getDate(ctx) != null) {
                        return date.getDate(ctx).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public LocalDateTime getTimeRangeEnd(ContentContext ctx) {
        try {
            for (Field field : getFields(ctx)) {
                if (field instanceof IDate && field.getName().contains("end")) {
                    IDate date = (IDate) field;
                    if (date.getDate(ctx) != null) {
                        return date.getDate(ctx).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}