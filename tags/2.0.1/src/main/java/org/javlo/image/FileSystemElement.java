/*
 * Created on 1 juil. 03
 */
package org.javlo.image;

import java.io.File;

/**
 * @author pvanderm
 */
public class FileSystemElement {

	File file;
	
	public String getFullName() {
		return file.getAbsolutePath();
	}

	public String getName() {
		return file.getName();
	}

	public String getLinkName() {
		StringBuffer strBuf = new StringBuffer();
		char[] nameArray = getName().toCharArray();
		for (int i = 0; i < nameArray.length; i++) {
			switch (nameArray[i]) {
				case ' ' :
					strBuf.append("%20");
					break;
				default :
					strBuf.append(nameArray[i]);
					break;
			}
		}
		return strBuf.toString();
	}
	
	public String getId() {
		StringBuffer strBuf = new StringBuffer();
		char[] nameArray = getName().toCharArray();
		for (int i = 0; i < nameArray.length; i++) {
			switch (nameArray[i]) {
				case ' ' :
					strBuf.append("");
					break;
				default :
					strBuf.append(nameArray[i]);
					break;
			}
		}
		return strBuf.toString();
	}


}
