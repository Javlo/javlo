package org.javlo.component.core;

import org.javlo.context.ContentContext;

public interface IPageRank {
	
	public int getRankValue(ContentContext ctx, String path);
	
	public int getVotes(ContentContext ctx, String path);

}
