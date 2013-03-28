package be.javlo.component.demo;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.context.ContentContext;

public class DemoComponent extends AbstractVisualComponent {

	@Override
	public String getType() {
		return "demo";
	}

	@Override
	public String getViewXHTMLCode(ContentContext ctx) throws Exception {
		return "Hello " + getValue() + " world.";
	}

}
