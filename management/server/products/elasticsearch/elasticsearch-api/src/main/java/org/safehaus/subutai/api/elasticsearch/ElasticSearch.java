package org.safehaus.subutai.api.elasticsearch;


import java.util.UUID;


public interface Elasticsearch {

    public UUID startNode(String clusterName, String lxcHostname);

}
