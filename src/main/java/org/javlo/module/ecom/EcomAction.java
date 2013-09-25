package org.javlo.module.ecom;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.ecom.ProductComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.EcomService;
import org.javlo.ecom.PayementExternalService;
import org.javlo.ecom.Product;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;
import org.javlo.service.RequestService;

public class EcomAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(EcomAction.class.getName());

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String outMsg = super.prepare(ctx, modulesContext);
		EcomService ecomService = EcomService.getInstance(ctx.getGlobalContext(), ctx.getRequest().getSession());
		ctx.getRequest().setAttribute("ecomServices", ecomService.getExternalService());
		return outMsg;
	}

	@Override
	public String getActionGroupName() {
		return "ecom";
	}

	public static String performCreate(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) {
		String name = rs.getParameter("name", null);
		if (name == null) {
			return "bad request structure, need 'name' as parameter";
		} else {
			PayementExternalService newService = new PayementExternalService();
			newService.setName(name);
			EcomService ecomService = EcomService.getInstance(globalContext, ctx.getRequest().getSession());
			ecomService.storePayementService(newService);
		}
		return null;
	}

	

	public static String performInitbasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		ContentContext ctx = ContentContext.getContentContext(request, response);
		Basket basket = Basket.getInstance(ctx);
		basket.init(ctx);
		return null;
	}

	public static String performDeletebasket(HttpServletRequest request, HttpServletResponse response) throws Exception {
		RequestService requestService = RequestService.getInstance(request);
		String productId = requestService.getParameter("id", null);
		if (productId != null) {
			ContentContext ctx = ContentContext.getContentContext(request, response);
			Basket basket = Basket.getInstance(ctx);
			basket.removeProduct(productId);
		}
		return null;
	}

}
