/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.templateregistry;


import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Logger;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;

import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is an implementation of TemplateRegistryManager
 */
public class TemplateRegistryManagerImpl implements TemplateRegistryManager {
    private static final Logger LOG = Logger.getLogger( TemplateRegistryManagerImpl.class.getName() );


    @Override
    public void registerTemplate( final String configFile, final String packagesFile ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( configFile ), "Config file contents is null or empty" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( packagesFile ), "Packages file contents is null or empty" );

        Template template = parseTemplate( configFile, packagesFile );

        //save template to storage

    }


    //@todo parse packages manifest to create packages list
    private Template parseTemplate( String configFile, String packagesFile ) {
        AbstractFileConfiguration configuration = new PropertiesConfiguration();
        try {
            configuration.load( new ByteArrayInputStream( configFile.getBytes() ) );
            String lxcUtsname = ( String ) configuration.getProperty( "lxc.utsname" );
            String lxcArch = ( String ) configuration.getProperty( "lxc.arch" );
            String subutaiConfigPath = ( String ) configuration.getProperty( "subutai.config.path" );
            String subutaiAppdataPath = ( String ) configuration.getProperty( "subutai.appdata.path" );
            String subutaiParent = ( String ) configuration.getProperty( "subutai.parent" );
            String subutaiGitBranch = ( String ) configuration.getProperty( "subutai.git.branch" );
            String subutaiGitUuid = ( String ) configuration.getProperty( "subutai.git.uuid" );

            return new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiAppdataPath, subutaiParent,
                    subutaiGitBranch, subutaiGitUuid, packagesFile );
        }
        catch ( ConfigurationException e ) {
            throw new RuntimeException( String.format( "Error parsing template %s", e ) );
        }
    }


    @Override
    public void unregisterTemplate( final String templateName ) {
        //delete template from storage
    }


    @Override
    public Template getTemplate( final String templateName ) {
        //retrieve template from storage
        return null;
    }


    @Override
    public List<Template> getTemplatesByParent( final String parentTemplateName ) {
        //retrieve child templates from storage
        return null;
    }


    @Override
    public Template getParentTemplate( final String childTemplateName ) {
        //retrieve parent template from storage
        return null;
    }


    @Override
    public TemplateTree getTemplateTree() {
        //retrieve all templates and fill template tree
        return null;
    }
}
