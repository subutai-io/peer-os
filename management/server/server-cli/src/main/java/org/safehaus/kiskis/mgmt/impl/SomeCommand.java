/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.SomeApi;

/**
 *
 * @author bahadyr
 */
@Command(scope = "subutai", name = "somecommand", description = "mydescription")
public class SomeCommand extends OsgiCommandSupport {

    private SomeApi someApi;
//
    public void setSomeApi(SomeApi someApi) {
        this.someApi = someApi;
    }
    
    public SomeCommand() {
        System.out.println("constructor");
    }

    @Override
    protected Object doExecute() throws Exception {
        String hello = someApi.sayHello("say Hello method executed");
        System.out.println(hello);
        someApi.writeLog("log test hello people");
        return null;
    }
}
