/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.zookeeper.rest;


import java.util.Set;

import org.safehaus.subutai.plugin.zookeeper.api.SetupType;


/**
 * @author dilshat
 */
public class TrimmedZKConfig
{

    private String clusterName = "";
    private int numberOfNodes = 3;
    private Set<String> nodes;
    private SetupType setupType;


    public SetupType getSetupType()
    {
        return setupType;
    }


    public String getClusterName()
    {
        return clusterName;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public Set<String> getNodes()
    {
        return nodes;
    }
}
