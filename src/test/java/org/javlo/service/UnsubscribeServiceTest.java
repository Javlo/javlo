package org.javlo.service;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import junit.framework.TestCase;

public class UnsubscribeServiceTest extends TestCase {

	private File file;

	@Override
	protected void setUp() throws Exception {
		file = File.createTempFile("unsubscribe-test", ".csv");
		file.delete();
	}

	@Override
	protected void tearDown() throws Exception {
		if (file.exists()) {
			file.delete();
		}
	}

	public void testUnsubscribeWithRoles() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter"));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
		assertFalse(service.isUnsubscribed("jean@exemple.be", Arrays.asList("client")));
		assertFalse(service.isUnsubscribed("autre@exemple.be", Arrays.asList("newsletter")));
	}

	public void testUnsubscribeWithoutRoleBlocksEverything() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", new LinkedList<String>());
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("client")));
		assertTrue(service.isUnsubscribed("jean@exemple.be", new LinkedList<String>()));
	}

	public void testAllRolesEntryBlocksEverything() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList(UnsubscribeService.ALL_ROLES));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("nimporte-quoi")));
	}

	public void testEmailNormalisation() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("  Jean@Exemple.BE ", Arrays.asList("newsletter"));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
		assertTrue(service.isUnsubscribed("JEAN@EXEMPLE.BE", Arrays.asList("newsletter")));
	}

	public void testPersistence() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter", "client"));
		UnsubscribeService reloaded = new UnsubscribeService(file);
		assertTrue(reloaded.isUnsubscribed("jean@exemple.be", Arrays.asList("client")));
		assertEquals(1, reloaded.getAll().size());
		UnsubscribeService.UnsubscribeEntry entry = reloaded.getAll().iterator().next();
		assertEquals("jean@exemple.be", entry.getEmail());
		assertEquals(2, entry.getRoles().size());
		assertNotNull(entry.getDate());
	}

	public void testIdempotence() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter"));
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter"));
		assertEquals(1, service.getAll().size());
		assertEquals(1, new UnsubscribeService(file).getAll().size());
	}

	public void testRolesAccumulate() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter"));
		service.unsubscribe("jean@exemple.be", Arrays.asList("client"));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
		assertTrue(service.isUnsubscribed("jean@exemple.be", Arrays.asList("client")));
		assertEquals(1, service.getAll().size());
		assertEquals(2, new UnsubscribeService(file).getAll().iterator().next().getRoles().size());
	}

	public void testResubscribe() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe("jean@exemple.be", Arrays.asList("newsletter"));
		service.unsubscribe("marie@exemple.be", Arrays.asList("newsletter"));
		service.resubscribe("JEAN@exemple.be");
		assertFalse(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
		assertTrue(service.isUnsubscribed("marie@exemple.be", Arrays.asList("newsletter")));
		UnsubscribeService reloaded = new UnsubscribeService(file);
		assertEquals(1, reloaded.getAll().size());
		assertFalse(reloaded.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
	}

	public void testEmptyOrMissingFile() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		assertEquals(0, service.getAll().size());
		assertFalse(service.isUnsubscribed("jean@exemple.be", Arrays.asList("newsletter")));
	}

	public void testNullAndEmptyEmailAreIgnored() throws Exception {
		UnsubscribeService service = new UnsubscribeService(file);
		service.unsubscribe(null, Arrays.asList("newsletter"));
		service.unsubscribe("   ", Arrays.asList("newsletter"));
		assertEquals(0, service.getAll().size());
		assertFalse(service.isUnsubscribed(null, Arrays.asList("newsletter")));
	}
}
