package org.safehaus.subutai.plugin.cassandra.api;


/**
 * Created by bahadyr on 9/4/14.
 */
public class TrimmedCassandraConfig {

    private String clusterName;
    private String domainName;
    private int numberOfSeeds;
    private int numberOfNodes;


    public String getClusterName() {
        return clusterName;
    }


    public void setClusterName( final String clusterName ) {
        this.clusterName = clusterName;
    }


    public String getDomainName() {
        return domainName;
    }


    public void setDomainName( final String domainName ) {
        this.domainName = domainName;
    }


    public int getNumberOfSeeds() {
        return numberOfSeeds;
    }


    public void setNumberOfSeeds( final int numberOfSeeds ) {
        this.numberOfSeeds = numberOfSeeds;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public void setNumberOfNodes( final int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }
}
