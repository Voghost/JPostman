package net.ooml.jpostman.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * File utility class
 */
public class FileUtil {
    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * Read file content as string
     */
    public static String readFileAsString(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + path);
        }
        return Files.readString(path);
    }

    /**
     * Write string to file
     */
    public static void writeStringToFile(Path path, String content) throws IOException {
        // Ensure parent directory exists
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    /**
     * Delete file
     */
    public static boolean deleteFile(Path path) {
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete file: " + path, e);
            return false;
        }
    }

    /**
     * Copy file
     */
    public static void copyFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Move file
     */
    public static void moveFile(Path source, Path target) throws IOException {
        Files.createDirectories(target.getParent());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Check if file exists
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Create directory if not exists
     */
    public static void createDirectories(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
    }

    /**
     * Get file size
     */
    public static long getFileSize(Path path) throws IOException {
        if (!Files.exists(path)) {
            return 0;
        }
        return Files.size(path);
    }

    /**
     * Get file extension
     */
    public static String getFileExtension(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    private FileUtil() {
        // Prevent instantiation
    }
}
