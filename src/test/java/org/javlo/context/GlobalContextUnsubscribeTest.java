package org.javlo.context;

import org.javlo.helper.StringHelper;
import org.javlo.test.servlet.FakeHttpContext;

import junit.framework.TestCase;

public class GlobalContextUnsubscribeTest extends TestCase {

	static GlobalContext getContext() throws Exception {
		FakeHttpContext httpContext = new FakeHttpContext("http://demo.javlo.org/view/en/index.html");
		return GlobalContext.getInstance(httpContext.getRequest());
	}

	public void testUnsubscribeLinkRoundTrip() throws Exception {
		GlobalContext ctx = getContext();
		ctx.setUnsubscribeLink("https://site.be/unsub?email=${email}");
		assertEquals("https://site.be/unsub?email=${email}", ctx.getUnsubscribeLink());
	}

	/**
	 * Vider le champ doit réellement le vider : c'est le comportement dont
	 * dépend l'override manuel de l'en-tête List-Unsubscribe.
	 */
	public void testUnsubscribeLinkCanBeCleared() throws Exception {
		GlobalContext ctx = getContext();
		ctx.setUnsubscribeLink("https://site.be/unsub");
		assertFalse(StringHelper.isEmpty(ctx.getUnsubscribeLink()));
		ctx.setUnsubscribeLink("");
		assertTrue(StringHelper.isEmpty(ctx.getUnsubscribeLink()));
	}
}
