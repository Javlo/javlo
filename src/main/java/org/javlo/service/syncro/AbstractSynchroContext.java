package org.javlo.service.syncro;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.javlo.helper.StringHelper;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.exception.SynchroNonFatalException;

/**
 * Class storing all information to process ONE synchro process run.
 * <br/>This class must be expanded to choose the way to retrieve information.
 * @author bdumont
 */
public abstract class AbstractSynchroContext {

	public enum SynchroSide {
		LOCAL, DISTANT, PREVIOUS;
	}

	public enum SynchroState {
		UNKNOWN, EXIST, DELETED;
	}

	public enum SynchroAction {
		COPY_TO_LOCAL, COPY_TO_DISTANT, DELETE_LOCAL, DELETE_DISTANT, MOVE_LOCAL, MOVE_DISTANT, CONFLICT;
	}

	protected final StringWriter reportBuffer = new StringWriter();
	protected final PrintWriter report = new PrintWriter(reportBuffer);

	protected boolean errorOccured = false;
	protected boolean changeOccured = false;	

	protected final Map<String, SynchroAction> actions = new HashMap<String, SynchroAction>();
	private final AbstractSynchroService<?> parentService;

	public AbstractSynchroContext(AbstractSynchroService<?> parentService) {
		this.parentService = parentService;
	}

	public void initialize(Object previousState) throws SynchroFatalException {
		report.println("Synchronisation report."); //TODO I18n?
		report.println("Start: " + StringHelper.renderTime(new Date()));
		report.println("From : " + parentService.getLocalName());
		report.println("To   : " + parentService.getDistantName());
		report.println("");
	}

	public AbstractSynchroService<?> getParentService() {
		return parentService;
	}

	public List<String> getPathList() throws SynchroFatalException {
		Set<String> paths = new HashSet<String>();
		for (SynchroSide side : SynchroSide.values()) {
			paths.addAll(getPathList(side));
		}
		return SynchroHelper.asSortedList(paths);
	}

	public abstract List<String> getPathList(SynchroSide side) throws SynchroFatalException;

	public abstract SynchroState getState(SynchroSide side, String path) throws SynchroNonFatalException, SynchroFatalException;

	public abstract boolean equals(String path, SynchroSide side1, SynchroSide side2) throws SynchroNonFatalException, SynchroFatalException;

	public void setAction(String path, SynchroAction action) {
		if (action == null) {
			actions.remove(path);
		} else {
			actions.put(path, action);
		}
	}

	public SynchroAction getAction(String path) {
		return actions.get(path);
	}

	public List<String> getPathsWithAction() {
		return SynchroHelper.asSortedList(actions.keySet());
	}

	public List<String> getPathsWithAction(SynchroAction action) {
		Set<String> out = new HashSet<String>();
		for (Entry<String, SynchroAction> entry : actions.entrySet()) {
			if (entry.getValue() == action) {
				out.add(entry.getKey());
			}
		}
		return SynchroHelper.asSortedList(out);
	}

	public void onError() {
		errorOccured = true;
	}
	public void onError(Throwable ex) {
		onError();
	}

	public void onChange() {
		changeOccured = true;
	}

	public boolean isErrorOccured() {
		return errorOccured;
	}

	public boolean isChangeOccured() {
		return changeOccured;
	}

	public PrintWriter getReportWriter() {
		return report;
	}

	public String getReport() {
		report.flush();
		reportBuffer.flush();
		return reportBuffer.toString();
	}

	public abstract Object getOutState();

	public void shutdown() {
		report.println("End  : " + StringHelper.renderTime(new Date()));
		try {
			reportBuffer.close();
		} catch (IOException ex) {
			//Ignore exception
		}
	}
}