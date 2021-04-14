/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.text;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.javlo.component.core.ComponentBean;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.CSSParser;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.XHTMLHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import io.bit3.jsass.CompilationException;

/**
 * @author pvandermaesen
 */
public class ExtendedWidget extends AbstractPropertiesComponent {
	
	private List<String> FIELDS = Arrays.asList(new String[] {"xhtml", "css", "file"});

	public static final String TYPE = "extendedWidget";

	public static final String XHTML_RESOURCE_FOLDER = "_xhtml_resources";

	private Boolean cachable = null;
	
	@Override
	public void init(ComponentBean bean, ContentContext ctx) throws Exception {	
		super.init(bean, ctx);
		File renderer = getRendererFile(ctx);
		if (!renderer.exists()) {
			renderer.getParentFile().mkdirs();
			createRenderer(ctx);
		}
	}
	
	public void createRenderer(ContentContext ctx) throws Exception {
		File renderer = getRendererFile(ctx);
		final String filePrefix = "<%@ taglib uri=\"http://java.sun.com/jsp/jstl/core\" prefix=\"c\"%><%@ taglib prefix=\"fn\" uri=\"http://java.sun.com/jsp/jstl/functions\"%><%@ taglib uri=\"/WEB-INF/javlo.tld\" prefix=\"jv\"%>";
		String css = getFieldValue("css");
		String style = "";
		if (css != null && css.contains("{")) {
			try {
				style = "<style>"+CSSParser.prefixAllQueries('.'+getSpecificCssClass(ctx), getFieldValue("css"))+"</style>";
			} catch (CompilationException e) {
				String randomId = StringHelper.getRandomId();
				logger.severe("ERROR "+randomId+" : "+e.getMessage());
				e.printStackTrace();
				style = "<div class=\"alert alert-danger\">error ["+randomId+"] : "+e.getMessage()+"</div>";
			}
		}
		String xhtml = getFieldValue("xhtml");
		String errorMsg = "<strong>Error : NO SCRIPLET</strong>";
		xhtml = xhtml.replace("<%", errorMsg);
		xhtml = xhtml.replace(errorMsg + '@', "<%@");
		xhtml = XHTMLHelper.replaceLinks(ctx, xhtml);
		ResourceHelper.writeStringToFile(renderer, filePrefix+style+xhtml);
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		File file = getRendererFile(ctx);
		if (!file.exists()) {
			file.createNewFile();
			createRenderer(ctx);
		}
	}
	
	@Override
	public void delete(ContentContext ctx) {
		super.delete(ctx);
		getRendererFile(ctx).delete();
	}
	
	private File getRendererFile(ContentContext ctx) {
		return new File(ctx.getRequest().getSession().getServletContext().getRealPath(getRenderer(ctx)));
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return getConfig(ctx).getComplexity(COMPLEXITY_STANDARD);
	}

	@Override
	public String getEditRenderer(ContentContext ctx) {
		return "/jsp/edit/component/extendedWidget/edit_extendedWidget.jsp";
	}
	
	@Override
	public String getRenderer(ContentContext ctx) {
		return "/jsp/view/component/"+TYPE+"/view_"+getId()+".jsp";
	}

	@Override
	public String getHexColor() {
		return GRAPHIC_COLOR;
	}
	
	@Override
	public String getSpecificCssClass(ContentContext ctx) {
		return "css-wrp-"+getId();
	}

	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		String msg = super.performEdit(ctx);
		if (isModify()) {
			createRenderer(ctx);
		}
		cachable=null;
		return msg;
	}

	/*
	 * @Override public String getPrefixViewXHTMLCode(ContentContext ctx) {
	 * return ""; }
	 * 
	 * @Override public String getSuffixViewXHTMLCode(ContentContext ctx) {
	 * return ""; }
	 */

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public String getHeaderContent(ContentContext ctx) {
		String xhtml = getValue();
		if (xhtml.toLowerCase().contains("<head")) {
			Document doc = Jsoup.parse(xhtml);
			Elements head = doc.select("head");
			return head.html();
		} else {
			return null;
		}
	}


//	@Override
//	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
//		String xhtml = getValue();
//		if (xhtml.toLowerCase().contains("<body")) {
//			Document doc = Jsoup.parse(xhtml);
//			Elements body = doc.select("body");
//			xhtml = body.html();
//		}
//		return XHTMLHelper.replaceLinks(ctx, XHTMLHelper.replaceJSTLData(ctx, xhtml));
//	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		if (cachable == null) {
			cachable = !getValue().contains("${");				
		}
		return cachable;
	}
	
	@Override
	public boolean isRealContent(ContentContext ctx) {
		return !StringHelper.isEmpty(getValue());
	}
	
	@Override
	protected boolean isXML() {
		return true;
	}
	
	@Override
	public String getFontAwesome() {	
		return "code";
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	
}
