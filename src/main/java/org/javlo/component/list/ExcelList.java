/*
 * Created on 19-sept.-2003
 */
package org.javlo.component.list;

import java.io.File;

import org.javlo.component.files.AbstractFileComponent;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.service.ReverseLinkService;


/**
 * @author pvandermaesen
 */
public class ExcelList extends AbstractFileComponent {
	
	public static final String TYPE = "excel-list";

	/**
	 * @see org.javlo.itf.IContentVisualComponent#getXHTMLCode()
	 */
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		ReverseLinkService reverserLinkService = ReverseLinkService.getInstance(globalContext);
		String value = textToXHTML(getValue());
		value = reverserLinkService.replaceLink(ctx, this, value);
		return value;
	}
	
	@Override
	public String getType() {
		return TYPE;
	}
	
	public static void main(String[] args) {
		File file = new File("c:/trans/footer.xlsx");
		
		
		
	}
}
