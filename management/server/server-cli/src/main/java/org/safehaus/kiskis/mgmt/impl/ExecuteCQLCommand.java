/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;

/**
 *
 * @author bahadyr
 */
@Command(
        scope = "subutai",
        name = "execute_cql",
        description = "Executes CQL query"
)
public class ExecuteCQLCommand extends OsgiCommandSupport {

    private DbManager dbManager;

    @Argument(index = 0, name = "arg",
            description = "The command argument",
            required = false, multiValued = false)
    String arg = null;

    public void setDbManager(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    protected Object doExecute() throws Exception {
        dbManager.executeCqlQuery(arg);
        return null;

    }
}
