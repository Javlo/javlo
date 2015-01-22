package org.javlo.fields;

import java.util.Locale;
import java.util.Map;

import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;

public interface IFieldContainer extends IContentVisualComponent {
	
	public String getContainerType(ContentContext ctx);
	
	public String getLabel(ContentContext ctx);
	
	public java.util.List<String> getFieldsNames(ContentContext ctx) throws Exception;
	
	public java.util.List<Field> getFields(ContentContext ctx) throws Exception;
	
	public Field getField(ContentContext ctx, String name) throws Exception;
	
	public String getFieldValue(ContentContext ctx, String name) throws Exception;
	
	public Map<String, String> getList(ContentContext ctx, String listName, Locale locale) throws Exception;
	
	public Map<String, String> getList(ContentContext ctx, String listName) throws Exception;
	
	public String getViewXHTMLCode(ContentContext ctx) throws Exception;
	
	public String getViewListXHTMLCode(ContentContext ctx) throws Exception;
	
	public boolean isRealContent(ContentContext ctx);
	
	public boolean isFieldContainer(ContentContext ctx);
	
	public MenuElement getPage();

}
