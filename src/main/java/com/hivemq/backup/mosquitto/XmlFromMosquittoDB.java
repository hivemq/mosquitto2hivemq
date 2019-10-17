/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.backup.mosquitto;

import com.hivemq.backup.mosquitto.db.Chunk;
import com.hivemq.backup.mosquitto.utils.DataExportZipper;
import com.hivemq.backup.mosquitto.xml.ClientSessionExporter;
import com.hivemq.backup.mosquitto.xml.RetainedMessagesExporter;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Style;
import picocli.CommandLine.Help.ColorScheme;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.Callable;

import static com.hivemq.backup.mosquitto.utils.DataExportZipper.EXPORT_DATE_FORMAT;
import static com.hivemq.backup.mosquitto.utils.DataExportZipper.convertTimeStamp;
import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * @author Lukas Brand
 * @since 1.0
 */
@SuppressWarnings("unused")
@Command(name = "HiveMQ Migration Tool", description = "Create a HiveMQ backup out of your persistent Mosquitto data.", version = "1.0", mixinStandardHelpOptions = true)
public class XmlFromMosquittoDB implements Callable<Integer> {

    @Option(names = {"-i", "--input"}, required = true, description = "Input file (mosquitto.db)")
    private Path inputFile;

    @Option(names = {"-o", "--output"}, description = "Output directory of migrated backup. If not set defaults to current directory (${DEFAULT-VALUE})")
    private Path outputFolder = new File(System.getProperty("user.dir")).toPath();

    @Option(names = {"-dc", "--display-chunks"}, description = "Display the created chunks to get more information about the migration data.")
    private boolean displayChunks;

    @Option(names = {"-k", "--keep-xml"}, description = "Keep the created folders with all the xml files.")
    private boolean keepFiles;

    @Option(names = {"-f", "--force"}, description = "Force migration if problems occur.")
    private boolean forceCreationWithFailures;


    private final static @NotNull ColorScheme colorScheme = new ColorScheme.Builder()
            .commands(Style.bold, Style.underline)    // combine multiple styles
            .options(Style.fg_yellow)                // yellow foreground color
            .parameters(Style.fg_yellow)
            .optionParams(Style.italic).build();


    private static final int DATA_EXPORT_XML_MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int DATA_EXPORT_ZIP_BUFFER_SIZE = 1024 * 1024;
    private static final @NotNull String hiveMqVersion = "4.2.1";
    private static final @NotNull String clusterId = "MOSQU";

    /**
     * Main method. Executes a new Command Line from pico-cli.
     *
     * @param args Input arguments.
     */
    public static void main(final String... args) {
        new CommandLine(new XmlFromMosquittoDB()).setColorScheme(colorScheme).execute(args);
    }

    /**
     * Gets called by the pico-cli as new Command Line. Starts the migration.
     *
     * @return Indicates success or failure.
     */
    @Override
    public Integer call() {

        Logger.info("Starting backup creation.");
        if (forceCreationWithFailures) {
            Logger.warn("Force migration enabled.");
        }

        final @NotNull Chunk chunk = new Chunk(forceCreationWithFailures);
        final long exportTime = System.currentTimeMillis();
        final @NotNull Path timestampedFolder = new File(outputFolder.toFile(), convertTimeStamp(exportTime).format(DateTimeFormatter.ofPattern(EXPORT_DATE_FORMAT))).toPath();

        try {
            byte[] dbBytes = chunk.readMosquittoDbFile(inputFile.toAbsolutePath());
            chunk.createChunksFromBinary(dbBytes, displayChunks);

        } catch (IOException e) {
            Logger.error("File not Found: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            Logger.error(e.getMessage());
            System.exit(1);
        }

        new RetainedMessagesExporter(exportTime, timestampedFolder, clusterId, hiveMqVersion, DATA_EXPORT_XML_MAX_FILE_SIZE)
                .writeToXml(chunk.getRetainedFromMsgStore());

        new ClientSessionExporter(exportTime, timestampedFolder, clusterId, hiveMqVersion)
                .writeToXml(chunk.getChunkClients(),
                        chunk.getChunkSubscriptions(),
                        chunk.getChunkClientMessages(),
                        chunk.getChunkMsgStores());

        try {
            createBackupFile(outputFolder, exportTime);

        } catch (IOException e) {
            Logger.error("Could not create File: " + e.getMessage());
            System.exit(1);
        }

        Logger.info("Backup created in: " + timestampedFolder);

        if (!keepFiles) {
            try {
                deleteXmlFilesOnExit(timestampedFolder);
            } catch (IOException e) {
                Logger.error("XML Files could not be deleted." + e.getMessage());
            }
        }
        deleteNodeFilesOnExit(timestampedFolder);

        return 0;
    }

    private static void deleteXmlFilesOnExit(final @NotNull Path timestampedFolder) throws IOException {
        final @NotNull File retainedMessagesFolder = Path.of(timestampedFolder + File.separator + "retained-messages").toFile();
        if (retainedMessagesFolder.isDirectory() && retainedMessagesFolder.exists()) {
            FileUtils.deleteDirectory(retainedMessagesFolder);
        }

        final @NotNull File clientSessionsFolder = Path.of(timestampedFolder + File.separator + "client-sessions").toFile();
        if (clientSessionsFolder.isDirectory() && clientSessionsFolder.exists()) {
            FileUtils.deleteDirectory(clientSessionsFolder);
        }

    }

    private static void deleteNodeFilesOnExit(final @NotNull Path timestampedFolder) {
        //Delete .node files on Exit
        final @Nullable File[] nodes = timestampedFolder.toFile().listFiles((dir, name) -> name.startsWith("node.") && name.endsWith(".backup"));
        if (nodes != null) {
            for (File node : nodes) {
                Objects.requireNonNull(node).deleteOnExit();
            }
        }
    }

    private static void createBackupFile(final @NotNull Path backupFolder, final long timestamp) throws IOException {
        final @NotNull DataExportZipper zipper = new DataExportZipper(clusterId, backupFolder, hiveMqVersion, DATA_EXPORT_ZIP_BUFFER_SIZE);
        zipper.zipExport(timestamp);
        zipper.mergeZipFiles(timestamp);
    }

}