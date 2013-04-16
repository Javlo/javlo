package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

public class StressTools {

	private static final String SCENARIO_FILE = "C:/work/stress_test/test_pres_local.txt";
	private static final String RESULT_PREFIX = "C:/work/stress_test/result-pres_local.";

	private static class StressThread extends Thread {

		public static Integer threadCount = 0;
		public int threadNumber = 0;

		private final long thinkTime;
		private final long occurence;
		private final String scenario;
		private final PrintStream resultStream;

		private final HttpClient httpclient;
		private final HttpContext localContext = new BasicHttpContext();

		public StressThread(long thinkTime, long occurence, String scenario, PrintStream resultStream) {
			super();
			synchronized (threadCount) {
				threadCount++;
				threadNumber = threadCount;
			}
			this.thinkTime = thinkTime;
			this.occurence = occurence;
			this.scenario = scenario;
			this.resultStream = resultStream;

			HttpParams params = new BasicHttpParams();
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			ClientConnectionManager cm = new ThreadSafeClientConnManager(params, registry);
			httpclient = new DefaultHttpClient(cm, params);

			localContext.setAttribute(ClientContext.COOKIE_STORE, new BasicCookieStore());
		}

		@Override
		public void run() {
			try {
				System.out.println("Start thread : " + threadCount);

				httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60 * 1000);

				runTest(httpclient, localContext, threadNumber, scenario, 2, resultStream, thinkTime);
				for (int i = 1; i < occurence; i++) {
					runTest(httpclient, localContext, threadNumber, scenario, 2, resultStream, thinkTime);
				}
				httpclient.getConnectionManager().shutdown();
				resultStream.close();
				System.out.println("Stop thread : " + threadCount);
				threadCount--;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void runTest(File scenario, String resultPrefix) throws IOException {
		if (scenario == null) {
			scenario = new File(SCENARIO_FILE);
		}
		if (resultPrefix == null) {
			resultPrefix = RESULT_PREFIX;
		}

		String scenarioText = ResourceHelper.loadStringFromFile(scenario);

		File resultFile = new File(resultPrefix + StringHelper.createFileName(StringHelper.renderTime(new Date())) + ".csv");
		resultFile.createNewFile();
		OutputStream outStream = new FileOutputStream(resultFile);
		PrintStream out = new PrintStream(outStream);
		out.println("\"time\", \"url\", \"return ok\", \"time\", \"error message\" ");

		for (int i = 0; i < 100; i++) {
			resultFile.createNewFile();
			StressThread thread = new StressThread(10, 20, scenarioText, out);
			thread.start();
		}

	}

	private static void runTest(HttpClient httpClient, HttpContext httpContext, int threadNumber, String scenario, int repeat, PrintStream out, long thinkTime) throws IOException {

		for (int i = 0; i < repeat; i++) {

			Reader r = new StringReader(scenario);
			BufferedReader br = new BufferedReader(r);

			String line = br.readLine();
			int lineNumber = 1;
			while (line != null) {
				String[] splitedLine = line.split(">>>");
				if (splitedLine.length == 2) {
					String key = splitedLine[0].trim();
					String result = splitedLine[1].trim();
					long startTime = System.currentTimeMillis();
					String content = null;
					String errorMessage = "";

					try {
						HttpGet httpget = new HttpGet(key);
						HttpResponse response = httpClient.execute(httpget, httpContext);
						HttpEntity entity = response.getEntity();
						InputStream in = null;
						if (entity != null) {
							try {
								in = entity.getContent();
								content = ResourceHelper.loadStringFromStream(in, Charset.defaultCharset());
							} finally {
								ResourceHelper.closeResource(in);
							}
						}
					} catch (Throwable t) {
						t.printStackTrace();
						errorMessage = t.getMessage();
					}

					/*
					 * try { content = NetHelper.readPage(new URL(key)); } catch (Exception e) { errorMessage = e.getMessage(); }
					 */
					long endTime = System.currentTimeMillis();
					boolean validReturn = content != null && result != null;
					if (content != null && result != null) {
						if (result.startsWith("-")) {
							result = result.substring(1);
							validReturn = !content.contains(result);
						} else {
							validReturn = content.contains(result);
						}
					}
					synchronized (out) {
						out.println("\"" + StringHelper.renderTime(new Date()) + "\",\"" + key + "\",\"" + validReturn + "\",\"" + StringHelper.renderTimeInSecond(endTime - startTime) + "\",\"" + errorMessage + "\"");
					}

					if (!validReturn) {
						System.out.println("");
						System.out.println(validReturn + " - " + threadNumber + " : " + key + " - " + errorMessage);
						System.out.println("");
					} else {
						if (lineNumber % 10 == 0) {
							System.out.println("");
						}
						System.out.print(" " + lineNumber + " ");
					}
				} else {
					synchronized (out) {
						out.println("\"ERROR BAD LINE : " + line + "\",\"\",\"\",\"\",\"\"");
					}
				}
				line = br.readLine();
				if (thinkTime > 0) {
					try {
						Thread.sleep(thinkTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				lineNumber++;
			}

			r.close();
			br.close();
		}
		synchronized (out) {
			out.flush();
		}
	}

	public static void main(String[] args) {
		try {
			runTest((File) null, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
