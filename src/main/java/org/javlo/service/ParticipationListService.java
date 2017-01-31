package org.javlo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.commons.codec.binary.Base32;
import org.javlo.context.ContentContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class ParticipationListService {

	private static final int SIZE = 8;
	private File file;
	private Properties prop = new Properties();
	
	public static final ParticipationListService getInstance(ContentContext ctx) throws IOException {
		final File defaultFile = new File(URLHelper.mergePath(ctx.getGlobalContext().getStaticFolder(), "participation_number.properties"));
		return getInstance(ctx, defaultFile);
	}

	public static final ParticipationListService getInstance(ContentContext ctx, File listFile) throws IOException {
		String KEY = listFile.getAbsolutePath();
		ServletContext application = ctx.getRequest().getSession().getServletContext();
		ParticipationListService instance = (ParticipationListService) application.getAttribute(KEY);
		if (instance == null) {
			instance = new ParticipationListService();
			instance.file = listFile;
			if (!instance.file.exists()) {
				instance.file.createNewFile();
			}
			FileReader reader = new FileReader(instance.file);
			try {
				instance.prop.load(reader);
			} finally {
				ResourceHelper.closeResource(reader);
			}
			application.setAttribute(KEY, instance);
		}
		return instance;
	}

	/**
	 * create a static logger.
	 */
	protected static Logger logger = Logger.getLogger(ParticipationListService.class.getName());

	public static Collection<String> createListNumber(int size, long length) {
		if (length < 4) {
			throw new NumberFormatException("length is to small min 5.");
		}
		if (Math.pow(length - 4, 10) < size) {
			throw new NumberFormatException("length is to small for this list size.");
		}
		Set<String> outList = new HashSet<String>();
		while (outList.size() < size) {
			long base = Math.round(Math.random() * Math.pow(length - 4, 10));
			int mod = (int) base % 127;
			String modBase32 = StringHelper.asBase32((byte) mod);
			String renderNumber = String.format("%0" + (length - 2) + "d", base);
			String finalNumber = renderNumber + modBase32.replace("=", "");
			if (!outList.contains(finalNumber) && finalNumber.length() == length) {
				outList.add(finalNumber);
			}
		}
		return outList;
	}
	
	public boolean checkNumber(String number) {
		System.out.println("***** ParticipationListService.checkNumber : prop.get("+number+") = "+prop.get(number)); //TODO: remove debug trace
		if (StringHelper.isTrue(prop.get(number), true)) {
			return false;
		} else {
			prop.setProperty(number, "true");
			return true;
		}
	}

	public static boolean checkNumberValidity(String number) {
		if (number == null || number.length() < 5) {
			return false;
		}
		try {
			long base = Long.parseLong(number.substring(0, number.length() - 2));
			String mod32Text = number.substring(number.length() - 2);
			Base32 base32 = new Base32();
			int mod32Value = base32.decode(mod32Text)[0];
			return base % 127 == mod32Value;
		} catch (Throwable t) {
			logger.warning(t.getMessage());
			return false;
		}
	}

	public static void createFile(File file, int size, Iterator it) throws Exception {
		PrintStream out = new PrintStream(new FileOutputStream(file));
		for (int i = 0; i < size; i++) {
			String val = (String) it.next();
			System.out.println(val + " - " + checkNumberValidity(val));
			if (!checkNumberValidity(val) || val.length() != SIZE) {
				out.close();
				throw new Exception("error unvalid char generated : " + val);
			}
			out.println(val);
		}
		out.close();
	}
}
