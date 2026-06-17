package org.javlo.mailing;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import org.javlo.context.GlobalContext;
import org.javlo.external.agitos.dkim.SigningAlgorithm;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class DKIMFactory {

	private static String DKIM_FOLDER = URLHelper.mergePath(ResourceHelper.PRIVATE_DIR, "dkim");

	/** Algorithme de signature par défaut pour les nouvelles clés. */
	private static final SigningAlgorithm DEFAULT_ALGORITHM = SigningAlgorithm.SHA256withRSA;

	private DKIMFactory() {
	}

	private static boolean isSha256(SigningAlgorithm algorithm) {
		return algorithm == SigningAlgorithm.SHA256withRSA;
	}

	private static File getPublicKeyFile(GlobalContext globalContext, SigningAlgorithm algorithm) {
		String fileName = isSha256(algorithm) ? "publickey_256.txt" : "publickey.txt";
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), DKIM_FOLDER, fileName));
	}

	private static File getPrivateKeyFile(GlobalContext globalContext, SigningAlgorithm algorithm) {
		String fileName = isSha256(algorithm) ? "privatekey_256.bin" : "privatekey.bin";
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), DKIM_FOLDER, fileName));
	}

	/**
	 * Détecte l'algorithme actif en se basant sur les fichiers de clé présents.
	 * SHA-256 (privatekey_256.bin) est prioritaire ; à défaut on retombe sur la
	 * clé SHA-1 historique (privatekey.bin) ; si aucune clé n'existe encore on
	 * utilise l'algorithme par défaut (SHA-256).
	 */
	private static SigningAlgorithm getActiveAlgorithm(GlobalContext globalContext) {
		if (getPrivateKeyFile(globalContext, SigningAlgorithm.SHA256withRSA).exists()) {
			return SigningAlgorithm.SHA256withRSA;
		} else if (getPrivateKeyFile(globalContext, SigningAlgorithm.SHA1withRSA).exists()) {
			return SigningAlgorithm.SHA1withRSA;
		} else {
			return DEFAULT_ALGORITHM;
		}
	}

	/**
	 * Retourne la valeur du tag DKIM "h=" (hash) correspondant à l'algorithme
	 * actif : "sha256" ou "sha1". Utilisé pour générer l'exemple d'entrée DNS.
	 */
	public static String getDKIMHashAlgorithm(GlobalContext globalContext) {
		return isSha256(getActiveAlgorithm(globalContext)) ? "sha256" : "sha1";
	}

	/**
	 * Retourne l'algorithme de signature à utiliser à partir du chemin du fichier
	 * de clé privée. Le nom du fichier encode l'algorithme : "_256" => SHA-256,
	 * sinon SHA-1 (clé historique).
	 */
	public static SigningAlgorithm getSigningAlgorithm(String privateKeyFilePath) {
		if (privateKeyFilePath != null && privateKeyFilePath.contains("_256")) {
			return SigningAlgorithm.SHA256withRSA;
		}
		return SigningAlgorithm.SHA1withRSA;
	}

	private static void createKeys(GlobalContext globalContext, SigningAlgorithm algorithm) {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(isSha256(algorithm) ? 2048 : 1024);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			File privateFile = getPrivateKeyFile(globalContext, algorithm);
			privateFile.getParentFile().mkdirs();
			ResourceHelper.writeBytesToFile(privateFile, keyPair.getPrivate().getEncoded());
			ResourceHelper.writeStringToFile(getPublicKeyFile(globalContext, algorithm), Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void resetKeys(GlobalContext globalContext) {
		try {
			getPrivateKeyFile(globalContext, SigningAlgorithm.SHA256withRSA).delete();
			getPublicKeyFile(globalContext, SigningAlgorithm.SHA256withRSA).delete();
			getPrivateKeyFile(globalContext, SigningAlgorithm.SHA1withRSA).delete();
			getPublicKeyFile(globalContext, SigningAlgorithm.SHA1withRSA).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static File getDKIMPrivateKeyFile(GlobalContext globalContext) {
		if (globalContext == null) {
			return null;
		} else {
			if (isDkimDefined(globalContext)) {
				SigningAlgorithm algorithm = getActiveAlgorithm(globalContext);
				File privateKeyFile = getPrivateKeyFile(globalContext, algorithm);
				if (!privateKeyFile.exists()) {
					createKeys(globalContext, algorithm);
				}
				return privateKeyFile;
			} else {
				return null;
			}
		}
	}

	public static boolean isDkimDefined(GlobalContext globalContext) {
		return !StringHelper.isOneEmpty(globalContext.getDKIMDomain(), globalContext.getDKIMSelector());
	}

	public static String getDKIMPublicKey(GlobalContext globalContext) {
		if (globalContext == null) {
			return null;
		} else {
			if (!StringHelper.isOneEmpty(globalContext.getDKIMDomain(), globalContext.getDKIMSelector())) {
				SigningAlgorithm algorithm = getActiveAlgorithm(globalContext);
				File publicKeyFile = getPublicKeyFile(globalContext, algorithm);
				if (!publicKeyFile.exists()) {
					createKeys(globalContext, algorithm);
				}
				try {
					return ResourceHelper.loadStringFromFile(publicKeyFile);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			} else {
				return null;
			}
		}
	}

}
