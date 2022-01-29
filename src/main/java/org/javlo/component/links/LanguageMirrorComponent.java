/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.links;

import java.util.Collection;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.ComponentBean;
import org.javlo.component.core.ISubTitle;
import org.javlo.component.image.IImageTitle;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.module.content.Edit;
import org.javlo.service.RequestService;

/**
 * display the list of component create in content area of a other page in place
 * of the mirrotComponent. use "copy page" for create the link.
 * 
 * @author pvandermaesen
 */
public class LanguageMirrorComponent extends AbstractVisualComponent implements IImageTitle, ISubTitle {

	public static final String TYPE = "lang-mirror-page";

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(LanguageMirrorComponent.class.getName());

	public String getCurrentInputName() {
		return "linked-comp-" + getId();
	}

	protected String getUnlinkAndCopyInputName() {
		return "unlink-copy-" + getId();
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<String> rsc = null;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				rsc = getPage().getExternalResources(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return rsc;
	}

	@Override
	public String getHexColor() {
		return LINK_COLOR;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		String lg = getValue();				
		if (ctx.getGlobalContext().getContentLanguages().contains(lg) && !ctx.getRequestContentLanguage().equals(lg)) {
			ctx.setVirtualCurrentPage(getPage()); 
			String saveLg = ctx.getRequestContentLanguage();
			ctx.setRequestContentLanguage(lg);						
			RequestService rs = RequestService.getInstance(ctx.getRequest());					
			rs.setParameter(NOT_EDIT_PREVIEW_PARAM_NAME, "true");
			rs.setParameter(CACHE_KEY_SUFFIX_PARAM_NAME, getPage().getId());
			String param = Edit.CONTENT_RENDERER + '?' + NOT_EDIT_PREVIEW_PARAM_NAME + "=true";
			String xhtml = executeJSP(ctx, param);
			rs.setParameter(NOT_EDIT_PREVIEW_PARAM_NAME, "false");
			ctx.setRequestContentLanguage(saveLg);
			ctx.getRequest().setAttribute("xhtml", xhtml);
		} else {
			ctx.getRequest().setAttribute("xhtml", "error bad language : "+getValue());
		}
		
		super.prepareView(ctx);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		setContainerPage(ctx, getPage());
		prepareView(ctx);
		return "<div lang=\""+getValue()+"\">"+ctx.getRequest().getAttribute("xhtml")+"</div>";
	}

	@Override
	public void init(ComponentBean bean, ContentContext newContext) throws Exception {
		super.init(bean, newContext);
	}

	@Override
	public boolean isList(ContentContext ctx) {
		return false;
	}

	@Override
	public int getLabelLevel(ContentContext ctx) {
		if (!StringHelper.isEmpty(getValue())) {
			return LOW_LABEL_LEVEL;
		} else {
			return 0;
		}
	}
		
	@Override
	public String getTextTitle(ContentContext ctx) {	
		String title = null;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				title = getPage().getTitle(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return title;
	}
	
	@Override
	public String getTextLabel(ContentContext ctx) {
		String svLg = ctx.getRequestContentLanguage();
		ctx.setRequestContentLanguage(getValue());
		String label = null;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				label = getPage().getLabel(ctx);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ctx.setRequestContentLanguage(svLg);
		return label;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {		
		boolean rc = false;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				rc = getPage().isRealContent(ctx);			
				ctx.setRequestContentLanguage(svLg);		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rc;
	}
	
	@Override
	public boolean isContentCachable(ContentContext ctx) {		
		boolean rc = false;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				rc = getPage().isCacheable(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rc;
	}

	@Override
	public int getPriority(ContentContext ctx) {
		int priority = 0;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				priority = getPage().getPriority();
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return priority;
	}

	@Override
	public String getSubTitle(ContentContext ctx) {		
		String title = null;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				title = getPage().getSubTitle(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return title;
	}

	@Override
	public int getSubTitleLevel(ContentContext ctx) {
		int level = 0;
		try {
			if (ctx.getGlobalContext().getContentLanguages().contains(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				level = getPage().getSubTitleLevel(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return level;
	}
	
	@Override
	public String getXHTMLId(ContentContext ctx) {
		return getType()+'-'+getId();
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}
	
	@Override
	public String getFontAwesome() {	
		return "clone";
	}
	
	private IImageTitle getImageTitle(ContentContext ctx) {
		
		IImageTitle image = null;
		try {			
			if (ctx.getGlobalContext().getContentLanguages().equals(getValue()) && !ctx.getRequestContentLanguage().equals(getValue())) {
				String svLg = ctx.getRequestContentLanguage();
				ctx.setRequestContentLanguage(getValue());
				image = getPage().getImage(ctx);
				ctx.setRequestContentLanguage(svLg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return image;
	}

	@Override
	public String getImageDescription(ContentContext ctx) {
		if (getImageTitle(ctx) != null) {
			return getImageTitle(ctx).getImageDescription(ctx);
		}
		return null;
	}

	@Override
	public String getResourceURL(ContentContext ctx) {
		if (getImageTitle(ctx) != null) {
			return getImageTitle(ctx).getResourceURL(ctx);
		}
		return null;
	}

	@Override
	public String getImageLinkURL(ContentContext ctx) {
		if (getImageTitle(ctx) != null) {
			return getImageTitle(ctx).getImageLinkURL(ctx);
		}
		return null;
	}

	@Override
	public boolean isImageValid(ContentContext ctx) {
		if (getImageTitle(ctx) != null) {
			return getImageTitle(ctx).isImageValid(ctx);
		}
		return false;
	}
	
	@Override
	public boolean isMobileOnly(ContentContext ctx) {
		return false;
	}
}
