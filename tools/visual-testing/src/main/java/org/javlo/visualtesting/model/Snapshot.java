package org.javlo.visualtesting.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;

import org.javlo.visualtesting.helper.ForwardException;

public class Snapshot {

	private PageSet parentSite;
	private String name;
	private Date time;
	private Path snapshotFolder;
	private Path layoutBugsFolder;
	private Collection<SnapshotedPage> pages;

	public Snapshot(PageSet parentSite, String snapshotName, Path snapshotFolder) {
		this.parentSite = parentSite;
		this.name = snapshotName;
		this.snapshotFolder = snapshotFolder;
		this.layoutBugsFolder = snapshotFolder.resolve("layout-bugs");
		this.time = new Date();
	}

	public String getName() {
		return this.name;
	}

	public Date getTime() {
		return this.time;
	}

	public PageSet getParentSite() {
		return parentSite;
	}

	public Collection<SnapshotedPage> getPages() {
		if (pages == null) {
			if (Files.exists(snapshotFolder)) {
				try (DirectoryStream<Path> s = Files.newDirectoryStream(snapshotFolder)) {
					Collection<SnapshotedPage> out = new LinkedList<>();
					for (Path p : s) {
						if (SnapshotedPage.isPageFolder(p)) {
							out.add(new SnapshotedPage(this, p));
						}
					}
					return out;
				} catch (IOException e) {
					throw new ForwardException(e);
				}
			} else {
				pages = new LinkedList<SnapshotedPage>();
			}
		}
		return pages;
	}

	public Path getFolder() {
		return snapshotFolder;
	}

	public Path getLayoutBugsFolder() {
		return layoutBugsFolder;
	}

}
