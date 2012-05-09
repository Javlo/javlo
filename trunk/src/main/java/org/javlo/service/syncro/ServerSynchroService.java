package org.javlo.service.syncro;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import org.javlo.helper.ResourceHelper;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.utils.DebugListening;

public class ServerSynchroService extends BaseSynchroService {

	private final String localName;
	private final File previousStateInAndOut;

	public static ServerSynchroService createInstance(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, File dataFolder, File previousStateInAndOut) {
		return new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, dataFolder, previousStateInAndOut);
	}

	protected ServerSynchroService(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, File dataFolder, File previousStateInAndOut) {
		super(newHttpClientService(serverURL, synchroCode, proxyHost, proxyPort), dataFolder);
		this.localName = localName;
		this.previousStateInAndOut = previousStateInAndOut;
	}

	protected static HttpClientService newHttpClientService(String serverURL, String synchroCode, String proxyHost, int proxyPort) {
		HttpClientService out = new HttpClientService();
		out.setServerURL(serverURL);
		out.setSynchroCode(synchroCode);
		out.setProxyHost(proxyHost);
		if (proxyPort > 0) {
			out.setProxyPort(proxyPort);
		}
		return out;
	}

	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	protected void initializeContext(BaseSynchroContext context, Object previousState) throws SynchroFatalException {
		if (previousState == null && previousStateInAndOut.exists()) {
			FileInputStream in = null;
			try {
				in = new FileInputStream(previousStateInAndOut);
				previousState = FileStructureFactory.readFromStream(in);
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Error loading synchro previous state " + previousStateInAndOut.getAbsolutePath(), ex);
			} finally {
				ResourceHelper.safeClose(in);
			}
		}
		super.initializeContext(context, previousState);
	}

	@Override
	protected void onShutdown(BaseSynchroContext context) {
		super.onShutdown(context);
		if (context.isErrorOccured()) {
			DebugListening.getInstance().sendError(context.getReport());
		}
		FileOutputStream out = null;
		try {
			previousStateInAndOut.getParentFile().mkdirs();
			out = new FileOutputStream(previousStateInAndOut);
			FileStructureFactory.writeToStream(context.getOutState().values(), out);
		} catch (IOException ex) {
			logger.log(Level.SEVERE, "Error writing synchro previous state " + previousStateInAndOut.getAbsolutePath(), ex);
		} finally {
			ResourceHelper.safeClose(out);
		}
	}

}
