/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.templateregistry;


/**
 */
public interface TemplateRegistryManager {

    public void getTemplates();

    public void getInstances();

    public void createNewTemplate();

    public void commitTemplate();

    public void destoryTemplate();

    public void getTsarFilesInfo();

    public void getTsarFileDescriptor();

    public void deleteTsarFile();

    public void pushTsarFileAsDebPackageIntoRepository();



}
