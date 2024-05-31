package io.github.spigotcvn.remapper.util;

import java.io.*;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarUtil {
    public static void inspectJar(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(entry -> System.out.println(entry.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void repackJar(File resultJarFile, File unarchiveDir) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(resultJarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {

            // Iterate over all files in the unarchive directory and add them to the JAR
            Util.iterateOverFiles(file -> {
                try {
                    String entryName = unarchiveDir.toPath()
                        .relativize(file.toPath())
                        .toString()
                        .replaceAll("\\" + File.seperator, "/");
                    JarEntry entry = new JarEntry(entryName);
                    jos.putNextEntry(entry);
                    Files.copy(file.toPath(), jos);
                    jos.closeEntry();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, unarchiveDir);
        }

        Util.iterateOverFiles(File::delete, unarchiveDir);
        unarchiveDir.delete();
    }

    public static void copyJarContents(File jarFile, File unarchiveDir) {
        if (!unarchiveDir.exists()) {
            unarchiveDir.mkdirs();
        }

        try (JarFile jar = new JarFile(jarFile)) {
            jar.stream().forEach(entry -> {
                try {
                    File file = new File(unarchiveDir, entry.getName());
                    if (entry.isDirectory()) {
                        file.mkdirs();
                    } else {
                        // Ensure parent directories exist
                        File parentDir = file.getParentFile();
                        if (parentDir != null && !parentDir.exists()) {
                            parentDir.mkdirs();
                        }
                        // Copy file contents
                        try (InputStream is = jar.getInputStream(entry);
                             OutputStream os = new FileOutputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
