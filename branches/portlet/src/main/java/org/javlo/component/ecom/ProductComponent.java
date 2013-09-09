package org.javlo.component.ecom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;


public class ProductComponent extends AbstractPropertiesComponent {

	static final List<String> FIELDS = Arrays.asList(new String[] { "name", "price", "vat", "promo", "currency", "offset", "weight", "production" });

	@Override
	public String getHeader() {
		return "Article V 1.0";
	}
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}

	public String getType() {
		return "product";
	}

	public String getName() {
		return getFieldValue("name");
	}

	public double getPrice() {
		return getFieldDoubleValue("price");
	}

	public double getVAT() {
		return getFieldDoubleValue("vat");
	}

	public double getReduction() {
		return getFieldDoubleValue("promo");
	}
	
	public String getCurrency() {
		return getFieldValue("currency", "EUR");
	}

	public long getOffset() {
		return getFieldLongValue("offset");
	}

	public long getWeight() {
		return getFieldLongValue("weight");
	}

	public long getProduction() {
		return getFieldLongValue("production");
	}

	public long getRealStock(ContentContext ctx) {
		try {
			String value = getViewData(ctx).getProperty("stock");
			return Long.valueOf(value);
		} catch (Exception e) {
			logger.log(Level.FINE, "invalid real stock, setting to zero...");
			setRealStock(ctx, 0);
			return 0;
		}
	}

	public long getVirtualStock(ContentContext ctx) {
		try {
			String value = getViewData(ctx).getProperty("virtual");
			return Long.valueOf(value);
		} catch (Exception e) {
			logger.log(Level.FINE, "invalid virtual stock, setting to zero...");
			setVirtualStock(ctx, 0);
			return 0;
		}
	}

	public void setRealStock(ContentContext ctx, long realStock) {
		try {
			getViewData(ctx).setProperty("stock", String.valueOf(realStock));
			storeViewData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setVirtualStock(ContentContext ctx, long virtualStock) {
		try {
			getViewData(ctx).put("virtual", String.valueOf(virtualStock));
			storeViewData(ctx);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);

		List<String> fields = getFields(ctx);

		out.println("<div class=\"edit\" style=\"padding: 3px;\">");
		
		for (String field : fields) {
			renderField(ctx, out, field, getRowSize(field), getFieldValue(field));
		}
		renderField(ctx, out, "stock", 1, getRealStock(ctx));
		renderField(ctx, out, "virtual", 1, getVirtualStock(ctx));

		out.println("</div>");

		out.flush();
		out.close();
		return writer.toString();
	}
	private void renderField(ContentContext ctx, PrintWriter out, String field, int rowSize, Object value) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		String fieldId = createKeyWithField(field);
		
		out.println("<div class=\"field-label\">");
		out.println("<label for=\"" + fieldId + "\">" + i18nAccess.getText("field." + field) + "</label>");
		out.println("</div>");
		out.println("<div class=\"field-input\">");
		out.print("<textarea rows=\"" + rowSize + "\" id=\"" + fieldId + "\" name=\"" + fieldId + "\">");
		out.print(String.valueOf(value));
		out.println("</textarea>");
		out.println("</div>");
	}

	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		super.performEdit(ctx);
		
		RequestService requestService = RequestService.getInstance(ctx.getRequest());

		String stockValue = requestService.getParameter(createKeyWithField("stock"), null);
		try {
			long stock = Long.valueOf(stockValue);
			if (stock != getRealStock(ctx)) {
				setRealStock(ctx, stock);
			}
		} catch (Exception e) {
		}
		String virtualValue = requestService.getParameter(createKeyWithField("virtual"), null);
		try {
			long virtual = Long.valueOf(virtualValue);
			if (virtual != getVirtualStock(ctx)) {
				setVirtualStock(ctx, virtual);
			}
		} catch (Exception e) {
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		if (getOffset() > 0) {
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			PrintStream out = new PrintStream(outStream);

			out.println("<form class=\"add-basket\" id=\"product-"+getName()+"_"+getId()+"\" action=\""+URLHelper.createURL(ctx)+"\">");
			out.println("<input type=\"hidden\" name=\"webaction\" value=\"ecom.buy\" />");
			out.println("<input type=\"hidden\" name=\"cid\" value=\""+getId()+"\" />");
			I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
			
			out.println("<div class=\"line name\">");		
			out.println("<span>"+getName()+"</span>");
			out.println("</div>");
			
			out.println("<div class=\"line price\">");
			out.println("<span class=\"label\">" + i18nAccess.getViewText("ecom.price") + "</span> <span>"+getPrice() + "&nbsp;" + getCurrency() + "</span>");
			out.println("</div>");
			
			out.println("<div class=\"line production\">");
			out.println("<span class=\"label\">" + i18nAccess.getViewText("ecom.production") + "</span> <span>" + getProduction() + "</span>");
			out.println("</div>");

			out.println("<div class=\"line stock\">");
			if (getVirtualStock(ctx) > 0) {
				out.println("<span class=\"label\">" + i18nAccess.getViewText("ecom.stock") + "</span> <span>" + getVirtualStock(ctx) + "</span>");
			} else {
				out.println("<span class=\"label\">" + i18nAccess.getViewText("ecom.stock") + "</span> <span>SOLD OUT</span>");
			}
			out.println("</div>");

			if (getVirtualStock(ctx) > getOffset()) {
				out.println("<div class=\"line quantity\">");
				String Qid = "product-"+StringHelper.getRandomId();
				out.println("<label for=\""+Qid+"\"><span>"+i18nAccess.getViewText("ecom.quantity")+"</span></label>");
				out.println("<input class=\"digit\" id=\""+Qid+"\" type=\"text\" name=\"quantity\" value=\"" + getOffset() + "\" maxlength=\"3\"/>");

				out.println("<span class=\"buy\"><input class=\"buy\" type=\"submit\" name=\"buy\" value=\""+i18nAccess.getViewText("ecom.buy")+"\" /></span>");
				out.println("</div>");
			}
			out.println("</form>");
			
			out.close();
			return new String(outStream.toByteArray());
		} else {
			return "";
		}
	}
	
	@Override
	public String getHexColor() {
		return ECOM_COLOR;
	}
}
