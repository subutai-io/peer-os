package io.subutai.core.identity.api.model;


import io.subutai.common.security.objects.PermissionObject;


/**
 * Created by talas on 12/10/15.
 */
public class RelationMeta
{
    private PermissionObject permissionObject;

    private String sourceId;
    private String sourcePath;

    private String targetId;
    private String targetPath;

    private String objectId;
    private String objectPath;

    private String keyId;


    public RelationMeta()
    {
    }


    public RelationMeta( final String sourceId, final String sourcePath, final String targetId, final String targetPath,
                         final String objectId, final String objectPath, PermissionObject permissionObject,
                         final String keyId )
    {
        this.sourceId = sourceId;
        this.sourcePath = sourcePath;
        this.targetId = targetId;
        this.targetPath = targetPath;
        this.objectId = objectId;
        this.objectPath = objectPath;
        this.permissionObject = permissionObject;
        this.keyId = keyId;
    }


    public RelationMeta( final Object source, final String sourceId, final Object object, final String objectId,
                         final PermissionObject permissionObject, final String keyId )
    {
        this.sourceId = sourceId;
        this.sourcePath = source.getClass().getSimpleName();
        this.targetId = sourceId;
        this.targetPath = source.getClass().getSimpleName();
        this.objectId = objectId;
        this.objectPath = object.getClass().getSimpleName();
        this.permissionObject = permissionObject;
        this.keyId = keyId;
    }


    public RelationMeta( final Object source, final String sourceId, final Object target, final String targetId,
                         final Object object, final String objectId, final PermissionObject permissionObject,
                         String keyId )
    {
        this.sourceId = sourceId;
        this.sourcePath = source.getClass().getSimpleName();
        this.targetId = targetId;
        this.targetPath = target.getClass().getSimpleName();
        this.objectId = objectId;
        this.objectPath = object.getClass().getSimpleName();
        this.permissionObject = permissionObject;
        this.keyId = keyId;
    }


    public String getSourceId()
    {
        return sourceId;
    }


    public void setSourceId( final String sourceId )
    {
        this.sourceId = sourceId;
    }


    public String getSourcePath()
    {
        return sourcePath;
    }


    public void setSourcePath( final String sourcePath )
    {
        this.sourcePath = sourcePath;
    }


    public String getTargetId()
    {
        return targetId;
    }


    public void setTargetId( final String targetId )
    {
        this.targetId = targetId;
    }


    public String getTargetPath()
    {
        return targetPath;
    }


    public void setTargetPath( final String targetPath )
    {
        this.targetPath = targetPath;
    }


    public String getObjectId()
    {
        return objectId;
    }


    public void setObjectId( final String objectId )
    {
        this.objectId = objectId;
    }


    public String getObjectPath()
    {
        return objectPath;
    }


    public void setObjectPath( final String objectPath )
    {
        this.objectPath = objectPath;
    }


    public PermissionObject getPermissionObject()
    {
        return permissionObject;
    }


    public void setPermissionObject( final PermissionObject permissionObject )
    {
        this.permissionObject = permissionObject;
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
