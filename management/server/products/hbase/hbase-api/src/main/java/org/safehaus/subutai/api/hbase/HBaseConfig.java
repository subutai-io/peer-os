/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.hbase;


import org.doomdark.uuid.UUIDGenerator;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;
import java.util.UUID;


/**
 * @author dilshat
 */
public class HBaseConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "HBase";
    private int numberOfNodes = 4;
    private UUID uuid;
    private UUID master;
    private Set<UUID> region;
    private Set<UUID> quorum;
    private UUID backupMasters;
    private String domainInfo;
    private Set<UUID> nodes;
    private String clusterName = "";
    private UUID hadoopNameNode;


    public HBaseConfig()
    {
        this.uuid = UUID.fromString( UUIDGenerator.getInstance().generateTimeBasedUUID().toString() );
    }


    public static String getProductKey()
    {
        return PRODUCT_KEY;
    }


    public UUID getHadoopNameNode()
    {
        return hadoopNameNode;
    }


    public void setHadoopNameNode( UUID hadoopNameNode )
    {
        this.hadoopNameNode = hadoopNameNode;
    }


    public UUID getUuid()
    {
        return uuid;
    }


    public void setUuid( UUID uuid )
    {
        this.uuid = uuid;
    }


    public void reset()
    {
        this.master = null;
        this.region = null;
        this.quorum = null;
        this.backupMasters = null;
        this.domainInfo = "";
        this.clusterName = "";
    }


    public UUID getMaster()
    {
        return master;
    }


    public void setMaster( UUID master )
    {
        this.master = master;
    }


    public Set<UUID> getRegion()
    {
        return region;
    }


    public void setRegion( Set<UUID> region )
    {
        this.region = region;
    }


    public Set<UUID> getQuorum()
    {
        return quorum;
    }


    public void setQuorum( Set<UUID> quorum )
    {
        this.quorum = quorum;
    }


    public UUID getBackupMasters()
    {
        return backupMasters;
    }


    public void setBackupMasters( UUID backupMasters )
    {
        this.backupMasters = backupMasters;
    }


    public String getDomainInfo()
    {
        return domainInfo;
    }


    public void setDomainInfo( String domainInfo )
    {
        this.domainInfo = domainInfo;
    }


    public Set<UUID> getNodes()
    {
        return nodes;
    }


    public void setNodes( Set<UUID> nodes )
    {
        this.nodes = nodes;
    }


    public int getNumberOfNodes()
    {
        return numberOfNodes;
    }


    public void setNumberOfNodes( int numberOfNodes )
    {
        this.numberOfNodes = numberOfNodes;
    }


    public String getClusterName()
    {
        return clusterName;
    }


    public void setClusterName( String clusterName )
    {
        this.clusterName = clusterName;
    }


    @Override
    public String getProductName()
    {
        return PRODUCT_KEY;
    }
}
