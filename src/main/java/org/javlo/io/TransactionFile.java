package org.javlo.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

/**
 * class for overwrite data on a file, with temp file. Data will never only in
 * memory.
 * 
 * @author pvandermaesen
 * 
 */
public class TransactionFile {
	
	private static Logger logger = Logger.getLogger(TransactionFile.class.getName());

	private File targetFile = null;
	private File tempFile = null;
	private FileOutputStream out = null;

	public TransactionFile(File targetFile) throws IOException {
		this.targetFile = targetFile;
		tempFile = new File(targetFile.getAbsolutePath() + "_" + StringHelper.getRandomId() + ".tmp");
		if (tempFile.exists()) {
			throw new IOException("temp file : " + tempFile + " allready exist.");
		} else {
			tempFile.createNewFile();
		}
		out = new FileOutputStream(tempFile);
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public void commit() {
		File tempTargetFile = new File(targetFile.getAbsolutePath()+".temp_"+StringHelper.getRandomId());
		try {
			try {
				out.close();
				FileUtils.moveFile(targetFile, tempTargetFile);
				FileUtils.moveFile(tempFile, targetFile);
				tempFile = null;
				tempTargetFile.delete();
			} catch (IOException e) {
				try {
					FileUtils.moveFile(tempTargetFile, targetFile);
				} catch (IOException e1) {				
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

	public void rollback() throws IOException {
		logger.warning("rollback on : "+targetFile);
		try {
			out.close();
		} finally {
			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}

	public static void main(String[] args) {
		File target = new File("c:/trans/test.jpg");
		File source = new File("c:/trans/test2.jpg");

		TransactionFile tFile = null;
		try {
			tFile = new TransactionFile(target);
			ResourceHelper.writeFileToStream(source, tFile.getOutputStream());
			tFile.commit();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				tFile.rollback();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
