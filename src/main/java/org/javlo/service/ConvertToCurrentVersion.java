package org.javlo.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import java.util.logging.Logger;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.links.ExternalLink;
import org.javlo.component.links.PageReferenceComponent;
import org.javlo.component.text.Paragraph;
import org.javlo.component.text.XHTML;
import org.javlo.component.title.Title;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService.LoadingBean;

public class ConvertToCurrentVersion {

	private static Logger logger = Logger.getLogger(ConvertToCurrentVersion.class.getName());

	public static int convert(ContentContext ctx, ComponentBean bean, String version) {
		
		if (version == null) {
			version = "1.0";
		}
		
		int convertion = 0;
		if (bean.getType().equals("page-links")) {
			bean.setType(PageReferenceComponent.TYPE);
			if (bean.getValue().contains("slide-show")) {
				bean.setRenderer("carousel");
			} else {
				bean.setRenderer("products");
			}
			bean.setModify(true);
			convertion++;
		}
		if (version.startsWith("1") && bean.getType().equals("video")) {
			convertion++;
			bean.setType(XHTML.TYPE);
			bean.setModify(true);
		}
		if (bean.getType().equals("page-teaser")) {
			convertion++;
			bean.setType(PageReferenceComponent.TYPE);
			bean.setModify(true);
		}
		if (bean.getType().equals("important-message")) {
			convertion++;
			bean.setType(Paragraph.TYPE);
			bean.setStyle("important");
			bean.setModify(true);
		}
		if (bean.getType().equals("note")) {
			convertion++;
			bean.setType(Paragraph.TYPE);
			bean.setStyle("note");
			bean.setModify(true);
		}
		if (bean.getType().equals("email")) {
			convertion++;
			bean.setType(ExternalLink.TYPE);			
			bean.setModify(true);
		}
		if (bean.getType().equals("external-describe-link")) {
			convertion++;
			bean.setType(ExternalLink.TYPE);			
			bean.setModify(true);
		}		
		if (bean.getType().equals("quotation")) {
			convertion++;
			bean.setType(Paragraph.TYPE);
			bean.setStyle("quotation");
			bean.setModify(true);
		}
		if (bean.getType().equals("middle-image")) {
			convertion++;
			bean.setType(GlobalImage.TYPE);			
			bean.setModify(true);			
		}
		if (bean.getType().equals("standard-image-clickable")) {
			convertion++;
			bean.setType(GlobalImage.TYPE);			
			bean.setModify(true);			
		}
		if (version.startsWith("1") && bean.getType().equals("banner")) {
			convertion++;
			bean.setType(GlobalImage.TYPE);
			bean.setValue(bean.getValue() + "\n" + GlobalImage.IMAGE_FILTER + "=banner");
			bean.setModify(true);
		}
		if (bean.getType().equals("double-title")) {
			convertion++;
			bean.setType(Title.TYPE);
			Properties prop = new Properties();
			try {
				prop.load(new StringReader(bean.getValue()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			bean.setValue(prop.getProperty("title"));
			bean.setModify(true);
		}
		return convertion;
	}

	public static void convert(ContentContext ctx, LoadingBean lBean) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NotificationService notificationService = NotificationService.getInstance(globalContext);

		MenuElement root = lBean.getRoot();
		MenuElement[] children;
		try {
			children = root.getAllChildren();

			int convertion = 0;

			for (MenuElement child : children) {
				ComponentBean[] beans = child.getContent();
				for (ComponentBean bean : beans) {
					convertion += convert(ctx, bean, lBean.getCmsVersion());
				}

			}

			if (convertion > 0) {
				String msg = "component converted (mode:" + ctx.getRenderMode() + ") : " + convertion;
				notificationService.addSystemNotification(msg, GenericMessage.INFO);
				logger.info(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
			notificationService.addSystemNotification("content convertion error : " + e.getMessage(), GenericMessage.ERROR);
		}
	}
}
