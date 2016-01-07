package org.javlo.visualtesting;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.javlo.visualtesting.helper.ImageHelper;
import org.javlo.visualtesting.model.PageSet;
import org.javlo.visualtesting.model.Snapshot;
import org.javlo.visualtesting.model.SnapshotComparison;
import org.javlo.visualtesting.model.SnapshotComparisonPage;
import org.javlo.visualtesting.model.SnapshotedPage;


public class SnapshotComparisonMaker implements AutoCloseable {

	private Path tmpFolder;

	public SnapshotComparisonMaker(Path tmpFolder) {
		this.tmpFolder = tmpFolder;
	}

	public void configure() {
		// TODO Auto-generated method stub

	}

	public SnapshotComparison compare(PageSet site, Snapshot oldSnap, Snapshot newSnap, boolean deleteExisting) throws IOException {
		SnapshotComparison comparison = site.createNewComparison(oldSnap, newSnap, deleteExisting);
		Map<String, SnapshotedPage> newPages = new HashMap<>();
		for (SnapshotedPage newPage : newSnap.getPages()) {
			newPages.put(newPage.getUrl(), newPage);
		}

		// Compare existing or deleted pages
		for (SnapshotedPage oldPage : oldSnap.getPages()) {
			SnapshotedPage newPage = newPages.remove(oldPage.getUrl());
			SnapshotComparisonPage p = comparePage(comparison, oldPage.getUrl(), oldPage, newPage);
			comparison.getResults().add(p);
		}
		// Compare new pages
		for (SnapshotedPage newPage : newPages.values()) {
			SnapshotComparisonPage p = comparePage(comparison, newPage.getUrl(), null, newPage);
			comparison.getResults().add(p);
		}
		return comparison;
	}

	private SnapshotComparisonPage comparePage(SnapshotComparison comparison, String pageUrl, SnapshotedPage oldPage, SnapshotedPage newPage) throws IOException {
		System.out.println("Compare: " + pageUrl);
		SnapshotComparisonPage out = new SnapshotComparisonPage(comparison, pageUrl);
		if (oldPage == null || newPage == null) {
			out.setMatchScore(0);
		} else {
			out.setMatchScore(compareImage(oldPage.getScreenShotFile(), newPage.getScreenShotFile()));
		}
		return out;
	}

	private double compareImage(Path img1, Path img2) throws IOException {
		return ImageHelper.compareImage(img1, img2);
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
