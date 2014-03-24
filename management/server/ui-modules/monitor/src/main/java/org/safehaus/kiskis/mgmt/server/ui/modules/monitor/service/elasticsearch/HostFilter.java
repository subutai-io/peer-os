package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch;

import org.codehaus.jackson.JsonNode;

public class HostFilter {

    public static JsonNode filter(JsonNode json, String host) {

        JsonNode hits = json.get("hits").get("hits");

        for (int i = 0; i < hits.size(); i++) {
            if ( hostEquals( hits.get(i), host ) ) {
                return hits.get(i).get("_source");
            }
        }

        return null;
    }

    private static boolean hostEquals(JsonNode json, String host) {
        String jsonHost = json.get("_source").get("log_host").asText();
        return host.equals(jsonHost);
    }
}
