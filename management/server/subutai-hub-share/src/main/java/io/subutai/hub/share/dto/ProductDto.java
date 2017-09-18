package io.subutai.hub.share.dto;


import java.util.Date;
import java.util.Set;


public class ProductDto
{
    public enum Type
    {
        PLUGIN, BLUEPRINT
    }
    private String id;

    private String name;

    private Type type;

    private Date createDate;

    private String description;

    private String ownerId;

    private String version;

    private Set<String> metadata;

    public ProductDto()
    {
    }

    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Type getType()
    {
        return type;
    }


    public void setType( final Type type )
    {
        this.type = type;
    }


    public Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( final Date createDate )
    {
        this.createDate = createDate;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription( final String description )
    {
        this.description = description;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion( final String version )
    {
        this.version = version;
    }


    public Set<String> getMetadata()
    {
        return metadata;
    }


    public void setMetadata( final Set<String> metadata )
    {
        this.metadata = metadata;
    }
}
