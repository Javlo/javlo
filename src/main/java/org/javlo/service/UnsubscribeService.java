package org.javlo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
import org.javlo.io.TransactionFile;

/**
 * Liste des adresses désabonnées d'un site.
 *
 * Stockage en fichier texte, une ligne par adresse :
 * <code>email TAB roles TAB date-iso</code>, les rôles séparés par des
 * point-virgules. Le rôle réservé {@link #ALL_ROLES} signifie « tous les
 * mailings du site ».
 */
public class UnsubscribeService {

	private static Logger logger = Logger.getLogger(UnsubscribeService.class.getName());

	public static final String ALL_ROLES = "*";

	private static final String SEPARATOR = "\t";

	private static final String ROLE_SEPARATOR = ";";

	private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

	private final File file;

	private Map<String, UnsubscribeEntry> entries = null;

	public UnsubscribeService(File file) {
		this.file = file;
	}

	private static final String KEY = UnsubscribeService.class.getName();

	private static final String FILE_NAME = "unsubscribe.csv";

	public static UnsubscribeService getInstance(GlobalContext globalContext) {
		UnsubscribeService instance = (UnsubscribeService) globalContext.getAttribute(KEY);
		if (instance == null) {
			File file = new File(URLHelper.mergePath(globalContext.getDataFolder(), ResourceHelper.PRIVATE_DIR, FILE_NAME));
			instance = new UnsubscribeService(file);
			globalContext.setAttribute(KEY, instance);
		}
		return instance;
	}

	static String normalize(String email) {
		if (email == null) {
			return null;
		}
		String out = email.trim().toLowerCase();
		return out.isEmpty() ? null : out;
	}

	private synchronized Map<String, UnsubscribeEntry> getEntries() {
		if (entries == null) {
			entries = new LinkedHashMap<String, UnsubscribeEntry>();
			if (file.exists()) {
				try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
					String line;
					while ((line = reader.readLine()) != null) {
						UnsubscribeEntry entry = parse(line);
						if (entry != null) {
							entries.put(entry.getEmail(), entry);
						}
					}
				} catch (IOException e) {
					logger.warning("can not read unsubscribe file " + file + " : " + e.getMessage());
				}
			}
		}
		return entries;
	}

	private static UnsubscribeEntry parse(String line) {
		if (line == null || line.trim().isEmpty()) {
			return null;
		}
		String[] parts = line.split(SEPARATOR, -1);
		if (parts.length < 2) {
			return null;
		}
		String email = normalize(parts[0]);
		if (email == null) {
			return null;
		}
		Set<String> roles = new LinkedHashSet<String>();
		if (!parts[1].trim().isEmpty()) {
			Collections.addAll(roles, parts[1].split(ROLE_SEPARATOR, -1));
		}
		Date date = null;
		if (parts.length > 2) {
			try {
				date = new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).parse(parts[2]);
			} catch (Exception e) {
				date = null;
			}
		}
		return new UnsubscribeEntry(email, roles, date);
	}

	private static String render(UnsubscribeEntry entry) {
		StringBuilder roles = new StringBuilder();
		for (String role : entry.getRoles()) {
			if (roles.length() > 0) {
				roles.append(ROLE_SEPARATOR);
			}
			roles.append(role);
		}
		Date date = entry.getDate() == null ? new Date() : entry.getDate();
		return entry.getEmail() + SEPARATOR + roles + SEPARATOR + new SimpleDateFormat(DATE_PATTERN, Locale.ROOT).format(date);
	}

	public synchronized boolean isUnsubscribed(String email, Collection<String> roles) {
		String key = normalize(email);
		if (key == null) {
			return false;
		}
		UnsubscribeEntry entry = getEntries().get(key);
		if (entry == null) {
			return false;
		}
		if (entry.getRoles().contains(ALL_ROLES)) {
			return true;
		}
		if (roles != null) {
			for (String role : roles) {
				if (entry.getRoles().contains(role)) {
					return true;
				}
			}
		}
		return false;
	}

	public synchronized void unsubscribe(String email, Collection<String> roles) {
		String key = normalize(email);
		if (key == null) {
			return;
		}
		Set<String> newRoles = new LinkedHashSet<String>();
		if (roles != null) {
			for (String role : roles) {
				if (role != null && !role.trim().isEmpty()) {
					newRoles.add(role.trim());
				}
			}
		}
		if (newRoles.isEmpty()) {
			newRoles.add(ALL_ROLES);
		}
		UnsubscribeEntry existing = getEntries().get(key);
		if (existing != null) {
			if (existing.getRoles().containsAll(newRoles)) {
				return;
			}
			newRoles.addAll(existing.getRoles());
			getEntries().put(key, new UnsubscribeEntry(key, newRoles, new Date()));
			storeAll();
		} else {
			UnsubscribeEntry entry = new UnsubscribeEntry(key, newRoles, new Date());
			getEntries().put(key, entry);
			append(entry);
		}
	}

	public synchronized void resubscribe(String email) {
		String key = normalize(email);
		if (key == null) {
			return;
		}
		if (getEntries().remove(key) != null) {
			storeAll();
		}
	}

	public synchronized Collection<UnsubscribeEntry> getAll() {
		return new LinkedList<UnsubscribeEntry>(getEntries().values());
	}

	private void append(UnsubscribeEntry entry) {
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, true))) {
				writer.write(render(entry));
				writer.newLine();
			}
		} catch (IOException e) {
			logger.warning("can not write unsubscribe file " + file + " : " + e.getMessage());
		}
	}

	private void storeAll() {
		TransactionFile tf = null;
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			tf = new TransactionFile(file);
			try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(tf.getOutputStream(), StandardCharsets.UTF_8))) {
				for (UnsubscribeEntry entry : getEntries().values()) {
					writer.write(render(entry));
					writer.newLine();
				}
			}
			tf.commit();
		} catch (IOException e) {
			logger.warning("can not write unsubscribe file " + file + " : " + e.getMessage());
			try {
				if (tf != null) {
					tf.rollback();
				}
			} catch (IOException e1) {
				logger.warning("can not rollback unsubscribe file " + file + " : " + e1.getMessage());
			}
		}
	}

	public static class UnsubscribeEntry {

		private final String email;
		private final Set<String> roles;
		private final Date date;

		public UnsubscribeEntry(String email, Set<String> roles, Date date) {
			this.email = email;
			this.roles = roles;
			this.date = date;
		}

		public String getEmail() {
			return email;
		}

		public Collection<String> getRoles() {
			return roles;
		}

		public Date getDate() {
			return date;
		}
	}
}
