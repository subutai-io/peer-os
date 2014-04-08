package org.safehaus.kiskis.mgmt.ui.monitor.service;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.kiskis.mgmt.ui.monitor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Query {

    private final static Logger LOG = LoggerFactory.getLogger(Query.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String QUERY = FileUtil.getContent("elasticsearch/query.json");

    public static String execute(String host, String metric, int maxSize) {

        String data = "";

        try {
            data = doExecute(host, metric, maxSize);
        } catch (Exception e) {
            LOG.error("Error while executing query: ", e);
        }

        return data;
    }

    private static String doExecute(String host, String metricName, int maxSize) throws Exception {

        String query = QUERY
                .replace("$host", host)
                .replace("$metricName", metricName);

        String response = HttpPost.execute(query);

        List<JsonNode> nodes = toNodes(response);

        LOG.info("nodes count: {}", nodes.size());

        // We need reverse the list b/c the query returns data in desc order
        Collections.reverse(nodes);

        return nodes.isEmpty() ? "" : Format.toPoints(nodes);
    }

    static List<JsonNode> toNodes(String response) throws IOException {

        JsonNode json = OBJECT_MAPPER.readTree(response);
        JsonNode hits = json.get("hits").get("hits");

        ArrayList<JsonNode> nodes = new ArrayList<JsonNode>();

        for (int i = 0; i < hits.size(); i++) {
            JsonNode node = hits.get(i).get("_source");
            nodes.add(node);

            LOG.info("node: {}", node);
        }

        return nodes;
    }


}
