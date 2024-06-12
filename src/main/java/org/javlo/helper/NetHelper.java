package org.javlo.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentStringParser;
import net.sf.uadetector.UserAgentType;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.javlo.bean.Company;
import org.javlo.component.core.ComponentBean;
import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.image.ImageHelper;
import org.javlo.image.ImageSize;
import org.javlo.mailing.MailConfig;
import org.javlo.mailing.MailService;
import org.javlo.mailing.MailingBuilder;
import org.javlo.navigation.MenuElement;
import org.javlo.service.notification.NotificationService;
import org.javlo.service.resource.VisualResource;
import org.javlo.servlet.FileServlet;
import org.javlo.servlet.IVersion;
import org.javlo.template.Template;
import org.javlo.user.IUserFactory;
import org.javlo.utils.MapCollectionWrapper;
import org.javlo.utils.TimeMap;
import org.javlo.ztatic.FileCache;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.*;
import java.util.zip.CRC32;

public class NetHelper {

	public static final String JAVLO_USER_AGENT = "Mozilla/5.0 bot Javlo/" + IVersion.VERSION;

	public static final String MOZILLA_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0";

	private static boolean INIT_HTTPS = false;

	private static final Map<String, Boolean> UserAgentCache = Collections.synchronizedMap(new TimeMap<String, Boolean>(60 * 60 * 24 * 30, 100000));

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NetHelper.class.getName());

	private static final String IMAGE_PAGE_FILE_KEY = "_images_from_page_analyse";

	private static final int MIN_IMAGE_SIZE = 640 * 480;

	public static final String HEADER_DATE = "Date";
	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String HEADER_ETAG = "ETag";
	public static final String HEADER_IF_MODIFIED_SINCE_ETAG = "if-None-Match";

	public static String readPageForMailing(URL url) throws Exception {
		return readPage(url, true, true, null, null, null, null, true);
	}

	public static String readPageForMailing(URL url, String login, String pwd) throws Exception {
		logger.info("url=" + url);
		return readPage(url, true, true, null, login, pwd, null, true);
	}

	public static String readPageForMailing(URL url, String token) throws Exception {
		logger.info("url : "+url);
		return readPage(url, true, true, null, null, null, token, true);
	}

	public static String readPage(URL url) throws Exception {
		return readPage(url, false, false, null, null, null, null, false);
	}

	public static String readPageGet(URLConnection conn, boolean checkReturnCode) throws Exception {
		// nocheckCertificatHttps();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				httpConn.setRequestProperty("User-Agent", MOZILLA_USER_AGENT);
				if (checkReturnCode && httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					throw new NetException("Response code: " + httpConn.getResponseCode());
				}
			}
			in = conn.getInputStream();
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
		return new String(out.toByteArray(), ContentContext.CHARACTER_ENCODING);
	}

	/**
	 * follow redirection and return final url
	 * 
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public static URL followURL(URL url) throws Exception {
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
			conn.setReadTimeout(5000);
			boolean redirect = true;
			int countRedirect = 0;
			while (redirect && countRedirect < 32) {
				int status = conn.getResponseCode();
				redirect = false;
				if (status != HttpURLConnection.HTTP_OK) {
					if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
						redirect = true;
				}
				if (redirect) {
					String newUrl = conn.getHeaderField("Location");
					conn.disconnect();
					conn = (HttpURLConnection) new URL(newUrl).openConnection();
				} else {
					URL outURL = conn.getURL();
					conn.disconnect();
					return outURL;
				}
				countRedirect++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getContentType(URL url) throws Exception {
		try {
			nocheckCertificatHttps();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
			conn.setReadTimeout(5000);
			boolean redirect = true;
			int countRedirect = 0;
			while (redirect && countRedirect < 16) {
				int status = conn.getResponseCode();
				redirect = false;
				if (status != HttpURLConnection.HTTP_OK) {
					if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
						redirect = true;
				}
				if (redirect) {
					String newUrl = conn.getHeaderField("Location");
					conn.disconnect();
					conn = (HttpURLConnection) new URL(newUrl).openConnection();
				} else {
					String contentType = conn.getHeaderField("Content-Type");
					conn.disconnect();
					return contentType;
				}
				countRedirect++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readPageGetFollowRedirect(URL url) throws Exception {
		try {
			nocheckCertificatHttps();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(5000);
			boolean redirect = true;
			int countRedirect = 0;
			while (redirect && countRedirect < 16) {
				int status = conn.getResponseCode();
				redirect = false;
				if (status != HttpURLConnection.HTTP_OK) {
					if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
						redirect = true;
				}
				if (redirect) {
					String newUrl = conn.getHeaderField("Location");
					conn.disconnect();
					conn = (HttpURLConnection) new URL(newUrl).openConnection();
				} else {
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String inputLine;
					StringBuffer html = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						html.append(inputLine);
					}
					in.close();
					conn.disconnect();
					return html.toString();
				}
				countRedirect++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String readPageGet(URL url) throws Exception {
		url = followURL(url);
		URLConnection conn = url.openConnection();
		return readPageGet(conn, true);
	}

	public static String readPageGet(URL url, Map<String,String> header) throws Exception {
		URLConnection conn = url.openConnection();
		for (Map.Entry<String,String> e : header.entrySet()) {
			conn.setRequestProperty(e.getKey(), e.getValue());
		}
		String content = readPageGet(conn, true);
		return content;
	}

	public static String readPageGet(URL url, boolean checkReturnCode) throws Exception {
		URLConnection conn = url.openConnection();
		String content = readPageGet(conn, checkReturnCode);
		return content;
	}

	public static String readPageNoError(URL url) throws Exception {
		return readPage(url, false, false, null, null, null, null, true);
	}

	public static String readPage(URL url, final String userName, final String password) throws Exception {
		return readPage(url, false, false, null, userName, password, null, false);
	}

	public static String readPage(String inURL, boolean cssInline) throws Exception {
		return readPage(new URL(inURL), cssInline, cssInline, null, null, null, null, false);
	}

	public static String readPage(URL url, boolean cssInline, String userAgent) throws Exception {
		return readPage(url, cssInline, cssInline, userAgent, null, null, null, false);
	}

	public static String postJsonRequest(URL url, String userAgent, Map<String, String> header, String json) throws Exception {
		logger.fine("postJsonRequest : " + url);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		InputStream in = null;
		try {
			url = removeParams(url);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// skip https validation
			if (connection instanceof HttpsURLConnection) {
				nocheckCertificatHttps();
			}

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			byte[] bytes = json.getBytes(ContentContext.CHARSET_DEFAULT);

			connection.setRequestProperty("Content-Length", "" + Integer.toString(bytes.length));
			connection.setRequestProperty("Accept-Charset", ContentContext.CHARACTER_ENCODING);

			for (Map.Entry<String, String> entry : header.entrySet()) {
				connection.setRequestProperty(entry.getKey(), entry.getValue());
			}

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.setAllowUserInteraction(true);
			connection.setInstanceFollowRedirects(true);

			if (userAgent != null) {
				connection.setRequestProperty("User-Agent", userAgent);
			}

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.write(bytes);
			wr.flush();
			wr.close();

			URLConnection conn = connection;

			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) conn;

				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM) {
					logger.warning("error readpage :  '" + url + "' return error code : " + ((HttpURLConnection) conn).getResponseCode());
					return null;
				}

				if (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https")) {
					if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM) {
						logger.warning("error readpage : " + httpConn.getResponseCode());
						return null;
					}
				}

			}
			in = conn.getInputStream();
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
		String content = new String(out.toByteArray(), ContentContext.CHARACTER_ENCODING);
		return content;
	}

	public static void nocheckCertificatHttps() throws NoSuchAlgorithmException, KeyManagementException {
		if (!INIT_HTTPS) {
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}

				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {

				}
			} };
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			INIT_HTTPS = true;
		}
	}

	public static void writeURLToStream(URL url, OutputStream out) throws Exception {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
		// skip https validation
		if (connection instanceof HttpsURLConnection) {
			nocheckCertificatHttps();
		}
		connection.setRequestMethod("GET");
		InputStream in = connection.getInputStream();
		try {
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	/**
	 * read a page a put content in a String.
	 * 
	 * @param url
	 *            a valid URL
	 * @return code returned by the http request on the URL.
	 * @throws IOException
	 */
	private static String readPage(URL url, boolean cssInline, boolean mailing, String userAgent, final String userName, final String password, String userToken, boolean noError) throws Exception {

		logger.fine("readPage : " + url + "  user:" + userName + "  password found:" + (StringHelper.neverNull(password).length() > 1) + "  token found:" + (StringHelper.neverNull(userToken).length() > 1));

		if (null != userName && userName.trim().length() != 0 && null != password && password.trim().length() != 0) {

			java.net.Authenticator.setDefault(new java.net.Authenticator() {

				protected java.net.PasswordAuthentication getPasswordAuthentication() {

					return new java.net.PasswordAuthentication(userName, password.toCharArray());

				}

			});

		}

		if (StringHelper.isVideo(url.getPath())) {
			return "";
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		InputStream in = null;
		try {

			if (userToken != null) {
				url = new URL(URLHelper.addParam(url.toString(), IUserFactory.TOKEN_PARAM, userToken));
			}

			String query = StringHelper.neverNull(url.getQuery(), "");
			url = removeParams(url);

			if (mailing && (url.getQuery() == null || !url.getQuery().contains(ContentContext.FORCE_ABSOLUTE_URL))) {
				url = new URL(URLHelper.addParam(url.toString(), ContentContext.FORCE_ABSOLUTE_URL, "true"));
			}

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", JAVLO_USER_AGENT);

			// skip https validation
			if (connection instanceof HttpsURLConnection) {
				nocheckCertificatHttps();
			}

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length", "" + Integer.toString(query.getBytes().length));
			// connection.setRequestProperty("Content-Language", "en-US");

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			connection.setAllowUserInteraction(true);
			connection.setInstanceFollowRedirects(true);

			if (userAgent != null) {
				connection.setRequestProperty("User-Agent", userAgent);
			}

			// Send request
			OutputStream outStr = connection.getOutputStream();
			DataOutputStream wr = new DataOutputStream(outStr);
			try {
				wr.writeBytes(query);
				wr.flush();
			} finally {
				ResourceHelper.closeResource(out, wr);
			}

			URLConnection conn = connection;

			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				if (!noError) {
					if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM) {
						logger.warning("error readpage :  '" + url + "' return error code : " + ((HttpURLConnection) conn).getResponseCode());
						return null;
					}

					if (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https")) {
						if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP && httpConn.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM) {
							logger.warning("error readpage : " + httpConn.getResponseCode());
							return null;
						}
					}
				}
			}
			int responseCode = connection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
				in = connection.getInputStream();
			} else {
				in = connection.getErrorStream();  // Use of getErrorStream to read the body of the response even in the event of an error
			}
			if (in == null) {
				logger.severe("connection not open[responseCode="+responseCode+"]");
			}
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
		String content = new String(out.toByteArray(), ContentContext.CHARACTER_ENCODING);
		
		if (mailing) {
			content = XHTMLHelper.prepareToMailing(content);
		}

		if (cssInline) {
			return CSSParser.mergeCSS(content, false);
		}
		return content;
	}

	/**
	 * read a page a put content in a String.
	 * 
	 * @param url
	 *            a valid URL
	 * @return code returned by the http request on the URL.
	 * @throws IOException
	 */
	public static JsonElement readJson(URL url) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		InputStream in = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);

			if (conn instanceof HttpURLConnection) {
				if (((HttpURLConnection) conn).getResponseCode() != HttpURLConnection.HTTP_OK) {
					logger.warning("help url '" + url + "' return error code : " + ((HttpURLConnection) conn).getResponseCode());
					return null;
				}
			}
			if (url.getProtocol().equalsIgnoreCase("http") || url.getProtocol().equalsIgnoreCase("https")) {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
				if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					return null;
				}
			}
			in = conn.getInputStream();
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}

		JsonParser parser = new JsonParser();
		return parser.parse(new String(out.toByteArray()));
	}

	public static Long readDate(URL url) throws Exception {
		URLConnection conn = url.openConnection();
		conn.connect();
		return conn.getDate();
	}

	public static void setJsonContentType(HttpServletResponse response) {
		response.setContentType("application/json");
	}

	/**
	 * read a page a put content in a Stream.
	 * 
	 * @param out
	 *            the output stream, it receive the url inputstream
	 * @return code returned by the http request on the URL.
	 * @throws IOException
	 */
	public static void readPage(URL url, OutputStream out) throws Exception {
		InputStream in = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
			in = conn.getInputStream();
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	/**
	 * read a page a put content in a Stream.
	 * 
	 * @param out
	 *            the output stream, it receive the url inputstream
	 * @return code returned by the http request on the URL.
	 * @throws IOException
	 */
	public static String readPageWithGet(URL url) {
		if (url == null) {
			return "";
		}
		InputStream in = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
			in = conn.getInputStream();
			return ResourceHelper.writeStreamToString(in, ContentContext.CHARACTER_ENCODING);
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return null;
		} finally {
			ResourceHelper.closeResource(in);
		}
	}

	/**
	 * extract the title of a web page.
	 * 
	 * @param url
	 * @return the title of the page.
	 * @throws Exception
	 */
	public static String getPageTitle(URL url) throws Exception {
		return getPageTitle(readPageGet(url));
	}

	/**
	 * extract the title of a web page.
	 * 
	 * @param URL
	 * @return the title of the page.
	 */
	public static String getPageTitle(String content) {

		if (content == null) {
			return "";
		}

		String contentLowerCase = content.toLowerCase();
		int indexTitleStart = contentLowerCase.indexOf("<title>");
		int indexTitleEnd = contentLowerCase.indexOf("</title>");

		if ((indexTitleStart >= 0) && (indexTitleEnd >= 0) && indexTitleEnd > indexTitleStart) {
			return content.substring(indexTitleStart + "<title>".length(), indexTitleEnd);
		}

		return null;
	}

	private static final String getMeta(Document doc, String field) {
		String value = null;
		Elements meta = doc.select("meta[property=og:" + field + "]");
		if (meta != null && !StringHelper.isEmpty(meta.attr("content"))) {
			value = meta.attr("content");
		} else {
			meta = doc.select("meta[name=twitter:" + field + "]");
			if (meta != null && !StringHelper.isEmpty(meta.attr("content"))) {
				value = meta.attr("content");
			} else {
				meta = doc.select("meta[property=article:" + field + "]");
				if (meta != null && !StringHelper.isEmpty(meta.attr("content"))) {
					value = meta.attr("content");
				} else {
					return null;
				}
			}
		}
		return value;
	}

	public static PageMeta getPageMeta(URL url) throws Exception {
		String html = readPageGet(url);
		PageMeta pageMeta = new PageMeta();
		Document doc = Jsoup.parse(html);

		String title = getMeta(doc, "title");
		if (StringHelper.isEmpty(title)) {
			return null;
		}
		pageMeta.setTitle(title);
		pageMeta.setUrl(url);
		String description = getMeta(doc, "description");
		pageMeta.setDescription(description);

		String imageUrl = getMeta(doc, "image");
		pageMeta.setImage(new URL(imageUrl));

		Locale locale = null;
		String locStr = getMeta(doc, "locale");
		;
		if (locStr != null) {
			if (locStr.contains("_")) {
				String[] locArray = locStr.split("_");
				if (locArray[0].length() == 2 && locArray[1].length() == 2) {
					locale = new Locale(locArray[0], locArray[1]);
				}
			} else {
				if (locStr.length() == 2) {
					locale = new Locale(locStr);
				}
			}
		}
		pageMeta.setLocale(locale);

		String dateStr = getMeta(doc, "modified_time");
		if (StringHelper.isEmpty(dateStr)) {
			dateStr = getMeta(doc, "published_time");
			if (StringHelper.isEmpty(dateStr)) {
				pageMeta.setDate(new Date(readDate(url)));
			}
		}
		if (!StringHelper.isEmpty(dateStr)) {
			Date date = null;
			try {
				date = StringHelper.parseIso8601(dateStr);
				pageMeta.setDate(date);
			} catch (ParseException e) {
				e.printStackTrace();
				pageMeta.setDate(new Date(readDate(url)));
			}

		}

		return pageMeta;
	}

	/**
	 * extract the title of a web page.
	 * 
	 * @param URL
	 * @return the title of the page.
	 */
	public static String getPageDescription(String content) {
		if (content == null) {
			return "";
		}
		String contentLowerCase = content.toLowerCase();
		int indexDescriptionStart = contentLowerCase.indexOf("name=\"description\"");
		if (indexDescriptionStart < 0) {
			return "";
		}
		indexDescriptionStart = contentLowerCase.indexOf("content=\"", indexDescriptionStart) + "content=\"".length();
		int indexDescriptionEnd = contentLowerCase.indexOf("\"", indexDescriptionStart + "content=\"".length() + 1);
		if ((indexDescriptionStart >= 0) && (indexDescriptionEnd >= 0) && indexDescriptionEnd > indexDescriptionStart) {
			return content.substring(indexDescriptionStart, indexDescriptionEnd);
		}

		return null;
	}

	public static boolean isUserAgentRobot(String userAgent) {
		if (userAgent == null) {
			return false;
		}
		return userAgent.contains("robo");
	}

	public static List<VisualResource> extractImage(URL inURL, String content, boolean needSize) {
		if (content == null) {
			return Collections.EMPTY_LIST;
		}
		List<VisualResource> urlList = new LinkedList<VisualResource>();

		int srcIndex = content.toLowerCase().indexOf("src=\"") + "src=\"".length();
		while (srcIndex >= "src=\"".length()) {
			int closeLink = content.indexOf("\"", srcIndex + 1);
			int closeTag = content.indexOf(">", srcIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(srcIndex, closeLink);
				int altIndex = content.toLowerCase().indexOf("alt=\"", srcIndex) + "alt=\"".length();
				String description = "";
				if (altIndex >= "alt=\"".length() && altIndex < closeTag) {
					try {
						description = content.substring(altIndex, content.indexOf("\"", altIndex + 1));
					} catch (Throwable e) {
					}
				}
				if (StringHelper.isImage(url)) {
					if (!URLHelper.isAbsoluteURL(url)) {
						if (!url.trim().startsWith("/")) {
							url = URLHelper.mergePath(URLHelper.extractPath(inURL.toString()), url);
						} else {
							url = "http://" + URLHelper.mergePath(URLHelper.extractHost(inURL.toString()), url);
						}
					}
					VisualResource res = new VisualResource();
					res.setId(url);
					res.setUri(url);
					res.setName(StringHelper.getFileNameFromPath(url));
					res.setDescription(StringHelper.removeTag(description));
					if (needSize) {
						ByteArrayOutputStream out = null;
						InputStream in = null;
						try {
							out = new ByteArrayOutputStream();
							in = new URL(url).openStream();
							ResourceHelper.writeStreamToStream(in, out);
							res.setSize(out.toByteArray().length);
							ByteArrayInputStream localIn = new ByteArrayInputStream(out.toByteArray());
							BufferedImage image = ImageIO.read(localIn);
							localIn.close();
							res.setWidth(image.getWidth());
							res.setHeight(image.getHeight());
							urlList.add(res);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							ResourceHelper.closeResource(in, out);
						}
					}
				}
			}
			srcIndex = content.toLowerCase().indexOf("src=\"", srcIndex) + "src=\"".length();
		}
		srcIndex = content.toLowerCase().indexOf("src='") + "src='".length();
		while (srcIndex >= "src='".length()) {
			int closeLink = content.indexOf("'", srcIndex + 1);
			int closeTag = content.indexOf(">", srcIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(srcIndex, closeLink);
				int altIndex = content.toLowerCase().indexOf("alt=\"", srcIndex) + "alt=\"".length();
				String description = "";
				if (altIndex >= "alt=\"".length() && altIndex < closeTag) {
					try {
						description = content.substring(altIndex, content.indexOf("\"", altIndex + 1));
					} catch (Throwable e) {
					}
				}
				if (StringHelper.isImage(url)) {
					if (!URLHelper.isAbsoluteURL(url)) {
						if (!url.trim().startsWith("/")) {
							url = URLHelper.mergePath(URLHelper.extractPath(inURL.toString()), url);
						} else {
							url = "http://" + URLHelper.mergePath(URLHelper.extractHost(inURL.toString()), url);
						}
					}
					VisualResource res = new VisualResource();
					res.setId(url);
					res.setUri(url);
					res.setName(StringHelper.getFileNameFromPath(url));
					res.setDescription(StringHelper.removeTag(description));
					if (needSize) {
						ByteArrayOutputStream out = null;
						InputStream in = null;
						try {
							out = new ByteArrayOutputStream();
							in = new URL(url).openStream();
							ResourceHelper.writeStreamToStream(in, out);
							res.setSize(out.toByteArray().length);
							ByteArrayInputStream localIn = new ByteArrayInputStream(out.toByteArray());
							BufferedImage image = ImageIO.read(localIn);
							localIn.close();
							res.setWidth(image.getWidth());
							res.setHeight(image.getHeight());
							urlList.add(res);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							ResourceHelper.closeResource(in, out);
						}
					}
				}
			}
			srcIndex = content.toLowerCase().indexOf("src='", srcIndex) + "src='".length();
		}

		return urlList;
	}

	public static List<String> extractURL(URL inURL, String content) {
		List<String> urlList = new LinkedList<String>();

		int httpIndex = content.toLowerCase().indexOf("http://");
		while (httpIndex >= 0) {
			int closeLink = content.indexOf("\"", httpIndex);
			if (closeLink >= 0) {
				String url = content.substring(httpIndex, closeLink);
				if (!url.contains(">")) {
					if (!urlList.contains(url)) {
						urlList.add(url);
					}
				}
			}
			httpIndex = content.toLowerCase().indexOf("http://", httpIndex + "http://".length());
		}

		int hrefIndex = content.toLowerCase().indexOf("href=\"") + "href=\"".length();
		while (hrefIndex >= "href=\"".length()) {
			int closeLink = content.indexOf("\"", hrefIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(hrefIndex, closeLink);
				if (!URLHelper.isAbsoluteURL(url)) {
					url = URLHelper.mergePath(URLHelper.extractPath(inURL.toString()), url);
				}
				if (!url.contains(">")) {
					if (!urlList.contains(url)) {
						urlList.add(url);
					}
				}
			}
			hrefIndex = content.toLowerCase().indexOf("href=\"", hrefIndex) + "href=\"".length();
		}

		int srcIndex = content.toLowerCase().indexOf("src=\"") + "src=\"".length();
		while (srcIndex >= "src=\"".length()) {
			int closeLink = content.indexOf("\"", srcIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(srcIndex, closeLink);
				if (!URLHelper.isAbsoluteURL(url)) {
					url = URLHelper.mergePath(URLHelper.extractPath(inURL.toString()), url);
				}
				if (!url.contains(">")) {
					if (!urlList.contains(url)) {
						urlList.add(url);
					}
				}
			}
			srcIndex = content.toLowerCase().indexOf("src=\"", srcIndex) + "src=\"".length();
		}

		return urlList;
	}

	public static List<String> extractExternalURL(URL inURL, String content) {
		List<String> urlList = new LinkedList<String>();

		String baseURL = URLHelper.extractPath(inURL.toString());
		int baseIndex = content.toLowerCase().indexOf("<base");
		if (baseIndex > 0) {
			String baseTag = content.substring(baseIndex, baseIndex + content.substring(baseIndex).indexOf('>'));
			int hrefIndex = baseTag.toLowerCase().indexOf("href");
			if (hrefIndex > 0) {
				int startIndex = hrefIndex + baseTag.substring(hrefIndex).indexOf('"');
				int endIndex = startIndex + baseTag.substring(startIndex + 1).indexOf('"') + 1;
				if (startIndex < hrefIndex) {
					startIndex = hrefIndex + baseTag.substring(hrefIndex).indexOf('\'');
					endIndex = startIndex + baseTag.substring(startIndex + 1).indexOf('\'') + 1;
				}
				baseURL = baseTag.substring(startIndex + 1, endIndex);
			}
		}
		int hrefIndex = content.toLowerCase().indexOf("href=\"") + "href=\"".length();
		while (hrefIndex >= "href=\"".length()) {
			int closeLink = content.indexOf("\"", hrefIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(hrefIndex, closeLink);
				if (!URLHelper.isAbsoluteURL(url)) {
					if (!url.startsWith("/")) {
						url = URLHelper.mergePath(baseURL, url);
					} else {
						URL baseURLParser;
						try {
							baseURLParser = new URL(baseURL);
							if (baseURLParser.getPort() > 0) {
								url = URLHelper.mergePath(baseURLParser.getProtocol() + ':' + baseURLParser.getPort() + "://" + baseURLParser.getHost(), url);
							} else {
								url = URLHelper.mergePath(baseURLParser.getProtocol() + "://" + baseURLParser.getHost(), url);
							}
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
					}
				}
				if (!url.contains(">")) {
					if (!urlList.contains(url)) {
						urlList.add(url);
					}
				}
			}
			hrefIndex = content.toLowerCase().indexOf("href=\"", hrefIndex) + "href=\"".length();
		}

		return urlList;
	}

	/**
	 * analyse a page and retreive a image
	 * 
	 * @param URL
	 *            the url of the page
	 * @param content
	 *            the content
	 * @return the uri to the local file
	 */
	public static String getLocalCopyOfPageImage(ContentContext ctx, URL inURL, String content, CRC32 crc32, boolean preferVertical, boolean needVertical) {
		StaticConfig staticConfig = StaticConfig.getInstance(ctx.getRequest().getSession());
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());
		return getLocalCopyOfPageImage(staticConfig.getCacheFolder(), globalContext.getDataFolder(), inURL, null, content, crc32, preferVertical, needVertical);
	}

	public static List<URL> extractMostSimilarLinks(URL url) throws Exception {
		String content = readPage(url);
		List<String> allLinks = extractExternalURL(url, content);

		MapCollectionWrapper<String, URL> urlByParentFolder = new MapCollectionWrapper<String, URL>();
		for (String link : allLinks) {
			urlByParentFolder.add(URLHelper.getParentURL(link), new URL(link));
		}
		// search the biggest url with the same parent folder.
		int biggestList = 1;
		String biggestKey = null;
		for (String key : urlByParentFolder.keySet()) {
			if (urlByParentFolder.get(key).size() > biggestList) {
				biggestList = urlByParentFolder.get(key).size();
				biggestKey = key;
			}
		}
		if (biggestKey == null) {
			return null;
		} else {
			return urlByParentFolder.get(biggestKey);
		}
	}

	/**
	 * analyse a page and retreive a image
	 * 
	 * @parem staticConfig pass the config (for call from a Thread )
	 * @param URL
	 *            the url of the page
	 * @param content
	 *            the content
	 * @return the uri to the local file
	 */
	public static String getLocalCopyOfPageImage(String cacheFolder, String dataFolder, URL pageURL, URL imageURL, String content, CRC32 crc32, boolean preferVertical, boolean needVertical) {

		logger.info("read : " + pageURL);

		if (content == null || content.trim().length() == 0) {
			logger.warning("bad content.");
		}

		if (needVertical) {
			preferVertical = true;
		}
		List<String> urls;
		if (imageURL != null) {
			urls = new LinkedList<String>();
			urls.add(imageURL.toString());
		} else {
			urls = extractExternalURL(pageURL, content);
		}
		logger.info("#URL found : " + urls.size());
		int maxSizeFound = -1;
		ImageSize bestImage = null;
		boolean bestImageJpg = false;
		String finalURL = null;
		ByteArrayOutputStream finalImgBuffer = null;

		boolean imageFoundedOk = false;
		ImageSize biggerImage = null;
		int bestBytesSize = 0;
		for (Iterator<String> iterator = urls.iterator(); iterator.hasNext() && !imageFoundedOk;) {
			String url = iterator.next();
			if (url.contains("?")) {
				url = url.substring(0, url.indexOf('?'));
			}
			if (StringHelper.isImage(url)) {
				URLConnection conn;
				try {
					ByteArrayOutputStream imgBuffer = new ByteArrayOutputStream();
					int imageSize = 0;
					conn = (new URL(url)).openConnection();
					conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
					conn.setRequestProperty("Referer", pageURL.toString());
					conn.setRequestProperty("Host", pageURL.getHost());
					conn.setReadTimeout(5000);
					InputStream in = conn.getInputStream();
					try {
						ResourceHelper.writeStreamToStream(in, imgBuffer);
					} finally {
						ResourceHelper.closeResource(in);
					}

					imgBuffer.close();
					// BufferedImage img = ImageIO.read(new
					// ByteArrayInputStream(imgBuffer.toByteArray()));

					byte[] imageArray = imgBuffer.toByteArray();
					ImageSize img = ImageHelper.getExifSize(new ByteArrayInputStream(imageArray));
					/*
					 * if (img == null) { img = ImageHelper.getJpegSize(new
					 * ByteArrayInputStream(imageArray)); }
					 */
					if (img == null) {
						BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(imgBuffer.toByteArray()));
						if (bufImg != null) {
							img = new ImageSize(bufImg.getWidth(), bufImg.getHeight());
						}
					}
					if (img != null) {
						imageArray = null;

						long readImageSize = img.getWidth() * img.getHeight();

						if ((float) img.getWidth() / (float) img.getHeight() > 0.40) { // no
							// banner
							if ((float) img.getHeight() / (float) img.getWidth() > 0.40) { // no
								// banner
								if (readImageSize > MIN_IMAGE_SIZE) {
									bestImage = img;
									maxSizeFound = imageSize;
									finalURL = url;
									String ext = StringHelper.getFileExtension(url).toLowerCase();
									if (ext.equals("jpg") || ext.equals("jpeg")) {
										bestImageJpg = true;
									}
								} else if (imageSize > maxSizeFound) {
									String ext = StringHelper.getFileExtension(url).toLowerCase();
									if (ext.equals("jpg") || ext.equals("jpeg")) {
										bestImage = img;
										maxSizeFound = imageSize;
										finalURL = url;
										bestImageJpg = true;
									} else if (!bestImageJpg) {
										bestImage = img;
										maxSizeFound = imageSize;
										finalURL = url;

									}
								}
								if (readImageSize > MIN_IMAGE_SIZE) {
									if (preferVertical) {
										if (bestImage != null && bestImage.getWidth() < bestImage.getHeight()) {
											imageFoundedOk = true;
										}
									} else {
										imageFoundedOk = true;
									}
								}
								if (imgBuffer.size() > bestBytesSize) {
									bestBytesSize = imgBuffer.size();
									biggerImage = bestImage;
									finalImgBuffer = imgBuffer;
								}
							}
						}
					} else {
						logger.warning("can not read image : " + url);
					}

				} catch (Exception e) {
					// just next image
					e.printStackTrace();
				}
			}
		}

		if (biggerImage != null) {
			if (needVertical) {
				if (biggerImage.getWidth() > biggerImage.getHeight()) {
					return null;
				}
			}
			String fileName = StringHelper.createFileName(finalURL);
			try {

				String fullFileName = URLHelper.mergePath(dataFolder, cacheFolder);
				fullFileName = URLHelper.mergePath(fullFileName, fileName);
				crc32.update(finalImgBuffer.toByteArray());
				FileCache.saveFile(fullFileName, new ByteArrayInputStream(finalImgBuffer.toByteArray()));

				return URLHelper.mergePath(cacheFolder, fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			logger.warning("image not found for url : " + pageURL);
			return null;
		}
	}

	public static boolean isURLValid(URL url) {
		return isURLValid(url, false);
	}

	public static boolean isURLValid(URL url, boolean only404) {
		try {
			if (CookieHandler.getDefault() == null) {
				CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
			}
			URLConnection urlConnection = url.openConnection();
			if (urlConnection instanceof HttpURLConnection) {
				HttpURLConnection conn = ((HttpURLConnection) urlConnection);
				if (conn instanceof HttpsURLConnection) {
					logger.info("init https context");
					try {
						TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
							@Override
							public java.security.cert.X509Certificate[] getAcceptedIssuers() {
								return null;
							}

							@Override
							public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
							}

							@Override
							public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
							}
						} };
						SSLContext sc = SSLContext.getInstance("SSL");
						sc.init(null, trustAllCerts, new java.security.SecureRandom());
						HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
						HostnameVerifier allHostsValid = new HostnameVerifier() {
							public boolean verify(String hostname, SSLSession session) {
								return true;
							}
						};
						((HttpsURLConnection) conn).setHostnameVerifier(allHostsValid);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					} catch (KeyManagementException e) {
						e.printStackTrace();
					}
				}
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
				int responseCode = ((HttpURLConnection) urlConnection).getResponseCode();
				if (only404) {
					return responseCode != 404;
				} else {
					return (responseCode >= 200) && (responseCode < 399);
				}
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.fine(e.getMessage());
			return false;
		}

	}

	public static List<URL> getLinks(String content, String inURL) {
		List<URL> urlList = new LinkedList<URL>();
		List<String> urlStrList = new LinkedList<String>();

		String lowerContext = content.toLowerCase();

		int hrefIndex = lowerContext.indexOf("href=\"") + "href=\"".length();
		while (hrefIndex >= "href=\"".length()) {
			int closeLink = lowerContext.indexOf("\"", hrefIndex + 1);
			if (closeLink >= 0) {
				String url = lowerContext.substring(hrefIndex, closeLink);
				if (!URLHelper.isAbsoluteURL(url)) {
					url = URLHelper.mergePath(URLHelper.extractPath(inURL.toString()), url);
				}
				if (!url.contains(">")) {
					if (!urlStrList.contains(url)) {
						try {
							URL newURL = new URL(url);
							urlStrList.add(url);
							urlList.add(newURL);
						} catch (MalformedURLException e) {
							// if URL malformed she is not added in the list
						}
					}
				}
			}
			hrefIndex = lowerContext.indexOf("href=\"", hrefIndex) + "href=\"".length();
		}

		return urlList;
	}

	public static List<URL> getExternalLinks(String content) {
		List<URL> urlList = new LinkedList<URL>();
		List<String> urlStrList = new LinkedList<String>();

		int hrefIndex = content.toLowerCase().indexOf("href=\"") + "href=\"".length();
		while (hrefIndex >= "href=\"".length()) {
			int closeLink = content.indexOf("\"", hrefIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(hrefIndex, closeLink);
				if (URLHelper.isAbsoluteURL(url)) {
					if (!url.contains(">")) {
						if (!urlStrList.contains(url)) {
							try {
								URL newURL = new URL(url);
								urlStrList.add(url);
								urlList.add(newURL);
							} catch (MalformedURLException e) {
								// if URL malformed she is not added in the list
							}
						}
					}
				}
			}
			hrefIndex = content.toLowerCase().indexOf("href=\"", hrefIndex) + "href=\"".length();
		}

		return urlList;
	}

	public static List<URL> getLinksFromText(String content) {
		List<URL> urlList = new LinkedList<URL>();
		List<String> urlStrList = new LinkedList<String>();

		int hrefIndex = content.toLowerCase().indexOf("href=\"") + "href=\"".length();
		while (hrefIndex >= "href=\"".length()) {
			int closeLink = content.indexOf("\"", hrefIndex + 1);
			if (closeLink >= 0) {
				String url = content.substring(hrefIndex, closeLink);
				if (URLHelper.isAbsoluteURL(url)) {
					if (!url.contains(">")) {
						if (!urlStrList.contains(url)) {
							try {
								URL newURL = new URL(url);
								urlStrList.add(url);
								urlList.add(newURL);
							} catch (MalformedURLException e) {
								// if URL malformed she is not added in the list
							}
						}
					}
				}
			}
			hrefIndex = content.toLowerCase().indexOf("href=\"", hrefIndex) + "href=\"".length();
		}

		return urlList;
	}

	/**
	 * check internet connection with stable server.
	 * 
	 * @return
	 */
	public static boolean isConnected() {
		return canReach("http://www.google.com") || canReach("http://www.belgium.be") || canReach("http://www.javlo.org");
	}

	/**
	 * check if the given url can be reached.
	 * 
	 * @param url
	 * @return
	 */
	public static boolean canReach(String url) {
		URLConnection c = null;
		try {
			c = (new URL(url)).openConnection();
			c.setUseCaches(false);
			c.setAllowUserInteraction(false);
			c.setConnectTimeout(1000);
			c.setReadTimeout(1000);
			c.connect();
			return true;
		} catch (UnknownHostException ignored) {
			// ignored;
		} catch (NoRouteToHostException ignored) {
			// ignored;
		} catch (java.net.SocketTimeoutException ex) {
			// ignored;
		} catch (IOException ignored) {
			// System.err.println(ignored.getClass().getName() + ": " +
			// ignored.getMessage());
			// ignored;
		} finally {
			if (c instanceof HttpURLConnection) {
				HttpURLConnection hc = (HttpURLConnection) c;
				try {
					hc.disconnect();
				} catch (Exception ignored) {
					// ignored
				}
			} else if (c != null) {
				try {
					c.getInputStream().close();
				} catch (Exception ignored) {
					// ignored
				}
			}
		}
		return false;
	}

	public static void sendMailToAdministrator(GlobalContext globalContext, String subject, String content) throws AddressException {
		sendMailToAdministrator(globalContext, new InternetAddress(globalContext.getAdministratorEmail()), subject, content);
	}

	public static void sendMailToAdministrator(GlobalContext globalContext, InternetAddress from, String subject, String content) {
		MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
		try {
			mailService.sendMail(from, new InternetAddress(globalContext.getAdministratorEmail()), subject, content, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendXHTMLMail(ContentContext ctx, InternetAddress from, InternetAddress to, InternetAddress cc, InternetAddress bcc, String subject, String content, String templateName) throws Exception {
		if (templateName == null) {
			templateName = "basic_mailing";
		}
		String contentId = StringHelper.getRandomId();

		// debug
		contentId = "142470258442657269888";

		ctx.getGlobalContext().addForcedContent(contentId, content);

		ContentContext pageCtx = ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE);
		pageCtx.setAbsoluteURL(true);
		String url = URLHelper.createURL(pageCtx, "/");
		url = URLHelper.addParam(url, ContentContext.FORCED_CONTENT_PREFIX + ComponentBean.DEFAULT_AREA, contentId);
		url = URLHelper.addParam(url, Template.FORCE_TEMPLATE_PARAM_NAME, templateName);
		String XHTMLContent = NetHelper.readPageGet(new URL(url));
		XHTMLContent = CSSParser.mergeCSS(XHTMLContent, false);
		sendMail(ctx.getGlobalContext(), from, to, cc, bcc, subject, XHTMLContent, StringHelper.removeTag(content), true);
	}

	public static boolean sendMail(GlobalContext globalContext, InternetAddress from, InternetAddress to, InternetAddress cc, InternetAddress bcc, String subject, String content) {
		MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
		try {
			mailService.sendMail(null, from, to, cc, bcc, subject, content, false);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean sendMail(GlobalContext globalContext, InternetAddress from, InternetAddress to, InternetAddress cc, InternetAddress bcc, String subject, String content, String contentTxt, boolean isHTML) {
		MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
		try {
			mailService.sendMail(null, from, to, cc, bcc, subject, content, contentTxt, isHTML, globalContext.getDKIMBean());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static void sendRedirectPermanently(HttpServletResponse response, String url) {
		response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		response.setHeader("Location", url);
		response.setHeader("Connection", "close");
	}

	public static void sendRedirectTemporarily(HttpServletResponse response, String url) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", url);
		response.setHeader("Connection", "close");
	}

	public static Cookie getCookie(HttpServletRequest request, String name) {
		if (request != null && request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie != null && cookie.getName() != null && cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	public static boolean isRobot(String userAgent) {
		String cacheKey = userAgent + "-robot";
		Boolean outVal = UserAgentCache.get(cacheKey);
		if (outVal == null) {
			if (userAgent.toLowerCase().contains("bot")) {
				outVal = true;
			} else {
				UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
				ReadableUserAgent agent = parser.parse(userAgent);
				outVal = agent.getType() == UserAgentType.ROBOT;
			}
			UserAgentCache.put(cacheKey, outVal);
		}
		return outVal;
		// if (userAgent == null) {
		// return false;
		// } else {
		// userAgent = userAgent.toLowerCase();
		// return userAgent.contains("bot") || userAgent.contains("robot");
		// }
	}

	public static boolean isMobile(String userAgent) {
		String cacheKey = userAgent + "-mobile";
		Boolean outVal = UserAgentCache.get(cacheKey);
		if (outVal == null) {
			if (userAgent.toLowerCase().contains("mobile")) {
				outVal = true;
			} else {
				UserAgentStringParser parser = UADetectorServiceFactory.getResourceModuleParser();
				ReadableUserAgent agent = parser.parse(userAgent);
				outVal = agent.getType() == UserAgentType.MOBILE_BROWSER;
			}
			UserAgentCache.put(cacheKey, outVal);
		}
		return outVal;

		/*
		 * if (userAgent == null) { return false; } else { userAgent =
		 * userAgent.toLowerCase(); return userAgent.contains("mobile"); }
		 */
	}

	/**
	 * remove params of a url
	 */
	public static URL removeParams(URL url) {
		if (url.getQuery() != null && url.getQuery().trim().length() > 0) {
			try {
				return new URL(url.toString().replace('?' + url.getQuery(), ""));
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return url;
			}
		} else {
			return url;
		}

	}

	public static void sendPageByMailing(ContentContext ctx, MenuElement page, String sender, String recipient, Map<String, Object> params) throws Exception {
		ctx = ctx.getContextOnPage(page);
		MailingBuilder mb = new MailingBuilder();
		mb.setSender(sender);
		mb.setRecipients(recipient);
		mb.setMaps(params);
		mb.setEditorGroups(new LinkedList<String>(page.getEditorRolesAndParent()));
		mb.setSubject(page.getTitle(ctx));
		mb.prepare(ctx);
		mb.sendMailing(ctx);
	}

	private static void testIt() {

		String https_url = "https://www.fidh.org/fr/regions/europe-asie-centrale/belgique/Accueil-des-personnes-handicapees";
		URL url;
		try {

			url = new URL(https_url);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

			// dumpl all cert info
			print_https_cert(con);

			// dump all the content
			// print_content(con);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * convert a ip in string (192.168.0.1) to a integer value.
	 * 
	 * @param ip
	 * @return
	 */
	public static int getIpAsInt(String ip) {
		Inet4Address a;
		try {
			a = (Inet4Address) InetAddress.getByName(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return 0;
		}
		byte[] b = a.getAddress();
		int i = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
		return i;
	}

	/**
	 * check if a ip is in a specific range
	 * 
	 * @param ip
	 * @param range
	 * @return
	 */
	public static boolean ipInRange(String ip, String range) {
		int index = range.indexOf('/');
		if (index < 0) {
			return ip.equals(range);
		} else {
			int subnet = getIpAsInt(range.substring(0, index));
			int bits = Integer.parseInt(range.substring(index + 1));

			int ipVal = getIpAsInt(ip);
			int mask = -1 << (32 - bits);

			if ((subnet & mask) == (ipVal & mask)) {
				return true;
			}
		}
		return false;

	}

	private static void print_https_cert(HttpsURLConnection con) {

		if (con != null) {

			try {

				System.out.println("Response Code : " + con.getResponseCode());
				System.out.println("Cipher Suite : " + con.getCipherSuite());
				System.out.println("\n");

				Certificate[] certs = con.getServerCertificates();
				for (Certificate cert : certs) {
					System.out.println("Cert Type : " + cert.getType());
					System.out.println("Cert Hash Code : " + cert.hashCode());
					System.out.println("Cert Public Key Algorithm : " + cert.getPublicKey().getAlgorithm());
					System.out.println("Cert Public Key Format : " + cert.getPublicKey().getFormat());
					System.out.println("\n");
				}

			} catch (SSLPeerUnverifiedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	public static boolean isIPAccepted(ContentContext ctx) {
		List<String> acceptedIPS = ctx.getGlobalContext().getStaticConfig().getIPMasks();
		if (acceptedIPS.isEmpty()) {
			return true;
		} else {
			String ip = ctx.getRealRemoteIp();
			for (String mask : acceptedIPS) {
				if (ipInRange(ip, mask)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Company validVATEuroparlEU(ContentContext ctx, String vat) throws MalformedURLException, Exception {
		if (ctx != null && !ctx.getGlobalContext().getStaticConfig().isInternetAccess() || vat == null) {
			return null;
		}
		vat = vat.replace(".", "");
		vat = vat.replace(" ", "");
		if (vat.length() != 12) {
			return null;
		}
		String country = vat.substring(0, 2);
		String number = vat.substring(2);
		if (!StringHelper.isDigit(number)) {
			return null;
		}
		String url = "http://ec.europa.eu/taxation_customs/vies/vatResponse.html?number=" + number + "&memberStateCode=" + country;
		String content = readPageGet(new URL(url));
		Document doc = Jsoup.parse(content);
		Element elem = doc.getElementById("vatResponseFormTable");
		Company company = new Company();
		if (elem != null) {
			Elements tds = elem.getElementsByTag("td");
			int i = 0;
			for (Element td : tds) {
				i++;
				if (i == 7) {
					company.setNumber(td.text());
				}
				if (i == 11) {
					company.setName(td.text());
				}
				if (i == 13) {
					company.setAddress(td.text());
				}
			}
		}
		return company;
	}

	/**
	 * insert and check ETag
	 * 
	 * @param ctx
	 * @param file
	 * @param secondaryHash
	 * @return true if content in cache (>> stop rendering)
	 * @throws IOException
	 */
	public static boolean insertEtag(ContentContext ctx, File file, String secondaryHash) throws IOException {
		if (file == null) {
			return false;
		}
		// Prepare some variables. The ETag is an unique identifier of the file.
		String fileName = file.getName();
		long length = file.length();
		long lastModified = file.lastModified();
		String eTag = fileName + "_" + length + "_" + lastModified;
		eTag = '"' + eTag + '"';
		if (secondaryHash != null) {
			eTag = eTag + secondaryHash;
		}
		long expires = System.currentTimeMillis() + FileServlet.DEFAULT_EXPIRE_TIME;
		HttpServletRequest request = ctx.getRequest();
		HttpServletResponse response = ctx.getResponse();

		// Validate request headers for caching
		// ---------------------------------------------------

		// If-None-Match header should contain "*" or ETag. If so, then return 304.
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && FileServlet.matches(ifNoneMatch, eTag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return true;
		}

		// If-Modified-Since header should be greater than LastModified. If so, then
		// return 304.
		// This header is ignored if any If-None-Match header is specified.
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return true;
		}

		// Validate request headers for resume
		// ----------------------------------------------------

		// If-Match header should contain "*" or ETag. If not, then return 412.
		String ifMatch = request.getHeader("If-Match");
		if (ifMatch != null && !FileServlet.matches(ifMatch, eTag)) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return true;
		}

		// If-Unmodified-Since header should be greater than LastModified. If not, then
		// return 412.
		long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
		if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return true;
		}

		response.setHeader("ETag", eTag);
		return false;

	}

	public static boolean testPort(String host, int port) {
		try (Socket ignored = new Socket(host, port)) {
			return false;
		} catch (IOException ignored) {
			return true;
		}
	}

	public static boolean insertEtag(ContentContext ctx, MenuElement page) throws Exception {
		if (page == null) {
			return false;
		}
		// Prepare some variables. The ETag is an unique identifier of the file.
		String pageName = page.getName();
		long lastModified = ctx.getGlobalContext().getPublishDate().getTime();
		String eTag = pageName + "_" + lastModified + "_" + page.hashCode();
		eTag = '"' + eTag + '"';
		if (!page.isCacheable(ctx) || !ctx.getGlobalContext().isPreviewMode()) {
			eTag = "W/" + eTag;
		}
		long expires = System.currentTimeMillis() + FileServlet.DEFAULT_EXPIRE_TIME;
		HttpServletRequest request = ctx.getRequest();
		HttpServletResponse response = ctx.getResponse();

		// Validate request headers for caching
		// ---------------------------------------------------

		// If-None-Match header should contain "*" or ETag. If so, then return 304.
		String ifNoneMatch = request.getHeader("If-None-Match");
		if (ifNoneMatch != null && FileServlet.matches(ifNoneMatch, eTag)) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return true;
		}

		// If-Modified-Since header should be greater than LastModified. If so, then
		// return 304.
		// This header is ignored if any If-None-Match header is specified.
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifNoneMatch == null && ifModifiedSince != -1 && ifModifiedSince + 1000 > lastModified) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			response.setHeader("ETag", eTag); // Required in 304.
			response.setDateHeader("Expires", expires); // Postpone cache with 1 week.
			return true;
		}

		// Validate request headers for resume
		// ----------------------------------------------------

		// If-Match header should contain "*" or ETag. If not, then return 412.
		String ifMatch = request.getHeader("If-Match");
		if (ifMatch != null && !FileServlet.matches(ifMatch, eTag)) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return true;
		}

		// If-Unmodified-Since header should be greater than LastModified. If not, then
		// return 412.
		long ifUnmodifiedSince = request.getDateHeader("If-Unmodified-Since");
		if (ifUnmodifiedSince != -1 && ifUnmodifiedSince + 1000 <= lastModified) {
			response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
			return true;
		}

		response.setHeader("ETag", eTag);
		return false;
	}

	public static String getIp(HttpServletRequest request) {
		String userIP = request.getHeader("x-real-ip");
		if (StringHelper.isEmpty(userIP)) {
			userIP = request.getRemoteAddr();
		}
		return userIP;
	}

	private static class DownloadThread extends Thread{

		private ContentContext ctx;
		private URL url;
		private File file;

		public DownloadThread(ContentContext ctx, URL url, File file) {
			this.ctx = ctx.getFreeContentContext();
			this.url = url;
			this.file = file;
		}

		@Override
		public void run() {
			super.run();
			String msg;
			boolean error = downloadFile(url, file);
			if (error) {
				msg = "download error please try agin : "+url+" x "+file.getName();
				logger.warning(msg);
			} else {
				msg = "download is finish : "+url+" > "+file.getName();
				logger.info(msg);
			}
			NotificationService.directUserNotification(ctx, error, "upload notification", msg);
		}
	}

	public static void downloadFileAsynchrone(ContentContext ctx, URL url, File file) {
		new DownloadThread(ctx, url, file).start();
	}

	public static boolean downloadFile(URL url, File file) {

		String newURL = url.toString();
		if (newURL.contains("dl=0")) {
			newURL = newURL.replace("dl=0", "dl=1");
		}

		if (newURL.contains("drive.google.com")) {
			String fileId = "";
			if (newURL.contains("/file/d/")) {
				fileId = newURL.split("/file/d/")[1].split("/")[0];
			} else if (newURL.contains("id=")) {
				fileId = newURL.split("id=")[1];
				int ampersandPosition = fileId.indexOf('&');
				if (ampersandPosition != -1) {
					fileId = fileId.substring(0, ampersandPosition);
				}
			}
			newURL = "https://drive.google.com/uc?export=download&id=" + fileId;
		}

		try {
			url = new URL(newURL);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		try {
			URLConnection conn = url.openConnection();
			conn.setRequestProperty("User-Agent", JAVLO_USER_AGENT);
			conn.setRequestProperty("Referer", url.toString());
			conn.setRequestProperty("Host", url.getHost());
			conn.setReadTimeout(5000);
			InputStream in = conn.getInputStream();
			try {
				ResourceHelper.writeStreamToFile(in, file);
				return false;
			} finally {
				ResourceHelper.closeResource(in);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

}
