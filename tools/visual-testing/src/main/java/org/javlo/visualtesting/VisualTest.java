package org.javlo.visualtesting;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.javlo.visualtesting.model.Site;
import org.javlo.visualtesting.model.Snapshot;

public class VisualTest {

	public static void main(String[] args) throws Exception {
		Path rootOutputFolder = Paths.get(System.getProperty("user.dir"), "output");
		Path tmpFolder = rootOutputFolder.resolve("tmp");

		String siteUrl = "http://www.javlo.org";
		Site site = new Site(rootOutputFolder, siteUrl);
		createSnapshot(site, tmpFolder);
		List<Snapshot> snaps = site.getSnapshots();
		Snapshot oldSnap = snaps.get(snaps.size() - 1);
		Snapshot newSnap = snaps.get(snaps.size());
		compareSnapshots(site, oldSnap, newSnap, tmpFolder);
	}

	private static Snapshot createSnapshot(Site site, Path tmpFolder) throws IOException {
		try (SnapshotMaker t = new SnapshotMaker(site.createNewSnapshot(), tmpFolder)) {
			t.configure();
			t.processSite();
			return t.getSnapshot();
		}
	}

	private static void compareSnapshots(Site site, Snapshot oldSnap, Snapshot newSnap, Path tmpFolder) throws IOException {
		try (SnapshotComparisonMaker m = new SnapshotComparisonMaker(site.createNewComparison(oldSnap, newSnap), tmpFolder)) {
			m.configure();
			m.run();
		}
	}

}
