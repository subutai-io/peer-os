/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api.solr;


import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;


public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Solr";
    private String clusterName = "";
    private int numberOfNodes = 3;
    private Set<Agent> nodes;


    public String getClusterName() {
        return clusterName;
    }


    public Config setClusterName( String clusterName ) {
        this.clusterName = clusterName;
        return this;
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }


    public int getNumberOfNodes() {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes ) {
        this.numberOfNodes = numberOfNodes;
    }


    public Set<Agent> getNodes() {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes ) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append( "clusterName", clusterName )
                .append( "numberOfNodes", numberOfNodes )
                .append( "nodes", nodes )
                .toString();
    }
}
