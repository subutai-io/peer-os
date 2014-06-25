/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.templateregistry;


import java.util.List;

import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.api.templateregistry.TemplateTree;


/**
 * This is an implementation of TemplateRegistryManager
 */
public class TemplateRegistryManagerImpl implements TemplateRegistryManager {


    @Override
    public void registerTemplate( final String configFile, final String packagesFile ) {

    }


    @Override
    public void unregisterTemplate( final String templateName ) {

    }


    @Override
    public Template getTemplate( final String templateName ) {
        return null;
    }


    @Override
    public List<Template> getTemplatesByParent( final String parentTemplateName ) {
        return null;
    }


    @Override
    public Template getParentTemplate( final String childTemplateName ) {
        return null;
    }


    @Override
    public TemplateTree getTemplateTree() {
        return null;
    }
}
