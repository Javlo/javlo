package org.javlo.macro;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.CssSelectorNodeFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;
import org.javlo.actions.IAction;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.text.WysiwygParagraph;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.service.RequestService;

public class ImportHTMLPageMacro implements IMacro, IAction {

	private static Logger logger = Logger.getLogger(ImportHTMLPageMacro.class.getName());

	@Override
	public String getName() {
		return "import-html";
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
		return "/jsp/macros/import-html.jsp";
	}

	@Override
	public String getActionGroupName() {
		return "macro-import-html";
	}

	public static String performImport(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws MalformedURLException, Exception {
		String url = rs.getParameter("url", null);
		String titleCSS = rs.getParameter("title", null);
		String contentCSS = rs.getParameter("content", null);
		String imageCSS = rs.getParameter("image", null);
		String dir = rs.getParameter("folder", "imported");

		String msg = "";

		if (url == null || titleCSS == null || contentCSS == null || imageCSS == null) {
			return "bad request structure : need 'url', 'title', 'content' and 'image' as parameter.";
		}
		try {
			URL pageURL = new URL(url);
			Parser htmlParser = new Parser(pageURL.openConnection());
			NodeFilter cssFilter = new CssSelectorNodeFilter(titleCSS.trim());
			MenuElement page = ctx.getCurrentPage();
			String latestComponentId = "0";
			// title
			NodeList nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
			if (nodeList.size() == 0) {
				msg = msg + "title not found (" + titleCSS + "). ";
			} else {
				String title = nodeList.elements().nextNode().toPlainTextString();
				latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, Title.TYPE, title);
			}
			htmlParser.reset();
			// image
			cssFilter = new CssSelectorNodeFilter(imageCSS.trim());
			nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
			if (nodeList.size() == 0) {
				msg = msg + "Content not found (" + contentCSS + "). ";
			} else {
				Node node = nodeList.elements().nextNode();
				if (node instanceof TagNode) {
					TagNode tag = (TagNode) node;
					URL imageURL = null;
					String imageTitle = null;
					if (tag.getTagName().equalsIgnoreCase("a")) { // a tag
						imageURL = new URL(tag.getAttribute("href"));
						imageTitle = tag.getAttribute("title");
					} else if (tag.getTagName().equalsIgnoreCase("img")) { // img tag
						imageURL = new URL(tag.getAttribute("src"));
						imageTitle = tag.getAttribute("alt");
					}
					if (imageURL != null) {
						String remoteFileName = StringHelper.getFileNameFromPath(imageURL.getFile());

						String fileName = URLHelper.extractFileName(remoteFileName);

						File folder = new File(URLHelper.mergePath(globalContext.getDataFolder(), globalContext.getStaticConfig().getImageFolder(), dir));

						folder.mkdirs();
						File localFile = new File(URLHelper.mergePath(folder.getAbsolutePath(), fileName));

						if (!localFile.exists()) {
							localFile.createNewFile();
							InputStream inImage = imageURL.openStream();
							ResourceHelper.writeStreamToFile(inImage, localFile);
							inImage.close();
						}

						latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, GlobalImage.TYPE, "file-name=" + remoteFileName + "\ndir=" + dir + "\nlabel=" + imageTitle, "image-center");

					}
				}
			}
			htmlParser.reset();
			// content
			cssFilter = new CssSelectorNodeFilter(contentCSS.trim());
			nodeList = htmlParser.extractAllNodesThatMatch(cssFilter);
			if (nodeList.size() == 0) {
				msg = msg + "Content not found (" + contentCSS + "). ";
			} else {
				String content = nodeList.elements().nextNode().toHtml();
				latestComponentId = MacroHelper.addContentIfNotExist(ctx, page, latestComponentId, WysiwygParagraph.TYPE, content);
			}
			htmlParser.reset();

		} catch (MalformedURLException e) {
			return "url is'nt valid : " + e.getMessage();
		}

		if (msg.trim().length() == 0) {
			return null;
		} else {
			return msg;
		}
	}
}
