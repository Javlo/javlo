package org.javlo.service.shared;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.filter.ImageFileFilter;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.navigation.MenuElement;
import org.javlo.template.Template;
import org.javlo.user.AdminUserSecurity;
import org.javlo.ztatic.StaticInfo;

public class LocalImageSharedContentProvider extends AbstractSharedContentProvider {

	private static Logger logger = Logger.getLogger(LocalImageSharedContentProvider.class.getName());

	public static final String NAME = "local-image";

	private Collection<SharedContent> content = new LinkedList<SharedContent>();

	LocalImageSharedContentProvider() {
		setName(NAME);
	}

	protected ComponentBean getComponentBean(String name, String category, String specialValue, String lg) {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("dir=" + category);
		out.println("file-name=" + name);
		if (specialValue != null) {
			out.println(specialValue);
		}
		out.close();
		String value = new String(outStream.toByteArray());
		ComponentBean imageBean = new ComponentBean(GlobalImage.TYPE, value, lg);
		return imageBean;
	}

	protected String getPreviewURL(ContentContext ctx, ComponentBean compBean) throws Exception {
		GlobalImage image = new GlobalImage();
		image.init(compBean, ctx);
		return image.getPreviewURL(ctx.getContextWithArea(ComponentBean.DEFAULT_AREA), "shared-preview");
	}

	protected FileFilter getFilter() {
		return new ImageFileFilter();
	}

	protected File getRootFolder(ContentContext ctx) {
		return new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getImageFolder()));
	}

	@Override
	public Collection<SharedContent> getContent(ContentContext ctx) {
		setCategories(new HashMap<String, String>());
		content.clear();
		File rootFolder = getRootFolder(ctx);		
		for (File file : ResourceHelper.getAllFiles(rootFolder, getFilter())) {			
			String category = StringHelper.cleanPath(file.getParentFile().getAbsolutePath().replace(rootFolder.getAbsolutePath(), ""));
			if (category.startsWith("/")) {
				category = category.substring(1);
			}
			if (!categories.containsKey(category)) {
				categories.put(category, category);
			}
			ComponentBean compBean = getComponentBean(file.getName(), category, GlobalImage.IMAGE_FILTER + "=full", ctx.getRequestContentLanguage());
			compBean.setArea(ctx.getArea());
			SharedContent sharedContent;
			try {
				if (isCategoryAccepted(ctx, category, ctx.getCurrentPage(), ctx.getCurrentTemplate())) {
					StaticInfo staticInfo = StaticInfo.getInstance(ctx, file);
					sharedContent = new SharedContent("" + file.hashCode(), compBean);
					sharedContent.addCategory(category);
					sharedContent.setSortOn(staticInfo.getCreationDate(ctx).getTime());
					content.add(sharedContent);

					sharedContent.setTitle(file.getName());
					sharedContent.setDescription(staticInfo.getTitle(ctx));
					sharedContent.setImageUrl(getPreviewURL(ctx, compBean));
					sharedContent.setEditAsModal(true);
					String url = URLHelper.createURL(ctx.getContextWithOtherRenderMode(ContentContext.EDIT_MODE));
					url = URLHelper.addParam(url, "webaction", "file.previewEdit");
					url = URLHelper.addParam(url, "module", "file");
					url = URLHelper.addParam(url, "nobreadcrumbs", "true");
					url = URLHelper.addParam(url, "file", URLHelper.encodePathForAttribute(file.getPath()));
					url = URLHelper.addParam(url, "previewEdit", "true");
					sharedContent.setEditURL(url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return content;
	}

	@Override
	protected boolean isCategoryAccepted(ContentContext ctx, String category, MenuElement cp, Template template) {
		if (!category.startsWith("/")) {
			category = '/'+category;
		}
		if (!category.startsWith(ctx.getGlobalContext().getStaticConfig().getImportFolder()) && !category.startsWith(ctx.getGlobalContext().getStaticConfig().getImportImageFolder()) && !category.startsWith(ctx.getGlobalContext().getStaticConfig().getImportGalleryFolder()) && !category.startsWith(ctx.getGlobalContext().getStaticConfig().getImportResourceFolder())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Map<String, String> getCategories(ContentContext ctx) {
		Map<String, String> outCategories = new HashMap<String, String>();
		MenuElement cp;
		try {
			cp = ctx.getCurrentPage();
			Template template = ctx.getCurrentTemplate();
			for (String category : categories.keySet()) {
				String catKey = category;
				category = '/' + category;
				if (isCategoryAccepted(ctx, category, cp, template)) {
					outCategories.put(catKey, catKey);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return outCategories;
	}

	@Override
	public String getType() {
		return TYPE_IMAGE;
	}

	@Override
	public boolean isUploadable(ContentContext ctx) {
		return AdminUserSecurity.isCurrentUserCanUpload(ctx);
	}
	
	protected boolean acceptedDocument(ContentContext ctx, String fileName) {
		return ResourceHelper.isAcceptedImage(ctx, fileName);
	}

	@Override
	public void upload(ContentContext ctx, String fileName, InputStream in, String category, boolean rename) throws IOException {
		if (!acceptedDocument(ctx, fileName)) {
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
			MessageRepository messageRepository = MessageRepository.getInstance(ctx);
			messageRepository.setGlobalMessage(new GenericMessage(i18nAccess.getText("global.message.file-format-error"), GenericMessage.ERROR));
			return;
		}
		File imageFolder = getRootFolder(ctx);
		imageFolder = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), category));
		File newFile = new File(URLHelper.mergePath(imageFolder.getAbsolutePath(), fileName));
		System.out.println("rename = "+rename);
		if (rename) {
			newFile = ResourceHelper.getFreeFileName(newFile);
		}
		ResourceHelper.writeStreamToFile(in, newFile);
		logger.info("imported file : " + newFile);
	}
}
