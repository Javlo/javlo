package org.javlo.component.web2;

import org.javlo.actions.IAction;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.data.taxonomy.TaxonomyBean;
import org.javlo.data.taxonomy.TaxonomyDisplayBean;
import org.javlo.data.taxonomy.TaxonomyService;
import org.javlo.helper.ComponentHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.helper.XHTMLBootstrapFormBuilder;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.RequestService;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

public class TaxonomySessionComponent extends AbstractPropertiesComponent implements IAction {
	
	private static final String LABEL = "label";

	private static final String TAXONOMY = "taxonomy";

	public static final List<String> FIELDS = new LinkedList<String> (Arrays.asList(new String[] { LABEL, TAXONOMY } ) );
	
	public static final String TYPE = "taxonomy-session";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	public boolean isListable() {
		return true;
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx);
		out.println(XHTMLBootstrapFormBuilder.renderSimpleText(i18nAccess.getViewText("global.label"), getInputName(LABEL), getFieldValue(LABEL), null));
		if (ctx.getGlobalContext().getAllTaxonomy(ctx).isActive()) {
			String taxoName = getInputName(TAXONOMY);
			out.println("<fieldset class=\"taxonomy\"><legend><label for=\"" + taxoName + "\">" + i18nAccess.getText(TAXONOMY) + "</label></legend>");
			out.println(ctx.getGlobalContext().getAllTaxonomy(ctx).getSelectHtml(taxoName, "form-control chosen-select", StringHelper.stringToCollection(getFieldValue(TAXONOMY),","), false, ctx.getGlobalContext().getSpecialConfig().isTaxonomyUnderlineActive()));
			out.println("</fieldset>");
		} else {
			out.println("<div class=\"alert alert-danger error\">no taxonomy found.</div>");
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	private String getInputName() {
		return "taxo-"+getId();
	}
	
	@Override
	public void prepareView(ContentContext ctx) throws Exception {
		super.prepareView(ctx);
		List<Map.Entry<String,TaxonomyDisplayBean>> values = new LinkedList<Map.Entry<String,TaxonomyDisplayBean>>();
		TaxonomyService taxoService = TaxonomyService.getInstance(ctx);
		TaxonomyBean taxoBean = taxoService.getTaxonomyBean(getFieldValue(TAXONOMY), true);
		if (taxoBean != null) {
			for (TaxonomyBean bean : taxoBean.getAllChildren()) {
				values.add(new AbstractMap.SimpleEntry(bean.getId(), new TaxonomyDisplayBean(ctx, bean)));
			}
			Collections.sort(values, new Comparator<Map.Entry<String,TaxonomyDisplayBean>>() {
				@Override
				public int compare(Entry<String, TaxonomyDisplayBean> o1, Entry<String, TaxonomyDisplayBean> o2) {
					return o1.getValue().getLabel().compareTo(o2.getValue().getLabel());
				}
			});
			ctx.getRequest().setAttribute("value", TaxonomyService.getSessionFilter(ctx, getId()));
			ctx.getRequest().setAttribute("label", getFieldValue(LABEL));
			ctx.getRequest().setAttribute("values", values);
		} else {
			logger.warning("taxonomy not found : "+getFieldValue(TAXONOMY));
		}
		ctx.getRequest().setAttribute("inputName", getInputName());
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		TaxonomyService taxoService = TaxonomyService.getInstance(ctx);
		TaxonomyBean taxoBean = taxoService.getTaxonomyBean(getFieldValue(TAXONOMY), true);
		if (taxoBean == null) {
			if (ctx.isAsPreviewMode()) {
				return "<div class=\"error\">taxonomy node not found : "+getFieldValue(TAXONOMY)+"</div>";
			} else {
				return "";
			}
		}
		List<Map.Entry<String,String>> values = new LinkedList<Map.Entry<String,String>>();
		for (TaxonomyBean bean : taxoBean.getAllChildren()) {
			values.add(new AbstractMap.SimpleEntry(bean.getId(), bean.getLabels().get(ctx.getRequestContentLanguage())));
		}
		Collections.sort(values, new Comparator<Map.Entry<String,String>>() {
			@Override
			public int compare(Entry<String, String> o1, Entry<String, String> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}
			
		});
		values.add(0, new AbstractMap.SimpleEntry("", getFieldValue(LABEL)));
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<form id=\"form-taxo-"+getId()+"\" action=\""+URLHelper.createURL(ctx)+"\" method=\"post\">");
		out.println("<input type=\"hidden\" name=\"webaction\" value=\""+TYPE+".choose\" />");
		out.println("<input type=\"hidden\" name=\""+COMP_ID_REQUEST_PARAM+"\" value=\""+getId()+"\" />");
		out.println(XHTMLBootstrapFormBuilder.renderSelect(getFieldValue(LABEL), getInputName(), values, TaxonomyService.getSessionFilter(ctx, getId()), true));
		out.println("</form>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	public static final String performChoose(ContentContext ctx, RequestService rs) throws Exception {
		TaxonomySessionComponent comp = (TaxonomySessionComponent)ComponentHelper.getComponentFromRequest(ctx);
		String newTaxo = rs.getParameter(comp.getInputName());
		String currentChoose = TaxonomyService.getSessionFilter(ctx, comp.getId());
		if (currentChoose != null && currentChoose.equals(newTaxo)) {
			newTaxo = null;
		}
		TaxonomyService.setSessionFilter(ctx, comp.getId(), newTaxo);
		return null;
	}
	
	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	@Override
	public String getFontAwesome() {
		return "tags";
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}
	
	private String getCurrentTaxonomy(ContentContext ctx) {
		return TaxonomyService.getSessionFilter(ctx, getId());
	}

	@Override
	public String getActionGroupName() {
		return getType();
	}

}
