package io.subutai.common.peer;


public class ExportedTemplate
{
    private String hash;
    private String md5sum;
    private long size;
    private String parent;


    public ExportedTemplate( final String hash, final String md5sum, final long size, final String parent )
    {
        this.hash = hash;
        this.md5sum = md5sum;
        this.size = size;
        this.parent = parent;
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


    public String getParent()
    {
        return parent;
    }


    @Override
    public String toString()
    {
        return "{" + "hash='" + hash + '\'' + ", md5sum='" + md5sum + '\'' + ", size=" + size + ", parent='" + parent
                + '\'' + '}';
    }
}
