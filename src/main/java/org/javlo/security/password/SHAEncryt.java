package org.javlo.security.password;

import org.javlo.helper.StringHelper;

public class SHAEncryt implements IPasswordEncryption {

	@Override
	public String encrypt(String password) {
		return StringHelper.encryptPassword(password);
	}

}
