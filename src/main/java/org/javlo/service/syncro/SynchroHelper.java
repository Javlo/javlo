package org.javlo.service.syncro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.PersistenceService;
import org.javlo.service.syncro.exception.SynchroNonFatalException;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.thread.AbstractThread;

public class SynchroHelper {

	private static final Logger logger = Logger.getLogger(SynchroHelper.class.getName());

	public static final long BIG_FILE_SIZE = 50 * 1024 * 1024;
	public static final long SPLIT_FILE_SIZE = 1024 * 1024; //WARNING: SPLIT_FILE_SIZE <= BIG_FILE_SIZE

	private static final String SPLIT_FILE_PREFIX = "part.";
	private static final String SPLIT_FILE_SUFFIX = ".part";

	public static boolean isBigFile(long size) {
		return size > SynchroHelper.BIG_FILE_SIZE;
	}

	public static String buildSplitFolderPath(String path) {
		return URLHelper.mergePath("/.split-repo", path);
	}

	public static String buildSplitVersionFolderPath(String path, String checksum) {
		return URLHelper.mergePath(SynchroHelper.buildSplitFolderPath(path), "version-" + checksum);
	}

	public static String buildSplitFileName(long pos, long partLength, long parentSize) {
		return SPLIT_FILE_PREFIX + pos + "." + partLength + "." + parentSize + SPLIT_FILE_SUFFIX;
	}

	public static class SplitFileFilter implements FileFilter {
		private String prefix;
		public SplitFileFilter(boolean firstOnly) {
			prefix = SPLIT_FILE_PREFIX + (firstOnly ? "0." : "");
		}
		@Override
		public boolean accept(File pathname) {
			String name = pathname.getName();
			return pathname.isFile() && name.startsWith(prefix) && name.endsWith(SPLIT_FILE_SUFFIX);
		}
	}

	/**
	 * Split files if required.
	 * @param baseFolder
	 * @param map
	 * @return <code>true</code> if something changed
	 * @throws IOException
	 */
	public static boolean splitBigFiles(File baseFolder, Map<String, FileInfo> map) throws IOException {
		List<String> paths = asSortedList(map.keySet());
		boolean out = false;
		for (String path : paths) {
			FileInfo info = map.get(path);
			if (SynchroHelper.isBigFile(info.getSize())) {
				File folder = new File(baseFolder, SynchroHelper.buildSplitVersionFolderPath(path, info.getChecksum()));
				if (!folder.exists()) {
					logger.finer("split file to " + folder.getAbsolutePath());
					SynchroHelper.splitFile(new File(baseFolder, path), folder);
					out = true;
				}
			}
		}
		return out;
	}

	public static void splitFile(File localFile, File destFolder) throws IOException {
		destFolder.mkdirs();
		long size = localFile.length();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(localFile);
			in = new BufferedInputStream(in);
			long pos = 0, nextFilePos = 0;
			int b = in.read();
			while (b >= 0) {
				if (out != null) {
					ResourceHelper.safeClose(out);
				}
				String splitFile = SynchroHelper.buildSplitFileName(pos, SPLIT_FILE_SIZE, size);
				logger.finest("create split file " + destFolder + "/" + splitFile);
				out = new FileOutputStream(new File(destFolder, splitFile));
				out = new BufferedOutputStream(out);
				nextFilePos = pos + SPLIT_FILE_SIZE;

				while (b >= 0 && pos < nextFilePos) {
					out.write(b);
					pos++;

					b = in.read();
				}
			}
		} finally {
			ResourceHelper.safeClose(out, in);
		}
	}

	public static void rebuildSplitted(String baseFolder, String path, String checksum) throws IOException, SynchroNonFatalException {
		File parentFile = new File(baseFolder, path);
		if (parentFile.exists()) {
			String computed = ResourceHelper.computeChecksum(parentFile);
			if (ResourceHelper.checksumEquals(computed, checksum)) {
				return;
			}
			parentFile.delete();//TODO is it good solution?
		}

		String relativeFolder = buildSplitVersionFolderPath(path, checksum);
		File versionFolder = new File(baseFolder, relativeFolder);
		if (!versionFolder.exists()) {
			//TODO Better
			throw new SynchroNonFatalException("Split version not found: " + path + " [" + checksum + "]");
		}

		List<SplitFile> files = getSplitFiles(versionFolder);
		long parentSize = -1;
		long filesSize = 0;
		for (SplitFile file : files) {
			if (parentSize < 0 || file.getPos() == 0) {
				parentSize = file.getParentSize();
			}
			filesSize += file.getFile().length();
		}
		if (filesSize != parentSize) {
			//TODO Better
			throw new SynchroNonFatalException("Split incomplete: " + path + " [" + checksum + "]");
		}
		InputStream in = null;
		InputStream chkIn;
		OutputStream out = null;
		try {
			in = new SequenceInputStream(asInputStreamEnumeration(files));
			in = chkIn = ResourceHelper.getChecksumInputStream(in);
			parentFile.getParentFile().mkdirs();
			out = new FileOutputStream(parentFile);
			out = new BufferedOutputStream(out);
			int b = in.read();
			while (b >= 0) {
				out.write(b);
				b = in.read();
			}
			out.flush();
		} finally {
			ResourceHelper.safeClose(in, out);
		}
		String outChecksum = ResourceHelper.getChecksumResult(chkIn);
		if (!ResourceHelper.checksumEquals(outChecksum, checksum)) {
			parentFile.delete();//TODO is it good solution?
			throw new SynchroNonFatalException("Split rebuilded file incorrect checksum: " + path + " [" + checksum + "]");
		}
		logger.finer("Splitted file rebuild successful: " + path + " [" + checksum + "]");
	}

	private static Enumeration<InputStream> asInputStreamEnumeration(List<SplitFile> files) {
		final Iterator<SplitFile> iterator = files.iterator();
		return new Enumeration<InputStream>() {
			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}
			@Override
			public InputStream nextElement() {
				SplitFile splitFile = iterator.next();
				InputStream in;
				try {
					logger.finest("Opening split file: " + splitFile.getFile().getAbsolutePath());
					in = splitFile.getInputStream(true);
					in = new BufferedInputStream(in);
				} catch (IOException ex) {
					logger.log(Level.SEVERE, "Error opening split file: " + splitFile.getFile().getAbsolutePath(), ex);
					in = new ByteArrayInputStream(new byte[0]);
				}
				return in;
			}
		};
	}

	public static List<SplitFile> getSplitFiles(File versionFolder) {
		List<SplitFile> out = new LinkedList<SynchroHelper.SplitFile>();
		File[] files = versionFolder.listFiles(new SplitFileFilter(false));
		if (files != null) {
			for (File file : files) {
				out.add(new SplitFile(file));
			}
		}
		Collections.sort(out, new Comparator<SplitFile>() {
			@Override
			public int compare(SplitFile o1, SplitFile o2) {
				return o1.getPos().compareTo(o2.getPos());
			}
		});
		return out;
	}

	public static class SplitFile {
		private final File file;
		private final Long pos;
		private final Long partLength;
		private final Long parentSize;
		private FileInputStream in;

		public SplitFile(File file) {
			this.file = file;
			String name = file.getName().replace(SPLIT_FILE_PREFIX, "").replace(SPLIT_FILE_SUFFIX, "");
			String[] parts = name.split("\\.");
			pos = StringHelper.safeParseLong(parts[0], -1L);
			partLength = StringHelper.safeParseLong(parts[1], -1L);
			parentSize = StringHelper.safeParseLong(parts[2], -1L);
		}

		public File getFile() {
			return file;
		}

		public Long getPos() {
			return pos;
		}

		public Long getPartLength() {
			return partLength;
		}

		public Long getParentSize() {
			return parentSize;
		}

		public FileInputStream getInputStream(boolean open) throws IOException {
			if (in == null && open) {
				in = new FileInputStream(file);
			}
			return in;
		}

	}

	public static List<String> asSortedList(Set<String> set) {
		List<String> out = new LinkedList<String>(set);
		Collections.sort(out);
		return out;
	}

	public static String encodeURLPath(String path) {
		final String[][] REPLACEMENTS = { //
		{ "/", "__SLASH__", "/" }, //
				{ "\\", "__BACKSLASH__", "\\" }, //
				{ " ", "__SPACE__", "%20" }, //
		};
		for (String[] repl : REPLACEMENTS) {
			path = path.replace(repl[0], repl[1]);
		}
		try {
			path = URLEncoder.encode(path, ContentContext.CHARACTER_ENCODING);
		} catch (UnsupportedEncodingException ex) {
			//Theorically impossible
			ex.printStackTrace();
		}
		for (String[] repl : REPLACEMENTS) {
			path = path.replace(repl[1], repl[2]);
		}
		return path;
	}
	
	public static void deletedRemoteCacheFile(ContentContext ctx, String fileName) {
		String url = URLHelper.mergePath(ctx.getGlobalContext().getDMZServerIntra().toString(), BaseSynchroService.SERVLET_RELATIVE_PATH, SynchronisationServlet.CLEAR_CACHE_SPECIAL_FILE_NAME);
		url = URLHelper.addParam(url, "file", fileName);
		url = URLHelper.addParam(url, SynchronisationServlet.SHYNCRO_CODE_PARAM_NAME, ctx.getGlobalContext().getStaticConfig().getSynchroCode());
		try {
			NetHelper.readPageGet(new URL(url));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * launch synchronization 
	 * @param application
	 * @param staticConfig
	 * @param globalContext
	 * @return id of the thread
	 * @throws Exception
	 */
	public static String performSynchro(ContentContext ctx) throws Exception {		
		GlobalContext globalContext = ctx.getGlobalContext();		
		PersistenceService.getInstance(globalContext).store(ctx);
		StaticConfig staticConfig = globalContext.getStaticConfig();
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		if (globalContext.getDMZServerIntra() != null) {			
			SynchroThread synchro = (SynchroThread) AbstractThread.createInstance(staticConfig.getThreadFolder(), SynchroThread.class);
			synchro.initSynchronisationThread(staticConfig, globalContext, application);
			synchro.store();
			return synchro.getId();
		}
		return null;
	}

}
