package org.javlo.visualtesting.model;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;

public class SnapshotComparison {

	public static final String SEPARATOR = "--vs--";

	private final PageSet parentSite;
	private final Path comparisonFolder;

	private final Collection<SnapshotComparisonPage> results = new LinkedList<>();

	public SnapshotComparison(PageSet parentSite, Path comparisonFolder) {
		this.parentSite = parentSite;
		this.comparisonFolder = comparisonFolder;
	}

	public String getName() {
		return comparisonFolder.getFileName().toString();
	}

	public Snapshot getOldSnapshot() {
		String name = getName().split(SEPARATOR)[0];
		return parentSite.getSnapshotByName(name);
	}

	public Snapshot getNewSnapshot() {
		String name = getName().split(SEPARATOR)[1];
		return parentSite.getSnapshotByName(name);
	}

	public Collection<SnapshotComparisonPage> getResults() {
		return results;
	}

}
