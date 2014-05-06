package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.hive.query.Config;
import org.safehaus.kiskis.mgmt.api.hive.query.HiveQuery;


/**
 * Displays the last log entries
 */
@Command(scope = "hivequery", name = "save-query", description = "Saves hive query in db")
public class SaveQueryCommand extends OsgiCommandSupport {
    private HiveQuery manager;

    public HiveQuery getManager() {
        return manager;
    }

    public void setManager(HiveQuery manager) {
        this.manager = manager;
    }

    protected Object doExecute() {
        Config query = new Config();
        query.setName("Sample select");
        query.setQuery("select * from table");
        query.setDescription("Sample description");

        manager.save(query);

        return null;
    }
}
