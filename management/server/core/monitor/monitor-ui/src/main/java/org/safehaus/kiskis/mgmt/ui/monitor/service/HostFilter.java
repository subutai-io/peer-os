package org.safehaus.kiskis.mgmt.ui.monitor.service;

import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO refactor - should work as instance
class HostFilter {

    private final static Logger LOG = LoggerFactory.getLogger(HostFilter.class);

    static List<JsonNode> filter(JsonNode json, String host, int maxSize) {

        ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();
        JsonNode hits = json.get("hits").get("hits");

        for (int i = 0; i < hits.size(); i++) {
            JsonNode source = hits.get(i).get("_source");

            if ( filter(nodes, source, host, maxSize) ) {
                break;
            }
        }

        return reverse(nodes);
    }

    private static List<JsonNode> reverse(List<JsonNode> nodes) {

        Collections.reverse(nodes);

        for (JsonNode node : nodes) {
            LOG.info("node: {}", node);
        }

        return nodes;
    }

    private static boolean filter(List<JsonNode> nodes, JsonNode node, String host, int maxSize) {

        if ( !hostEquals( node, host ) ) {
            return false;
        }

        nodes.add(node);

        return nodes.size() >= maxSize;
    }

    private static boolean hostEquals(JsonNode json, String host) {
        String jsonHost = json.get("log_host").asText();
        return host.equals(jsonHost);
    }
}
