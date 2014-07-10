package org.safehaus.subutai.rest.templateregistry;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 */

public class RestServiceImpl implements RestService {

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
    public String getParentTemplate( final String childName ) {
        return gson.toJson( templateRegistryManager.getParentTemplate( childName ) );
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
    public String getChildTemplates( final String parentTemplateName ) {
        return gson.toJson( templateRegistryManager.getChildTemplates( parentTemplateName ) );
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
