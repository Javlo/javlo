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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.javlo.helper.ResourceHelper;
import org.javlo.utils.JSONMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.paypal.api.payments.Address;
import com.paypal.api.payments.Amount;
import com.paypal.api.payments.AmountDetails;
import com.paypal.api.payments.CreditCard;
import com.paypal.api.payments.FundingInstrument;
import com.paypal.api.payments.Link;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.PayerInfo;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

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

	private Payment payment;

	public PaypalConnector(String baseUrl, String user, String password) {
		System.out.println("***** PaypalConnector.PaypalConnector : baseUrl = " + baseUrl); // TODO:
																							// remove
																							// debug
																							// trace
		System.out.println("***** PaypalConnector.PaypalConnector : user = " + user); // TODO:
																						// remove
																						// debug
																						// trace
		System.out.println("***** PaypalConnector.PaypalConnector : password = " + password); // TODO:
																								// remove
																								// debug
																								// trace
		this.baseUrl = baseUrl;
		this.user = user;
		this.password = password;

		Properties prop = new Properties();
		prop.setProperty("service.EndPoint", baseUrl);
		prop.setProperty("clientID", user);
		prop.setProperty("clientSecret", password);

		prop.setProperty("http.ConnectionTimeOut", "5000");

		prop.setProperty("http.Retry", "1");
		prop.setProperty("http.ReadTimeOut", "30000");
		prop.setProperty("http.MaxConnection", "100");

		prop.setProperty("http.ProxyPort", "8080");
		prop.setProperty("http.ProxyHost", "127.0.0.1");
		prop.setProperty("http.ProxyUserName", "null");
		prop.setProperty("http.ProxyPassword", "null");

		prop.setProperty("http.GoogleAppEngine", "false");

		Payment.initConfig(prop);
	}

	protected String authenticate() throws IOException {
		String content = excutePost(baseUrl + AUTH_TOKEN_PATH, "grant_type=client_credentials", "application/x-www-form-urlencoded", user, password);
		JSONMap obj = JSONMap.parseMap(content);
		token = obj.getValue("access_token", String.class);
		return token;
	}

	public Payment getPayment() {
		return payment;
	}

	public static String formatDouble(double dbl) {
		return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(dbl);
	}

	public String createTestPaypalPayment(Basket basket, URL returnURL, URL cancelURL) throws Exception {
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(user, password);

		String accessToken = tokenCredential.getAccessToken();

		AmountDetails amountDetails = new AmountDetails();
		amountDetails.setSubtotal("7.41");
		amountDetails.setTax("0.03");
		amountDetails.setShipping("0.03");

		Amount amount = new Amount();
		amount.setTotal("7.47");
		amount.setCurrency("USD");
		amount.setDetails(amountDetails);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("This is the payment transaction description.");

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");

		Payment payment = new Payment();
		payment.setIntent("sale");
		payment.setPayer(payer);
		RedirectUrls urls = new RedirectUrls();
		urls.setCancelUrl(cancelURL.toString());
		urls.setReturnUrl(returnURL.toString());
		payment.setRedirectUrls(urls);
		payment.setTransactions(transactions);
		payment = payment.create(accessToken);

		String finalLink = null;

		for (Link link : payment.getLinks()) {
			System.out.println("***** PaypalConnector.createTestPaypalPayment : href = " + link.getHref()); // TODO:
																											// remove
																											// debug
																											// trace
			System.out.println("***** PaypalConnector.createPaypalPayment : link.getRel() = " + link.getRel()); // TODO:
																												// remove
																												// debug
																												// trace
			if (link.getRel().equals("approval_url")) {
				finalLink = link.getHref();
			}
		}

		return finalLink;
	}

	public String createPaypalPayment(Basket basket, URL returnURL, URL cancelURL) throws Exception {
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(user, password);

		String accessToken = tokenCredential.getAccessToken();

		Address billingAddress = new Address();
		billingAddress.setLine1(basket.getAddress());
		billingAddress.setCity(basket.getCity());
		billingAddress.setCountryCode(basket.getCountry());
		billingAddress.setPostalCode(basket.getZip());
		billingAddress.setCity(basket.getCity());

		Amount amount = new Amount();
		amount.setTotal(formatDouble(basket.getTotalIncludingVAT()));
		amount.setCurrency(basket.getCurrencyCode());
		// amount.setDetails(amountDetails);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription("Total: " + amount.getTotal() + ' ' + amount.getCurrency());

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");
		PayerInfo payerInfo = new PayerInfo();
		payerInfo.setFirstName(basket.getFirstName());
		payerInfo.setLastName(basket.getLastName());
		payerInfo.setEmail(basket.getContactEmail());
		payerInfo.setPhone(basket.getContactPhone());
		payerInfo.setShippingAddress(billingAddress);
		// payer.setPayerInfo(payerInfo);

		payment = new Payment();
		payment.setIntent("sale");
		payment.setPayer(payer);
		payment.setTransactions(transactions);
		RedirectUrls urls = new RedirectUrls();
		urls.setCancelUrl(cancelURL.toString());
		urls.setReturnUrl(returnURL.toString());
		payment.setRedirectUrls(urls);
		payment = payment.create(accessToken);

		String finalLink = null;

		for (Link link : payment.getLinks()) {
			System.out.println("***** PaypalConnector.createPaypalPayment : link.getRel() = " + link.getHref()); // TODO:
																													// remove
																													// debug
																													// trace
			System.out.println("***** PaypalConnector.createPaypalPayment : link.getRel() = " + link.getRel()); // TODO:
																												// remove
																												// debug
																												// trace
			if (link.getRel().equals("approval_url")) {
				finalLink = link.getHref();
			}
		}

		return finalLink;
	}

	public String executePaypalPayment(String paymentToken, String payerID) throws PayPalRESTException {
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(user, password);
		String accessToken = tokenCredential.getAccessToken();
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerID);
		Payment newPayment = payment.execute(accessToken, paymentExecution);
		for (Link link : newPayment.getLinks()) {
			return link.getHref();
		}
		return null;
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
		System.out.println("Create request: " + reqContent); // TODO remove

		String content = excutePost(baseUrl + CREATE_PAYMENT_PATH, reqContent, "application/json", null, token);
		System.out.println("Create response: " + content); // TODO remove

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
		// String token = authenticate();
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("payer_id", payerID);
		String reqContent = JSONMap.JSON.toJson(obj);
		System.out.println("***** PaypalConnector.executePayment : executeUrl = " + executeUrl); // TODO:
																									// remove
																									// debug
																									// trace
		String content = excutePost(executeUrl, reqContent, "application/json", null, token);
		JSONMap result = JSONMap.parseMap(content);
		return result.getValue("state", String.class);
	}

	private static String excutePost(String targetURL, String content, String contentType, String user, String pwd) throws IOException {
		if (content == null) {
			content = "";
		}
		// Map<String, String> params = URLHelper.getParams(urlParameters);
		// StringBuffer encodedParam = new StringBuffer();
		// String sep = "";
		// for (Map.Entry<String, String> param : params.entrySet()) {
		// encodedParam.append(sep);
		// encodedParam.append(param.getKey());
		// encodedParam.append("=");
		// encodedParam.append(URLEncoder.encode(param.getValue()));
		// sep = "&";
		// }

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

			// if (responseCode >= 200 && responseCode < 300) {
			// throw new IOException("Unexpected response HTTP Status: " +
			// responseCode + " " + connection.getResponseMessage());
			// }

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
			String user = "AZ1_hxCGaj9fSng1Xik5Egr03NqTWMzeiiUDK99dSJ5j6qsEVx1n_rjbu_lm";
			String pwd = "EAPINRCdAaPCf7NmGNDOwsv-bcCWbl1BJS3OYo4eQhv-M5-wmxN22PU0Ds7w";

			Basket basket = new TestBasket();
			PaypalConnector conn = new PaypalConnector("https://api.sandbox.paypal.com", user, pwd);
			System.out.println("***** PaypalConnector.main : url = " + conn.createTestPaypalPayment(basket, new URL("http://www.javlo.org/valid"), new URL("http://www.javlo.org/not_valid")));

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void testPayPalFromJavlo() throws Exception {
		PaypalConnector c = new PaypalConnector("https://api.sandbox.paypal.com", "AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca", "EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3");
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
