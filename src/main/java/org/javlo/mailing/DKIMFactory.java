package org.javlo.mailing;

import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import org.apache.xmlbeans.impl.util.Base64;
import org.javlo.context.GlobalContext;
import org.javlo.helper.ResourceHelper;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

public class DKIMFactory {
	
	private static String DKIM_FOLDER = URLHelper.mergePath(ResourceHelper.PRIVATE_DIR, "dkim");

	private DKIMFactory() {
	}
	
	private static File getPublicKeyFile(GlobalContext globalContext) {
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), DKIM_FOLDER, "publickey.txt"));
	}
	
	private static File getPrivateKeyFile(GlobalContext globalContext) {
		return new File(URLHelper.mergePath(globalContext.getDataFolder(), DKIM_FOLDER, "privatekey.bin"));
	}
	
	private static void createKeys(GlobalContext globalContext) {
		KeyPairGenerator keyPairGenerator;
		try {
			keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			keyPairGenerator.initialize(1024);
			KeyPair keyPair = keyPairGenerator.genKeyPair();
			File privateFile = getPrivateKeyFile(globalContext);
			privateFile.getParentFile().mkdirs();			
			ResourceHelper.writeBytesToFile(getPrivateKeyFile(globalContext), keyPair.getPrivate().getEncoded());
			ResourceHelper.writeBytesToFile(getPublicKeyFile(globalContext), Base64.encode(keyPair.getPublic().getEncoded()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void resetKeys(GlobalContext globalContext) {		
		try {
			getPrivateKeyFile(globalContext).delete();
			getPublicKeyFile(globalContext).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static File getDKIMPrivateKeyFile(GlobalContext globalContext) {
		if (globalContext == null) {
			return null;
		} else {
			if (isDkimDefined(globalContext)) {
				File privateKeyFile = getPrivateKeyFile(globalContext);
				if (!privateKeyFile.exists()) {
					createKeys(globalContext);
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
				File publicKeyFile = getPublicKeyFile(globalContext);
				if (!publicKeyFile.exists()) {
					createKeys(globalContext);
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
