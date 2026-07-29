package org.javlo.helper;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.javlo.component.form.GenericForm;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.service.PersistenceService;
import org.javlo.servlet.SynchronisationServlet;

import junit.framework.TestCase;

/**
 * non regression tests for the information disclosure on /media, /file and
 * /resource : the user store and the content persistence files must never be
 * reachable from a request path.
 */
public class ResourcePathSecurityTest extends TestCase {

	private static final Set<String> PUBLIC_PATHS = new LinkedHashSet<String>(Arrays.asList("/static", "/sessionFolder"));

	private Set<String> privatePaths;

	private File dataFolder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		/* what getPrivatePaths(StaticConfig) builds for a default configuration */
		privatePaths = ResourcePathSecurity.getDefaultPrivatePaths();
		privatePaths.add("/users");
		privatePaths.add("/backup");
		privatePaths.add("/_cache");
		privatePaths.add("/components");
		privatePaths.add("/static/" + GenericForm.DYNAMIC_FORM_RESULT_FOLDER);
		privatePaths.add("/static/" + BasketPersistenceService.FOLDER);

		dataFolder = new File(System.getProperty("java.io.tmpdir"), "javlo-path-security-" + System.nanoTime());
		assertTrue(new File(dataFolder, "static/images").mkdirs());
		assertTrue(new File(dataFolder, "users/view").mkdirs());
		assertTrue(new File(dataFolder, "persitence").mkdirs());
		assertTrue(new File(dataFolder, "static/images/photo.jpg").createNewFile());
		assertTrue(new File(dataFolder, "users/view/users-list.csv").createNewFile());
		assertTrue(new File(dataFolder, "persitence/content_2.xml").createNewFile());
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.deleteFolder(dataFolder);
		super.tearDown();
	}

	private boolean servable(String path) {
		return ResourcePathSecurity.isServable(PUBLIC_PATHS, privatePaths, true, path);
	}

	public void testNormalize() throws Exception {
		assertEquals("/static/images", ResourcePathSecurity.normalize("static/images"));
		assertEquals("/static/images", ResourcePathSecurity.normalize("/static//images/"));
		assertEquals("/static/images", ResourcePathSecurity.normalize("\\static\\images"));
		assertEquals("/", ResourcePathSecurity.normalize("/"));
		assertNull(ResourcePathSecurity.normalize(null));
	}

	public void testPublicPathsStayServable() throws Exception {
		assertTrue(servable("/static/images/photo.jpg"));
		assertTrue(servable("static/files/doc.pdf"));
		assertTrue(servable("/sessionFolder/_sid-abc/upload.png"));
	}

	/** the paths of the vulnerability report */
	public void testReportedPathsAreRefused() throws Exception {
		assertFalse(servable("/users/view/users-list.csv"));
		assertFalse(servable("/users/admin/edit-users-list.csv"));
		assertFalse(servable("/persitence/content_2.xml"));
	}

	public void testOtherPrivatePathsAreRefused() throws Exception {
		assertFalse(servable("/_private/credentials/smtp.properties"));
		assertFalse(servable("/db/javlo.mv.db"));
		assertFalse(servable("/context_data.properties"));
		assertFalse(servable("/backup/2026-07-29.zip"));
		assertFalse(servable("/navigation.txt"));
		assertFalse(servable("/file_structure.properties"));
	}

	/** a private folder sitting under a public root is refused too */
	public void testPrivatePathUnderPublicRoot() throws Exception {
		assertFalse(servable("/static/dynamic-form-result/form-1.csv"));
		assertTrue(servable("/static/galleries/summer/1.jpg"));
	}

	public void testHiddenFilesAreRefused() throws Exception {
		assertFalse(servable("/static/.htaccess"));
		assertFalse(servable("/.ssh/id_rsa"));
		assertFalse(servable("/"));
	}

	public void testCaseDoesNotBypassDenyList() throws Exception {
		assertFalse(servable("/USERS/view/users-list.csv"));
		assertFalse(servable("/Persitence/content_2.xml"));
	}

	/** non strict mode keeps the deny-list, it only drops the allow-list */
	public void testNonStrictModeStillRefusesPrivatePaths() throws Exception {
		assertTrue(ResourcePathSecurity.isServable(PUBLIC_PATHS, privatePaths, false, "/somewhere/else/doc.pdf"));
		assertFalse(ResourcePathSecurity.isServable(PUBLIC_PATHS, privatePaths, false, "/persitence/content_2.xml"));
		assertFalse(ResourcePathSecurity.isServable(PUBLIC_PATHS, privatePaths, false, "/users/view/users-list.csv"));
	}

	public void testResolveInsideKeepsNormalPath() throws Exception {
		File file = ResourcePathSecurity.resolveInside(dataFolder, "/static/images/photo.jpg");
		assertNotNull(file);
		assertTrue(file.exists());
	}

	public void testResolveInsideBlocksTraversal() throws Exception {
		assertNull(ResourcePathSecurity.resolveInside(dataFolder, "/../../etc/passwd"));
		assertNull(ResourcePathSecurity.resolveInside(dataFolder, "/static/../../secret.txt"));
		assertNull(ResourcePathSecurity.resolveInside(dataFolder, ".."));
	}

	/** a traversal staying inside the data folder must still hit the policy */
	public void testTraversalBackIntoPrivateFolderIsRefused() throws Exception {
		File file = ResourcePathSecurity.resolveInside(dataFolder, "/static/../persitence/content_2.xml");
		assertNotNull("this path does not leave the data folder", file);
		String relative = ResourcePathSecurity.relativize(dataFolder, file);
		assertEquals("/persitence/content_2.xml", relative);
		assertFalse("the policy must be applied on the resolved path", servable(relative));
	}

	public void testRelativize() throws Exception {
		assertEquals("/static/images/photo.jpg", ResourcePathSecurity.relativize(dataFolder, new File(dataFolder, "static/images/photo.jpg")));
		assertEquals("/", ResourcePathSecurity.relativize(dataFolder, dataFolder));
		assertNull(ResourcePathSecurity.relativize(dataFolder, new File(dataFolder.getParentFile(), "elsewhere.txt")));
	}

	/**
	 * the deny-list is built from the constants of the owner classes : if one of
	 * them is renamed without updating its owner, this test fails instead of
	 * silently reopening the hole.
	 */
	public void testDenyListComesFromOwnerConstants() throws Exception {
		Set<String> defaults = ResourcePathSecurity.getDefaultPrivatePaths();
		assertTrue(defaults.contains(ResourcePathSecurity.normalize(PersistenceService._DIRECTORY)));
		assertTrue(defaults.contains(ResourcePathSecurity.normalize(ResourceHelper.PRIVATE_DIR)));
		assertTrue(defaults.contains(ResourcePathSecurity.normalize(GlobalContext.DATABASE_FOLDER)));
		assertTrue(defaults.contains(ResourcePathSecurity.normalize(SynchronisationServlet.FILE_INFO)));
		/* every entry is normalized, whatever the constant looked like */
		for (String path : defaults) {
			assertEquals(path, ResourcePathSecurity.normalize(path));
		}
	}

	public void testParsePathList() throws Exception {
		Set<String> paths = ResourcePathSecurity.parsePathList("/foo; bar ;;/baz/");
		assertEquals(3, paths.size());
		assertTrue(paths.contains("/foo"));
		assertTrue(paths.contains("/bar"));
		assertTrue(paths.contains("/baz"));
		assertTrue(ResourcePathSecurity.parsePathList("").isEmpty());
		assertTrue(ResourcePathSecurity.parsePathList(null).isEmpty());
	}

}
