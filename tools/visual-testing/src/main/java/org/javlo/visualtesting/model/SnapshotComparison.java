package org.javlo.visualtesting.model;

import java.nio.file.Path;

public class SnapshotComparison {

	public static final String SEPARATOR = "--vs--";

	private final Site parentSite;
	private final Path comparisonFolder;

	public SnapshotComparison(Site parentSite, Path comparisonFolder) {
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

}
