package org.javlo.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;

/**
 * write and read file with encryption
 * 
 * @author user
 *
 */
public class SecureFile {
	
	private static Logger logger = Logger.getLogger(SecureFile.class.getName());

	private static final String FILE_SUFFIX = "-secure.zip";

	public static File createSecuredFile(File file) {
		return new File(file.getAbsolutePath() + FILE_SUFFIX);
	}

	public static boolean isExist(File file) {
		if (file == null) {
			return false;
		}
		return createSecuredFile(file).exists();
	}

	public static void createCyptedFile(File file, String code, InputStream in) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setFileNameInZip(file.getName());
		ZipFile zipFile = new ZipFile(createSecuredFile(file), code.toCharArray());
		zipFile.addStream(in, zipParameters);
	}
	
	public static void decodeCyptedFile(File file, String code, OutputStream out) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setFileNameInZip(file.getName());
		ZipFile zipFile = new ZipFile(createSecuredFile(file), code.toCharArray());
		FileHeader fileHeader = zipFile.getFileHeader(file.getName());
		InputStream in = zipFile.getInputStream(fileHeader);
		try {
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}
	
	public static InputStream decodeCyptedFile(File file, String code) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		zipParameters.setEncryptFiles(true);
		zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		zipParameters.setFileNameInZip(file.getName());
		ZipFile zipFile = new ZipFile(createSecuredFile(file), code.toCharArray());
		FileHeader fileHeader = zipFile.getFileHeader(file.getName());
		return zipFile.getInputStream(fileHeader);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File file = new File("c:/trans/zip/image1.jpg");
		//createCyptedFile(file, "1234", new FileInputStream(file));
		decodeCyptedFile(file, "1234", new FileOutputStream(file));
	}

}
