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
package com.hivemq.backup.mosquitto.util;

import com.hivemq.backup.mosquitto.XmlFromMosquittoDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
public class XmlExtractorUtil {

    public static List<String> getHiveMqXmlFile(Path backupFile, Path outputFolder) throws IOException {
        //Get all Files of .backup File from HiveMQ
        String hiveMqFolder = outputFolder.toString() + "/hivemq";
        return CopyUtil.unZip(backupFile.toString(), hiveMqFolder);
    }

    public static List<String> getMosquittoXmlFile(Path mosquittoFile, Path outputFolder) throws IOException {
        //Get all Files of .backup File from Mosquitto
        String mosquittoFolder = outputFolder + "/mosquitto";
        XmlFromMosquittoDB.main("-i", mosquittoFile.toString(), "-o", mosquittoFolder + "/backup");
        Stream<Path> walk = Files.walk(Paths.get(mosquittoFolder + "/backup"));
        List<String> result = walk.filter(Files::isRegularFile).map(Path::toString)
                .collect(Collectors.toList());
        return CopyUtil.unZip(Objects.requireNonNull(CopyUtil.fetchFirstMatchFromList(result, "backup")), mosquittoFolder);
    }

}
