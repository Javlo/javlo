package org.javlo.navigation;

public class TitleURLCreatorNoAccent extends TitleURLCreator {
	
	@Override
	protected boolean isRemoveAccent() {
		return true;
	}

}
