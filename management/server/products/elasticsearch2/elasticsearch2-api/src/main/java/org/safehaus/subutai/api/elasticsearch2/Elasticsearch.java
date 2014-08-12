package org.safehaus.subutai.api.elasticsearch2;


import org.safehaus.subutai.shared.protocol.ApiBase;

import java.util.UUID;


public interface Elasticsearch extends ApiBase<Config> {

    UUID startAllNodes( String clusterName );

    UUID checkAllNodes( String clusterName );

    UUID stopAllNodes( String clusterName );
}
