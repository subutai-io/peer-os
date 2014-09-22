package org.safehaus.subutai.plugin.hbase.api;


public enum HBaseType
{

    HMaster( "HMaster is running", "HMaster is NOT running" ),
    HQuorumPeer( "HQuorumPeer is running", "HQuorumPeer is NOT running" ),
    HRegionServer( "HRegionServer is running", "HRegionServer is NOT running" ),
    BackupMaster( "HMaster is running", "HMaster is NOT running" );
    String runningMsg;
    String notRunningMsg;


    HBaseType( String runningMsg, String notRunningMsg )
    {
        this.runningMsg = runningMsg;
        this.notRunningMsg = notRunningMsg;
    }


    public String getRunningMsg()
    {
        return runningMsg;
    }


    public String getNotRunningMsg()
    {
        return notRunningMsg;
    }
}
