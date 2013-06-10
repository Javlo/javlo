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
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;
import org.javlo.i18n.I18nAccess;
import org.javlo.message.GenericMessage;
import org.javlo.message.MessageRepository;
import org.javlo.service.ContentService;

/**
 * @author pvandermaesen
 */
public class ZipManagement {

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ZipManagement.class
			.getName());

	public static void zipDirectory(ZipOutputStream out, String targetDir,
			String sourceDir, HttpServletRequest request) throws IOException {
		zipDirectory(out, targetDir, sourceDir, request, null, null);
	}

	public static void zipDirectory(ZipOutputStream out, String targetDir,
			String sourceDir, HttpServletRequest request, Set<String> excludes,
			Set<String> includes) throws IOException {
		if (targetDir == null) {
			targetDir = "";
		} else {
			targetDir += '/';
		}

		File[] files = ResourceHelper.getFileList(sourceDir, request);
		for (File file2 : files) {
			String name = targetDir + file2.getName();

			if (excludes != null && URLHelper.contains(excludes, name, true)) {
				continue;
			}
			if (file2.isDirectory()) {
				zipDirectory(out, name, sourceDir + '/' + file2.getName(),
						request, excludes, includes);
			} else {
				if (includes != null
						&& !URLHelper.contains(includes, name, true)) {
					continue;
				}
				ZipEntry entry = new ZipEntry(name);
				out.putNextEntry(entry);
				try {
					FileInputStream file = new FileInputStream(file2);

					try {
						int size = ResourceHelper
								.writeStreamToStream(file, out);
						entry.setSize(size);
					} finally {
						ResourceHelper.closeResource(file);
					}

				} catch (IOException e) { // don't stop the for
					e.printStackTrace();
				}
				out.closeEntry();
			}
		}
	}

	public static void zipFile(File zipFile, File inFile) throws IOException {
		zipFile(zipFile, inFile, inFile.getParentFile());
	}

	public static void zipFile(File zipFile, File inFile, File refDir)
			throws IOException {
		if (!zipFile.exists()) {
			zipFile.createNewFile();
		}
		OutputStream out = new FileOutputStream(zipFile);
		ZipOutputStream outZip = new ZipOutputStream(out);

		zipFile(outZip, inFile, refDir);

		outZip.close();
		out.close();
	}

	public static void gzipFile(File outFilename, File inFile)
			throws IOException {
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

	public static void zipFile(ZipOutputStream out, File inFile, File refDir)
			throws IOException {
		if (inFile.isDirectory()) {
			File[] files = inFile.listFiles();
			for (File file : files) {
				zipFile(out, file, refDir);
			}
		} else {
			String inPath = inFile.getAbsolutePath().replace('\\', '/');
			String refPath = refDir.getAbsolutePath().replace('\\', '/');

			String relativePath = inPath.replaceFirst(refPath, "").trim();
			if (StringHelper.isCharset(relativePath.getBytes(),
					ContentContext.CHARACTER_ENCODING)) {
				if (relativePath.startsWith("/")) {
					relativePath = relativePath.substring(1,
							relativePath.length());
				}

				try {
					ZipEntry entry = new ZipEntry(relativePath);
					out.putNextEntry(entry);
					FileInputStream file = new FileInputStream(inFile);

					int size = ResourceHelper.writeStreamToStream(file, out);
					entry.setSize(size);
					file.close();
				} catch (Throwable t) { // don't stop the for
					logger.warning("bad file name : " + inFile + " ("
							+ t.getMessage() + ')');
					// t.printStackTrace();
				}
				out.closeEntry();
			}
		}
	}

	public static void addFileInZip(ZipOutputStream out, String fileName,
			InputStream in) throws IOException {
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

	public static File saveFile(ServletContext serveltContext, String dir,
			String fileName, InputStream in) throws IOException {
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

	public static void uploadZipFile(HttpServletRequest request, HttpServletResponse response, InputStream in) throws Exception {
		StaticConfig staticConfig = StaticConfig.getInstance(request.getSession());
		GlobalContext globalContext = GlobalContext.getInstance(request);
		String dataFolder = globalContext.getDataFolder();
		if (staticConfig.isDownloadCleanDataFolder()) {
			ResourceHelper.moveToGlobalTrash(staticConfig, dataFolder);
		}
		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		while (entry != null) {
			try {
				saveFile(request.getSession().getServletContext(), dataFolder, entry.getName(), zipIn);
			} catch (Exception e){
				logger.warning("Error on file : "+entry.getName()+" ("+e.getMessage()+')');
			}
			entry = zipIn.getNextEntry();
		}
		zipIn.close();

		ContentService.getInstance(request).releaseAll(ContentContext.getContentContext(request, response), globalContext);
	}

	public static void uploadZipTemplate(ContentContext ctx, InputStream in,
			String templateId) throws Exception {
		I18nAccess i18nAccess = I18nAccess.getInstance(ctx.getRequest());
		MessageRepository msgRepo = MessageRepository.getInstance(ctx);

		ZipInputStream zipIn = new ZipInputStream(in);
		ZipEntry entry = zipIn.getNextEntry();
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest()
				.getSession());
		String templateFolder;
		templateFolder = URLHelper.mergePath(staticConfig.getTemplateFolder(),
				templateId);

		boolean foundIndex = false;
		boolean foundConfig = false;
		while (entry != null) {
			File file = new File(URLHelper.mergePath(templateFolder,
					entry.getName()));
			if ((!file.getAbsolutePath().contains("/CVS"))
					&& (!file.getAbsolutePath().contains("\\CVS"))) {
				if (!entry.isDirectory()) {
					file.getParentFile().mkdirs();
					try {
						if (file.getName().endsWith("index.html")) {
							foundIndex = true;
						}
						if (file.getName().endsWith("config.properties")) {
							foundConfig = true;
						}
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
		if (!foundIndex && foundConfig) {
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess
					.getText("command.admin.template.html-not-found"),
					GenericMessage.ERROR));
		}
		if (!foundConfig) {
			msgRepo.setGlobalMessage(new GenericMessage(i18nAccess
					.getText("command.admin.template.config-not-found"),
					GenericMessage.ERROR));
		}
		zipIn.close();
	}
}
