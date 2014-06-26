/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.templateregistry;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an implementation of TemplateRegistryManager
 */
public class TemplateRegistryManagerImpl implements TemplateRegistryManager {

    private final TemplateDAO templateDAO;


    public TemplateRegistryManagerImpl( final DbManager dbManager ) {
        Preconditions.checkNotNull( dbManager, "DB Manager is null" );
        templateDAO = new TemplateDAO( dbManager );
    }


    @Override
    public void registerTemplate( final String configFile, final String packagesFile ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( configFile ), "Config file contents is null or empty" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( packagesFile ), "Packages file contents is null or empty" );

        Template template = parseTemplate( configFile, packagesFile );

        //save template to storage
        if ( !templateDAO.saveTemplate( template ) ) {
            throw new RuntimeException( String.format( "Error registering template %s", template.getTemplateName() ) );
        }
    }


    //@todo parse packages manifest to create packages list
    private Template parseTemplate( String configFile, String packagesFile ) {
        Properties properties = new Properties();
        try {
            properties.load( new ByteArrayInputStream( configFile.getBytes() ) );
            String lxcUtsname = properties.getProperty( "lxc.utsname" );
            String lxcArch = properties.getProperty( "lxc.arch" );
            String subutaiConfigPath = properties.getProperty( "subutai.config.path" );
            String subutaiAppdataPath = properties.getProperty( "subutai.appdata.path" );
            String subutaiParent = properties.getProperty( "subutai.parent" );
            String subutaiGitBranch = properties.getProperty( "subutai.git.branch" );
            String subutaiGitUuid = properties.getProperty( "subutai.git.uuid" );

            return new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiAppdataPath, subutaiParent,
                    subutaiGitBranch, subutaiGitUuid, packagesFile );
        }
        catch ( IOException e ) {
            throw new RuntimeException( String.format( "Error parsing template configuration %s", e ) );
        }
    }


    @Override
    public void unregisterTemplate( final String templateName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Template name is null or empty" );
        //delete template from storage
        Template template = getTemplate( templateName );
        if ( template != null ) {
            templateDAO.removeTemplate( template );
        }
        else {
            throw new RuntimeException( String.format( "Template %s not found", templateName ) );
        }
    }


    @Override
    public Template getTemplate( final String templateName ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Template name is null or empty" );
        //retrieve template from storage
        return templateDAO.getTemplateByName( templateName );
    }


    @Override
    public List<Template> getTemplatesByParent( final String parentTemplateName ) {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( parentTemplateName ), "Parent template name is null or empty" );
        //retrieve child templates from storage
        return templateDAO.getTemplatesByParentName( parentTemplateName );
    }


    @Override
    public Template getParentTemplate( final String childTemplateName ) {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( childTemplateName ), "Child template name is null or empty" );
        //retrieve parent template from storage
        return templateDAO.getTemplateByChildName( childTemplateName );
    }


    @Override
    public TemplateTree getTemplateTree() {
        //retrieve all templates and fill template tree
        TemplateTree templateTree = new TemplateTree();
        //add master template
        templateTree.addTemplate( Template.getMasterTemplate() );
        List<Template> allTemplates = templateDAO.getAllTemplates();
        for ( Template template : allTemplates ) {
            templateTree.addTemplate( template );
        }
        return templateTree;
    }
}
