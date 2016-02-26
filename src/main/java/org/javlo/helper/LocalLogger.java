/*
 * Created on 20-fevr.-2004
 */
package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletContext;

import org.javlo.io.AppendableTextFile;

/**
 * @author pvandermaesen
 */
public class LocalLogger {
	
	public static File SPECIAL_LOG_FILE = null;

	public static boolean PRINT_TIME = false;
	
	public static boolean ERROR_ON_INIT = false;

	private static AppendableTextFile specialLogFile = null;

	// 0 no trace
	// 1 minimum information
	// 2 large trace
	// 3 debug
	static final int DEBUG_LEVEL = 3;

	static final Map<String, Long> times = new Hashtable<String, Long>();

	static final String START_LOG = "DCLOG: ";

	public final static int INFO = 1;
	public final static int WARNING = 2;
	public final static int ERROR = 3;
	public final static int DEBUG = 4;
	
	public static void init(ServletContext application) {
		
	}

	static String getMessageTypeLabel(int type) {
		String res;
		switch (type) {
		case INFO:
			res = "info";
			break;
		case WARNING:
			res = "warning";
			break;
		case ERROR:
			res = "error";
			break;
		case DEBUG:
			res = "debug";
			break;
		default:
			res = "undefined";
			break;
		}
		return res;
	}

	public static void log(int type, String message) {
		if (DEBUG_LEVEL > 0) {
			System.out.println(START_LOG + getMessageTypeLabel(type) + " - " + message);
		}
	}

	public static void log(Throwable e) {
		if (DEBUG_LEVEL > 0) {
			System.out.println(START_LOG + getMessageTypeLabel(ERROR) + " - " + e.getMessage());
			if (DEBUG_LEVEL > 2) {
				System.out.flush();
				e.printStackTrace();
			}

		}
	}

	public static void startCount(String key) {
		if (!PRINT_TIME) {
			return;
		}
		Long time = new Long(System.currentTimeMillis());
		times.put(key, time);
	}

	public static void forceStartCount(String key) {
		Long time = new Long(System.currentTimeMillis());
		times.put(key, time);
	}

	public static void forceStepCount(String key, String message) {
		Long oldTime = (Long) times.get(key);
		if (oldTime == null) {
			System.out.println("key not found : "+key);
		} else {
			log(DEBUG, '(' + key + ") " + message + " [" + (System.currentTimeMillis() - oldTime.longValue()) + " ms ]");
		}
	}

	public static void stepCount(String key, String message) {
		if (!PRINT_TIME) {
			return;
		}
		Long oldTime = (Long) times.get(key);
		log(DEBUG, message + " [" + (System.currentTimeMillis() - oldTime.longValue()) + " ms ]");
	}

	public static void endCount(String key, String message) {
		if (!PRINT_TIME) {
			return;
		}
		Long oldTime = (Long) times.get(key);
		if (oldTime == null) {
			System.out.println("key not found : "+key);
		} else {
			times.remove(key);
			log(DEBUG, message + " [" + (System.currentTimeMillis() - oldTime.longValue()) + " ms ]");
		}
	}

	public static void close() throws IOException {
		if (specialLogFile != null) {
			specialLogFile.close();
		}
	}
	
	public static final void log(boolean print, String text) {
		if (print) {
			log(text);
		}
	}
	
	public static final void logStack(boolean print) {
		if (print) {
			StringWriter out = new StringWriter();
			Exception e = new Exception();
			e.printStackTrace(new PrintWriter(out));
			log(out.toString());
		}
	}

	public static final void log(String text) {
		if (ERROR_ON_INIT || SPECIAL_LOG_FILE == null) {
			return;
		}
		try {
			if (specialLogFile == null) {
				SPECIAL_LOG_FILE.getParentFile().mkdirs();
				specialLogFile = new AppendableTextFile(SPECIAL_LOG_FILE);
				specialLogFile.setAutoFlush(true);
			}
			specialLogFile.println(text);
		} catch (IOException e) {
			e.printStackTrace();
			ERROR_ON_INIT = true;
		}
	}
}
