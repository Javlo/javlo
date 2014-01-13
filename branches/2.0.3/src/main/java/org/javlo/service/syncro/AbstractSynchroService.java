package org.javlo.service.syncro;

import java.util.List;
import java.util.logging.Logger;

import org.javlo.service.syncro.AbstractSynchroContext.SynchroAction;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroSide;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroState;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.exception.SynchroNonFatalException;

/**
 * Abstract service defining required elements for synchronisation.
 * @author bdumont
 * @param <SC> A subclass of {@link AbstractSynchroContext} specialized for subclasses.
 */
public abstract class AbstractSynchroService<SC extends AbstractSynchroContext> {

	public static Logger logger = Logger.getLogger(AbstractSynchroService.class.getName());

	private Object previousOutState;

	/**
	 * Get the name of the local side. Can be used in conflict file name.
	 * @return the local name
	 */
	public abstract String getLocalName();

	/**
	 * Get the name of the distant side.
	 * @return the ditant name
	 */
	public abstract String getDistantName();

	/**
	 * The synchronisation process main method.
	 * @return <code>true</code> if the synchronisation ended without error.
	 */
	public synchronized boolean synchronize() {
		SC context = null;
		try {
			context = newSynchroContext();
			logger.info("start synchronisation between '" + getLocalName() + "' and '" + getDistantName() + "'");
			try {
				initializeContext(context, previousOutState);
				defineActions(context);
				onActionsDefined(context);
				applyActions(context);
				onActionsApplied(context);
				return !context.isErrorOccured();
			} catch (SynchroFatalException ex) {
				onFatalException(context, ex);
			}
		} catch (Throwable ex) {
			onUncaughtException(context, ex);
		} finally {
			if (context != null) {
				onShutdown(context);
				logger.info("end synchronisation between '" + getLocalName() + "' and '" + getDistantName() + "'");
			}
		}
		return false;
	}

	/**
	 * Initialize the context. It's the first method that can thrown synchro exception.
	 * @param context
	 * @param previousState
	 * @throws SynchroFatalException
	 */
	protected void initializeContext(SC context, Object previousState) throws SynchroFatalException {
		context.initialize(previousState);
	}

	/**
	 * Instanciate a context for ONE synchro run. 
	 * @return an instance of {@link AbstractSynchroContext} specialized for the current {@link AbstractSynchroService} implementation.
	 */
	protected abstract SC newSynchroContext();

	/**
	 * Called in the synchro process. Call {@link #defineAction(AbstractSynchroContext, String)} for each path and {@link AbstractSynchroContext#setAction(String, SynchroAction)} with the result.
	 * @param context the current {@link AbstractSynchroContext}
	 * @throws SynchroFatalException
	 */
	protected void defineActions(SC context) throws SynchroFatalException {
		List<String> paths = context.getPathList();
		for (String path : paths) {
			try {
				SynchroAction action = defineAction(context, path);
				if (action != null) {
					context.setAction(path, action);
				}
			} catch (SynchroNonFatalException ex) {
				onNonFatalException(context, ex, path);
			}
		}
	}

	/**
	 * Called to define the required action for the specified path.
	 * @param context the current {@link AbstractSynchroContext}
	 * @param path
	 * @return the required action.
	 * @throws SynchroNonFatalException
	 * @throws SynchroFatalException
	 */
	protected SynchroAction defineAction(SC context, String path) throws SynchroNonFatalException, SynchroFatalException {
		SynchroState localState = context.getState(SynchroSide.LOCAL, path);
		SynchroState distantState = context.getState(SynchroSide.DISTANT, path);
		SynchroState previousState = context.getState(SynchroSide.PREVIOUS, path);
		switch (localState) {
		case UNKNOWN:
			switch (distantState) {
			case UNKNOWN:
				//Skip
				return null;
			case EXIST:
				if (previousState == SynchroState.EXIST) {
					return SynchroAction.DELETE_DISTANT;
				} else {
					return SynchroAction.COPY_TO_LOCAL;
				}
			case DELETED:
				if (previousState != SynchroState.DELETED) {
					//Mark as deleted in out state
					return SynchroAction.DELETE_LOCAL;
				} else {
					return null;
				}
			}
			break;
		case EXIST:
			switch (distantState) {
			case UNKNOWN:
				if (previousState == SynchroState.EXIST) {
					return SynchroAction.DELETE_LOCAL;
				} else {
					return SynchroAction.COPY_TO_DISTANT;
				}
			case EXIST:
				if (context.equals(path, SynchroSide.LOCAL, SynchroSide.DISTANT)) {
					//Both equals
					return null;
				} else if (previousState == SynchroState.EXIST) {
					if (context.equals(path, SynchroSide.PREVIOUS, SynchroSide.LOCAL)) {
						//Distant is modified
						return SynchroAction.COPY_TO_LOCAL;
					} else if (context.equals(path, SynchroSide.PREVIOUS, SynchroSide.DISTANT)) {
						//Local is modified
						return SynchroAction.COPY_TO_DISTANT;
					} else {
						//Both modified
						return SynchroAction.CONFLICT;
					}
				} else {
					//previousState is UNKNOWN or DELETED
					return SynchroAction.CONFLICT;
				}
			case DELETED:
				if (previousState == SynchroState.DELETED) {
					return SynchroAction.COPY_TO_DISTANT;
				} else {
					return SynchroAction.DELETE_LOCAL;
				}
			}
			break;
		case DELETED:
			switch (distantState) {
			case UNKNOWN:
				//Mark as deleted in distant?
				return null;
			case EXIST:
				if (previousState == SynchroState.EXIST) {
					return SynchroAction.DELETE_DISTANT;
				} else {
					return SynchroAction.COPY_TO_LOCAL;
				}
			case DELETED:
				//Both DELETED
				return null;
			}
			break;
		}
		return null;
	}

	/**
	 * Called when all paths are analysed and required actions defined.
	 * @param context the current {@link AbstractSynchroContext}
	 */
	protected void onActionsDefined(SC context) {
	}

	/**
	 * Call {@link #applyAction(AbstractSynchroContext, String, SynchroAction)} for each path with a required action.
	 * @param context the current {@link AbstractSynchroContext}
	 */
	protected void applyActions(SC context) {
		for (String path : context.getPathsWithAction()) {
			try {
				applyAction(context, path, context.getAction(path));
			} catch (SynchroNonFatalException ex) {
				onNonFatalException(context, ex, path);
			}
		}
	}

	/**
	 * Apply the action required for the path.
	 * @param context the current {@link AbstractSynchroContext}
	 * @param path
	 * @param action
	 * @throws SynchroNonFatalException
	 */
	protected abstract void applyAction(SC context, String path, SynchroAction action) throws SynchroNonFatalException;

	/**
	 * Called when all actions are applied.
	 * @param context the current {@link AbstractSynchroContext}
	 */
	protected void onActionsApplied(SC context) {
	}

	/**
	 * Called when an {@link SynchroNonFatalException} is thrown during the synchro process. Only the current file will be skipped.
	 * @param context the current {@link AbstractSynchroContext}
	 * @param ex
	 * @param currentFilePath 
	 */
	protected void onNonFatalException(SC context, SynchroNonFatalException ex, String currentFilePath) {
		context.onError(ex);
	}

	/**
	 * Called when an {@link SynchroFatalException} is thrown during the synchro process. This is fatal for ONE run.
	 * @param context the current {@link AbstractSynchroContext}
	 * @param ex
	 */
	protected void onFatalException(SC context, SynchroFatalException ex) {
		context.onError(ex);
	}

	/**
	 * Called when an exception is thrown during the synchro process. This is fatal for ONE run.
	 * @param context the current {@link AbstractSynchroContext}. Can be <code>null</code> if the exception is thrown during the call of {@link #newSynchroContext()}.
	 * @param ex
	 */
	protected void onUncaughtException(SC context, Throwable ex) {
		if (context != null) {
			context.onError(ex);
		}
	}

	/**
	 * Called every time a synchro run is finished even if a {@link SynchroFatalException} is thrown. See {@link AbstractSynchroContext#isErrorOccured()}.
	 * <br/>Not called the {@link #newSynchroContext()} failed.
	 * @param context the current {@link AbstractSynchroContext}
	 */
	protected void onShutdown(SC context) {
		previousOutState = context.getOutState();
		context.shutdown();
	}

}
