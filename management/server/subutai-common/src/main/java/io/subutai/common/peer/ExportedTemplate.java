package io.subutai.common.peer;


public class ExportedTemplate
{
    private String hash;
    private String md5sum;
    private long size;


    public ExportedTemplate( final String hash, final String md5sum, final long size )
    {
        this.hash = hash;
        this.md5sum = md5sum;
        this.size = size;
    }


    public String getHash()
    {
        return hash;
    }


    public String getMd5sum()
    {
        return md5sum;
    }


    public long getSize()
    {
        return size;
    }


    @Override
    public String toString()
    {
        return "{" + "hash='" + hash + '\'' + ", md5sum='" + md5sum + '\'' + ", size=" + size + '}';
    }
}
