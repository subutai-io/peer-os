/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.templateregistry;


import java.util.List;


/**
 */
public interface TemplateRegistryManager {

    /**
     * Adds template entry to registry
     *
     * @param configFile - template configuration file contents
     * @param packagesFile - template packages manifest file contents
     */
    public void registerTemplate( String configFile, String packagesFile );

    /**
     * Removes template entry from registry
     *
     * @param templateName - name of template to remove
     */
    public void unregisterTemplate( String templateName );

    /**
     * Returns template by name
     *
     * @param templateName - name of template
     *
     * @return - {@code Template}
     */
    public Template getTemplate( String templateName );

    /**
     * Returns templates belonging to this parent
     *
     * @param parentTemplateName - parent template name
     *
     * @return - list of {@code Template}
     */
    public List<Template> getTemplatesByParent( String parentTemplateName );

    /**
     * Returns parent template
     *
     * @param childTemplateName - child template name
     *
     * @return -  {@code Template}
     */
    public Template getParentTemplate( String childTemplateName );

    /**
     * Returns templates in genealogical hierarchy
     *
     * @return - {@code TemplateTree}
     */
    public TemplateTree getTemplateTree();
}
