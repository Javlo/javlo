package org.javlo.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
	
	public void testCleanFolderList() {
		Collection<String> testFolder = Arrays.asList(new String[] {"/test", "\\test\\dir1", "dir", "", null, "dir1/dir2"});
		List<String> correctedFolders = ResourceHelper.cleanFolderList(testFolder);
		assertEquals(correctedFolders.get(0), "test");
		assertEquals(correctedFolders.get(1), "test/dir1");
		assertEquals(correctedFolders.get(2), "dir");
		assertEquals(correctedFolders.get(3), "");
		assertEquals(correctedFolders.get(4), null);
		assertEquals(correctedFolders.get(5), "dir1/dir2");
	}

}


