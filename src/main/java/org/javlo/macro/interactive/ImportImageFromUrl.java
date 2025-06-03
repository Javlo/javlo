package org.javlo.macro.interactive;

import org.javlo.actions.IAction;
import org.javlo.component.image.GlobalImage;
import org.javlo.context.ContentContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.macro.core.IInteractiveMacro;
import org.javlo.service.RequestService;
import org.javlo.ztatic.StaticInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ImportImageFromUrl implements IInteractiveMacro, IAction {

	private static final int MIN_WIDTH = 300;
	
	private static Logger logger = Logger.getLogger(ImportImageFromUrl.class.getName());

	public static class ImageBean {
		public String id = StringHelper.getRandomId();
		public String url;
		public String alt;

		ImageBean(String url, String alt) {
			this.url = url;
			this.alt = alt;
		}

		@Override
		public String toString() {
			return "ImageBean{url='" + url + "', alt='" + alt + "'}";
		}

		public String getId() {
			return id;
		}

		public String getUrl() {
			return url;
		}

		public String getAlt() {
			return alt;
		}
	}

	private static boolean isImageUrl(String url) {
		return url != null && url.matches("(?i).+\\.(png|jpe?g|gif|webp|bmp|svg)$");
	}

	private static Map<String, ImageBean> getLargeImagesFromUrl(String pageUrl) {
		Map<String, ImageBean> images = new HashMap<>();
		try {
			Document doc = Jsoup.connect(pageUrl).get();
			Elements imgElements = doc.select("img");

			logger.info("load " + imgElements.size() + " images");

			for (Element img : imgElements) {
				String imgUrl = img.absUrl("src");
				String dataSrc = img.attr("data-src");
				if (dataSrc != null && !dataSrc.isEmpty()) {
					imgUrl = dataSrc;
					if (!StringHelper.isURL(imgUrl)) {
						imgUrl = img.absUrl("src");
					}
				}

				// 🔍 Vérifie si le parent est un lien <a> avec un href pointant vers une image
				Element parent = img.parent();
				if (parent != null && parent.tagName().equals("a")) {
					String href = parent.absUrl("href");
					if (isImageUrl(href)) {
						imgUrl = href;
					}
				}

				String alt = img.attr("alt");

				if (!imgUrl.isEmpty()) {
					try (InputStream in = new URL(imgUrl).openStream()) {
						//BufferedImage image = ImageIO.read(in);
						//if (image != null && image.getWidth() >= MIN_WIDTH) {
						ImageBean imgBean = new ImageBean(imgUrl, alt);
						images.put(imgBean.getId(), imgBean);
					/*} else {
						logger.info("image too small: " + imgUrl);
					}*/
					} catch (Exception e) {
						logger.warning(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // ou logger
		}
		return images;
	}


	@Override
	public String getName() {
		return "import-image-from-url";
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
	public boolean isPreview() {
		return true;
	}

	@Override
	public boolean isAdd() {
		return false;
	}

	@Override
	public boolean isInterative() {
		return true;
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(ContentContext ctx) {

	}

	@Override
	public String getInfo(ContentContext ctx) {
		return "";
	}

	@Override
	public String getIcon() {
		return "bi bi-images";
	}

	@Override
	public String getUrl() {
		return "";
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public int getType() {
		return TYPE_TOOLS;
	}

	@Override
	public String getActionGroupName() {
		return getName();
	}

	@Override
	public boolean haveRight(ContentContext ctx, String action) {
		return true;
	}

	@Override
	public String getRenderer() {
		return "/jsp/macros/import-image-from-url/home.jsp";
	}

	@Override
	public String prepare(ContentContext ctx) {
		
		String url = ctx.getRequest().getParameter("url");
		if (StringHelper.isURL(url)) {
			logger.info("load image : "+url);
			ctx.getRequest().getSession().setAttribute("images", getLargeImagesFromUrl(url));
		} else {
			logger.info("url not found or invalid: " + url);
		}

		return null;

	}

	public static String performUpload(RequestService rs, ContentContext ctx) throws Exception {
		if (ctx.getRequest().getSession().getAttribute("images") != null) {
			Map<String, ImageBean> images = (Map<String, ImageBean>)ctx.getRequest().getSession().getAttribute("images");
			try {
				String importFolder = URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), ctx.getGlobalContext().getStaticConfig().getImageFolder(), GlobalImage.getImportFolderPath(ctx, ctx.getCurrentPage()));
				new File(importFolder).mkdirs();
				for (ImageBean imgBean : images.values()) {
					if (ctx.getRequest().getParameter("img-" + imgBean.getId()) != null) {
						String fileName = StringHelper.getFileNameFromPath(imgBean.getUrl());
						File importFile = new File(importFolder, fileName);
						if (!importFile.exists()) {
							logger.info("import : "+importFile);
							importFile.createNewFile();
							try (FileOutputStream outStr = new FileOutputStream(importFile)) {
								NetHelper.writeURLToStream(new URL(imgBean.getUrl()), outStr);
							}
							StaticInfo si = StaticInfo.getInstance(ctx, importFile);
							si.setTitle(ctx, imgBean.getAlt());
						}
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		ctx.setClosePopup(true);
		return null;
	}

	@Override
	public String getModalSize() {
		return DEFAULT_MAX_MODAL_SIZE;
	}

}
