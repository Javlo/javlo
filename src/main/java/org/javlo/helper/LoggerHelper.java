package org.javlo.helper;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class LoggerHelper {
	
	public static final String LEVEL_INFO = "info";
	public static final String LEVEL_DEBUG = "debug";

	private static Logger logger = LoggerFactory.getLogger(LoggerHelper.class);
	private static String cachedLevel = "";
	
	public static void changeLogLevel(Level level) {
		if (cachedLevel.equalsIgnoreCase(level.levelStr)) {
			logger.debug("level: {} not changed", cachedLevel);
			return;
		}
		logger.info("level will change from: {} to: {}", cachedLevel, level.levelStr);
		cachedLevel = level.levelStr;
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<ch.qos.logback.classic.Logger> loggerList = loggerContext.getLoggerList();
		loggerList.stream().forEach(tmpLogger -> tmpLogger.setLevel(level));

		logger.debug("debug message");
		logger.info("info message");
		logger.error("error message");
	}

	public static void changeLogLevel(String logLevel) {
		Level level = Level.toLevel(logLevel.toUpperCase()); // default to Level.DEBUG
		changeLogLevel(level);
	}
}
