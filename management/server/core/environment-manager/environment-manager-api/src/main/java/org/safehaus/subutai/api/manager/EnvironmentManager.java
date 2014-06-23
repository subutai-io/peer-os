/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.api.manager;


import java.util.Set;


/**
 */
public interface EnvironmentManager {


    public boolean buildEnvironment(String blueprintStr);

    public Set<Environment> getEnvironments();

    public Environment getEnvironmentInfo(String environmentName);

    public boolean destroyEnvironment(String environmentName);



}
