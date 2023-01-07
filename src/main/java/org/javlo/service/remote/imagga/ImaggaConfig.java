package org.javlo.service.remote.imagga;

import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class ImaggaConfig {

	private GlobalContext globalContext;

	public ImaggaConfig(GlobalContext globalContext) {
		this.globalContext = globalContext;
	}

	public static final ImaggaConfig getInstance(GlobalContext globalContext) {
		return new ImaggaConfig(globalContext);
	}

	public String getApiKey() {
		return StringHelper.trim(globalContext.getSpecialConfig().get("immaga.key.api", null));
	}

	public String getApiSecret() {
		return StringHelper.trim(globalContext.getSpecialConfig().get("immaga.secret", null));
	}

	public boolean isActive() {
		return !StringHelper.isOneEmpty(getApiKey(), getApiSecret());
	}

	public boolean isTag() {
		if (!isActive()) {
			return false;
		} else {
			return StringHelper.isTrue(globalContext.getSpecialConfig().get("immaga.tags", null), true);
		}
	}
	
	public boolean isFocus() {
		if (!isActive()) {
			return false;
		} else {
			return StringHelper.isTrue(globalContext.getSpecialConfig().get("immaga.focus", null), true);
		}
	}

}
