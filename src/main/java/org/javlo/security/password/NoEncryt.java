package org.javlo.security.password;

public class NoEncryt implements IPasswordEncryption {

	@Override
	public String encrypt(String password) {
		return password;
	}

}
