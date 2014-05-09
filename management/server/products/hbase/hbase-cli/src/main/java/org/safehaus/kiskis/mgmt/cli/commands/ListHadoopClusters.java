package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

/**
 * Created by bahadyr on 5/8/14.
 */
@Command(scope = "hbase", name = "list-hadoop-clusters", description = "Shows the list of installed Hadoop clusters")
public class ListHadoopClusters extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {

        return null;
    }
}
