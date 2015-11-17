package org.javlo.mailing;

import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.helper.StringHelper;

public class MailConfig {
	
	private String SMTPHost = null;
	
	private String SMTPPort = "25";
	
	private String login = null;
	
	private String password = null;
	
	private String tempDir = null;
	
	public MailConfig(String host, int post, String login, String password) {
		this.SMTPHost = host;
		if (post > 0) {
		this.SMTPPort = ""+post;
		}
		this.login = login;
		this.password = password;		
	}

	public MailConfig(GlobalContext globalContext, StaticConfig staticConfig, Mailing mailing) {
		if (staticConfig != null) {
			tempDir = staticConfig.getTempDir();
		}
		if (mailing != null && !StringHelper.isEmpty(mailing.getSmtpHost())) {
			SMTPHost =  mailing.getSmtpHost();
			SMTPPort = mailing.getSmtpPort();
			login = mailing.getSmtpUser();
			password = mailing.getSmtpPassword();
		}
		if (globalContext != null && !StringHelper.isEmpty(globalContext.getSMTPHost())) {
			SMTPHost =  globalContext.getSMTPHost();
			SMTPPort = globalContext.getSMTPPort();
			login = globalContext.getSMTPUser();
			password = globalContext.getSMTPPassword();
		} else if (staticConfig != null && !StringHelper.isEmpty(staticConfig.getSMTPHost())) {
			SMTPHost =  staticConfig.getSMTPHost();
			SMTPPort = staticConfig.getSMTPPort();
			login = staticConfig.getSMTPUser();
			password = staticConfig.getSMTPPasswordParam();			
		}
	}

	public String getSMTPHost() {
		if (StringHelper.isEmpty(SMTPHost)) {
			return null;
		} else {
			return SMTPHost;
		}
	}

	public void setSMTPHost(String sMTPHost) {
		SMTPHost = sMTPHost;
	}

	public String getSMTPPort() {
		return SMTPPort;
	}
	
	public int getSMTPPortInt() {
		if (SMTPPort == null) {
			return 25;
		} else {
			return Integer.parseInt(SMTPPort);
		}
	}

	public void setSMTPPort(String sMTPPort) {
		SMTPPort = sMTPPort;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getTempDir() {
		return tempDir;
	}

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
	
	@Override
	public String toString() {	
		return "SMTP [host:"+getSMTPHost()+" port:"+getSMTPPort()+" user:"+getLogin()+" pwd?:"+!StringHelper.isEmpty(getPassword())+']';
	}

}
