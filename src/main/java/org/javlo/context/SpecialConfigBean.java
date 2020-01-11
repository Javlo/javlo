package org.javlo.context;

import java.util.Map;

import org.javlo.config.StaticConfig;
import org.javlo.helper.MapHelper;
import org.javlo.helper.StringHelper;

public class SpecialConfigBean {

	private Map config;
	private StaticConfig staticConfig = null;

	public SpecialConfigBean(Map config, StaticConfig staticConfig) {
		this.config = config;
		this.staticConfig = staticConfig;
	}

	public Map getMap() {
		return config;
	}

	public boolean isPasteAsMirror() {
		return StringHelper.isTrue(config.get("content.pasteasmirror"));
	}

	/**
	 * track access to resource and page. Use for display the most readed picture or
	 * page. false by default
	 * 
	 * @return
	 */
	public boolean isTrackingAccess() {
		return StringHelper.isTrue(config.get("tracking.access"));
	}

	public boolean isNeedLogForPreview() {
		return StringHelper.isTrue(config.get("security.need-log-for-preview"));
	}

	public boolean isSitemapResources() {
		return StringHelper.isTrue(config.get("sitemap.resources"), true);
	}

	public boolean isCreateAccountWithToken() {
		return StringHelper.isTrue(config.get("security.account.token"), false);
	}
	
	public String getSearchPageName() {
			return StringHelper.neverEmpty((String)config.get("page.search"), staticConfig.getSearchPageName());
	}
	
	public String getRegisterPageName() {
		return StringHelper.neverEmpty((String)config.get("page.register"), staticConfig.getRegisterPageName());
	}
	
	public String getLoginPageName() {
		return StringHelper.neverEmpty((String)config.get("page.login"), staticConfig.getLoginPageName());
	}
	
	public String getNewsPageName() {
		return StringHelper.neverEmpty((String)config.get("page.news"), staticConfig.getNewsPageName());
	}

}
