package org.javlo.visualtesting.model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.javlo.visualtesting.helper.FileHelper;
import org.javlo.visualtesting.helper.ForwardException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Site {

	private String siteUrl;
	private Path outputFolder;
	private Path snapshotsFolder;
	private Path comparisonsFolder;

	public Site(Path rootOutputFolder, String siteUrl) {
		this.siteUrl = siteUrl;
		this.outputFolder = rootOutputFolder.resolve(FileHelper.encodeAsFileName(siteUrl));
		this.snapshotsFolder = this.outputFolder.resolve("snapshots");
		this.comparisonsFolder = this.outputFolder.resolve("comparisons");
	}

	public String getUrl() {
		return siteUrl;
	}

	public Collection<String> getUrlToProcess() {
		try {
			Collection<String> out = new LinkedList<>();
			URL siteMapUrl = new URL(siteUrl + "/sitemap.xml");
			Document doc = Jsoup.parse(siteMapUrl, 5000);
			Elements urls = doc.select("urlset url");
			for (Element urlNode : urls) {
				String pageUrl = urlNode.select("loc").text();
				out.add(pageUrl);
			}
			return out;
		} catch (IOException ex) {
			throw new ForwardException(ex);
		}
	}

	public Snapshot createNewSnapshot() {
		Date snapshotDate = new Date();
		Path snapshotFolder = snapshotsFolder.resolve(formatSnapshotFolderName(snapshotDate));
		Snapshot snapshot = new Snapshot(this, snapshotFolder);
		for (String pageUrl : getUrlToProcess()) {
			snapshot.getPages().add(new SnapshotedPage(snapshot, pageUrl));
		}
		return snapshot;
	}

	private String formatSnapshotFolderName(Date now) {
		return FileHelper.createFileName(now);
	}

	public List<Snapshot> getSnapshots() {
		try (DirectoryStream<Path> s = Files.newDirectoryStream(snapshotsFolder)) {
			List<Snapshot> out = new LinkedList<>();
			for (Path p : s) {
				out.add(buildSnapshot(p));
			}
			Collections.sort(out, new Comparator<Snapshot>() {
				@Override
				public int compare(Snapshot s1, Snapshot s2) {
					return s1.getName().compareTo(s2.getName());
				}
			});
			return out;
		} catch (IOException e) {
			throw new ForwardException(e);
		}
	}

	public Snapshot getSnapshotByName(String name) {
		try (DirectoryStream<Path> s = Files.newDirectoryStream(snapshotsFolder)) {
			List<Snapshot> out = new LinkedList<>();
			for (Path p : s) {
				if (p.getFileName().toString().equals(name)) {
					return buildSnapshot(p);
				}
			}
			return null;
		} catch (IOException e) {
			throw new ForwardException(e);
		}
	}

	protected Snapshot buildSnapshot(Path snapshotFolder) {
		return new Snapshot(this, snapshotFolder);
	}

	public SnapshotComparison createNewComparison(Snapshot oldSnap, Snapshot newSnap) {
		Path comparisonFolder = comparisonsFolder.resolve(formatComparisonFolderName(oldSnap, newSnap));
		SnapshotComparison comparison = new SnapshotComparison(this, comparisonFolder);
//		for (String pageUrl : getUrlToProcess()) {
//			comparison.getPages().add(new SnapshotedPage(snapshot, pageUrl));
//		}
		return comparison;
	}

	private String formatComparisonFolderName(Snapshot oldSnap, Snapshot newSnap) {
		return oldSnap.getName() + SnapshotComparison.SEPARATOR + newSnap.getName();
	}

}
