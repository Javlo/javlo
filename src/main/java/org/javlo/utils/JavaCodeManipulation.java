package org.javlo.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

public class JavaCodeManipulation {
	
	public static final String extractClassName(String code) {
		List<String> items = StringHelper.extractItem(code, "public class ", "implements");
		if (items.size()>0) {
			return items.iterator().next().trim();
		}
		items = StringHelper.extractItem(code, "public class ", "extend");
		if (items.size()>0) {
			return items.iterator().next().trim();
		}
		items = StringHelper.extractItem(code, "public class ", "{");
		if (items.size()>0) {
			return items.iterator().next().trim();
		}
		return null;
	}
	
	public static final void changeLogger(File file) throws IOException {
		if (!file.exists()) {
			return;
		}
		String code = ResourceHelper.loadStringFromFile(file);
		String className = extractClassName(code);
		System.out.println(">>>> change logger for class : "+className);
		code = code.replace("public static java.util.logging.Logger logger", "//public static java.util.logging.Logger logger");
		code = code.replace("public static Logger logger", "//public static Logger logger");
		
	}
	
	public static void main(String[] args) throws IOException {
		File file = new File("c:/trans/UserFactory.java");
		changeLogger(file);
	}

}
