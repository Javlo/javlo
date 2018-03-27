package org.javlo.component.meta;

import java.util.Arrays;
import java.util.List;

import org.javlo.component.properties.AbstractPropertiesComponent;
import org.javlo.context.ContentContext;

public class DefinitionComponent extends AbstractPropertiesComponent {
	
	public static final String TYPE = "definition";
	
	private List<String> FIELDS = Arrays.asList(new String[] {"word", "definition"});

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public List<String> getFields(ContentContext ctx) throws Exception {
		return FIELDS;
	}
	
	public String getWord() {
		return getFieldValue("word");
	}
	
	public String getDefinition() {
		return getFieldValue("definition");
	}
	
	@Override
	public String getFontAwesome() {
		return "commenting-o";
	}

}
