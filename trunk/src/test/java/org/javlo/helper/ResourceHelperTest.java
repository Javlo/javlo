package org.javlo.helper;

import junit.framework.TestCase;

import org.javlo.context.ContentContext;
import org.javlo.test.servlet.FakeHttpContext;
import org.javlo.test.servlet.TestRequest;

public class ResourceHelperTest extends TestCase {
	
	public void testIsTranformURL() throws Exception {
		FakeHttpContext httpContext = FakeHttpContext.getInstance();		 
		TestRequest request = httpContext.getRequest("http://demo.javlo.org/view/en/media.html?webaction=test");
		request.setContextPath("context");
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());		
		assertTrue(ResourceHelper.isTransformURL(ctx, "/context/transform/free/template/zonea/static/gallery/image.jpg"));
		assertFalse(ResourceHelper.isTransformURL(ctx, "/context/free/template/zonea/static/gallery/image.jpg"));		
	}
	
	public void testIsResourceURL() throws Exception {
		FakeHttpContext httpContext = FakeHttpContext.getInstance();		 
		TestRequest request = httpContext.getRequest("http://demo.javlo.org/view/en/media.html?webaction=test");
		request.setContextPath("context");
		ContentContext ctx = ContentContext.getContentContext(request, httpContext.getResponse());		
		assertTrue(ResourceHelper.isResourceURL(ctx, "/context/resource/static/gallery/image.jpg"));
		assertFalse(ResourceHelper.isResourceURL(ctx, "/context/static/gallery/image.jpg"));		
	}

}
