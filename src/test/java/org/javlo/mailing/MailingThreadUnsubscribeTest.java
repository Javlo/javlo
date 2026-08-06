package org.javlo.mailing;

import java.util.Arrays;

import org.javlo.config.StaticConfig;
import org.javlo.context.ContentContext;
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.UnsubscribeTokenService;
import org.javlo.test.servlet.FakeHttpContext;

import jakarta.mail.internet.InternetAddress;
import junit.framework.TestCase;

/**
 * Vue d'ensemble du chemin assemblé : ce que MailingThread pose réellement
 * dans l'en-tête List-Unsubscribe.
 *
 * Ces assertions couvrent les deux défauts qui n'apparaissaient qu'une fois
 * les pièces réunies : une URL pointant vers un servlet incapable d'exécuter
 * les actions, et un jeton qui ne se relit pas.
 */
public class MailingThreadUnsubscribeTest extends TestCase {

	private static final String RECIPIENT = "Jean@Exemple.be";

	private static GlobalContext getContext() throws Exception {
		FakeHttpContext httpContext = new FakeHttpContext("http://demo.javlo.org/view/en/index.html");
		return GlobalContext.getInstance(httpContext.getRequest());
	}

	/**
	 * Mailing minimal, tel que MailingModuleContext.sendMailing le construit :
	 * clé de contexte, identifiant, rôles, et l'URL absolue de la page du
	 * mailing en VIEW_MODE.
	 */
	private static Mailing buildMailing(GlobalContext globalContext, String baseURL) throws Exception {
		Mailing mailing = new Mailing();
		mailing.setContextKey(globalContext.getContextKey());
		mailing.setId(StaticConfig.getInstance(globalContext.getServletContext()).getMailingStaticConfig(), "test-unsubscribe-mailing");
		mailing.setRoles(Arrays.asList("newsletter", "client"));
		mailing.setUnsubscribeURL(baseURL);
		return mailing;
	}

	private static String extractParam(String url, String name) {
		int start = url.indexOf(name + "=");
		assertTrue("param '" + name + "' absent de " + url, start >= 0);
		start = start + name.length() + 1;
		int end = url.indexOf('&', start);
		return end < 0 ? url.substring(start) : url.substring(start, end);
	}

	/**
	 * C1 : l'en-tête doit viser /view/, seul chemin routé vers AccessServlet,
	 * qui appelle ServletHelper.execAction. Une URL en /page/ tombe sur
	 * ContentOnlyServlet et le paramètre webaction y est ignoré.
	 */
	public void testGeneratedHeaderTargetsAnActionCapableURL() throws Exception {
		GlobalContext globalContext = getContext();
		Mailing mailing = buildMailing(globalContext, "https://demo.javlo.org/javlo/view/fr/p.html");

		UnsubscribeInfo info = MailingThread.buildUnsubscribeInfo(mailing, new InternetAddress(RECIPIENT), globalContext);

		String url = info.getUrl();
		assertNotNull(url);
		assertTrue("l'URL doit passer par /view/ : " + url, url.contains("/view/"));
		assertFalse("l'URL ne doit pas passer par /page/ : " + url, url.contains("/page/"));
		assertTrue("webaction manquant : " + url, url.contains("webaction=unsecure.unsubscribe"));
		assertTrue("jeton manquant : " + url, url.contains("lut="));
		assertTrue("l'en-tête doit être absolu : " + url, url.startsWith("https://"));
		assertEquals('<' + url + '>', info.getHeaderValue());
	}

	/** RFC 8058 : le one-click n'est annoncé que sur HTTPS. */
	public void testOneClickOnlyOnHttps() throws Exception {
		GlobalContext globalContext = getContext();

		UnsubscribeInfo https = MailingThread.buildUnsubscribeInfo(buildMailing(globalContext, "https://demo.javlo.org/javlo/view/fr/p.html"), new InternetAddress(RECIPIENT), globalContext);
		assertTrue(https.isOneClick());

		UnsubscribeInfo http = MailingThread.buildUnsubscribeInfo(buildMailing(globalContext, "http://demo.javlo.org/javlo/view/fr/p.html"), new InternetAddress(RECIPIENT), globalContext);
		assertFalse(http.isOneClick());
		assertTrue(http.getUrl().contains("lut="));
	}

	/**
	 * Le jeton posé dans l'URL doit se relire avec le secret du site : sinon
	 * performUnsubscribe rejetterait tous les désabonnements sans le dire.
	 */
	public void testTokenInGeneratedURLRoundTrips() throws Exception {
		GlobalContext globalContext = getContext();
		Mailing mailing = buildMailing(globalContext, "https://demo.javlo.org/javlo/view/fr/p.html");

		UnsubscribeInfo info = MailingThread.buildUnsubscribeInfo(mailing, new InternetAddress(RECIPIENT), globalContext);
		String token = extractParam(info.getUrl(), "lut");

		UnsubscribeTokenService.TokenData data = new UnsubscribeTokenService(globalContext.getUnsubscribeSecret()).read(token, globalContext.getContextKey());

		assertNotNull("le jeton de l'en-tête ne se relit pas", data);
		assertEquals(RECIPIENT, data.getEmail());
		assertEquals(globalContext.getContextKey(), data.getContextKey());
		assertEquals("test-unsubscribe-mailing", data.getMailingId());
		assertEquals(2, data.getRoles().size());
		assertTrue(data.getRoles().contains("newsletter"));
		assertTrue(data.getRoles().contains("client"));
	}

	/**
	 * C1, côté URL : la raison du bug. Un ContentContext en PAGE_MODE produit
	 * le segment '/page/', mappé sur ContentOnlyServlet qui n'exécute aucune
	 * action ; le même contexte en VIEW_MODE ne le produit pas. Réutiliser
	 * l'URL de PAGE_MODE comme URL de désabonnement rendait la fonctionnalité
	 * inerte.
	 */
	public void testPageModeURLCarriesTheNonActionableSegment() throws Exception {
		FakeHttpContext httpContext = new FakeHttpContext("http://demo.javlo.org/view/en/index.html");
		ContentContext ctx = ContentContext.getContentContext(httpContext.getRequest(), httpContext.getResponse());
		ctx.setAllLanguage("en");

		ContentContext pageCtx = ctx.getContextWithOtherRenderMode(ContentContext.PAGE_MODE);
		pageCtx.setAbsoluteURL(true);
		pageCtx.resetDMZServerInter();
		String pageURL = URLHelper.createURL(pageCtx, "/p");

		ContentContext viewCtx = pageCtx.getContextWithOtherRenderMode(ContentContext.VIEW_MODE);
		String viewURL = URLHelper.createURL(viewCtx, "/p");

		assertTrue("PAGE_MODE doit produire /page/ : " + pageURL, pageURL.contains("/page/"));
		assertFalse("VIEW_MODE ne doit pas produire /page/ : " + viewURL, viewURL.contains("/page/"));

		/** l'absoluité doit survivre au changement de mode : sans hôte, l'en-tête est inutilisable */
		assertTrue("VIEW_MODE doit rester absolu : " + viewURL, viewURL.startsWith("http://demo.javlo.org"));
		assertTrue(viewCtx.isAbsoluteURL());
	}

	/** Le lien saisi à la main reste prioritaire et n'est jamais one-click. */
	public void testManualLinkWins() throws Exception {
		GlobalContext globalContext = getContext();
		Mailing mailing = buildMailing(globalContext, "https://demo.javlo.org/javlo/view/fr/p.html");
		mailing.setManualUnsubscribeLink("https://site.be/unsub?email=${email}");

		UnsubscribeInfo info = MailingThread.buildUnsubscribeInfo(mailing, new InternetAddress(RECIPIENT), globalContext);

		assertFalse(info.isOneClick());
		assertFalse(info.getUrl().contains("lut="));
		assertTrue(info.getUrl().contains(RECIPIENT));
	}
}
