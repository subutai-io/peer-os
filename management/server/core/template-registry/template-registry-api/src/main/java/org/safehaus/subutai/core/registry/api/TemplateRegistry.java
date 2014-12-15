/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.registry.api;


import java.util.List;
import java.util.Set;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.git.api.GitChangedFile;


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
     * @param lxcArch - lxc architecture
     * @param md5sum - lxc md5sum
     * @param templateVersion - lxc version
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName, String lxcArch, String md5sum, String templateVersion );

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
     * @param lxcArch - lxc architecture
     *
     * @return - list of {@code Template}
     */
    public List<Template> getChildTemplates( String parentTemplateName, String lxcArch );

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
     * Returns templates in genealogical hierarchy
     *
     * @return - {@code TemplateTree}
     */
    public TemplateTree getTemplateTree();

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
     * Update template usage on FAI servers
     *
     * @param faiHostname - hostname of FAI
     * @param templateName - target template
     * @param inUse - true - template is in use, false - template is out of use
     */
    public boolean updateTemplateUsage( String faiHostname, String templateName, boolean inUse )
            throws RegistryException;


    /**
     * Indicates of template is in use on any of FAI servers
     *
     * @return true - in use, false - not in use
     */
    public boolean isTemplateInUse( String templateName ) throws RegistryException;


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
}
