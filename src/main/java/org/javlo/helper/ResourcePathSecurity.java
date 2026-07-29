package org.javlo.helper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.component.core.AbstractVisualComponent;
import org.javlo.component.form.GenericForm;
import org.javlo.config.StaticConfig;
import org.javlo.context.GlobalContext;
import org.javlo.ecom.BasketPersistenceService;
import org.javlo.i18n.I18nResource;
import org.javlo.io.SessionFolder;
import org.javlo.macro.interactive.PushStaticOnFtp;
import org.javlo.module.remote.RemoteService;
import org.javlo.module.ticket.TicketService;
import org.javlo.service.GeoService;
import org.javlo.service.PersistenceService;
import org.javlo.service.proxy.RemoteCacheService;
import org.javlo.service.visitors.UserDataService;
import org.javlo.servlet.SynchronisationServlet;
import org.javlo.user.LdapDirectUserFactory;
import org.javlo.user.Role;

/**
 * Central path policy for every servlet serving files out of a context data
 * folder (FileServlet on /media and /file, ResourceServlet on /resource,
 * ImageTransformServlet on /img and /transform).
 * <p>
 * Those servlets used to map the request path straight onto the data folder,
 * which exposed internal directories - the user store (with password hashes),
 * the content persistence files, the credentials in {@code _private} - to any
 * anonymous visitor. Two independent checks are applied here:
 * <ol>
 * <li>containment: the canonical resolved file must stay inside the base
 * folder, which neutralises every encoding trick since the check is done on the
 * resolved file and not on the request string;</li>
 * <li>policy: the base relative path must sit under a public root
 * ({@code static}, {@code sessionFolder}) and must not sit under a private one
 * ({@code persitence}, {@code users}, {@code _private}, ...).</li>
 * </ol>
 * Both lists are extensible from static-config, and the allow-list can be
 * turned off with {@code security.resource-path-strict = false} for an
 * installation storing public files outside the static folder.
 */
public class ResourcePathSecurity {

	private static Logger logger = Logger.getLogger(ResourcePathSecurity.class.getName());

	/**
	 * root of the mailings, relative to the data folder. The mailing folders of
	 * StaticConfig point inside it (/mailing/todo, /mailing/old, ...).
	 */
	public static final String MAILING_FOLDER = "mailing";

	/** root of the mailing templates, relative to the data folder */
	public static final String MAILING_TEMPLATE_FOLDER = "mailing-template";

	/**
	 * folders and files of the data folder that must never reach a web client,
	 * whatever the allow-list says. Paths are relative to the data folder, and
	 * come from the class owning each one so the policy follows a rename.
	 */
	private static final String[] DEFAULT_PRIVATE_PATHS = {
			/* content store and credentials */
			PersistenceService._DIRECTORY, ResourceHelper.PRIVATE_DIR, GlobalContext.DATABASE_FOLDER,
			/* context internals */
			Role.FOLDER, RemoteService.FOLDER, TicketService.FOLDER, I18nResource.FOLDER, MAILING_FOLDER, MAILING_TEMPLATE_FOLDER, GlobalContext.CALENDAR_FOLDER, RemoteCacheService.FOLDER, PushStaticOnFtp.TEMP_FOLDER, AbstractVisualComponent.VIEW_DATA_FOLDER,
			/* context files */
			GlobalContext.DATA_FILE, GlobalContext.DATA_BACKUP_FILE, GlobalContext.NAVIGATION_FILE, GlobalContext.REDIRECT_URL_LIST, GlobalContext.URL_404_LIST, UserDataService.FILE, LdapDirectUserFactory.QUERIES_FILE, GeoService.CACHE_FILE, SynchronisationServlet.FILE_INFO };

	/**
	 * only segment starting with a dot which stays reachable, the others are
	 * hidden files.
	 */
	private static final String WELL_KNOWN = ".well-known";

	/**
	 * clean a request path so it can be compared with the configured lists :
	 * back-slashes to slashes, no duplicated slash, always a leading slash, no
	 * trailing slash.
	 */
	public static String normalize(String path) {
		if (path == null) {
			return null;
		}
		String out = path.replace('\\', '/');
		while (out.contains("//")) {
			out = out.replace("//", "/");
		}
		if (!out.startsWith("/")) {
			out = '/' + out;
		}
		while (out.length() > 1 && out.endsWith("/")) {
			out = out.substring(0, out.length() - 1);
		}
		return out;
	}

	/**
	 * resolve <code>relativePath</code> under <code>base</code> and check the
	 * result did not escape the base folder. Symbolic links pointing outside
	 * the base are rejected as well, since the comparison is done on canonical
	 * paths.
	 *
	 * @return the resolved file, or null when the path escapes the base folder.
	 */
	public static File resolveInside(File base, String relativePath) {
		if (base == null || relativePath == null) {
			return null;
		}
		try {
			String canonicalBase = base.getCanonicalPath();
			File file = new File(base, relativePath);
			String canonicalFile = file.getCanonicalPath();
			if (!canonicalFile.equals(canonicalBase) && !canonicalFile.startsWith(canonicalBase + File.separator)) {
				logger.severe("path traversal blocked : '" + relativePath + "' resolved to " + canonicalFile + " outside " + canonicalBase);
				return null;
			}
			return file;
		} catch (IOException e) {
			logger.severe("could not resolve '" + relativePath + "' in " + base + " : " + e.getMessage());
			return null;
		}
	}

	/**
	 * path of <code>file</code> relative to <code>base</code>, slash separated
	 * and with a leading slash, or null when the file is not inside the base.
	 */
	public static String relativize(File base, File file) {
		if (base == null || file == null) {
			return null;
		}
		try {
			String canonicalBase = base.getCanonicalPath();
			String canonicalFile = file.getCanonicalPath();
			if (canonicalFile.equals(canonicalBase)) {
				return "/";
			}
			if (!canonicalFile.startsWith(canonicalBase + File.separator)) {
				return null;
			}
			return normalize(canonicalFile.substring(canonicalBase.length()));
		} catch (IOException e) {
			logger.severe("could not relativize " + file + " on " + base + " : " + e.getMessage());
			return null;
		}
	}

	/**
	 * roots of the data folder that may be served to a web client.
	 */
	public static Set<String> getPublicPaths(StaticConfig staticConfig) {
		Set<String> out = new LinkedHashSet<String>();
		out.add(normalize(staticConfig.getStaticFolder()));
		out.add(normalize(SessionFolder.SESSION_FOLDER));
		out.addAll(staticConfig.getExtraPublicResourcePaths());
		return out;
	}

	/**
	 * folders and files of the data folder that are never served to a web
	 * client, even when they sit under a public root.
	 */
	public static Set<String> getPrivatePaths(StaticConfig staticConfig) {
		Set<String> out = getDefaultPrivatePaths();
		out.add(normalize(staticConfig.getUserFolder()));
		out.add(normalize(staticConfig.getBackupFolder()));
		out.add(normalize(staticConfig.getCacheFolder()));
		out.add(normalize(staticConfig.getExternComponentFolder()));
		/*
		 * those two sit under the static folder, which is a public root : they
		 * are built from the configured folder name and not from a literal.
		 */
		String staticFolder = normalize(staticConfig.getStaticFolder());
		out.add(staticFolder + '/' + GenericForm.DYNAMIC_FORM_RESULT_FOLDER);
		out.add(staticFolder + '/' + BasketPersistenceService.FOLDER);
		out.addAll(staticConfig.getExtraPrivateResourcePaths());
		return out;
	}

	/**
	 * check a data folder relative path against the policy. The path is
	 * expected to be already resolved and contained (see
	 * {@link #resolveInside(File, String)}), this method only applies the
	 * allow-list and the deny-list.
	 */
	public static boolean isServable(StaticConfig staticConfig, String relativePath) {
		return isServable(getPublicPaths(staticConfig), getPrivatePaths(staticConfig), staticConfig.isResourcePathStrict(), relativePath);
	}

	/**
	 * policy without the config lookup, so it can be exercised on its own.
	 *
	 * @param strict
	 *            when false the allow-list is ignored, the deny-list and the
	 *            hidden file rule still apply.
	 */
	public static boolean isServable(Set<String> publicPaths, Set<String> privatePaths, boolean strict, String relativePath) {
		if (relativePath == null) {
			return false;
		}
		String path = normalize(relativePath).toLowerCase();
		if (path.equals("/")) {
			return false;
		}
		for (String segment : path.split("/")) {
			if (segment.startsWith(".") && !segment.equals(WELL_KNOWN)) {
				return false;
			}
		}
		for (String privatePath : privatePaths) {
			if (isUnder(path, privatePath.toLowerCase())) {
				return false;
			}
		}
		if (!strict) {
			return true;
		}
		for (String publicPath : publicPaths) {
			if (isUnder(path, publicPath.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * the private paths applied whatever the configuration, exposed for the
	 * tests and for the installations building their own policy.
	 */
	public static Set<String> getDefaultPrivatePaths() {
		Set<String> out = new LinkedHashSet<String>();
		for (String path : DEFAULT_PRIVATE_PATHS) {
			/* the constants come from their owner class, with or without a leading slash */
			out.add(normalize(path));
		}
		return out;
	}

	private static boolean isUnder(String path, String folder) {
		return path.equals(folder) || path.startsWith(folder + "/");
	}

	/**
	 * resolve a request path inside a context data folder and apply the full
	 * policy. This is the method the servlets serving the data folder must use
	 * instead of building the File themselves.
	 *
	 * @return the file to serve, or null when access must be refused. The
	 *         caller is expected to answer 403 (or 404 when it prefers not to
	 *         confirm the existence of the resource).
	 */
	public static File resolvePublicFile(StaticConfig staticConfig, String dataFolder, String relativePath) {
		if (staticConfig == null || dataFolder == null || relativePath == null) {
			return null;
		}
		File base = new File(dataFolder);
		File file = resolveInside(base, relativePath);
		if (file == null) {
			return null;
		}
		/*
		 * the policy is applied on the resolved path so that /static/../users
		 * is refused, and not only the paths escaping the data folder.
		 */
		String resolvedPath = relativize(base, file);
		if (!isServable(staticConfig, resolvedPath)) {
			logger.warning("access refused to private resource : '" + relativePath + "' (resolved : " + resolvedPath + ")");
			return null;
		}
		return file;
	}

	/**
	 * split a ';' separated config value, as the other list properties of
	 * static-config.
	 */
	public static Set<String> parsePathList(String value) {
		if (StringHelper.isEmpty(value)) {
			return Collections.emptySet();
		}
		Set<String> out = new LinkedHashSet<String>();
		List<String> paths = Arrays.asList(value.split(";"));
		for (String path : paths) {
			if (!StringHelper.isEmpty(path.trim())) {
				out.add(normalize(path.trim()));
			}
		}
		return out;
	}

}
