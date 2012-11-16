package org.javlo.utils;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import org.javlo.context.GlobalContext;

public class LoggerFormatter extends Formatter {

	@Override
	public String format(LogRecord record) {
		StringBuffer sb = new StringBuffer();

		// TODO add param, display short call ctx and config JVM
		for (Object param : record.getParameters()) {
			if (param instanceof GlobalContext) {
				GlobalContext ctx = (GlobalContext) param;
				sb.append(ctx.getContextKey());
				sb.append(": ");
			}
		}
		sb.append(record.getMessage());
		
		return sb.toString();
	}

}
