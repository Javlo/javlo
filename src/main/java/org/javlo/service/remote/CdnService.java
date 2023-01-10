package org.javlo.service.remote;

import java.net.URL;
import java.util.logging.Logger;

import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class CdnService {

	private static Logger logger = Logger.getLogger(CdnService.class.getName());

	private static final String KEY = CdnService.class.getCanonicalName();
	private GlobalContext globalContext;
	private boolean active = true;
	private boolean releaseCache = false;
	private long latestTest = 0;
	private static final long TEST_DELTA = 1000 * 60; // test every 1 min

	private class TestThread extends Thread {

		private CdnService cdnService;

		@Override
		public void run() {
			cdnService.internalTestCdn();
		}

	}

	public static CdnService getInstance(GlobalContext globalContext) {
		CdnService out = (CdnService) globalContext.getAttribute(KEY);
		if (out == null) {
			out = new CdnService();
			globalContext.setAttribute(KEY, out);
			out.globalContext = globalContext;
			out.testCdn();
		}
		return out;
	}

	public String getMainCdn() {
		return globalContext.getSpecialConfig().getMainCdn();
	}

	public String getMainCdnAuto() {
		if (active) {
			return globalContext.getSpecialConfig().getMainCdn();
		} else {
			return null;
		}
	}

	public void testCdn() {
		if (System.currentTimeMillis() - latestTest > TEST_DELTA) {
			latestTest = System.currentTimeMillis();
			TestThread testThread = new TestThread();
			testThread.cdnService = this;
			testThread.start();
		}
	}

	public boolean internalTestCdn() {
		if (StringHelper.isEmpty(getMainCdn())) {
			return false;
		}
		try {
			String retrunCdn = NetHelper.readPage(new URL(URLHelper.mergePath(getMainCdn(), "check")));
			boolean newActive = retrunCdn.trim().equalsIgnoreCase("ok");
			if (!newActive && active) {
				releaseCache = true;
			}
			active = newActive;
			if (!StringHelper.isEmpty(getMainCdn())) {
				if (!active) {
					logger.severe("error on connect cdn : " + getMainCdn());
				} else {
					logger.info("cdn ok : " + getMainCdn());
				}
			}
			return active;
		} catch (Exception e) {
			boolean newActive = false;
			if (!newActive && active) {
				releaseCache = true;
			}
			active = newActive;
			logger.severe("error on connect cdn : " + getMainCdn()+" msg:"+e.getMessage());
			return false;
		}
	}

	public boolean isReleaseCache() {
		return releaseCache;
	}

	public void setReleaseCache(boolean releaseCache) {
		this.releaseCache = releaseCache;
	}

}
