/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.component.config.ComponentConfig;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComplexPropertiesLink;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.meta.Tags;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.taxonomy.ITaxonomyContainer;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.Comparator.*;
import org.javlo.helper.*;
import org.javlo.i18n.I18nAccess;
import org.javlo.module.content.Edit;
import org.javlo.navigation.MenuElement;
import org.javlo.navigation.PageBean;
import org.javlo.navigation.ReactionMenuElementComparator;
import org.javlo.navigation.ReactionSmartPageBeanComparator;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.utils.TimeRange;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * list of links to a subset of pages.
 * <h4>exposed variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link PageBean} pages : list of pages selected to display.</li>
 * <li>{@link String} title : title of the page list. See
 * {@link #getContentTitle}</li>
 * <li>{@link PageReferenceComponent} comp : current component.</li>
 * <li>{@link String} firstPage : first page rendered in xHTML.</li>
 * </ul>
 *
 * @author pvandermaesen
 */
public class PageReferenceComponent extends ComplexPropertiesLink implements IAction, ITaxonomyContainer {

    public static final String MOUNT_FORMAT = "MMMM yyyy";

    private static Logger logger = Logger.getLogger(PageReferenceComponent.class.getName());

    public static class PageEvent {
        private Date start = null;
        private Date end = null;

        public Date getStart() {
            return start;
        }

        public void setStart(Date startDate) {
            this.start = startDate;
        }

        public Date getEnd() {
            return end;
        }

        public void setEnd(Date endDate) {
            this.end = endDate;
        }

    }

    public static class PagesStatus {
        private int totalSize = 0;
        private int realContentSize = 0;

        public PagesStatus(int totalSize, int realContentSize) {
            super();
            this.totalSize = totalSize;
            this.realContentSize = realContentSize;
        }

        public int getRealContentSize() {
            return realContentSize;
        }

        public int getTotalSize() {
            return totalSize;
        }

        public void setRealContentSize(int realContentSize) {
            this.realContentSize = realContentSize;
        }

        public void setTotalSize(int totalSize) {
            this.totalSize = totalSize;
        }

    }

    public static final String TYPE = "page-reference";

    private static final String PAGE_REF_PROP_KEY = "page-ref";

    private static final String PAGE_START_PROP_KEY = "page-start";

    private static final String PAGE_END_PROP_KEY = "page-end";

    private static final String ORDER_KEY = "order";

    private static final String PARENT_NODE_PROP_KEY = "parent-node";

    private static final String TAG_KEY = "tag";

    private static final String DEFAULT_SELECTED_PROP_KEY = "is-def-selected";

    private static final String PAGE_SEPARATOR = ";";

    private static final String STATIC = "static";

    private static final String INTERACTIVE = "interactive";

    private static final List<String> TIME_SELECTION_OPTIONS = Arrays.asList(new String[]{"before", "inside", "after"});

    private static final String TIME_SELECTION_KEY = "time-selection";

    private static final String START_DATE_KEY = "start-date";

    private static final String END_DATE_KEY = "end-date";

    private static final String DISPLAY_FIRST_PAGE_KEY = "display-first-page";

    private static final String CHANGE_ORDER_KEY = "reverse-order";

    private static final String SESSION_TAXONOMY_KEY = "session-taxonomy";

    private static final String DYNAMIC_ORDER_KEY = "dynamic-order";

    private static final String WIDTH_EMPTY_PAGE_PROP_KEY = "width_empty";
    private static final String TAXONOMY = "taxonomy";

    private static final String ONLY_EVENT = "only_event";

    private static final String INTRANET_MODE_KEY = "intranet_mode";

    private static final int MAX_PAGES = 250;

    public static final Integer getCurrentMonth(HttpSession session) {
        return (Integer) session.getAttribute("___current_month");
    }

    /************/
    /** ACTION **/
    /************/

    public static final Integer getCurrentYear(HttpSession session) {
        return (Integer) session.getAttribute("___current-year");
    }

    public static final String performCalendar(HttpServletRequest request, HttpServletResponse response) {
        RequestService requestService = RequestService.getInstance(request);

        String newYear = requestService.getParameter("year", null);
        if (newYear != null) {
            setCurrentYear(request.getSession(), Integer.parseInt(newYear));
        }
        String newMonth = requestService.getParameter("month", null);
        if (newMonth != null) {
            setCurrentMonth(request.getSession(), Integer.parseInt(newMonth));
        }

        return null;
    }

    public static final void setCurrentMonth(HttpSession session, int currentMonth) {
        session.setAttribute("___current_month", currentMonth);
    }

    public static final void setCurrentYear(HttpSession session, int currentYear) {
        session.setAttribute("___current-year", currentYear);
    }

    private static Collection<String> extractCommandFromFilter(String filter) {
        if (filter == null || filter.trim().length() == 0) {
            return Collections.emptyList();
        }
        Collection<String> commands = new HashSet<String>();
        String[] filterSplited = StringUtils.split(filter, ' ');
        for (String command : filterSplited) {
            command = command.trim();
            if (command.startsWith(":") && command.length() > 2) {
                commands.add(command.substring(1));
            }
        }
        return commands;
    }

    private static String removeCommandFromFilter(String filter) {
        if (filter == null || filter.trim().length() == 0) {
            return filter;
        }
        StringBuffer outFilter = new StringBuffer();
        String[] filterSplited = StringUtils.split(filter, ' ');
        for (String command : filterSplited) {
            String trimCommand = command.trim();
            if (!(trimCommand.startsWith(":") && trimCommand.length() > 2)) {
                outFilter.append(command);
                outFilter.append(' ');
            }
        }
        return outFilter.toString().trim();
    }

    private boolean validPageForCommand(ContentContext ctx, MenuElement page, Collection<MenuElement> currentSelection, Collection<String> commands) throws Exception {
        for (String command : commands) {
            if (command.equals("checked")) {
                if (!currentSelection.contains(page)) {
                    return false;
                }
            } else if (command.equals("unchecked")) {
                if (currentSelection.contains(page)) {
                    return false;
                }
            }
            if (command.equals("visible")) {
                if (!page.isVisible()) {
                    return false;
                }
            } else if (command.equals("unvisible")) {
                if (page.isVisible()) {
                    return false;
                }
            } else if (command.startsWith("depth")) {
                for (int i = 0; i < 100; i++) {
                    if (command.equals("depth" + i)) {
                        if (page.getDepth() != i) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * filter the page
     *
     * @param ctx  current contentcontext
     * @param page a page
     * @return true if page is accepted
     * @throws Exception
     */
    protected boolean filterPage(ContentContext ctx, MenuElement page, Collection<MenuElement> currentSelection, Collection<String> commands, String filter, boolean widthUnactive) throws Exception {
        if (!page.isActive(ctx) && !widthUnactive) {
            return false;
        }

        // Collection<String> commands = extractCommandFromFilter(filter);

        if (commands.contains("all")) {
            return true;
        }
        if (!validPageForCommand(ctx, page, currentSelection, commands)) {
            return false;
        }

        if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
            if (this.getTaxonomy() != null && this.getTaxonomy().size() > 0) {
                if (page.getTaxonomy() == null || page.getTaxonomy().size() == 0) {
                    return false;
                }
                if (!ctx.getGlobalContext().getAllTaxonomy(ctx).isMatch(page, this)) {
                    return false;
                }
            }
            if (isSessionTaxonomy(ctx)) {
                if (!ctx.getGlobalContext().getAllTaxonomy(ctx).isMatchWidthParent(page, TaxonomyService.getSessionFilter(ctx))) {
                    return false;
                }
            }
        }

        if (ctx.getRequest().getParameter("lang") != null) {
            ctx.setAllLanguage(ctx.getRequest().getParameter("lang"));
        }

        // filter = removeCommandFromFilter(filter);
        String pageData = page.getName() + ' ' + page.getTitle(ctx) + ' ' + ' ' + page.getLabel(ctx);
        if (filter != null && !(pageData).toLowerCase().contains(filter.toLowerCase())) {
            return false;
        }
        if (!page.isChildOf(getParentNode(ctx))) {
            return false;
        }
        if (page.getEvent(ctx) == null && isOnlyEvent()) {
            return false;
        }

        if (getTimeSelection() != null) {
            Date today = TimeHelper.convertRemoveAfterDay(Calendar.getInstance()).getTime();
            boolean timeAccept = false;
            if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(0))) {
                if (page.getTimeRange(ctx).isAfter(today)) {
                    timeAccept = true;
                }
            }
            if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(1))) {
                if (page.getTimeRange(ctx).isInside(today)) {
                    timeAccept = true;
                }
            }
            if (getTimeSelection().contains(TIME_SELECTION_OPTIONS.get(2))) {
                if (page.getTimeRange(ctx).isBefore(today)) {
                    timeAccept = true;
                }
            }
            if (!timeAccept) {
                return false;
            }
        }

        String startDateStr = properties.getProperty(START_DATE_KEY);
        String endDateStr = properties.getProperty(END_DATE_KEY);
        if (!StringHelper.isEmpty(startDateStr) || !StringHelper.isEmpty(endDateStr)) {
            Date startDate = StringHelper.parseInputDate(startDateStr);
            Date endDate = StringHelper.parseInputDate(endDateStr);
            TimeRange tr = new TimeRange(startDate, endDate);
            if (!tr.isInside(page.getContentDateNeverNull(ctx))) {
                return false;
            }
        }

        boolean out = false;

        if (getSelectedTag(ctx).size() == 0) {
            out = true;
        }
        ContentContext lgDefaultCtx = new ContentContext(ctx);
        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
        Iterator<String> contentLg = globalContext.getContentLanguages().iterator();
        while (page.getContentByType(lgDefaultCtx, Tags.TYPE).size() == 0 && contentLg.hasNext()) {
            String lg = contentLg.next();
            lgDefaultCtx.setContentLanguage(lg);
            lgDefaultCtx.setRequestContentLanguage(lg);
        }
        if (!Collections.disjoint(page.getTags(lgDefaultCtx), getSelectedTag(ctx))) {
            out = true;
        }

        /** interactive **/
        if (out && isInteractive()) {
            RequestService rs = RequestService.getInstance(ctx.getRequest());
            String query = rs.getParameter("query");
            if (!StringHelper.isEmpty(query)) {
                String pageInfo = page.getTitle(ctx) + ' ' + page.getDescription(ctx) + ' ' + page.getTaxonomy();
                return StringHelper.isContains(pageInfo, query);
            }
        }
        return out;
    }

    protected boolean isInteractive() {
        return getStyle() != null && getStyle().equals(INTERACTIVE);
    }

    protected String getCompInputName() {
        return "comp_" + getId();
    }

    @Override
    public int getComplexityLevel(ContentContext ctx) {
        return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
    }

    private String getContentTitle() {
        return properties.getProperty("content-title", "");
    }

    private String getOnlyDepth() {
        return properties.getProperty("only-depth", "");
    }

    private void setOnlyDepth(String value) {
        if (StringHelper.isDigit(value)) {
            properties.setProperty("only-depth", value);
        } else if (StringHelper.isEmpty(value)) {
            properties.remove("only-depth");
        }
    }

    public boolean isPopup() {
        return StringHelper.isTrue(properties.getProperty("popup", null), false);
    }

    public boolean isRefDefaultLang() {
        return StringHelper.isTrue(properties.getProperty("refdeflang", null), false);
    }

    public boolean isDisplayDate() {
        return StringHelper.isTrue(properties.getProperty("displaydate", null), true);
    }

    private void setPopup(boolean popup) {
        properties.setProperty("popup", "" + popup);
    }

    private void setDisplayDate(boolean displayDate) {
        properties.setProperty("displaydate", "" + displayDate);
    }

    public String getCSSClassName(ContentContext ctx) {
        return getComponentCssClass(ctx);
    }

    protected String getDefaultSelectedInputName() {
        return "default-selected-" + getId();
    }

    @Override
    protected String getDisplayAsInputName() {
        return "display-as-" + getId();
    }

    protected boolean isUITimeSelection(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("ui.time-selection", null), true);
    }

    protected boolean isDateRangeSelection(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("ui.date-range", null), true);
    }

    protected boolean isUIFullDisplayFirstPage(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("ui.full-display-first-page", null), false);
    }

    protected boolean isUIFilterOnEditUsers(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("ui.filter-on-edit-users", null));
    }

    protected boolean isUILargeSorting(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("ui.large-sorting", null), true);
    }

    @Override
    public String getEditXHTMLCode(ContentContext ctx) throws Exception {

        ContentService content = ContentService.getInstance(ctx.getRequest());
        MenuElement menu = content.getNavigation(ctx);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);

        I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

        String wrapperId = "pageref-wrapper-" + getId();

        out.println("<div class=\"row\"><div class=\"col-sm-8\">");
        out.println("<div class=\"line\">");
        out.println("<label for=\"" + getInputNameTitle() + "\">" + i18nAccess.getText("global.title") + " : </label>");
        out.println("<input type=\"text\" id=\"" + getInputNameTitle() + "\" name=\"" + getInputNameTitle() + "\" value=\"" + getContentTitle() + "\"  />");
        out.println("</div>");
        out.println("</div><div class=\"col-sm-4\">");
        if (!ctx.getContentLanguage().equals(ctx.getGlobalContext().getDefaultLanguage())) {

            String js = "if(this.checked) {document.getElementById('" + wrapperId + "').classList.add('hidden');} else {document.getElementById('" + wrapperId + "').classList.remove('hidden');}";

            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getCheckbox(getInputRefDefaultLang(), isRefDefaultLang(), js));
            out.println("<label for=\"" + getInputRefDefaultLang() + "\">" + i18nAccess.getText("content.page-teaser.link-default-language", "popup") + "</label></div>");
        }
        out.println("</div></div>");

        out.println("<div id=\"" + wrapperId + "\" class=\"" + (isRefDefaultLang() ? "hidden" : "") + "\">");
        out.println("<div class=\"row\"><div class=\"col-sm-8\">");
        out.println("<input type=\"hidden\" name=\"" + getCompInputName() + "\" value=\"true\" />");

        out.println("<fieldset class=\"config\">");
        out.println("<legend>" + i18nAccess.getText("global.config") + "</legend>");

        out.println("<div class=\"row\">");

        /* rendering */
        out.println("<div class=\"col-md-6\">");

        if (isUIFullDisplayFirstPage(ctx)) {
            /* first page full */
            out.println("<div class=\"line\">");
            String selected = "";
            if (isDisplayFirstPage()) {
                selected = " checked=\"checked\"";
            }
            out.println("<input type=\"checkbox\" name=\"" + getInputFirstPageFull() + "\"" + selected + " />");
            out.println("<label for=\"" + getInputFirstPageFull() + "\">" + i18nAccess.getText("content.display-first-page") + "</label>");
            out.println("</div>");
        }

        out.println("<div class=\"line\">");
        out.println(XHTMLHelper.getCheckbox(getInputPopup(), isPopup()));
        out.println("<label for=\"" + getInputPopup() + "\">" + i18nAccess.getText("content.page-teaser.popup", "popup") + "</label></div>");

        out.println("<div class=\"line\">");
        out.println(XHTMLHelper.getCheckbox(getInputDisplayDate(), isDisplayDate()));
        out.println("<label for=\"" + getInputPopup() + "\">" + i18nAccess.getText("content.page-teaser.display-date", "display date") + "</label></div>");

        if (isUIFilterOnEditUsers(ctx)) {
            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getCheckbox(getIntranetModeInputName(), isIntranetMode()));
            out.println("<label for=\"" + getIntranetModeInputName() + "\">" + i18nAccess.getText("content.intranet-mode") + "</label></div>");
        }

        out.println("</div>");

        /* list selection */
        out.println("<div class=\"col-md-6\">");

        out.println("<div class=\"line\">");
        out.println("<label for=\"" + getInputNameOnlyDepth() + "\">" + i18nAccess.getText("content.page-teaser.only-depth", "only depth") + " : </label>");
        out.println("<input type=\"text\" id=\"" + getInputNameOnlyDepth() + "\" name=\"" + getInputNameOnlyDepth() + "\" value=\"" + getOnlyDepth() + "\"  />");
        out.println("</div>");

        out.println("<div class=\"line\">");
        out.println(XHTMLHelper.getCheckbox(getDefaultSelectedInputName(), isDefaultSelected()));
        out.println("<label for=\"" + getDefaultSelectedInputName() + "\">" + i18nAccess.getText("content.page-teaser.default-selected") + "</label></div>");

        out.println("<div class=\"line\">");
        out.println(XHTMLHelper.getCheckbox(getWidthEmptyPageInputName(), isWidthEmptyPage()));
        out.println("<label for=\"" + getWidthEmptyPageInputName() + "\">" + i18nAccess.getText("content.page-teaser.width-empty-page") + "</label></div>");

        out.println("<div class=\"line\">");
        out.println(XHTMLHelper.getCheckbox(getEventInputName(), isOnlyEvent()));
        out.println("<label for=\"" + getEventInputName() + "\">" + i18nAccess.getText("content.page-teaser.event") + "</label></div>");

        out.println("</div>");

        out.println("</div>"); // /row

        /* tag filter */
        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
        if (globalContext.isTags() && globalContext.getTags().size() > 0) {
            out.println("<div class=\"line-inline\">");
            out.println("<label class=\"main\" for=\"" + getTagsInputName() + "\">" + i18nAccess.getText("content.page-teaser.tag") + " : </label>");
            for (String tag : globalContext.getTags()) {
                String id = tag + getId();
                String checked = "";
                if (getSelectedTag(ctx).contains(tag)) {
                    checked = " checked=\"checked\"";
                }
                out.println("<input type=\"checkbox\" id=\"" + id + "\" name=\"" + getTagsInputName() + "\"" + checked + " value=\"" + tag + "\"/>");
                out.println("<label for=\"" + id + "\">" + tag + "</label>");

            }
            // out.println(XHTMLHelper.getInputOneSelectFirstEnpty(getTagsInputName(),
            // globalContext.getTags(), getSelectedTag(ctx)));
            out.println("</div>");
        }

        /* parent node */
        out.println("<div class=\"line\"><div class=\"row\"><div class=\"col-xs-10\">");
        out.println("<label for=\"" + getParentNodeInputName() + "\">" + i18nAccess.getText("content.page-teaser.parent-node") + " : </label>");
        out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, menu, getParentNodeInputName(), getParentNode(ctx), true));
        out.println("</div><div class=\"col-xs-2\">");
        out.println("<input type=\"button\" class=\"btn btn-default btn-xs\" onclick=\"jQuery('#" + getParentNodeInputName() + "').val('" + ctx.getCurrentPage().getPath() + "');\" value=\"" + i18nAccess.getText("global.current-page") + "\" >");
        out.println("</div></div></div>");

        /* sequence of pages */
        out.println("<div class=\"line-inline first\">");
        out.println("<label for=\"" + getFirstPageNumberInputName() + "\">" + i18nAccess.getText("content.page-teaser.start-page") + " : </label>");
        out.println("<input id=\"" + getFirstPageNumberInputName() + "\" name=\"" + getFirstPageNumberInputName() + "\" value=\"" + getFirstPageNumber() + "\"/>");
        out.println("<label for=\"" + getLastPageNumberInputName() + "\">" + i18nAccess.getText("content.page-teaser.end-page") + " : </label>");
        String lastValue = "" + getLastPageNumber();
        if (getLastPageNumber() == Integer.MAX_VALUE) {
            lastValue = "";
        }
        out.println("<input id=\"" + getLastPageNumberInputName() + "\" name=\"" + getLastPageNumberInputName() + "\" value=\"" + lastValue + "\"/>");
        out.println("</div>");

        out.println("<div class=\"row\"><div class=\"col-md-6\">");

        if (isDateRangeSelection(ctx)) {
            out.println("<div class=\"line-inline\">");
            out.println("<label>" + i18nAccess.getText("global.from") + " : ");
            out.println("<input type=\"date\" name=\"" + getInputName(START_DATE_KEY) + "\" value=\"" + getStartDate() + "\" /></label>");
            out.println("<label>" + i18nAccess.getText("global.to") + " : ");
            out.println("<input type=\"date\" name=\"" + getInputName(END_DATE_KEY) + "\" value=\"" + getEndDate() + "\" /></label>");
            out.println("</div>");
        }

        out.println("</div><div class=\"col-md-6\">");
        /* time selection */
        if (isUITimeSelection(ctx)) {
            out.println("<div class=\"line-inline\">");
            out.println("<label>" + i18nAccess.getText("content.page-teaser.time-selection") + " : </label>");
            for (String option : getTimeSelectionOptions()) {
                String selected = "";
                if (getTimeSelection().contains(option)) {
                    selected = " checked=\"checked\"";
                }
                out.println("<input type=\"checkbox\" name=\"" + getTimeSelectionInputName(option) + "\"" + selected + " />");
                out.println("<label for=\"" + getTimeSelectionInputName(option) + "\">" + i18nAccess.getText("content.page-teaser." + option, option) + "</label>");
            }
            out.println("</div>");
        }

        out.println("</div></div>");

        out.println("</fieldset>");

        out.println("</div><div class=\"col-sm-4\">");

        out.println("<fieldset class=\"order\">");
        out.println("<legend>" + i18nAccess.getText("global.order") + "</legend>");

        out.println("<div class=\"line dynamic\">");
        out.println(XHTMLHelper.getCheckbox(getDynamicOrderInput(), isDynamicOrder(ctx)));
        out.println("<label for=\"" + getDynamicOrderInput() + "\">" + i18nAccess.getText("content.page-teaser.dynamic-order") + "</label></div>");

        out.println("<div class=\"line reverse\">");
        out.println(XHTMLHelper.getCheckbox(getReverseOrderInput(), isReverseOrder(ctx)));
        out.println("<label for=\"" + getReverseOrderInput() + "\">" + i18nAccess.getText("content.page-teaser.reverse-order") + "</label></div>");

        out.println("<div class=\"line no\">");
        out.println(XHTMLHelper.getRadio(getOrderInputName(), "no-order", getOrder()));
        out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.no-order") + "</label></div>");
        out.println("<div class=\"line date\">");
        out.println(XHTMLHelper.getRadio(getOrderInputName(), "date", getOrder()));
        out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.order-date") + "</label></div>");
        if (isUILargeSorting(ctx)) {
            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getRadio(getOrderInputName(), "creation", getOrder()));
            out.println("<label for=\"date\">" + i18nAccess.getText("content.page-teaser.order-creation") + "</label></div>");
            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getRadio(getOrderInputName(), "visit", getOrder()));
            out.println("<label for=\"visit\">" + i18nAccess.getText("content.page-teaser.order-visit") + "</label></div>");
            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getRadio(getOrderInputName(), "popularity", getOrder()));
            out.println("<label for=\"popularity\">" + i18nAccess.getText("content.page-teaser.order-popularity") + "</label></div>");
            out.println("<div class=\"line\">");
            out.println(XHTMLHelper.getRadio(getOrderInputName(), "content", getOrder()));
            out.println("<label for=\"popularity\">" + i18nAccess.getText("content.page-teaser.order-content", "like content structure") + "</label></div>");
        }
        out.println("</fieldset>");
        out.println("</div></div>"); // row

        if (globalContext.getAllTaxonomy(ctx).isActive()) {
            String taxoName = getTaxonomiesInputName();
            out.println("<fieldset class=\"taxonomy\"><legend><label for=\"" + taxoName + "\">" + i18nAccess.getText("taxonomy") + "</label></legend>");
            out.println("<div class=\"line reverse\">");
            out.println(XHTMLHelper.getCheckbox(getInputName("taxosession"), isSessionTaxonomy(ctx)));
            out.println("<label for=\"" + getInputName("taxosession") + "\">" + i18nAccess.getText("content.page-teaser.session-taxonomy", "session taxonomy") + "</label></div>");
            out.println(globalContext.getAllTaxonomy(ctx).getSelectHtml(taxoName, "form-control chosen-select", getTaxonomy(), true));
            out.println("</fieldset>");
        }

        out.println("<fieldset class=\"page-list\">");
        out.println("<legend>" + i18nAccess.getText("content.page-teaser.page-list") + "</legend>");
        /* array filter */
        String tableID = "table-" + getId();
        out.println("<div class=\"row\"><div class=\"col-sm-9\"><div class=\"array-filter line\">");
        String ajaxURL = URLHelper.createExpCompLink(ctx, getId());
        out.println("<input class=\"input\" type=\"text\" placeholder=\"" + i18nAccess.getText("global.filter") + "\" onkeyup=\"filterPage('" + ajaxURL + "',this.value, '." + tableID + " tbody', '" + ctx.getRequestContentLanguage() + "');\"/>");
        String resetFilterScript = "jQuery('#comp-" + getId() + " .array-filter .input').val(''); filterPage('" + ajaxURL + "',jQuery('#comp-" + getId() + " .array-filter .input').val(), '." + tableID + " tbody', '" + ctx.getRequestContentLanguage() + "'); return false;";
        out.println("<input type=\"button\" onclick=\"" + resetFilterScript + "\" value=\"" + i18nAccess.getText("global.reset") + "\" />");
        String allScript = "if (jQuery('#comp-" + getId() + " .array-filter .input').val().indexOf(':all')<0) {jQuery('#comp-" + getId() + " .array-filter .input').val(jQuery('#comp-" + getId() + " .array-filter .input').val()+' :all'); filterPage('" + ajaxURL + "',jQuery('#comp-" + getId() + " .array-filter .input').val(), '." + tableID + " tbody'); return false;}";
        out.println("<input type=\"button\" onclick=\"" + allScript + "\" value=\"" + i18nAccess.getText("global.all") + "\" />");

        out.println("</div></div>");
        out.println("<div class=\"col-sm-3\"><div class=\"direct-link\">");
        out.println("<input class=\"input\" type=\"text\" name=\"" + getDirectLinkInputName() + "\" placeholder=\"" + i18nAccess.getText("global.direct-link", "direct link with page id or page name") + "\" />");
        out.println("<input type=\"submit\" value=\"" + i18nAccess.getText("global.ok") + "\" />");
        out.println("</div></div></div>");

        MenuElement basePage = null;
        if (getParentNode(ctx).length() > 1) { // if parent node is not root
            // node
            basePage = menu.searchChild(ctx, getParentNode(ctx));
        }
        if (basePage != null) {
            menu = basePage;
        }

        List<MenuElement> allChildren = menu.getAllChildrenList();
        Collections.sort(allChildren, new MenuElementModificationDateComparator(true));
        Collection<MenuElement> currentSelection = getSelectedPages(ctx, allChildren);

        out.print("<div class=\"page-list-container\"><table class=\"");
        out.print("page-list" + ' ' + tableID);
        String onlyCheckedScript = "if (jQuery('#comp-" + getId() + " .array-filter .input').val().indexOf(':checked')<0) {jQuery('#comp-" + getId() + " .array-filter .input').val(jQuery('#comp-" + getId() + " .array-filter .input').val()+' :checked'); filterPage('" + ajaxURL + "',jQuery('#comp-" + getId() + " .array-filter .input').val(), '." + tableID + " tbody', '" + ctx.getRequestContentLanguage() + "'); return false;}";
        out.println("\"><thead><tr><th>&nbsp</th><th>" + i18nAccess.getText("global.label") + "</th><th>" + i18nAccess.getText("global.date") + "</th><th>" + i18nAccess.getText("global.modification") + "</th><th>" + i18nAccess.getText("content.page-teaser.language") + "</th><th title=\"" + i18nAccess.getText("content.page-reference.content.help") + "\">" + i18nAccess.getText("content.page-reference.content") + "</th><th>" + i18nAccess.getText("global.select") + " <a href=\"#\" onclick=\"" + onlyCheckedScript + "\">(" + currentSelection.size() + ")</a></th></tr></thead><tbody>");

        int numberOfPage = 16384;
        if (allChildren.size() < numberOfPage) {
            numberOfPage = allChildren.size();
        }

        if (ctx.isExport()) { // if export mode render only the list of page.
            outStream = new ByteArrayOutputStream();
            out = new PrintStream(outStream);
        }
        RequestService rs = RequestService.getInstance(ctx.getRequest());
        String filter = rs.getParameter("filter", null);

        if (numberOfPage < MAX_PAGES || filter != null) {
            ByteArrayOutputStream outStreamTemp = new ByteArrayOutputStream();
            PrintStream outTemp = new PrintStream(outStreamTemp);
            int countPage = 0;

            Collection<String> commands = extractCommandFromFilter(filter);
            filter = removeCommandFromFilter(filter);
            // Set<String> currentSelection = getPagesId(ctx,
            // page.getRoot().getAllChildren());
            for (int i = 0; i < numberOfPage; i++) {
                ContentContext newCtx = new ContentContext(ctx);
                newCtx.setArea(null);
                ContentContext lgCtx = ctx;
                MenuElement page = allChildren.get(i);
                // if
                // (GlobalContext.getInstance(ctx.getRequest()).isAutoSwitchToDefaultLanguage())
                // {
                // lgCtx = page.getContentContextWithContent(ctx);
                // }
                if (filterPage(lgCtx, allChildren.get(i), currentSelection, commands, filter, true)) {
                    renderPageSelectLine(lgCtx, outTemp, currentSelection, page, i + 1);
                    countPage++;
                }
            }
            if (countPage < MAX_PAGES || commands.contains("all")) {
                out.print(new String(outStreamTemp.toByteArray()));
            } else {
                out.println("<td colspan=\"5\" class=\"error\"><div class=\"notification msgalert\">" + i18nAccess.getText("content.page-reference.too-many-pages", "Too many pages found, please use the filter above to limit results.") + " (#" + numberOfPage + ")</div></td>");
            }

        } else {
            out.println("<td colspan=\"5\" class=\"error\"><div class=\"notification msgalert\">" + i18nAccess.getText("content.page-reference.too-many-pages", "Too many pages found, please use the filter above to limit results.") + " (#" + numberOfPage + ")</div></td>");
        }

        if (!ctx.isExport()) {
            out.println("</tbody></table></div></fieldset>");
        }
        out.println("</div>");
        return new String(outStream.toByteArray());
    }

    private void renderPageSelectLine(ContentContext ctx, PrintStream out, Collection<MenuElement> currentSelection, MenuElement page, int num) throws Exception {
        String editPageURL = URLHelper.createEditURL(page.getPath(), ctx);
        if (ctx.isEditPreview()) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("closePopup", "true");
            params.put("parentURL", URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.PREVIEW_MODE), page.getPath()));
            editPageURL = URLHelper.createURL(ctx, page.getPath(), params);
        }
        String checked = "";
        if (currentSelection.contains(page)) {
            checked = " checked=\"checked\"";
        }
        out.print("<tr class=\"filtered" + (num % 2 == 0 ? " odd" : " even") + "\">");
        out.print("<td><input type=\"hidden\" name=\"" + getPageDisplayedId(page) + "\" value=\"1\" /><input type=\"checkbox\" name=\"" + getPageId(page) + "\" value=\"" + page.getId() + "\"" + checked + "/></td>");

        out.print("<td class=\"label\"><a data-toggle=\"tooltip\" data-placement=\"right\" title=\"" + XHTMLHelper.stringToAttribute(NavigationHelper.getBreadCrumb(ctx, page)) + "\" href=\"" + editPageURL + "\">" + page.getFullLabel(ctx) + "</a></td>");
        out.print("<td>" + StringHelper.neverNull(StringHelper.renderLightDate(page.getContentDate(ctx))) + "</td>");
        out.println("<td>" + StringHelper.renderLightDate(page.getModificationDate(ctx)) + "</td><td>" + StringHelper.neverNull(page.getRealContentLanguage(ctx)) + "</td>");
        String contentCode = "";
        String sep = "";
        if (page.isRealContent(ctx)) {
            contentCode += "R";
            sep = " - ";
        }
        if (!StringHelper.isEmpty(page.getContentTitle(ctx))) {
            contentCode += sep + "T";
            sep = " - ";
        }
        if (!StringHelper.isEmpty(page.getDescription(ctx))) {
            contentCode += sep + "D";
            sep = " - ";
        }
        if (page.getImages(ctx) != null && page.getImages(ctx).size() > 0) {
            contentCode += sep + "I";
            sep = " - ";
        }
        out.println("<td>" + contentCode + "</td>");
        out.print("</tr>");
    }

    private int getFirstPageNumber() {
        if (StringHelper.isDigit(properties.getProperty(PAGE_START_PROP_KEY, null))) {
            return Integer.parseInt(properties.getProperty(PAGE_START_PROP_KEY, "1"));
        } else {
            return 1;
        }
    }

    private String getFirstPageNumberInputName() {
        return "first_page_number_" + getId();
    }

    @Override
    public String getHexColor() {
        return LINK_COLOR;
    }

    private String getInputNameTitle() {
        return "title_" + getId();
    }

    private String getInputNameOnlyDepth() {
        return "onlydepth_" + getId();
    }

    private String getInputPopup() {
        return "popup_" + getId();
    }

    private String getInputRefDefaultLang() {
        return "refdefaultlang_" + getId();
    }

    private String getInputDisplayDate() {
        return "disdplaydate_" + getId();
    }

    private int getLastPageNumber() {
        if (!StringHelper.isDigit(properties.getProperty(PAGE_END_PROP_KEY, ""))) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.parseInt(properties.getProperty(PAGE_END_PROP_KEY, "" + Integer.MAX_VALUE));
        }
    }

    private String getLastPageNumberInputName() {
        return "last_page_number_" + getId();
    }

    protected String getOrder() {
        return properties.getProperty(ORDER_KEY, "date");
    }

    protected String getOrderInputName() {
        return "orde-" + getId();
    }

    protected String getDirectLinkInputName() {
        return "direct-link-" + getId();
    }

    protected String getPageId(MenuElement page) {
        return "p_" + getId() + "_" + page.getId();
    }

    protected String getPageDisplayedId(MenuElement page) {
        return "pd_" + getId() + "_" + page.getId();
    }

    protected List<MenuElement> getSelectedPages(ContentContext ctx, List<MenuElement> children) throws Exception {
        String value = properties.getProperty(PAGE_REF_PROP_KEY, "");
        if (value.trim().length() == 0 && !isDefaultSelected()) {
            return Collections.EMPTY_LIST;
        }
        List<MenuElement> out;
        if (!isDefaultSelected()) {
            out = new LinkedList<MenuElement>();
            ContentService contentService = ContentService.getInstance(ctx.getRequest());
            MenuElement root = contentService.getNavigation(ctx);
            for (String id : Arrays.asList(StringHelper.split(value, PAGE_SEPARATOR))) {
                MenuElement page = root.searchChildFromId(id);
                if (page != null && !out.contains(page)) {
                    out.add(page);
                }
            }
        } else {
            List<MenuElement> selectedPage = new LinkedList<MenuElement>();
            MenuElement parentNode = null;
            if (children.size() > 0) {
                parentNode = children.get(0).getRoot().searchChild(ctx, getParentNode(ctx));
            }
            List<String> selectedId = StringHelper.stringToCollection(value, PAGE_SEPARATOR);
            for (MenuElement page : children) {
                if (page.isActive(ctx)) {
                    if (!selectedId.contains(page.getId())) {
                        if ((parentNode == null || page.isChildOf(parentNode)) && !page.isChildrenOfAssociation()) {
                            selectedPage.add(page);
                        }
                    }
                }
            }
            out = selectedPage;
        }
        return out;
    }

    protected String getParentNode(ContentContext ctx) {
        String parentNodePath = properties.getProperty(PARENT_NODE_PROP_KEY, "/");
        if (StringHelper.isEmpty(parentNodePath)) {
            parentNodePath = "/";
        }
        ContentService contentService = ContentService.getInstance(ctx.getRequest());
        MenuElement page;
        try {
            page = contentService.getNavigation(ctx).searchChild(ctx, parentNodePath);
            if (page == null) {
                page = contentService.getNavigation(ctx).searchChildFromName(URLHelper.extractFileName(parentNodePath));
                if (page != null) {
                    parentNodePath = page.getPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parentNodePath;
    }

    protected String getParentNodeInputName() {
        return "parent-node-" + getId();
    }

    @Override
    public String getPrefixViewXHTMLCode(ContentContext ctx) {
        String colPrefix = getColomnablePrefix(ctx);
        if (getConfig(ctx).getProperty("prefix", null) != null) {
            String prefix = "";
            if (ctx.isPreview()) {
                prefix = "<div " + getPrefixCssClass(ctx, "") + getSpecialPreviewCssId(ctx) + ">";
            }
            return colPrefix + prefix + getConfig(ctx).getProperty("prefix", null);
        }

        String specialClass = "";
        if (isDateOrder(ctx)) {
            specialClass = " date-order" + specialClass;
        } else if (isCreationOrder(ctx)) {
            specialClass = " creation-order" + specialClass;
        } else if (isVisitOrder(ctx)) {
            specialClass = " visit-order" + specialClass;
        } else if (isPopularityOrder(ctx)) {
            specialClass = " popularity-order" + specialClass;
        }
        return colPrefix + "<div " + getPrefixCssClass(ctx, "page-reference" + specialClass + ' ' + getComponentCssClass(ctx)) + getSpecialPreviewCssId(ctx) + ">";
    }

    protected String getReverseOrderInput() {
        return "reserve-order-" + getId();
    }

    protected String getDynamicOrderInput() {
        return "dynamic-order-" + getId();
    }

    @Override
    public String[] getStyleList(ContentContext ctx) {
        return new String[]{STATIC, INTERACTIVE};
    }

    @Override
    public String getStyleTitle(ContentContext ctx) {
        try {
            I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
            return i18nAccess.getText("page-teaser.rules");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "page-teaser-rules";
    }

    @Override
    public String getSuffixViewXHTMLCode(ContentContext ctx) {
        String colSuffix = getColomnableSuffix(ctx);
        if (getConfig(ctx).getProperty("suffix", null) != null) {
            String suffix = "";
            if (ctx.isPreview()) {
                suffix = "</div>";
            }
            return getConfig(ctx).getProperty("suffix", null) + suffix + colSuffix;
        }
        return "</div>" + colSuffix;
    }

    protected Collection<String> getSelectedTag(ContentContext ctx) {
        if (isDynamicOrder(ctx) && ctx.getRequest().getParameter("tag") != null) {
            return StringHelper.stringToCollection(ctx.getRequest().getParameter("tag"), ",");
        } else {
            return StringHelper.stringToCollection(properties.getProperty(TAG_KEY, ""), ",");
        }
    }

    protected String getTagsInputName() {
        return "tag-" + getId();
    }

    private Collection<String> getTimeSelection() {
        if (properties.getProperty(TIME_SELECTION_KEY, null) == null) {
            return getTimeSelectionOptions();
        }
        return StringHelper.stringToCollection(properties.getProperty(TIME_SELECTION_KEY, null));
    }

    private String getStartDate() {
        return properties.getProperty(START_DATE_KEY, "");
    }

    private String getEndDate() {
        return properties.getProperty(END_DATE_KEY, "");
    }

    protected boolean isDisplayFirstPage() {
        return StringHelper.isTrue(properties.getProperty(DISPLAY_FIRST_PAGE_KEY, "false"));
    }

    protected void setDisplayFirstPage(boolean value) {
        properties.setProperty(DISPLAY_FIRST_PAGE_KEY, "" + value);
    }

    protected void setIntranetMode(boolean mode) {
        properties.setProperty(INTRANET_MODE_KEY, "" + mode);
    }

    protected String getInputFirstPageFull() {
        return "first-page-full-" + getId();
    }

    protected String getTimeSelectionInputName(String option) {
        return "time-selection-" + getId() + '-' + option;
    }

    private Collection<String> getTimeSelectionOptions() {
        return TIME_SELECTION_OPTIONS;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    protected String getWidthEmptyPageInputName() {
        return "width-empty-page-" + getId();
    }

    protected String getOnlyWithoutChildrenInputName() {
        return "only-without-children-" + getId();
    }

    protected String getEventInputName() {
        return getInputName("event");
    }

    protected String getIntranetModeInputName() {
        return "intranet-mode-" + getId();
    }

    @Override
    public void init(ComponentBean bean, ContentContext newContext) throws Exception {
        super.init(bean, newContext);
        if (getValue().trim().length() > 0) {
            properties.load(stringToStream(getValue()));
            if (properties.getProperty("type") != null) {
                setRenderer(newContext, properties.getProperty("type"));
                properties.remove("type");
            }
        }
    }

    @Override
    public boolean isContentCachable(ContentContext ctx) {
//		if (isForceCachable()) {
//			return true;
//		}
        if (isSessionTaxonomy(ctx)) {
            return false;
        }
        return StringHelper.isTrue(getConfig(ctx).getProperty("config.cache." + getCurrentRenderer(ctx), getConfig(ctx).getProperty("config.cache", null)), false);
    }

    @Override
    public boolean isContentCachableByQuery(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("config.cache-query", null), true);
    }

    @Override
    public boolean isContentTimeCachable(ContentContext ctx) {
        if (isSessionTaxonomy(ctx)) {
            return false;
        }
        return StringHelper.isTrue(getConfig(ctx).getProperty("config.time-cache", null), true);
    }

    private boolean isDefaultSelected() {
        return StringHelper.isTrue(properties.getProperty(DEFAULT_SELECTED_PROP_KEY, null));
    }

    private boolean isDynamicOrder(ContentContext ctx) {
        return StringHelper.isTrue(properties.getProperty(DYNAMIC_ORDER_KEY, null));
    }

    private boolean checkOrder(ContentContext ctx, String orderName) {
        boolean dynOrderDefined = false;
        if (isDynamicOrder(ctx)) {
            for (Object param : ctx.getRequest().getParameterMap().keySet()) {
                if (param != null && param.toString().endsWith("_order") && !param.equals("reverse_order")) {
                    dynOrderDefined = true;
                }
            }
        }
        if (StringHelper.isTrue(ctx.getRequest().getParameter(orderName + "_order"))) {
            return true;
        } else {
            if (!dynOrderDefined) {
                return getOrder().equals(orderName);
            } else {
                return false;
            }
        }
    }

    private boolean isCreationOrder(ContentContext ctx) {
        return checkOrder(ctx, "creation");
    }

    private boolean isDateOrder(ContentContext ctx) {
        return checkOrder(ctx, "date");
    }

    private boolean isReactionOrder(ContentContext ctx) {
        return checkOrder(ctx, "reaction");
    }

    private boolean isNoOrder(ContentContext ctx) {
        return checkOrder(ctx, "no-order");
    }

    private boolean isPopularityOrder(ContentContext ctx) {
        return checkOrder(ctx, "popularity");
    }

    private boolean isContentOrder(ContentContext ctx) {
        return checkOrder(ctx, "content");
    }

    protected boolean isReverseOrder(ContentContext ctx) {
        if (isDynamicOrder(ctx) && StringHelper.isTrue(ctx.getRequest().getParameter("reverse_order"))) {
            return true;
        } else {
            return StringHelper.isTrue(properties.getProperty(CHANGE_ORDER_KEY), false);
        }
    }

    protected boolean isSessionTaxonomy(ContentContext ctx) {
        return StringHelper.isTrue(properties.getProperty(SESSION_TAXONOMY_KEY), true);
    }

    private boolean isVisitOrder(ContentContext ctx) {
        return checkOrder(ctx, "visit");
    }

    private boolean isWidthEmptyPage() {
        return StringHelper.isTrue(properties.getProperty(WIDTH_EMPTY_PAGE_PROP_KEY), false);
    }

    private boolean isOnlyEvent() {
        return StringHelper.isTrue(properties.getProperty(ONLY_EVENT), false);
    }

    private boolean isIntranetMode() {
        return StringHelper.isTrue(properties.getProperty(INTRANET_MODE_KEY), false);
    }

    public int getPageSize(ContentContext ctx) {
        String size = getConfig(ctx).getProperty("page.size", null);
        if (size == null) {
            return 10; // default value
        } else {
            return Integer.parseInt(size);
        }
    }

    private void sort(ContentContext ctx, List<MenuElement> pages, boolean ascending) throws Exception {
        if (!isNoOrder(ctx)) {
            if (isReactionOrder(ctx)) {
                Collections.sort(pages, new ReactionMenuElementComparator(ctx, ascending));
            } else if (isDateOrder(ctx)) {
                Collections.sort(pages, new MenuElementGlobalDateComparator(ctx, ascending));
            } else if (isCreationOrder(ctx)) {
                Collections.sort(pages, new MenuElementCreationDateComparator(ascending));
            } else if (isVisitOrder(ctx)) {
                Collections.sort(pages, new MenuElementVisitComparator(ctx, ascending));
            } else if (isPopularityOrder(ctx)) {
                Collections.sort(pages, new MenuElementPopularityComparator(ctx, ascending));
            } else if (isContentOrder(ctx)) {
                Collections.sort(pages, new MenuElementPriorityComparator(!ascending));
            }
        }
    }

    private void sortSmartPageBean(ContentContext ctx, List<SmartPageBean> pages, boolean ascending) throws Exception {
        if (!isNoOrder(ctx)) {
            if (isReactionOrder(ctx)) {
                Collections.sort(pages, new ReactionSmartPageBeanComparator(ascending));
            } else if (isDateOrder(ctx)) {
                Collections.sort(pages, new SmartPageBeanGlobalDateComparator(ctx, ascending));
            } else if (isCreationOrder(ctx)) {
                Collections.sort(pages, new SmartPageBeanCreationDateComparator(ascending));
            } else if (isVisitOrder(ctx)) {
                Collections.sort(pages, new SmartPageBeanVisitComparator(ctx, ascending));
            } else if (isPopularityOrder(ctx)) {
                Collections.sort(pages, new SmartPageBeanPopularityComparator(ctx, ascending));
            } else if (isContentOrder(ctx)) {
                Collections.sort(pages, new SmartPageBeanPriorityComparator(!ascending));
            }
        }
    }

    protected boolean isRedisplay(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("redisplay", null), false);
    }

    protected PageReferenceComponent getRefComponent(ContentContext ctx) {
        if (isRefDefaultLang()) {
            PageReferenceComponent comp = null;
            try {
                comp = (PageReferenceComponent) getReferenceComponent(ctx);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (comp == null) {
                logger.severe("reference composant not found in " + ctx.getGlobalContext().getDefaultLanguage() + " type:" + getType());
                return this;
            } else {
                return comp;
            }
        } else {
            return this;
        }
    }

    @Override
    public void prepareView(ContentContext ctx) throws Exception {

        // LocalLogger.PRINT_TIME = true;

        LocalLogger.startCount("pageref");

        super.prepareView(ctx);

        LocalLogger.stepCount("pageref", "step 1");

        SimpleDateFormat format = new SimpleDateFormat(MOUNT_FORMAT, ctx.getLocale());

        ContentService content = ContentService.getInstance(ctx.getRequest());
        MenuElement menu = content.getNavigation(ctx);
        List<MenuElement> allChildren = menu.getAllChildrenList();

        LocalLogger.stepCount("pageref", "step 2");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        new PrintStream(outStream);

        boolean ascending = false;
        Calendar todayCal = Calendar.getInstance();
        Calendar pageCal = Calendar.getInstance();

        List<MenuElement> selectedPage = getRefComponent(ctx).getSelectedPages(ctx, allChildren);

        LocalLogger.stepCount("pageref", "step 3");

        int firstPageNumber = getFirstPageNumber();
        int lastPageNumber = getLastPageNumber();
        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
        LocalLogger.stepCount("pageref", "step 4");
        // Set<String> currentSelection = getPagesId(ctx, allChildren);

        for (MenuElement page : selectedPage) {
            ContentContext lgCtx = page.getContentContextWithContent(ctx);
            Date pageDate = page.getModificationDate(ctx);
            Date contentDate;
            contentDate = page.getContentDate(lgCtx);
            if (contentDate != null) {
                boolean futurPage = page.getCreationDate().getTime() - page.getContentDate(lgCtx).getTime() < 0;
                if (!futurPage) {
                    ascending = true;
                }
                pageDate = page.getContentDate(lgCtx);
            }
            pageCal.setTime(pageDate);
            if (todayCal.after(pageCal)) {
                ascending = true;
            }
        }

        LocalLogger.stepCount("pageref", "step 5");

        if (isReverseOrder(ctx)) {
            ascending = !ascending;
        }

        sort(ctx, selectedPage, ascending);

        LocalLogger.stepCount("pageref", "step 6");

        int countPage = 0;
        int realContentSize = 0;
        MenuElement firstPage = null;

        String tagFilter = null;
        Calendar startDate = null;
        Calendar endDate = null;
        String catFilter = null;
        String monthFilter = null;

        if (ctx.getRequest().getParameter("comp_id") == null || ctx.getRequest().getParameter("comp_id").equals(getId())) {
            tagFilter = ctx.getRequest().getParameter("tag");
            catFilter = ctx.getRequest().getParameter("category");
            monthFilter = ctx.getRequest().getParameter("month");
            if (monthFilter != null && monthFilter.trim().length() > 0) {
                startDate = Calendar.getInstance();
                endDate = Calendar.getInstance();
                Date mount = format.parse(monthFilter);
                startDate.setTime(mount);
                endDate.setTime(mount);
                startDate = TimeHelper.convertRemoveAfterMonth(startDate);
                endDate = TimeHelper.convertRemoveAfterMonth(endDate);
                endDate.add(Calendar.MONTH, 1);
                endDate.add(Calendar.MILLISECOND, -1);
            } else {
                monthFilter = null;
            }
        }

        List<SmartPageBean> pageBeans = new LinkedList<SmartPageBean>();
        Collection<Calendar> allMonths = new LinkedList<Calendar>();
        Collection<String> allMonthsKeys = new HashSet<String>();

        boolean withEmptyPage = getRefComponent(ctx).isWidthEmptyPage();
        boolean intranetMode = getRefComponent(ctx).isIntranetMode();

        Integer onlyDepth = null;
        if (StringHelper.isDigit(getOnlyDepth())) {
            onlyDepth = Integer.parseInt(getOnlyDepth());
        }

        int refDepth = ctx.getCurrentPage().getDepth();

        for (MenuElement page : selectedPage) {
            ContentContext lgCtx = ctx;
            boolean pageRealContent = page.isRealContent(lgCtx);
            if (!pageRealContent && GlobalContext.getInstance(ctx.getRequest()).isAutoSwitchToDefaultLanguage()) {
                lgCtx = page.getContentContextWithContent(ctx);
                pageRealContent = page.isRealContent(lgCtx);
            }
            if (filterPage(lgCtx, page, selectedPage, Collections.EMPTY_LIST, "", false)) {
                if ((withEmptyPage || page.isRealContentAnyLanguage(lgCtx))) {
                    if (firstPage == null) {
                        firstPage = page;
                    }
                    boolean realContent = true;
                    if (!withEmptyPage) {
                        realContent = pageRealContent;
                    }
                    if (realContent) {
                        realContentSize++;
                    }
                    if (onlyDepth == null || onlyDepth == page.getDepth() - refDepth) {
                        if (!intranetMode || page.getEditorRoles().size() == 0 || (ctx.getCurrentEditUser() != null && ctx.getCurrentEditUser().validForRoles(page.getEditorRoles()))) {
                            if (realContent) {
                                if (tagFilter == null || tagFilter.trim().length() == 0 || page.getTags(lgCtx).contains(tagFilter)) {
                                    if (catFilter == null || catFilter.trim().length() == 0 || page.getCategory(lgCtx).equals(catFilter)) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTime(page.getContentDateNeverNull(lgCtx));
                                        cal = TimeHelper.convertRemoveAfterMonth(cal);
                                        String key = ("" + cal.get(Calendar.YEAR)) + '-' + cal.get(Calendar.MONTH);
                                        if (!allMonthsKeys.contains(key)) {
                                            allMonths.add(cal);
                                            allMonthsKeys.add(key);
                                        }
                                        if (monthFilter == null || TimeHelper.betweenInDay(page.getContentDateNeverNull(lgCtx), startDate.getTime(), endDate.getTime())) {
                                            SmartPageBean pageBean = SmartPageBean.getInstance(ctx, lgCtx, page, this);
                                            boolean isRedisplay = getRefComponent(ctx).isRedisplay(ctx);
                                            boolean isAlreadyDisplayed = pageBean.isAlreadyDisplayed();
                                            if (isRedisplay || !isAlreadyDisplayed) {
                                                countPage++;
                                            }
                                            if (countPage >= firstPageNumber && countPage <= lastPageNumber) {
                                                if (isRedisplay || !isAlreadyDisplayed) {
                                                    pageBeans.add(pageBean);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LocalLogger.stepCount("pageref", "step 7");

        if (getRefComponent(ctx).isDisplayFirstPage() && firstPage != null && ctx.getRequest().getParameter("_wcms_content_path") == null) {
            String path = firstPage.getPath();
            String pageRendered = getRefComponent(ctx).executeJSP(ctx, Edit.CONTENT_RENDERER + "?_wcms_content_path=" + path);
            ctx.getRequest().setAttribute("firstPage", pageRendered);
        } else {
            ctx.getRequest().removeAttribute("firstPage");
        }

        PagesStatus pagesStatus = new PagesStatus(countPage, realContentSize);
        PaginationContext pagination = PaginationContext.getInstance(ctx.getRequest(), getId(), pageBeans.size(), getPageSize(ctx));

        List<String> months = new LinkedList<String>();
        for (Calendar calendar : allMonths) {
            months.add(format.format(calendar.getTime()));
        }

        LocalLogger.stepCount("pageref", "step 8");

        ctx.getRequest().setAttribute("pagination", pagination);
        ctx.getRequest().setAttribute("pagesStatus", pagesStatus);
        ctx.getRequest().setAttribute("pages", pageBeans);
        ctx.getRequest().setAttribute("title", getContentTitle());
        ctx.getRequest().setAttribute("comp", this);
        ctx.getRequest().setAttribute("months", months);
        ctx.getRequest().setAttribute("tags", globalContext.getTags());
        ctx.getRequest().setAttribute("popup", isPopup());
        ctx.getRequest().setAttribute("interactive", isInteractive());
        if (globalContext.getAllTaxonomy(ctx).isActive()) {
            ctx.getRequest().setAttribute("taxonomyList", TaxonomyDisplayBean.convert(ctx, globalContext.getAllTaxonomy(ctx).convert(getTaxonomy())));
        }

        LocalLogger.PRINT_TIME = false;
    }

    @Override
    public ComponentConfig getConfig(ContentContext ctx) {
        PageReferenceComponent comp = getRefComponent(ctx);
        if (comp != this) {
            return getRefComponent(ctx).getConfig(ctx);
        } else {
            return super.getConfig(ctx);
        }
    }

    @Override
    public String getCurrentRenderer(ContentContext ctx) {
        PageReferenceComponent comp = getRefComponent(ctx);
        if (comp != this) {
            return getRefComponent(ctx).getCurrentRenderer(ctx);
        } else {
            return super.getCurrentRenderer(ctx);
        }
    }

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        System.out.println("date : " + format.format(date));

    }

    private void popularitySorting(ContentContext ctx, List<MenuElement> pages, int pertinentPageToBeSort) throws Exception {
        double minMaxPageRank = 0;
        TreeSet<MenuElement> maxElement = new TreeSet<MenuElement>(new MenuElementPopularityComparator(ctx, false));
        for (MenuElement page : pages) {
            double pageRank = page.getPageRank(ctx);
            if (pageRank >= minMaxPageRank) {
                if (maxElement.size() > pertinentPageToBeSort) {
                    maxElement.pollFirst();
                    minMaxPageRank = maxElement.first().getPageRank(ctx);
                }
                maxElement.add(page);
            }
        }
        for (MenuElement page : maxElement) {
            pages.remove(page);
        }
        for (MenuElement page : maxElement) {
            pages.add(0, page);
        }
    }

    private void popularitySortingSmartPageBean(ContentContext ctx, List<SmartPageBean> pages, int pertinentPageToBeSort) throws Exception {
        double minMaxPageRank = 0;
        TreeSet<SmartPageBean> maxElement = new TreeSet<SmartPageBean>(new SmartPageBeanPopularityComparator(ctx, false));
        for (SmartPageBean page : pages) {
            double pageRank = page.getPage().getPageRank(ctx);
            if (pageRank >= minMaxPageRank) {
                if (maxElement.size() > pertinentPageToBeSort) {
                    maxElement.pollFirst();
                    minMaxPageRank = maxElement.first().getPage().getPageRank(ctx);
                }
                maxElement.add(page);
            }
        }
        for (SmartPageBean page : maxElement) {
            pages.remove(page);
        }
        for (SmartPageBean page : maxElement) {
            pages.add(0, page);
        }
    }

    @Override
    public String performEdit(ContentContext ctx) throws Exception {
        performColumnable(ctx);
        RequestService requestService = RequestService.getInstance(ctx.getRequest());

        if (requestService.getParameter(getCompInputName(), null) != null) {
            ContentService content = ContentService.getInstance(ctx.getRequest());
            MenuElement menu = content.getNavigation(ctx);
            List<MenuElement> allChildren = menu.getAllChildrenList();
            List<String> currentPageSelected = getPageSelected();
            Collection<String> pagesSelected = new HashSet<String>();
            List<String> pagesNotSelected = new LinkedList<String>();
            Collection<MenuElement> currentSelection = getSelectedPages(ctx, allChildren);
            for (MenuElement element : allChildren) {
                String selectedPage = requestService.getParameter(getPageId(element), null);
                if (requestService.getParameter(getPageDisplayedId(element), null) != null) {
                    if (isDefaultSelected() ^ (selectedPage != null) && filterPage(ctx, element, currentSelection, Collections.EMPTY_LIST, null, true)) {
                        pagesSelected.add(element.getId());
                    } else {
                        pagesNotSelected.add(element.getId());
                    }
                }
            }

            if (!properties.getProperty(START_DATE_KEY, "").equals(requestService.getParameter(getInputName(START_DATE_KEY), ""))) {
                properties.setProperty(START_DATE_KEY, requestService.getParameter(getInputName(START_DATE_KEY), ""));
                setModify();
            }
            if (!properties.getProperty(END_DATE_KEY, "").equals(requestService.getParameter(getInputName(END_DATE_KEY), ""))) {
                properties.setProperty(END_DATE_KEY, requestService.getParameter(getInputName(END_DATE_KEY), ""));
                setModify();
            }

            if (!StringHelper.isEmpty(requestService.getParameter(getDirectLinkInputName()))) {
                String page = requestService.getParameter(getDirectLinkInputName());
                MenuElement pageFound = menu.searchChildFromId(page);
                if (pageFound == null) {
                    pageFound = menu.searchChildFromName(page);
                }
                if (pageFound != null) {
                    pagesSelected.add(pageFound.getId());
                } else {
                    return I18nAccess.getInstance(ctx).getText("global.page-not-found") + " (" + page + ")";
                }
            }

            pagesSelected.addAll(currentPageSelected);
            pagesSelected.removeAll(pagesNotSelected);
            if (!currentPageSelected.equals(pagesSelected)) {
                setPageSelected(StringHelper.collectionToString(pagesSelected, PAGE_SEPARATOR));
                setModify();
            }

            String order = requestService.getParameter(getOrderInputName(), "date");
            if (!getOrder().equals(order)) {
                setOrder(order);
                storeProperties();
                setModify();
            }

            String sessionTaxo = requestService.getParameter(getInputName("taxosession"), null);
            if (isSessionTaxonomy(ctx) != StringHelper.isTrue(sessionTaxo)) {
                setSessionTaxonomy(StringHelper.isTrue(sessionTaxo));
                storeProperties();
                setModify();
            }

            String title = requestService.getParameter(getInputNameTitle(), "");
            if (!getContentTitle().equals(title)) {
                setContentTitle(title);
                storeProperties();
                setModify();
            }

            String onlyDepth = requestService.getParameter(getInputNameOnlyDepth(), "");
            if (!getOnlyDepth().equals(onlyDepth)) {
                setOnlyDepth(onlyDepth);
                storeProperties();
                setModify();
            }

            String popupRaw = requestService.getParameter(getInputPopup(), null);
            setPopup(StringHelper.isTrue(popupRaw));
            setDisplayDate(StringHelper.isTrue(requestService.getParameter(getInputDisplayDate(), null)));

            List<String> tags = requestService.getParameterListValues(getTagsInputName(), null);
            String tagRaw = StringHelper.collectionToString(tags, ",");
            if (!properties.getProperty(TAG_KEY, "").equals(tagRaw)) {
                setTag(tagRaw);
                setModify();
                setNeedRefresh(true);
            }

            String dynamicOrder = requestService.getParameter(getDynamicOrderInput(), "false");
            boolean newDynamicOrder = StringHelper.isTrue(dynamicOrder);
            if (isDynamicOrder(ctx) != newDynamicOrder) {
                setDynamicOrder(newDynamicOrder);
                setModify();
            }

            String reverseOrder = requestService.getParameter(getReverseOrderInput(), "false");
            boolean newReserveOrder = StringHelper.isTrue(reverseOrder);
            if (isReverseOrder(ctx) != newReserveOrder) {
                setReverseOrder(newReserveOrder);
                setModify();
            }

            String eventOnly = requestService.getParameter(getEventInputName(), "false");
            boolean newEventOnly = StringHelper.isTrue(eventOnly);
            if (isOnlyEvent() != newEventOnly) {
                setEventOnly(newEventOnly);
                setModify();
            }

            boolean refdeflang = StringHelper.isTrue(requestService.getParameter(getInputRefDefaultLang(), "false"));
            if (isRefDefaultLang() != refdeflang) {
                setRefDefaultLanguage(refdeflang);
                setModify();
            }

            String firstPageNumber = requestService.getParameter(getFirstPageNumberInputName(), "1");
            if (!firstPageNumber.equals("" + getFirstPageNumber())) {
                setFirstPageNumber(firstPageNumber);
                setModify();
            }

            if (isUITimeSelection(ctx) && requestService.getParameter(getOrderInputName(), null) != null) {
                Collection<String> timeSelectionList = new LinkedList<String>();
                for (String option : getTimeSelectionOptions()) {
                    String timeSelection = requestService.getParameter(getTimeSelectionInputName(option), null);
                    if (timeSelection != null) {
                        timeSelectionList.add(option);
                    }
                }
                if (!getTimeSelection().equals(timeSelectionList)) {
                    setTimeSelection(timeSelectionList);
                    setModify();
                }
            }

            if (isUIFullDisplayFirstPage(ctx)) {
                boolean displayFirstPage = requestService.getParameter(getInputFirstPageFull(), null) != null;
                if (displayFirstPage != isDisplayFirstPage()) {
                    setDisplayFirstPage(displayFirstPage);
                    setModify();
                }
            }

            if (isUIFilterOnEditUsers(ctx)) {
                boolean intranetMode = requestService.getParameter(getIntranetModeInputName(), null) != null;
                if (intranetMode != isIntranetMode()) {
                    setIntranetMode(intranetMode);
                    setModify();
                }
            }

            String lastPageNumber = requestService.getParameter(getLastPageNumberInputName(), "");
            if (!lastPageNumber.equals("" + getLastPageNumber())) {
                setLastPageNumber(lastPageNumber);
                setModify();
            }

            boolean defaultSelected = requestService.getParameter(getDefaultSelectedInputName(), null) != null;
            if (defaultSelected != isDefaultSelected()) {
                setModify();
                setNeedRefresh(true);
                setPageSelected("");
            }
            setDefaultSelected(defaultSelected);

            boolean withEmptyPage = requestService.getParameter(getWidthEmptyPageInputName(), null) != null;
            if (withEmptyPage != isWidthEmptyPage()) {
                setModify();
            }
            setWidthPageEmpty(withEmptyPage);

            String basePage = requestService.getParameter(getParentNodeInputName(), "/");
            if (!basePage.equals(getParentNode(ctx))) {
                setNeedRefresh(true);
                setModify();
            }

            if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
                String[] taxonomy = requestService.getParameterValues(getTaxonomiesInputName(), null);
                if (taxonomy != null) {
                    setTaxonomy(Arrays.asList(taxonomy));
                } else {
                    setTaxonomy(Collections.EMPTY_SET);
                }
            }

            setParentNode(basePage);

            storeProperties();

        }
        return null;
    }

    private void setContentTitle(String title) {
        properties.setProperty("content-title", title);
    }

    private void setDefaultSelected(boolean selected) {
        properties.setProperty(DEFAULT_SELECTED_PROP_KEY, "" + selected);
    }

    private void setFirstPageNumber(String firstPage) {
        properties.setProperty(PAGE_START_PROP_KEY, firstPage);
    }

    private void setLastPageNumber(String last) {
        properties.setProperty(PAGE_END_PROP_KEY, last);
    }

    protected void setOrder(String order) {
        properties.setProperty(ORDER_KEY, order);
    }

    protected void setSessionTaxonomy(boolean sessionTaxo) {
        properties.setProperty(SESSION_TAXONOMY_KEY, "" + sessionTaxo);
    }

    private void setPageSelected(String pagesSelected) {
        if (!properties.getProperty(PAGE_REF_PROP_KEY, "").equals(pagesSelected)) {
            setModify();
        }
        properties.setProperty(PAGE_REF_PROP_KEY, pagesSelected);
    }

    private List<String> getPageSelected() {
        return StringHelper.stringToCollection(properties.getProperty(PAGE_REF_PROP_KEY, ""), PAGE_SEPARATOR);
    }

    protected void setParentNode(String node) {
        properties.setProperty(PARENT_NODE_PROP_KEY, node);
    }

    protected void setReverseOrder(boolean reverseOrder) {
        properties.setProperty(CHANGE_ORDER_KEY, "" + reverseOrder);
    }

    protected void setEventOnly(boolean onlyEvent) {
        properties.setProperty(ONLY_EVENT, "" + onlyEvent);
    }

    protected void setRefDefaultLanguage(boolean onlyEvent) {
        properties.setProperty("refdeflang", "" + onlyEvent);
    }

    protected void setDynamicOrder(boolean dynamicOrder) {
        properties.setProperty(DYNAMIC_ORDER_KEY, "" + dynamicOrder);
    }

    protected void setTag(String tag) {
        properties.setProperty(TAG_KEY, tag);
    }

    private void setTimeSelection(Collection<String> timeSelection) {
        properties.setProperty(TIME_SELECTION_KEY, StringHelper.collectionToString(timeSelection));
    }

    private void setWidthPageEmpty(boolean selected) {
        properties.setProperty(WIDTH_EMPTY_PAGE_PROP_KEY, "" + selected);
    }

    private void setTaxonomy(Collection<String> taxonomy) {
        properties.setProperty(TAXONOMY, "" + StringHelper.collectionToString(taxonomy));
    }

    private void visitSorting(ContentContext ctx, List<MenuElement> pages, int pertinentPageToBeSort) throws Exception {
        int minMaxVisit = 0;
        TreeSet<MenuElement> maxElement = new TreeSet<MenuElement>(new MenuElementVisitComparator(ctx, false));

        for (MenuElement page : pages) {
            if (page.isRealContent(ctx)) {
                int access = page.getLastAccess(ctx);
                if (access >= minMaxVisit) {
                    if (maxElement.size() >= pertinentPageToBeSort) {
                        maxElement.pollFirst();
                        minMaxVisit = maxElement.first().getLastAccess(ctx);
                    }
                    maxElement.add(page);
                }
            }
        }

        for (MenuElement page : maxElement) {
            pages.remove(page);
        }
        for (MenuElement page : maxElement) {
            pages.add(0, page);
        }
    }

    private void visitSortingSmartPageBean(ContentContext ctx, List<SmartPageBean> pages, int pertinentPageToBeSort) throws Exception {
        int minMaxVisit = 0;
        TreeSet<SmartPageBean> maxElement = new TreeSet<SmartPageBean>(new SmartPageBeanVisitComparator(ctx, false));

        for (SmartPageBean page : pages) {
            if (page.isRealContent()) {
                int access = page.getPage().getLastAccess(ctx);
                if (access >= minMaxVisit) {
                    if (maxElement.size() >= pertinentPageToBeSort) {
                        maxElement.pollFirst();
                        minMaxVisit = maxElement.first().getPage().getLastAccess(ctx);
                    }
                    maxElement.add(page);
                }
            }
        }

        for (SmartPageBean page : maxElement) {
            pages.remove(page);
        }
        for (SmartPageBean page : maxElement) {
            pages.add(0, page);
        }
    }

    @Override
    public boolean isRealContent(ContentContext ctx) {
        ContentService content = ContentService.getInstance(ctx.getRequest());
        try {
            MenuElement menu = content.getNavigation(ctx);
            return getSelectedPages(ctx, menu.getAllChildrenList()).size() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getActionGroupName() {
        return "page-links";
    }

    @Override
    public int getSearchLevel() {
        return 0;
    }

    @Override
    protected Object getLock(ContentContext ctx) {
        return ctx.getGlobalContext().getLockLoadContent();
    }

    @Override
    public boolean initContent(ContentContext ctx) throws Exception {
        super.initContent(ctx);
        if (isEditOnCreate(ctx)) {
            return false;
        }
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);

        List<MenuElement> articles = MacroHelper.searchArticleRoot(ctx);
        if (articles.size() > 0) {
            out.println("parent-node=" + articles.iterator().next().getPath());
            out.println("width_empty=false");
            out.println("is-def-selected=true");
            out.println("page-end=5");
            out.close();
            setValue(new String(outStream.toByteArray()));
        }
        return true;
    }

    @Override
    public boolean isMirroredByDefault(ContentContext ctx) {
        return true;
    }

    @Override
    public String getContentAsText(ContentContext ctx) {
        return getContentTitle();
    }

    private String getTaxonomiesInputName() {
        return "taxonomie-" + getId();
    }

    @Override
    public Set<String> getTaxonomy() {
        return StringHelper.stringToSet(properties.getProperty(TAXONOMY, null));
    }

    /*@Override
    public String getFontAwesome() {
        return "list-alt";
    }*/

    @Override
    public String getIcon() {
        return "bi bi-card-checklist";
    }

    @Override
    protected boolean getColumnableDefaultValue() {
        return true;
    }

    @Override
    public boolean isDisplayable(ContentContext ctx) throws Exception {
        ContentService content = ContentService.getInstance(ctx.getRequest());
        MenuElement menu = content.getNavigation(ctx);
        List<MenuElement> allChildren = menu.getAllChildrenList();
        return getSelectedPages(ctx, allChildren).size() > 1;
    }

}
