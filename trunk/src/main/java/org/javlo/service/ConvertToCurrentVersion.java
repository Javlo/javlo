package org.javlo.service;

import java.util.logging.Logger;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.image.GlobalImage;
import org.javlo.component.links.PageReferenceComponent;
import org.javlo.component.text.XHTML;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.message.GenericMessage;
import org.javlo.navigation.MenuElement;
import org.javlo.service.PersistenceService.LoadingBean;

public class ConvertToCurrentVersion {

	private static Logger logger = Logger.getLogger(ConvertToCurrentVersion.class.getName());

	public static int convert(ContentContext ctx, ComponentBean bean, String version) {
		ContentService content = ContentService.getInstance(ctx.getRequest());
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
		if (version.startsWith("1") && bean.getType().equals("banner")) {
			convertion++;
			bean.setType(GlobalImage.TYPE);
			bean.setValue(bean.getValue() + "\n" + GlobalImage.IMAGE_FILTER + "=banner");
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
