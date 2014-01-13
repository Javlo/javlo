package org.javlo.helper.importation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.layout.ContentSeparation;
import org.javlo.component.list.List;
import org.javlo.component.text.Paragraph;
import org.javlo.component.text.XHTML;
import org.javlo.component.title.MenuTitle;
import org.javlo.component.title.SubTitle;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.utils.UnclosableInputStream;
import org.javlo.xml.NodeXML;
import org.javlo.xml.XMLFactory;

public class TanukiImportTools {
	
	private static Logger logger = Logger.getLogger(TanukiImportTools.class.getName());
	
	/**
	 * import a zip entry localy.
	 * 
	 * @param gc
	 * @param entry
	 *            the zip entry.
	 * @param localFolder
	 *            the local forlder in static folder.
	 * @return false if file not create.
	 * @throws IOException
	 */
	public static boolean importZipEntryToDataFolder(GlobalContext gc, ZipEntry entry, InputStream in, String localFolder) throws IOException {
		File newFile = new File(URLHelper.mergePath(gc.getDataFolder(), localFolder, entry.getName()));
		if (newFile.exists() || entry.isDirectory()) {
			return false;
		} else {
			newFile.getParentFile().mkdirs();
			if (!newFile.createNewFile()) {
				return false;
			} else {
				ResourceHelper.writeStreamToFile(in, newFile);
			}
			return true;
		}

	}
	
	private static void importContent (ContentContext ctx, MenuElement page, NodeXML node) throws Exception {
		String parentId = "0";
		for (NodeXML childNode : node.getChildren()) {			
			if (childNode.getName().equals("fragment")) {
				String type = childNode.getAttributeValue("type", null);
				String lang = childNode.getAttributeValue("language", null);
				String zone = childNode.getAttributeValue("zone", null);	
				boolean repeat = StringHelper.isTrue(childNode.getAttributeValue("to-be-inherited"));
				if (type != null) {
					ComponentBean compBean = null;
					ComponentBean compBeanBis = null;
					if (type.equals("paragraph") && childNode.getChild("value") != null) {
						String value = childNode.getChild("value").getContent();
						String variant = childNode.getChild("variant").getContent();
						if (variant.equals("raw")) {
							compBean = new ComponentBean(XHTML.TYPE, value, lang);	
						} else if (variant.equals("list")) {
							compBean = new ComponentBean(List.TYPE, value, lang);
						} else {
							compBean = new ComponentBean(Paragraph.TYPE, value, lang);
						}						
					} else if (type.equals("heading")) {
						String level = childNode.getChild("level").getContent();
						String value = childNode.getChild("value").getContent();
						compBean = new ComponentBean(SubTitle.TYPE, value, lang);
						compBean.setStyle(""+(Integer.parseInt(level)+1));
					} else if (type.equals("separation")) {
						compBean = new ComponentBean(ContentSeparation.TYPE, "", lang);
					} else if (type.equals("meta")) {
						String value = childNode.getChild("title").getContent();
						if (childNode.getChild("nav-title") != null) {
							compBeanBis = new ComponentBean (MenuTitle.TYPE, value, lang);
						}
						compBean = new ComponentBean(Title.TYPE, value, lang);
					}
					if (compBean != null) {
						compBean.setArea(zone);
						compBean.setId(StringHelper.getRandomId());
						compBean.setRepeat(repeat);
						page.addContent(parentId, compBean);
						parentId = compBean.getId();						
					}
					if (compBeanBis != null) {
						compBeanBis.setArea(zone);
						compBeanBis.setId(StringHelper.getRandomId());
						compBeanBis.setRepeat(repeat);
						page.addContent(parentId, compBeanBis);
						parentId = compBean.getId();						
					}
				}				
			}
		}			
	}
	
	private static void importChildren (ContentContext ctx, MenuElement page, NodeXML node, String depth) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		int priority = 10;
		for (NodeXML childNode : node.getChildren()) {
			if (childNode.getName().equals("page")) {
				String pageName = childNode.getAttributeValue("name", "no-name");
				System.out.println(depth+pageName); //TODO: remove debug trace				
				
				String finalPageName = pageName;
				int pageNumber = 0;
				while (content.getNavigation(ctx).searchChildFromName(finalPageName) != null) {
					pageNumber++;
					finalPageName = pageName+'_'+pageNumber;
				}
				
				MenuElement newChild = MenuElement.getInstance(ctx.getGlobalContext());
				newChild.setName(finalPageName);				
				newChild.setCreator(ctx.getCurrentUserId());
				
				Date date = StringHelper.parseDate(childNode.getAttributeValue("date"), "dd/MM/yyyy HH:mm");;
				newChild.setVisible(date.before(new Date()));
				
				newChild.setPriority(priority);
				page.addChildMenuElement(newChild);			
				page.releaseCache();
				importContent(ctx, newChild, childNode);
				importChildren(ctx, newChild, childNode, depth+"   ");
				priority+=10;
			}
		}
	}
	
	public static void createContentFromTanuki(ContentContext ctx, InputStream in, String name, String lang) throws Exception {
		logger.info("import tanuki content : "+name);
		MenuElement page = ctx.getCurrentPage();
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		String baseStaticFolder = "/import/" + name;
		while (entry != null) {			
			if (page != null && StringHelper.isImage(entry.getName())) {
				importZipEntryToDataFolder(ctx.getGlobalContext(), entry, zipIn, URLHelper.mergePath(ctx.getGlobalContext().getStaticConfig().getImageFolder(), baseStaticFolder));
			} else if (entry.getName().endsWith("live.xml")) {				
				NodeXML root = XMLFactory.getFirstNode(new UnclosableInputStream(zipIn));
				root = root.getChild("page");
				importChildren(ctx, ctx.getCurrentPage(), root, "   ");
			}
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}

}


