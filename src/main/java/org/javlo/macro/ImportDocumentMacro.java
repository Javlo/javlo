package org.javlo.macro;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.list.FreeTextList;
import org.javlo.component.text.Paragraph;
import org.javlo.component.title.Title;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XMLManipulationHelper;
import org.javlo.helper.XMLManipulationHelper.TagDescription;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class ImportDocumentMacro extends AbstractMacro {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ImportDocumentMacro.class.getName());

	private static final String TO_IMPORT_FOLDER = "files-to-import";
	private static final String IMPORTED_FILE_FOLDER = "imported-files";

	private int latestInsertionStart = 0;

	public String getName() {
		return "import-doc-here";
	}

	private String insertTag(MenuElement page, String lg, String parentId, TagDescription[] allTags, TagDescription tag, String content, String componentContent) throws Exception {
		String insideContent = tag.getInside(content);
		insideContent = StringHelper.removeCR(insideContent);

		if (componentContent != null) {
			insideContent = componentContent;
		} else {
			insideContent = StringHelper.removeTag(insideContent);
			insideContent = StringHelper.CRtoSpace(insideContent);
		}

		if (insideContent.trim().length() > 0) {
			if (tag.getName().equalsIgnoreCase("img") || tag.getOpenStart() > latestInsertionStart) {
				if (tag.getName().equalsIgnoreCase("h1")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, Title.TYPE, insideContent);
				} else if (tag.getName().equalsIgnoreCase("h2")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, org.javlo.component.title.SubTitle.TYPE, insideContent);
				} else if (tag.getName().equalsIgnoreCase("h3")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, org.javlo.component.title.SubTitle.TYPE, "3", insideContent);
				} else if (tag.getName().equalsIgnoreCase("h4")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, org.javlo.component.title.SubTitle.TYPE, "4", insideContent);
				} else if (tag.getName().equalsIgnoreCase("h5")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, org.javlo.component.title.SubTitle.TYPE, "5", insideContent);
				} else if (tag.getName().equalsIgnoreCase("p")) {
					Set<String> tags = XMLManipulationHelper.getAllParentName(allTags, tag);
					if (!XMLManipulationHelper.getAllParentName(allTags, tag).contains("ul") && !XMLManipulationHelper.getAllParentName(allTags, tag).contains("ol")) {
						latestInsertionStart = tag.getCloseEnd();
						return MacroHelper.addContent(lg, page, parentId, Paragraph.TYPE, insideContent);
					}
				} else if (tag.getName().equalsIgnoreCase("img")) {
					return MacroHelper.addContent(lg, page, parentId, GlobalImage.TYPE, insideContent);
				} else if (tag.getName().equalsIgnoreCase("ul") || XMLManipulationHelper.getAllParentName(allTags, tag).contains("ul")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, FreeTextList.TYPE, insideContent);
				} else if (tag.getName().equalsIgnoreCase("ol") || XMLManipulationHelper.getAllParentName(allTags, tag).contains("ol")) {
					latestInsertionStart = tag.getCloseEnd();
					return MacroHelper.addContent(lg, page, parentId, FreeTextList.TYPE, FreeTextList.NUMBER_LIST, insideContent);
				}
			}
		}

		return null;
	}

	public void createPage(ContentContext ctx, File file, MenuElement currentPage) throws Exception {
		int tagInsered = 0;
		if (file.isDirectory() || StringHelper.isHTML(file.getName())) {
			String pageName = StringHelper.createFileName(StringHelper.getFileNameWithoutExtension(file.getName()));
			if (currentPage.searchChildFromName(pageName) == null) {
				MenuElement newPage = MacroHelper.addPageIfNotExist(ctx, currentPage, pageName, false);
				if (file.isFile()) {
					String content = ResourceHelper.getFileContent(file);
					TagDescription[] tags = XMLManipulationHelper.searchAllTag(content, false);
					// Arrays.sort(tags, new
					// XMLManipulationHelper.TagComparatorOnStartTag());
					String parentId = "0";
					logger.info("tags found in HTML file : " + tags.length);
					latestInsertionStart = 0;
					for (int i = 0; i < tags.length; i++) {
						TagDescription tag = tags[i];
						String fileURI = null;
						if (tag.getName().equalsIgnoreCase("img")) {
							String imageSrc = tag.getAttributes().get("src");
							if (imageSrc == null) {
								imageSrc = tag.getAttributes().get("SRC");
							}
							if (imageSrc != null) {
								File imageFile = new File(URLHelper.mergePath(file.getParentFile().getAbsolutePath(), imageSrc));
								if (imageFile.exists()) {
									StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
									GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
									String imageFolder = URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getImageFolder());
									imageFolder = URLHelper.mergePath(imageFolder, IMPORTED_FILE_FOLDER);
									File fileImageFolder = new File(imageFolder);
									fileImageFolder.mkdirs();
									FileUtils.copyFileToDirectory(imageFile, fileImageFolder);
									fileURI = URLHelper.mergePath(staticConfig.getImageFolder(), IMPORTED_FILE_FOLDER);
									fileURI = URLHelper.mergePath(fileURI, imageFile.getName());
								} else {
									logger.warning("file not found : "+imageFile);
								}
							}
						}

						String tagContent = null;
						if (fileURI != null) {
							StringWriter writer = new StringWriter();
							PrintWriter out = new PrintWriter(writer);
							File fileFileURI = new File(fileURI);
							out.println(GlobalImage.DIR_KEY + '=' + IMPORTED_FILE_FOLDER);
							out.println(GlobalImage.FILE_NAME_KEY + '=' + fileFileURI.getName());
							out.println(GlobalImage.LABEL_KEY + '=' + StringHelper.neverEmpty(tag.getAttributes().get("alt"), ""));
							out.close();
							tagContent = writer.toString();
						}
						String newParentId = insertTag(newPage, ctx.getContentLanguage(), parentId, tags, tag, content, tagContent);
						if (newParentId != null) {
							parentId = newParentId;
							tagInsered++;
						}
					}
				}
			}
		}
		logger.info("tag identified and insered : " + tagInsered);
	}

	public String perform(ContentContext ctx, Map<String, Object> params) throws Exception {

		ContentService content = ContentService.createContent(ctx.getRequest());
		MenuElement currentPage = ctx.getCurrentPage();
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession().getServletContext());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		if (currentPage != null) {
			File dir = new File(URLHelper.mergePath(globalContext.getDataFolder(), staticConfig.getStaticFolder()) + '/' + TO_IMPORT_FOLDER);
			if (!dir.exists()) {
				dir.mkdirs();
			}

			File[] subdirs = dir.listFiles();
			for (File dirFile : subdirs) {
				createPage(ctx, dirFile, currentPage);
			}
		}

		return null;
	}
}
