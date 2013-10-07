package org.javlo.ecom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.javlo.helper.ResourceHelper;
import org.javlo.utils.JSONMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class PaypalConnector {

	private static final String CHARSET = "UTF-8";
	private static final String AUTH_TOKEN_PATH = "/v1/oauth2/token";
	private static final String CREATE_PAYMENT_PATH = "/v1/payments/payment";

	private String baseUrl;
	private String user;
	private String password;

	private String token;
	private String approvalUrl;
	private String executeUrl;

	public PaypalConnector(String baseUrl, String user, String password) {
		System.out.println("***** PaypalConnector.PaypalConnector : baseUrl = "+baseUrl); //TODO: remove debug trace
		System.out.println("***** PaypalConnector.PaypalConnector : user = "+user); //TODO: remove debug trace
		System.out.println("***** PaypalConnector.PaypalConnector : password = "+password); //TODO: remove debug trace
		this.baseUrl = baseUrl;
		this.user = user;
		this.password = password;
	}

	protected String authenticate() throws IOException {
		String content = excutePost(baseUrl + AUTH_TOKEN_PATH, "grant_type=client_credentials", "application/x-www-form-urlencoded", user, password);
		JSONMap obj = JSONMap.parseMap(content);
		token = obj.getValue("access_token", String.class);
		return token;
	}
	
	public static String formatDouble(double dbl) {
		return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(dbl);
	}
	
	public String createPayment(double amountIn, String currencyIn, String descriptionIn, URL returnURL, URL cancelURL) throws IOException {
		String token = authenticate();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("intent", "sale");
		Map<String, String> urls = new LinkedHashMap<String, String>();
		obj.put("redirect_urls", urls);
		urls.put("return_url", returnURL.toString());
		urls.put("cancel_url", cancelURL.toString());
		Map<String, String> payer = new LinkedHashMap<String, String>();
		obj.put("payer", payer);
		payer.put("payment_method", "paypal");
		List<Map<String, Object>> transactions = new LinkedList<Map<String, Object>>();
		
		Map<String, Object> transaction = new LinkedHashMap<String, Object>();		
		Map<String, String> amount = new LinkedHashMap<String, String>();		
		amount.put("total", formatDouble(amountIn));
		amount.put("currency", currencyIn);
		transaction.put("amount", amount);		
		transaction.put("description", descriptionIn);
		transactions.add(transaction);
		obj.put("transactions", transactions);

		String reqContent = JSONMap.JSON.toJson(obj);
		System.out.println("Create request: " + reqContent); //TODO remove

		String content = excutePost(baseUrl + CREATE_PAYMENT_PATH, reqContent, "application/json", null, token);
		System.out.println("Create response: " + content); //TODO remove

		JSONMap result = JSONMap.parseMap(content);
		if (!"created".equals(result.getValue("state", String.class))) {
			throw new IllegalStateException("Payment not created");
		}
		List<JsonElement> links = result.getValue("links", new TypeToken<List<JsonElement>>() {
		}.getType());
		for (JsonElement linkJson : links) {
			JsonObject o = linkJson.getAsJsonObject();
			String rel = o.get("rel").getAsString();
			if ("approval_url".equals(rel)) {
				approvalUrl = o.get("href").getAsString();
			} else if ("execute".equals(rel)) {
				executeUrl = o.get("href").getAsString();
			}
		}
		return approvalUrl;
	}

	public String executePayment(String paymentToken, String payerID) throws IOException {
		//String token = authenticate();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("payer_id", payerID);
		String reqContent = JSONMap.JSON.toJson(obj);
		System.out.println("***** PaypalConnector.executePayment : executeUrl = "+executeUrl); //TODO: remove debug trace
		String content = excutePost(executeUrl, reqContent, "application/json", null, token);		
		JSONMap result = JSONMap.parseMap(content);
		return result.getValue("state", String.class);
	}

	private static String excutePost(String targetURL, String content, String contentType, String user, String pwd) throws IOException {
		if (content == null) {
			content = "";
		}
//		Map<String, String> params = URLHelper.getParams(urlParameters);
//		StringBuffer encodedParam = new StringBuffer();
//		String sep = "";
//		for (Map.Entry<String, String> param : params.entrySet()) {
//			encodedParam.append(sep);
//			encodedParam.append(param.getKey());
//			encodedParam.append("=");
//			encodedParam.append(URLEncoder.encode(param.getValue()));
//			sep = "&";
//		}

		HttpURLConnection connection = null;
		OutputStream outStream = null;
		InputStream inStream = null;
		ByteArrayOutputStream buffer = null;
		try {
			// Create connection
			URL url = new URL(targetURL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Accept-Language", "en_US");
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			// user authentification
			if (user != null && pwd != null) {
				connection.setRequestProperty("Authorization", "Basic " + DatatypeConverter.printBase64Binary((user + ':' + pwd).getBytes()));
			} else if (pwd != null) {
				connection.setRequestProperty("Authorization", "Bearer " + pwd);
			}

			// Send request
			outStream = connection.getOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), CHARSET);
			writer.write(content);
			writer.flush();

			int responseCode = connection.getResponseCode();

//			if (responseCode >= 200 && responseCode < 300) {
//				throw new IOException("Unexpected response HTTP Status: " + responseCode + " " + connection.getResponseMessage());
//			}

			// Get Response
			inStream = connection.getInputStream();
			int b = inStream.read();
			buffer = new ByteArrayOutputStream();
			while (b >= 0) {
				buffer.write(b);
				b = inStream.read();
			}
			return new String(buffer.toByteArray(), CHARSET);
		} finally {
			ResourceHelper.closeResource(outStream, inStream, buffer);
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public static void main(String[] args) {
		try {
			testPayPalFromJavlo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private static void testPayPalFromJavlo() throws Exception {
		PaypalConnector c = new PaypalConnector("https://api.sandbox.paypal.com",
				"AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca",
				"EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3");
		testCreate(c);		
		String token = null;
		String payerID = null;
		testExecute(c, token, payerID);
	}

	private static void testCreate(PaypalConnector c) throws Exception {
		String approvalUrl = c.createPayment(120, "EUR", "Test payment", new URL("http://localhost:8080/ecom/ok?prout=ettoi"), new URL("http://localhost:8080/ecom/cancel"));
		System.out.println("Token: " + c.token);
		System.out.println("Approval url: " + approvalUrl);
		System.out.println("Execute Url: " + c.executeUrl);
	}

	private static void testExecute(PaypalConnector c, String token, String payerID) throws Exception {
		if (token == null && payerID == null) {
			BufferedReader re = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("'token' parameter:");
			token = re.readLine();
			System.out.println("'PayerID' parameter:");
			payerID = re.readLine();
		}
		String out = c.executePayment(token, payerID);
		System.out.println(out);
	}

}
