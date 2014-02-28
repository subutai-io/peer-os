/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 *
 * @author bahadyr
 */
@Command(scope = "LAK", name = "commandname", description = "This  is a sample custom command.")
public class CommandName extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        System.out.println("doExecute run");
        return null;
    }

}
