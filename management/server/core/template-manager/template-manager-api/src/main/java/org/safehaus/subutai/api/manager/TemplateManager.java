/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


/**
 */
public interface TemplateManager {

    public void getTemplatesInfo();

    public void getInstancesInfo();

    public void createNewTemplate();

    public void commitTemplate();

    public void destoryTemplate();

    public void getTsarFilesInfo();

    public void getTsarFileDescriptor();

    public void deleteTsarFile();

    public void pushTsarFileAsDebPackageIntoRepository();



}
