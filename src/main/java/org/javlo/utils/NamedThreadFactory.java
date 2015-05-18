package org.javlo.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private static AtomicInteger threadNumber = new AtomicInteger(1);

	private final String prefix;

	private final boolean daemon;

	public NamedThreadFactory(String namePrefix) {
		this(namePrefix, true);
	}
	public NamedThreadFactory(String namePrefix, boolean daemon) {
		this.prefix = namePrefix;
		this.daemon = daemon;
	}

	public String getPrefix() {
		return prefix;
	}

	public boolean isDaemon() {
		return daemon;
	}

	@Override
	public Thread newThread(Runnable runnable) {
		Thread out = new Thread(runnable, prefix + " thread-" + threadNumber.getAndIncrement());

		if (isDaemon() != out.isDaemon()) {
			out.setDaemon(isDaemon());
		}

		return out;
	}

}
