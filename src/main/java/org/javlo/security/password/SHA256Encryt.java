package org.javlo.security.password;

import org.javlo.helper.StringHelper;

public class SHA256Encryt implements IPasswordEncryption {

	@Override
	public String encrypt(String password) {
		return StringHelper.encryptPasswordSHA256(password);
	}

}
