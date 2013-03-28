package org.javlo.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javlo.image.ImageHelper;
import org.javlo.image.ImageSize;

public class ReadJpeg {

	public static final int unsigned(byte b) {
		return b & 0xff;
	}

	public static void main(String[] args) {
		File jpegFile = new File("d:/trans/2.jpg");
		try {
			InputStream in = new FileInputStream(jpegFile);
			ImageSize size = ImageHelper.getJpegSize(in);
			System.out.println("***** ReadJpeg.main : size : " + size); // TODO: remove debug trace
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void _main(String[] args) {
		File jpegFile = new File("d:/trans/5.jpg");
		byte[] buffed = new byte[1024 * 255];
		InputStream in;
		try {
			in = new FileInputStream(jpegFile);
			BufferedInputStream bufIn = new BufferedInputStream(in);
			int read = bufIn.read(buffed);
			in.close();
			System.out.println("read = " + read);
			for (int i = 0; i < read; i++) {
				// System.out.print(" "+buffed[i]);
				if (buffed[i] == (byte) 0xff) {
					// System.out.println("marker");
					if (buffed[i + 1] == (byte) 0xc0) {
						System.out.println("marker index : " + i);

						System.out.println("+1 : " + (int) buffed[i + 2]);
						System.out.println("+2 : " + (int) buffed[i + 3]);
						System.out.println("+3 : " + (int) buffed[i + 4]);

						int j = 1;
						while (buffed[i + j] != 8) {
							j++;
						}

						System.out.println("j = " + j);

						System.out.println("j+1 = " + (int) buffed[i + j + 1]);
						System.out.println("j+2 = " + (int) buffed[i + j + 2]);
						System.out.println("j+3 = " + (int) buffed[i + j + 3]);
						System.out.println("j+4 = " + (int) buffed[i + j + 4]);

						int height = unsigned(buffed[i + j + 1]) * 255 + unsigned(buffed[i + j + 2]);
						System.out.println("height = " + height);
						int width = unsigned(buffed[i + j + 3]) * 255 + unsigned(buffed[i + j + 4]);
						System.out.println("width = " + width);
					}
				}
			}
			System.out.println("end.");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
