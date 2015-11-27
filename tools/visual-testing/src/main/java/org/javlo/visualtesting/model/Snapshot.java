package org.javlo.visualtesting.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

import org.javlo.visualtesting.helper.ForwardException;

public class Snapshot {

	private Site parentSite;
	private Path snapshotFolder;
	private Path layoutBugsFolder;
	private Collection<SnapshotedPage> pages;

	public Snapshot(Site parentSite, Path snapshotFolder) {
		this.parentSite = parentSite;
		this.snapshotFolder = snapshotFolder;
		this.layoutBugsFolder = snapshotFolder.resolve("layout-bugs");
	}

	public String getName() {
		return snapshotFolder.getFileName().toString();
	}

	public Site getParentSite() {
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
