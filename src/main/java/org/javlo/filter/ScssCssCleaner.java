package org.javlo.filter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;

public class ScssCssCleaner {

    private static final Logger LOGGER = Logger.getLogger(ScssCssCleaner.class.getName());

    public static void cleanOldCssFiles(String directoryPath) {
        File rootDirectory = new File(directoryPath);

        if (!rootDirectory.exists() || !rootDirectory.isDirectory()) {
            LOGGER.log(Level.SEVERE, "Invalid directory: {0}", directoryPath);
            return;
        }

        Map<File, Set<File>> scssDependencies = new HashMap<>();
        List<File> scssFiles = new ArrayList<>();
        findScssFiles(rootDirectory, scssFiles);

        for (File scssFile : scssFiles) {
            Set<File> dependencies = findImports(scssFile, rootDirectory);
            scssDependencies.put(scssFile, dependencies);
        }

        for (File scssFile : scssFiles) {
            try {
                File cssFile = findRootCssFile(scssFile, scssDependencies);
                if (cssFile != null && cssFile.exists()) {
                    if (scssFile.lastModified() > cssFile.lastModified()) {
                        LOGGER.log(Level.INFO, "Deleting outdated CSS file: {0} because : "+scssFile, cssFile.getAbsolutePath());
                        Files.delete(cssFile.toPath());
                    } else {
                        //LOGGER.log(Level.INFO, "CSS file is up-to-date: {0}", cssFile.getAbsolutePath());
                    }
                } else {
                    //LOGGER.log(Level.WARNING, "No corresponding root CSS file found for SCSS file: {0}", scssFile.getAbsolutePath());
                }
            } catch (IOException e) {
                //LOGGER.log(Level.SEVERE, "Error processing file: {0}", scssFile.getAbsolutePath());
            }
        }
    }

    private static void findScssFiles(File directory, List<File> scssFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findScssFiles(file, scssFiles);
                } else if (file.getName().endsWith(".scss")) {
                    scssFiles.add(file);
                }
            }
        }
    }

    private static Set<File> findImports(File scssFile, File rootDirectory) {
        Set<File> imports = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(scssFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("@import")) {
                    String importPath = extractImportPath(line);
                    if (importPath != null) {
                        File importedFile = findScssFileByPath(importPath, scssFile.getParentFile(), rootDirectory);
                        if (importedFile != null) {
                            imports.add(importedFile);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error reading SCSS file: {0}", scssFile.getAbsolutePath());
        }
        return imports;
    }

    private static String extractImportPath(String line) {
        // Extract path between quotes in @import
        int start = line.indexOf('"');
        int end = line.lastIndexOf('"');
        if (start != -1 && end != -1 && start < end) {
            return line.substring(start + 1, end);
        }

        start = line.indexOf('\'');
        end = line.lastIndexOf('\'');
        if (start != -1 && end != -1 && start < end) {
            return line.substring(start + 1, end);
        }

        return null;
    }

    private static File findScssFileByPath(String importPath, File currentDirectory, File rootDirectory) {
        // Handle partials (starting with '_') and default .scss extension
        String normalizedPath = importPath;

        if (!normalizedPath.endsWith(".scss")) {
            normalizedPath += ".scss";
        }

        File importedFile = new File(currentDirectory, normalizedPath);
        if (importedFile.exists()) {
            return importedFile;
        }

        // Check for partial (underscore prefix)
        String partialPath = "_" + normalizedPath;
        importedFile = new File(currentDirectory, partialPath);
        if (importedFile.exists()) {
            return importedFile;
        }

        // Try in root directory
        importedFile = new File(rootDirectory, normalizedPath);
        if (importedFile.exists()) {
            return importedFile;
        }

        importedFile = new File(rootDirectory, partialPath);
        if (importedFile.exists()) {
            return importedFile;
        }

        return null;
    }

    private static File findRootCssFile(File scssFile, Map<File, Set<File>> scssDependencies) {
        for (Map.Entry<File, Set<File>> entry : scssDependencies.entrySet()) {
            if (entry.getValue().contains(scssFile)) {
                // If the SCSS file is imported, return the CSS file of the parent
                return findCssFileForScss(entry.getKey());
            }
        }
        // If no parent found, assume the CSS file corresponds to the current SCSS file
        return findCssFileForScss(scssFile);
    }

    private static File findCssFileForScss(File scssFile) {
        String cssFileName = scssFile.getName().replaceAll("\\.scss$", ".css");
        return new File(scssFile.getParent(), cssFileName);
    }

    public static void main(String[] args) {
        String directoryPath = "C:\\trans\\scss";
        cleanOldCssFiles(directoryPath);
    }
}
