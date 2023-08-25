package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

public class TaxonomyDisplayComponent extends AbstractVisualComponent {

	public static final String TYPE = "taxonomy-display";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		Set<String> taxonomy = ctx.getCurrentPage().getTaxonomy();
		if (taxonomy != null && taxonomy.size() > 0) {
			out.println("<div class=\"taxonomy\">");
			if (!StringHelper.isEmpty(getValue())) {
				out.println("<span class=\"prefix\">" + Encode.forHtmlContent(getValue()) + "</span>");
			}
			int i=1;

			TaxonomyService ts = TaxonomyService.getInstance(ctx);

			for (TaxonomyDisplayBean bean : TaxonomyDisplayBean.convert(ctx, ctx.getGlobalContext().getAllTaxonomy(ctx).convert(taxonomy))) {

				ts.setImage(ctx, bean.getBean());

				String image = "";
				if (bean.getImage() != null) {
						image += "<span class=\"image\"><img style=\"max-width: 100px\" src=\""+bean.getImage()+"\" /></span>";
				}
				out.println("<span class=\"item-"+i+" label label-default badge badge-secondary bg-secondary name-" + Encode.forHtmlAttribute(bean.getName()) + "\">" + Encode.forHtmlContent(StringHelper.neverEmpty(bean.getLabel(), bean.getName())) + image + "</span>");
				i++;
			}
			out.println("</div>");
		} else {
			out.println("<!-- no taxonomy found on page : "+ctx.getCurrentPage().getName()+" !-->");
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		return false;
	}

	@Override
	public boolean isDisplayable(ContentContext ctx) throws Exception {
		Set<String> taxonomy = ctx.getCurrentPage().getTaxonomy();
		if (taxonomy != null && taxonomy.size() > 0) {
			return true;
		} else {
			return super.isDisplayable(ctx);
		}
	}

	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}
	
	@Override
	public String getFontAwesome() {	
		return "sitemap";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

}