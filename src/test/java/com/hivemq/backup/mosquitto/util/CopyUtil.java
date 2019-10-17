package com.hivemq.backup.mosquitto.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Michael Walter
 */
public class CopyUtil {

    /**
     * Unpack the zip file to the given destination.
     *
     * @param zipFilePath The path of the zip file to unpack.
     * @param outputDir   The destination where to unpack the content of the zip file.
     * @return Return a list of files that were unpacked (directories are excluded).
     * @throws IOException Thrown if there is a problem with extracting the zip file.
     */
    public static List<String> unZip(@NotNull final String zipFilePath, @NotNull final String outputDir) throws IOException {

        final File zipFile = new File(zipFilePath);
        final List<String> fileNames = new ArrayList<>();
        final File destDir = new File(outputDir);

        destDir.mkdirs();

        ZipEntry entry;

        try (final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {

            while ((entry = zis.getNextEntry()) != null) {

                final File newFile = newFile(destDir, entry);

                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    try (final FileOutputStream fos = new FileOutputStream(newFile)) {
                        final byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fileNames.add(outputDir + File.separator + entry.getName());
                    }
                }
            }
        }
        return fileNames;
    }

    private static File newFile(@NotNull final File destinationDir, @NotNull final ZipEntry zipEntry) throws IOException {
        final File destFile = new File(destinationDir, zipEntry.getName());

        final String destDirPath = destinationDir.getCanonicalPath();
        final String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    /**
     * Get the first String in the list that contains the <b>matchString</b>.
     *
     * @param list        The list to search through.
     * @param matchString The string to look for.
     * @return The String that contains the matchString or <b>null</b> if it didn't match any String in the list.
     */
    @Nullable
    public static String fetchFirstMatchFromList(@NotNull final List<String> list, final @NotNull String matchString) {

        for (String entry : list) {
            if (entry.contains(matchString)) {
                return entry;
            }
        }

        return null;
    }
}
