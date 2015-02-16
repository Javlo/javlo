import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CheckServer {
	
	private static final int SMTP_PORT = 25;
	private static final String SMTP_HOST = "localhost";
	private static final String SMTP_HOST_PARAM = "mail.smtp.host";
	private static final String SMTP_PORT_PARAM = "mail.smtp.port";
	
	/*private static Properties getMailInfo() {
		Properties finalProps = new Properties();
		finalProps.put(SMTP_HOST_PARAM, SMTP_HOST);
		finalProps.put(SMTP_PORT_PARAM, SMTP_PORT);
		return finalProps;
	}
	
	public static final Transport getMailTransport() throws MessagingException {
		Session mailSession = Session.getDefaultInstance(getMailInfo());
		Transport transport = mailSession.getTransport("smtp");
		transport.connect(SMTP_HOST, null, null);
		return transport;
	}
	
	public static final void sendMail(String subject, String email, String message) throws MessagingException {
		Session mailSession = Session.getDefaultInstance(getMailInfo());
		MimeMessage msg = new MimeMessage(mailSession);
		msg.setSentDate(new Date());
		msg.setFrom(new InternetAddress("local-monitoring@javlo.org"));
		msg.setRecipients(Message.RecipientType.TO, new InternetAddress[] {new InternetAddress(email)});
		msg.setText(message);
		Transport transport = getMailTransport();
		try {
			transport.sendMessage(msg, msg.getAllRecipients());
		} finally {
			transport.close();
		}
	}*/

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length != 5) {
			System.err.println("bad parameters, use : url encoding keyword restartScript historyFolder.");
			return;
		}
		String inURL = args[0];
		String encoding = args[1];
		String keyword = args[2];
		String restartScript = args[3];
		String historyFolder = args[4];
		
		String errorMessage =  "keyword not found."; 
		
		URL url;
		try {
			url = new URL(inURL);
			URLConnection conn = url.openConnection();
			InputStream in = conn.getInputStream();		
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, encoding));
			String line = reader.readLine();
			while (line != null) {
				if (line.contains(keyword)) {
					System.out.println(inURL+" is UP.");
					return;
				}
				line = reader.readLine();
			}			
		} catch (Throwable e) {
			errorMessage = e.getMessage();
			e.printStackTrace();
		}
		
		/** restart **/
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		OutputStream outStream = new FileOutputStream(new File(historyFolder+'/'+"restart_"+format.format(new Date())+".txt"));
		PrintStream out = new PrintStream(outStream);
		
		format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		out.println("");
		out.println(format.format(new Date()));
		out.println("");
		out.println("Monitoring error on : "+inURL);
		out.println("");
		out.println("Error : "+errorMessage);
		out.println("");
		out.println("Restart feedback : ");
		
		try {
			Process p = new ProcessBuilder(restartScript, "").start();
			out.println("no IOException on restart.");
		} catch (IOException e) {	
			out.println("IOException error : "+e.getMessage());
			e.printStackTrace();
		}		
		out.close();		
		
		//sendMail("local monitoring error on "+inURL, email, mailBody);

	}

}

