
import java.net.URI;
import java.net.URL;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;

public class TestMain {

	private static CloseableHttpClient httpClient = null;

	public static void initHttpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(100);
		HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManager(connectionManager);
		httpClient = clientBuilder.build();
		
		System.out.println("httpClient >> "+httpClient);
		
		((DefaultHttpClient)httpClient).setRedirectStrategy(new DefaultRedirectStrategy() {
		    /** Redirectable methods. */
		    private String[] REDIRECT_METHODS = new String[] { 
		        HttpGet.METHOD_NAME, HttpPost.METHOD_NAME, HttpHead.METHOD_NAME 
		    };

		    @Override
		    protected boolean isRedirectable(String method) {
		        for (String m : REDIRECT_METHODS) {
		            if (m.equalsIgnoreCase(method)) {
		                return true;
		            }
		        }
		        return false;
		    }
		});
	}

	public static boolean doesUrlExist(String urlStr) {

		boolean out = false;

		try {
			URL url = new URL(urlStr);

			// do not use url.toURI() directly, as the path can be malformed (special
			// characters not encoded)
			String uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), null).toString();
			HttpHead httpHead = new HttpHead(uri);
			CloseableHttpResponse response = httpClient.execute(httpHead, new BasicHttpContext());
			try {
				int statusCode = response.getStatusLine().getStatusCode();
				out = (statusCode == HttpStatus.SC_OK);

			} finally {
				response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

		return out;
	}

	public static void main(String[] args) {
		initHttpClient();
		System.out.println("http  : "+doesUrlExist("http://www.europarl.europa.eu/meetdocs/2014_2019/plmrep/COMMITTEES/CJ13/AM/2020/12-14/1218572EN.pdf"));
		System.out.println("https : "+doesUrlExist("https://www.europarl.europa.eu/meetdocs/2014_2019/plmrep/COMMITTEES/CJ13/AM/2020/12-14/1218572EN.pdf"));
	}

}
