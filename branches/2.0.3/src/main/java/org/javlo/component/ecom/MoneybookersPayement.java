package org.javlo.component.ecom;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;

public class MoneybookersPayement extends AbstractVisualComponent {

	public static final String TYPE = "moneybookers-payement";
	
	public static class PayementDetails {
		private int amount = 0;
		private String currency = "EUR";
		private String description = "";
		private String text = "";
		private final String id = StringHelper.getRandomString(30);
		
		public int getAmount() {
			return amount;
		}
		public void setAmount(int amount) {
			this.amount = amount;
		}
		public String getCurrency() {
			return currency;
		}
		public void setCurrency(String currency) {
			this.currency = currency;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getId() {
			return id;
		}
		
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {	
		super.prepareView(ctx);
		
		PayementDetails pd = new PayementDetails();
		pd.setAmount(1);
		pd.setDescription("test - description");
		pd.setText("test - text");
		
		ctx.getRequest().setAttribute("payementDetails", pd);
		
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

}
