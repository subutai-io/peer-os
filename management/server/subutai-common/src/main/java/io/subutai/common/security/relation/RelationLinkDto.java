package io.subutai.common.security.relation;


import com.google.common.base.Preconditions;


/**
 * Created by ape-craft on 3/29/16.
 */
public class RelationLinkDto implements RelationLink
{
    private String linkId;
    private String uniqueIdentifier;
    private String classPath;
    private String context = "";
    private String keyId;


    public RelationLinkDto( final String uniqueIdentifier, final String classPath, final String context,
                            final String keyId )
    {
        this.uniqueIdentifier = uniqueIdentifier;
        this.classPath = classPath;
        this.context = context;
        this.keyId = keyId;
        this.linkId = String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    public RelationLinkDto( RelationLink relationLink )
    {
        Preconditions.checkNotNull( relationLink, "Error relationLink is null." );
        this.uniqueIdentifier = relationLink.getUniqueIdentifier();
        this.classPath = relationLink.getClassPath();
        this.linkId = relationLink.getLinkId();
        this.context = relationLink.getContext();
        this.keyId = relationLink.getKeyId();
    }


    @Override
    public String getLinkId()
    {
        return linkId;
    }


    @Override
    public String getUniqueIdentifier()
    {
        return uniqueIdentifier;
    }


    @Override
    public String getClassPath()
    {
        return classPath;
    }


    @Override
    public String getContext()
    {
        return context;
    }


    public void setContext( final String context )
    {
        this.context = context;
    }


    public void setLinkId( final String id )
    {
        this.linkId = id;
    }


    public void setUniqueIdentifier( final String uniqueIdentifier )
    {
        this.uniqueIdentifier = uniqueIdentifier;
    }


    public void setClassPath( final String classPath )
    {
        this.classPath = classPath;
    }


    @Override
    public String getKeyId()
    {
        return keyId;
    }
}
