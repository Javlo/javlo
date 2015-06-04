package org.javlo.visualtesting.model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

	public Site(Path rootOutputFolder, String siteUrl) {
		this.siteUrl = siteUrl;
		this.outputFolder = rootOutputFolder.resolve(FileHelper.encodeAsFileName(siteUrl));
		this.snapshotsFolder = this.outputFolder.resolve("snapshots");
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
		LocalDateTime snapshotDate = LocalDateTime.now();
		Path snapshotFolder = snapshotsFolder.resolve(formatSnapshotFolderName(snapshotDate));
		Snapshot snapshot = new Snapshot(this, snapshotFolder);
		for (String pageUrl : getUrlToProcess()) {
			snapshot.getPages().add(new SnapshotedPage(snapshot, pageUrl));
		}
		return snapshot;
	}

	private String formatSnapshotFolderName(LocalDateTime now) {
		return FileHelper.createFileName(now);
	}

	public List<Snapshot> getSnapshots() {
		try {
			return Files.list(snapshotsFolder)
					.sorted((p1, p2) -> p1.getFileName().toString().compareTo(p2.getFileName().toString()))
					.map((p) -> new Snapshot(this, p))
					.collect(Collectors.toCollection(LinkedList::new));
		} catch (IOException e) {
			throw new ForwardException(e);
		}
	}

	public SnapshotComparison createNewComparison(Snapshot oldSnap, Snapshot newSnap) {
		// TODO Auto-generated method stub
		return null;
	}

}
