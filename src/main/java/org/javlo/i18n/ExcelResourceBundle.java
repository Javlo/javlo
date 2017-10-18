package org.javlo.i18n;

import java.io.File;
import java.util.Enumeration;
import java.util.ResourceBundle;

public class ExcelResourceBundle extends ResourceBundle {
	
	public ExcelResourceBundle(File file) {
		
	}

	@Override
	protected Object handleGetObject(String key) {
		return null;
	}

	@Override
	public Enumeration<String> getKeys() {
		return null;
	}

}
