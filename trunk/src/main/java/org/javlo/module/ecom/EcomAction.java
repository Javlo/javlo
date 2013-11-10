package org.javlo.module.ecom;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.javlo.actions.AbstractModuleAction;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.ecom.EcomService;
import org.javlo.ecom.PayementExternalService;
import org.javlo.helper.BeanHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.module.core.ModulesContext;
import org.javlo.service.RequestService;

public class EcomAction extends AbstractModuleAction {

	private static Logger logger = Logger.getLogger(EcomAction.class.getName());

	@Override
	public String prepare(ContentContext ctx, ModulesContext modulesContext) throws Exception {
		String outMsg = super.prepare(ctx, modulesContext);
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		ctx.getRequest().setAttribute("baskets", basketPersistenceService.getAllBaskets());
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
	
	public static String performStoreBasket(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException {
		BasketPersistenceService service = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Collection baskets = service.getAllBaskets();
		File file = BasketPersistenceService.getFolder(ctx.getGlobalContext());
		if (file.exists()) {
			File storeFile = new File (URLHelper.mergePath(file.getAbsolutePath(), "csv", StringHelper.createFileName("basket_storage_"+StringHelper.renderSortableTime(new Date())+".csv")));
			if (!storeFile.exists()) {
				storeFile.getParentFile().mkdirs();
				storeFile.createNewFile();
			}			
			BeanHelper.storeBeanToCSV(storeFile, baskets);			
		} else {
			return "file not found : "+file;
		}
		messageRepository.setGlobalMessage(new GenericMessage(""+baskets.size()+" baskets exported.", GenericMessage.INFO));
		return null;
	}
	
	public static String performManualPay(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		if (basket != null) {
			basket.setStatus(Basket.STATUS_MANUAL_PAYED);
			basketPersistenceService.storeBasket(basket);
		}
		return null;
	}
	
	public static String performSended(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		if (basket != null) {
			basket.setStatus(Basket.STATUS_SENDED);
			basketPersistenceService.storeBasket(basket);
		}
		return null;
	}
	
	public static String performDelete(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws IOException {
		BasketPersistenceService basketPersistenceService = BasketPersistenceService.getInstance(ctx.getGlobalContext());
		Basket basket = basketPersistenceService.getBasket(rs.getParameter("basket", null));
		if (basket != null) {
			basket.setDeleted(true);
			basketPersistenceService.storeBasket(basket);
		}
		return null;
	}
	
	public static String performImportPayement(RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) {
		
		return null;
	}

}
