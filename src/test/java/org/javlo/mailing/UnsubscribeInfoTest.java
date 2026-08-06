package org.javlo.mailing;

import junit.framework.TestCase;

public class UnsubscribeInfoTest extends TestCase {

	public void testManualKeepsRawValue() throws Exception {
		UnsubscribeInfo info = UnsubscribeInfo.manual("<https://site.be/unsub>, <mailto:unsub@site.be>");
		assertEquals("<https://site.be/unsub>, <mailto:unsub@site.be>", info.getHeaderValue());
		assertFalse(info.isOneClick());
		assertFalse(info.isEmpty());
	}

	public void testManualNeverOneClickEvenOnHttps() throws Exception {
		assertFalse(UnsubscribeInfo.manual("https://site.be/unsub").isOneClick());
	}

	/** M3 : le RFC 2369 impose les chevrons, même sur un lien saisi à la main. */
	public void testManualBareUrlIsWrapped() throws Exception {
		UnsubscribeInfo info = UnsubscribeInfo.manual("https://site.be/unsub?email=x@y.be");
		assertEquals("<https://site.be/unsub?email=x@y.be>", info.getHeaderValue());
		assertFalse(info.isOneClick());
	}

	/** M3 : un lien manuel déjà encadré n'est pas encadré deux fois. */
	public void testManualAlreadyBracketedIsNotWrappedTwice() throws Exception {
		assertEquals("<https://site.be/unsub>", UnsubscribeInfo.manual("<https://site.be/unsub>").getHeaderValue());
		assertEquals("<mailto:unsub@site.be>", UnsubscribeInfo.manual("  <mailto:unsub@site.be>  ").getHeaderValue());
	}

	public void testOneClickWrapsUrlInBrackets() throws Exception {
		UnsubscribeInfo info = UnsubscribeInfo.oneClick("https://site.be/fr/page?webaction=unsecure.unsubscribe&lut=ABC");
		assertEquals("<https://site.be/fr/page?webaction=unsecure.unsubscribe&lut=ABC>", info.getHeaderValue());
		assertTrue(info.isOneClick());
	}

	public void testGeneratedHttpUrlIsWrappedButNotOneClick() throws Exception {
		UnsubscribeInfo info = UnsubscribeInfo.oneClick("http://site.be/fr/page?lut=ABC");
		assertFalse(info.isOneClick());
		assertEquals("<http://site.be/fr/page?lut=ABC>", info.getHeaderValue());
		assertFalse(info.isEmpty());
	}

	public void testHttpsCheckIsCaseInsensitive() throws Exception {
		assertTrue(UnsubscribeInfo.oneClick("HTTPS://site.be/x").isOneClick());
	}

	public void testEmptyValues() throws Exception {
		assertTrue(UnsubscribeInfo.manual(null).isEmpty());
		assertTrue(UnsubscribeInfo.manual("").isEmpty());
		assertTrue(UnsubscribeInfo.manual("   ").isEmpty());
		assertTrue(UnsubscribeInfo.oneClick(null).isEmpty());
		assertFalse(UnsubscribeInfo.oneClick(null).isOneClick());
		assertNull(UnsubscribeInfo.manual(null).getHeaderValue());
	}
}
