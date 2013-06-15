/*
 * Created on 19-sept.-2003 
 */
package org.javlo.component.gadget;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

/**
 * @author pvandermaesen
 */
public class CountAccess extends AbstractVisualComponent {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(CountAccess.class.getName());

	public static final String TYPE = "count-access";
	
	private synchronized int getCount(ContentContext ctx) throws IOException {		
		String count = getViewData(ctx).getProperty("count", "0");
		return Integer.parseInt(count);
	}
	
	private synchronized void addCount(ContentContext ctx) throws IOException {		
		String count = getViewData(ctx).getProperty("count", "0");
		getViewData(ctx).setProperty("count", ""+(Integer.parseInt(count)+1));
		storeViewData(ctx);
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		
		if (ctx.getRequest().getSession().getAttribute(getId()) == null) {
			if (ctx.getDevice().isHuman() && ctx.getRequest().getCookies() != null) {
				addCount(ctx);
				ctx.getRequest().getSession().setAttribute(getId(), getCount(ctx));				
			}
		}		
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		
		String value = getValue();
		
		if (value.contains("#")) {
			value = value.replace("#", "<span>"+ctx.getRequest().getSession().getAttribute(getId())+"</span>");
		} else {
			value = value + "<span>"+ctx.getRequest().getSession().getAttribute(getId())+"</span>";
		}
		
		out.println("<div class=\"counter\">");		
		out.println(value);
		
		out.println("</div>");
		out.close();
		return new String(outStream.toByteArray());
	}

	public String getType() {
		return TYPE;
	}

	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_ADMIN;
	}

	@Override
	public String getHexColor() {
		return WEB2_COLOR;
	}

}
