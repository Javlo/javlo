package org.javlo.test;

//Include the Dropbox SDK.
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import org.javlo.helper.URLHelper;
import org.javlo.service.syncro.DropboxService;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class DropboxTest {
	public static void main(String[] args) throws Exception {
		// Get your app key and secret from the Dropbox developers website.
		final String APP_KEY = "thv91nmpgdhn9s5";
		final String APP_SECRET = "qm788m7ehurg88d";

		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

		DbxRequestConfig config = new DbxRequestConfig("Javlo/2.0", Locale.getDefault().toString());		
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

		// Have the user sign in and authorize your app.
		/*String authorizeUrl = webAuth.start();
		System.out.println("1. Go to: " + authorizeUrl);
		System.out.println("2. Click \"Allow\" (you might have to log in first)");
		System.out.println("3. Copy the authorization code.");
		String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();

		DbxAuthFinish authFinish = webAuth.finish(code);
		String accessToken = authFinish.accessToken;*/
		String accessToken = "fkOvxPllqJ4AAAAAAAcV92fJV4Y1ZIhU7xfKtDzxayEsR1CV21BVnGOKoPG4mNqy";

		DbxClient client = new DbxClient(config, accessToken);
		System.out.println("Linked account: " + client.getAccountInfo().displayName + "(token:"+accessToken+")");
		
		File dropboxTarget = new File("C:/trans/dropbox");
		String dropboxRoot = "/data";
		DropboxService dropboxService = DropboxService.getInstance(null, accessToken);
		dropboxService.synchronize(dropboxTarget, dropboxRoot);
		
		/*DbxEntry.WithChildren listing = client.getMetadataWithChildren(dropboxRoot);
		System.out.println("Files in the root path:");
		for (DbxEntry child : listing.children) {
		    System.out.println("	" + child.name + ": " + child.toString());
		    File targetFile = new File(URLHelper.mergePath(dropboxTarget.getAbsolutePath(), child.path.replaceFirst(dropboxRoot, "")));
		    FileOutputStream outputStream = new FileOutputStream(targetFile);
		    try {
		        DbxEntry.File downloadedFile = client.getFile(child.path, null,outputStream);
		        System.out.println("Metadata: " + downloadedFile.toString());
		    } finally {
		        outputStream.close();
		    }
		}*/
	}

}
