package org.javlo.component.column.row;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public abstract class AbstractRowComponent extends AbstractVisualComponent {
	
	@Override
	public int getComplexityLevel(ContentContext ctx) {
		return COMPLEXITY_STANDARD;
	}
	
}
