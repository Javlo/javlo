package org.javlo.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import junit.framework.TestCase;

public class UnsubscribeTokenServiceTest extends TestCase {

	private static final String SECRET = "un-secret-de-test-suffisamment-long";

	public void testRoundTrip() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		Collection<String> roles = Arrays.asList("newsletter", "client");
		String token = service.create(new UnsubscribeTokenService.TokenData("monsite", "mailing-42", "Jean@Exemple.be", roles, 1700000000000L));
		UnsubscribeTokenService.TokenData data = service.read(token, "monsite");
		assertNotNull(data);
		assertEquals("monsite", data.getContextKey());
		assertEquals("mailing-42", data.getMailingId());
		assertEquals("Jean@Exemple.be", data.getEmail());
		assertEquals(1700000000000L, data.getTimestamp());
		assertEquals(2, data.getRoles().size());
		assertTrue(data.getRoles().contains("newsletter"));
		assertTrue(data.getRoles().contains("client"));
	}

	public void testEmptyRoles() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		String token = service.create(new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", new LinkedList<String>(), 1L));
		UnsubscribeTokenService.TokenData data = service.read(token, "monsite");
		assertNotNull(data);
		assertEquals(0, data.getRoles().size());
	}

	public void testSpecialCharactersInFields() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		String token = service.create(new UnsubscribeTokenService.TokenData("mon|site", "m|1", "a+tag@b.be", Arrays.asList("role|bizarre"), 1L));
		UnsubscribeTokenService.TokenData data = service.read(token, "mon|site");
		assertNotNull(data);
		assertEquals("mon|site", data.getContextKey());
		assertEquals("m|1", data.getMailingId());
		assertEquals("a+tag@b.be", data.getEmail());
		assertTrue(data.getRoles().contains("role|bizarre"));
	}

	public void testTamperedTokenIsRejected() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		String token = service.create(new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", Arrays.asList("r"), 1L));
		char lastChar = token.charAt(token.length() - 1);
		char replacement = (lastChar == 'A') ? 'B' : 'A';
		String tampered = token.substring(0, token.length() - 1) + replacement;
		assertNull(service.read(tampered, "monsite"));
	}

	public void testWrongContextKeyIsRejected() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		String token = service.create(new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", Arrays.asList("r"), 1L));
		assertNull(service.read(token, "autresite"));
	}

	public void testWrongSecretIsRejected() throws Exception {
		String token = new UnsubscribeTokenService(SECRET).create(new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", Arrays.asList("r"), 1L));
		assertNull(new UnsubscribeTokenService("un-autre-secret").read(token, "monsite"));
	}

	public void testGarbageIsRejectedWithoutException() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		assertNull(service.read(null, "monsite"));
		assertNull(service.read("", "monsite"));
		assertNull(service.read("pas-du-base64-!!!", "monsite"));
		assertNull(service.read("QUJD", "monsite"));
	}

	public void testTwoTokensForSameDataDiffer() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		UnsubscribeTokenService.TokenData data = new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", Arrays.asList("r"), 1L);
		assertFalse(service.create(data).equals(service.create(data)));
	}

	public void testTokenIsUrlSafe() throws Exception {
		UnsubscribeTokenService service = new UnsubscribeTokenService(SECRET);
		String token = service.create(new UnsubscribeTokenService.TokenData("monsite", "m1", "a@b.be", Arrays.asList("r"), 1L));
		assertTrue(token.matches("[A-Za-z0-9_-]+"));
	}
}
