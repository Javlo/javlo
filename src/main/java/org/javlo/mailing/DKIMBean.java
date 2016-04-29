package org.javlo.mailing;

public class DKIMBean {

	private String signingdomain;
	private String selector;
	private String privatekey;
	private String mailUser; 

	public DKIMBean(String signingdomain, String selector, String privatekey, String mailUser) {
		this.signingdomain = signingdomain;
		this.selector = selector;
		this.privatekey = privatekey;
		this.mailUser = mailUser;
	}

	public String getSigningdomain() {
		return signingdomain;
	}

	public String getSelector() {
		return selector;
	}

	public String getPrivatekey() {
		return privatekey;
	}

	public String getMailUser() {
		return mailUser;
	}

}
