package org.javlo.navigation;

public class LabelAndSectionURLCreatorNoExtWidthId extends LabelAndSectionURLCreatorNoExt {

	@Override
	protected boolean isWithId() {	
		return true;
	}
	
	@Override
	public String createURLKey(String url) {
		int slashIndex = url.lastIndexOf('/');
		if (slashIndex >= 0) {
			return url.substring(slashIndex);
		} else {
			return url;
		}
	}
	
}
