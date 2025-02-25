package org.javlo.context;

import org.javlo.config.StaticConfig;
import org.javlo.helper.StringHelper;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SpecialConfigBean {
	
	public static final String GOOGLE_RECAPTHCA_PARAM_NAME = "g-recaptcha-response"; 
	
	private String mainCdn = null;

	private Map config;
	private StaticConfig staticConfig = null;

	private Boolean trackingAccess = null;

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

	public Boolean isProd() {
		if (config.get("prod") == null) {
			return null;
		} else {
			return StringHelper.isTrue(config.get("prod"));
		}
	}

	/**
	 * track access to resource and page. Use for display the most readed picture or
	 * page. false by default
	 * 
	 * @return
	 */
	public boolean isTrackingAccess() {
		if (trackingAccess  == null) {
			trackingAccess = StringHelper.isTrue(config.get("tracking.access"));
		}
		return trackingAccess;
	}

	public boolean isNeedLogForPreview() {
		return StringHelper.isTrue(config.get("security.need-log-for-preview"));
	}
	
	public boolean isContentAddTitle() {
		return StringHelper.isTrue(config.get("content.add-title"), false);
	}

	public boolean isSitemapResources() {
		return StringHelper.isTrue(config.get("sitemap.resources"), true);
	}

	public boolean isCreateAccountWithToken() {
		return StringHelper.isTrue(config.get("security.account.token"), false);
	}
	
	public boolean isStorageZipped() {
		if (isSecureEncrypt()) {
			return true;
		}
		boolean out = StringHelper.isTrue(config.get("persistence.zip"), staticConfig.isStorageZipped());
		return out;
	}
	
	public boolean isSecureEncrypt() {
		boolean out = StringHelper.isTrue(config.get("security.encrypt"), staticConfig.isSecureEncrypt());
		if (out) {
			out = getSecureEncryptPassword() != null;
		}
		return out;
	}


	public Collection<String> getSecureHeaderLoginKey() {
		String keyRaw = (String)config.get("security.auto-login.header.keys");
		if (keyRaw == null || StringHelper.isEmpty(keyRaw)) {
			return null;
		} else {
			return StringHelper.stringToCollection(keyRaw,",");
		}
	}
	
	public String getSecureEncryptPassword() {
		if (!StringHelper.isTrue(config.get("security.encrypt"), false)) {
			return null;
		};
		String pwd = (String)config.get("security.encrypt.password");
		if (pwd == null) {
			pwd = System.getenv("JAVLO_ENCRYPT_KEY"); 
		}
		return pwd;
	}
	
	public String getMainCdn() {
		if (mainCdn == null) {
			mainCdn = (String)config.get("cdn.1");
			if (mainCdn==null) {
				mainCdn = "0";
			}
		}
		if (mainCdn.length()==1) {
			return null;
		} else {
			return mainCdn;
		}
	}
	
	public List<String> getCdnRefreshUrl() {
		String refreshCdh = (String)config.get("cdn.refresh");
		return StringHelper.stringToCollection(refreshCdh, ",", true);
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
	
	public String getMailingRole() {
		return StringHelper.neverEmpty((String)config.get("mailing.role"), "mailing");
	}
	
	public String getTranslatorGoogleApiKey() {
		return StringHelper.neverNull(config.get("translator.google.api.key"));
	}
	
	public String getTranslatorDeepLApiKey() {
		return StringHelper.neverNull(config.get("translator.deepl.api.key"));
	}
	
	public String getMailFrom() {
		return StringHelper.neverNull(config.get("mail.from"));
	}
	public String getSeoCountry() {
		return StringHelper.neverNull(config.get("seo.country"));
	}
	
	public boolean isGoogleRecaptcha() {
		return !StringHelper.isEmpty(getGoogleRecaptchaPrivateKey()) && !StringHelper.isEmpty(getGoogleRecaptchaPublicKey());
	}
	
	public String getGoogleRecaptchaPrivateKey() {
		return (String)config.get("google-recaptcha.private-key");
	}
	
	public String getGoogleRecaptchaPublicKey() {
		return (String)config.get("google-recaptcha.public-key");
	}
	
	public String get(String key, String defaultValue) {
		String value = (String)config.get(key);
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}

	public boolean isTaxonomyUnderlineActive() {
		return StringHelper.isTrue(config.get("taxonomy.underline-active"), false);
	}

	public boolean isMsgOnForward() {
		return StringHelper.isTrue(config.get("no-content.msg-on-forward"), true);
	}
	
}
