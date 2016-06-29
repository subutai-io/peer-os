package io.subutai.common.security.relation.model;


import io.subutai.common.security.relation.RelationLink;


public class RelationMeta
{
    private RelationLink source;

    private RelationLink target;

    private RelationLink object;

    // Key id to verify relation
    private String keyId;


    public RelationMeta()
    {
    }


    public RelationMeta( final RelationLink source, final RelationLink target, final RelationLink object,
                         final String keyId )
    {
        this.source = source;
        this.target = target;
        this.object = object;
        this.keyId = keyId;
    }


    public RelationMeta( final RelationLink target, final RelationLink object, final String keyId )
    {
        this.target = target;
        this.object = object;
        this.keyId = keyId;
    }


    public RelationLink getSource()
    {
        return source;
    }


    public void setSource( final RelationLink source )
    {
        this.source = source;
    }


    public RelationLink getTarget()
    {
        return target;
    }


    public void setTarget( final RelationLink target )
    {
        this.target = target;
    }


    public RelationLink getObject()
    {
        return object;
    }


    public void setObject( final RelationLink object )
    {
        this.object = object;
    }


    public String getKeyId()
    {
        return keyId;
    }


    public void setKeyId( final String keyId )
    {
        this.keyId = keyId;
    }
}
