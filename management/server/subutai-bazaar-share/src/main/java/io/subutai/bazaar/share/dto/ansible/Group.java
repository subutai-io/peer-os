package io.subutai.bazaar.share.dto.ansible;


import java.util.HashSet;
import java.util.Set;


public class Group
{
    private String name;
    private Set<Host> hosts = new HashSet<>();


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Set<Host> getHosts()
    {
        return hosts;
    }


    public void setHosts( final Set<Host> hosts )
    {
        this.hosts = hosts;
    }
}
