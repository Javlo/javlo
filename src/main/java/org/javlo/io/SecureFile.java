package org.javlo.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

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

	public static final String FILE_SUFFIX = ".zip";

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
		if (!StringHelper.isEmpty(code)) {
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		}
		zipParameters.setFileNameInZip(file.getName());
		ZipFile zipFile;
		if (StringHelper.isEmpty(code)) {
			zipFile = new ZipFile(createSecuredFile(file));
		} else {
			zipFile = new ZipFile(createSecuredFile(file), code.toCharArray());
		}
		zipFile.addStream(in, zipParameters);
	}
	
	public static void decodeCyptedFile(File file, String code, OutputStream out) throws IOException {
		InputStream in = decodeCyptedFile(file, code);
		try {
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}
	
	public static InputStream decodeCyptedFile(File file, String code) throws IOException {
		ZipParameters zipParameters = new ZipParameters();
		if (!StringHelper.isEmpty(code)) {
			zipParameters.setEncryptFiles(true);
			zipParameters.setEncryptionMethod(EncryptionMethod.AES);
		}
		zipParameters.setFileNameInZip(file.getName());
		ZipFile zipFile;
		if (StringHelper.isEmpty(code)) {
			zipFile = new ZipFile(createSecuredFile(file));
		} else {
			zipFile = new ZipFile(createSecuredFile(file), code.toCharArray());
		}
		FileHeader fileHeader = zipFile.getFileHeader(file.getName());
		return zipFile.getInputStream(fileHeader);
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File file = new File("c:/trans/zip/image1.jpg");
		createCyptedFile(file, "", new FileInputStream(file));
		//decodeCyptedFile(file, "1234", new FileOutputStream(file));
	}

}
