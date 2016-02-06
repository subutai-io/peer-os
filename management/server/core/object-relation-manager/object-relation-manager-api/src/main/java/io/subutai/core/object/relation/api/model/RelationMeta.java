package io.subutai.core.object.relation.api.model;


import io.subutai.common.security.relation.RelationLink;


/**
 * Created by talas on 12/10/15.
 */
public class RelationMeta
{
    private RelationLink source;

    private RelationLink target;

    private RelationLink object;

    private String keyId;

    private String context;


    public RelationMeta()
    {
    }

    //    public RelationMeta( final Object source, final String sourceId, final Object target, final String targetId,
    //                         final Object object, final String objectId, final String keyId, final String context )
    //    {
    //        this.sourceId = sourceId;
    //        this.sourcePath = source.getClass().getSimpleName();
    //        this.targetId = targetId;
    //        this.targetPath = target.getClass().getSimpleName();
    //        this.objectId = objectId;
    //        this.objectPath = object.getClass().getSimpleName();
    //        this.keyId = keyId;
    //        this.context = context;
    //    }


    public RelationMeta( final RelationLink source, final RelationLink target, final RelationLink object,
                         final String keyId, final String context )
    {
        this.source = source;
        this.target = target;
        this.object = object;
        this.keyId = keyId;
        this.context = context;
    }


    public RelationMeta( final RelationLink source, final RelationLink object, final String keyId,
                         final String context )
    {
        this.source = source;
        this.target = source;
        this.object = object;
        this.keyId = keyId;
        this.context = context;
    }


    public String getContext()
    {
        return context;
    }


    public void setContext( final String context )
    {
        this.context = context;
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
