package org.javlo.utils.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class BackupThread extends Thread {
	
	public static int INSTANCE_COUNT = 0;

	private static Logger logger = Logger.getLogger(BackupThread.class.getName());

	public static boolean RUN = true;
	
	public boolean run = true;

	private static final long WAIT_BETWEEN = 1000 * 60;
	
	private File targetFolder = null;
	private List<BackupBean> backupList = new LinkedList<BackupBean>();

	private final byte[] bytes = new byte[4096];

	public BackupThread(File targetFolder) {
		INSTANCE_COUNT++;
		this.targetFolder = targetFolder;
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		INSTANCE_COUNT--;
	}

	private void addToZipFile(String mainFolder, File file, ZipOutputStream zos) throws FileNotFoundException, IOException {
		String fileName = file.getAbsolutePath();
		FileInputStream fis = new FileInputStream(file);
		try {
			if (fileName.startsWith(mainFolder)) {
				fileName = fileName.substring(mainFolder.length());
			}
			ZipEntry zipEntry = new ZipEntry(fileName);
			zos.putNextEntry(zipEntry);

			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zos.write(bytes, 0, length);
			}
		} finally {
			zos.closeEntry();
			fis.close();
		}
	}

	private void backup(BackupBean bean) throws IOException {
		if (!bean.getFolder().exists() || bean.getFolder().listFiles().length == 0) {
			return;
		}
		File zipFile = new File(URLHelper.mergePath(targetFolder.getAbsolutePath(), bean.getFolder().getName() + "_" + bean.getFolder().hashCode(), StringHelper.renderFileTime(new Date()) + ".zip"));
		logger.info("backup start: " + zipFile);
		File backupFolder = zipFile.getParentFile();
		backupFolder.mkdirs();
		while (backupFolder.list().length >= bean.getBackupCount()) {
			Date oldDate = new Date();
			File oldestFile = null;
			for (File file : ResourceHelper.getAllFilesList(backupFolder)) {
				try {
					if (file.isFile()) {
						Date fd = StringHelper.parseFileTime(StringHelper.getFileNameWithoutExtension(file.getName()));
						if (fd.getTime() < oldDate.getTime()) {
							oldDate = fd;
							oldestFile = file;
						}
					}
				} catch (ParseException e) {
				}
				if (oldestFile != null) {
					oldestFile.delete();
				} else {
					logger.warning("no file found for delete : " + backupFolder);
				}
			}
		}
		FileOutputStream fos = new FileOutputStream(zipFile);
		ZipOutputStream zos = new ZipOutputStream(fos);
		try {
			for (File file : ResourceHelper.getAllFilesList(bean.getFolder())) {
				if (file.isFile()) {
					addToZipFile(bean.getFolder().getAbsolutePath(), file, zos);
				}
			}
		} finally {
			ResourceHelper.closeResource(zos);
			ResourceHelper.closeResource(fos);
		}
		logger.info("backup done: " + zipFile);
	}

	public void addBackup(BackupBean bean) {
		synchronized (backupList) {
			backupList.add(bean);
		}
	}

	@Override
	public void run() {
		while (RUN && run) {
			try {
				Thread.sleep(WAIT_BETWEEN);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized (backupList) {
				Date currentDate = new Date();
				for (BackupBean bean : backupList) {
					if (bean.getLatestBackup().getTime()+bean.getDelta()*1000 < currentDate.getTime()) {
						try {
							backup(bean);
							bean.setLatestBackup(currentDate);
						} catch (IOException e) {
							e.printStackTrace();
						}
						currentDate = new Date();
					}
				}
			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		File target = new File("c:/trans/backup");
		File source = new File("c:/trans/xxx");
		BackupBean bean = new BackupBean(source, 5, 40);
		BackupThread thread = new BackupThread(target);
		thread.addBackup(bean);
		thread.start();
		Thread.sleep(1000 * 60);
		thread.RUN = false;
	}

}
