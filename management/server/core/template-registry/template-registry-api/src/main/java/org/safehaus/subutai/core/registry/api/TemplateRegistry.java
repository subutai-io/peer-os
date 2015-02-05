/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.registry.api;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.datatypes.TemplateVersion;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.git.api.GitChangedFile;

import org.apache.commons.lang3.tuple.Pair;


/**
 * Class to work with templates registry, templates metadata
 */
public interface TemplateRegistry
{

    /**
     * Adds template entry to registry
     *
     * @param configFile - template configuration file contents
     * @param packagesFile - template packages manifest file contents
     */
    public boolean registerTemplate( String configFile, String packagesFile, String md5sum ) throws RegistryException;


    /**
     * Returns packages difference for current template with its parent template.
     */
    public Set<String> getPackagesDiff( Template template );

    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     */
    public boolean unregisterTemplate( String templateName ) throws RegistryException;

    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     * @param lxcArch - lxc architecture
     */
    public void unregisterTemplate( String templateName, String lxcArch ) throws RegistryException;


    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     * @param templateVersion - lxc architecture
     * @param lxcArch - lxc architecture
     */
    public boolean unregisterTemplate( String templateName, TemplateVersion templateVersion, String lxcArch )
            throws RegistryException;


    /**
     * Returns template by name
     *
     * @param templateName - name of template
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName );

    /**
     * Returns template by name
     *
     * @param templateName - name of template
     * @param lxcArch - lxc architecture
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName, String lxcArch );


    /**
     * Returns template by name
     *
     * @param templateName - name of template
     * @param templateVersion - template version
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName, TemplateVersion templateVersion );


    /**
     * Returns template by name and version
     *
     * @param templateName - name of template
     * @param templateVersion - template version
     * @param lxcArch - lxc architecture
     *
     * @return - {@code Template}
     */
    public Template getTemplate( final String templateName, TemplateVersion templateVersion, String lxcArch );


    /**
     * Returns template by name
     *
     * @param templateName - name of template
     * @param lxcArch - lxc architecture
     * @param md5sum - lxc md5sum
     * @param templateVersion - lxc version
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName, String lxcArch, String md5sum, TemplateVersion templateVersion );

    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName );


    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName, TemplateVersion templateVersion );

    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch );


    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName, TemplateVersion templateVersion,
                                             String lxcArch );


    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName );

    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     * @param lxcArch - lxc architecture
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName, String lxcArch );


    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     * @param templateVersion - child template version
     * @param lxcArch - lxc architecture
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName, TemplateVersion templateVersion, String lxcArch );


    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName );


    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName, String lxcArch );


    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     * @param templateVersion - template version
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName, TemplateVersion templateVersion );


    /**
     * Returns list of all parent templates starting from MASTER
     *
     * @param childTemplateName - name of template whose parents to return
     * @param templateVersion - version of template whose parents to return
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getParentTemplates( String childTemplateName, TemplateVersion templateVersion,
                                              String lxcArch );


    /**
     * Update template usage on FAI servers
     *
     * @param faiHostname - hostname of FAI
     * @param templateName - target template
     * @param inUse - true - template is in use, false - template is out of use
     */
    public boolean updateTemplateUsage( String faiHostname, String templateName, boolean inUse )
            throws RegistryException;


    /**
     * Update template usage on FAI servers
     *
     * @param faiHostname - hostname of FAI
     * @param templateName - target template
     * @param templateVersion - target template version
     * @param inUse - true - template is in use, false - template is out of use
     */
    public boolean updateTemplateUsage( String faiHostname, String templateName, TemplateVersion templateVersion,
                                        boolean inUse ) throws RegistryException;


    /**
     * Indicates of template is in use on any of FAI servers
     *
     * @return true - in use, false - not in use
     */
    public boolean isTemplateInUse( String templateName ) throws RegistryException;


    /**
     * Indicates of template is in use on any of FAI servers
     *
     * @return true - in use, false - not in use
     */
    public boolean isTemplateInUse( String templateName, TemplateVersion templateVersion ) throws RegistryException;


    /**
     * Returns list of all templates
     *
     * @return -list of {@code Template}
     */
    public List<Template> getAllTemplates();

    /**
     * Returns list of all templates of specified architecture
     *
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getAllTemplates( String lxcArch );


    /**
     * Adds template entry to registry
     *
     * @param template - template
     */
    public boolean registerTemplate( Template template ) throws RegistryException;

    /**
     * Returns list of GitChangedFile between two templates
     */
    public List<GitChangedFile> getChangedFiles( Template template ) throws RegistryException;


    /**
     * Returns template download token, with which a template package can be downloaded from registry REST endpoint
     * within the specified timeout
     *
     * @param timeout - timeout of template download token in seconds
     *
     * @return - template download token
     */
    public String getTemplateDownloadToken( int timeout );

    /**
     * Returns true if token is valid or false if token is expired or does not exist
     *
     * @param token -template download token
     *
     * @return - {@code Boolean}
     */
    public boolean checkTemplateDownloadToken( String token );

    public Pair<String, String> getChangedFileVersions( String branchA, String branchB, GitChangedFile file );


    public List<Template> getTemplateTree();
}
