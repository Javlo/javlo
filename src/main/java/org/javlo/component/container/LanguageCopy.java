package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.navigation.MenuElement;

public class LanguageCopy extends AbstractPropertiesComponent {

	public static final String TYPE = "language-component-copy";

	private static final String REF_LG_FIELD = "ref-lg";

	private static final String REF_TYPE_FIELD = "ref-type";

	@Override
	public String getType() {
		return TYPE;
	}

	static final List<String> FIELDS = Arrays.asList(new String[] { REF_LG_FIELD, REF_TYPE_FIELD });

	@Override
	public List<String> getFields(ContentContext ctx) {
		return FIELDS;
	}

	@Override
	public String getHeader() {
		return getType();
	}

	public String getLgInputName() {
		return createKeyWithField(REF_LG_FIELD);
	}

	@Override
	public String getTypeInputName() {
		return createKeyWithField(REF_TYPE_FIELD);
	}

	public String getRefLanguage(ContentContext ctx) {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return getFieldValue(REF_LG_FIELD, globalContext.getDefaultLanguage());
	}

	public String getRefType() {
		return getFieldValue(REF_TYPE_FIELD, "");
	}

	private Collection<IContentVisualComponent> getComponents(ContentContext ctx) {
		ContentContext refContext = new ContentContext(ctx);
		refContext.setRequestContentLanguage(getRefLanguage(ctx));
		ContentElementList refContent;
		try {
			refContent = ctx.getCurrentPage().getContent(refContext);

			Collection<IContentVisualComponent> outComps = new LinkedList<IContentVisualComponent>();
			while (refContent.hasNext(refContext)) {
				IContentVisualComponent comp = refContent.next(refContext);
				if (comp.getType().equals(getRefType())) {
					outComps.add(comp);
				}
			}
			return outComps;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		ContentContext refContext = new ContentContext(ctx);
		refContext.setRequestContentLanguage(getRefLanguage(ctx));
		Collection<IContentVisualComponent> comps = getComponents(refContext);
		for (IContentVisualComponent comp : comps) {
			out.println(comp.getPrefixViewXHTMLCode(refContext));
			out.println(comp.getXHTMLCode(refContext));
			out.println(comp.getSufixViewXHTMLCode(refContext));
		}
		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getPrefixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	public String getSufixViewXHTMLCode(ContentContext ctx) {
		return "";
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		out.println("<div class=\"line\">");
		out.print("<label for=\"" + getLgInputName() + "\">");
		out.print(i18nAccess.getText("global.language") + " : ");
		out.println("</label>");
		out.println(XHTMLHelper.renderOnlySelectLangue(ctx, getLgInputName(), getLgInputName(), getRefLanguage(ctx), false));
		out.println("</div>");

		out.println("<div class=\"line\">");
		out.print("<label for=\"" + getTypeInputName() + "\">");
		out.print(i18nAccess.getText("global.component-type") + " : ");
		out.println("</label>");

		Collection<String> compTypeList = new LinkedList<String>();
		Collection<IContentVisualComponent> componentList = new LinkedList<IContentVisualComponent>();
		ContentContext lgCtx = new ContentContext(ctx);
		lgCtx.setRequestContentLanguage(getRefLanguage(ctx));
		MenuElement currentPage = ctx.getCurrentPage();
		ContentElementList content = currentPage.getContent(lgCtx);
		while (content.hasNext(lgCtx)) {
			IContentVisualComponent comp = content.next(lgCtx);
			if (!compTypeList.contains(comp.getType())) {
				compTypeList.add(comp.getType());
				componentList.add(comp);
			}
		}
		String[][] compsLabel = new String[compTypeList.size()][];
		int i = 0;
		for (IContentVisualComponent comp : componentList) {
			compsLabel[i] = new String[2];
			compsLabel[i][0] = comp.getType();
			// compsLabel[i][1] = i18nAccess.getText("content." + comp.getType(), comp.getType());
			compsLabel[i][1] = comp.getComponentLabel(ctx, globalContext.getEditLanguage(ctx.getRequest().getSession()));
			i++;
		}
		out.println(XHTMLHelper.getInputOneSelect(getTypeInputName(), compsLabel, getRefType()));
		out.println("</div>");

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public int getComplexityLevel() {
		return COMPLEXITY_ADMIN;
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		try {
			for (IContentVisualComponent comp : getComponents(ctx)) {
				if (comp.isRealContent(ctx)) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return false;
	}

	@Override
	public Collection<String> getExternalResources(ContentContext ctx) {
		Collection<IContentVisualComponent> comps = getComponents(ctx);
		if (comps.size() > 0) {
			return comps.iterator().next().getExternalResources(ctx);
		}
		return super.getExternalResources(ctx);
	}

}
