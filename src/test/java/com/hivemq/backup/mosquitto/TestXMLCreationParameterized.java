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

import com.hivemq.backup.mosquitto.extension.TemporaryFolderExtension;
import com.hivemq.backup.mosquitto.util.CopyUtil;
import com.hivemq.backup.mosquitto.util.XmlComparatorUtil;
import com.hivemq.backup.mosquitto.util.XmlExtractorUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
@ExtendWith(TemporaryFolderExtension.class)
class TestXMLCreationParameterized {

    private Path currentTemporaryFolder;

    @RegisterExtension
    TemporaryFolderExtension temporaryFolder = new TemporaryFolderExtension(this.getClass().getResource(".").getPath());

    @BeforeEach
    void setup() throws IOException {
        currentTemporaryFolder = temporaryFolder.newFolder().toPath();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/mqtt311compare.csv", numLinesToSkip = 1)
    void testMqtt311XmlCreation(String inputMosquittoDatabaseFile, String expectedEqualBackupFile) throws IOException {
        final String mqtt311Path = "src/test/resources/MQTT311/";
        final Path inputMosquitto = Path.of(mqtt311Path + inputMosquittoDatabaseFile);
        List<String> mosquittoXmlFiles = XmlExtractorUtil.getMosquittoXmlFile(inputMosquitto, currentTemporaryFolder);

        final Path expectedBackup = Path.of(mqtt311Path + expectedEqualBackupFile);
        List<String> hiveMqXmlFiles = XmlExtractorUtil.getHiveMqXmlFile(expectedBackup, currentTemporaryFolder);

        mosquittoXmlFiles.forEach(System.out::println);
        hiveMqXmlFiles.forEach(System.out::println);

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "retained") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "retained"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "retained")))));
        }

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Publisher") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "Publisher"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Publisher")))));
        }

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Subscriber") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "Subscriber"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Subscriber")))));
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/mqtt5compare.csv", numLinesToSkip = 1)
    void testMqtt5XmlCreation(String inputMosquittoDatabaseFile, String expectedEqualBackupFile) throws IOException {
        final String mqtt5Path = "src/test/resources/MQTT5/";
        final Path inputMosquitto = Path.of(mqtt5Path + inputMosquittoDatabaseFile);
        List<String> mosquittoXmlFiles = XmlExtractorUtil.getMosquittoXmlFile(inputMosquitto, currentTemporaryFolder);

        final Path expectedBackup = Path.of(mqtt5Path + expectedEqualBackupFile);
        List<String> hiveMqXmlFiles = XmlExtractorUtil.getHiveMqXmlFile(expectedBackup, currentTemporaryFolder);

        mosquittoXmlFiles.forEach(System.out::println);
        hiveMqXmlFiles.forEach(System.out::println);

        System.out.println(inputMosquittoDatabaseFile);

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "retained") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "retained"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "retained")))));
        }

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Publisher") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "Publisher"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Publisher")))));
        }

        if (CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Subscriber") != null) {
            assertTrue(XmlComparatorUtil.checkXmlEquality(
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(mosquittoXmlFiles, "Subscriber"))),
                    new File(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(hiveMqXmlFiles, "Subscriber")))));
        }
    }
}
