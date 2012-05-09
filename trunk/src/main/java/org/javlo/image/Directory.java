/*
 * Created on 30 juin 2003
 
 */
package org.javlo.image;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author pvanderm
 */
public class Directory extends FileSystemElement {

	public Directory(String path) throws PathNotExistException {
		this(new File(path));
	}

	Directory(File newFile) throws PathNotExistException {

		if (newFile == null) {
			throw new NullPointerException("file can not be null in Directory contructor.");
		}
		if (!newFile.exists()) {
			throw new PathNotExistException("path: " + newFile.getPath() + " not exist.");
		}
		file = newFile;
	}

	public boolean access(Set passwords) {
		boolean res = false;
		if ( getPasswords() == null  ) {
			res = true;
		} else if (passwords != null) {
			Set dirPass = getPasswords();
			dirPass.retainAll(passwords);
			if (dirPass.size() > 0) {
				res = true;
			}
		}
		return res;
	}

	public Directory[] getAccessibleChild(Set password) {
		ArrayList dirList = new ArrayList();
		Directory[] childs = getChild();
		for (int i = 0; i < childs.length; i++) {
			if (childs[i].access(password)) {
				dirList.add(childs[i]);
			}
		}
		Directory[] res = new Directory[dirList.size()];
		dirList.toArray(res);
		return res;
	}

	public Directory[] getChild() {
		ArrayList dirList = new ArrayList();
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				try {
					dirList.add(new Directory(files[i]));
				} catch (PathNotExistException e) {
					e.printStackTrace();
				}
			}
		}
		Directory[] res = new Directory[dirList.size()];
		dirList.toArray(res);
		return res;
	}

	boolean isImage(String name) {
		boolean res = false;
		if (name.lastIndexOf(".") > 0) {
			String ext = "";
			if (name.lastIndexOf(".") < name.length()) {
				ext = name.substring(name.lastIndexOf(".") + 1, name.length());
			}
			if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg")) {
				res = true;
			}
		}
		return res;
	}

	boolean isZip(String name) {
		boolean res = false;
		if (name.lastIndexOf(".") > 0) {
			String ext = "";
			if (name.lastIndexOf(".") < name.length()) {
				ext = name.substring(name.lastIndexOf(".") + 1, name.length());
			}
			if (ext.equalsIgnoreCase("zip") || ext.equalsIgnoreCase("gzip")) {
				res = true;
			}
		}
		return res;
	}

	public FileSystemElement[] getAccessibleAll(Set passwords) {
		FileSystemElement[] res = null;
		ArrayList resList = new ArrayList();
		resList.addAll(Arrays.asList(getAccessibleChild(passwords)));
		resList.addAll(Arrays.asList(getImages()));
		res = new FileSystemElement[resList.size()];
		resList.toArray(res);
		return res;
	}

	public FileSystemElement[] getAll() {
		FileSystemElement[] res = null;
		ArrayList resList = new ArrayList();
		resList.addAll(Arrays.asList(getChild()));
		resList.addAll(Arrays.asList(getImages()));
		res = new FileSystemElement[resList.size()];
		resList.toArray(res);
		return res;
	}

	public Image[] getImages() {
		ArrayList imgList = new ArrayList();
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && isImage(files[i].getName())) {
				imgList.add(new Image(files[i]));
			}
		}
		Image[] res = new Image[imgList.size()];
		imgList.toArray(res);
		return res;
	}

	public int countImages() {
		int imageCount = 0;
		File[] files = file.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile() && isImage(files[i].getName())) {
				imageCount++;
			} else if (files[i].isDirectory()) {
				imageCount++;
			}
		}
		return imageCount;
	}

	public int lastPage(int imagesPerPage) {
		int countPage = 1;
		if (imagesPerPage != 0) {
			int countImages = countImages();
			if ((countImages % imagesPerPage) == 0) {
				countPage = countImages / imagesPerPage;
			} else {
				countPage = countImages / imagesPerPage + 1;
			}
		}
		return countPage;
	}

	/**
	 * return a set with the password for acces to this directory. 
	 * @return null if file not exist.
	 */
	public Set getPasswords() {
		Set passwords = null;
		File passwordFile = new File(file.getAbsolutePath() + "/.passwords");
		if (passwordFile.exists()) {
			passwords = new HashSet();
			try {
				FileReader in = new FileReader(passwordFile);
				BufferedReader reader = new BufferedReader(in);
				String line = reader.readLine();	
				while (line != null) {					
					passwords.add(line.trim());
					line = reader.readLine();
				}				
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Directory parent = null;
			try {
				if (file.getParentFile() != null) {
					parent = new Directory(file.getParentFile());
				}
			} catch (PathNotExistException e) {
				e.printStackTrace();
			}
			if (parent != null) {
				passwords = parent.getPasswords();
			}
		}
		return passwords;
	}
}
