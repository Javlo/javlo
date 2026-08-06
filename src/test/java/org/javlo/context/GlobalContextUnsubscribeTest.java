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

	/**
	 * La valeur doit être persistée sur disque. Sans le save() dans setUnsubscribeLink,
	 * une deuxième instance de GlobalContext (qui recharge depuis le fichier properties)
	 * ne verrait pas la valeur écrite par la première instance.
	 */
	public void testUnsubscribeLinkIsPersisted() throws Exception {
		// Derive unique value from current time to avoid false passes from stale files
		String uniqueValue = "https://site.be/unsub?ts=" + System.currentTimeMillis();

		// First context: write the value
		GlobalContext ctx1 = getContext();
		ctx1.setUnsubscribeLink(uniqueValue);
		assertEquals(uniqueValue, ctx1.getUnsubscribeLink());

		// Second, independent context: should reload from disk and see the value
		GlobalContext ctx2 = getContext();
		assertEquals("Value should be persisted to disk and reloaded in second context",
			uniqueValue, ctx2.getUnsubscribeLink());
	}

	public void testUnsubscribeSecretIsGenerated() throws Exception {
		String secret = getContext().getUnsubscribeSecret();
		assertNotNull(secret);
		assertTrue("secret trop court : " + secret.length(), secret.length() >= 32);
	}

	public void testUnsubscribeSecretIsStable() throws Exception {
		GlobalContext ctx = getContext();
		assertEquals(ctx.getUnsubscribeSecret(), ctx.getUnsubscribeSecret());
	}

	public void testUnsubscribeSecretIsUrlSafe() throws Exception {
		assertTrue(getContext().getUnsubscribeSecret().matches("[A-Za-z0-9_-]+"));
	}

	public void testUnsubscribeSecretIsPersisted() throws Exception {
		String secret = getContext().getUnsubscribeSecret();
		assertEquals(secret, getContext().getUnsubscribeSecret());
	}
}
