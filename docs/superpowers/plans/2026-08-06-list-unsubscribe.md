# List-Unsubscribe one-click — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Générer automatiquement les en-têtes `List-Unsubscribe` et `List-Unsubscribe-Post` sur les mailings Javlo, avec un désabonnement one-click réellement effectif pour toutes les origines de destinataires.

**Architecture :** L'en-tête pointe vers l'URL absolue de la page du mailing avec un token AES-GCM par destinataire, traité par une action du groupe `unsecure` (donc sans session ni contrôle de module). Le désabonnement alimente une liste de suppression persistante par site, consultée à la constitution des destinataires et juste avant chaque envoi.

**Tech Stack :** Java 17, Maven, JUnit 4.13.2 (style `junit.framework.TestCase`), Jakarta Mail 2.1.3, JCE natif du JDK (`AES/GCM/NoPadding`). Aucune dépendance nouvelle.

**Spec de référence :** `docs/superpowers/specs/2026-08-06-list-unsubscribe-design.md`

## Global Constraints

- Java 17 (`maven.compiler.release=17` dans `pom.xml:63`).
- Tests en JUnit 4.13.2, écrits dans le style existant du dépôt : classe étendant `junit.framework.TestCase`, méthodes préfixées `test`, pas d'annotations `@Test`. Voir `src/test/java/org/javlo/helper/StringSecurityUtilTest.java`.
- Aucune dépendance Maven nouvelle. Tout ce qui est cryptographique vient du JDK.
- Commande de test : `mvn -q test -Dtest=<NomDeClasse>`. Compilation seule : `mvn -q -DskipTests compile`.
- **Avant la Task 1**, établir la référence : lancer `mvn test` une fois et noter les échecs préexistants. Les étapes de ce plan attendent `PASS` sur la suite complète ; si la référence est déjà rouge, l'attendu devient « aucun échec nouveau par rapport à la référence ». Ne jamais corriger un test cassé sans rapport avec ce travail — le signaler.
- Encodage des sources : UTF-8. Les fichiers existants utilisent des tabulations pour l'indentation — les respecter.
- Ne jamais casser une signature publique existante de `MailService` : les surcharges acceptant `String unsubribeLink` restent en place.
- Le pseudo-rôle global est la constante `UnsubscribeService.ALL_ROLES`, de valeur `"*"`.
- Le nom de l'action web est `unsecure.unsubscribe`. Il doit se terminer par `unsubscribe` pour que `Mailing.getCountUnsubscribe():769-780` continue de compter les désabonnements.

## Faits vérifiés dans le code, à ne pas re-vérifier

- `GlobalContext.getAttribute(String):1128` et `GlobalContext.setAttribute(String, Object):3007` existent.
- `GlobalContext.getRealInstance(ServletContext, String contextKey):516` permet d'obtenir le contexte d'un site depuis sa clé, hors requête HTTP.
- `Mailing.getRoles():810` renvoie `List<String>`.
- `Mailing.onMailSent(InternetAddress to, String error):569` écrit `<horodatage> [error]` dans le journal d'envoi, et appelle `addErrorReceive(to)` si `error` est non vide.
- `MailingThread.sendReport():93` teste `data.toLowerCase().indexOf("unsubscribe") >= 1` pour afficher « not sent » dans le rapport.
- `URLHelper extends ElementaryURLHelper`, donc `URLHelper.addParam(String, String, String)` (`ElementaryURLHelper:102`) est accessible.
- `ResourceHelper.PRIVATE_DIR` vaut `"_private"`.
- Le module mailing déclare ses écrans dans `MailingModuleContext:73-77` et pose ses attributs de requête dans `MailingAction:145-162`.

---

### Task 1 : Correctifs préalables

Deux défauts indépendants, corrigés avant de construire dessus. Aucun test unitaire possible : `GlobalContext` exige un `ServletContext`, et le dépôt n'a pas d'infrastructure de test pour ça. Vérification par compilation et lecture.

**Files:**
- Modify: `src/main/java/org/javlo/context/GlobalContext.java:4342-4344`
- Modify: `src/main/java/org/javlo/mailing/Mailing.java:137-140`

**Interfaces:**
- Consumes: rien
- Produces: rien (nettoyage)

- [ ] **Step 1 : Ajouter le `save()` manquant**

Dans `GlobalContext.java`, remplacer :

```java
	public void setUnsubscribeLink(String link) {
		properties.setProperty("unsubscribeLink", link);
	}
```

par :

```java
	public void setUnsubscribeLink(String link) {
		synchronized (properties) {
			properties.setProperty("unsubscribeLink", link);
			save();
		}
	}
```

Le bloc `synchronized (properties)` et l'appel à `save()` reproduisent le motif de `setMailingSenders:3367-3372` et `setMailingSubject:3374-3379`. Sans lui, la valeur n'est persistée que par effet de bord du `setDKIMDomain()` appelé ensuite dans `AdminAction:535`.

- [ ] **Step 2 : Supprimer le code mort**

Dans `Mailing.java`, supprimer entièrement la méthode :

```java
	String getUnsubscribeURL(String mail) {
		String params = "?webaction=mailing.Unsubscriberole&mail=" + mail + "&roles=" + StringHelper.collectionToString(roles);
		return getUnsubscribeURL() + params;
	}
```

Ne pas toucher à `getUnsubscribeURL()` sans argument (ligne 623), ni au champ `unsubscribeURL` : ils sont réutilisés à la Task 6.

- [ ] **Step 3 : Vérifier la compilation**

Run: `mvn -q -DskipTests compile`
Expected: succès. Si une erreur signale un appel à `getUnsubscribeURL(String)`, c'est que la méthode n'était pas morte — arrêter et signaler.

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/org/javlo/context/GlobalContext.java src/main/java/org/javlo/mailing/Mailing.java
git commit -m "fix: persist unsubscribeLink and drop dead getUnsubscribeURL(String)"
```

---

### Task 2 : Le générateur de token

Chiffrement authentifié AES-256-GCM. La classe ne dépend pas de `GlobalContext` : elle prend un secret en chaîne de caractères, ce qui la rend testable sans infrastructure servlet.

**Files:**
- Create: `src/main/java/org/javlo/service/UnsubscribeTokenService.java`
- Test: `src/test/java/org/javlo/service/UnsubscribeTokenServiceTest.java`

**Interfaces:**
- Consumes: rien
- Produces:
  - `new UnsubscribeTokenService(String secret)`
  - `String create(UnsubscribeTokenService.TokenData data)`
  - `UnsubscribeTokenService.TokenData read(String token, String expectedContextKey)` — renvoie `null` si invalide
  - `new UnsubscribeTokenService.TokenData(String contextKey, String mailingId, String email, Collection<String> roles, long timestamp)`
  - Accesseurs de `TokenData` : `getContextKey()`, `getMailingId()`, `getEmail()`, `getRoles()` (`Collection<String>`), `getTimestamp()`

- [ ] **Step 1 : Écrire le test qui échoue**

Créer `src/test/java/org/javlo/service/UnsubscribeTokenServiceTest.java` :

```java
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
```

- [ ] **Step 2 : Lancer le test pour vérifier qu'il échoue**

Run: `mvn -q test -Dtest=UnsubscribeTokenServiceTest`
Expected: échec de compilation, `UnsubscribeTokenService` n'existe pas.

- [ ] **Step 3 : Écrire l'implémentation**

Créer `src/main/java/org/javlo/service/UnsubscribeTokenService.java` :

```java
package org.javlo.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Génère et relit les tokens de désabonnement utilisés dans l'en-tête
 * List-Unsubscribe.
 *
 * Le token est un chiffré authentifié AES-256-GCM : toute modification, même
 * d'un seul bit, est détectée au déchiffrement, et l'adresse email n'apparaît
 * pas en clair dans l'URL.
 */
public class UnsubscribeTokenService {

	private static Logger logger = Logger.getLogger(UnsubscribeTokenService.class.getName());

	private static final String VERSION = "v1";

	private static final String TRANSFORMATION = "AES/GCM/NoPadding";

	private static final int IV_SIZE = 12;

	private static final int TAG_SIZE_BIT = 128;

	private static final char FIELD_SEPARATOR = '|';

	private static final char ROLE_SEPARATOR = ';';

	private final SecretKey key;

	private final SecureRandom random = new SecureRandom();

	public UnsubscribeTokenService(String secret) {
		this.key = buildKey(secret);
	}

	private static SecretKey buildKey(String secret) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8));
			return new SecretKeySpec(hash, "AES");
		} catch (Exception e) {
			throw new IllegalStateException("can not build unsubscribe key", e);
		}
	}

	public String create(TokenData data) {
		try {
			byte[] iv = new byte[IV_SIZE];
			random.nextBytes(iv);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BIT, iv));
			byte[] encrypted = cipher.doFinal(serialize(data).getBytes(StandardCharsets.UTF_8));
			ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
			buffer.put(iv);
			buffer.put(encrypted);
			return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException("can not create unsubscribe token", e);
		}
	}

	/**
	 * @return les données du token, ou null si le token est invalide, corrompu,
	 *         ou émis pour un autre site.
	 */
	public TokenData read(String token, String expectedContextKey) {
		if (token == null || token.trim().isEmpty()) {
			return null;
		}
		try {
			byte[] raw = Base64.getUrlDecoder().decode(token);
			if (raw.length <= IV_SIZE) {
				return null;
			}
			byte[] iv = new byte[IV_SIZE];
			System.arraycopy(raw, 0, iv, 0, IV_SIZE);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_SIZE_BIT, iv));
			byte[] plain = cipher.doFinal(raw, IV_SIZE, raw.length - IV_SIZE);
			TokenData data = deserialize(new String(plain, StandardCharsets.UTF_8));
			if (data == null || !data.getContextKey().equals(expectedContextKey)) {
				return null;
			}
			return data;
		} catch (Exception e) {
			logger.fine("invalid unsubscribe token : " + e.getMessage());
			return null;
		}
	}

	private static String encode(String value) {
		return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
	}

	private static String decode(String value) {
		return URLDecoder.decode(value, StandardCharsets.UTF_8);
	}

	private static String serialize(TokenData data) {
		StringBuilder roles = new StringBuilder();
		for (String role : data.getRoles()) {
			if (roles.length() > 0) {
				roles.append(ROLE_SEPARATOR);
			}
			roles.append(encode(role));
		}
		StringBuilder out = new StringBuilder();
		out.append(VERSION);
		out.append(FIELD_SEPARATOR).append(encode(data.getContextKey()));
		out.append(FIELD_SEPARATOR).append(encode(data.getMailingId()));
		out.append(FIELD_SEPARATOR).append(encode(data.getEmail()));
		out.append(FIELD_SEPARATOR).append(roles);
		out.append(FIELD_SEPARATOR).append(data.getTimestamp());
		return out.toString();
	}

	private static TokenData deserialize(String plain) {
		String[] parts = plain.split("\\" + FIELD_SEPARATOR, -1);
		if (parts.length != 6 || !VERSION.equals(parts[0])) {
			return null;
		}
		Collection<String> roles = new LinkedList<String>();
		if (!parts[4].isEmpty()) {
			for (String role : parts[4].split(String.valueOf(ROLE_SEPARATOR), -1)) {
				roles.add(decode(role));
			}
		}
		return new TokenData(decode(parts[1]), decode(parts[2]), decode(parts[3]), roles, Long.parseLong(parts[5]));
	}

	public static class TokenData {

		private final String contextKey;
		private final String mailingId;
		private final String email;
		private final Collection<String> roles;
		private final long timestamp;

		public TokenData(String contextKey, String mailingId, String email, Collection<String> roles, long timestamp) {
			this.contextKey = contextKey;
			this.mailingId = mailingId;
			this.email = email;
			this.roles = roles == null ? new LinkedList<String>() : roles;
			this.timestamp = timestamp;
		}

		public String getContextKey() {
			return contextKey;
		}

		public String getMailingId() {
			return mailingId;
		}

		public String getEmail() {
			return email;
		}

		public Collection<String> getRoles() {
			return roles;
		}

		public long getTimestamp() {
			return timestamp;
		}
	}
}
```

`URLEncoder` transforme `|` en `%7C`, donc aucun champ ne peut casser le découpage, quel que soit son contenu. C'est ce que vérifie `testSpecialCharactersInFields`.

- [ ] **Step 4 : Lancer les tests**

Run: `mvn -q test -Dtest=UnsubscribeTokenServiceTest`
Expected: PASS, 9 tests.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/org/javlo/service/UnsubscribeTokenService.java src/test/java/org/javlo/service/UnsubscribeTokenServiceTest.java
git commit -m "feat: add AES-GCM unsubscribe token service"
```

---

### Task 3 : La liste de suppression

Service par site adossé à un fichier ligne par ligne. Le constructeur prenant un `File` sert aux tests ; la fabrique `getInstance(GlobalContext)` est ajoutée à la Task 4.

**Files:**
- Create: `src/main/java/org/javlo/service/UnsubscribeService.java`
- Test: `src/test/java/org/javlo/service/UnsubscribeServiceTest.java`

**Interfaces:**
- Consumes: rien
- Produces:
  - `UnsubscribeService.ALL_ROLES` — constante `String` de valeur `"*"`
  - `new UnsubscribeService(File file)`
  - `boolean isUnsubscribed(String email, Collection<String> roles)`
  - `void unsubscribe(String email, Collection<String> roles)`
  - `Collection<UnsubscribeService.UnsubscribeEntry> getAll()`
  - `void resubscribe(String email)`
  - `UnsubscribeEntry` : `getEmail()`, `getRoles()` (`Collection<String>`), `getDate()` (`Date`)

- [ ] **Step 1 : Écrire le test qui échoue**

Créer `src/test/java/org/javlo/service/UnsubscribeServiceTest.java` :

```java
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
```

- [ ] **Step 2 : Lancer le test pour vérifier qu'il échoue**

Run: `mvn -q test -Dtest=UnsubscribeServiceTest`
Expected: échec de compilation, `UnsubscribeService` n'existe pas.

- [ ] **Step 3 : Écrire l'implémentation**

Créer `src/main/java/org/javlo/service/UnsubscribeService.java` :

```java
package org.javlo.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
				date = new SimpleDateFormat(DATE_PATTERN).parse(parts[2]);
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
		return entry.getEmail() + SEPARATOR + roles + SEPARATOR + new SimpleDateFormat(DATE_PATTERN).format(date);
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
		try {
			if (file.getParentFile() != null) {
				file.getParentFile().mkdirs();
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8, false))) {
				for (UnsubscribeEntry entry : getEntries().values()) {
					writer.write(render(entry));
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.warning("can not write unsubscribe file " + file + " : " + e.getMessage());
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
```

- [ ] **Step 4 : Lancer les tests**

Run: `mvn -q test -Dtest=UnsubscribeServiceTest`
Expected: PASS, 10 tests.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/org/javlo/service/UnsubscribeService.java src/test/java/org/javlo/service/UnsubscribeServiceTest.java
git commit -m "feat: add per-site unsubscribe suppression list"
```

---

### Task 4 : Câblage des services sur GlobalContext

Le secret par site et la fabrique du service de suppression.

**Files:**
- Modify: `src/main/java/org/javlo/context/GlobalContext.java` (juste après `setUnsubscribeLink`, vers la ligne 4345)
- Modify: `src/main/java/org/javlo/service/UnsubscribeService.java`

**Interfaces:**
- Consumes: `UnsubscribeService(File)` de la Task 3
- Produces:
  - `String GlobalContext.getUnsubscribeSecret()` — génère et persiste à la première demande
  - `static UnsubscribeService UnsubscribeService.getInstance(GlobalContext globalContext)`

- [ ] **Step 1 : Ajouter le secret par site**

Dans `GlobalContext.java`, juste après la méthode `setUnsubscribeLink` corrigée à la Task 1 :

```java
	/**
	 * secret propre au site, utilisé pour signer les tokens de désabonnement.
	 * Généré à la première demande avec un CSPRNG.
	 */
	public String getUnsubscribeSecret() {
		synchronized (properties) {
			String secret = properties.getString("mailing.unsubscribe-secret", null);
			if (StringHelper.isEmpty(secret)) {
				byte[] raw = new byte[32];
				new java.security.SecureRandom().nextBytes(raw);
				secret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
				properties.setProperty("mailing.unsubscribe-secret", secret);
				save();
			}
			return secret;
		}
	}
```

Les noms pleinement qualifiés évitent d'ajouter des imports dans un fichier de plus de 4500 lignes, pour un usage ponctuel.

- [ ] **Step 2 : Ajouter la fabrique du service de suppression**

Dans `UnsubscribeService.java`, ajouter ces imports :

```java
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.URLHelper;
```

et, juste après le constructeur `UnsubscribeService(File file)` :

```java
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
```

`getAttribute:1128` et `setAttribute:3007` existent sur `GlobalContext` — vérifié. L'emplacement du fichier reproduit celui de `token_page.properties` (`GlobalContext.getTokenPageFile():3834-3836`).

- [ ] **Step 3 : Vérifier la compilation et la non-régression**

Run: `mvn -q -DskipTests compile`
Expected: succès.

Run: `mvn -q test -Dtest=UnsubscribeServiceTest`
Expected: PASS, 10 tests — la fabrique n'a pas cassé le constructeur utilisé par les tests.

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/org/javlo/context/GlobalContext.java src/main/java/org/javlo/service/UnsubscribeService.java
git commit -m "feat: wire per-site unsubscribe secret and suppression list"
```

---

### Task 5 : L'objet de valeur `UnsubscribeInfo` et l'émission des en-têtes

La décision d'émettre `List-Unsubscribe-Post` est isolée dans un objet de valeur pur, testable sans session mail.

**Files:**
- Create: `src/main/java/org/javlo/mailing/UnsubscribeInfo.java`
- Test: `src/test/java/org/javlo/mailing/UnsubscribeInfoTest.java`
- Modify: `src/main/java/org/javlo/mailing/MailService.java:315`, `:355-357`, `:237`, `:286`, `:606`
- Modify: `src/main/java/org/javlo/mailing/EMail.java:25` et `:169-175`
- Modify: `src/main/java/org/javlo/external/agitos/dkim/DKIMSigner.java:52-57`

**Interfaces:**
- Consumes: rien
- Produces:
  - `static UnsubscribeInfo UnsubscribeInfo.manual(String url)` — en-tête brut, jamais one-click
  - `static UnsubscribeInfo UnsubscribeInfo.oneClick(String url)` — one-click seulement si l'URL est en HTTPS
  - `boolean isEmpty()`, `String getHeaderValue()`, `boolean isOneClick()`, `String getUrl()`
  - `UnsubscribeInfo EMail.getUnsubscribeInfo()` / `void EMail.setUnsubscribeInfo(UnsubscribeInfo)`
  - `String MailService.sendMail(Transport, InternetAddress, InternetAddress, String, String, boolean, UnsubscribeInfo, DKIMBean, String)`

- [ ] **Step 1 : Écrire le test qui échoue**

Créer `src/test/java/org/javlo/mailing/UnsubscribeInfoTest.java` :

```java
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
```

- [ ] **Step 2 : Lancer le test pour vérifier qu'il échoue**

Run: `mvn -q test -Dtest=UnsubscribeInfoTest`
Expected: échec de compilation, `UnsubscribeInfo` n'existe pas.

- [ ] **Step 3 : Créer l'objet de valeur**

Créer `src/main/java/org/javlo/mailing/UnsubscribeInfo.java` :

```java
package org.javlo.mailing;

import org.javlo.helper.StringHelper;

/**
 * Valeur de l'en-tête List-Unsubscribe d'un message, et décision d'émettre ou
 * non List-Unsubscribe-Post.
 *
 * Immuable. La règle du one-click est isolée ici pour rester testable sans
 * session mail.
 */
public class UnsubscribeInfo {

	private final String url;

	private final boolean oneClick;

	private final boolean generated;

	private UnsubscribeInfo(String url, boolean oneClick, boolean generated) {
		this.url = url;
		this.oneClick = oneClick;
		this.generated = generated;
	}

	/**
	 * Lien saisi à la main dans les propriétés du site. Utilisé tel quel : on ne
	 * peut pas garantir qu'une URL externe respecte le protocole one-click.
	 */
	public static UnsubscribeInfo manual(String url) {
		return new UnsubscribeInfo(url, false, false);
	}

	/**
	 * Lien généré par Javlo. Le one-click n'est annoncé que sur HTTPS, le
	 * RFC 8058 l'exigeant.
	 */
	public static UnsubscribeInfo oneClick(String url) {
		boolean https = !StringHelper.isEmpty(url) && url.trim().toLowerCase().startsWith("https://");
		return new UnsubscribeInfo(url, https, true);
	}

	public boolean isEmpty() {
		return StringHelper.isEmpty(url);
	}

	public boolean isOneClick() {
		return oneClick && !isEmpty();
	}

	public String getUrl() {
		return url;
	}

	/**
	 * @return la valeur exacte à poser dans l'en-tête. Un lien généré est
	 *         encadré par des chevrons ; un lien manuel est laissé tel quel,
	 *         l'auteur les ayant déjà écrits s'il le fallait.
	 */
	public String getHeaderValue() {
		if (isEmpty()) {
			return null;
		}
		if (generated) {
			return '<' + url.trim() + '>';
		}
		return url;
	}
}
```

Le champ `generated` est distinct de `oneClick` : un lien généré sur un site en HTTP doit être encadré par des chevrons tout en n'étant pas annoncé one-click. C'est ce que vérifie `testGeneratedHttpUrlIsWrappedButNotOneClick`.

- [ ] **Step 4 : Lancer les tests**

Run: `mvn -q test -Dtest=UnsubscribeInfoTest`
Expected: PASS, 6 tests.

- [ ] **Step 5 : Porter `EMail` sur le nouvel objet**

Dans `EMail.java`, remplacer le champ `private String unsubscribeLink;` (ligne 25) par :

```java
	private UnsubscribeInfo unsubscribeInfo;
```

et remplacer les accesseurs des lignes 169-175 par :

```java
	public UnsubscribeInfo getUnsubscribeInfo() {
		return unsubscribeInfo;
	}

	public void setUnsubscribeInfo(UnsubscribeInfo unsubscribeInfo) {
		this.unsubscribeInfo = unsubscribeInfo;
	}

	public String getUnsubscribeLink() {
		return unsubscribeInfo == null ? null : unsubscribeInfo.getUrl();
	}

	public void setUnsubscribeLink(String unsubscribeLink) {
		this.unsubscribeInfo = UnsubscribeInfo.manual(unsubscribeLink);
	}
```

Les deux dernières méthodes préservent les appelants existants de `EMail`.

- [ ] **Step 6 : Porter `MailService` sur le nouvel objet**

Dans `MailService.java`, changer le type du paramètre de la méthode privée ligne 315 : `String unsubscribeLink` devient `UnsubscribeInfo unsubscribe`. Puis remplacer les lignes 355-357 :

```java
			if (!StringHelper.isEmpty(unsubscribeLink)) {
				msg.setHeader("List-Unsubscribe", unsubscribeLink);
			}
```

par :

```java
			if (unsubscribe != null && !unsubscribe.isEmpty()) {
				msg.setHeader("List-Unsubscribe", unsubscribe.getHeaderValue());
				if (unsubscribe.isOneClick()) {
					msg.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
				}
			}
```

Adapter les appelants de la méthode privée :

- ligne 237 : `email.getUnsubscribeLink()` devient `email.getUnsubscribeInfo()`
- ligne 286 : `email.getUnsubscribeLink()` devient `email.getUnsubscribeInfo()`
- ligne 606 : `unsubribeLink` devient `UnsubscribeInfo.manual(unsubribeLink)`

Ajouter une surcharge publique acceptant un `UnsubscribeInfo`, juste après celle de la ligne 601 — c'est elle qu'appellera `MailingThread` à la Task 6 :

```java
	public String sendMail(Transport transport, InternetAddress sender, InternetAddress recipient, String subject, String content, boolean isHTML, UnsubscribeInfo unsubscribe, DKIMBean dkinBean, String mailId) throws MessagingException {
		List<InternetAddress> recipients = null;
		if (recipient != null) {
			recipients = Arrays.asList(recipient);
		}
		return sendMail(transport, sender, recipients, null, null, subject, content, null, isHTML, null, unsubscribe, dkinBean, mailId);
	}
```

La surcharge à `String` de la ligne 601 reste en place : c'est elle que continuent d'utiliser les mails transactionnels via la ligne 618, qui n'émettent donc jamais de `List-Unsubscribe-Post`.

- [ ] **Step 7 : Faire signer `List-Unsubscribe-Post` par DKIM**

Dans `DKIMSigner.java`, ajouter `"List-Unsubscribe-Post"` au tableau `defaultHeadersToSign` (lignes 52-57), juste après `"List-Unsubscribe"` :

```java
			"List-Archive","List-Help","List-Unsubscribe","List-Unsubscribe-Post","MIME-Version","Message-ID","Resent-Sender",
```

Sans cela, l'en-tête one-click voyagerait hors de la signature DKIM, ce que les filtres anti-spam pénalisent.

- [ ] **Step 8 : Vérifier la compilation et la non-régression**

Run: `mvn -q -DskipTests compile`
Expected: succès. Toute erreur signale un appelant de `EMail.getUnsubscribeLink()` ou de la méthode privée non adapté — le corriger.

Run: `mvn -q test`
Expected: PASS. Aucun test existant ne doit régresser.

- [ ] **Step 9 : Commit**

```bash
git add src/main/java/org/javlo/mailing/UnsubscribeInfo.java src/test/java/org/javlo/mailing/UnsubscribeInfoTest.java src/main/java/org/javlo/mailing/EMail.java src/main/java/org/javlo/mailing/MailService.java src/main/java/org/javlo/external/agitos/dkim/DKIMSigner.java
git commit -m "feat: emit List-Unsubscribe-Post header for one-click links"
```

---

### Task 6 : Génération de l'en-tête à l'envoi

L'URL de base est calculée à la création du mailing, où un `ContentContext` est disponible. Le secret, lui, est lu directement depuis le contexte du site au moment de l'envoi : `MailingThread` peut l'obtenir via `GlobalContext.getRealInstance(application, contextKey)`, ce qui évite de dupliquer un secret dans chaque fichier de mailing.

**Files:**
- Modify: `src/main/java/org/javlo/mailing/MailingThread.java:34` (champ `application`), `:46-53` (constructeur), `:133-190` (boucle d'envoi)
- Modify: `src/main/java/org/javlo/module/mailing/MailingModuleContext.java:291-297`

**Interfaces:**
- Consumes: `UnsubscribeTokenService`, `UnsubscribeInfo`, `Mailing.getUnsubscribeURL()`, `Mailing.getManualUnsubscribeLink()`, `Mailing.getRoles()`, `GlobalContext.getUnsubscribeSecret()`
- Produces: `GlobalContext MailingThread.getSiteContext(Mailing)` — méthode privée, consommée par la Task 8

- [ ] **Step 1 : Restaurer l'accès au ServletContext dans MailingThread**

Dans `MailingThread.java`, remplacer la ligne 34 commentée :

```java
	//ServletContext application;
```

par :

```java
	private ServletContext application;
```

et, dans le constructeur (lignes 46-53), remplacer la ligne commentée :

```java
//		application = inApplication;
```

par :

```java
		application = inApplication;
```

`jakarta.servlet.ServletContext` est déjà importé (ligne 3).

- [ ] **Step 2 : Ajouter l'accès au contexte du site**

Toujours dans `MailingThread.java`, ajouter cette méthode privée, par exemple juste avant `sendMailing` :

```java
	/**
	 * Contexte du site auquel appartient le mailing. Le thread s'exécutant hors
	 * requête HTTP, on le résout depuis la clé de contexte portée par le
	 * mailing.
	 */
	private GlobalContext getSiteContext(Mailing mailing) {
		if (StringHelper.isEmpty(mailing.getContextKey())) {
			return null;
		}
		try {
			return GlobalContext.getRealInstance(application, mailing.getContextKey());
		} catch (Exception e) {
			logger.warning("can not load context '" + mailing.getContextKey() + "' : " + e.getMessage());
			return null;
		}
	}
```

Ajouter les imports en tête du fichier :

```java
import org.javlo.context.GlobalContext;
import org.javlo.helper.URLHelper;
import org.javlo.service.UnsubscribeTokenService;
```

- [ ] **Step 3 : Ajouter le constructeur de l'en-tête**

Toujours dans `MailingThread.java`, ajouter :

```java
	/**
	 * Construit l'en-tête List-Unsubscribe du destinataire. Le lien saisi à la
	 * main dans les propriétés du site est prioritaire et n'est jamais
	 * one-click ; sinon Javlo génère un lien signé propre au destinataire.
	 */
	private static UnsubscribeInfo buildUnsubscribeInfo(Mailing mailing, InternetAddress to, GlobalContext siteContext) {
		String manualLink = mailing.getManualUnsubscribeLink();
		if (!StringHelper.isEmpty(manualLink)) {
			return UnsubscribeInfo.manual(manualLink.replace("${email}", to.getAddress()));
		}
		if (siteContext == null || StringHelper.isEmpty(mailing.getUnsubscribeURL())) {
			return UnsubscribeInfo.manual(null);
		}
		UnsubscribeTokenService tokenService = new UnsubscribeTokenService(siteContext.getUnsubscribeSecret());
		String token = tokenService.create(new UnsubscribeTokenService.TokenData(mailing.getContextKey(), mailing.getId(), to.getAddress(), mailing.getRoles(), System.currentTimeMillis()));
		String url = URLHelper.addParam(URLHelper.addParam(mailing.getUnsubscribeURL(), "webaction", "unsecure.unsubscribe"), "lut", token);
		return UnsubscribeInfo.oneClick(url);
	}
```

- [ ] **Step 4 : Brancher dans la boucle d'envoi**

Dans `sendMailing`, résoudre le contexte du site une seule fois. Juste après la ligne 145 (`MailService mailingManager = MailService.getInstance(mailConfig);`) :

```java
			GlobalContext siteContext = getSiteContext(mailing);
```

Puis remplacer le bloc des lignes 172-177 :

```java
				try {
					String unsubsribeLink = mailing.getManualUnsubscribeLink();
					if (!StringHelper.isEmpty(unsubsribeLink)) {
						unsubsribeLink = unsubsribeLink.replace("${email}", to.getAddress());
					}					
					mailing.setWarningMessage(mailingManager.sendMail(transport, mailing.getFrom(), to, mailing.getSubject(), content.replace("##MAILING-ID##", mailing.getId()), true, unsubsribeLink, dkimBean, mailing.getId()));
				} catch (Exception ex) {
```

par :

```java
				try {
					UnsubscribeInfo unsubscribe = buildUnsubscribeInfo(mailing, to, siteContext);
					mailing.setWarningMessage(mailingManager.sendMail(transport, mailing.getFrom(), to, mailing.getSubject(), content.replace("##MAILING-ID##", mailing.getId()), true, unsubscribe, dkimBean, mailing.getId()));
				} catch (Exception ex) {
```

- [ ] **Step 5 : Renseigner l'URL de base à la création du mailing**

Dans `MailingModuleContext.sendMailing`, le bloc des lignes 291-297 devient :

```java
		if (!StringHelper.isEmpty(ctx.getGlobalContext().getUnsubscribeLink())) {
			String link = ctx.getGlobalContext().getUnsubscribeLink();
			if (link.contains("page:")) {
				link = URLHelper.replacePageReference(ctx.getContextForAbsoluteURL().getContextWithOtherRenderMode(ContentContext.VIEW_MODE), link);
			}
			m.setManualUnsubscribeLink(link);
		} else {
			m.setUnsubscribeURL(url.toString());
		}
```

`url` est la variable déjà construite ligne 273 — `URL url = new URL(URLHelper.createURL(pageCtx))` avec `pageCtx.setAbsoluteURL(true)` ligne 270 — donc l'URL absolue de la page du mailing. Le champ `unsubscribeURL` est déjà persisté par `Mailing.store` (`Mailing:492-493`) et relu au chargement (`Mailing:302`) : rien à ajouter côté sérialisation.

Le `else` traduit la décision de la spec : le champ manuel est un override total, et il n'y a pas de one-click quand il est rempli.

- [ ] **Step 6 : Vérifier la compilation et la non-régression**

Run: `mvn -q -DskipTests compile`
Expected: succès.

Run: `mvn -q test`
Expected: PASS.

- [ ] **Step 7 : Commit**

```bash
git add src/main/java/org/javlo/mailing/MailingThread.java src/main/java/org/javlo/module/mailing/MailingModuleContext.java
git commit -m "feat: generate signed one-click unsubscribe header per recipient"
```

---

### Task 7 : L'action de désabonnement

L'action publique, sans session, et le comportement partagé par les deux chemins d'entrée.

**Files:**
- Modify: `src/main/java/org/javlo/module/mailing/MailingAction.java:318-356`
- Modify: `src/main/java/org/javlo/actions/UnsecureAction.java`
- Modify: `src/main/java/org/javlo/component/links/UnsubsribeLink.java:34-36`

**Interfaces:**
- Consumes: `UnsubscribeTokenService`, `UnsubscribeService`, `GlobalContext.getUnsubscribeSecret()`
- Produces: l'action web `unsecure.unsubscribe`, acceptant soit `lut=<token>` (en-tête), soit `_mfb=<id>` (lien du corps)

- [ ] **Step 1 : Étendre le comportement de désabonnement**

Dans `MailingAction.java`, remplacer intégralement `performUnsubscribe` (lignes 318-356) par :

```java
	public static final String UNSUBSCRIBE_TOKEN_PARAM_NAME = "lut";

	/**
	 * Désabonne un destinataire. Deux jetons possibles : 'lut', signé, porté par
	 * l'en-tête List-Unsubscribe ; ou '_mfb', porté par le lien présent dans le
	 * corps du mail.
	 *
	 * Renvoie toujours null : un jeton invalide ne doit pas être distingué d'un
	 * jeton valide, sous peine de renseigner un attaquant.
	 */
	public static String performUnsubscribe(ServletContext application, HttpServletRequest request, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		GlobalContext globalContext = GlobalContext.getInstance(ctx.getRequest());

		String email = null;
		String mailingId = null;
		Collection<String> roles = new LinkedList<String>();

		String signedToken = rs.getParameter(UNSUBSCRIBE_TOKEN_PARAM_NAME, null);
		if (signedToken != null) {
			UnsubscribeTokenService tokenService = new UnsubscribeTokenService(globalContext.getUnsubscribeSecret());
			UnsubscribeTokenService.TokenData data = tokenService.read(signedToken, globalContext.getContextKey());
			if (data == null) {
				logger.warning("invalid unsubscribe token on site : " + globalContext.getContextKey());
				return null;
			}
			email = data.getEmail();
			mailingId = data.getMailingId();
			roles.addAll(data.getRoles());
		} else {
			String mfb = rs.getParameter(MailingAction.MAILING_FEEDBACK_PARAM_NAME, null);
			if (mfb == null) {
				return null;
			}
			DataToIDService serv = DataToIDService.getInstance(application);
			String rawData = serv.getData(mfb);
			if (rawData == null) {
				return null;
			}
			Map<String, String> params = StringHelper.uriParamToMap(rawData);
			if (params == null) {
				return null;
			}
			email = params.get("to");
			mailingId = params.get("mailing");
			roles.addAll(StringHelper.stringToCollection(rs.getParameter("roles", ""), ";"));
		}

		if (StringHelper.isEmpty(email)) {
			return null;
		}

		logger.info("mailing unsubscribe : " + email + " site:" + globalContext.getContextKey() + " roles:" + roles);

		/** liste de suppression : couvre toutes les origines de destinataires **/
		UnsubscribeService.getInstance(globalContext).unsubscribe(email, roles);

		/** retrait des rôles du compte utilisateur, s'il existe **/
		try {
			InternetAddress add = new InternetAddress(email);
			IUserFactory userFactory = UserFactory.createUserFactory(request);
			User user = userFactory.getUser(add.getAddress());
			if (user != null) {
				user.getUserInfo().removeRoles(new HashSet<String>(roles));
				userFactory.store();
			} else {
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				PrintStream out = new PrintStream(outStream);
				out.println("Site title : " + globalContext.getGlobalTitle());
				out.println("E-Mail     : " + email);
				out.println("");
				out.println("--");
				out.println("Direct Link : " + URLHelper.createAbsoluteViewURL(ctx, "/"));
				out.close();
				NetHelper.sendMailToAdministrator(globalContext, new InternetAddress(email), "Mailing unsubscribe : " + globalContext.getContextKey(), new String(outStream.toByteArray()));
			}
		} catch (AddressException e) {
			logger.warning("bad email on unsubscribe : " + email);
		}

		/** trace pour la statistique du module mailing **/
		if (mailingId != null) {
			try {
				Mailing mailing = new Mailing();
				if (mailing.isExist(application, mailingId)) {
					mailing.setId(StaticConfig.getInstance(application).getMailingStaticConfig(), mailingId);
					FeedBackMailingBean bean = new FeedBackMailingBean();
					bean.setEmail(email);
					bean.setDate(new Date());
					bean.setUrl(ctx.getRequest().getPathInfo());
					bean.setWebaction("unsecure.unsubscribe");
					bean.setIp(ctx.getRequest().getRemoteHost());
					mailing.addFeedBack(bean);
				}
			} catch (Exception e) {
				logger.warning("can not store unsubscribe feedback : " + e.getMessage());
			}
		}

		return null;
	}
```

Ajouter les imports manquants en tête de `MailingAction.java` : `java.util.Collection`, `java.util.Date`, `java.util.HashSet`, `java.util.LinkedList`, `java.util.Map`, `jakarta.mail.internet.AddressException`, `jakarta.mail.internet.InternetAddress`, `org.javlo.config.StaticConfig`, `org.javlo.helper.NetHelper`, `org.javlo.helper.URLHelper`, `org.javlo.mailing.FeedBackMailingBean`, `org.javlo.mailing.Mailing`, `org.javlo.service.UnsubscribeService`, `org.javlo.service.UnsubscribeTokenService`. Supprimer ceux que le compilateur signale comme inutilisés.

Le nom `unsecure.unsubscribe` posé dans le `FeedBackMailingBean` se termine par `unsubscribe`, ce que teste `Mailing.getCountUnsubscribe():769-780` : la statistique existante du module continue de fonctionner sans modification.

- [ ] **Step 2 : Exposer l'action dans le groupe unsecure**

Dans `UnsecureAction.java`, ajouter après `performChangePasswordWithToken` :

```java
	public static String performUnsubscribe(ServletContext application, HttpServletRequest request, RequestService rs, ContentContext ctx, MessageRepository messageRepository, I18nAccess i18nAccess) throws Exception {
		return MailingAction.performUnsubscribe(application, request, rs, ctx, messageRepository, i18nAccess);
	}
```

et les imports :

```java
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.javlo.module.mailing.MailingAction;
```

C'est le motif de délégation de `performAskChangePassword` vers `UserAction`. `UnsecureAction` n'implémentant pas `IModuleAction`, `ActionManager:220-232` n'applique aucun contrôle de droits sur le module courant, ce qui rend l'action utilisable sans session.

- [ ] **Step 3 : Repointer le lien du corps de mail**

Dans `UnsubsribeLink.java`, remplacer :

```java
	@Override
	protected String getParam() throws Exception {
		return "?webaction=mailing.unsubscribe&roles="+MailingAction.DATA_MAIL_PREFIX+"roles"+MailingAction.DATA_MAIL_SUFFIX;
	}
```

par :

```java
	@Override
	protected String getParam() throws Exception {
		return "?webaction=unsecure.unsubscribe&roles="+MailingAction.DATA_MAIL_PREFIX+"roles"+MailingAction.DATA_MAIL_SUFFIX;
	}
```

Le composant continue d'utiliser le token `_mfb` : il vit dans le corps d'un mail déjà destiné à cette personne, et changer de jeton casserait les mailings déjà envoyés.

- [ ] **Step 4 : Vérifier la compilation et la non-régression**

Run: `mvn -q -DskipTests compile`
Expected: succès.

Run: `mvn -q test`
Expected: PASS.

- [ ] **Step 5 : Commit**

```bash
git add src/main/java/org/javlo/module/mailing/MailingAction.java src/main/java/org/javlo/actions/UnsecureAction.java src/main/java/org/javlo/component/links/UnsubsribeLink.java
git commit -m "feat: session-less unsubscribe action feeding the suppression list"
```

---

### Task 8 : Le filtrage à l'envoi

Deux points de contrôle : à la constitution des destinataires pour que le compte affiché soit juste, et juste avant chaque envoi pour honorer un désabonnement survenu pendant un mailing en cours.

**Files:**
- Modify: `src/main/java/org/javlo/module/mailing/MailingModuleContext.java:205-247`
- Modify: `src/main/java/org/javlo/mailing/MailingThread.java` (boucle d'envoi, vers la ligne 149)

**Interfaces:**
- Consumes: `UnsubscribeService.getInstance(GlobalContext)`, `UnsubscribeService.isUnsubscribed(String, Collection)`, `MailingThread.getSiteContext(Mailing)` de la Task 6
- Produces: rien

- [ ] **Step 1 : Filtrer à la constitution des destinataires**

Dans `MailingModuleContext.java`, remplacer le `return true;` de la ligne 247 par :

```java
				/** retirer les adresses désabonnées, quelle que soit leur origine **/
				UnsubscribeService unsubscribeService = UnsubscribeService.getInstance(ctx.getGlobalContext());
				Collection<String> targetedRoles = groups == null ? new LinkedList<String>() : new LinkedList<String>(groups);
				Iterator<InternetAddress> recipientIterator = allRecipients.iterator();
				while (recipientIterator.hasNext()) {
					if (unsubscribeService.isUnsubscribed(recipientIterator.next().getAddress(), targetedRoles)) {
						recipientIterator.remove();
					}
				}
				return true;
```

Ajouter les imports `java.util.Collection`, `java.util.Iterator`, `java.util.LinkedList` et `org.javlo.service.UnsubscribeService` s'ils manquent.

Vérifier au préalable le nom du paramètre `ContentContext` de la méthode englobante :

Run: `grep -n "allRecipients.clear()" -B 25 src/main/java/org/javlo/module/mailing/MailingModuleContext.java`

Si le contexte porte un autre nom que `ctx`, adapter l'appel `UnsubscribeService.getInstance(...)` en conséquence.

- [ ] **Step 2 : Filtrer juste avant chaque envoi**

Dans `MailingThread.sendMailing`, insérer en tête de la boucle `while (to != null) {` (ligne 149), avant la ligne `String data = "mailing=" + mailing.getId() + "&to=" + to;` :

```java
				if (siteContext != null && UnsubscribeService.getInstance(siteContext).isUnsubscribed(to.getAddress(), mailing.getRoles())) {
					logger.info("skip unsubscribed receiver : " + to);
					mailing.onMailSent(to, "unsubscribe");
					to = mailing.getNextReceiver();
					continue;
				}
```

La variable `siteContext` est celle résolue à la Task 6, Step 4. Ajouter l'import `org.javlo.service.UnsubscribeService`.

Deux conséquences voulues de `onMailSent(to, "unsubscribe")`, d'après `Mailing:569-583` :

- la valeur écrite est `<horodatage> [unsubscribe]`, dont l'index de `"unsubscribe"` est supérieur ou égal à 1, donc `sendReport:93` affichera le destinataire en « not sent » dans le rapport de fin de mailing ;
- l'adresse est aussi ajoutée à `errorReceivers` via `addErrorReceive`. Cette collection ne sert qu'au rapport et à la remontée des adresses en erreur : y voir figurer une adresse désabonnée est sémantiquement imprécis mais sans effet de bord ailleurs. Ne pas chercher à l'éviter en modifiant `onMailSent`, qui est partagée avec le chemin d'envoi normal.

- [ ] **Step 3 : Vérifier la compilation et la non-régression**

Run: `mvn -q -DskipTests compile`
Expected: succès.

Run: `mvn -q test`
Expected: PASS.

- [ ] **Step 4 : Commit**

```bash
git add src/main/java/org/javlo/module/mailing/MailingModuleContext.java src/main/java/org/javlo/mailing/MailingThread.java
git commit -m "feat: filter unsubscribed recipients at build and send time"
```

---

### Task 9 : L'interface d'administration

Sans écran, toute demande de réinscription impose une édition de fichier sur le serveur.

**Files:**
- Create: `src/main/webapp/modules/mailing/jsp/unsubscribe.jsp`
- Modify: `src/main/java/org/javlo/module/mailing/MailingAction.java` (branche d'attribut vers la ligne 149, et nouvelle action)
- Modify: `src/main/java/org/javlo/module/mailing/MailingModuleContext.java:75-77`
- Modify: `src/main/webapp/modules/mailing/i18n/edit_fr.properties`, `edit_en.properties`

**Interfaces:**
- Consumes: `UnsubscribeService.getAll()`, `UnsubscribeService.resubscribe(String)`
- Produces: l'action `mailing.resubscribe` avec le paramètre `email`, et l'attribut de requête `unsubscribeList`

- [ ] **Step 1 : Déclarer l'écran dans la navigation du module**

Dans `MailingModuleContext.java`, après la ligne 76 (`outContext.navigation.add(... "queue" ...)`) :

```java
			outContext.navigation.add(new LinkToRenderer(I18nAccess.getInstance(request).getText("mailing.title.unsubscribe"), "unsubscribe", "jsp/unsubscribe.jsp"));
```

- [ ] **Step 2 : Exposer la liste à la JSP**

Dans `MailingAction.java`, remplacer le bloc des lignes 148-161 :

```java
				MailingFactory mailingFactory = MailingFactory.getInstance(session.getServletContext());
				if (currentModule.getRenderer().contains("history")) {
```

par une branche `unsubscribe` placée avant, le reste étant inchangé :

```java
				MailingFactory mailingFactory = MailingFactory.getInstance(session.getServletContext());
				if (currentModule.getRenderer().contains("unsubscribe")) {
					request.setAttribute("unsubscribeList", UnsubscribeService.getInstance(globalContext).getAll());
				} else if (currentModule.getRenderer().contains("history")) {
```

Le reste du bloc (`if (!globalContext.isMaster()) { ... }` et son `else`) reste tel quel.

- [ ] **Step 3 : Ajouter l'action de réinscription**

Toujours dans `MailingAction.java`, ajouter à côté de `performDeletemailing` :

```java
	public static String performResubscribe(ContentContext ctx, RequestService rs) throws Exception {
		String email = rs.getParameter("email", null);
		if (StringHelper.isEmpty(email)) {
			return "need 'email' as parameter.";
		}
		UnsubscribeService.getInstance(ctx.getGlobalContext()).resubscribe(email);
		return null;
	}
```

Cette action reste dans le groupe `mailing`, donc protégée par `security.roles=mailing` déclaré dans `modules/mailing/config.properties` : seul un administrateur du module peut réinscrire quelqu'un.

- [ ] **Step 4 : Créer l'écran**

Créer `src/main/webapp/modules/mailing/jsp/unsubscribe.jsp`, sur la structure de boîte de `history.jsp` :

```jsp
<%@ taglib uri="jakarta.tags.core" prefix="c"
%><%@ taglib prefix="fn" uri="jakarta.tags.functions"
%>
<div class="box preview">
<h3><span>${i18n.edit['mailing.title.unsubscribe']}</span></h3>
	<div class="content">
		<table cellpadding="0" cellspacing="0" border="0" class="dyntable cell-border compact stripe display" id="unsubscribe-table">
			<thead>
				<tr>
					<th class="head1">${i18n.edit['mailing.title.unsubscribe.email']}</th>
					<th class="head0">${i18n.edit['mailing.title.unsubscribe.roles']}</th>
					<th class="head1">${i18n.edit['mailing.title.unsubscribe.date']}</th>
					<th width="120" class="head0">&nbsp;</th>
				</tr>
			</thead>
			<colgroup>
				<col class="con1" />
				<col class="con0" />
				<col class="con1" />
				<col class="con0" />
			</colgroup>
			<tbody>
				<c:forEach var="entry" items="${unsubscribeList}">
				<tr>
					<td class="con1">${entry.email}</td>
					<td class="con0"><c:forEach var="role" items="${entry.roles}" varStatus="s">${role}<c:if test="${!s.last}">, </c:if></c:forEach></td>
					<td class="con1">${entry.date}</td>
					<td class="con0"><a href="${info.currentURL}?webaction=mailing.resubscribe&amp;email=${entry.email}">${i18n.edit['mailing.action.resubscribe']}</a></td>
				</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
```

- [ ] **Step 5 : Ajouter les libellés i18n**

Dans `src/main/webapp/modules/mailing/i18n/edit_fr.properties` :

```properties
mailing.title.unsubscribe            = d\u00E9sabonn\u00E9s
mailing.title.unsubscribe.email      = adresse
mailing.title.unsubscribe.roles      = r\u00F4les
mailing.title.unsubscribe.date       = date
mailing.action.resubscribe           = r\u00E9inscrire
```

Dans `src/main/webapp/modules/mailing/i18n/edit_en.properties` :

```properties
mailing.title.unsubscribe            = unsubscribed
mailing.title.unsubscribe.email      = address
mailing.title.unsubscribe.roles      = roles
mailing.title.unsubscribe.date       = date
mailing.action.resubscribe           = resubscribe
```

- [ ] **Step 6 : Vérifier**

Run: `mvn -q -DskipTests compile`
Expected: succès.

Déployer, ouvrir le module mailing, cliquer l'onglet « désabonnés » : l'écran doit s'afficher, lister les adresses désabonnées, et le lien de réinscription doit les retirer de la liste et du fichier `unsubscribe.csv`.

- [ ] **Step 7 : Commit**

```bash
git add src/main/webapp/modules/mailing src/main/java/org/javlo/module/mailing/MailingAction.java src/main/java/org/javlo/module/mailing/MailingModuleContext.java
git commit -m "feat: admin screen for the unsubscribe list"
```

---

### Task 10 : Vérification de bout en bout

Les tests unitaires ne couvrent ni la pose réelle des en-têtes, ni le POST sans session. Cette tâche les vérifie sur le déploiement local.

**Files:** aucun. Vérification manuelle.

**Interfaces:**
- Consumes: toutes les tâches précédentes
- Produces: rien

Contexte de déploiement, d'après `CLAUDE.md` : le projet est déployé dans `C:\opt\tomcat10\webapps\javlo2`, accessible sur `http://localhost/javlo2/sexy/preview/fr/`, identifiants `admin` / `admin`.

- [ ] **Step 1 : Déployer**

Construire et copier les classes et ressources modifiées dans `C:\opt\tomcat10\webapps\javlo2`, puis redémarrer Tomcat.

- [ ] **Step 2 : Vérifier l'en-tête sur un mailing réel**

Envoyer un mailing de test depuis le module mailing vers une adresse contrôlée. Ouvrir la source du message reçu et vérifier :

- `List-Unsubscribe: <https://…?webaction=unsecure.unsubscribe&lut=…>` est présent
- `List-Unsubscribe-Post: List-Unsubscribe=One-Click` est présent si le site est en HTTPS, absent sinon
- le paramètre `lut` ne contient que des caractères `[A-Za-z0-9_-]`
- l'adresse email n'apparaît nulle part en clair dans l'en-tête

Si le site local est en HTTP, l'absence de `List-Unsubscribe-Post` est le comportement attendu, pas un défaut.

- [ ] **Step 3 : Vérifier le POST one-click sans session**

Extraire l'URL de l'en-tête et la rejouer sans cookie :

```bash
curl -i -X POST -d 'List-Unsubscribe=One-Click' '<URL extraite du header>'
```

Expected: `200`. Vérifier ensuite que l'adresse figure dans `<dossier de données du site>/_private/unsubscribe.csv`, avec les rôles du mailing, ou `*` si le mailing n'en ciblait aucun.

- [ ] **Step 4 : Vérifier l'idempotence**

Rejouer exactement la même commande.
Expected: `200`, et `unsubscribe.csv` contient toujours une seule ligne pour cette adresse.

- [ ] **Step 5 : Vérifier le rejet d'un token invalide**

```bash
curl -i -X POST -d 'List-Unsubscribe=One-Click' '<URL avec le dernier caractère du lut modifié>'
```

Expected: `200`, aucune nouvelle ligne dans `unsubscribe.csv`, et un avertissement `invalid unsubscribe token` dans les journaux Tomcat.

- [ ] **Step 6 : Vérifier le filtrage**

Préparer un nouveau mailing vers le même groupe. Le compte de destinataires affiché doit être diminué de l'adresse désabonnée, et celle-ci ne doit pas recevoir le message.

- [ ] **Step 7 : Vérifier le lien dans le corps du mail**

Sur un mailing contenant un composant `unsubscribe-link`, cliquer le lien depuis le message reçu. Vérifier que l'adresse est ajoutée à `unsubscribe.csv` — c'est le chemin `_mfb`, distinct du chemin `lut`.

- [ ] **Step 8 : Vérifier la non-régression du champ manuel**

Renseigner le champ *Unsubscribe* dans les propriétés du site, envoyer un mailing, et vérifier que l'en-tête reprend exactement la valeur saisie, sans `List-Unsubscribe-Post`, avec `${email}` remplacé par l'adresse du destinataire.

- [ ] **Step 9 : Vérifier l'absence d'en-tête sur un mail transactionnel**

Soumettre un formulaire de contact du site. Le mail reçu ne doit porter ni `List-Unsubscribe` ni `List-Unsubscribe-Post`, sauf si le champ manuel du site est renseigné — c'est le comportement historique de `MailService:618`, inchangé.

- [ ] **Step 10 : Vérifier la persistance du secret**

Redémarrer Tomcat, envoyer un nouveau mailing, et vérifier qu'un token émis **avant** le redémarrage fonctionne toujours. C'est la garantie que `mailing.unsubscribe-secret` est bien persisté dans le fichier de propriétés du site et non régénéré à chaque démarrage.
