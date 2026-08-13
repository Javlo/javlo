package org.javlo.mailing;

import java.io.File;
import java.nio.file.Files;

import org.javlo.external.agitos.dkim.SigningAlgorithm;

import junit.framework.TestCase;

/**
 * La clé privée DKIM est copiée dans le dossier du mailing au moment de la
 * préparation. Le nom du fichier porte l'algorithme de signature (voir
 * {@link DKIMFactory#getSigningAlgorithm(String)}) : si la copie perd le suffixe
 * "_256", le mailing signe en rsa-sha1 alors que l'enregistrement DNS publié
 * annonce h=sha256, et la signature est rejetée.
 */
public class MailingDkimKeyTest extends TestCase {

	private File root;
	private Mailing mailing;

	@Override
	protected void setUp() throws Exception {
		root = Files.createTempDirectory("javlo-mailing-dkim").toFile();
		mailing = new Mailing();
		mailing.setDirectory(new File(root, "mailing-id"));
	}

	@Override
	protected void tearDown() throws Exception {
		deleteRecursive(root);
	}

	private static void deleteRecursive(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		File[] children = file.listFiles();
		if (children != null) {
			for (File child : children) {
				deleteRecursive(child);
			}
		}
		file.delete();
	}

	private File createSourceKey(String name) throws Exception {
		File source = new File(root, "source/" + name);
		source.getParentFile().mkdirs();
		Files.write(source.toPath(), new byte[] { 1, 2, 3 });
		return source;
	}

	/** Une clé SHA-256 doit rester une clé SHA-256 après la copie. */
	public void testStoreKeepsSha256FileName() throws Exception {
		mailing.storePrivateKeyFile(createSourceKey("privatekey_256.bin"));

		File stored = mailing.getDkimPrivateKeyFile();
		assertEquals("privatekey_256.bin", stored.getName());
		assertTrue("la clé doit être copiée dans le dossier du mailing", stored.exists());
		assertSame(SigningAlgorithm.SHA256withRSA, DKIMFactory.getSigningAlgorithm(stored.getAbsolutePath()));
	}

	/** Une clé SHA-1 historique reste signée en SHA-1. */
	public void testStoreKeepsLegacyFileName() throws Exception {
		mailing.storePrivateKeyFile(createSourceKey("privatekey.bin"));

		File stored = mailing.getDkimPrivateKeyFile();
		assertEquals("privatekey.bin", stored.getName());
		assertTrue(stored.exists());
		assertSame(SigningAlgorithm.SHA1withRSA, DKIMFactory.getSigningAlgorithm(stored.getAbsolutePath()));
	}

	/** Un mailing déjà sur disque avec l'ancien nom reste lisible. */
	public void testLegacyKeyOnDiskIsStillFound() throws Exception {
		File legacy = new File(root, "mailing-id/privatekey.bin");
		legacy.getParentFile().mkdirs();
		Files.write(legacy.toPath(), new byte[] { 1 });

		assertEquals(legacy.getAbsolutePath(), mailing.getDkimPrivateKeyFile().getAbsolutePath());
	}

	/** La clé SHA-256 est prioritaire si les deux fichiers coexistent. */
	public void testSha256KeyWinsOverLegacyKey() throws Exception {
		File dir = new File(root, "mailing-id");
		dir.mkdirs();
		Files.write(new File(dir, "privatekey.bin").toPath(), new byte[] { 1 });
		Files.write(new File(dir, "privatekey_256.bin").toPath(), new byte[] { 2 });

		assertEquals("privatekey_256.bin", mailing.getDkimPrivateKeyFile().getName());
	}

	/** Sans clé sur disque, on retombe sur le nom par défaut (SHA-256). */
	public void testDefaultsToSha256WhenNoKeyOnDisk() throws Exception {
		assertEquals("privatekey_256.bin", mailing.getDkimPrivateKeyFile().getName());
	}
}
