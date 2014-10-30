package org.javlo.component.extranet;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.helper.MacroHelper;
import org.javlo.helper.StringHelper;
import org.javlo.macro.core.IMacro;
import org.javlo.macro.core.MacroFactory;
import org.javlo.service.RequestService;

public class LaunchMacro extends AbstractVisualComponent {

	public static final String TYPE = "launch-macro";

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return MacroHelper.getLaunchMacroXHTML(ctx, getMacro(), getLabel());
	}
	
	@Override
	public String getEmptyXHTMLCode(ContentContext ctx) throws Exception { 
		return getViewXHTMLCode(ctx);
	}
	
	private String getMacro() {
		Collection<String> values= StringHelper.stringToCollection(getValue(), "-");
		if (values.size() < 2) {
			return null;
		} else {
			return values.iterator().next();
		}		
	}
	
	private String getLabel() {
		Collection<String> values= StringHelper.stringToCollection(getValue(), "-");
		if (values.size() < 2) {
			return null;
		} else {
			Iterator<String> it = values.iterator();
			it.next();
			return it.next();
		}		
	}
	
	@Override
	protected String getEditXHTMLCode(ContentContext ctx) throws Exception {
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(outStream);
		out.println("<div class=\"line\"><select name=\""+getInputName("macro")+"\">");
		for (IMacro macro : MacroFactory.getInstance(ctx.getGlobalContext().getStaticConfig()).getMacros()) {
			String selected = "";
			if (macro.getName().equals(getMacro())) {
				selected=" selected=\"selected\"";
			}
			out.println("<option"+selected+">"+macro.getName()+"</option>");
		}
		out.println("</select></div>");
		out.println("<div class=\"line\"><input type=\"text\" name=\""+getInputName("label")+"\" value=\""+getLabel()+"\" ></div>");
		out.close();
		return new String(outStream.toByteArray());
	}
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return IContentVisualComponent.COMPLEXITY_ADMIN;
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {
		RequestService requestService = RequestService.getInstance(ctx.getRequest());
		
		String macro = requestService.getParameter(getInputName("macro"), null);
		String label = requestService.getParameter(getInputName("label"), null);
		Collection<String> newValueCol = new LinkedList<String>();
		newValueCol.add(macro);
		newValueCol.add(label);
		String newValue = StringHelper.collectionToString(newValueCol,"-");
		if (!newValue.equals(getValue())) {
			setValue(newValue);
			setModify();
		}
	}

}
