package io.subutai.core.kurjun.impl;


/**
 * Created by ape-craft on 5/27/16.
 */
public class PackageProgress
{
    private long total;
    private long received;


    public PackageProgress( final long total, final long received )
    {
        this.total = total;
        this.received = received;
    }


    public long getPercentage()
    {
        return ( 100 * received ) / total;
    }


    public void addReceivedBytesCount( final long received )
    {
        this.received += received;
    }
}
