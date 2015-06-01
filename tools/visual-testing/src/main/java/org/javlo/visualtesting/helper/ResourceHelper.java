package org.javlo.visualtesting.helper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourceHelper {

	public static Path createParentFolder(Path file) {
		try {
			Files.createDirectories(file.getParent());
		} catch (IOException e) {
			throw new ForwardException(e);
		}
		return file;
	}

}
