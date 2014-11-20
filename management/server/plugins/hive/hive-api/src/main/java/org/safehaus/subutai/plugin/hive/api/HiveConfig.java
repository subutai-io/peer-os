package org.safehaus.subutai.plugin.hive.api;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.ConfigBase;


public class HiveConfig implements ConfigBase
{

    public static final String PRODUCT_KEY = "Hive";
    public static final String TEMPLATE_NAME = "hadoophive";

    private SetupType setupType;
    private String clusterName = "";
    private String hadoopClusterName = "";
    private UUID server;
    private Set<UUID> clients = new HashSet();
    private Set<UUID> hadoopNodes = new HashSet<>();
    private UUID environmentId;


    public static String getTemplateName()
    {
        return TEMPLATE_NAME;
    }


    public SetupType getSetupType()
    {
        return setupType;
    }


    public void setSetupType( SetupType setupType )
    {
        this.setupType = setupType;
    }


    public UUID getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final UUID environmentId )
    {
        this.environmentId = environmentId;
    }


    public int getNumberOfNodes()
    {
        return clients.size() + 1;
    }


    @Override
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


    @Override
    public String getProductKey()
    {
        return PRODUCT_KEY;
    }


    public String getHadoopClusterName()
    {
        return hadoopClusterName;
    }


    public void setHadoopClusterName( String hadoopClusterName )
    {
        this.hadoopClusterName = hadoopClusterName;
    }


    public UUID getServer()
    {
        return server;
    }


    public void setServer( UUID server )
    {
        this.server = server;
    }


    public Set<UUID> getClients()
    {
        return clients;
    }


    public void setClients( Set<UUID> clients )
    {
        this.clients = clients;
    }


    public Set<UUID> getHadoopNodes()
    {
        return hadoopNodes;
    }


    public void setHadoopNodes( Set<UUID> hadoopNodes )
    {
        this.hadoopNodes = hadoopNodes;
    }


    public Set<UUID> getAllNodes()
    {
        Set<UUID> allNodes = new HashSet<>();
        if ( clients != null )
        {
            allNodes.addAll( clients );
        }
        if ( server != null )
        {
            allNodes.add( server );
        }
        return allNodes;
    }


    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode( this.clusterName );
        return hash;
    }


    @Override
    public boolean equals( Object obj )
    {
        if ( obj instanceof HiveConfig )
        {
            HiveConfig o = ( HiveConfig ) obj;
            return clusterName != null ? clusterName.equals( o.clusterName ) : false;
        }
        return false;
    }


    @Override
    public String toString()
    {
        return "Config{" + "clusterName=" + clusterName + ", server=" + server + ", clients=" + ( clients != null ?
                                                                                                  clients.size() : 0 )
                + '}';
    }
}
