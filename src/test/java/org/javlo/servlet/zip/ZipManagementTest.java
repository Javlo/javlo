package org.javlo.servlet.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import junit.framework.TestCase;

/**
 * Regression tests for the ZipSlip mitigation in {@link ZipManagement#saveFile}.
 * Each test creates an isolated temp directory and asserts that traversal attempts
 * neither escape the destination nor leave files behind outside it.
 */
public class ZipManagementTest extends TestCase {

	private Path target;
	private Path siblingOutside;

	@Override
	protected void setUp() throws Exception {
		target = Files.createTempDirectory("javlo-zipslip-target-");
		siblingOutside = target.getParent().resolve("javlo-zipslip-outside-" + System.nanoTime());
		Files.createDirectory(siblingOutside);
	}

	@Override
	protected void tearDown() throws Exception {
		deleteRecursively(target.toFile());
		deleteRecursively(siblingOutside.toFile());
	}

	private static void deleteRecursively(File f) {
		if (f == null || !f.exists()) return;
		if (f.isDirectory()) {
			File[] kids = f.listFiles();
			if (kids != null) for (File k : kids) deleteRecursively(k);
		}
		f.delete();
	}

	private static ByteArrayInputStream payload() {
		return new ByteArrayInputStream("pwned".getBytes(StandardCharsets.UTF_8));
	}

	/** A benign entry resolves under the target and gets written normally. */
	public void testSafeEntryIsAccepted() throws Exception {
		File written = ZipManagement.saveFile(null, target.toString(), "sub/ok.txt", payload());
		assertTrue("safe entry must be written", written.exists());
		assertTrue("safe entry must stay under target",
				written.getCanonicalPath().startsWith(target.toFile().getCanonicalPath() + File.separator));
		assertEquals("pwned", new String(Files.readAllBytes(written.toPath()), StandardCharsets.UTF_8));
	}

	/** "../shell.jsp" must be rejected and nothing written outside target. */
	public void testParentTraversalIsRejected() throws Exception {
		String malicious = "../" + siblingOutside.getFileName() + "/shell.jsp";
		try {
			ZipManagement.saveFile(null, target.toString(), malicious, payload());
			fail("ZipSlip entry should have been rejected: " + malicious);
		} catch (IOException expected) {
			// expected
		}
		File leaked = siblingOutside.resolve("shell.jsp").toFile();
		assertFalse("no file may be created outside the destination directory", leaked.exists());
	}

	/** Deep traversal "../../../etc/passwd" must be rejected. */
	public void testDeepTraversalIsRejected() throws Exception {
		try {
			ZipManagement.saveFile(null, target.toString(), "../../../etc/passwd", payload());
			fail("deep ZipSlip entry should have been rejected");
		} catch (IOException expected) {
			// expected
		}
	}

	/** Windows-style backslash traversal must also be rejected. */
	public void testBackslashTraversalIsRejected() throws Exception {
		String malicious = "..\\" + siblingOutside.getFileName() + "\\shell.jsp";
		try {
			ZipManagement.saveFile(null, target.toString(), malicious, payload());
			// On non-Windows JVMs backslashes are treated as part of the filename.
			// In that case the file must still resolve UNDER the target directory.
			File created = new File(target.toFile(), malicious);
			assertTrue("on non-Windows, backslash entry must stay under target as a literal file name",
					created.getCanonicalPath().startsWith(target.toFile().getCanonicalPath()));
		} catch (IOException expected) {
			// expected on Windows where backslash is a separator
		}
		File leaked = siblingOutside.resolve("shell.jsp").toFile();
		assertFalse("no file may be created outside the destination directory via backslash", leaked.exists());
	}

	/** Null entry name must be rejected, not NPE. */
	public void testNullEntryNameIsRejected() {
		try {
			ZipManagement.saveFile(null, target.toString(), null, payload());
			fail("null entry name should have been rejected");
		} catch (IOException expected) {
			// expected
		}
	}

	/** Absolute path entries must not escape the target. */
	public void testAbsolutePathIsContained() throws Exception {
		String absolute = new File(siblingOutside.toFile(), "shell.jsp").getAbsolutePath();
		try {
			File written = ZipManagement.saveFile(null, target.toString(), absolute, payload());
			// Some JDKs resolve "new File(target, absolutePath)" as absolutePath itself —
			// in that case the canonical check inside resolveSafeZipEntry must reject it.
			assertTrue("absolute entry, if accepted, must remain under target",
					written.getCanonicalPath().startsWith(target.toFile().getCanonicalPath() + File.separator));
		} catch (IOException expected) {
			// also acceptable: explicit rejection
		}
		File leaked = new File(siblingOutside.toFile(), "shell.jsp");
		assertFalse("absolute-path entry must not leak outside the destination", leaked.exists());
	}
}
