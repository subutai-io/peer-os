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
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;

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
//    private static SomeApi someApi;
//    
//    static {
//        someApi = ServiceLocator.getService(SomeApi.class);
//    }
    
    public SomeCommand() {
        System.out.println("constructor");
    }

    @Override
    protected Object doExecute() throws Exception {
        System.out.println("Executqqing some command");
//        SomeApi someApi = ServiceLocator.getService(SomeApi.class);
        String hello = someApi.sayHello("Bahadyr");
        System.out.println(hello);

        return null;
    }
}
