package org.javlo.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class StressTest {

	public static void main(String[] args) {
		try {
			for (int i = 0; i < 1000; i++) {
				URL url = new URL("http://localhost:8080/jopa/fr/editions/createurs/milia-m.html");
				URLConnection conn = url.openConnection();
				InputStream in = conn.getInputStream();
				int byteRead = 0;
				while(in.read() >= 0) {
					byteRead++;
				}
				in.close();
				System.out.println("i="+i+" bytes readed : "+byteRead);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("");
		System.out.println("END.");
	}

}
