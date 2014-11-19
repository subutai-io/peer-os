/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.plugin.accumulo.rest;


import java.util.Set;


/**
 * @author dilshat
 */
public class TrimmedAccumuloConfig
{

    private String clusterName = "";
    private String instanceName = "";
    private String password = "";
    private String masterNode;
    private String gcNode;
    private String monitor;
    private Set<String> tracers;
    private Set<String> slaves;
    private String hadoopClusterName = "";


    public String getClusterName()
    {
        return clusterName;
    }


    public String getInstanceName()
    {
        return instanceName;
    }


    public String getPassword()
    {
        return password;
    }


    public String getMasterNode()
    {
        return masterNode;
    }


    public String getGcNode()
    {
        return gcNode;
    }


    public String getMonitor()
    {
        return monitor;
    }


    public Set<String> getTracers()
    {
        return tracers;
    }


    public Set<String> getSlaves()
    {
        return slaves;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }
}
