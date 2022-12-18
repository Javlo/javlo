package org.javlo.component.container;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.service.ContentService;

public class DisplayComponent extends AbstractVisualComponent {

	public static final String TYPE = "display-component";

	@Override
	public String getType() {
		return TYPE;
	}
	
	@Override
	protected boolean getColumnableDefaultValue() {
		return true;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		if (StringHelper.isDigit(getValue())) {
			IContentVisualComponent comp = ContentService.getInstance(ctx.getGlobalContext()).getComponent(ctx, getValue());
			if (comp != null) {
				return renderOtherComponent(ctx, comp);
			} else {
				System.out.println(">>>>>>>>> DisplayComponent.getViewXHTMLCode : comp = "+comp); //TODO: remove debug trace
				//return ComponentHelper.renderComponent(ctx, comp);
				return "";
			}
		}
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public String performEdit(ContentContext ctx) throws Exception {
		this.performColumnable(ctx);
		return super.performEdit(ctx);
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}

}
