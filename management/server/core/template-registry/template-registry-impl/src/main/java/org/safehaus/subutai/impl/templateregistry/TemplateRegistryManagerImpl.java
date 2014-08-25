/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.templateregistry;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.safehaus.subutai.api.dbmanager.DBException;
import org.safehaus.subutai.api.dbmanager.DbManager;
import org.safehaus.subutai.api.templateregistry.RegistryException;
import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;
import org.safehaus.subutai.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


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
    public void registerTemplate( final String configFile, final String packagesFile ) throws RegistryException {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( configFile ), "Config file contents is null or empty" );
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( packagesFile ), "Packages file contents is null or empty" );

        Template template = parseTemplate( configFile, packagesFile );

        //save template to storage
        try {

            templateDAO.saveTemplate( template );
        }
        catch ( DBException e ) {
            throw new RegistryException(
                    String.format( "Error saving template %s, %s", template.getTemplateName(), e.getMessage() ) );
        }
    }


    private Template parseTemplate( String configFile, String packagesFile ) throws RegistryException {
        Properties properties = new Properties();
        try {
            properties.load( new ByteArrayInputStream( configFile.getBytes() ) );
            String lxcUtsname = properties.getProperty( "lxc.utsname" );
            String lxcArch = properties.getProperty( "lxc.arch" );
            String subutaiConfigPath = properties.getProperty( "subutai.config.path" );
            String subutaiParent = properties.getProperty( "subutai.parent" );
            String subutaiGitBranch = properties.getProperty( "subutai.git.branch" );
            String subutaiGitUuid = properties.getProperty( "subutai.git.uuid" );

            Template template = new Template( lxcArch, lxcUtsname, subutaiConfigPath, subutaiParent, subutaiGitBranch,
                    subutaiGitUuid, packagesFile );

            if ( template.getParentTemplateName() == null ) {

                template.setProducts( getPackagesDiff( null, template ) );
            }
            else {
                Template parentTemplate = getTemplate( template.getParentTemplateName() );

                template.setProducts( getPackagesDiff( parentTemplate, template ) );
            }


            return template;
        }
        catch ( Throwable e ) {
            throw new RegistryException( String.format( "Error parsing template configuration %s", e ) );
        }
    }


    private Set<String> getPackagesDiff( Template parent, Template child ) {
        if ( parent == null ) {
            return extractPackageNames( Sets.newHashSet( child.getPackagesManifest().split( "\n" ) ) );
        }
        else {
            return getPackagesDiff( Sets.newHashSet( parent.getPackagesManifest().split( "\n" ) ),
                    Sets.newHashSet( child.getPackagesManifest().split( "\n" ) ) );
        }
    }


    private Set<String> extractPackageNames( Set<String> ls ) {
        Pattern p = Pattern.compile( String.format( "(%s.+?)\\s", Common.PACKAGE_PREFIX ) );
        Matcher m = p.matcher( "" );

        SortedSet<String> res = new TreeSet<>();
        for ( String s : ls ) {
            if ( m.reset( s ).find() ) {
                res.add( m.group( 1 ) );
            }
        }

        return res;
    }


    private Set<String> getPackagesDiff( Set<String> parentPackages, Set<String> childPackages ) {
        Set<String> p = extractPackageNames( parentPackages );
        Set<String> c = extractPackageNames( childPackages );
        c.removeAll( p );

        return c;
    }


    @Override
    public void unregisterTemplate( final String templateName ) throws RegistryException {
        unregisterTemplate( templateName, Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public void unregisterTemplate( final String templateName, final String lxcArch ) throws RegistryException {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "LxcArch is null or empty" );

        //find template
        Template template = getTemplate( templateName, lxcArch );

        if ( template != null ) {
            //check if template has children
            List<Template> children = getChildTemplates( templateName, lxcArch );
            if ( !children.isEmpty() ) {
                throw new RegistryException(
                        String.format( "Can no delete template %s from registry because it has children",
                                templateName ) );
            }

            //delete template from storage
            try {
                templateDAO.removeTemplate( template );
            }
            catch ( DBException e ) {
                throw new RegistryException(
                        String.format( "Error deleting template %s, %s", templateName, e.getMessage() ) );
            }
        }
        else {
            throw new RegistryException( String.format( "Template %s not found", templateName ) );
        }
    }


    @Override
    public Template getTemplate( final String templateName ) {
        return getTemplate( templateName, Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public Template getTemplate( final String templateName, String lxcArch ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ), "Template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "LxcArch is null or empty" );
        //retrieve template from storage

        try {
            return templateDAO.getTemplateByName( templateName, lxcArch );
        }
        catch ( DBException e ) {
            return null;
        }
    }


    @Override
    public List<Template> getChildTemplates( final String parentTemplateName ) {
        return getChildTemplates( parentTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public List<Template> getChildTemplates( final String parentTemplateName, String lxcArch ) {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( parentTemplateName ), "Parent template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "LxcArch is null or empty" );
        //retrieve child templates from storage
        try {
            return templateDAO.geChildTemplates( parentTemplateName, lxcArch );
        }
        catch ( DBException e ) {
            return Collections.emptyList();
        }
    }


    @Override
    public Template getParentTemplate( final String childTemplateName ) {
        return getParentTemplate( childTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public Template getParentTemplate( final String childTemplateName, final String lxcArch ) {
        Preconditions
                .checkArgument( !Strings.isNullOrEmpty( childTemplateName ), "Child template name is null or empty" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( lxcArch ), "LxcArch is null or empty" );
        //retrieve parent template from storage
        Template child = getTemplate( childTemplateName, lxcArch );

        if ( child != null ) {
            if ( !Strings.isNullOrEmpty( child.getParentTemplateName() ) ) {
                return getTemplate( child.getParentTemplateName(), child.getLxcArch() );
            }
        }

        return null;
    }


    @Override
    public TemplateTree getTemplateTree() {
        //retrieve all templates and fill template tree
        TemplateTree templateTree = new TemplateTree();
        try {
            List<Template> allTemplates = templateDAO.getAllTemplates();
            for ( Template template : allTemplates ) {
                templateTree.addTemplate( template );
            }
        }
        catch ( DBException ignored ) {
        }
        return templateTree;
    }


    @Override
    public List<Template> getParentTemplates( String childTemplateName ) {
        return getParentTemplates( childTemplateName, Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public List<Template> getParentTemplates( final String childTemplateName, final String lxcArch ) {
        List<Template> parents = new ArrayList<>();

        Template parent = getParentTemplate( childTemplateName, lxcArch );
        while ( parent != null ) {
            parents.add( parent );
            parent = getParentTemplate( parent.getTemplateName(), lxcArch );
        }
        Collections.reverse( parents );
        return parents;
    }


    @Override
    public List<Template> getAllTemplates() {
        return getAllTemplates( Common.DEFAULT_LXC_ARCH );
    }


    @Override
    public List<Template> getAllTemplates( final String lxcArch ) {
        try {
            List<Template> allTemplates = templateDAO.getAllTemplates();
            List<Template> result = new ArrayList<>();
            for ( Template template : allTemplates ) {
                if ( template.getLxcArch().equalsIgnoreCase( lxcArch ) ) {
                    result.add( template );
                }
            }
            return result;
        }
        catch ( DBException e ) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }
}
