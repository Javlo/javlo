package org.javlo.security.password;

import org.javlo.helper.StringHelper;

public class MD5Encryt implements IPasswordEncryption {

	@Override
	public String encrypt(String password) {
		return StringHelper.md5Hex(password);
	}

}
