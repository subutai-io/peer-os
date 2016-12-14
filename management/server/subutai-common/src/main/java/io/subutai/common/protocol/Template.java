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
}
