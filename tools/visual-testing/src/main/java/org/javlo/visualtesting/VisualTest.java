package org.javlo.visualtesting;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.internal.Killable;

import com.googlecode.fightinglayoutbugs.FightingLayoutBugs;
import com.googlecode.fightinglayoutbugs.LayoutBug;
import com.googlecode.fightinglayoutbugs.WebPage;

public class VisualTest implements AutoCloseable {

	public static void main(String[] args) throws Exception {
		Path rootOutputFolder = Paths.get(System.getProperty("user.dir"), "output");
		String siteUrl = "http://www.javlo.org";
		try (VisualTest t = new VisualTest(rootOutputFolder, siteUrl)) {
			t.configure();
			t.processSite();
		}
	}

	private String siteUrl;
	private Path outputFolder;

	private Path layoutBugsFolder;

	private WebDriver drv;
	private FightingLayoutBugs fightBugs;

	public VisualTest(Path rootOutputFolder, String siteUrl) {
		this.siteUrl = siteUrl;
		this.outputFolder = rootOutputFolder.resolve(FileUtils.encodeAsFileName(siteUrl));
		this.layoutBugsFolder = outputFolder.resolve("layout-bugs");
	}

	protected void configure() throws IOException {
		Files.createDirectories(layoutBugsFolder);
		drv = new FirefoxDriver();
		fightBugs = new FightingLayoutBugs();
		fightBugs.setScreenshotDir(layoutBugsFolder.toFile());
	}

	@Override
	public void close() {
		try {
			drv.quit();
		} catch (RuntimeException ex) {
			if (drv instanceof Killable) {
				((Killable) drv).kill();
			} else {
				throw ex;
			}
		}
	}

	private void processSite() throws IOException {
		URL siteMapUrl = new URL(siteUrl + "/sitemap.xml");
		Document doc = Jsoup.parse(siteMapUrl, 5000);
		Elements urls = doc.select("urlset url");
		for (Element urlNode : urls) {
			String pageUrl = urlNode.select("loc").text();
			processPage(pageUrl);
		}
	}

	private void processPage(String pageUrl) throws IOException {
		drv.navigate().to(pageUrl);
		String pageFileName = FileUtils.encodeAsFileName(pageUrl);
		if(drv instanceof TakesScreenshot) {
			byte[] png = ((TakesScreenshot) drv).getScreenshotAs(OutputType.BYTES);
			Path outpuPath = outputFolder.resolve(pageFileName + ".png");
			Files.write(outpuPath, png, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		}

		WebPage webPage = new WebPage(drv);
		try {
			Collection<LayoutBug> bugs = fightBugs.findLayoutBugsIn(webPage);
			if (!bugs.isEmpty()) {
				System.out.println(pageUrl);
				for (LayoutBug bug : bugs) {
					System.out.println("\t" + bug.getDescription());
				}
			}
		} catch (Exception ex) {
			System.out.println(pageUrl);
			System.out.println(ex.getMessage());
		}
	}
}
