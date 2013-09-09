package org.javlo.helper;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;

public class VFSHelper {

	public static boolean closeManager(FileSystemManager manager) {
		if (manager instanceof DefaultFileSystemManager) {
			((DefaultFileSystemManager) manager).close();
			return true;
		}
		return false;
	}

	public static void closeFileSystem(FileObject file) {
		if (file != null) {
			try {
				file.close();
				file.getFileSystem().getFileSystemManager().closeFileSystem(file.getFileSystem());
			} catch (Exception ignored) {
				//ignore
			}
		}
	}

}
