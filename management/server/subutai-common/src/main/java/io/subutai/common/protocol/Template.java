package io.subutai.common.protocol;


import java.util.Set;

import com.google.gson.annotations.SerializedName;

import io.subutai.common.host.HostArchitecture;


public class Template
{
    private String id;
    private String name;
    private long size;
    @SerializedName( "owner" )
    private Set<String> owners;
    private Set<String> tags;
    private String parent;
    private String version;
    private String filename;
    private HostArchitecture architecture;


    public String getId()
    {
        return id;
    }


    public String getName()
    {
        return name;
    }


    public long getSize()
    {
        return size;
    }


    public Set<String> getOwners()
    {
        return owners;
    }


    public Set<String> getTags()
    {
        return tags;
    }


    public String getParent()
    {
        return parent;
    }


    public String getVersion()
    {
        return version;
    }


    public String getFilename()
    {
        return filename;
    }


    public HostArchitecture getArchitecture()
    {
        return architecture;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "Template{" );
        sb.append( "id='" ).append( id ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", size=" ).append( size );
        sb.append( ", owners=" ).append( owners );
        sb.append( ", tags=" ).append( tags );
        sb.append( ", parent='" ).append( parent ).append( '\'' );
        sb.append( ", version='" ).append( version ).append( '\'' );
        sb.append( ", filename='" ).append( filename ).append( '\'' );
        sb.append( ", architecture=" ).append( architecture );
        sb.append( '}' );
        return sb.toString();
    }
}
