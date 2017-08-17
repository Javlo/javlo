package org.javlo.component.meta;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.helper.StringHelper;
import org.owasp.encoder.Encode;

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
			for (TaxonomyDisplayBean bean : TaxonomyDisplayBean.convert(ctx, ctx.getGlobalContext().getTaxonomy().convert(taxonomy))) {
				out.println("<span class=\"label label-default name-" + Encode.forHtmlAttribute(bean.getName()) + "\">" + Encode.forHtmlContent(bean.getLabel()) + "</span>");
			}
			out.println("</div>");
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
	public boolean isContentCachable(ContentContext ctx) {
		return true;
	}

	@Override
	public boolean isDefaultValue(ContentContext ctx) {
		return false;
	}

}