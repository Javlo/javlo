package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Locale;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.fields.Field;
import org.javlo.fields.IFieldContainer;
import org.javlo.helper.XHTMLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.service.ContentService;
import org.javlo.service.DynamicComponentService;

public class DynamicComponentFilter extends AbstractPropertiesComponent {

	public static final String TYPE = "dynamic-component-filter";

	private Boolean realContent = null;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<String> container = service.getAllType(ctx, content.getNavigation(ctx));

		out.println(XHTMLHelper.getInputOneSelect(createKeyWithField("type"), container, getSelectedType()));

		out.close();
		return new String(outStream.toByteArray());
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {

		realContent = false;

		IFieldContainer fieldContainer = getFieldContainer(ctx);
		if (fieldContainer == null) {
			return "";
		}

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		List<IFieldContainer> containers = service.getFieldContainers(ctx, content.getNavigation(ctx), getSelectedType());

		for (IFieldContainer container : containers) {
			boolean display = true;
			List<Field> fields = container.getFields(ctx);
			for (Field field : fields) {
				if (!fieldMatch(ctx, field.getName(), field.getValue(new Locale(ctx.getRequestContentLanguage())))) {
					display = false;
				}
			}
			if (display) {
				realContent = true;
				out.println("<div class=\"dynamic-component\">");
				out.println(container.getViewListXHTMLCode(ctx));
				out.println("</div>");
			}
		}

		out.close();
		return new String(outStream.toByteArray());

	}

	private boolean fieldMatch(ContentContext ctx, String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String getSelectedType() {
		return properties.getProperty("type");
	}

	IFieldContainer getFieldContainer(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		DynamicComponentService service = DynamicComponentService.getInstance(globalContext);

		ContentService content = ContentService.getInstance(ctx.getRequest());
		List<IFieldContainer> containers = service.getFieldContainers(ctx, content.getNavigation(ctx), getSelectedType());
		if (containers.size() > 0) {
			return containers.iterator().next();
		} else {
			return null;
		}
	}

	@Override
	public String getHeader() {
		return getType();
	}

	@Override
	public boolean isRealContent(ContentContext ctx) {
		if (realContent == null) {
			try {
				getViewXHTMLCode(ctx);
			} catch (Exception e) {
				realContent = false;
				e.printStackTrace();
			}
		}
		return realContent;
	}

	@Override
	public boolean isContentCachable(ContentContext ctx) {
		try {
			IFieldContainer containers = getFieldContainer(ctx);
			if (containers == null) {
				return false;
			}
			List<Field> fields = containers.getFields(ctx);
			for (Field field : fields) {
				if (ctx.getRequest().getParameter(field.getName()) != null) {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String getHexColor() {
		return DEFAULT_COLOR;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
