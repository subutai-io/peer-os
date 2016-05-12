package io.subutai.common.security.relation.model;


import java.util.Map;

import com.google.common.collect.Maps;

import io.subutai.common.security.objects.Ownership;


public class RelationInfoMeta
{
    //read, write, delete, update
    private boolean readPermission;

    private boolean writePermission;

    private boolean updatePermission;

    private boolean deletePermission;

    //Permission, role
    private int ownershipLevel = Ownership.ALL.getLevel();

    private Map<String, String> relationTraits = Maps.newHashMap();


    public RelationInfoMeta()
    {
    }


    public RelationInfoMeta( final boolean readPermission, final boolean writePermission,
                             final boolean updatePermission, final boolean deletePermission, final int ownershipLevel )
    {
        this.readPermission = readPermission;
        this.writePermission = writePermission;
        this.updatePermission = updatePermission;
        this.deletePermission = deletePermission;
        this.ownershipLevel = ownershipLevel;
    }


    public Map<String, String> getRelationTraits()
    {
        return relationTraits;
    }


    public void setRelationTraits( final Map<String, String> relationTraits )
    {
        this.relationTraits = relationTraits;
    }


    public boolean isReadPermission()
    {
        return readPermission;
    }


    public void setReadPermission( final boolean readPermission )
    {
        this.readPermission = readPermission;
    }


    public boolean isWritePermission()
    {
        return writePermission;
    }


    public void setWritePermission( final boolean writePermission )
    {
        this.writePermission = writePermission;
    }


    public boolean isUpdatePermission()
    {
        return updatePermission;
    }


    public void setUpdatePermission( final boolean updatePermission )
    {
        this.updatePermission = updatePermission;
    }


    public boolean isDeletePermission()
    {
        return deletePermission;
    }


    public void setDeletePermission( final boolean deletePermission )
    {
        this.deletePermission = deletePermission;
    }


    public int getOwnershipLevel()
    {
        return ownershipLevel;
    }


    public void setOwnershipLevel( final int ownershipLevel )
    {
        this.ownershipLevel = ownershipLevel;
    }
}
