package org.safehaus.subutai.api.elasticsearch;


import java.util.UUID;


public interface ElasticSearch {

    public UUID startNode(String clusterName, String lxcHostname);

}
