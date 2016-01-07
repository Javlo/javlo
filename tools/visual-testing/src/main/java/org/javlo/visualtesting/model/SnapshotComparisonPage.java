package org.javlo.visualtesting.model;

public class SnapshotComparisonPage {

	private SnapshotComparison parent;
	private String url;
	private double matchScore = -1;

	public SnapshotComparisonPage(SnapshotComparison parent, String url) {
		this.parent = parent;
		this.url = url;
	}

	public SnapshotComparison getParent() {
		return parent;
	}

	public String getUrl() {
		return this.url;
	}

	public double getMatchScore() {
		return this.matchScore;
	}

	public void setMatchScore(double matchScore) {
		this.matchScore = matchScore;
	}

}
