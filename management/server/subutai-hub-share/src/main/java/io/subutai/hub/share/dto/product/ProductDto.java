package io.subutai.hub.share.dto.product;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;


//Version 1.1
public class ProductDto
{
    public enum Type
    {
        PLUGIN
    }


    private String id;

    private String name;

    private Type type;

    private Date createDate;

    private String description;

    private String ownerId;

    private String version;

    private Set<String> metadata = new HashSet<>();

    private Set<String> dependencies = new HashSet<>();


    public ProductDto()
    {
    }


    //JSONObject to ProductDto
    public ProductDto( JSONObject objProduct ) throws ParseException
    {
        this.id = objProduct.getString( "id" );
        this.name = objProduct.getString( "name" );
        this.type = Type.valueOf( objProduct.getString( "type" ) );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        this.createDate = sdf.parse( "2013-09-29T18:46:19Z" );
        this.ownerId = objProduct.getString( "ownerId" );
        this.version = objProduct.getString( "version" );
        this.description = objProduct.getString( "description" );

        JSONArray metadataJson = objProduct.getJSONArray( "metadata" );
        for ( int i = 0; i < metadataJson.length(); i++ )
        {
            this.metadata.add( metadataJson.getString( i ) );
        }

        JSONArray dependenciesJson = objProduct.getJSONArray( "dependencies" );
        for ( int i = 0; i < dependenciesJson.length(); i++ )
        {
            this.dependencies.add( dependenciesJson.getString( i ) );
        }
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


    public void addDependency( final String dependencyId )
    {
        this.dependencies.add( dependencyId );
    }


    public Set<String> getDependencies()
    {
        return dependencies;
    }


    public void setDependencies( final Set<String> dependencies )
    {
        this.dependencies = dependencies;
    }
}
