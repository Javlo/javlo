package org.javlo.service.syncro;

import java.io.File;

import org.javlo.helper.URLHelper;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroAction;
import org.javlo.service.syncro.AbstractSynchroContext.SynchroSide;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.exception.SynchroNonFatalException;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.utils.DebugListening;

public class ServerSynchroService extends BaseSynchroService {

	private final String localName;
	private String subName;

	private String prefix;
	private boolean deleteIntraAfterTransfert = false;
	private boolean deleteDMZIfNotFoundIntra = false;
	private boolean pushOnDMZ = true;
	private boolean downloadFromDMZ = true;

	public static ServerSynchroService getInstance(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String dataFolder) {
		ServerSynchroService s = new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, dataFolder);
		s.subName = "Data Folder";
		s.prefix = "";
		s.downloadFromDMZ = false;
		s.deleteDMZIfNotFoundIntra = true;
		return s;
	}

	public static ServerSynchroService getInstanceForMailing(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String mailingFolder) {
		ServerSynchroService s = new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, mailingFolder);
		s.subName = "Mailing";
		s.prefix = SynchronisationServlet.MAILING_PREFIX;
		s.deleteIntraAfterTransfert = true;
		s.downloadFromDMZ = false;
		return s;
	}

	public static ServerSynchroService getInstanceForMailingHistory(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String mailingHistoryFolder) {
		ServerSynchroService s = new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, mailingHistoryFolder);
		s.subName = "Mailing History";
		s.prefix = SynchronisationServlet.MAILING_HISTORY_PREFIX;
		s.pushOnDMZ = false;
		return s;
	}

	public static ServerSynchroService getInstanceForTemplate(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String templateFolder) {
		ServerSynchroService s = new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, templateFolder);
		s.subName = "Template";
		s.prefix = SynchronisationServlet.TEMPLATE_PREFIX;
		s.deleteDMZIfNotFoundIntra = true;
		s.downloadFromDMZ = false;
		return s;
	}

	public static ServerSynchroService getInstanceForShareFiles(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String shareFolder) {
		ServerSynchroService s = new ServerSynchroService(localName, serverURL, proxyHost, proxyPort, synchroCode, shareFolder);
		s.subName = "Share files";
		s.prefix = SynchronisationServlet.SHARE_PREFIX;
		s.deleteDMZIfNotFoundIntra = true;
		s.downloadFromDMZ = false;
		return s;
	}

	protected ServerSynchroService(String localName, String serverURL, String proxyHost, int proxyPort, String synchroCode, String baseFolder) {
		super(newHttpClientService(serverURL, synchroCode, proxyHost, proxyPort), new File(baseFolder));
		setManageDeletedFiles(false);
		setSplitBigFiles(false);
		this.localName = localName;
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
		return localName + " (" + subName + ")";
	}

	@Override
	protected SynchroAction defineAction(BaseSynchroContext context, String path) throws SynchroNonFatalException, SynchroFatalException {
		// TODO Auto-generated method stub
		return super.defineAction(context, path);
	}

	@Override
	protected void applyAction(BaseSynchroContext context, String path, SynchroAction action) throws SynchroNonFatalException {
		switch (action) {
		case COPY_TO_LOCAL: {
			if (downloadFromDMZ) {
				FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
				copyDistantToLocal(context, distantInfo);
			}
		}
			break;
		case COPY_TO_DISTANT: {
			if (pushOnDMZ) {				
				FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
				copyLocalToDistant(context, localInfo);
				if (deleteIntraAfterTransfert) {
					deleteLocalFile(context, localInfo);
				}
			}
		}
			break;
		case DELETE_LOCAL: {
			//Disabled
//			FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
//			if (localInfo == null) {
//				// In "mark as deleted" case
//				localInfo = context.getInfo(SynchroSide.DISTANT, path);
//			}
//			deleteLocalFile(context, localInfo);
		}
			break;
		case DELETE_DISTANT: {
			if (deleteDMZIfNotFoundIntra) {
				FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
				deleteDistantFile(context, distantInfo);
			}
		}
			break;
		// case MOVE_LOCAL:
		// break;
		// case MOVE_DISTANT:
		// break;
		case CONFLICT: {
			if (pushOnDMZ) {
				FileInfo localInfo = context.getInfo(SynchroSide.LOCAL, path);
				copyLocalToDistant(context, localInfo);
			} else if (downloadFromDMZ) {
				FileInfo distantInfo = context.getInfo(SynchroSide.DISTANT, path);
				copyDistantToLocal(context, distantInfo);
			}
		}
			break;
		default:
			logger.warning("Unmanaged synchro action: " + action);
			break;
		}
	}

	@Override
	public String buildURL(String url) {
		return super.buildURL(URLHelper.mergePath(prefix, url));
	}

	@Override
	protected void onShutdown(BaseSynchroContext context) {
		super.onShutdown(context);
		if (context.isErrorOccured()) {
			DebugListening.getInstance().sendError(context.getReport());
		}
	}

	@Override
	protected void deleteDistantDirectories(BaseSynchroContext context) {
		// Disabled
	}

	/*@Override
	protected boolean deleteLocalFile(BaseSynchroContext context, FileInfo localInfo) {
		File file = buildLocalFile(localInfo.getPath());
		System.out.println("***** ServerSynchroService.deleteLocalFile : delete : "+file.getAbsolutePath()); //TODO: remove debug trace
		System.out.println("***** ServerSynchroService.deleteLocalFile : exist ? "+file.exists()); //TODO: remove debug trace
		if (!file.exists()) {
			logger.fine("could not delete file : "+file+" (file not found).");
		} else {
			if (file.isDirectory()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				file.delete();
			}
		}
		return false;
	}*/

	@Override
	protected void deleteLocalDirectories(BaseSynchroContext context) {
		// Disabled for safety
	}

}
