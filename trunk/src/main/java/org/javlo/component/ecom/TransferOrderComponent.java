package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.actions.IAction;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.Basket;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.ecom.Product;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.NetHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.MessageRepository;
import org.javlo.service.RequestService;

public class TransferOrderComponent extends AbstractOrderComponent implements IAction {

	@Override
	public String getType() {
		return "transfer-order";
	}
	
	protected String getBaseURL() {
		return getData().getProperty("url");
	}
	
	protected String getUser() {		
		return getData().getProperty("user");
	}
	
	protected String getPassword() {
		return getData().getProperty("password");
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		
		if (ctx.getRequest().getAttribute("msg") != null) {			
			return ctx.getRequest().getAttribute("msg").toString();			
		}
		if (basket.getStep() != Basket.ORDER_STEP) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);		
		out.println("<form class=\"transfer\" action\""+URLHelper.createURL(ctx)+"\">");
		out.println("<fieldset>");		
		out.println("<input type=\"hidden\" name=\""+IContentVisualComponent.COMP_ID_REQUEST_PARAM+"\" value=\""+getId()+"\" />");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\"transfer.pay\" />");
		out.println("<input type=\"submit\" value=\""+getData().get("button")+"\" />");
		out.println("</fieldset>");
		out.println("</form>");
		out.close();
		return new String(outStream.toByteArray());	
	}
	
	public static String performPay(RequestService rs, ContentContext ctx, GlobalContext globalContext, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		Basket basket = Basket.getInstance(ctx);
		
		if (basket.getCurrencyCode() == null) {
			logger.severe("currency not found.");
			return i18nAccess.getViewText("ecom.message.error");
		}
		
		basket.setStep(Basket.FINAL_STEP);		
		basket.setStatus(Basket.STATUS_WAIT_PAY);
		BasketPersistenceService.getInstance(globalContext).storeBasket(basket);
		
		AbstractOrderComponent comp = (AbstractOrderComponent) ComponentHelper.getComponentFromRequest(ctx);
		
		String msg = XHTMLHelper.textToXHTML(comp.getConfirmationEmail(ctx,basket));
		
		msg = "<p>" + i18nAccess.getViewText("ecom.basket-confirmed") + "</p><p>" + msg + "</p>";
		
		comp.sendConfirmationEmail(ctx, basket);
		ctx.getRequest().setAttribute("msg", msg);		
		
		basket.reset(ctx);
		
		NetHelper.sendMailToAdministrator(globalContext, "basket confirmed with transfert : "+globalContext.getContextKey(), basket.toString());
		
		return null;	
	}
	
	@Override
	public String getActionGroupName() {
		return "transfer";
	}

}

