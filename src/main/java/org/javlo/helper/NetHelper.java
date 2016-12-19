package org.javlo.helper;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.javlo.service.resource.VisualResource;
import org.javlo.template.Template;
import org.javlo.user.IUserFactory;
import org.javlo.utils.MapCollectionWrapper;
import org.javlo.ztatic.FileCache;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class NetHelper {

	/**
	 * create a static logger.
	 */
	protected static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NetHelper.class.getName());

	private static final String IMAGE_PAGE_FILE_KEY = "_images_from_page_analyse";

	private static final int MIN_IMAGE_SIZE = 640 * 480;

	public static final String HEADER_LAST_MODIFIED = "Last-Modified";
	public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String HEADER_ETAG = "ETag";
	public static final String HEADER_IF_MODIFIED_SINCE_ETAG = "if-None-Match";

	public static String readPageForMailing(URL url) throws Exception {
		return readPage(url, true, true, null, null, null, null, false);
	}

	public static String readPageForMailing(URL url, String login, String pwd) throws Exception {
		return readPage(url, true, true, null, login, pwd, null, false);
	}

	public static String readPageForMailing(URL url, String token) throws Exception {
		return readPage(url, true, true, null, null, null, token, false);
	}

	public static String readPage(URL url) throws Exception {
		return readPage(url, false, false, null, null, null, null, false);
	}

	public static String readPageGet(URLConnection conn, boolean checkReturnCode) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		InputStream in = null;
		try {
			if (conn instanceof HttpURLConnection) {
				HttpURLConnection httpConn = (HttpURLConnection) conn;
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

	public static String readPageGet(URL url) throws Exception {
		URLConnection conn = url.openConnection();
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

	/**
	 * read a page a put content in a String.
	 * 
	 * @param url
	 *            a valid URL
	 * @return code returned by the http request on the URL.
	 * @throws IOException
	 */
	private static String readPage(URL url, boolean cssInline, boolean mailing, String userAgent, final String userName, final String password, String userToken, boolean noError) throws Exception {

		logger.info("readPage : " + url + "  user:" + userName + "  password found:" + (StringHelper.neverNull(password).length() > 1) + "  token found:" + (StringHelper.neverNull(userToken).length() > 1));

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

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(query);
			wr.flush();
			wr.close();

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
			in = conn.getInputStream();
			ResourceHelper.writeStreamToStream(in, out);
		} finally {
			ResourceHelper.closeResource(in);
		}
		String content = new String(out.toByteArray(), ContentContext.CHARACTER_ENCODING);
		if (mailing) {
			content = XHTMLHelper.prepareToMailing(content); // transform list
																// -> array
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
			in = url.openConnection().getInputStream();
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
			in = url.openConnection().getInputStream();
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
			return null;
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
					description = content.substring(altIndex, content.indexOf("\"", altIndex + 1));
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

						int readImageSize = img.getWidth() * img.getHeight();

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
	
	public static void main(String[] args) throws MalformedURLException {
		boolean valid = isURLValid(new URL("http://www.lesoir.be/"), true);
		System.out.println("***** NetHelper.main : valid = "+valid); //TODO: remove debug trace
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
				conn.setConnectTimeout(10 * 1000);
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

	public static void sendMail(GlobalContext globalContext, InternetAddress from, InternetAddress to, InternetAddress cc, InternetAddress bcc, String subject, String content) {
		MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
		try {
			mailService.sendMail(null, from, to, cc, bcc, subject, content, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void sendMail(GlobalContext globalContext, InternetAddress from, InternetAddress to, InternetAddress cc, InternetAddress bcc, String subject, String content, String contentTxt, boolean isHTML) {
		MailService mailService = MailService.getInstance(new MailConfig(globalContext, globalContext.getStaticConfig(), null));
		try {
			mailService.sendMail(null, from, to, cc, bcc, subject, content, contentTxt, isHTML, globalContext.getDKIMBean());
		} catch (Exception e) {
			e.printStackTrace();
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
		if (userAgent == null) {
			return false;
		} else {
			userAgent = userAgent.toLowerCase();
			return userAgent.contains("bot") || userAgent.contains("robot");
		}
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
		if (index<0) {
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
	
	
}
