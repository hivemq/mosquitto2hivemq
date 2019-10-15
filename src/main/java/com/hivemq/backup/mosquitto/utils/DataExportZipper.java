package com.hivemq.backup.mosquitto.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.apache.commons.compress.utils.IOUtils.copy;

/**
 * @author Florian Limpöck
 * @since 4.2.0
 */
public class DataExportZipper {

    public static final String EXPORT_DATE_FORMAT = "yyyyMMdd-HHmmss";
    private static final int COMPRESSION = Deflater.BEST_SPEED;
    private final String clusterId;
    private final Path backupFolderLocation;
    private final String hiveMqVersion;
    private final int zipBufferSize;

    public DataExportZipper(final @NotNull String clusterId,
                            final @NotNull Path backupFolderLocation,
                            final @NotNull String hiveMqVersion,
                            final int zipBufferSize) {
        this.clusterId = clusterId;
        this.backupFolderLocation = backupFolderLocation;
        this.hiveMqVersion = hiveMqVersion;
        this.zipBufferSize = zipBufferSize;
    }

    public void zipExport(final long timestamp) throws IOException {

        final @NotNull byte[] buffer = new byte[zipBufferSize];

        final @NotNull String fileTimeStamp = convertTimeStamp(timestamp).format(DateTimeFormatter.ofPattern(EXPORT_DATE_FORMAT));
        final @NotNull File backupFolder = new File(backupFolderLocation.toFile(), fileTimeStamp);

        final @Nullable File[] filesAndDirectories = backupFolder.listFiles();
        if (filesAndDirectories == null || filesAndDirectories.length == 0) {
            return;
        }

        try (final @NotNull FileOutputStream fileStream = new FileOutputStream(new @NotNull File(backupFolder, getNodeBackupFileName(clusterId)))) {
            try (final @NotNull ZipOutputStream zos = new ZipOutputStream(fileStream)) {
                zos.setLevel(COMPRESSION);
                for (final @Nullable File file : filesAndDirectories) {
                    zipFile(buffer, zos, file, "");
                }
            }
        }
    }

    private String getNodeBackupFileName(final @NotNull String nodeName) {
        return "node." + nodeName + ".backup";
    }

    private void zipFile(final @NotNull byte[] buffer, final @NotNull ZipOutputStream zos, final @Nullable File fileOrDirectory, final @NotNull String parent) throws IOException {
        if (Objects.requireNonNull(fileOrDirectory).isDirectory()) {
            final @NotNull ZipEntry ze = new ZipEntry(fileOrDirectory.getName() + "/");
            zos.putNextEntry(ze);
            final @Nullable File[] files = fileOrDirectory.listFiles();
            if (files == null || files.length == 0) {
                zos.closeEntry();
                return;
            }
            //noinspection NullableProblems
            for (final @NotNull File file : files) {
                zipFile(buffer, zos, file, ze.getName());
            }
            zos.closeEntry();

        } else {
            if (fileOrDirectory.getName().endsWith(".xml")) {
                final ZipEntry ze = new ZipEntry(parent + fileOrDirectory.getName());
                zos.putNextEntry(ze);
                try (final @NotNull FileInputStream in = new FileInputStream(fileOrDirectory)) {
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                }
                zos.closeEntry();
            }
        }

    }

    public void mergeZipFiles(final long exportTimeStamp) throws IOException {

        final @NotNull String fileTimeStamp = convertTimeStamp(exportTimeStamp).format(DateTimeFormatter.ofPattern(EXPORT_DATE_FORMAT));
        final @NotNull File backupFolder = new File(backupFolderLocation.toFile(), fileTimeStamp + File.separator);
        final @Nullable File[] inputZipFiles = backupFolder.listFiles((dir, name) -> name.startsWith("node.") && name.endsWith(".backup"));

        if (inputZipFiles == null || inputZipFiles.length == 0) {
            return;
        }

        final @NotNull String outputZipPath = getOutputZipPath(fileTimeStamp, backupFolder);

        final @NotNull List<ZipEntryIndexed> indexedList = new ArrayList<>();
        final @NotNull List<ZipFile> originList = new ArrayList<>(inputZipFiles.length);

        boolean retainedMessagesFolderCreated = false;
        boolean sharedSubscriptinsFolderCreated = false;
        boolean clientSessionsFolderCreated = false;

        try (final @NotNull FileOutputStream out = new FileOutputStream(outputZipPath)) {
            try (final @NotNull ZipOutputStream resultZip = new ZipOutputStream(out)) {
                //Important set level to the same as the one of the origin.
                resultZip.setLevel(COMPRESSION);

                //add all zip entries from all files into one array-list
                for (int i = 0; i < inputZipFiles.length; i++) {
                    //noinspection ConstantConditions
                    final @NotNull ZipFile origin = new ZipFile(inputZipFiles[i]);
                    originList.add(origin);
                    final @NotNull Enumeration<? extends ZipEntry> entries = origin.entries();
                    while (entries.hasMoreElements()) {
                        final @NotNull ZipEntry entry = new ZipEntry(entries.nextElement());
                        indexedList.add(new ZipEntryIndexed(entry, i));
                    }
                }

                //copy all zip entries into the result zip file
                for (final @NotNull ZipEntryIndexed indexed : indexedList) {
                    final @NotNull ZipEntry zipEntry = indexed.zipEntry;
                    final int index = indexed.index;
                    if (zipEntry.getName().equals("retained-messages/") && !retainedMessagesFolderCreated) {
                        resultZip.putNextEntry(zipEntry);
                        resultZip.closeEntry();
                        retainedMessagesFolderCreated = true;
                        continue;
                    }
                    if (zipEntry.getName().equals("shared-subscriptions/") && !sharedSubscriptinsFolderCreated) {
                        resultZip.putNextEntry(zipEntry);
                        resultZip.closeEntry();
                        sharedSubscriptinsFolderCreated = true;
                        continue;
                    }
                    if (zipEntry.getName().equals("client-sessions/") && !clientSessionsFolderCreated) {
                        resultZip.putNextEntry(zipEntry);
                        resultZip.closeEntry();
                        clientSessionsFolderCreated = true;
                        continue;
                    }
                    if (!zipEntry.isDirectory()) {
                        resultZip.putNextEntry(zipEntry);
                        final @NotNull InputStream entryInputStream = originList.get(index).getInputStream(zipEntry);
                        copy(entryInputStream, resultZip);
                        resultZip.closeEntry();
                        entryInputStream.close();
                    }
                }

                //close all input streams
                for (final @NotNull ZipFile zipFile : originList) {
                    zipFile.close();
                }
            }
        }
    }

    private String getOutputZipPath(final @NotNull String fileTimeStamp, final @NotNull File backupFolder) {
        return backupFolder.getAbsolutePath() + File.separator + fileTimeStamp + ".hivemq-" + hiveMqVersion + ".backup";
    }

    private class ZipEntryIndexed {
        private final @NotNull ZipEntry zipEntry;
        private final int index;

        ZipEntryIndexed(final @NotNull ZipEntry entry, final int index) {
            this.zipEntry = entry;
            this.index = index;
        }
    }

    public static @NotNull LocalDateTime convertTimeStamp(final long timeStamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }

}
