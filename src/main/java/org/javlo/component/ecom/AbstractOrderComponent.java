package org.javlo.component.ecom;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.javlo.component.core.AbstractVisualComponent;

public abstract class AbstractOrderComponent extends AbstractVisualComponent {

	protected Properties getData() {
		Properties prop = new Properties();
		try {
			prop.load(new StringReader(getValue()));
		} catch (IOException e) {	
			e.printStackTrace();
		}
		return prop;
	}
	
	@Override
	public String getSpecificClass() {
		return "order";
	}
}
