package org.javlo.helper;import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.javlo.ecom.BankTransfert;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class QRCodeHelper {
	
	public static OutputStream createTransfertCode(BankTransfert transfert, OutputStream out) {
		QRCode.from(transfert.toString()).to(ImageType.PNG).withSize(512,512).writeTo(out);
		return out;
	}
	
	public static OutputStream createUrlCode(String url, OutputStream out) {
		return createUrlCode(url, out, null);
	}
	
	public static OutputStream createUrlCode(String url, OutputStream out, Integer size) {
		if (size == null) {
			size = 512;
		}
		QRCode.from(url).to(ImageType.PNG).withSize(size,size).writeTo(out);
		return out;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		File outPng = new File("c:/trans/qrcode.png");
//		BankTransfert transfert = new BankTransfert("BE72000000001616", "BPOTBEB1", 2,"croix rouge", "aide au dev.", null);
//		System.out.println(transfert);
		createUrlCode("http://www.javlo.org", new FileOutputStream(outPng)).close();
	}
}
