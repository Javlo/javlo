package org.javlo.service.proxy;

import jakarta.servlet.http.HttpServletResponse;
import org.javlo.actions.IAction;
import org.javlo.context.ContentContext;
import org.javlo.helper.StringHelper;
import org.javlo.helper.URLHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class RemoteCacheService implements IAction {

    protected static Logger logger = Logger.getLogger(RemoteCacheService.class.getName());

    public File getCacheFolder(ContentContext ctx) {
        return new File(URLHelper.mergePath(ctx.getGlobalContext().getDataFolder(), "_remote_cache"));
    }

    /**
     * Convert any key to a filesystem-safe filename using SHA-256 hex.
     * Example: "user:ABC/123?x=y" -> "a1b2...ff.data"
     */
    private static String toSafeFileName(String key) {
        if (key == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                // convert to lowercase hex
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit((b) & 0xF, 16));
            }
            return hex.toString() + ".data";
        } catch (NoSuchAlgorithmException e) {
            // Fallback: use Java's hashCode if SHA-256 is unavailable (extremely unlikely)
            return Integer.toHexString(key.hashCode()) + ".data";
        }
    }

    /**
     * Perform GET: retrieve cached data by hashed filename.
     */
    public static final String performGet(ContentContext ctx) {
        String key = ctx.getRequest().getParameter("key");
        if (StringHelper.isEmpty(key)) {
            return "Missing key parameter.";
        }

        RemoteCacheService service = new RemoteCacheService();
        File cacheFolder = service.getCacheFolder(ctx);
        String safeName = toSafeFileName(key);
        File cacheFile = new File(cacheFolder, safeName);

        if (!cacheFile.exists()) {
            logger.info("key not found : "+key);
            return "Cache not found.";
        }

        // Read file content (UTF-8)
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(cacheFile), StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
            // Trim trailing newline if any
            int len = content.length();

            HttpServletResponse response = ctx.getResponse();

            String data = (len > 0 && content.charAt(len - 1) == '\n')
                    ? content.substring(0, len - 1)
                    : content.toString();

            // write data in response
            response.setContentType("text/plain; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");

            try (PrintWriter out = response.getWriter()) {
                out.write(data);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        } catch (IOException e) {
            logger.warning("Error reading cache file '" + cacheFile.getAbsolutePath() + "': " + e.getMessage());
            return "Error reading cache.";
        }
    }

    /**
     * Perform PUT: store data by hashed filename.
     */
    public static final String performPut(ContentContext ctx) {
        String key = ctx.getRequest().getParameter("key");
        String data = ctx.getRequest().getParameter("data");

        if (StringHelper.isEmpty(key)) {
            logger.warning("Missing key parameter.");
            return "Missing key parameter.";
        }

        RemoteCacheService service = new RemoteCacheService();
        File cacheFolder = service.getCacheFolder(ctx);
        cacheFolder.mkdirs();

        // Ensure folder exists
        if (!cacheFolder.exists() && !cacheFolder.mkdirs()) {
            logger.warning("unable to create cache folder: " + cacheFolder.getAbsolutePath());
            return "Unable to create cache folder.";
        }

        String safeName = toSafeFileName(key);
        File cacheFile = new File(cacheFolder, safeName);

        // Write data (UTF-8)
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(cacheFile), StandardCharsets.UTF_8))) {
            writer.write(data != null ? data : "");
            logger.info("put data in cache : "+key);
            return "Cache stored successfully.";
        } catch (IOException e) {
            logger.warning("Error writing cache file '" + cacheFile.getAbsolutePath() + "': " + e.getMessage());
            return "Error writing cache.";
        }
    }

    @Override
    public String getActionGroupName() {
        return "cache";
    }

    @Override
    public boolean haveRight(ContentContext ctx, String action) {
        return true;
    }
}

