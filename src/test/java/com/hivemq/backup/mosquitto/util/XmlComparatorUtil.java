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

import org.jetbrains.annotations.NotNull;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.File;
import java.util.Set;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
public class XmlComparatorUtil {

    public static boolean checkXmlEquality(final @NotNull File mosquittoXml, final @NotNull File hivemqXml) {
        final @NotNull Set<String> attributes = Set.of("disconnected-since", "hivemq-version", "exported-at");
        final @NotNull Set<String> nodes = Set.of("timestamp", "exported-at");

        final @NotNull Diff differences = DiffBuilder.compare(mosquittoXml)
                .withTest(hivemqXml)
                .ignoreComments()
                .ignoreWhitespace()
                .withAttributeFilter(attr -> !attributes.contains(attr.getName()))
                .withNodeFilter(node -> !nodes.contains(node.getParentNode().getNodeName()))
                .build();

        for (final @NotNull Difference difference : differences.getDifferences()) {
            final @NotNull Comparison.Detail controlDetails = difference.getComparison().getControlDetails();
            switch (controlDetails.getTarget().getParentNode().getNodeName()) {
                case "publish-id":
                    final int mosquittoPublishId = Integer.parseInt((String) controlDetails.getValue());
                    if (0 > mosquittoPublishId) {
                        System.out.println("Publish");
                        return false;
                    }
                    break;

                case "cluster-id":
                    final @NotNull String mosquittoFakeClusterId = (String)controlDetails.getValue();
                    if (!mosquittoFakeClusterId.equals("MOSQU")) {
                        System.out.println("Cluster");
                        return false;
                    }
                    break;

                case "message-expiry-interval":
                    final long mosquittoMessageExpiryInterval = Long.parseLong((String) controlDetails.getValue());
                    final long hiveMqMessageExpiryInterval = Long.parseLong((String)difference.getComparison().getTestDetails().getValue());

                    //The Mosquitto message expiry interval is calculated with the messages expiry time and the export timestamp,
                    //therefore we need an epsilon (10)
                    if (hiveMqMessageExpiryInterval < mosquittoMessageExpiryInterval) {
                        System.out.println(difference);
                        return false;
                    }
                    break;

                case "retain-handling":
                    final short retainHandling = Short.parseShort((String) controlDetails.getValue());
                    System.out.println("RETAIN HANDLING IS CURRENTLY BUGGED IN MOSQUITTO PERSISTENCE. ALWAYS 0. IGNORING.");
                    if (!(retainHandling == 0 || retainHandling == 1 || retainHandling == 2 || retainHandling == 3)) {
                        return false;
                    }
                    break;

                default:
                    System.out.println(difference);
                    return false;
            }
        }
        return true;
    }

}
