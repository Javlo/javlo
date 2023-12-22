package org.javlo.helper;

import jakarta.servlet.http.HttpServletRequest;

import org.javlo.component.core.ContentElementList;
import org.javlo.component.core.IContentVisualComponent;
import org.javlo.context.ContentContext;
import org.javlo.test.servlet.FakeHttpContext;

import junit.framework.TestCase;

public class ComponentHelperTest extends TestCase {
	
	public void testGetComponentPosition() throws Exception {
		FakeHttpContext httpContext = new FakeHttpContext("http://demo.javlo.org/view/en/media.html?webaction=test");
		HttpServletRequest request = httpContext.getRequest();		
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());		
		
		IContentVisualComponent p = null;
		int pos = 0;
		ContentElementList content = ctx.getCurrentPage().getContent(ctx);
		while (content.hasNext(ctx)) {
			IContentVisualComponent comp = content.next(ctx);			
			if (comp.getType().equals("paragraph")) {
				p = comp;
				pos++;
			}
		}
		
		
	}

}
