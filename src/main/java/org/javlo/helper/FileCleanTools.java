package org.javlo.helper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileCleanTools {

    /**
     * Determines whether a character is legal in XML 1.0 documents.
     */
    private static boolean isXml10Legal(char ch) {
        // Allow TAB, LF, CR
        if (ch == 0x9 || ch == 0xA || ch == 0xD) return true;
        // Valid Unicode ranges for XML 1.0
        return (ch >= 0x20 && ch <= 0xD7FF)
                || (ch >= 0xE000 && ch <= 0xFFFD)
                || (ch >= 0x10000 && ch <= 0x10FFFF);
    }

    public static void main(String[] args) {
        // Example file path — replace with your actual JSP file
        String file = "C:\\work\\andromede\\yamabiko\\echorobotics\\components\\partners.html";

        try {
            cleanFile(file);
            System.out.println("File cleaned successfully: " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clean illegal XML 1.0 characters from the specified file.
     */
    public static void cleanFile(String filePath) throws IOException {
        File jspFile = new File(filePath);
        if (!jspFile.exists() || !jspFile.isFile()) {
            throw new IllegalArgumentException("The provided file does not exist or is not valid: " + filePath);
        }

        // Read content as UTF-8
        String content = new String(Files.readAllBytes(jspFile.toPath()), StandardCharsets.UTF_8);

        // Remove illegal XML control characters (except TAB, LF, CR)
        StringBuilder cleaned = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (isXml10Legal(ch)) {
                cleaned.append(ch);
            }
        }

        // Overwrite the file with cleaned content
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(jspFile), StandardCharsets.UTF_8))) {
            writer.write(cleaned.toString());
        }
    }

}

