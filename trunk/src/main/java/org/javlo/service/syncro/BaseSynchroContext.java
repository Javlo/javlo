package org.javlo.service.syncro;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.javlo.helper.ResourceHelper;
import org.javlo.service.syncro.exception.SynchroFatalException;
import org.javlo.service.syncro.exception.SynchroNonFatalException;
import org.javlo.servlet.SynchronisationServlet;

/**
 * Javlo implementation of the {@link AbstractSynchroContext}.
 * @author bdumont
 */
public class BaseSynchroContext extends AbstractSynchroContext {

	private final BaseSynchroService parentService;

	private Map<String, FileInfo> outState;
	private Map<SynchroSide, Map<String, FileInfo>> infos = new HashMap<SynchroSide, Map<String, FileInfo>>();
	private List<FileInfo> localDirectoryToDelete = new LinkedList<FileInfo>();
	private List<FileInfo> distantDirectoryToDelete = new LinkedList<FileInfo>();

	public BaseSynchroContext(BaseSynchroService parentService) {
		super(parentService);
		this.parentService = parentService;
	}

	@Override
	public void initialize(Object previousState) throws SynchroFatalException {
		super.initialize(previousState);

		//Load structure from the previous sync
		Map<String, FileInfo> previousInfo = loadPreviousInfo(previousState);
		infos.put(SynchroSide.PREVIOUS, previousInfo);

		//Load structure from the distant folder
		Map<String, FileInfo> distantInfo = loadDistantInfo();
		infos.put(SynchroSide.DISTANT, distantInfo);

		//Load structure from the local folder
		Map<String, FileInfo> localInfo = loadLocalInfo();
		infos.put(SynchroSide.LOCAL, localInfo);

		//Build the outState that will be updated when actions are applied
		if (previousState == null) {
			outState = FileStructureFactory.cloneMap(localInfo);
		} else {
			outState = FileStructureFactory.cloneMap(previousInfo);
		}
	}

	protected Map<String, FileInfo> loadPreviousInfo(Object previousState) {
		Map<String, FileInfo> previousInfo;
		if (previousState == null) {
			previousInfo = new HashMap<String, FileInfo>();
		} else {
			previousInfo = (Map<String, FileInfo>) previousState;
		}
		return previousInfo;
	}

	protected Map<String, FileInfo> loadDistantInfo() throws SynchroFatalException {
		HttpClientService client = getParentService().getHttpClientService();
		HttpResponse response = null;
		try {
			HttpGet request = new HttpGet(client.encodeURL(getParentService().buildURL(SynchronisationServlet.FILE_INFO)));
			response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new SynchroFatalException("Error loading distant structure: " + response.getStatusLine());
			}
			HttpEntity entity = response.getEntity();
			return FileStructureFactory.readFromStream(entity.getContent());
		} catch (Exception ex) {
			throw new SynchroFatalException("Exception loading distant structure", ex);
		} finally {
			client.safeConsume(response);
		}
	}

	protected Map<String, FileInfo> loadLocalInfo() throws SynchroFatalException {
		try {
			FileStructureFactory fsf = FileStructureFactory.getInstance(getParentService().buildLocalFile(""));
			return fsf.fileTreeToMap(parentService.isManageDeletedFiles(), parentService.isManageDeletedFiles());
		} catch (Exception ex) {
			throw new SynchroFatalException("Exception loading local structure", ex);
		}
	}

	@Override
	public BaseSynchroService getParentService() {
		return (BaseSynchroService) super.getParentService();
	}

	@Override
	public List<String> getPathList(SynchroSide side) throws SynchroFatalException {
		Map<String, FileInfo> sideInfos = infos.get(side);
		return SynchroHelper.asSortedList(sideInfos.keySet());
	}

	@Override
	public SynchroState getState(SynchroSide side, String path) throws SynchroNonFatalException, SynchroFatalException {
		Map<String, FileInfo> sideInfos = infos.get(side);
		FileInfo info = sideInfos.get(path);
		if (info == null) {
			return SynchroState.UNKNOWN;
		} else {
			if (info.isDeleted()) {
				return SynchroState.DELETED;
			} else {
				return SynchroState.EXIST;
			}
		}
	}

	@Override
	public boolean equals(String path, SynchroSide side1, SynchroSide side2) throws SynchroNonFatalException, SynchroFatalException {
		FileInfo info1 = getInfo(side1, path);
		FileInfo info2 = getInfo(side2, path);
		boolean out;
		if (info1.isDirectory() == info2.isDirectory()) {
			if (info1.isDirectory()) {
				out = true;
			} else {
				out = info1.getSize() == info2.getSize();
				out = out && ResourceHelper.checksumEquals(info1.getChecksum(), info2.getChecksum());
			}
		} else {
			out = false;
		}
		return out;
	}

	public FileInfo getInfo(SynchroSide side, String path) {
		Map<String, FileInfo> sideInfos = infos.get(side);
		return sideInfos.get(path);
	}

	public String getChecksum(SynchroSide side, String path) {
		FileInfo info = getInfo(side, path);
		return info.getChecksum();
	}

	public FileInfo updateOutState(String path, File localFile, FileInfo fileInfo) {
		FileInfo outFI;
		if (localFile == null) {
			outState.remove(path);
			outFI = null;
		} else {
			outFI = new FileInfo(path, //
					fileInfo.isDirectory(), //
					localFile.exists() ? localFile.length() : -1, //
					localFile.exists() ? localFile.lastModified() : fileInfo.getModificationDate(), //
					localFile.exists() ? fileInfo.getChecksum() : null //
			);
			outState.put(path, outFI);
		}
		onChange();
		return outFI;
	}

	public List<FileInfo> getLocalDirectoryToDelete() {
		return localDirectoryToDelete;
	}

	public List<FileInfo> getDistantDirectoryToDelete() {
		return distantDirectoryToDelete;
	}

	@Override
	public Map<String, FileInfo> getOutState() {
		return outState;
	}

}