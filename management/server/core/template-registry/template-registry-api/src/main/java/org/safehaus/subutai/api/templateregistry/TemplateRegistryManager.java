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

    public void registerTemplate( String configFile, String packagesFile );

    public void unregisterTemplate( String templateName );

    public Template getTemplate( String templateName );

    public List<Template> getTemplatesByParent( String parentTemplateName );

    public Template getParentTemplate( String childTemplateName );


}
