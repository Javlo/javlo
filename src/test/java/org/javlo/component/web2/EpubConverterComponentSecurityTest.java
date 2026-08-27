package org.javlo.component.web2;

import java.io.File;
import java.io.IOException;

import org.javlo.helper.ResourceHelper;

import junit.framework.TestCase;

/**
 * non regression tests for the unauthenticated arbitrary file write reported on
 * epub-converter-component.convert : the name sent by the uploader must never
 * be able to place a file outside the epub folder, and only the extensions the
 * converter can actually handle may be written.
 */
public class EpubConverterComponentSecurityTest extends TestCase {

	private File epubFolder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		epubFolder = new File(System.getProperty("java.io.tmpdir"), "javlo-epub-security-" + System.nanoTime());
		assertTrue(epubFolder.mkdirs());
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.deleteFolder(epubFolder);
		super.tearDown();
	}

	private void assertRejected(String submittedName) throws Exception {
		try {
			File resolved = EpubConverterComponent.resolveUploadTarget(epubFolder, submittedName);
			fail("upload name must be refused : '" + submittedName + "' resolved to " + resolved);
		} catch (IOException expected) {
			// the upload is refused before anything is written
		}
	}

	public void testAcceptsPdfInsideTheEpubFolder() throws Exception {
		File resolved = EpubConverterComponent.resolveUploadTarget(epubFolder, "report.pdf");
		assertEquals(epubFolder.getCanonicalFile(), resolved.getParentFile().getCanonicalFile());
		assertEquals("report.pdf", resolved.getName());
	}

	public void testAcceptsDocxInsideTheEpubFolder() throws Exception {
		File resolved = EpubConverterComponent.resolveUploadTarget(epubFolder, "Rapport Annuel.DOCX");
		assertEquals(epubFolder.getCanonicalFile(), resolved.getParentFile().getCanonicalFile());
	}

	public void testRejectsTheReportedJspPayload() throws Exception {
		assertRejected("../../../../../cvdwhoami.jsp");
	}

	/** a traversal keeping an allowed extension : the extension check alone is not enough */
	public void testRejectsTraversalWithAllowedExtension() throws Exception {
		assertRejected("../../../../../evil.pdf");
	}

	public void testRejectsWindowsTraversal() throws Exception {
		assertRejected("..\\..\\..\\..\\evil.pdf");
	}

	public void testRejectsAbsolutePath() throws Exception {
		assertRejected(new File(System.getProperty("java.io.tmpdir"), "evil.pdf").getAbsolutePath());
	}

	public void testRejectsExtensionTheConverterCannotHandle() throws Exception {
		assertRejected("shell.jsp");
	}

	public void testRejectsNameWithoutExtension() throws Exception {
		assertRejected("shell");
	}

	public void testRejectsEmptyName() throws Exception {
		assertRejected("");
	}

	public void testRejectsNullName() throws Exception {
		assertRejected(null);
	}

	/** a name made only of separators must not resolve to the folder itself */
	public void testRejectsDotDotOnly() throws Exception {
		assertRejected("..");
	}
}
