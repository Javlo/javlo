package org.javlo.visualtesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.javlo.visualtesting.helper.ResourceHelper;
import org.javlo.visualtesting.model.PageSet;
import org.javlo.visualtesting.model.Snapshot;
import org.javlo.visualtesting.model.SnapshotedPage;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.internal.Killable;

public class SnapshotMaker implements AutoCloseable {

	private Path tmpFolder;

	private WebDriver drv;
//	private FightingLayoutBugs fightBugs;

	public SnapshotMaker(Path tmpFolder) {
		this.tmpFolder = tmpFolder;
	}

	protected void configure(int pageWidth) throws IOException {
		Files.createDirectories(tmpFolder);
		drv = new FirefoxDriver();
		setViewportWidth(pageWidth);
//		fightBugs = new FightingLayoutBugs();
//		fightBugs.setScreenshotDir(tmpFolder.toFile());
	}

	private void setViewportWidth(int width) {
		Dimension sz = drv.manage().window().getSize();
		int window = sz.getWidth();
		int viewport = Integer.parseInt("" + ((JavascriptExecutor) drv).executeScript("return window.innerWidth"));
		width = width + (window - viewport);
		drv.manage().window().setSize(new Dimension(width, sz.height));
	}

	@Override
	public void close() {
		try {
			if (drv != null) {
				drv.quit();
			}
		} catch (RuntimeException ex) {
			if (drv instanceof Killable) {
				((Killable) drv).kill();
			} else {
				ex.printStackTrace();
			}
		}
		try {
			FileUtils.deleteDirectory(tmpFolder.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Snapshot snapshot(PageSet site, String snapshotName, boolean deleteExisting) throws IOException {
		Snapshot snap = site.createNewSnapshot(snapshotName, deleteExisting);
		Collection<SnapshotedPage> pages = snap.getPages();
		for (SnapshotedPage page : pages) {
			System.out.println("Snapshot: " + page.getUrl());
			processPage(snap, page);
		}
		return snap;
	}

	private void processPage(Snapshot snap, SnapshotedPage page) throws IOException {
		drv.navigate().to(page.getUrl());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Take screen shot
		if(drv instanceof TakesScreenshot) {
			byte[] png = ((TakesScreenshot) drv).getScreenshotAs(OutputType.BYTES);
			Path outpuPath = ResourceHelper.createParentFolder(page.getScreenShotFile());
			Files.write(outpuPath, png, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		}

		//Find layout bugs
//		try {
//			WebPage webPage = new WebPage(drv);
//			Collection<LayoutBug> bugs = fightBugs.findLayoutBugsIn(webPage);
//			if (!bugs.isEmpty()) {
//				for (LayoutBug bug : bugs) {
//					page.addLayoutBugs(bug.getDescription(), bug.getHtml(), bug.getScreenshot().toPath());
//				}
//			}
//		} catch (Exception ex) {
//			//throw new ForwardException(ex);
//		}
		page.save();
	}

}
