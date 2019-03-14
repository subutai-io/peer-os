package io.subutai.common.host;


import java.util.Date;
import java.util.Objects;

import com.google.common.base.Preconditions;


public class IntegralSnapshot
{
    // lxc container partitions:
    public static final String PARTITION_ROOTFS = "rootfs";
    public static final String PARTITION_HOME = "home";
    public static final String PARTITION_OPT = "opt";
    public static final String PARTITION_VAR = "var";

    private String containerName;
    private String label;
    private Snapshot rootfsSnapshot;
    private Snapshot homeSnapshot;
    private Snapshot optSnapshot;
    private Snapshot varSnapshot;


    public IntegralSnapshot()
    {
    }


    public IntegralSnapshot( final String containerName, final String label, final Snapshot rootfsSnapshot,
                             final Snapshot homeSnapshot, final Snapshot optSnapshot, final Snapshot varSnapshot )
    {
        this.containerName = containerName;
        this.label = label;
        setRootfsSnapshot( rootfsSnapshot );
        setHomeSnapshot( homeSnapshot );
        setOptSnapshot( optSnapshot );
        setVarSnapshot( varSnapshot );
    }


    public String getContainerName()
    {
        return containerName;
    }


    public void setContainerName( final String containerName )
    {
        this.containerName = containerName;
    }


    public String getLabel()
    {
        return label;
    }


    public void setLabel( final String label )
    {
        this.label = label;
    }


    public Snapshot getRootfsSnapshot()
    {
        return rootfsSnapshot;
    }


    public void setRootfsSnapshot( final Snapshot rootfsSnapshot )
    {
        if ( rootfsSnapshot != null )
        {
            Preconditions.checkArgument( Objects.equals( rootfsSnapshot.getContainerName(), this.containerName ) );
        }
        this.rootfsSnapshot = rootfsSnapshot;
    }


    public Snapshot getHomeSnapshot()
    {
        return homeSnapshot;
    }


    public void setHomeSnapshot( final Snapshot homeSnapshot )
    {
        if ( homeSnapshot != null )
        {
            Preconditions.checkArgument( Objects.equals( homeSnapshot.getContainerName(), this.containerName ) );
        }
        this.homeSnapshot = homeSnapshot;
    }


    public Snapshot getOptSnapshot()
    {
        return optSnapshot;
    }


    public void setOptSnapshot( final Snapshot optSnapshot )
    {
        if ( optSnapshot != null )
        {
            Preconditions.checkArgument( Objects.equals( optSnapshot.getContainerName(), this.containerName ) );
        }
        this.optSnapshot = optSnapshot;
    }


    public Snapshot getVarSnapshot()
    {
        return varSnapshot;
    }


    public void setVarSnapshot( final Snapshot varSnapshot )
    {
        if ( varSnapshot != null )
        {
            Preconditions.checkArgument( Objects.equals( varSnapshot.getContainerName(), this.containerName ) );
        }
        this.varSnapshot = varSnapshot;
    }


    public Date getSnapshotDate()
    {
        if ( rootfsSnapshot != null )
        {
            return rootfsSnapshot.getCreated();
        }
        else if ( homeSnapshot != null )
        {
            return homeSnapshot.getCreated();
        }
        else if ( optSnapshot != null )
        {
            return optSnapshot.getCreated();
        }
        else if ( varSnapshot != null )
        {
            return varSnapshot.getCreated();
        }

        return null;
    }


    public void setPartitionSnapshot( String partition, Snapshot snapshot )
    {
        if ( PARTITION_ROOTFS.equals( partition ) )
        {
            setRootfsSnapshot( snapshot );
        }
        else if ( PARTITION_HOME.equals( partition ) )
        {
            setHomeSnapshot( snapshot );
        }
        else if ( PARTITION_OPT.equals( partition ) )
        {
            setOptSnapshot( snapshot );
        }
        else if ( PARTITION_VAR.equals( partition ) )
        {
            setVarSnapshot( snapshot );
        }
    }


    /*
    For container snapshot to be integral(full), all 4 partitions must be present
     */
    public boolean isIntegral()
    {
        return this.rootfsSnapshot != null && this.homeSnapshot != null && this.optSnapshot != null
                && this.varSnapshot != null;
    }
}
