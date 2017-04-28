package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;

import org.javlo.helper.ResourceHelper;

public class ImageIOLeakTest {

	public static void main(String[] args) {
		try {
			test(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void test(ServletContext application) throws IOException {
		final int iterations = 100;
		File file;
		if (application == null) {
			file = new File("d:/trans/test.jpg");
		} else {
			file = new File(ResourceHelper.getRealPath(application,"/images/logo.png"));
		}

		if (!file.exists()) {
			System.out.println("file not found : " + file);
			return;
		}

		String processId = ManagementFactory.getRuntimeMXBean().getName();
		processId = processId.substring(0, processId.indexOf('@'));
		System.out.println("Image is " + file);
		System.out.println("Process ID is " + processId);
		System.out.println("Iterating " + iterations + " times ");
		int height = 0;
		int width = 0;
		for (int i = 0; i < iterations; i++) {
			ImageInputStream iis = null;
			try {
				iis = ImageIO.createImageInputStream(file);
				Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if (iter.hasNext()) {
					ImageReader reader = null;
					try {
						reader = iter.next();
						reader.setInput(iis);
						height = reader.getHeight(0);
						width = reader.getWidth(0);
					} finally {
						if (reader != null) {
							reader.dispose();
						}
					}
				}
			} finally {
				if (iis != null) {
					try {
						iis.close();
					} catch (Exception ignored) {
						// Ignore
					}
				}
			}
		}
		String mmapFile = "/proc/" + processId + "/maps";
		System.out.println("Examining " + mmapFile + " for leaks.");
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(mmapFile));
			int totalMMaps = 0;
			int markedAsDeleted = 0;
			String ln = in.readLine();
			while (ln != null) {
				totalMMaps++;
				int index = ln.indexOf("(deleted)");
				if (index != -1) {
					markedAsDeleted++;
				}
				ln = in.readLine();
			}
			System.out.println("Image height, width is " + height + ", " + width);
			System.out.println("Total mmaps is " + totalMMaps);
			System.out.println("(deleted) mmaps is " + markedAsDeleted);
			System.out.println("difference is " + (totalMMaps - markedAsDeleted));
		} finally {
			ResourceHelper.safeClose(in);
		}
	}
}
