package org.javlo.macro;

public class TransfertComponentBadAreaToContentOnePage extends TransfertComponentBadAreaToContent {

	@Override
	public String getName() {
		return "correct-area-page";
	}

	public boolean isOnCurrentPage() {
		return true;
	}

	@Override
	public boolean isPreview() {
		return true;
	}

}
