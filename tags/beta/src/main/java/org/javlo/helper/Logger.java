/*
 * Created on 20-fevr.-2004
 */
package org.javlo.helper;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author pvandermaesen
 */
public class Logger {
	
	public static boolean PRINT_TIME = false;
	
	// 0 no trace
	// 1 minimum information
	// 2 large trace
	// 3 debug
	static final int DEBUG_LEVEL = 3;
	
	static final Map<String,Long> times = new Hashtable<String,Long>();
	
	static final String START_LOG = "DCLOG: ";
	
	public final static int INFO = 1;
	public final static int WARNING = 2;
	public final static int ERROR = 3;
	public final static int DEBUG = 4;
	
	static String getMessageTypeLabel ( int type ) {
		String res;
		switch (type) {
			case INFO :
				res = "info";
				break;
			case WARNING :
				res = "warning";
				break;
			case ERROR :
				res = "error";
				break;
			case DEBUG :
				res = "debug";
				break;
			default :
				res="undefined";
				break;
		}
		return res;
	}
	
	public static void log ( int type, String message ) {
		if ( DEBUG_LEVEL > 0 ) {		
			System.out.println(START_LOG+getMessageTypeLabel(type)+" - "+message);
		}
	}
	
	public static void log ( Throwable e ) {
		if ( DEBUG_LEVEL > 0 ) {
			System.out.println(START_LOG+getMessageTypeLabel(ERROR)+" - "+e.getMessage());			
			if ( DEBUG_LEVEL > 2 ) {
				System.out.flush();
				e.printStackTrace();	
			}

		}
	}
	
	public static void startCount ( String key ) {
		if (!PRINT_TIME) {
			return;
		}
		Long time = new Long ( System.currentTimeMillis() );
		times.put( key, time );
	}
	
	public static void forceStartCount ( String key ) {
		Long time = new Long ( System.currentTimeMillis() );
		times.put( key, time );
	}
	
	public static void forceStepCount ( String key, String message ) {
		Long oldTime = (Long)times.get(key);
		log ( DEBUG, '('+key+") "+message+ " ["+(System.currentTimeMillis()-oldTime.longValue())+" ms ]" );
	}
	
	public static void stepCount ( String key, String message ) {
		if (!PRINT_TIME) {
			return;
		}
		Long oldTime = (Long)times.get(key);
		log ( DEBUG, message+ " ["+(System.currentTimeMillis()-oldTime.longValue())+" ms ]" );
	}
	
	public static void endCount ( String key, String message ) {
		if (!PRINT_TIME) {
			return;
		}
		Long oldTime = (Long)times.get(key);
		times.remove(key);
		log ( DEBUG, message+ " ["+(System.currentTimeMillis()-oldTime.longValue())+" ms ]" );
	}
}
