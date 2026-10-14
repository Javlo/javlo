package org.javlo.component.links;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javlo.context.ContentContext;

/**
 * collect timing of the rendering of a page reference component (prepareView + jsp).
 * Stored in the request, one instance by component. The report is logged in INFO
 * when the rendering is slower than SLOW_THRESHOLD_MS, in FINE otherwise.
 */
public class PageReferenceTimer {

	private static Logger logger = Logger.getLogger(PageReferenceTimer.class.getName());

	public static long SLOW_THRESHOLD_MS = 500;

	private static final String REQUEST_KEY = PageReferenceTimer.class.getName() + "_";

	private final long start = System.nanoTime();

	/* step name -> {total nano, count} */
	private final Map<String, long[]> steps = new LinkedHashMap<String, long[]>();

	private final Map<String, Object> infos = new LinkedHashMap<String, Object>();

	private String slowestPage = null;
	private long slowestPageTime = 0;

	public static PageReferenceTimer create(ContentContext ctx, PageReferenceComponent comp) {
		PageReferenceTimer timer = new PageReferenceTimer();
		ctx.getRequest().setAttribute(REQUEST_KEY + comp.getId(), timer);
		return timer;
	}

	/**
	 * @return current timer or a dummy timer (never null)
	 */
	public static PageReferenceTimer get(ContentContext ctx, PageReferenceComponent comp) {
		if (ctx == null || ctx.getRequest() == null || comp == null) {
			return new PageReferenceTimer();
		}
		PageReferenceTimer timer = (PageReferenceTimer) ctx.getRequest().getAttribute(REQUEST_KEY + comp.getId());
		if (timer == null) {
			return new PageReferenceTimer();
		}
		return timer;
	}

	public synchronized void add(String step, long startNano) {
		long[] value = steps.computeIfAbsent(step, k -> new long[2]);
		value[0] += System.nanoTime() - startNano;
		value[1]++;
	}

	public synchronized void info(String key, Object value) {
		infos.put(key, value);
	}

	public synchronized void page(String path, long startNano) {
		long time = System.nanoTime() - startNano;
		if (time > slowestPageTime) {
			slowestPageTime = time;
			slowestPage = path;
		}
	}

	private static String ms(long nano) {
		return "" + (nano / 1000000) + "ms";
	}

	public synchronized void log(ContentContext ctx, PageReferenceComponent comp) {
		if (ctx.getRequest() != null) {
			ctx.getRequest().removeAttribute(REQUEST_KEY + comp.getId());
		}
		long total = System.nanoTime() - start;
		boolean slow = total / 1000000 >= SLOW_THRESHOLD_MS;
		Level level = slow ? Level.INFO : Level.FINE;
		if (!logger.isLoggable(level)) {
			return;
		}
		StringBuilder out = new StringBuilder();
		out.append(slow ? "SLOW " : "").append("page-reference [").append(comp.getId()).append("] total=").append(ms(total));
		try {
			out.append(" page=").append(ctx.getPath()).append(" lg=").append(ctx.getRequestContentLanguage()).append(" mode=").append(ctx.getRenderMode());
			out.append(" renderer=").append(comp.getCurrentRenderer(ctx));
		} catch (Exception e) {
			out.append(" (error:" + e.getMessage() + ')');
		}
		for (Map.Entry<String, Object> info : infos.entrySet()) {
			out.append(' ').append(info.getKey()).append('=').append(info.getValue());
		}
		for (Map.Entry<String, long[]> step : steps.entrySet()) {
			out.append("\n   - ").append(step.getKey()).append(" : ").append(ms(step.getValue()[0])).append(" (x").append(step.getValue()[1]).append(')');
		}
		if (slowestPage != null) {
			out.append("\n   - slowest page in loop : ").append(slowestPage).append(" (").append(ms(slowestPageTime)).append(')');
		}
		logger.log(level, out.toString());
	}
}
