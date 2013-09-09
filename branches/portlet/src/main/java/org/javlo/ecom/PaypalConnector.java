package org.javlo.ecom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class PaypalConnector {

	private static void testPayPalConnectionSample() throws IOException {
		URL u = new URL("https://www.paypal.com/cgi-bin/webscr");
		URLConnection uc = u.openConnection();
		uc.setDoOutput(true);
		uc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		PrintWriter pw = new PrintWriter(uc.getOutputStream());
		pw.println("test=test");
		pw.close();

		BufferedReader in = new BufferedReader(
		new InputStreamReader(uc.getInputStream()));
		String res = in.readLine();
		in.close();
	}
	
	private static void testPayPalFromJavlo() throws Exception {
		String content = ResourceHelper.excutePost("https://api.sandbox.paypal.com/v1/oauth2/token", "grant_type=client_credentials", "application/x-www-form-urlencoded", "en_US", "EOJ2S-Z6OoN_le_KS1d75wsZ6y0SFdVsY9183IvxFyZp","EClusMEUk8e9ihI7ZdVLF5cZ6y0SFdVsY9183IvxFyZp");
		//String content = ResourceHelper.excutePost("https://google.com", "grant_type=client_credentials", "application/json", "en_US", "EOJ2S-Z6OoN_le_KS1d75wsZ6y0SFdVsY9183IvxFyZp","EClusMEUk8e9ihI7ZdVLF5cZ6y0SFdVsY9183IvxFyZp");
		
		System.out.println("content :");
		Map<String,String> response = URLHelper.getParams(content);
		System.out.println(content);
		System.out.println("");
		System.out.println("ERROR : "+StringHelper.neverNull(response.get("L_LONGMESSAGE0")).replace("%20", " "));
	}
	
	public static void main(String[] args) {
		try {
			testPayPalFromJavlo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
