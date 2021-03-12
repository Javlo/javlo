package org.javlo.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ImageWebpLibraryWrapper {

	private static Logger logger = Logger.getLogger(ImageWebpLibraryWrapper.class.getName());

	private String converterPath;

	public ImageWebpLibraryWrapper(String converterPath) {
		this.converterPath = converterPath;
	}

	public boolean isWebPAvailable() {
		if (converterPath == null) {
			return false;
		}
		return new File(converterPath).exists();
	}

	public boolean convertToWebP(File imageFile, File targetFile, int quality) {

		logger.info("webp center [Q:" + quality + "] : " + imageFile + " to " + targetFile);

		Process process;
		try {
			process = new ProcessBuilder(converterPath, "-q", "" + quality, imageFile.getAbsolutePath(), "-o", targetFile.getAbsolutePath()).start();
//			if (imageFile.length() > 1 * 1024 * 1024) {
//				if (imageFile.length() > 5 * 1024 * 1024) {
//					process.waitFor(10, TimeUnit.SECONDS);
//				} else {
//					process.waitFor(4, TimeUnit.SECONDS);
//				}
//			} else {
//				process.waitFor(1, TimeUnit.SECONDS);
//			}
			process.waitFor(10, TimeUnit.SECONDS);
			if (process.exitValue() == 0) {
				// Success
				printProcessOutput(process.getInputStream(), System.out);
				return true;
			} else {
				printProcessOutput(process.getErrorStream(), System.err);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void printProcessOutput(InputStream inputStream, PrintStream output) throws IOException {
		try (InputStreamReader isr = new InputStreamReader(inputStream); BufferedReader bufferedReader = new BufferedReader(isr)) {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				output.println(line);
			}
		}
	}
}
