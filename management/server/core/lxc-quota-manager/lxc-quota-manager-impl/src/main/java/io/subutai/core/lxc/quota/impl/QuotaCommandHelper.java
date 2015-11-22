package io.subutai.core.lxc.quota.impl;


import java.util.ArrayList;
import java.util.List;

import io.subutai.common.peer.ContainerQuota;


/**
 * Quota commands helper
 */
public class QuotaCommandHelper
{
    private String containerName;
    private ContainerQuota containerQuota;


    public QuotaCommandHelper( final String containerName, final ContainerQuota containerQuota )
    {
        this.containerName = containerName;
        this.containerQuota = containerQuota;
    }


    List<String> getQuotaSetCommands()
    {
        final List<String> result = new ArrayList<>();
        if ( containerQuota.getRam() != null )
        {
            result.add( setRamCommand() );
        }
        return result;
    }


    String getRamCommand()
    {
        return String.format( "subutai quota %s ram", containerName );
    }


    String setRamCommand()
    {
        if ( containerQuota.getRam() != null )
        {
            return String.format( "subutai quota %s ram -s %d", containerName, containerQuota.getRam() );
        }
        else
        {
            return null;
        }
    }


    String setCpuCommand()
    {
        if ( containerQuota.getCpu() != null )
        {
            return String.format( "subutai quota %s cpu -s %d", containerName, containerQuota.getCpu() );
        }
        else
        {
            return null;
        }
    }


    String setOptSpaceCommand()
    {
        if ( containerQuota.getOpt() != null )
        {
            return String.format( "subutai quota %s diskOpt -s %d", containerName, containerQuota.getOpt() );
        }
        else
        {
            return null;
        }
    }


    String setHomeSpaceCommand()
    {
        if ( containerQuota.getHome() != null )
        {
            return String.format( "subutai quota %s diskHome -s %d", containerName, containerQuota.getHome() );
        }
        else
        {
            return null;
        }
    }


    String setVarSpaceCommand()
    {
        if ( containerQuota.getVar() != null )
        {
            return String.format( "subutai quota %s diskVar -s %d", containerName, containerQuota.getVar() );
        }
        else
        {
            return null;
        }
    }


    String setRootSpaceCommand()
    {
        if ( containerQuota.getRoot() != null )
        {
            return String.format( "subutai quota %s diskRootfs -s %d", containerName, containerQuota.getRoot() );
        }
        else
        {
            return null;
        }
    }
}
