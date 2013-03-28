package org.javlo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Date;

import org.javlo.helper.NetHelper;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;

public class StressTools {

	private static final String SCENARIO_FILE = "c:/trans/test1.txt";
	private static final String RESULT_PREFIX = "c:/trans/result-";

	private static class StressThread extends Thread {

		public static int threadCount = 0;

		private final long thinkTime;
		private final long occurence;
		private final String scenario;
		private final OutputStream resultStream;

		public StressThread(long thinkTime, long occurence, String scenario, OutputStream resultStream) {
			super();
			threadCount++;
			this.thinkTime = thinkTime;
			this.occurence = occurence;
			this.scenario = scenario;
			this.resultStream = resultStream;
		}

		@Override
		public void run() {
			try {
				System.out.println("Start thread : " + threadCount);
				runTest(scenario, resultStream, true, thinkTime);
				for (int i = 1; i < occurence; i++) {
					runTest(scenario, resultStream, false, thinkTime);
				}
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

		for (int i = 0; i < 100; i++) {
			File resultFile = new File(resultPrefix + StringHelper.createFileName(StringHelper.renderTime(new Date())) + '-' + StressThread.threadCount + ".csv");
			resultFile.createNewFile();
			OutputStream outStream = new FileOutputStream(resultFile);
			StressThread thread = new StressThread(100, 2, scenarioText, outStream);
			thread.start();
		}

	}

	private static void runTest(String scenario, OutputStream resultOut, boolean printHeader, long thinkTime) throws IOException {

		Reader r = new StringReader(scenario);
		BufferedReader br = new BufferedReader(r);

		PrintStream out = new PrintStream(resultOut);

		if (printHeader) {
			out.println("\"time\", \"url\", \"return ok\", \"time\", \"error message\" ");
		}

		String line = br.readLine();
		while (line != null) {
			String[] splitedLine = line.split(">>>");
			if (splitedLine.length == 2) {
				String key = splitedLine[0].trim();
				String result = splitedLine[1].trim();
				long startTime = System.currentTimeMillis();
				String content = null;
				String errorMessage = "";
				try {
					content = NetHelper.readPage(new URL(key));
				} catch (Exception e) {
					errorMessage = e.getMessage();
				}
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
				out.println("\"" + StringHelper.renderTime(new Date()) + "\",\"" + key + "\",\"" + validReturn + "\",\"" + StringHelper.renderTimeInSecond(endTime - startTime) + "\",\"" + errorMessage + "\"");
			} else {
				out.println("\"ERROR BAD LINE : " + line + "\",\"\",\"\",\"\",\"\"");
			}
			line = br.readLine();
			if (thinkTime > 0) {
				try {
					Thread.sleep(thinkTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		r.close();
		br.close();
		out.flush();
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
