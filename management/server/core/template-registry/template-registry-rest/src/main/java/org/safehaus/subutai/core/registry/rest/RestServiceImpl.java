package org.safehaus.subutai.core.registry.rest;


import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.common.util.FileUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.registry.api.RegistryException;
import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;
import org.safehaus.subutai.core.registry.api.TemplateTree;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService {

    private static final String TEMPLATE_PARENT_DELIMITER = " ";
    private static final String TEMPLATES_DELIMITER = "\n";

    private static final Gson gson =
            new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( TemplateRegistryManager templateRegistryManager ) {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    public String getTemplate( final String templateName ) {
        return gson.toJson( templateRegistryManager.getTemplate( templateName ) );
    }


    @Override
    public Response registerTemplate( final String configFilePath, final String packagesFilePath,
                                      final String md5sum ) {
        try {

            templateRegistryManager.registerTemplate( FileUtil.readFile( configFilePath, Charset.defaultCharset() ),
                    FileUtil.readFile( packagesFilePath, Charset.defaultCharset() ), md5sum );

            return Response.status( Response.Status.OK ).build();
        }
        catch ( IOException | RegistryException | RuntimeException e ) {
            return Response.status( Response.Status.BAD_REQUEST ).header( "exception", e.getMessage() ).build();
        }
    }


    @Override
    public Response unregisterTemplate( final String templateName ) {
        try {

            templateRegistryManager.unregisterTemplate( templateName );

            return Response.status( Response.Status.OK ).build();
        }
        catch ( RegistryException | RuntimeException e ) {
            return Response.status( Response.Status.BAD_REQUEST ).header( "exception", e.getMessage() ).build();
        }
    }


    @Override
    public String getTemplate( final String templateName, final String lxcArch ) {
        return gson.toJson( templateRegistryManager.getTemplate( templateName, lxcArch ) );
    }


    @Override
    public String getParentTemplate( final String childTemplateName ) {
        return gson.toJson( templateRegistryManager.getParentTemplate( childTemplateName ) );
    }


    @Override
    public String getParentTemplate( final String childTemplateName, final String lxcArch ) {
        return gson.toJson( templateRegistryManager.getParentTemplate( childTemplateName, lxcArch ) );
    }


    @Override
    public String getParentTemplates( final String childTemplateName ) {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistryManager.getParentTemplates( childTemplateName ) ) {
            parents.add( template.getTemplateName() );
        }
        return gson.toJson( parents );
    }


    @Override
    public String getParentTemplates( final String childTemplateName, final String lxcArch ) {
        List<String> parents = new ArrayList<>();
        for ( Template template : templateRegistryManager.getParentTemplates( childTemplateName, lxcArch ) ) {
            parents.add( template.getTemplateName() );
        }
        return gson.toJson( parents );
    }


    @Override
    public String getChildTemplates( final String parentTemplateName ) {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistryManager.getChildTemplates( parentTemplateName ) ) {
            children.add( template.getTemplateName() );
        }
        return gson.toJson( children );
    }


    @Override
    public String getChildTemplates( final String parentTemplateName, final String lxcArch ) {
        List<String> children = new ArrayList<>();
        for ( Template template : templateRegistryManager.getChildTemplates( parentTemplateName, lxcArch ) ) {
            children.add( template.getTemplateName() );
        }
        return gson.toJson( children );
    }


    @Override
    public String getTemplateTree() {
        TemplateTree tree = templateRegistryManager.getTemplateTree();
        List<Template> uberTemplates = tree.getRootTemplates();
        if ( uberTemplates != null ) {
            for ( Template template : uberTemplates ) {
                addChildren( tree, template );
            }
        }
        return gson.toJson( uberTemplates );
    }


    @Override
    public String isTemplateInUse( final String templateName ) {
        try {
            return JsonUtil.toJson( "RESULT", templateRegistryManager.isTemplateInUse( templateName ) );
        }
        catch ( RegistryException e ) {
            return JsonUtil.toJson( "ERROR", e.getMessage() );
        }
    }


    @Override
    public Response setTemplateInUse( final String faiHostname, final String templateName, final String isInUse ) {
        try {

            templateRegistryManager.updateTemplateUsage( faiHostname, templateName, Boolean.parseBoolean( isInUse ) );

            return Response.status( Response.Status.OK ).build();
        }
        catch ( RegistryException e ) {
            return Response.status( Response.Status.BAD_REQUEST ).header( "exception", e.getMessage() ).build();
        }
    }


    @Override
    public String listTemplates() {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistryManager.getAllTemplates() ) {
            templates.add( template.getTemplateName() );
        }
        return gson.toJson( templates );
    }


    @Override
    public String listTemplates( final String lxcArch ) {
        List<String> templates = new ArrayList<>();
        for ( Template template : templateRegistryManager.getAllTemplates( lxcArch ) ) {
            templates.add( template.getTemplateName() );
        }
        return gson.toJson( templates );
    }


    @Override
    public String listTemplatesPlain() {
        return listTemplatesPlain( Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public String listTemplatesPlain( final String lxcArch ) {
        StringBuilder output = new StringBuilder();
        List<Template> templates = templateRegistryManager.getAllTemplates( lxcArch );

        for ( final Template template : templates ) {
            output.append( template.getTemplateName() ).append( TEMPLATE_PARENT_DELIMITER ).append(
                    Strings.isNullOrEmpty( template.getParentTemplateName() ) ? "" : template.getParentTemplateName() )
                  .append( TEMPLATES_DELIMITER );
        }

        return output.toString();
    }


    private void addChildren( TemplateTree tree, Template currentTemplate ) {
        List<Template> children = tree.getChildrenTemplates( currentTemplate );
        if ( !( children == null || children.isEmpty() ) ) {
            currentTemplate.addChildren( children );
            for ( Template child : children ) {
                addChildren( tree, child );
            }
        }
    }
}
