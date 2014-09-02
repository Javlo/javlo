package org.javlo.component.meta;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.service.ContentService;

public class AutoNumberComponent extends AbstractVisualComponent {
	
	public static final String TYPE="auto-number";	

	@Override
	public String getType() {
		return TYPE;
	}
	
	private static synchronized void createNewNumber(ContentContext ctx, AutoNumberComponent currentComp) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		int maxNumber = 0;
		for (IContentVisualComponent comp : content.getAllContent(ctx.getContextWithArea(null))) {
			if (comp.getType().equals(TYPE)) {
				int compNumber = ((AutoNumberComponent)comp).getNumber(ctx);
				if (compNumber > maxNumber) {
					maxNumber = compNumber;
				}
			}
		}
		setNumber(maxNumber+1);
	}
	
	private static void setNumber(int i) {
		// TODO Auto-generated method stub
		
		
	}

	protected int getNumber(ContentContext ctx) {
		return -1;
	}

}
