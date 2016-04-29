package org.javlo.navigation;

import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.test.servlet.FakeHttpContext;
import org.javlo.test.servlet.TestRequest;

import junit.framework.TestCase;

public class TestMenuElement extends TestCase {
	
		FakeHttpContext httpContext = FakeHttpContext.getInstance();
		TestRequest request = httpContext.getRequest("http://demo.javlo.org/view/en/media.html?webaction=test");
	
		private ContentContext getContentContext() throws Exception {
			return ContentContext.getContentContext(request, httpContext.getResponse());
		}
	
		private MenuElement getRoot(ContentContext ctx) throws Exception {
			GlobalContext globalContext = GlobalContext.getInstance(request);			
			return globalContext.getPageIfExist(ctx, "/", true);			
		}
		
		public void testEquals() throws Exception {
			ContentContext ctx = getContentContext();
			MenuElement root1 = getRoot(ctx);
			MenuElement root2 = getRoot(ctx);			
			assertTrue("two root as the same reference.", root1 != root2);
			assertTrue("two root as not the same (with children).", root1.equals(ctx, root2, true));
			assertTrue("two root as not the same (without children).", root1.equals(ctx, root2, false));			
			assertFalse("root and first child has the same (with children).", root1.equals(ctx, root2.getFirstChild(), true));
			assertFalse("root and first child has the same (without children).", root1.equals(ctx, root2.getFirstChild(), false));
			
			root2.getFirstChild().getContent(ctx).next(ctx).setValue("change value");
			assertFalse("two root must be different (with children).", root1.equals(ctx, root2, true));
			assertTrue("two root must be the same (without children).", root1.equals(ctx, root2, false));
		}
	
}
