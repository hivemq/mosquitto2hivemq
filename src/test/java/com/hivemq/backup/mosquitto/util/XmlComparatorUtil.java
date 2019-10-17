package com.hivemq.backup.mosquitto.util;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import java.io.File;
import java.util.Set;

import static java.lang.Math.abs;

public class XmlComparatorUtil {

    public static boolean checkXmlEquality(File mosquittoXml, File hivemqXml) {
        final Set<String> attributes = Set.of("disconnected-since", "hivemq-version", "exported-at");
        final Set<String> nodes = Set.of("timestamp", "exported-at");

        final Diff differences = DiffBuilder.compare(mosquittoXml)
                .withTest(hivemqXml)
                .ignoreComments()
                .ignoreWhitespace()
                .withAttributeFilter(attr -> !attributes.contains(attr.getName()))
                .withNodeFilter(node -> !nodes.contains(node.getParentNode().getNodeName()))
                .build();

        for (Difference difference : differences.getDifferences()) {
            final Comparison.Detail controlDetails = difference.getComparison().getControlDetails();
            switch (controlDetails.getTarget().getParentNode().getNodeName()) {
                case "publish-id":
                    final int mosquittoPublishId = Integer.parseInt((String) controlDetails.getValue());
                    if (0 > mosquittoPublishId) {
                        System.out.println("Publish");
                        return false;
                    }
                    break;

                case "cluster-id":
                    final String mosquittoFakeClusterId = (String)controlDetails.getValue();
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
                    if (abs(mosquittoMessageExpiryInterval-hiveMqMessageExpiryInterval) > 10000) {
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
