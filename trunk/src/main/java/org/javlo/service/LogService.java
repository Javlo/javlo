package org.javlo.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
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
	private Map<Long, LogLine> lines = new TimeMap<Long, LogLine>(4 * 60 * 60); //4H
	private long lastLineId = 0;
	private long filePointer = Long.MAX_VALUE;

	private LogService(ServletContext application) {
		this.application = application;
	}

	public synchronized void clear() {
		filePointer = Long.MAX_VALUE;
		lines.clear();
	}

	public Long fillLineList(Long lineId, List<LogLine> list) {
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
			LogLine line = lines.get(lineId);
			if (line != null) {
				list.add(line);
			}
		}
		return currentLastId;
	}

	private synchronized void updateHistory() {
		StaticConfig staticConfig = StaticConfig.getInstance(application);
		String path = staticConfig.getLogFile();
		File logFile;
		logFile = new File(application.getRealPath(path));
		if (!logFile.exists()) {
			lines.put(lastLineId++, new LogLine(Level.SEVERE.getName(), "Log file not found: " + logFile));
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
						LogLine lastLine = lines.get(lastLineId - 1);
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

	private void addLine(Level level, String text) {
		lines.put(lastLineId++, new LogLine(level == null ? null : level.getName(), text));
	}

	public static class LogLine {

		private String level;
		private String text;

		public LogLine() {
		}

		public LogLine(String level, String text) {
			super();
			this.level = level;
			this.text = text;
		}

		public String getLevel() {
			return level;
		}
		public void setLevel(String level) {
			this.level = level;
		}

		public String getText() {
			return text;
		}
		public void setText(String line) {
			this.text = line;
		}

	}

}
