package org.javlo.service.integrity;

import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;

/**
 * factory for the content integrity tester.
 * content integrity could check site of component like description or title.
 * @author pvandermaesen
 *
 */
public class IntegrityFactory {

	private int errorLevel = 0;

	private List<IntegrityBean> beans = new LinkedList<IntegrityFactory.IntegrityBean>();

	public static class IntegrityBean {
		private IIntegrityChecker checker;
		private ContentContext ctx = null;

		public IntegrityBean(ContentContext ctx, IIntegrityChecker checker) {
			this.checker = checker;
			this.ctx = ctx;
		}

		public String getErrorMessage() {
			return checker.getErrorMessage(ctx);
		}

		public int getErrorCount() {
			return checker.getErrorCount(ctx);
		}

		public int getLevel() {
			return checker.getLevel(ctx);
		}

		public String getLevelLabel() {
			return checker.getLevelLabel(ctx);
		}
	}

	private static String KEY = "integrities";

	private IntegrityFactory() {
	}

	protected List<IIntegrityChecker> getAllChecker(ContentContext ctx) {
		List<IIntegrityChecker> checkers = new LinkedList<IIntegrityChecker>();
		checkers.add(new CheckTitle());
		checkers.add(new CheckDescription());
		checkers.add(new CheckTitleHierarchy());
		checkers.add(new CheckImageLabel());		
		return checkers;
	}

	public static IntegrityFactory getInstance(ContentContext ctx) {
		if (ctx.getGlobalContext().getStaticConfig().isIntegrityCheck()) {
			IntegrityFactory outFactory = (IntegrityFactory) ctx.getRequest().getAttribute(KEY);
			if (outFactory == null) {
				outFactory = new IntegrityFactory();
				int maxErrorLevel = 0;
				for (IIntegrityChecker checker : outFactory.getAllChecker(ctx)) {
					try {
						if (!checker.checkPage(ctx, ctx.getCurrentPage())) {
							if (checker.getLevel(ctx) > maxErrorLevel) {
								maxErrorLevel = checker.getLevel(ctx);
							}
							outFactory.beans.add(new IntegrityBean(ctx, checker));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				outFactory.errorLevel = maxErrorLevel;
				ctx.getRequest().setAttribute(KEY, outFactory);
			}
			return outFactory;
		} else {
			return null;
		}
	}

	public List<IntegrityBean> getChecker() {
		return beans;
	}

	public String getLevelLabel() {
		return IIntegrityChecker.LEVEL_LABEL[errorLevel];
	}

}
