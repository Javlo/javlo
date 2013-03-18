package org.javlo.component.links;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;

/**
 * Links for change language of the current page. <h4>JSTL variable :</h4>
 * <ul>
 * <li>inherited from {@link AbstractVisualComponent}</li>
 * <li>{@link LanguageBean} languagesList : list of languages.</li>
 * <li>boolean pages : is a list of content language, false if the list if navigation language.</li>
 * </ul>
 * 
 * @author pvandermaesen
 */
public class ChangeLanguageMenu extends AbstractVisualComponent {

	private static final String CONTENT_LANGUAGES_STYLE = "contentLanguages";

	public static class LanguageBean {
		private String language = null;
		private String label = null;
		private String translatedLabel = null;
		private String url = null;
		private boolean realContent = true;

		public String getLanguage() {
			return language;
		}

		public void setLanguage(String language) {
			this.language = language;
		}

		public String getLabel() {
			return label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getTranslatedLabel() {
			return translatedLabel;
		}

		public void setTranslatedLabel(String translatedLabel) {
			this.translatedLabel = translatedLabel;
		}

		public boolean isRealContent() {
			return realContent;
		}

		public void setRealContent(boolean realContent) {
			this.realContent = realContent;
		}
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		return "";
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSuffixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	public static final String TYPE = "change-language";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getRenderer(ContentContext ctx) {
		if (getConfig(ctx).getRenderes().size() > 0) {
			return getConfig(ctx).getRenderes().values().iterator().next();
		}
		return "languages.jsp";
	}

	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		Collection<LanguageBean> outLanguages = new LinkedList<LanguageBean>();
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		Collection<String> languages;
		boolean contentLanguages = isContentLanguages(ctx);
		if (contentLanguages) {
			languages = globalContext.getContentLanguages();
		} else {
			languages = globalContext.getLanguages();
		}
		ContentContext lgCtx = new ContentContext(ctx);
		for (String lg : languages) {
			lgCtx.setRequestContentLanguage(lg);
			lgCtx.setContentLanguage(lg);
			if (!contentLanguages) {
				lgCtx.setLanguage(lg);
			}
			String label = (new Locale(lg)).getDisplayLanguage(new Locale(lg));
			String translatedLabel = (new Locale(lg)).getDisplayLanguage(new Locale(ctx.getLanguage()));
			String url = URLHelper.createURL(lgCtx);
			LanguageBean bean = new LanguageBean();
			bean.setLanguage(lg);
			bean.setLabel(label);
			bean.setTranslatedLabel(translatedLabel);
			bean.setUrl(url);
			// Content content = Content.createContent(lgCtx.getRequest());
			// MenuElement currentPage = content.getNavigation(lgCtx).getCurrentPage(lgCtx);
			bean.setRealContent(lgCtx.getCurrentPage().isRealContent(lgCtx));
			outLanguages.add(bean);
		}
		ctx.getRequest().setAttribute("languagesList", outLanguages);
		ctx.getRequest().setAttribute("isContentLanguages", contentLanguages);
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isContentTimeCachable(ContentContext ctx) {
		return false;
	}

	public boolean isContentLanguages(ContentContext ctx) {
		return CONTENT_LANGUAGES_STYLE.equals(getStyle(ctx));
	}

	@Override
	public String getHexColor() {
		return IContentVisualComponent.LINK_COLOR;
	}

	@Override
	public String[] getStyleList(ContentContext ctx) {
		return new String[] { "languages", CONTENT_LANGUAGES_STYLE };
	}

	@Override
	public int getComplexityLevel() {
		return AbstractVisualComponent.COMPLEXITY_STANDARD;
	}

}
