package org.safehaus.kiskis.mgmt.ui.monitor.service;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

class Format {

    private final static Logger LOG = LoggerFactory.getLogger(Format.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");

    static String toPoints(List<JsonNode> nodes) {

        String str = "";

        for (int i = 0; i < nodes.size(); i++) {
            if (!str.isEmpty()) {
                str += ", ";
            }

            str += getPoint(nodes.get(i), i - nodes.size());
        }

        return String.format("[%s]", str);
    }

    private static String getPoint(JsonNode node, int i) {

        long time = parseTime(node.get("@timestamp").asText());
        double value = node.get("val").asDouble();

        return String.format("{ x: %s, y: %s }", time, value);
    }

    private static long parseTime(String dateStr) {

        String target = dateStr.replace("T", " ").replace("Z", "");
        long time = 0;

        try {
            time = DATE_FORMAT.parse(target).getTime();
        } catch (ParseException e) {
            LOG.error("Error while parsing time: ", e);
        }

        return time;
    }
}
