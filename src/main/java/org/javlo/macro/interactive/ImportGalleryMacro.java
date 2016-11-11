package org.javlo.macro.interactive;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class ImportGalleryMacro implements IInteractiveMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportGalleryMacro.class.getName());

	@Override
	public String getName() {
		return "import-gallery";
	}

	@Override
	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {
		return null;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/import-gallery.jsp";
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-gallery";
	}

	public static String performImport(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		String url = rs.getParameter("url", null);
		String dir = rs.getParameter("dir", null);
		if (url == null || dir == null) {
			return "bad request structure : need 'url' and 'dir' as parameter.";
		}
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			return "url is'nt valid : " + e.getMessage();
		}
		Collection<URL> imagesLinks = NetHelper.extractMostSimilarLinks(new URL(url));
		if (imagesLinks == null) {
			return "gallery not found.";
		} else {
			if (imagesLinks.size() < 2 || !StringHelper.isImage(imagesLinks.iterator().next().getFile())) {
				return "this is not a image gallery.";
			}
			File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getStaticFolder(), dir));
			if (!folder.exists()) {
				folder.mkdirs();
			} else {
				if (folder.isFile()) {
					return dir + " is'nt a folder.";
				}
			}
			logger.info("import image in : " + folder);
			int c = 0;
			for (URL imageURL : imagesLinks) {
				String fileName = URLHelper.extractFileName(imageURL.getFile());
				File localFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), fileName));
				if (localFile.exists()) {
					logger.warning("file already exist : " + localFile);
					messageRepository.setGlobalMessage(new GenericMessage("some files already exist locally.", GenericMessage.ALERT));
				} else {
					try {
						logger.info("import new file : " + localFile);
						InputStream in = imageURL.openConnection().getInputStream();
						localFile.createNewFile();
						ResourceHelper.writeStreamToFile(in, localFile);
						c++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			messageRepository.setGlobalMessage(new GenericMessage(c + " file imported.", GenericMessage.INFO));
			if (c > 0) {
				String browseURL = URLHelper.createModuleURL(ctx, URLHelper.createURL(ctx), "file");
				String relativeFolder = URLHelper.mergePath(globalContext.getStaticConfig().getStaticFolder(), dir);
				ctx.getRequest().setAttribute("browse", browseURL + "&path=" + relativeFolder + "&page=meta&webaction=changerenderer");
			}
		}
		return null;
	}

	@Override
	public String prepare(ContentContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPreview() {
		return false;
	}
}
