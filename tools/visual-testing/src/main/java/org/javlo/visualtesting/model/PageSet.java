package org.javlo.visualtesting.model;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.javlo.visualtesting.helper.FileHelper;
import org.javlo.visualtesting.helper.ForwardException;

public class PageSet {

	private Path outputFolder;
	private Path snapshotsFolder;
	private Path comparisonsFolder;
	private Collection<String> pageUrls;

	public PageSet(Path outputFolder) {
		this.outputFolder = outputFolder;
		this.snapshotsFolder = this.outputFolder.resolve("snapshots");
		this.comparisonsFolder = this.outputFolder.resolve("comparisons");
	}

	public Collection<String> getUrlToProcess() {
		return pageUrls;
	}

	public void addUrlToProcess(String url) {
		if (pageUrls == null) {
			pageUrls = new LinkedList<>();
		}
		pageUrls.add(url);
	}

	public Snapshot createNewSnapshot(String snapshotName, boolean deleteExisting) throws IOException {
		Date snapshotDate = new Date();
		if (snapshotName == null) {
			snapshotName = formatSnapshotFolderName(snapshotDate);
		}
		Path snapshotFolder = snapshotsFolder.resolve(snapshotName);
		if (deleteExisting) {
			FileUtils.deleteDirectory(snapshotFolder.toFile());
		} else {
			throw new FileAlreadyExistsException(snapshotFolder.toString());
		}
		Snapshot snapshot = new Snapshot(this, snapshotName, snapshotFolder);
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
		return new Snapshot(this, snapshotFolder.getFileName().toString(), snapshotFolder);
	}

	public SnapshotComparison createNewComparison(Snapshot oldSnap, Snapshot newSnap, boolean deleteExisting) throws IOException {
		Path comparisonFolder = comparisonsFolder.resolve(formatComparisonFolderName(oldSnap, newSnap));
		if (deleteExisting) {
			FileUtils.deleteDirectory(comparisonFolder.toFile());
		} else {
			throw new FileAlreadyExistsException(comparisonFolder.toString());
		}
		return new SnapshotComparison(this, comparisonFolder);
	}

	private String formatComparisonFolderName(Snapshot oldSnap, Snapshot newSnap) {
		return oldSnap.getName() + SnapshotComparison.SEPARATOR + newSnap.getName();
	}

}
