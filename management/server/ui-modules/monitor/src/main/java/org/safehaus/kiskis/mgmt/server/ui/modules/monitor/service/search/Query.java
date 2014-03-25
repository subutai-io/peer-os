package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.search;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.safehaus.kiskis.mgmt.server.ui.modules.monitor.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        String query = QUERY.replace("$metricName", metricName);
        String response = HttpPost.execute(query);
        JsonNode json = OBJECT_MAPPER.readTree(response);
        List<JsonNode> nodes = HostFilter.filter(json, host, maxSize);

        return Format.toPoints(nodes);
    }

}
