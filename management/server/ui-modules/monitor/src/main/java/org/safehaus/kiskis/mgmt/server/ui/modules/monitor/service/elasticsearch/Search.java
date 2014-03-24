package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.elasticsearch;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Search {

    private final static Logger LOG = LoggerFactory.getLogger(Search.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void execute() {

        try {
            String params = Params.getCpu();
            LOG.info("params: ", params);
            System.out.println("params: " + params);

            String response = HttpPost.execute(params);
            LOG.info("response: ", response);
            System.out.println("response: " + response);

            JsonNode json = OBJECT_MAPPER.readTree(response);
            json = HostFilter.filter(json, "py453399588-lxc-hadoop1");
            LOG.info("json: {}", json);
            System.out.println("json: " + json);

        } catch (Exception e) {
            LOG.error("Error while request: ", e);
            e.printStackTrace();
        }
    }


}
