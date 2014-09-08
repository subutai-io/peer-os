package org.safehaus.subutai.plugin.mahout.api;


import java.util.List;


/**
 * Created by bahadyr on 9/4/14.
 */
public class TrimmedMahoutClusterConfig {

    String clusterName;
    List<String> nodes;


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( final String clusterName ) {
        this.clusterName = clusterName;
    }


    public List<String> getNodes() {
        return nodes;
    }


    public void setNodes( final List<String> nodes ) {
        this.nodes = nodes;
    }
}
