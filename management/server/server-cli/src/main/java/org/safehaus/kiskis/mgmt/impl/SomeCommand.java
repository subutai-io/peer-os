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

    private SomeApi sa;

    public void setSa(SomeApi sa) {
        this.sa = sa;
    }

    @Override
    protected Object doExecute() throws Exception {
        System.out.println("Executqqing some command");
        String hello = sa.sayHello("Bahadyr");
        System.out.println(hello);

        return null;

    }
}
