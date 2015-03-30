package org.javlo.ecom;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.javlo.context.ContentContext;
import org.javlo.ecom.Product.ProductBean;

import com.paypal.api.payments.Amount;
import com.paypal.api.payments.Item;
import com.paypal.api.payments.ItemList;
import com.paypal.api.payments.Link;
import com.paypal.api.payments.Payer;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.api.payments.Transaction;
import com.paypal.core.rest.OAuthTokenCredential;
import com.paypal.core.rest.PayPalRESTException;

public class PaypalConnector {

	private String baseUrl;
	private String user;
	private String password;

	private Payment payment;

	public PaypalConnector(String baseUrl, String user, String password) {
		
		this.baseUrl = baseUrl;
		this.user = user;
		this.password = password;

		Properties prop = new Properties();
		prop.setProperty("service.EndPoint", baseUrl);

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

	public Payment getPayment() {
		return payment;
	}

	public static String formatDouble(double dbl) {
		return new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).format(dbl);
	}
	
	public String createPaypalPayment(ContentContext ctx,Basket basket, URL returnURL, URL cancelURL) throws Exception {
		OAuthTokenCredential tokenCredential = new OAuthTokenCredential(user, password);

		String accessToken = tokenCredential.getAccessToken();

		

		Amount amount = new Amount();
		amount.setTotal(formatDouble(basket.getTotal(ctx,true)));
		amount.setCurrency(basket.getCurrencyCode());
		// amount.setDetails(amountDetails);

		Transaction transaction = new Transaction();
		transaction.setAmount(amount);
		transaction.setDescription(basket.getDescription());
		
		ItemList itemList = new ItemList();
		List<Item> items = new LinkedList<Item>();
		for (ProductBean product : basket.getProductsBean()) {
			Item item = new Item();
			item.setName(product.getName());
			item.setPrice(formatDouble(product.getPrice()));
			item.setCurrency(product.getCurrencyCode());
			item.setQuantity(""+product.getQuantity());
			items.add(item);
		}
		itemList.setItems(items);
		
		/*ShippingAddress shippingAddress = new ShippingAddress();
		shippingAddress.setRecipientName(basket.getFirstName()+' '+basket.getLastName());
		shippingAddress.setPhone(basket.getContactPhone());
		shippingAddress.setLine1(basket.getAddress());
		shippingAddress.setCity(basket.getCity());
		shippingAddress.setCountryCode(StringHelper.neverNull(basket.getCountry()).toUpperCase());
		shippingAddress.setState(basket.getCity());
		shippingAddress.setPostalCode(basket.getZip());
		shippingAddress.setCity(basket.getCity());
		shippingAddress.setType("residential");
		itemList.setShippingAddress(shippingAddress);*/
		
		transaction.setItemList(itemList);

		List<Transaction> transactions = new ArrayList<Transaction>();
		transactions.add(transaction);

		Payer payer = new Payer();
		payer.setPaymentMethod("paypal");
		
		/*PayerInfo payerInfo = new PayerInfo();
		payerInfo.setFirstName(basket.getFirstName());
		payerInfo.setLastName(basket.getLastName());
		payerInfo.setEmail(basket.getContactEmail());
		payerInfo.setPhone(basket.getContactPhone());	
		payerInfo.setShippingAddress(billingAddress);
		payer.setPayerInfo(payerInfo);*/

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
		
		String json = payment.toJSON();
		System.out.println("");
		System.out.println("json");
		System.out.println(json);
		System.out.println("");

		for (Link link : payment.getLinks()) {
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

	public static void main(String[] args) {
		try {
			//testPayPalFromJavlo();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*private static void testPayPalFromJavlo() throws Exception {
		PaypalConnector c = new PaypalConnector("https://api.sandbox.paypal.com", "AdXPHxByf43Y9wg8YovePiWLpzqi62ow1M3PYQig61f2mQit5E6_E-hGBLca", "EFof4xCAgQevlK2AX7XJrhkZgfnUPd8iTVLH5_DsR6TvTn64yHiGBPKaGsh3");
		String url = testCreate(c);
		openUrl(url);
		String token = null;
		String payerID = null;
		testExecute(c, token, payerID);
	}*/

	private static String testCreate(ContentContext ctx, PaypalConnector c) throws Exception {
		Basket basket = new Basket();
		basket.setInfo("Basket info");
		basket.setOrganization("Terrieur SA");
		basket.setFirstName("Alain");
		basket.setLastName("Terrieur");
		basket.setAddress("13, Rue de la Folie");
		basket.setZip("1000");
		basket.setCity("Bruxelles");
		basket.setCountry("be");
		basket.setContactEmail("alain@terrieur.com");
		basket.setContactPhone("0123456789");
		List<ProductBean> products = new LinkedList<ProductBean>();
		ProductBean product = new ProductBean();
		product.setId("ID-ART-001");
		product.setName("Article 1");
		product.setDescription("Short Desc article 1");
		product.setPrice(12);
		product.setCurrencyCode("EUR");
		product.setQuantity(2);
		product.setVAT(0.21);
		product.setReduction(0);
		products.add(product);
		product = new ProductBean();
		product.setId("ID-ART-002");
		product.setName("Article 2");
		product.setDescription("Short Desc article 2");
		product.setPrice(25.1);
		product.setCurrencyCode("EUR");
		product.setQuantity(3);
		product.setVAT(0.21);
		product.setReduction(0);
		products.add(product);		
		String approvalUrl = c.createPaypalPayment(ctx,basket, new URL("http://localhost:8080/ecom/ok"), new URL("http://localhost:8080/ecom/cancel"));
		System.out.println("Approval url: " + approvalUrl);
		return approvalUrl;
	}

	private static void testExecute(PaypalConnector c, String token, String payerID) throws Exception {
		if (token == null && payerID == null) {
			BufferedReader re = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("'token' parameter:");
			token = re.readLine();
			System.out.println("'PayerID' parameter:");
			payerID = re.readLine();
		}
		String out = c.executePaypalPayment(token, payerID);
		System.out.println(out);
	}

	private static void openUrl(String url) throws Exception {
		if (Desktop.isDesktopSupported())
		{
			Desktop.getDesktop().browse(new URI(url));
		}
	}

}
