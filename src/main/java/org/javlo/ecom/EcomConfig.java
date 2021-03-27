package org.javlo.ecom;

import org.javlo.context.GlobalContext;
import org.javlo.context.SpecialConfigBean;
import org.javlo.helper.StringHelper;

public class EcomConfig {
	
	private SpecialConfigBean specialConfig;
	
	public EcomConfig(GlobalContext globalContext) {
		this.specialConfig = globalContext.getSpecialConfig();
	}
	
	public boolean isStock() {
		return StringHelper.isTrue(specialConfig.getMap().get("ecom.stock"));
	}
	
	public boolean isDeliveryuDate() {
		return StringHelper.isTrue(specialConfig.getMap().get("ecom.delivery.date"));
	}

}
