package com.hivemq.backup.mosquitto.util;

import com.hivemq.backup.mosquitto.XmlFromMosquittoDB;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
