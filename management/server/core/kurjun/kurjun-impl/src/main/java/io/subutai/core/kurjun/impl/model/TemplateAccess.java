package io.subutai.core.kurjun.impl.model;


import ai.subut.kurjun.metadata.common.subutai.TemplateId;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLink;


/**
 * Template Access object to be used in building of trust relation links
 *
 */
public class TemplateAccess implements RelationLink
{

    private final TemplateId templateId;


    public TemplateAccess( String ownerFprint, String md5 )
    {
        templateId = new TemplateId( ownerFprint, md5 );
    }


    public TemplateAccess( TemplateId templateId )
    {
        this.templateId = templateId;
    }


    public TemplateId getTemplateId()
    {
        return templateId;
    }


    @Override
    public String getLinkId()
    {
        return String.format( "%s|%s", getClassPath(), getUniqueIdentifier() );
    }


    @Override
    public String getUniqueIdentifier()
    {
        return templateId.get();
    }


    @Override
    public String getClassPath()
    {
        return this.getClass().getSimpleName();
    }


    @Override
    public String getContext()
    {
        return PermissionObject.TemplateManagement.getName();
    }


    @Override
    public String getKeyId()
    {
        return templateId.getOwnerFprint();
    }
}
