package org.javlo.remote;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTPAccess {

	public SFTPAccess() {
	}

	 public static void main(String[] args) throws JSchException, SftpException {
		 JSch jsch = new JSch();
		 Session session = jsch.getSession("web", "proxy.javlo.org", 22);
		  
		 // Java 6 version
		 session.setPassword("".getBytes(Charset.forName("UTF-8")));
		              
		 // Java 5 version
		 // session.setPassword(password.getBytes("ISO-8859-1"));
		  
		 Properties config = new java.util.Properties();
		 config.put("StrictHostKeyChecking", "no");
		 session.setConfig(config);
		  
		 session.connect();
		 
		 Channel channel = session.openChannel("sftp");
		 channel.connect();
		 ChannelSftp c = (ChannelSftp) channel;
		 Vector<LsEntry> v = c.ls("/etc/nginx/sites-available");
		 System.out.println("# : "+v.size());
		 for (LsEntry entry : v) {
			 System.out.println(entry.getLongname());
		 }
	}
	
}
