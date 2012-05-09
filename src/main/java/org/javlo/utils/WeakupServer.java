package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WeakupServer {

	private static long TIME_BETWEEN_CHECK = 10*1000; // 10 sec
	private static long TIME_AFTER_SCRIPT = 60*1000; // wait 60 sec after script running

	private static int getURLConnectionStatus(URLConnection conn) {
		try {
			if (conn.getHeaderField(null) != null) {
				String[] data = conn.getHeaderField(null).split(" ");
				if (data.length > 1) {
					return Integer.parseInt(data[1]);
				}
			}
		} catch (Throwable t) {
			System.out.println("");
			System.out.println("connection error : "+t.getMessage());
			System.out.println("");
		}
		return -1;
	}

	public static void surveil(URL url, String restartCommand, File lockFile) {
		int errorCount = 0;
		while (errorCount < 3) {
			boolean connectionError = false;
			while (!connectionError) {
				if (lockFile.exists()) {
					System.out.println("-LK-");
					try {
						Thread.sleep(TIME_BETWEEN_CHECK);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					System.out.print(".");
					URLConnection conn;
					try {
						conn = url.openConnection();
						int status = getURLConnectionStatus(conn);
						if (status == -1 || status >= 400) {
							System.out.println("");
							System.out.println("error status = " + status);
							connectionError = true;
							errorCount++;
						} else {
							errorCount = 0;
							connectionError = false;
						}
					} catch (IOException e) {
						System.out.println("");
						System.out.println("error : " + e.getMessage());
						connectionError = true;
						errorCount++;
					}
					if (connectionError) {
						try {

							System.out.println("");
							System.out.println("exec command : " + restartCommand);
							System.out.println("");

							Runtime rt = Runtime.getRuntime();
							Process p = rt.exec(restartCommand);
							BufferedReader out = new BufferedReader(new InputStreamReader(p.getErrorStream()));

							String line = out.readLine();
							while (line != null) {
								System.out.println(line);
								line = out.readLine();
							}
							System.out.println("");
							Thread.sleep(TIME_AFTER_SCRIPT);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						try {
							Thread.sleep(TIME_BETWEEN_CHECK);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		System.out.println("****************************************");
		System.out.println("** WARNING TO MUSH ERROR : " + errorCount + "    **");
		System.out.println("****************************************");
	}

	public static void main(String[] args) {
		String url = "http://localhost:8080";
		String command = "/opt/java/tomcat/bin/restart.sh";
		String lockFile = "/tmp/no_check_server.sticky";
		if (args.length == 3) {
			url = args[0];
			command = args[1];
		}
		System.out.println("WeakupServer V.1");
		System.out.println("url = " + url);
		System.out.println("command = " + command);
		System.out.println("lockFile = " + lockFile);
		System.out.println("");
		try {
			surveil(new URL(url), command, new File(lockFile));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("END.");
	}
}
