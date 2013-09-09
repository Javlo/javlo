package org.javlo.macro;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.fileupload.FileItem;
import org.javlo.actions.IAction;
import org.javlo.component.core.ComponentBean;
import org.javlo.context.ContentContext;
import org.javlo.context.EditContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ContentHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class ImportContent implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportContent.class.getName());

	@Override
	public String getName() {
		return "import-content";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-content";
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/import-content.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		return null;
	}

	public static String performImport(RequestService rs, ContentContext ctx, EditContext editCtx, ContentService content, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {

		String encoding = rs.getParameter("encoding", ContentContext.CHARACTER_ENCODING);

		Collection<FileItem> items = rs.getAllFileItem();
		for (FileItem fileItem : items) {
			InputStream in = null;
			try {
				in = fileItem.getInputStream();
				List<ComponentBean> newBeans = null;
				if (StringHelper.isHTML(fileItem.getName())) {
					String html = ResourceHelper.loadStringFromStream(fileItem.getInputStream(), Charset.forName(encoding));
					newBeans = ContentHelper.createContentWithHTML(html, ctx.getRequestContentLanguage());
				} else if (StringHelper.getFileExtension(fileItem.getName()).equalsIgnoreCase("odt")) {
					newBeans = ContentHelper.createContentFromODT(GlobalContext.getInstance(ctx.getRequest()), in, fileItem.getName(), ctx.getRequestContentLanguage());
				}
				if (newBeans != null) {
					logger.info("import file : " + newBeans.size() + " components.");
					String parentId = "0";
					for (ComponentBean bean : newBeans) {
						parentId = content.createContent(ctx, bean, parentId, false);
					}
				}

			} finally {
				ResourceHelper.closeResource(in);
			}
		}

		String url = rs.getParameter("url", "");
		if (StringHelper.isURL(url)) {
			String html = NetHelper.readPage(new URL(url));
			List<ComponentBean> newBeans = ContentHelper.createContentWithHTML(html, ctx.getRequestContentLanguage());

			logger.info("import url : " + newBeans.size() + " components.");

			String parentId = "0";
			for (ComponentBean bean : newBeans) {
				parentId = content.createContent(ctx, bean, parentId, false);
			}
		}

		ctx.getCurrentPage().releaseCache();

		if (ctx.isEditPreview()) {
			ctx.setClosePopup(true);
		}

		return null;
	}

	@Override
	public boolean isPreview() {
		return true;
	}
}
