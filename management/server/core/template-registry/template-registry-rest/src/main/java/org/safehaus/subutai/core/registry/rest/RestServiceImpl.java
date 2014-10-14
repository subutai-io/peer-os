package org.safehaus.subutai.core.registry.rest;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistry;
import org.safehaus.subutai.core.registry.api.TemplateTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService
{

    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String EXCEPTION_HEADER = "exception";
    private static final String TEMPLATE_PARENT_DELIMITER = " ";
    private static final String TEMPLATES_DELIMITER = "\n";

    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private TemplateRegistry templateRegistry;


    public void setTemplateRegistry( TemplateRegistry templateRegistry )
    {
        Preconditions.checkNotNull( templateRegistry, "TemplateRegistry is null." );
        this.templateRegistry = templateRegistry;
    }


    @Override
    public Response getTemplate( final String templateName )
    {
        Template template = templateRegistry.getTemplate( templateName );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response registerTemplate( final String configFilePath, final String packagesFilePath, final String md5sum,
                                      final String peerId )
    {
        try
        {

            templateRegistry.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                    FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ), md5sum,
                    UUID.fromString( peerId ) );

            return Response.ok().build();
        }
        catch ( IOException | RegistryException | RuntimeException e )
        {
            LOG.error( "Error in registerTemplate", e );
            return Response.status( Response.Status.BAD_REQUEST ).header( EXCEPTION_HEADER, e.getMessage() ).build();
        }
    }


    @Override
    public Response unregisterTemplate( final String templateName )
    {
        try
        {

            templateRegistry.unregisterTemplate( templateName );

            return Response.ok().build();
        }
        catch ( RegistryException | RuntimeException e )
        {
            LOG.error( "Error in unregisterTemplate", e );
            return Response.status( Response.Status.BAD_REQUEST ).header( EXCEPTION_HEADER, e.getMessage() ).build();
        }
    }


    @Override
    public Response getTemplate( final String templateName, final String lxcArch )
    {
        Template template = templateRegistry.getTemplate( templateName, lxcArch );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplate( final String childTemplateName )
    {
        Template template = templateRegistry.getParentTemplate( childTemplateName );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplate( final String childTemplateName, final String lxcArch )
    {
        Template template = templateRegistry.getParentTemplate( childTemplateName, lxcArch );
        if ( template != null )
        {
            return Response.ok().entity( GSON.toJson( template ) ).build();
        }
        else
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response getParentTemplates( final String childTemplateName )
    {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistry.getParentTemplates( childTemplateName ) )
        {
            parents.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( parents ) ).build();
    }


    @Override
    public Response getParentTemplates( final String childTemplateName, final String lxcArch )
    {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistry.getParentTemplates( childTemplateName, lxcArch ) )
        {
            parents.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( parents ) ).build();
    }


    @Override
    public Response getChildTemplates( final String parentTemplateName )
    {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistry.getChildTemplates( parentTemplateName ) )
        {
            children.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( children ) ).build();
    }


    @Override
    public Response getChildTemplates( final String parentTemplateName, final String lxcArch )
    {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistry.getChildTemplates( parentTemplateName, lxcArch ) )
        {
            children.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( children ) ).build();
    }


    @Override
    public Response getTemplateTree()
    {
        TemplateTree tree = templateRegistry.getTemplateTree();
        List<Template> uberTemplates = tree.getRootTemplates();
        if ( uberTemplates != null )
        {
            for ( Template template : uberTemplates )
            {
                addChildren( tree, template );
            }
        }
        return Response.ok().entity( GSON.toJson( uberTemplates ) ).build();
    }


    @Override
    public Response isTemplateInUse( final String templateName )
    {
        try
        {
            return Response.ok().entity( JsonUtil.toJson( "RESULT", templateRegistry.isTemplateInUse( templateName ) ) )
                           .build();
        }
        catch ( RegistryException e )
        {
            LOG.error( "Error in isTemplateInUse", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).header( EXCEPTION_HEADER, e.getMessage() )
                           .build();
        }
    }


    @Override
    public Response setTemplateInUse( final String faiHostname, final String templateName, final String isInUse )
    {
        try
        {

            templateRegistry.updateTemplateUsage( faiHostname, templateName, Boolean.parseBoolean( isInUse ) );

            return Response.ok().build();
        }
        catch ( RegistryException e )
        {
            LOG.error( "Error in setTemplateInUse", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).header( EXCEPTION_HEADER, e.getMessage() )
                           .build();
        }
    }


    @Override
    public Response listTemplates()
    {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistry.getAllTemplates() )
        {
            templates.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( templates ) ).build();
    }


    @Override
    public Response listTemplates( final String lxcArch )
    {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistry.getAllTemplates( lxcArch ) )
        {
            templates.add( template.getTemplateName() );
        }
        return Response.ok().entity( GSON.toJson( templates ) ).build();
    }


    @Override
    public Response listTemplatesPlain()
    {
        return listTemplatesPlain( Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public Response listTemplatesPlain( final String lxcArch )
    {
        StringBuilder output = new StringBuilder();
        List<Template> templates = templateRegistry.getAllTemplates( lxcArch );

        for ( final Template template : templates )
        {
            output.append( template.getTemplateName() ).append( TEMPLATE_PARENT_DELIMITER ).append(
                    Strings.isNullOrEmpty( template.getParentTemplateName() ) ? "" : template.getParentTemplateName() )
                  .append( TEMPLATES_DELIMITER );
        }

        return Response.ok().entity( GSON.toJson( output.toString() ) ).build();
    }


    private void addChildren( TemplateTree tree, Template currentTemplate )
    {
        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( !( children == null || children.isEmpty() ) )
        {
            currentTemplate.addChildren( children );
            for ( Template child : children )
            {
                addChildren( tree, child );
            }
        }
    }
}
