package org.javlo.visualtesting.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

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

	public Site getParentSite() {
		return parentSite;
	}

	public Collection<SnapshotedPage> getPages() {
		if (pages == null) {
			if (Files.exists(snapshotFolder)) {
				try {
					pages = Files.list(snapshotFolder)
							.filter(SnapshotedPage.FILTER)
							.map((p) -> new SnapshotedPage(this, p))
							.collect(Collectors.toCollection(LinkedList::new));
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
