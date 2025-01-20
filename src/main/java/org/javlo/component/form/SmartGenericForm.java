package org.javlo.component.form;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.javlo.actions.IAction;
import org.javlo.actions.IEventRegistration;
import org.javlo.bean.Company;
import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.core.IDataContainer;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.data.InfoBean;
import org.javlo.helper.*;
import org.javlo.helper.Comparator.StringComparator;
import org.javlo.i18n.I18nAccess;
import org.javlo.mailing.EMail;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.mailing.MailService.Attachment;
import org.javlo.mailing.Mailing;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.CaptchaService;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;
import org.javlo.service.ReverseLinkService;
import org.javlo.service.database.DataBaseService;
import org.javlo.service.document.DataDocument;
import org.javlo.service.document.DataDocumentService;
import org.javlo.service.event.Event;
import org.javlo.service.google.translation.ITranslator;
import org.javlo.service.visitors.UserDataService;
import org.javlo.user.IUserFactory;
import org.javlo.user.IUserInfo;
import org.javlo.user.UserFactory;
import org.javlo.utils.*;
import org.javlo.ztatic.StaticInfo;

import jakarta.mail.internet.InternetAddress;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Logger;

public class SmartGenericForm extends AbstractVisualComponent implements IAction, IEventRegistration, IDataContainer {

    private static final String USER_FIELD = "_user";

    public static final String RECAPTCHASECRETKEY = "recaptchasecretkey";

    public static final String RECAPTCHAKEY = "recaptchakey";

    public static final String FOLDER = "dynamic-form-result";

    private static final String EDIT_LINE_PARAM = "_el";

    private static final String VALID_LINE_PARAM = "_vl";

    private static final String VALIDED = "__valided";

    private static final Object LOCK_SUBMIT = new Object();

    private Properties bundle;

    private Integer countCache = null;

    private static final Map<String, String> cacheForm = Collections.synchronizedMap(new TimeMap<String, String>(60 * 60 * 2)); // 2 hours validity

    private static Logger logger = Logger.getLogger(SmartGenericForm.class.getName());

    protected static final Object LOCK_ACCESS_FILE = new Object();

    public Properties getLocalConfig(boolean reload) {
        if (bundle == null || reload) {
            Properties newBundle = new StructuredProperties();
            try {
                newBundle.load(new StringReader(getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.bundle = newBundle;
        }
        return bundle;
    }

    protected File getAttachFolder(ContentContext ctx) throws Exception {
        File file = getFile(ctx);
        File dir = new File(URLHelper.mergePath(getFile(ctx).getParentFile().getAbsolutePath(), StringHelper.getFileNameWithoutExtension(file.getName())));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    protected boolean acceptLinks(ContentContext ctx) {
        return StringHelper.isTrue(getConfig(ctx).getProperty("content.links", null), false);
    }

    protected String getInputEditLineName(ContentContext ctx) throws Exception {
        return EDIT_LINE_PARAM + '_' + StringSecurityUtil.encode(getId(), ctx.getGlobalContext().getStaticConfig().getSecretKey());
    }

    protected String getInputValidLineName(ContentContext ctx) throws Exception {
        return VALID_LINE_PARAM + '_' + StringSecurityUtil.encode(getId(), ctx.getGlobalContext().getStaticConfig().getSecretKey());
    }

    protected String encodeEditNumber(ContentContext ctx, int number) throws Exception {
        return StringSecurityUtil.encode("" + number, ctx.getGlobalContext().getStaticConfig().getSecretKey());
    }

    protected int decodeEditNumber(ContentContext ctx, String number) throws NumberFormatException, Exception {
        try {
            if (!StringHelper.isEmpty(number)) {
                return Integer.parseInt(StringSecurityUtil.decode("" + number, ctx.getGlobalContext().getStaticConfig().getSecretKey()));
            }
        } catch (Throwable t) {
        }
        return -1;
    }

    protected int decodeUserEditNumber(ContentContext ctx, String number) throws Exception {
        if (ctx.getCurrentUserId() == null || !StringHelper.isDigit(number)) {
            return -1;
        }
        int targetNumber = Integer.parseInt(number);
        List<Map<String, String>> data = getData(ctx);
        int userNumber = 0;
        int lineNumber = 0;
        for (Map<String, String> line : data) {
            if (ctx.getCurrentUserId().equals(line.get(USER_FIELD))) {
                if (userNumber == targetNumber) {
                    return lineNumber;
                }
                userNumber++;
            }
            lineNumber++;
        }
        return -1;
    }

    protected boolean isCaptcha(ContentContext ctx) {
        return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "" + isCaptcha()));
    }

    public boolean isCaptcha() {
        return StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", "true"));
    }

    public String getRecaptchaKey() {
        return getLocalConfig(false).getProperty(RECAPTCHAKEY, null);
    }

    public String getRecaptchaSecretKey() {
        return getLocalConfig(false).getProperty(RECAPTCHASECRETKEY, null);
    }

    // public int getCountSubscription(ContentContext ctx) throws Exception {
    // if (countCache == null) {
    // File file = getFile(ctx);
    // if (!file.exists()) {
    // countCache = 0;
    // } else {
    // BufferedReader reader = new BufferedReader(new FileReader(file));
    // try {
    // int countLine = 0;
    // String read = reader.readLine();
    // while (read != null) {
    // countLine++;
    // read = reader.readLine();
    // }
    // countCache = countLine - 1;
    // } finally {
    // ResourceHelper.closeResource(reader);
    // }
    // }
    // }
    // return countCache;
    // }

    public int getCountSubscription(ContentContext ctx) throws Exception {
        if (countCache == null) {
            Field countField = null;
            for (Field field : getFields(ctx)) {
                if (field.getRole().equals(Field.ROLE_COUNT_PART)) {
                    countField = field;
                }
            }
            List<Map<String, String>> data = getData(ctx);
            if (countField == null) {
                countCache = data.size();
            } else {
                int countParticipal = 0;
                for (Map<String, String> line : data) {
                    if (StringHelper.isDigit(line.get(countField.getName()))) {
                        countParticipal += Integer.parseInt(line.get(countField.getName()));
                    }
                }
                countCache = countParticipal;
            }
        }
        return countCache;
    }


    private Set<InternetAddress> getParticipantsEmail(ContentContext ctx) throws Exception {
        Field emailField = null;
        Field firstnameField = null;
        Field lastnameField = null;
        for (Field field : getFields(ctx)) {
            if (field.getRole().equals("user_email")) {
                emailField = field;
            }
            if (emailField == null && field.getName().equalsIgnoreCase("email")) {
                emailField = field;
            }
            if (field.getRole().equalsIgnoreCase("user_firstname")) {
                firstnameField = field;
            }
            if (firstnameField == null && field.getName().equalsIgnoreCase("firstname")) {
                firstnameField = field;
            }
            if (field.getRole().equalsIgnoreCase("user_lastname")) {
                lastnameField = field;
            }
            if (lastnameField == null && field.getName().equalsIgnoreCase("lastname")) {
                lastnameField = field;
            }
        }

        Set<InternetAddress> out = new HashSet<>();
        List<Map<String, String>> data = getData(ctx);
        if (emailField == null) {
			logger.warning("email field not found.");
            return null;
        } else {
			System.out.println("#data = "+data.size());
            for (Map<String, String> line : data) {
				System.out.println("line.get('"+emailField.getName()+"') = "+line.get(emailField.getName()));
                if (StringHelper.isMail(line.get(emailField.getName()))) {
                    String firstname = "";
                    if (firstnameField != null) {
                        firstname = line.get(firstnameField.getName());
                    }
                    String lastname = "";
                    if (lastnameField != null) {
                        lastname = line.get(lastnameField.getName());
                    }
                    String displayName = (firstname + " " + lastname).trim();

                    InternetAddress internetAddress;
                    if (!StringHelper.isEmpty(displayName)) {
                        internetAddress = new InternetAddress(line.get(emailField.getName()), displayName);
                    } else {
                        internetAddress = new InternetAddress(line.get(emailField.getName()));
                    }
                    out.add(internetAddress);
                }
            }
        }
        return out;
    }

    @Override
    public List<Map<String, String>> getData(ContentContext ctx) throws Exception {
        File file = getFile(ctx);
        if (!file.exists()) {
            return Collections.EMPTY_LIST;
        } else {
            return CSVFactory.loadContentAsMap(file);
        }
    }

    @Override
    public void setValue(String inContent) {
        super.setValue(inContent);
        bundle = null;
    }

    public boolean isEvent() {
        return true;
    }

    public boolean isDocument() {
        return true;
    }

    public static final String TYPE = "smart-generic-form";

    private String getAutoCompleteHTMLList() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);
        String[] list = new String[]{"off", "on", "name", "given-name", "additional-name", "family-name", "honorific-suffix", "nickname", "email", "username", "new-password", "current-password", "one-time-code", "organization-title", "organization", "street-address", "address-line1", "address-line2", "address-line3", "address-level1", "country", "country-name", "postal-code", "cc-name", "cc-given-name", "cc-additional-name", "cc-family-name", "cc-number", "cc-exp", "cc-exp-month", "cc-exp-year", "cc-csc", "cc-type", "transaction-currency", "transaction-amount", "language", "bday", "bday-day", "bday-month", "bday-year", "sex", "tel", "tel-country-code", "tel-national", "tel-area-code", "tel-local", "tel-extension", "impp", "url", "photo"};
        out.println("<datalist id=\"autocomplete-list\">");
        for (String item : list) {
            out.print("<option value=\"" + item + "\">");
        }
        out.println("</datalist>");
        out.close();
        return new String(outStream.toByteArray());
    }

    @Override
    protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);
        StaticConfig staticConfig = ctx.getGlobalContext().getStaticConfig();

        // out.println(XHTMLHelper.renderLine("title", getInputName("title"),
        // getLocalConfig(false).getProperty("title", "")));
        // out.println(XHTMLHelper.renderLine("filename", getInputName("filename"),
        // getLocalConfig(false).getProperty("filename", "")));

        out.println("<div class=\"row\">");
        out.println("<div class=\"col-sm-2\"><label for=\"" + getInputName("title") + "\">title</label>");
        // out.println(div(label("title").attr("for",
        // getInputName("title"))).withClass("col-sm-2").render());
        out.println("</div><div class=\"form-group col-sm-10\"><input class=\"form-control\" id=\"" + getInputName("title") + "\" name=\"" + getInputName("title") + "\" value=\"" + getLocalConfig(false).getProperty("title", "") + "\" />");
        // out.println(div(div(input().withClass("form-control").attr("id",
        // getInputName("title")).attr("name",
        // getInputName("title")).attr("value",getLocalConfig(false).getProperty("title",
        // ""))).withClass("form-group")).withClass("col-sm-10").render());
        out.println("</div></div>");

        String dbprefix = getInputName("dbprefix");
        out.println("<div class=\"row\">");
        out.println("<div class=\"col-sm-2\"><label for=\"" + dbprefix + "\">DB table prefix</label>");
        out.println("</div><div class=\"form-group col-sm-6\"><input class=\"form-control\" id=\"" + dbprefix + "\" name=\"" + dbprefix + "\" value=\"" + getLocalConfig(false).getProperty("dbprefix", "") + "\" />");
        out.println("</div></div>");

        String inputName = getInputName("filename");
        out.println("<div class=\"row\">");
        out.println("<div class=\"col-sm-2\"><label for=\"" + inputName + "\">filename</label>");
        // out.println(div(label("filename").attr("for",
        // inputName)).withClass("col-sm-2").render());
        out.println("</div><div class=\"form-group col-sm-6\"><input class=\"form-control\" id=\"" + inputName + "\" name=\"" + inputName + "\" value=\"" + getLocalConfig(false).getProperty("filename", "") + "\" />");
        // out.println(div(div(input().withClass("form-control").attr("id",
        // inputName).attr("name",
        // inputName).attr("value",getLocalConfig(false).getProperty("filename",
        // ""))).withClass("form-group")).withClass("col-sm-8").render());
        String csvLink = URLHelper.createResourceURL(ctx, URLHelper.mergePath("/", staticConfig.getStaticFolder(), FOLDER, getLocalConfig(false).getProperty("filename", "")));
        String xlsxLink = FilenameUtils.removeExtension(csvLink) + ".xlsx";
        String xlsLightLink = URLHelper.addParam(xlsxLink, "light", "true");
        out.println("</div><div class=\"col-sm-2\"><a href=\"" + xlsxLink + "\">[XSLX-LG]</a> - <a href=\"" + xlsLightLink + "\">[XSLX-SM]</a> - <a href=\"" + csvLink + "\">[CSV]</a><label style=\"float: right\">&nbsp;" + XHTMLHelper.getCheckbox(getInputName("store-label"), isStoreLabel()) + " store label</label></div>");
        out.println("<div class=\"col-sm-2\"><button class=\"btn btn-default btn-xs\" type=\"submit\" name=\"reset-file\" value=\"" + getLocalConfig(false).getProperty("filename", "") + "\">reset</button></div>");
        // out.println(div(a("[XSLX]").attr("href", xlsxLink),span(" -
        // "),a("[CSV]").attr("href", csvLink)).withClass("col-sm-2").render());
        out.println("</div>");

        File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), GenericForm.DYNAMIC_FORM_RESULT_FOLDER, getLocalConfig(false).getProperty("filename", "")));
        out.println("<div class=\"backup-file-list\">");
        for (File f : ResourceHelper.getAllFiles(file.getParentFile(), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(file.getName());
            }
        })) {
            out.print("<span class=\"backup-file\" style=\"font-size: 0.8em; display: inline-block; margin-left: 15px; font-style: italic;\">" + f.getName() + "</span>");
        }
        out.println("</div>");

        out.println("<div class=\"row\"><div class=\"col-sm-2\">");
        out.println(XHTMLHelper.renderLine("captcha", getInputName("captcha"), StringHelper.isTrue(getLocalConfig(false).getProperty("captcha", null))));
        out.println("</div><div class=\"col-sm-5\">");
        out.println(XHTMLHelper.renderLine("google recaptcha site key", getInputName(RECAPTCHAKEY), getLocalConfig(false).getProperty(RECAPTCHAKEY, "")));
        out.println("</div><div class=\"col-sm-5\">");
        out.println(XHTMLHelper.renderLine("google secret key", getInputName(RECAPTCHASECRETKEY), getLocalConfig(false).getProperty(RECAPTCHASECRETKEY, "")));
        out.println("</div></div>");
        if (isFile()) {
            out.println(XHTMLHelper.renderLine("max file size (Kb)", getInputName("filesize"), "" + getMaxFileSize()));
        }
        out.println("<div class=\"col-group\"><div class=\"one_half\"><fieldset><legend>e-mail</legend>");
        out.println(XHTMLHelper.renderLine("mail to :", getInputName("to"), getLocalConfig(false).getProperty("mail.to", "")));
        out.println(XHTMLHelper.renderLine("mail cc :", getInputName("cc"), getLocalConfig(false).getProperty("mail.cc", "")));
        out.println(XHTMLHelper.renderLine("mail bcc :", getInputName("bcc"), getLocalConfig(false).getProperty("mail.bcc", "")));
        out.println(XHTMLHelper.renderLine("mail subject :", getInputName("subject"), getLocalConfig(false).getProperty("mail.subject", "")));
        out.println(XHTMLHelper.renderLine("mail subject field :", getInputName("subject-field"), getLocalConfig(false).getProperty("mail.subject.field", "")));
        out.println(XHTMLHelper.renderLine("mail from :", getInputName("from"), getLocalConfig(false).getProperty("mail.from", "")));
        out.println(XHTMLHelper.renderLine("mail from field :", getInputName("from-field"), getLocalConfig(false).getProperty("mail.from.field", "")));
        out.println("</fieldset></div>");

        /** MESSAGE **/
        out.println("<div class=\"one_half\"><fieldset><legend>message</legend>");
        out.println(XHTMLHelper.renderLine("field required :", getInputName("message-required"), getLocalConfig(false).getProperty("message.required", "")));
        out.println(XHTMLHelper.renderLine("error generic :", getInputName("error-generic"), getLocalConfig(false).getProperty("error.generic", "")));
        out.println(XHTMLHelper.renderLine("error required :", getInputName("error-required"), getLocalConfig(false).getProperty("error.required", "")));
        out.println(XHTMLHelper.renderLine("thanks :", getInputName("message-thanks"), getLocalConfig(false).getProperty("message.thanks", "")));
        out.println(XHTMLHelper.renderLine("error :", getInputName("message-error"), getLocalConfig(false).getProperty("message.error", "")));
        out.println(XHTMLHelper.renderLine("reset :", getInputName("message-reset"), getLocalConfig(false).getProperty("message.reset", "")));
        if (isCaptcha()) {
            out.println(XHTMLHelper.renderLine("captcha :", getInputName("label-captcha"), getLocalConfig(false).getProperty("label.captcha", "")));
            out.println(XHTMLHelper.renderLine("captcha error :", getInputName("error.captcha"), getLocalConfig(false).getProperty("error.captcha", "")));
        }
        if (isFile()) {
            out.println(XHTMLHelper.renderLine("bad file format :", getInputName("message-bad-file"), getLocalConfig(false).getProperty("message.bad-file", "")));
            out.println(XHTMLHelper.renderLine("file to big :", getInputName("message-tobig-file"), getLocalConfig(false).getProperty("message.tobig-file", "")));
        }
        out.println("</fieldset></div></div>");

        /** EVENT **/
        if (isEvent()) {
            out.println("<fieldset><legend>Event</legend><div class=\"row\"><div class=\"col-sm-6\"><div class=\"row\"><div class=\"col-xs-6\">");
            out.println(XHTMLHelper.renderLine("subscribe limit :", getInputName("event-limit"), getLocalConfig(false).getProperty("event.limit", "")));
            out.println("</div><div class=\"col-xs-6\">");
            out.println(XHTMLHelper.renderLine("subscribe alert :", getInputName("event.alert-limit"), getLocalConfig(false).getProperty("event.alert-limit", "")));
            out.println("</div></div></div><div class=\"col-sm-6\">");
            out.println(XHTMLHelper.renderLine("current subscription :", "" + getCountSubscription(ctx)));
            out.println("</div></div><div class=\"row\"><div class=\"col-xs-6\">");
            out.println(XHTMLHelper.renderLine("confirm subject :", getInputName("mail-confirm-subject"), getLocalConfig(false).getProperty("mail.confirm.subject", "")));
            out.println("<div class=\"line validation-email\"><label>Confirm Email page : </label>");
            out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("mail-confirm-link"), getLocalConfig(false).getProperty("mail.confirm.link", ""), true) + "</div>");
            out.println(XHTMLHelper.renderLine("open message :", getInputName("event-open-message"), getLocalConfig(false).getProperty("event.open.message", "")));
            out.println("</div><div class=\"col-xs-6\">");
            out.println(XHTMLHelper.renderLine("closed subject :", getInputName("mail-closed-subject"), getLocalConfig(false).getProperty("mail.closed.subject", "")));
            out.println("<div class=\"line validation-email\"><label>Closed Email page : </label>");
            out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("mail-closed-link"), getLocalConfig(false).getProperty("mail.closed.link", ""), true) + "</div>");
            out.println(XHTMLHelper.renderLine("close message :", getInputName("event-close-message"), getLocalConfig(false).getProperty("event.close.message", "")));
            out.println("</div></div></fieldset>");

            /** Reminder **/
            String mailIcon = "<i class=\"bi bi-envelope-slash\"></i>";
            Mailing mailing = getMailing(ctx);
            if (mailing.isSend()) {
                mailIcon = "<i class=\"bi bi-envelope-check\"></i>";
            }
            out.println("<fieldset><legend>Reminder " + mailIcon + "</legend>");
            out.println(XHTMLHelper.renderLine("days before event notif. :", getInputName("event-reminder-date"), getLocalConfig(false).getProperty("event.reminder-date", "1")));
            out.println(XHTMLHelper.renderLine("notification subject :", getInputName("event-reminder-mail-subject"), StringHelper.neverEmpty(mailing.getSubject(), "")));
            out.println("<div class=\"line validation-email\"><label>Reminder Email page : </label>");
            out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("event-reminder-link"), getLocalConfig(false).getProperty("event.reminder-link", ""), true));
            out.println("</div></fieldset>");
        }

        /** DOC **/
        if (isDocument()) {
            out.println("<fieldset><legend>Doc</legend><div class=\"row\"><div class=\"col-sm-6\">");
            out.println(XHTMLNavigationHelper.renderComboNavigation(ctx, getPage().getRoot(), getInputName("doc-link"), getLocalConfig(false).getProperty("doc-link", ""), true));
            out.println("</div><div class=\"col-sm-6\">");
            out.println("<input class=\"form-control\" placeholder=\"category\" name=\"" + getInputName("doc-category") + "\" value=\"" + getLocalConfig(false).getProperty("doc-category", "") + "\" />");
            out.println("</div></div></fieldset>");
        }

        out.println("<div class=\"action-add\"><input type=\"text\" name=\"" + getInputName("new-name") + "\" placeholder=\"field name\" /> <input type=\"submit\" name=\"" + getInputName("add-first") + "\" value=\"add as first\" /> <input type=\"submit\" name=\"" + getInputName("add") + "\" value=\"add as last\" /></div>");
        if (getFields(ctx).size() > 0) {
            out.println(getAutoCompleteHTMLList());
            out.println("<table class=\"sTable2\">");
            String listTitle = "";
            if (isList()) {
                listTitle = "<td>list (>list name, #> start empty)</td>";
            }
            out.println("<thead><tr><td>name</td><td>label</td><td>condition</td><td>autocomplete</td>" + listTitle + "<td>type</td><td>role</td><td>width</td><td>required</td><td>order</td><td>action</td></tr></thead>");
            out.println("<tbody>");
            List<Field> fields = getFields(ctx);
            for (Field field : fields) {
                out.println(getEditXHTML(ctx, field));
            }
            out.println("</tbody>");
            out.println("</table>");
        }

        out.println("<br /><div class=\"row upload\">");
        out.println("<div class=\"col-sm-2\"><label for=\"" + getInputName("title") + "\">import fields as xlsx</label></div>");
        out.println("<div class=\"col-sm-6\"><input type=\"file\" name=\"" + getInputName("form-as-excel") + "\" /></div>");

        String downloadExcelUrl = URLHelper.createActionURL(ctx, getActionGroupName() + ".downloadForm", getType() + getId() + ".xlsx");
        downloadExcelUrl = URLHelper.addParam(downloadExcelUrl, IContentVisualComponent.COMP_ID_REQUEST_PARAM, getId());

        out.println("<div class=\"col-sm-4 align-right\"><a target=\"_blank\" href=\"" + downloadExcelUrl + "\">" + StringHelper.stringToFileName(getPage().getTitle(ctx)) + "_" + getId() + ".xlsx</a></div>");
        out.println("</div>");

        out.close();
        return new String(outStream.toByteArray());
    }

    private boolean isStoreLabel() {
        return StringHelper.isTrue(getLocalConfig(false).getProperty("store.label", null), false);
    }

    private String getEditXHTML(ContentContext ctx, Field field) {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outStream);
        String scrollToMe = "";
        if (StringHelper.neverNull(ctx.getRequest().getAttribute(getNewFieldKey())).equals(field.getName())) {
            scrollToMe = " scroll-to-me";
        }
        out.println("<tr class=\"field-line" + scrollToMe + "\">");
        out.println("<td class=\"input name\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("name-" + field.getName()) + "\" value=\"" + field.getName() + "\"/></td>");
        out.println("<td class=\"input label\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("label-" + field.getName()) + "\" value=\"" + field.getLabel() + "\"/></td>");
        out.println("<td class=\"input condition\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("condition-" + field.getName()) + "\" value=\"" + field.getCondition() + "\"/></td>");
        out.println("<td class=\"input autocomplete\"><input class=\"form-control\" type=\"text\" name=\"" + getInputName("autocomplete-" + field.getName()) + "\" list=\"autocomplete-list\" value=\"" + field.getAutocomplete() + "\"/></td>");
        if (isList()) {
            if (field.isNeedList()) {
                out.println("<td class=\"list\"><textarea class=\"form-control\" name=\"" + getInputName("list-" + field.getName()) + "\">" + field.getRawList() + "</textarea></td>");
            } else if (field.getType().equals("registered-list")) {
                out.println("<td class=\"list\"><input class=\"form-control\" name=\"" + getInputName("registered-list-" + field.getName()) + "\" placeholder=\"list name\" value=\"" + field.getRegisteredList() + "\"/></td>");
            } else {
                out.println("<td class=\"list\">&nbsp;</td>");
            }
        }
        out.println("<td class=\"type\">" + XHTMLHelper.getInputOneSelect(getInputName("type-" + field.getName()), field.getFieldTypes(), field.getType(), "form-control", (String) null, true) + "</td>");
        out.println("<td class=\"role\">" + XHTMLHelper.getInputOneSelect(getInputName("role-" + field.getName()), field.getFieldRoles(), field.getRole(), "form-control", (String) null, true) + "</td>");
        out.println("<td class=\"width\"><select class=\"form-control\" name=\"" + getInputName("width-" + field.getName()) + "\" >");
        for (int i = 1; i <= 12; i++) {
            String selected = "";
            if (i == field.getWidth()) {
                selected = " selected=\"selected\"";
            }
            out.println("<option" + selected + ">" + i + "</option>");
        }
        out.println("</select></td>");
        String required = "";
        if (field.isRequire()) {
            required = " checked=\"checked\"";
        }
        out.println("<td class=\"required\" style=\"display: flex; justify-content: center; align-items: center; height: 30px;\"><input type=\"checkbox\" name=\"" + getInputName("require-" + field.getName()) + "\"" + required + " /></td>");
        out.println("<td class=\"required\"><input style=\"width: 64px\" type=\"number\" min=\"0\" class=\"form-control\" name=\"" + getInputName("order-" + field.getName()) + "\" value=\"" + field.getOrder() + "\" /></td>");
        out.println("<td class=\"buttons\"><div  class=\"btn-group btn-group-sm\">");
        out.println("  <button class=\"up btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("up-" + field.getName()) + "\" ><span class=\"glyphicon glyphicon-menu-up\" aria-hidden=\"true\"></span></button>");
        out.println("  <button class=\"down btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("down-" + field.getName()) + "\"><span class=\"glyphicon glyphicon-menu-down\" aria-hidden=\"true\"></span></button>");
        out.println("  <button class=\"needconfirm btn btn-default btn-sm ajax\" type=\"submit\" name=\"" + getInputName("del-" + field.getName()) + "\" ><span class=\"glyphicon glyphicon-trash\" aria-hidden=\"true\"></span></button>");
        out.println("</div></td>");
        out.println("</tr>");

        out.close();
        return new String(outStream.toByteArray());
    }

    protected Field getField(ContentContext ctx, String fieldName) {
        for (Field field : getFields(ctx)) {
            if (field.getName().equalsIgnoreCase(fieldName)) {
                return field;
            }
        }
        return null;
    }

    @Deprecated
    /**
     * use fields directly
     *
     * @return
     */
    public synchronized List<Field> getFields() {
        return getFields(null);
    }

    public List<Field> getFields(ContentContext ctx) {
        List<Field> fields = new LinkedList<Field>();
        Properties p = getLocalConfig(false);

        for (Object objKey : p.keySet()) {
            String key = objKey.toString();
            if (key.startsWith("field.")) {
                String name = key.replaceFirst("field.", "").trim();
                if (name.trim().length() > 0) {
                    String value = p.getProperty(key);
                    String[] data = StringUtils.splitPreserveAllTokens(value, Field.SEP);
                    String label = (String) LangHelper.arrays(data, 0, "");
                    try {
                        // change reverlink only in view mode !
                        if (ctx != null && label.length() > 0 && ctx.isAsViewMode()) {
                            label = ReverseLinkService.getInstance(ctx.getGlobalContext()).replaceLink(ctx, this, label);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Field field = new Field(ctx, name, label, (String) LangHelper.arrays(data, 1, ""), (String) LangHelper.arrays(data, 9, ""), (String) LangHelper.arrays(data, 8, ""), (String) LangHelper.arrays(data, 2, ""), (String) LangHelper.arrays(data, 3, ""), (String) LangHelper.arrays(data, 5, ""), Integer.parseInt("" + LangHelper.arrays(data, 6, "0")), Integer.parseInt("" + LangHelper.arrays(data, 7, "6")), (String) LangHelper.arrays(data, 10, ""));
                    fields.add(field);
                }
            }
        }
        Collections.sort(fields, new Field.FieldComparator());
        int currentWidth = 0;
        Field lastField = null;
        for (Field field : fields) {
            if (!field.getType().equals("hidden")) {
                if (currentWidth == 0) {
                    field.setFirst(true);
                }
                currentWidth = currentWidth + field.getWidth();
                if (currentWidth >= 12) {
                    field.setLast(true);
                    currentWidth = 0;
                }
                lastField = field;
            }
        }
        if (lastField != null) {
            lastField.setLast(true);
        }
        return fields;
    }

    public Mailing getMailing(ContentContext ctx) throws IOException {
        Mailing out = new Mailing();
        out.load(StaticConfig.getInstance(ctx.getServletContext()).getMailingStaticConfig(), getId());
        return out;
    }

    public boolean isFile() {
        for (Field field : getFields(null)) {
            if (field.getType().equals("file")) {
                return true;
            }
        }
        return false;
    }

    public boolean isList() {
        for (Field field : getFields(null)) {
            if (field.isNeedList()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getRenderer(ContentContext ctx) {
        return super.getRenderer(ctx);
    }

    public String getTitle() {
        return getLocalConfig(false).getProperty("title");
    }

    protected synchronized void store(Field field) {
        String key = field.getPrefix() + '.' + field.getName();
        Properties prop = getLocalConfig(false);
        if (prop.contains(key)) {
            prop.remove(prop);
        }
        getLocalConfig(false).put(key, field.toString());
    }

    protected synchronized void delField(String name) {
        getLocalConfig(false).remove("field." + name);
    }

    public void store(ContentContext ctx) throws IOException {
        Writer writer = new StringWriter();
        getLocalConfig(false).store(writer, "comp:" + getId());
        if (!getValue().equals(writer.toString())) {
            setValue(writer.toString());
            setModify();
            setNeedRefresh(true);
        }
    }

    public boolean isClose(ContentContext ctx) throws Exception {
        Properties localConfig = getLocalConfig(false);
        String eventLimistStr = localConfig.getProperty("event.limit");
        if (StringHelper.isDigit(eventLimistStr)) {
            int maxSubscription = Integer.parseInt(eventLimistStr);
            if (maxSubscription > 0) {
                int countSubscription = getCountSubscription(ctx);
                if (countSubscription >= maxSubscription) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isFilledFromCookies() {
        return false;
    }

    protected String getDataKey() {
        String prefix = "survey-" + getPage().getParent().getId() + '-';
        return prefix + getType() + getId();
    }

    @Override
    public void prepareView(ContentContext ctx) throws Exception {
        super.prepareView(ctx);
        CaptchaService.getInstance(ctx.getRequest().getSession());
        Properties localConfig = getLocalConfig(false);
        ctx.getRequest().setAttribute("ci18n", localConfig);
        String code = StringHelper.getRandomId();
        cacheForm.put(code, "");
        ctx.getRequest().setAttribute("formCode", code);
        String eventLimistStr = localConfig.getProperty("event.limit");
        ctx.getRequest().setAttribute("openEvent", true);
        ctx.getRequest().setAttribute("fields", getFields(ctx));
        if (StringHelper.isDigit(eventLimistStr)) {
            int maxSubscription = Integer.parseInt(eventLimistStr);
            if (maxSubscription > 0) {
                int countSubscription = getCountSubscription(ctx);
                ctx.getRequest().setAttribute("countSubscription", countSubscription);
                GenericMessage msg;
                if (countSubscription < maxSubscription) {
                    msg = new GenericMessage((String) localConfig.get("event.open.message"), GenericMessage.INFO);
                } else {
                    ctx.getRequest().setAttribute("openEvent", false);
                    msg = new GenericMessage((String) localConfig.get("event.close.message"), GenericMessage.ALERT);
                }
                if (ctx.getRequest().getAttribute("msg") == null) {
                    ctx.getRequest().setAttribute("msg", msg);
                }
            }
        }
        RequestService rs = RequestService.getInstance(ctx.getRequest());
        String editLineStr = rs.getParameter(getInputEditLineName(ctx));
        int editLine = decodeEditNumber(ctx, editLineStr);
        if (editLine <= 0 && ctx.getCurrentUserId() != null) {
            editLine = decodeUserEditNumber(ctx, rs.getParameter("line"));
        }
        if (editLine > 0) {
            synchronized (LOCK_ACCESS_FILE) {
                ctx.getRequest().setAttribute("editForm", "true");
                ctx.getRequest().setAttribute("editLine", editLineStr);
                ctx.getRequest().setAttribute("inputLine", getInputEditLineName(ctx));
                File csvFile = getFile(ctx);
                List<Map<String, String>> data = CSVFactory.loadContentAsMap(csvFile);
                if (data.size() > editLine) {
                    Map<String, String> line = data.get(editLine);
                    for (Map.Entry<String, String> entry : line.entrySet()) {
                        rs.setParameter(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        String validLineStr = rs.getParameter(getInputValidLineName(ctx));
        int validLine = decodeEditNumber(ctx, validLineStr);
        if (validLine > 0) {
            logger.info("valid line : " + validLine);
            synchronized (LOCK_ACCESS_FILE) {
                ctx.getRequest().setAttribute("validForm", true);
                ctx.getRequest().setAttribute("editLine", editLineStr);
                ctx.getRequest().setAttribute("inputLine", getInputValidLineName(ctx));

                File csvFile = getFile(ctx);
                List<Map<String, String>> data = CSVFactory.loadContentAsMap(csvFile);
                if (data.size() >= validLine) {
                    Map<String, String> line = data.get(validLine);
                    for (Map.Entry<String, String> entry : line.entrySet()) {
                        rs.setParameter(entry.getKey(), entry.getValue());
                    }
                }

                if (data.size() >= validLine) {
                    Map<String, String> line = data.get(validLine);
                    line.put(VALIDED, "true");
                }
                CSVFactory.storeContentAsMap(csvFile, data, false);

                MessageRepository messageRepository = MessageRepository.getInstance(ctx);
                GenericMessage msg = new GenericMessage(I18nAccess.getInstance(ctx).getViewText("form.confirm", "data is confirmed."), GenericMessage.INFO);
                messageRepository.setGlobalMessage(msg);
                if (ctx.getRequest().getAttribute("msg") == null) {
                    ctx.getRequest().setAttribute("msg", msg);
                }
            }
        }

        Map<String, String> data = null;
        if (isFilledFromCookies()) {
            UserDataService userDataService = UserDataService.getInstance(ctx);
            String data1 = userDataService.getUserData(ctx, getDataKey());
            if (!StringHelper.isEmpty(data1)) {
                data = StringHelper.stringToMap(data1);
                for (Field field : getFields(ctx)) {
                    if (data.get(field.getName()) != null) {
                        field.setValue(data.get(field.getName()));
                    }
                }
            }
        }

        if (ctx.getCurrentUser() != null) {
            for (Field field : getFields(ctx)) {
                if (field.getRole().startsWith("user_") && StringHelper.isEmpty(rs.getParameter(field.getName()))) {
                    String userAttribute = field.getRole().substring("user_".length());
                    userAttribute = userAttribute.substring(0, 1).toUpperCase() + userAttribute.substring(1);
                    IUserInfo userInfo = ctx.getCurrentUser().getUserInfo();
                    try {
                        Method method = userInfo.getClass().getMethod("get" + userAttribute);
                        if (method != null) {
                            rs.setParameter(field.getName(), "" + method.invoke(userInfo));
                        }
                    } catch (NoSuchMethodError e) {

                    }
                }
            }
        }

        if (!StringHelper.isEmpty(ctx.getGlobalContext().getSpecialConfig().get(RECAPTCHAKEY, null)) && !StringHelper.isEmpty(ctx.getGlobalContext().getSpecialConfig().get(RECAPTCHASECRETKEY, null))) {
            if (StringHelper.isEmpty(getRecaptchaKey())) {
                getLocalConfig(false).setProperty(RECAPTCHAKEY, "" + ctx.getGlobalContext().getSpecialConfig().get(RECAPTCHAKEY, null));
                getLocalConfig(false).setProperty(RECAPTCHASECRETKEY, "" + ctx.getGlobalContext().getSpecialConfig().get(RECAPTCHASECRETKEY, null));
            }
        }

    }

    protected boolean isUpdate(ContentContext ctx) throws Exception {
        RequestService rs = RequestService.getInstance(ctx.getRequest());
        int num = decodeEditNumber(ctx, rs.getParameter(getInputEditLineName(ctx)));
        return num > 0;

    }

    protected long getMaxFileSize() {
        String fileSize = getLocalConfig(false).getProperty("file.max-size");
        if (StringHelper.isDigit(fileSize)) {
            return Long.parseLong(fileSize);
        } else {
            return 0;
        }
    }

    protected String getNewFieldKey() {
        return "_new_field_" + getId();
    }

    protected boolean isWarningEventSite(ContentContext ctx) throws Exception {
        Properties localConfig = getLocalConfig(false);
        String eventLimistStr = localConfig.getProperty("event.alert-limit");
        int countSubscription = getCountSubscription(ctx);
        if (StringHelper.isDigit(eventLimistStr)) {
            return Integer.parseInt(eventLimistStr) == countSubscription;
        } else {
            return false;
        }
    }

    protected boolean isClosedEventSite(ContentContext ctx) throws Exception {
        Properties localConfig = getLocalConfig(false);
        String eventLimistStr = localConfig.getProperty("event.limit");
        int countSubscription = getCountSubscription(ctx);
        if (StringHelper.isDigit(eventLimistStr)) {
            return Integer.parseInt(eventLimistStr) == countSubscription;
        } else {
            return false;
        }
    }

    protected boolean isFullEventSite(ContentContext ctx) throws Exception {
        Properties localConfig = getLocalConfig(false);
        String eventLimistStr = localConfig.getProperty("event.limit");
        int countSubscription = getCountSubscription(ctx);
        if (StringHelper.isDigit(eventLimistStr)) {
            return Integer.parseInt(eventLimistStr) < countSubscription;
        } else {
            return false;
        }
    }

    protected boolean importFieldAsExcel(ContentContext ctx) throws Exception {
        RequestService rs = RequestService.getInstance(ctx.getRequest());
        FileItem item = rs.getFileItem(getInputName("form-as-excel"));
        if (item != null) {
            InputStream in = item.getInputStream();
            if (in != null) {
                Cell[][] cells = XLSTools.getXLSXArray(ctx, in, null);
                if (cells.length < 2) {
                    logger.warning("bad form-as-excel file empty");
                    return false;
                }
                if (cells[0].length != 9 && cells[0].length != 10) {
                    logger.warning("bad form-as-excel width : " + cells[0].length + " in place of 9.");
                    return false;
                }
                for (Field field : getFields(ctx)) {
                    delField(field.getName());
                }
                for (int i = 1; i < cells.length; i++) {
                    if (cells[i].length < 4) {
                        return false;
                    } else {
                        int order = i;
                        if (StringHelper.isDigit(cells[i][6].getValue())) {
                            order = Integer.parseInt(cells[i][6].getValue());
                        }
                        int width = 12;
                        if (StringHelper.isDigit(cells[i][7].getValue())) {
                            width = Integer.parseInt(cells[i][7].getValue());
                        }
                        if (!StringHelper.isEmpty(cells[i][0].getValue())) {
                            String label = StringHelper.neverEmpty(cells[i][1].getValue(), "");
                            String type = StringHelper.neverEmpty(cells[i][2].getValue(), "");
                            String cond = StringHelper.neverEmpty(cells[i][3].getValue(), "");
                            String list = StringHelper.neverEmpty(cells[i][4].getValue(), "").replace(',', '\n');
                            String regList = StringHelper.neverEmpty(cells[i][5].getValue(), "");
                            // public Field(String name, String label, String type, String role, String
                            // condition, String value, String list, String registeredList, int order, int
                            // width) {
                            Field field = new Field(ctx, cells[i][0].getValue(), label, type, "", cond, "", list, regList, order, width, "");
                            field.setRequire(StringHelper.isTrue(cells[i][8].getValue()));
                            if (cells[i].length > 9) {
                                field.setAutocomplete(StringHelper.neverNull(cells[i][9].getValue()));
                            }
                            store(field);
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void configureMailing(ContentContext ctx, boolean updateContent) throws Exception {
        Mailing mailing = getMailing(ctx);
        mailing.setId(StaticConfig.getInstance(ctx.getServletContext()).getMailingStaticConfig(), getId());
        if (mailing.isSend()) {
            logger.warning("mailing already send.");
        } else {
            RequestService rs = RequestService.getInstance(ctx.getRequest());
            String pagePath = rs.getParameter(getInputName("event-reminder-link"), null);
            mailing.setFrom(new InternetAddress(ctx.getGlobalContext().getMailFrom()));
            mailing.setSubject(rs.getParameter(getInputName("event-reminder-mail-subject"), ""));

            ContentContext absCtx = ctx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE).getContextForAbsoluteURL();
            Map<String, String> params = new HashMap<>();

            params.put("eventTitle", getPage().getTitle(ctx));
            params.put("eventDescription", getPage().getDescription(ctx).getText());
            params.put("eventDate", getPage().getTimeRange(ctx).getRenderDates());
            params.put("eventUrl", URLHelper.createURL(absCtx, getPage()));

            if (updateContent) {
                URL url = new URL(URLHelper.createURL(absCtx, pagePath, params));
                String email = NetHelper.readPageForMailing(url);
                mailing.setContent(email);
            }
			mailing.setReceivers(getParticipantsEmail(ctx));
            mailing.store(ctx.getServletContext());

            mailing.setSmtpHost(ctx.getGlobalContext().getSMTPHost());
            mailing.setSmtpPort(ctx.getGlobalContext().getSMTPPort());
            mailing.setSmtpUser(ctx.getGlobalContext().getSMTPUser());
            mailing.setSmtpPassword(ctx.getGlobalContext().getSMTPPassword());
        }
    }

    @Override
    public String performEdit(ContentContext ctx) throws Exception {
        performColumnable(ctx);
        RequestService rs = RequestService.getInstance(ctx.getRequest());
        getLocalConfig(false).setProperty("title", rs.getParameter(getInputName("title"), ""));
        getLocalConfig(false).setProperty("dbprefix", rs.getParameter(getInputName("dbprefix"), ""));
        getLocalConfig(false).setProperty("filename", rs.getParameter(getInputName("filename"), ""));
        boolean oldCaptcha = isCaptcha();
        getLocalConfig(false).setProperty("captcha", rs.getParameter(getInputName("captcha"), ""));
        getLocalConfig(false).setProperty(RECAPTCHAKEY, rs.getParameter(getInputName(RECAPTCHAKEY), ""));
        getLocalConfig(false).setProperty(RECAPTCHASECRETKEY, rs.getParameter(getInputName(RECAPTCHASECRETKEY), ""));
        if (oldCaptcha != isCaptcha()) {
            ctx.setClosePopup(false);
        }
        getLocalConfig(false).setProperty("file.max-size", rs.getParameter(getInputName("filesize"), ""));
        getLocalConfig(false).setProperty("mail.to", rs.getParameter(getInputName("to"), ""));
        getLocalConfig(false).setProperty("mail.cc", rs.getParameter(getInputName("cc"), ""));
        getLocalConfig(false).setProperty("mail.bcc", rs.getParameter(getInputName("bcc"), ""));
        getLocalConfig(false).setProperty("mail.subject", rs.getParameter(getInputName("subject"), ""));
        getLocalConfig(false).setProperty("mail.subject.field", rs.getParameter(getInputName("subject-field"), ""));
        getLocalConfig(false).setProperty("mail.from", rs.getParameter(getInputName("from"), ""));
        getLocalConfig(false).setProperty("mail.from.field", rs.getParameter(getInputName("from-field"), ""));
        getLocalConfig(false).setProperty("error.generic", rs.getParameter(getInputName("error-generic"), ""));
        getLocalConfig(false).setProperty("message.required", rs.getParameter(getInputName("message-required"), ""));
        getLocalConfig(false).setProperty("error.required", rs.getParameter(getInputName("error-required"), ""));
        getLocalConfig(false).setProperty("message.thanks", rs.getParameter(getInputName("message-thanks"), ""));
        getLocalConfig(false).setProperty("message.error", rs.getParameter(getInputName("message-error"), ""));
        getLocalConfig(false).setProperty("message.reset", rs.getParameter(getInputName("message-reset"), ""));

        getLocalConfig(false).setProperty("event.limit", rs.getParameter(getInputName("event-limit"), ""));
        getLocalConfig(false).setProperty("event.alert-limit", rs.getParameter(getInputName("event.alert-limit"), ""));
        getLocalConfig(false).setProperty("mail.confirm.subject", rs.getParameter(getInputName("mail-confirm-subject"), ""));
        getLocalConfig(false).setProperty("mail.confirm.link", rs.getParameter(getInputName("mail-confirm-link"), ""));
        getLocalConfig(false).setProperty("mail.closed.subject", rs.getParameter(getInputName("mail-closed-subject"), ""));
        getLocalConfig(false).setProperty("mail.closed.link", rs.getParameter(getInputName("mail-closed-link"), ""));
        getLocalConfig(false).setProperty("event.open.message", rs.getParameter(getInputName("event-open-message"), ""));
        getLocalConfig(false).setProperty("event.close.message", rs.getParameter(getInputName("event-close-message"), ""));

        getLocalConfig(false).setProperty("doc-link", rs.getParameter(getInputName("doc-link"), ""));
        getLocalConfig(false).setProperty("doc-category", rs.getParameter(getInputName("doc-category"), ""));

        getLocalConfig(false).setProperty("store.label", rs.getParameter(getInputName("store-label"), ""));

        getLocalConfig(false).setProperty("event.reminder-date", rs.getParameter(getInputName("event-reminder-date"), ""));
        getLocalConfig(false).setProperty("event.reminder-link", rs.getParameter(getInputName("event-reminder-link"), ""));

        if (isCaptcha()) {
            getLocalConfig(false).setProperty("label.captcha", rs.getParameter(getInputName("label-captcha"), ""));
            getLocalConfig(false).setProperty("error.captcha", rs.getParameter(getInputName("error.captcha"), ""));
        }
        // getLocalConfig(false).setProperty("",
        // rs.getParameter(getInputName(""), ""));

        if (isFile()) {
            getLocalConfig(false).setProperty("message.bad-file", rs.getParameter(getInputName("message-bad-file"), ""));
            getLocalConfig(false).setProperty("message.tobig-file", rs.getParameter(getInputName("message-tobig-file"), ""));
        }

        int pos = 10;
        for (Field field : getFields(ctx)) {
            field.setOrder(pos);
            pos = pos + 10;
            String oldName = field.getName();

            String name = getInputName("del-" + oldName);
            if (rs.getParameter(name, null) != null) {
                delField(oldName);
            } else {
                field.setName(rs.getParameter(getInputName("name-" + oldName), ""));
                field.setRequire(rs.getParameter(getInputName("require-" + oldName), null) != null);
                field.setLabel(rs.getParameter(getInputName("label-" + oldName), ""));
                String cond = rs.getParameter(getInputName("condition-" + oldName), "");
                field.setCondition(cond);
                String autocomplete = rs.getParameter(getInputName("autocomplete-" + oldName), "");
                field.setAutocomplete(autocomplete);
                field.setType(rs.getParameter(getInputName("type-" + oldName), ""));
                field.setRole(rs.getParameter(getInputName("role-" + oldName), ""));
                field.setWidth(Integer.parseInt(rs.getParameter(getInputName("width-" + oldName), "6")));
                if (!oldName.equals(field.getName())) {
                    delField(oldName);
                }
                String listValue = rs.getParameter(getInputName("list-" + oldName), null);
                if (listValue != null) {
                    field.setRawList(listValue);
                }
                String registeredListValue = rs.getParameter(getInputName("registered-list-" + oldName), null);
                if (registeredListValue != null) {
                    field.setRegisteredList(registeredListValue);
                }

                field.setOrder(Integer.parseInt(rs.getParameter(getInputName("order-" + oldName), "")));

                String up = getInputName("up-" + oldName);
                if (rs.getParameter(up, null) != null) {
                    field.setOrder(field.getOrder() - 15);
                }
                String down = getInputName("down-" + oldName);
                if (rs.getParameter(down, null) != null) {
                    field.setOrder(field.getOrder() + 15);
                }
                store(field);
            }
        }

        configureMailing(ctx, true);

        pos = 10;
        for (Field field : getFields(ctx)) {
            field.setOrder(pos);
            pos = pos + 10;
            store(field);
        }

        String resetFile = rs.getParameter("reset-file");
        if (!StringHelper.isEmpty(resetFile)) {
            File file = new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getStaticFolder(), GenericForm.DYNAMIC_FORM_RESULT_FOLDER, resetFile));
            if (file.exists()) {
                File newFile = new File(file.getAbsoluteFile() + "_" + StringHelper.renderFileTime(new Date()));
                MessageRepository messageRepository = MessageRepository.getInstance(ctx);
                if (file.renameTo(newFile)) {
                    messageRepository.setGlobalMessage(new GenericMessage("file reset, backup file created : " + newFile, GenericMessage.INFO));
                } else {
                    messageRepository.setGlobalMessage(new GenericMessage("error on reset, backup file not created : " + newFile, GenericMessage.ERROR));
                }
                ctx.setClosePopup(true);
            }
        }

        if (rs.getParameter(getInputName("new-name"), "").trim().length() > 0) {
            String fieldName = StringHelper.createFileName(rs.getParameter(getInputName("new-name"), null));
            if (rs.getParameter(getInputName("add-first")) != null) {
                int order = 20;
                for (Field field : getFields(ctx)) {
                    field.setOrder(order);
                    store(field);
                    order += 10;
                }
                store(new Field(ctx, fieldName, "", "text", "", "", "text", "", "", 10, 6, ""));
            } else {
                store(new Field(ctx, fieldName, "", "text", "", "", "text", "", "", pos + 10, 6, ""));
            }
            ctx.getRequest().setAttribute(getNewFieldKey(), fieldName);
        }

        try {
            importFieldAsExcel(ctx);
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }

        store(ctx);
        createOrUpdateTable(ctx);
        countCache = null;
        return null;
    }

    protected String getMailHeader(ContentContext ctx) {
        return "";
    }

    protected String getMailFooter(ContentContext ctx) {
        return "";
    }

    protected boolean isHTMLMail() {
        return false;
    }

    protected boolean isStorage() {
        return true;
    }

    public String getTableSqlName() {
        String dbprefix = getLocalConfig(false).getProperty("dbprefix", null);
        if (!StringHelper.isEmpty(dbprefix)) {
            return dbprefix;
        } else {
            return null;
        }
    }

    public String getTableSqlNameReference() {
        String dbprefix = getLocalConfig(false).getProperty("dbprefix", null);
        if (!StringHelper.isEmpty(dbprefix)) {
            return dbprefix + "_ref";
        } else {
            return null;
        }
    }

    protected File getFile(ContentContext ctx) throws Exception {
        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
        String loc = ctx.getCurrentPage().getLocation(ctx);
        if (!StringHelper.isEmpty(loc)) {
            loc = "_" + StringHelper.createFileName(loc);
        } else {
            loc = "";
        }
        String fileName = StringHelper.createFileName(InfoBean.getCurrentInfoBean(ctx).getSection()) + "/df-" + StringHelper.createFileName(getPage().getTitle(ctx) + "_" + StringHelper.renderDate(getPage().getContentDateNeverNull(ctx))) + loc + ".csv";
        if (getLocalConfig(false).get("filename") != null && getLocalConfig(false).get("filename").toString().trim().length() > 0) {
            fileName = getLocalConfig(false).getProperty("filename");
        } else {
            getLocalConfig(false).setProperty("filename", fileName);
            store(ctx);
        }
        File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), GenericForm.DYNAMIC_FORM_RESULT_FOLDER, fileName));
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    private int lineNumber = -1;

    protected int storeResult(ContentContext ctx, Map<String, String> data, boolean sort) throws Exception {
        synchronized (LOCK_ACCESS_FILE) {
            File file = getFile(ctx);
            if (lineNumber < 0) {
                if (!file.exists()) {
                    lineNumber = -1;
                } else {
                    lineNumber = ResourceHelper.countLines(file) - 2;
                    if (lineNumber < 0) {
                        lineNumber = 0;
                    }
                }
            }
            Collection<String> titles = CSVFactory.loadTitle(file);
            boolean newTitleFound = false;
            for (String newTitle : data.keySet()) {
                if (!titles.contains(newTitle)) {
                    newTitleFound = true;
                }
            }
            RequestService rs = RequestService.getInstance(ctx.getRequest());
            int editLineNumber = 0;
            if (StringHelper.isDigit(rs.getParameter("line")) && ctx.getCurrentUserId() != null) {
                int lineNumber = decodeUserEditNumber(ctx, rs.getParameter("line"));
                if (ctx.getCurrentUserId().equals(data.get(USER_FIELD))) {
                    editLineNumber = lineNumber;
                } else {
                    throw new SecurityException("try to edit bad line data.");
                }
            } else {
                editLineNumber = decodeEditNumber(ctx, rs.getParameter(getInputEditLineName(ctx)));
            }
            if (editLineNumber > 0) {
                lineNumber = editLineNumber;
                File csvFile = getFile(ctx);
                List<Map<String, String>> allData = CSVFactory.loadContentAsMap(csvFile);
                if (allData.size() >= editLineNumber) {
                    Map<String, String> oldData = allData.get(editLineNumber);
                    for (Map.Entry<String, String> entry : data.entrySet()) {
                        Field field = getField(ctx, entry.getKey());
                        if (field != null && field.getType().equals("file") && StringHelper.isEmpty(entry.getValue())) {
                            data.put(entry.getKey(), oldData.get(entry.getKey()));
                        }
                    }
                    allData.set(editLineNumber, data);
                }
                CSVFactory.storeContentAsMap(getFile(ctx), allData, sort);
            } else {
                if (newTitleFound) {
                    List<Map<String, String>> newData = CSVFactory.loadContentAsMap(file);
                    newData.add(data);
                    CSVFactory.storeContentAsMap(file, newData, sort);
                } else {
                    CSVFactory.appendContentAsMap(file, data, sort);
                }
                lineNumber++;
            }
        }
        return lineNumber;
    }

    protected boolean isSendEmail() {
        return true;
    }

    protected InternetAddress getConfirmToEmail(ContentContext ctx) {
        String emailConformField = getLocalConfig(false).getProperty("mail.confirm.field", null);
        if (emailConformField == null) {
            for (Field field : getFields(ctx)) {
                if (field.getType().equals("email") && emailConformField == null) {
                    emailConformField = field.getName();
                }
            }
        }
        RequestService requestService = RequestService.getInstance(ctx.getRequest());
        if (emailConformField != null && requestService.getParameter(emailConformField, "") != null) {
            String tmpEmail = requestService.getParameter(emailConformField, "");
            try {
                return new InternetAddress(tmpEmail);
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        }
        return null;
    }

    /**
     * check if form is expired (too much time for submit)
     *
     * @return
     */
    protected boolean isFormExpire() {
        return true;
    }

    private boolean isDb(ContentContext ctx) {
        if (StringHelper.isEmpty(getTableSqlNameReference())) {
            return false;
        } else {
            return !DataBaseService.getInstance(ctx.getGlobalContext()).isInternalDb();
        }
    }

    private boolean createOrUpdateTable(ContentContext ctx) throws Exception {
        String tableName = DataBaseService.getDefaultDbName(ctx);
        if (!isDb(ctx)) {
            return false;
        }
        boolean out = false;
        DataBaseService dbService = DataBaseService.getInstance(ctx.getGlobalContext());
        Connection conn = dbService.getConnection(tableName);
        Statement st = conn.createStatement();
        try {
            /** main table **/
            try {
                String sql = "CREATE TABLE " + getTableSqlName() + " (id SERIAL)";
                st.executeUpdate(sql);
                logger.info("SQL : create table : " + getTableSqlName());
                out = true;
            } catch (SQLException e) {
                // e.printStackTrace();
            }
            List<Field> fields = getFields(ctx);
            for (Field field : fields) {
                try {
                    String sql = "ALTER TABLE " + getTableSqlName() + " ADD " + field.getName() + " " + field.getSqlType();
                    st.executeUpdate(sql);
                    logger.info("SQL : create field : " + field.getName() + " [" + field.getSqlType() + "]");
                    out = true;
                } catch (SQLException e) {
                    // e.printStackTrace();
                }
            }
            /** ref table **/
            try {
                String sql = "CREATE TABLE " + getTableSqlNameReference() + " (id SERIAL, name text, label text, lang text)";
                st.executeUpdate(sql);
                logger.info("SQL : create table : " + getTableSqlName());
                out = true;
            } catch (SQLException e) {
                // e.printStackTrace();
            }
            st.executeUpdate("DELETE FROM " + getTableSqlNameReference() + " WHERE lang='" + ctx.getRequestContentLanguage() + "'");
            for (Field field : fields) {
                try {
                    String sql = "INSERT INTO " + getTableSqlNameReference() + " (name, label, lang) VALUES (?,?,?)";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setString(1, field.getName());
                    ps.setString(2, field.getLabel());
                    ps.setString(3, ctx.getRequestContentLanguage());
                    ps.execute();
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            dbService.releaseConnection(st, conn);
        }
        return out;
    }

    public static String performSubmit(HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println("#################### performSubmit 1");

        String userIP = request.getHeader("x-real-ip");
        if (StringHelper.isEmpty(userIP)) {
            userIP = request.getRemoteAddr();
        }

        Object value = null;
        String finalValue = null;

        RequestService rs = RequestService.getInstance(request);
        ContentContext ctx = ContentContext.getContentContext(request, response);
        ContentService content = ContentService.getInstance(request);
        SmartGenericForm comp = (SmartGenericForm) content.getComponent(ctx, rs.getParameter("comp_id", null));

        String fakeField = comp.getLocalConfig(false).getProperty("field.fake", "fake");
        boolean withXHTML = StringHelper.isTrue(comp.getLocalConfig(false).getProperty("field.xhtml", null));
        boolean fakeFilled = false;

        Map<String, Object> params = rs.getParameterMap();
        Map<String, String> result = new LinkedHashMap<String, String>();
        List<String> errorFields = new LinkedList<String>();


        Map<String, String> adminMailData = new LinkedHashMap<String, String>();
        Map<String, String> userMailData = new LinkedHashMap<String, String>();
        String errorFieldList = " (";
        String errorFieldSep = "";
        Collection<String> errorKeyFound = new HashSet<String>();
        boolean badFormatFound = false;
        boolean update = comp.isUpdate(ctx);

        GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

        String subject = "GenericForm submit : '" + globalContext.getGlobalTitle() + "' ";
        String subjectField = comp.getLocalConfig(false).getProperty("mail.subject.field", null);

        boolean eventClose = comp.isClose(ctx);
        String loc = ctx.getCurrentPage().getLocation(ctx);

        Map<String, String> dataDoc = new HashMap<>();
        Map<String, String> dataMap = null;

        String registrationID = StringHelper.getShortRandomId();
        String code = rs.getParameter("_form-code", "");

        synchronized (LOCK_SUBMIT) {

            System.out.println("#################### performSubmit 2");

            if (comp.isFormExpire() && !comp.cacheForm.containsKey(code) && !comp.isCaptcha(ctx)) {
                I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
                GenericMessage msg = new GenericMessage(i18nAccess.getViewText("error.bad-from-version", "This form has experied, try again."), GenericMessage.ERROR);
                request.setAttribute("msg", msg);
                return "This form has expired, try again. Please ensure to complete the form within 2 hours.";
            }

            System.out.println("#################### performSubmit 3");

            /** check captcha **/
            String captcha = rs.getParameter("captcha", null);

            if (comp.isCaptcha(ctx)) {
                if (StringHelper.isOneEmpty(comp.getRecaptchaKey(), comp.getRecaptchaSecretKey())) {
                    if (captcha == null || CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode() == null || !CaptchaService.getInstance(request.getSession()).getCurrentCaptchaCode().equals(captcha)) {
                        GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
                        request.setAttribute("msg", msg);
                        request.setAttribute("error_captcha", "true");
                        CaptchaService.getInstance(request.getSession()).reset();
                        return null;
                    } else {
                        CaptchaService.getInstance(request.getSession()).reset();
                    }
                } else {
//					Map<String, String> paramsLocal = new HashMap<String, String>();
//					paramsLocal.put("secret", comp.getRecaptchaKey());
//					paramsLocal.put("response", rs.getParameter("g-recaptcha-response", ""));
//					paramsLocal.put("remoteip", request.getRemoteAddr());
                    String url = URLHelper.addAllParams("https://www.google.com/recaptcha/api/siteverify", "secret=" + comp.getRecaptchaSecretKey(), "response=" + rs.getParameter("g-recaptcha-response", ""), "remoteip=" + userIP);
                    String captchaResponse = NetHelper.readPage(new URL(url));
                    JSONMap map = JSONMap.parseMap(captchaResponse);
                    if (map == null || !StringHelper.isTrue(map.get("success"))) {
                        GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.captcha", "bad captcha."), GenericMessage.ERROR);
                        request.setAttribute("msg", msg);
                        request.setAttribute("error_captcha", "true");
                        return null;
                    }
                }
            }

            System.out.println("#################### performSubmit 4");

            if (subjectField != null && rs.getParameter(subjectField, null) != null) {
                subject = comp.getLocalConfig(false).getProperty("mail.subject", "") + rs.getParameter(subjectField, null);
            } else {
                subject = comp.getLocalConfig(false).getProperty("mail.subject", subject);
            }
            InfoBean.getCurrentInfoBean(ctx); // create info bean if not exist
            subject = XHTMLHelper.replaceJSTLData(ctx, subject);
            result.put("__registration time", StringHelper.renderSortableTime(new Date()));
            result.put("__local addr", request.getLocalAddr());
            result.put("__remote addr", request.getRemoteAddr());
            result.put("__X-Forwarded-For", request.getHeader("x-forwarded-for"));
            result.put("__X-Real-IP", request.getHeader("x-real-ip"));
            result.put("__referer", StringHelper.removeTag(request.getHeader("referer")));
            result.put("__agent", request.getHeader("User-Agent"));
            result.put("_registrationID", registrationID);
            result.put("_event-close", "" + comp.isClose(ctx));
            result.put("_compid", "" + comp.getId());

            result.put(USER_FIELD, StringHelper.neverNull(ctx.getCurrentUserId(), ""));
            result.put(VALIDED, "false");

            List<String> keys = new LinkedList<String>(params.keySet());
            Collections.sort(keys, new StringComparator());

            /** store attach files **/

            Map<String, String> specialValues = new HashMap<String, String>();

            String badFileFormatRAW = comp.getLocalConfig(false).getProperty("file.bad-file", "exe,bat,scr,bin,obj,lib,dll,bat,sh,com,cmd,msi,jsp,xml,html,htm,vbe,wsf,wsc,asp,php");
            List<String> badFileFormat = StringHelper.stringToCollection(badFileFormatRAW, ",");
            long maxFileSize = comp.getMaxFileSize();

            for (FileItem file : rs.getAllFileItem()) {
                String ext = StringHelper.getFileExtension(file.getName()).toLowerCase();
                if (badFileFormat.contains(ext)) {
                    logger.warning("file blocked because bad extention : " + file.getName());
                    GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.bad-file", "bad file format."), GenericMessage.ERROR);
                    request.setAttribute("msg", msg);
                    return null;
                }
                InputStream in = file.getInputStream();
                if (in != null) {
                    try {
                        if (file.getName().trim().length() > 0) {
                            String fileName = URLHelper.mergePath(comp.getAttachFolder(ctx).getAbsolutePath(), StringHelper.createFileName(file.getName()));
                            File freeFile = ResourceHelper.getFreeFileName(new File(fileName));
                            if (ResourceHelper.writeStreamToFile(in, freeFile, maxFileSize) < 0) {
                                GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.tobig-file", "file to big."), GenericMessage.ERROR);
                                request.setAttribute("msg", msg);
                                return null;
                            }
                            StaticInfo staticInfo = StaticInfo.getInstance(ctx, freeFile);
                            String fileURL = URLHelper.createResourceURL(ctx.getContextForAbsoluteURL(), URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), staticInfo.getStaticURL()));
                            result.put(file.getFieldName(), fileURL);
                            specialValues.put(file.getFieldName(), fileURL);
                        }
                    } finally {
                        ResourceHelper.closeResource(in);
                    }
                }
            }

            System.out.println("#################### performSubmit 5");

            if (!StringHelper.isEmpty(loc)) {
                adminMailData.put("location", loc);
            }


            if (comp.isFilledFromCookies()) {
                dataMap = new HashMap<>();
            }

            String sql = null;
            PreparedStatement ps = null;
            Connection conn = null;
            if (comp.isDb(ctx)) {
                sql = "INSERT INTO " + comp.getTableSqlName() + "(";
                String values = "(";
                String sep = "";
                for (Field field : comp.getFields(ctx)) {
                    sql += sep + field.getName();
                    values += sep + "?";
                    sep = ",";
                }
                sql += ") VALUES " + values + ")";
                conn = DataBaseService.getInstance(ctx.getGlobalContext()).getConnection(DataBaseService.getDefaultDbName(ctx));
                ps = conn.prepareStatement(sql);
            }

            int statementIndex = 1;
            for (Field field : comp.getFields(ctx)) {

                String key = field.getName();
                value = params.get(key);

                System.out.print("## key = "+key+ "  =  "+ value);

                if (dataMap != null) {
                    dataMap.put(key, "" + value);
                }

                if (value != null && value.toString().contains("//") && !comp.acceptLinks(ctx)) {
                    logger.severe("no link in the content !");
                    return I18nAccess.getInstance(ctx).getViewText("global.error.content.no-link");
                }

                if (specialValues.get(key) != null) {
                    value = specialValues.get(key);
                }
                finalValue = rs.getParameter(key, "");
                if (specialValues.get(key) != null) {
                    finalValue = specialValues.get(key);
                }

                if (rs.getParameterListValues(key) != null && rs.getParameterListValues(key).size() > 1) {
                    finalValue = StringHelper.collectionToString(rs.getParameterListValues(key), ",");
                }

                if (key.equals(fakeField) && finalValue.trim().length() > 0) {
                    fakeFilled = true;
                } else if (!withXHTML && (finalValue.toLowerCase().contains("</a>") || finalValue.toLowerCase().contains("</div>"))) {
                    fakeFilled = true;
                }

                if (field.getType().equals("hidden")) {
                    dataDoc.put(StringHelper.firstLetterLower(key), field.getLabel());
                } else {
                    dataDoc.put(StringHelper.firstLetterLower(key), "" + value);
                }

                if (ps != null) {
                    ps.setObject(statementIndex, value);
                    statementIndex++;
                }

                if (!update || !field.getType().equals("file")) {
                    // if (!field.isFilledWidth(finalValue) &&
                    // StringHelper.containsUppercase(key.substring(0, 1))) {
                    if (!field.isFilledWidth(finalValue, !field.getType().equals("list")) && StringHelper.containsUppercase(key.substring(0, 1)) && StringHelper.isEmpty(field.getCondition())) {
                        errorKeyFound.add(key);
                        errorFields.add(key);
                        errorFieldList = errorFieldList + errorFieldSep + field.getLabel() + (field.getFormatLabel() != null ? " - " + field.getFormatLabel() : "");
                        errorFieldSep = ",";
                        if (badFormatFound) {
                            GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.generic", "please check all fields.") + errorFieldList + ')', GenericMessage.ERROR);
                            request.setAttribute("msg", msg);
                        } else {
                            GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.required", "please could you fill all required fields.") + errorFieldList + ')', GenericMessage.ERROR);
                            request.setAttribute("msg", msg);
                        }
                    }
                }
                if (!StringHelper.isEmpty(finalValue)) {
                    if (field.getType().equals(Field.TYPE_VAT) && !StringHelper.isEmpty(finalValue)) {
                        Boolean errorVAT = false;
                        Company company = NetHelper.validVATEuroparlEU(ctx, finalValue);
                        if (ctx.getGlobalContext().getStaticConfig().isInternetAccess()) {
                            if (company == null) {
                                errorVAT = true;
                            } else {
                                for (Field f : comp.getFields(ctx)) {
                                    if (f.getName().equalsIgnoreCase("adresse") || f.getName().equalsIgnoreCase("address") && StringHelper.isEmpty(f.getValue())) {
                                        f.setValue(company.getAddress());
                                    }
                                }
                            }
                        } else {
                            errorVAT = StringHelper.isVAT(field.getValue());
                        }
                        if (errorVAT) {
                            errorKeyFound.add(key);
                            errorFields.add(key);
                            errorFieldList = errorFieldList + errorFieldSep + field.getLabel();
                            errorFieldSep = ",";
                            I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
                            GenericMessage msg = new GenericMessage(i18nAccess.getViewText("error.vat", "VAT number not idenfied. (sample:BE0824.985.592)") + ')', GenericMessage.ERROR);
                            request.setAttribute("msg", msg);
                            badFormatFound = true;
                        }
                    } else if (!field.isValueValid(finalValue) && !errorKeyFound.contains(key)) {
                        errorKeyFound.add(key);
                        errorFields.add(key);
                        errorFieldList = errorFieldList + errorFieldSep + field.getLabel() + (field.getFormatLabel() != null ? " - " + field.getFormatLabel() : "");
                        errorFieldSep = ",";
                        GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("error.generic", "please check all fields.") + errorFieldList + ')', GenericMessage.ERROR);
                        request.setAttribute("msg", msg);
                        badFormatFound = true;
                    }
                }

                // if (!StringHelper.isEmpty(field.getRegisteredList())) {
                // List<IListItem> list = ListService.getInstance(ctx).getList(ctx,
                // field.getRegisteredList());
                // if (list != null) {
                // for (IListItem item : list) {
                // if (item.getKey().equals(finalValue)) {
                // finalValue = finalValue+" ("+item.getValue()+')';
                // }
                // }
                // }
                // }

                if (ctx.getCurrentUser() != null) {
                    adminMailData.put("user", ctx.getCurrentUser().getLogin());
                }
                String pageLink = URLHelper.createVirtualURL(ctx.getContextForAbsoluteURL());
                adminMailData.put("title", "<a href=\"" + pageLink + "\">" + ctx.getCurrentPage().getPageTitle(ctx) + "</a>");

                if (value instanceof Object[]) {
                    finalValue = StringHelper.arrayToString((Object[]) params.get(key), ",");
                    adminMailData.put(field.getLabel() + " (" + key + ") ", finalValue);
                    userMailData.put(field.getLabel() + MailService.HIDDEN_DIV + key + "</div>", finalValue);
                } else {
                    adminMailData.put(field.getLabel() + " (" + key + ") ", finalValue);
                    userMailData.put(field.getLabel() + MailService.HIDDEN_DIV + key + "</div>", finalValue);
                }

                if (comp.isStoreLabel()) {
                    result.put(field.getLabel(), finalValue);
                } else {
                    result.put(key, finalValue);
                }

            }

            System.out.println("#################### performSubmit 6");

            if (ps != null) {
                ps.execute();
                DataBaseService.getInstance(ctx.getGlobalContext()).releaseConnection(conn);
            }

            if (comp.isFilledFromCookies()) {
                UserDataService userDataService = UserDataService.getInstance(ctx);
                userDataService.addUserData(ctx, comp.getDataKey(), StringHelper.mapToString(dataMap));
            }

        } // Syncronized

        if (fakeFilled) {
            logger.warning("spam detected fake field filled : " + comp.getPage().getPath());
        }

        if (errorFields.size() == 0) {
            logger.info(adminMailData.toString());
            int lineNumber = -1;
            if (comp.isStorage()) {
                lineNumber = comp.storeResult(ctx, result, !comp.isStoreLabel());
            }
            if (comp.isSendEmail() && !fakeFilled) {
                String emailFrom = comp.getLocalConfig(false).getProperty("mail.from", StaticConfig.getInstance(request.getSession()).getSiteEmail());
                emailFrom = MailService.getDefaultSenderEmail(ctx, MailService.getDefaultSenderEmail(ctx, emailFrom));

                String emailFromField = comp.getLocalConfig(false).getProperty("mail.from.field", null);
                if (emailFromField != null && rs.getParameter(emailFromField, "") != null) {
                    String tmpEmail = rs.getParameter(emailFromField, "");
                    try {
                        new InternetAddress(tmpEmail);
                        emailFrom = tmpEmail;
                    } catch (Exception e) {
                        logger.warning(e.getMessage());
                    }
                }
                String emailTo = comp.getLocalConfig(false).getProperty("mail.to", null);
                if (!StringHelper.isMail(emailTo)) {
                    emailTo = MailService.getDefaultReceiverEmail(ctx, MailService.getDefaultSenderEmail(ctx, emailFrom));
                }
                String emailCC = comp.getLocalConfig(false).getProperty("mail.cc", null);
                String emailBCC = comp.getLocalConfig(false).getProperty("mail.bcc", null);
                MailService mailService = null;

                Map<String, String> paramsEdit = new HashMap<String, String>();
                paramsEdit.put(comp.getInputEditLineName(ctx), comp.encodeEditNumber(ctx, lineNumber));
                String editURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), paramsEdit);
                paramsEdit = new HashMap<String, String>();
                paramsEdit.put(comp.getInputValidLineName(ctx), comp.encodeEditNumber(ctx, lineNumber));
                String validURL = URLHelper.createURL(ctx.getContextForAbsoluteURL(), ctx.getPath(), paramsEdit);
                try {
                    mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
                    InternetAddress fromEmail = new InternetAddress(emailFrom);
                    InternetAddress toEmail = new InternetAddress(emailTo);
                    InternetAddress ccEmail = null;
                    if (emailCC != null && emailCC.trim().length() > 0) {
                        ccEmail = new InternetAddress(emailCC);
                    }
                    InternetAddress bccEmail = null;
                    if (emailBCC != null && emailBCC.trim().length() > 0) {
                        bccEmail = new InternetAddress(emailBCC);
                    }

                    List<InternetAddress> ccList = null;
                    if (ccEmail != null) {
                        ccList = Arrays.asList(ccEmail);
                    }
                    List<InternetAddress> bccList = null;
                    if (bccEmail != null) {
                        bccList = Arrays.asList(bccEmail);
                    }

                    comp.countCache = null;
                    ContentContext absCtx = ctx.getContextForAbsoluteURL();

                    String mailAdminContent;
                    String prefix = "Event registration :";
                    if (comp.isFullEventSite(ctx)) {
                        prefix = "Waiting list : ";
                        subject = prefix + subject;
                    }

                    if (!StringHelper.isEmpty(comp.getLocalConfig(false).getProperty("event.limit"))) {
                        mailAdminContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), prefix + comp.getCountSubscription(ctx) + "/" + comp.getLocalConfig(false).getProperty("event.limit"), adminMailData, editURL, "edit >>", null);
                    } else {
                        mailAdminContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Form submit - " + comp.getCountSubscription(ctx), adminMailData, editURL, "edit >>", null);
                    }
                    mailService.sendMail(null, fromEmail, toEmail, ccList, bccList, subject, mailAdminContent, true, null, globalContext.getDKIMBean());

                    if (comp.isWarningEventSite(ctx)) {
                        subject = globalContext.getContextKey() + " - WARNING Event almost full : " + ctx.getCurrentPage().getTitle(ctx) + " [" + StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx)) + ']';
                        Map data = new HashMap();
                        String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
                        int countSubscription = comp.getCountSubscription(ctx);
                        data.put("event", ctx.getCurrentPage().getTitle(ctx));
                        data.put("subscription", countSubscription);
                        data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
                        data.put("alert limit", eventLimistStr);
                        if (loc != null) {
                            data.put("location", loc);
                        }
                        absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
                        String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it will be automaticly closed soon.", data, editURL, "edit >>", null);

                        EMail email = new EMail();
                        email.setSender(new InternetAddress(globalContext.getAdministratorEmail()));
                        email.addRecipients(toEmail);
                        email.setCcRecipients(ccList);
                        email.setBccRecipients(bccList);
                        email.setSubject(subject);
                        email.setContent(adminMailContent);
                        email.setHtml(true);
                        email.setDkim(globalContext.getDKIMBean());

                        mailService.sendMail(null, email);

                        // mailService.sendMail(null, new
                        // InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList,
                        // bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
                    }

                    if (comp.isClosedEventSite(ctx)) {
                        subject = globalContext.getContextKey() + " - WARNING Event full : " + ctx.getCurrentPage().getTitle(ctx) + " [" + StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx)) + ']';
                        Map data = new HashMap();
                        String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
                        int countSubscription = comp.getCountSubscription(ctx);
                        data.put("event", ctx.getCurrentPage().getTitle(ctx));
                        data.put("subscription", countSubscription);
                        data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
                        data.put("alert limit", eventLimistStr);
                        if (loc != null) {
                            data.put("location", loc);
                        }
                        absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
                        String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it has been closed.", data, editURL, "edit >>", null);
                        mailService.sendMail(null, new InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList, bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
                    }

                    if (eventClose) {
                        subject = globalContext.getContextKey() + " - WARNING registration on full event : " + ctx.getCurrentPage().getTitle(ctx) + " [" + StringHelper.renderDate(comp.getPage().getContentDateNeverNull(absCtx)) + ']';
                        Map data = new HashMap();
                        String eventLimistStr = comp.getLocalConfig(false).getProperty("event.alert-limit");
                        int countSubscription = comp.getCountSubscription(ctx);
                        data.put("event", ctx.getCurrentPage().getTitle(ctx));
                        data.put("subscription", countSubscription);
                        data.put("limit", comp.getLocalConfig(false).getProperty("event.limit", ""));
                        data.put("alert limit", eventLimistStr);
                        if (loc != null) {
                            data.put("location", loc);
                        }
                        absCtx.setRenderMode(ContentContext.PREVIEW_MODE);
                        String adminMailContent = XHTMLHelper.createAdminMail(ctx.getCurrentPage().getTitle(ctx), "Please check you event, it is full.", data, editURL, "edit >>", null);
                        mailService.sendMail(null, new InternetAddress(globalContext.getAdministratorEmail()), toEmail, ccList, bccList, subject, adminMailContent, true, null, globalContext.getDKIMBean());
                    }

                    String mailPath = comp.getLocalConfig(false).getProperty("mail.confirm.link", null);
                    String mailSubject = comp.getLocalConfig(false).getProperty("mail.confirm.subject", null);

                    Attachment attachment = null;
                    if (!eventClose) {
                        String category = comp.getLocalConfig(false).getProperty("doc-category");
                        if (comp.isDocument() && !StringHelper.isEmpty(category) && !StringHelper.isEmpty(comp.getLocalConfig(false).getProperty("doc-link", null))) {
                            DataDocument doc = new DataDocument(category, dataDoc);
                            doc.resetToken();
                            DataDocumentService.getInstance(ctx.getGlobalContext()).createDocumentData(doc);
                            ContentContext pdfCtx = new ContentContext(ctx);
                            pdfCtx.setFormat("pdf");
                            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
                            String pdfURL = URLHelper.createDataDocumentURL(pdfCtx.getContextForAbsoluteURL(), doc, comp.getLocalConfig(false).getProperty("doc-link", null));
                            NetHelper.readPage(new URL(pdfURL), pdfStream);
                            pdfStream.flush();
                            attachment = new Attachment(category + '_' + doc.getId() + ".pdf", pdfStream.toByteArray());
                        }
                    } else {
                        mailPath = comp.getLocalConfig(false).getProperty("mail.closed.link", null);
                        mailSubject = comp.getLocalConfig(false).getProperty("mail.closed.subject", null);
                    }

                    ContentContext pageCtx = ctx.getContextForAbsoluteURL();
                    pageCtx.setRenderMode(ContentContext.PAGE_MODE);
                    if (!StringHelper.isEmpty(mailSubject)) {
                        mailSubject = XHTMLHelper.replaceJSTLData(pageCtx, mailSubject);
                        URL mailURL = new URL(URLHelper.createURL(pageCtx, mailPath));
                        logger.info("read mail from : " + mailURL);
                        String email = NetHelper.readPageForMailing(mailURL);
                        if (email != null && email.length() > 0) {
                            InternetAddress to = comp.getConfirmToEmail(ctx);
                            if (to != null) {
                                for (Field field : comp.getFields(ctx)) {
                                    email = email.replace("${field." + field.getName() + "}", rs.getParameter(field.getName(), ""));
                                    email = email.replace("${field." + field.getName().toLowerCase() + "}", rs.getParameter(field.getName(), ""));
                                }
                                email = email.replace("${registrationID}", registrationID);
                                email = email.replace("${communication}", StringHelper.encodeAsStructuredCommunicationMod97(registrationID));
                                email = email.replace("${htmlFields}", XHTMLHelper.createHTMLTable(userMailData));

                                email = email.replace("${url.edit}", editURL);
                                email = email.replace("${url.valid}", validURL);
                                email = email.replace("${url.page}", URLHelper.createURL(ctx.getContextForAbsoluteURL()));
                                email = email.replace("${url.root}", URLHelper.createURL(ctx.getContextForAbsoluteURL(), "/"));

                                email = email.replace("${event.title}", comp.getPage().getTitle(pageCtx));
                                email = email.replace("${event.location}", comp.getPage().getLocation(pageCtx));
                                email = email.replace("${event.description}", XHTMLHelper.textToXHTML(comp.getPage().getDescriptionAsText(pageCtx), globalContext));
                                Event event = comp.getPage().getEvent(pageCtx);
                                if (event != null) {
                                    email = email.replace("${event.start}", StringHelper.renderDate(event.getStart()));
                                    email = email.replace("${event.end}", StringHelper.renderDate(event.getEnd()));
                                }

                                InternetAddress registrationFrom = new InternetAddress(comp.getLocalConfig(false).getProperty("mail.from", StaticConfig.getInstance(request.getSession()).getSiteEmail()));

                                EMail userMail = new EMail();
                                userMail.setSender(registrationFrom);
                                userMail.addRecipient(to);
                                userMail.addBccRecipient(bccEmail);
                                userMail.setSubject(mailSubject);
                                userMail.setContent(email);
                                userMail.setHtml(true);
                                userMail.addAttachment(attachment);
                                userMail.setDkim(globalContext.getDKIMBean());

                                mailService.sendMail(null, userMail);

                                // NetHelper.sendMail(ctx.getGlobalContext(), registrationFrom, to, null,
                                // bccEmail, mailSubject, email, null, true);
                            } else {
                                return "warning : no recipient found.";
                            }
                        }
                    }
                } catch (Exception e) {
                    if (mailService != null && mailService.getMailConfig() != null) {
                        System.out.println("SMTP host  = " + mailService.getMailConfig().getSMTPHost());
                        System.out.println("SMTP port  = " + mailService.getMailConfig().getSMTPPort());
                        System.out.println("SMTP login = " + mailService.getMailConfig().getLogin());
                        System.out.println("SMTP pwd?  = " + !StringHelper.isEmpty(mailService.getMailConfig().getPassword()));
                    }
                    String errorID = "E" + StringHelper.getRandomId();
                    logger.severe("error id:" + errorID + " = " + e.getMessage());
                    e.printStackTrace();
                    request.setAttribute("valid", "false");
                    return comp.getLocalConfig(false).getProperty("message.error", "technical error.") + " (" + errorID + ')';
                }
            }

            GenericMessage msg = new GenericMessage(comp.getLocalConfig(false).getProperty("message.thanks"), GenericMessage.INFO);
            request.setAttribute("msg", msg);
            request.setAttribute("valid", "true");
            comp.cacheForm.remove(code);
        } else {
            request.setAttribute("errorFields", new CollectionAsMap<String>(errorFields));
        }

        MenuElement nextPage = comp.getNextPage(ctx);
        if (nextPage != null && errorFields.size() == 0) {
            ctx.setPath(nextPage.getPath());
        }

		comp.configureMailing(ctx, false);

        return null;
    }

    protected MenuElement getNextPage(ContentContext ctx) {
        return null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getActionGroupName() {
        return "smart-form";
    }

    @Override
    public boolean isRealContent(ContentContext ctx) {
        return true;
    }

    @Override
    public boolean isContentCachable(ContentContext ctx) {
        return false;
    }

    @Override
    public boolean isContentCachableByQuery(ContentContext ctx) {
        return false;
    }

    @Override
    public boolean initContent(ContentContext ctx) throws Exception {
        boolean outB = super.initContent(ctx);
        getLocalConfig(false).setProperty("title", getType());

        I18nAccess i18nAccess = I18nAccess.getInstance(ctx);

        getLocalConfig(false).setProperty("mail.subject", i18nAccess.getViewText("form.mail.subject", "Contact : ") + ctx.getGlobalContext().getGlobalTitle());
        getLocalConfig(false).setProperty("mail.from", ctx.getGlobalContext().getAdministratorEmail());
        getLocalConfig(false).setProperty("error.generic", i18nAccess.getViewText("form.error", ""));
        getLocalConfig(false).setProperty("message.required", "* " + i18nAccess.getViewText("form.required", "required"));

        getLocalConfig(false).setProperty("error.required", i18nAccess.getViewText("form.error.required", ""));
        getLocalConfig(false).setProperty("message.thanks", i18nAccess.getViewText("form.thanks", ""));
        getLocalConfig(false).setProperty("message.error", i18nAccess.getViewText("contact.technical-error", "reset	"));
        getLocalConfig(false).setProperty("message.reset", i18nAccess.getViewText("form.reset", "reset	"));

        getLocalConfig(false).setProperty("label.captcha", i18nAccess.getViewText("global.captcha", ""));
        getLocalConfig(false).setProperty("error.captcha", i18nAccess.getViewText("message.error.bad-captcha", ""));

        store(ctx);
        return outB;
    }

    @Override
    public int getComplexityLevel(ContentContext ctx) {
        return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
    }

    @Override
    public List<IUserInfo> getParticipants(ContentContext ctx) throws Exception {
        List<Map<String, String>> data = getData(ctx);
        if (data.size() == 0) {
            return Collections.EMPTY_LIST;
        } else {
            IUserFactory userFactory = UserFactory.createUserFactory(ctx.getGlobalContext(), ctx.getRequest().getSession());
            List<IUserInfo> users = new LinkedList<IUserInfo>();
            for (Map<String, String> line : data) {
                IUserInfo userInfo = userFactory.createUserInfos();
                for (Object key : BeanHelper.beanSetList(userInfo)) {
                    String value = line.get(key.toString());
                    if (value == null) {
                        value = line.get(StringUtils.capitalize(key.toString()));
                    }
                    if (value != null) {
                        BeanHelper.setProperty(userInfo, key.toString(), value);
                    }
                }
                if (StringHelper.isEmpty(userInfo.getLogin())) {
                    userInfo.setLogin(userInfo.getEmail());
                    if (StringHelper.isEmpty(userInfo.getLogin())) {
                        userInfo.setLogin("???");
                    }
                }
                users.add(userInfo);
            }
            return users;
        }
    }

    @Override
    public String getUserLink(ContentContext ctx) throws Exception {
        return null;
    }

    @Override
    public String getFontAwesome() {
        return "address-card";
    }

    @Override
    public List<Map<String, String>> getData(ContentContext ctx, String login) throws Exception {
        if (StringHelper.isEmpty(login)) {
            return Collections.EMPTY_LIST;
        }
        List<Map<String, String>> outData = new LinkedList<Map<String, String>>();
        List<Map<String, String>> data = getData(ctx);
        for (Map<String, String> line : data) {
            if (login.equals(line.get(USER_FIELD))) {
                outData.add(line);
            }
        }
        return outData;
    }

    public static String performDownloadForm(ContentContext ctx, RequestService rs) throws Exception {
        SmartGenericForm comp = (SmartGenericForm) ComponentHelper.getComponentFromRequest(ctx);
        if (comp == null) {
            logger.severe("component id not found in the URL.");
            return "component id not found in the URL.";
        }
        Cell[][] cells = XLSTools.createArray(10, comp.getFields(ctx).size() + 1);
        int y = 0;
        cells[0][y] = new Cell("name", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("label", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("type", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("condition", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("list", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("registered list", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("order", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("width", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("required", null, cells, 0, y);
        y++;
        cells[0][y] = new Cell("autocomplete", null, cells, 0, y);
        y++;

        int x = 1;
        for (Field field : comp.getFields(ctx)) {
            y = 0;
            cells[x][y] = new Cell(field.getName(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(field.getLabel(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(field.getType(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(field.getCondition(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(StringHelper.collectionToString(field.getList(), ","), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(field.getRegisteredList(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell("" + field.getOrder(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell("" + field.getWidth(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell("" + field.isRequire(), null, cells, x, y);
            y++;
            cells[x][y] = new Cell(field.getAutocomplete(), null, cells, x, y);
            y++;
            x++;
        }
        XLSTools.writeXLSX(cells, ctx.getResponse().getOutputStream());
        return null;
    }

    @Override
    protected boolean getColumnableDefaultValue() {
        return true;
    }

    @Override
    public boolean transflateFrom(ContentContext ctx, ITranslator translator, String lang) {
        for (Field field : getFields(ctx)) {
            String label = field.getLabel();
            String newLabel = translator.translate(ctx, label,  lang, ctx.getRequestContentLanguage());
            field.setLabel(newLabel);
            store(field);
        }
        try {
            store(ctx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    protected boolean isValueTranslatable() {
        return true;
    }
}
