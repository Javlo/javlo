package org.javlo.component.meta;

import java.util.Properties;

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
	
	private String getKey(ContentContext ctx) throws Exception {
		return "number-"+getValue();
	}
	
	private static synchronized void createNewNumber(ContentContext ctx, AutoNumberComponent currentComp) throws Exception {
		ContentService content = ContentService.getInstance(ctx.getRequest());
		int maxNumber = 1;
		for (IContentVisualComponent comp : content.getAllContent(ctx.getContextWithArea(null))) {
			if (comp.getType().equals(TYPE)) {
				System.out.println("***** AutoNumberComponent.createNewNumber : comp id = "+comp.getId()); //TODO: remove debug trace
				if (((AutoNumberComponent)comp).isNumber(ctx)) {
				int compNumber = ((AutoNumberComponent)comp).getNumber(ctx)+1;
				System.out.println("***** AutoNumberComponent.createNewNumber : compNumber = "+compNumber); //TODO: remove debug trace
				if (compNumber > maxNumber) {
					maxNumber = compNumber;
				}
				}
				
			}
		}		
		Properties data = currentComp.getViewData(ctx);
		data.setProperty(currentComp.getKey(ctx), ""+maxNumber);
	}
	
	protected boolean isNumber(ContentContext ctx) throws Exception {		
		return getViewData(ctx).getProperty(getKey(ctx)) != null;
	}

	protected int getNumber(ContentContext ctx) throws Exception {		
		Properties data = getViewData(ctx);
		if (!isNumber(ctx)) {
			createNewNumber(ctx, this);
		}
		System.out.println("***** AutoNumberComponent.getNumber : data.getProperty(\""+getKey(ctx)+"\") = "+data.getProperty(getKey(ctx))); //TODO: remove debug trace
		return Integer.parseInt(data.getProperty(getKey(ctx)));
	}
	
	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {	
		return getValue()+getNumber(ctx);
	}
	
	@Override
	public void performEdit(ContentContext ctx) throws Exception {	
		super.performEdit(ctx);
		if (isModify()) {
			getViewData(ctx).clear();
			storeViewData(ctx);
		}
	}

}
