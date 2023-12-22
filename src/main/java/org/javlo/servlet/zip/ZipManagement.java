/*
 * Created on 31-janv.-2004
 */
package org.javlo.servlet.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.service.ContentService;

import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionMethod;

/**
 * @author pvandermaesen
 */
public class ZipManagement {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ZipManagement.class.getName());

	public static void zipDirectory(OutputStream out, String sourceDir, HttpServletRequest request) throws IOException {
		zipDirectory(out, sourceDir, request, null, null);
	}

	private ZipOutputStream initializeZipOutputStream(File outputZipFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(outputZipFile);
		return new ZipOutputStream(fos);
	}

	public static void zipDirectory(OutputStream out, String sourceDir, HttpServletRequest request, Set<String> excludes, Set<String> includes) throws IOException {
		byte[] buff = new byte[4096];
		int readLen;
		
		sourceDir = URLHelper.cleanPath(sourceDir, false);

		try (ZipOutputStream zos = new ZipOutputStream(out)) {
			ZipParameters zipParameters = new ZipParameters();
			for (File fileToAdd : createFileList(sourceDir, excludes, includes)) {
				// Entry size has to be set if you want to add entries of STORE compression
				// method (no compression)
				// This is not required for deflate compression
				if (zipParameters.getCompressionMethod() == CompressionMethod.STORE) {
					zipParameters.setEntrySize(fileToAdd.length());
				}

				String fileName = URLHelper.cleanPath(fileToAdd.getAbsolutePath(), false).replace(sourceDir, "");
				if (fileName.startsWith("/")) {
					fileName = fileName.substring(1);
				}
				
				zipParameters.setFileNameInZip(fileName);
				zos.putNextEntry(zipParameters);

				try (InputStream inputStream = new FileInputStream(fileToAdd)) {
					while ((readLen = inputStream.read(buff)) != -1) {
						zos.write(buff, 0, readLen);
					}
				}
				zos.closeEntry();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		FileOutputStream out = new FileOutputStream(new File("c:/trans/test.zip"));
		zipDirectory(out, "C:/trans/xxx", null, null, null);
	}
	
	public static Collection<File> createFileList(String sourceDir, Set<String> excludes, Set<String> includes) throws IOException {
		return createFileList(sourceDir, sourceDir, excludes, includes);
	}

	
	private static Collection<File> createFileList(String basicSource, String sourceDir, Set<String> excludes, Set<String> includes) throws IOException {
		File[] files = ResourceHelper.getFileList(sourceDir);
		Collection<File> outFiles = new LinkedList<File>();
		for (File file2 : files) {
			String path = URLHelper.cleanPath(file2.getAbsolutePath(), false);
			path = path.replace(URLHelper.cleanPath(basicSource, false), "");
			if (excludes != null && URLHelper.contains(excludes, path, true)) {
				continue;
			}
			if (file2.isDirectory()) {
				outFiles.addAll(createFileList(basicSource, file2.getAbsolutePath(), excludes, includes));
			} else {
				if (includes != null && !URLHelper.contains(includes, path, true)) {
					continue;
				}
				outFiles.add(file2);
			}
		}
		return outFiles;
	}

	// public static void zipDirectoryJAVA(ZipOutputStream out, String targetDir,
	// String sourceDir, HttpServletRequest request, Set<String> excludes,
	// Set<String> includes) throws IOException {
	// if (targetDir == null) {
	// targetDir = "";
	// } else {
	// targetDir += '/';
	// }
	//
	// File[] files = ResourceHelper.getFileList(sourceDir, request);
	// for (File file2 : files) {
	// String name = targetDir + file2.getName();
	//
	// if (excludes != null && URLHelper.contains(excludes, name, true)) {
	// continue;
	// }
	// if (file2.isDirectory()) {
	// zipDirectory(out, name, sourceDir + '/' + file2.getName(), request, excludes,
	// includes);
	// } else {
	// if (includes != null && !URLHelper.contains(includes, name, true)) {
	// continue;
	// }
	// try {
	// ZipEntry entry = new ZipEntry(name);
	// out.putNextEntry(entry);
	// FileInputStream file = new FileInputStream(file2);
	//
	// try {
	// int size = ResourceHelper.writeStreamToStream(file, out);
	// entry.setSize(size);
	// } finally {
	// ResourceHelper.closeResource(file);
	// }
	//
	// } catch (IOException e) { // don't stop the for
	// e.printStackTrace();
	// }
	// out.closeEntry();
	// }
	// }
	// }

	public static void zipFile(File zipFile, File inFile) throws IOException {
		zipFile(zipFile, inFile, inFile.getParentFile());
	}

	public static void zipFile(File zipFile, File inFile, File refDir) throws IOException {
		if (!zipFile.exists()) {
			zipFile.createNewFile();
		}
		OutputStream out = new FileOutputStream(zipFile);
		java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(out);

		zipFile(outZip, inFile, refDir);

		outZip.close();
		out.close();
	}

	public static void gzipFile(File outFilename, File inFile) throws IOException {
		FileOutputStream fos = new FileOutputStream(outFilename);
		GZIPOutputStream gzos = new GZIPOutputStream(fos);
		FileInputStream fin = new FileInputStream(inFile);
		BufferedInputStream in = new BufferedInputStream(fin);

		try {
			byte[] buffer = new byte[1024];
			int i;
			while ((i = in.read(buffer)) >= 0) {
				gzos.write(buffer, 0, i);
			}
		} finally {
			ResourceHelper.closeResource(in);
			ResourceHelper.closeResource(fin);
			ResourceHelper.closeResource(gzos);
			ResourceHelper.closeResource(fos);
		}
	}

	public static void zipFile(java.util.zip.ZipOutputStream out, File inFile, File refDir) throws IOException {
		if (inFile.isDirectory()) {
			File[] files = inFile.listFiles();
			for (File file : files) {
				zipFile(out, file, refDir);
			}
		} else {
			String inPath = inFile.getAbsolutePath().replace('\\', '/');
			String refPath = refDir.getAbsolutePath().replace('\\', '/');

			String relativePath = inPath.replaceFirst(refPath, "").trim();
			if (StringHelper.isCharset(relativePath.getBytes(), ContentContext.CHARACTER_ENCODING)) {
				if (relativePath.startsWith("/")) {
					relativePath = relativePath.substring(1, relativePath.length());
				}

				try {
					ZipEntry entry = new ZipEntry(relativePath);
					out.putNextEntry(entry);
					FileInputStream file = new FileInputStream(inFile);

					int size = ResourceHelper.writeStreamToStream(file, out);
					entry.setSize(size);
					file.close();
				} catch (Throwable t) { // don't stop the for
					logger.warning("bad file name : " + inFile + " (" + t.getMessage() + ')');
					// t.printStackTrace();
				}
				out.closeEntry();
			}
		}
	}

	public static void addFileInZip(java.util.zip.ZipOutputStream out, String fileName, InputStream in) throws IOException {
		ZipEntry entry = new ZipEntry(fileName);
		out.putNextEntry(entry);
		int read = in.read();
		int size = 0;
		while (read >= 0) {
			out.write(read);
			read = in.read();
			size++;
		}
		entry.setSize(size);
		out.closeEntry();
	}

	public static File saveFile(ServletContext serveltContext, String dir, String fileName, InputStream in) throws IOException {
		String fullPath = dir + '/' + fileName;
		File file = new File(fullPath);
		if (fileName.endsWith("/") || fileName.endsWith("\\")) {
			file.mkdirs();
		} else {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				ResourceHelper.writeStreamToStream(in, out);
			} finally {
				ResourceHelper.closeResource(out);
			}
		}
		return file;
	}

	public static void uploadZipFile(ContentContext ctx, InputStream in) throws Exception {
		GlobalContext globalContext = ctx.getGlobalContext();
		StaticConfig staticConfig = globalContext.getStaticConfig();

		String dataFolder = globalContext.getDataFolder();
		if (staticConfig.isDownloadCleanDataFolder()) {
			ResourceHelper.moveToGlobalTrash(staticConfig, dataFolder);
		}
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			try {
				saveFile(ctx.getRequest().getSession().getServletContext(), dataFolder, entry.getName(), zipIn);
			} catch (Exception e) {
				logger.warning("Error on file : " + entry.getName() + " (" + e.getMessage() + ')');
			}
			entry = zipIn.getNextEntry();
		}
		zipIn.close();

		ContentService.getInstance(ctx.getRequest()).releaseAll(ctx, globalContext);
	}

	public static void uploadZipTemplate(String templateFolder, InputStream in, String templateId) throws Exception {
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		templateFolder = URLHelper.mergePath(templateFolder, templateId);

		while (entry != null) {
			File file = new File(URLHelper.mergePath(templateFolder, entry.getName()));
			if ((!file.getAbsolutePath().contains("/CVS")) && (!file.getAbsolutePath().contains("\\CVS"))) {
				if (!entry.isDirectory()) {
					file.getParentFile().mkdirs();
					try {
						file.createNewFile();
					} catch (Throwable t) {
						t.printStackTrace();
					}
					OutputStream out = new FileOutputStream(file);
					IOUtils.copy(zipIn, out);
					out.close();
				}
			}

			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
}
