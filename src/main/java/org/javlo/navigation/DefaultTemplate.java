package org.javlo.navigation;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper.BadXMLException;
import org.javlo.i18n.I18nAccess;
import org.javlo.rendering.Device;
import org.javlo.template.Template;


public class DefaultTemplate extends Template {

	public static final String NAME = "__adam";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = java.util.logging.Logger.getLogger(DefaultTemplate.class.getName());

	private boolean mailing = false;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getOwner() {
		return "";
	}

	@Override
	public void reload() {
	}

	public String getHTMLFile() {
		return "index.html";
	}

	@Override
	public String getHTMLHomeFile() {
		return null;
	}

	@Override
	public String getLicenceFile() {
		return "";
	}

	public String getHomeRenderer() {
		return "home.jsp";
	}

	@Override
	public String getLocalWorkTemplateFolder() {
		return "/work_template";
	}

	@Override
	public String getWorkTemplateFolder() {
		return config.getRealPath(getLocalWorkTemplateFolder());
	}

	@Override
	public String getLocalWorkMailingTemplateFolder() {
		return "/work_mailing_template";
	}

	@Override
	protected String getRendererFile(Device device) {
		return "index.jsp";
	}

	@Override
	public String getHTMLFile(Device device) {
		return "index.html";
	}

	@Override
	public boolean isTemplateInWebapp(ContentContext ctx) throws IOException {
		/*GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		File templateTgt = new File(URLHelper.mergePath(getWorkTemplateFolder(), getFolder(globalContext)));
		if (isMailing()) {
			templateTgt = new File(URLHelper.mergePath(getWorkMailingTemplateFolder(), getFolder(globalContext)));
		}
		return templateTgt.exists();*/
		return true;
	}

	public String getRendererFile() {
		return "index.jsp";
	}

	public String getRenderer() throws IOException, BadXMLException {
		return "index.jsp";
	}

	public boolean isRenderer() {
		return true;
	}

	@Override
	public boolean isHTML() {
		return true;
	}

	@Override
	public String getImageConfigFile() {
		return "image-config.properties";
	}

	@Override
	public String getSearchFormID() {
		return "tagid.form.search";
	}

	@Override
	public String getSelectedClass() {
		return "selected";
	}

	@Override
	public String getLastSelectedClass() {
		return "final-selected";
	}

	@Override
	public String getUnSelectedClass() {
		return "unselected";
	}

	@Override
	public boolean isValid() {
		return false;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean exist() {
		return false;
	}

	@Override
	public String getAuthors() {
		return getOwner();
	}

	@Override
	public int getDepth() {
		return 1;
	}

	@Override
	public String getDominantColor() {
		return "none";
	}

	@Override
	public String getSource() {
		return "";
	}

	@Override
	public String getVisualFile() {
		return "visual.png";
	}

	@Override
	public String getVisualPDFile() {
		return "visual.pdf";
	}

	@Override
	public boolean isPDFFile() {
		return false;
	}
	
	@Override
	public Properties getI18nProperties(GlobalContext globalContext, Locale locale) throws IOException {
		return I18nAccess.FAKE_I18N_FILE;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<String> getAreas() {
		return Collections.EMPTY_LIST;
	}

	/**
	 * this area is display if specialrendere is defined
	 *
	 * @return
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<String> getAreasForceDisplay() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public Date getCreationDate() {
		return new Date(0);
	}

	@Override
	public void delete() {
	}

	@Override
	protected String getImageFiltersRAW() {
		return "standard;under-control-free";
	}

	@Override
	public List<String> getImageFilters() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getLinkEmailFileName(String lg) {
		return "";
	}

	@Override
	public File getLinkEmail(String lg) {
		String templateFolder = config.getTemplateFolder();
		if (isMailing()) {
			templateFolder = config.getMailingTemplateFolder();
		}
		File linkEmailFile = new File(URLHelper.mergePath(URLHelper.mergePath(templateFolder, getSourceFolder()), getLinkEmailFileName(lg)));
		return linkEmailFile;

	}

	@Override
	public boolean isLinkEmail(String lg) {
		return false;
	}

	@Override
	public List<String> getEmailLinkFileList() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public boolean isSubjectLocked() {
		return false;
	}

	@Override
	public boolean visibleForRoles(Collection<String> inRoles) {
		return false;
	}

	@Override
	public Collection<String> getCategories() {
		return Collections.EMPTY_LIST;
	}

	@Override
	public String getSpecialRendererTemplate() {
		return null;
	}

	@Override
	public Template getParent() {
		return null;
	}

	private Template getParent(StaticConfig config) throws IOException, ConfigurationException {
		return null;
	}

	@Override
	public void importTemplateInWebapp(ContentContext ctx) throws IOException {
		return;
	}

	@Override
	protected void importTemplateInWebapp(ContentContext ctx, GlobalContext globalContext, File templateTarget) throws IOException {
		return;
	}

	@Override
	public PropertiesConfiguration getImageConfig() {
		return null;
	}

}
