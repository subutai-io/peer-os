package org.safehaus.subutai.impl.elasticsearch;


import java.util.UUID;
import java.util.concurrent.Executors;

import org.safehaus.subutai.api.elasticsearch.Elasticsearch;


public class ElasticsearchImpl implements Elasticsearch {

    public ElasticsearchImpl() {
        System.out.println( "" );
    }


    public void init() {
//            executor = Executors.newCachedThreadPool();
    }


    public void destroy() {
//            executor.shutdown();
    }


    @Override
    public UUID startNode( String clusterName, String lxcHostname ) {

        System.out.println( "startNode()" );

        return null;
    }
}
