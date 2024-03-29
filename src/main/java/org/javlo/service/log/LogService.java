package org.javlo.service.log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.utils.TimeMap;

public class LogService {

	private static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LogService.class.getName());

	private static final String LOG_SERVICE_ATTRIBUTE_NAME = LogService.class.getName();

	public static LogService getInstance(HttpSession session) {
		LogService instance = (LogService) session.getServletContext().getAttribute(LOG_SERVICE_ATTRIBUTE_NAME);
		if (instance == null) {
			instance = new LogService(session.getServletContext());
			session.getServletContext().setAttribute(LOG_SERVICE_ATTRIBUTE_NAME, instance);
		}
		return instance;
	}

	private final ServletContext application;
	private Map<Long, Log> lines = new TimeMap<Long, Log>(1 * 60 * 60); //1H
	private long lastLineId = 0;
	private long filePointer = Long.MAX_VALUE;

	private LogService(ServletContext application) {
		this.application = application;
	}

	public synchronized void clear() {
		filePointer = Long.MAX_VALUE;
		lines.clear();
	}

	public Long fillLineList(Long lineId, List<Log> list) {
		updateHistory();
		long currentLastId = lastLineId;
		if (lineId == null) {
			try {
				lineId = Collections.min(lines.keySet());
			} catch (NoSuchElementException e) {
				lineId = currentLastId + 1;
			}
		} else {
			lineId++;
		}
		for (; lineId <= currentLastId; lineId++) {
			Log line = lines.get(lineId);
			if (line != null) {
				list.add(line);
			}
		}
		return currentLastId;
	}

	private synchronized void updateHistory() {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		String path = staticConfig.getLogFile();
		File logFile = null;
		if (StringHelper.isEmpty(path)) {
			long latestModifMax = 0;
			File dir = new File(ResourceHelper.getRealPath(application,staticConfig.getLogDir()));
			if (!dir.exists()) {
				logger.severe("log dir not found : "+dir);
			} else {
				for (File file : dir.listFiles()) {
					if (StringHelper.getFileExtension(file.getName()).equalsIgnoreCase("log")) {
						if (file.lastModified()>latestModifMax) {
							logFile = file;
							latestModifMax = file.lastModified();
						}
					}
				}
			}
		} else {
			logFile = new File(ResourceHelper.getRealPath(application,path));
		}
		if (logFile == null || !logFile.exists()) {
			addLine(Level.SEVERE, "Log file not found: " + (logFile!=null?"f:"+logFile:"d:"+staticConfig.getLogDir()));
			return;
		}

		long fileLength = logFile.length();
		if (filePointer > fileLength) {
			filePointer = Math.max(0, fileLength - 1 * 1024 * 1024); // Last 1 MB 
		}

		if (fileLength > filePointer) {
			RandomAccessFile file = null;
			try {
				file = new RandomAccessFile(logFile, "r");
				file.seek(filePointer);
				String line1 = null;
				long line1Pointer = file.getFilePointer();
				String line2 = ResourceHelper.readLine(file, ContentContext.CHARSET_DEFAULT);
				long line2Pointer = file.getFilePointer();
				while (line2 != null) {
					Level level = null;
					int pos = line2.indexOf(':');
					if (pos >= 0) {
						try {
							level = Level.parse(line2.substring(0, pos));
						} catch (Exception ignored) {
						}
					}
					if (level != null) {
						filePointer = line2Pointer;
						addLine(level, line1 + "\n" + line2);
						line2 = null;
					} else if (line1 != null) {
						Log lastLine = lines.get(lastLineId - 1);
						if (lastLine != null && lastLine.getLevel() == null) {
							filePointer = line1Pointer;
							lastLine.setText(lastLine.getText() + "\n" + line1);
						} else {
							filePointer = line1Pointer;
							addLine(null, line1);
						}
					}
					line1 = line2;
					line1Pointer = line2Pointer;
					line2 = ResourceHelper.readLine(file, ContentContext.CHARSET_DEFAULT);
					line2Pointer = file.getFilePointer();
				}
				file.close();
			} catch (Exception ex) {
				// TODO: handle exception
				ex.printStackTrace();
			} finally {
				if (file != null) {
					try {
						file.close();
					} catch (IOException ignored) {
					}
				}
			}
		}
		//TODO Remove old lines
	}
	
	public void addLine(Log log) {
		lines.put(++lastLineId, log);
	}

	public void addLine(Level level, String text) {
		String levelStr = Level.INFO.getName();
		if (level != null) {
			levelStr = level.getName();
		}
		lines.put(++lastLineId, new Log(levelStr, "external", text));
	}

}
