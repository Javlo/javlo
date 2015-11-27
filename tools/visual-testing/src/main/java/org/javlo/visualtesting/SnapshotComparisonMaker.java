package org.javlo.visualtesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.javlo.visualtesting.model.Snapshot;
import org.javlo.visualtesting.model.SnapshotComparison;
import org.javlo.visualtesting.model.SnapshotedPage;

public class SnapshotComparisonMaker implements AutoCloseable {

	private SnapshotComparison comparison;
	private Path tmpFolder;

	public SnapshotComparisonMaker(SnapshotComparison comparison, Path tmpFolder) {
		this.comparison = comparison;
		this.tmpFolder = tmpFolder;
	}

	public SnapshotComparison getComparison() {
		return comparison;
	}

	public void configure() {
		// TODO Auto-generated method stub

	}

	public void run() {
		Snapshot oldSnap = comparison.getOldSnapshot();
		Snapshot newSnap = comparison.getNewSnapshot();
		Map<String, SnapshotedPage> newPages = new HashMap<>();
		for (SnapshotedPage newPage : newSnap.getPages()) {
			newPages.put(newPage.getUrl(), newPage);
		}

		// Compare existing or deleted pages
		for (SnapshotedPage oldPage : oldSnap.getPages()) {
			SnapshotedPage newPage = newPages.remove(oldPage.getUrl());
			comparePages(oldPage, newPage);
		}
		// Compare new pages
		for (SnapshotedPage newPage : newPages.values()) {
			comparePages(null, newPage);
		}
	}

	private void comparePages(SnapshotedPage oldPage, SnapshotedPage newPage) {

	}

	@Override
	public void close() throws IOException {
		try {
			FileUtils.deleteDirectory(tmpFolder.toFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
