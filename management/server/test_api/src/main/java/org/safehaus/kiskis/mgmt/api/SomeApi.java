/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.api;

/**
 *
 * @author bahadyr
 */
public interface SomeApi {

    public String sayHello(String name);

    public boolean install(String program);

    public boolean start(String serviceName);

    public boolean stop(String serviceName);

    public boolean status(String serviceName);

    public boolean purge(String serviceName);
    
    public boolean runCommand(String program);
    
}
