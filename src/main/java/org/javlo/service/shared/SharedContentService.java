package org.javlo.service.shared;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.javlo.context.ContentContext;
import org.javlo.navigation.MenuElement;
import org.javlo.service.ContentService;

public class SharedContentService {
	
	public static SharedContentService getInstance(ContentContext ctx) {
		return new SharedContentService();
	}
	
	public List<SharedContent> getAllSharedContent(ContentContext ctx) {
		List<SharedContent> outContent = new LinkedList<SharedContent>();
		ContentService content = ContentService.getInstance(ctx.getRequest());
		
		try {
			for (MenuElement page : content.getNavigation(ctx).getAllChildren()) {
				if (page.getSharedName() != null && page.getSharedName().length() > 0) {
					SharedContent sharedContent = new SharedContent(ctx, page.getSharedName(), Arrays.asList(page.getContent()));
					outContent.add(sharedContent);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return outContent;
	}
	
	public SharedContent getSharedContent(ContentContext ctx, String id) {
		for (SharedContent sharedContent : getAllSharedContent(ctx)) {
			if (sharedContent.getId().equals(id)) {
				return sharedContent;
			}
		}
		return null;
	}

}
