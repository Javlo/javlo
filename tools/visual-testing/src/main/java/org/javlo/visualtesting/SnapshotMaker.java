package org.javlo.visualtesting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.javlo.visualtesting.helper.ResourceHelper;
import org.javlo.visualtesting.model.Snapshot;
import org.javlo.visualtesting.model.SnapshotedPage;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.internal.Killable;

import com.googlecode.fightinglayoutbugs.FightingLayoutBugs;
import com.googlecode.fightinglayoutbugs.LayoutBug;
import com.googlecode.fightinglayoutbugs.WebPage;

public class SnapshotMaker implements AutoCloseable {

	private Snapshot snap;
	private Path tmpFolder;

	private WebDriver drv;
	private FightingLayoutBugs fightBugs;

	public SnapshotMaker(Snapshot snap, Path tmpFolder) {
		this.snap = snap;
		this.tmpFolder = tmpFolder;
	}

	public Snapshot getSnapshot() {
		return snap;
	}

	protected void configure() throws IOException {
		Files.createDirectories(tmpFolder);
		drv = new FirefoxDriver();
		fightBugs = new FightingLayoutBugs();
		fightBugs.setScreenshotDir(tmpFolder.toFile());
	}

	@Override
	public void close() {
		try {
			drv.quit();
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

	public void processSite() throws IOException {
		Collection<SnapshotedPage> pages = snap.getPages();
		for (SnapshotedPage page : pages) {
			processPage(snap, page);
		}
	}

	private void processPage(Snapshot snap, SnapshotedPage page) throws IOException {
		drv.navigate().to(page.getUrl());

		//Take screen shot
		if(drv instanceof TakesScreenshot) {
			byte[] png = ((TakesScreenshot) drv).getScreenshotAs(OutputType.BYTES);
			Path outpuPath = ResourceHelper.createParentFolder(page.getScreenShotFile());
			Files.write(outpuPath, png, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		}

		//Find layout bugs
		try {
			WebPage webPage = new WebPage(drv);
			Collection<LayoutBug> bugs = fightBugs.findLayoutBugsIn(webPage);
			if (!bugs.isEmpty()) {
				for (LayoutBug bug : bugs) {
					page.addLayoutBugs(bug.getDescription(), bug.getHtml(), bug.getScreenshot().toPath());
				}
			}
		} catch (Exception ex) {
			//throw new ForwardException(ex);
		}
		page.save();
	}

}
