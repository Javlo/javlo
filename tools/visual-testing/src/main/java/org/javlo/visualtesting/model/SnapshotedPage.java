package org.javlo.visualtesting.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import org.javlo.visualtesting.helper.FileHelper;
import org.javlo.visualtesting.helper.ForwardException;
import org.javlo.visualtesting.helper.JsonHelper;
import org.javlo.visualtesting.helper.ResourceHelper;

public class SnapshotedPage {

	private static final String PAGE_PREFIX = "page-";
	public static final Predicate<? super Path> FILTER = (p) -> {
		return p.getFileName().toString().startsWith(PAGE_PREFIX) && Files.isDirectory(p);
	};

	private transient Snapshot parentSnapshot;
	private String url;
	private List<PageBug> layoutBugs;
	private transient Path pageFolder;
	private transient Path screenshotFile;
	private transient Path dataFile;

	public SnapshotedPage(Snapshot snapshot, String pageUrl) {
		this.parentSnapshot = snapshot;
		this.url = pageUrl;
		String siteurl = parentSnapshot.getParentSite().getUrl();
		String shortPageUrl;
		if (pageUrl.startsWith(siteurl)) {
			shortPageUrl = pageUrl.substring(siteurl.length());
		} else {
			shortPageUrl = pageUrl;
		}
		String pageFolderName = PAGE_PREFIX + FileHelper.encodeAsFileName(shortPageUrl);
		this.pageFolder = parentSnapshot.getFolder().resolve(pageFolderName);
		this.screenshotFile = this.pageFolder.resolve("screenshot.png");
		this.dataFile = this.pageFolder.resolve("data.json");
		this.layoutBugs = new LinkedList<>();
		commonInit();
	}

	public SnapshotedPage(Snapshot snapshot, Path pageFolder) {
		this.parentSnapshot = snapshot;
		this.pageFolder = pageFolder;
		this.dataFile = this.pageFolder.resolve("data.json");
		SnapshotedPage loaded = JsonHelper.load(this.dataFile, SnapshotedPage.class);
		this.url = loaded.url;
		this.layoutBugs = loaded.layoutBugs;
		if (this.layoutBugs == null) {
			this.layoutBugs = new LinkedList<PageBug>();
		}
		commonInit();
	}

	private void commonInit() {
	}

	public List<PageBug> getLayoutBugs() {
		return layoutBugs;
	}

	public String getUrl() {
		return url;
	}

	public Path getScreenShotFile() {
		return screenshotFile;
	}

	public void save() {
		JsonHelper.save(dataFile, this);
	}

	public void addLayoutBugs(String description, String html, Path screenshotFile) {
		PageBug bug = new PageBug();
		bug.setDescription(description);
		bug.setHtml(html);
		bug.setFileName(screenshotFile.getFileName().toString());
		try {
			Files.move(screenshotFile, ResourceHelper.createParentFolder(resolveLayoutBugScreenShotFile(bug)));
		} catch (IOException e) {
			throw new ForwardException(e);
		}
		layoutBugs.add(bug);
	}

	private Path resolveLayoutBugScreenShotFile(PageBug bug) {
		return pageFolder.resolve(bug.getFileName());
	}

}
