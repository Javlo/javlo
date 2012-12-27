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

	public static void convert(ContentContext ctx, LoadingBean lBean) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		NotificationService notificationService = NotificationService.getInstance(globalContext);

		MenuElement root = lBean.getRoot();
		MenuElement[] children;
		try {
			children = root.getAllChildren();

			int pageLinkConvertion = 0;
			int pageVideoConvertion = 0;
			int pageBannerConvertion = 0;

			for (MenuElement child : children) {
				ComponentBean[] beans = child.getContent();
				for (ComponentBean bean : beans) {
					if (bean.getType().equals("page-links")) {
						bean.setType(PageReferenceComponent.TYPE);
						if (bean.getValue().contains("slide-show")) {
							bean.setRenderer("carousel");
						} else {
							bean.setRenderer("products");
						}
						bean.setModify(true);
						pageLinkConvertion++;
					}
					if (lBean.getCmsVersion().startsWith("1") && bean.getType().equals("video")) {
						pageVideoConvertion++;
						bean.setType(XHTML.TYPE);
						bean.setModify(true);
					}
					if (lBean.getCmsVersion().startsWith("1") && bean.getType().equals("banner")) {
						pageBannerConvertion++;
						bean.setType(GlobalImage.TYPE);
						bean.setValue(bean.getValue() + "\n" + GlobalImage.IMAGE_FILTER + "=banner");
						bean.setModify(true);
					}
				}

			}

			if (pageLinkConvertion > 0) {
				String msg = "page-links converted (mode:" + ctx.getRenderMode() + ") : " + pageLinkConvertion;
				notificationService.addSystemNotification(msg, GenericMessage.INFO);
				logger.info(msg);
			}

			if (pageVideoConvertion > 0) {
				String msg = "videos converted (mode:" + ctx.getRenderMode() + ") : " + pageVideoConvertion;
				notificationService.addSystemNotification(msg, GenericMessage.INFO);
				logger.info(msg);
			}

			if (pageBannerConvertion > 0) {
				String msg = "banner converted (mode:" + ctx.getRenderMode() + ") : " + pageBannerConvertion;
				notificationService.addSystemNotification(msg, GenericMessage.INFO);
				logger.info(msg);
			}

		} catch (Exception e) {
			e.printStackTrace();
			notificationService.addSystemNotification("content convertion error : " + e.getMessage(), GenericMessage.ERROR);
		}
	}
}
